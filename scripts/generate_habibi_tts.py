#!/usr/bin/env python3
"""Generate all Sawti voice packs with the official Unified Habibi-TTS checkpoint.

Audio: neural_tts/<dialect>/<semantic_key>.mp3, mono/24000 Hz/96 kbps.
Requires a valid Kotlin-exported app/build/speech_catalog.json and a legal
voice reference. Never calls edge-tts or imports Microsoft voice data.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import math
import os
import subprocess
import sys
import time
import warnings
from importlib.metadata import version
import random
import re
import tempfile
import unicodedata
from importlib.resources import files
from pathlib import Path

from habibi_contract import DIALECT_IDS, EXPECTED_CLIPS, load_catalog, file_sha256

DIACRITICS = re.compile(r"[\u0610-\u061A\u064B-\u065F\u0670\u06D6-\u06ED\u0640]")
SPACES = re.compile(r"\s+")
DIGITS = re.compile(r"\d", re.UNICODE)
PROVIDER = "Habibi-TTS"
MANIFEST_VERSION = 2
MODEL = "Unified"
MODEL_REVISION = "3ad11a152851245c002156526c5e1d0b12f2b9d5"
LICENSE = "CC-BY-NC-SA-4.0"
SEED = 0
NFE_STEP = 32
CFG_STRENGTH = 2.0
SWAY_SAMPLING_COEF = -1.0
SPEED_SHORT = 0.75
SPEED_NORMAL = 0.88
SAMPLE_RATE = 24000
BITRATE = "96k"
PIPELINE_VERSION = 4
REFERENCE_FALLBACK = "MSA.mp3"
DURATION_SCALE = 2.0
MIN_WORD_SECONDS = 1.2
MIN_PHRASE_SECONDS = 1.4
RECOVERY_STEP_SECONDS = 0.5
MAX_SYNTHESIS_ATTEMPTS = 3


class GeneratedAudioError(ValueError):
    """The model returned audio which failed the existing output quality gate."""

# Exact unvowelized transcript of upstream src/habibi_tts/assets/MSA.mp3.
REFERENCE_TEXT = ("كان اللعيب حاضرا في العديد من الأنشطة والفعاليات "
                  "المرتبطة بكأس العالم، مما سمح للجماهير بالتفاعل معه "
                  "والتقاط الصور التذكارية.")


def clean_for_habibi(text: str) -> str:
    if not isinstance(text, str):
        raise TypeError("Habibi text must be a string")
    cleaned = SPACES.sub(" ", DIACRITICS.sub("", unicodedata.normalize("NFC", text))).strip()
    if not cleaned:
        raise ValueError("Empty Habibi text after cleaning")
    if DIGITS.search(cleaned):
        raise ValueError(f"Numeric digits must be written as words: {cleaned!r}")
    return cleaned


def hash_string(value: str) -> str:
    return hashlib.sha256(value.encode("utf-8")).hexdigest()


def atomic_json(path: Path, obj: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.NamedTemporaryFile(
        mode="w", encoding="utf-8", dir=path.parent, prefix=".manifest-",
        suffix=".tmp", delete=False,
    ) as handle:
        json.dump(obj, handle, ensure_ascii=False, indent=2)
        handle.write("\n")
        temporary = Path(handle.name)
    temporary.replace(path)


def load_references(config_path: Path, dialect: str):
    """Read optional per-dialect pair; otherwise use upstream bundled MSA pair."""
    config = json.loads(config_path.read_text(encoding="utf-8"))
    if config.get("schema_version") != 1:
        raise ValueError(f"Reference config has invalid version: {config_path}")
    mapping = config.get("dialects", {})
    if set(mapping) != set(DIALECT_IDS):
        raise ValueError("ref_config.json must list all nine Sawti dialects")
    data = mapping[dialect]
    if not isinstance(data, dict):
        raise ValueError(f"Invalid voice reference for {dialect}")
    audio, words = data.get("ref_audio"), data.get("ref_text")
    if (audio is None) != (words is None):
        raise ValueError(f"{dialect}: ref_audio and ref_text must be paired")
    candidate = (config_path.parent / str(audio)).resolve() if audio is not None else None
    if candidate is None or not candidate.is_file():
        if candidate is not None:
            warnings.warn(f"{dialect}: missing custom reference {candidate}; using bundled MSA")
        bundled = files("habibi_tts").joinpath("assets").joinpath(REFERENCE_FALLBACK)
        if not bundled.is_file():
            raise FileNotFoundError("Upstream Habibi assets/MSA.mp3 missing")
        print(f"NOTICE: {dialect}: using official MSA reference. Accent and gender not guaranteed.")
        return str(bundled), clean_for_habibi(REFERENCE_TEXT), "official-MSA-fallback"
    words = clean_for_habibi(words)
    from pydub import AudioSegment
    duration = len(AudioSegment.from_file(str(candidate)))
    if not 4000 <= duration <= 8000:
        raise ValueError(f"{dialect}: custom reference must be 4-8 seconds; got {duration} ms")
    return str(candidate), words, "custom"


def set_seed():
    os.environ.setdefault("CUBLAS_WORKSPACE_CONFIG", ":4096:8")
    import numpy as np
    import torch
    random.seed(SEED)
    np.random.seed(SEED)
    torch.manual_seed(SEED)
    if torch.cuda.is_available():
        torch.cuda.manual_seed_all(SEED)
    if hasattr(torch.backends, "cudnn"):
        torch.backends.cudnn.deterministic = True
        torch.backends.cudnn.benchmark = False
    torch.use_deterministic_algorithms(True)


def build_model(device: str):
    """Same official model/vocab/vocoder/dialect tags as upstream infer_cli."""
    from huggingface_hub import hf_hub_download
    from f5_tts.infer.utils_infer import load_model, load_vocoder
    from hydra.utils import get_class
    from omegaconf import OmegaConf

    from importlib.resources import files as resources
    cfg = OmegaConf.load(str(resources("f5_tts").joinpath("configs/F5TTS_v1_Base.yaml")))
    backbone = get_class("f5_tts.model." + cfg.model.backbone)
    vocoder_type = cfg.model.mel_spec.mel_spec_type
    ckpt = hf_hub_download("SWivid/Habibi-TTS", "Unified/model_200000.safetensors",
                           revision=MODEL_REVISION)
    vocab = hf_hub_download("SWivid/Habibi-TTS", "Unified/vocab.txt", revision=MODEL_REVISION)
    model = load_model(backbone, cfg.model.arch, ckpt,
                       mel_spec_type=vocoder_type, vocab_file=vocab, device=device)
    vocoder = load_vocoder(vocoder_name=vocoder_type, is_local=False,
                           local_path="", device=device)
    return model, vocoder, vocoder_type


def speed_for_text(text: str) -> float:
    """Only short SINGLE words receive the slower rate; sentences retain .88."""
    letters = sum(ch.isalpha() for ch in text)
    return SPEED_SHORT if len(text.split()) == 1 and letters < 10 else SPEED_NORMAL


def synthesis_duration(ref_frames: int, ref_rate: int, ref_text: str,
                       text: str, speed: float) -> float:
    """Budget actual model frames for complete words, including short phrases.

    The upstream reference-byte ratio alone yielded 0.29-second family words
    and truncated requests. Allocate extra generation frames before sampling;
    changing playback speed or padding the encoded audio cannot restore speech.
    fix_duration includes BOTH reference and generation. The fractional frame
    prevents upstream's floating-point floor from removing a requested frame.
    """
    prompt = ref_text + " " if len(ref_text[-1].encode("utf-8")) == 1 else ref_text
    ref_len = math.ceil(ref_frames * SAMPLE_RATE / ref_rate) // 256
    estimated = ref_len / len(prompt.encode("utf-8")) * len(text.encode("utf-8")) / speed
    floor_seconds = MIN_WORD_SECONDS if len(text.split()) == 1 else MIN_PHRASE_SECONDS
    gen_len = math.ceil(max(estimated * DURATION_SCALE, floor_seconds * SAMPLE_RATE / 256))
    return (ref_len + gen_len + .01) * 256 / SAMPLE_RATE


def minimum_speech_seconds(text: str) -> float:
    """Conservative rejection floor, NOT a pronunciation/completeness verdict."""
    words = text.split()
    if len(words) == 1:
        return .3 if sum(ch.isalpha() for ch in text) < 3 else .5
    return .8 + .12 * (len(words) - 2)


def duration_policy() -> dict:
    return {"scale": DURATION_SCALE, "minimumWordSeconds": MIN_WORD_SECONDS,
            "minimumPhraseSeconds": MIN_PHRASE_SECONDS, "retryStepSeconds": RECOVERY_STEP_SECONDS}


def mp3_output(wave, rate: int, destination: Path, minimum_seconds: float = .15):
    import numpy as np
    import soundfile as sf
    from pydub import AudioSegment
    from pydub.silence import detect_nonsilent

    destination.parent.mkdir(parents=True, exist_ok=True)
    samples = np.asarray(wave, dtype=np.float32)
    if samples.ndim != 1 or not samples.size or not np.isfinite(samples).all():
        raise GeneratedAudioError(f"Invalid/non-finite mono model output: {destination}")
    peak = float(np.max(np.abs(samples)))
    if peak == 0:
        raise GeneratedAudioError(f"Silent model output: {destination}")
    # Normalize BEFORE integer PCM conversion so amplitudes >1 cannot clip.
    samples = samples * (10 ** (-1.5 / 20) / peak)
    with tempfile.TemporaryDirectory(prefix=".sawti-habibi-", dir=destination.parent) as temp:
        raw = Path(temp) / "raw.wav"
        encoded = Path(temp) / "render.mp3"
        sf.write(str(raw), samples, rate, subtype="PCM_16")
        with raw.open("rb") as stream:
            audio = AudioSegment.from_wav(stream).set_channels(1).set_frame_rate(SAMPLE_RATE)
        if len(audio) < 150 or audio.dBFS == float("-inf"):
            raise GeneratedAudioError(f"Empty or silent synthesized speech: {destination}")
        # Remove only outer silence, never gaps needed between syllables.
        ranges = detect_nonsilent(audio, min_silence_len=80,
                                  silence_thresh=max(-52.0, audio.dBFS - 30))
        if not ranges:
            raise GeneratedAudioError(f"Generated audio contains no audible speech: {destination}")
        begin = max(0, ranges[0][0] - 80)
        finish = min(len(audio), ranges[-1][1] + 100)
        audio = audio[begin:finish]
        if audio.max_dBFS > float("-inf"):
            audio = audio.apply_gain(-1.5 - audio.max_dBFS)
        if len(audio) < minimum_seconds * 1000:
            raise GeneratedAudioError(f"Audio unexpectedly truncated: {destination}")
        with encoded.open("wb+") as stream:
            audio.export(stream, format="mp3", bitrate=BITRATE,
                         parameters=["-ac", "1", "-ar", str(SAMPLE_RATE)])
        if encoded.stat().st_size < 1024:
            raise GeneratedAudioError(f"MP3 encode produced too few bytes: {destination}")
        encoded.replace(destination)
        return {"decodedDurationSeconds": len(audio) / 1000,
                "minimumDurationSeconds": minimum_seconds}


def synthesize_clip(infer, target: Path, text: str, first_duration: float) -> dict:
    """Retry rejected words AND requests with extra model time, without padding."""
    for attempt in range(MAX_SYNTHESIS_ATTEMPTS):
        extra_frames = math.ceil(attempt * RECOVERY_STEP_SECONDS * SAMPLE_RATE / 256)
        duration = first_duration + extra_frames * 256 / SAMPLE_RATE
        set_seed()
        wave, rate, _ = infer(duration)
        try:
            quality = mp3_output(wave, rate, target, minimum_seconds=minimum_speech_seconds(text))
        except GeneratedAudioError:
            if attempt + 1 == MAX_SYNTHESIS_ATTEMPTS:
                raise
            print(f"Rejected incomplete/invalid audio for {text!r}; retrying with more synthesis time", flush=True)
            continue
        return {"synthesisAttempts": attempt + 1, "fixedDurationSeconds": duration, **(quality or {})}
    raise RuntimeError("Inference ended without validated audio")


def generate_one_dialect(args, dialect, grouped, model, vocoder, mel_type):
    from f5_tts.infer.utils_infer import preprocess_ref_audio_text
    from habibi_tts.infer.utils_infer import infer_process
    from habibi_tts.model.utils import dialect_id_map

    import torchaudio

    tag = DIALECT_IDS[dialect]
    preview = getattr(args, "libyan_preview", False)
    model_texts = {"animal.cow": "بگرة"} if preview else {}
    if tag not in dialect_id_map:
        raise ValueError(f"Habibi Unified has no dialect tag: {tag}")
    path, reference_text, reference_type = load_references(args.ref_config, dialect)
    ref_sha = file_sha256(Path(path))
    reference_text_hash = hash_string(reference_text)
    prepared_ref_audio, prepared_ref_text = preprocess_ref_audio_text(path, reference_text)
    ref_wave, ref_rate = torchaudio.load(prepared_ref_audio)
    ref_frames = ref_wave.shape[-1]
    outdir = args.output / dialect.lower()
    manifest_path = outdir / "manifest.json"
    meta = {
        "version": MANIFEST_VERSION, "engine": PROVIDER,
        "pipelineVersion": PIPELINE_VERSION,
        "runtime": {name: version(name) for name in ("habibi-tts", "f5-tts", "torch", "torchaudio")},
        "model": MODEL, "modelRevision": MODEL_REVISION, "dialectId": tag, "license": LICENSE,
        "referenceSha256": ref_sha, "referenceTextSha256": reference_text_hash,
        "referenceType": reference_type, "seed": SEED, "nfeStep": NFE_STEP,
        "cfgStrength": CFG_STRENGTH, "swaySamplingCoef": SWAY_SAMPLING_COEF,
        "speedShort": SPEED_SHORT, "speedNormal": SPEED_NORMAL,
        "durationPolicy": duration_policy(),
        "sampleRate": SAMPLE_RATE, "bitrate": BITRATE,
    }
    if preview:
        meta["previewOnly"] = True
        meta["modelTextOverrides"] = model_texts
    previous = {}
    if manifest_path.is_file():
        try:
            previous = json.loads(manifest_path.read_text(encoding="utf-8"))
        except (ValueError, OSError):
            pass
    matches = all(previous.get(k) == v for k, v in meta.items())
    completed = previous.get("entries", {}) if matches else {}
    result = {}
    if args.skip_existing and isinstance(completed, dict):
        for key, text in grouped[dialect].items():
            target = outdir / (key + ".mp3")
            old = completed.get(key, {})
            if (isinstance(old, dict) and old.get("file") == key + ".mp3"
                    and old.get("speechText") == text and target.is_file()
                    and target.stat().st_size >= 1024
                    and old.get("sha256") == file_sha256(target)):
                result[key] = old
    # Keep ALL valid existing entries on every checkpoint, including keys later
    # in sorted order. A failed batch must never discard unvisited cache entries.
    atomic_json(manifest_path, {**meta, "entries": result})
    generated = 0
    for key, raw_text in sorted(grouped[dialect].items()):
        text = clean_for_habibi(raw_text)
        if text != raw_text:
            raise ValueError(f"Catalog output not clean at {dialect}/{key}")
        target = outdir / (key + ".mp3")
        reusable = key in result
        if not reusable and ((args.max_new > 0 and generated >= args.max_new)
                             or time.monotonic() >= args.deadline):
            continue
        if not reusable:
            model_text = model_texts.get(key, text)
            clip_speed = speed_for_text(model_text)
            duration = synthesis_duration(ref_frames, ref_rate, prepared_ref_text, model_text, clip_speed)
            def infer_at(fixed_duration):
                return infer_process(
                    prepared_ref_audio, prepared_ref_text, model_text,
                    model, vocoder, mel_spec_type=mel_type,
                    nfe_step=NFE_STEP, cfg_strength=CFG_STRENGTH,
                    sway_sampling_coef=SWAY_SAMPLING_COEF, speed=clip_speed,
                    device=args.device, dialect_id=dialect_id_map[tag],
                    fix_duration=fixed_duration,
                )
            timing = synthesize_clip(infer_at, target, text, duration)
            generated += 1
        if not reusable:
            result[key] = {
                "file": key + ".mp3", "speechText": text,
                "sha256": file_sha256(target), **timing,
            }
        # Persist progress atomically: --skip-existing works after interruption.
        atomic_json(manifest_path, {**meta, "entries": result})
        if not reusable:
            print(f"Completed {dialect}/{key}: {len(result)}/{len(grouped[dialect])} shard clips", flush=True)
            if getattr(args, "checkpoint_run_id", None) and generated % args.checkpoint_every == 0:
                subprocess.run([
                    sys.executable, str(Path(__file__).with_name("habibi_ci.py")), "save",
                    "--catalog", str(args.catalog), "--root", str(args.output),
                    "--dialect", dialect, "--run-id", args.checkpoint_run_id,
                    "--shard-index", str(args.shard_index), "--shard-count", str(args.shard_count),
                ], check=True)
    if (set(result) != set(grouped[dialect]) and args.max_new == 0
            and time.monotonic() < args.deadline):
        raise RuntimeError(f"Missing output from {dialect} voice pack")
    print(f"Batch: {dialect}/{tag} newly generated={generated}, verified={len(result)} of {len(grouped[dialect])}")


def shard_items(items: dict, index: int, count: int) -> dict:
    """Stable disjoint partitions; each semantic key keeps its own seed and path."""
    if count < 1 or not 0 <= index < count:
        raise ValueError("Require shard-count >=1 and 0 <= shard-index < shard-count")
    return {key: items[key] for n, key in enumerate(sorted(items)) if n % count == index}


def dialect_arg(value: str) -> str:
    code = value.strip().upper()
    aliases = {tag: name for name, tag in DIALECT_IDS.items()}
    aliases.update(dict(zip(
        ("AR-LY", "AR-TN", "AR-DZ", "AR-MA", "AR-EG", "AR-SY", "AR-IQ", "AR-SA", "AR-AE"),
        ("LIBYAN", "TUNISIAN", "ALGERIAN", "MOROCCAN", "EGYPTIAN", "SYRIAN", "IRAQI", "SAUDI", "STANDARD"),
    )))
    code = aliases.get(code, code)
    if code not in {"ALL", *DIALECT_IDS}:
        raise argparse.ArgumentTypeError("Use ALL, a Sawti dialect name, official Habibi tag or ar-XX locale")
    return code


def selected_items(grouped: dict, dialects: list[str], keys: list[str] | None) -> dict:
    """Filter only AFTER the complete canonical 2399-entry contract passes."""
    if not keys:
        return {d: grouped[d] for d in dialects}
    requested = set(keys)
    for dialect in dialects:
        missing = requested - grouped[dialect].keys()
        if missing:
            raise ValueError(f"Unknown semantic keys for {dialect}: {sorted(missing)}")
    return {d: {k: grouped[d][k] for k in sorted(requested)} for d in dialects}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--dialect", type=dialect_arg, default="ALL",
                        metavar="DIALECT_CODE")
    parser.add_argument("--skip-existing", action="store_true")
    parser.add_argument("--libyan-preview", action="store_true",
                        help="Allow the bundled reference in an isolated Libyan preview")
    parser.add_argument("--semantic-key", action="append", dest="semantic_keys",
                        help="Generate a named pilot key (repeatable); requires a separate --output")
    parser.add_argument("--shard-index", type=int, default=0)
    parser.add_argument("--shard-count", type=int, default=1)
    parser.add_argument("--checkpoint-run-id", help="GitHub run ID for durable CI checkpoints")
    parser.add_argument("--checkpoint-every", type=int, default=8)
    parser.add_argument("--max-new", type=int, default=0,
                        help="Maximum new clips per selected dialect per run; zero means ALL")
    parser.add_argument("--device", choices=["cuda", "cpu"], default="cpu")
    parser.add_argument("--catalog", type=Path, default=Path("app/build/speech_catalog.json"))
    parser.add_argument("--output", type=Path,
                        default=Path("app/src/main/assets/neural_tts"))
    parser.add_argument("--ref-config", type=Path,
                        default=Path("scripts/ref_voices/ref_config.json"))
    parser.add_argument("--max-seconds", type=int, default=0,
                        help="Soft run budget; stop between clips and save progress (0=unlimited)")
    args = parser.parse_args()
    if args.max_new < 0:
        parser.error("--max-new must be >= 0")
    if args.max_seconds < 0:
        parser.error("--max-seconds must be >= 0")
    args.deadline = time.monotonic() + args.max_seconds if args.max_seconds else math.inf
    if args.shard_count < 1 or not 0 <= args.shard_index < args.shard_count:
        parser.error("Require shard-count >=1 and 0 <= shard-index < shard-count")
    if args.checkpoint_every < 1 or (args.checkpoint_run_id is not None and
                                   not re.fullmatch(r"[0-9]+", args.checkpoint_run_id)):
        parser.error("checkpoint interval must be positive and run ID numeric")
    assets = Path("app/src/main/assets/neural_tts").resolve()
    production_output = args.output.resolve().is_relative_to(assets)
    if args.libyan_preview and (args.dialect != "LIBYAN" or production_output or args.checkpoint_run_id):
        parser.error("Libyan preview requires LIBYAN and an isolated output without Git checkpoints")
    if args.semantic_keys and (production_output or args.checkpoint_run_id):
        parser.error("Pilot keys require an output outside production assets and no corpus checkpoint")
    if production_output and not args.checkpoint_run_id and (args.max_new or args.max_seconds):
        parser.error("Budgeted batches require an output outside production assets")
    grouped = load_catalog(args.catalog)
    if sum(map(len, grouped.values())) != EXPECTED_CLIPS:
        raise RuntimeError("The exact 2399-card corpus must pass first")
    dialects = list(DIALECT_IDS) if args.dialect == "ALL" else [args.dialect]
    try:
        grouped = selected_items(grouped, dialects, args.semantic_keys)
    except ValueError as error:
        parser.error(str(error))
    if "LIBYAN" in dialects and not args.semantic_keys and not args.libyan_preview:
        _, _, reference_type = load_references(args.ref_config, "LIBYAN")
        if reference_type != "custom":
            parser.error("Libyan batches require a Libyan reference; use named pilot keys for MSA diagnostics")
    if args.device == "cuda":
        import torch
        if not torch.cuda.is_available():
            parser.error("CUDA requested but unavailable")
    set_seed()
    model, vocoder, mel_type = build_model(args.device)
    for dialect in dialects:
        if time.monotonic() >= args.deadline:
            break
        selected = {dialect: shard_items(grouped[dialect], args.shard_index, args.shard_count)}
        generate_one_dialect(args, dialect, selected, model, vocoder, mel_type)


if __name__ == "__main__":
    main()
