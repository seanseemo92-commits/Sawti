package com.sawti.data

import android.app.Application
import android.content.Context
import com.sawti.data.db.*
import com.sawti.domain.model.*
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
class DialectEditingTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repo: RoomWordRepository
    private lateinit var settings: SettingsRepository
    private val seed = WordSeed.itemEntities().first { it.semanticKey == "food.bread" }
    private var id = 0L

    @Before fun setup() = runBlocking {
        context = RuntimeEnvironment.getApplication()
        db = AppDatabase.build(context)
        settings = SettingsRepository(context)
        repo = RoomWordRepository(db, context, settings)
        db.wordDao().insertCategory(CategoryEntity(seed.categoryId, "قسم", 0, false, 0))
        id = db.wordDao().insertItem(seed.copy(sentencePrompt = "جملة شخصية",
            photoPath = "/personal/photo.jpg", audioPath = "/personal/voice.m4a"))
    }

    @After fun close() { db.close() }

    private suspend fun saved() = requireNotNull(db.wordDao().wordById(id))
    private suspend fun save(before: CardDetails, edited: CardDetails) {
        repo.updateDetails(id, edited.display, edited.speech, edited.cue, edited.sentence, edited.phone,
            baseline = before, group = edited.group)
    }

    @Test fun egyptianEditorNoOpPreservesOriginalRowExactly() = runBlocking {
        val original = saved()
        settings.setDialect(ArabicDialect.EGYPTIAN)
        val visible = repo.observeCategories().first().single().items.single()
        assertEquals("عيش", visible.displayText)
        val baseline = CardDetails.from(visible.forEditing())
        save(baseline, baseline)
        assertEquals(original, saved())
    }

    @Test fun phoneOnlyEditKeepsSentencePronunciationAndAutomaticDialect() = runBlocking {
        val original = saved()
        val baseline = CardDetails.from(DialectCatalog.localize(original.toDomain(), ArabicDialect.EGYPTIAN))
        save(baseline, baseline.copy(phone = "0912345678"))
        assertEquals(original.copy(phone = "0912345678"), saved())
        assertEquals("عيش", DialectCatalog.localize(saved().toDomain(), ArabicDialect.EGYPTIAN).displayText)
    }

    @Test fun speechOverrideSurvivesReloadAndAllDialectChanges() = runBlocking {
        val baseline = CardDetails.from(saved().toDomain())
        save(baseline, baseline.copy(speech = "نطق شخصي"))
        db.close()
        db = AppDatabase.build(context)
        val row = saved()
        assertEquals(CardDetails.SPEECH, row.customTextFields)
        ArabicDialect.entries.forEach {
            assertEquals("نطق شخصي", DialectCatalog.localize(row.toDomain(), it).ttsPrompt)
        }
        assertEquals("/personal/photo.jpg", row.photoPath)
        assertEquals("/personal/voice.m4a", row.audioPath)
    }

    @Test fun sentenceAndCueCustomizationRemainVisibleOutsideLibyan() = runBlocking {
        val baseline = CardDetails.from(saved().toDomain())
        save(baseline, baseline.copy(sentence = "جملتي الجديدة", cue = "تلميحي"))
        val displayed = DialectCatalog.localize(saved().toDomain(), ArabicDialect.SYRIAN)
        assertEquals("جملتي الجديدة", displayed.sentencePrompt)
        assertEquals("تلميحي", displayed.phonemicCue)
    }

    @Test fun deliberateSentenceRemovalIsNotRefilledBySeedQueries() = runBlocking {
        val baseline = CardDetails.from(saved().toDomain())
        save(baseline, baseline.copy(sentence = null))
        repo.seedOrUpdateDefaults()
        assertNull(saved().sentencePrompt)
        assertEquals(CardDetails.SENTENCE, saved().customTextFields)
    }

    @Test fun staleEditorPreservesNewMediaAndUneditedText() = runBlocking {
        val before = CardDetails.from(saved().toDomain())
        save(before, before.copy(speech = "أحدث نطق"))
        db.wordDao().updatePhoto(id, "/personal/new.jpg")
        save(before, before.copy(phone = "123"))
        val row = saved()
        assertEquals("أحدث نطق", row.ttsPrompt); assertEquals("123", row.phone)
        assertEquals("/personal/new.jpg", row.photoPath)
    }

    @Test fun conflictingSaveFailsAtomically() = runBlocking {
        val before = CardDetails.from(saved().toDomain())
        save(before, before.copy(speech = "أحدث نطق"))
        val latest = saved()
        var failed = false
        try { save(before, before.copy(speech = "نطق قديم", phone = "999")) } catch (_: IllegalArgumentException) { failed = true }
        assertTrue(failed); assertEquals(latest, saved())
    }

    @Test fun seedUpdatesRespectEditedLabelSpeechCueAndSentence() = runBlocking {
        val before = CardDetails.from(saved().toDomain())
        save(before, before.copy(display = "مية", speech = "نطقي", cue = "تلميحي", sentence = "جملتي"))
        val expected = saved()
        repeat(2) { repo.seedOrUpdateDefaults() }
        assertEquals(expected, saved())
        assertEquals("مية", DialectCatalog.localize(saved().toDomain(), ArabicDialect.EGYPTIAN).displayText)
    }

    @Test fun customTextMetadataSurvivesBackupAndRestore() = runBlocking {
        db.wordDao().updatePhoto(id, null)
        db.wordDao().updateAudio(id, null)
        val before = CardDetails.from(saved().toDomain())
        save(before, before.copy(speech = "نطق محفوظ", sentence = "جملة محفوظة"))
        val expected = saved()
        val archive = java.io.File(context.cacheDir, "custom-text.zip")
        val manager = BackupManager(context, db)
        manager.export(android.net.Uri.fromFile(archive))
        manager.restore(android.net.Uri.fromFile(archive))
        BackupManager.recoverIfInterrupted(context)
        db = AppDatabase.build(context)
        assertEquals(expected, saved())
        val shown = DialectCatalog.localize(saved().toDomain(), ArabicDialect.EGYPTIAN)
        assertEquals("نطق محفوظ", shown.ttsPrompt)
        assertEquals("جملة محفوظة", shown.sentencePrompt)
    }
}
