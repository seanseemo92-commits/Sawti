# Shared component corrections

Branch: fix/prelaunch-cleanup-hint. No main, version, signing, storage or speech behavior changes.

## Changes
- Hint buttons use opaque theme surfaces; the lightbulb keeps an amber identity with darker light-mode and brighter dark-mode ink.
- ActionPill selects the higher-contrast of Ink and white against its composited background. Optional contentColor permits explicit overrides. Disabled controls expose disabled Button semantics and cannot invoke the click action.
- bounceClick exposes Button role in ordinary and dwell modes; dwell accessibility actions merge descendant labels. Existing gesture filtering and haptics remain.
- Both cascade variants immediately render their final position in Performance Mode and remember completion to avoid replay on turning it off.
- Home aura creates an infinite transition only while speaking and outside Performance Mode.

## Tests added, not executed here
Action contrast: amber, navy, white, and both hint foreground/background pairs. Existing CI must verify compilation, unit tests, lint, Debug and Release for the resulting commit. These tests do not replace a screen-reader or real-device check.

## Remaining checks
TalkBack labels/disabled actions and dwell; switching Performance Mode during entrance and playback; small screens and large font; actual light/dark rendering; low-end performance. Fixed semantic stat colors still need contrast review. Phase 6 and production release remain open. No merge without approval.

Complete-file updates compact source formatting while preserving the other component functions and layouts.
