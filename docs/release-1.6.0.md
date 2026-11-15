# Sawti v1.6.0 — Nine-Dialect Neural Speech

This source release modernizes AAC card narration across the nine available Arabic dialects. A public signed production release must not be published until the quality and voice-output licensing checks below are resolved.

## Speech behavior

- Caregiver voice recordings always take priority.
- Unmodified, built-in cards use the offline neural MP3 identified by `(dialect, semanticKey)`, not by fragile equality against the previous diacritics.
- A caregiver's change to **either** the card label or the speech text bypasses the neural clip and respects the custom pronunciation. Personal categories are never matched by key.
- A missing or invalid neural recording falls back to dialect-aware Android TTS. Preserve the existing audio-focus handling and notification ducking.
- One `DialectSpeechCatalog.resolve` projection provides UI speech, phonemic cue, neural-export text and Android fallback. Retire conflicting legacy phonetic adjustment layers.
- The `edge-tts` generator synthesizes at `rate="-18%"`, records that rate in each dialect manifest and rejects stale-rate downloads. Normal playback runs the synthesized MP3 at 1.0×. Optional accessibility Slow mode runs at 0.85×.
- The Libyan pronunciation has been updated for `نِبِّي مَيَّة`, `دَحِي`, `رُوشِن`, `فِلْفِل`, first-person verbs, selected dishes, and family relationship compounds.
- Family, cousin, in-law and common action terms are localized across the seven other colloquial dialects; Standard Arabic remains available.

## Offline assets

The GitHub generation workflow exports speech directly from Kotlin and bundles **2,399 MP3 clips** at the corrected `-18%` synthesis rate: Libyan 271; Tunisian, Moroccan, Algerian, Egyptian, Syrian, Iraqi, Saudi and Standard Arabic 266 each. Each dialect contains `manifest.json` and individual concept-keyed MP3s.

## Final pronunciation correction pass (PR #50)

- Eight regional dialects have 14 individually vowelled greeting phrases each (112 explicit forms). Standard Arabic greeting pauses are corrected as well.
- 423 previously display-only dialect entries now carry explicit written speech candidates, including Iraqi mint tea "تشَاي", Syrian "حْرَام" for blanket and Moroccan "قْنِيَّة" for rabbit.
- Correct Libyan diphthongs, common verbs, in-law/family references and 22 problematic lexical forms. The approved Syrian/Moroccan amendments are reflected in the 245-entry speech fixture.
- Regenerate all **2,399 voice clips** after the correction commits at a native synthesis rate of **-18%**; normal playback remains 1.0×.
- Avoid stale downloaded recordings shadowing updated embedded clips by comparing local and embedded per-key manifest speech entries.
- Native-speaker listening and voice-output redistribution rights remain independent release requirements.

## Validation requirements

- Passing `testDebugUnitTest`, `lintRelease` and `assembleRelease` is required before merge.
- Validate all nine manifests, rate metadata, semantic keys and nonempty MP3 assets.
- Test on devices with variable screen size, Android API levels and without network access.
- **Pending:** native-speaker listening tests for real-world pronunciation in all nine dialects, including careful evaluation of slower synthetic pacing for people with aphasia. Automated textual tests are not linguistic or clinical approval.

## Voice licensing: release blocker pending clarification

These recordings were synthesized through the community `edge-tts` tool against Microsoft's Edge Read Aloud voices. The open-source wrapper's license does not itself grant permission to redistribute voice output. Do not assume that official paid-tier Azure AI Speech output permissions automatically extend to recordings made by this tool. Obtain Microsoft's applicable written permission for publicly bundling these outputs, **or replace them with a suitably licensed speech source**, before publishing signed APK/AAB assets.

No changes to the Room database schema or Quran recitation assets are intended in this release.
