# Sawti Habibi-TTS source migration

Implementation branch: `migration/habibi-tts-20261116`.

## Protected application behavior

- `AppDatabase.VERSION = 1`, Room entities, exported schema and migrations are unchanged.
- No presentation screens, Quran audio/files/player, caregiver settings or ducking implementation were changed. `onDuck` remains `0.25f`.
- Embedded vocabulary plays verified local MP3 assets at
  `app/src/main/assets/neural_tts/<dialect>/<semantic_key>.mp3`.
- Built-in cards never fall back to Android TTS, including sentence-strip playback.
- Personal words, caregiver-edited text and dynamic hints/questions retain SpeechManager. Existing personal recordings remain supported.
- Habibi v2 manifests identify the Unified model, dialect, reference, settings, clean text and each MP3's SHA-256. Legacy voice manifests are rejected.

## Vocabulary coverage

There are **391 distinct semantic keys** and **2,399 active dialect/key pairs**:

| Vocabulary | Coverage |
| --- | --- |
| Shared concepts | 251 keys × 9 dialects = 2,259 |
| Libyan cuisine | 20 keys |
| Other regional cuisines | 15 keys × 8 dialects = 120 |
| Total | 391 distinct keys; 2,399 pairs |

391 keys in *every* dialect would instead be 3,519 pairs. The existing regional cuisine filters and all semantic identities are preserved.

Built-in speech is projected directly from the clean display text. Dictionaries, seed text, greetings, transport and regional cuisine sources contain no historical diacritics or tatweel. Personal text is preserved. Tests check the actual Kotlin-exported corpus, identity fixture, per-dialect counts, plain Arabic, digits and source literals.

## Generation

Install Python 3.11 or 3.12 and ffmpeg. For a CPU machine:

```sh
python -m pip install torch==2.8.0 torchaudio==2.8.0 --index-url https://download.pytorch.org/whl/cpu
python -m pip install -r scripts/requirements-habibi.txt
./gradlew :app:testDebugUnitTest --tests 'com.sawti.domain.model.ExportSpeechCatalogTest'
python scripts/generate_habibi_tts.py --dialect LIBYAN --skip-existing --device cpu --max-new 16 --output build/habibi-batch
```

For CUDA, install the matching PyTorch/torchaudio CUDA wheels first and use `--device cuda`. `--dialect ALL` covers all nine dialects. `--max-new 0` is unlimited. `--max-seconds` is an optional soft deadline checked between clips.

The official `SWivid/Habibi-TTS` Unified checkpoint and vocabulary are pinned to revision `3ad11a152851245c002156526c5e1d0b12f2b9d5`. The official `dialect_id_map` supplies native model tokens: LBY, TUN, ALG, MAR, EGY, LEV, IRQ, SAU and MSA.

Generation uses seed 0 per clip, 32 steps, CFG 2.0 and sway -1.0. Short single words use speed .75; other phrases use .88. Pipeline 4 reserves at least 1.2 seconds for words and 1.4 seconds for phrases, scaled by text length and reference pace. A rejected clip is retried at most twice with additional generation time. Matching software and hardware are needed for byte-identical output.

Output uses mono 24kHz MP3 at 96kbps. Floating-point peaks are normalized before integer WAV conversion to prevent clipping. Only outer silence is removed, retaining short onset/tail margins; pauses within speech remain intact. Temporary files share the destination filesystem for atomic replacement. NaN, empty and silent outputs are rejected.

`scripts/ref_voices/ref_config.json` accepts a licensed 4–8 second recording and its matching plain transcript. For Libyan speech, the bundled MSA recording is available only for explicit diagnostic keys. A Libyan batch requires a native Libyan reference and a successful listening pilot using that reference. Keep personal recordings outside version control. A separate, permitted production reference is needed for distribution.

Cache reuse requires the same model revision, runtime versions, reference hashes, parameters, text and MP3 hash. All already validated entries remain in each atomic manifest checkpoint, even when synthesis of an earlier key fails. Interrupted batches can be resumed with `--skip-existing`.

## Libyan pilot and batch

The pilot uses a seeded draw from actions, food, transport and greetings. It runs against the exact exported Kotlin catalog. The tool checks the text, MP3 encoding, duration and checksum and screens for duplicated waveforms. The report keeps listening pending because these checks cannot verify vowels, consonant length or the regional accent.

```sh
python scripts/review_habibi_pilot.py --list-keys
python -m unittest discover -s scripts -p 'test_habibi*.py'
python scripts/verify_habibi_packs.py --verify-codec
```

Review the reference and each sample with a listener from the intended Libyan variety. Include the previously rejected requests `request.water` and `request.toilet` in pronunciation review before approving the batch. Check the spoken distinction between `body.leg` and “man”, and between `body.tooth` and “year”. The visible text and the semantic keys remain stable.

## GitHub Actions

- `habibi-audio-pilot.yml` generates samples on manual dispatch and stores them as artifacts outside Android assets.
- `generate-neural-voices.yml` stages completed clips as an artifact. For Libyan, it requires confirmation that the reference passed listening. It has read-only repository permissions.
- `complete-habibi-project.yml` runs on manual dispatch. It validates the configured reference before scheduling jobs, uses six checkpoint shards per dialect, verifies the assembled corpus and delivers an isolated preview APK as a workflow artifact.
- Release verification requires all 2,399 clips with exact text and key coverage, matching hashes and codec checks. A complete Libyan pack cannot use the MSA diagnostic reference. Signed builds verify packs before packaging.

## Model and audio provenance

Habibi Unified is used with its pinned vocabulary and dialect token. Reference conditioning guides the synthesis; it does not fine-tune model weights. Upstream code is MIT and the model weights are CC BY-NC-SA 4.0. Check the separate terms and permissions of any reference audio before distribution. See the [official model repository](https://github.com/SWivid/Habibi-TTS#license).
