#!/usr/bin/env python3
"""Exact CSV / exported Kotlin catalog parity gate for v1.6.2."""
import csv
import json
from pathlib import Path
from generate_neural_voices import VOICES

SOURCE = Path("scripts/neural_v162_overrides.csv")
EXPORTED = Path("app/build/speech_catalog.json")
COUNTS = {"LIBYAN": 271, **{dialect: 266 for dialect in VOICES if dialect != "LIBYAN"}}


def main():
    with SOURCE.open(encoding="utf-8-sig", newline="") as stream:
        rows = list(csv.DictReader(stream))
    if len(rows) != 2399:
        raise SystemExit(f"Expected 2,399 source rows; got {len(rows)}")
    source = {}
    for row in rows:
        dialect, key, text = row["dialect"], row["semantic_key"], row["neural_speech_text"]
        identity = (dialect, key)
        if dialect not in VOICES or identity in source or not text or not row["display"]:
            raise SystemExit(f"Bad or duplicate identity: {identity}")
        if row["voice_id"] != VOICES[dialect][0]:
            raise SystemExit(f"Wrong Microsoft Neural voice: {identity}")
        if "ّْ" in text:
            raise SystemExit(f"Forbidden shadda + sukun: {identity}")
        source[identity] = text
    counts = {dialect: sum(r["dialect"] == dialect for r in rows) for dialect in VOICES}
    if counts != COUNTS:
        raise SystemExit(f"Unexpected dialect counts: {counts}")
    data = json.loads(EXPORTED.read_text(encoding="utf-8"))
    if data.get("version") != 1:
        raise SystemExit("Wrong speech catalog export version")
    exported = {}
    for item in data["entries"]:
        identity = (item["dialect"], item["semanticKey"])
        if identity in exported:
            raise SystemExit(f"Duplicate export identity: {identity}")
        exported[identity] = item["speechText"]
    if source != exported:
        missing = source.keys() - exported.keys()
        extra = exported.keys() - source.keys()
        changed = [k for k in source.keys() & exported.keys() if source[k] != exported[k]]
        raise SystemExit(f"CSV/export mismatch: missing={len(missing)}, extra={len(extra)}, different={len(changed)}; samples={changed[:5]}")
    print(f"v1.6.2 CSV exactly matches exported Kotlin speech for {len(rows)} cards.")


if __name__ == "__main__":
    main()
