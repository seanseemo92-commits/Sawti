package com.sawti.presentation.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.sawti.R
import com.sawti.domain.model.ArabicDialect

internal fun dialectFlagResource(dialect: ArabicDialect): Int? = when (dialect) {
    ArabicDialect.STANDARD -> null
    ArabicDialect.LIBYAN -> R.drawable.dialect_flag_ly
    ArabicDialect.SAUDI -> R.drawable.dialect_flag_sa
    ArabicDialect.EGYPTIAN -> R.drawable.dialect_flag_eg
    ArabicDialect.SYRIAN -> R.drawable.dialect_flag_sy
    ArabicDialect.IRAQI -> R.drawable.dialect_flag_iq
    ArabicDialect.TUNISIAN -> R.drawable.dialect_flag_tn
    ArabicDialect.MOROCCAN -> R.drawable.dialect_flag_ma
    ArabicDialect.ALGERIAN -> R.drawable.dialect_flag_dz
}

/** Decorative beside a spoken dialect label; do not repeat country names in TalkBack. */
@Composable
internal fun DialectFlag(dialect: ArabicDialect) {
    val image = dialectFlagResource(dialect)
    if (image == null) {
        Box(Modifier.size(36.dp, 27.dp), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Language, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(27.dp))
        }
    } else {
        val shape = RoundedCornerShape(3.dp)
        Image(painterResource(image), contentDescription = null,
            modifier = Modifier.size(36.dp, 27.dp).clip(shape)
                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, shape))
    }
}
