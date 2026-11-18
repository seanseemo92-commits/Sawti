"""Verify and assemble the complete isolated Libyan Habibi preview pack."""
import argparse
import json
import shutil
from pathlib import Path

from generate_habibi_tts import atomic_json, shard_items
from habibi_contract import file_sha256, load_catalog
from verify_habibi_packs import verify


def assemble(catalog: Path, shards: Path, output: Path, count: int):
    expected = load_catalog(catalog)["LIBYAN"]
    entries = {}
    assets = {}
    header = None
    for index in range(count):
        folder = shards / f"libyan-shard-{index}" / "libyan"
        manifest = json.loads((folder / "manifest.json").read_text())
        rows = manifest.pop("entries")
        if header is not None and manifest != header:
            raise ValueError(f"Shard {index} used incompatible inference settings")
        header = manifest
        if set(rows) != set(shard_items(expected, index, count)):
            raise ValueError(f"Shard {index} has incomplete or unexpected keys")
        for key, row in rows.items():
            path = folder / f"{key}.mp3"
            if row["file"] != path.name or row["speechText"] != expected[key]:
                raise ValueError(f"Wrong text or filename for {key}")
            if file_sha256(path) != row["sha256"]:
                raise ValueError(f"Checksum mismatch for {key}")
            entries[key] = row
            assets[key] = path

    if set(entries) != set(expected):
        raise ValueError("The preview does not contain all Libyan keys")
    folder = output / "libyan"
    folder.mkdir(parents=True, exist_ok=False)
    for key, source in assets.items():
        shutil.copyfile(source, folder / f"{key}.mp3")
    atomic_json(folder / "manifest.json", {**header, "entries": entries})
    verify(catalog, output, "LIBYAN", check_codec=True, libyan_preview=True)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--catalog", type=Path, required=True)
    parser.add_argument("--shards", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--count", type=int, required=True)
    args = parser.parse_args()
    assemble(args.catalog, args.shards, args.output, args.count)
