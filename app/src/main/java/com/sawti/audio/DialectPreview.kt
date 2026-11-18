package com.sawti.audio

import com.sawti.data.db.WordSeed
import com.sawti.data.db.toDomain
import com.sawti.domain.model.ArabicDialect
import com.sawti.domain.model.DialectCatalog
import com.sawti.domain.model.WordItem

/** Bridge for the existing settings preview API; screens keep their current code. */
internal object DialectPreview {
    private val source by lazy {
        WordSeed.itemEntities().single { it.semanticKey == "request.water" }.toDomain()
    }

    fun matching(text: String, dialect: ArabicDialect): WordItem? =
        DialectCatalog.localize(source, dialect).takeIf { it.speechText == text }
}
