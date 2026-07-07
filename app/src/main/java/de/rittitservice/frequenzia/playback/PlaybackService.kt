package de.rittitservice.frequenzia.playback

import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * Läuft als Foreground-Service weiter, auch wenn die App im Hintergrund ist
 * oder der Bildschirm gesperrt wird. Steuerung erfolgt über die System-
 * Benachrichtigung und den Lockscreen (via MediaSession).
 */
class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    override fun onCreate() {
        super.onCreate()

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus= */ true)
            .setHandleAudioBecomingNoisy(true) // pausiert bei Kopfhörer-Trennung
            .build()

        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    // Stoppt die Wiedergabe immer, sobald die App aus den "Zuletzt verwendet"
    // weggewischt wird – auch wenn gerade ein Sender läuft. Das ist bewusst
    // anders als das übliche Musik-App-Verhalten (dort läuft es meist weiter).
    override fun onTaskRemoved(rootIntent: android.content.Intent?) {
        mediaSession?.player?.apply {
            stop()
            clearMediaItems()
        }
        stopSelf()
    }
}
