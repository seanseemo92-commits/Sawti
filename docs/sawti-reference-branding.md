# Sawti – reference-based welcome / app icon identity

This implementation follows the **user-supplied reference image**, with a sky-blue
to gentle purple gradient in light mode and a deep navy (#0F172A family)
in dark mode. The vector symbol is an Arabic **ص** connected to sound pulses.
The icon is drawn in white with subtle metallic-gold accents by day and gold
on navy by night. No competing alternative brand direction is introduced.

The launcher is provided as Android density-independent XML vector graphics with
adaptive icon backgrounds for API 26+ and XML-layer icon fallbacks for API 24–25.
All device densities (mdpi/hdpi/xhdpi/xxhdpi/xxxhdpi) scale from the vectors
without bitmap stretching. Android 13 themed icons use the matching monochrome mark.

The startup Welcome Screen is native Jetpack Compose, using the same graphic
and matching gradient/night colors. The logo is proportional to available height,
and bottom credits are independently aligned inside Android system-bar insets:

- تطوير: إسماعيل بن سالم
- Ismail92sa@gmail.com
- الإصدار 1.0.0 • صُنع بحب في ليبيا 🇱🇾

The displayed version matches `defaultConfig.versionName = "1.0.0"`; the existing
versionCode is preserved to allow updates over already-installed builds.
The original four-page first-use guide, app functions, categories, words, Room,
photos, voices and recordings remain intact. The first guide page also shows
the developer credits so users can read them after the brief startup.

## Device review checklist before releasing
1. Start on a small Android phone and a tablet; ensure no logo stretching or
   developer-footer collision, including large accessibility font settings.
2. Test light, dark and follow-system themes, plus navigation/status bar visibility.
3. Confirm initial startup returns to first-launch guide or existing home screen.
4. Check launcher icon round masks and Android 13 monochrome themed icons.
5. Ensure updates preserve Room data and customized photos/voice recordings.

**CI intentionally skipped** as requested. Run the build workflow and check the
APK only when the user authorizes it.
