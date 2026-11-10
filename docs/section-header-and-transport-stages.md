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

Existing transactional insertion adds only missing category/concept IDs; no schema change or destructive migration is required. Tests cover upgrade from the 365-card baseline, reseeding and reopening, unchanged old rows and attached media, retained practice history, dialect projections, customized text, and packaged image decoding. Final integration CI passed.

## Validation

Stage 1: commit `ecdd3664ce16fec8ce3bef10158a32a3710d9640`; Debug APK build, complete unit tests and lintDebug passed in run https://github.com/seanseemo92-commits/Sawti/actions/runs/42795357930. Stage 2: commit `a37138e3077d72c21429306a065b06261c9d9097`; Debug APK build, unit tests and lintDebug passed in run https://github.com/seanseemo92-commits/Sawti/actions/runs/42796413089. On-device transition and TTS listening checks are reported separately from automated compilation/tests.

Stage 3: integration commit `f4e8c54b4d805742305eddf3737b59150ddeda30`; successful run https://github.com/seanseemo92-commits/Sawti/actions/runs/42799419399 on 2026-11-10. Verified `assembleDebug`, the complete `testDebugUnitTest` suite, `lintDebug`, `assembleRelease` including R8, and `lintRelease`. Release validation was unsigned; this run did not publish an APK or consume artifact storage. Photo checkpoints are `497f51f`, `561c93f`, `1d852eb` and `7a7d3f3`.

## Remaining device review

- Check smooth section/status-bar transitions in light/dark mode, back navigation, cutout devices and performance mode.
- Check the speaking-person launcher icon in the device launcher (including themed icons).
- Listen to the 108 transport speech candidates with the installed Arabic TTS engine and selected voice; written dialect forms do not imply nine native recorded voices or completed native-speaker review.

All requested code/content stages are saved on the work branch. `main` remains unchanged at `0dc7b28b7559182e03ada314b63a959de273ac31`.
