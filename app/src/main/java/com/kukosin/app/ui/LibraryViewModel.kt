package com.kukosin.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kukosin.app.data.AlbumGroup
import com.kukosin.app.data.ArtistGroup
import com.kukosin.app.data.FolderNode
import com.kukosin.app.data.MusicLibrary
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

enum class LibraryTab {
    CANCIONES,
    ALBUMES,
    ARTISTAS,
    CARPETAS,
    YOUTUBE_MUSIC,
}

data class LibraryUiState(
    val tracks: List<Track> = emptyList(),
    val albums: List<AlbumGroup> = emptyList(),
    val artists: List<ArtistGroup> = emptyList(),
    val rootFolder: FolderNode? = null,
    val folderPath: List<FolderNode> = emptyList(),
    val activeTab: LibraryTab = LibraryTab.CANCIONES,
    val selectedAlbum: AlbumGroup? = null,
    val selectedArtist: ArtistGroup? = null,
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
}

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val tracks = MutableStateFlow<List<Track>>(emptyList())
    private val albums = MutableStateFlow<List<AlbumGroup>>(emptyList())
    private val artists = MutableStateFlow<List<ArtistGroup>>(emptyList())
    private val rootFolder = MutableStateFlow<FolderNode?>(null)
    private val folderPath = MutableStateFlow<List<FolderNode>>(emptyList())
    private val activeTab = MutableStateFlow(LibraryTab.CANCIONES)
    private val selectedAlbum = MutableStateFlow<AlbumGroup?>(null)
    private val selectedArtist = MutableStateFlow<ArtistGroup?>(null)
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
        combine(activeTab, selectedAlbum, selectedArtist, inspectTrack) { tab, sal, sar, ins ->
            Tuple4(tab, sal, sar, ins)
        },
        combine(ytmTracks, ytmQuery, isYtmSearching) { yTr, yQ, ySc ->
            Tuple3(yTr, yQ, ySc)
        },
        combine(query, isScanning, hasScanned, error, ytmError) { q, sc, hasSc, err, yErr ->
            Tuple5(q, sc, hasSc, err, yErr)
        },
    ) { (tr, al, ar, rf, fp), (tab, sal, sar, ins), (yTr, yQ, ySc), (q, sc, hasSc, err, yErr) ->
        LibraryUiState(
            tracks = tr,
            albums = al,
            artists = ar,
            rootFolder = rf,
            folderPath = fp,
            activeTab = tab,
            selectedAlbum = sal,
            selectedArtist = sar,
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

    fun setTab(tab: LibraryTab) {
        activeTab.value = tab
    }

    fun selectAlbum(album: AlbumGroup?) {
        selectedAlbum.value = album
    }

    fun selectArtist(artist: ArtistGroup?) {
        selectedArtist.value = artist
    }

    fun navigateToFolder(folder: FolderNode) {
        folderPath.value = folderPath.value + folder
    }

    fun navigateUpFolder(): Boolean {
        val current = folderPath.value
        if (current.isNotEmpty()) {
            folderPath.value = current.dropLast(1)
            return true
        }
        return false
    }

    val audioConfig: StateFlow<AudioConfig> = AudioSettingsManager.configState

    init {
        AudioSettingsManager.init(getApplication())
        // Antes se llamaba a searchYtm("") aquí y en otro bloque init duplicado: dos peticiones
        // de red al abrir la app gastaban datos y latencia sin que el usuario lo pidiera.
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
        val tree = MusicLibrary.buildFolderTree(scannedTracks)
        rootFolder.value = tree
        if (folderPath.value.isEmpty()) {
            folderPath.value = listOf(tree)
        }
    }
}

private data class Tuple3<A, B, C>(val a: A, val b: B, val c: C)
private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
private data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
