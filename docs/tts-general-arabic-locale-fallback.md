# Sawti: stable Android TTS engine language

The Android TTS engine now receives the same language candidates for **every** selected
dialect: `ar`, then `ar-SA`, then `Locale.getDefault()` as a last resort.
There are **no** requests to set an `ar-TN`, `ar-LY`, `ar-MA`, etc. locale.

Dialect variation is supplied exclusively by the already-separated `speechText` /
`ttsPrompt` (marked phonetic text) in `AudioDispatcher`. Selecting a new dialect
updates this text via the existing repository without changing the engine language.
A manually selected voice is applied only when its declared locale is generic
`ar` or `ar-SA`. The voice picker is limited to those compatible voices.

Engine initialization and playback failures are now distinguished:
- `EngineUnavailable` appears only when Android package discovery confirms no
  TTS service. A TTS engine existing but failing initialization produces the
  separate `PlaybackFailed` issue.
- Unsupported Arabic locales do not disable speech; the device default is tried.
- `TextToSpeech.speak()` returning ERROR retries fixed fallback locales, then
  reports a playback error if it still cannot speak.
- Runtime utterance failures no longer display the false "No TTS engine" warning.
- `MissingArabicVoice` remains available for future explicit voice-management
  workflows, rather than mistaking absent region-specific voice data for no engine.

Limitations: an installed but broken TTS engine may still fail all language fallbacks.
A locale fallback also cannot create missing Arabic voice data, or guarantee the
accent of an engine that supports only general Arabic. On-device verification is
required before treating the bug as resolved on all Android vendors.

**No APK build was requested for this change**. The pure JVM
`ArabicTtsLocalePolicyTest` is committed but unexecuted under `[skip ci]`.
