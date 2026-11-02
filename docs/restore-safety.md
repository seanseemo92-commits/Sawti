# Foundation restore validation and first-start recovery

The new application accepts only `sawti-foundation-backup` format version 1,
`databaseIdentity=sawti-foundation`, database version 1. The mandatory payloads
are `database/sawti.db` and `preferences/settings.xml`. Historical archives and
schemas are intentionally unsupported; they are rejected before closing live Room.

Before installation, restore checks entry/path/size bounds, exact manifest hashes,
typed settings, actual Room tables/columns/indices/foreign keys, SQLite integrity,
row validity and every referenced attachment. Persistent triggers, views and extra
tables are rejected. The staged Room identity table is removed so a forged hash
cannot bypass generated schema validation. Only the staged copy is modified.
Settings are reconstructed from a typed whitelist; an empty map uses app defaults,
but a missing settings payload is an error.

The `sawti_foundation_restore_journal` retains original database, media and settings
until the next startup validates the installed database and required settings.
An installed marker means pending startup validation. Failure resumes rollback;
removing the installed marker first prevents a partial rollback from committing.
The separate `sawti_foundation_restore_committed` marker makes cleanup resumable.
Preference `.bak`, SQLite WAL/SHM and rollback-journal files participate in the swap.
Historical journal names are not consumed by the new application.

Recovery runs before application stores open. Temporary validation uses TRUNCATE
journaling and permits main-thread queries only on that short-lived instance.
The regular Room instance retains its thread guard. Once restore closes Room,
that instance is retired; the UI must restart the process to reload stores.

Robolectric/native-SQLite tests exercise real ZIP import, forged schemas and indices,
old formats/versions, wrong identity, missing/wrong settings, missing attachments,
orphans, triggers, extra tables, invalid override bits, interrupted installation,
rollback and cleanup. Physical-device process-death, SAF and low-storage checks
remain required before release.
