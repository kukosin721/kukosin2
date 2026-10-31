package com.kukosin.app.ui

import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Piano
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Usb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.session.MediaController
import coil.compose.AsyncImage
import com.kukosin.app.data.AlbumGroup
import com.kukosin.app.data.ArtistGroup
import com.kukosin.app.data.FolderNode
import com.kukosin.app.data.LibraryInsights
import com.kukosin.app.data.TagCategoryGroup
import com.kukosin.app.data.Track
import com.kukosin.app.data.formatTime
import com.kukosin.app.data.ytm.YtmTrack
import com.kukosin.app.playback.OutputDevice
import com.kukosin.app.playback.QueueManager
import com.kukosin.app.ui.theme.Cream
import com.kukosin.app.ui.theme.CreamMuted
import com.kukosin.app.ui.theme.Gold
import com.kukosin.app.ui.theme.Ink
import com.kukosin.app.ui.theme.InkElevated
import com.kukosin.app.ui.theme.InkSoft
import com.kukosin.app.ui.theme.Line
import kotlin.math.roundToInt

internal enum class AppSection {
    LIBRARY,
    EQUALIZER,
    SEARCH,
}

/**
 * Fila inferior estilo Poweramp con los 4 accesos rápidos principales del reproductor:
 * Biblioteca · Ecualizador · Búsqueda · Configuración
 */
@Composable
internal fun PlayerShortcutBar(
    enabled: Boolean,
    onOpenLibrary: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShortcutPillButton(
            icon = Icons.Rounded.LibraryMusic,
            label = "Biblioteca",
            enabled = enabled,
            onClick = onOpenLibrary,
            modifier = Modifier.weight(1f),
        )
        ShortcutPillButton(
            icon = Icons.Rounded.Tune,
            label = "Ecualizador",
            enabled = enabled,
            onClick = onOpenEqualizer,
            modifier = Modifier.weight(1f),
        )
        ShortcutPillButton(
            icon = Icons.Rounded.Search,
            label = "Búsqueda",
            enabled = enabled,
            onClick = onOpenSearch,
            modifier = Modifier.weight(1f),
        )
        ShortcutPillButton(
            icon = Icons.Rounded.Settings,
            label = "Configuración",
            enabled = enabled,
            onClick = onOpenSettings,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ShortcutPillButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 130, easing = FastOutSlowInEasing),
        label = "shortcutPress",
    )
    Surface(
        color = InkSoft,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Gold.copy(alpha = 0.28f)),
        modifier = modifier
            .height(64.dp)
            .graphicsLayer { scaleX = pressScale; scaleY = pressScale }
            .clip(RoundedCornerShape(14.dp))
            .clickable(interactionSource = interaction, indication = LocalIndication.current, enabled = enabled, onClick = onClick),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 2.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                lineHeight = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Cream,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private data class CategoryCardSpec(
    val category: LibraryCategory,
    val icon: ImageVector,
    val subtitle: String,
    val badge: String? = null,
)

@Composable
internal fun PowerampLibraryWorkspace(
    library: LibraryUiState,
    hasPermission: Boolean,
    output: OutputDevice,
    currentUri: Uri?,
    isPlaying: Boolean,
    controller: MediaController?,
    bottomPadding: Int,
    onBackToPlayer: () -> Unit,
    onSelectCategory: (LibraryCategory?) -> Unit,
    onQueryChange: (String) -> Unit,
    onRequestPermission: () -> Unit,
    onRescan: () -> Unit,
    onOpenSignalPath: () -> Unit,
    onOpenQueueSheet: () -> Unit,
    onTrackClick: (List<Track>, Int) -> Unit,
    onAlbumClick: (AlbumGroup) -> Unit,
    onArtistClick: (ArtistGroup) -> Unit,
    onTagGroupClick: (TagCategoryGroup) -> Unit,
    onFolderClick: (FolderNode) -> Unit,
    onNavigateUpFolder: () -> Unit,
    onInspectTrack: (Track) -> Unit,
    onMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeCategory = library.activeCategory
    val queueSnapshot = rememberQueueSnapshot(controller, library.tracks)
    val queueCount = queueSnapshot.totalCount
    val favoriteUris by LibraryInsights.favorites.collectAsStateWithLifecycle()
    val playCounts by LibraryInsights.playCounts.collectAsStateWithLifecycle()
    val favoriteTracks = remember(library.tracks, library.query, favoriteUris) {
        library.filteredTracks.filter { it.uri.toString() in favoriteUris }
    }
    val mostPlayedTracks = remember(library.tracks, library.query, playCounts) {
        library.filteredTracks
            .filter { (playCounts[it.uri.toString()] ?: 0) > 0 }
            .sortedWith(compareByDescending<Track> { playCounts[it.uri.toString()] ?: 0 }
                .thenBy { it.title.lowercase() })
    }
    val favoriteCount = remember(library.tracks, favoriteUris) {
        library.tracks.count { it.uri.toString() in favoriteUris }
    }
    val playedCount = remember(library.tracks, playCounts) {
        library.tracks.count { (playCounts[it.uri.toString()] ?: 0) > 0 }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        // Barra superior estilo Poweramp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = {
                    when {
                        activeCategory == LibraryCategory.CARPETAS && library.folderPath.size > 1 -> onNavigateUpFolder()
                        activeCategory != null -> onSelectCategory(null)
                        else -> onBackToPlayer()
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Volver",
                    tint = Gold,
                    modifier = Modifier.size(26.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activeCategory?.title ?: "Biblioteca",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Cream,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (activeCategory == null) {
                        if (library.isScanning) "Escaneando colección…" else "${library.tracks.size} pistas · Volver al reproductor"
                    } else {
                        "Categorías de biblioteca"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = CreamMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Surface(
                color = InkSoft,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Gold.copy(alpha = 0.35f)),
                modifier = Modifier.clickable(onClick = onOpenSignalPath),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (output.isDac) Icons.Rounded.Usb else Icons.Rounded.GraphicEq,
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = if (output.isDac) "DAC" else "Audio",
                        style = MaterialTheme.typography.labelSmall,
                        color = Gold,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            IconButton(onClick = onRescan) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Reescanear",
                    tint = CreamMuted,
                )
            }
        }

        if (activeCategory == null) {
            val categories = remember(
                library.tracks.size,
                library.albums.size,
                library.artists.size,
                library.albumArtists.size,
                library.genres.size,
                library.years.size,
                library.composers.size,
                library.rootFolder,
                queueCount,
                favoriteCount,
                playedCount,
            ) {
                listOf(
                    CategoryCardSpec(
                        category = LibraryCategory.TODAS_LAS_CANCIONES,
                        icon = Icons.Rounded.MusicNote,
                        subtitle = "${library.tracks.size} canciones",
                    ),
                    CategoryCardSpec(
                        category = LibraryCategory.CARPETAS,
                        icon = Icons.Rounded.Folder,
                        subtitle = "${library.rootFolder?.subfolders?.size ?: 0} carpetas",
                    ),
                    CategoryCardSpec(
                        category = LibraryCategory.ALBUMES,
                        icon = Icons.Rounded.Album,
                        subtitle = "${library.albums.size} álbumes",
                    ),
                    CategoryCardSpec(
                        category = LibraryCategory.ARTISTAS,
                        icon = Icons.Rounded.Person,
                        subtitle = "${library.artists.size} artistas",
                    ),
                    CategoryCardSpec(
                        category = LibraryCategory.ARTISTAS_DEL_ALBUM,
                        icon = Icons.Rounded.Group,
                        subtitle = "${library.albumArtists.size} artistas de álbum",
                    ),
                    CategoryCardSpec(
                        category = LibraryCategory.GENEROS,
                        icon = Icons.Rounded.Category,
                        subtitle = "${library.genres.size} géneros",
                    ),
                    CategoryCardSpec(
                        category = LibraryCategory.ANOS,
                        icon = Icons.Rounded.DateRange,
                        subtitle = "${library.years.size} años",
                    ),
                    CategoryCardSpec(
                        category = LibraryCategory.COMPOSITORES,
                        icon = Icons.Rounded.Piano,
                        subtitle = "${library.composers.size} compositores",
                    ),
                    CategoryCardSpec(
                        category = LibraryCategory.LISTAS_DE_REPRODUCCION,
                        icon = Icons.AutoMirrored.Rounded.PlaylistAdd,
                        subtitle = "Listas personalizadas",
                        badge = "Pronto",
                    ),
                    CategoryCardSpec(
                        category = LibraryCategory.LA_COLA,
                        icon = Icons.AutoMirrored.Rounded.QueueMusic,
                        subtitle = "$queueCount en cola",
                    ),
                    CategoryCardSpec(
                        category = LibraryCategory.FAVORITOS,
                        icon = Icons.Rounded.Favorite,
                        subtitle = "$favoriteCount temas marcados",
                    ),
                    CategoryCardSpec(
                        category = LibraryCategory.MAS_REPRODUCIDO,
                        icon = Icons.Rounded.Star,
                        subtitle = "$playedCount temas escuchados",
                    ),
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 6.dp,
                    bottom = bottomPadding.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                library.error?.let { message ->
                    item(span = { GridItemSpan(2) }) {
                        Surface(
                            color = InkSoft,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Gold.copy(alpha = 0.45f)),
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Text("No se pudo escanear la biblioteca", color = Cream, fontWeight = FontWeight.Bold)
                                Text(message, color = CreamMuted, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                if (!hasPermission) {
                    item(span = { GridItemSpan(2) }) {
                        Surface(
                            color = InkSoft,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Gold.copy(alpha = 0.45f)),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Permiso de audio pendiente",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Cream,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        "Concédelo para indexar tus carpetas y pistas locales.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CreamMuted,
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Button(
                                    onClick = onRequestPermission,
                                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink),
                                    shape = RoundedCornerShape(12.dp),
                                ) {
                                    Text("Permitir", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                items(categories, key = { it.category.name }) { spec ->
                    LibraryCategoryCard(
                        spec = spec,
                        onClick = { onSelectCategory(spec.category) },
                    )
                }
            }
        } else {
            val requiresLocalScan = activeCategory in setOf(
                LibraryCategory.TODAS_LAS_CANCIONES,
                LibraryCategory.CARPETAS,
                LibraryCategory.ALBUMES,
                LibraryCategory.ARTISTAS,
                LibraryCategory.ARTISTAS_DEL_ALBUM,
                LibraryCategory.GENEROS,
                LibraryCategory.ANOS,
                LibraryCategory.COMPOSITORES,
                LibraryCategory.FAVORITOS,
                LibraryCategory.MAS_REPRODUCIDO,
            )

            when {
                requiresLocalScan && !hasPermission -> PermissionCard(onGrant = onRequestPermission)
                requiresLocalScan && library.isScanning && library.tracks.isEmpty() -> LoadingLibrary()
                requiresLocalScan && library.hasScanned && library.tracks.isEmpty() -> EmptyLibrary(hasQuery = false)
                else -> {
                    when (activeCategory) {
                        LibraryCategory.TODAS_LAS_CANCIONES -> {
                            SearchField(query = library.query, onQueryChange = onQueryChange)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Button(
                                    onClick = {
                                        if (library.filteredTracks.isNotEmpty()) {
                                            onTrackClick(library.filteredTracks, 0)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Reproducir (${library.filteredTracks.size})", fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = {
                                        if (library.filteredTracks.isNotEmpty()) {
                                            val shuffled = library.filteredTracks.shuffled()
                                            onTrackClick(shuffled, 0)
                                        }
                                    },
                                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.6f)),
                                    shape = RoundedCornerShape(12.dp),
                                ) {
                                    Icon(Icons.Rounded.Shuffle, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Aleatorio", color = Gold, fontWeight = FontWeight.Bold)
                                }
                            }
                            TrackList(
                                tracks = library.filteredTracks,
                                currentUri = currentUri,
                                isPlaying = isPlaying,
                                controller = controller,
                                onTrackClick = { idx -> onTrackClick(library.filteredTracks, idx) },
                                onInspectClick = onInspectTrack,
                                onMessage = onMessage,
                                modifier = Modifier.weight(1f),
                                bottomPadding = bottomPadding,
                            )
                        }

                        LibraryCategory.CARPETAS -> {
                            FolderBrowserView(
                                currentFolder = library.currentFolder,
                                folderPath = library.folderPath,
                                currentUri = currentUri,
                                isPlaying = isPlaying,
                                controller = controller,
                                onFolderClick = onFolderClick,
                                onNavigateUp = onNavigateUpFolder,
                                onTrackClick = onTrackClick,
                                onInspectClick = onInspectTrack,
                                onMessage = onMessage,
                                modifier = Modifier.weight(1f),
                                bottomPadding = bottomPadding,
                            )
                        }

                        LibraryCategory.ALBUMES -> {
                            SearchField(query = library.query, onQueryChange = onQueryChange)
                            AlbumView(
                                albums = library.filteredAlbums,
                                onAlbumClick = onAlbumClick,
                                modifier = Modifier.weight(1f),
                                bottomPadding = bottomPadding,
                            )
                        }

                        LibraryCategory.ARTISTAS -> {
                            SearchField(query = library.query, onQueryChange = onQueryChange)
                            ArtistView(
                                artists = library.filteredArtists,
                                onArtistClick = onArtistClick,
                                modifier = Modifier.weight(1f),
                                bottomPadding = bottomPadding,
                            )
                        }

                        LibraryCategory.ARTISTAS_DEL_ALBUM -> {
                            SearchField(query = library.query, onQueryChange = onQueryChange)
                            ArtistView(
                                artists = library.filteredAlbumArtists,
                                onArtistClick = onArtistClick,
                                modifier = Modifier.weight(1f),
                                bottomPadding = bottomPadding,
                            )
                        }

                        LibraryCategory.GENEROS -> {
                            SearchField(query = library.query, onQueryChange = onQueryChange)
                            TagCategoryListView(
                                groups = library.filteredGenres,
                                fallbackIcon = Icons.Rounded.Category,
                                onGroupClick = onTagGroupClick,
                                modifier = Modifier.weight(1f),
                                bottomPadding = bottomPadding,
                            )
                        }

                        LibraryCategory.ANOS -> {
                            SearchField(query = library.query, onQueryChange = onQueryChange)
                            TagCategoryListView(
                                groups = library.filteredYears,
                                fallbackIcon = Icons.Rounded.DateRange,
                                onGroupClick = onTagGroupClick,
                                modifier = Modifier.weight(1f),
                                bottomPadding = bottomPadding,
                            )
                        }

                        LibraryCategory.COMPOSITORES -> {
                            SearchField(query = library.query, onQueryChange = onQueryChange)
                            TagCategoryListView(
                                groups = library.filteredComposers,
                                fallbackIcon = Icons.Rounded.Piano,
                                onGroupClick = onTagGroupClick,
                                modifier = Modifier.weight(1f),
                                bottomPadding = bottomPadding,
                            )
                        }

                        LibraryCategory.LA_COLA -> {
                            InlineQueueCategoryView(
                                controller = controller,
                                snapshot = queueSnapshot,
                                isPlaying = isPlaying,
                                bottomPadding = bottomPadding,
                                onOpenFullQueueEditor = onOpenQueueSheet,
                                modifier = Modifier.weight(1f),
                            )
                        }

                        LibraryCategory.LISTAS_DE_REPRODUCCION -> {
                            UpcomingCategoryPlaceholder(
                                icon = Icons.AutoMirrored.Rounded.PlaylistAdd,
                                title = "Listas de reproducción",
                                description = "Aquí podrás crear tus propias listas, guardar colas actuales e importar archivos M3U/M3U8. Programaremos su lógica en el siguiente paso.",
                                modifier = Modifier.weight(1f),
                            )
                        }

                        LibraryCategory.FAVORITOS -> {
                            SearchField(query = library.query, onQueryChange = onQueryChange)
                            if (favoriteTracks.isEmpty() && library.query.isBlank()) {
                                UpcomingCategoryPlaceholder(
                                    icon = Icons.Rounded.Favorite,
                                    title = "Aún no hay favoritos",
                                    description = "Marca el corazón en el reproductor para guardar aquí tus canciones locales favoritas.",
                                    modifier = Modifier.weight(1f),
                                )
                            } else {
                                TrackList(
                                    tracks = favoriteTracks,
                                    currentUri = currentUri,
                                    isPlaying = isPlaying,
                                    controller = controller,
                                    onTrackClick = { index -> onTrackClick(favoriteTracks, index) },
                                    onInspectClick = onInspectTrack,
                                    onMessage = onMessage,
                                    modifier = Modifier.weight(1f),
                                    bottomPadding = bottomPadding,
                                )
                            }
                        }

                        LibraryCategory.MAS_REPRODUCIDO -> {
                            SearchField(query = library.query, onQueryChange = onQueryChange)
                            if (mostPlayedTracks.isEmpty() && library.query.isBlank()) {
                                UpcomingCategoryPlaceholder(
                                    icon = Icons.Rounded.Star,
                                    title = "Aún no hay reproducciones",
                                    description = "Tus canciones locales se ordenarán aquí a medida que empieces a escucharlas.",
                                    modifier = Modifier.weight(1f),
                                )
                            } else {
                                TrackList(
                                    tracks = mostPlayedTracks,
                                    currentUri = currentUri,
                                    isPlaying = isPlaying,
                                    controller = controller,
                                    onTrackClick = { index -> onTrackClick(mostPlayedTracks, index) },
                                    onInspectClick = onInspectTrack,
                                    onMessage = onMessage,
                                    extraLabel = { track ->
                                        val count = playCounts[track.uri.toString()] ?: 0
                                        "$count ${if (count == 1) "reproducción" else "reproducciones"}"
                                    },
                                    modifier = Modifier.weight(1f),
                                    bottomPadding = bottomPadding,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryCategoryCard(
    spec: CategoryCardSpec,
    onClick: () -> Unit,
) {
    Surface(
        color = InkSoft,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Line),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Gold.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = spec.icon,
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(22.dp),
                    )
                }

                if (spec.badge != null) {
                    Surface(
                        color = InkElevated,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Gold.copy(alpha = 0.35f)),
                    ) {
                        Text(
                            text = spec.badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = Gold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.NavigateNext,
                        contentDescription = null,
                        tint = CreamMuted,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = spec.category.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Cream,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = spec.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = CreamMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun TagCategoryListView(
    groups: List<TagCategoryGroup>,
    fallbackIcon: ImageVector,
    onGroupClick: (TagCategoryGroup) -> Unit,
    modifier: Modifier = Modifier,
    bottomPadding: Int,
) {
    if (groups.isEmpty()) {
        EmptyLibrary(hasQuery = true)
        return
    }
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = bottomPadding.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(groups, key = { it.name }) { group ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(InkSoft)
                    .clickable { onGroupClick(group) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(InkElevated),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(fallbackIcon, contentDescription = null, tint = Gold)
                    val art = group.artworkUri
                    if (art != null) {
                        AsyncImage(
                            model = art,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Cream,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = group.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = CreamMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.NavigateNext,
                    contentDescription = null,
                    tint = CreamMuted,
                )
            }
        }
    }
}

@Composable
private fun InlineQueueCategoryView(
    controller: MediaController?,
    snapshot: QueueSnapshot,
    isPlaying: Boolean,
    bottomPadding: Int,
    onOpenFullQueueEditor: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (controller == null || snapshot.items.isEmpty()) {
        UpcomingCategoryPlaceholder(
            icon = Icons.AutoMirrored.Rounded.QueueMusic,
            title = "La cola está vacía",
            description = "Reproduce cualquier canción o álbum para armar la cola activa.",
            modifier = modifier,
        )
        return
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = onOpenFullQueueEditor,
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.AutoMirrored.Rounded.QueueMusic, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Reordenar cola (${snapshot.totalCount})", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = { QueueManager.toggleShuffle(controller) },
                border = BorderStroke(1.dp, Gold.copy(alpha = 0.55f)),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Rounded.Shuffle, contentDescription = "Aleatorio", tint = Gold, modifier = Modifier.size(18.dp))
            }
            OutlinedButton(
                onClick = { QueueManager.clearUpcoming(controller) },
                border = BorderStroke(1.dp, Line),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Rounded.DeleteSweep, contentDescription = "Limpiar siguientes", tint = CreamMuted, modifier = Modifier.size(18.dp))
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 6.dp,
                bottom = bottomPadding.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            itemsIndexed(snapshot.items, key = { idx, item -> "${idx}_${item.mediaItem.mediaId}" }) { idx, item ->
                val title = item.track?.title ?: item.mediaItem.mediaMetadata.title?.toString().orEmpty().ifBlank { "Sin título" }
                val artist = item.track?.effectiveArtist ?: item.mediaItem.mediaMetadata.artist?.toString().orEmpty().ifBlank { "Artista desconocido" }
                Surface(
                    color = if (item.isCurrent) Gold.copy(alpha = 0.14f) else InkSoft,
                    shape = RoundedCornerShape(14.dp),
                    border = if (item.isCurrent) BorderStroke(1.dp, Gold.copy(alpha = 0.6f)) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            controller.seekToDefaultPosition(item.index)
                            controller.play()
                        },
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${idx + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (item.isCurrent) Gold else CreamMuted,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(28.dp),
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (item.isCurrent) FontWeight.Bold else FontWeight.Medium,
                                color = if (item.isCurrent) Gold else Cream,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = artist,
                                style = MaterialTheme.typography.bodySmall,
                                color = CreamMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        if (item.isCurrent && isPlaying) {
                            Icon(
                                imageVector = Icons.Rounded.GraphicEq,
                                contentDescription = null,
                                tint = Gold,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpcomingCategoryPlaceholder(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            color = InkSoft,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Gold.copy(alpha = 0.3f)),
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Gold.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = Gold, modifier = Modifier.size(30.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Cream,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CreamMuted,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TagGroupDetailSheet(
    group: TagCategoryGroup,
    currentUri: Uri?,
    isPlaying: Boolean,
    controller: MediaController?,
    onTrackClick: (Int) -> Unit,
    onInspectClick: (Track) -> Unit,
    onMessage: (String) -> Unit,
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
                .navigationBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Cream,
                    )
                    Text(
                        text = "${group.subtitle} · ${formatTime(group.totalDurationMs)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gold,
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = { onTrackClick(0) },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Reproducir", fontWeight = FontWeight.Bold)
                }
                if (controller != null) {
                    OutlinedButton(
                        onClick = {
                            val count = QueueManager.addToCustomQueue(
                                controller,
                                group.tracks.map(Track::toMediaItem),
                                playNext = false,
                            )
                            onMessage("$count canciones añadidas a la cola")
                        },
                        border = BorderStroke(1.dp, Gold.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.QueueMusic, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("A la cola", color = Gold, fontWeight = FontWeight.Bold)
                    }
                }
            }

            TrackList(
                tracks = group.tracks,
                currentUri = currentUri,
                isPlaying = isPlaying,
                controller = controller,
                onTrackClick = onTrackClick,
                onInspectClick = onInspectClick,
                onMessage = onMessage,
                modifier = Modifier.height(420.dp),
                bottomPadding = 24,
            )
        }
    }
}

/**
 * Pantalla visual del Ecualizador estilo Poweramp (interfaz lista para conectar el motor DSP después).
 */
@Composable
internal fun PowerampEqualizerWorkspace(
    bottomPadding: Int,
    onBackToPlayer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val freqLabels = remember { listOf("31", "62", "125", "250", "500", "1k", "2k", "4k", "8k", "16k") }
    val presets = remember {
        mapOf(
            "Plano" to listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
            "Bajo & Alto" to listOf(6f, 5f, 3f, 0f, -1f, -1f, 1f, 3f, 5f, 6f),
            "Rock" to listOf(5f, 4f, 2f, -1f, -2f, 0f, 2f, 4f, 5f, 5f),
            "Vocal" to listOf(-2f, -1f, 1f, 3f, 5f, 5f, 3f, 1f, 0f, -1f),
            "Hi-Fi Cálido" to listOf(3f, 2.5f, 1.5f, 0.5f, 0f, 0f, 1f, 2f, 2.5f, 2f),
        )
    }
    var eqEnabled by remember { mutableStateOf(true) }
    var activePreset by remember { mutableStateOf("Plano") }
    var preampDb by remember { mutableFloatStateOf(0f) }
    var bassTone by remember { mutableFloatStateOf(0.25f) }
    var trebleTone by remember { mutableFloatStateOf(0.20f) }
    val bands = remember { mutableStateListOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBackToPlayer) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver", tint = Gold, modifier = Modifier.size(26.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Ecualizador", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Cream)
                Text("Vista previa interfaz · DSP próximamente", style = MaterialTheme.typography.labelSmall, color = Gold)
            }
            Switch(
                checked = eqEnabled,
                onCheckedChange = { eqEnabled = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Ink,
                    checkedTrackColor = Gold,
                    uncheckedThumbColor = CreamMuted,
                    uncheckedTrackColor = InkSoft,
                ),
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(presets.keys.toList()) { presetName ->
                FilterChip(
                    selected = activePreset == presetName,
                    onClick = {
                        activePreset = presetName
                        presets[presetName]?.forEachIndexed { i, v -> bands[i] = v }
                    },
                    label = { Text(presetName, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Gold,
                        selectedLabelColor = Ink,
                        containerColor = InkSoft,
                        labelColor = Cream,
                    ),
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = bottomPadding.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Preamp
            Surface(
                color = InkSoft,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Line),
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Preamplificador", style = MaterialTheme.typography.labelLarge, color = Cream, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${if (preampDb >= 0f) "+" else ""}${"%.1f".format(preampDb)} dB",
                            style = MaterialTheme.typography.labelLarge,
                            color = Gold,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Slider(
                        value = preampDb,
                        onValueChange = { preampDb = it },
                        valueRange = -12f..12f,
                        enabled = eqEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = Gold,
                            activeTrackColor = Gold,
                            inactiveTrackColor = Ink,
                        ),
                    )
                }
            }

            // 10 Bandas gráficas verticales estilo Poweramp
            Surface(
                color = InkSoft,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Line),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    freqLabels.forEachIndexed { idx, freq ->
                        val gain = bands[idx]
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "${if (gain >= 0f) "+" else ""}${gain.roundToInt()}",
                                fontSize = 9.5.sp,
                                color = if (eqEnabled) Gold else CreamMuted,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .width(24.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Ink)
                                    .pointerInput(eqEnabled) {
                                        if (!eqEnabled) return@pointerInput
                                        detectVerticalDragGestures { change, dragAmount ->
                                            change.consume()
                                            val h = size.height.toFloat().coerceAtLeast(1f)
                                            val deltaDb = (-dragAmount / h) * 24f
                                            bands[idx] = (bands[idx] + deltaDb).coerceIn(-12f, 12f)
                                            activePreset = "Personalizado"
                                        }
                                    },
                                contentAlignment = Alignment.BottomCenter,
                            ) {
                                val normalized = ((gain + 12f) / 24f).coerceIn(0.05f, 1f)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(normalized)
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    if (eqEnabled) Gold else CreamMuted,
                                                    if (eqEnabled) Gold.copy(alpha = 0.25f) else InkElevated,
                                                ),
                                            ),
                                        ),
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = freq,
                                fontSize = 9.5.sp,
                                color = CreamMuted,
                            )
                        }
                    }
                }
            }

            // Tono Graves / Agudos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    color = InkSoft,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Line),
                    modifier = Modifier.weight(1f),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Graves (Bass)", style = MaterialTheme.typography.labelMedium, color = Cream, fontWeight = FontWeight.Bold)
                            Text("${(bassTone * 100).roundToInt()}%", style = MaterialTheme.typography.labelMedium, color = Gold)
                        }
                        Slider(
                            value = bassTone,
                            onValueChange = { bassTone = it },
                            enabled = eqEnabled,
                            colors = SliderDefaults.colors(thumbColor = Gold, activeTrackColor = Gold, inactiveTrackColor = Ink),
                        )
                    }
                }
                Surface(
                    color = InkSoft,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Line),
                    modifier = Modifier.weight(1f),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Agudos (Treble)", style = MaterialTheme.typography.labelMedium, color = Cream, fontWeight = FontWeight.Bold)
                            Text("${(trebleTone * 100).roundToInt()}%", style = MaterialTheme.typography.labelMedium, color = Gold)
                        }
                        Slider(
                            value = trebleTone,
                            onValueChange = { trebleTone = it },
                            enabled = eqEnabled,
                            colors = SliderDefaults.colors(thumbColor = Gold, activeTrackColor = Gold, inactiveTrackColor = Ink),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Pantalla dedicada de Búsqueda (Biblioteca local + YouTube Music Online).
 */
@Composable
internal fun PowerampSearchWorkspace(
    library: LibraryUiState,
    hasPermission: Boolean,
    currentUri: Uri?,
    playingYtmId: String?,
    isPlaying: Boolean,
    playbackError: String?,
    controller: MediaController?,
    bottomPadding: Int,
    onBackToPlayer: () -> Unit,
    onQueryChange: (String) -> Unit,
    onYtmQueryChange: (String) -> Unit,
    onSearchYtm: () -> Unit,
    onRequestPermission: () -> Unit,
    onLocalTrackClick: (List<Track>, Int) -> Unit,
    onAlbumClick: (AlbumGroup) -> Unit,
    onArtistClick: (ArtistGroup) -> Unit,
    onInspectTrack: (Track) -> Unit,
    onYtmTrackClick: (YtmTrack) -> Unit,
    onMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchSource by remember { mutableIntStateOf(0) } // 0 = Local, 1 = YouTube Music

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBackToPlayer) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver", tint = Gold, modifier = Modifier.size(26.dp))
            }
            Text(
                text = "Búsqueda",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Cream,
                modifier = Modifier.weight(1f),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = searchSource == 0,
                    onClick = { searchSource = 0 },
                    label = { Text("Local", fontWeight = FontWeight.Bold) },
                    leadingIcon = {
                        Icon(Icons.Rounded.LibraryMusic, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Gold,
                        selectedLabelColor = Ink,
                        selectedLeadingIconColor = Ink,
                        containerColor = InkSoft,
                        labelColor = Cream,
                        iconColor = Gold,
                    ),
                )
                FilterChip(
                    selected = searchSource == 1,
                    onClick = { searchSource = 1 },
                    label = { Text("YouTube Music", fontWeight = FontWeight.Bold) },
                    leadingIcon = {
                        Icon(Icons.Rounded.Cloud, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Gold,
                        selectedLabelColor = Ink,
                        selectedLeadingIconColor = Ink,
                        containerColor = InkSoft,
                        labelColor = Cream,
                        iconColor = Gold,
                    ),
                )
            }
        }

        if (searchSource == 1) {
            YtmView(
                ytmTracks = library.ytmTracks,
                ytmQuery = library.ytmQuery,
                isSearching = library.isYtmSearching,
                errorMessage = library.ytmError ?: playbackError,
                playingVideoId = playingYtmId,
                isPlaying = isPlaying,
                onQueryChange = onYtmQueryChange,
                onSearch = onSearchYtm,
                onTrackClick = onYtmTrackClick,
                modifier = Modifier.weight(1f),
                bottomPadding = bottomPadding,
            )
        } else {
            if (!hasPermission) {
                PermissionCard(onGrant = onRequestPermission)
            } else {
                SearchField(query = library.query, onQueryChange = onQueryChange)
                val q = library.query.trim()
                if (q.isNotEmpty() && library.filteredAlbums.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(library.filteredAlbums.take(10), key = { it.name + "_" + it.artist }) { album ->
                            Surface(
                                color = InkSoft,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, Line),
                                modifier = Modifier
                                    .width(150.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onAlbumClick(album) },
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(InkElevated),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(Icons.Rounded.Album, contentDescription = null, tint = Gold, modifier = Modifier.size(20.dp))
                                        album.artworkUri?.let { art ->
                                            AsyncImage(
                                                model = art,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize(),
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(album.name, style = MaterialTheme.typography.labelMedium, color = Cream, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(album.artist, style = MaterialTheme.typography.labelSmall, color = CreamMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                }
                if (q.isNotEmpty() && library.filteredArtists.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(library.filteredArtists.take(10), key = { it.name }) { artist ->
                            Surface(
                                color = InkSoft,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, Line),
                                modifier = Modifier.clickable { onArtistClick(artist) },
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(Icons.Rounded.Person, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(artist.name, style = MaterialTheme.typography.labelMedium, color = Cream, maxLines = 1)
                                }
                            }
                        }
                    }
                }
                TrackList(
                    tracks = library.filteredTracks,
                    currentUri = currentUri,
                    isPlaying = isPlaying,
                    controller = controller,
                    onTrackClick = { idx -> onLocalTrackClick(library.filteredTracks, idx) },
                    onInspectClick = onInspectTrack,
                    onMessage = onMessage,
                    modifier = Modifier.weight(1f),
                    bottomPadding = bottomPadding,
                )
            }
        }
    }
}
