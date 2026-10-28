# Sawti: signed distribution without disabling Play Protect

Google Play Protect is controlled by Android/Google, not by an app setting. A scan
recommendation for a sideloaded debug/unknown APK cannot be suppressed by Kotlin,
a manifest permission, a signing flag, or by changing the app's icon.

## Stable signing (required for production updates)

The existing CI workflow continues to build **debug** and **unsigned release**
artifacts for validation. A separate manual GitHub Actions workflow
`.github/workflows/release-signed.yml` produces a signed Release APK and
AAB when the repository owner provides a private, long-lived upload keystore.

Configure these GitHub **Actions repository secrets** securely:
- `SAWTI_RELEASE_KEYSTORE_BASE64`: base64 of the keystore binary, as **one line**
- `SAWTI_RELEASE_STORE_PASSWORD`: keystore password
- `SAWTI_RELEASE_KEY_ALIAS`: key alias
- `SAWTI_RELEASE_KEY_PASSWORD`: key password

Do not commit keystores, store passwords in source files, send signing keys in
chat, or print their contents in workflow logs. Keep an offline recovery copy
of the key and passwords. All future direct APK updates must use the same
release signing certificate; the debug certificate is **different**.

After configuring the secrets, run **Actions → Sawti signed release (manual) →
Run workflow** on `main`. The job verifies the signed APK and produces a
signed AAB for Play Console. It refuses to publish a release if secrets are absent.
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

## Artwork and theme validation

The daytime launcher resources remain unchanged. The dark launcher uses a
foreground **without a second internal rounded card**, allowing Android's
adaptive mask to own the crop. The previous raster welcome artwork was retired in favor of the new
**Sawti Aurora** native Compose screen. Its emblem uses the same scalable
`ic_launcher_foreground.xml` as the app icon; the new Arabic/English title
is real Android text instead of an upscaled PNG. See
`docs/aurora-splash.md` for startup/animation and visual QA.

Test on real devices: Android 7/8/13+, light and dark phone themes, app theme
overrides, circular launchers, home and word cards from each category, long
press, touch dwell, speech aura, and accessibility text sizes.
