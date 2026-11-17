#!/usr/bin/env python3
"""One auditable Habibi-TTS mapping, license and pack manifest contract."""
import hashlib
import json
import re
from pathlib import Path

# Nine Sawti dialects are explicit. LEV is used for Syrian; requires listening review.
DIALECT_IDS = {
    "STANDARD": "MSA", "LIBYAN": "LBY", "SAUDI": "SAU",
    "EGYPTIAN": "EGY", "SYRIAN": "LEV", "IRAQI": "IRQ",
    "TUNISIAN": "TUN", "MOROCCAN": "MAR", "ALGERIAN": "ALG",
}
PROVIDER = "Habibi-TTS"
MANIFEST_VERSION = 2
EXPECTED_CLIPS = 2399
EXPECTED_KEYS = 391
SAFE_KEY = re.compile(r"^[a-z0-9][a-z0-9._-]*$")
ARABIC_DIACRITICS = re.compile(r"[\u0610-\u061A\u064B-\u065F\u0670\u06D6-\u06ED\u0640]")
DIGITS = re.compile(r"\d", re.UNICODE)
EXPECTED_BY_DIALECT = {name: 271 if name == "LIBYAN" else 266 for name in DIALECT_IDS}


def file_sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def load_catalog(path: Path):
    source = json.loads(path.read_text(encoding="utf-8"))
    if source.get("version") != 1 or not isinstance(source.get("entries"), list):
        raise ValueError("Expected Kotlin ExportSpeechCatalogTest JSON v1")
    groups = {dialect: {} for dialect in DIALECT_IDS}
    for entry in source["entries"]:
        dialect = entry.get("dialect")
        key = entry.get("semanticKey")
        spoken = entry.get("speechText")
        if dialect not in groups or not isinstance(key, str) or not SAFE_KEY.fullmatch(key) or ".." in key:
            raise ValueError(f"Unsafe dialect/semantic key {dialect}/{key}")
        if not isinstance(spoken, str) or not spoken.strip():
            raise ValueError(f"Empty Habibi text {dialect}/{key}")
        if ARABIC_DIACRITICS.search(spoken):
            raise ValueError(f"Unexpected Arabic diacritics {dialect}/{key}")
        if DIGITS.search(spoken):
            raise ValueError(f"Write numeric digits as words: {dialect}/{key}")
        if spoken != spoken.strip() or re.search(r"\s{2,}", spoken):
            raise ValueError(f"Unnormalized whitespace: {dialect}/{key}")
        if key in groups[dialect]:
            raise ValueError(f"Duplicate Habibi key {dialect}/{key}")
        groups[dialect][key] = spoken
    for name, group in groups.items():
        if len(group) != EXPECTED_BY_DIALECT[name]:
            raise ValueError(f"{name}: expected {EXPECTED_BY_DIALECT[name]} entries, got {len(group)}")
    # 251 shared concepts in all nine dialects, plus 140 regional dishes:
    # 20 Libyan dishes and 15 in each of the other eight dialects.
    common = {name: {k for k in group if not k.startswith("dish.")}
              for name, group in groups.items()}
    shared = common["STANDARD"]
    if len(shared) != 251 or any(keys != shared for keys in common.values()):
        raise ValueError("All 251 common semantic keys must exist in every dialect")
    regional = [k for group in groups.values() for k in group if k.startswith("dish.")]
    if len(regional) != 140 or len(set(regional)) != 140:
        raise ValueError("All 140 regional dish keys must occur in their own dialect")
    if sum(map(len, groups.values())) != EXPECTED_CLIPS:
        raise ValueError("Speech catalog cardinality changed; review before generating")
    if len({key for group in groups.values() for key in group}) != EXPECTED_KEYS:
        raise ValueError(f"Expected {EXPECTED_KEYS} distinct semantic keys")
    return groups
