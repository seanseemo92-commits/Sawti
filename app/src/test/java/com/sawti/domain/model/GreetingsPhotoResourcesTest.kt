package com.sawti.domain.model

import android.graphics.BitmapFactory
import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import com.sawti.presentation.common.categoryDrawableName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Regression guard against the previous low-resolution 4x4 photo atlas. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class GreetingsPhotoResourcesTest {
    @Test fun all14ConceptsAndTheHomeCoverAreDistinctHighResolutionWebPPhotos() {
        val context = RuntimeEnvironment.getApplication()
        val items = WordSeed.itemEntities().filter { it.categoryId == GreetingsCatalog.CATEGORY_ID }
        assertEquals(14, items.size)
        val images = items.map { requireNotNull(WordImageCatalog.imageFor(it.toDomain())) }
        assertEquals(14, images.toSet().size)
        assertEquals(GreetingsCatalog.greetings.map { it.key }, items.map { it.semanticKey })
        val cover = requireNotNull(categoryDrawableName(GreetingsCatalog.CATEGORY_ID))
        assertEquals("img_cat_greetings", cover)

        for (name in images + cover) {
            @Suppress("DiscouragedApi")
            val id = context.resources.getIdentifier(name, "drawable", context.packageName)
            assertNotEquals("Missing bundled greeting photo: $name", 0, id)
            context.resources.openRawResource(id).use { stream ->
                val bytes = stream.readBytes()
                assertNotEquals("Unexpectedly small photo: $name", true, bytes.size < 18_000)
                assertEquals(name, "RIFF", String(bytes, 0, 4, Charsets.US_ASCII))
                assertEquals(name, "WEBP", String(bytes, 8, 4, Charsets.US_ASCII))
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                assertEquals(name, 816, bounds.outWidth)
                assertEquals(name, 960, bounds.outHeight)
            }
        }
    }

    @Test fun allDialectsKeepTheSameImageIdentity() {
        val items = WordSeed.itemEntities().filter { it.categoryId == GreetingsCatalog.CATEGORY_ID }
        for (source in items) for (dialect in ArabicDialect.entries) {
            val before = source.toDomain()
            val localized = DialectCatalog.localize(before, dialect)
            assertEquals(before.semanticKey, localized.semanticKey)
            assertEquals(WordImageCatalog.imageFor(before), WordImageCatalog.imageFor(localized))
        }
    }
}
