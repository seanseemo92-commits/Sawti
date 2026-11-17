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
python scripts/generate_habibi_tts.py --dialect LIBYAN --skip-existing --device cpu --max-new 16
```

For CUDA, install the matching PyTorch/torchaudio CUDA wheels first and use `--device cuda`. `--dialect ALL` covers all nine dialects. `--max-new 0` is unlimited. `--max-seconds` is an optional soft deadline checked between clips.

The official `SWivid/Habibi-TTS` Unified checkpoint and vocabulary are pinned to revision `3ad11a152851245c002156526c5e1d0b12f2b9d5`. The official `dialect_id_map` supplies native model tokens: LBY, TUN, ALG, MAR, EGY, LEV, IRQ, SAU and MSA.

Generation settings: seed 0 per clip, 32 steps, CFG 2.0, sway -1.0. A single word under ten letters uses speed .75; sentences and longer words use .88. For upstream's <10-byte speed override, an exact frame-based `fix_duration` preserves the requested speed without adding punctuation or modifying the text. Identical seed/settings provide repeatable runs on a matching runtime; different hardware or package versions need not produce byte-identical audio.

Output uses mono 24kHz MP3 at 96kbps. Floating-point peaks are normalized before integer WAV conversion to prevent clipping. Only outer silence is removed, retaining short onset/tail margins; pauses within speech remain intact. Temporary files share the destination filesystem for atomic replacement. NaN, empty and silent outputs are rejected.

`scripts/ref_voices/ref_config.json` lists an optional reference pair for every dialect. Paths are relative to that JSON file. A custom recording must be 4–8 seconds with its matching plain transcript. A missing recording or an absent pair uses the package's `assets/MSA.mp3` and its official transcript. This is a real bundled reference, but does not guarantee a female voice or matching regional accent; supply a calm female recording for that requirement. An incomplete configuration pair fails explicitly.

Cache reuse requires the same model revision, runtime versions, reference hashes, parameters, text and MP3 hash. All already validated entries remain in each atomic manifest checkpoint, even when synthesis of an earlier key fails. Interrupted batches can be resumed with `--skip-existing`.

## GitHub Actions

`generate-neural-voices.yml` now runs Habibi exclusively and supports manual dialect selection, batch size and verified cache reuse on the branch selected by workflow dispatch. It uses CPU wheels, caches the model, budgets synthesis time and saves verified completed clips to that branch, including after synthesis failure. An artifact preserves output if a push fails. It has no hard-coded branch name. The existing workflow filename is retained so the default branch can discover the workflow while the Habibi implementation is still on the migration branch; no legacy engine code remains in that workflow. Dispatch with the migration ref.

```sh
python -m unittest discover -s scripts -p 'test_habibi*.py'
python scripts/verify_habibi_packs.py --verify-codec
```

Release verification requires all 2,399 real MP3s, exact text/key coverage, matching hashes and MP3 encoding. `--allow-partial` is only for generation checkpoints and is never used by a release workflow.

## Delivery scope

The requested deliverables are full source files, reference configuration, workflow and tests. The branch is not a complete audio release until all 2,399 clips have been generated and reviewed. The earlier migration branch removed old engine recordings; they are not relabelled as Habibi recordings. Do not build a distributable silent app from an incomplete corpus.

Upstream code is MIT; Unified model weights are CC-BY-NC-SA-4.0. Reference audio has its own provenance. See the [official repository](https://github.com/SWivid/Habibi-TTS#license).
