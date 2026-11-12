# Compact category grid correction — 2026-11-12

## Report and cause

An Honor Play 20c user reported a single large card per row inside categories.
The shared word grid used `GridCells.Adaptive(minSize = 160.dp)`. With
16dp padding on each side and 14dp between columns, two columns required at
least 366dp of container width. Smaller logical widths therefore became one
column. This threshold depends on the available dp width and display scaling,
not just the physical pixel resolution.

## Change

- Keep a minimum of two columns for the shared category word grid.
- Preserve extra columns on wider containers when 160dp cards and spacing fit.
- Use the same column count for the actual grid, caption measurements, card
  height and row animations. Caption height still responds to font scaling.
- Flashcard mode, the home grid and the Quran layout are unchanged.

| Container width | Before | After |
|---|---:|---:|
| 320dp | 1 | 2 |
| 360dp | 1 | 2 |
| 366dp | 2 | 2 |
| 540dp | 3 | 3 |
| 714dp | 4 | 4 |

## Validation

Local Gradle 8.9, JDK 17, Android SDK 35:

```sh
gradle --no-daemon --max-workers=2 :app:testDebugUnitTest \
  --tests com.sawti.presentation.category.WordCardSizingTest \
  --tests com.sawti.presentation.category.GreetingsLayoutTest
```

Final result: **16 passed, 0 failures, 0 errors, 0 skipped**.
Both production and test Kotlin sources compiled successfully.

Eight geometry tests cover compact widths, the old threshold, wider-screen
column boundaries and measured caption heights. Eight Compose/Robolectric
tests include a 360dp grid and a 320dp grid with 2x Arabic text, asserting that
the first two cards share a row without overlap and that the final card
remains reachable and tappable. Generated light and dark screenshots were
visually reviewed. Existing grid navigation and flashcard checks also passed.

The initial environment attempts failed before UI assertions because of missing
build tooling or an uncached Robolectric runtime. After restoring the tools and
runtime, the final full targeted run above passed.

The initial source correction was based on main `0337df2aea0c48e288318cd8e7dba4adbecb2a18`.

## Signed follow-up delivery

Sawti 1.5.1 (versionCode 7) was subsequently built locally from application source
`60d666db848e2a1da6c1e407b22d11f1668aff9c`, with the original publisher signature.
The full suite of 340 tests and Release/R8/Lint passed. The previous v1.5 APK
is retained as a historical release; a newly built APK is required to apply
the grid correction. Physical Honor update validation remains pending.

See [release notes](release-1.5.1.md) and [final verification](final-apk-verification-1.5.1.md).
