#!/usr/bin/env python3
"""Strict offline content verification for Sawti Quran short-surah APK assets."""
import argparse
import hashlib
import json
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "app/src/main/assets/quran_surahs"
CHAPTERS = {1:7,112:4,113:5,114:6,108:3,103:3,109:6,110:3,111:5,105:5,106:4,107:7,104:9}
FIRST = {1:1,103:6177,104:6180,105:6189,106:6194,107:6198,108:6205,
         109:6208,110:6214,111:6217,112:6222,113:6226,114:6231}
READERS = {"ar.alafasy", "salah.bukhatir", "husary.muallim"}


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--decode", action="store_true")
    args = parser.parse_args()
    manifest = json.loads((ASSETS / "catalog.json").read_text(encoding="utf-8"))
    assert manifest["version"] == 1
    assert len(manifest["verses"]) == 67
    assert set(manifest["textSha256"]) == {str(n) for n in CHAPTERS}
    seen = set()
    assets_expected = set()
    for surah, count in CHAPTERS.items():
        textfile = ASSETS / "text" / f"surah_{surah}.json"
        assert digest(textfile) == manifest["textSha256"][str(surah)]
        source = json.loads(textfile.read_text(encoding="utf-8"))
        data = source["data"]
        assert source["status"].upper() == "OK"
        assert data["number"] == surah and data["numberOfAyahs"] == count
        assert len(data["ayahs"]) == count
        for ayah, verse in enumerate(data["ayahs"], 1):
            assert verse["numberInSurah"] == ayah
            assert verse["number"] == FIRST[surah] + ayah - 1
            assert verse["text"].strip()
    for item in manifest["verses"]:
        surah, ayah, global_id = item["surah"], item["ayah"], item["global"]
        assert surah in CHAPTERS and 1 <= ayah <= CHAPTERS[surah]
        assert global_id == FIRST[surah] + ayah - 1
        assert (surah, ayah) not in seen
        seen.add((surah, ayah))
        assert set(item["audio"]) == READERS
        for reader, recording in item["audio"].items():
            relative = f"{reader}/{surah:03d}{ayah:03d}.mp3"
            assert recording["asset"] == "quran_surahs/" + relative
            assert "everyayah.com/data/" in recording["source"]
            assert recording["processing"] in {"unaltered", "lossless_remux", "reencoded_known_corrupt_frame"}
            if recording["processing"] == "reencoded_known_corrupt_frame":
                assert (reader, surah, ayah) == ("husary.muallim", 113, 5)
            asset = ASSETS / relative
            assert 1024 < asset.stat().st_size <= 12 * 1024 * 1024
            assert digest(asset) == recording["sha256"]
            assets_expected.add(relative)
            if args.decode:
                subprocess.run(["ffmpeg", "-nostdin", "-v", "error", "-xerror", "-i",
                                str(asset), "-f", "null", "-"], check=True,
                               stdout=subprocess.DEVNULL)
    assert seen == {(surah, ayah) for surah, n in CHAPTERS.items() for ayah in range(1, n+1)}
    actual = {str(file.relative_to(ASSETS)) for file in ASSETS.rglob("*.mp3")}
    assert actual == assets_expected and len(actual) == 201
    print("VERIFIED: 13 Uthmani surah texts, 67 ayahs, 201 SHA-256 checked MP3 files, one recorded source repair.")


if __name__ == "__main__":
    main()
