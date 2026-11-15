# Sawti 1.6.0 — Hybrid Regional Neural Voices

This is the proposed 1.6.0 source update. A signed production APK is **not** created by this PR.

- Keep caregiver recordings first, then exact-match offline neural MP3 packs, then Android TTS.
- Pick the best available Arabic voice for the selected country with Arabic-only fallbacks. Devices with no regional voice continue to work with general Arabic TTS.
- Support both downloaded packs and APK-embedded `assets/neural_tts/<dialect>/<semanticKey>.mp3` via a per-dialect exact-text manifest.
- A changed personal card speech text never reuses an outdated neural recording.
- Reuse the existing player/focus lifecycle and 25% notification ducking for offline neural audio.
- Export the nine dialect catalogs directly from Kotlin for the GitHub Actions Edge TTS pack generator, preserving exact approved written speech strings.
- Start with a Libyan-only generator run to control APK size. No generated neural MP3s are in this source commit; run the generator workflow explicitly after merging/activating it.
- Do not change Room database VERSION=1, Quran playback, or catalog revisions.

## Release validation

PR CI must pass `testDebugUnitTest`, `lintRelease`, `assembleDebug`, and `assembleRelease` before adoption. Real-device listening in each country remains required: automated synthesis does not certify dialect pronunciation. Check provider terms before redistributing synthesized voices.
