package com.sawti.data

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sawti.data.db.AppDatabase
import com.sawti.data.db.CategoryEntity
import com.sawti.data.db.WordItemEntity
import com.sawti.domain.model.FamilyRelationships
import kotlinx.coroutines.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.SQLiteMode
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
@SQLiteMode(SQLiteMode.Mode.NATIVE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SharedMediaSafetyTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repo: RoomWordRepository
    private val dao get() = db.wordDao()

    @Before fun setup() = runBlocking {
        context = RuntimeEnvironment.getApplication()
        db = Room.databaseBuilder(context, AppDatabase::class.java, "media-test.db")
            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE).allowMainThreadQueries().build()
        repo = RoomWordRepository(db, context, SettingsRepository(context))
        dao.insertCategories(listOf(
            CategoryEntity("family", "العائلة", 0, false, 0),
            CategoryEntity("custom_test", "خاص", 0, false, 1),
        ))
    }

    @After fun cleanup() { db.close() }

    private fun file(folder: String = "custom_audio", name: String = "shared.m4a") =
        File(context.filesDir, "$folder/$name").apply { parentFile!!.mkdirs(); writeText("original media") }

    private suspend fun card(
        photo: String? = null, audio: String? = null,
        category: String = "custom_test", group: String = "",
    ) = dao.insertItem(WordItemEntity(
        categoryId = category, subgroup = group, displayText = "بطاقة",
        ttsPrompt = "بطاقة", phonemicCue = "", photoPath = photo, audioPath = audio,
    ))

    private fun picture(): ByteArray {
        val bitmap = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888)
        return ByteArrayOutputStream().use {
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
            bitmap.recycle()
            it.toByteArray()
        }
    }

    private fun failWrites(operation: String = "UPDATE") {
        db.openHelper.writableDatabase.execSQL(
            "CREATE TRIGGER fail_write BEFORE $operation ON word_items BEGIN SELECT RAISE(ABORT, 'simulated disk failure'); END"
        )
    }

    private suspend fun expectFailure(block: suspend () -> Unit) {
        var failed = false
        try { block() } catch (_: Exception) { failed = true }
        assertTrue("Operation must fail", failed)
    }

    @Test fun sharedPhotoSurvivesUntilLastReferenceIsCleared() = runBlocking {
        val f = file("photos", "shared.jpg")
        val a = card(photo = f.path); val b = card(photo = f.path)
        repo.clearPhoto(a)
        assertNull(dao.photoPath(a)); assertTrue(f.exists())
        assertEquals(f.path, dao.photoPath(b))
        repo.clearPhoto(b)
        assertFalse(f.exists())
    }

    @Test fun sharedAudioSurvivesUntilLastReferenceIsCleared() = runBlocking {
        val f = file()
        val a = card(audio = f.path); val b = card(audio = f.path)
        repo.clearAudio(a)
        assertTrue(f.exists()); assertEquals(f.path, dao.audioPath(b))
        repo.clearAudio(b)
        assertFalse(f.exists())
    }

    @Test fun referencesAcrossBothColumnsAndCanonicalAliasesAreProtected() = runBlocking {
        val f = file("photos", "shared.jpg")
        val alias = File(context.filesDir, "photos/../photos/shared.jpg").path
        val a = card(photo = f.path, audio = alias)
        repo.clearPhoto(a)
        assertTrue(f.exists())
        repo.clearAudio(a)
        assertFalse(f.exists())
    }

    @Test fun familyAndCustomDeletionShareOneOwnershipRule() = runBlocking {
        val photo = file("photos", "shared.jpg"); val audio = file()
        val a = card(photo.path, audio.path, "family", "الأبناء")
        val b = card(photo.path, audio.path)
        repo.deleteFamilyMember(a)
        assertNull(dao.wordById(a)); assertTrue(photo.exists()); assertTrue(audio.exists())
        repo.deleteCustomCard(b)
        assertNull(dao.wordById(b)); assertFalse(photo.exists()); assertFalse(audio.exists())
    }

    @Test fun presetFamilyCardsAndBuiltInCategoriesRemainProtected() = runBlocking {
        val f = file()
        val a = card(audio = f.path, category = "family", group = FamilyRelationships.RELATIVES)
        repo.deleteFamilyMember(a)
        expectFailure { repo.deleteCustomCard(a) }
        assertNotNull(dao.wordById(a)); assertTrue(f.exists())
    }

    @Test fun replacingPhotoKeepsSharedOldFileAndStoresCompleteNewImage() = runBlocking {
        val f = file("photos", "shared.jpg")
        val a = card(photo = f.path); val b = card(photo = f.path)
        repo.setPhoto(a, picture().inputStream())
        val replacement = File(requireNotNull(dao.photoPath(a)))
        assertNotEquals(f.path, replacement.path)
        assertTrue(replacement.length() > 0); assertTrue(f.exists())
        repo.clearPhoto(b)
        assertFalse(f.exists()); assertTrue(replacement.exists())
    }

    @Test fun importingAudioKeepsSharedOldFile() = runBlocking {
        val f = file()
        val a = card(audio = f.path); val b = card(audio = f.path)
        repo.importAudio(a, "new audio".byteInputStream(), "m4a")
        val replacement = File(requireNotNull(dao.audioPath(a)))
        assertEquals("new audio", replacement.readText()); assertTrue(f.exists())
        repo.clearAudio(b)
        assertFalse(f.exists()); assertTrue(replacement.exists())
    }

    @Test fun attachingRecordingKeepsSharedOldFileAndSameFileReassignment() = runBlocking {
        val f = file(); val next = file(name = "next.m4a")
        val a = card(audio = f.path); val b = card(audio = f.path)
        repo.setAudio(a, next)
        assertTrue(f.exists()); assertTrue(next.exists())
        repo.setAudio(a, File(context.filesDir, "custom_audio/../custom_audio/next.m4a"))
        assertTrue(next.exists())
        repo.clearAudio(b)
        assertFalse(f.exists()); assertTrue(next.exists())
    }

    @Test fun failedPhotoClearKeepsFileAndDatabaseReference() = runBlocking {
        val f = file("photos", "old.jpg"); val a = card(photo = f.path)
        failWrites()
        expectFailure { repo.clearPhoto(a) }
        assertEquals(f.path, dao.photoPath(a)); assertEquals("original media", f.readText())
    }

    @Test fun failedAudioClearKeepsFileAndDatabaseReference() = runBlocking {
        val f = file(); val a = card(audio = f.path)
        failWrites()
        expectFailure { repo.clearAudio(a) }
        assertEquals(f.path, dao.audioPath(a)); assertTrue(f.exists())
    }

    @Test fun failedDeletionKeepsFamilyAndCustomMedia() = runBlocking {
        val f = file()
        val a = card(audio = f.path); val b = card(audio = f.path, category = "family", group = "الأبناء")
        failWrites("DELETE")
        expectFailure { repo.deleteCustomCard(a) }
        expectFailure { repo.deleteFamilyMember(b) }
        assertNotNull(dao.wordById(a)); assertNotNull(dao.wordById(b)); assertTrue(f.exists())
    }

    @Test fun failedImportsRemoveOnlyNewFiles() = runBlocking {
        val old = file(); val photo = file("photos", "old.jpg")
        val a = card(photo.path, old.path)
        failWrites()
        expectFailure { repo.importAudio(a, "new".byteInputStream(), "m4a") }
        expectFailure { repo.setPhoto(a, picture().inputStream()) }
        assertEquals(setOf(old.name), old.parentFile!!.list()!!.toSet())
        assertEquals(setOf(photo.name), photo.parentFile!!.list()!!.toSet())
        assertEquals(old.path, dao.audioPath(a)); assertEquals(photo.path, dao.photoPath(a))
    }

    @Test fun failedRecordingAssignmentDeletesOnlyUnreferencedRecording() = runBlocking {
        val old = file(); val next = file(name = "next.m4a")
        val a = card(audio = old.path); val owner = card(audio = next.path)
        failWrites()
        expectFailure { repo.setAudio(a, next) }
        assertTrue(next.exists()); assertEquals(next.path, dao.audioPath(owner))
        val unowned = file(name = "unowned.m4a")
        expectFailure { repo.setAudio(a, unowned) }
        assertFalse(unowned.exists()); assertTrue(old.exists())
    }

    @Test fun missingCardCannotProduceOrphanImportsOrSuccessfulAssignment() = runBlocking {
        val next = file(name = "unowned.m4a")
        expectFailure { repo.setAudio(999, next) }
        assertFalse(next.exists())
        expectFailure { repo.importAudio(999, "new".byteInputStream(), "m4a") }
        expectFailure { repo.setPhoto(999, picture().inputStream()) }
        expectFailure { repo.clearPhoto(999) }
        expectFailure { repo.clearAudio(999) }
        assertTrue(File(context.filesDir, "photos").listFiles().orEmpty().isEmpty())
        assertTrue(File(context.filesDir, "custom_audio").listFiles().orEmpty().isEmpty())
    }

    @Test fun externalFilesAndEscapingSymlinksAreNeverDeleted() = runBlocking {
        val outside = File(context.cacheDir, "outside.m4a").apply { writeText("keep") }
        val dir = File(context.filesDir, "custom_audio").apply { mkdirs() }
        val link = File(dir, "link.m4a")
        Files.createSymbolicLink(link.toPath(), outside.toPath())
        val a = card(photo = outside.path, audio = link.path)
        repo.deleteCustomCard(a)
        assertEquals("keep", outside.readText()); assertTrue(link.exists())
        val b = card()
        expectFailure { repo.setAudio(b, outside) }
        assertTrue(outside.exists()); assertNull(dao.audioPath(b))
    }

    @Test fun legacyRecordingsAlsoRespectSharedOwnership() = runBlocking {
        val f = file("recordings", "legacy.m4a")
        val a = card(audio = f.path, category = "family", group = "الأبناء")
        val b = card(audio = f.path)
        repo.deleteFamilyMember(a)
        assertTrue(f.exists())
        repo.deleteCustomCard(b)
        assertFalse(f.exists())
    }

    @Test fun invalidPhotoInputKeepsExistingPhotoAndLeavesNoPartialFile() = runBlocking {
        val f = file("photos", "old.jpg"); val a = card(photo = f.path)
        expectFailure { repo.setPhoto(a, "not an image".byteInputStream()) }
        assertEquals(f.path, dao.photoPath(a)); assertTrue(f.exists())
        assertEquals(setOf(f.name), f.parentFile!!.list()!!.toSet())
        assertFalse(context.filesDir.listFiles().orEmpty().any { it.name.endsWith(".incoming") })
    }

    @Test fun cancellationDuringImportDoesNotDeleteExistingMedia() = runBlocking {
        val f = file(); val a = card(audio = f.path)
        val job = launch {
            val importingJob = currentCoroutineContext().job
            val input = object : ByteArrayInputStream("new audio".toByteArray()) {
                override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
                    importingJob.cancel()
                    return super.read(buffer, offset, length)
                }
            }
            repo.importAudio(a, input, "m4a")
        }
        job.join()
        assertTrue(job.isCancelled); assertTrue(f.exists()); assertEquals(f.path, dao.audioPath(a))
        assertEquals(setOf(f.name), f.parentFile!!.list()!!.toSet())
    }

    @Test fun concurrentRepositoriesNeverDeleteAnAttachmentWithRemainingReferences() = runBlocking {
        val f = file()
        val anchor = card(audio = f.path)
        val ids = (1..20).map { card(audio = f.path) }
        val other = RoomWordRepository(db, context, SettingsRepository(context))
        coroutineScope {
            ids.mapIndexed { i, id ->
                async(Dispatchers.IO) {
                    val repository = if (i % 2 == 0) repo else other
                    repository.clearAudio(id)
                    repository.setAudio(id, f)
                    repository.deleteCustomCard(id)
                }
            }.awaitAll()
        }
        assertTrue(f.exists()); assertEquals(f.path, dao.audioPath(anchor))
        repo.clearAudio(anchor)
        assertFalse(f.exists())
    }

    @Test fun exceptionAfterCommitDoesNotDeleteNewReferencedImport() = runBlocking {
        val old = file(); val a = card(audio = old.path)
        val input = object : ByteArrayInputStream("new audio".toByteArray()) {
            override fun close() {
                throw java.io.IOException("caller stream close after commit")
            }
        }
        expectFailure { repo.importAudio(a, input, "m4a") }
        val saved = File(requireNotNull(dao.audioPath(a)))
        assertNotEquals(old.path, saved.path)
        assertEquals("new audio", saved.readText())
        assertFalse(old.exists())
    }
}
