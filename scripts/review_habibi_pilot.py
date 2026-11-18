#!/usr/bin/env python3
"""Audit a reproducible set of Libyan clips across multiple card sections.

Codec, duration, hashes and waveform differences cannot certify pronunciation.
The report deliberately records listeningApproval as pending, even on success.
"""
import argparse
import itertools
import json
import random
from pathlib import Path

from generate_habibi_tts import PIPELINE_VERSION, minimum_speech_seconds, atomic_json
from habibi_contract import file_sha256, load_catalog
from verify_habibi_packs import inspect_mp3, validate_timing

PILOT_SEED = 162
PILOT_SECTIONS = ('action', 'food', 'transport', 'greeting')
PRONUNCIATION_CONTROLS = ('request.water', 'request.toilet', 'body.leg', 'body.tooth')


def pilot_keys(expected: dict[str, str], include_controls: bool = False) -> tuple[str, ...]:
    rng = random.Random(PILOT_SEED)
    keys = []
    for section in PILOT_SECTIONS:
        candidates = sorted(key for key in expected if key.startswith(section + '.'))
        if not candidates:
            raise ValueError(f'Libyan catalog has no cards in {section}')
        keys.append(rng.choice(candidates))
    if include_controls:
        for key in PRONUNCIATION_CONTROLS:
            if key not in expected:
                raise ValueError(f'Libyan catalog has no pronunciation control {key}')
            keys.append(key)
    return tuple(keys)


def waveform(path: Path):
    import numpy as np
    from pydub import AudioSegment
    audio = AudioSegment.from_mp3(path).set_channels(1).set_frame_rate(8000)
    samples = np.asarray(audio.get_array_of_samples(), dtype=np.float64)
    samples -= samples.mean()
    return samples, len(audio) / 1000


def similarity(first, second) -> float:
    """Compare full energy at small lags; do not match isolated shared phonemes."""
    import numpy as np
    from scipy.signal import correlate, correlation_lags
    if min(len(first), len(second)) / max(len(first), len(second)) < .85:
        return 0.
    energy = np.linalg.norm(first) * np.linalg.norm(second)
    if not energy:
        raise ValueError('Silent pilot waveform')
    corr = correlate(first, second, method='fft')
    lags = correlation_lags(len(first), len(second))
    return float(np.max(np.abs(corr[np.abs(lags) <= 640])) / energy)


def review(root: Path, catalog: Path, baseline: Path, report: Path, include_controls: bool = False):
    expected = load_catalog(catalog)['LIBYAN']
    keys = pilot_keys(expected, include_controls)
    manifest = json.loads((root / 'libyan/manifest.json').read_text())
    if manifest.get('pipelineVersion') != PIPELINE_VERSION:
        raise ValueError('Pilot was not generated with the repaired pipeline')
    if set(manifest['entries']) != set(keys):
        raise ValueError('Pilot manifest does not match the seeded section selection')
    clips, waves = {}, {}
    for key in keys:
        entry = manifest['entries'][key]
        path = root / 'libyan' / (key + '.mp3')
        if entry['speechText'] != expected[key] or entry['sha256'] != file_sha256(path):
            raise ValueError(f'Pilot text/hash mismatch: {key}')
        validate_timing(entry, expected[key])
        inspect_mp3(path)
        wave, duration = waveform(path)
        if duration < minimum_speech_seconds(expected[key]):
            raise ValueError(f'Pilot remains implausibly short: {key}')
        waves[key] = wave
        old = baseline / 'libyan' / (key + '.mp3')
        clips[key] = {'speechText': expected[key], 'durationSeconds': duration,
                      'sha256': entry['sha256']}
        if old.is_file():
            clips[key]['previousDurationSeconds'] = waveform(old)[1]
            clips[key]['matchesBundledBytes'] = file_sha256(old) == entry['sha256']
    pairs = []
    for first, second in itertools.combinations(keys, 2):
        score = similarity(waves[first], waves[second])
        pairs.append({'first': first, 'second': second, 'correlation': score})
        if expected[first] != expected[second] and score >= .995:
            raise ValueError(f'Different words have near-identical audio: {first}, {second}: {score}')
    result = {'technicalChecks': 'passed', 'listeningApproval': 'pending',
              'pipelineVersion': PIPELINE_VERSION, 'selectionSeed': PILOT_SEED,
              'referenceType': manifest['referenceType'], 'clips': clips, 'pairs': pairs}
    atomic_json(report, result)
    return result


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--root', type=Path)
    parser.add_argument('--catalog', type=Path, default=Path('app/build/speech_catalog.json'))
    parser.add_argument('--baseline', type=Path, default=Path('app/src/main/assets/neural_tts'))
    parser.add_argument('--report', type=Path)
    parser.add_argument('--list-keys', action='store_true')
    parser.add_argument('--include-controls', action='store_true')
    args = parser.parse_args()
    if args.list_keys:
        print(*pilot_keys(load_catalog(args.catalog)['LIBYAN'], args.include_controls), sep='\n')
        raise SystemExit(0)
    if not args.root or not args.report:
        parser.error('--root and --report are required unless listing selected keys')
    result = review(args.root, args.catalog, args.baseline, args.report, args.include_controls)
    print(f"{len(result['clips'])} clips passed technical checks. Listening approval: {result['listeningApproval']}")
