package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import org.junit.Assert.*
import org.junit.Test

/** Written phonetic checks only: native listening remains a separate release gate. */
class NeuralSpeechNormalizerTest {
    private data class Case(
        val dialect: ArabicDialect, val display: String, val legacy: String,
        val expected: String,
    )

    @Test fun preservesLexicalVowelsWhileCleaningRoboticEndingsInEveryDialect() {
        val cases = listOf(
            Case(ArabicDialect.LIBYAN, "نبي مية", "نَبِّيْ إِمِّيَّهْ", "نَبِّي مَيَّة"),
            Case(ArabicDialect.SAUDI, "قارورة موية", "قَارُورَةُ مَوْيَةْ", "قَارُورَة مَوْيَة"),
            Case(ArabicDialect.EGYPTIAN, "إزازة مية", "إِزَازِتْ مَيَّهْ", "إِزَازِة مَيَّة"),
            Case(ArabicDialect.SYRIAN, "قنينة مي", "قَنِّينَةُ مَيّْ", "قَنِّينَة مَيّ"),
            Case(ArabicDialect.IRAQI, "كبة تمن", "كُبَّتْ تِمَّنْ", "كُبَّة تِمَّن"),
            Case(ArabicDialect.TUNISIAN, "دبوزة ماء", "دَبُّوزَةُ مَاءْ", "دَبُّوزَة مَاء"),
            Case(ArabicDialect.ALGERIAN, "قرعة ماء", "قَرْعَةُ مَاءْ", "قَرْعَة مَاء"),
            Case(ArabicDialect.MOROCCAN, "بغيت الما", "بْغِيتْ الْمَا", "بغِيت المَا"),
            Case(ArabicDialect.STANDARD, "دراجة نارية", "دَرَّاجَةٌ نَارِيَّهْ", "دَرَّاجَة نَارِيَّة"),
        )
        assertEquals(9, cases.map { it.dialect }.distinct().size)
        for (case in cases) {
            assertEquals(case.dialect.name,
                case.expected, NeuralSpeechNormalizer.normalize(
                    case.display, case.legacy, case.dialect))
        }
    }

    @Test fun fixesProstheticHamzasAndAffricatesWithoutDeletingRealHamzas() {
        val dialect = ArabicDialect.LIBYAN
        val samples = mapOf(
            "مكرونة مبكبكة" to ("مَكَرُونَهْ إِمْبَكْبَكَهْ" to "مَكَرُونَة مبَكْبَكَة"),
            "طبيخة بطاطا" to ("إِطْبِيخِتْ بَطَاطَا" to "طبِيخَة بَطَاطَا"),
            "شخاشير" to ("إِشْخَاشِيرْ" to "شخَاشِير"),
            "نقعمز" to ("إِنْقَعْمِزْ" to "نقَعْمِز"),
            "نسكر" to ("إِنْسَكِّرْ" to "نسَكِّر"),
            "نشوف" to ("إِنْشُوفْ" to "نشُوف"),
            "نصلي" to ("إِنْصَلِّي" to "نصَلِّي"),
            "سفنز" to ("إِسْفِنْزْ" to "سفِنْز"),
            "نحس بالبرد" to ("إِنْحِسّْ بِالْبَرْدْ" to "نحِسّ بِالبَرْد"),
        )
        for ((display, terms) in samples) {
            assertEquals(display, terms.second,
                NeuralSpeechNormalizer.normalize(display, terms.first, dialect))
        }
        assertEquals("إِزَازِة", NeuralSpeechNormalizer.normalize(
            "إزازة", "إِزَازِةْ", ArabicDialect.EGYPTIAN))
        assertEquals("سِمَتش مَسْكُوف", NeuralSpeechNormalizer.normalize(
            "سمچ مسكوف", "سِمَتْشْ مَسْكُوفْ", ArabicDialect.IRAQI))
    }

    @Test fun cleanConstructStatePreservesShaddaAndDialectVowels() {
        val cases = listOf(
            Triple("رشدة كسكاس", "رِشْدِتْ كِسْكَاسْ", "رِشْدِة كِسْكَاس"),
            Triple("شربة فريك", "شُرْبِتْ فْرِيكْ", "شُرْبِة فْرِيك"),
            Triple("قهوة العشية", "قَهْوَةُ الْعَشِيَّهْ", "قَهْوَة العَشِيَّة"),
            Triple("مقلوبة باذنجان", "مَقْلُوبِتْ بَاذِنْجَانْ", "مَقْلُوبِة بَاذِنْجَان"),
        )
        for ((display, old, expected) in cases) {
            assertEquals(display, expected, NeuralSpeechNormalizer.normalize(
                display, old, ArabicDialect.ALGERIAN))
        }
        for (word in listOf("رِجْل", "سِنَّة", "زُمِيطَة", "دَنْجَال")) {
            assertEquals(word, NeuralSpeechNormalizer.normalize(
                word.replace(Regex("[\\u064B-\\u065F]"), ""),
                word, ArabicDialect.LIBYAN))
        }
    }

    @Test fun legacyFallbackRemainsUnchangedAndCustomSpeechIsNeverNeuralNormalized() {
        val original = WordSeed.itemEntities().single { it.semanticKey == "request.water" }.toDomain()
        val dialect = ArabicDialect.LIBYAN
        val old = DialectSpeechCatalog.resolve(original, dialect)
        val neural = DialectSpeechCatalog.resolveForNeural(original, dialect)
        assertNotEquals(old.speechText, neural.speechText)
        assertEquals("نبي مية", neural.displayText)
        assertEquals(old.speechText, DialectSpeechCatalog.resolve(original, dialect).speechText)
        val custom = original.copy(ttsPrompt = "نَبِّيْ إِمِّيَّهْ",
            customTextFields = CardDetails.SPEECH)
        assertEquals(custom.ttsPrompt,
            DialectSpeechCatalog.resolveForNeural(custom, dialect).speechText)
        assertNull(DialectCatalog.localize(custom, dialect).neuralSpeechText)
    }

    @Test fun libyanColdLabelIsNaturalWhileOldAndroidTtsSpeechStaysIntact() {
        val original = WordSeed.itemEntities().single { it.semanticKey == "request.cold" }.toDomain()
        val card = DialectCatalog.localize(original, ArabicDialect.LIBYAN)
        assertEquals("نحس بالبرد", card.displayText)
        assertTrue(card.ttsPrompt.startsWith("إِنْحِسّ"))
        assertTrue(requireNotNull(card.neuralSpeechText).startsWith("نحِسّ"))
        assertEquals(original, card.forEditing())
    }
}
