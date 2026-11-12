"""Validate release metadata against the Android Gradle source of truth."""
import os
import re
import sys
from pathlib import Path

source = Path("app/build.gradle.kts").read_text()
version = re.search(r'versionName\s*=\s*"([^"]+)"', source).group(1)
code = int(re.search(r'versionCode\s*=\s*(\d+)', source).group(1))
assert re.fullmatch(r"\d+\.\d+", version), "Invalid versionName"
assert code >= 1, "versionCode must be a positive integer"
assert 'applicationId = "com.sawti"' in source
if len(sys.argv) > 1:
    badging = Path(sys.argv[1]).read_text()
    package = re.search(r"package: name='([^']+)' versionCode='([^']+)' versionName='([^']+)'", badging)
    assert package and package.groups() == ("com.sawti", str(code), version), "APK metadata mismatch"
    for setting, field in (("minSdk", "sdkVersion"), ("targetSdk", "targetSdkVersion")):
        expected = re.search(rf'{setting}\s*=\s*(\d+)', source).group(1)
        assert f"{field}:'{expected}'" in badging, f"APK {setting} differs from source"
    assert "application-debuggable" not in badging, "Production APK is debuggable"
    for permission in ("android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION",
                       "android.permission.READ_PHONE_STATE", "android.permission.READ_PHONE_NUMBERS"):
        assert permission not in badging, f"Unexpected permission in Sawti APK: {permission}"
elif os.environ.get("GITHUB_ENV"):
    with open(os.environ["GITHUB_ENV"], "a") as output:
        output.write(f"RELEASE_VERSION={version}\nRELEASE_CODE={code}\n")
print(f"Validated Sawti {version} ({code})")
