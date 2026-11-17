package com.sawti

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sawti.audio.AudioFocusController
import com.sawti.data.BackupManager
import com.sawti.data.db.AppDatabase
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Runs on a device/emulator without opening the patient's live Sawti database. */
@RunWith(AndroidJUnit4::class)
class SawtiIntegrationTest {
    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    private fun newDatabase(name: String): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, name)
            .allowMainThreadQueries()
            .build()

    private fun withDatabase(name: String, action: (AppDatabase) -> Unit) {
        val db = newDatabase(name)
        try {
            action(db)
        } finally {
            db.close()
        }
    }

    @Test fun roomPersistsAcrossCloseAndReopen() {
        val name = "sawti_instrumentation_" + System.nanoTime() + ".db"
        try {
            withDatabase(name) { db ->
                db.openHelper.writableDatabase.execSQL(
                    "INSERT INTO categories(id,title,palette,previewOnTap,sortOrder) " +
                        "VALUES('family','patient card',0,1,0)"
                )
            }
            withDatabase(name) { db ->
                db.openHelper.readableDatabase.query(
                    "SELECT title FROM categories WHERE id='family'"
                ).use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    assertEquals("patient card", cursor.getString(0))
                }
            }
        } finally {
            context.deleteDatabase(name)
        }
    }

    @Test fun zipTraversalCannotRetireRoomOrWriteOutsideStaging() {
        val name = "sawti_instrumentation_" + System.nanoTime() + ".db"
        val zip = File(context.cacheDir, "sawti_zip_slip_" + System.nanoTime() + ".zip")
        val escapedFile = File(context.cacheDir, "escape.jpg")
        escapedFile.delete()
        val db = newDatabase(name)
        try {
            db.openHelper.writableDatabase
            ZipOutputStream(zip.outputStream()).use { out ->
                out.putNextEntry(ZipEntry("files/photos/../escape.jpg"))
                out.write("untrusted".toByteArray())
                out.closeEntry()
            }
            var rejected = false
            try {
                runBlocking { BackupManager(context, db).restore(Uri.fromFile(zip)) }
            } catch (_: IllegalArgumentException) {
                rejected = true
            }
            assertTrue("ZIP traversal must be rejected", rejected)
            assertFalse("No ZIP entry may escape staging", escapedFile.exists())
            assertTrue("Room must be usable after rejecting a ZIP", db.isOpen)
        } finally {
            db.close()
            context.deleteDatabase(name)
            zip.delete()
        }
    }

    @Test fun sharedAudioFocusPreemptsPreviousOwner() {
        val focus = AudioFocusController(context)
        var previousStopped = false
        val first = focus.acquire(onLost = { previousStopped = true })
        assumeTrue("Audio focus unavailable on this device", first != null)
        try {
            val next = focus.acquire(onLost = {})
            assumeTrue("Audio focus unavailable for replacement", next != null)
            try {
                assertTrue("Previous playback must stop on focus transfer", previousStopped)
            } finally {
                if (next != null) focus.release(next)
            }
        } finally {
            if (first != null) focus.release(first)
        }
    }
}
