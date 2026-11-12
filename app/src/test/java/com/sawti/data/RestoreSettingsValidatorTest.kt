package com.sawti.data

import android.app.Application
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class RestoreSettingsValidatorTest {
    @get:Rule val temp = TemporaryFolder()

    private fun parse(xml: String): String {
        val file = temp.newFile()
        file.writeText(xml)
        return RestoreSettingsValidator.canonicalXml(file)
    }

    @Test fun validSettingsRoundTripIncludingEscapedVoiceName() {
        val xml = parse("""<map>
            <boolean name="haptics" value="false"/>
            <boolean name="onboarding_completed" value="true"/>
            <string name="dialect">LIBYAN</string>
            <string name="theme_mode">DARK</string>
            <string name="preferred_voice">Arabic &amp; local</string>
        </map>""")
        assertEquals(xml, parse(xml))
        assertTrue(xml.contains("LIBYAN"))
        assertTrue(xml.contains("false"))
        assertTrue(xml.contains("Arabic &amp; local"))
    }

    @Test fun omittedSettingsAreAllowedForOlderBackups() {
        assertTrue(parse("<map/>").contains("map"))
    }

    @Test fun allSupportedQuranRecitersSurviveBackupValidation() {
        for (reader in listOf("ar.alafasy", "salah.bukhatir", "husary.muallim")) {
            val xml = parse("""<map><string name="quran_edition">$reader</string></map>""")
            assertTrue(xml.contains(reader))
            assertEquals(xml, parse(xml))
        }
        assertThrows(Exception::class.java) {
            parse("""<map><string name="quran_edition">unknown-reader</string></map>""")
        }
    }

    @Test fun rejectsWrongTypesAndValues() {
        listOf(
            """<map><string name="onboarding_completed">yes</string></map>""",
            """<map><boolean name="haptics" value="yes"/></map>""",
            """<map><boolean name="theme_mode" value="true"/></map>""",
            """<map><string name="theme_mode">UNKNOWN</string></map>""",
            """<map><string name="unknown">anything</string></map>""",
            """<map><int name="haptics" value="1"/></map>""",
        ).forEach { xml -> assertThrows(Exception::class.java) { parse(xml) } }
    }

    @Test fun rejectsDuplicatesNestedMarkupDoctypeAndTruncation() {
        listOf(
            """<map><boolean name="haptics" value="true"/><boolean name="haptics" value="false"/></map>""",
            """<map><string name="preferred_voice"><map/></string></map>""",
            """<!DOCTYPE map [<!ENTITY x SYSTEM "file:///not-read">]><map/>""",
            """<map><string name="preferred_voice">&unknown;</string></map>""",
            """<map><string name="dialect">LIBYAN</string>""",
            "<map/><map/>",
            """<map><boolean name="haptics" value="true" extra="x"/></map>""",
        ).forEach { xml -> assertThrows(Exception::class.java) { parse(xml) } }
    }

    @Test fun rejectsOversizedFileAndVoice() {
        assertThrows(Exception::class.java) { parse("<map>" + " ".repeat(65536) + "</map>") }
        assertThrows(Exception::class.java) {
            parse("""<map><string name="preferred_voice">""" + "x".repeat(1025) + "</string></map>")
        }
    }
}
