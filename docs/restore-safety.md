# Restore validation and first-start recovery

Restore accepts ZIP format versions 1–2 and database versions 3–6. See consistent-backups.md for version-2 relative media paths and export coordination.
Before closing the live Room instance it verifies archive bounds/hashes, rejects
persistent SQL triggers/views, removes the *staged* Room identity table, and opens
the staged database using the same generated Room schema and migrations as the app.
An identity hash supplied by an archive is never proof of schema compatibility.
Foreign-key violations and missing referenced attachments reject the import.

Settings are limited to 64 KiB and reconstructed from a whitelist of typed,
bounded values. Unknown keys, duplicate keys, incorrect types, unsupported enum
values, nested markup and document type declarations reject the import.
Missing settings/keys remain supported. No raw imported preference XML is installed.
This intentionally rejects incomplete archives instead of silently deleting media
references; it does not yet repair older exports that omitted recordings/.

The journal retains the original database, media and preferences after installation.
An installed marker means *pending startup validation*, not success of startup.
Application startup already calls recoverIfInterrupted before opening any stores:
it validates/opens the installed Room database and checks installed settings, then
writes the separate committed marker before deleting the retained originals.
Failure resumes rollback. Removing the installed marker before rollback ensures
an interrupted rollback cannot later commit a mixture of old and new files.
The committed-marker cleanup path remains compatible with older installations.
Preference .bak and SQLite rollback-journal files are included in the swap.

The temporary validation Room instance uses TRUNCATE journaling so the installed
database is self-contained. Main-thread permission applies only to that short-lived
validator during startup recovery; the normal app database keeps its thread guard.

Regression tests use Robolectric with native SQLite and the real generated Room
implementation. They exercise actual ZIP imports, a genuine Room identity paired
with a forged schema, wrong preference types, missing media, trigger rejection,
v3/v4 migrations, retained originals, startup success/failure, preference sidecars,
and interrupted installation. Settings tests cover canonical round trips and
malformed/oversized input. Run:

```sh
gradle --no-daemon :app:assembleDebug :app:assembleRelease :app:testDebugUnitTest :app:lintRelease
```

Scope: this change addresses audit item 1 and refuses missing attachments safely.
It does not solve concurrent repository/media writes, shared-file deletion,
legacy recording export, seed-version synchronization, or the other audit items.
On-device process-death and low-storage testing remains required before release.
No destructive migration, database rename or UI redesign is introduced.
