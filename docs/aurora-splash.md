# Sawti — approved responsive Light & Dark welcome

The welcome now renders the two **approved** original compositions:
- `drawable-nodpi/sawti_welcome_light.webp`
- `drawable-nodpi/sawti_welcome_dark.webp`

The two user-approved layouts include the actual communication illustration,
Arabic/English titles, glowing conversation bubbles, waves, flat Continue
button and developer credits. These are not recomposed or changed to a new
layout.

The source generated images were 941x1672 px. The top 72 px that contained
mock status/notification chrome were removed so Android shows a real clock.
The delivered images are 941x1600 high-quality WebP (quality 95).
This preserves original image detail, without claiming super-resolution.

`SawtiAuroraSplash.kt` chooses its image based on `LocalIsDark.current`,
so Sawti's in-app LIGHT/DARK/SYSTEM preference is honored independently of
Android's current system theme. The art is fit at its own aspect ratio inside
`WindowInsets.safeDrawing`, keeping the people, title, button and credits
visible on small phones and tablets. Matching gradient extends into the
unused area on unusually tall or wide screens.

On first entering the welcome, the complete scene fades/zooms in, the
communication area reveals slightly later, and the footer follows. There is
**no timed navigation**. A transparent, accessibility-enabled Compose button
overlays the painted "متابعة" button, and navigation occurs only after a
real tap. Rapid repeated taps cannot navigate more than once. Low-performance
mode skips animation.

The former single-theme file `sawti_approved_reference.webp` was removed;
launcher icons, Room, backup and other app functionality remain unchanged.

## Verification

The Android CI workflow checks that both theme resources are present and
the old illustration is absent, then compiles Debug + R8 Release and runs
unit tests and Lint. A successful CI build does **not** substitute for
manual visual checks on phones and tablets, especially varied aspect
ratios and dark/light preference switching.
