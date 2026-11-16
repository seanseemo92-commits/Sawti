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
# From SWivid/Habibi-TTS model card. SAU/UAE and Unified are NC.
APACHE_SPECIALIZED = frozenset(("MSA", "ALG", "EGY", "MAR", "IRQ"))
PROVIDER = "Habibi-TTS"
MANIFEST_VERSION = 2
EXPECTED_CLIPS = 2399
SAFE_KEY = re.compile(r"^[a-z0-9][a-z0-9._-]*$")
ARABIC_DIACRITICS = re.compile(r"[\u0610-\u061A\u064B-\u065F\u0670\u06D6-\u06ED]")


def license_policy(dialect: str, requested: str = "auto"):
    if dialect not in DIALECT_IDS:
        raise ValueError("Unknown dialect: " + dialect)
    tag = DIALECT_IDS[dialect]
    if requested not in ("auto", "unified", "specialized"):
        raise ValueError("Model must be auto|unified|specialized")
    model = ("Specialized" if requested == "specialized" or
             (requested == "auto" and tag in APACHE_SPECIALIZED) else "Unified")
    if model == "Specialized" and tag not in APACHE_SPECIALIZED:
        raise ValueError(f"Specialized Apache model not available for {tag}")
    license_id = "Apache-2.0" if model == "Specialized" else "CC-BY-NC-SA-4.0"
    return tag, model, license_id


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
        if key in groups[dialect]:
            raise ValueError(f"Duplicate Habibi key {dialect}/{key}")
        groups[dialect][key] = spoken
    if any(not group for group in groups.values()):
        raise ValueError("Each of the nine dialects must contain words")
    if sum(map(len, groups.values())) != EXPECTED_CLIPS:
        raise ValueError("Speech catalog cardinality changed; review before generating")
    return groups
