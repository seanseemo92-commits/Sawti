# Approved icon, dialect flags and first-run dialect

User-approved design: white flowing speaking-face contour, inner white sound wave and outer gold wave on purple. Native day/night adaptive foregrounds, legacy foreground and Android themed monochrome icon use the same geometry. All 30 legacy PNGs are generated from the native vectors at the same visual scale as adaptive icons. The foreground stays within the 33dp safe circle (measured 32.68dp).

Eight bundled country flags appear next to the dialect names and the current dialect in settings; Fusha uses a globe. SVG sources and MIT attribution are retained, with the license bundled in the APK. Flag images are independent of device emoji versions.

Initial dialect is inferred locally only when no dialect key has been saved: SIM provider country first, registered network country if unavailable, then system locale region. Supported countries map to the existing eight regional dialects; unavailable or unsupported countries use Fusha. This is a country hint, not proof of nationality. No GPS, IP lookup, country tracking, phone identifiers or new permissions are used.

The chosen default is persisted once. Existing/manual choices, including Fusha, and restored settings always win. No database/schema change is needed. Regression tests cover supported countries, roaming priorities, missing sources, unsupported countries, persistence, saved Fusha/voice settings, resolver failures, actual SIM lookup and packaged flag resources.

Validation: native XML, 30 launcher PNGs and all eight 144×108 flag PNGs verified locally; packaging-verifier tests passed. Full build run https://github.com/seanseemo92-commits/Sawti/actions/runs/42814639257 succeeded for commit `99160378d2544ed96922a729a94f47ffeb556ec2`: Debug build, 233 unit tests (zero failures/errors/skips), lintDebug, signed optimized Release/R8, lintRelease, publisher certificate, package/version, ZIP integrity, transport photo mappings, eight flag resources and bundled attribution. The final APK also passed the check forbidding added location/phone-reading permissions.

Icon checkpoint: `8a22f5c6822423e5167349ebfee79d78b53d6a10`. Feature checkpoint: `99160378d2544ed96922a729a94f47ffeb556ec2`. No changes to main (`0dc7b28b7559182e03ada314b63a959de273ac31`).

Signed APK: `Sawti-Icon-Dialects-2026-11-10-Release.apk` (24,074,592 bytes), publisher certificate SHA-256 `754b679c29b0c8acf7c61ca265813d9528c0d0c3afe912b9319a6354cbd31b21`, version 1.0 / code 1. Delivered through the private pre-release fallback because Actions artifact quota is exhausted: https://github.com/seanseemo92-commits/Sawti/releases/tag/sawti-review-42814639257-1.
