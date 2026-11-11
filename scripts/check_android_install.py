"""Install and launch the exact delivered APK on a clean Android emulator."""
import json
import subprocess
import time
from pathlib import Path


def adb(*args, check=True):
    result = subprocess.run(["adb", *args], text=True, capture_output=True, timeout=120)
    if check and result.returncode:
        raise RuntimeError(f"adb {' '.join(args)}: {result.stdout}\n{result.stderr}")
    return result.stdout.strip()


apk = Path("previews/quran-passages-test/Sawti-Quran-Passages-test.apk")
report = {
    "apk": str(apk),
    "sdk": adb("shell", "getprop", "ro.build.version.sdk"),
    "abis": adb("shell", "getprop", "ro.product.cpu.abilist"),
    "model": adb("shell", "getprop", "ro.product.model"),
}
adb("logcat", "-c")
report["install"] = adb("install", "-r", str(apk))
assert "Success" in report["install"], report
report["launch"] = adb("shell", "am", "start", "-W", "-n", "com.sawti.quranpreview/com.sawti.MainActivity")
assert "Status: ok" in report["launch"], report
time.sleep(4)
report["pid"] = adb("shell", "pidof", "com.sawti.quranpreview")
assert report["pid"], report
report["same_signature_update"] = adb("install", "-r", str(apk))
assert "Success" in report["same_signature_update"], report
logs = adb("logcat", "-d", "-s", "AndroidRuntime:E")
assert "FATAL EXCEPTION" not in logs, logs
print("SAWTI_INSTALL_DIAGNOSTIC " + json.dumps(report, ensure_ascii=False), flush=True)
