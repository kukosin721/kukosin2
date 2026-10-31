package com.kukosin.app.data.ytm

import android.util.Log
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.MediaFormat
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.exceptions.AgeRestrictedContentException
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException
import org.schabi.newpipe.extractor.exceptions.ContentNotSupportedException
import org.schabi.newpipe.extractor.exceptions.GeographicRestrictionException
import org.schabi.newpipe.extractor.exceptions.PrivateContentException
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import org.schabi.newpipe.extractor.exceptions.SignInConfirmNotBotException
import org.schabi.newpipe.extractor.exceptions.YoutubeMusicPremiumContentException
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization
import org.schabi.newpipe.extractor.search.SearchInfo
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.DeliveryMethod
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import java.io.IOException
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/** Canción de YouTube Music (metadatos, sin URL de audio resuelta todavía). */
data class YtmTrack(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "YouTube Music",
    val thumbnailUri: String = "",
    val durationMs: Long = 0L,
) {
    val watchUrl: String
        get() = "https://music.youtube.com/watch?v=$id"

    val durationLabel: String
        get() {
            if (durationMs <= 0L) return ""
            val totalSeconds = durationMs / 1000
            return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
        }
}

/** Pista de audio ya resuelta, lista para entregar a ExoPlayer. */
data class YtmAudio(
    val url: String,
    val mimeType: String,
    val codecLabel: String,
    val bitrateKbps: Int,
    val itag: Int,
) {
    val qualityLabel: String
        get() {
            val kbps = if (bitrateKbps > 0) "$bitrateKbps kbps" else "bitrate variable"
            return "$codecLabel $kbps"
        }
}

/** Error de YouTube Music con un mensaje ya listo para mostrar en la UI. */
class YtmException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Búsqueda y reproducción de YouTube Music usando NewPipeExtractor.
 *
 * Antes el proyecto usaba la API de Piped, que hoy devuelve `audioStreams` vacío porque YouTube
 * exige po_token/SABR: la búsqueda devolvía canciones pero ninguna se podía reproducir.
 * NewPipeExtractor resuelve las URLs reales de audio en el propio dispositivo.
 */
object YtmService {
    private const val TAG = "KukosinYtm"
    private const val SEARCH_LIMIT = 40
    private const val STREAM_CACHE_TTL_MS = 90 * 60 * 1000L

    /** Filtro de búsqueda de YouTube Music: solo canciones. */
    private const val MUSIC_SONGS_FILTER = "music_songs"

    /** `=w226-h226` / `=s176` en las URLs de lh3.googleusercontent.com. */
    private val THUMBNAIL_SIZE = Regex("=w\\d+-h\\d+")
    private val THUMBNAIL_SQUARE = Regex("=s\\d+")

    /** Esquema de las URIs "simbólicas" de YTM: el audio real se resuelve al abrir el stream. */
    const val STREAM_SCHEME = "kukosin-ytm"

    // Contrato de extras de MediaMetadata para audio remoto (lo lee PlayerApp).
    const val EXTRA_STREAM_VIDEO_ID = "com.kukosin.app.stream.VIDEO_ID"
    const val EXTRA_STREAM_THUMBNAIL = "com.kukosin.app.stream.THUMBNAIL"

    private val initMutex = Mutex()

    @Volatile
    private var initialized = false

    private data class CachedAudio(val audio: YtmAudio, val expiresAt: Long)

    private val audioCache = ConcurrentHashMap<String, CachedAudio>()

    /**
     * Busca canciones en YouTube Music. Con una consulta vacía devuelve una lista vacía:
     * no se inventan resultados de ejemplo.
     */
    suspend fun search(query: String): List<YtmTrack> = withContext(Dispatchers.IO) {
        val needle = query.trim()
        if (needle.isEmpty()) return@withContext emptyList()
        ensureInitialized()

        val startedAt = System.currentTimeMillis()
        runCatching {
            // Ojo: SearchInfo.getInfo(extractor) NO llama a fetchPage() y devuelve una lista
            // vacía con NPEs silenciosos. Este overload sí hace el fetch antes de parsear.
            val queryHandler = ServiceList.YouTube.searchQHFactory.fromQuery(
                needle,
                listOf(MUSIC_SONGS_FILTER),
                "",
            )
            val info = SearchInfo.getInfo(ServiceList.YouTube, queryHandler)
            info.relatedItems
                .asSequence()
                .filterIsInstance<StreamInfoItem>()
                .mapNotNull { it.toYtmTrack() }
                .distinctBy { it.id }
                .take(SEARCH_LIMIT)
                .toList()
        }.onSuccess { results ->
            Log.d(TAG, "search \"$needle\": ${results.size} resultados en ${System.currentTimeMillis() - startedAt} ms")
        }.onFailure { throwable ->
            Log.w(TAG, "search \"$needle\" falló: ${throwable::class.java.simpleName}: ${throwable.message}")
        }.getOrElse { throwable ->
            throw YtmException(describeYtmError(throwable), throwable)
        }
    }

    /**
     * Resuelve la URL de audio reproducible de una canción.
     * Las URLs de googlevideo caducan, por eso se cachean solo [STREAM_CACHE_TTL_MS].
     */
    suspend fun resolveAudio(track: YtmTrack): YtmAudio =
        resolveAudio(track.id, track.watchUrl)

    suspend fun resolveAudio(
        videoId: String,
        watchUrl: String = "https://music.youtube.com/watch?v=$videoId",
    ): YtmAudio = withContext(Dispatchers.IO) {
        audioCache[videoId]?.let { cached ->
            if (cached.expiresAt > System.currentTimeMillis()) {
                Log.d(TAG, "resolveAudio($videoId): URL desde caché")
                return@withContext cached.audio
            }
            audioCache.remove(videoId)
        }

        ensureInitialized()

        val startedAt = System.currentTimeMillis()
        val streamInfo = runCatching {
            StreamInfo.getInfo(ServiceList.YouTube, watchUrl)
        }.recoverCatching {
            StreamInfo.getInfo(ServiceList.YouTube, "https://www.youtube.com/watch?v=$videoId")
        }.getOrElse { throwable ->
            throw YtmException(describeYtmError(throwable), throwable)
        }

        val stream = pickBestAudio(streamInfo.audioStreams)
            ?: throw YtmException(
                "YouTube no entregó ninguna pista de audio para esta canción. " +
                    "Puede ser contenido con restricción o la extracción quedó desactualizada " +
                    "(actualiza NewPipeExtractor en gradle/libs.versions.toml).",
            )

        val audio = YtmAudio(
            url = stream.content,
            mimeType = stream.format?.mimeType ?: "audio/mp4",
            codecLabel = stream.codecLabel(),
            bitrateKbps = stream.bitrateKbps(),
            itag = stream.itag,
        )

        Log.d(
            TAG,
            "resolveAudio($videoId): ${audio.codecLabel} ${audio.bitrateKbps}kbps " +
                "itag=${audio.itag} en ${System.currentTimeMillis() - startedAt} ms",
        )
        audioCache[videoId] = CachedAudio(audio, System.currentTimeMillis() + STREAM_CACHE_TTL_MS)
        trimCache()
        audio
    }

    /**
     * Empaqueta la canción como [MediaItem] **sin resolver** el audio todavía.
     *
     * La URI apunta al esquema [STREAM_SCHEME]; [com.kukosin.app.playback.PlaybackService] la
     * resuelve a la URL real de googlevideo justo antes de abrir el stream. Así se puede
     * encolar toda la búsqueda sin hacer 40 peticiones de red al tocar play.
     */
    fun toMediaItem(track: YtmTrack): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist)
            .setAlbumTitle(track.album)
            .setArtworkUri(track.thumbnailUri.takeIf { it.isNotBlank() }?.toUri())
            .setIsBrowsable(false)
            .setIsPlayable(true)
            .setExtras(
                bundleOf(
                    EXTRA_STREAM_VIDEO_ID to track.id,
                    EXTRA_STREAM_THUMBNAIL to track.thumbnailUri,
                ),
            )
            .build()

        return MediaItem.Builder()
            .setMediaId("ytm_${track.id}")
            .setUri("$STREAM_SCHEME://stream/${track.id}".toUri())
            .setMediaMetadata(metadata)
            .build()
    }

    fun clearCache() = audioCache.clear()

    //region Audio selection

    private fun pickBestAudio(streams: List<AudioStream>): AudioStream? {
        val playable = streams.filter { it.isUrl && it.content.isNotBlank() }
        if (playable.isEmpty()) return null

        // Los streams de audio de YouTube suelen venir como PROGRESSIVE_HTTP (URL directa).
        // Si solo hubiera manifiestos DASH/HLS no se pueden reproducir sin los módulos
        // media3-exoplayer-dash / -hls, así que se prefieren las URLs directas.
        val direct = playable.filter {
            it.deliveryMethod != DeliveryMethod.DASH && it.deliveryMethod != DeliveryMethod.HLS
        }
        val candidates = direct.ifEmpty { playable }

        return candidates.sortedWith(
            compareByDescending<AudioStream> { it.bitrateKbps() }
                .thenByDescending { it.format?.codecRank() ?: 0 },
        ).firstOrNull()
    }

    private fun AudioStream.bitrateKbps(): Int {
        val raw = if (averageBitrate > 0) averageBitrate else bitrate
        if (raw <= 0) return 0
        // NewPipe entrega kbps en los itag, pero algunos extractores devuelven bps.
        return if (raw > 10_000) raw / 1000 else raw
    }

    private fun AudioStream.codecLabel(): String = when (format) {
        MediaFormat.WEBMA_OPUS -> "Opus"
        MediaFormat.M4A -> "AAC"
        MediaFormat.WEBMA -> "Vorbis"
        MediaFormat.MP3 -> "MP3"
        else -> "AUDIO"
    }

    //endregion

    //region Helpers

    /**
     * El extractor devuelve la miniatura con el tamaño incrustado en la URL (`=w226-h226`),
     * pero Google sirve la imagen original si se le pide más grande. Con esto la carátula del
     * reproductor deja de verse pixelada.
     */
    private fun upscaleThumbnailUrl(url: String): String {
        if (url.isBlank()) return url
        var out = THUMBNAIL_SIZE.replace(url, "=w1080-h1080")
        out = THUMBNAIL_SQUARE.replace(out, "=s1080")
        return out
    }

    private fun StreamInfoItem.toYtmTrack(): YtmTrack? {
        val videoId = extractVideoId(url) ?: return null
        val thumbnail = thumbnails
            .maxByOrNull { it.height }
            ?.url
            ?: thumbnails.firstOrNull()?.url
            ?: ""

        return YtmTrack(
            id = videoId,
            title = name.ifBlank { "Canción de YouTube Music" },
            artist = uploaderName.ifBlank { "Artista desconocido" },
            thumbnailUri = upscaleThumbnailUrl(thumbnail),
            durationMs = duration.coerceAtLeast(0L) * 1000L,
        )
    }

    private val videoIdRegex = Regex("[?&]v=([A-Za-z0-9_-]{11})")

    private fun extractVideoId(url: String?): String? {
        if (url.isNullOrBlank()) return null
        videoIdRegex.find(url)?.let { return it.groupValues[1] }
        val lastSegment = url.substringAfterLast('/').substringBefore('?')
        return lastSegment.takeIf { it.length == 11 && it.all { c -> c.isLetterOrDigit() || c == '_' || c == '-' } }
    }

    private suspend fun ensureInitialized() {
        if (initialized) return
        initMutex.withLock {
            if (initialized) return@withLock
            val locale = Locale.getDefault()
            val language = locale.language.takeIf { it.isNotBlank() } ?: "es"
            val country = locale.country.takeIf { it.isNotBlank() } ?: "CL"
            NewPipe.init(
                NewPipeDownloader(),
                Localization(language, country),
                ContentCountry(country),
            )
            initialized = true
        }
    }

    private fun trimCache() {
        if (audioCache.size <= 24) return
        val now = System.currentTimeMillis()
        audioCache.entries.removeAll { it.value.expiresAt <= now }
        if (audioCache.size > 24) {
            audioCache.keys.take(audioCache.size - 24).forEach { audioCache.remove(it) }
        }
    }

    private fun MediaFormat.codecRank(): Int = when (this) {
        MediaFormat.WEBMA_OPUS -> 4
        MediaFormat.M4A -> 3
        MediaFormat.WEBMA -> 2
        MediaFormat.MP3 -> 1
        else -> 0
    }

    //endregion
}

/** Traduce las excepciones de NewPipeExtractor a algo que el usuario pueda entender. */
fun describeYtmError(error: Throwable): String = when (error) {
    is YtmException -> error.message ?: "No se pudo completar la operación en YouTube Music."
    is SignInConfirmNotBotException, is ReCaptchaException ->
        "YouTube pidió una verificación anti-bot. Espera un minuto y vuelve a intentar."
    is AgeRestrictedContentException ->
        "Ese contenido tiene restricción de edad y YouTube no lo entrega sin una cuenta."
    is YoutubeMusicPremiumContentException ->
        "Es contenido exclusivo de YouTube Music Premium."
    is GeographicRestrictionException ->
        "El contenido está bloqueado en tu región."
    is PrivateContentException ->
        "El contenido es privado."
    is ContentNotSupportedException ->
        "Formato de contenido no soportado por el extractor."
    is ContentNotAvailableException ->
        "YouTube no tiene ese contenido disponible."
    is IOException ->
        "Sin conexión con YouTube. Revisa tu red e intenta de nuevo."
    else ->
        "Error de YouTube Music: ${error.message ?: error::class.java.simpleName}"
}
