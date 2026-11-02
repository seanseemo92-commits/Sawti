# Uniform word-card grid sizing

Branch: fix/prelaunch-cleanup-hint. No main, version, signing, vocabulary, speech or stored-data changes.

## Report
The user screenshot shows a tall MSA daily-request card and a large gap beside it. WordCard selected a 360 * fontScale dp minimum for labels over 17 characters, while short labels used aspectRatio(0.85).

## Correction
- Remove character-count-based height selection.
- Calculate adaptive grid columns and cell width using actual available width and existing padding/spacing.
- Measure visible labels at the current density/font scale, existing 18sp/24sp bold style and a four-line maximum.
- Use the maximum measured caption height for one shared height across the visible subgroup's grid, reserving room for hint controls and image content.
- Apply explicit bounded card height so fillMaxSize children receive finite height.
- Keep image/title overlays, hint actions, category controls, family/custom editing, flashcards and all original text/data.

## Validation
Five sizing-rule unit tests added, NOT executed here. UI text measurement and actual device layout are not covered by these pure tests. CI compilation/lint/tests/build results are pending. Manual verification required: reported MSA bathroom phrase beside a short label; other dialects; normal/large/extra-large fonts; light/dark; rotation and wider devices; no hint/title overlap. Text beyond four lines still uses the existing ellipsis policy. Full phase 6 remains open.

Complete-file update compacts formatting outside the focused sizing change.
