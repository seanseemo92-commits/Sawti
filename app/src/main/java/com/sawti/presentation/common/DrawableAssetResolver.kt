package com.sawti.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.sawti.domain.model.WordItem

/**
 * Android drawable lookup by stable file name.
 * We intentionally avoid R.drawable.img_* references until optional WebP assets are
 * installed; the application still compiles and displays its existing fallbacks.
 */
@Composable
fun drawableResourceId(resourceName: String?): Int? {
    val context = LocalContext.current
    return remember(context, resourceName) {
        resourceName?.let { name ->
            @Suppress("DiscouragedApi")
            val id = context.resources.getIdentifier(name, "drawable", context.packageName)
            id.takeIf { it != 0 }
        }
    }
}

fun categoryDrawableName(categoryId: String): String? = when (categoryId) {
    "libyan" -> "img_cat_cuisine"
    "family" -> "img_cat_family"
    "daily" -> "img_cat_daily"
    "routine" -> "img_cat_routine"
    "vegetables" -> "img_cat_vegetables"
    "fruits" -> "img_cat_fruits"
    else -> null
}

/** Only built-in words with matching image assets are mapped; custom cards keep their photos. */
fun wordDrawableName(item: WordItem): String? {
    val label = item.displayText.trim().replace(Regex("[\\u064B-\\u065F\\u0670]"), "")
    return when (label) {
        "موز" -> "word_banana"
        "عنب" -> "word_grapes"
        "شاهي", "نبي شاهي", "شاهي بالنعناع" -> "word_tea"
        "ماء", "مية", "نبي مية" -> "word_water"
        else -> null
    }
}
