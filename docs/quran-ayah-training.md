# Quran ayah training

The Quran section is for an adult to listen to one ayah and tap it again to
repeat with the reciter. Every tap intentionally restarts the selected ayah.
There is no fixed player, playback queue, whole-surah playback, or favourites.
The stop/cancel action belongs only to the currently active ayah.

## Visual and interaction contract

- Retain Sawti's ceramic surfaces, `CardShape`, micro-borders, shared
  `bounceClick`, touch dwell, optional haptics and performance-aware motion.
- Use Quran-local emerald/green and decorative gold tokens. Active ayahs use
  green surfaces in both themes instead of the app's blue primary container.
- Keep Quran text stationary. Only the card's existing press feedback and
  restrained surface/outline transitions animate.
- Allow long reciter names and actions to wrap. Do not use fixed card heights
  that can clip large Arabic text.
- Top-bar back and system back both navigate ayah screen -> surah list -> home.
- One ayah remains the touch target; no word-level hit regions or timings are
  inferred from an ayah MP3.

## Packaged font

`app/src/main/res/font/amiri_quran.ttf` is Amiri Quran Regular, version 1.003,
from Google's font distribution. It is used explicitly for ayah text, with
normal weight, no synthetic font styling, ample line height and font padding.
The original text is not rewritten or re-diacritized.

- Source: https://github.com/google/fonts/tree/main/ofl/amiriquran
- Git blob: `2a4de2c4fd3e6fd23656586151935a98723acaff`
- SHA-256: `e2a47644762d16bdfb6d33e0d8db8c6ff30beae84150ef5a705316bbd829455c`
- Unmodified SIL OFL 1.1: `app/src/main/assets/licenses/AmiriQuran-OFL.txt`.

Font cmap review against the 67 supported ayahs found coverage for their
visible characters and Quranic marks. U+FEFF in the upstream text is a
zero-width format character, not a missing visible letter.

## Playback and download ownership

`QuranViewModel` owns loading and download jobs. Generation tokens reject late
results; cancellation is not displayed as a network failure. Cache counting and
validation run on IO. Reciter caches remain separate and existing valid files
stay in place.

`QuranAyahPlayer` uses the application's audio-focus controller, stops pending
and active playback when the screen stops, ignores stale player callbacks, and
retries a decoder-rejected local recording once after invalidating it. Focus
refusal does not delete a recording. Acquisition failure and decode failure
have distinct user messages.

MP3 acceptance checks consecutive complete MPEG Layer III frames, caps the
response size and rejects a known Content-Length mismatch. This is format
sanity checking, not a cryptographic guarantee of content identity or a full
codec validation. The original recordings are not transcoded or normalized.
Ayah IDs must match the canonical surah and local ayah before playback.

## Verification

Quran regression tests cover wrong/duplicate ayah IDs, invalid ID3-only bodies,
clean early EOF, damaged-cache replacement, per-reciter cache separation,
repeated taps, late callbacks, three repeats, both visual themes, large text
with the long Husary name, and loading the packaged font. Native Robolectric
captures are written to `app/build/outputs/quran-review` for visual review.

Real-device checks remain necessary for Bluetooth/headphones, calls, Android
and OEM lifecycle behavior, and final Arabic rendering. Reciter recording
quality is unchanged by this UI work.
