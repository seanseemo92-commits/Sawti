package com.sawti.data

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import com.sawti.data.db.AppDatabase
import com.sawti.data.db.CategoryEntity
import com.sawti.data.db.WordItemEntity
import com.sawti.domain.model.PracticeMode
import com.sawti.domain.model.PracticeOutcome
import kotlinx.coroutines.*
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.SQLiteMode
import java.io.File
import java.util.zip.ZipFile

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
@SQLiteMode(SQLiteMode.Mode.NATIVE)
class BackupSnapshotTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repo: RoomWordRepository
    private val dao get() = db.wordDao()

    @Before fun setup() = runBlocking {
        context = RuntimeEnvironment.getApplication()
        db = AppDatabase.build(context)
        repo = RoomWordRepository(db, context, SettingsRepository(context))
        dao.insertCategory(CategoryEntity("custom_snapshot", "خاص", 0, false, 0))
    }

    @After fun close() { db.close() }

    private fun media(folder: String, name: String, bytes: String) =
        File(context.filesDir, "$folder/$name").apply { parentFile!!.mkdirs(); writeText(bytes) }

    private suspend fun card(photo: String? = null, audio: String? = null) = dao.insertItem(
        WordItemEntity(categoryId = "custom_snapshot", displayText = "بطاقة", ttsPrompt = "بطاقة",
            phonemicCue = "", photoPath = photo, audioPath = audio)
    )

    private suspend fun export(name: String = "backup.zip"): File {
        val archive = File(context.cacheDir, name)
        BackupManager(context, db).export(Uri.fromFile(archive))
        return archive
    }

    private fun entries(archive: File): Map<String, ByteArray> = ZipFile(archive).use { zip ->
        zip.entries().asSequence().associate { it.name to zip.getInputStream(it).use { input -> input.readBytes() } }
    }

    private fun paths(payload: Map<String, ByteArray>): List<Pair<String?, String?>> {
        val file = File(context.cacheDir, "inspect_${System.nanoTime()}.db")
        file.writeBytes(payload.getValue("database/my_words.db"))
        return try {
            SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY).use { sqlite ->
                sqlite.rawQuery("SELECT photoPath,audioPath FROM word_items ORDER BY id", null).use { cursor ->
                    buildList { while (cursor.moveToNext()) add(cursor.getString(0) to cursor.getString(1)) }
                }
            }
        } finally { file.delete() }
    }

    @Test fun legacyAndCurrentRecordingsWithSameNameSurviveRoundTrip() = runBlocking {
        val legacy = media("recordings", "same.m4a", "legacy voice")
        val current = media("custom_audio", "same.m4a", "current voice")
        val a = card(audio = legacy.path); val b = card(audio = current.path)
        val archive = export()
        val payload = entries(archive)
        assertEquals(2, JSONObject(String(payload.getValue("manifest.json"))).getInt("version"))
        val stored = paths(payload).map { requireNotNull(it.second) }
        assertEquals(2, stored.toSet().size)
        assertEquals(setOf("legacy voice", "current voice"),
            stored.map { String(payload.getValue("files/$it")) }.toSet())
        assertEquals(legacy.path, dao.audioPath(a))
        assertEquals(current.path, dao.audioPath(b))
        BackupManager(context, db).restore(Uri.fromFile(archive))
        BackupManager.recoverIfInterrupted(context)
        db = AppDatabase.build(context)
        val restoredA = File(requireNotNull(db.wordDao().audioPath(a)))
        val restoredB = File(requireNotNull(db.wordDao().audioPath(b)))
        assertEquals("legacy voice", restoredA.readText())
        assertEquals("current voice", restoredB.readText())
        assertNotEquals(restoredA.path, restoredB.path)
        assertEquals("custom_audio", restoredA.parentFile!!.name)
    }

    @Test fun aliasesAndCrossColumnSharingExportOneFileAndRestoreOneReference() = runBlocking {
        val shared = media("photos", "shared.jpg", "image bytes")
        val a = card(photo = shared.path)
        val b = card(audio = File(context.filesDir, "photos/../photos/shared.jpg").path)
        val archive = export()
        val payload = entries(archive)
        assertEquals(1, payload.keys.count { it.startsWith("files/") })
        assertEquals(paths(payload)[0].first, paths(payload)[1].second)
        BackupManager(context, db).restore(Uri.fromFile(archive))
        BackupManager.recoverIfInterrupted(context)
        db = AppDatabase.build(context)
        assertEquals(db.wordDao().photoPath(a), db.wordDao().audioPath(b))
    }

    @Test fun unreferencedAndInProgressFilesAreExcluded() = runBlocking {
        card(audio = media("recordings", "saved.m4a", "saved").path)
        media("custom_audio", "still_recording.m4a", "partial")
        media("photos", "orphan.jpg", "orphan")
        val payload = entries(export())
        assertEquals(1, payload.keys.count { it.startsWith("files/") })
        assertEquals("saved", String(payload.getValue("files/${paths(payload).single().second}")))
    }

    @Test fun missingOrExternalReferencedMediaRejectsExportWithoutChangingLiveData() = runBlocking {
        val missing = File(context.filesDir, "recordings/missing.m4a").path
        val id = card(audio = missing)
        var failed = false
        try { export() } catch (_: Exception) { failed = true }
        assertTrue(failed); assertEquals(missing, dao.audioPath(id))
        val outside = File(context.cacheDir, "outside.m4a").apply { writeText("private elsewhere") }
        dao.updateAudio(id, outside.path)
        failed = false
        try { export() } catch (_: Exception) { failed = true }
        assertTrue(failed); assertTrue(outside.exists()); assertEquals(outside.path, dao.audioPath(id))
    }

    @Test fun archivedBytesStayValidAfterLiveAttachmentIsReplaced() = runBlocking {
        val old = media("custom_audio", "old.m4a", "before")
        val id = card(audio = old.path)
        val archive = export()
        repo.importAudio(id, "after".byteInputStream(), "m4a")
        assertFalse(old.exists())
        val payload = entries(archive)
        assertEquals("before", String(payload.getValue("files/${paths(payload).single().second}")))
    }

    @Test fun concurrentExportsAndReplacementsAlwaysContainReferencedBytes() = runBlocking {
        val id = card(audio = media("custom_audio", "initial.m4a", "version-0").path)
        coroutineScope {
            val writer = async(Dispatchers.IO) {
                repeat(15) { repo.importAudio(id, "version-${it+1}".byteInputStream(), "m4a") }
            }
            val exporter = async(Dispatchers.IO) {
                repeat(6) { index ->
                    val payload = entries(export("concurrent-$index.zip"))
                    val relative = requireNotNull(paths(payload).single().second)
                    assertTrue(String(payload.getValue("files/$relative")).startsWith("version-"))
                    assertEquals(1, payload.keys.count { it.startsWith("files/") })
                }
            }
            writer.await(); exporter.await()
        }
    }

    @Test fun gateWaitsForMetadataAndProgressWriters() = runBlocking {
        val id = card()
        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val holder = launch(Dispatchers.IO) {
            db.storage.access { entered.complete(Unit); release.await() }
        }
        entered.await()
        val edit = async(start = CoroutineStart.UNDISPATCHED) {
            repo.updateDetails(id, "جديد", "جديد", "", null, "")
        }
        val progress = async(start = CoroutineStart.UNDISPATCHED) {
            RoomProgressRepository(db).log(id, PracticeMode.RECALL, PracticeOutcome.SELF)
        }
        val backup = async(start = CoroutineStart.UNDISPATCHED) { export() }
        assertFalse(edit.isCompleted); assertFalse(progress.isCompleted); assertFalse(backup.isCompleted)
        release.complete(Unit)
        holder.join(); edit.await(); progress.await(); backup.await()
        assertEquals("جديد", dao.wordById(id)!!.displayText)
    }

    @Test fun oldRepositoriesCannotWriteOrCleanNewRestoredData() = runBlocking {
        val original = media("custom_audio", "old.m4a", "old")
        val id = card(audio = original.path)
        val archive = export()
        BackupManager(context, db).restore(Uri.fromFile(archive))
        val after = media("custom_audio", "new-process-owned.m4a", "keep")
        var failed = false
        try { repo.setAudio(id, after) } catch (_: Exception) { failed = true }
        assertTrue(failed); assertTrue(after.exists())
        // Stale background progress/seeding is discarded rather than crashing after restore.
        RoomProgressRepository(db).log(id, PracticeMode.RECALL, PracticeOutcome.SELF)
        repo.seedOrUpdateDefaults()
        failed = false
        try { export("stale.zip") } catch (_: Exception) { failed = true }
        assertTrue(failed)
        BackupManager.recoverIfInterrupted(context)
    }

    @Test fun stageRespectsLimitsBeforeCreatingOversizedMediaPayload() = runBlocking {
        card(audio = media("recordings", "large.m4a", "x".repeat(200)).path)
        // Reuse the actual online snapshot path, without any live-path rewriting.
        val folder = File(context.cacheDir, "bounded").apply { mkdirs() }
        val snapshot = File(folder, "copy.db")
        db.storage.access {
            db.openHelper.writableDatabase.execSQL("VACUUM INTO ?", arrayOf(snapshot.path))
            val files = linkedMapOf("database/my_words.db" to snapshot)
            var failed = false
            try {
                BackupMediaSnapshot.stage(context, snapshot, folder, files, snapshot.length() + 100, 2000)
            } catch (_: Exception) { failed = true }
            assertTrue(failed)
            assertFalse(File(folder, "files").exists())
        }
    }
}
