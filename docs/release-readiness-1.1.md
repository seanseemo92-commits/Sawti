# Sawti 1.1 release readiness

Application source: `ad96f14eb526ebc0f75309d892f8608de1fb10c3` on `fix/prelaunch-cleanup-hint`. Main is unchanged. This is an incremental update of the current 1.0 baseline: package `com.sawti`, versionName `1.1`, versionCode `2`.

## Changes

- Add Settings → About Sawti with the existing launcher artwork, shared theme colors and ceramic card styles, developer name إسماعيل بن سالم, the installed package's actual version and a user-initiated email draft to ismail92sa@gmail.com. Missing/inaccessible mail apps show an Arabic message.
- Replace fixed-height, single-line settings choices with wrapping choices, a minimum 48dp touch target and radio selection semantics. Large Arabic text can expand vertically.
- Bind practice work to the resumed practice screen. Cancel pending questions, delayed advancement and speech on pause/navigation. Replay requests replace earlier pending requests. Return to a solved question resumes advancement once without logging another answer.
- Preserve current vocabulary, all nine dialects, photos and recordings. No Room schema, seed identifiers, restore format or user-data changes were introduced.

## Review and checks

The source review covered current settings/navigation/theme, practice playback, Quran ayah playback/cache and backup/restore boundaries. Existing suites cover database constraints, restore validation and rollback, shared-media ownership, dialect editing and initial selection, source catalogs, Quran resource layout and playback, and speech focus/cancellation. This review found no additional confirmed issue requiring a change to the backup or Quran paths.

The full validation job runs the complete unit suite, assembles Debug and checks Debug Lint before loading signing material. It then assembles the R8-optimized Release, checks Release Lint, verifies package metadata, ZIP integrity and the pinned publisher certificate, and checks packaged transport and dialect-flag resources. About tests cover light/dark rendering, 2× font scaling, explicit dark-title contrast, mail-draft addressing and missing mail apps; practice tests cover pending-question cancellation, replay replacement and pause/resume of a solved question.
Validation completed successfully: **252 tests, 0 failures, 0 errors, 0 skipped**. Debug assembly/Lint, optimized signed Release assembly/Lint, APK metadata, publisher signature and packaged-resource verification all passed. Rendered light, dark and 2×-font About screens were inspected; the dark heading uses the theme foreground and the app name is no longer duplicated.

Build validation: https://github.com/seanseemo92-commits/Sawti/actions/runs/42892515795

The publication job independently requires successful completion of that exact run, unchanged application/build/test inputs, matching APK checksum, metadata `com.sawti` / `1.1` / `2`, and the established publisher's SHA-256 certificate `754b679c29b0c8acf7c61ca265813d9528c0d0c3afe912b9319a6354cbd31b21`. It publishes the identical checked binary without rebuilding it or exposing signing material. The release tag includes the documentation/publication commit; a strict diff guard permits only README, this report and the publication workflow after the tested application commit. BUILD-INFO identifies the application build commit. The release includes test totals, source/run metadata, checksum, public signature report and rendered About screenshots.

Physical-device update/restore acceptance and listening with each installed TTS engine are outside the hosted automated checks. Success does not establish the absence of every possible defect or vulnerability.
