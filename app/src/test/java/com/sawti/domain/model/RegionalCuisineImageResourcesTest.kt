package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class RegionalCuisineImageResourcesTest {
    @Test fun everyRegionalPhotoResolvesToAPackagedWebP() {
        val context = RuntimeEnvironment.getApplication()
        val cards = WordSeed.itemEntities().associateBy { it.semanticKey }
        val ids = RegionalDishesCatalog.additionalDialects().map { (_, dish) ->
            val item = requireNotNull(cards[dish.semanticKey]).toDomain()
            val name = requireNotNull(WordImageCatalog.imageFor(item))
            @Suppress("DiscouragedApi")
            val id = context.resources.getIdentifier(name, "drawable", context.packageName)
            assertNotEquals(dish.semanticKey, 0, id)
            context.resources.openRawResource(id).use { stream ->
                val header = ByteArray(12)
                var offset = 0
                while (offset < header.size) {
                    val count = stream.read(header, offset, header.size - offset)
                    assertTrue(name, count > 0)
                    offset += count
                }
                assertEquals(name, "RIFF", String(header, 0, 4, Charsets.US_ASCII))
                assertEquals(name, "WEBP", String(header, 8, 4, Charsets.US_ASCII))
            }
            id
        }
        assertEquals(120, ids.size)
        assertEquals(120, ids.toSet().size)
    }
}
