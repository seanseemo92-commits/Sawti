# Cinematic Spotlight Tour — test checklist

This note accompanies a requested signed review APK. It is not a main-branch release.

## First use
- A first-run optional tutorial appears only after category loading succeeds.
- Tap **ابدأ الجولة** and verify the caregiver gear, Quran hero, family card, daily requests, and language icon.
- Confirm the screen dims, only the target is highlighted, and the guide controls work without triggering underlying buttons.
- Test **السابق**, **التالي**, **تخطي**, and completion. Reopen to verify it does not repeat.
- Skip the tour and verify no forced onboarding on the next open.

## Contextual tours
- Enter Quran for the first time and verify surah focus.
- Open a surah after loading and verify reciter settings and the first verse focus.
- Enter family and verify relatives and people-group guidance. Actual additions must still require caregiver mode.
- With caregiver mode enabled, open Settings > **دليل استخدام صوتي** to replay the guides.

## Compatibility
- Verify RTL, portrait and landscape or tablet layout, system light/dark themes, extra-large text, and performance mode.
- Ensure the app remains usable if Quran audio is offline or a category fails to load.
- Verify backups, family media, TTS, and Quran playback are unchanged.
- This is a manual QA checklist; device behavior has not yet been attested by CI.
