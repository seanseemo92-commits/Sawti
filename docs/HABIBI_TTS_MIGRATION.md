# Sawti / Habibi-TTS — migration status

Branch: migration/habibi-tts-20261116 (main and its published releases stay untouched).

## Architecture
- AppDatabase.VERSION remains 1. No Room schema or migrations are changed.
- The existing semantic-key asset path is preserved:
  app/src/main/assets/neural_tts/<dialect>/<semantic_key>.mp3
- HabibiAudioResolver accepts ONLY complete Habibi-TTS Unified v2 manifests,
  with dialect tag, exact clean text, SHA-256 and mono 24 kHz/96 kbps.
- AudioDispatcher never invokes Android TTS for a built-in card, even when
  an MP3 is missing or fails. Custom cards can retain their caregiver recording
  and Android TTS; dynamic prompts, hints and questions use SpeechManager.
- Quran files, player, audio ducking (0.25), settings UI and caregiver UI untouched.

## Text and generation
- Nine tags: LIBYAN=LBY, TUNISIAN=TUN, ALGERIAN=ALG,
  MOROCCAN=MAR, EGYPTIAN=EGY, SYRIAN=LEV, IRAQI=IRQ,
  SAUDI=SAU, STANDARD=MSA.
- Dialect texts, seed words, corrections and greetings are unvowelized.
- Kotlin ExportSpeechCatalogTest exports exactly 391 distinct semantic keys and
  2399 active dialect-specific entries. Unit tests reject diacritics, tatweel,
  digits and inconsistent display versus generated speech.
- scripts/generate_habibi_tts.py loads the Unified checkpoint ONCE, sets
  seed=0 per clip and uses explicit native Habibi dialect IDs.
- Inference steps=32, cfg_strength=2, sway=-1, speed=.75 (<10 letters),
  .88 otherwise, and writes 24 kHz mono MP3 @96kbps with outer silence
  trimming and peak normalization. Caching validates all parameters.
- scripts/ref_voices/ref_config.json accepts optional per-dialect WAV+exact
  transcription; if absent, it uses upstream Habibi-TTS assets/MSA.mp3 and
  its official transcript. This fallback is NOT an actual regional reference
  and cannot guarantee a female voice, matching accent or 4-8 second duration.
- GitHub Actions manual workflow supports dialect and limited CPU batch sizes;
  releases verify the entire corpus before APK signing.

## Licensing/quality blocker — NOT READY TO PUBLISH
Upstream Habibi Unified weights are CC-BY-NC-SA-4.0; the MIT source code license
does NOT grant commercial use rights. Upstream MSA speaker reference also has
separate provenance. Review permission before distributing generated audio in
a publicly distributed or commercially used app.

The GitHub branch has source code and scripts but must receive actual generated
and independently reviewed 2399 Habibi clips. Without them, v2 resolver
intentionally refuses legacy Microsoft clips and some cards will be silent.
Do NOT merge or publish until complete, including native speaker listening
for Libyan G consonants, clip endings, patient-facing warmth and clarity.

Official model reference: https://github.com/SWivid/Habibi-TTS
License: https://github.com/SWivid/Habibi-TTS#license
