package com.sawti.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class InitialDialectPolicyTest {
    @Test fun everySupportedCountryHasTheExpectedDialect() {
        val expected = mapOf("LY" to ArabicDialect.LIBYAN, "SA" to ArabicDialect.SAUDI,
            "EG" to ArabicDialect.EGYPTIAN, "SY" to ArabicDialect.SYRIAN, "IQ" to ArabicDialect.IRAQI,
            "TN" to ArabicDialect.TUNISIAN, "MA" to ArabicDialect.MOROCCAN, "DZ" to ArabicDialect.ALGERIAN)
        expected.forEach { (code, dialect) ->
            assertEquals(dialect, InitialDialectPolicy.choose(" ${code.lowercase()} ", null, emptyList()))
        }
    }

    @Test fun homeSimWinsOverRoamingNetworkAndDeviceRegion() {
        assertEquals(ArabicDialect.LIBYAN, InitialDialectPolicy.choose("ly", "eg", listOf("TN")))
        assertEquals(ArabicDialect.STANDARD, InitialDialectPolicy.choose("gb", "eg", listOf("LY")))
    }

    @Test fun absentOrInvalidSourcesFallBackInOrderThenToFusha() {
        assertEquals(ArabicDialect.EGYPTIAN, InitialDialectPolicy.choose(null, "eg", listOf("TN")))
        assertEquals(ArabicDialect.TUNISIAN, InitialDialectPolicy.choose("", "", listOf("", "TN")))
        assertEquals(ArabicDialect.IRAQI, InitialDialectPolicy.choose("invalid", "??", listOf("001", "iq")))
        assertEquals(ArabicDialect.STANDARD, InitialDialectPolicy.choose(null, null, emptyList()))
        assertEquals(ArabicDialect.STANDARD, InitialDialectPolicy.choose(null, null, listOf("US")))
    }
}
