# Sawti: signed distribution without disabling Play Protect

Google Play Protect is controlled by Android/Google, not by an app setting. A scan
recommendation for a sideloaded debug/unknown APK cannot be suppressed by Kotlin,
a manifest permission, a signing flag, or by changing the app's icon.

## Stable signing (required for production updates)

The existing CI workflow continues to build **debug** and **unsigned release**
artifacts for validation. A separate GitHub Actions workflow
`.github/workflows/release-signed.yml` produces a signed Release APK and
AAB using the original private publisher keystore. Repository secrets are preferred; when all four are absent, the workflow reuses the private backup from run 40346497322, exactly as the previous v4.4 release did. Validation checks that the APK and AAB certificates match each other **and** match the independent historical publisher SHA-256 fingerprint `754b679c29b0c8acf7c61ca265813d9528c0d0c3afe912b9319a6354cbd31b21`, which was verified by the v4.6.0 workflow against the published v4.4 APK. This protects continuity of the signing identity; it does not replace installation and restore tests. Partial secret configuration fails closed. It runs when the app build configuration changes on main, or by manual dispatch.

Configure these GitHub **Actions repository secrets** securely:
- `SAWTI_RELEASE_KEYSTORE_BASE64`: base64 of the keystore binary, as **one line**
- `SAWTI_RELEASE_STORE_PASSWORD`: keystore password
- `SAWTI_RELEASE_KEY_ALIAS`: key alias
- `SAWTI_RELEASE_KEY_PASSWORD`: key password

Do not commit keystores, store passwords in source files, send signing keys in
chat, or print their contents in workflow logs. Keep an offline recovery copy
of the key and passwords. All future direct APK updates must use the same
release signing certificate; the debug certificate is **different**. The new foundation version is `1.0` with `versionCode=1`; this **cannot update** an already-installed old version with a higher versionCode even when certificates match. Treat new installs separately; preserve/export user data before considering uninstall or migration.

After configuring the secrets, run **Actions → Sawti signed release →
Run workflow** on `main`. The job verifies the signed APK and produces a
signed AAB for Play Console. The **Publish versioned release** step is disabled (`if: false`) and the workflow has read-only contents permission: successful signing uploads private workflow artifacts but **does not publish a GitHub Release**. Public publication requires a separately approved change.

**Security follow-up:** The fallback currently downloads an old private Actions artifact that contains both the publisher key and instructions holding its password. Even though the repository is private, keeping both together broadens access risk. After securely recording the same certificate and storing the key/password in the four repository secrets, remove this fallback in a separate reviewed change. The backup artifact expires on 2027-01-24; never delete the last recoverable key before verifying an offline backup. No private key is uploaded in the newly generated signed-release artifact.
The normal build workflow never claims that its unsigned Release APK is installable.

## Avoid repeated Play Protect prompts through official Google Play testing

1. Create/prepare the app in Google Play Console using the **same applicationId**
   (`com.sawti`) and ensure each new store release has a higher `versionCode`.
2. Upload the signed AAB to **Testing → Internal testing** and add tester emails.
3. Accept the tester invitation and install/update **through Google Play**.

Internal testing is preferable to unknown sideloaded APKs and does not require
disabling Play Protect. Google still decides security prompts and app reputation,
so no workflow can promise a guaranteed absence of scans.

**Important data warning:** Uninstalling a debug-signed version to install a
release-signed version can erase app-private data. Export and verify a backup
first, and only reinstall when the restore/restart workflow has been tested.

## Current verification (2026-11-02)

Signed v1.0 APK/AAB compilation and the APK/AAB cross-certificate check passed on [Actions run 41416103375](https://github.com/seanseemo92-commits/Sawti/actions/runs/41416103375). The signed files were uploaded as private workflow artifacts; the publication step was skipped. The newly restored historical-certificate pin must still pass a new signed CI run before final approval. The code has not been tested for actual user interactions, speech output, or restore/process-death behavior on a physical device. Android Lint also still reports warnings, and the speech review corpus remains unapproved by native speakers.

## Artwork and theme validation

The welcome screen uses the approved light/dark WebP artwork rendered in Compose.
The button artwork and developer credits are embedded in those images; the interactive
hit target and press feedback are native Compose. Dark mode uses the light button
reference, and the copyright strip scales uniformly. See `docs/aurora-splash.md`.

Test on real devices: Android 7/8/13+, light and dark phone themes, app theme
overrides, circular launchers, home and word cards from each category, long
press, touch dwell, speech aura, and accessibility text sizes.

