# Sawti Greetings — signed review checklist

This review build contains the new **التحيات** section; it is not a production release.

- Open Home: full-width Quran hero, then **التحيات** beside **عائلتي** as the first phone row
- Verify warm coral #BF5A61 is visible on the main cover, the section toolbar and vocabulary cards
- Check category tile uses a dedicated realistic adult greeting photo rather than an icon-only placeholder
- Check animations, gestures, font size, RTL and both light and dark themes are unchanged
- Tap each greeting and confirm it speaks without showing a family-photo preview
- Switch among Fusha, Libyan, Saudi, Egyptian, Syrian, Iraqi, Tunisian, Moroccan and Algerian
- Confirm 14 greeting cards each dialect, with localized display and voice candidate and no repeated cards
- Note: Native-speaker proofreading and TTS listening on supported Android engines remain manual QA and are **not** certified by unit tests
- Install signed APK over the existing app; make a backup first as a general precaution
- Re-seed or reopen after update: existing family members, personal images, recordings, saved edits, backup and progress must survive
- This delivery changes neither Room schema nor app version, nor merges main

## Validation baseline
- Newly seeded greeting cards: 14 (391 total foundation words), idempotent by semantic key
- No Room schema changes; archived media and custom text remain owned by the user
- Automated validation: debug compile, unit tests, lint, optimized signed Release and certificate verification before delivery

## Photo cards QA
- Each of the 14 greeting concepts displays a unique offline photo rather than a single letter fallback.
- Home greetings tile uses a full-bleed dedicated greeting frame, while retaining the coral bottom banner and existing tap animations.
- Family category, Quran hero and all other category cards remain untouched.
- Across all nine dialects, the picture is the same for a semantic concept while text and TTS remain localized.
- A caregiver's own card photo remains higher priority than the bundled image.
- Test both light/dark themes and large text; native-device visual and speech review is still required.
