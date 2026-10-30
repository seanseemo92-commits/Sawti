# Sawti welcome: light reference button and protected footer

The original 941×1600 light and dark WebP scenes are retained unchanged.
`SawtiAuroraSplash` selects the scene with `LocalIsDark.current`.

## Shared Continue appearance

The LIGHT artwork is the reference: source bounds (119, 1236)–(822, 1353).
Dark mode replaces the old, smaller painted pill during rendering. A cached
`WelcomeButtonArtwork` overlay retains the light reference's actual gradient,
Arabic lettering and arrow. Its blue shadow is alpha-matted against the light
backdrop; the white panel is not copied into the dark scene. A small sampled
dark backing removes the old pill's residual edge. The source assets, faces,
wordmark and developer credits are not rewritten.

Both themes share one hit rectangle and one clipped 115 ms press glow, without
a second Material ripple. Absolute positioning keeps this image-coordinate
hit target aligned in RTL as well as LTR. An accepted click produces one short
Compose TextHandleMove haptic before navigation. App and system touch-feedback settings
are respected; no VIBRATE permission or override flags are used. The existing
single-navigation guard prevents rapid repeated clicks from repeating either
the navigation or the haptic. Physical feedback depends on device hardware.

## Responsive geometry

Only source strips 0–70, 1090–1200 and 1570–1600 absorb additional height.
The entire 1200–1570 footer is scaled uniformly. The old boundary at 1540
intersected the light copyright line and elongated its lower glyphs on tall
phones. The new boundary leaves the complete text and copyright symbol intact.
The title, people and CTA also retain their original proportions.

## Validation

Geometry tests cover the reference size, tall phones, tablets and landscape,
including each row across the former copyright stretch boundary. CI compiles
Debug and R8 Release, runs unit tests and Android lint. Device checks remain
necessary for physical haptic feel and final on-device rendering.

