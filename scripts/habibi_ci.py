#!/usr/bin/env python3
"""Durable Git checkpoints and verified aggregation for Habibi CPU shards.

Only completed, checksum-verified clips enter the assembled Android asset pack.
No speech model or private credentials are imported here.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import os
import re
import subprocess
from pathlib import Path

from generate_habibi_tts import atomic_json, shard_items
from habibi_contract import DIALECT_IDS, load_catalog
from verify_habibi_packs import verify

ASSETS = Path('app/src/main/assets/neural_tts')
SAFE_RUN_ID = re.compile(r'^[0-9]+$')


def git(*args: str, binary: bool = False):
    return subprocess.check_output(['git', *args], text=not binary).strip() if not binary else subprocess.check_output(['git', *args])


def checkpoint_ref(run_id: str, dialect: str, index: int) -> str:
    if not SAFE_RUN_ID.fullmatch(run_id) or dialect not in DIALECT_IDS or index < 0:
        raise ValueError('Invalid generated checkpoint reference')
    return f'habibi-checkpoints/{run_id}/{dialect.lower()}-{index}'


def expected_keys(catalog: Path, dialect: str, index: int, count: int) -> set[str]:
    return set(shard_items(load_catalog(catalog)[dialect], index, count))


def save_checkpoint(catalog: Path, root: Path, dialect: str, index: int, count: int, branch: str):
    manifest = root / dialect.lower() / 'manifest.json'
    if not manifest.is_file():
        print('No completed synthesis checkpoint to save')
        return
    verify(catalog, root, dialect, check_codec=True, allow_partial=True)
    data = json.loads(manifest.read_text())
    if not set(data['entries']).issubset(expected_keys(catalog, dialect, index, count)):
        raise ValueError('Checkpoint includes keys assigned to another shard')
    paths = [str(manifest), *(str(manifest.parent / row['file']) for row in data['entries'].values())]
    subprocess.run(['git', 'add', '--', *paths], check=True)
    changed = subprocess.run(['git', 'diff', '--cached', '--quiet']).returncode
    if changed:
        subprocess.run(['git', 'commit', '-m', f'feat(habibi): checkpoint {dialect} shard {index}, {len(data["entries"])} clips'], check=True)
    subprocess.run(['git', 'push', 'origin', f'HEAD:refs/heads/{branch}'], check=True)
    summary = os.environ.get('GITHUB_STEP_SUMMARY')
    if summary:
        with open(summary, 'a') as stream:
            stream.write(f'{dialect} shard {index}: {len(data["entries"])} / {len(expected_keys(catalog, dialect, index, count))} verified clips.\n')


def combine_shards(groups: dict, manifests: list[dict], read_clip, output: Path, count: int):
    """The read callback supplies bytes from immutable Git checkpoint commits."""
    merged = {dialect: {} for dialect in DIALECT_IDS}
    metadata = {}
    seen = set()
    for item in manifests:
        dialect, index, data, ref = (item[k] for k in ('dialect', 'index', 'data', 'ref'))
        if (dialect, index) in seen:
            raise ValueError('Duplicate checkpoint shard')
        seen.add((dialect, index))
        allowed = set(shard_items(groups[dialect], index, count))
        entries = data.get('entries')
        if not isinstance(entries, dict) or set(entries) != allowed:
            raise ValueError(f'{dialect}/{index}: shard incomplete; expected {len(allowed)} clips')
        header = {k: v for k, v in data.items() if k != 'entries'}
        if dialect in metadata and metadata[dialect] != header:
            raise ValueError(f'{dialect}: model, reference or runtime differs between shards')
        metadata[dialect] = header
        folder = output / dialect.lower()
        folder.mkdir(parents=True, exist_ok=True)
        for key, row in entries.items():
            if key in merged[dialect] or row.get('file') != key + '.mp3' or row.get('speechText') != groups[dialect][key]:
                raise ValueError(f'{dialect}/{key}: duplicate, invalid filename or wrong text')
            clip = read_clip(ref, dialect, row['file'])
            if len(clip) < 1024 or hashlib.sha256(clip).hexdigest() != row.get('sha256'):
                raise ValueError(f'{dialect}/{key}: corrupted or missing MP3')
            (folder / row['file']).write_bytes(clip)
            merged[dialect][key] = row
    if seen != {(d, i) for d in DIALECT_IDS for i in range(count)}:
        raise ValueError('Missing dialect/shard checkpoints')
    for dialect, entries in merged.items():
        if set(entries) != set(groups[dialect]):
            raise ValueError(f'{dialect}: final semantic-key coverage mismatch')
        atomic_json(output / dialect.lower() / 'manifest.json',
                    {**metadata[dialect], 'entries': dict(sorted(entries.items()))})
    return sum(map(len, merged.values()))


def assemble(catalog: Path, output: Path, run_id: str, count: int):
    manifests = []
    for dialect in DIALECT_IDS:
        for index in range(count):
            branch = checkpoint_ref(run_id, dialect, index)
            subprocess.run(['git', 'fetch', 'origin', f'refs/heads/{branch}'], check=True)
            ref = git('rev-parse', 'FETCH_HEAD')
            path = ASSETS / dialect.lower() / 'manifest.json'
            data = json.loads(git('show', f'{ref}:{path}'))
            manifests.append(dict(dialect=dialect, index=index, data=data, ref=ref))
    def read(ref, dialect, filename):
        return git('show', f'{ref}:{ASSETS / dialect.lower() / filename}', binary=True)
    total = combine_shards(load_catalog(catalog), manifests, read, output, count)
    verify(catalog, output, 'ALL', check_codec=True)
    print(f'Complete corpus assembled and verified: {total} MP3s')


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('operation', choices=['save', 'assemble'])
    parser.add_argument('--catalog', type=Path, default=Path('app/build/speech_catalog.json'))
    parser.add_argument('--root', type=Path, default=ASSETS)
    parser.add_argument('--dialect', choices=list(DIALECT_IDS))
    parser.add_argument('--shard-index', type=int, default=0)
    parser.add_argument('--shard-count', type=int, default=6)
    parser.add_argument('--run-id', required=True)
    args = parser.parse_args()
    if not SAFE_RUN_ID.fullmatch(args.run_id) or args.shard_count < 1:
        parser.error('Invalid workflow run ID or shard count')
    if args.operation == 'assemble':
        assemble(args.catalog, args.root, args.run_id, args.shard_count)
    else:
        if args.dialect is None or not 0 <= args.shard_index < args.shard_count:
            parser.error('save requires a valid dialect and shard index')
        branch = checkpoint_ref(args.run_id, args.dialect, args.shard_index)
        save_checkpoint(args.catalog, args.root, args.dialect, args.shard_index, args.shard_count, branch)


if __name__ == '__main__':
    main()
