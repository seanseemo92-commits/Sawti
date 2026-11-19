"""Verify and assemble the complete isolated Libyan Habibi preview pack."""
import argparse
import json
import shutil
from pathlib import Path

from generate_habibi_tts import atomic_json, shard_items
from habibi_contract import file_sha256, load_catalog
from verify_habibi_packs import verify


def assemble(catalog: Path, shards: Path, output: Path, count: int, repair_shard: int | None):
    expected = load_catalog(catalog)["LIBYAN"]
    entries = {}
    assets = {}
    header = None
    def collect(folder: Path, keys: set[str]):
        nonlocal header
        manifest = json.loads((folder / "manifest.json").read_text())
        rows = manifest.pop("entries")
        if header is not None and manifest != header:
            raise ValueError(f"{folder}: incompatible inference settings")
        header = manifest
        if set(rows) != keys:
            raise ValueError(f"{folder}: incomplete or unexpected keys")
        for key, row in rows.items():
            path = folder / f"{key}.mp3"
            if row["file"] != path.name or row["speechText"] != expected[key]:
                raise ValueError(f"Wrong text or filename for {key}")
            if file_sha256(path) != row["sha256"]:
                raise ValueError(f"Checksum mismatch for {key}")
            entries[key] = row
            assets[key] = path

    if repair_shard is not None and not 0 <= repair_shard < count:
        raise ValueError("Invalid repair shard index")
    for index in range(count):
        keys = set(shard_items(expected, index, count))
        if index == repair_shard:
            for key in sorted(keys):
                collect(shards / f"libyan-repair-{key}" / "libyan", {key})
        else:
            collect(shards / f"libyan-shard-{index}" / "libyan", keys)

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
    parser.add_argument("--repair-shard", type=int)
    args = parser.parse_args()
    assemble(args.catalog, args.shards, args.output, args.count, args.repair_shard)
