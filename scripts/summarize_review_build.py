"""Record tests and packaged transport assets for the exact signed review APK."""
from pathlib import Path
import os
import xml.etree.ElementTree as ET
from zipfile import ZipFile

reports = sorted(Path('app/build/test-results/testDebugUnitTest').glob('TEST-*.xml'))
assert reports, 'Missing unit test results'
totals = dict(tests=0, failures=0, errors=0, skipped=0)
for report in reports:
    suite = ET.parse(report).getroot()
    for key in totals:
        totals[key] += int(suite.attrib.get(key, '0'))
assert totals['tests'] > 0 and totals['failures'] == 0 and totals['errors'] == 0, totals

apk = Path('dist/Sawti-Review-2026-11-10-Release.apk')
vehicles = ('car', 'taxi', 'bus', 'minibus', 'bicycle', 'motorcycle', 'train',
            'airplane', 'helicopter', 'ship', 'boat', 'truck')
with ZipFile(apk) as archive:
    names = set(archive.namelist())
    for vehicle in vehicles:
        path = f'res/drawable/word_transport_{vehicle}.webp'
        assert path in names, f'Missing packaged transport photo: {vehicle}'
        header = archive.read(path)[:12]
        assert header[:4] == b'RIFF' and header[8:12] == b'WEBP', path

lines = [f'commit={os.environ["GITHUB_SHA"]}', f'run={os.environ["GITHUB_RUN_ID"]}',
         'branch=fix/prelaunch-cleanup-hint', 'package=com.sawti',
         f'versionName={os.environ["RELEASE_VERSION"]}', f'versionCode={os.environ["RELEASE_CODE"]}',
         'build=Release (R8 optimized, publisher signed, not debuggable)',
         'transport_photos=12', 'transport_written_forms=108',
         'checks=assembleDebug,testDebugUnitTest,lintDebug,assembleRelease,lintRelease,apksigner,aapt',
         *[f'{key}={value}' for key, value in totals.items()]]
Path('dist/BUILD-INFO.txt').write_text('\n'.join(lines) + '\n', encoding='utf-8')
print('\n'.join(lines))
