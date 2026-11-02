# Sentence-hint removal

Branch: fix/prelaunch-cleanup-hint. User explicitly requested deletion of the speech-bubble card hint and relocation of the phonemic lightbulb into its slot. No main, signing, version or publication changes.

## UI batch
Category grid, flashcards, preview and practice no longer call the removed hint. The lightbulb occupies TopEnd (physical left in the RTL app). The editor no longer offers the retired field and preserves its saved baseline value. Sentence composition remains separate.

## Runtime removal batch
Remove the PhraseHintButton declaration and chat-icon import, LibraryActions.onSentenceHint and its ViewModel implementation, PracticeActions.sentenceHint and its implementation, and AudioDispatcher.sentenceHint. Practice comments now describe only the phonemic cue. Existing playback, recordings, practice scoring, loading/retry, theme fixes and sentence-strip sequencing are retained.

Add source-guard unit tests checking absence of the four retired runtime symbols and presence of the phonemic hint and sentence-strip playback. Tests have NOT been executed by the assistant; verify CI for the resulting commit.

## Deliberately retained / outstanding
Persisted sentencePrompt and CardDetails.sentence values, backup/schema/migration support and baseline-preserving save plumbing are kept to avoid deleting user data. These are not an exposed card-hint feature. Historical documentation may mention the retired feature. The unused sentence_hint and field_sentence XML labels still need a separate resource cleanup; no claim that resource cleanup is complete.

Manual checks remain: lamp placement in RTL, phonemic cue playback, editor save preserving data, sentence strip, practice scoring, light/dark and large font. No claim of passed build, tests, lint or UI acceptance until results are verified.
