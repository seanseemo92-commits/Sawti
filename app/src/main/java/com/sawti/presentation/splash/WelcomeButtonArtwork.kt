package com.sawti.presentation.splash

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Reuses the light reference's actual lettering, arrow and gradient, rather
 * than approximating them with a different font or a second button design.
 * Built once per composition, never inside the animated drawing loop.
 */
internal object WelcomeButtonArtwork {
    const val LEFT = 79
    const val TOP = 1216
    const val WIDTH = 783
    const val HEIGHT = 166

    fun referenceOverlay(light: ImageBitmap): ImageBitmap {
        val source = light.asAndroidBitmap()
        val pixels = IntArray(WIDTH * HEIGHT)
        source.getPixels(pixels, 0, WIDTH, LEFT, TOP, WIDTH, HEIGHT)
        val button = WelcomeArtLayout.referenceButton
        val radius = (button.bottom - button.top) / 2f
        val centerY = (button.top + button.bottom) / 2f
        for (y in 0 until HEIGHT) {
            // Estimate the clean light backdrop from either side of the CTA.
            // Outside the pill, retain only its blue shadow as transparency;
            // the white light-theme panel must never be pasted onto dark art.
            val a = source.getPixel(LEFT, TOP + y)
            val b = source.getPixel(LEFT + WIDTH - 1, TOP + y)
            for (x in 0 until WIDTH) {
                val px = LEFT + x + 0.5f
                val py = TOP + y + 0.5f
                val nearestX = px.coerceIn(button.left + radius, button.right - radius)
                val dx = px - nearestX
                val dy = py - centerY
                val distance = sqrt(dx * dx + dy * dy)
                if (distance < radius - 4f) continue
                val color = pixels[y * WIDTH + x]
                val t = x.toFloat() / (WIDTH - 1)
                val bgR = AndroidColor.red(a) * (1f - t) + AndroidColor.red(b) * t
                val bgG = AndroidColor.green(a) * (1f - t) + AndroidColor.green(b) * t
                if (distance <= radius + 3f) {
                    // Unmatte the anti-aliased pill edge too. Copying opaque
                    // light edge pixels creates a white fringe on dark mode.
                    val inset = (radius - 6f) / distance.coerceAtLeast(1f)
                    val foreground = source.getPixel(
                        (nearestX + dx * inset).toInt(), (centerY + dy * inset).toInt()
                    )
                    val dr = bgR - AndroidColor.red(foreground)
                    val dg = bgG - AndroidColor.green(foreground)
                    val alpha = (((bgR - AndroidColor.red(color)) * dr +
                        (bgG - AndroidColor.green(color)) * dg) /
                        (dr * dr + dg * dg).coerceAtLeast(1f)).coerceIn(0f, 1f)
                    pixels[y * WIDTH + x] = AndroidColor.argb(
                        (alpha * 255f).roundToInt(), AndroidColor.red(foreground),
                        AndroidColor.green(foreground), AndroidColor.blue(foreground)
                    )
                    continue
                }
                // Recover opacity against the blue-violet shadow color.
                val alpha = (((bgR - AndroidColor.red(color)) / (bgR - 65f).coerceAtLeast(1f) +
                    (bgG - AndroidColor.green(color)) / (bgG - 80f).coerceAtLeast(1f)) / 2f)
                    .coerceIn(0f, 0.35f)
                val feather = minOf(x / 12f, (WIDTH - 1 - x) / 12f,
                    y / 12f, (HEIGHT - 1 - y) / 12f, 1f).coerceAtLeast(0f)
                pixels[y * WIDTH + x] = AndroidColor.argb(
                    (alpha * feather * 255f).roundToInt(), 65, 80, 245
                )
            }
        }
        return Bitmap.createBitmap(pixels, WIDTH, HEIGHT, Bitmap.Config.ARGB_8888).asImageBitmap()
    }

    // The original dark pill is almost covered by the larger reference pill,
    // but its lower edge would otherwise remain as a second purple outline.
    // Sample clean surrounding dark art for a small bilinear backing patch.
    fun darkBacking(dark: ImageBitmap): ImageBitmap {
        val source = dark.asAndroidBitmap()
        return Bitmap.createBitmap(
            intArrayOf(
                source.getPixel(110, 1238), source.getPixel(830, 1238),
                source.getPixel(110, 1360), source.getPixel(830, 1360),
            ), 2, 2, Bitmap.Config.ARGB_8888
        ).asImageBitmap()
    }
}
