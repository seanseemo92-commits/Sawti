# Light and dark UI review

Branch: fix/prelaunch-cleanup-hint. No main, version or signing changes.

## Targeted color corrections
- Settings block titles use onSurface. Selected choices retain the light design and use primaryContainer/onPrimaryContainer in dark mode.
- Progress section headings use onBackground; attempt counts use primary. Semantic green/orange/slate colors are unchanged pending contrast review.
- Practice completion title uses onBackground. Exercise selection uses a matched dark selected pair; unselected options use surface/onSurface rather than fixed white.
- Recording stop button explicitly uses white text/icons on RecordRed in both themes.

Layouts, actions, text, recording permission handling and data operations are preserved. Source formatting was compacted while preparing complete-file updates.

## Validation status
Code changes only. Compilation, unit tests, lint and visual checks are NOT reported as passed. Review CI for the resulting commit. Manual checks remain: settings selected/unselected states; progress with data; practice modes and completion; recording stop; light/dark with large fonts and small screens.

## Remaining phase 6 work
Shared components, top bar, measured semantic-color contrast, large-font overflow, focus/preview sizing, animation/performance and real-device checks remain open. No claim that the complete phase is finished.
