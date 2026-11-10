"""Resolve logical drawable identities through aapt2's actual APK resource table."""
import re

VEHICLES = ('car', 'taxi', 'bus', 'minibus', 'bicycle', 'motorcycle', 'train',
            'airplane', 'helicopter', 'ship', 'boat', 'truck')


def verify_transport_photos(archive, resource_dump):
    entries = list(re.finditer(r'(?m)^\s*resource\s+0x[0-9a-fA-F]+\s+([^\s]+)', resource_dump))
    resources = {}
    for index, entry in enumerate(entries):
        name = entry.group(1).rstrip(':').split(':')[-1]
        end = entries[index + 1].start() if index + 1 < len(entries) else len(resource_dump)
        resources[name] = resource_dump[entry.end():end]
    paths = []
    for vehicle in VEHICLES:
        name = f'drawable/word_transport_{vehicle}'
        assert name in resources, f'Missing logical APK resource: {name}'
        candidates = set(re.findall(r'\bres/[^\s\"\'()<>]+', resources[name]))
        candidates = {value.rstrip(',;') for value in candidates}
        assert len(candidates) == 1, f'Expected one packaged photo for {name}: {candidates}'
        path = candidates.pop()
        assert path in archive.namelist(), f'Missing packaged file for {name}: {path}'
        with archive.open(path) as stream:
            header = stream.read(12)
        assert header[:4] == b'RIFF' and header[8:12] == b'WEBP', name
        paths.append(path)
    assert len(set(paths)) == len(VEHICLES), 'Transport photos must be distinct resources'
    return paths
