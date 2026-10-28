# Sawti TTS diacritization audit – 28 October 2026

## Architecture
- The existing Room field `ttsPrompt` already separates pronunciation from `displayText`; there is **no schema migration**.
- `WordItem.speechText` exposes that existing field explicitly, and `AudioDispatcher.play()` sends `item.speechText` to Android TTS when there is no user-recorded audio.
- `DialectCatalog.localize()` no longer replaces `ttsPrompt` with the unvocalized label. It generates a speech-only text for all default shared cards, including default words without a translation.
- The persisted `semanticKey`, row ID, custom photo, voice recording, and favorites are never changed by localization.

## Coverage
- Eight colloquial dialects: Libyan, Tunisian, Algerian, Moroccan, Egyptian, Syrian, Iraqi and Saudi/Gulf. Standard Arabic is also processed as a ninth selectable voice/dialect.
- **183 hand-specified pronunciations** target phonologically distinctive or particularly error-prone words (water, eggs, socks, animals, clothing, requests, actions and more).
- Every remaining bundled shared card receives a **deterministic marked fallback**. It retains the seed TTS for an unchanged label or marks the localized word for a changed label.
- `DialectSpeechCatalog.isFullyMarked()` verifies Arabic letter clusters, a short vowel/sukun on each mark-bearing consonant, and a final pause. Long alif is exempt, since sukun on a long-vowel carrier is not valid standard Arabic orthography.
- Written Iraqi `چ` is rendered phonetically as `تْش` for Android TTS voice compatibility. Final `ة` becomes pronounced `هْ` in the synthesized speech only.
- Country-specific dishes are excluded intentionally (future separate artwork/phonetics project). Custom cards and private family names keep caregiver-entered TTS text and recordings.

## Validation and limitations
- JVM unit tests were added to enumerate *all bundled shared seed cards across all nine selectable dialects*. They reject missing or insufficiently marked TTS text and verify that the semantic image key and media references do not change.
- The `[skip ci]` commit **does not run unit tests or an Android build**. The tests must run in the next requested CI build.
- **Syntactic mark coverage is not 100% natural phonetic accuracy.** The automatic fallback assigns a conservative vowel to unmarked consonants; its vowels are not linguistic proof. Different Android voices may ignore marks, mishandle initial consonant clusters or replace uncommon phones. Native-speaker listening checks are required for uncurated words before claiming full accent accuracy.
