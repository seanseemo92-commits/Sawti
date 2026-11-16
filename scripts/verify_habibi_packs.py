#!/usr/bin/env python3
"""Fail-closed Habibi-only 9-dialect audio pack verification."""
import argparse
import json
from pathlib import Path

from habibi_contract import (
    EXPECTED_CLIPS, MANIFEST_VERSION, PROVIDER,
    file_sha256, license_policy, load_catalog,
)


def verify(catalog, root):
    groups = load_catalog(catalog)
    count = 0
    for dialect, expected in groups.items():
        folder = root / dialect.lower()
        data = json.loads((folder / "manifest.json").read_text(encoding="utf-8"))
        model = data.get("model")
        if model not in ("Specialized", "Unified"):
            raise ValueError(f"Invalid Habibi model: {dialect}")
        tag, _, license_id = license_policy(dialect, model.lower())
        if (data.get("version") != MANIFEST_VERSION or
            data.get("engine") != PROVIDER or data.get("dialectId") != tag or
            data.get("license") != license_id or
            len(data.get("referenceSha256", "")) != 64):
            raise ValueError(f"Invalid Habibi metadata: {dialect}")
        entries = data.get("entries")
        if not isinstance(entries, dict) or set(entries) != set(expected):
            raise ValueError(f"Incomplete Habibi manifest: {dialect}")
        for key, spoken in expected.items():
            row = entries[key]
            clip = folder / (key + ".mp3")
            if (row.get("file") != key + ".mp3" or
                row.get("speechText") != spoken or
                not clip.is_file() or clip.stat().st_size < 1024 or
                row.get("sha256") != file_sha256(clip)):
                raise ValueError(f"Invalid Habibi audio: {dialect}/{key}")
            count += 1
        print(f"{dialect}: {len(expected)} verified")
    if count != EXPECTED_CLIPS:
        raise ValueError(f"Expected {EXPECTED_CLIPS} but found {count}")
    print("All Habibi MP3 files validated (native listening still required).")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--catalog", type=Path, default=Path("app/build/speech_catalog.json"))
    parser.add_argument("--root", type=Path, default=Path("app/src/main/assets/habibi_tts"))
    args = parser.parse_args()
    verify(args.catalog, args.root)


if __name__ == "__main__":
    main()
