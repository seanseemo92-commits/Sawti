# Sawti welcome — approved visual reference

The welcome screen now uses the **exact user-approved composition** as a
single reference artwork resource:
`app/src/main/res/drawable-nodpi/sawti_approved_reference.webp`.

The image has only the top 57 screenshot pixels removed so Android can display
its **real clock and status indicators**, avoiding duplicate or frozen system
icons. The source artwork otherwise retains the precise positions and styling
of the title, three conversation partners, speech bubbles, flowing ribbons,
flat purple-blue button, developer credit, email, and copyright.

`SawtiAuroraSplash.kt` fits the artwork without cropping and stages a light
cinematic fade/zoom, separate conversation-scene reveal and footer reveal.
**No timer starts navigation**. A real accessible Compose clickable surface
with label `متابعة` is positioned on top of the painted button; this
prevents double navigation and supports actual touch and accessibility input.
The idle screen is identical to the approved artwork.

This is an intentionally image-led design because **strict screenshot fidelity**
was explicitly requested. Responsive image fitting avoids portrait cropping on
smaller devices, though wider tablets may have side gutters. Text in the artwork
is raster, so future copy changes need the layered source design rather than
editing Kotlin strings. The app's main screen, local Room data, recording and
backup code were not changed.

## QA

- On the user's portrait phone, verify that the approved image fills the safe
  area, with the real status clock on top and no duplicate screenshot icons.
- Verify the Continue tap and TalkBack activation, including during animation.
- Keep the welcome visible indefinitely until the user explicitly continues.
- Check both performance modes and device theme modes.
- Confirm short-screen portrait and tablet fit without cropping any credits.
- Verify no old low-resolution people cut-out resource remains.
