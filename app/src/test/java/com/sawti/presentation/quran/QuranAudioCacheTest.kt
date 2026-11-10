package com.sawti.presentation.quran

import android.app.Application
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class QuranAudioCacheTest {
    private lateinit var app: Application
    private lateinit var audioDir: File
    private val sampleMp3 = ByteArray(417 * 4).apply {
        // Four complete MPEG-1 Layer III frames (128 kbps, 44.1 kHz).
        for (offset in indices step 417) {
            this[offset] = 0xFF.toByte()
            this[offset + 1] = 0xFB.toByte()
            this[offset + 2] = 0x90.toByte()
        }
    }

    @Before
    fun setup() {
        app = RuntimeEnvironment.getApplication()
        audioDir = File(app.filesDir, "quran_recitations/ar.alafasy")
        audioDir.deleteRecursively()
        File(app.filesDir, "quran_recitations/salah.bukhatir").deleteRecursively()
        File(app.filesDir, "quran_recitations/husary.muallim").deleteRecursively()
    }

    @After
    fun cleanup() {
        audioDir.deleteRecursively()
        File(app.filesDir, "quran_recitations/salah.bukhatir").deleteRecursively()
        File(app.filesDir, "quran_recitations/husary.muallim").deleteRecursively()
    }

    @Test
    fun firstDownloadPersistsAndOfflinePlaybackReusesSameFile() = runBlocking {
        var calls = 0
        val downloader = QuranAudioCache(app) { url ->
            calls += 1
            FakeConnection(url, sampleMp3)
        }
        val first = downloader.obtain(6205)
        assertTrue(first.isFile)
        assertArrayEquals(sampleMp3, first.readBytes())
        assertEquals(1, calls)

        // Simulate a cold app relaunch without any Internet connection.
        val offline = QuranAudioCache(app) { throw IOException("No network") }
        assertEquals(first.absolutePath, offline.obtain(6205).absolutePath)
        assertNotNull(offline.cachedFile(6205))
        assertEquals(1, calls)
    }

    @Test
    fun invalidHttpBodyIsNeverSavedAsAyah() {
        val html = ByteArray(2048) { 'X'.code.toByte() }
        val cache = QuranAudioCache(app) { url -> FakeConnection(url, html) }
        assertThrows(IOException::class.java) { runBlocking { cache.obtain(1) } }
        assertNull(cache.cachedFile(1))
        assertTrue(audioDir.listFiles()?.none { it.extension == "part" } ?: true)
    }

    @Test
    fun incompleteNetworkResponseIsRemovedAndRetryWorks() = runBlocking {
        val cache = QuranAudioCache(app) { url ->
            object : FakeConnection(url, sampleMp3) {
                override fun getInputStream(): InputStream = object : ByteArrayInputStream(sampleMp3) {
                    var firstRead = true
                    override fun read(b: ByteArray, off: Int, len: Int): Int {
                        if (!firstRead) throw IOException("Disconnected")
                        firstRead = false
                        return super.read(b, off, len)
                    }
                }
            }
        }
        assertThrows(IOException::class.java) { runBlocking { cache.obtain(2) } }
        assertNull(cache.cachedFile(2))
        assertFalse(audioDir.listFiles()?.any { it.extension == "part" } ?: false)
        val retry = QuranAudioCache(app) { url -> FakeConnection(url, sampleMp3) }
        assertTrue(retry.obtain(2).isFile)
    }

    @Test
    fun eachReciterUsesSeparatePersistentFiles() = runBlocking {
        val alafasyCache = QuranAudioCache(app, QuranReciter.ALAFASY) { url ->
            FakeConnection(url, sampleMp3)
        }
        val bukhatirCache = QuranAudioCache(app, QuranReciter.SALAH_BUKHATIR) { url ->
            assertTrue(url.toString().contains("Salaah_AbdulRahman_Bukhatir_128kbps/108001.mp3"))
            FakeConnection(url, sampleMp3)
        }
        val husaryCache = QuranAudioCache(app, QuranReciter.HUSARY_MUALLIM) { url ->
            assertTrue(url.toString().contains("Husary_Muallim_128kbps/108001.mp3"))
            FakeConnection(url, sampleMp3)
        }
        val alafasy = alafasyCache.obtain(6205)
        val bukhatir = bukhatirCache.obtain(6205, 108, 1)
        val husary = husaryCache.obtain(6205, 108, 1)

        assertTrue(alafasy.absolutePath.contains("/ar.alafasy/"))
        assertTrue(bukhatir.absolutePath.contains("/salah.bukhatir/"))
        assertTrue(husary.absolutePath.contains("/husary.muallim/"))
        assertTrue(setOf(alafasy.absolutePath, bukhatir.absolutePath, husary.absolutePath).size == 3)

        val offline = QuranAudioCache(app, QuranReciter.HUSARY_MUALLIM) {
            throw IOException("Offline")
        }
        assertEquals(husary.absolutePath, offline.obtain(6205, 108, 1).absolutePath)
    }

    @Test
    fun bukhatirFallsBackToVerifiedMirrorAfterPrimaryRefuses() = runBlocking {
        val requestedUrls = mutableListOf<String>()
        val cache = QuranAudioCache(app, QuranReciter.SALAH_BUKHATIR) { url ->
            requestedUrls.add(url.toString())
            object : FakeConnection(url, sampleMp3) {
                override fun getResponseCode(): Int =
                    if (url.host == "everyayah.com") HTTP_FORBIDDEN else HTTP_OK
            }
        }
        val file = cache.obtain(1, 1, 1)
        assertTrue(file.isFile)
        assertEquals(2, requestedUrls.size)
        assertTrue(requestedUrls[0].startsWith("https://everyayah.com/"))
        assertTrue(requestedUrls[1].contains("maqra-project/salah-bukhatir-128kbps"))
        assertArrayEquals(sampleMp3, file.readBytes())
    }

    @Test
    fun id3TagWithoutAudioFramesIsRejected() {
        val body = ByteArray(4096).apply {
            this[0] = 73; this[1] = 68; this[2] = 51; this[3] = 4
        }
        val cache = QuranAudioCache(app) { url -> FakeConnection(url, body) }
        assertThrows(IOException::class.java) { runBlocking { cache.obtain(1) } }
        assertNull(cache.cachedFile(1))
    }

    @Test
    fun cleanEarlyEofDoesNotPublishTruncatedResponse() {
        val cache = QuranAudioCache(app) { url ->
            object : FakeConnection(url, sampleMp3) {
                override fun getContentLengthLong() = sampleMp3.size.toLong() + 500
            }
        }
        assertThrows(IOException::class.java) { runBlocking { cache.obtain(1) } }
        assertNull(cache.cachedFile(1))
        assertTrue(audioDir.listFiles()?.none { it.extension == "part" } ?: true)
    }

    @Test
    fun brokenOldCacheIsReplacedAndDecoderInvalidationAllowsRetry() = runBlocking {
        audioDir.mkdirs()
        File(audioDir, "ayah_1.mp3").writeBytes(ByteArray(2048).apply {
            this[0] = 73; this[1] = 68; this[2] = 51
        })
        var calls = 0
        val cache = QuranAudioCache(app) { url -> calls++; FakeConnection(url, sampleMp3) }
        assertNull(cache.cachedFile(1))
        cache.obtain(1)
        assertEquals(1, calls)
        cache.invalidate(1)
        assertNull(cache.cachedFile(1))
        cache.obtain(1)
        assertEquals(2, calls)
    }

    private open class FakeConnection(url: URL, private val audio: ByteArray) :
        HttpURLConnection(url) {
        override fun disconnect() = Unit
        override fun usingProxy() = false
        override fun connect() = Unit
        override fun getResponseCode() = HTTP_OK
        override fun getContentLengthLong(): Long = audio.size.toLong()
        override fun getInputStream(): InputStream = ByteArrayInputStream(audio)
    }
}
