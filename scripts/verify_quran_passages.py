"""Validate packaged Quran passage text/audio without accessing the network."""
import argparse
import hashlib
import json
import subprocess
from pathlib import Path

REPO = Path(__file__).resolve().parent.parent
ASSETS = REPO / "app/src/main/assets"
READERS = {"ar.alafasy", "salah.bukhatir", "husary.muallim"}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--decode", action="store_true")
    args = parser.parse_args()
    plan = json.loads((REPO / "scripts/quran_passages_plan.json").read_text())
    catalog = json.loads((ASSETS / "quran_passages/catalog.json").read_text())
    assert catalog["version"] == 1
    assert [v["ayah"] for v in catalog["verses"]] == [255, 285, 286]
    paths = set()
    for expected, verse in zip(plan["verses"], catalog["verses"]):
        assert verse["global"] == verse["ayah"] + 7
        assert verse["text"] == expected["text"]
        assert verse["textResponseSha256"] == expected["textResponseSha256"]
        assert " ".join(part["text"] for part in verse["parts"]) == verse["text"]
        assert len(verse["parts"]) == {255: 9, 285: 5, 286: 8}[verse["ayah"]]
        for index, (part, reference) in enumerate(zip(verse["parts"], expected["parts"]), 1):
            assert part["text"] == reference["text"]
            assert set(part["audio"]) == READERS
            for reader, audio in part["audio"].items():
                assert audio["asset"] == f"quran_passages/{reader}/2_{verse['ayah']}_{index}.mp3"
                assert audio["asset"] not in paths
                paths.add(audio["asset"])
                assert audio["sourceSha256"] == expected["sources"][reader]["sha256"]
                assert audio["startMs"] == reference["bounds"][reader]["startMs"]
                assert audio["endMs"] == reference["bounds"][reader]["endMs"]
                assert 0 <= audio["startMs"] < audio["endMs"]
                file = ASSETS / audio["asset"]
                assert 1000 < file.stat().st_size < 2 * 1024 * 1024
                assert hashlib.sha256(file.read_bytes()).hexdigest() == audio["sha256"]
                if args.decode:
                    subprocess.run(["ffmpeg", "-nostdin", "-v", "error", "-xerror", "-i", str(file),
                                    "-f", "null", "-"], check=True)
                    duration = float(subprocess.check_output(["ffprobe", "-v", "error", "-show_entries",
                        "format=duration", "-of", "csv=p=0", str(file)], text=True))
                    expected_duration = (audio["endMs"] - audio["startMs"]) / 1000
                    assert abs(duration - expected_duration) < .15, (file, duration, expected_duration)
    assert len(paths) == 66
    actual = {str(p.relative_to(ASSETS)) for p in (ASSETS / "quran_passages").rglob("*.mp3")}
    assert paths == actual, "Unexpected/missing packaged Quran recordings"
    print("Quran passages: exact source text, 22 unique parts, all 66 hashes" +
          (", full MP3 decoding and durations" if args.decode else "") + " verified.")
    print("This does not certify waqf or listening quality; human listening review is pending.")


if __name__ == "__main__":
    main()
