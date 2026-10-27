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
    "animals" -> "img_cat_animals"
    "home" -> "word_living_room" // Cover the home section card with its existing living-room photo.
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
        // Batch 12: remaining fruit photos, including Libyan Arabic synonyms.
        "إجاص", "كمثرى" -> "word_pear"
        "رمان" -> "word_pomegranate"
        "كرموس", "تين" -> "word_figs"
        "كيوي" -> "word_kiwi"
        "أناناس" -> "word_pineapple"
        "كرز" -> "word_cherry"
        "توت" -> "word_berries"
        "مندرين", "يوسفي", "كلمنتينا" -> "word_mandarin"
        "أفوكادو" -> "word_avocado"
        "جوز الهند" -> "word_coconut"
        // Batch 5: remaining fresh vegetable photographs.
        "ثوم" -> "word_garlic"
        "باذنجان" -> "word_eggplant"
        "فلفل" -> "word_pepper"
        "خس" -> "word_lettuce"
        "كوسة" -> "word_zucchini"
        "كسبرة", "كزبرة" -> "word_coriander"
        "معدنوس", "بقدونس" -> "word_parsley"
        "بازيلية", "بازيليه", "بازلاء", "بسلة" -> "word_peas"
        "ليمون" -> "word_lemon"
        "فجل" -> "word_radish"
        // Batch 6: vegetables and home/furniture.
        "قرعة", "بقطين", "يقطين" -> "word_pumpkin"
        "نعناع" -> "word_mint"
        "ذرة" -> "word_corn"
        "مخدة" -> "word_pillow"
        "طاولة" -> "word_table"
        "ورغان / بطانية", "ورغان", "بطانية" -> "word_blanket"
        "صالون" -> "word_living_room"
        "روشن", "نافذة" -> "word_window"
        "ثلاجة" -> "word_fridge"
        // Batch 7: home appliances and Libyan kitchen vocabulary.
        "تلفزيون" -> "word_tv"
        "غسالة" -> "word_washing_machine"
        "غاز" -> "word_gas_stove"
        "مكيف" -> "word_air_conditioner"
        "كوشة" -> "word_oven"
        "دولاب" -> "word_wardrobe"
        "مروحة" -> "word_fan"
        "ساعة" -> "word_clock"
        "مفتاح" -> "word_key"
        "كوجينة" -> "word_kitchen"
        // Batch 8: Libyan home and dining vocabulary (nine independent photos).
        "براد" -> "word_teapot"
        "موس (سكين)", "موس", "سكين" -> "word_knife"
        "طنجرة" -> "word_pot"
        "شيشة" -> "word_water_bottle"
        "بساط / حواشي", "بساط", "حواشي" -> "word_carpet_mat"
        "طاسة" -> "word_glass_cup"
        "صونية" -> "word_plate"
        "كاشيك", "ملعقة" -> "word_spoon"
        "فرشيطة", "شوكة" -> "word_fork"
        // Batch 9: modern everyday clothing (Libyan names and common alternatives).
        "تيشرت", "تيشيرت", "تي شيرت" -> "word_tshirt"
        "قميص" -> "word_shirt"
        "سروال", "بنطلون" -> "word_pants"
        "جاكة", "جاكيت" -> "word_jacket"
        "سورية", "بلوزة", "سورية / بلوزة" -> "word_blouse"
        "شورت" -> "word_shorts"
        "شخاشير", "جوارب" -> "word_socks"
        "كبوط", "بالطو" -> "word_coat"
        "طاقية", "كاسكيطة" -> "word_cap"
        // Batch 10: body parts, real images with Libyan labels and spelling variants.
        "راس", "رأس" -> "word_head"
        "عين", "عيون" -> "word_eye"
        "ودن", "أذن", "وذن", "اذن" -> "word_ear"
        "خشم", "أنف", "انف" -> "word_nose"
        "فم", "فم وأسنان" -> "word_mouth"
        "يد", "كف" -> "word_hand"
        "رجل / كراع", "رجل", "ساق", "كراع" -> "word_leg"
        "بطن" -> "word_stomach"
        "ظهر" -> "word_back"
        "قلب" -> "word_heart"
        // Batch 11 part 1: pets, farm animals, and birds.
        "قطوسة", "قطة" -> "word_cat"
        "كلب" -> "word_dog"
        "خروف" -> "word_sheep"
        "حصان" -> "word_horse"
        "بقرة" -> "word_cow"
        "عصفور" -> "word_sparrow"
        "حمامة" -> "word_pigeon"
        "دجاجة" -> "word_chicken"
        "بطة" -> "word_duck"
        // Batch 11 part 2: complete the animal and bird vocabulary.
        "جمل" -> "word_camel"
        "سردوك" -> "word_rooster"
        "قنية" -> "word_rabbit"
        // Batch 13: remaining body parts, including local Libyan labels.
        "وجه" -> "word_face"
        "صدر" -> "word_chest"
        "سنة", "سنون", "أسنان", "سن" -> "word_tooth"
        "كتف" -> "word_shoulder"
        "لسان" -> "word_tongue"
        "شعر" -> "word_hair"
        "ركبة" -> "word_knee"
        "رقبة" -> "word_neck"
        "صبع", "إصبع" -> "word_finger"
        // Batch 14: food and drinks, including Libyan aliases.
        "خبزة", "خبز" -> "word_bread"
        "بيض", "دحي", "بيض / دحي" -> "word_eggs"
        "جبنة", "جبن" -> "word_cheese"
        "زبدة" -> "word_butter"
        "زبادي" -> "word_yogurt"
        "مكرونة" -> "word_pasta"
        "أرز", "رز" -> "word_rice"
        "لحم" -> "word_meat"
        "شوربة" -> "word_soup"
        "دجاج" -> "word_chicken_food"
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
        "كندرة", "سبيدرو", "سبيدرو / كندرة", "أحذية" -> "word_shoes"
        "فرشاة أسنان", "فرشة سنان" -> "word_toothbrush"
        "صابون" -> "word_soap"
        "منديل", "مناديل" -> "word_tissue"
        "باب" -> "word_door"
        "كرسي" -> "word_chair"
        else -> null
    }
}
