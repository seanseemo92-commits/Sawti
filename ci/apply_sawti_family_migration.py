"""Safe, idempotent family migration insertion into Sawti base after overlays.
Exit on an unexpected source to avoid releasing an unreviewed database schema.
"""
from pathlib import Path
p=Path('extracted/MyWords/app/src/main/java/com/mywords/data/db/AppDatabase.kt')
s=p.read_text()
if 'MIGRATION_3_4 = object' in s:
    raise SystemExit('Family schema migration unexpectedly already applied')
if s.count('version = 3,')!=1:
    raise SystemExit('Expected Room version 3 exactly once before migration')
if s.count('.addMigrations(MIGRATION_1_2, MIGRATION_2_3)')!=1:
    raise SystemExit('Unexpected preexisting Room migrations')
if s.count('        fun build(context: Context): AppDatabase')!=1:
    raise SystemExit('Database builder anchor not unique')
s=s.replace('    version = 3,','    version = 4,')
s=s.replace('.addMigrations(MIGRATION_1_2, MIGRATION_2_3)', '.addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)')
s=s.replace('        fun build(context: Context): AppDatabase', '        /** v3 → v4: remove only the untouched names bundled with old releases.\n         * Any person with a custom picture, recording, phone number or edited text is preserved.\n         * This is an intentional user-requested removal of obsolete sample family members.\n         */\n        val MIGRATION_3_4 = object : Migration(3, 4) {\n            override fun migrate(db: SupportSQLiteDatabase) {\n                for (member in LegacyFamilyDefaults.entries) {\n                    val prompt = com.mywords.domain.model.TtsEndings.normalize(member.tts)\n                    val cue = member.cue\n                    val derived = com.mywords.domain.model.PhonemicCue.derive(prompt)\n                    val originalSentence = SeedExtras.sentencePrompt("family", member.name)\n                    db.execSQL(\n                        """DELETE FROM word_items\n                           WHERE categoryId = \'family\' AND displayText = ? AND subgroup = ?\n                             AND photoPath IS NULL AND audioPath IS NULL AND TRIM(phone) = \'\'\n                             AND (ttsPrompt = ? OR ttsPrompt = ? OR ttsPrompt = ?)\n                             AND (phonemicCue = ? OR phonemicCue = ? OR phonemicCue = \'\')\n                             AND (sentencePrompt IS NULL OR sentencePrompt = ?)\n                        """.trimIndent(),\n                        arrayOf(member.name, member.group, member.name, member.tts, prompt,\n                            cue, derived, originalSentence),\n                    )\n                }\n            }\n        }\n\n'+'        fun build(context: Context): AppDatabase')
p.write_text(s)
