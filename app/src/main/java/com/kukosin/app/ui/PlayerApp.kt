@file:OptIn(UnstableApi::class)

package com.kukosin.app.ui

import androidx.media3.common.util.UnstableApi
import android.Manifest
import android.content.ComponentName
import android.net.Uri
import android.util.Log
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Usb
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import com.kukosin.app.data.artwork.EmbeddedArtwork
import com.kukosin.app.data.ytm.YtmService
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.compose.AsyncImage
import com.kukosin.app.data.AlbumGroup
import com.kukosin.app.data.ArtistGroup
import com.kukosin.app.data.FolderNode
import com.kukosin.app.data.Track
import com.kukosin.app.data.formatTime
import com.kukosin.app.data.ytm.YtmTrack
import com.kukosin.app.playback.AudioRoute
import com.kukosin.app.playback.OutputDevice
import com.kukosin.app.playback.PlaybackService
import com.kukosin.app.playback.QueueManager
import com.kukosin.app.playback.UsbDacManager
import com.kukosin.app.ui.theme.Cream
import com.kukosin.app.ui.theme.CreamMuted
import com.kukosin.app.ui.theme.Gold
import com.kukosin.app.ui.theme.Ink
import com.kukosin.app.ui.theme.InkElevated
import com.kukosin.app.ui.theme.InkSoft
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun PlayerApp(viewModel: LibraryViewModel = viewModel()) {
    val context = LocalContext.current
    val library by viewModel.uiState.collectAsStateWithLifecycle()
    var hasPermission by remember { mutableStateOf(hasAudioPermission(context)) }
    var controller by remember { mutableStateOf<MediaController?>(null) }
    var showNowPlaying by remember { mutableStateOf(false) }
    var showSignalPath by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var userMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userMessage) {
        if (userMessage != null) {
            delay(3000)
            userMessage = null
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        hasPermission = grants.entries.any { it.key != Manifest.permission.POST_NOTIFICATIONS && it.value }
        if (hasPermission) viewModel.refresh()
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission && !library.hasScanned && !library.isScanning) {
            viewModel.refresh()
        }
    }

    DisposableEffect(Unit) {
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        val executor = ContextCompat.getMainExecutor(context)
        future.addListener(
            {
                if (future.isDone && !future.isCancelled) {
                    runCatching { controller = future.get() }
                }
            },
            executor,
        )
        onDispose {
            MediaController.releaseFuture(future)
            controller = null
        }
    }

    DisposableEffect(controller) {
        val player = controller
        if (player != null) QueueManager.attach(player)
        onDispose { if (player != null) QueueManager.detach(player) }
    }

    val playback = rememberPlaybackState(controller, library.tracks)

    /** videoId de YouTube Music que está sonando, para pintar la fila activa en la pestaña YTM. */
    val playingYtmId = playback.current?.uri
        ?.takeIf { it.scheme == YtmService.STREAM_SCHEME }
        ?.lastPathSegment
    val output by AudioRoute.current.collectAsStateWithLifecycle()
    val analysis by viewModel.analysisState.collectAsStateWithLifecycle()
    val audioConfig by viewModel.audioConfig.collectAsStateWithLifecycle()
    var showSettings by remember { mutableStateOf(false) }

    LaunchedEffect(playback.current?.uri) {
        // Los streams de YouTube Music no tienen archivo ni PCM local: pedir metadatos/análisis
        // solo gastaba trabajo y podía dejar la pantalla "analizando" para siempre.
        val currentTrack = playback.current
        if (currentTrack != null && currentTrack.isLocal) {
            viewModel.prepareMetadata(currentTrack)
            if (audioConfig.autoAnalyze) {
                viewModel.analyzeSpectral(currentTrack)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Ink, InkElevated, Ink),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            Header(
                trackCount = library.tracks.size,
                scanning = library.isScanning,
                outputName = output.name,
                outputKind = output.typeLabel,
                isDac = output.isDac,
                isBitPerfect = output.isBitPerfectSupported,
                hasUsbPerm = output.hasUsbPermission,
                error = library.error,
                onRequestUsb = { UsbDacManager.requestPermission(context) },
                onOpenSignalPath = { showSignalPath = true },
                onOpenSettings = { showSettings = true },
            )

            LibraryTabs(
                activeTab = library.activeTab,
                onTabSelected = viewModel::setTab,
            )

            if (library.activeTab != LibraryTab.YOUTUBE_MUSIC && hasPermission) {
                SearchField(
                    query = library.query,
                    onQueryChange = viewModel::onQueryChange,
                )
            }

            val bottomPadding = if (playback.current != null) 108 else 24

            when (library.activeTab) {
                LibraryTab.YOUTUBE_MUSIC -> YtmView(
                    ytmTracks = library.ytmTracks,
                    ytmQuery = library.ytmQuery,
                    isSearching = library.isYtmSearching,
                    onQueryChange = viewModel::onYtmQueryChange,
                    onSearch = { viewModel.searchYtm() },
                    errorMessage = library.ytmError ?: playback.error,
                    playingVideoId = playingYtmId,
                    isPlaying = playback.isPlaying,
                    onTrackClick = { ytmTrack ->
                        controller?.let { player ->
                            // Cola real: se encola TODA la búsqueda y se arranca en la tocada,
                            // así "siguiente" y el autoplay siguen con el resto de resultados.
                            val queue = library.ytmTracks.map(YtmService::toMediaItem)
                            val startIndex = library.ytmTracks
                                .indexOfFirst { it.id == ytmTrack.id }
                                .coerceAtLeast(0)
                            QueueManager.playItems(player, queue, startIndex)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    bottomPadding = bottomPadding,
                )
                else -> {
                    when {
                        !hasPermission -> PermissionCard(
                            onGrant = { permissionLauncher.launch(audioPermissions()) },
                        )
                        library.isScanning && library.tracks.isEmpty() -> LoadingLibrary()
                        library.hasScanned && library.tracks.isEmpty() -> EmptyLibrary(hasQuery = false)
                        else -> {
                            when (library.activeTab) {
                                LibraryTab.CANCIONES -> TrackList(
                                    tracks = library.filteredTracks,
                                    currentUri = playback.current?.uri,
                                    isPlaying = playback.isPlaying,
                                    controller = controller,
                                    onTrackClick = { index ->
                                        playTrackList(controller, library.filteredTracks, index)
                                    },
                                    onInspectClick = viewModel::setInspectTrack,
                                    onMessage = { msg -> userMessage = msg },
                                    modifier = Modifier.weight(1f),
                                    bottomPadding = bottomPadding,
                                )
                                LibraryTab.ALBUMES -> AlbumView(
                                    albums = library.filteredAlbums,
                                    onAlbumClick = viewModel::selectAlbum,
                                    modifier = Modifier.weight(1f),
                                    bottomPadding = bottomPadding,
                                )
                                LibraryTab.ARTISTAS -> ArtistView(
                                    artists = library.filteredArtists,
                                    onArtistClick = viewModel::selectArtist,
                                    modifier = Modifier.weight(1f),
                                    bottomPadding = bottomPadding,
                                )
                                LibraryTab.CARPETAS -> FolderBrowserView(
                                    currentFolder = library.currentFolder,
                                    folderPath = library.folderPath,
                                    currentUri = playback.current?.uri,
                                    isPlaying = playback.isPlaying,
                                    onFolderClick = viewModel::navigateToFolder,
                                    onNavigateUp = { viewModel.navigateUpFolder() },
                                    onTrackClick = { tracks, index ->
                                        playTrackList(controller, tracks, index)
                                    },
                                    onInspectClick = viewModel::setInspectTrack,
                                    modifier = Modifier.weight(1f),
                                    bottomPadding = bottomPadding,
                                )
                                else -> {}
                            }
                        }
                    }
                }
            }
        }

        playback.current?.let { current ->
            MiniPlayer(
                track = current,
                isPlaying = playback.isPlaying,
                progress = playback.progress,
                isDac = output.isDac,
                onToggle = { controller?.let { if (it.isPlaying) it.pause() else it.play() } },
                onNext = { controller?.seekToNext() },
                onOpen = { showNowPlaying = true },
                onOpenSignalPath = { showSignalPath = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }

        AnimatedVisibility(
            visible = userMessage != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = if (playback.current != null) 92.dp else 24.dp, start = 16.dp, end = 16.dp),
        ) {
            Surface(
                color = InkElevated,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Gold.copy(alpha = 0.6f)),
                shadowElevation = 8.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        userMessage.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Cream,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = {
                        showQueueSheet = true
                        userMessage = null
                    }) {
                        Text("VER COLA", color = Gold, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // `controller` es un var delegado: no admite smart cast, así que se captura en locales
    // en vez de forzar con `!!` (que rompería si algo cambia entre el chequeo y el uso).
    val sheetController = controller
    val sheetTrack = playback.current

    if (showNowPlaying && sheetTrack != null && sheetController != null) {
        NowPlayingSheet(
            track = sheetTrack,
            controller = sheetController,
            isPlaying = playback.isPlaying,
            output = output,
            analysis = analysis,
            audioConfig = audioConfig,
            onDismiss = { showNowPlaying = false },
            onOpenQueue = { showQueueSheet = true },
            onOpenSignalPath = { showSignalPath = true },
        )
    }

    if (showQueueSheet && sheetController != null) {
        QueueSheet(
            controller = sheetController,
            tracks = library.tracks,
            audioConfig = audioConfig,
            onDismiss = { showQueueSheet = false },
            onMessage = { msg -> userMessage = msg },
        )
    }

    if (showSettings) {
        FullScreenSettingsScreen(
            output = output,
            autoAnalyze = audioConfig.autoAnalyze,
            onToggleAutoAnalyze = viewModel::toggleAutoAnalyze,
            audioDriver = audioConfig.driver,
            onSelectAudioDriver = viewModel::setAudioDriver,
            targetSampleRate = audioConfig.targetSampleRate,
            onSelectTargetSampleRate = viewModel::setTargetSampleRate,
            targetPcmFormat = audioConfig.targetPcmFormat,
            onSelectTargetPcmFormat = viewModel::setTargetPcmFormat,
            bitPerfectEnabled = audioConfig.bitPerfectEnabled,
            onToggleBitPerfect = viewModel::toggleBitPerfect,
            oboDirectOutput = audioConfig.useOboeDirectOutput,
            onToggleOboeDirectOutput = viewModel::toggleOboeDirectOutput,
            onRescan = {
                if (hasPermission) viewModel.refresh() else permissionLauncher.launch(audioPermissions())
            },
            onDismiss = { showSettings = false },
        )
    }

    if (showSignalPath) {
        AudioSignalPathSheet(
            track = playback.current ?: library.tracks.firstOrNull(),
            output = output,
            onDismiss = { showSignalPath = false },
        )
    }

    library.selectedAlbum?.let { album ->
        AlbumDetailSheet(
            album = album,
            currentUri = playback.current?.uri,
            isPlaying = playback.isPlaying,
            controller = controller,
            onTrackClick = { index -> playTrackList(controller, album.tracks, index) },
            onInspectClick = viewModel::setInspectTrack,
            onMessage = { msg -> userMessage = msg },
            onDismiss = { viewModel.selectAlbum(null) },
        )
    }

    library.selectedArtist?.let { artist ->
        ArtistDetailSheet(
            artist = artist,
            currentUri = playback.current?.uri,
            isPlaying = playback.isPlaying,
            controller = controller,
            onTrackClick = { index -> playTrackList(controller, artist.tracks, index) },
            onInspectClick = viewModel::setInspectTrack,
            onMessage = { msg -> userMessage = msg },
            onDismiss = { viewModel.selectArtist(null) },
        )
    }

    library.inspectTrack?.let { track ->
        MetadataInspectorDialog(
            track = track,
            output = output,
            analysis = analysis,
            onDismiss = { viewModel.setInspectTrack(null) },
            onOpenSignalPath = { showSignalPath = true },
            onAnalyzeClick = { viewModel.analyzeSpectral(track) },
        )
    }
}

/**
 * Convierte un MediaItem de YouTube Music en el [Track] que pinta la interfaz.
 *
 * Los datos de audio (códec, bitrate) llegan por extras porque el archivo todavía no existe:
 * el audio se resuelve recién cuando ExoPlayer abre el stream (ver PlaybackService).
 */
private fun MediaItem.toStreamTrack(): Track {
    val videoId = localConfiguration?.uri?.lastPathSegment.orEmpty()
    return Track(
        id = videoId.hashCode().toLong(),
        title = mediaMetadata.title?.toString().orEmpty(),
        artist = mediaMetadata.artist?.toString().orEmpty(),
        album = "YouTube Music",
        durationMs = mediaMetadata.durationMs ?: 0L,
        uri = localConfiguration?.uri ?: Uri.EMPTY,
        artworkOverride = mediaMetadata.artworkUri,
        // El códec real se conoce recién al resolver la URL, así que no se inventa acá.
        streamCodecLabel = "YouTube Music",
        albumId = 0L,
        fileName = "",
    )
}

/** Un fling es ~2.2 pasos por segundo: lo suficientemente decidido para no activarse por accidente. */
private const val SWIPE_FLING_FACTOR = 2.2f

/** Con 0.18 la carátula acompaña al dedo pero deja claro que no hay tema siguiente/anterior. */
private const val SWIPE_EDGE_RESISTANCE = 0.18f

/** Espera (hasta ~600 ms) a que el reproductor publique el cambio de canción. */
private suspend fun awaitIndexChange(controller: MediaController, previousIndex: Int): Boolean {
    val deadline = System.currentTimeMillis() + 600
    while (System.currentTimeMillis() < deadline) {
        if (controller.currentMediaItemIndex != previousIndex) return true
        delay(16)
    }
    return controller.currentMediaItemIndex != previousIndex
}

private fun playTrackList(controller: MediaController?, tracks: List<Track>, index: Int) {
    if (controller == null || tracks.isEmpty()) return
    QueueManager.playTracks(controller, tracks, index)
}

private data class PlaybackSnapshot(
    val current: Track?,
    val isPlaying: Boolean,
    val progress: Float,
    /** Último error del reproductor (p. ej. un stream de YouTube Music que no se pudo resolver). */
    val error: String? = null,
)

@Composable
private fun rememberPlaybackState(
    controller: MediaController?,
    tracks: List<Track>,
): PlaybackSnapshot {
    var mediaId by remember { mutableStateOf<String?>(null) }
    var currentItem by remember { mutableStateOf<MediaItem?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var playError by remember { mutableStateOf<String?>(null) }

    DisposableEffect(controller) {
        if (controller == null) {
            return@DisposableEffect onDispose { }
        }
        val listener = object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                mediaId = player.currentMediaItem?.mediaId
                currentItem = player.currentMediaItem
                isPlaying = player.isPlaying
                if (player.playerError == null) playError = null
            }

            override fun onPlayerError(error: PlaybackException) {
                playError = error.message ?: error.errorCodeName
            }
        }
        controller.addListener(listener)
        mediaId = controller.currentMediaItem?.mediaId
        currentItem = controller.currentMediaItem
        isPlaying = controller.isPlaying
        onDispose { controller.removeListener(listener) }
    }

    LaunchedEffect(controller) {
        val player = controller ?: return@LaunchedEffect
        while (isActive) {
            val duration = player.duration.coerceAtLeast(0)
            progress = if (duration > 0) player.currentPosition.toFloat() / duration else 0f
            delay(400)
        }
    }

    // Primero la biblioteca local (por uri); si el ítem es un stream de YouTube Music no está
    // en la lista, así que se reconstruye desde el MediaItem del reproductor.
    val current = tracks.firstOrNull { it.uri.toString() == mediaId }
        ?: currentItem?.takeIf { it.localConfiguration?.uri?.scheme == YtmService.STREAM_SCHEME }
            ?.toStreamTrack()
    return PlaybackSnapshot(current, isPlaying, progress, playError)
}

@Composable
private fun Header(
    trackCount: Int,
    scanning: Boolean,
    outputName: String,
    outputKind: String,
    isDac: Boolean,
    isBitPerfect: Boolean,
    hasUsbPerm: Boolean,
    error: String?,
    onRequestUsb: () -> Unit,
    onOpenSignalPath: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "KUKOSIN",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Gold,
                    )
                    Spacer(Modifier.width(8.dp))
                    if (isDac || isBitPerfect) {
                        Surface(
                            color = Gold.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.5f)),
                            modifier = Modifier.clickable(onClick = onOpenSignalPath),
                        ) {
                            Text(
                                text = "BIT-PERFECT",
                                style = MaterialTheme.typography.labelSmall,
                                color = Gold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Biblioteca HiFi",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Cream,
                )
                Text(
                    text = when {
                        scanning -> "Escaneando metadatos y codecs…"
                        trackCount == 0 -> "FLAC, WAV, ALAC, DSD, APE, YTM Stream"
                        else -> "$trackCount pistas locales"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = CreamMuted,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onOpenSignalPath)
                        .padding(vertical = 2.dp),
                ) {
                    if (isDac) {
                        Icon(
                            imageVector = Icons.Rounded.Usb,
                            contentDescription = null,
                            tint = Gold,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(
                        text = if (isDac) "DAC USB · $outputName (Ver Ruta ↓)" else "$outputKind · $outputName (Ver Ruta ↓)",
                        style = MaterialTheme.typography.labelMedium,
                        color = Gold,
                    )
                }
                if (isDac && !hasUsbPerm) {
                    TextButton(onClick = onRequestUsb) {
                        Text("★ Conceder permiso USB Host", color = Gold, style = MaterialTheme.typography.labelSmall)
                    }
                }
                if (error != null) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gold,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Configuración",
                    tint = Gold,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}

@Composable
private fun LibraryTabs(
    activeTab: LibraryTab,
    onTabSelected: (LibraryTab) -> Unit,
) {
    val tabs = LibraryTab.entries
    ScrollableTabRow(
        selectedTabIndex = activeTab.ordinal,
        containerColor = Color.Transparent,
        contentColor = Gold,
        edgePadding = 16.dp,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                Modifier.tabIndicatorOffset(tabPositions[activeTab.ordinal]),
                color = Gold,
            )
        },
    ) {
        tabs.forEach { tab ->
            val label = when (tab) {
                LibraryTab.CANCIONES -> "Canciones"
                LibraryTab.ALBUMES -> "Álbumes"
                LibraryTab.ARTISTAS -> "Artistas"
                LibraryTab.CARPETAS -> "Carpetas"
                LibraryTab.YOUTUBE_MUSIC -> "YouTube Music"
            }
            Tab(
                selected = activeTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (tab == LibraryTab.YOUTUBE_MUSIC) {
                            Icon(Icons.Rounded.Cloud, contentDescription = null, tint = if (activeTab == tab) Gold else CreamMuted, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                        }
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (activeTab == tab) Gold else CreamMuted,
                            fontWeight = if (activeTab == tab) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        placeholder = { Text("Buscar canción, artista, álbum o formato") },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = CreamMuted) },
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Gold,
            unfocusedBorderColor = InkSoft,
            focusedContainerColor = InkSoft,
            unfocusedContainerColor = InkSoft,
            focusedTextColor = Cream,
            unfocusedTextColor = Cream,
            cursorColor = Gold,
        ),
    )
}

@Composable
private fun YtmView(
    ytmTracks: List<YtmTrack>,
    ytmQuery: String,
    isSearching: Boolean,
    errorMessage: String?,
    playingVideoId: String?,
    isPlaying: Boolean,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onTrackClick: (YtmTrack) -> Unit,
    modifier: Modifier = Modifier,
    bottomPadding: Int,
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = ytmQuery,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            placeholder = { Text("Buscar en YouTube Music Online...") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = Gold) },
            trailingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Gold, strokeWidth = 2.dp)
                } else {
                    IconButton(onClick = onSearch) {
                        Icon(Icons.Rounded.Search, contentDescription = "Buscar", tint = Gold)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Gold,
                unfocusedBorderColor = InkSoft,
                focusedContainerColor = InkSoft,
                unfocusedContainerColor = InkSoft,
                focusedTextColor = Cream,
                unfocusedTextColor = Cream,
                cursorColor = Gold,
            ),
        )

        errorMessage?.let { message ->
            Surface(
                color = Gold.copy(alpha = 0.12f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
            ) {
                Text(
                    message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Cream,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = bottomPadding.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (ytmTracks.isEmpty()) {
                item {
                    Text(
                        if (isSearching) "Buscando en YouTube Music..." else "Busca una canción, artista o álbum para reproducir desde YouTube Music.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CreamMuted,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 24.dp),
                    )
                }
            }
            items(ytmTracks, key = { it.id }) { track ->
                YtmTrackRow(
                    track = track,
                    isPlaying = track.id == playingVideoId && isPlaying,
                    onClick = { onTrackClick(track) },
                )
            }
        }
    }
}

@Composable
private fun YtmTrackRow(
    track: YtmTrack,
    isPlaying: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(InkSoft)
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(InkElevated),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Cloud, contentDescription = null, tint = CreamMuted)
            if (track.thumbnailUri.isNotBlank()) {
                AsyncImage(
                    model = track.thumbnailUri,
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                track.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isPlaying) Gold else Cream,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                listOf(track.artist, track.durationLabel).filter { it.isNotBlank() }.joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = CreamMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Surface(
                color = Gold.copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp),
            ) {
                Text(
                    "YouTube Music",
                    style = MaterialTheme.typography.labelSmall,
                    color = Gold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }
        IconButton(onClick = onClick) {
            Icon(
                if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                tint = Gold,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun PermissionCard(onGrant: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Rounded.MusicNote,
            contentDescription = null,
            tint = Gold,
            modifier = Modifier.size(56.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text("Tu música, sin pérdidas", style = MaterialTheme.typography.titleLarge, color = Cream)
        Spacer(Modifier.height(8.dp))
        Text(
            "Kukosin lee la biblioteca local, decodifica en software de alta resolución y envía audio bit-perfect al DAC USB o a la salida activa.",
            style = MaterialTheme.typography.bodyMedium,
            color = CreamMuted,
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onGrant,
            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text("Permitir acceso a audio")
        }
    }
}

@Composable
private fun LoadingLibrary() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Gold)
            Spacer(Modifier.height(16.dp))
            Text("Escaneando la biblioteca…", color = CreamMuted)
        }
    }
}

@Composable
private fun EmptyLibrary(hasQuery: Boolean) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = if (hasQuery) "Nada coincide con la búsqueda" else "No hay archivos de audio indexados",
            color = CreamMuted,
        )
    }
}

@Composable
private fun TrackList(
    tracks: List<Track>,
    currentUri: android.net.Uri?,
    isPlaying: Boolean,
    controller: MediaController? = null,
    onTrackClick: (Int) -> Unit,
    onInspectClick: (Track) -> Unit,
    onMessage: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    bottomPadding: Int,
) {
    if (tracks.isEmpty()) {
        EmptyLibrary(hasQuery = true)
        return
    }
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = bottomPadding.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        itemsIndexed(tracks, key = { _, track -> track.uri.toString() }) { index, track ->
            TrackRow(
                track = track,
                selected = track.uri == currentUri,
                isPlaying = isPlaying && track.uri == currentUri,
                controller = controller,
                onClick = { onTrackClick(index) },
                onInspect = { onInspectClick(track) },
                onMessage = onMessage,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrackRow(
    track: Track,
    selected: Boolean,
    isPlaying: Boolean,
    controller: MediaController? = null,
    onClick: () -> Unit,
    onInspect: (() -> Unit)? = null,
    onMessage: ((String) -> Unit)? = null,
) {
    var showMenu by remember { mutableStateOf(false) }
    var swipeHandled by remember { mutableStateOf(false) }
    var artworkDragX by remember { mutableFloatStateOf(0f) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            // Nunca confirmar el dismissal real: solo queremos ejecutar una acción
            // y dejar que el ítem regrese a su estado normal.
            false
        },
    )

    // Ejecutar la acción una sola vez al iniciar el swipe en una dirección.
    // El ítem regresa automáticamente porque confirmValueChange retorna false.
    LaunchedEffect(dismissState.dismissDirection) {
        val dir = dismissState.dismissDirection
        if (!swipeHandled) {
            when (dir) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    swipeHandled = true
                    val msg = QueueActions.playNext(controller, track)
                    onMessage?.invoke(msg)
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    swipeHandled = true
                    val msg = QueueActions.addToQueue(controller, track)
                    onMessage?.invoke(msg)
                }
                else -> Unit
            }
        }
    }

    // Reset cuando el ítem vuelve a su posición normal, permitiendo el próximo gesto
    LaunchedEffect(dismissState.dismissDirection) {
        if (dismissState.dismissDirection == SwipeToDismissBoxValue.Settled) {
            swipeHandled = false
        }
    }

    val artworkAlpha by animateFloatAsState(
        targetValue = if (artworkDragX > 0f) (1f - (artworkDragX / 90f).coerceIn(0f, 1f)) else 1f,
        animationSpec = tween(150),
        label = "artworkDragAlpha",
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val isPlayNext = dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd
            val color = if (dismissState.dismissDirection != SwipeToDismissBoxValue.Settled) InkSoft else Color.Transparent
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp))
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = if (isPlayNext) Alignment.CenterStart else Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = if (isPlayNext) Icons.AutoMirrored.Rounded.PlaylistAdd else Icons.AutoMirrored.Rounded.QueueMusic,
                    contentDescription = if (isPlayNext) "A continuación" else "+ Cola",
                    tint = Gold,
                    modifier = Modifier.size(24.dp),
                )
            }
        },
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (selected) InkSoft else Ink.copy(alpha = 0f))
                    .clickable(onClick = onClick)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .graphicsLayer { alpha = artworkAlpha }
                        .pointerInput(track.uri.toString()) {
                            detectDragGestures(
                                onDragStart = { offset: Offset ->
                                    artworkDragX = 0f
                                },
                                onDrag = { change: PointerInputChange, dragAmount: Offset ->
                                    val absY = if (dragAmount.y < 0f) -dragAmount.y else dragAmount.y
                                    if (abs(dragAmount.x) > 2f && absY < 3f) {
                                        change.consume()
                                        artworkDragX += dragAmount.x
                                        if (artworkDragX > 60f) {
                                            controller?.seekToNext()
                                            artworkDragX = 0f
                                        }
                                    } else {
                                        artworkDragX = 0f
                                    }
                                },
                                onDragEnd = {
                                    artworkDragX = 0f
                                },
                                onDragCancel = {
                                    artworkDragX = 0f
                                },
                            )
                        },
                ) {
                    Artwork(track, Modifier.fillMaxSize())
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        track.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (selected) Gold else Cream,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "${track.effectiveArtist} · ${track.album}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CreamMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        track.qualityLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = Gold.copy(alpha = 0.85f),
                        maxLines = 1,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isPlaying) {
                        Icon(Icons.Rounded.GraphicEq, contentDescription = "Reproduciendo", tint = Gold, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(formatTime(track.durationMs), style = MaterialTheme.typography.labelSmall, color = CreamMuted)
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                Icons.Rounded.MoreVert,
                                contentDescription = "Opciones",
                                tint = CreamMuted.copy(alpha = 0.8f),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        if (showMenu) {
                            TrackOptionsMenu(
                                track = track,
                                controller = controller,
                                expanded = showMenu,
                                onDismiss = { showMenu = false },
                                onInspect = { onInspect?.invoke() },
                                onMessage = { msg -> onMessage?.invoke(msg) },
                            )
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun AlbumView(
    albums: List<AlbumGroup>,
    onAlbumClick: (AlbumGroup) -> Unit,
    modifier: Modifier = Modifier,
    bottomPadding: Int,
) {
    if (albums.isEmpty()) {
        EmptyLibrary(hasQuery = true)
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = bottomPadding.dp, top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(albums, key = { it.name + "_" + it.artist }) { album ->
            AlbumCard(album = album, onClick = { onAlbumClick(album) })
        }
    }
}

@Composable
private fun AlbumCard(album: AlbumGroup, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(InkSoft)
            .clickable(onClick = onClick)
            .padding(10.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(InkElevated),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Album, contentDescription = null, tint = CreamMuted, modifier = Modifier.size(48.dp))
            val artwork = album.artworkUri
            if (artwork != null) {
                AsyncImage(
                    model = artwork,
                    contentDescription = album.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            if (album.isHiRes) {
                Surface(
                    color = Gold,
                    contentColor = Ink,
                    shape = RoundedCornerShape(bottomStart = 8.dp),
                    modifier = Modifier.align(Alignment.TopEnd),
                ) {
                    Text(
                        "Hi-Res",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            album.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Cream,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            album.artist,
            style = MaterialTheme.typography.bodyMedium,
            color = CreamMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            "${album.tracks.size} canciones${if (album.year > 0) " · ${album.year}" else ""}",
            style = MaterialTheme.typography.labelSmall,
            color = Gold,
        )
    }
}

@Composable
private fun ArtistView(
    artists: List<ArtistGroup>,
    onArtistClick: (ArtistGroup) -> Unit,
    modifier: Modifier = Modifier,
    bottomPadding: Int,
) {
    if (artists.isEmpty()) {
        EmptyLibrary(hasQuery = true)
        return
    }
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = bottomPadding.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(artists, key = { it.name }) { artist ->
            ArtistRow(artist = artist, onClick = { onArtistClick(artist) })
        }
    }
}

@Composable
private fun ArtistRow(artist: ArtistGroup, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(InkSoft)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(InkElevated),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Person, contentDescription = null, tint = Gold)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(artist.name, style = MaterialTheme.typography.titleMedium, color = Cream, fontWeight = FontWeight.Bold)
            Text(
                "${artist.albumCount} álbumes · ${artist.trackCount} canciones",
                style = MaterialTheme.typography.bodyMedium,
                color = CreamMuted,
            )
        }
        Icon(Icons.AutoMirrored.Rounded.NavigateNext, contentDescription = null, tint = CreamMuted)
    }
}

@Composable
private fun FolderBrowserView(
    currentFolder: FolderNode?,
    folderPath: List<FolderNode>,
    currentUri: android.net.Uri?,
    isPlaying: Boolean,
    controller: MediaController? = null,
    onFolderClick: (FolderNode) -> Unit,
    onNavigateUp: () -> Unit,
    onTrackClick: (List<Track>, Int) -> Unit,
    onInspectClick: (Track) -> Unit,
    onMessage: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    bottomPadding: Int,
) {
    if (currentFolder == null) {
        EmptyLibrary(hasQuery = false)
        return
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (folderPath.size > 1) {
                IconButton(onClick = onNavigateUp) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Atrás", tint = Gold)
                }
            } else {
                Icon(Icons.Rounded.Folder, contentDescription = null, tint = Gold, modifier = Modifier.padding(8.dp))
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = currentFolder.name,
                style = MaterialTheme.typography.titleMedium,
                color = Cream,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = bottomPadding.dp, top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(currentFolder.subfolders, key = { it.path }) { subfolder ->
                FolderRow(folder = subfolder, onClick = { onFolderClick(subfolder) })
            }

            itemsIndexed(currentFolder.tracks, key = { _, track -> track.uri.toString() }) { index, track ->
                TrackRow(
                    track = track,
                    selected = track.uri == currentUri,
                    isPlaying = isPlaying && track.uri == currentUri,
                    controller = controller,
                    onClick = { onTrackClick(currentFolder.tracks, index) },
                    onInspect = { onInspectClick(track) },
                    onMessage = onMessage,
                )
            }
        }
    }
}

@Composable
private fun FolderRow(folder: FolderNode, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(InkSoft)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Folder, contentDescription = null, tint = Gold, modifier = Modifier.size(36.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(folder.name, style = MaterialTheme.typography.titleMedium, color = Cream, fontWeight = FontWeight.Bold)
            Text(
                "${folder.totalTracksCount} canciones",
                style = MaterialTheme.typography.bodyMedium,
                color = CreamMuted,
            )
        }
        Icon(Icons.AutoMirrored.Rounded.NavigateNext, contentDescription = null, tint = CreamMuted)
    }
}

@Composable
internal fun Artwork(
    track: Track,
    modifier: Modifier = Modifier,
    artworkUri: Uri? = null,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(InkSoft),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = CreamMuted)
        AsyncImage(
            model = artworkUri ?: track.artworkUri,
            contentDescription = track.album,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun MiniPlayer(
    track: Track,
    isPlaying: Boolean,
    progress: Float,
    isDac: Boolean,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onOpen: () -> Unit,
    onOpenSignalPath: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = InkSoft,
        tonalElevation = 0.dp,
        shadowElevation = 12.dp,
    ) {
        Column {
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = Gold,
                trackColor = Ink,
            )
            Row(
                modifier = Modifier
                    .clickable(onClick = onOpen)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Artwork(track, Modifier.size(48.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Cream, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (isDac) {
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                color = Gold,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.clickable(onClick = onOpenSignalPath),
                            ) {
                                Text("DAC", style = MaterialTheme.typography.labelSmall, color = Ink, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                            }
                        }
                    }
                    Text("${track.effectiveArtist} · ${track.codecLabel}", maxLines = 1, overflow = TextOverflow.Ellipsis, color = CreamMuted, style = MaterialTheme.typography.bodyMedium)
                }
                IconButton(onClick = onToggle) {
                    Icon(
                        if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                        tint = Gold,
                    )
                }
                IconButton(onClick = onNext) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = "Siguiente", tint = Cream)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NowPlayingSheet(
    track: Track,
    controller: MediaController,
    isPlaying: Boolean,
    output: OutputDevice,
    analysis: com.kukosin.app.data.analysis.AudioAnalysis?,
    audioConfig: com.kukosin.app.playback.AudioConfig,
    onDismiss: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenSignalPath: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var position by remember { mutableLongStateOf(controller.currentPosition) }
    var duration by remember { mutableLongStateOf(controller.duration.coerceAtLeast(0)) }
    val shuffle by QueueManager.shuffleEnabled.collectAsStateWithLifecycle()
    var repeatMode by remember { mutableStateOf(controller.repeatMode) }
    var seeking by remember { mutableStateOf(false) }
    var slider by remember { mutableFloatStateOf(0f) }
    var isExpanded by remember { mutableStateOf(false) }
    var showLyrics by remember { mutableStateOf(false) }
    var chromeVisible by remember { mutableStateOf(true) }
    var volume by remember { mutableFloatStateOf(controller.volume) }
    var lastVolume by remember { mutableFloatStateOf(1f) }
    // Dentro de `awaitEachGesture` (AwaitPointerEventScope está restringido) no se pueden
    // llamar suspend functions ajenas, así que el dedo escribe estado plano y la animación de
    // cierre se lanza al salir del gesto. Se pinta una u otra según si hay animación en curso.
    // Scope de la composición para las animaciones de cierre (PointerInputScope no es un
    // CoroutineScope y el de los gestos está restringido).
    val gestureScope = rememberCoroutineScope()
    // Única fuente de verdad del desplazamiento: el gesto la escribe mientras el dedo
    // arrastra y la animación de cierre la escribe frame a frame. Antes había dos estados
    // (dedo + animación) y la carátula quedaba congelada a mitad de camino.
    var dragX by remember { mutableFloatStateOf(0f) }
    val chromeScope = rememberCoroutineScope()
    var chromeRevealJob by remember { mutableStateOf<Job?>(null) }

    val currentLyrics = analysis?.takeIf { it.uri == track.uri }?.lyrics

    LaunchedEffect(showLyrics) {
        chromeRevealJob?.cancel()
        chromeVisible = true
    }

    val onLyricsScroll: (Boolean) -> Unit = { scrolling ->
        if (scrolling) {
            chromeRevealJob?.cancel()
            chromeVisible = false
        } else {
            chromeRevealJob?.cancel()
            chromeRevealJob = chromeScope.launch {
                delay(2500)
                chromeVisible = true
            }
        }
    }

    LaunchedEffect(Unit) {
        isExpanded = true
    }

    val artworkSize by animateDpAsState(
        targetValue = if (isExpanded) (if (isPlaying) 340.dp else 280.dp) else 80.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "appleMusicArtworkExpand",
    )
    val artworkScale by animateFloatAsState(
        targetValue = if (isExpanded) (if (isPlaying) 1f else 0.94f) else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "artworkScale",
    )

    val artworkBlur by animateDpAsState(
        targetValue = if (dragX != 0f) abs(dragX).coerceAtMost(12f).dp else 0.dp,
        animationSpec = tween(120),
        label = "artworkDragBlur",
    )
    val artworkAlpha by animateFloatAsState(
        targetValue = if (dragX != 0f) (1f - (abs(dragX) / 90f).coerceIn(0f, 1f)) else 1f,
        animationSpec = tween(120),
        label = "artworkDragAlpha",
    )

    /** URI del arte de la canción que está [offset] posiciones respecto a la actual. */
    fun trackArtworkUriAt(offset: Int): Uri? {
        val index = controller.currentMediaItemIndex + offset
        if (index !in 0 until controller.mediaItemCount) return null
        val item = controller.getMediaItemAt(index)
        val itemUri = item.localConfiguration?.uri
        // En los archivos locales la metadata lleva la carátula del sistema (chica); para
        // dibujar se usa la embebida, igual que el resto de la interfaz.
        return if (itemUri != null && (itemUri.scheme == "content" || itemUri.scheme == "file")) {
            EmbeddedArtwork.uriFor(itemUri)
        } else {
            item.mediaMetadata?.artworkUri
        }
    }

    val artworkWidthPx = with(LocalDensity.current) { artworkSize.toPx() }
    val dragThreshold = (artworkWidthPx * 0.35f).coerceAtLeast(60f)

    LaunchedEffect(controller, showLyrics) {
        while (isActive) {
            if (!seeking) {
                position = controller.currentPosition
                duration = controller.duration.coerceAtLeast(0)
            }
            repeatMode = controller.repeatMode
            delay(if (showLyrics) 80L else 250L)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Ink,
        contentColor = Cream,
        modifier = Modifier.fillMaxHeight(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Minimizar", tint = CreamMuted, modifier = Modifier.size(32.dp))
                }

                Text(
                    "EN REPRODUCCIÓN",
                    style = MaterialTheme.typography.labelMedium,
                    color = Gold,
                    fontWeight = FontWeight.Bold,
                )

                IconButton(onClick = onOpenQueue) {
                    if (audioConfig.showQueueBadge && controller.mediaItemCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = Gold,
                                    contentColor = Ink,
                                ) {
                                    Text("${controller.mediaItemCount}", fontWeight = FontWeight.Bold)
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                                contentDescription = "Cola de reproducción",
                                tint = Gold,
                                modifier = Modifier.size(26.dp),
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                            contentDescription = "Cola de reproducción",
                            tint = Gold,
                            modifier = Modifier.size(26.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            AnimatedContent(
                targetState = showLyrics,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "artworkLyricsTransition",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) { showingLyrics ->
                if (showingLyrics) {
                    LyricsPanel(
                        lyrics = currentLyrics,
                        positionMs = position,
                        onLineClick = { timeMs -> controller.seekTo(timeMs) },
                        onScrollActivity = onLyricsScroll,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(artworkSize)
                                .pointerInput(track.uri.toString(), artworkSize) {
                                    val step = artworkWidthPx
                                    val threshold = dragThreshold
                                    val flingThreshold = step * SWIPE_FLING_FACTOR
                                    val tracker = VelocityTracker()

                                    // Anima dragX desde donde esté hasta target (se puede llamar
                                    // desde el scope de la composición, no desde el gesto).
                                    suspend fun animateDragTo(target: Float, spec: AnimationSpec<Float>) {
                                        val from = dragX
                                        animate(from, target, animationSpec = spec) { value, _ ->
                                            dragX = value
                                        }
                                    }

                                    suspend fun snapBack() {
                                        animateDragTo(0f, spring(0.9f, Spring.StiffnessMediumLow))
                                    }

                                    // Lleva la carátula hasta el borde, cambia de canción y recién
                                    // después vuelve a cero, para que no se vea el salto.
                                    suspend fun commit(goNext: Boolean) {
                                        animateDragTo(
                                            if (goNext) -step else step,
                                            tween(240, easing = FastOutSlowInEasing),
                                        )
                                        val switched = try {
                                            val previousIndex = controller.currentMediaItemIndex
                                            if (goNext) controller.seekToNext() else controller.seekToPrevious()
                                            awaitIndexChange(controller, previousIndex)
                                        } catch (throwable: Throwable) {
                                            false
                                        }
                                        Log.d(
                                            "KukosinSwipe",
                                            "commit ${if (goNext) "siguiente" else "anterior"}: " +
                                                "se cambio=$switched, indice=${controller.currentMediaItemIndex}",
                                        )
                                        if (switched) {
                                            // Un frame para que el sheet ya esté pintando la
                                            // canción nueva antes de volver el offset a cero.
                                            withFrameNanos { }
                                            dragX = 0f
                                        } else {
                                            snapBack()
                                        }
                                    }

                                    detectHorizontalDragGestures(
                                        onDragStart = {
                                            tracker.resetTracking()
                                            dragX = 0f
                                        },
                                        onHorizontalDrag = { change, dragAmount ->
                                            change.consume()
                                            tracker.addPosition(change.uptimeMillis, change.position)
                                            val raw = dragX + dragAmount
                                            // En el primer/último tema la carátula se resiste.
                                            val blocked =
                                                (raw > 0f && !controller.hasPreviousMediaItem()) ||
                                                    (raw < 0f && !controller.hasNextMediaItem())
                                            dragX = if (blocked) {
                                                dragX + dragAmount * SWIPE_EDGE_RESISTANCE
                                            } else {
                                                raw
                                            }
                                        },
                                        onDragEnd = {
                                            val velocity = tracker.calculateVelocity().x
                                            val goNext = dragX < -threshold || velocity < -flingThreshold
                                            val goPrevious = dragX > threshold || velocity > flingThreshold
                                            Log.d(
                                                "KukosinSwipe",
                                                "soltar: drag=${dragX.toInt()}px (umbral ${threshold.toInt()}) " +
                                                    "vel=${velocity.toInt()} nuevo=$goNext anterior=$goPrevious " +
                                                    "haySiguiente=${controller.hasNextMediaItem()} " +
                                                    "hayAnterior=${controller.hasPreviousMediaItem()}",
                                            )
                                            gestureScope.launch {
                                                when {
                                                    goNext && controller.hasNextMediaItem() -> commit(goNext = true)
                                                    goPrevious && controller.hasPreviousMediaItem() ->
                                                        commit(goNext = false)
                                                    else -> snapBack()
                                                }
                                            }
                                        },
                                        onDragCancel = {
                                            gestureScope.launch { snapBack() }
                                        },
                                    )
                                },
                        ) {
                            val progress = (abs(dragX) / artworkWidthPx).coerceIn(0f, 1f)
                            val direction = if (dragX > 0f) 1 else if (dragX < 0f) -1 else 0

                            // Carátula de la canción destino (siguiente / anterior). Se desliza desde el
                            // lado opuesto al gesto y va ocupando el centro mientras arrastras.
                            // Izquierda → siguiente (entra desde la derecha); derecha → anterior (entra desde la izquierda).
                            val incomingUri = when (direction) {
                                1 -> trackArtworkUriAt(-1)
                                -1 -> trackArtworkUriAt(+1)
                                else -> null
                            }
                            if (incomingUri != null && dragX != 0f) {
                                Box(
                                    modifier = Modifier
                                        .size(artworkSize)
                                        .clip(RoundedCornerShape(20.dp))
                                        .graphicsLayer {
                                            translationX = if (dragX > 0f) dragX - artworkWidthPx else dragX + artworkWidthPx
                                            alpha = progress
                                        },
                                ) {
                                    AsyncImage(
                                        model = incomingUri,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                            }

                            // Carátula actual: se mueve con el dedo, se desenfoca y se desvanece al salir.
                            Box(
                                modifier = Modifier
                                    .size(artworkSize)
                                    .clip(RoundedCornerShape(20.dp))
                                    .blur(artworkBlur, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                                    .graphicsLayer {
                                        translationX = dragX
                                        scaleX = artworkScale
                                        scaleY = artworkScale
                                        alpha = artworkAlpha
                                    },
                            ) {
                                Artwork(
                                    track,
                                    Modifier.fillMaxSize(),
                                )
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                        Text(track.title, style = MaterialTheme.typography.headlineMedium, color = Cream, fontWeight = FontWeight.Bold, maxLines = 2)
                        Spacer(Modifier.height(4.dp))
                        Text("${track.effectiveArtist} — ${track.album}", style = MaterialTheme.typography.bodyLarge, color = CreamMuted, maxLines = 1)
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            color = InkSoft,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Gold.copy(alpha = 0.5f)),
                            modifier = Modifier.clickable(onClick = onOpenSignalPath),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = if (output.isDac) Icons.Rounded.Usb else Icons.Rounded.GraphicEq,
                                    contentDescription = null,
                                    tint = Gold,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (output.isDac) "Bit-Perfect hacia ${output.name} (Ver Ruta ↓)" else "Salida · ${output.name} (Ver Ruta ↓)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Gold,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Slider(
                            value = if (seeking) slider else if (duration > 0) position.toFloat() / duration else 0f,
                            onValueChange = {
                                seeking = true
                                slider = it
                            },
                            onValueChangeFinished = {
                                controller.seekTo((slider * duration).toLong())
                                seeking = false
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = Gold,
                                activeTrackColor = Gold,
                                inactiveTrackColor = InkSoft,
                            ),
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(formatTime(if (seeking) (slider * duration).toLong() else position), color = CreamMuted, style = MaterialTheme.typography.labelSmall)
                            Text(formatTime(duration), color = CreamMuted, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = chromeVisible || !showLyrics,
                enter = fadeIn(tween(400, easing = FastOutSlowInEasing)) +
                    expandVertically(
                        expandFrom = Alignment.Bottom,
                        animationSpec = tween(400, easing = FastOutSlowInEasing),
                    ),
                exit = fadeOut(tween(300, easing = FastOutSlowInEasing)) +
                    shrinkVertically(
                        shrinkTowards = Alignment.Bottom,
                        animationSpec = tween(300, easing = FastOutSlowInEasing),
                    ),
            ) {
                Column(Modifier.fillMaxWidth()) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { QueueManager.toggleShuffle(controller) }) {
                            Icon(Icons.Rounded.Shuffle, contentDescription = "Aleatorio", tint = if (shuffle) Gold else CreamMuted)
                        }
                        IconButton(onClick = { controller.seekToPrevious() }) {
                            Icon(Icons.Rounded.SkipPrevious, contentDescription = "Anterior", tint = Cream, modifier = Modifier.size(40.dp))
                        }
                        FilledIconButton(
                            onClick = { if (controller.isPlaying) controller.pause() else controller.play() },
                            modifier = Modifier.size(72.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Gold, contentColor = Ink),
                            shape = CircleShape,
                        ) {
                            Icon(
                                if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                                modifier = Modifier.size(40.dp),
                            )
                        }
                        IconButton(onClick = { controller.seekToNext() }) {
                            Icon(Icons.Rounded.SkipNext, contentDescription = "Siguiente", tint = Cream, modifier = Modifier.size(40.dp))
                        }
                        IconButton(
                            onClick = {
                                controller.repeatMode = when (controller.repeatMode) {
                                    Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                                    Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                                    else -> Player.REPEAT_MODE_OFF
                                }
                            },
                        ) {
                            Icon(
                                if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                                contentDescription = "Repetir",
                                tint = if (repeatMode == Player.REPEAT_MODE_OFF) CreamMuted else Gold,
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = {
                            if (volume > 0f) {
                                lastVolume = volume
                                volume = 0f
                                controller.volume = 0f
                            } else {
                                val restored = lastVolume.coerceIn(0.01f, 1f)
                                volume = restored
                                controller.volume = restored
                            }
                        }) {
                            Icon(
                                imageVector = if (volume == 0f) Icons.AutoMirrored.Rounded.VolumeOff else Icons.AutoMirrored.Rounded.VolumeDown,
                                contentDescription = if (volume == 0f) "Activar sonido" else "Silenciar",
                                tint = Gold,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Slider(
                            value = volume,
                            valueRange = 0f..1f,
                            onValueChange = { newVolume ->
                                if (newVolume > 0f) lastVolume = newVolume
                                volume = newVolume
                                controller.volume = newVolume
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = Gold,
                                activeTrackColor = Gold,
                                inactiveTrackColor = InkSoft,
                            ),
                        )
                        Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "Subir volumen", tint = CreamMuted, modifier = Modifier.size(20.dp))
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            color = InkSoft,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Gold.copy(alpha = 0.5f)),
                            modifier = Modifier.clickable { showLyrics = !showLyrics },
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MusicNote,
                                    contentDescription = null,
                                    tint = Gold,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    if (showLyrics) "Ocultar letras" else if (currentLyrics != null) "Ver letras" else "Sin letras",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Gold,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }

                        Surface(
                            color = InkSoft,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Gold.copy(alpha = 0.5f)),
                            modifier = Modifier.clickable(onClick = onOpenQueue),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                                    contentDescription = null,
                                    tint = Gold,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Cola (${controller.mediaItemCount})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Gold,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AudioSignalPathSheet(
    track: Track?,
    output: OutputDevice,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Ink,
        contentColor = Cream,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.GraphicEq,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(28.dp),
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "Cadena de Señal Audio (Signal Path)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Gold,
                    )
                    Text(
                        "Tráfico de señal punto a punto desde el archivo hasta el DAC / Salida",
                        style = MaterialTheme.typography.bodySmall,
                        color = CreamMuted,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            val sampleRateStr = if (track != null && track.sampleRate > 0) {
                if (track.sampleRate >= 1000) "${track.sampleRate / 1000.0} kHz (${track.sampleRate} Hz)" else "${track.sampleRate} Hz"
            } else "44.1 kHz"

            val bitDepthStr = if (track != null && track.bitDepth > 0) "${track.bitDepth}-bit" else "16-bit"
            val channelsStr = if (track != null && track.channels > 0) {
                when (track.channels) {
                    1 -> "Mono (1.0)"
                    2 -> "Estéreo (2.0)"
                    else -> "${track.channels} canales"
                }
            } else "Estéreo (2.0)"

            val isStream = track != null && !track.isLocal
            val codecStr = track?.codecLabel ?: "FLAC / Audio"
            val bitrateStr = when {
                isStream -> "Variable (Opus/AAC)"
                track != null && track.bitrate > 0 -> "${track.bitrate / 1000} kbps"
                else -> "Variable / Lossless"
            }
            val decoderName = when {
                isStream -> "ExoPlayer · stream progresivo (YouTube Music)"
                codecStr in listOf("FLAC", "DSD", "APE", "WavPack", "ALAC", "TrueHD", "DTS", "Opus") ->
                    "NextLib Native FFmpeg Decoder"
                else -> "ExoPlayer Media3 Native Audio Decoder"
            }

            LazyColumn(
                modifier = Modifier.height(460.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    SignalNodeCard(
                        stepNumber = "1",
                        title = "Archivo de Origen (Source)",
                        subtitle = "${track?.title ?: "Audio Local"} · $codecStr",
                        icon = Icons.Rounded.MusicNote,
                        details = listOf(
                            "Formato / Codec" to codecStr,
                            "Frecuencia Muestreo" to sampleRateStr,
                            "Profundidad Bits" to bitDepthStr,
                            "Canales" to channelsStr,
                            "Tasa de Bits" to bitrateStr,
                            "Ruta Archivo" to (track?.filePath?.ifBlank { track.fileName } ?: "Local Audio"),
                        ),
                    )
                }
                item { SignalConnectorArrow("Decodificación en Software") }
                item {
                    SignalNodeCard(
                        stepNumber = "2",
                        title = "Decodificador de Audio (Decoder)",
                        subtitle = decoderName,
                        icon = Icons.Rounded.GraphicEq,
                        details = listOf(
                            "Motor Decodificador" to decoderName,
                            "Entrada Stream" to "$codecStr ($bitDepthStr / $sampleRateStr)",
                            "Salida PCM" to "32-bit IEEE PCM Float ($sampleRateStr)",
                            "Estado Motor" to "Decodificación Nativa Sin Pérdidas",
                        ),
                    )
                }
                item { SignalConnectorArrow("Procesamiento DSP & Resampler") }
                item {
                    SignalNodeCard(
                        stepNumber = "3",
                        title = "Re-Muestreo & DSP",
                        subtitle = if (output.isDac || output.isBitPerfectSupported) "Bypassed (Bit-Perfect Direct)" else "Frecuencia Nativa Mantenedora",
                        icon = Icons.Rounded.Settings,
                        details = listOf(
                            "Estado Resampler" to if (output.isDac || output.isBitPerfectSupported) "OMITIDO / BYPASSED (Bit-Perfect Direct)" else "Inactivo (Sin Re-muestreo)",
                            "Procesadores DSP" to "Ninguno (Señal Pura Bit a Bit)",
                            "Velocidad / Pitch" to "Desactivado (1.0x sin interpolación)",
                        ),
                    )
                }
                item { SignalConnectorArrow("Transferencia AudioTrack / System Sink") }
                item {
                    SignalNodeCard(
                        stepNumber = "4",
                        title = "AudioTrack & Mezclador Sistema",
                        subtitle = "PCM_FLOAT / Low Latency Direct",
                        icon = Icons.Rounded.Memory,
                        details = listOf(
                            "Formato AudioTrack" to "ENCODING_PCM_FLOAT (32-bit Flotante)",
                            "Atributos Audio" to "USAGE_MEDIA · CONTENT_TYPE_MUSIC",
                            "Modo Mezclador" to if (output.isDac) "Direct Bit-Perfect Passthrough" else "AudioTrack Direct Sync",
                            "Latencia" to "Baja Latencia (Low-Latency Direct)",
                        ),
                    )
                }
                item { SignalConnectorArrow("Dispositivo de Salida Final (Hardware)") }
                item {
                    SignalNodeCard(
                        stepNumber = "5",
                        title = "Dispositivo de Salida (Output Hardware)",
                        subtitle = "${output.typeLabel} (${output.name})",
                        icon = if (output.isDac) Icons.Rounded.Usb else Icons.Rounded.Headphones,
                        isHighlight = output.isDac || output.isBitPerfectSupported,
                        details = listOf(
                            "Dispositivo Salida" to output.name,
                            "Tipo Conexión" to output.typeLabel,
                            "Frecuencias DAC" to output.sampleRatesLabel,
                            "Estado Bit-Perfect" to if (output.isDac || output.isBitPerfectSupported) "ACTIVO (Transferencia Bit a Bit Directa)" else "Mezclador Estándar",
                            "Permiso USB Host" to if (output.hasUsbPermission) "Concedido (Acceso Directo Hardware)" else "No Requerido / Estándar",
                        ),
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SignalNodeCard(
    stepNumber: String,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    details: List<Pair<String, String>>,
    isHighlight: Boolean = false,
) {
    Surface(
        color = if (isHighlight) Gold.copy(alpha = 0.12f) else InkSoft,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isHighlight) Gold else InkElevated,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isHighlight) Gold else InkElevated),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        stepNumber,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isHighlight) Ink else Gold,
                    )
                }
                Spacer(Modifier.width(10.dp))
                Icon(icon, contentDescription = null, tint = Gold, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Cream)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = if (isHighlight) Gold else CreamMuted)
                }
            }
            Spacer(Modifier.height(10.dp))
            details.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(label, style = MaterialTheme.typography.labelSmall, color = CreamMuted)
                    Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = Cream)
                }
            }
        }
    }
}

@Composable
private fun SignalConnectorArrow(label: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(12.dp)
                .background(Gold.copy(alpha = 0.6f)),
        )
        Text(
            text = "↓ $label",
            style = MaterialTheme.typography.labelSmall,
            color = Gold,
            fontWeight = FontWeight.Bold,
        )
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(12.dp)
                .background(Gold.copy(alpha = 0.6f)),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlbumDetailSheet(
    album: AlbumGroup,
    currentUri: android.net.Uri?,
    isPlaying: Boolean,
    controller: MediaController? = null,
    onTrackClick: (Int) -> Unit,
    onInspectClick: (Track) -> Unit,
    onMessage: ((String) -> Unit)? = null,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Ink,
        contentColor = Cream,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(InkSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Album, contentDescription = null, tint = CreamMuted, modifier = Modifier.size(36.dp))
                    val art = album.artworkUri
                    if (art != null) {
                        AsyncImage(model = art, contentDescription = album.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(album.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Cream)
                    Text(album.artist, style = MaterialTheme.typography.bodyLarge, color = CreamMuted)
                    Text(
                        "${album.tracks.size} canciones${if (album.year > 0) " · ${album.year}" else ""}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Gold,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = {
                        playTrackList(controller, album.tracks, 0)
                        onMessage?.invoke("Reproduciendo álbum: ${album.name}")
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Reproducir", fontWeight = FontWeight.Bold)
                }

                Surface(
                    color = InkSoft,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.5f)),
                    modifier = Modifier.clickable {
                        val msg = QueueActions.addListToQueue(controller, album.tracks, playNext = false)
                        onMessage?.invoke(msg)
                    },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.QueueMusic, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("+ Cola", style = MaterialTheme.typography.labelMedium, color = Gold, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            LazyColumn(
                modifier = Modifier.height(340.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                itemsIndexed(album.tracks) { index, track ->
                    TrackRow(
                        track = track,
                        selected = track.uri == currentUri,
                        isPlaying = isPlaying && track.uri == currentUri,
                        controller = controller,
                        onClick = { onTrackClick(index) },
                        onInspect = { onInspectClick(track) },
                        onMessage = onMessage,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArtistDetailSheet(
    artist: ArtistGroup,
    currentUri: android.net.Uri?,
    isPlaying: Boolean,
    controller: MediaController? = null,
    onTrackClick: (Int) -> Unit,
    onInspectClick: (Track) -> Unit,
    onMessage: ((String) -> Unit)? = null,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Ink,
        contentColor = Cream,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(InkSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Person, contentDescription = null, tint = Gold, modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(artist.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Cream)
                    Text("${artist.albumCount} álbumes · ${artist.trackCount} canciones", style = MaterialTheme.typography.bodyMedium, color = CreamMuted)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = {
                        playTrackList(controller, artist.tracks, 0)
                        onMessage?.invoke("Reproduciendo artista: ${artist.name}")
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Reproducir", fontWeight = FontWeight.Bold)
                }

                Surface(
                    color = InkSoft,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.5f)),
                    modifier = Modifier.clickable {
                        val msg = QueueActions.addListToQueue(controller, artist.tracks, playNext = false)
                        onMessage?.invoke(msg)
                    },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.QueueMusic, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("+ Cola", style = MaterialTheme.typography.labelMedium, color = Gold, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            LazyColumn(
                modifier = Modifier.height(340.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                itemsIndexed(artist.tracks) { index, track ->
                    TrackRow(
                        track = track,
                        selected = track.uri == currentUri,
                        isPlaying = isPlaying && track.uri == currentUri,
                        controller = controller,
                        onClick = { onTrackClick(index) },
                        onInspect = { onInspectClick(track) },
                        onMessage = onMessage,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MetadataInspectorDialog(
    track: Track,
    output: OutputDevice,
    analysis: com.kukosin.app.data.analysis.AudioAnalysis?,
    onDismiss: () -> Unit,
    onOpenSignalPath: () -> Unit,
    onAnalyzeClick: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.82f)
                .clip(RoundedCornerShape(24.dp)),
            color = Ink,
            tonalElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.4f)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Info, contentDescription = null, tint = Gold, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Info y Autenticidad",
                            style = MaterialTheme.typography.titleLarge,
                            color = Gold,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Cerrar", tint = CreamMuted)
                    }
                }

                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item {
                        AnalysisPanel(
                            analysis = analysis?.takeIf { it.uri == track.uri },
                            scrollable = false,
                            onAnalyzeClick = onAnalyzeClick,
                            canAnalyze = track.isLocal,
                        )
                    }
                    item {
                        Spacer(Modifier.height(4.dp))
                        MetaGroupHeader("Etiquetas del Archivo")
                        MetaItem("Título", track.title)
                        MetaItem("Artista", track.effectiveArtist)
                        MetaItem("Álbum", track.album)
                        if (track.albumArtist.isNotBlank()) MetaItem("Artista del Álbum", track.albumArtist)
                        if (track.genre.isNotBlank()) MetaItem("Género", track.genre)
                        if (track.composer.isNotBlank()) MetaItem("Compositor", track.composer)
                        if (track.trackNumber > 0) MetaItem("Número de Pista", track.trackNumber.toString())
                        if (track.discNumber > 0) MetaItem("Número de Disco", track.discNumber.toString())
                        if (track.year > 0) MetaItem("Año / Fecha", track.year.toString())
                        if (track.comment.isNotBlank()) MetaItem("Comentario", track.comment)
                    }
                    item {
                        Spacer(Modifier.height(4.dp))
                        MetaGroupHeader("Especificaciones de Audio")
                        MetaItem("Codec / Formato", track.codecLabel)
                        MetaItem("Tipo Mime", track.mimeType)
                        MetaItem("Frecuencia de Muestreo", if (track.sampleRate > 0) "${track.sampleRate} Hz" else "N/A")
                        MetaItem("Profundidad de Bits", if (track.bitDepth > 0) "${track.bitDepth}-bit" else "N/A")
                        MetaItem("Canales", if (track.channels > 0) "${track.channels}" else "Estéreo")
                        MetaItem("Tasa de Bits", if (track.bitrate > 0) "${track.bitrate / 1000} kbps" else "Variable / Lossless")
                        MetaItem("Duración", formatTime(track.durationMs))
                    }
                    item {
                        Spacer(Modifier.height(4.dp))
                        MetaGroupHeader("Archivo e Integración Bit-Perfect")
                        MetaItem("Ruta de Archivo", track.filePath.ifBlank { track.uri.toString() })
                        MetaItem("Tamaño de Archivo", track.fileSizeLabel)
                        MetaItem("Salida Activa", "${output.typeLabel} (${output.name})", onClick = onOpenSignalPath)
                        MetaItem("Estado Bit-Perfect", if (output.isBitPerfectSupported) "Soportado / Activo" else "Mezclador Estándar", onClick = onOpenSignalPath)
                        MetaItem("Frecuencias Soportadas DAC", output.sampleRatesLabel, onClick = onOpenSignalPath)
                    }
                    item {
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun FullScreenSettingsScreen(
    output: OutputDevice,
    autoAnalyze: Boolean,
    onToggleAutoAnalyze: () -> Unit,
    audioDriver: String,
    onSelectAudioDriver: (String) -> Unit,
    targetSampleRate: String,
    onSelectTargetSampleRate: (String) -> Unit,
    targetPcmFormat: String,
    onSelectTargetPcmFormat: (String) -> Unit,
    bitPerfectEnabled: Boolean,
    onToggleBitPerfect: () -> Unit,
    oboDirectOutput: Boolean,
    onToggleOboeDirectOutput: () -> Unit,
    onRescan: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Ink,
            contentColor = Cream,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Atrás", tint = Gold, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Configuración de Audio & HiFi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Gold)
                        Text("Kukosin High-Resolution Audio Engine", style = MaterialTheme.typography.labelSmall, color = CreamMuted)
                    }
                }

                Spacer(Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item {
                        Text("Controlador de Salida Audio (Audio Driver)", style = MaterialTheme.typography.labelLarge, color = Gold, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            color = InkSoft,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Motor / API de Salida de Sonido", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Cream)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Selecciona la API nativa de Android para entregar las muestras de audio al hardware.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CreamMuted,
                                )
                                Spacer(Modifier.height(10.dp))
                                val drivers = listOf("AAudio", "AudioTrack", "Hi-Res Direct", "OpenSL ES")
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    drivers.forEach { driver ->
                                        val selected = audioDriver == driver
                                        Surface(
                                            color = if (selected) Gold else Ink,
                                            shape = RoundedCornerShape(10.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Gold),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { onSelectAudioDriver(driver) },
                                        ) {
                                            Text(
                                                text = driver,
                                                color = if (selected) Ink else Gold,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                textAlign = TextAlign.Center,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text("Tasa de Muestreo Objetivo (Sample Rate)", style = MaterialTheme.typography.labelLarge, color = Gold, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            color = InkSoft,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Frecuencia de Trabajo ($targetSampleRate)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Cream)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Dependiendo del controlador seleccionado, puedes forzar una tasa fija o seguir el archivo original.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CreamMuted,
                                )
                                Spacer(Modifier.height(10.dp))
                                val sampleRates = listOf("Auto (Bit-Perfect)", "44.1 kHz", "48.0 kHz", "88.2 kHz", "96.0 kHz", "192.0 kHz", "384.0 kHz")
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(sampleRates) { rate ->
                                        val selected = targetSampleRate == rate
                                        Surface(
                                            color = if (selected) Gold else Ink,
                                            shape = RoundedCornerShape(10.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Gold),
                                            modifier = Modifier.clickable { onSelectTargetSampleRate(rate) },
                                        ) {
                                            Text(
                                                text = rate,
                                                color = if (selected) Ink else Gold,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text("Frecuencias Permitidas por el Hardware DAC", style = MaterialTheme.typography.labelLarge, color = Gold, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            color = InkSoft,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(output.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Gold)
                                Spacer(Modifier.height(4.dp))
                                Text("Frecuencias de muestreo reportadas por la salida activa:", style = MaterialTheme.typography.bodySmall, color = CreamMuted)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = output.sampleRatesLabel.ifBlank { "44.1 kHz, 48.0 kHz, 88.2 kHz, 96.0 kHz, 176.4 kHz, 192.0 kHz, 384.0 kHz" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Cream,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }

                    item {
                        Text("Formato de Muestreo PCM & Profundidad", style = MaterialTheme.typography.labelLarge, color = Gold, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            color = InkSoft,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Codificación PCM ($targetPcmFormat)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Cream)
                                Spacer(Modifier.height(10.dp))
                                val pcmFormats = listOf("PCM Float 32-bit", "PCM Int 24-bit", "PCM Int 16-bit", "Auto")
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    pcmFormats.forEach { fmt ->
                                        val selected = targetPcmFormat == fmt
                                        Surface(
                                            color = if (selected) Gold else Ink,
                                            shape = RoundedCornerShape(10.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Gold),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { onSelectTargetPcmFormat(fmt) },
                                        ) {
                                            Text(
                                                text = fmt,
                                                color = if (selected) Ink else Gold,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                textAlign = TextAlign.Center,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text("Modo Bit-Perfect (Passthrough Directo)", style = MaterialTheme.typography.labelLarge, color = Gold, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            color = InkSoft,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Activar Direct Hardware Access", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Cream)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Garantiza salida exacta bit por bit sin procesamiento ni alteración del mezclador Android.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CreamMuted,
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Switch(
                                    checked = bitPerfectEnabled,
                                    onCheckedChange = { onToggleBitPerfect() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Ink,
                                        checkedTrackColor = Gold,
                                        uncheckedThumbColor = CreamMuted,
                                        uncheckedTrackColor = InkSoft,
                                    ),
                                )
                            }
                        }
                    }

                    item {
                        Text("Motor de Audio Oboe (Salida Directa)", style = MaterialTheme.typography.labelLarge, color = Gold, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            color = InkSoft,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Usar motor nativo Oboe (C++ / JNI)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Cream)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Rutea el audio decodificado directamente al motor Oboe (AAudio/OpenSL ES), aplicando el controlador y formato PCM seleccionados arriba.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CreamMuted,
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Switch(
                                    checked = oboDirectOutput,
                                    onCheckedChange = { onToggleOboeDirectOutput() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Ink,
                                        checkedTrackColor = Gold,
                                        uncheckedThumbColor = CreamMuted,
                                        uncheckedTrackColor = InkSoft,
                                    ),
                                )
                            }
                        }
                    }

                    item {
                        Text("Análisis Espectral y Autenticidad", style = MaterialTheme.typography.labelLarge, color = Gold, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            color = InkSoft,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Análisis Espectral Automático", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Cream)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Decodificar muestras PCM y calcular espectro FFT de 4096 puntos automáticamente al reproducir pistas.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CreamMuted,
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Switch(
                                    checked = autoAnalyze,
                                    onCheckedChange = { onToggleAutoAnalyze() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Ink,
                                        checkedTrackColor = Gold,
                                        uncheckedThumbColor = CreamMuted,
                                        uncheckedTrackColor = InkSoft,
                                    ),
                                )
                            }
                        }
                    }

                    item {
                        Text("Biblioteca y Deduplicación", style = MaterialTheme.typography.labelLarge, color = Gold, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            color = InkSoft,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Deduplicación de Pistas Activa", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Cream)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Filtrado inteligente por ruta de archivo para evitar canciones duplicadas entre MediaStore Audio y MediaStore Files.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CreamMuted,
                                )
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        onRescan()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Rescanear Biblioteca Completa", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        Text("Acerca de Kukosin Player", style = MaterialTheme.typography.labelLarge, color = Gold, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            color = InkSoft,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Kukosin HiFi Audio Player v1.0.0", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Cream)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Motor de reproducción lossless impulsado por Android Jetpack Media3, decodificadores nativos FFmpeg, NextLib MediaInfo y análisis de espectro FFT.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CreamMuted,
                                )
                            }
                        }
                    }

                    item {
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MetaGroupHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = Gold,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 4.dp),
    )
}

@Composable
private fun MetaItem(label: String, value: String, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = CreamMuted, modifier = Modifier.weight(0.4f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = if (onClick != null) Gold else Cream, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.6f))
    }
}

private fun audioPermissions(): Array<String> {
    val permissions = mutableListOf<String>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions += Manifest.permission.READ_MEDIA_AUDIO
        permissions += Manifest.permission.POST_NOTIFICATIONS
    } else {
        permissions += Manifest.permission.READ_EXTERNAL_STORAGE
    }
    return permissions.toTypedArray()
}

private fun hasAudioPermission(context: android.content.Context): Boolean {
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    return ContextCompat.checkSelfPermission(context, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
}
