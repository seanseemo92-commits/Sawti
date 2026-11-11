package com.sawti.domain.model

import android.graphics.BitmapFactory
import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import com.sawti.presentation.common.categoryDrawableName
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TransportImageResourcesTest {
    @Test fun allTransportCardsAndSectionCoverResolveToDistinct512PixelWebPPhotos() {
        val context = RuntimeEnvironment.getApplication()
        val names = WordSeed.itemEntities().filter { it.categoryId == CategoryIds.TRANSPORT }.map {
            requireNotNull(WordImageCatalog.imageFor(it.toDomain()))
        }
        assertEquals(12, names.size)
        assertEquals(12, names.toSet().size)
        assertEquals("word_transport_car", categoryDrawableName(CategoryIds.TRANSPORT))
        for (name in names) {
            @Suppress("DiscouragedApi")
            val id = context.resources.getIdentifier(name, "drawable", context.packageName)
            assertNotEquals(name, 0, id)
            context.resources.openRawResource(id).use { stream ->
                val bytes = stream.readBytes()
                assertEquals(name, "RIFF", String(bytes, 0, 4, Charsets.US_ASCII))
                assertEquals(name, "WEBP", String(bytes, 8, 4, Charsets.US_ASCII))
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                assertEquals(name, 512, options.outWidth)
                assertEquals(name, 512, options.outHeight)
            }
        }
    }
}
