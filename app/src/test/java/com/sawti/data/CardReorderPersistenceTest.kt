package com.sawti.data

import android.app.Application
import android.content.Context
import com.sawti.data.db.AppDatabase
import com.sawti.domain.model.CategoryIds
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
class CardReorderPersistenceTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repo: RoomWordRepository

    @Before fun prepare() = runBlocking {
        context = RuntimeEnvironment.getApplication()
        context.deleteDatabase(AppDatabase.NAME)
        db = AppDatabase.build(context)
        repo = RoomWordRepository(db, context, SettingsRepository(context))
        repo.seedOrUpdateDefaults()
    }

    @After fun cleanup() {
        db.close()
        context.deleteDatabase(AppDatabase.NAME)
    }

    @Test fun caregiverCategoryOrderPersistsAndNeverCreatesNegativeSortOrder() = runBlocking {
        val original = repo.observeCategories().first().map { it.id }
        assertEquals(listOf("greetings", CategoryIds.FAMILY), original.take(2))
        val order = original.toMutableList().apply {
            java.util.Collections.swap(this, 0, 1)
        }
        repo.reorderCategories(order)
        val dao = db.wordDao()
        assertEquals(order, repo.observeCategories().first().map { it.id })
        val entities = dao.observeCategories().first()
        assertEquals((0 until order.size).toList(), entities.map { it.sortOrder })
        assertTrue(entities.all { it.sortOrder >= 0 })
        db.close()
        db = AppDatabase.build(context)
        repo = RoomWordRepository(db, context, SettingsRepository(context))
        assertEquals(order, repo.observeCategories().first().map { it.id })
        val stage = File(context.cacheDir, "reorder-validator.db")
        context.getDatabasePath(AppDatabase.NAME).copyTo(stage, overwrite = true)
        RestoreDatabaseValidator.validate(context, stage)
        stage.delete()
        assertEquals(1, AppDatabase.VERSION)
    }

    @Test fun caregiverWordOrderPersistsForReorderedSubgroup() = runBlocking {
        val originals = repo.observeCategories().first()
            .first { it.id == CategoryIds.DAILY }.items.take(3).map { it.id }
        val reordered = originals.reversed()
        repo.reorderWords(reordered)
        val orderedIds = db.wordDao().observeItems().first()
            .filter { it.categoryId == CategoryIds.DAILY }.take(3).map { it.id }
        assertEquals(reordered, orderedIds)
        assertEquals(reordered.indices.toList(), reordered.map { db.wordDao().wordById(it)!!.sortOrder })
        assertTrue(db.wordDao().observeItems().first().all { it.sortOrder >= 0 })
        db.close()
        db = AppDatabase.build(context)
        assertEquals(reordered, db.wordDao().observeItems().first()
            .filter { it.categoryId == CategoryIds.DAILY }.take(3).map { it.id })
    }
}
