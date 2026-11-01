# Consistent foundation backups

Every repository mutation, seed operation, progress write and media cleanup uses
the StorageCoordinator owned by the singleton AppDatabase. Export holds this gate
while taking a SQLite snapshot and staging its referenced files. Compression,
hashing and SAF delivery then read immutable staged data after releasing the gate.
The checkpoint/copy fallback for older SQLite uses the same gate.

The new contract is `sawti-foundation-backup` version 1, with separate database
identity `sawti-foundation` and schema version 1. `database/sawti.db` and validated
`preferences/settings.xml` are mandatory. There is no historical backup conversion.
Future releases must explicitly preserve this foundation contract or migrate it;
changing a version number is not permission to discard new users' data.

Media references must resolve to files directly within private `photos/` or
`custom_audio/`. Missing or escaping paths reject export. Unreferenced/in-progress
files are excluded. Canonical aliases are copied once; unique archive filenames
preserve distinct files even when their basenames match. Only the snapshot gets
relative paths. Restore validates them and installs absolute private paths.
No live database paths are rewritten during export.

Settings use one in-memory SharedPreferences snapshot, with typed serialization
and validation. Restore holds the gate through installation and retires the old
Room instance. Queued old edits/exports reject use; stale seed/progress/cleanup
operations cannot touch newly restored files. Startup recovery runs before stores.

Native-SQLite tests cover round trips, sharing, same basenames, concurrent exports
and replacements, writer ordering, stale repositories, bounded copying and the
checkpoint fallback. Device SAF, storage exhaustion and process death still need
manual validation. Automated tests do not verify speech or recording quality.
