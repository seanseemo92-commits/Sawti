"""Version-independent foundation checks shared by PR CI and the source audit."""
from pathlib import Path
import re
import xml.etree.ElementTree as ET

root = Path('.')
def require(condition, message):
    if not condition:
        raise AssertionError(message)

require('rootProject.name = "Sawti"' in Path('settings.gradle.kts').read_text(), 'Wrong project identity')
gradle = Path('app/build.gradle.kts').read_text()
version = re.search(r'versionName\s*=\s*"([^"]+)"', gradle).group(1)
code = int(re.search(r'versionCode\s*=\s*(\d+)', gradle).group(1))
require(re.fullmatch(r'\d+\.\d+', version) is not None and code > 0, 'Invalid version')
require('applicationId = "com.sawti"' in gradle, 'Wrong package')
android = '{http://schemas.android.com/apk/res/android}'
manifest = ET.parse('app/src/main/AndroidManifest.xml').getroot()
app = manifest.find('application')
require(app is not None and app.get(android + 'allowBackup') == 'false', 'Cloud backup must stay disabled')
require(app.get(android + 'dataExtractionRules') == '@xml/data_extraction_rules',
        'Explicit Android 12+ backup and transfer exclusions are missing')
rules = ET.parse('app/src/main/res/xml/data_extraction_rules.xml').getroot()
domains = {'root', 'file', 'database', 'sharedpref', 'external', 'device_root',
           'device_file', 'device_database', 'device_sharedpref'}
for mode in ('cloud-backup', 'device-transfer'):
    section = rules.find(mode)
    require(section is not None and not section.findall('include'), 'Automatic backup was re-enabled')
    require({(entry.get('domain'), entry.get('path')) for entry in section.findall('exclude')} ==
            {(domain, '.') for domain in domains}, 'Personal data exclusion is incomplete: ' + mode)
require(app.get(android + 'supportsRtl') == 'true', 'Arabic RTL support missing')
strings = ET.parse('app/src/main/res/values/strings.xml').getroot()
require(any(e.get('name') == 'app_name' and e.text == 'صوتي' for e in strings), 'Wrong app name')
activity = Path('app/src/main/java/com/sawti/MainActivity.kt').read_text()
require('AppRoot(' in activity and 'SawtiAuroraSplash' not in activity, 'Retired welcome gate returned')
for name in ('LegacyFamilyCleanup.kt', 'LegacyFamilyDefaults.kt'):
    require(not Path('app/src/main/java/com/sawti/data/db', name).exists(), 'Retired family cleanup returned')
for name in ('sawti_adaptive_brand.xml', 'ic_launcher_foreground.xml'):
    require(Path('app/src/main/res/drawable', name).is_file(), 'Missing launcher vector: ' + name)
for name in ('app/src/main/res/drawable-night/sawti_adaptive_brand.xml',
             'app/schemas/com.sawti.data.db.AppDatabase/1.json'):
    require(Path(name).is_file(), 'Missing protected resource/schema: ' + name)
for path in ('app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml',
             'app/src/main/res/values-v31/themes.xml', 'app/src/main/res/values-night-v31/themes.xml'):
    require('sawti_adaptive_brand' in Path(path).read_text(), 'Wrong native launcher/splash artwork: ' + path)
for dpi in ('mdpi', 'hdpi', 'xhdpi', 'xxhdpi', 'xxxhdpi'):
    for kind in ('ic_launcher', 'ic_launcher_round'):
        for folder, name in ((f'mipmap-{dpi}', f'{kind}.png'), (f'mipmap-{dpi}', f'{kind}_night.png'),
                             (f'mipmap-night-{dpi}', f'{kind}.png')):
            path = Path('app/src/main/res', folder, name)
            require(path.is_file() and path.stat().st_size > 0, 'Missing launcher raster: ' + str(path))
for path in root.rglob('*'):
    if path.is_file() and '.git' not in path.parts and 'build' not in path.parts:
        require(path.suffix.lower() not in ('.jks', '.p12', '.keystore'), 'Signing key found in repository source')
        if path.suffix in ('.kt', '.kts', '.xml'):
            require(re.search(r'(?:Warning: truncated output|\d+ tokens truncated|Total output lines:)',
                              path.read_text(encoding='utf-8')) is None,
                    'Truncated tool output found inside application source: ' + str(path))
require(Path(f'docs/release-{version}.md').is_file(), 'Release notes must match the current version')
print(f'Repository baseline OK: Sawti {version} ({code}); RTL, manual backup, current launcher and schema intact')
