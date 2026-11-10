package com.sawti.presentation.common

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.sawti.R
import com.sawti.domain.model.WordItem

/**
 * 4×4 offline photo atlas, each frame an independent no-text greeting image.
 * No network, drawable lookup by localized wording, or new database columns.
 * Use semantic keys so all nine Arabic dialects retain the same photo.
 */
object GreetingPhotoAtlas {
    const val CATEGORY_ID = "greetings"
    const val HOME_SLOT = 14
    const val COLUMNS = 4
    const val TOTAL_TILES = 16

    private val slots: Map<String, Int> = mapOf(
        "greeting.salam" to 0,
        "greeting.reply" to 1,
        "greeting.hello" to 2,
        "greeting.welcome" to 3,
        "greeting.how_are_you" to 4,
        "greeting.fine" to 5,
        "greeting.good_morning" to 6,
        "greeting.good_evening" to 7,
        "greeting.brighten" to 8,
        "greeting.good_health" to 9,
        "greeting.goodbye" to 10,
        "greeting.safe" to 11,
        "greeting.thanks" to 12,
        "greeting.bless" to 13,
    )

    fun slotFor(item: WordItem): Int? =
        if (item.categoryId == CATEGORY_ID) slots[item.semanticKey] else null

    fun slotForKey(key: String?): Int? = slots[key]

    fun keys(): Set<String> = slots.keys
}

@Composable
fun GreetingPhoto(
    slot: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val atlas = ImageBitmap.imageResource(R.drawable.greetings_atlas)
    val cellWidth = atlas.width / GreetingPhotoAtlas.COLUMNS
    val cellHeight = atlas.height / GreetingPhotoAtlas.COLUMNS
    val frame = slot.coerceIn(0, GreetingPhotoAtlas.TOTAL_TILES - 1)
    val painter = remember(atlas, frame) {
        BitmapPainter(
            image = atlas,
            srcOffset = IntOffset((frame % GreetingPhotoAtlas.COLUMNS) * cellWidth,
                                  (frame / GreetingPhotoAtlas.COLUMNS) * cellHeight),
            srcSize = IntSize(cellWidth, cellHeight),
            filterQuality = FilterQuality.Medium,
        )
    }
    Image(
        painter = painter,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier,
    )
}
