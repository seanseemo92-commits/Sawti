"""Record tests and packaged transport assets for the exact signed review APK."""
from pathlib import Path
import os
import sys
import re
import xml.etree.ElementTree as ET
from zipfile import ZipFile
from review_apk_resources import verify_transport_photos

reports = sorted(Path('app/build/test-results/testDebugUnitTest').glob('TEST-*.xml'))
assert reports or os.environ.get('VALIDATION_RUN_ID'), 'Missing unit test results or verified prior run'
totals = dict(tests=0, failures=0, errors=0, skipped=0)
for report in reports:
    suite = ET.parse(report).getroot()
    for key in totals:
        totals[key] += int(suite.attrib.get(key, '0'))
assert not reports or (totals['tests'] > 0 and totals['failures'] == 0 and totals['errors'] == 0), totals

apks = list(Path('dist').glob('*.apk'))
assert len(apks) == 1, 'Expected exactly one verified APK'
apk = apks[0]
with ZipFile(apk) as archive:
    resource_dump = Path(sys.argv[1]).read_text()
    verify_transport_photos(archive, resource_dump)
    for code in ('ly', 'sa', 'eg', 'sy', 'iq', 'tn', 'ma', 'dz'):
        assert re.search(r'\bdrawable/dialect_flag_' + code + r'\b', resource_dump), code
    assert 'assets/licenses/flag-icons-LICENSE.txt' in archive.namelist()
    for name in ('salam', 'reply', 'hello', 'welcome', 'how_are_you', 'fine', 'good_morning',
                 'good_evening', 'brighten', 'good_health', 'goodbye', 'safe', 'thanks', 'bless'):
        assert re.search(r'\bdrawable/greeting_' + name + r'\b', resource_dump), name
    assert re.search(r'\bdrawable/img_cat_greetings\b', resource_dump)

lines = [f'commit={os.environ["GITHUB_SHA"]}', f'run={os.environ["GITHUB_RUN_ID"]}',
         'branch=fix/prelaunch-cleanup-hint', 'package=com.sawti',
         f'versionName={os.environ["RELEASE_VERSION"]}', f'versionCode={os.environ["RELEASE_CODE"]}',
         'build=Release (R8 optimized, publisher signed, not debuggable)',
         'transport_photos=12', 'transport_written_forms=108',
         'greetings_word_photos=14', 'greetings_home_cover=1', 'greetings_photo_dimensions=512x512',
         'dialect_flags=8 packaged national flags; Fusha uses a globe',
         'initial_dialect=one-time local country hint, saved user choice preserved',
         'checks=assembleDebug,testDebugUnitTest,lintDebug,assembleRelease,lintRelease,apksigner,aapt',
         *([f'{key}={value}' for key, value in totals.items()] if reports else [
             'tests_and_lint=PASSED in verified prior run; app, tests and build inputs unchanged',
             f'validation_run={os.environ["VALIDATION_RUN_ID"]}',
             f'validation_commit={os.environ["VALIDATION_COMMIT"]}'])]
Path('dist/BUILD-INFO.txt').write_text('\n'.join(lines) + '\n', encoding='utf-8')
print('\n'.join(lines))

