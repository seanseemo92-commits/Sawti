package com.sawti.data

import android.util.Xml
import com.sawti.domain.model.ArabicDialect
import com.sawti.domain.model.SpeechRate
import com.sawti.domain.model.TextSize
import com.sawti.domain.model.ThemeMode
import com.sawti.domain.model.TouchDwell
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.StringWriter

/** Bounded, typed import. Never install arbitrary SharedPreferences XML from a ZIP. */
internal object RestoreSettingsValidator {
    const val MAX_BYTES = 64L * 1024
    private val booleans = setOf("onboarding_completed", "haptics", "performance_mode", "sentence_strip")
    private val enums = mapOf(
        "theme_mode" to ThemeMode.entries.map { it.name }.toSet(),
        "text_size" to TextSize.entries.map { it.name }.toSet(),
        "speech_rate" to SpeechRate.entries.map { it.name }.toSet(),
        "dialect" to ArabicDialect.entries.map { it.name }.toSet(),
        "touch_dwell" to TouchDwell.entries.map { it.name }.toSet(),
    )

    /** Returns canonical XML only after the complete input was accepted. Missing keys use defaults. */
    fun canonicalXml(file: File): String {
        require(file.isFile && file.length() in 1..MAX_BYTES) { "حجم ملف الإعدادات غير صالح" }
        val values = linkedMapOf<String, Pair<String, String>>()
        file.inputStream().use { input ->
            val parser = Xml.newPullParser()
            parser.setInput(input, "UTF-8")
            var rootSeen = false
            var rootClosed = false
            var key: String? = null
            var tag: String? = null
            val text = StringBuilder()
            while (true) {
                when (parser.nextToken()) {
                    XmlPullParser.END_DOCUMENT -> break
                    XmlPullParser.DOCDECL ->
                        error("ملف الإعدادات يحتوي تعريفاً غير مسموح")
                    XmlPullParser.ENTITY_REF -> {
                        require(tag == "string" && parser.text != null &&
                            (parser.name in setOf("amp", "lt", "gt", "quot", "apos") || parser.name.startsWith("#")))
                        text.append(parser.text)
                        require(text.length <= 1024)
                    }
                    XmlPullParser.START_TAG -> {
                        require(parser.prefix.isNullOrEmpty() && parser.namespace.isNullOrEmpty())
                        if (parser.depth == 1) {
                            require(!rootSeen && !rootClosed && parser.name == "map" && parser.attributeCount == 0)
                            rootSeen = true
                        } else {
                            require(rootSeen && !rootClosed && parser.depth == 2 && key == null)
                            val name = parser.getAttributeValue(null, "name") ?: error("إعداد بلا اسم")
                            require(name !in values) { "إعداد مكرر" }
                            val kind = if (name in booleans) "boolean" else "string"
                            require(name in booleans || name in enums || name == "preferred_voice") { "إعداد غير مدعوم" }
                            require(parser.name == kind) { "نوع الإعداد غير صالح" }
                            require(parser.attributeCount == if (kind == "boolean") 2 else 1)
                            for (i in 0 until parser.attributeCount) {
                                require(parser.getAttributeNamespace(i).isNullOrEmpty())
                                require(parser.getAttributeName(i) in if (kind == "boolean") setOf("name", "value") else setOf("name"))
                            }
                            key = name
                            tag = kind
                            text.clear()
                            if (kind == "boolean") {
                                val value = parser.getAttributeValue(null, "value")
                                require(value == "true" || value == "false") { "قيمة الإعداد غير صالحة" }
                                values[name] = kind to requireNotNull(value)
                            }
                        }
                    }
                    XmlPullParser.TEXT, XmlPullParser.CDSECT, XmlPullParser.IGNORABLE_WHITESPACE -> {
                        if (tag == "string") {
                            text.append(parser.text)
                            require(text.length <= 1024) { "نص الإعداد طويل جداً" }
                        } else require(parser.text.isNullOrBlank())
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.depth == 2) {
                            val name = requireNotNull(key)
                            if (tag == "string") {
                                val value = text.toString()
                                enums[name]?.let { require(value in it) { "قيمة الإعداد غير مدعومة" } }
                                values[name] = "string" to value
                            }
                            key = null
                            tag = null
                        } else {
                            require(parser.depth == 1 && parser.name == "map" && key == null)
                            rootClosed = true
                        }
                    }
                }
            }
            require(rootSeen && rootClosed && key == null) { "ملف الإعدادات غير مكتمل" }
        }
        val output = StringWriter()
        val serializer = Xml.newSerializer()
        serializer.setOutput(output)
        serializer.startDocument("UTF-8", true)
        serializer.startTag(null, "map")
        values.forEach { (name, entry) ->
            serializer.startTag(null, entry.first).attribute(null, "name", name)
            if (entry.first == "boolean") serializer.attribute(null, "value", entry.second)
            else serializer.text(entry.second)
            serializer.endTag(null, entry.first)
        }
        serializer.endTag(null, "map")
        serializer.endDocument()
        return output.toString()
    }
}
