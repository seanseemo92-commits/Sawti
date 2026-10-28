# Sawti Aurora — new launch experience

The previous welcome screens and their dedicated transparent PNGs, traced wordmarks,
bitmap wave backgrounds, and image-generation scripts were removed. The new
splash is implemented solely in
`app/src/main/java/com/sawti/presentation/splash/SawtiAuroraSplash.kt`.

## Design

- **Light:** bright pearl → pale sky → lavender, with soft procedural aurora
  and two Bezier sound ribbons.
- **Dark:** deep navy / violet with warm metallic gold accents.
- The centerpiece is the existing **native vector launcher symbol**, reused
  directly from `ic_launcher_foreground.xml`. It cannot exhibit the alpha
  pinholes or JPEG/PNG upscale artifacts of the previous wordmarks.
- The title `صوتي` and English `SAWTI` render as actual Android text, not
  baked-in low-resolution pixels. A concise subtitle and seven gently moving
  equalizer bars complete the composition.
- Intro fades and lifts over 720 ms; the splash hands control to the existing
  `AppRoot` after approximately 2.3 s.
- With the app's Performance Mode enabled, continuous effects are disabled.
- Sizes scale to the safe usable viewport for both small phones and tablets.
- `sawti_aurora_window.xml` provides a matching window background during
  the Android cold-start interval before Jetpack Compose renders.

## Scope and safeguards

This redesign does not touch Room migrations, user-supplied family photos,
recordings, TTS, backup/restore, existing launcher icons or home/category screens.

Retained shared assets:
`ic_launcher_foreground.xml`, launcher-adaptive foregrounds/backgrounds,
night launcher mipmaps, and color values `sawti_logo_primary/accent`.
These are needed by the app icon and the new splash; they are **not** legacy
splash files.

## Verification

Run `gradle --no-daemon :app:assembleDebug :app:assembleRelease
:app:testDebugUnitTest :app:lintRelease`.
On real devices, verify:
1. Light/dark/app-theme override on a small Android phone and a tablet.
2. At increased text scale, title stays legible without clipping.
3. Performance Mode disables looping animation and still navigates.
4. Cold start avoids a flash of the old gradient; background theme matches.
5. No blank launch, duplicate navigation or overlap with status/navigation bars.
6. Launcher icon remains unchanged and backup data is preserved.

A green CI build does **not** prove exact visual appearance on every device;
final sign-off requires a screenshot from the target phone.
