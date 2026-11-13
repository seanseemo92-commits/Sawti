"""Verify installation and launch of an explicitly provided Sawti APK on a test device.

Example: python3 scripts/check_android_install.py path/to/Sawti-v1.5.2-Release.apk
Never uninstalls an existing app or clears user data.
"""
import argparse
import json
import subprocess
import time
from pathlib import Path


def main():
    parser = argparse.ArgumentParser(description="Install a selected Sawti APK for device verification")
    parser.add_argument("apk", type=Path, help="Exact APK to test (must exist)")
    parser.add_argument("--package", default="com.sawti", help="Installed package ID")
    parser.add_argument("--activity", default="com.sawti.MainActivity", help="Launcher activity")
    parser.add_argument("--serial", help="adb device serial, when multiple devices are connected")
    args = parser.parse_args()
    apk = args.apk.resolve(strict=True)
    if apk.suffix.lower() != ".apk":
        parser.error("Only an APK file is accepted")
    prefix = ["adb"] + (["-s", args.serial] if args.serial else [])

    def adb(*command, check=True):
        completed = subprocess.run(prefix + list(command), text=True,
                                   capture_output=True, timeout=120)
        if check and completed.returncode:
            raise RuntimeError(f"adb {' '.join(command)}: {completed.stdout}\n{completed.stderr}")
        return completed.stdout.strip()

    report = {
        "apk": str(apk),
        "sdk": adb("shell", "getprop", "ro.build.version.sdk"),
        "abis": adb("shell", "getprop", "ro.product.cpu.abilist"),
        "model": adb("shell", "getprop", "ro.product.model"),
        "package": args.package,
    }
    adb("logcat", "-c")
    report["install"] = adb("install", "-r", str(apk))
    assert "Success" in report["install"], report
    component = f"{args.package}/{args.activity}"
    report["launch"] = adb("shell", "am", "start", "-W", "-n", component)
    assert "Status: ok" in report["launch"], report
    time.sleep(4)
    report["pid"] = adb("shell", "pidof", args.package)
    assert report["pid"], report
    report["same_signature_update"] = adb("install", "-r", str(apk))
    assert "Success" in report["same_signature_update"], report
    logs = adb("logcat", "-d", "-s", "AndroidRuntime:E")
    assert "FATAL EXCEPTION" not in logs or args.package not in logs, logs
    print("SAWTI_INSTALL_DIAGNOSTIC " + json.dumps(report, ensure_ascii=False), flush=True)


if __name__ == "__main__":
    main()
