# Sawti 1.6.0 — Hybrid Regional Neural Voices

This is the proposed 1.6.0 source update. A signed production APK is **not** created by this PR.

- Keep caregiver recordings first, then exact-match offline neural MP3 packs, then Android TTS.
- Pick the best available Arabic voice for the selected country with Arabic-only fallbacks. Devices with no regional voice continue to work with general Arabic TTS.
- Support both downloaded packs and APK-embedded `assets/neural_tts/<dialect>/<semanticKey>.mp3` via a per-dialect exact-text manifest.
- A changed personal card speech text never reuses an outdated neural recording.
- Reuse the existing player/focus lifecycle and 25% notification ducking for offline neural audio.
- Export the nine dialect catalogs directly from Kotlin for the GitHub Actions Edge TTS pack generator, preserving exact approved written speech strings.
- Start with a Libyan-only generator run to control APK size. The Libyan pack has been generated and committed: 271 MP3 recordings + one exact-text manifest using ar-LY-ImanNeural. Remaining eight dialect packs can be generated via manual workflow dispatch. Generated assets must still undergo listening review before public release.
- Do not change Room database VERSION=1, Quran playback, or catalog revisions.

## Release validation

PR CI must pass `testDebugUnitTest`, `lintRelease`, `assembleDebug`, and `assembleRelease` before adoption. Real-device listening in each country remains required: automated synthesis does not certify dialect pronunciation. Check provider terms before redistributing synthesized voices.

## Voice output usage rights

The initial Libyan pack was generated via the community `edge-tts` tool. Do not assume the paid Azure Speech output license applies to Edge Read Aloud. Confirm Microsoft's applicable authorization for redistribution and public-app bundling before distributing a signed production build; if needed, replace these samples with properly licensed Azure Speech (paid tier) or recorded voices. Avoid advertising the neural audio as clinically or native-speaker validated before real-device listening.
