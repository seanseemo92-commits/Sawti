# Greetings design parity — Sawti 1.2

The greetings section uses the same `LuxuryCategoryCard`, `WordGrid`, `WordCard`, `FlashcardPage`, image renderer, touch dwell and bounce interactions as other vocabulary sections. Its card geometry, padding, text measurement, image crop, corner shapes and gestures remain shared.

## Findings addressed

- The old home cover contained black/blurred frame bands inside its 512px bitmap; several inner stock photos portrayed generic subjects rather than their greeting. Replace the cover and all 14 photos with full-bleed, visually consistent adult scenes.
- Vary faces, ages, clothing and gestures. Women wear hijab. Simplify the ambiguous raised/crossed hand pose in the conversation scene, and review arm/wrist/finger anatomy before packaging.
- The old coral `#BF5A61` provided 4.33:1 contrast against white. Use the distinct coral `#B14D57` at 5.16:1, meeting 4.5:1 for normal-size text.
- Focus and preview dialogs discarded the section palette and returned to the numbered preset. Pass the resolved section palette through those dialogs; resolve the greetings editor through the same palette helper.
- Retire automatic stock-photo replacement on trigger-document pushes. The historical stock import is manual only, so it cannot silently overwrite the reviewed artwork during normal documentation edits.

## Assets and preservation

All 15 images are distinct RGB WebPs at exactly 512×512, matching existing Family/Transport image dimensions. Pixel generation, final hashes and scene prompts are recorded in `greetings-images-1.2.json`. Packaged filenames and semantic keys are unchanged, so all nine dialects share the appropriate concept image and caregiver photographs still take priority. No database, vocabulary identity, audio, settings or backup-format changes were made.

## Validation

The full signed review workflow must pass before production publication. Existing 512px resource and nine-dialect identity tests remain. New coverage checks black frame edges, white-label contrast, home-card geometry/navigation against Family, all 14 card taps, repeated taps, the Libyan dark grid, 2× text scaling, flashcard controls and focus-dialog palette. Native graphics screenshots are captured for visual inspection. Validation succeeded on commit `289eaa1c969a6b4e2250e55ac946a53af5a5f79e`: **260 tests, 0 failures, 0 errors, 0 skipped**. Debug assembly/Lint and optimized signed Release assembly/Lint passed, along with actual APK identity/version, publisher-certificate and packaged-resource checks.

Run: https://github.com/seanseemo92-commits/Sawti/actions/runs/42903643167

The native graphics screenshots for Home beside Family, the light grid, Libyan dark grid, 2× text and the flashcard were visually inspected. The cover has no embedded frame bands, the photos occupy the normal shared card area, Arabic labels are readable, and the flashcard's current listen action is exercised by its stable card identity. Publication rechecks the exact APK checksum, established signing certificate and `com.sawti` / `1.2` / `3` metadata before marking the release stable. Main is unchanged.

Hosted rendering does not certify physical-device behavior or native-speaker/TTS pronunciation.
