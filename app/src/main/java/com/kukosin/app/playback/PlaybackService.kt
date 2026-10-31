package com.kukosin.app.playback

import android.util.Log
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.kukosin.app.data.ytm.YtmService
import com.kukosin.app.data.ytm.describeYtmError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import java.io.IOException

@UnstableApi
class PlaybackService : MediaSessionService() {
    private companion object {
        const val TAG = "KukosinPlayback"
    }

    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null
    private var settingsJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        setMediaNotificationProvider(DefaultMediaNotificationProvider(this))
        AudioSettingsManager.init(this)
        recreatePlayer()

        settingsJob = AudioSettingsManager.configState
            .onEach {
                recreatePlayer()
            }
            .launchIn(serviceScope)
    }

    private fun recreatePlayer() {
        val currentItems = player?.let { p ->
            val count = p.mediaItemCount
            if (count == 0) return@let null
            val list = List(count) { i -> p.getMediaItemAt(i) }
            val index = p.currentMediaItemIndex
            val pos = p.currentPosition
            val isPlaying = p.isPlaying
            Triple(list, index, pos to isPlaying)
        }

        player?.release()
        mediaSession?.release()

        val newPlayer = ExoPlayer.Builder(this)
            .setRenderersFactory(HiFiRenderersFactory(this))
            .setMediaSourceFactory(buildMediaSourceFactory())
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true,
            )
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .setSkipSilenceEnabled(false)
            .build()
        newPlayer.volume = 1f

        AudioRoute.attach(this, newPlayer)
        mediaSession = MediaSession.Builder(this, newPlayer).build()
        player = newPlayer

        if (currentItems != null && currentItems.first.isNotEmpty()) {
            val (items, index, posPlaying) = currentItems
            newPlayer.setMediaItems(items, index, posPlaying.first)
            newPlayer.prepare()
            if (posPlaying.second) {
                newPlayer.play()
            }
        }
    }

    /**
     * Fuente de datos con resolución diferida para YouTube Music.
     *
     * Los MediaItem de YTM usan el esquema [YtmService.STREAM_SCHEME]; acá se cambia esa URI
     * simbólica por la URL real de googlevideo en el momento de abrir el stream. Cualquier otra
     * URI (archivos locales, http normal) pasa intacta, así que la reproducción local no cambia.
     */
    private fun buildMediaSourceFactory(): MediaSource.Factory {
        val resolving = ResolvingDataSource.Factory(DefaultDataSource.Factory(this)) { dataSpec ->
            resolveStreamDataSpec(dataSpec)
        }
        return DefaultMediaSourceFactory(this).setDataSourceFactory(resolving)
    }

    private fun resolveStreamDataSpec(dataSpec: DataSpec): DataSpec {
        if (dataSpec.uri.scheme != YtmService.STREAM_SCHEME) return dataSpec
        val videoId = dataSpec.uri.lastPathSegment ?: return dataSpec

        val startedAt = System.currentTimeMillis()
        val audio = try {
            // El resolver es síncrono por contrato (se ejecuta en el hilo de carga de ExoPlayer).
            runBlocking { YtmService.resolveAudio(videoId) }
        } catch (throwable: Throwable) {
            Log.w(TAG, "stream $videoId falló: ${describeYtmError(throwable)}")
            throw IOException(
                "No se pudo resolver el audio de YouTube Music: ${describeYtmError(throwable)}",
                throwable,
            )
        }
        Log.d(
            TAG,
            "stream $videoId -> ${audio.codecLabel} ${audio.bitrateKbps}kbps " +
                "(${System.currentTimeMillis() - startedAt} ms)",
        )
        return dataSpec.withUri(audio.url.toUri())
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        settingsJob?.cancel()
        mediaSession?.run {
            release()
        }
        player?.release()
        mediaSession = null
        player = null
        super.onDestroy()
    }
}
