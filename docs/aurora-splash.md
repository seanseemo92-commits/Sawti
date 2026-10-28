# Sawti — approved conversation welcome

The user-approved welcome screen replaces the timed Aurora intro.

- Exact composition: older man at left, bearded adult man center, hijab-wearing
  woman at right, with speech-wave conversation artwork derived from the
  approved image. The people are decorative only; they never collect user data.
- The title, subtitle, Continue CTA and developer footer are **native Compose
  elements**, not bitmap text. This keeps Arabic glyphs sharp and interaction
  accessible.
- Blue → purple top and animated Bezier ribbons echo the app's adaptive app bar.
- Light/dark adapt the lower surface and footer while the upper visual identity
  remains dark blue.
- Opening is staged: background 1100ms; people 1050ms (170ms stagger);
  typography 700ms (420ms stagger); CTA 600ms (780ms stagger).
- Continuous slow drift stops in Performance Mode; the intro becomes static
  there. No online services or video decoders are required.
- **There is no automatic navigation or countdown**. Continue triggers
  `onFinished` once, even if pressed repeatedly.
- Window insets and viewport constraints adapt to narrow phones, display
  cutouts and tablets.
- The scene asset is stored in `drawable-nodpi/sawti_welcome_people.webp`
  so Android never density-upscales it at resource loading.

## Real-device QA

- Test light and dark themes, landscape and smallest phones.
- Ensure no clipped footer with large font scaling and display size.
- Verify status bar icons are visible on the dark top gradient.
- Verify the app remains on welcome for at least a minute with no auto advance.
- Double-tap Continue and check navigation happens exactly once.
- Test Performance Mode and TalkBack; no continuous animation in reduced mode.
- Compare scene picture clarity on the target device before final visual signoff.
- Never delete app data or change the existing backup/restore path.
