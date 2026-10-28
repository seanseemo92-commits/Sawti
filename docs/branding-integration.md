# Sawti branding integration — 28 October 2026

Source: user-supplied `sawti_branding_assets.zip`.

- All 23 original PNGs within `app/` have been uploaded with their original bytes:
  20 density-specific launcher/round/day/night PNGs, two regular splash
  logos and the original drawable-night splash logo.
- Two original 512×512 store icons are retained as
  `store_assets/icon_{light,dark}_512.png` and provided to adaptive
  icons as `drawable[-night]-nodpi/sawti_adaptive_brand.png`.
- Ten night-qualified mipmap aliases make the supplied night launcher
  icons selectable by Android night mode on pre-adaptive launchers.
- The old `mipmap-anydpi` layer-list icon aliases are removed so they
  cannot shadow the density-specific PNGs on older Android versions.
  `mipmap-anydpi-v26` uses the full brand PNG for adaptive foreground,
  retains the accessible monochrome vector, and selects day/night
  artwork via resource qualifiers.
- Two optional background-transparent logo variants (derived locally
  from the user-supplied images) are used by the Compose splash to
  avoid an opaque square over the vertical gradient.
- Current app package remains `com.sawti`. The imported screenshot
  implementation's `ly.sawti.app` name was not copied as a separate
  conflicting Activity; instead its design and credits were integrated
  into the existing `SawtiWelcomeScreen`.
- Splash duration is exactly 2,000 ms in `MainActivity` and takes the
  user directly to `AppRoot` / the home screen; the four-step
  `OnboardingScreen` and its guide link have been removed.
- Fresh installs default to standard Arabic and completed setup.
  Existing explicitly selected dialect preferences are preserved,
  as are every personal photo, recording and Room database entry.

## Developer footer from attached branding file
`Developed by Ismail Bin Salem`
`Ismail92sa@gmail.com`
`v1.0.0 • © 2026 All Rights Reserved`

## Validation
The Git tree is checked for resources and startup settings.
The task specifically requests `[skip ci]`, so no Gradle build or device
render test is claimed. Run GitHub Actions when requested to verify
Android resource merging, launcher rendering and splash on actual devices.
