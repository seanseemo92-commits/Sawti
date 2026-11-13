package com.sawti.data

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.exifinterface.media.ExifInterface
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SafePhotoProcessorOrientationTest {
    @Test fun cameraExifRotationIsAppliedBeforeResize() {
        val context = RuntimeEnvironment.getApplication()
        val original = File(context.cacheDir, "camera-source.jpg")
        val photos = File(context.filesDir, "photos").apply { mkdirs() }
        val output = File(photos, "rotated-import.jpg")
        val bitmap = Bitmap.createBitmap(60, 120, Bitmap.Config.ARGB_8888)
        try {
            original.outputStream().use { assertEquals(true, bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)) }
            ExifInterface(original.path).apply {
                setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_ROTATE_90.toString())
                saveAttributes()
            }
            original.inputStream().use { SafePhotoProcessor.import(it, output) }
            val processed = BitmapFactory.decodeFile(output.path)
            assertNotNull(processed)
            try {
                assertEquals(120, processed.width)
                assertEquals(60, processed.height)
            } finally { processed.recycle() }
        } finally {
            bitmap.recycle()
            original.delete()
            output.delete()
        }
    }
}
