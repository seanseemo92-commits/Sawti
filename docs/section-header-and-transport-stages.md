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

Pending: stable concepts and 512px WebP photographs, nine dialect variants with vocalized speech text, safe idempotent insertion into existing databases, and custom-text/photo/recording preservation.

## Validation

Stage 1: commit `ecdd3664ce16fec8ce3bef10158a32a3710d9640`; Debug APK build, complete unit tests and lintDebug passed in run https://github.com/seanseemo92-commits/Sawti/actions/runs/42795357930. Stage 2 Android validation: pending. On-device transition and TTS listening checks are reported separately from automated compilation/tests.
