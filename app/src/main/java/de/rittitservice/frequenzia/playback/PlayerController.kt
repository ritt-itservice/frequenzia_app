package de.rittitservice.frequenzia.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import de.rittitservice.frequenzia.data.RadioStation

/**
 * Kapselt die Verbindung zum PlaybackService, damit die Compose-UI nicht
 * direkt mit ExoPlayer/MediaSession-APIs hantieren muss.
 */
class PlayerController(private val context: Context) {

    private var controller: MediaController? = null

    fun connect(onReady: (MediaController) -> Unit) {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                controller = controllerFuture.get()
                controller?.let(onReady)
            },
            MoreExecutors.directExecutor()
        )
    }

    fun playStation(station: RadioStation) {
        val mediaItem = MediaItem.Builder()
            .setUri(station.url_resolved)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(station.name)
                    .setArtist(station.country ?: station.countrycode)
                    .setArtworkUri(station.favicon?.let { android.net.Uri.parse(it) })
                    .build()
            )
            .build()

        controller?.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }
    }

    fun togglePlayPause() {
        controller?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun release() {
        controller?.release()
        controller = null
    }
}
