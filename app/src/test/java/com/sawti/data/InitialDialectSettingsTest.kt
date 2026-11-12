package com.sawti.data

import android.app.Application
import android.content.Context
import android.telephony.TelephonyManager
import com.sawti.domain.model.ArabicDialect
import com.sawti.domain.model.TextSize
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class InitialDialectSettingsTest {
    private lateinit var context: Context
    @Before fun setup() {
        context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("quran_reciter", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test fun savedReciterMigratesBeforeTheFirstBackupWithoutOpeningQuran() {
        context.getSharedPreferences("quran_reciter", Context.MODE_PRIVATE).edit()
            .putString("selected_edition", "salah.bukhatir").commit()
        SettingsRepository(context)
        assertEquals("salah.bukhatir", context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getString("quran_edition", null))
    }

    @Test fun restoredReciterWinsOverTheOldDeviceSelection() {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit()
            .putString("quran_edition", "husary.muallim").commit()
        context.getSharedPreferences("quran_reciter", Context.MODE_PRIVATE).edit()
            .putString("selected_edition", "salah.bukhatir").commit()
        SettingsRepository(context)
        assertEquals("husary.muallim", context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getString("quran_edition", null))
    }

    @Test fun retiredOrUnknownLegacyReciterFallsBackToAlafasy() {
        context.getSharedPreferences("quran_reciter", Context.MODE_PRIVATE).edit()
            .putString("selected_edition", "removed-reader").commit()
        SettingsRepository(context)
        assertEquals("ar.alafasy", context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getString("quran_edition", null))
    }

    @Test fun firstChoiceIsPersistedAndNeverRedetectedAfterManualSelection() {
        var calls = 0
        val first = SettingsRepository(context) { calls++; ArabicDialect.LIBYAN }
        assertEquals(ArabicDialect.LIBYAN, first.state.value.dialect)
        assertEquals(1, calls)
        assertEquals(ArabicDialect.LIBYAN,
            SettingsRepository(context) { calls++; ArabicDialect.EGYPTIAN }.state.value.dialect)
        first.setDialect(ArabicDialect.MOROCCAN)
        assertEquals(ArabicDialect.MOROCCAN,
            SettingsRepository(context) { error("Saved choice must prevent country detection") }.state.value.dialect)
        assertEquals(1, calls)
    }

    @Test fun savedFushaAndOtherPreferencesArePreservedAcrossUpdateOrRestore() {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit()
            .putString("dialect", "STANDARD").putString("preferred_voice", "my-arabic-voice")
            .putString("text_size", "EXTRA_LARGE").putBoolean("haptics", false).commit()
        val settings = SettingsRepository(context) { error("Never replace a saved Fusha choice") }.state.value
        assertEquals(ArabicDialect.STANDARD, settings.dialect)
        assertEquals("my-arabic-voice", settings.preferredVoice)
        assertEquals(TextSize.EXTRA_LARGE, settings.textSize)
        assertFalse(settings.hapticsEnabled)
    }

    @Test fun unavailableCountryDetectionFailsSafelyAndPersistsFusha() {
        assertEquals(ArabicDialect.STANDARD,
            SettingsRepository(context) { throw SecurityException("unavailable") }.state.value.dialect)
        assertEquals(ArabicDialect.STANDARD,
            SettingsRepository(context) { error("Do not retry or change the initial default") }.state.value.dialect)
    }

    @Test fun realResolverUsesSimCountryWithoutAddingLocationOrPhonePermissions() {
        val phone = context.getSystemService(TelephonyManager::class.java)
        shadowOf(phone).setSimCountryIso("ly")
        shadowOf(phone).setNetworkCountryIso("eg")
        assertEquals(ArabicDialect.LIBYAN, SettingsRepository(context).state.value.dialect)
        val permissions = context.packageManager.getPackageInfo(context.packageName,
            android.content.pm.PackageManager.GET_PERMISSIONS).requestedPermissions.orEmpty().toSet()
        for (permission in listOf("android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.READ_PHONE_STATE", "android.permission.READ_PHONE_NUMBERS")) {
            assertFalse(permission, permission in permissions)
        }
    }
}
