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
import os
import random
import re
import tempfile
import unicodedata
from importlib.resources import files
from pathlib import Path

from habibi_contract import DIALECT_IDS, EXPECTED_CLIPS, load_catalog, file_sha256

DIACRITICS = re.compile(r"[\u0610-\u061A\u064B-\u065F\u0670\u06D6-\u06ED\u0640]")
SPACES = re.compile(r"\s+")
DIGITS = re.compile(r"[0-9\u0660-\u0669\u06F0-\u06F9]")
PROVIDER = "Habibi-TTS"
MANIFEST_VERSION = 2
MODEL = "Unified"
LICENSE = "CC-BY-NC-SA-4.0"
SEED = 0
NFE_STEP = 32
CFG_STRENGTH = 2.0
SWAY_SAMPLING_COEF = -1.0
SPEED_SHORT = 0.75
SPEED_NORMAL = 0.88
SAMPLE_RATE = 24000
BITRATE = "96k"
REFERENCE_FALLBACK = "MSA.mp3"
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
    if audio is None:
        bundled = files("habibi_tts").joinpath("assets").joinpath(REFERENCE_FALLBACK)
        if not bundled.is_file():
            raise FileNotFoundError("Upstream Habibi assets/MSA.mp3 missing")
        print(f"NOTICE: {dialect}: using official MSA reference. Accent and gender not guaranteed.")
        return str(bundled), clean_for_habibi(REFERENCE_TEXT), "official-MSA-fallback"
    candidate = (config_path.parent / str(audio)).resolve()
    if not candidate.is_file():
        raise FileNotFoundError(f"{dialect}: reference audio missing at {candidate}")
    words = clean_for_habibi(words)
    from pydub import AudioSegment
    duration = len(AudioSegment.from_file(str(candidate)))
    if not 4000 <= duration <= 8000:
        raise ValueError(f"{dialect}: custom reference must be 4-8 seconds; got {duration} ms")
    return str(candidate), words, "custom"


def set_seed():
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
    torch.use_deterministic_algorithms(True, warn_only=True)


def build_model(device: str):
    """Same official model/vocab/vocoder/dialect tags as upstream infer_cli."""
    from cached_path import cached_path
    from f5_tts.infer.utils_infer import load_model, load_vocoder
    from hydra.utils import get_class
    from omegaconf import OmegaConf

    from importlib.resources import files as resources
    cfg = OmegaConf.load(str(resources("f5_tts").joinpath("configs/F5TTS_v1_Base.yaml")))
    backbone = get_class("f5_tts.model." + cfg.model.backbone)
    vocoder_type = cfg.model.mel_spec.mel_spec_type
    ckpt = str(cached_path("hf://SWivid/Habibi-TTS/Unified/model_200000.safetensors"))
    vocab = str(cached_path("hf://SWivid/Habibi-TTS/Unified/vocab.txt"))
    model = load_model(backbone, cfg.model.arch, ckpt,
                       mel_spec_type=vocoder_type, vocab_file=vocab, device=device)
    vocoder = load_vocoder(vocoder_name=vocoder_type, is_local=False,
                           local_path="", device=device)
    return model, vocoder, vocoder_type


def mp3_output(wave, rate: int, destination: Path):
    import soundfile as sf
    from pydub import AudioSegment
    from pydub.silence import detect_nonsilent

    destination.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="sawti-habibi-") as temp:
        raw = Path(temp) / "raw.wav"
        encoded = Path(temp) / "render.mp3"
        sf.write(str(raw), wave, rate, subtype="PCM_16")
        audio = AudioSegment.from_wav(str(raw)).set_channels(1).set_frame_rate(SAMPLE_RATE)
        if len(audio) < 150 or audio.dBFS == float("-inf"):
            raise ValueError(f"Empty or silent synthesized speech: {destination}")
        # Remove only outer silence, never gaps needed between syllables.
        ranges = detect_nonsilent(audio, min_silence_len=80,
                                  silence_thresh=max(-52.0, audio.dBFS - 30))
        if not ranges:
            raise ValueError(f"Generated audio contains no audible speech: {destination}")
        begin = max(0, ranges[0][0] - 80)
        finish = min(len(audio), ranges[-1][1] + 100)
        audio = audio[begin:finish]
        if audio.max_dBFS > float("-inf"):
            audio = audio.apply_gain(-1.5 - audio.max_dBFS)
        if len(audio) < 150:
            raise ValueError(f"Audio unexpectedly truncated: {destination}")
        audio.export(str(encoded), format="mp3", bitrate=BITRATE,
                     parameters=["-ac", "1", "-ar", str(SAMPLE_RATE)])
        if encoded.stat().st_size < 1024:
            raise ValueError(f"MP3 encode produced too few bytes: {destination}")
        encoded.replace(destination)


def generate_one_dialect(args, dialect, grouped, model, vocoder, mel_type):
    from f5_tts.infer.utils_infer import preprocess_ref_audio_text
    from habibi_tts.infer.utils_infer import infer_process
    from habibi_tts.model.utils import dialect_id_map

    tag = DIALECT_IDS[dialect]
    if tag not in dialect_id_map:
        raise ValueError(f"Habibi Unified has no dialect tag: {tag}")
    path, reference_text, reference_type = load_references(args.ref_config, dialect)
    ref_sha = file_sha256(Path(path))
    reference_text_hash = hash_string(reference_text)
    prepared_ref_audio, prepared_ref_text = preprocess_ref_audio_text(path, reference_text)
    outdir = args.output / dialect.lower()
    manifest_path = outdir / "manifest.json"
    meta = {
        "version": MANIFEST_VERSION, "engine": PROVIDER,
        "model": MODEL, "dialectId": tag, "license": LICENSE,
        "referenceSha256": ref_sha, "referenceTextSha256": reference_text_hash,
        "referenceType": reference_type, "seed": SEED, "nfeStep": NFE_STEP,
        "cfgStrength": CFG_STRENGTH, "swaySamplingCoef": SWAY_SAMPLING_COEF,
        "speedShort": SPEED_SHORT, "speedNormal": SPEED_NORMAL,
        "sampleRate": SAMPLE_RATE, "bitrate": BITRATE,
    }
    previous = {}
    if manifest_path.is_file():
        try:
            previous = json.loads(manifest_path.read_text(encoding="utf-8"))
        except (ValueError, OSError):
            pass
    matches = all(previous.get(k) == v for k, v in meta.items())
    completed = previous.get("entries", {}) if matches else {}
    result = {}
    for key, raw_text in sorted(grouped[dialect].items()):
        text = clean_for_habibi(raw_text)
        if text != raw_text:
            raise ValueError(f"Catalog output not clean at {dialect}/{key}")
        target = outdir / (key + ".mp3")
        old = completed.get(key, {})
        reusable = (args.skip_existing and old.get("file") == key + ".mp3"
                    and old.get("speechText") == text and
                    target.is_file() and target.stat().st_size >= 1024 and
                    old.get("sha256") == file_sha256(target))
        if not reusable:
            set_seed()  # repeated per key for reproducibility independent of batch order
            visible_count = sum(1 for ch in text if ch.isalpha())
            clip_speed = SPEED_SHORT if visible_count < 10 else SPEED_NORMAL
            # Upstream F5 infer_batch_process forcibly sets speed=0.3 for UTF-8
            # strings under 10 bytes. An Arabic comma extends punctuation
            # (not the spoken word) to keep the requested 0.75 speed effective.
            payload = text
            while len(payload.encode("utf-8")) < 10:
                payload += "،"
            wave, rate, _ = infer_process(
                prepared_ref_audio, prepared_ref_text, payload,
                model, vocoder, mel_spec_type=mel_type,
                nfe_step=NFE_STEP, cfg_strength=CFG_STRENGTH,
                sway_sampling_coef=SWAY_SAMPLING_COEF, speed=clip_speed,
                device=args.device, dialect_id=dialect_id_map[tag],
            )
            mp3_output(wave, rate, target)
        result[key] = {
            "file": key + ".mp3", "speechText": text,
            "sha256": file_sha256(target),
        }
        # Persist progress atomically: --skip-existing works after interruption.
        atomic_json(manifest_path, {**meta, "entries": result})
    if set(result) != set(grouped[dialect]):
        raise RuntimeError(f"Missing output from {dialect} voice pack")
    print(f"Done: {dialect}/{tag} with {len(result)} clips.")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--dialect", default="ALL",
                        choices=["ALL", *DIALECT_IDS.keys()])
    parser.add_argument("--skip-existing", action="store_true")
    parser.add_argument("--device", choices=["cuda", "cpu"], default="cpu")
    parser.add_argument("--catalog", type=Path, default=Path("app/build/speech_catalog.json"))
    parser.add_argument("--output", type=Path,
                        default=Path("app/src/main/assets/neural_tts"))
    parser.add_argument("--ref-config", type=Path,
                        default=Path("scripts/ref_voices/ref_config.json"))
    parser.add_argument("--ack-noncommercial", action="store_true",
                        help="Confirm authorized use of CC-BY-NC-SA-4.0 model/audio")
    args = parser.parse_args()
    # Licensing is part of correctness: the Unified weights are NC, even
    # though the Habibi-TTS source code is MIT licensed.
    if not args.ack_noncommercial:
        parser.error("Unified weights are CC-BY-NC-SA-4.0; explicit authorization required")
    grouped = load_catalog(args.catalog)
    if sum(map(len, grouped.values())) != EXPECTED_CLIPS:
        raise RuntimeError("The exact 2399-card corpus must pass first")
    if args.device == "cuda":
        import torch
        if not torch.cuda.is_available():
            parser.error("CUDA requested but unavailable")
    set_seed()
    model, vocoder, mel_type = build_model(args.device)
    dialects = list(DIALECT_IDS) if args.dialect == "ALL" else [args.dialect]
    for dialect in dialects:
        generate_one_dialect(args, dialect, grouped, model, vocoder, mel_type)


if __name__ == "__main__":
    main()
