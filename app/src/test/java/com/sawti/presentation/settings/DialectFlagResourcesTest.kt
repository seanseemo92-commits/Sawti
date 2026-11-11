package com.sawti.presentation.settings

import android.app.Application
import android.graphics.BitmapFactory
import com.sawti.domain.model.ArabicDialect
import java.util.Locale
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class DialectFlagResourcesTest {
    @Test fun allRegionalFlagsArePackagedAndFushaHasNoNationalFlag() {
        val context = RuntimeEnvironment.getApplication()
        assertNull(dialectFlagResource(ArabicDialect.STANDARD))
        val ids = ArabicDialect.entries.filter { it != ArabicDialect.STANDARD }.map { dialect ->
            val id = requireNotNull(dialectFlagResource(dialect))
            assertEquals("dialect_flag_${dialect.countryCode!!.lowercase(Locale.ROOT)}",
                context.resources.getResourceEntryName(id))
            context.resources.openRawResource(id).use {
                val bytes = it.readBytes()
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                assertEquals(144, bounds.outWidth)
                assertEquals(108, bounds.outHeight)
            }
            id
        }
        assertEquals(8, ids.distinct().size)
    }
}
