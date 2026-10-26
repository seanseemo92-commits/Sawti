"""One-time rename of Sawti's Android technical identity, preserving source semantics.

Runs on a temporary branch and is deleted before merging. This changes the
Android package ID: the renamed app is a fresh installation, NOT an upgrade.
"""
from pathlib import Path
import re

root = Path(".").resolve()
old = root / "app/src/main/java/com/mywords"
new = root / "app/src/main/java/com/sawti"
test_old = root / "app/src/test/java/com/mywords"
test_new = root / "app/src/test/java/com/sawti"
assert old.is_dir() and not new.exists()
assert test_old.is_dir() and not test_new.exists()
assert (old / "MyWordsApp.kt").is_file()
assert (root / "app/src/main/res/values/strings.xml").is_file()
assert (root / "settings.gradle.kts").read_text().find('rootProject.name = "Sawti"') >= 0

supported = {".kt", ".kts", ".xml", ".pro", ".properties", ".md"}
count = 0
for p in sorted(root.rglob("*")):
    if not p.is_file() or p.suffix not in supported:
        continue
    if any(part in {".git", ".github", "ci", ".gradle", "build"} for part in p.relative_to(root).parts):
        continue
    s = p.read_text(encoding="utf-8")
    clean = s.replace("MYWORDS", "SAWTI").replace("MyWords", "Sawti").replace("mywords", "sawti")
    if p.relative_to(root).as_posix() == "app/build.gradle.kts":
        assert clean.count('versionCode = 7') == 1
        assert clean.count('versionName = "4.2"') == 1
        clean = clean.replace('versionCode = 7', 'versionCode = 8')
        clean = clean.replace('versionName = "4.2"', 'versionName = "4.3"')
    if p.name == "README.md":
        clean = clean.replace("**الإصدار 4.2**", "**الإصدار 4.3**")
        clean = clean.replace("نسخة APK الموقّعة منشورة في GitHub Release v4.2.",
            "الإصدار v4.3 يستخدم هوية Android الجديدة com.sawti. لا يُثبَّت كتحديث فوق التطبيق ذي المعرّف السابق، ولا يرث بياناته تلقائيًا. احتفظ بنسخة احتياطية من بياناتك.")
    if clean != s:
        p.write_text(clean, encoding="utf-8")
        count += 1

old.rename(new)
test_old.rename(test_new)
(new / "MyWordsApp.kt").rename(new / "SawtiApp.kt")

manifest = (root / "app/src/main/AndroidManifest.xml").read_text()
build = (root / "app/build.gradle.kts").read_text()
app = (new / "SawtiApp.kt").read_text()
assert 'android:name=".SawtiApp"' in manifest
assert 'android:theme="@style/Theme.Sawti"' in manifest
assert 'applicationId = "com.sawti"' in build
assert 'namespace = "com.sawti"' in build
assert 'versionCode = 8' in build and 'versionName = "4.3"' in build
assert 'class SawtiApp' in app
assert (root / "app/src/main/res/values/themes.xml").read_text().find('Theme.Sawti') >= 0
assert (root / "app/src/main/res/values/strings.xml").read_text().find('name="app_name">صوتي') >= 0

for p in sorted(root.rglob("*")):
    if any(part in {".git", ".github", "ci", ".gradle", "build"} for part in p.relative_to(root).parts):
        continue
    if "mywords" in p.relative_to(root).as_posix().lower():
        raise AssertionError(f"Old project path remains: {p}")
    if p.is_file() and p.suffix in supported:
        if "mywords" in p.read_text(encoding="utf-8").lower():
            raise AssertionError(f"Old project text remains: {p}")

print(f"Renamed {count} files, all code paths, namespace and applicationId to Sawti 4.3")
