package com.sawti.data

import android.app.Application
import android.content.Context
import android.net.Uri
import com.sawti.data.db.AppDatabase
import com.sawti.data.db.WordSeed
import com.sawti.domain.model.ArabicDialect
import com.sawti.domain.model.CardDetails
import com.sawti.domain.model.DialectCatalog
import com.sawti.domain.model.WordImageCatalog
import com.sawti.data.db.toDomain
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
class SemanticSeedPersistenceTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var settings: SettingsRepository
    private lateinit var repo: RoomWordRepository

    @Before fun setup() = runBlocking {
        context = RuntimeEnvironment.getApplication()
        db = AppDatabase.build(context)
        settings = SettingsRepository(context)
        repo = RoomWordRepository(db, context, settings)
        repo.seedOrUpdateDefaults()
    }
    @After fun close() { db.close() }

    @Test fun reseedingRenamedCardDoesNotDuplicateResetOrRetargetIt() = runBlocking {
        val dao = db.wordDao()
        val water = dao.observeItems().first().single { it.semanticKey == "request.water" }
        repo.updateDetails(water.id, "طلبي الخاص", "صوتي الخاص", "تلميحي", "جملتي", "123")
        val expected = requireNotNull(dao.wordById(water.id))
        context.getSharedPreferences("seed", Context.MODE_PRIVATE).edit().clear().commit()
        repeat(2) { repo.seedOrUpdateDefaults() }
        val rows = dao.observeItems().first()
        assertEquals(WordSeed.itemEntities().size, rows.size)
        assertEquals(1, rows.count { it.semanticKey == "request.water" })
        assertEquals(expected, dao.wordById(water.id))
        assertEquals("word_water", WordImageCatalog.imageFor(expected.toDomain()))
        settings.setDialect(ArabicDialect.LIBYAN)
        val view = repo.observeCategories().first().flatMap { it.items }.single { it.id == water.id }
        assertEquals(expected.displayText, view.displayText)
        assertEquals(expected.ttsPrompt, view.ttsPrompt)
    }

    @Test fun backupRestoreAndStartupKeepEditedConceptMediaAndIdentity() = runBlocking {
        val source = db.wordDao().observeItems().first().single { it.semanticKey == "food.bread" }
        val photo = File(context.filesDir, "photos/owned.jpg").apply {
            parentFile!!.mkdirs(); writeText("photo payload")
        }
        val audio = File(context.filesDir, "custom_audio/owned.m4a").apply {
            parentFile!!.mkdirs(); writeText("audio payload")
        }
        db.wordDao().updatePhoto(source.id, photo.path)
        db.wordDao().updateAudio(source.id, audio.path)
        repo.updateDetails(source.id, "خبزي الخاص", "نطقي", "تلميحي", null, "123")
        val expected = requireNotNull(db.wordDao().wordById(source.id))
        val archive = File(context.cacheDir, "semantic-foundation.zip")
        BackupManager(context, db).also { it.export(Uri.fromFile(archive)); it.restore(Uri.fromFile(archive)) }
        BackupManager.recoverIfInterrupted(context)
        db = AppDatabase.build(context)
        repo = RoomWordRepository(db, context, SettingsRepository(context))
        repeat(2) { repo.seedOrUpdateDefaults() }
        val restored = requireNotNull(db.wordDao().wordById(source.id))
        assertEquals(expected.copy(photoPath = restored.photoPath, audioPath = restored.audioPath), restored)
        assertEquals("photo payload", File(requireNotNull(restored.photoPath)).readText())
        assertEquals("audio payload", File(requireNotNull(restored.audioPath)).readText())
        assertEquals(365, db.wordDao().observeItems().first().size)
        assertEquals(1, db.wordDao().semanticKeys().count { it == "food.bread" })
        assertEquals("word_bread", WordImageCatalog.imageFor(restored.toDomain()))
        assertEquals(CardDetails.LABEL or CardDetails.SPEECH or CardDetails.CUE or CardDetails.SENTENCE,
            restored.customTextFields)
    }

    @Test fun allBundledImageReferencesResolveAndCustomTextNeverClaimsAConcept() = runBlocking {
        for (word in WordSeed.itemEntities()) {
            val image = WordImageCatalog.imageFor(word.toDomain()) ?: continue
            assertNotEquals("Missing $image", 0, context.resources.getIdentifier(image, "drawable", context.packageName))
        }
        val category = repo.addCustomCategory("قسمي", 0, 999)
        val id = repo.addCustomCard(category, "ماء", "نطقي")
        repo.seedOrUpdateDefaults()
        val saved = requireNotNull(db.wordDao().wordById(id)).toDomain()
        assertNull(saved.semanticKey)
        assertNull(WordImageCatalog.imageFor(saved))
        for (dialect in ArabicDialect.entries) assertEquals(saved, DialectCatalog.localize(saved, dialect))
    }
}
