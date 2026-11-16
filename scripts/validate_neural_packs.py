#!/usr/bin/env python3
"""Fail closed if any of the nine offline neural packs is incomplete or stale."""
import json
from pathlib import Path
from generate_neural_voices import VOICES, LIBYAN_TEACHER, load_catalog

ROOT = Path("app/src/main/assets/neural_tts")
CATALOG = Path("app/build/speech_catalog.json")
EXPECTED_TOTAL = 2399  # 271 Libyan entries + 266 entries for each other dialect


def main():
    grouped = load_catalog(CATALOG)
    total = sum(map(len, grouped.values()))
    if total != EXPECTED_TOTAL:
        raise SystemExit(f"Speech catalog changed: {total} entries; expected {EXPECTED_TOTAL}. Audit before release.")
    for dialect, expected in grouped.items():
        folder = ROOT / dialect.lower()
        path = folder / "manifest.json"
        if not path.is_file():
            raise SystemExit(f"Missing neural manifest: {path}")
        manifest = json.loads(path.read_text(encoding="utf-8"))
        expected_voice = LIBYAN_TEACHER["voice"] if dialect == "LIBYAN" else VOICES[dialect][0]
        expected_rate = LIBYAN_TEACHER["rate"] if dialect == "LIBYAN" else "-18%"
        expected_pitch = LIBYAN_TEACHER["pitch"] if dialect == "LIBYAN" else "+0Hz"
        if (manifest.get("version") != 1 or
                manifest.get("voice") != expected_voice or
                manifest.get("rate") != expected_rate or
                manifest.get("pitch", "+0Hz") != expected_pitch):
            raise SystemExit(f"Invalid neural manifest version/voice/rate/pitch: {path}")
        actual = manifest.get("entries")
        if not isinstance(actual, dict) or set(actual) != set(expected):
            missing = set(expected) - set(actual or {})
            extra = set(actual or {}) - set(expected)
            raise SystemExit(f"Invalid coverage {dialect}: missing={len(missing)} extra={len(extra)}")
        for key, expected_speech in expected.items():
            entry = actual[key]
            audio = folder / f"{key}.mp3"
            if (entry.get("speechText") != expected_speech or
                entry.get("file") != f"{key}.mp3" or
                not audio.is_file() or audio.stat().st_size <= 1024):
                raise SystemExit(f"Missing/stale/empty neural audio: {dialect}/{key}")
        print(f"{dialect}: {len(expected)} verified clips ({manifest['voice']})")
    print(f"Verified all {total} offline neural voice clips in nine dialects.")


if __name__ == "__main__":
    main()
