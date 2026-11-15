package com.sawti.presentation.quran

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import com.sawti.MainActivity

/** Main-thread bridge: the existing ayah player remains the single playback owner. */
internal data class QuranPlaybackSnapshot(
    val title: String = "القرآن الكريم",
    val phase: RecitationPhase = RecitationPhase.IDLE,
    val positionMs: Int = 0,
)

internal object QuranPlaybackBridge {
    private var owner: Any? = null
    private var playAction: (() -> Unit)? = null
    private var pauseAction: (() -> Unit)? = null
    private var stopAction: (() -> Unit)? = null
    var snapshot = QuranPlaybackSnapshot()
        private set
    var service: QuranPlaybackService? = null

    fun attach(owner: Any, onPlay: () -> Unit, onPause: () -> Unit, onStop: () -> Unit) {
        this.owner = owner
        playAction = onPlay
        pauseAction = onPause
        stopAction = onStop
    }

    fun detach(owner: Any) {
        if (this.owner !== owner) return
        this.owner = null
        playAction = null
        pauseAction = null
        stopAction = null
        snapshot = QuranPlaybackSnapshot()
        service?.endPlayback()
    }

    fun play() { playAction?.invoke() }
    fun pause() { pauseAction?.invoke() }
    fun stop() { stopAction?.invoke() }

    fun publish(status: RecitationStatus, title: String) {
        val previous = snapshot
        snapshot = QuranPlaybackSnapshot(title, status.phase, status.positionMs)
        val running = service ?: return
        if (status.phase == RecitationPhase.IDLE || status.phase == RecitationPhase.ERROR) {
            running.endPlayback()
        } else if (previous.title != snapshot.title || previous.phase != snapshot.phase ||
            previous.positionMs / 1000 != snapshot.positionMs / 1000) {
            running.render(snapshot)
        }
    }

    /** Called synchronously from the Quran play button, never from a background callback. */
    fun start(context: Context) {
        if (snapshot.phase == RecitationPhase.IDLE || snapshot.phase == RecitationPhase.ERROR) return
        runCatching {
            context.startForegroundService(Intent(context, QuranPlaybackService::class.java))
        }.onFailure { Log.w("SawtiQuran", "Could not start Quran media playback service", it) }
    }
}

/**
 * A non-exported media-playback foreground service. Notification permission is optional
 * for foreground media playback on Android 13+; the service stays usable when denied.
 */
class QuranPlaybackService : Service() {
    private lateinit var session: MediaSession
    private lateinit var notifications: NotificationManager
    private var inForeground = false

    override fun onCreate() {
        super.onCreate()
        notifications = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifications.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "تلاوة القرآن الكريم", NotificationManager.IMPORTANCE_LOW)
        )
        session = MediaSession(this, "Sawti Quran")
        session.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or
            MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS)
        session.setCallback(object : MediaSession.Callback() {
            override fun onPlay() { QuranPlaybackBridge.play() }
            override fun onPause() { QuranPlaybackBridge.pause() }
            override fun onStop() { QuranPlaybackBridge.stop() }
        })
        session.isActive = true
        QuranPlaybackBridge.service = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> { QuranPlaybackBridge.play(); return START_NOT_STICKY }
            ACTION_PAUSE -> { QuranPlaybackBridge.pause(); return START_NOT_STICKY }
            ACTION_STOP -> { QuranPlaybackBridge.stop(); return START_NOT_STICKY }
        }
        val current = QuranPlaybackBridge.snapshot
        if (current.phase == RecitationPhase.IDLE || current.phase == RecitationPhase.ERROR) {
            endPlayback()
            return START_NOT_STICKY
        }
        updateMediaSession(current)
        startForeground(NOTIFICATION_ID, notification(current))
        inForeground = true
        return START_NOT_STICKY
    }

    fun render(current: QuranPlaybackSnapshot) {
        if (!inForeground) return
        updateMediaSession(current)
        runCatching { notifications.notify(NOTIFICATION_ID, notification(current)) }
            .onFailure { Log.w("SawtiQuran", "Could not refresh media notification", it) }
    }

    private fun updateMediaSession(current: QuranPlaybackSnapshot) {
        session.setMetadata(MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE, current.title)
            .putString(MediaMetadata.METADATA_KEY_ARTIST, "القرآن الكريم")
            .build())
        val state = when (current.phase) {
            RecitationPhase.PLAYING -> PlaybackState.STATE_PLAYING
            RecitationPhase.PAUSED -> PlaybackState.STATE_PAUSED
            RecitationPhase.BUFFERING -> PlaybackState.STATE_BUFFERING
            else -> PlaybackState.STATE_STOPPED
        }
        session.setPlaybackState(PlaybackState.Builder()
            .setActions(PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PAUSE or
                PlaybackState.ACTION_PLAY_PAUSE or PlaybackState.ACTION_STOP)
            .setState(state, current.positionMs.toLong(),
                if (current.phase == RecitationPhase.PLAYING) 1f else 0f,
                SystemClock.elapsedRealtime())
            .build())
    }

    private fun notification(current: QuranPlaybackSnapshot): Notification {
        val launch = PendingIntent.getActivity(this, 0,
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val isPaused = current.phase == RecitationPhase.PAUSED
        val toggleAction = if (isPaused) ACTION_PLAY else ACTION_PAUSE
        val toggleIcon = if (isPaused) android.R.drawable.ic_media_play
            else android.R.drawable.ic_media_pause
        val toggleLabel = if (isPaused) "تشغيل" else "إيقاف مؤقت"
        val toggle = PendingIntent.getService(this, 1,
            Intent(this, QuranPlaybackService::class.java).setAction(toggleAction),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val stop = PendingIntent.getService(this, 2,
            Intent(this, QuranPlaybackService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(current.title)
            .setContentText("تلاوة القرآن الكريم")
            .setContentIntent(launch)
            .setCategory(Notification.CATEGORY_TRANSPORT)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setShowWhen(false)
            .addAction(Notification.Action.Builder(toggleIcon, toggleLabel, toggle).build())
            .addAction(Notification.Action.Builder(
                android.R.drawable.ic_menu_close_clear_cancel, "إيقاف", stop).build())
            .setStyle(Notification.MediaStyle().setMediaSession(session.sessionToken)
                .setShowActionsInCompactView(0, 1))
            .build()
    }

    fun endPlayback() {
        if (inForeground) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            inForeground = false
        }
        stopSelf()
    }

    override fun onDestroy() {
        if (QuranPlaybackBridge.service === this) QuranPlaybackBridge.service = null
        session.isActive = false
        session.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "sawti_quran_playback"
        private const val NOTIFICATION_ID = 5401
        private const val ACTION_PLAY = "com.sawti.quran.PLAY"
        private const val ACTION_PAUSE = "com.sawti.quran.PAUSE"
        private const val ACTION_STOP = "com.sawti.quran.STOP"
    }
}
