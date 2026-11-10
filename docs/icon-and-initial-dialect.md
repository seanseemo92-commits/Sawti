# Approved icon, dialect flags and first-run dialect

User-approved design: white flowing speaking-face contour, inner white sound wave and outer gold wave on purple. Native day/night adaptive foregrounds, legacy foreground and Android themed monochrome icon use the same geometry. All 30 legacy PNGs are generated from the native vectors at the same visual scale as adaptive icons. The foreground stays within the 33dp safe circle (measured 32.68dp).

Eight bundled country flags appear next to the dialect names and the current dialect in settings; Fusha uses a globe. SVG sources and MIT attribution are retained, with the license bundled in the APK. Flag images are independent of device emoji versions.

Initial dialect is inferred locally only when no dialect key has been saved: SIM provider country first, registered network country if unavailable, then system locale region. Supported countries map to the existing eight regional dialects; unavailable or unsupported countries use Fusha. This is a country hint, not proof of nationality. No GPS, IP lookup, country tracking, phone identifiers or new permissions are used.

The chosen default is persisted once. Existing/manual choices, including Fusha, and restored settings always win. No database/schema change is needed. Regression tests cover supported countries, roaming priorities, missing sources, unsupported countries, persistence, saved Fusha/voice settings, resolver failures, actual SIM lookup and packaged flag resources.

Validation: native XML, 30 launcher PNGs and all eight 144×108 flag PNGs verified locally; packaging-verifier tests passed. Full Android tests, Lint and signed Release build are pending.
