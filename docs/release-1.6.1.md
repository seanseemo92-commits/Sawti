# Sawti v1.6.1 — Arabic lexical and neural-phonetic refinements

Version **1.6.1**, code **14**, includes corrections across eight spoken Arabic dialects plus Standard Arabic and 14 card namespaces.

## Speech changes

- Every regional dialect has explicit in-law, cousin, nephew and niece speech and labels. The v1.6.1 lexicon takes priority over the former `completed` candidates and legacy patches without editing persisted card data or caregiver overrides.
- Common items have monophthongized vowels where dialectally appropriate; regional construct phrases no longer carry erroneous Fusha `ةُ` or `جَوْزُ`.
- Pronunciations of food, fruits, vegetables, home, body, clothing, and actions follow the specified regional written forms. Libyan `food.eggs` is changed from `دحي` to `بيض` in this user-requested release.
- Transport, greetings and regional cuisine speech remove artificial word-final sukun and final `هْ`. Selected Standard Arabic dishes remove awkward medial tanween plus final sukun.
- Tests cover semantic keys, all dialect projections, audio-vs-label distinctions, non-destructive caregiver editing and prohibited phonetic sequences.

## Neural voices and signed release

- A change on `main` triggers **Generate regional neural voices** with all nine dialects; the generator exports final Kotlin speech forms and re-creates 2,399 MP3s and all nine manifests using `--rate=-18%`.
- A successful voice generation triggers **Sawti signed release** against updated `main`, which verifies matching manifests before packaging the signed APK/AAB. The signed release never starts merely because the Gradle version changed.
- Written unit tests and CI do **not** replace native-speaker listening, accessibility review and device playback checks.

## Outstanding redistribution gate

As documented in v1.6.0, public redistribution permission for Edge Read Aloud-derived neural MP3s has not been established. Do not publicly distribute the generated binaries without confirming applicable licensing terms or choosing a permitted speech source.
