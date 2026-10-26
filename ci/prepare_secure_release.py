"""Hardening applied only to the sanitized public Android build.
Fail on unexpected source so a partially patched release never ships.
"""
from pathlib import Path
root=Path("extracted/MyWords")
src=root/"app/src/main/java/com/mywords"
backup=src/"data/BackupManager.kt"
s=backup.read_text(encoding="utf-8")
original='require(sqlite.version == 3) { "إصدار قاعدة البيانات غير مدعوم" }'
assert s.count(original)==1, "Unexpected export database-version check"
s=s.replace(original,'require(sqlite.version == 4) { "إصدار قاعدة البيانات غير مدعوم" }')
original='require(version == 3) { "إصدار قاعدة البيانات غير مدعوم" }'
assert s.count(original)==1, "Unexpected restore database-version check"
s=s.replace(original,'require(version in 3..4) { "إصدار قاعدة البيانات غير مدعوم" }')
backup.write_text(s,encoding="utf-8")

repo=src/"data/RoomWordRepository.kt"
s=repo.read_text(encoding="utf-8")
assert s.count('old?.let { File(it).delete() }')==1
s=s.replace('old?.let { File(it).delete() }','deletePrivateMedia(old)')
assert s.count('dao.photoPath(id)?.let { File(it).delete() }')==1
s=s.replace('dao.photoPath(id)?.let { File(it).delete() }','deletePrivateMedia(dao.photoPath(id))')
assert s.count('if (old != null && old != file.absolutePath) File(old).delete()')==1
s=s.replace('if (old != null && old != file.absolutePath) File(old).delete()','if (old != file.absolutePath) deletePrivateMedia(old)')
assert s.count('if (old != null && old != target.absolutePath) File(old).delete()')==1
s=s.replace('if (old != null && old != target.absolutePath) File(old).delete()','if (old != target.absolutePath) deletePrivateMedia(old)')
assert s.count('dao.audioPath(id)?.let { File(it).delete() }')==1
s=s.replace('dao.audioPath(id)?.let { File(it).delete() }','deletePrivateMedia(dao.audioPath(id))')
# Avoid deleting user-selected content or a path imported through a legacy backup.
preamble='    private companion object {\n        const val KEY_SEED_VERSION'
assert s.count(preamble)==1
helper='''    /** Never delete arbitrary paths from a database imported through a user-selected backup. */
    private fun deletePrivateMedia(raw: String?) {
        if (raw.isNullOrBlank()) return
        runCatching {
            val file = File(raw).canonicalFile
            val root = filesDir.canonicalFile
            val allowed = setOf("photos", "custom_audio", "recordings")
            if (file.isFile && file.parentFile?.parentFile == root &&
                file.parentFile?.name in allowed) {
                file.delete()
            }
        }
    }

'''
s=s.replace(preamble,helper+preamble)
repo.write_text(s,encoding="utf-8")

manifest=root/"app/src/main/AndroidManifest.xml"
s=manifest.read_text(encoding="utf-8")
assert s.count('android:allowBackup="true"')==1
s=s.replace('android:allowBackup="true"','android:allowBackup="false"\n        android:fullBackupContent="false"')
manifest.write_text(s,encoding="utf-8")

build=root/"app/build.gradle.kts"
s=build.read_text(encoding="utf-8")
assert s.count('versionCode = 6')==1 and s.count('versionName = "4.1"')==1
s=s.replace('versionCode = 6','versionCode = 7').replace('versionName = "4.1"','versionName = "4.2"')
build.write_text(s,encoding="utf-8")

# Replace obsolete comment that erroneously claimed private sample names are seeded.
app=src/"MyWordsApp.kt"
s=app.read_text(encoding="utf-8")
s=s.replace('// First run (or after a schema upgrade): insert the 21 family members + all categories.',
            '// On startup add public relationship labels and built-in vocabulary, never sample people.')
app.write_text(s,encoding="utf-8")
assert not (src/"data/db/LegacyFamilyDefaults.kt").exists()
assert "LegacyFamilyCleanup.removeUntouchedOldSamples" in (src/"data/db/AppDatabase.kt").read_text()
print("Release hardening verified: backup schema 4, restore 3-4, safe media deletion, no cloud backup, v4.2")
