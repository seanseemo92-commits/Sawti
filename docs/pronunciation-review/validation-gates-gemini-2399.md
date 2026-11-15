# Gemini 2,399-card pronunciation review — regression gates

This candidate branch preserves the 2,399-row review and all 1,809 written Gemini suggestions as audit data. It does **not** certify the proposals as native-reviewed or clinically tested.

## Why raw proposals were not all activated

- Proposed word-final sukuns conflict with the v1.6.1 pausal-pronunciation rule and prior tests.
- Extended consonant characters (`گ`, `ڨ`, `چ`, `پ`) have no verified listening result for the selected Edge Read Aloud voices.
- Fusha case inflections, phrase rewriting, and previously audited dish/greeting/request forms need independent review.
- Previously patched or completed dialect words, explicit v1.6.1 lexical overrides and two Libyan golden examples are not overwritten.
- Only matching-source, diacritic-only, structurally safe candidates can be considered automatically.
- Caregiver-edited speech, labels, and phonemic cues remain protected.

## Validation procedure

1. Run `:app:testDebugUnitTest` (402 tests after the additional regression cases).
2. Export final speech text with `ExportSpeechCatalogTest`.
3. Regenerate each of the nine neural MP3 packs and manifests from **final runtime forms**.
4. Run `scripts/validate_neural_packs.py` after generated assets have been committed; older CI jobs can fail this check due to stale MP3/manifest assets during regeneration.
5. Run Android lint and the release compilation checks. Do not publish to main from this review branch.
6. Complete native-dialect listening and confirm audio distribution rights before a public release.

See `gemini-v161-corrections-1809.csv` for the complete unchanged review input.
