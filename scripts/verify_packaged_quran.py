"""Verify the actual APK contains exactly the reviewed offline Quran recordings."""
import hashlib
import json
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "app/src/main/assets"


def verify(apk):
    with zipfile.ZipFile(apk) as archive:
        assert archive.testzip() is None, "Corrupt APK entry"
        names = archive.namelist()
        assert len(names) == len(set(names)), "Duplicate APK entries"
        expected = {}
        for catalog_name in ("quran_passages/catalog.json", "quran_surahs/catalog.json"):
            source = (ASSETS / catalog_name).read_bytes()
            assert archive.read("assets/" + catalog_name) == source, "Catalog differs from source"
            catalog = json.loads(source)
            if catalog_name.startswith("quran_passages"):
                assert [verse["ayah"] for verse in catalog["verses"]] == [255, 285, 286]
                audio_rows = [part for verse in catalog["verses"] for part in verse["parts"]]
            else:
                audio_rows = catalog["verses"]
                for number, digest in catalog["textSha256"].items():
                    data = archive.read(f"assets/quran_surahs/text/surah_{number}.json")
                    assert hashlib.sha256(data).hexdigest() == digest, "Quran text hash mismatch"
            for row in audio_rows:
                for recording in row["audio"].values():
                    name = "assets/" + recording["asset"]
                    assert name not in expected, "Repeated Quran recording"
                    expected[name] = recording["sha256"]
        assert len(expected) == 267, "Missing offline recording declarations"
        actual = {name for name in names if name.endswith(".mp3") and
                  name.startswith(("assets/quran_passages/", "assets/quran_surahs/"))}
        assert actual == set(expected), "Unexpected or missing packaged Quran audio"
        for name, digest in expected.items():
            assert hashlib.sha256(archive.read(name)).hexdigest() == digest, name
        assert not any("/2_284_" in name for name in actual), "Removed ayah 284 is packaged"
    print("APK verified: all 267 offline Quran recordings, both catalogs and 13 text hashes.")


if __name__ == "__main__":
    verify(Path(sys.argv[1]))
