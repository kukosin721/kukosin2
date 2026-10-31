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
import androidx.compose.animation.core.animate
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Usb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import com.kukosin.app.ui.component.KukosinWaveformSlider
import androidx.compose.material3.Surface
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import com.kukosin.app.data.LibraryInsights
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
import com.kukosin.app.ui.theme.AccentPreset
import com.kukosin.app.ui.theme.AccentTheme
import com.kukosin.app.ui.theme.Gold
import com.kukosin.app.ui.theme.Line
import com.kukosin.app.ui.theme.accentColor
import com.kukosin.app.ui.theme.Ink
import com.kukosin.app.ui.theme.InkElevated
import com.kukosin.app.ui.theme.InkSoft
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun PlayerApp(viewModel: LibraryViewModel = viewModel()) {
    val context = LocalContext.current
    val library by viewModel.uiState.collectAsStateWithLifecycle()
    var hasPermission by remember { mutableStateOf(hasAudioPermission(context)) }
    var controller by remember { mutableStateOf<MediaController?>(null) }
    // Estilo Poweramp: el reproductor siempre es la pantalla principal al iniciar.
    var showNowPlaying by remember { mutableStateOf(true) }
    var activeSection by remember { mutableStateOf(AppSection.LIBRARY) }
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
    val settingsVisibility = remember { MutableTransitionState(false) }
    settingsVisibility.targetState = showSettings
    val settingsDialogVisible = settingsVisibility.currentState || settingsVisibility.targetState || !settingsVisibility.isIdle

    val density = LocalDensity.current
    val playerSheetScope = rememberCoroutineScope()
    // Estilo Poweramp: el reproductor siempre es la pantalla principal (1f = reproductor, 0f = sección activa).
    var sheetProgress by remember { mutableFloatStateOf(1f) }
    var playerLyricsProgress by remember { mutableFloatStateOf(0f) }
    var isSheetDragging by remember { mutableStateOf(false) }
    var wasSheetDragged by remember { mutableStateOf(false) }
    var sheetAnimJob by remember { mutableStateOf<Job?>(null) }
    var lyricsAnimJob by remember { mutableStateOf<Job?>(null) }
    val favoriteUris by LibraryInsights.favorites.collectAsStateWithLifecycle()

    var rootCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var rootSizePx by remember { mutableStateOf(IntSize.Zero) }
    var miniArtRestingRect by remember { mutableStateOf(Rect.Zero) }
    var fullArtSlotRect by remember { mutableStateOf(Rect.Zero) }
    var lyricsArtSlotRect by remember { mutableStateOf(Rect.Zero) }

    val playPauseScale by animateFloatAsState(
        targetValue = if (playback.isPlaying) 1f else 0.92f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "artworkPlayPauseScale",
    )

    fun animatePlayerSheet(target: Float, initialVelocity: Float = 0f, fromDrag: Boolean = false) {
        sheetAnimJob?.cancel()
        isSheetDragging = false
        wasSheetDragged = fromDrag
        showNowPlaying = target > 0.5f
        val from = sheetProgress
        if (abs(from - target) < 0.0005f) {
            sheetProgress = target
            wasSheetDragged = false
            return
        }
        sheetAnimJob = playerSheetScope.launch {
            animate(
                initialValue = from,
                targetValue = target,
                initialVelocity = initialVelocity.coerceIn(-8f, 8f),
                animationSpec = spring(
                    dampingRatio = 0.90f,
                    stiffness = 380f,
                ),
            ) { value, _ ->
                sheetProgress = value.coerceIn(0f, 1f)
            }
            sheetProgress = target
            wasSheetDragged = false
        }
    }

    fun animateLyrics(target: Float, initialVelocity: Float = 0f) {
        lyricsAnimJob?.cancel()
        lyricsAnimJob = playerSheetScope.launch {
            animate(
                initialValue = playerLyricsProgress,
                targetValue = target,
                initialVelocity = initialVelocity.coerceIn(-8f, 8f),
                animationSpec = spring(dampingRatio = 0.88f, stiffness = 320f),
            ) { value, _ -> playerLyricsProgress = value.coerceIn(0f, 1f) }
            playerLyricsProgress = target
        }
    }

    // Al escanear la biblioteca, prepara el primer tema en pausa si la cola estaba vacía,
    // de modo que el reproductor principal tenga carátula y onda listas al instante.
    LaunchedEffect(controller, library.tracks.size) {
        val player = controller
        if (player != null && library.tracks.isNotEmpty() && player.mediaItemCount == 0) {
            QueueManager.cueTracksIfEmpty(player, library.tracks)
        }
    }

    val dragRangePx = (rootSizePx.height * 0.68f).takeIf { it > 240f }
        ?: with(density) { 520.dp.toPx() }
    val currentDragRangePx by rememberUpdatedState(dragRangePx)
    val lyricsDragRangePx = with(density) { 200.dp.toPx() }

    // Reservar espacio a la onda ampliada sin sacar los controles de la pantalla.
    val waveformHeight = if (rootSizePx.height > 0 && with(density) { rootSizePx.height.toDp() } < 720.dp) {
        108.dp
    } else {
        128.dp
    }
    val maxArtDp = if (rootSizePx.height > 0 && rootSizePx.width > 0) {
        with(density) {
            minOf(
                320.dp.toPx(),
                (rootSizePx.width - 48.dp.toPx()).coerceAtLeast(1.dp.toPx()),
                (rootSizePx.height - (waveformHeight + 405.dp).toPx()).coerceAtLeast(120.dp.toPx()),
            ).toDp()
        }
    } else {
        304.dp
    }

    val resolveMiniArtRect: () -> Rect = {
        if (miniArtRestingRect != Rect.Zero) {
            miniArtRestingRect
        } else {
            val sizePx = with(density) { 48.dp.toPx() }
            val left = with(density) { 28.dp.toPx() }
            val bottom = (rootSizePx.height.takeIf { it > 0 } ?: 1800) - with(density) { 36.dp.toPx() }
            Rect(left, bottom - sizePx, left + sizePx, bottom)
        }
    }

    val resolveFullArtSlotRect: () -> Rect = {
        val baseFull = if (fullArtSlotRect != Rect.Zero) {
            fullArtSlotRect
        } else {
            val artPx = with(density) { maxArtDp.toPx() }
            val w = (rootSizePx.width.takeIf { it > 0 } ?: 1080).toFloat()
            val h = (rootSizePx.height.takeIf { it > 0 } ?: 2200).toFloat()
            val left = ((w - artPx) * 0.5f).coerceAtLeast(0f)
            val top = h * 0.12f
            Rect(left, top, left + artPx, top + artPx)
        }
        val scaledFull = scaleRectAroundCenter(baseFull, playPauseScale)
        val lp = playerLyricsProgress.coerceIn(0f, 1f)
        if (lp > 0.001f && lyricsArtSlotRect != Rect.Zero) {
            lerpRect(scaledFull, lyricsArtSlotRect, lp)
        } else {
            scaledFull
        }
    }

    // Como el reproductor es la pantalla principal, Atrás en Biblioteca/Ecualizador/Búsqueda
    // retrocede en subcategorías o vuelve con animación al reproductor principal.
    val modalOpen = showQueueSheet || showSignalPath || settingsDialogVisible ||
        library.selectedAlbum != null || library.selectedArtist != null ||
        library.selectedTagGroup != null || library.inspectTrack != null
    BackHandler(enabled = !modalOpen && (!showNowPlaying || sheetProgress < 0.999f)) {
        if (sheetProgress > 0.01f) {
            animatePlayerSheet(target = 1f, fromDrag = false)
        } else if (activeSection == AppSection.LIBRARY) {
            when {
                library.activeCategory == LibraryCategory.CARPETAS && viewModel.navigateUpFolder() -> Unit
                library.activeCategory != null -> viewModel.selectCategory(null)
                else -> animatePlayerSheet(target = 1f, fromDrag = false)
            }
        } else {
            animatePlayerSheet(target = 1f, fromDrag = false)
        }
    }

    val sheetController = controller
    val sheetTrack = playback.current ?: library.tracks.firstOrNull()

    LaunchedEffect(sheetTrack?.uri, library.inspectTrack?.uri, audioConfig.autoAnalyze) {
        val currentTrack = sheetTrack
        if (library.inspectTrack == null && currentTrack != null && currentTrack.isLocal) {
            viewModel.prepareMetadata(currentTrack)
            if (audioConfig.autoAnalyze) {
                viewModel.analyzeSpectral(currentTrack)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                rootCoords = coords
                rootSizePx = coords.size
            }
            .background(
                Brush.verticalGradient(
                    listOf(Ink, InkElevated, Ink),
                ),
            ),
    ) {
        val bottomPadding = 108

        // La sección entra al minimizar y sale al volver, ligada al mismo progreso
        // del morph para que nunca haya un corte entre ambas pantallas.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val reveal = (1f - sheetProgress).coerceIn(0f, 1f)
                    alpha = reveal
                    translationY = (1f - reveal) * 28.dp.toPx()
                    val scale = 0.975f + 0.025f * reveal
                    scaleX = scale
                    scaleY = scale
                },
        ) {
            when (activeSection) {
                AppSection.LIBRARY -> PowerampLibraryWorkspace(
                    library = library,
                    hasPermission = hasPermission,
                    output = output,
                    currentUri = playback.current?.uri,
                    isPlaying = playback.isPlaying,
                    controller = controller,
                    bottomPadding = bottomPadding,
                    onBackToPlayer = { animatePlayerSheet(target = 1f, fromDrag = false) },
                    onSelectCategory = viewModel::selectCategory,
                    onQueryChange = viewModel::onQueryChange,
                    onRequestPermission = { permissionLauncher.launch(audioPermissions()) },
                    onRescan = {
                        if (hasPermission) viewModel.refresh() else permissionLauncher.launch(audioPermissions())
                    },
                    onOpenSignalPath = { showSignalPath = true },
                    onOpenQueueSheet = { showQueueSheet = true },
                    onTrackClick = { tracks, index -> playTrackList(controller, tracks, index) },
                    onAlbumClick = viewModel::selectAlbum,
                    onArtistClick = viewModel::selectArtist,
                    onTagGroupClick = viewModel::selectTagGroup,
                    onFolderClick = viewModel::navigateToFolder,
                    onNavigateUpFolder = { viewModel.navigateUpFolder() },
                    onInspectTrack = viewModel::setInspectTrack,
                    onMessage = { msg -> userMessage = msg },
                )

                AppSection.EQUALIZER -> PowerampEqualizerWorkspace(
                    bottomPadding = bottomPadding,
                    onBackToPlayer = { animatePlayerSheet(target = 1f, fromDrag = false) },
                )

                AppSection.SEARCH -> PowerampSearchWorkspace(
                    library = library,
                    hasPermission = hasPermission,
                    currentUri = playback.current?.uri,
                    playingYtmId = playingYtmId,
                    isPlaying = playback.isPlaying,
                    playbackError = playback.error,
                    controller = controller,
                    bottomPadding = bottomPadding,
                    onBackToPlayer = { animatePlayerSheet(target = 1f, fromDrag = false) },
                    onQueryChange = viewModel::onQueryChange,
                    onYtmQueryChange = viewModel::onYtmQueryChange,
                    onSearchYtm = { viewModel.searchYtm() },
                    onRequestPermission = { permissionLauncher.launch(audioPermissions()) },
                    onLocalTrackClick = { tracks, index -> playTrackList(controller, tracks, index) },
                    onAlbumClick = viewModel::selectAlbum,
                    onArtistClick = viewModel::selectArtist,
                    onInspectTrack = viewModel::setInspectTrack,
                    onYtmTrackClick = { ytmTrack ->
                        controller?.let { player ->
                            val queue = library.ytmTracks.map(YtmService::toMediaItem)
                            val startIndex = library.ytmTracks
                                .indexOfFirst { it.id == ytmTrack.id }
                                .coerceAtLeast(0)
                            QueueManager.playItems(player, queue, startIndex)
                        }
                    },
                    onMessage = { msg -> userMessage = msg },
                )
            }
        }

        MiniPlayer(
            track = sheetTrack,
            isPlaying = playback.isPlaying,
            progress = playback.progress,
            isDac = output.isDac,
            sheetProgress = sheetProgress,
            onToggle = {
                val player = controller
                if (player != null) {
                    if (player.mediaItemCount == 0 && library.tracks.isNotEmpty()) {
                        playTrackList(player, library.tracks, 0)
                    } else if (player.isPlaying) {
                        player.pause()
                    } else {
                        player.play()
                    }
                }
            },
            onNext = { controller?.seekToNext() },
            onOpen = { animatePlayerSheet(target = 1f, fromDrag = false) },
            onOpenSignalPath = { showSignalPath = true },
            onArtMeasured = { artCoords ->
                val root = rootCoords
                if (root != null && root.isAttached && artCoords.isAttached) {
                    miniArtRestingRect = root.localBoundingBoxOf(artCoords, clipBounds = false)
                }
            },
            onVerticalDragStart = {
                sheetAnimJob?.cancel()
                isSheetDragging = true
                wasSheetDragged = true
            },
            onVerticalDragDelta = { dragAmountPx ->
                val range = currentDragRangePx
                val next = (sheetProgress - dragAmountPx / range).coerceIn(0f, 1f)
                sheetProgress = next
                if (next > 0.01f) showNowPlaying = true
            },
            onVerticalDragEnd = { velocityYPxPerSec ->
                val range = currentDragRangePx
                val progressVel = -velocityYPxPerSec / range
                val shouldOpen = velocityYPxPerSec < -520f ||
                    (sheetProgress > 0.24f && velocityYPxPerSec < 420f)
                animatePlayerSheet(
                    target = if (shouldOpen) 1f else 0f,
                    initialVelocity = progressVel,
                    fromDrag = true,
                )
            },
            onVerticalDragCancel = {
                animatePlayerSheet(
                    target = if (sheetProgress > 0.5f) 1f else 0f,
                    fromDrag = true,
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        )

        // Scrim progresivo sobre la sección secundaria mientras el reproductor está abierto o en tránsito.
        if (sheetProgress > 0.001f) {
            val blockBackgroundTouches = showNowPlaying || sheetProgress > 0.08f || isSheetDragging
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = (sheetProgress.coerceIn(0f, 1f) * 0.68f).coerceIn(0f, 0.68f)
                    }
                    .background(Color.Black)
                    .then(
                        if (blockBackgroundTouches) {
                            Modifier.pointerInput(Unit) {
                                detectTapGestures {
                                    animatePlayerSheet(target = 0f, fromDrag = false)
                                }
                            }
                        } else {
                            Modifier
                        },
                    ),
            )
        }

        if (showNowPlaying || sheetProgress > 0.001f || isSheetDragging) {
            NowPlayingSheet(
                track = sheetTrack,
                controller = sheetController,
                isPlaying = playback.isPlaying,
                hasPermission = hasPermission,
                isScanning = library.isScanning,
                output = output,
                analysis = analysis,
                sheetProgress = sheetProgress,
                lyricsProgress = playerLyricsProgress,
                allowLyricsBack = !modalOpen,
                isFavorite = sheetTrack?.uri?.toString() in favoriteUris,
                onToggleFavorite = {
                    sheetTrack?.takeIf { it.isLocal }?.let { LibraryInsights.toggleFavorite(it.uri.toString()) }
                },
                interactiveEnabled = showNowPlaying || sheetProgress > 0.08f || isSheetDragging,
                maxArtDp = maxArtDp,
                waveformHeight = waveformHeight,
                playPauseScale = playPauseScale,
                miniArtRectProvider = resolveMiniArtRect,
                fullArtSlotRectProvider = resolveFullArtSlotRect,
                onFullArtSlotMeasured = { artCoords ->
                    val root = rootCoords
                    if (root != null && root.isAttached && artCoords.isAttached) {
                        fullArtSlotRect = root.localBoundingBoxOf(artCoords, clipBounds = false)
                    }
                },
                onLyricsArtSlotMeasured = { artCoords ->
                    val root = rootCoords
                    if (root != null && root.isAttached && artCoords.isAttached) {
                        lyricsArtSlotRect = root.localBoundingBoxOf(artCoords, clipBounds = false)
                    }
                },
                onLyricsDragStart = { lyricsAnimJob?.cancel() },
                onLyricsDragDelta = { amount ->
                    playerLyricsProgress = (playerLyricsProgress - amount / lyricsDragRangePx).coerceIn(0f, 1f)
                },
                onLyricsDragEnd = { velocity ->
                    val open = shouldOpenLyrics(playerLyricsProgress, velocity)
                    animateLyrics(if (open) 1f else 0f, -velocity / lyricsDragRangePx)
                },
                onLyricsDragCancel = {
                    animateLyrics(if (playerLyricsProgress > 0.5f) 1f else 0f)
                },
                onOpenLyrics = { animateLyrics(1f) },
                onCloseLyrics = { animateLyrics(0f) },
                onRequestPermission = { permissionLauncher.launch(audioPermissions()) },
                onFallbackPlayFirst = {
                    if (library.tracks.isNotEmpty()) {
                        playTrackList(controller, library.tracks, 0)
                    }
                },
                onVerticalDragStart = {
                    sheetAnimJob?.cancel()
                    isSheetDragging = true
                    wasSheetDragged = true
                },
                onVerticalDragDelta = { dragAmountPx ->
                    val range = currentDragRangePx
                    sheetProgress = (sheetProgress - dragAmountPx / range).coerceIn(0f, 1f)
                },
                onVerticalDragEnd = { velocityYPxPerSec ->
                    val range = currentDragRangePx
                    val progressVel = -velocityYPxPerSec / range
                    val shouldClose = velocityYPxPerSec > 520f ||
                        (sheetProgress < 0.74f && velocityYPxPerSec > -420f)
                    animatePlayerSheet(
                        target = if (shouldClose) 0f else 1f,
                        initialVelocity = progressVel,
                        fromDrag = true,
                    )
                },
                onVerticalDragCancel = {
                    animatePlayerSheet(
                        target = if (sheetProgress > 0.5f) 1f else 0f,
                        fromDrag = true,
                    )
                },
                onDismiss = { animatePlayerSheet(target = 0f, fromDrag = false) },
                onOpenSignalPath = { showSignalPath = true },
                onOpenLibraryShortcut = {
                    viewModel.openLibraryRoot()
                    activeSection = AppSection.LIBRARY
                    animatePlayerSheet(target = 0f, fromDrag = false)
                },
                onOpenEqualizerShortcut = {
                    activeSection = AppSection.EQUALIZER
                    animatePlayerSheet(target = 0f, fromDrag = false)
                },
                onOpenSearchShortcut = {
                    activeSection = AppSection.SEARCH
                    animatePlayerSheet(target = 0f, fromDrag = false)
                },
                onOpenSettingsShortcut = {
                    showSettings = true
                },
            )
        }

        // Carátula compartida en vuelo diagonal (MiniPlayer 48dp izq ↔ Reproductor grande)
        if (sheetProgress > 0.001f && sheetProgress < 0.998f) {
            SharedTransitionArtwork(
                track = sheetTrack,
                sheetProgress = sheetProgress,
                lyricsProgress = playerLyricsProgress,
                isSheetDragging = isSheetDragging,
                wasSheetDragged = wasSheetDragged,
                baseArtDp = maxArtDp,
                miniArtRectProvider = resolveMiniArtRect,
                fullArtSlotRectProvider = resolveFullArtSlotRect,
            )
        }

        AnimatedVisibility(
            visible = userMessage != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = if (sheetProgress < 0.2f) 92.dp else 24.dp, start = 16.dp, end = 16.dp),
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

    if (showQueueSheet && sheetController != null) {
        QueueSheet(
            controller = sheetController,
            tracks = library.tracks,
            audioConfig = audioConfig,
            onDismiss = { showQueueSheet = false },
            onMessage = { msg -> userMessage = msg },
        )
    }

    if (settingsDialogVisible) {
        FullScreenSettingsScreen(
            visibilityState = settingsVisibility,
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
            accentHue = AccentTheme.hue,
            onPreviewAccent = { AccentTheme.preview(it) },
            onSelectAccent = { AccentTheme.select(it) },
            onCommitAccent = { AccentTheme.commit() },
            onResetAccent = { AccentTheme.reset() },
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
            onRequestUsb = { UsbDacManager.requestPermission(context) },
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

    library.selectedTagGroup?.let { group ->
        TagGroupDetailSheet(
            group = group,
            currentUri = playback.current?.uri,
            isPlaying = playback.isPlaying,
            controller = controller,
            onTrackClick = { index -> playTrackList(controller, group.tracks, index) },
            onInspectClick = viewModel::setInspectTrack,
            onMessage = { msg -> userMessage = msg },
            onDismiss = { viewModel.selectTagGroup(null) },
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
internal fun SearchField(query: String, onQueryChange: (String) -> Unit) {
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
internal fun YtmView(
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
internal fun PermissionCard(onGrant: () -> Unit) {
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
internal fun LoadingLibrary() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Gold)
            Spacer(Modifier.height(16.dp))
            Text("Escaneando la biblioteca…", color = CreamMuted)
        }
    }
}

@Composable
internal fun EmptyLibrary(hasQuery: Boolean) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = if (hasQuery) "Nada coincide con la búsqueda" else "No hay archivos de audio indexados",
            color = CreamMuted,
        )
    }
}

@Composable
internal fun TrackList(
    tracks: List<Track>,
    currentUri: android.net.Uri?,
    isPlaying: Boolean,
    controller: MediaController? = null,
    onTrackClick: (Int) -> Unit,
    onInspectClick: (Track) -> Unit,
    onMessage: ((String) -> Unit)? = null,
    extraLabel: ((Track) -> String)? = null,
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
                extraLabel = extraLabel?.invoke(track),
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
    extraLabel: String? = null,
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
                        listOfNotNull(track.qualityLabel, extraLabel).joinToString(" · "),
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
internal fun AlbumView(
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
internal fun ArtistView(
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
internal fun FolderBrowserView(
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

private fun lerpFloat(start: Float, stop: Float, fraction: Float): Float =
    start + (stop - start) * fraction

private fun lerpRect(start: Rect, stop: Rect, fraction: Float): Rect = Rect(
    left = lerpFloat(start.left, stop.left, fraction),
    top = lerpFloat(start.top, stop.top, fraction),
    right = lerpFloat(start.right, stop.right, fraction),
    bottom = lerpFloat(start.bottom, stop.bottom, fraction),
)

private fun scaleRectAroundCenter(rect: Rect, scale: Float): Rect {
    val cx = rect.center.x
    val cy = rect.center.y
    val hw = rect.width * scale * 0.5f
    val hh = rect.height * scale * 0.5f
    return Rect(cx - hw, cy - hh, cx + hw, cy + hh)
}

@Composable
internal fun Artwork(
    track: Track?,
    modifier: Modifier = Modifier,
    artworkUri: Uri? = null,
    cornerRadius: Dp = 12.dp,
) {
    Box(
        modifier = modifier
            .then(if (cornerRadius > 0.dp) Modifier.clip(RoundedCornerShape(cornerRadius)) else Modifier)
            .background(InkSoft),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = CreamMuted)
        val image = artworkUri ?: track?.artworkUri
        if (image != null) {
            AsyncImage(
                model = image,
                contentDescription = track?.album,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun SharedTransitionArtwork(
    track: Track?,
    sheetProgress: Float,
    lyricsProgress: Float,
    isSheetDragging: Boolean,
    wasSheetDragged: Boolean,
    baseArtDp: Dp,
    miniArtRectProvider: () -> Rect,
    fullArtSlotRectProvider: () -> Rect,
) {
    val p = sheetProgress.coerceIn(0f, 1f)
    val edgeRamp = minOf(p / 0.08f, (1f - p) / 0.06f).coerceIn(0f, 1f)
    val midArc = sin(p * PI).toFloat().coerceIn(0f, 1f)
    val targetBlurVal = when {
        p <= 0.002f || p >= 0.998f -> 0f
        isSheetDragging -> 16f * edgeRamp
        wasSheetDragged -> 12f * midArc
        else -> 4.5f * midArc
    }
    val transitBlur by animateDpAsState(
        targetValue = targetBlurVal.dp,
        animationSpec = tween(durationMillis = 90),
        label = "sharedArtworkTransitBlur",
    )

    // El tamaño base coincide con el slot real: también cabe en pantallas estrechas,
    // y se escala/traslada en GPU sin re-layout durante cada frame.
    Box(
        modifier = Modifier
            .size(baseArtDp)
            .graphicsLayer {
                val prog = sheetProgress.coerceIn(0f, 1f)
                val miniR = miniArtRectProvider()
                val fullR = fullArtSlotRectProvider()
                val curR = lerpRect(miniR, fullR, prog)
                val basePx = baseArtDp.toPx().coerceAtLeast(1f)
                val sx = (curR.width / basePx).coerceAtLeast(0.05f)
                val sy = (curR.height / basePx).coerceAtLeast(0.05f)
                translationX = curR.left
                translationY = curR.top
                scaleX = sx
                scaleY = sy
                transformOrigin = TransformOrigin(0f, 0f)
                val fullCorner = 20.dp - 8.dp * lyricsProgress.coerceIn(0f, 1f)
                val desiredCornerDp = 12.dp + (fullCorner - 12.dp) * prog
                shape = RoundedCornerShape(desiredCornerDp / sx)
                clip = true
                shadowElevation = (4.dp + 20.dp * sin(prog * PI).toFloat()).toPx()
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (transitBlur > 0.2.dp) {
                        Modifier.blur(transitBlur, edgeTreatment = BlurredEdgeTreatment.Rectangle)
                    } else {
                        Modifier
                    },
                ),
        ) {
            Artwork(
                track = track,
                modifier = Modifier.fillMaxSize(),
                cornerRadius = 0.dp,
            )
        }
    }
}

@Composable
private fun MiniPlayer(
    track: Track?,
    isPlaying: Boolean,
    progress: Float,
    isDac: Boolean,
    sheetProgress: Float,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onOpen: () -> Unit,
    onOpenSignalPath: () -> Unit,
    onArtMeasured: (LayoutCoordinates) -> Unit,
    onVerticalDragStart: () -> Unit,
    onVerticalDragDelta: (Float) -> Unit,
    onVerticalDragEnd: (Float) -> Unit,
    onVerticalDragCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnDragStart by rememberUpdatedState(onVerticalDragStart)
    val currentOnDragDelta by rememberUpdatedState(onVerticalDragDelta)
    val currentOnDragEnd by rememberUpdatedState(onVerticalDragEnd)
    val currentOnDragCancel by rememberUpdatedState(onVerticalDragCancel)

    // Se mide fuera de graphicsLayer: es la posición en reposo incluso en el primer vuelo.
    Box(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    val p = sheetProgress.coerceIn(0f, 1f)
                    alpha = (1f - p / 0.28f).coerceIn(0f, 1f)
                    translationY = -p * 42.dp.toPx()
                }
                .pointerInput(Unit) {
                    val tracker = VelocityTracker()
                    detectVerticalDragGestures(
                        onDragStart = {
                            tracker.resetTracking()
                            currentOnDragStart()
                        },
                        onVerticalDrag = { change, amount ->
                            change.consume()
                            tracker.addPosition(change.uptimeMillis, change.position)
                            currentOnDragDelta(amount)
                        },
                        onDragEnd = { currentOnDragEnd(tracker.calculateVelocity().y) },
                        onDragCancel = { currentOnDragCancel() },
                    )
                },
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
                        .clickable(enabled = sheetProgress < 0.08f, onClick = onOpen)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .graphicsLayer { alpha = if (sheetProgress > 0.001f) 0f else 1f },
                    ) {
                        Artwork(track, Modifier.fillMaxSize())
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                track?.title ?: "Sin canción seleccionada",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Cream,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f, fill = false),
                            )
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
                        Text(
                            track?.let { "${it.effectiveArtist} · ${it.codecLabel}" } ?: "Abre el reproductor",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = CreamMuted,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    IconButton(onClick = onToggle, enabled = sheetProgress < 0.1f && track != null) {
                        Icon(
                            if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                            tint = Gold,
                        )
                    }
                    IconButton(onClick = onNext, enabled = sheetProgress < 0.1f && track != null) {
                        Icon(Icons.Rounded.SkipNext, contentDescription = "Siguiente", tint = Cream)
                    }
                }
            }
        }
        // 4 dp de la barra + 12 dp de padding de la fila.
        Box(
            Modifier
                .padding(start = 12.dp, top = 16.dp)
                .size(48.dp)
                .onGloballyPositioned(onArtMeasured),
        )
    }
}

@Composable
private fun NowPlayingSheet(
    track: Track?,
    controller: MediaController?,
    isPlaying: Boolean,
    hasPermission: Boolean,
    isScanning: Boolean,
    output: OutputDevice,
    analysis: com.kukosin.app.data.analysis.AudioAnalysis?,
    sheetProgress: Float,
    lyricsProgress: Float,
    allowLyricsBack: Boolean,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    interactiveEnabled: Boolean,
    maxArtDp: Dp,
    waveformHeight: Dp,
    playPauseScale: Float,
    miniArtRectProvider: () -> Rect,
    fullArtSlotRectProvider: () -> Rect,
    onFullArtSlotMeasured: (LayoutCoordinates) -> Unit,
    onLyricsArtSlotMeasured: (LayoutCoordinates) -> Unit,
    onLyricsDragStart: () -> Unit,
    onLyricsDragDelta: (Float) -> Unit,
    onLyricsDragEnd: (Float) -> Unit,
    onLyricsDragCancel: () -> Unit,
    onOpenLyrics: () -> Unit,
    onCloseLyrics: () -> Unit,
    onRequestPermission: () -> Unit,
    onFallbackPlayFirst: () -> Unit,
    onVerticalDragStart: () -> Unit,
    onVerticalDragDelta: (Float) -> Unit,
    onVerticalDragEnd: (Float) -> Unit,
    onVerticalDragCancel: () -> Unit,
    onDismiss: () -> Unit,
    onOpenSignalPath: () -> Unit,
    onOpenLibraryShortcut: () -> Unit,
    onOpenEqualizerShortcut: () -> Unit,
    onOpenSearchShortcut: () -> Unit,
    onOpenSettingsShortcut: () -> Unit,
) {
    var position by remember(controller) { mutableLongStateOf(controller?.currentPosition ?: 0L) }
    var duration by remember(controller) { mutableLongStateOf((controller?.duration ?: 0L).coerceAtLeast(0L)) }
    var dragX by remember { mutableFloatStateOf(0f) }
    var horizontalJob by remember { mutableStateOf<Job?>(null) }
    var previousArt by remember { mutableStateOf<Uri?>(null) }
    var nextArt by remember { mutableStateOf<Uri?>(null) }
    var hasPrevious by remember { mutableStateOf(false) }
    var hasNext by remember { mutableStateOf(false) }
    val gestureScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val artworkWidthPx = with(density) { maxArtDp.toPx() }
    val cornerArtWidthPx = with(density) { 56.dp.toPx() }
    var sheetCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var centerArtRect by remember { mutableStateOf(Rect.Zero) }
    var cornerArtRect by remember { mutableStateOf(Rect.Zero) }

    // Los gestos largos leen siempre los callbacks y progresos más recientes, sin reiniciar
    // pointerInput a mitad del arrastre cuando cambia el progreso de letras o del reproductor.
    val currentLyricsProgress by rememberUpdatedState(lyricsProgress)
    val currentSheetProgress by rememberUpdatedState(sheetProgress)
    val currentInteractive by rememberUpdatedState(interactiveEnabled)
    val currentArtScale by rememberUpdatedState(playPauseScale)
    val currentOnLyricsStart by rememberUpdatedState(onLyricsDragStart)
    val currentOnLyricsDelta by rememberUpdatedState(onLyricsDragDelta)
    val currentOnLyricsEnd by rememberUpdatedState(onLyricsDragEnd)
    val currentOnLyricsCancel by rememberUpdatedState(onLyricsDragCancel)
    val currentOnSheetStart by rememberUpdatedState(onVerticalDragStart)
    val currentOnSheetDelta by rememberUpdatedState(onVerticalDragDelta)
    val currentOnSheetEnd by rememberUpdatedState(onVerticalDragEnd)
    val currentOnSheetCancel by rememberUpdatedState(onVerticalDragCancel)

    BackHandler(enabled = allowLyricsBack && lyricsProgress > 0.01f && sheetProgress > 0.9f) {
        onCloseLyrics()
    }

    val currentLyrics = analysis?.takeIf { track != null && it.uri == track.uri }?.lyrics
    val context = LocalContext.current
    var realWaveform by remember(track?.uri) { mutableStateOf<List<Float>?>(null) }
    val flatWaveform = remember { List(180) { 0f } }
    LaunchedEffect(track?.uri) {
        realWaveform = null
        val selected = track
        if (selected != null && selected.isLocal) {
            realWaveform = com.kukosin.app.data.analysis.WaveformExtractor.getWaveform(context, selected, 180)
        }
    }
    LaunchedEffect(controller) {
        while (isActive) {
            position = controller?.currentPosition ?: 0L
            duration = (controller?.duration ?: 0L).coerceAtLeast(0L)
            delay(32L)
        }
    }

    val artworkBlur by animateDpAsState(
        targetValue = if (dragX != 0f) abs(dragX).coerceAtMost(12f).dp else 0.dp,
        animationSpec = tween(120),
        label = "artworkDragBlur",
    )
    val artworkAlpha by animateFloatAsState(
        targetValue = if (dragX != 0f) 1f - (abs(dragX) / 90f).coerceIn(0f, 1f) else 1f,
        animationSpec = tween(120),
        label = "artworkDragAlpha",
    )

    fun artworkAt(offset: Int): Uri? {
        val player = controller ?: return null
        val index = player.currentMediaItemIndex + offset
        if (index !in 0 until player.mediaItemCount) return null
        val item = player.getMediaItemAt(index)
        val uri = item.localConfiguration?.uri
        return if (uri != null && (uri.scheme == "content" || uri.scheme == "file")) {
            EmbeddedArtwork.uriFor(uri)
        } else {
            item.mediaMetadata.artworkUri
        }
    }

    // La animación se ejecuta fuera del AwaitPointerEventScope restringido; dragX es
    // la única fuente de verdad tanto al arrastrar como al completar/cancelar el gesto.
    fun settleHorizontal(step: Float, velocityX: Float, cancelled: Boolean) {
        val player = controller
        val threshold = (step * 0.35f).coerceAtLeast(30f)
        val flingThreshold = step * SWIPE_FLING_FACTOR
        val goNext = dragX < -threshold || velocityX < -flingThreshold
        val goPrevious = dragX > threshold || velocityX > flingThreshold
        horizontalJob?.cancel()
        horizontalJob = gestureScope.launch {
            suspend fun snapBack() {
                animate(dragX, 0f, animationSpec = spring(0.9f, Spring.StiffnessMediumLow)) { value, _ ->
                    dragX = value
                }
            }
            val next = !cancelled && goNext && player?.hasNextMediaItem() == true
            val previous = !cancelled && goPrevious && player?.hasPreviousMediaItem() == true
            if (player != null && (next || previous)) {
                animate(
                    initialValue = dragX,
                    targetValue = if (next) -step else step,
                    animationSpec = tween(240, easing = FastOutSlowInEasing),
                ) { value, _ -> dragX = value }
                val oldIndex = player.currentMediaItemIndex
                val switched = try {
                    if (next) player.seekToNext() else player.seekToPrevious()
                    awaitIndexChange(player, oldIndex)
                } catch (cancel: kotlinx.coroutines.CancellationException) {
                    throw cancel
                } catch (error: Exception) {
                    Log.w("KukosinSwipe", "No se pudo cambiar de pista", error)
                    false
                }
                if (switched) {
                    // La vecina permanece centrada hasta que el controlador publique la nueva pista.
                    withFrameNanos { }
                    dragX = 0f
                } else {
                    snapBack()
                }
            } else {
                snapBack()
            }
        }
    }

    // El hit-test vive en el contenedor estable, por encima de la carátula dibujada.
    // El rectángulo sigue la portada visual; una vez iniciado, el gesto usa coordenadas
    // del reproductor que no se mueven mientras aparecen las letras.
    val scaledCenterArt = if (centerArtRect == Rect.Zero) Rect.Zero
        else scaleRectAroundCenter(centerArtRect, playPauseScale)
    val visibleCoverRect = if (scaledCenterArt == Rect.Zero) Rect.Zero else {
        val destination = if (cornerArtRect != Rect.Zero) {
            lerpRect(scaledCenterArt, cornerArtRect, lyricsProgress.coerceIn(0f, 1f))
        } else scaledCenterArt
        Rect(destination.left + dragX, destination.top, destination.right + dragX, destination.bottom)
    }
    val hitBounds = expandedCoverHitBounds(visibleCoverRect, with(density) { 12.dp.toPx() })
    var swipeStep by remember { mutableFloatStateOf(1f) }
    val coverGesture = rememberCoverGestureModifier(
        enabled = interactiveEnabled && sheetProgress >= 0.98f,
        hitBounds = hitBounds,
        lyricsProgress = lyricsProgress,
        controllerKey = controller,
        trackKey = track?.uri,
        onStart = { axis ->
            when (axis) {
                CoverGestureAxis.TRACK -> {
                    horizontalJob?.cancel()
                    swipeStep = lerpFloat(
                        artworkWidthPx * currentArtScale,
                        cornerArtWidthPx,
                        currentLyricsProgress.coerceIn(0f, 1f),
                    ).coerceAtLeast(1f)
                    hasPrevious = controller?.hasPreviousMediaItem() == true
                    hasNext = controller?.hasNextMediaItem() == true
                    previousArt = artworkAt(-1)
                    nextArt = artworkAt(1)
                }
                CoverGestureAxis.LYRICS -> currentOnLyricsStart()
                CoverGestureAxis.MINIMIZE -> currentOnSheetStart()
            }
        },
        onDrag = { axis, dx, dy ->
            when (axis) {
                CoverGestureAxis.TRACK -> {
                    val raw = dragX + dx
                    val blocked = (raw > 0f && !hasPrevious) || (raw < 0f && !hasNext)
                    dragX = if (blocked) dragX + dx * SWIPE_EDGE_RESISTANCE
                        else raw.coerceIn(-swipeStep, swipeStep)
                }
                CoverGestureAxis.LYRICS -> currentOnLyricsDelta(dy)
                CoverGestureAxis.MINIMIZE -> currentOnSheetDelta(dy)
            }
        },
        onEnd = { axis, velocityX, velocityY ->
            when (axis) {
                CoverGestureAxis.TRACK -> settleHorizontal(swipeStep, velocityX, cancelled = false)
                CoverGestureAxis.LYRICS -> currentOnLyricsEnd(velocityY)
                CoverGestureAxis.MINIMIZE -> currentOnSheetEnd(velocityY)
            }
        },
        onCancel = { axis ->
            when (axis) {
                CoverGestureAxis.TRACK -> settleHorizontal(swipeStep, 0f, cancelled = true)
                CoverGestureAxis.LYRICS -> currentOnLyricsCancel()
                CoverGestureAxis.MINIMIZE -> currentOnSheetCancel()
            }
        },
    )

    // La cabecera permite cerrar letras con un arrastre, sin robar el scroll del panel.
    val headerDrag = Modifier.pointerInput(Unit) {
        val tracker = VelocityTracker()
        var draggingLyrics = false
        detectVerticalDragGestures(
            onDragStart = {
                if (currentInteractive) {
                    tracker.resetTracking()
                    draggingLyrics = currentLyricsProgress > 0.01f
                    if (draggingLyrics) currentOnLyricsStart() else currentOnSheetStart()
                }
            },
            onVerticalDrag = { change, amount ->
                if (currentInteractive) {
                    change.consume()
                    tracker.addPosition(change.uptimeMillis, change.position)
                    if (draggingLyrics) currentOnLyricsDelta(amount) else currentOnSheetDelta(amount)
                }
            },
            onDragEnd = {
                if (currentInteractive) {
                    val velocity = tracker.calculateVelocity().y
                    if (draggingLyrics) currentOnLyricsEnd(velocity) else currentOnSheetEnd(velocity)
                }
            },
            onDragCancel = {
                if (currentInteractive) {
                    if (draggingLyrics) currentOnLyricsCancel() else currentOnSheetCancel()
                }
            },
        )
    }
    // Este nodo permanece montado durante el gesto. Solo inicia el minimizado si
    // el arrastre empezó fuera de letras y va hacia abajo; no roba su scroll.
    val bodyDismiss = Modifier.pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            if (!currentInteractive || currentLyricsProgress > 0.01f || currentSheetProgress < 0.98f) {
                return@awaitEachGesture
            }
            val tracker = VelocityTracker()
            tracker.addPosition(down.uptimeMillis, down.position)
            var totalX = 0f
            var totalY = 0f
            var dragging = false
            var finished = false
            try {
                while (true) {
                    val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                    tracker.addPosition(change.uptimeMillis, change.position)
                    if (change.isConsumed) break
                    if (!change.pressed) {
                        if (dragging) {
                            currentOnSheetEnd(tracker.calculateVelocity().y)
                            change.consume()
                        }
                        finished = true
                        break
                    }
                    totalX += change.position.x - change.previousPosition.x
                    val deltaY = change.position.y - change.previousPosition.y
                    totalY += deltaY
                    val amount = if (!dragging) {
                        val slop = viewConfiguration.touchSlop
                        if (maxOf(abs(totalX), abs(totalY)) <= slop) continue
                        if (totalY <= 0f || abs(totalX) > abs(totalY)) break
                        dragging = true
                        currentOnSheetStart()
                        (totalY - slop).coerceAtLeast(0f)
                    } else deltaY
                    currentOnSheetDelta(amount)
                    change.consume()
                }
            } finally {
                if (!finished && dragging) currentOnSheetCancel()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { sheetCoords = it }
            .then(coverGesture)
            .then(bodyDismiss),
    ) {
        // El fondo sigue a la carátula durante el morph con el MiniPlayer.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val p = sheetProgress.coerceIn(0f, 1f)
                    val full = fullArtSlotRectProvider()
                    val current = lerpRect(miniArtRectProvider(), full, p)
                    translationY = (current.top - full.top).coerceAtLeast(0f)
                    alpha = ((p - 0.04f) / 0.76f).coerceIn(0f, 1f)
                    val topCorner = 28.dp * (1f - p)
                    shape = RoundedCornerShape(topStart = topCorner, topEnd = topCorner)
                    clip = true
                }
                .background(Brush.verticalGradient(listOf(InkElevated, Ink, Ink)))
                .pointerInput(Unit) { detectTapGestures { } },
        )

        // Los dos slots se miden sin capas transformadas en sus ancestros. Así el arte
        // compartido y el arte de letras utilizan exactamente los mismos rectángulos.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(headerDrag)
                    .graphicsLayer {
                        val p = sheetProgress.coerceIn(0f, 1f)
                        val full = fullArtSlotRectProvider()
                        val current = lerpRect(miniArtRectProvider(), full, p)
                        translationY = (current.top - full.top).coerceAtLeast(0f)
                        alpha = ((p - 0.12f) / 0.72f).coerceIn(0f, 1f)
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp, bottom = 2.dp)
                        .width(38.dp)
                        .height(4.5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(CreamMuted.copy(alpha = 0.45f)),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onDismiss, enabled = interactiveEnabled) {
                        Icon(Icons.Rounded.KeyboardArrowDown, "Minimizar", tint = CreamMuted, modifier = Modifier.size(32.dp))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("EN REPRODUCCIÓN", style = MaterialTheme.typography.labelMedium, color = Gold, fontWeight = FontWeight.Bold)
                        Text(
                            if (track == null) "Tu música" else if (track.isLocal) "Música local" else "YouTube Music",
                            style = MaterialTheme.typography.labelSmall,
                            color = CreamMuted,
                        )
                    }
                    if (track?.isLocal == true) {
                        IconButton(onClick = onToggleFavorite, enabled = interactiveEnabled) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = if (isFavorite) "Quitar de favoritos" else "Añadir a favoritos",
                                tint = if (isFavorite) Gold else CreamMuted,
                                modifier = Modifier.size(25.dp),
                            )
                        }
                    } else {
                        Spacer(Modifier.size(48.dp))
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(maxArtDp)
                            .onGloballyPositioned { coords ->
                                val root = sheetCoords
                                if (root != null && root.isAttached && coords.isAttached) {
                                    centerArtRect = root.localBoundingBoxOf(coords, clipBounds = false)
                                    onFullArtSlotMeasured(coords)
                                }
                            }
                            .then(
                                if (lyricsProgress < 0.5f) {
                                    Modifier.semantics { contentDescription = "Carátula. Toca o desliza hacia arriba para abrir letras" }
                                } else {
                                    Modifier.clearAndSetSemantics { }
                                },
                            )
                            .clickable(enabled = interactiveEnabled, onClick = onOpenLyrics),
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                val p = sheetProgress.coerceIn(0f, 1f)
                                val lp = lyricsProgress.coerceIn(0f, 1f)
                                val full = fullArtSlotRectProvider()
                                val current = lerpRect(miniArtRectProvider(), full, p)
                                translationY = current.bottom - full.bottom - 30.dp.toPx() * lp
                                translationX = -20.dp.toPx() * lp
                                alpha = ((p - 0.10f) / 0.72f).coerceIn(0f, 1f) * (1f - lp * 1.4f).coerceIn(0f, 1f)
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = track?.title ?: "Tu música empieza aquí",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Cream,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = track?.let { "${it.effectiveArtist} — ${it.album}" }
                                ?: if (isScanning) "Escaneando tu biblioteca…" else "Elige una canción en Biblioteca o Búsqueda",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CreamMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(5.dp))
                        if (track != null) {
                            Text(
                                text = "Desliza ↑ o toca la carátula para abrir letras",
                                style = MaterialTheme.typography.labelSmall,
                                color = CreamMuted.copy(alpha = 0.8f),
                                maxLines = 1,
                            )
                            Spacer(Modifier.height(5.dp))
                            Surface(
                                color = InkSoft,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Gold.copy(alpha = 0.42f)),
                                modifier = Modifier.clickable(enabled = interactiveEnabled, onClick = onOpenSignalPath),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        if (output.isDac) Icons.Rounded.Usb else Icons.Rounded.GraphicEq,
                                        contentDescription = null,
                                        tint = Gold,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "${track.qualityLabel} · ${output.name}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Gold,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        } else if (!hasPermission) {
                            Button(
                                onClick = onRequestPermission,
                                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink),
                                shape = RoundedCornerShape(12.dp),
                            ) { Text("Permitir acceso a audio") }
                        }
                        Spacer(Modifier.height(10.dp))
                        KukosinWaveformSlider(
                            progress = if (duration > 0) position.toFloat() / duration else 0f,
                            onSeek = { fraction -> if (interactiveEnabled) controller?.seekTo((fraction * duration).toLong()) },
                            waveHeight = waveformHeight,
                            waveform = if (track == null) flatWaveform else realWaveform,
                            trackKey = track?.uri?.toString(),
                            isPlaying = isPlaying,
                            enabled = controller != null && track != null && interactiveEnabled,
                            onPlayPause = {
                                if (interactiveEnabled && controller != null) {
                                    when {
                                        controller.mediaItemCount == 0 -> onFallbackPlayFirst()
                                        controller.isPlaying -> controller.pause()
                                        else -> controller.play()
                                    }
                                }
                            },
                            onPrev = { if (interactiveEnabled) controller?.seekToPrevious() },
                            onNext = { if (interactiveEnabled) controller?.seekToNext() },
                            onShuffle = { if (interactiveEnabled && controller != null) QueueManager.toggleShuffle(controller) },
                            onRepeat = {
                                if (interactiveEnabled && controller != null) {
                                    controller.repeatMode = when (controller.repeatMode) {
                                        Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                                        Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                                        else -> Player.REPEAT_MODE_OFF
                                    }
                                }
                            },
                            positionLabel = formatTime(position),
                            durationLabel = formatTime(duration),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .then(if (lyricsProgress > 0.01f) headerDrag else Modifier),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .onGloballyPositioned { coords ->
                                    val root = sheetCoords
                                    if (root != null && root.isAttached && coords.isAttached) {
                                        cornerArtRect = root.localBoundingBoxOf(coords, clipBounds = false)
                                        onLyricsArtSlotMeasured(coords)
                                    }
                                }
                                .then(
                                    if (lyricsProgress > 0.01f) {
                                        Modifier
                                            .semantics { contentDescription = "Carátula. Toca para cerrar letras" }
                                            .clickable(onClick = onCloseLyrics)
                                    } else Modifier,
                                ),
                        )
                        if (lyricsProgress > 0.01f) {
                            Spacer(Modifier.width(12.dp))
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .graphicsLayer {
                                        alpha = ((lyricsProgress - 0.08f) / 0.62f).coerceIn(0f, 1f) *
                                            ((sheetProgress - 0.45f) / 0.55f).coerceIn(0f, 1f)
                                        translationX = (1f - lyricsProgress) * 18.dp.toPx()
                                    },
                            ) {
                                Text(track?.title ?: "Sin canción seleccionada", style = MaterialTheme.typography.titleMedium, color = Cream, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(track?.effectiveArtist ?: "Tu música", style = MaterialTheme.typography.bodySmall, color = CreamMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(track?.qualityLabel ?: "Letras", style = MaterialTheme.typography.labelSmall, color = Gold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            IconButton(onClick = onCloseLyrics, enabled = interactiveEnabled) {
                                Icon(Icons.Rounded.KeyboardArrowDown, "Cerrar letras", tint = Gold)
                            }
                        }
                    }
                    if (lyricsProgress > 0.01f) {
                        LyricsPanel(
                            lyrics = currentLyrics,
                            positionMs = position,
                            onLineClick = if (controller != null) { timeMs -> controller.seekTo(timeMs) } else null,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .graphicsLayer {
                                    val lp = lyricsProgress.coerceIn(0f, 1f)
                                    alpha = ((lp - 0.08f) / 0.72f).coerceIn(0f, 1f) *
                                        ((sheetProgress - 0.45f) / 0.55f).coerceIn(0f, 1f)
                                    translationY = (1f - lp) * 72.dp.toPx()
                                },
                        )
                    }
                }
            }

            PlayerShortcutBar(
                enabled = interactiveEnabled,
                onOpenLibrary = onOpenLibraryShortcut,
                onOpenEqualizer = onOpenEqualizerShortcut,
                onOpenSearch = onOpenSearchShortcut,
                onOpenSettings = onOpenSettingsShortcut,
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 8.dp)
                    .graphicsLayer {
                        val p = sheetProgress.coerceIn(0f, 1f)
                        val full = fullArtSlotRectProvider()
                        val current = lerpRect(miniArtRectProvider(), full, p)
                        translationY = current.bottom - full.bottom
                        alpha = ((p - 0.25f) / 0.65f).coerceIn(0f, 1f)
                    },
            )
        }

        if (sheetProgress >= 0.998f && centerArtRect != Rect.Zero) {
            val lp = lyricsProgress.coerceIn(0f, 1f)
            val step = lerpFloat(artworkWidthPx * playPauseScale, cornerArtWidthPx, lp).coerceAtLeast(1f)
            val previous = dragX > 0f
            val showNeighbor = dragX != 0f && if (previous) hasPrevious else hasNext
            if (showNeighbor) {
                PlayerMorphingArtwork(
                    track = null,
                    artworkOverride = if (previous) previousArt else nextArt,
                    centerRect = centerArtRect,
                    cornerRect = cornerArtRect,
                    lyricsProgress = lp,
                    baseArtDp = maxArtDp,
                    playPauseScale = playPauseScale,
                    offsetX = if (previous) dragX - step else dragX + step,
                    alpha = (abs(dragX) / step).coerceIn(0f, 1f),
                    blur = 0.dp,
                )
            }
            PlayerMorphingArtwork(
                track = track,
                centerRect = centerArtRect,
                cornerRect = cornerArtRect,
                lyricsProgress = lp,
                baseArtDp = maxArtDp,
                playPauseScale = playPauseScale,
                offsetX = dragX,
                alpha = artworkAlpha,
                blur = artworkBlur,
            )
        }
    }
}

/** La misma carátula se desplaza y encoge del centro a la esquina superior izquierda. */
@Composable
private fun PlayerMorphingArtwork(
    track: Track?,
    centerRect: Rect,
    cornerRect: Rect,
    lyricsProgress: Float,
    baseArtDp: Dp,
    playPauseScale: Float,
    offsetX: Float,
    alpha: Float,
    blur: Dp,
    artworkOverride: Uri? = null,
) {
    Box(
        modifier = Modifier
            .size(baseArtDp)
            .graphicsLayer {
                val lp = lyricsProgress.coerceIn(0f, 1f)
                val center = scaleRectAroundCenter(centerRect, playPauseScale)
                val corner = if (cornerRect == Rect.Zero) center else cornerRect
                val destination = lerpRect(center, corner, lp)
                val basePx = baseArtDp.toPx().coerceAtLeast(1f)
                val sx = (destination.width / basePx).coerceAtLeast(0.01f)
                val sy = (destination.height / basePx).coerceAtLeast(0.01f)
                translationX = destination.left + offsetX
                translationY = destination.top
                scaleX = sx
                scaleY = sy
                transformOrigin = TransformOrigin(0f, 0f)
                shape = RoundedCornerShape((20.dp - 8.dp * lp) / sx)
                clip = true
                this.alpha = alpha.coerceIn(0f, 1f)
            },
    ) {
        Artwork(
            track = track,
            artworkUri = artworkOverride,
            cornerRadius = 0.dp,
            modifier = Modifier
                .fillMaxSize()
                .then(if (blur > 0.2.dp) Modifier.blur(blur, edgeTreatment = BlurredEdgeTreatment.Rectangle) else Modifier),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AudioSignalPathSheet(
    track: Track?,
    output: OutputDevice,
    onRequestUsb: () -> Unit,
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
            if (output.isUsb && !output.hasUsbPermission) {
                TextButton(onClick = onRequestUsb) {
                    Text("Conceder permiso USB Host", color = Gold, fontWeight = FontWeight.Bold)
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
    visibilityState: MutableTransitionState<Boolean>,
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
    accentHue: Float,
    onPreviewAccent: (Float) -> Unit,
    onSelectAccent: (AccentPreset) -> Unit,
    onCommitAccent: () -> Unit,
    onResetAccent: () -> Unit,
    onRescan: () -> Unit,
    onDismiss: () -> Unit,
) {
    val transition = rememberTransition(visibilityState, label = "settingsScreen")
    val reveal by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = if (targetState) 300 else 220, easing = FastOutSlowInEasing)
        },
        label = "settingsReveal",
    ) { visible -> if (visible) 1f else 0f }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = reveal
                    translationY = (1f - reveal) * 32.dp.toPx()
                    val scale = 0.975f + 0.025f * reveal
                    scaleX = scale
                    scaleY = scale
                },
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
                        Text("Personalización", style = MaterialTheme.typography.labelLarge, color = Gold, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            color = InkSoft,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Color de Acento", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Cream)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "El dorado de fábrica se puede cambiar por otro tono. Se aplica a toda la app y queda guardado.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CreamMuted,
                                )
                                Spacer(Modifier.height(14.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(AccentPreset.entries.toList()) { preset ->
                                        val selected = AccentTheme.isSelected(preset)
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { onSelectAccent(preset) }
                                                .padding(vertical = 4.dp, horizontal = 2.dp),
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(accentColor(preset.hueDegrees))
                                                    .then(
                                                        if (selected) {
                                                            Modifier.border(2.dp, Cream, CircleShape)
                                                        } else {
                                                            Modifier
                                                        },
                                                    ),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                if (selected) {
                                                    Icon(
                                                        Icons.Rounded.Check,
                                                        contentDescription = null,
                                                        tint = Ink,
                                                        modifier = Modifier.size(18.dp),
                                                    )
                                                }
                                            }
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                preset.label,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (selected) Cream else CreamMuted,
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Tono fino", style = MaterialTheme.typography.labelMedium, color = CreamMuted)
                                    Text(
                                        "${accentHue.toInt()}°",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Gold,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                Slider(
                                    value = accentHue,
                                    onValueChange = onPreviewAccent,
                                    onValueChangeFinished = onCommitAccent,
                                    valueRange = 0f..360f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Gold,
                                        activeTrackColor = Gold,
                                        inactiveTrackColor = Line,
                                    ),
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                ) {
                                    Text(
                                        "Volver al oro",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Gold,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { onResetAccent() }
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                    )
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
