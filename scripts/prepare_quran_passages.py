"""Rebuild the three selected ayahs' private test assets from pinned recordings.

Requires ffmpeg. No synthetic speech, word concatenation, gain changes, or reader
substitution. Source audio is hash checked before use. Clip boundaries are
reciter-specific and remain candidates for human listening review.
"""
import argparse
import hashlib
import json
import subprocess
import urllib.request
from pathlib import Path

REPO = Path(__file__).resolve().parent.parent
ASSETS = REPO / "app/src/main/assets"


def sha(data):
    return hashlib.sha256(data).hexdigest()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--sources", type=Path, required=True, help="Local source-audio scratch directory")
    args = parser.parse_args()
    args.sources.mkdir(parents=True, exist_ok=True)
    plan = json.loads((REPO / "scripts/quran_passages_plan.json").read_text())
    catalog = {"version": 1, "verses": []}
    for verse in plan["verses"]:
        item = {key: verse[key] for key in ("ayah", "global", "text", "textSource", "textResponseSha256")}
        item["parts"] = [{"text": part["text"], "audio": {}} for part in verse["parts"]]
        for reader, source in verse["sources"].items():
            original = args.sources / f"{reader}-{verse['ayah']}.mp3"
            if not original.is_file():
                with urllib.request.urlopen(source["url"], timeout=60) as response:
                    original.write_bytes(response.read())
            assert sha(original.read_bytes()) == source["sha256"], f"Source recording changed: {original.name}"
            for index, part in enumerate(verse["parts"], 1):
                bounds = part["bounds"][reader]
                relative = f"quran_passages/{reader}/2_{verse['ayah']}_{index}.mp3"
                destination = ASSETS / relative
                destination.parent.mkdir(parents=True, exist_ok=True)
                subprocess.run([
                    "ffmpeg", "-nostdin", "-y", "-v", "error", "-i", str(original),
                    "-ss", str(bounds["startMs"] / 1000),
                    "-t", str((bounds["endMs"] - bounds["startMs"]) / 1000),
                    "-map_metadata", "-1", "-acodec", "libmp3lame", "-b:a", "128k",
                    "-ar", "44100", "-ac", "1", str(destination),
                ], check=True)
                item["parts"][index - 1]["audio"][reader] = {
                    "asset": relative, "sha256": sha(destination.read_bytes()),
                    "sourceSha256": source["sha256"], **bounds,
                }
        catalog["verses"].append(item)
    (ASSETS / "quran_passages/catalog.json").write_text(json.dumps(catalog, ensure_ascii=False, indent=2) + "\n")
    print("Prepared 22 text fragments and 66 recorded phrase clips; listening review remains pending.")


if __name__ == "__main__":
    main()
