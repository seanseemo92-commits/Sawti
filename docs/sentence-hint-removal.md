# Sentence-hint removal: UI batch

Branch: fix/prelaunch-cleanup-hint. User explicitly requested removal of the speech-bubble sentence-hint feature and relocation of the phonemic lightbulb into its slot.

This batch removes sentence-hint UI calls/imports from category grid, flashcards, preview and practice. The grid/flashcard lightbulb is the only hint at TopEnd (physical left in Sawti's RTL layout). The editor no longer edits the sentence hint; it passes the original saved baseline value unchanged, avoiding deletion of user data. Sentence composition strip, phonemic hints, playback, recordings, card geometry and version 4.6.1 (12) are preserved.

Pending next batch: remove PhraseHintButton declaration and its chat icon import, LibraryActions.onSentenceHint and implementation, PracticeActions.sentenceHint and implementation, AudioDispatcher.sentenceHint, obsolete resources/comments/tests as verified. Persisted sentence fields/schema must not be dropped or blanked as part of UI cleanup. Historical documentation can describe the retired feature.

Status: source changes prepared; no compilation, lint, test or physical-device result is asserted here. Verify CI for the actual commit, then complete backend removal. Full removal is NOT complete with this UI batch. No main changes, merge or publishing.
