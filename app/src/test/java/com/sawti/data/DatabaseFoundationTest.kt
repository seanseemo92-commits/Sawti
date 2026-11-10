package com.sawti.data

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import androidx.room.withTransaction
import com.sawti.data.db.*
import com.sawti.domain.model.*
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
@SQLiteMode(SQLiteMode.Mode.NATIVE)
class DatabaseFoundationTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repo: RoomWordRepository

    @Before fun setup() {
        context = RuntimeEnvironment.getApplication()
        reopen()
    }

    private fun reopen() {
        db = AppDatabase.build(context)
        repo = RoomWordRepository(db, context, SettingsRepository(context))
    }

    @After fun close() { db.close() }

    private suspend fun fails(block: suspend () -> Unit) {
        var failure: Exception? = null
        try { block() } catch (error: Exception) { failure = error }
        assertNotNull("Operation should reject invalid data", failure)
    }

    @Test fun freshSchemaMatchesExportedContractAndIgnoresHistoricalDatabase() = runBlocking {
        val old = context.getDatabasePath("my_words.db")
        old.parentFile!!.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(old, null).use {
            it.execSQL("CREATE TABLE personal_old_data(value TEXT)")
            it.execSQL("INSERT INTO personal_old_data VALUES('keep untouched')")
            it.version = 7
        }
        val before = old.readBytes()
        repo.seedOrUpdateDefaults()
        assertEquals(1, db.openHelper.readableDatabase.version)
        assertEquals(13, db.wordDao().categoryIds().size)
        assertEquals(377, db.wordDao().observeItems().first().size)
        assertArrayEquals(before, old.readBytes())
        val exported = JSONObject(File("schemas/com.sawti.data.db.AppDatabase/1.json").readText())
            .getJSONObject("database")
        assertEquals(AppDatabase.VERSION, exported.getInt("version"))
        db.openHelper.readableDatabase.query("SELECT identity_hash FROM room_master_table WHERE id=42").use {
            assertTrue(it.moveToFirst())
            assertEquals(exported.getString("identityHash"), it.getString(0))
        }
        assertTrue(context.getDatabasePath(AppDatabase.NAME).isFile)
    }

    @Test fun uniqueConceptsAndForeignKeysRejectInvalidWritesAtomically() = runBlocking {
        repo.seedOrUpdateDefaults()
        val dao = db.wordDao()
        val original = dao.observeItems().first()
        val seed = original.first()
        fails {
            db.withTransaction {
                dao.insertCategory(CategoryEntity("custom_rollback", "مؤقت", 0, false, 0))
                dao.insertItem(seed.copy(id = 0))
            }
        }
        assertNull(dao.categoryById("custom_rollback"))
        assertEquals(original, dao.observeItems().first())
        fails { dao.insertItem(seed.copy(id = 0, categoryId = "missing", semanticKey = null)) }
        fails { db.progressDao().insertAttempt(ProgressRecordEntity(wordId = Long.MAX_VALUE,
            timestamp = 1, mode = "RECALL", outcome = "SELF")) }
        assertTrue(db.progressDao().observeOutcomeCounts().first().isEmpty())
        val category = repo.addCustomCategory("خاص", 0, 900)
        val a = repo.addCustomCard(category, "نفس النص", "نطق")
        val b = repo.addCustomCard(category, "نفس النص", "نطق")
        assertNotEquals(a, b)
        assertNull(dao.wordById(a)!!.semanticKey)
        assertNull(dao.wordById(b)!!.semanticKey)
    }

    @Test fun editMoveDeleteAndReopenPreserveCustomCardAndCategoryState() = runBlocking {
        val first = repo.addCustomCategory("الأول", 0, 800)
        val second = repo.addCustomCategory("الثاني", 1, 801)
        val id = repo.addCustomCard(first, "بطاقتي", "نطقي")
        fails { repo.deleteEmptyCustomCategory(first) }
        repo.updateDetails(id, "نص جديد", "صوت جديد", "تلميح", "جملتي", "123")
        repo.moveCardToCustomCategory(id, second)
        repo.editCustomCategory(second, "عنوان جديد", 2, 802)
        repo.deleteEmptyCustomCategory(first)
        val expected = db.wordDao().wordById(id)!!
        db.close(); reopen()
        assertEquals(expected, db.wordDao().wordById(id))
        assertEquals(second, expected.categoryId)
        assertEquals(15, expected.customTextFields)
        assertEquals(CategoryEntity(second, "عنوان جديد", 2, false, 802), db.wordDao().categoryById(second))
        assertNull(db.wordDao().categoryById(first))
        repo.deleteCustomCard(id)
        repo.deleteEmptyCustomCategory(second)
        db.close(); reopen()
        assertNull(db.wordDao().wordById(id))
        assertTrue(db.wordDao().categoryIds().isEmpty())
    }

    @Test fun deletedCardsAndLateResultsKeepHistoryWithoutDanglingReferences() = runBlocking {
        repo.seedOrUpdateDefaults()
        val category = repo.addCustomCategory("خاص", 0, 900)
        val card = repo.addCustomCard(category, "بطاقة تدريب", "تدريب")
        val person = repo.addFamilyMember("شخص مخصص", "الأبناء", "123")
        val progress = RoomProgressRepository(db)
        progress.log(card, PracticeMode.RECALL, PracticeOutcome.CUED)
        progress.log(person, PracticeMode.RECALL, PracticeOutcome.SHOWN)
        assertEquals(2, progress.observeSummary().first().cuedWords.size)
        repo.deleteCustomCard(card)
        repo.deleteFamilyMember(person)
        progress.log(card, PracticeMode.RECALL, PracticeOutcome.SELF)
        val summary = progress.observeSummary().first()
        assertEquals(3, summary.recallAttempts)
        assertEquals(1, summary.selfRecalled)
        assertEquals(1, summary.cued)
        assertEquals(1, summary.shown)
        assertTrue(summary.cuedWords.isEmpty())
        db.openHelper.readableDatabase.query("SELECT COUNT(*) FROM progress_records WHERE wordId IS NULL").use {
            assertTrue(it.moveToFirst()); assertEquals(3, it.getInt(0))
        }
        db.close(); reopen()
        assertEquals(summary, RoomProgressRepository(db).observeSummary().first())
        RoomProgressRepository(db).clear()
        assertEquals(0, RoomProgressRepository(db).observeSummary().first().recallAttempts)
    }

    @Test fun futureSchemaIsRejectedWithoutDestructiveFallback() = runBlocking {
        val category = repo.addCustomCategory("لا تحذف بياناتي", 0, 900)
        db.close()
        val file = context.getDatabasePath(AppDatabase.NAME)
        SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READWRITE).use { it.version = 2 }
        reopen()
        fails { db.wordDao().categoryIds() }
        SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY).use {
            assertEquals(2, it.version)
            it.rawQuery("SELECT title FROM categories WHERE id=?", arrayOf(category)).use { row ->
                assertTrue(row.moveToFirst()); assertEquals("لا تحذف بياناتي", row.getString(0))
            }
        }
    }

    @Test fun newBackupRestoresFamilyCustomCardsSettingsAndDeletedCardHistory() = runBlocking {
        repo.seedOrUpdateDefaults()
        val category = repo.addCustomCategory("احتياجات خاصة", 3, 900)
        val card = repo.addCustomCard(category, "طلبي", "نطقي")
        val deleted = repo.addCustomCard(category, "مؤقت", "مؤقت")
        val person = repo.addFamilyMember("شخص مخصص", "الأبناء", "123456")
        repo.updateDetails(person, "شخص مخصص", "نطق مخصص", "تلميحي", "جملتي", "123456")
        val photo = File(context.filesDir, "photos/person.jpg").apply { parentFile!!.mkdirs(); writeText("photo") }
        val audio = File(context.filesDir, "custom_audio/person.m4a").apply { parentFile!!.mkdirs(); writeText("audio") }
        db.wordDao().updatePhoto(person, photo.path)
        db.wordDao().updateAudio(person, audio.path)
        val progress = RoomProgressRepository(db)
        progress.log(person, PracticeMode.RECALL, PracticeOutcome.CUED)
        progress.log(deleted, PracticeMode.CHOICE, PracticeOutcome.CORRECT)
        repo.deleteCustomCard(deleted)
        val beforeProgress = progress.observeSummary().first()
        val beforeCards = db.wordDao().observeItems().first()
        val beforeCategories = db.wordDao().observeCategories().first()
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        prefs.edit().putString("dialect", ArabicDialect.STANDARD.name).putBoolean("haptics", false).commit()
        val archive = File(context.cacheDir, "foundation.zip")
        val manager = BackupManager(context, db)
        manager.export(Uri.fromFile(archive))
        repo.deleteFamilyMember(person)
        repo.deleteCustomCard(card)
        progress.clear()
        prefs.edit().putBoolean("haptics", true).commit()
        manager.restore(Uri.fromFile(archive))
        BackupManager.recoverIfInterrupted(context)
        reopen()
        repo.seedOrUpdateDefaults()
        assertEquals(beforeCategories, db.wordDao().observeCategories().first())
        val after = db.wordDao().observeItems().first()
        assertEquals(beforeCards.map { it.copy(photoPath = null, audioPath = null) },
            after.map { it.copy(photoPath = null, audioPath = null) })
        val restoredPerson = after.single { it.id == person }
        assertEquals("photo", File(restoredPerson.photoPath!!).readText())
        assertEquals("audio", File(restoredPerson.audioPath!!).readText())
        assertNull(db.wordDao().wordById(deleted))
        assertEquals(beforeProgress, RoomProgressRepository(db).observeSummary().first())
        // Restore requires process restart; inspect disk rather than the old cached preferences.
        val xml = File(context.applicationInfo.dataDir, "shared_prefs/settings.xml").readText()
        assertTrue(xml.contains("STANDARD"))
        assertTrue(xml.contains("name=\"haptics\" value=\"false\""))
    }
}
