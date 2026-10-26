"""Strip historical personal-family examples from the distributable Android source."""
from pathlib import Path
import re, hashlib
root=Path("extracted/MyWords")
base=root/"app/src/main/java/com/mywords"
legacy=base/"data/db/LegacyFamilyDefaults.kt"
clean=base/"data/db/LegacyFamilyCleanup.kt"
assert legacy.is_file() and not clean.exists(), "Expected unmodified historical family baseline"
entries=re.findall(r'Member\("([^"]+)", "([^"]+)", "([^"]+)", "([^"]+)"\)',legacy.read_text(encoding="utf-8"))
assert len(entries)==24, "Unexpected historical sample count: stop for review"
def mark(c): return "\u064b"<=c<="\u065f" or c=="\u0670"
def normalize(original):
    body,tail=original.strip(),""
    while body and not(body[-1].isalpha() or mark(body[-1])):
        tail=body[-1]+tail; body=body[:-1]
    if not body: return original.strip()
    if body.endswith("\u064b\u0627"): body=body[:-2]
    i=len(body)
    while i and mark(body[i-1]): i-=1
    if not i: return original.strip()
    letter,prefix=body[i-1],body[:i-1]
    marks="".join(c for c in body[i:] if c not in "\u064b\u064c\u064d\u064e\u064f\u0650")
    if letter=="\u0629": return prefix+"\u0647\u0652"+tail
    if letter in "\u0627\u0649" or "\u0652" in marks or "\u0651" in marks: return prefix+letter+marks+tail
    return prefix+letter+marks+"\u0652"+tail
def derived(tts):
    clusters=[]
    for ch in tts.strip():
        if mark(ch) and clusters: clusters[-1]+=ch
        elif ch.isalpha() and not mark(ch): clusters.append(ch)
    if not clusters: return ""
    i=2 if len(clusters)>2 and clusters[0]=="\u0627" and clusters[1].startswith("\u0644") else 0
    first=clusters[i]
    prefix=first.replace("\u0651","")
    nxt=clusters[i+1] if i+1<len(clusters) else None
    vowel=any("\u064b"<=ch<="\u0650" or ch=="\u0670" for ch in prefix)
    if nxt and (first[0] in "\u0623\u0625\u0622\u0627" or not vowel or "\u0652" in nxt): prefix+=nxt.replace("\u0651","")
    return prefix+"\u0640..."
def fp(values): return hashlib.sha256("\x1f".join(values).encode("utf-8")).hexdigest()
signatures={fp((g,n,v,c)) for g,n,tts,cue in entries for v in {n,tts,normalize(tts)} for c in {cue,"",derived(normalize(tts))}}
source='''package com.mywords.data.db

import androidx.sqlite.db.SupportSQLiteDatabase
import java.security.MessageDigest

/** Delete historical unedited sample people, not user-customized family cards. */
internal object LegacyFamilyCleanup {
    private val historicalSignatures = setOf(
'''+''.join('        "'+sig+'",\n' for sig in sorted(signatures))+'''    )
    private val defaultSentence = "أُرِيدُ أَنْ أَتَكَلَّمَ مَعَ ..."

    private fun fingerprint(group: String, name: String, voice: String, cue: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(listOf(group, name, voice, cue).joinToString("\\u001F").toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it.toInt() and 0xff) }

    fun removeUntouchedOldSamples(db: SupportSQLiteDatabase) {
        val toDelete = mutableListOf<Long>()
        db.query(
            """SELECT id, subgroup, displayText, ttsPrompt, phonemicCue FROM word_items
               WHERE categoryId = 'family'
                 AND photoPath IS NULL AND audioPath IS NULL AND TRIM(phone) = ''
                 AND (sentencePrompt IS NULL OR sentencePrompt = ?)""".trimIndent(),
            arrayOf(defaultSentence),
        ).use { cursor ->
            while (cursor.moveToNext()) {
                if (fingerprint(cursor.getString(1), cursor.getString(2),
                        cursor.getString(3), cursor.getString(4)) in historicalSignatures) {
                    toDelete += cursor.getLong(0)
                }
            }
        }
        for (id in toDelete) {
            db.execSQL("DELETE FROM word_items WHERE id = ? AND categoryId = 'family'", arrayOf(id))
        }
    }
}
'''
clean.write_text(source,encoding="utf-8")
legacy.unlink()
p=base/"data/db/AppDatabase.kt"; s=p.read_text(encoding="utf-8")
start=s.index("        /** v3 → v4:")
end=s.index("        fun build(context: Context)",start)
s=s[:start]+'''        /** v3 → v4: remove only unchanged historical sample people. */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                LegacyFamilyCleanup.removeUntouchedOldSamples(db)
            }
        }

'''+s[end:]
p.write_text(s,encoding="utf-8")
phrase=[n for _,n,_,_ in entries if "؟" in n]
assert len(phrase)==1
phrase=phrase[0]
p=base/"data/db/SeedExtras.kt"; s=p.read_text(encoding="utf-8")
needle=', "family/'+phrase+'"'
assert s.count(needle)==1
p.write_text(s.replace(needle,""),encoding="utf-8")
p=base/"domain/model/DialectCatalog.kt"
lines=p.read_text(encoding="utf-8").splitlines(keepends=True)
filtered=[line for line in lines if not line.lstrip().startswith('"'+phrase+'" to listOf(')]
assert len(filtered)==len(lines)-1
p.write_text("".join(filtered),encoding="utf-8")
p=base/"domain/model/Models.kt"; s=p.read_text(encoding="utf-8")
for _,name,_,_ in entries: s=s.replace('"'+name+'"','"مثال"')
p.write_text(s,encoding="utf-8")
p=root/"app/src/test/java/com/mywords/domain/model/FamilyRelationshipsTest.kt"
s=p.read_text(encoding="utf-8")
s,count=re.subn(r'^\s*assertFalse\(cat\.words\.any \{ it\.display == .*? \}\)\s*$','\n        assertTrue(cat.words.all { it.subgroup == FamilyRelationships.RELATIVES })',s,flags=re.MULTILINE)
assert count==1
p.write_text(s,encoding="utf-8")
for p in root.rglob("*.kt"):
    s=p.read_text(encoding="utf-8")
    for _,name,tts,_ in entries:
        assert '"'+name+'"' not in s, "Personal name still in "+str(p)
        assert '"'+tts+'"' not in s, "Personal TTS prompt still in "+str(p)
    assert phrase not in s, "Personal phrase still in "+str(p)
p=root/"app/build.gradle.kts";s=p.read_text(encoding="utf-8")
s=s.replace('versionName = "4.0"','versionName = "4.1"')
s=s.replace("versionCode = 5","versionCode = 6")
p.write_text(s,encoding="utf-8")
print("Verified clean public source; historical-person matches:",len(entries),"signatures:",len(signatures))
