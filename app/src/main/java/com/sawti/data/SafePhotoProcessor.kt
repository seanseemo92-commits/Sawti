package com.sawti.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.InputStream

/**
 * Bounded, private image import. Keeps the ORIGINAL file and database row untouched until
 * a fully encoded 1024px-or-smaller JPEG is ready; all intermediate Bitmaps are recycled.
 * JPEG uses an opaque white background for transparent source pixels.
 */
internal object SafePhotoProcessor {
    fun import(input: InputStream, target: File) {
        val stage = File(target.parentFile, ".photo_${System.nanoTime()}.incoming")
        var sampled: Bitmap? = null
        var oriented: Bitmap? = null
        var scaled: Bitmap? = null
        var flattened: Bitmap? = null
        var completed = false
        try {
            SafeMediaImport.copy(input, stage, SafeMediaImport.MAX_PHOTO_BYTES)
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(stage.absolutePath, bounds)
            ImageResizePolicy.validateSource(bounds.outWidth, bounds.outHeight)
            val options = BitmapFactory.Options().apply {
                inSampleSize = ImageResizePolicy.sampleSize(bounds.outWidth, bounds.outHeight)
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val decoded = requireNotNull(BitmapFactory.decodeFile(stage.absolutePath, options)) {
                "تعذّر فتح الصورة"
            }
            sampled = decoded

            val orientation = ExifInterface(stage.absolutePath)
                .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.setScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> { matrix.setRotate(90f); matrix.postScale(-1f, 1f) }
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
                ExifInterface.ORIENTATION_TRANSVERSE -> { matrix.setRotate(-90f); matrix.postScale(-1f, 1f) }
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
            }
            val upright = if (matrix.isIdentity) decoded else
                Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
            oriented = upright
            val (width, height) = ImageResizePolicy.outputSize(upright.width, upright.height)
            val resized = if (upright.width == width && upright.height == height) upright else
                Bitmap.createScaledBitmap(upright, width, height, true)
            scaled = resized
            val opaque = if (resized.hasAlpha()) {
                Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also {
                    val canvas = Canvas(it)
                    canvas.drawColor(Color.WHITE)
                    canvas.drawBitmap(resized, 0f, 0f, null)
                }
            } else resized
            flattened = opaque
            target.outputStream().buffered().use { output ->
                require(opaque.compress(Bitmap.CompressFormat.JPEG, 80, output)) {
                    "تعذّر ضغط الصورة"
                }
            }
            require(target.isFile && target.length() > 0L) { "تعذّر حفظ الصورة" }
            completed = true
        } finally {
            listOfNotNull(flattened, scaled, oriented, sampled).distinct().forEach {
                if (!it.isRecycled) it.recycle()
            }
            stage.delete()
            if (!completed) target.delete()
        }
    }
}
