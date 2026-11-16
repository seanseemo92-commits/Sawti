# Sawti Habibi-TTS migration

## Work branch: migration/habibi-tts-20261116

Main is unchanged. This is NOT release-ready until the complete Habibi
voice corpus and live text capabilities have been implemented and tested.

* Kotlin dialect source has had all manual vowels/sukun speech overrides removed.
* All nine dialects have explicit Habibi identifiers: MSA, LBY, SAU, EGY,
  LEV, IRQ, TUN, MAR, ALG. LEV is the nearest Habibi tag for Syrian, but
  native listening is still required.
* Habibi model weights have separate licenses from the MIT source code.
  Specialized ALG/EGY/IRQ/MAR/MSA: Apache-2.0. Unified, SAU and UAE
  weights: CC-BY-NC-SA-4.0; non-commercial restriction applies.
* License reference: https://github.com/SWivid/Habibi-TTS#license
* Quran recitations and user's own recordings/edits must never be changed.
* Removing diacritics is a developer preference, not automatic proof of
  correct Libyan ق pronunciation or therapeutic-appropriate audio.

## Generate voice corpus (licensed GPU environment)

Export Kotlin-approved card text:

    ./gradlew :app:testDebugUnitTest --tests 'com.sawti.domain.model.ExportSpeechCatalogTest'

Then use appropriately licensed reference WAV + transcript for each dialect
in the references folder, e.g. libyan.wav and libyan.txt:

    python scripts/test_habibi_contract.py
    python scripts/generate_habibi_packs.py --references references --dialects EGY
    python scripts/verify_habibi_packs.py

The generator never calls Microsoft. It fails if a model with
non-commercial restrictions is chosen without explicit acknowledgement.
The verifier requires all 2399 clips and manifest hashes before release.

## Remaining blockers

- Produce 2399 Habibi MP3 files with valid model/voice usage rights.
- Connect Android speech dispatcher exclusively to the new resolver.
- Plan dynamic Habibi inference for user-entered text, names, hints.
- Retire old assets, Microsoft generator and workflow after coverage is complete.
- Replace old vowel-assumption tests and run APK and listening tests.
- Do not publish or merge until voice coverage and legal review pass.
