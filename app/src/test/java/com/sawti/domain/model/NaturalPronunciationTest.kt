package com.sawti.domain.model

import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import org.junit.Assert.*
import org.junit.Test

/** Tests one natural built-in speech path without altering any personal pronunciation. */
class NaturalPronunciationTest {
    private fun seed(key: String) = WordSeed.itemEntities()
        .single { it.semanticKey == key }.toDomain()

    @Test fun neutralizesRoboticEndingsButPreservesDeliberateArabicHamza() {
        val cases = listOf(
            Triple(ArabicDialect.LIBYAN, "نبي مية", "نِبِّي مَيَّة"),
            Triple(ArabicDialect.EGYPTIAN, "إزازة مية", "إِزَازِة مَيَّة"),
            Triple(ArabicDialect.SAUDI, "قارورة موية", "قَارُورَة مَوْيَة"),
        )
        for ((dialect, visible, expected) in cases) {
            val raw = if (dialect == ArabicDialect.LIBYAN) "نِبِّيْ إِمِّيَّهْ"
                else if (dialect == ArabicDialect.EGYPTIAN) "إِزَازِتْ مَيَّهْ"
                else "قَارُورَةُ مَوْيَةْ"
            assertEquals(expected,
                DialectSpeechCatalog.cleanSpeech(visible, raw, dialect))
        }
    }

    @Test fun preservesLibyanConsonantClustersWhenPartOfRequestedWord() {
        assertEquals("صْبَع", DialectSpeechCatalog.resolve(seed("body.finger"),
            ArabicDialect.LIBYAN).speechText)
        assertEquals("نِبِّي دْوَايَا", DialectSpeechCatalog.resolve(seed("request.medicine"),
            ArabicDialect.LIBYAN).speechText)
    }

    @Test fun explicitInitialAndIraqiAffricateSukunsSurviveNormalization() {
        val cases = mapOf(
            ArabicDialect.LIBYAN to mapOf(
                "animal.camel" to "جْمَل", "animal.chicken" to "دْجَاجَة",
                "animal.horse" to "حْصَان", "food.chicken" to "دْجَاج",
                "clothing.socks" to "شْخَاشِير",
                "dish.mbakbaka" to "مَكَرُونَة مْبَكْبَكَة",
                "dish.potato_stew" to "طْبِيخَة بَطَاطَا",
                "dish.sfinz" to "إِسْفِنْز"),
            ArabicDialect.IRAQI to mapOf(
                "food.tea" to "تْشَاي", "food.fish" to "سِمَتْش",
                "greeting.how_are_you" to "شْلُونَك؟"),
            ArabicDialect.TUNISIAN to mapOf(
                "greeting.how_are_you" to "شْنُوَّة أَحْوَالَك؟"),
            ArabicDialect.ALGERIAN to mapOf("fruit.orange" to "تْشِينَة"),
        )
        for ((dialect, expected) in cases) for ((key, speech) in expected) {
            assertEquals("$dialect/$key", speech, DialectSpeechCatalog.resolve(seed(key), dialect).speechText)
        }
    }

    @Test fun allDialectsPreserveRoomSourceAndPersonalTextAndMedia() {
        val source = seed("request.water").copy(audioPath = "/my/voice.m4a",
            photoPath = "/my/photo.jpg")
        for (dialect in ArabicDialect.entries) {
            val projected = DialectCatalog.localize(source, dialect)
            assertEquals(source, projected.forEditing())
            assertEquals(source.audioPath, projected.audioPath)
            assertEquals(source.photoPath, projected.photoPath)
            for (flag in listOf(CardDetails.SPEECH, CardDetails.LABEL)) {
                val personal = source.copy(ttsPrompt = "صَلَاةٌ چ گ", customTextFields = flag)
                val result = DialectCatalog.localize(personal, dialect)
                assertEquals(personal.ttsPrompt, result.ttsPrompt)
                assertNull(result.neuralSpeechText)
            }
        }
    }

    @Test fun eachDialectKeepsItsOwnVocabulary() {
        assertEquals("بيض", DialectCatalog.localize(seed("food.eggs"),
            ArabicDialect.LIBYAN).displayText)
        assertEquals("عظم", DialectCatalog.localize(seed("food.eggs"),
            ArabicDialect.TUNISIAN).displayText)
        assertEquals("بيض", DialectCatalog.localize(seed("food.eggs"),
            ArabicDialect.SAUDI).displayText)
    }

    @Test fun everyBuiltInCardUsesTheSameSpeechForExportAndPlayback() {
        for (dialect in ArabicDialect.entries) {
            for (source in WordSeed.itemEntities().map { it.toDomain() }) {
                assertEquals(dialect.name + "/" + source.semanticKey,
                    DialectSpeechCatalog.resolve(source, dialect).speechText,
                    DialectSpeechCatalog.resolveForNeural(source, dialect).speechText)
            }
        }
    }
}
