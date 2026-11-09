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
    private val sampleMp3 = ByteArray(4096).apply {
        this[0] = 'I'.code.toByte()
        this[1] = 'D'.code.toByte()
        this[2] = '3'.code.toByte()
    }

    @Before
    fun setup() {
        app = RuntimeEnvironment.getApplication()
        audioDir = File(app.filesDir, "quran_recitations/ar.alafasy")
        audioDir.deleteRecursively()
        File(app.filesDir, "quran_recitations/ar.sudais").deleteRecursively()
    }

    @After
    fun cleanup() {
        audioDir.deleteRecursively()
        File(app.filesDir, "quran_recitations/ar.sudais").deleteRecursively()
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
        val defaultCache = QuranAudioCache(app, QuranReciter.ALAFASY) { url ->
            FakeConnection(url, sampleMp3)
        }
        val sudaisCache = QuranAudioCache(app, QuranReciter.SUDAIS) { url ->
            assertTrue(url.toString().contains("Abdurrahmaan_As-Sudais_192kbps/108001.mp3"))
            FakeConnection(url, sampleMp3)
        }
        val alafasy = defaultCache.obtain(6205)
        val sudais = sudaisCache.obtain(6205, 108, 1)

        assertTrue(alafasy.absolutePath.contains("/ar.alafasy/"))
        assertTrue(sudais.absolutePath.contains("/ar.sudais/"))
        assertTrue(alafasy.absolutePath != sudais.absolutePath)
        assertNotNull(defaultCache.cachedFile(6205))
        assertNotNull(sudaisCache.cachedFile(6205))

        val offline = QuranAudioCache(app, QuranReciter.SUDAIS) {
            throw IOException("No network")
        }
        assertEquals(sudais.absolutePath, offline.obtain(6205, 108, 1).absolutePath)
    }

    @Test
    fun sudaisFallsBackToMirrorAfterPrimaryRejectsAndCachesLocally() = runBlocking {
        val attempted = mutableListOf<String>()
        val cache = QuranAudioCache(app, QuranReciter.SUDAIS) { url ->
            attempted.add(url.toString())
            object : FakeConnection(url, sampleMp3) {
                override fun getResponseCode(): Int =
                    if (url.host == "everyayah.com") HTTP_FORBIDDEN else HTTP_OK
            }
        }

        val audio = cache.obtain(1, 1, 1)
        assertTrue(audio.isFile)
        assertEquals(2, attempted.size)
        assertTrue(attempted[0].startsWith("https://everyayah.com/"))
        assertTrue(attempted[1].startsWith("https://verse.mp3quran.net/"))
        assertArrayEquals(sampleMp3, audio.readBytes())

        val offline = QuranAudioCache(app, QuranReciter.SUDAIS) {
            throw IOException("Disconnected")
        }
        assertEquals(audio.absolutePath, offline.obtain(1, 1, 1).absolutePath)
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
