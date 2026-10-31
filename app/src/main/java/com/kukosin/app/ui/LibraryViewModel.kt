package com.kukosin.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kukosin.app.data.AlbumGroup
import com.kukosin.app.data.ArtistGroup
import com.kukosin.app.data.FolderNode
import com.kukosin.app.data.MusicLibrary
import com.kukosin.app.data.TagCategoryGroup
import com.kukosin.app.data.Track
import com.kukosin.app.data.analysis.AudioAnalysis
import com.kukosin.app.data.analysis.AudioAnalyzer
import com.kukosin.app.data.ytm.YtmService
import com.kukosin.app.data.ytm.YtmTrack
import com.kukosin.app.data.ytm.describeYtmError
import com.kukosin.app.playback.AudioConfig
import com.kukosin.app.playback.AudioSettingsManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LibraryCategory(val title: String) {
    TODAS_LAS_CANCIONES("Todas las canciones"),
    CARPETAS("Carpetas"),
    ALBUMES("Álbumes"),
    ARTISTAS("Artistas"),
    ARTISTAS_DEL_ALBUM("Artistas del álbum"),
    GENEROS("Géneros"),
    ANOS("Años"),
    COMPOSITORES("Compositores"),
    LISTAS_DE_REPRODUCCION("Listas de reproducción"),
    LA_COLA("La cola"),
    FAVORITOS("Favoritos"),
    MAS_REPRODUCIDO("Más reproducido"),
}

data class LibraryUiState(
    val tracks: List<Track> = emptyList(),
    val albums: List<AlbumGroup> = emptyList(),
    val artists: List<ArtistGroup> = emptyList(),
    val albumArtists: List<ArtistGroup> = emptyList(),
    val genres: List<TagCategoryGroup> = emptyList(),
    val years: List<TagCategoryGroup> = emptyList(),
    val composers: List<TagCategoryGroup> = emptyList(),
    val rootFolder: FolderNode? = null,
    val folderPath: List<FolderNode> = emptyList(),
    val activeCategory: LibraryCategory? = null,
    val selectedAlbum: AlbumGroup? = null,
    val selectedArtist: ArtistGroup? = null,
    val selectedTagGroup: TagCategoryGroup? = null,
    val inspectTrack: Track? = null,
    val ytmTracks: List<YtmTrack> = emptyList(),
    val ytmQuery: String = "",
    val isYtmSearching: Boolean = false,
    val ytmError: String? = null,
    val query: String = "",
    val isScanning: Boolean = false,
    val hasScanned: Boolean = false,
    val error: String? = null,
) {
    val currentFolder: FolderNode?
        get() = folderPath.lastOrNull() ?: rootFolder

    val filteredTracks: List<Track>
        get() {
            val needle = query.trim()
            if (needle.isEmpty()) return tracks
            return tracks.filter { track ->
                track.title.contains(needle, ignoreCase = true) ||
                    track.effectiveArtist.contains(needle, ignoreCase = true) ||
                    track.effectiveAlbumArtist.contains(needle, ignoreCase = true) ||
                    track.album.contains(needle, ignoreCase = true) ||
                    track.codecLabel.contains(needle, ignoreCase = true) ||
                    track.genre.contains(needle, ignoreCase = true) ||
                    track.composer.contains(needle, ignoreCase = true)
            }
        }

    val filteredAlbums: List<AlbumGroup>
        get() {
            val needle = query.trim()
            if (needle.isEmpty()) return albums
            return albums.filter { album ->
                album.name.contains(needle, ignoreCase = true) ||
                    album.artist.contains(needle, ignoreCase = true)
            }
        }

    val filteredArtists: List<ArtistGroup>
        get() {
            val needle = query.trim()
            if (needle.isEmpty()) return artists
            return artists.filter { artist ->
                artist.name.contains(needle, ignoreCase = true)
            }
        }

    val filteredAlbumArtists: List<ArtistGroup>
        get() {
            val needle = query.trim()
            if (needle.isEmpty()) return albumArtists
            return albumArtists.filter { artist ->
                artist.name.contains(needle, ignoreCase = true)
            }
        }

    val filteredGenres: List<TagCategoryGroup>
        get() {
            val needle = query.trim()
            if (needle.isEmpty()) return genres
            return genres.filter { it.name.contains(needle, ignoreCase = true) }
        }

    val filteredYears: List<TagCategoryGroup>
        get() {
            val needle = query.trim()
            if (needle.isEmpty()) return years
            return years.filter { it.name.contains(needle, ignoreCase = true) }
        }

    val filteredComposers: List<TagCategoryGroup>
        get() {
            val needle = query.trim()
            if (needle.isEmpty()) return composers
            return composers.filter { it.name.contains(needle, ignoreCase = true) }
        }
}

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val tracks = MutableStateFlow<List<Track>>(emptyList())
    private val albums = MutableStateFlow<List<AlbumGroup>>(emptyList())
    private val artists = MutableStateFlow<List<ArtistGroup>>(emptyList())
    private val albumArtists = MutableStateFlow<List<ArtistGroup>>(emptyList())
    private val genres = MutableStateFlow<List<TagCategoryGroup>>(emptyList())
    private val years = MutableStateFlow<List<TagCategoryGroup>>(emptyList())
    private val composers = MutableStateFlow<List<TagCategoryGroup>>(emptyList())
    private val rootFolder = MutableStateFlow<FolderNode?>(null)
    private val folderPath = MutableStateFlow<List<FolderNode>>(emptyList())
    private val activeCategory = MutableStateFlow<LibraryCategory?>(null)
    private val selectedAlbum = MutableStateFlow<AlbumGroup?>(null)
    private val selectedArtist = MutableStateFlow<ArtistGroup?>(null)
    private val selectedTagGroup = MutableStateFlow<TagCategoryGroup?>(null)
    private val inspectTrack = MutableStateFlow<Track?>(null)
    private val ytmTracks = MutableStateFlow<List<YtmTrack>>(emptyList())
    private val ytmQuery = MutableStateFlow("")
    private val isYtmSearching = MutableStateFlow(false)
    private val ytmError = MutableStateFlow<String?>(null)
    private val query = MutableStateFlow("")
    private val isScanning = MutableStateFlow(false)
    private val hasScanned = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)
    private val analysis = MutableStateFlow<AudioAnalysis?>(null)
    private var analysisJob: Job? = null
    private var ytmSearchJob: Job? = null

    val uiState: StateFlow<LibraryUiState> = combine(
        combine(tracks, albums, artists, rootFolder, folderPath) { tr, al, ar, rf, fp ->
            Tuple5(tr, al, ar, rf, fp)
        },
        combine(albumArtists, genres, years, composers) { aar, gn, yr, cp ->
            Tuple4(aar, gn, yr, cp)
        },
        combine(activeCategory, selectedAlbum, selectedArtist, selectedTagGroup) { cat, sal, sar, stg ->
            Tuple4(cat, sal, sar, stg)
        },
        combine(ytmTracks, ytmQuery, isYtmSearching, inspectTrack) { yTr, yQ, ySc, ins ->
            Tuple4(yTr, yQ, ySc, ins)
        },
        combine(query, isScanning, hasScanned, error, ytmError) { q, sc, hasSc, err, yErr ->
            Tuple5(q, sc, hasSc, err, yErr)
        },
    ) { (tr, al, ar, rf, fp), (aar, gn, yr, cp), (cat, sal, sar, stg), (yTr, yQ, ySc, ins), (q, sc, hasSc, err, yErr) ->
        LibraryUiState(
            tracks = tr,
            albums = al,
            artists = ar,
            albumArtists = aar,
            genres = gn,
            years = yr,
            composers = cp,
            rootFolder = rf,
            folderPath = fp,
            activeCategory = cat,
            selectedAlbum = sal,
            selectedArtist = sar,
            selectedTagGroup = stg,
            inspectTrack = ins,
            ytmTracks = yTr,
            ytmQuery = yQ,
            isYtmSearching = ySc,
            ytmError = yErr,
            query = q,
            isScanning = sc,
            hasScanned = hasSc,
            error = err,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        LibraryUiState(),
    )

    val analysisState: StateFlow<AudioAnalysis?> = analysis

    fun openLibraryRoot() {
        activeCategory.value = null
        selectedTagGroup.value = null
        query.value = ""
    }

    fun selectCategory(category: LibraryCategory?) {
        activeCategory.value = category
        selectedTagGroup.value = null
        query.value = ""
        if (category == LibraryCategory.CARPETAS) {
            rootFolder.value?.let { folderPath.value = listOf(it) }
        }
    }

    fun selectAlbum(album: AlbumGroup?) {
        selectedAlbum.value = album
    }

    fun selectArtist(artist: ArtistGroup?) {
        selectedArtist.value = artist
    }

    fun selectTagGroup(group: TagCategoryGroup?) {
        selectedTagGroup.value = group
    }

    fun navigateToFolder(folder: FolderNode) {
        folderPath.value = folderPath.value + folder
    }

    fun navigateUpFolder(): Boolean {
        val current = folderPath.value
        if (current.size > 1) {
            folderPath.value = current.dropLast(1)
            return true
        }
        return false
    }

    val audioConfig: StateFlow<AudioConfig> = AudioSettingsManager.configState

    init {
        AudioSettingsManager.init(getApplication())
    }

    fun toggleAutoAnalyze() {
        AudioSettingsManager.setAutoAnalyze(!audioConfig.value.autoAnalyze)
    }

    fun setAudioDriver(driver: String) {
        AudioSettingsManager.setDriver(driver)
    }

    fun setTargetSampleRate(rate: String) {
        AudioSettingsManager.setTargetSampleRate(rate)
    }

    fun setTargetPcmFormat(format: String) {
        AudioSettingsManager.setTargetPcmFormat(format)
    }

    fun toggleBitPerfect() {
        AudioSettingsManager.setBitPerfectEnabled(!audioConfig.value.bitPerfectEnabled)
    }

    fun toggleOboeDirectOutput() {
        AudioSettingsManager.setUseOboeDirectOutput(!audioConfig.value.useOboeDirectOutput)
    }

    fun setDecoder(decoder: String) {
        AudioSettingsManager.setDecoder(decoder)
    }

    fun setInspectTrack(track: Track?) {
        inspectTrack.value = track
        if (track != null) {
            prepareMetadata(track)
            if (audioConfig.value.autoAnalyze) {
                analyzeSpectral(track)
            }
        }
    }

    fun prepareMetadata(track: Track) {
        if (analysis.value?.uri == track.uri && analysis.value?.technical?.isNotEmpty() == true) {
            return
        }
        analysisJob?.cancel()
        analysis.value = AudioAnalyzer.extractMetadataOnly(getApplication(), track)
    }

    fun analyzeSpectral(track: Track) {
        if (analysis.value?.uri == track.uri && analysis.value?.isAnalyzing == true) return
        analysisJob?.cancel()
        analysisJob = viewModelScope.launch {
            val current = analysis.value?.takeIf { it.uri == track.uri }
            analysis.value = (current ?: AudioAnalysis(uri = track.uri)).copy(isAnalyzing = true)
            val result = runCatching { AudioAnalyzer.analyze(getApplication(), track) }
                .getOrElse { error ->
                    AudioAnalysis(
                        uri = track.uri,
                        error = error.message ?: "No se pudo analizar",
                        verdictTitle = "Error de análisis",
                        verdictDetail = error.message.orEmpty(),
                    )
                }
            analysis.value = result
        }
    }

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onYtmQueryChange(value: String) {
        ytmQuery.value = value
    }

    fun searchYtm(q: String = ytmQuery.value) {
        val needle = q.trim()
        if (needle.isEmpty()) {
            ytmTracks.value = emptyList()
            ytmError.value = null
            return
        }
        ytmSearchJob?.cancel()
        ytmSearchJob = viewModelScope.launch {
            isYtmSearching.value = true
            ytmError.value = null
            try {
                val results = YtmService.search(needle)
                ytmTracks.value = results
                if (results.isEmpty()) {
                    ytmError.value = "Sin resultados para \"$needle\""
                }
            } catch (throwable: Throwable) {
                ytmTracks.value = emptyList()
                ytmError.value = describeYtmError(throwable)
            } finally {
                isYtmSearching.value = false
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            isScanning.value = true
            error.value = null
            try {
                val scanned = MusicLibrary.scan(getApplication())
                updateState(scanned)
                hasScanned.value = true
                isScanning.value = false

                val enriched = MusicLibrary.enrich(getApplication(), scanned)
                updateState(enriched)
            } catch (throwable: Throwable) {
                error.value = throwable.message ?: "No se pudo escanear la biblioteca"
                hasScanned.value = true
                isScanning.value = false
            }
        }
    }

    private fun updateState(scannedTracks: List<Track>) {
        tracks.value = scannedTracks
        albums.value = MusicLibrary.groupAlbums(scannedTracks)
        artists.value = MusicLibrary.groupArtists(scannedTracks)
        albumArtists.value = MusicLibrary.groupAlbumArtists(scannedTracks)
        genres.value = MusicLibrary.groupGenres(scannedTracks)
        years.value = MusicLibrary.groupYears(scannedTracks)
        composers.value = MusicLibrary.groupComposers(scannedTracks)
        val tree = MusicLibrary.buildFolderTree(scannedTracks)
        // El segundo escaneo enriquece metadatos: sustituimos los nodos antiguos sin
        // perder la carpeta que el usuario tenía abierta, si todavía existe.
        val oldPaths = folderPath.value.map { it.path }
        val restored = mutableListOf(tree)
        for (path in oldPaths.drop(1)) {
            val next = restored.last().subfolders.firstOrNull { it.path == path } ?: break
            restored.add(next)
        }
        rootFolder.value = tree
        folderPath.value = restored
    }
}

private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
private data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
