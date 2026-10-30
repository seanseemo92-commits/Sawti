# Canonical editing and personal pronunciation

This branch builds on the cumulative data-safety work in PR #26.
Dialect projections carry immutable source details; the caregiver editor displays
those saved details, not translated labels, synthesized speech or suppressed
sentence hints. Its opening baseline stays fixed until it closes. Saving merges
only changed fields against the latest row in one transaction. Unrelated edits,
favorites and media survive; conflicting changes to the same field reject the save.
Family-group edits are validated and committed in the same transaction.

Room v6 adds one integer customTextFields column (default 0), with bits for label,
speech, cue and sentence. There is no destructive migration. Migration 5→6 compares
existing values with the built-in seed by category/subgroup/semantic key, falling
back to the original name. Differences are marked as personal overrides; unmatched
historical cards are conservatively marked personal. No existing text/media/ID is
rewritten. An old customization identical to a default cannot be inferred from old
data. Previously overwritten/lost text also cannot be reconstructed automatically.

Changed text fields are explicit overrides and remain as saved across dialects;
custom speech bypasses the curated pronunciation and automatic vocalization.
Unedited built-in fields continue to localize. Explicit sentence removal is
protected from seed refilling. Seed pronunciation/rename queries also respect
the override bits. Recording playback keeps its existing priority over TTS.
The editor explains that personal text/pronunciation applies across dialects.
This stage does not add per-dialect user dictionaries or an override-reset UI.

Backups include the metadata in Room. Restore accepts database schemas 3–6 and
upgrades them through the same migration path; export snapshots schema 6.
Regression coverage includes all-nine-dialect editor round trips, exact custom
speech/cues/sentences, phone-only and no-op saves, stale editor conflict handling,
seed protection, and a real v5→v6 migration preserving old personal values/media.
The data-safety tests from the previous stages run alongside these tests.
On-device editor interaction and listening to the selected TTS engine remain
necessary before release; source tests cannot establish natural voice quality.
