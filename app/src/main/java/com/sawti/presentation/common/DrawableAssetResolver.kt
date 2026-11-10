package com.sawti.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.sawti.domain.model.WordItem
import com.sawti.domain.model.WordImageCatalog

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
    "greetings" -> "img_cat_greetings"
    "transport" -> "word_transport_car"
    "cuisine" -> "img_cat_cuisine"
    "family" -> "img_cat_family"
    "daily" -> "img_cat_daily"
    "routine" -> "img_cat_routine"
    "vegetables" -> "img_cat_vegetables"
    "fruits" -> "img_cat_fruits"
    "food" -> "img_cat_food_drink"
    "clothes" -> "img_cat_clothes"
    "body" -> "img_cat_body"
    "animals" -> "img_cat_animals"
    "actions" -> "img_cat_actions"
    "home" -> "word_living_room" // Cover the home section card with its existing living-room photo.
    else -> null
}

/**
 * Image lookup uses Room's stable semanticKey, preserved by all dialect projections.
 * The legacy label fallback is only for older transient items without a key.
 */
fun wordDrawableName(item: WordItem): String? = WordImageCatalog.imageFor(item)
