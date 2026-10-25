# MyWords development audit

The repository currently stores the Android project as `MyWords.zip`, rather than version-controlled source files. This prevents useful per-file code review, diffs, automated builds and reliable incremental fixes.

## Required repository restructuring
1. Extract the ZIP into the repository root (including `app/`, `gradle/`, `settings.gradle.kts`, and `build.gradle.kts`), preserving source file paths.
2. Add trusted Gradle 8.9 wrapper scripts and JAR.
3. Add CI to compile and test the Android project.
4. Verify Room migration 1→2 against a real legacy v1 database before release; the uploaded source drops `family_members`, `voice_recordings`, and `pain_reports` after copying only selected fields.
5. Verify Arabic TTS fallback and RTL layout on devices/emulators.

Do not merge to main or ship as production-ready until these are verified. Preserve existing user data and media files.
