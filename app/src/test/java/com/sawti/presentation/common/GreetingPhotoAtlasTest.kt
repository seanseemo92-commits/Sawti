package com.sawti.presentation.common

import android.app.Application
import com.sawti.R
import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import com.sawti.domain.model.ArabicDialect
import com.sawti.domain.model.DialectCatalog
import com.sawti.domain.model.GreetingsCatalog
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class GreetingPhotoAtlasTest {
    @Test fun allFourteenGreetingConceptsHaveUniquePhotos() {
        val entries = GreetingsCatalog.greetings
        assertEquals(14, entries.size)
        assertEquals(entries.map { it.key }.toSet(), GreetingPhotoAtlas.keys())
        val slots = entries.map { requireNotNull(GreetingPhotoAtlas.slotForKey(it.key)) }
        assertEquals(14, slots.distinct().size)
        assertTrue(slots.all { it in 0 until GreetingPhotoAtlas.HOME_SLOT })
    }

    @Test fun photoBindingStaysStableAcrossAllNineDialects() {
        val original = WordSeed.itemEntities().filter { it.categoryId == "greetings" }.map { it.toDomain() }
        for (item in original) {
            val expected = requireNotNull(GreetingPhotoAtlas.slotFor(item))
            for (dialect in ArabicDialect.entries) {
                assertEquals(expected, GreetingPhotoAtlas.slotFor(DialectCatalog.localize(item, dialect)))
            }
        }
        assertNull(GreetingPhotoAtlas.slotForKey("request.water"))
    }

    @Test fun bundledOfflineAtlasExistsAndIsFourByFour() {
        val context = RuntimeEnvironment.getApplication()
        val options = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.resources.openRawResource(R.drawable.greetings_atlas).use {
            android.graphics.BitmapFactory.decodeStream(it, null, options)
        }
        assertEquals(512, options.outWidth)
        assertEquals(512, options.outHeight)
        assertEquals(GreetingPhotoAtlas.TOTAL_TILES, 16)
    }
}
