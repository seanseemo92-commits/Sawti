package com.sawti.data

import android.content.Context
import android.content.res.Resources
import android.telephony.TelephonyManager
import com.sawti.domain.model.ArabicDialect
import com.sawti.domain.model.InitialDialectPolicy

/** Local, permission-free initial hint. No GPS, IP lookup, identifiers or country tracking. */
internal object DeviceDialectResolver {
    fun detect(context: Context): ArabicDialect {
        val phone = runCatching { context.getSystemService(TelephonyManager::class.java) }.getOrNull()
        // Unsupported devices, unavailable SIMs and OEM permission failures all have safe fallbacks.
        val sim = runCatching { phone?.simCountryIso }.getOrNull()
        val network = runCatching { phone?.networkCountryIso }.getOrNull()
        val countries = runCatching {
            val locales = Resources.getSystem().configuration.locales
            (0 until locales.size()).map { locales[it].country }
        }.getOrDefault(emptyList())
        return InitialDialectPolicy.choose(sim, network, countries)
    }
}
