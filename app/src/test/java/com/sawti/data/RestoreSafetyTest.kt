package com.sawti.data

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import androidx.room.RoomDatabase
import com.sawti.data.db.AppDatabase
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.SQLiteMode
import java.io.File
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
@SQLiteMode(SQLiteMode.Mode.NATIVE)
class RestoreSafetyTest {
    private lateinit var context: Context
    private lateinit var live: AppDatabase
    private lateinit var source: File
    private lateinit var state: File
    private lateinit var oldPhoto: File
    private val prefs get() = File(context.applicationInfo.dataDir, "shared_prefs/settings.xml")
    private val liveFile get() = context.getDatabasePath(AppDatabase.NAME)

    private fun createRoom(file: File, title: String): AppDatabase {
        val room = AppDatabase.builder(context, file.absolutePath)
            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE).allowMainThreadQueries().build()
        room.openHelper.writableDatabase.execSQL(
            "INSERT INTO categories(id,title,palette,previewOnTap,sortOrder) VALUES('family',?,0,1,0)",
            arrayOf(title),
        )
        return room
    }

    @Before fun setup() {
        context = RuntimeEnvironment.getApplication()
        state = File(context.noBackupFilesDir, "sawti_foundation_restore_journal")
        state.deleteRecursively()
        context.deleteDatabase(AppDatabase.NAME)
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit().clear()
            .putBoolean("haptics", false).commit()
        oldPhoto = File(context.filesDir, "photos/old.jpg").apply {
            parentFile!!.mkdirs()
            writeText("original photo")
        }
        live = createRoom(liveFile, "original")
        source = File(context.cacheDir, "incoming.db")
        source.delete()
        createRoom(source, "restored").close()
    }

    @After fun cleanup() {
        live.close()
    }

    private fun sql(file: File, block: (SQLiteDatabase) -> Unit) {
        SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READWRITE).use(block)
    }

    private fun title(file: File): String {
        var value = ""
        sql(file) { db ->
            db.rawQuery("SELECT title FROM categories WHERE id='family'", null).use {
                check(it.moveToFirst())
                value = it.getString(0)
            }
        }
        return value
    }

    private fun archive(
        settings: String? = "<map/>", format: String = BackupFormat.NAME,
        identity: String = AppDatabase.IDENTITY, databaseVersion: Int = AppDatabase.VERSION,
    ): Uri {
        val entries = linkedMapOf(BackupFormat.DATABASE_ENTRY to source.readBytes())
        settings?.let { entries["preferences/settings.xml"] = it.toByteArray() }
        val hashes = JSONObject()
        entries.forEach { (name, bytes) ->
            hashes.put(name, MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) })
        }
        val manifest = JSONObject().put("format", format).put("version", BackupFormat.VERSION)
            .put("databaseIdentity", identity).put("databaseVersion", databaseVersion)
            .put("package", context.packageName).put("sha256", hashes)
        val file = File(context.cacheDir, "test-backup.zip")
        ZipOutputStream(file.outputStream()).use { zip ->
            zip.putNextEntry(ZipEntry("manifest.json"))
            zip.write(manifest.toString().toByteArray())
            zip.closeEntry()
            entries.forEach { (name, bytes) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(bytes)
                zip.closeEntry()
            }
        }
        return Uri.fromFile(file)
    }

    private fun restore(uri: Uri) = runBlocking { BackupManager(context, live).restore(uri) }

    private fun assertOriginalUntouched() {
        assertTrue(live.isOpen)
        assertEquals("original", title(liveFile))
        assertEquals("original photo", oldPhoto.readText())
        assertTrue(prefs.readText().contains("false"))
        assertFalse(state.exists())
    }

    @Test fun forgedMatchingRoomHashCannotHideMissingColumns() {
        sql(source) { db ->
            // Keep the genuine room_master_table identity while replacing the entity schema.
            db.execSQL("DROP TABLE word_items")
            db.execSQL("CREATE TABLE word_items(id INTEGER PRIMARY KEY, photoPath TEXT, audioPath TEXT)")
        }
        assertThrows(Exception::class.java) { restore(archive()) }
        assertOriginalUntouched()
    }

    @Test fun wrongTypedSettingsRejectedBeforeClosingLiveRoom() {
        assertThrows(Exception::class.java) {
            restore(archive("""<map><string name="onboarding_completed">yes</string></map>"""))
        }
        assertOriginalUntouched()
    }

    @Test fun missingAttachmentRejectedInsteadOfSilentlyErasingItsReference() {
        sql(source) { db ->
            db.execSQL("""INSERT INTO word_items(categoryId,subgroup,displayText,ttsPrompt,phonemicCue,
                phone,emoji,sortOrder,photoPath) VALUES('family','','أمي','أمي','','','',0,'photos/missing.jpg')""")
        }
        assertThrows(Exception::class.java) { restore(archive()) }
        assertOriginalUntouched()
    }

    @Test fun triggerRejectedWithoutExecutingIt() {
        sql(source) { it.execSQL("CREATE TRIGGER surprise AFTER UPDATE ON word_items BEGIN DELETE FROM categories; END") }
        assertThrows(Exception::class.java) { restore(archive()) }
        assertOriginalUntouched()
    }

    @Test fun unsupportedVersionAndOrphanedCardAreRejected() {
        sql(source) { it.version = 99 }
        assertThrows(Exception::class.java) { restore(archive()) }
        assertOriginalUntouched()
        sql(source) { db ->
            db.version = AppDatabase.VERSION
            db.execSQL("PRAGMA foreign_keys=OFF")
            db.execSQL("""INSERT INTO word_items(categoryId,subgroup,displayText,ttsPrompt,phonemicCue,
                phone,emoji,sortOrder) VALUES('missing-category','','بطاقة','بطاقة','','','',0)""")
        }
        assertThrows(Exception::class.java) { restore(archive()) }
        assertOriginalUntouched()
    }

    @Test fun validRestoreRetainsOriginalUntilStartupValidationSucceeds() {
        restore(archive("""<map><boolean name="haptics" value="true"/></map>"""))
        assertEquals("restored", title(liveFile))
        assertTrue(File(state, "installed").isFile)
        assertTrue(File(state, "previous/0").isFile)
        assertEquals("original photo", File(state, "previous/3/old.jpg").readText())
        BackupManager.recoverIfInterrupted(context)
        assertEquals("restored", title(liveFile))
        assertTrue(prefs.readText().contains("true"))
        assertFalse(state.exists())
        // Recovery is idempotent after commit cleanup.
        BackupManager.recoverIfInterrupted(context)
        assertEquals("restored", title(liveFile))
    }

    @Test fun brokenInstalledDatabaseRollsBackOnStartup() {
        restore(archive("""<map><boolean name="haptics" value="true"/></map>"""))
        sql(liveFile) { it.execSQL("DROP TABLE word_items") }
        BackupManager.recoverIfInterrupted(context)
        assertEquals("original", title(liveFile))
        assertEquals("original photo", oldPhoto.readText())
        assertTrue(prefs.readText().contains("false"))
        assertFalse(state.exists())
        BackupManager.recoverIfInterrupted(context)
        assertEquals("original", title(liveFile))
    }

    @Test fun brokenInstalledPreferencesAlsoRollBackAllFiles() {
        restore(archive("<map/>"))
        prefs.writeText("""<map><string name="haptics">bad</string></map>""")
        BackupManager.recoverIfInterrupted(context)
        assertEquals("original", title(liveFile))
        assertEquals("original photo", oldPhoto.readText())
        assertTrue(prefs.readText().contains("false"))
    }

    @Test fun interruptedInstallationWithoutInstalledMarkerRollsBack() {
        restore(archive("<map/>"))
        assertTrue(File(state, "installed").delete())
        BackupManager.recoverIfInterrupted(context)
        assertEquals("original", title(liveFile))
        assertTrue(oldPhoto.isFile)
    }

    @Test fun preferencesBackupCannotOverrideNewSettingsAfterRestart() {
        File(prefs.path + ".bak").writeText("""<map><boolean name="haptics" value="false"/></map>""")
        restore(archive("""<map><boolean name="haptics" value="true"/></map>"""))
        assertFalse(File(prefs.path + ".bak").exists())
        BackupManager.recoverIfInterrupted(context)
        assertTrue(prefs.readText().contains("true"))
    }

    @Test fun interruptedRollbackResumesWithoutCommittingMixedFiles() {
        restore(archive("""<map><boolean name="haptics" value="true"/></map>"""))
        // Simulate termination after rollback restored only the original database.
        assertTrue(File(state, "installed").delete())
        assertTrue(liveFile.delete())
        assertTrue(File(state, "previous/0").renameTo(liveFile))
        BackupManager.recoverIfInterrupted(context)
        assertEquals("original", title(liveFile))
        assertEquals("original photo", oldPhoto.readText())
        assertTrue(prefs.readText().contains("false"))
        assertFalse(state.exists())
    }

    @Test fun committedCleanupNeverRollsBackAfterPartialDeletion() {
        restore(archive("<map/>"))
        // Simulate interrupted cleanup after the durable commit marker.
        File(context.noBackupFilesDir, "sawti_foundation_restore_committed").writeText("1")
        File(state, "previous/0").delete()
        BackupManager.recoverIfInterrupted(context)
        assertEquals("restored", title(liveFile))
        assertFalse(state.exists())
        assertFalse(File(context.noBackupFilesDir, "sawti_foundation_restore_committed").exists())
    }

    @Test fun oldFormatsWrongIdentityAndMissingSettingsLeaveLiveDataUntouched() {
        for (uri in listOf(
            archive(format = "sawti-backup"),
        )) {
            assertThrows(Exception::class.java) { restore(uri) }
            assertOriginalUntouched()
        }
        assertThrows(Exception::class.java) { restore(archive(identity = "other-app")) }
        assertOriginalUntouched()
        assertThrows(Exception::class.java) { restore(archive(databaseVersion = 7)) }
        assertOriginalUntouched()
        assertThrows(Exception::class.java) { restore(archive(settings = null)) }
        assertOriginalUntouched()
    }

    @Test fun everyHistoricalDatabaseVersionIsRejectedWithoutMigration() {
        for (version in 2..7) {
            sql(source) { it.version = version }
            assertThrows(Exception::class.java) { restore(archive()) }
            assertOriginalUntouched()
            sql(source) { assertEquals(version, it.version) }
        }
    }

    @Test fun unexpectedTablesAreRejected() {
        sql(source) { it.execSQL("CREATE TABLE surprise(secret TEXT)") }
        assertThrows(Exception::class.java) { restore(archive()) }
        assertOriginalUntouched()
    }

    @Test fun forgedIdentityCannotHideMissingUniqueSemanticIndex() {
        sql(source) { it.execSQL("DROP INDEX index_word_items_semanticKey") }
        assertThrows(Exception::class.java) { restore(archive()) }
        assertOriginalUntouched()
    }

    @Test fun orphanedProgressAndInvalidOverrideFlagsAreRejected() {
        sql(source) {
            it.execSQL("PRAGMA foreign_keys=OFF")
            it.execSQL("INSERT INTO progress_records(wordId,timestamp,mode,outcome) VALUES(999,1,'RECALL','SELF')")
        }
        assertThrows(Exception::class.java) { restore(archive()) }
        assertOriginalUntouched()
        sql(source) {
            it.execSQL("DELETE FROM progress_records")
            it.execSQL("""INSERT INTO word_items(categoryId,subgroup,displayText,ttsPrompt,phonemicCue,
                phone,emoji,sortOrder,customTextFields) VALUES('family','','بطاقة','بطاقة','','','',0,16)""")
        }
        assertThrows(Exception::class.java) { restore(archive()) }
        assertOriginalUntouched()
    }

    @Test fun missingInstalledSettingsRollBackInsteadOfCommittingPartialRestore() {
        restore(archive())
        assertTrue(prefs.delete())
        BackupManager.recoverIfInterrupted(context)
        assertEquals("original", title(liveFile))
        assertEquals("original photo", oldPhoto.readText())
        assertTrue(prefs.readText().contains("false"))
    }
}
