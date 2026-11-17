#!/usr/bin/env python3
"""Verify exact Sawti Habibi-TTS packs; never accept Microsoft v1.

--dialect EGYPTIAN checks all clips in one dialect; default ALL requires all
2399 audio clips and all 391 unique semantic keys. Partial batches are invalid
for release and should only be resumed through the generation workflow.
"""
import argparse
import json
import math
import subprocess
from pathlib import Path

from habibi_contract import (
    DIALECT_IDS, EXPECTED_CLIPS, EXPECTED_KEYS, file_sha256, load_catalog,
)
from generate_habibi_tts import (
    MANIFEST_VERSION, PROVIDER, LICENSE, MODEL, MODEL_REVISION, SAMPLE_RATE, BITRATE,
    SEED, NFE_STEP, CFG_STRENGTH, SWAY_SAMPLING_COEF,
    SPEED_SHORT, SPEED_NORMAL, PIPELINE_VERSION, clean_for_habibi,
)


def inspect_mp3(path: Path):
    result = subprocess.run(
        ["ffprobe", "-v", "error", "-select_streams", "a:0",
         "-show_entries", "stream=codec_name,sample_rate,channels,bit_rate",
         "-of", "json", str(path)], capture_output=True, text=True, check=True,
        timeout=20,
    )
    streams = json.loads(result.stdout).get("streams", [])
    if len(streams) != 1:
        raise ValueError(f"Expected one audio stream: {path}")
    info = streams[0]
    if (info.get("codec_name") != "mp3" or
        int(info.get("sample_rate", 0)) != SAMPLE_RATE or
        int(info.get("channels", 0)) != 1):
        raise ValueError(f"Invalid mono/24kHz MP3 encoding: {path}")
    rate = int(info.get("bit_rate", 0))
    if not (64000 <= rate <= 100000):
        raise ValueError(f"Unexpected MP3 bitrate: {path} ({rate})")


def verify(catalog: Path, root: Path, dialect: str, check_codec: bool, allow_partial: bool = False):
    groups = load_catalog(catalog)
    selected = list(DIALECT_IDS) if dialect == "ALL" else [dialect]
    counted = 0
    for name in selected:
        folder = root / name.lower()
        manifest = folder / "manifest.json"
        if allow_partial and not manifest.is_file():
            continue
        data = json.loads(manifest.read_text(encoding="utf-8"))
        tag = DIALECT_IDS[name]
        expected = groups[name]
        required = {
            "version": MANIFEST_VERSION, "engine": PROVIDER,
            "pipelineVersion": PIPELINE_VERSION,
            "model": MODEL, "modelRevision": MODEL_REVISION, "license": LICENSE, "dialectId": tag,
            "sampleRate": SAMPLE_RATE, "bitrate": BITRATE,
            "seed": SEED, "nfeStep": NFE_STEP,
            "cfgStrength": CFG_STRENGTH,
            "swaySamplingCoef": SWAY_SAMPLING_COEF,
            "speedShort": SPEED_SHORT, "speedNormal": SPEED_NORMAL,
        }
        for key, value in required.items():
            if data.get(key) != value:
                raise ValueError(f"{name}: invalid {key}: {data.get(key)!r}")
        for field in ("referenceSha256", "referenceTextSha256"):
            value = data.get(field, "")
            import re
            if not isinstance(value, str) or not re.fullmatch(r"[0-9a-f]{64}", value):
                raise ValueError(f"{name}: missing {field}")
        entries = data.get("entries")
        if (not isinstance(entries, dict) or not set(entries).issubset(expected) or
                (not allow_partial and set(entries) != set(expected))):
            missing = set(expected) - set(entries or {})
            extra = set(entries or {}) - set(expected)
            raise ValueError(f"{name}: incomplete pack; missing={len(missing)}, extra={len(extra)}")
        actual_files = {p.name for p in folder.glob("*.mp3")}
        expected_files = {key + ".mp3" for key in entries}
        if (not expected_files.issubset(actual_files) or
                (not allow_partial and actual_files != expected_files)):
            raise ValueError(f"{name}: unexpected legacy/missing MP3 files")
        for key in entries:
            spoken = expected[key]
            row = entries[key]
            file = folder / (key + ".mp3")
            if (row.get("file") != key + ".mp3" or
                row.get("speechText") != spoken or
                clean_for_habibi(spoken) != spoken or
                not file.is_file() or file.stat().st_size < 1024 or
                row.get("sha256") != file_sha256(file)):
                raise ValueError(f"{name}/{key}: stale/missing/corrupted clip")
            if "synthesisAttempts" in row or "fixedDurationSeconds" in row:
                attempts, duration = row.get("synthesisAttempts"), row.get("fixedDurationSeconds")
                if (type(attempts) is not int or not 1 <= attempts <= 3 or
                        (duration is not None and (type(duration) not in (int, float) or
                         not math.isfinite(duration) or duration <= 0)) or
                        (attempts > 1 and duration is None)):
                    raise ValueError(f"{name}/{key}: invalid synthesis timing provenance")
            if check_codec:
                inspect_mp3(file)
            counted += 1
        print(f"{name}/{tag}: verified {len(entries)}/{len(expected)} Habibi clips")
    if dialect == "ALL" and not allow_partial:
        if counted != EXPECTED_CLIPS or len({
            key for group in groups.values() for key in group
        }) != EXPECTED_KEYS:
            raise ValueError(f"Corpus coverage mismatch: {counted}")
    scope = "PARTIAL batch (not release-ready)" if allow_partial else "COMPLETE pack"
    print(f"Verification passed: {counted} clips; {scope}. Listening review is separate.")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--catalog", type=Path,
                        default=Path("app/build/speech_catalog.json"))
    parser.add_argument("--root", type=Path,
                        default=Path("app/src/main/assets/neural_tts"))
    parser.add_argument("--dialect", choices=["ALL", *DIALECT_IDS.keys()], default="ALL")
    parser.add_argument("--verify-codec", action="store_true")
    parser.add_argument("--allow-partial", action="store_true",
                        help="Verify only completed manifest entries; NEVER use for releases")
    args = parser.parse_args()
    verify(args.catalog, args.root, args.dialect, args.verify_codec, args.allow_partial)


if __name__ == "__main__":
    main()
