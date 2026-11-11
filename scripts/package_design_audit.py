"""Include measured native layouts before/after; do not treat screenshots as a device test."""
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
import base64
import json

root = Path('app/build/outputs')
cases = ('home-light-normal', 'home-light-large', 'home-dark-large', 'sentence-light-large',
         'sentence-dark-large', 'practice-recall-large', 'practice-choice-large', 'progress-dark-large')
metrics = {}
with ZipFile('dist/CODE-DESIGN-AUDIT.zip', 'w', ZIP_DEFLATED) as archive:
    for stage in ('before', 'after'):
        folder = root / ('design-audit-' + stage)
        records = []
        for name in cases:
            record = json.loads((folder / (name + '.json')).read_text())
            image = (folder / (name + '.png')).read_bytes()
            assert image.startswith(b'\x89PNG\r\n\x1a\n')
            records.append(record)
            archive.write(folder / (name + '.json'), stage + '/' + name + '.json')
            archive.write(folder / (name + '.png'), stage + '/' + name + '.png')
            print('SAWTI_DESIGN_SCREENSHOT ' + stage + '-' + name + ' ' + base64.b64encode(image).decode('ascii'))
        metrics[stage] = dict(cases=len(records), issues=sum(len(r['issues']) for r in records), records=records)
    assert metrics['after']['issues'] == 0, metrics['after']
    archive.writestr('metrics.json', json.dumps(metrics, ensure_ascii=False, indent=2))
    archive.write('docs/code-design-audit-1.4.md', 'code-design-audit-1.4.md')
    for folder in ('quran-review', 'about-review', 'greetings-review'):
        for image in (root / folder).glob('*.png'):
            archive.write(image, folder + '/' + image.name)
print('DESIGN_AUDIT_METRICS ' + json.dumps(metrics, ensure_ascii=False))
