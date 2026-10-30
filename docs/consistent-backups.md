# Consistent backups and legacy recordings

This integration branch contains the restore hardening from PR #24 and shared-media
ownership from PR #25, followed by this stage. Main is not changed by preparing it.

Every word-repository mutation, seeding operation, progress write and media cleanup
uses the StorageCoordinator owned by the app's singleton AppDatabase. Export holds
that gate while taking the SQLite snapshot and copying its referenced media into
private staging. It then releases the gate; hashing, compression and SAF delivery
read only staged files. This also excludes writes during the checkpoint/copy
fallback used on older SQLite versions. Future writers must use the same gate.

Media is selected from the copied database, not by enumerating live folders.
References under private photos/, custom_audio/ and legacy recordings/ are copied.
Active recordings and orphan files with no card reference are excluded. Missing,
external or escaping media paths reject export instead of producing an incomplete
backup. A canonical source is copied once, preserving sharing across rows/columns.
Unique archive names prevent legacy/current files with equal basenames colliding.
Only the staged database receives relative paths; live data is never normalized
or deleted by export. Limits apply while copying, not only after building the ZIP.

New exports use sawti-backup format version 2 with exact relative media paths.
Restore accepts both version 1 (the previous basename/column-folder convention)
and version 2. Version 2 converts legacy recordings into restored custom_audio
files without overwriting a same-named modern recording. Older app builds reject
version 2; update the receiving app before restoring a newly exported backup.
An old archive that already omitted a recording cannot reconstruct its bytes.

Settings use one SharedPreferences.getAll() snapshot, serialized and validated in
staging, rather than copying an XML file that may be replaced while it is read.
Restore holds the gate through installation. Once it closes Room, the old instance
is retired even if rollback is needed: queued old edits/exports are rejected, old
media cleanup is skipped, and stale background seed/progress writes are discarded.
Startup recovery runs before the new application's stores are opened.

Regression tests include real ZIP export/restore with same-named legacy/current
recordings, cross-column aliases, orphan/in-progress exclusion, missing/external
media rejection, concurrent exports/replacements, writer-gate ordering, retired
repository behavior and bounded copying. The suites from stages one and two run
alongside them. Physical-device SAF, low-storage and process-death checks remain
needed before release. Seed-version synchronization and orphan collection remain
separate audit items; no destructive schema migration is introduced here.
