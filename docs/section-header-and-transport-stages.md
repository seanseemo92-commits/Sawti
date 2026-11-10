# Section colors, startup, speaking-person icon and transport

Approved scope: shared animated toolbar/status-bar colors, direct home startup, a speaking-person launcher icon, and a photographed transport vocabulary category for all nine Arabic variants. Work stays on `fix/prelaunch-cleanup-hint`; no merge to main.

## Stage 1 — header and startup

- Use the existing section banner colors for the shared header/status-bar background.
- Animate all three gradient stops together over 360 ms; performance mode uses an immediate transition.
- Derive toolbar ink and status-icon brightness from the currently rendered colors.
- Open AppRoot directly without the welcome Continue screen; the OS-owned Android startup surface remains platform-managed.
- Test contrast throughout transitions and retain the existing category/data-loading and backup guards.

## Stage 2 — launcher identity

Implemented a native speaking-person profile with three sound waves aligned to the mouth. Updated day/night adaptive vectors, the themed monochrome icon, the legacy foreground, and all 30 legacy PNG variants. The Android 12 system launch icon uses the same native vector. Validated XML decoding, PNG dimensions, day/night rendering, and the 32.67dp foreground radius inside the 33dp adaptive safe zone.

## Stage 3 — transport vocabulary and photos

Implemented 12 stable concepts: car, taxi, bus, minibus, bicycle, motorcycle, train, airplane, helicopter, ship, boat and truck. Each has a distinct generated 512×512 WebP photograph and explicit display/speech forms in all nine Arabic variants (108 forms). The new section is appended without reordering existing sections and uses a petrol-blue header accent. Written content is recorded in `transport-vocabulary-review.tsv`; native-speaker and device-listening review remain pending.

Existing transactional insertion adds only missing category/concept IDs; no schema change or destructive migration is required. Tests cover upgrade from the 365-card baseline, reseeding and reopening, unchanged old rows and attached media, retained practice history, dialect projections, customized text, and packaged image decoding. Final integration CI is pending.

## Validation

Stage 1: commit `ecdd3664ce16fec8ce3bef10158a32a3710d9640`; Debug APK build, complete unit tests and lintDebug passed in run https://github.com/seanseemo92-commits/Sawti/actions/runs/42795357930. Stage 2: commit `a37138e3077d72c21429306a065b06261c9d9097`; Debug APK build, unit tests and lintDebug passed in run https://github.com/seanseemo92-commits/Sawti/actions/runs/42796413089. On-device transition and TTS listening checks are reported separately from automated compilation/tests.
