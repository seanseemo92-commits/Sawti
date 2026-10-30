# Shared attachment ownership

All repository photo/audio replacement, clearing and card deletion paths now share
one process-wide mutex. Row changes run in a Room transaction and must affect an
existing card. Files are considered for deletion only after that transaction ends.
Cleanup reacquires the same mutex and reads both media columns, comparing canonical
paths so aliases from older/imported data cannot hide another reference.

A failed update leaves the old reference and file intact. A successful update may
delete the retired file only if no card references it. New imports/recordings are
also cleanup candidates: the database's actual committed state decides whether to
keep them, even when cancellation is delivered just after commit. Cleanup runs in
NonCancellable on IO. Query/path/delete failures retain files rather than risk data
loss; a crash can therefore leave an orphan, which is safer than a broken live card.
Only files directly inside private photos/, custom_audio/ or recordings/ are
eligible. External files, escaping symlinks and directories are never deleted.

setAudio takes responsibility for a finished private recording. The ViewModel no
longer deletes it on a repository exception, because that exception can follow a
successful commit. Imported streams are owned/closed once by the public import,
including early validation failures or cancellation. Import helpers borrow them.

Twenty native-SQLite Robolectric tests cover sharing across cards and columns,
canonical aliases, family/custom deletion, relationship-card protection, all
replacement routes, SQL-aborted updates/deletes, rejected/missing cards, partial
imports, legacy recordings, external paths, cancellation, failures after commit
and interleaved operations through two repository instances.

Run the standard Debug/Release/unit/lint workflow. No schema migration is needed.
This patch is independent of restore-hardening PR #24; both use the same test-only
Robolectric configuration. Backup/export coordination, restore writer quiescence,
legacy recording export and orphan garbage collection remain separate work.
All future code assigning/removing media references must use this ownership path,
not raw DAO writes or direct file deletion.
