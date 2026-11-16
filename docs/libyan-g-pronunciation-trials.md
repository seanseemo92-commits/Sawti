# Libyan /ɡ/ — pronunciation test gate (2026-11-16)

The /ɡ/ feedback for words such as **نقعمز** (heard target: **نْگَعْمِزْ**) is valid. The v1.6.2 OOV hotfix replaced unsupported U+06AF/U+06A8 with ق in the **neural speech channel** to prevent swallowed consonants. It does **not** prove the resulting sound is /ɡ/. The visual label remains standard Arabic orthography.

**Do not automatically alter the built-in 2,399 entries, tests, or offline MP3 pack based on guessed graphemes.**

- This experiment covers `action.sit`, `action.sleep`, `action.stand`, `food.coffee`, `body.heart`, `animal.cow`, and `animal.cat`. Each has four recordings.
- `edge-tts` allows speed adjustments but **not** custom SSML phoneme instructions; the variants are acoustic hypotheses, not validated /ɡ/ corrections.
- `ar-LY-ImanNeural` with ق is the baseline. `ar-LY-OmarNeural` tests a second native Libyan voice. ك is a negative control (voiceless /k/, not /ɡ/). `ar-EG-SalmaNeural` with ج may provide /ɡ/ but changes the regional voice, so it cannot be shipped unreviewed.
- On a suitable Azure Speech endpoint, full `SSML <phoneme alphabet="ipa" ph="...">` might specify /ɡ/, **but compatibility with the ar-LY voice has not been exercised**. Azure authorization is not bundled in this repo and must not be bypassed.
- Require a native speaker's confirmation for each recorded word and consistent /ɡ/ before promoting changes into neural assets. Verify manifests, APK and regression tests afterwards.

Production `main` and `fix/audit-v161-199-confirmed-20261116` remain unchanged by the experiment.
