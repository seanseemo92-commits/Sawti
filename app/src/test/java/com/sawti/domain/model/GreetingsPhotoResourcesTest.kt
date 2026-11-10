package com.sawti.domain.model

import android.graphics.BitmapFactory
import android.graphics.Color
import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import com.sawti.presentation.common.categoryDrawableName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Regression guard that greetings use the same bitmap dimensions as Sawti's existing sections. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class GreetingsPhotoResourcesTest {
    @Test fun all14ConceptsAndTheHomeCoverMatchTheExistingPhotoDimensions() {
        val context = RuntimeEnvironment.getApplication()
        val items = WordSeed.itemEntities().filter { it.categoryId == GreetingsCatalog.CATEGORY_ID }
        assertEquals(14, items.size)
        val images = items.map { requireNotNull(WordImageCatalog.imageFor(it.toDomain())) }
        assertEquals(14, images.toSet().size)
        assertEquals(GreetingsCatalog.greetings.map { it.key }, items.map { it.semanticKey })
        val cover = requireNotNull(categoryDrawableName(GreetingsCatalog.CATEGORY_ID))
        assertEquals("img_cat_greetings", cover)

        fun bounds(name: String): Pair<Int, Int> {
            @Suppress("DiscouragedApi")
            val id = context.resources.getIdentifier(name, "drawable", context.packageName)
            assertNotEquals("Missing bundled photo: $name", 0, id)
            context.resources.openRawResource(id).use { stream ->
                val bytes = stream.readBytes()
                assertEquals(name, "RIFF", String(bytes, 0, 4, Charsets.US_ASCII))
                assertEquals(name, "WEBP", String(bytes, 8, 4, Charsets.US_ASCII))
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                return options.outWidth to options.outHeight
            }
        }

        // Source dimensions, not card UI aspect ratio, define the specification.
        val wordDimensions = bounds("word_transport_car")
        val coverDimensions = bounds("img_cat_family")
        assertEquals(512 to 512, wordDimensions)
        for (name in images) assertEquals("Inconsistent word image $name", wordDimensions, bounds(name))
        assertEquals("Inconsistent greetings home cover", coverDimensions, bounds(cover))
    }


    @Test fun bundledGreetingPhotosDoNotContainWideBlackFrameEdges() {
        val context = RuntimeEnvironment.getApplication()
        val names = GreetingsCatalog.greetings.map { key ->
            val item = WordSeed.itemEntities().single { it.semanticKey == key.key }.toDomain()
            requireNotNull(WordImageCatalog.imageFor(item))
        } + requireNotNull(categoryDrawableName("greetings"))
        for (name in names) {
            @Suppress("DiscouragedApi")
            val id = context.resources.getIdentifier(name, "drawable", context.packageName)
            val bitmap = context.resources.openRawResource(id).use { requireNotNull(BitmapFactory.decodeStream(it)) }
            try {
                fun black(x: Int, y: Int): Boolean {
                    val pixel = bitmap.getPixel(x, y)
                    return Color.red(pixel) < 24 && Color.green(pixel) < 24 && Color.blue(pixel) < 24
                }
                val edges = listOf(
                    (0 until bitmap.width).count { black(it, 0) }.toFloat() / bitmap.width,
                    (0 until bitmap.width).count { black(it, bitmap.height - 1) }.toFloat() / bitmap.width,
                    (0 until bitmap.height).count { black(0, it) }.toFloat() / bitmap.height,
                    (0 until bitmap.height).count { black(bitmap.width - 1, it) }.toFloat() / bitmap.height,
                )
                assertTrue("Black frame edge in $name: $edges", edges.all { it < 0.7f })
            } finally { bitmap.recycle() }
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

