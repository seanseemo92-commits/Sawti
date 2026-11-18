#!/usr/bin/env python3
"""Audit the six real-model Libyan pilot clips; listening remains mandatory.

Codec, duration, hashes and waveform differences cannot certify pronunciation.
The report deliberately records listeningApproval as pending, even on success.
"""
import argparse
import itertools
import json
from pathlib import Path

from generate_habibi_tts import PIPELINE_VERSION, minimum_speech_seconds, atomic_json
from habibi_contract import file_sha256
from verify_habibi_packs import inspect_mp3, validate_timing

PILOT_KEYS = (
    'family.mother', 'family.father', 'family.husband',
    'request.water', 'request.toilet', 'request.adjust_pillow',
)


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


def review(root: Path, catalog: Path, baseline: Path, report: Path):
    from habibi_contract import load_catalog
    expected = load_catalog(catalog)['LIBYAN']
    manifest = json.loads((root / 'libyan/manifest.json').read_text())
    if manifest.get('pipelineVersion') != PIPELINE_VERSION:
        raise ValueError('Pilot was not generated with the repaired pipeline')
    if set(manifest['entries']) != set(PILOT_KEYS):
        raise ValueError('Expected exactly the six approved pilot keys')
    clips, waves = {}, {}
    for key in PILOT_KEYS:
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
            if file_sha256(old) == entry['sha256']:
                raise ValueError(f'Pilot reused old audio: {key}')
    pairs = []
    for first, second in itertools.combinations(PILOT_KEYS, 2):
        score = similarity(waves[first], waves[second])
        pairs.append({'first': first, 'second': second, 'correlation': score})
        if expected[first] != expected[second] and score >= .995:
            raise ValueError(f'Different words have near-identical audio: {first}, {second}: {score}')
    result = {'technicalChecks': 'passed', 'listeningApproval': 'pending',
              'pipelineVersion': PIPELINE_VERSION, 'clips': clips, 'pairs': pairs}
    atomic_json(report, result)
    return result


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--root', type=Path, required=True)
    parser.add_argument('--catalog', type=Path, default=Path('app/build/speech_catalog.json'))
    parser.add_argument('--baseline', type=Path, default=Path('app/src/main/assets/neural_tts'))
    parser.add_argument('--report', type=Path, required=True)
    args = parser.parse_args()
    result = review(args.root, args.catalog, args.baseline, args.report)
    print(f"Six clips passed technical checks. Pronunciation/listening approval: {result['listeningApproval']}")
