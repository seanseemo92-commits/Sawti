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
    "food" -> "img_cat_food_drink"
    "clothes" -> "img_cat_clothes"
    "body" -> "img_cat_body"
    else -> null
}

/** Only built-in words with matching image assets are mapped; custom cards keep their photos. */
fun wordDrawableName(item: WordItem): String? {
    val label = item.displayText.trim().replace(Regex("[\\u064B-\\u065F\\u0670]"), "")
    return when (label) {
        // Dedicated photographic assets for the first 10 Libyan dishes.
        "بازين" -> "word_bazin"
        "رشدة كسكاس" -> "word_reshda_kaskas"
        "رشدة برمة" -> "word_reshda_borma"
        "مكرونة مبكبكة" -> "word_mbakbaka"
        "مكرونة مسقية" -> "word_pasta_masqiya"
        "كسكسي بالحوت" -> "word_couscous_fish"
        "رز بلاو" -> "word_rice_blaw"
        "شربة ليبية" -> "word_libyan_soup"
        "طبيخة بطاطا" -> "word_potato_stew"
        "عصبان" -> "word_osban"
        // Corrected Libyan cuisine / daily-request photographs (batch 2).
        "بوريك" -> "word_bourek"
        "خبزة طاجين" -> "word_khobz_tajin"
        "مقروض" -> "word_maqroudh"
        "قهوة عربية" -> "word_arabic_coffee"
        "مبطن" -> "word_mbattan"
        "حرايمي" -> "word_hraymi"
        "سفنز" -> "word_sfinz"
        "زميطة" -> "word_zmeeta"
        "عيش باللبن" -> "word_aysh_laban"
        "عدّل لي المخدة" -> "word_adjust_pillow"
        // Batch 3: daily requests and prayer / everyday routines.
        "سقعانة", "بردان" -> "word_cold"
        "راسي يوجع" -> "word_headache"
        "أذان" -> "word_azan"
        "وضوء" -> "word_wudhu"
        "صلاة" -> "word_salah"
        "سجادة" -> "word_prayer_rug"
        "سبحة" -> "word_misbaha"
        "مصحف" -> "word_quran"
        "قهوة العشية" -> "word_evening_coffee"
        "مشي", "نمشي" -> "word_walking"
        // Fourth batch: real photos for seven fruit words.
        "فراولة" -> "word_strawberry"
        "مانجو" -> "word_mango"
        "دلاع" -> "word_watermelon"
        "قلعاوي" -> "word_melon_libyan"
        "خوخ" -> "word_peach"
        "مشمش" -> "word_apricot"
        "تمر", "بلح" -> "word_dates"
        "موز" -> "word_banana"
        "عنب" -> "word_grapes"
        "شاهي", "نبي شاهي", "شاهي بالنعناع" -> "word_tea"
        "ماء", "مية", "نبي مية" -> "word_water"
        // Matching real-world photos for common words and short everyday requests.
        "بصل" -> "word_onion"
        "طماطم" -> "word_tomato"
        "تفاح" -> "word_apple"
        "جزر" -> "word_carrot"
        "بطاطا" -> "word_potato"
        "خيار" -> "word_cucumber"
        "حليب" -> "word_milk"
        "دواء", "دوا", "نبي دوايا" -> "word_medicine"
        "دوش", "استحمام" -> "word_shower"
        "حمام", "نبي الحمام" -> "word_toilet"
        "نوم", "نرقد", "سرير", "نبي نرتاح", "راحة" -> "word_sleep"
        "منشفة", "فوطة" -> "word_towel"
        "برتقال" -> "word_orange"
        "هدوم", "ملابس" -> "word_clothes"
        "كندرة", "أحذية" -> "word_shoes"
        "فرشاة أسنان", "فرشة سنان" -> "word_toothbrush"
        "صابون" -> "word_soap"
        "منديل", "مناديل" -> "word_tissue"
        "باب" -> "word_door"
        "كرسي" -> "word_chair"
        else -> null
    }
}
