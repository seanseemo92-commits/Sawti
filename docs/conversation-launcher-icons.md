# Sawti launcher identity — three conversations (Light + Dark)

The launcher and Android 12+ native startup now share a new symbol **derived
from the approved welcome illustrations**: three overlapping cyan/violet/rose
speech bubbles, short sound marks and two flowing communication waves.
The outdated standalone letter ص was deliberately removed.

## One source of truth

- `res/drawable/sawti_adaptive_brand.xml`: scalable light foreground
- `res/drawable-night/sawti_adaptive_brand.xml`: scalable dark foreground
- `res/drawable/ic_launcher_background.xml` and `drawable-night/...`:
  full-bleed adaptive icon gradient backgrounds, no nested inset square
- `res/drawable/ic_launcher_monochrome.xml`: matching Android themed icon
- `res/drawable/ic_launcher_foreground.xml`: matching older foreground
- `res/mipmap-anydpi-v26/ic_launcher*.xml`: Android 8+ adaptive launcher
- `res/values-v31/themes.xml`, `values-night-v31/themes.xml`:
  use this same icon on the OS-owned Android 12+ splash
- `res/mipmap-*/ic_launcher*.png`: 30 fallback PNGs generated from those
  vectors for Android 7 and OEM/legacy launchers.

Run `python tools/generate_launcher_icons.py` when altering vector art,
then commit the resulting PNG changes together. Never upscale a screenshot
of the icon or bake a second rounded-square border inside the adaptive mask.

## Light/Dark switching

The launcher and OS splash use the **phone's** light/dark resource qualifier;
the app's welcome screen follows the **Sawti in-app theme choice** via
`LocalIsDark`. Android does not guarantee that changing an in-app preference
will refresh the home-screen launcher icon if the phone theme is unchanged.
Some OEM launchers also cache icons; switching the device theme or restarting
the launcher may be necessary after installing the new APK.

## Test matrix

1. Install/update on Android 13+: inspect installed app details, desktop icon,
   OS splash icon and welcome in both device dark and light mode.
2. Test circular, rounded-square and themed/monochrome launchers.
3. Test Android 7 fallback bitmap icons (both night and normal resources).
4. Confirm no large blue launcher panel still shows the old letter ص.
5. Confirm tapping Continue produces no large grey halo (from PR #21) and
   still opens the app, without automatic welcome timeout.
6. Verify Room records, family media, recordings and backups are unchanged.

A green CI build checks resource linking and application compilation; visually
inspect an APK on the user's device because OEM icon rendering and cache
behaviors vary.
