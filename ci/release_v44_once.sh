#!/usr/bin/env bash
set -euo pipefail
# Trusted one-time release from the verified main source only.
# Release branch may add only this script and its trigger; Android source must match main.
changed="$(git diff --name-only 904a4724badcb3be0adde40d2358c59f362caa8e HEAD)"
while IFS= read -r file; do
    case "$file" in
        ci/release_v44_once.sh|.github/workflows/release-v44-once.yml|"") ;;
        *) echo "Unexpected release branch change: $file"; exit 1 ;;
    esac
done <<< "$changed"
grep -q 'applicationId = "com.sawti"' app/build.gradle.kts
grep -q 'versionName = "4.4"' app/build.gradle.kts
grep -q 'versionCode = 9' app/build.gradle.kts
test ! -f app/src/main/java/com/sawti/data/db/LegacyFamilyDefaults.kt
mkdir -p private-release app/signing release-output
gh run download 40346497322 --repo "$GITHUB_REPOSITORY" --name PRIVATE-MyWords-Publisher-Key-BACKUP --dir private-release
test -s private-release/mywords-publisher.p12
test -s private-release/KEEP_PRIVATE_SIGNING_INSTRUCTIONS.txt
PASS="$(sed -n 's/^Store and key password: //p' private-release/KEEP_PRIVATE_SIGNING_INSTRUCTIONS.txt | head -1 | tr -d '\r')"
test -n "$PASS"
echo "::add-mask::$PASS"
export SAWTI_KEYPASS="$PASS"
cp private-release/mywords-publisher.p12 app/signing/sawti-release.p12
cat >> app/build.gradle.kts <<'GRADLE'

android {
    signingConfigs {
        create("verifiedPublisher") {
            storeFile = file("signing/sawti-release.p12")
            storePassword = System.getenv("SAWTI_KEYPASS")
            keyAlias = "mywords-release"
            keyPassword = System.getenv("SAWTI_KEYPASS")
        }
    }
    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("verifiedPublisher")
        }
    }
}
GRADLE
gradle --no-daemon :app:assembleRelease :app:testDebugUnitTest :app:lintRelease
mv app/build/outputs/apk/release/app-release.apk release-output/Sawti-v4.4-Release.apk
APK="release-output/Sawti-v4.4-Release.apk"
test -s "$APK"
APKSIGNER="$(find "$ANDROID_HOME/build-tools" -maxdepth 2 -name apksigner -type f | sort -V | tail -1)"
AAPT="$(find "$ANDROID_HOME/build-tools" -maxdepth 2 -name aapt -type f | sort -V | tail -1)"
"$APKSIGNER" verify --verbose --print-certs "$APK" > release-output/signer-report.txt
grep -q 'Verified using v2 scheme (APK Signature Scheme v2): true' release-output/signer-report.txt
grep -q '754b679c29b0c8acf7c61ca265813d9528c0d0c3afe912b9319a6354cbd31b21' release-output/signer-report.txt
"$AAPT" dump badging "$APK" | grep -q "^package: name='com.sawti' versionCode='9' versionName='4.4'"
(cd release-output && sha256sum Sawti-v4.4-Release.apk > SHA256SUMS.txt)
# Sensitive signing backup is never added to an artifact or to the GitHub Release.
cat > release-output/NOTES.md <<'NOTES'
# صوتي | Sawti v4.4

Release improvements:
- Preserve modified words and family cards during vocabulary updates, without silent automatic deletion.
- Limit imported images to 15 MiB and audio to 50 MiB, and remove incomplete files on failure.
- Handle failed audio/photo imports and release playback resources reliably.
- Safer microphone recorder initialization and validation of empty recordings.
- Unit tests cover oversized, empty and unsafe media imports.

Verified: release build (R8), Android Lint, JVM unit tests, package identity, APK v2 signature and publisher certificate.

Package ID: com.sawti; versionCode: 9. APK is signed with the same private publisher key as v4.2, but v4.2 has a different package ID and is not upgradable in place. Preserve earlier data and backups before uninstalling the old app.

This is not a security penetration test and is not a substitute for on-device QA. No private key is distributed. Repository is private because its Git history contains older personal-data examples.
NOTES
if gh release view v4.4 --repo "$GITHUB_REPOSITORY" >/dev/null 2>&1; then
  echo 'Release v4.4 already exists. Refusing to overwrite.'
  exit 1
fi
gh release create v4.4 "$APK" release-output/SHA256SUMS.txt \
  --repo "$GITHUB_REPOSITORY" \
  --target 904a4724badcb3be0adde40d2358c59f362caa8e \
  --title 'صوتي | Sawti v4.4' --notes-file release-output/NOTES.md
