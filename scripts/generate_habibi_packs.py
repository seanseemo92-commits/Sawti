#!/usr/bin/env python3
"""Generate one Habibi-TTS voice pack per Sawti dialect without Microsoft Edge.

Requires the upstream Habibi-TTS/F5-TTS stack, ffmpeg, and licensed speaker
reference audio plus precise transcripts. Uses unvowelized Kotlin catalog text.
"""
import argparse
import json
import os
import subprocess
import tempfile
from importlib.resources import files
from pathlib import Path

from habibi_contract import (
    DIALECT_IDS, MANIFEST_VERSION, PROVIDER, file_sha256, license_policy, load_catalog,
)


def load_inference(family, tag, device):
    from cached_path import cached_path
    from f5_tts.infer.utils_infer import load_model, load_vocoder
    from habibi_tts.model.utils import dialect_id_map
    from hydra.utils import get_class
    from omegaconf import OmegaConf

    config = OmegaConf.load(str(files("f5_tts").joinpath("configs/F5TTS_v1_Base.yaml")))
    backbone = get_class("f5_tts.model." + config.model.backbone)
    vocoder_type = config.model.mel_spec.mel_spec_type
    if family == "Unified":
        directory, step = "Unified", 200000
    else:
        directory = "Specialized/" + tag
        step = 200000 if tag == "MSA" else 100000
    prefix = "hf://SWivid/Habibi-TTS/" + directory
    model = load_model(
        backbone, config.model.arch,
        str(cached_path(prefix + f"/model_{step}.safetensors")),
        mel_spec_type=vocoder_type,
        vocab_file=str(cached_path(prefix + "/vocab.txt")), device=device,
    )
    vocoder = load_vocoder(vocoder_name=vocoder_type, is_local=False,
                           local_path="", device=device)
    return model, vocoder, vocoder_type, (
        dialect_id_map[tag] if family == "Unified" else None
    )


def encode_mp3(audio, rate, destination):
    import soundfile as sf
    destination.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="sawti-habibi-") as work:
        source = Path(work) / "source.wav"
        output = Path(work) / "render.mp3"
        sf.write(str(source), audio, rate, subtype="PCM_16")
        subprocess.run([
            "ffmpeg", "-nostdin", "-hide_banner", "-loglevel", "error", "-y",
            "-i", str(source), "-ac", "1", "-ar", "24000",
            "-codec:a", "libmp3lame", "-q:a", "4", str(output),
        ], check=True, timeout=180)
        if output.stat().st_size < 1024:
            raise RuntimeError("Unusable generated audio: " + str(destination))
        output.replace(destination)


def generate(args):
    from f5_tts.infer.utils_infer import preprocess_ref_audio_text
    from habibi_tts.infer.utils_infer import infer_process

    grouped = load_catalog(args.catalog)
    dialects = list(DIALECT_IDS) if args.dialects.upper() == "ALL" else [
        x.strip().upper() for x in args.dialects.split(",")
    ]
    if not dialects or len(dialects) != len(set(dialects)) or any(
        name not in DIALECT_IDS for name in dialects
    ):
        raise ValueError("Specify ALL or distinct Sawti dialect enum values")
    for dialect in dialects:
        tag, family, license_id = license_policy(dialect, args.model)
        if license_id != "Apache-2.0" and not args.ack_noncommercial:
            raise ValueError(
                f"{tag}: {family} is non-commercial ({license_id}). "
                "Review authorization before using --ack-noncommercial."
            )
        reference = args.references / (dialect.lower() + ".wav")
        transcript = args.references / (dialect.lower() + ".txt")
        if not reference.is_file() or not transcript.is_file():
            raise FileNotFoundError(f"Licensed reference audio/transcript missing: {dialect}")
        reference_text = transcript.read_text(encoding="utf-8").strip()
        if not reference_text:
            raise ValueError("Empty reference transcript: " + dialect)
        reference_hash = file_sha256(reference)
        output_dir = args.output / dialect.lower()
        manifest_path = output_dir / "manifest.json"
        old = {}
        if manifest_path.is_file():
            try:
                old = json.loads(manifest_path.read_text(encoding="utf-8"))
            except (OSError, ValueError):
                pass
        # One model load per dialect, not per card.
        model, vocoder, mel_type, dialect_id = load_inference(
            family, tag, args.device
        )
        normalized_audio, normalized_text = preprocess_ref_audio_text(
            str(reference), reference_text
        )
        entries = {}
        for key, text in sorted(grouped[dialect].items()):
            clip = output_dir / (key + ".mp3")
            prior = old.get("entries", {}).get(key, {})
            reusable = (
                not args.force and old.get("engine") == PROVIDER
                and old.get("version") == MANIFEST_VERSION
                and old.get("model") == family and old.get("dialectId") == tag
                and old.get("speed") == args.speed
                and old.get("referenceSha256") == reference_hash
                and prior.get("speechText") == text and clip.is_file()
                and clip.stat().st_size > 1024
                and prior.get("sha256") == file_sha256(clip)
            )
            if not reusable:
                wave, rate, _ = infer_process(
                    normalized_audio, normalized_text, text, model, vocoder,
                    mel_spec_type=mel_type, speed=args.speed,
                    device=args.device, dialect_id=dialect_id,
                )
                encode_mp3(wave, rate, clip)
            entries[key] = {
                "file": key + ".mp3", "speechText": text,
                "sha256": file_sha256(clip),
            }
        new_manifest = {
            "version": MANIFEST_VERSION, "engine": PROVIDER, "model": family,
            "dialectId": tag, "license": license_id,
            "referenceSha256": reference_hash, "speed": args.speed,
            "entries": entries,
        }
        output_dir.mkdir(parents=True, exist_ok=True)
        temporary = output_dir / "manifest.json.tmp"
        temporary.write_text(
            json.dumps(new_manifest, ensure_ascii=False, indent=2) + "\n",
            encoding="utf-8",
        )
        os.replace(temporary, manifest_path)
        print(f"{dialect}: generated/verified {len(entries)} Habibi clips")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--catalog", type=Path,
                        default=Path("app/build/speech_catalog.json"))
    parser.add_argument("--output", type=Path,
                        default=Path("app/src/main/assets/habibi_tts"))
    parser.add_argument("--references", type=Path, required=True)
    parser.add_argument("--dialects", default="ALL")
    parser.add_argument("--model", choices=["auto", "unified", "specialized"],
                        default="auto")
    parser.add_argument("--device", default="cuda")
    parser.add_argument("--speed", type=float, default=0.92)
    parser.add_argument("--ack-noncommercial", action="store_true")
    parser.add_argument("--force", action="store_true")
    args = parser.parse_args()
    if not 0.75 <= args.speed <= 1.15:
        parser.error("speed must be between 0.75 and 1.15")
    generate(args)


if __name__ == "__main__":
    main()
