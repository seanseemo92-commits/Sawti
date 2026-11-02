# Canonical editing and personal pronunciation

This branch builds on the cumulative data-safety work in PR #26.
Dialect projections carry immutable source details; the caregiver editor displays
those saved details, not translated labels, synthesized speech or suppressed
sentence hints. Its opening baseline stays fixed until it closes. Saving merges
only changed fields against the latest row in one transaction. Unrelated edits
and media survive; conflicting changes to the same field reject the save.
Family-group edits are validated and committed in the same transaction.

The foundation Room v1 schema stores customTextFields with bits for label, speech,
cue and sentence. New edits explicitly record these overrides; no historical-value
inference or migration is performed. Stable semantic keys identify seeded concepts.

Changed text fields are explicit overrides and remain as saved across dialects;
custom speech bypasses the curated pronunciation and automatic vocalization.
Unedited built-in fields continue to localize. Explicit sentence removal is
protected from seed refilling. Seeding inserts missing concepts and never rewrites saved rows. Recording playback keeps its existing priority over TTS.
The editor explains that personal text/pronunciation applies across dialects.
This stage does not add per-dialect user dictionaries or an override-reset UI.

Foundation backups include this metadata in Room and validate schema 1 before
installation. Regression tests cover nine-dialect editor round trips, personal
speech/cues/sentences, phone-only/no-op saves, stale conflicts, seed protection and
new-format backup/restore. Historical migration tests have been replaced by
foundation persistence and explicit legacy rejection tests.
On-device editor interaction and listening to the selected TTS engine remain
necessary before release; source tests cannot establish natural voice quality.
