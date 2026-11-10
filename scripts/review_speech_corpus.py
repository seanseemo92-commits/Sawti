"""Package the actual production projection; text coverage is not listening approval."""
import csv
import base64
import gzip
import io
import json
import re
from collections import Counter
from pathlib import Path
from zipfile import ZIP_DEFLATED, ZipFile

corpus = Path('app/build/reports/speech-review.tsv')
with corpus.open() as stream:
    rows = list(csv.DictReader(stream, delimiter='\t'))
baseline = gzip.decompress(base64.b64decode(Path('docs/speech-baseline-1.2.tsv.gz.b64').read_bytes())).decode('utf-8')
before_rows = list(csv.DictReader(io.StringIO(baseline), delimiter='\t'))
key = lambda row: (row['dialect'], row['semantic_key'])
before = {key(row): row['speech'] for row in before_rows}
assert len(rows) == len(before_rows) == len(before) == 2399
assert {key(row) for row in rows} == before.keys()
assert len({row['dialect'] for row in rows}) == 9
assert all(row['speech'].strip() and row['native_review'] == row['tts_listening'] == 'PENDING' for row in rows)
assert not any(row['source'] == 'UNVOCALIZED_FALLBACK' for row in rows)
assert not any(re.search(r'ة[ً-ٟ]*[؟!،.\s]*$', row['speech']) for row in rows)
changes = [(row['dialect'], row['semantic_key'], before[key(row)], row['speech'])
           for row in rows if before[key(row)] != row['speech']]
output = Path('app/build/reports/speech-review'); output.mkdir(exist_ok=True)
with (output / 'changes.tsv').open('w') as stream:
    writer = csv.writer(stream, delimiter='\t', lineterminator='\n')
    writer.writerow(['dialect', 'semantic_key', 'before', 'after'])
    writer.writerows(changes)
metrics = dict(rows=len(rows), dialects=9, changed_speech_rows=len(changes),
               remaining_unvocalized_fallbacks=0, explicit_candidate_revisions=210,
               rows_by_dialect=dict(Counter(row['dialect'] for row in rows)),
               changed_by_dialect=dict(Counter(row[0] for row in changes)),
               native_listening='PENDING', android_device_listening='PENDING',
               baseline_commit='6d78eeec30b501e87211452c70690aeca8b11842')
(output / 'metrics.json').write_text(json.dumps(metrics, ensure_ascii=False, indent=2) + '\n')
Path('dist').mkdir(exist_ok=True)
with ZipFile('dist/SPEECH-REVIEW.zip', 'w', ZIP_DEFLATED) as archive:
    for path in [corpus, output / 'changes.tsv', output / 'metrics.json',
                 Path('docs/speech-candidates-1.3.tsv'), Path('docs/speech-pronunciation-1.3.md')]:
        archive.write(path, path.name)
print(json.dumps(metrics, ensure_ascii=False))
print('SAWTI_SPEECH_CORPUS ' + base64.b64encode(gzip.compress(corpus.read_bytes(), mtime=0)).decode('ascii'))
