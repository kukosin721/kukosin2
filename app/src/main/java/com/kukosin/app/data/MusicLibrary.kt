package com.kukosin.app.data

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import io.github.anilbeesetti.nextlib.mediainfo.MediaInfoBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object MusicLibrary {
    private val audioExtensions = listOf(
        "flac", "wav", "aiff", "aif", "alac", "m4a", "aac", "mp3",
        "ogg", "oga", "opus", "dsf", "dff", "ape", "wv", "tta", "tak",
        "mka", "mlp", "thd", "dts", "ac3", "eac3", "wma",
    )

    suspend fun scan(context: Context): List<Track> = withContext(Dispatchers.IO) {
        val byKey = LinkedHashMap<String, Track>()

        fun addTrack(track: Track) {
            val key = when {
                track.filePath.isNotBlank() -> track.filePath.lowercase()
                else -> "${track.title.lowercase()}_${track.artist.lowercase()}_${track.durationMs}"
            }
            byKey.putIfAbsent(key, track)
        }

        queryAudio(context).forEach { addTrack(it) }
        queryFiles(context).forEach { addTrack(it) }

        byKey.values.sortedBy { it.title.lowercase() }
    }

    suspend fun enrich(context: Context, tracks: List<Track>): List<Track> = withContext(Dispatchers.IO) {
        tracks.map { track ->
            runCatching { probe(context, track) }.getOrDefault(track)
        }
    }

    fun buildFolderTree(tracks: List<Track>): FolderNode {
        val rootPath = "/storage/emulated/0"
        val rootNode = FolderNode(name = "Almacenamiento interno", path = rootPath)
        val folderMap = mutableMapOf<String, MutableList<Track>>()

        tracks.forEach { track ->
            val file = if (track.filePath.isNotBlank()) File(track.filePath) else null
            val folderPath = file?.parent ?: "Desconocido"
            folderMap.getOrPut(folderPath) { mutableListOf() }.add(track)
        }

        fun buildNode(currentPath: String): FolderNode {
            val name = File(currentPath).name.ifBlank { currentPath }
            val directTracks = folderMap[currentPath] ?: emptyList()
            
            val childPaths = folderMap.keys
                .filter { path ->
                    path != currentPath && path.startsWith(currentPath) &&
                        File(path).parent == currentPath
                }
                .sorted()

            val childNodes = childPaths.map { buildNode(it) }
            return FolderNode(
                name = name,
                path = currentPath,
                subfolders = childNodes,
                tracks = directTracks.sortedBy { it.trackNumber.takeIf { t -> t > 0 } ?: Int.MAX_VALUE },
            )
        }

        val topLevelPaths = folderMap.keys
            .filter { path ->
                path.startsWith(rootPath) && folderMap.keys.none { parent -> parent != path && path.startsWith("$parent/") }
            }
            .sorted()

        val rootChildren = if (topLevelPaths.isNotEmpty()) {
            topLevelPaths.map { buildNode(it) }
        } else {
            folderMap.map { (path, tList) ->
                FolderNode(
                    name = File(path).name.ifBlank { "Música" },
                    path = path,
                    tracks = tList.sortedBy { it.title.lowercase() },
                )
            }
        }

        return rootNode.copy(subfolders = rootChildren)
    }

    fun groupAlbums(tracks: List<Track>): List<AlbumGroup> {
        return tracks.groupBy { it.album.ifBlank { "Álbum desconocido" } }
            .map { (albumName, albumTracks) ->
                val first = albumTracks.first()
                val sortedTracks = albumTracks.sortedWith(
                    compareBy<Track> { it.discNumber.takeIf { d -> d > 0 } ?: 1 }
                        .thenBy { it.trackNumber.takeIf { t -> t > 0 } ?: Int.MAX_VALUE }
                        .thenBy { it.title.lowercase() },
                )
                AlbumGroup(
                    name = albumName,
                    artist = first.effectiveAlbumArtist,
                    albumId = first.albumId,
                    tracks = sortedTracks,
                    year = albumTracks.map { it.year }.firstOrNull { it > 0 } ?: 0,
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    fun groupArtists(tracks: List<Track>): List<ArtistGroup> {
        return tracks.groupBy { it.effectiveArtist }
            .map { (artistName, artistTracks) ->
                val albums = groupAlbums(artistTracks)
                ArtistGroup(
                    name = artistName,
                    albums = albums,
                    tracks = artistTracks.sortedBy { it.title.lowercase() },
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    private fun queryAudio(context: Context): List<Track> {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        val projections = listOf(
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.COMPOSER,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.SIZE,
            ),
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.MIME_TYPE,
            ),
        )
        return queryWithFallback(context, collection, projections) { cursor ->
            readTrack(
                cursor = cursor,
                collection = collection,
                idColumn = MediaStore.Audio.Media._ID,
            )
        }
    }

    private fun queryFiles(context: Context): List<Track> {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri("external")
        }
        val likeClauses = audioExtensions.joinToString(" OR ") {
            "${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ?"
        }
        val args = audioExtensions.map { "%.$it" }.toTypedArray()
        val projections = listOf(
            arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.DURATION,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.SIZE,
            ),
            arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.MIME_TYPE,
            ),
        )
        return queryWithFallback(
            context = context,
            collection = collection,
            projections = projections,
            selection = likeClauses,
            selectionArgs = args,
        ) { cursor ->
            readTrack(
                cursor = cursor,
                collection = collection,
                idColumn = MediaStore.Files.FileColumns._ID,
            )
        }
    }

    private fun queryWithFallback(
        context: Context,
        collection: Uri,
        projections: List<Array<String>>,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        mapper: (Cursor) -> Track?,
    ): List<Track> {
        var lastError: Exception? = null
        for (projection in projections) {
            try {
                context.contentResolver.query(
                    collection,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                )?.use { cursor ->
                    val tracks = ArrayList<Track>(cursor.count.coerceAtLeast(0))
                    while (cursor.moveToNext()) {
                        mapper(cursor)?.let(tracks::add)
                    }
                    return tracks
                }
            } catch (error: SecurityException) {
                return emptyList()
            } catch (error: IllegalArgumentException) {
                lastError = error
            } catch (error: UnsupportedOperationException) {
                lastError = error
            }
        }
        lastError?.printStackTrace()
        return emptyList()
    }

    private fun readTrack(cursor: Cursor, collection: Uri, idColumn: String): Track? {
        val id = cursor.longOrNull(idColumn) ?: return null
        val fileName = cursor.stringOrEmpty(MediaStore.MediaColumns.DISPLAY_NAME)
        val mime = cursor.stringOrEmpty(MediaStore.MediaColumns.MIME_TYPE)
        val duration = cursor.longOrNull(MediaStore.MediaColumns.DURATION) ?: 0L
        if (!isMusic(fileName, mime, duration)) return null
        
        val title = cursor.stringOrEmpty(MediaStore.MediaColumns.TITLE)
            .ifBlank { fileName.substringBeforeLast('.').ifBlank { "Sin título" } }
            
        val rawTrack = cursor.intOrZero(MediaStore.Audio.Media.TRACK)
        val trackNum = if (rawTrack >= 1000) rawTrack % 1000 else rawTrack
        val discNum = if (rawTrack >= 1000) rawTrack / 1000 else 0

        val filePath = cursor.stringOrEmpty(MediaStore.MediaColumns.DATA)
        val fileSize = cursor.longOrNull(MediaStore.MediaColumns.SIZE) ?: File(filePath).takeIf { it.exists() }?.length() ?: 0L

        return Track(
            id = id,
            uri = ContentUris.withAppendedId(collection, id),
            title = title,
            artist = cursor.stringOrEmpty(MediaStore.Audio.Media.ARTIST)
                .takeIf { it.isNotBlank() && it != "<unknown>" }
                ?: "Artista desconocido",
            album = cursor.stringOrEmpty(MediaStore.Audio.Media.ALBUM)
                .ifBlank { "Álbum desconocido" },
            albumArtist = cursor.stringOrEmpty("album_artist"),
            durationMs = duration,
            albumId = cursor.longOrNull(MediaStore.Audio.Media.ALBUM_ID) ?: 0L,
            bitrate = cursor.intOrZero(MediaStore.Audio.Media.BITRATE),
            sampleRate = 0,
            bitDepth = 0,
            channels = 0,
            mimeType = mime,
            fileName = fileName,
            year = cursor.intOrZero(MediaStore.Audio.Media.YEAR),
            trackNumber = trackNum,
            discNumber = discNum,
            composer = cursor.stringOrEmpty(MediaStore.Audio.Media.COMPOSER),
            filePath = filePath,
            fileSize = fileSize,
        )
    }

    private fun isMusic(fileName: String, mime: String, durationMs: Long): Boolean {
        if (mime.startsWith("video/", ignoreCase = true)) return false
        if (audioExtensions.any { fileName.endsWith(".$it", ignoreCase = true) }) return true
        return mime.startsWith("audio/", ignoreCase = true) && durationMs >= 1500
    }

    private fun probe(context: Context, track: Track): Track {
        val info = runCatching {
            MediaInfoBuilder().from(context, track.uri).build()
        }.getOrNull()
        val audio = info?.audioStreams?.firstOrNull()
        
        var sampleRate = audio?.sampleRate ?: track.sampleRate
        var channels = audio?.channels ?: track.channels
        var bitDepth = bitDepthFromSampleFormat(audio?.sampleFormat).takeIf { it > 0 } ?: track.bitDepth
        var duration = info?.duration?.takeIf { it > 0 } ?: track.durationMs
        var bitrate = audio?.bitRate?.toInt()?.takeIf { it > 0 } ?: track.bitrate
        var mime = audio?.codecName?.takeIf { it.isNotBlank() }?.let { "audio/$it" } ?: track.mimeType

        var albumArtist = track.albumArtist
        var genre = track.genre
        var composer = track.composer
        var comment = track.comment
        var trackNum = track.trackNumber
        var discNum = track.discNumber
        var yearVal = track.year

        runCatching {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, track.uri)
            
            val mmrArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)
            if (!mmrArtist.isNullOrBlank()) albumArtist = mmrArtist
            
            val mmrGenre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
            if (!mmrGenre.isNullOrBlank()) genre = mmrGenre

            val mmrComposer = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER)
            if (!mmrComposer.isNullOrBlank()) composer = mmrComposer

            val mmrTrack = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
            if (!mmrTrack.isNullOrBlank()) {
                val parsed = mmrTrack.substringBefore('/').toIntOrNull()
                if (parsed != null && parsed > 0) trackNum = parsed
            }

            val mmrDisc = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER)
            if (!mmrDisc.isNullOrBlank()) {
                val parsed = mmrDisc.substringBefore('/').toIntOrNull()
                if (parsed != null && parsed > 0) discNum = parsed
            }

            val mmrYear = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
            if (!mmrYear.isNullOrBlank()) {
                val parsed = mmrYear.take(4).toIntOrNull()
                if (parsed != null && parsed > 0) yearVal = parsed
            }

            val mmrBitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull()
            if (bitrate <= 0 && mmrBitrate != null && mmrBitrate > 0) {
                bitrate = mmrBitrate
            }

            retriever.release()
        }

        return track.copy(
            sampleRate = sampleRate,
            channels = channels,
            bitDepth = bitDepth,
            durationMs = duration,
            bitrate = bitrate,
            mimeType = mime,
            albumArtist = albumArtist,
            genre = genre,
            composer = composer,
            comment = comment,
            trackNumber = trackNum,
            discNumber = discNum,
            year = yearVal,
        )
    }

    private fun Cursor.stringOrEmpty(column: String): String {
        val index = getColumnIndex(column)
        if (index < 0) return ""
        return getString(index).orEmpty()
    }

    private fun Cursor.longOrNull(column: String): Long? {
        val index = getColumnIndex(column)
        if (index < 0 || isNull(index)) return null
        return getLong(index)
    }

    private fun Cursor.intOrZero(column: String): Int {
        val index = getColumnIndex(column)
        if (index < 0 || isNull(index)) return 0
        return getInt(index)
    }
}
