# Quran passages — Debug test delivery

This is an explicitly requested **test build**, not a production or GitHub
Release. Work stays on `fix/prelaunch-cleanup-hint`; `main` is not modified.

## Content and interactions

The existing scrolling list receives two entries: Ayat al-Kursi (2:255), and
Al-Baqarah 2:284–286. They use the same cards, Quran font, green/gold tokens,
cascade, press feedback, touch dwell, per-card stop action, reciter selection,
1/3/5 repetitions and lifecycle/audio-focus ownership as the existing surahs.
There is no new pager, previous/next control, fixed player, playback queue or
automatic advancement. Existing short-surah recordings and their cached files
are not replaced.

| Ayah | Parts |
|---|---:|
| 2:255 | 9 |
| 2:284 | 4 |
| 2:285 | 5 |
| 2:286 | 8 |

Parts retain their original ayah reference and an additional part number.
Distinct selection IDs prevent the two entries from colliding even though
both belong to Al-Baqarah. Distinct segment keys prevent cards in the same
ayah from sharing playback highlights, list state or cache files.

## Recorded audio and provenance

All 26 parts have a recording from each of the existing readers: Mishary
Alafasy, Salah Bukhatir and Husary Muallim (78 files). These small fixed
passages are bundled, so the new content works without a first network
download. Playback still uses the existing player and a verified private file.
No TTS, voice cloning, word concatenation or reader substitution is used.

Canonical Uthmani text was fetched from AlQuran.cloud for the four requested
ayahs. Joining each ayah's fragments with their original spaces reproduces
the source text exactly, including Quran marks. The source-response hashes
and recording hashes are pinned in `scripts/quran_passages_plan.json`.

Each reader has its own source file and phrase boundaries. Source recordings
are EveryAyah's `Alafasy_128kbps`, `Salaah_AbdulRahman_Bukhatir_128kbps`, and
`Husary_Muallim_128kbps`; hashes identify the exact files used. Existing
short-surah source choices are unchanged. Phrase clips use MP3 at 128kbps,
44.1kHz mono, without gain normalization or speed changes. This is one
lossy re-encoding of each selected range, not an audio-quality enhancement.

Alafasy and Husary boundaries were assisted by Collin Fair's quran-align
2016-11-24 data (CC BY 4.0, attribution/license packaged). Salah boundaries
were assisted by canonical-text alignment and checks of short source windows.
His source 2:255 repeats the opening phrase, and 2:286 repeats the forgiveness
phrase; each corresponding card selects one complete occurrence. Audio is
not concatenated across the skipped repetition.

**Human listening and waqf review are pending.** Hashes, decoding, automated
alignment and tests do not certify suitable Quranic stopping/starting or that
every boundary is perceptually clean. This test build is for that review;
do not describe it as approved for production. Phrase-boundary corrections
can change the packaged hash/cache key without disturbing whole-ayah files.

## Test installation

Build with `-PquranPreview=true` using the Debug variant. It uses package
`com.sawti.quranpreview`, version suffix `-quran-test`, and label
`صوتي — اختبار القرآن`. It installs beside `com.sawti`, with its own database,
preferences and audio storage. It does not read or upgrade the installed
production app's data. Ordinary Debug/Release configuration keeps its current
identity when this property is absent. No publisher signing key is accessed.

## Validation

`scripts/verify_quran_passages.py --decode` checks exact text reconstruction,
26 unique parts, all three readers, 78 file/source hashes, durations and full
FFmpeg decoding. Android tests cover all offline assets, corrupted segment
recovery, preserved whole-ayah files, same-ayah segment identity, repeated
taps, 3x repetition without advancement, both selections, scroll reachability,
dark/light themes and double font scale. The dedicated workflow builds Debug
only and runs the complete existing test suite plus Debug lint.

Candidate listening checks on the test phone:

1. Listen to every part with each reader; compare its first and last word
   with the displayed text. Check madd, hamza, ghunna, connected words,
   trailing sounds and suitable stopping/starting.
2. Tap the same card twice, select another part in the same ayah, try 1/3/5,
   change reader, and leave/re-enter the screen. Confirm no overlap or
   automatic next-part playback.
3. Test all new content offline, with large text, dark mode, touch dwell,
   headphones and an incoming call.

The workflow delivers an Actions artifact, or commits the test APK, checksums,
screenshots and results under `previews/quran-passages-test` if artifact quota
is exhausted. It never creates a Release or pre-release.
