package com.sawti.audio

import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import com.sawti.domain.model.ArabicDialect
import com.sawti.domain.model.DialectSpeechCatalog
import com.sawti.domain.model.QuestionPhrases
import org.junit.Assert.*
import org.junit.Test

class DialectPreviewTest {
    @Test fun existingSettingsPreviewUsesTheBuiltinAssetKeyInAllDialects() {
        val source = WordSeed.itemEntities().single { it.semanticKey == "request.water" }.toDomain()
        for (dialect in ArabicDialect.entries) {
            val text = DialectSpeechCatalog.resolveForHabibi(source, dialect).speechText
            val preview = requireNotNull(DialectPreview.matching(text, dialect))
            assertEquals("request.water", preview.semanticKey)
            assertEquals(text, DialectSpeechCatalog.habibiTextFor(preview, dialect))
            assertNull(DialectPreview.matching(QuestionPhrases.where(preview), dialect))
            assertNull(DialectPreview.matching("رسالة توجيهية", dialect))
        }
    }
}
