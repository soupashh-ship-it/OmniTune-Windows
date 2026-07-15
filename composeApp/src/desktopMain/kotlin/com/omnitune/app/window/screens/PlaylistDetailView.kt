package com.omnitune.app.window.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.omnitune.app.platform.SavedQueuePlaylist
import com.omnitune.app.platform.QaRuntime
import com.omnitune.app.player.NavScreen
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.app.platform.PlaybackState
import com.omnitune.app.service.YouTubeService
import com.omnitune.app.window.BorderLow
import com.omnitune.app.window.IrisSoft
import com.omnitune.app.window.LocalHomeReferenceMetrics
import com.omnitune.app.window.OmniGradients
import com.omnitune.app.window.OmniReferenceColors
import com.omnitune.app.window.Surface1
import com.omnitune.app.window.TextMuted
import com.omnitune.app.window.TextPrimary
import com.omnitune.app.window.TextSecondary
import com.omnitune.app.window.components.OmniEmptyState
import com.omnitune.app.window.components.OmniShimmerBlock
import com.omnitune.innertube.models.Artist
import com.omnitune.innertube.models.PlaylistItem
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.pages.PlaylistPage
import com.omnitune.innertube.toHighResThumbnail
import org.koin.compose.koinInject
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
fun PlaylistDetailView(player: PlayerViewModel) {
    val playlistId by player.currentPlaylistId.collectAsState()
    val service = koinInject<YouTubeService>()
    var page by remember(playlistId) { mutableStateOf<PlaylistPage?>(null) }
    var error by remember(playlistId) { mutableStateOf<String?>(null) }

    val currentSong by player.currentSong.collectAsState()
    val playbackState by player.playbackState.collectAsState()
    val liked by player.likedSongs.collectAsState()
    val discoveryNew by player.discoveryNew.collectAsState()
    val savedPlaylists by player.savedQueuePlaylists.collectAsState()
    val localSaved = remember(savedPlaylists, playlistId) { savedPlaylists.firstOrNull { it.id == playlistId } }
    val localPlaylist = remember(localSaved) {
        localSaved?.let { saved ->
            PlaylistPage(
                playlist = PlaylistItem(
                    id = saved.id,
                    title = saved.name,
                    author = Artist("OmniTune", null),
                    songCountText = "${saved.songs.size} songs",
                    thumbnail = saved.coverPath ?: saved.songs.firstOrNull()?.thumbnail,
                    playEndpoint = null,
                    shuffleEndpoint = null,
                    radioEndpoint = null,
                    isEditable = true,
                ),
                songs = saved.songs,
                songsContinuation = null,
                continuation = null,
            )
        }
    }
    val relatedPlaylists = remember(discoveryNew, playlistId) {
        discoveryNew.filterIsInstance<PlaylistItem>().filter { it.id != playlistId }.distinctBy { it.id }
    }

    LaunchedEffect(playlistId, localSaved?.id) {
        page = null
        error = null
        val id = playlistId ?: return@LaunchedEffect
        if (localSaved == null) {
            runCatching { service.playlist(id) }
                .onSuccess { page = it }
                .onFailure { error = it.message ?: "Playlist failed to load." }
        }
    }

    val loadedPage = localPlaylist ?: page
    when {
        playlistId == null -> OmniEmptyState("No playlist selected", "Open a playlist to view its details.")
        error != null -> OmniEmptyState("Couldn't load playlist", error ?: "Unknown error")
        loadedPage == null -> PlaylistDetailLoading()
        else -> PlaylistDetailReferenceContent(
            page = loadedPage,
            localPlaylist = localSaved,
            savedPlaylists = savedPlaylists,
            relatedPlaylists = relatedPlaylists,
            currentSong = currentSong,
            playbackState = playbackState,
            likedIds = liked,
            onPlayPlaylist = { player.playSongList(loadedPage.songs) },
            onShuffle = { player.playShuffledSongs(loadedPage.songs) },
            onSong = { songs, index -> player.playSongList(songs, index) },
            onLikeSong = { player.toggleLikeSong(it) },
            onAddSong = { player.addToQueue(it) },
            onPlayNext = { player.playNext(it) },
            onDownloadSong = { player.downloadSong(it) },
            onDownloadPlaylist = { player.downloadSongs(loadedPage.songs) },
            onSavePlaylist = {
                player.saveSongsAsPlaylist(loadedPage.playlist.title, loadedPage.songs)
            },
            onCreatePlaylist = { name, description -> player.createPlaylist(name, description) },
            onAddSongToPlaylists = { song, ids -> player.addSongToSavedPlaylists(song, ids) },
            onUpdatePlaylist = { id, name, description, tags, coverPath ->
                player.updateSavedPlaylistMetadata(id, name, description, tags, coverPath)
            },
            onRemoveSong = { id, songId -> player.removeSongFromSavedPlaylist(id, songId) },
            onMoveSong = { id, from, to -> player.moveSavedPlaylistSong(id, from, to) },
            onDeletePlaylist = { id -> player.deleteSavedPlaylist(id) },
            onOpenPlaylists = { player.navigateTo(NavScreen.Playlists) },
            onOpenRelated = { player.openPlaylist(it.id) },
            onPlayRelated = { player.playPlaylist(it.id) },
            onOpenAlbum = { id -> player.openAlbum(id) },
            onOpenArtist = { id -> player.openArtist(id) },
        )
    }
}

@Composable
private fun PlaylistDetailReferenceContent(
    page: PlaylistPage,
    localPlaylist: SavedQueuePlaylist?,
    savedPlaylists: List<SavedQueuePlaylist>,
    relatedPlaylists: List<PlaylistItem>,
    currentSong: SongItem?,
    playbackState: PlaybackState,
    likedIds: Set<String>,
    onPlayPlaylist: () -> Unit,
    onShuffle: () -> Unit,
    onSong: (List<SongItem>, Int) -> Unit,
    onLikeSong: (SongItem) -> Unit,
    onAddSong: (SongItem) -> Unit,
    onPlayNext: (SongItem) -> Unit,
    onDownloadSong: (SongItem) -> Unit,
    onDownloadPlaylist: () -> Unit,
    onSavePlaylist: () -> Result<String>,
    onCreatePlaylist: (String, String) -> Result<String>,
    onAddSongToPlaylists: (SongItem, Set<String>) -> Result<Int>,
    onUpdatePlaylist: (String, String, String, List<String>, String?) -> Result<String>,
    onRemoveSong: (String, String) -> Result<String>,
    onMoveSong: (String, Int, Int) -> Result<String>,
    onDeletePlaylist: (String) -> Result<Unit>,
    onOpenPlaylists: () -> Unit,
    onOpenRelated: (PlaylistItem) -> Unit,
    onPlayRelated: (PlaylistItem) -> Unit,
    onOpenAlbum: (String) -> Unit,
    onOpenArtist: (String) -> Unit,
) {
    val metrics = LocalHomeReferenceMetrics.current
    val playlist = page.playlist
    val songs = page.songs
    val totalDuration = remember(songs) { songs.sumOf { it.duration ?: 0 } }
    var selectedSongId by remember(playlist.id) { mutableStateOf<String?>(null) }
    var addSheetSong by remember { mutableStateOf<SongItem?>(null) }
    var editSheetOpen by remember { mutableStateOf(false) }
    var actionMessage by remember(playlist.id) { mutableStateOf<String?>(null) }
    val qaState = remember { QaRuntime.playlistState }
    val tags = localPlaylist?.tags?.ifEmpty { inferredPlaylistTags(playlist.title, songs) } ?: inferredPlaylistTags(playlist.title, songs)
    val description = localPlaylist?.description?.takeIf { it.isNotBlank() }
        ?: "A playlist built from real OmniTune tracks and provider metadata."

    LaunchedEffect(qaState, songs, localPlaylist?.id) {
        when (qaState) {
            "expanded", "menu" -> selectedSongId = songs.getOrNull(1)?.id ?: songs.firstOrNull()?.id
            "add" -> addSheetSong = songs.firstOrNull()
            "edit" -> if (localPlaylist != null) editSheetOpen = true
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val showRail = maxWidth > 980.dp
        val railWidth = if (showRail) metrics.px(235f) else 0.dp
        val coverSize = if (maxWidth < 1040.dp) metrics.px(150f) else metrics.px(220f)

        Row(
            modifier = Modifier.fillMaxSize().padding(start = metrics.px(18f), top = metrics.px(14f), end = metrics.px(18f)),
            horizontalArrangement = Arrangement.spacedBy(metrics.px(20f)),
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                contentPadding = PaddingValues(bottom = metrics.px(150f)),
                verticalArrangement = Arrangement.spacedBy(metrics.px(5f)),
            ) {
                item {
                    PlaylistHero(
                        playlist = playlist,
                        localPlaylist = localPlaylist,
                        songs = songs,
                        description = description,
                        tags = tags,
                        duration = totalDuration,
                        coverSize = coverSize,
                        onPlay = onPlayPlaylist,
                        onShuffle = onShuffle,
                        onSave = {
                            onSavePlaylist()
                                .onSuccess { actionMessage = "Saved playlist: $it" }
                                .onFailure { actionMessage = it.message ?: "Could not save playlist." }
                        },
                        onDownload = {
                            onDownloadPlaylist()
                            actionMessage = "Download queued for ${songs.size} tracks."
                        },
                        onEdit = { if (localPlaylist != null) editSheetOpen = true },
                        editable = localPlaylist != null,
                    )
                }
                actionMessage?.let { message ->
                    item { Text(message, color = IrisSoft, fontSize = 10.sp, modifier = Modifier.padding(start = metrics.px(4f))) }
                }
                item { PlaylistTrackHeader() }
                itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                    val selected = selectedSongId == song.id
                    val active = song.id == currentSong?.id
                    Column(Modifier.animateContentSize()) {
                        PlaylistTrackRow(
                            song = song,
                            index = index,
                            selected = selected,
                            active = active,
                            playing = active && playbackState == PlaybackState.PLAYING,
                            liked = likedIds.contains(song.id),
                            editable = localPlaylist != null,
                            canMoveUp = index > 0,
                            canMoveDown = index < songs.lastIndex,
                            forceMenu = qaState == "menu" && index == 1,
                            onSelect = { selectedSongId = if (selected) null else song.id },
                            onPlay = { onSong(songs, index) },
                            onLike = { onLikeSong(song) },
                            onAddToPlaylist = { addSheetSong = song },
                            onAction = { action ->
                                when (action) {
                                    TrackAction.PlayNext -> onPlayNext(song)
                                    TrackAction.AddToQueue -> onAddSong(song)
                                    TrackAction.ToggleLike -> onLikeSong(song)
                                    TrackAction.AddToPlaylist -> addSheetSong = song
                                    TrackAction.Download -> onDownloadSong(song)
                                    TrackAction.ViewAlbum -> song.album?.id?.let(onOpenAlbum)
                                    TrackAction.GoToArtist -> song.artists.firstOrNull()?.id?.let(onOpenArtist)
                                    TrackAction.Remove -> localPlaylist?.let { onRemoveSong(it.id, song.id) }
                                    TrackAction.MoveUp -> localPlaylist?.let { onMoveSong(it.id, index, index - 1) }
                                    TrackAction.MoveDown -> localPlaylist?.let { onMoveSong(it.id, index, index + 1) }
                                }
                            },
                        )
                        if (selected) {
                            ExpandedPlaylistTrack(
                                song = song,
                                onPlayNext = { onPlayNext(song) },
                                onAddToQueue = { onAddSong(song) },
                                onGoToArtist = { song.artists.firstOrNull()?.id?.let(onOpenArtist) },
                            )
                        }
                    }
                }
                if (!showRail && relatedPlaylists.isNotEmpty()) {
                    item {
                        RelatedPlaylistRail(
                            title = "More like this",
                            related = relatedPlaylists,
                            onOpen = onOpenRelated,
                            onPlay = onPlayRelated,
                            onSeeAll = onOpenPlaylists,
                            modifier = Modifier.fillMaxWidth().height(metrics.px(260f)).padding(top = metrics.px(10f)),
                        )
                    }
                }
            }

            if (showRail) {
                PlaylistRightRail(
                    relatedPlaylists = relatedPlaylists,
                    tags = tags,
                    onOpen = onOpenRelated,
                    onPlay = onPlayRelated,
                    onSeeAll = onOpenPlaylists,
                    modifier = Modifier.width(railWidth).padding(top = metrics.px(24f)),
                )
            }
        }

        addSheetSong?.let { song ->
            AddToPlaylistSheet(
                song = song,
                playlists = savedPlaylists,
                onClose = { addSheetSong = null },
                onCreatePlaylist = onCreatePlaylist,
                onAdd = { ids ->
                    onAddSongToPlaylists(song, ids)
                        .onSuccess { count ->
                            actionMessage = if (count == 0) "Selected playlists already contain this song, or no playlist was selected." else "Added to $count playlist${if (count == 1) "" else "s"}."
                            if (count > 0) addSheetSong = null
                        }
                        .onFailure { actionMessage = it.message ?: "Could not add to playlist." }
                },
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }

        if (editSheetOpen && localPlaylist != null) {
            EditPlaylistSheet(
                playlist = localPlaylist,
                onClose = { editSheetOpen = false },
                onSave = { name, descriptionValue, editedTags, coverPath ->
                    onUpdatePlaylist(localPlaylist.id, name, descriptionValue, editedTags, coverPath)
                        .onSuccess {
                            actionMessage = "Saved playlist changes."
                            editSheetOpen = false
                        }
                        .onFailure { actionMessage = it.message ?: "Could not save playlist." }
                },
                onDelete = {
                    onDeletePlaylist(localPlaylist.id)
                        .onFailure { actionMessage = it.message ?: "Could not delete playlist." }
                },
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }
    }
}

@Composable
private fun PlaylistHero(
    playlist: PlaylistItem,
    localPlaylist: SavedQueuePlaylist?,
    songs: List<SongItem>,
    description: String,
    tags: List<String>,
    duration: Int,
    coverSize: androidx.compose.ui.unit.Dp,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    onSave: () -> Unit,
    onDownload: () -> Unit,
    onEdit: () -> Unit,
    editable: Boolean,
) {
    val metrics = LocalHomeReferenceMetrics.current
    var moreOpen by remember { mutableStateOf(false) }
    Column {
        Text("Playlists  ›  ${playlist.title}", color = TextSecondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(metrics.px(10f)))
        Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(22f))) {
            AsyncImage(
                model = localPlaylist?.coverPath ?: playlist.thumbnail?.toHighResThumbnail(),
                contentDescription = playlist.title,
                modifier = Modifier
                    .size(coverSize)
                    .clip(RoundedCornerShape(metrics.px(10f)))
                    .background(Surface1)
                    .border(1.dp, BorderLow.copy(alpha = 0.76f), RoundedCornerShape(metrics.px(10f))),
                contentScale = ContentScale.Crop,
            )
            Column(Modifier.weight(1f).padding(top = metrics.px(8f))) {
                Text(if (editable) "PLAYLIST" else "PROVIDER PLAYLIST", color = TextSecondary, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(metrics.px(8f)))
                Text(playlist.title, color = TextPrimary, fontSize = 32.sp, lineHeight = 42.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(metrics.px(8f)))
                Text(description, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(metrics.px(8f)))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Created by ${playlist.author?.name ?: "OmniTune"}", color = TextSecondary, fontSize = 11.sp)
                    Spacer(Modifier.width(metrics.px(6f)))
                    Icon(Icons.Default.Check, null, tint = IrisSoft, modifier = Modifier.size(metrics.px(12f)).clip(CircleShape).background(IrisSoft.copy(alpha = 0.18f)).padding(metrics.px(2f)))
                }
                Spacer(Modifier.height(metrics.px(5f)))
                Text("${playlist.songCountText ?: "${songs.size} songs"}${if (duration > 0) " · ${formatDurationLong(duration)}" else ""}", color = TextSecondary, fontSize = 10.sp)
                Spacer(Modifier.height(metrics.px(12f)))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(metrics.px(8f))) {
                    tags.take(6).forEach { TruthChip(it) }
                }
                Spacer(Modifier.height(metrics.px(18f)))
                Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(10f)), verticalAlignment = Alignment.CenterVertically) {
                    ReferencePrimaryButton("Play", Icons.Default.PlayArrow, onPlay, Modifier.width(metrics.px(86f)).height(metrics.px(32f)))
                    ReferenceSecondaryButton("Shuffle", Icons.Default.Shuffle, onShuffle, Modifier.width(metrics.px(98f)).height(metrics.px(32f)))
                    RoundAction(Icons.Default.FavoriteBorder, onSave)
                    RoundAction(Icons.Default.Download, onDownload)
                    if (editable) RoundAction(Icons.Default.Edit, onEdit)
                    else {
                        Box {
                            RoundAction(Icons.Default.MoreHoriz) { moreOpen = true }
                            DropdownMenu(
                                expanded = moreOpen,
                                onDismissRequest = { moreOpen = false },
                                modifier = Modifier
                                    .width(metrics.px(160f))
                                    .clip(RoundedCornerShape(metrics.px(10f)))
                                    .border(1.dp, BorderLow.copy(alpha = 0.72f), RoundedCornerShape(metrics.px(10f)))
                                    .background(Color(0xF20E1221)),
                                containerColor = Color(0xF20E1221),
                                tonalElevation = 0.dp,
                                shadowElevation = 12.dp,
                            ) {
                                PlaylistMenuAction(Icons.Default.FavoriteBorder, "Save as playlist") {
                                    moreOpen = false
                                    onSave()
                                }
                                PlaylistMenuAction(Icons.Default.Download, "Download playlist") {
                                    moreOpen = false
                                    onDownload()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistTrackHeader() {
    val metrics = LocalHomeReferenceMetrics.current
    Row(Modifier.fillMaxWidth().height(metrics.px(24f)).padding(horizontal = metrics.px(8f)), verticalAlignment = Alignment.CenterVertically) {
        Text("#", color = TextMuted, fontSize = 8.sp, modifier = Modifier.width(metrics.px(34f)))
        Text("TITLE", color = TextMuted, fontSize = 8.sp, modifier = Modifier.weight(1.8f))
        Text("ARTIST", color = TextMuted, fontSize = 8.sp, modifier = Modifier.weight(1.25f))
        Text("ALBUM", color = TextMuted, fontSize = 8.sp, modifier = Modifier.weight(1.2f))
        Text("DURATION", color = TextMuted, fontSize = 8.sp, modifier = Modifier.width(metrics.px(70f)))
        Spacer(Modifier.width(metrics.px(88f)))
    }
}

private enum class TrackAction {
    PlayNext, AddToQueue, ToggleLike, AddToPlaylist, Download, ViewAlbum, GoToArtist, Remove, MoveUp, MoveDown
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun PlaylistTrackRow(
    song: SongItem,
    index: Int,
    selected: Boolean,
    active: Boolean,
    playing: Boolean,
    liked: Boolean,
    editable: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    forceMenu: Boolean = false,
    onSelect: () -> Unit,
    onPlay: () -> Unit,
    onLike: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onAction: (TrackAction) -> Unit,
) {
    val metrics = LocalHomeReferenceMetrics.current
    var menuOpen by remember { mutableStateOf(false) }
    var dragAccumulated by remember { mutableStateOf(0f) }
    LaunchedEffect(forceMenu) {
        if (forceMenu) menuOpen = true
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(metrics.px(39f))
            .clip(RoundedCornerShape(metrics.px(7f)))
            .background(
                when {
                    selected -> OmniReferenceColors.SurfaceSelected.copy(alpha = 0.82f)
                    active -> OmniReferenceColors.SurfaceSelected.copy(alpha = 0.58f)
                    else -> OmniReferenceColors.SurfaceBase.copy(alpha = 0.24f)
                }
            )
            .border(
                1.dp,
                if (selected) OmniReferenceColors.Accent.copy(alpha = 0.88f) else Color.Transparent,
                RoundedCornerShape(metrics.px(7f)),
            )
            .onPointerEvent(PointerEventType.Press) {
                if (it.buttons.isSecondaryPressed) menuOpen = true
            }
            .clickable(onClick = onSelect)
            .padding(horizontal = metrics.px(8f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(metrics.px(34f)), contentAlignment = Alignment.CenterStart) {
            if (editable) {
                Icon(
                    Icons.Default.DragIndicator,
                    contentDescription = "Drag to reorder",
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(metrics.px(16f))
                        .pointerInput(canMoveUp, canMoveDown) {
                            detectVerticalDragGestures(
                                onDragStart = { dragAccumulated = 0f },
                                onDragEnd = { dragAccumulated = 0f },
                                onDragCancel = { dragAccumulated = 0f },
                            ) { change, dragAmount ->
                                change.consume()
                                dragAccumulated += dragAmount
                                if (dragAccumulated <= -22f && canMoveUp) {
                                    dragAccumulated = 0f
                                    onAction(TrackAction.MoveUp)
                                } else if (dragAccumulated >= 22f && canMoveDown) {
                                    dragAccumulated = 0f
                                    onAction(TrackAction.MoveDown)
                                }
                            }
                        },
                )
            } else if (playing) Icon(Icons.Default.GraphicEq, null, tint = IrisSoft, modifier = Modifier.size(metrics.px(13f)))
            else if (active) Icon(Icons.Default.PlayArrow, null, tint = IrisSoft, modifier = Modifier.size(metrics.px(13f)))
            else Text("${index + 1}", color = TextSecondary, fontSize = 9.sp)
        }
        AsyncImage(song.thumbnail.toHighResThumbnail(), song.title, Modifier.size(metrics.px(25f)).clip(RoundedCornerShape(metrics.px(4f))).clickable(onClick = onPlay), contentScale = ContentScale.Crop)
        Spacer(Modifier.width(metrics.px(8f)))
        Column(Modifier.weight(1.8f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(song.title, color = if (active || selected) TextPrimary else TextPrimary, fontSize = 9.8.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (song.explicit) {
                    Spacer(Modifier.width(metrics.px(4f)))
                    Text("E", color = TextMuted, fontSize = 7.sp, modifier = Modifier.clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.12f)).padding(horizontal = 3.dp))
                }
            }
        }
        Text(song.artists.joinToString(", ") { it.name }, color = TextSecondary, fontSize = 8.6.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1.25f))
        Text(song.album?.name ?: "Single", color = TextSecondary, fontSize = 8.6.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1.2f))
        Text(song.durationLabel(), color = TextSecondary, fontSize = 8.5.sp, modifier = Modifier.width(metrics.px(70f)))
        Icon(if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "Like", tint = if (liked) IrisSoft else TextSecondary, modifier = Modifier.size(metrics.px(13f)).clickable(onClick = onLike))
        Spacer(Modifier.width(metrics.px(17f)))
        Icon(Icons.Default.Add, contentDescription = "Add to playlist", tint = TextSecondary, modifier = Modifier.size(metrics.px(15f)).clickable(onClick = onAddToPlaylist))
        Spacer(Modifier.width(metrics.px(15f)))
        Box {
            Icon(Icons.Default.MoreHoriz, contentDescription = "Track actions", tint = TextSecondary, modifier = Modifier.size(metrics.px(15f)).clickable { menuOpen = true })
            PlaylistTrackContextMenu(
                expanded = menuOpen,
                editable = editable,
                liked = liked,
                hasAlbum = song.album?.id != null,
                hasArtist = song.artists.any { !it.id.isNullOrBlank() },
                onDismiss = { menuOpen = false },
                onAction = {
                    menuOpen = false
                    onAction(it)
                },
            )
        }
    }
}

@Composable
private fun PlaylistTrackContextMenu(
    expanded: Boolean,
    editable: Boolean,
    liked: Boolean,
    hasAlbum: Boolean,
    hasArtist: Boolean,
    onDismiss: () -> Unit,
    onAction: (TrackAction) -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .width(184.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, BorderLow.copy(alpha = 0.76f), RoundedCornerShape(12.dp))
            .background(Color(0xF20E1221))
            .padding(vertical = 7.dp),
        containerColor = Color(0xF20E1221),
        tonalElevation = 0.dp,
        shadowElevation = 14.dp,
    ) {
        PlaylistMenuAction(Icons.Default.Shuffle, "Play next") { onAction(TrackAction.PlayNext) }
        PlaylistMenuAction(Icons.Default.Add, "Add to queue") { onAction(TrackAction.AddToQueue) }
        PlaylistMenuAction(
            if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            if (liked) "Remove from liked songs" else "Save to liked songs",
            iconTint = IrisSoft,
        ) { onAction(TrackAction.ToggleLike) }
        PlaylistMenuAction(Icons.Default.Add, "Add to another playlist", trailing = ">") { onAction(TrackAction.AddToPlaylist) }
        PlaylistMenuSeparator()
        PlaylistMenuAction(Icons.Default.Download, "Download") { onAction(TrackAction.Download) }
        if (hasAlbum) PlaylistMenuAction(Icons.Default.GraphicEq, "View album") { onAction(TrackAction.ViewAlbum) }
        if (hasArtist) PlaylistMenuAction(Icons.Default.Person, "Go to artist") { onAction(TrackAction.GoToArtist) }
        if (editable) {
            PlaylistMenuSeparator()
            PlaylistMenuAction(Icons.Default.ArrowUpward, "Move up") { onAction(TrackAction.MoveUp) }
            PlaylistMenuAction(Icons.Default.ArrowDownward, "Move down") { onAction(TrackAction.MoveDown) }
            PlaylistMenuSeparator()
            PlaylistMenuAction(Icons.Default.Delete, "Remove from playlist", textColor = Color(0xFFFF6575), iconTint = Color(0xFFFF4B61)) {
                onAction(TrackAction.Remove)
            }
        }
    }
}

@Composable
private fun ColumnScope.PlaylistMenuAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    textColor: Color = TextPrimary,
    iconTint: Color = TextSecondary,
    trailing: String? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = iconTint, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        if (trailing != null) {
            Text(trailing, color = TextMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ColumnScope.PlaylistMenuSeparator() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 5.dp)
            .height(1.dp)
            .background(BorderLow.copy(alpha = 0.44f))
    )
}

@Composable
private fun ExpandedPlaylistTrack(
    song: SongItem,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onGoToArtist: () -> Unit,
) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(metrics.px(92f))
            .clip(RoundedCornerShape(metrics.px(7f)))
            .background(Brush.horizontalGradient(listOf(Color(0xAA151246), Color(0x66101228), Color(0xAA080D1B))))
            .border(1.dp, OmniReferenceColors.Accent.copy(alpha = 0.38f), RoundedCornerShape(metrics.px(7f)))
            .padding(metrics.px(10f)),
        horizontalArrangement = Arrangement.spacedBy(metrics.px(14f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(song.thumbnail.toHighResThumbnail(), song.title, Modifier.size(metrics.px(75f)).clip(RoundedCornerShape(metrics.px(7f))), contentScale = ContentScale.Crop)
        Column(Modifier.weight(1f)) {
            Text(song.artists.joinToString(", ") { it.name }, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.album?.name ?: "Single", color = TextSecondary, fontSize = 8.7.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(metrics.px(7f)))
            Text("${song.durationLabel().ifBlank { "Unknown duration" }}${if (song.explicit) "  •  Explicit" else ""}", color = TextSecondary, fontSize = 8.5.sp)
            Spacer(Modifier.height(metrics.px(7f)))
            Box(Modifier.fillMaxWidth().height(metrics.px(18f)), contentAlignment = Alignment.CenterStart) {
                Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(2f))) {
                    repeat(34) { i ->
                        val h = metrics.px((4 + ((i * 7) % 14)).toFloat())
                        Box(Modifier.width(1.dp).height(h).clip(CircleShape).background(if (i % 3 == 0) IrisSoft else Color(0xFF8C7BFF).copy(alpha = 0.45f)))
                    }
                }
            }
        }
        Column(Modifier.weight(0.9f)) {
            Text("Track notes", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(metrics.px(5f)))
            Text(
                "From ${song.album?.name ?: "this playlist"} by ${song.artists.firstOrNull()?.name ?: "Unknown artist"}. Lyrics preview appears in Now Playing when lyrics are available.",
                color = TextSecondary,
                fontSize = 8.5.sp,
                lineHeight = 12.sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(Modifier.width(metrics.px(120f)), verticalArrangement = Arrangement.spacedBy(metrics.px(6f))) {
            SheetActionButton("Play Next", Icons.Default.PlayArrow, onPlayNext)
            SheetActionButton("Add to Queue", Icons.Default.Add, onAddToQueue)
            SheetActionButton("Go to Artist", Icons.Default.Person, onGoToArtist)
        }
    }
}

@Composable
private fun AddToPlaylistSheet(
    song: SongItem,
    playlists: List<SavedQueuePlaylist>,
    onClose: () -> Unit,
    onCreatePlaylist: (String, String) -> Result<String>,
    onAdd: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalHomeReferenceMetrics.current
    var query by remember { mutableStateOf("") }
    var selected by remember(song.id, playlists) { mutableStateOf(playlists.filter { it.songs.any { s -> s.id == song.id } }.map { it.id }.toSet()) }
    var creating by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val filtered by remember(playlists, query) {
        derivedStateOf {
            if (query.isBlank()) playlists else playlists.filter { it.name.contains(query, ignoreCase = true) }
        }
    }
    PlaylistSideSheet("Add to Playlist", onClose, modifier) {
        SearchField(query, "Search playlists") { query = it }
        SheetSectionTitle("RECENT PLAYLISTS")
        filtered.take(5).forEach { playlist ->
            PlaylistCheckRow(playlist, checked = playlist.id in selected) {
                selected = if (playlist.id in selected) selected - playlist.id else selected + playlist.id
            }
        }
        Spacer(Modifier.height(metrics.px(8f)))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            SheetSectionTitle("ALL PLAYLISTS", Modifier.weight(1f))
            SheetSmallButton("+ Create New Playlist") { creating = !creating }
        }
        if (creating) {
            TextField(
                value = newName,
                onValueChange = { newName = it },
                placeholder = { Text("Playlist name") },
                singleLine = true,
                colors = darkTextFieldColors(),
                modifier = sheetTextFieldModifier().fillMaxWidth().height(metrics.px(48f)),
            )
            SheetActionButton("Create", Icons.Default.Add, onClick = {
                onCreatePlaylist(newName, "")
                    .onSuccess { id ->
                        selected = selected + id
                        newName = ""
                        creating = false
                    }
                    .onFailure { error = it.message ?: "Could not create playlist." }
            })
        }
        filtered.drop(5).forEach { playlist ->
            PlaylistCheckRow(playlist, checked = playlist.id in selected) {
                selected = if (playlist.id in selected) selected - playlist.id else selected + playlist.id
            }
        }
        Spacer(Modifier.weight(1f))
        error?.let { Text(it, color = Color(0xFFFF7B85), fontSize = 10.sp) }
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(metrics.px(9f)))
                .background(OmniReferenceColors.SurfaceSelected.copy(alpha = 0.38f))
                .border(1.dp, BorderLow.copy(alpha = 0.72f), RoundedCornerShape(metrics.px(9f)))
                .padding(metrics.px(10f)),
        ) {
            Text("ADDING SONG", color = TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(metrics.px(8f)))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(song.thumbnail.toHighResThumbnail(), song.title, Modifier.size(metrics.px(35f)).clip(RoundedCornerShape(metrics.px(5f))), contentScale = ContentScale.Crop)
                Spacer(Modifier.width(metrics.px(8f)))
                Column(Modifier.weight(1f)) {
                    Text(song.title, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(song.artists.joinToString(", ") { it.name }, color = TextSecondary, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(Modifier.height(metrics.px(10f)))
            ReferencePrimaryButton("Add to Playlist", Icons.Default.Check, { onAdd(selected) }, Modifier.fillMaxWidth().height(metrics.px(34f)))
        }
    }
}

@Composable
private fun EditPlaylistSheet(
    playlist: SavedQueuePlaylist,
    onClose: () -> Unit,
    onSave: (String, String, List<String>, String?) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalHomeReferenceMetrics.current
    var title by remember(playlist.id) { mutableStateOf(playlist.name) }
    var description by remember(playlist.id) { mutableStateOf(playlist.description) }
    var tagText by remember(playlist.id) { mutableStateOf(playlist.tags.joinToString(", ")) }
    var coverPath by remember(playlist.id) { mutableStateOf(playlist.coverPath) }
    PlaylistSideSheet("Edit Playlist", onClose, modifier) {
        SheetSectionTitle("Cover Image")
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(coverPath ?: playlist.songs.firstOrNull()?.thumbnail?.toHighResThumbnail(), playlist.name, Modifier.size(metrics.px(68f)).clip(RoundedCornerShape(metrics.px(8f))).background(Surface1), contentScale = ContentScale.Crop)
            Spacer(Modifier.width(metrics.px(12f)))
            Column(verticalArrangement = Arrangement.spacedBy(metrics.px(7f))) {
                SheetSmallButton("Change Cover") {
                    chooseImageFile()?.let { coverPath = it }
                }
                if (coverPath != null) {
                    SheetSmallButton("Reset Cover") { coverPath = null }
                }
            }
        }
        SheetSectionTitle("Title")
        TextField(title, { title = it.take(100) }, singleLine = true, colors = darkTextFieldColors(), modifier = sheetTextFieldModifier().fillMaxWidth().height(metrics.px(48f)))
        SheetSectionTitle("Description")
        TextField(description, { description = it.take(300) }, colors = darkTextFieldColors(), modifier = sheetTextFieldModifier().fillMaxWidth().height(metrics.px(74f)))
        SheetSectionTitle("Mood & Tags")
        TextField(tagText, { tagText = it }, placeholder = { Text("Comma separated tags") }, colors = darkTextFieldColors(), modifier = sheetTextFieldModifier().fillMaxWidth().height(metrics.px(48f)))
        SheetSectionTitle("Arrangement")
        Text("Manual order is supported. Use track menu Move up/down controls to reorder songs; changes persist immediately.", color = TextSecondary, fontSize = 10.sp, lineHeight = 14.sp)
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(8f))) {
            ReferenceSecondaryButton("Cancel", Icons.Default.Close, onClose, Modifier.weight(1f).height(metrics.px(34f)))
            ReferencePrimaryButton("Save Changes", Icons.Default.Check, {
                onSave(title, description, tagText.split(","), coverPath)
            }, Modifier.weight(1f).height(metrics.px(34f)))
        }
        Spacer(Modifier.height(metrics.px(8f)))
        SheetActionButton("Delete Playlist", Icons.Default.Delete, onDelete, destructive = true)
    }
}

@Composable
private fun PlaylistSideSheet(title: String, onClose: () -> Unit, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Column(
        modifier = modifier
            .width(metrics.px(310f))
            .fillMaxHeight()
            .clip(RoundedCornerShape(topStart = metrics.px(13f), bottomStart = metrics.px(13f)))
            .background(Brush.verticalGradient(listOf(Color(0xFF0A1021), Color(0xFF11112D), Color(0xFF090D1B))))
            .border(1.dp, BorderLow.copy(alpha = 0.76f), RoundedCornerShape(topStart = metrics.px(13f), bottomStart = metrics.px(13f)))
            .padding(metrics.px(18f)),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Icon(Icons.Default.Close, "Close", tint = TextPrimary, modifier = Modifier.size(metrics.px(18f)).clickable(onClick = onClose))
        }
        Spacer(Modifier.height(metrics.px(18f)))
        content()
    }
}

@Composable
private fun sheetTextFieldModifier(): Modifier {
    val metrics = LocalHomeReferenceMetrics.current
    return Modifier
        .clip(RoundedCornerShape(metrics.px(7f)))
        .background(Color(0xFF11182B))
        .border(1.dp, BorderLow.copy(alpha = 0.64f), RoundedCornerShape(metrics.px(7f)))
}

@Composable
private fun PlaylistRightRail(
    relatedPlaylists: List<PlaylistItem>,
    tags: List<String>,
    onOpen: (PlaylistItem) -> Unit,
    onPlay: (PlaylistItem) -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier,
) {
    val metrics = LocalHomeReferenceMetrics.current
    Column(modifier, verticalArrangement = Arrangement.spacedBy(metrics.px(10f))) {
        RelatedPlaylistRail("More like this", relatedPlaylists, onOpen, onPlay, onSeeAll, Modifier.fillMaxWidth().height(metrics.px(230f)))
        RailCard("Mood Notes") {
            Text("Best when you want a focused listening session. Tags are generated from available playlist and track metadata.", color = TextSecondary, fontSize = 10.sp, lineHeight = 14.sp)
            Spacer(Modifier.height(metrics.px(9f)))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(metrics.px(6f))) {
                tags.take(5).forEach { TruthChip(it) }
            }
        }
        RailCard("Playlist tools") {
            Text("Use the row menu to add songs, play next, download, move songs in editable playlists, or remove tracks.", color = TextSecondary, fontSize = 10.sp, lineHeight = 14.sp)
        }
    }
}

@Composable
private fun RelatedPlaylistRail(
    title: String,
    related: List<PlaylistItem>,
    onOpen: (PlaylistItem) -> Unit,
    onPlay: (PlaylistItem) -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalHomeReferenceMetrics.current
    RailCard(title, modifier) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(1f))
            Text("See all", color = IrisSoft, fontSize = 8.5.sp, modifier = Modifier.clickable(onClick = onSeeAll))
        }
        related.take(6).forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth().height(metrics.px(38f)).clickable { onOpen(item) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsyncImage(item.thumbnail?.toHighResThumbnail(), item.title, Modifier.size(metrics.px(30f)).clip(RoundedCornerShape(metrics.px(5f))), contentScale = ContentScale.Crop)
                Spacer(Modifier.width(metrics.px(8f)))
                Column(Modifier.weight(1f)) {
                    Text(item.title, color = TextPrimary, fontSize = 9.4.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(item.songCountText ?: item.author?.name ?: "Playlist", color = TextSecondary, fontSize = 7.8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Box(Modifier.size(metrics.px(21f)).clip(CircleShape).background(Color(0xFFDCDDF3)).clickable { onPlay(item) }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.PlayArrow, null, tint = Color(0xFF101124), modifier = Modifier.size(metrics.px(13f)))
                }
            }
        }
    }
}

@Composable
private fun RailCard(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(metrics.px(9f)))
            .background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.82f))
            .border(1.dp, BorderLow.copy(alpha = 0.65f), RoundedCornerShape(metrics.px(9f)))
            .padding(metrics.px(12f)),
        verticalArrangement = Arrangement.spacedBy(metrics.px(7f)),
    ) {
        Text(title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        content()
    }
}

@Composable
private fun PlaylistCheckRow(playlist: SavedQueuePlaylist, checked: Boolean, onToggle: () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(Modifier.fillMaxWidth().height(metrics.px(42f)).clickable(onClick = onToggle), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = { onToggle() })
        AsyncImage(playlist.coverPath ?: playlist.songs.firstOrNull()?.thumbnail?.toHighResThumbnail(), playlist.name, Modifier.size(metrics.px(30f)).clip(RoundedCornerShape(metrics.px(5f))).background(Surface1), contentScale = ContentScale.Crop)
        Spacer(Modifier.width(metrics.px(8f)))
        Column(Modifier.weight(1f)) {
            Text(playlist.name, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${playlist.songs.size} songs", color = TextSecondary, fontSize = 9.sp)
        }
    }
}

@Composable
private fun SearchField(value: String, placeholder: String, onValueChange: (String) -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(
        Modifier.fillMaxWidth().height(metrics.px(36f)).clip(RoundedCornerShape(metrics.px(7f))).background(Color(0xFF11182B)).border(1.dp, BorderLow.copy(alpha = 0.64f), RoundedCornerShape(metrics.px(7f))).padding(horizontal = metrics.px(10f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.Search, null, tint = TextSecondary, modifier = Modifier.size(metrics.px(14f)))
        Spacer(Modifier.width(metrics.px(7f)))
        TextField(value, onValueChange, placeholder = { Text(placeholder) }, singleLine = true, colors = darkTextFieldColors(), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SheetSectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, color = TextMuted, fontSize = 8.3.sp, fontWeight = FontWeight.Bold, modifier = modifier.padding(top = LocalHomeReferenceMetrics.current.px(12f), bottom = LocalHomeReferenceMetrics.current.px(5f)))
}

@Composable
private fun SheetActionButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, destructive: Boolean = false) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(
        Modifier.fillMaxWidth().height(metrics.px(29f)).clip(RoundedCornerShape(metrics.px(7f))).background(if (destructive) Color(0x33FF4C61) else OmniReferenceColors.SurfaceSelected.copy(alpha = 0.58f)).clickable(onClick = onClick).padding(horizontal = metrics.px(10f)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, null, tint = if (destructive) Color(0xFFFF6875) else TextPrimary, modifier = Modifier.size(metrics.px(13f)))
        Spacer(Modifier.width(metrics.px(6f)))
        Text(text, color = if (destructive) Color(0xFFFF6875) else TextPrimary, fontSize = 9.6.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SheetSmallButton(text: String, onClick: () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Box(
        Modifier.height(metrics.px(28f)).clip(RoundedCornerShape(metrics.px(7f))).background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.82f)).border(1.dp, BorderLow.copy(alpha = 0.7f), RoundedCornerShape(metrics.px(7f))).clickable(onClick = onClick).padding(horizontal = metrics.px(10f)),
        contentAlignment = Alignment.Center,
    ) { Text(text, color = TextPrimary, fontSize = 9.5.sp) }
}

@Composable
private fun TinyIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean, onClick: () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Box(
        Modifier.size(metrics.px(25f)).clip(RoundedCornerShape(metrics.px(6f))).background(OmniReferenceColors.SurfaceBase.copy(alpha = if (enabled) 0.8f else 0.28f)).clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, tint = if (enabled) TextSecondary else TextMuted, modifier = Modifier.size(metrics.px(13f))) }
}

@Composable
private fun TruthChip(label: String) {
    val metrics = LocalHomeReferenceMetrics.current
    Box(
        Modifier.clip(RoundedCornerShape(metrics.px(99f))).background(OmniReferenceColors.SurfaceSelected.copy(alpha = 0.62f)).border(1.dp, BorderLow.copy(alpha = 0.7f), RoundedCornerShape(metrics.px(99f))).padding(horizontal = metrics.px(10f), vertical = metrics.px(4f))
    ) {
        Text(label, color = TextPrimary, fontSize = 8.sp, maxLines = 1)
    }
}

@Composable
private fun ReferencePrimaryButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, modifier: Modifier) {
    Row(
        modifier = modifier.clip(RoundedCornerShape(LocalHomeReferenceMetrics.current.px(9f))).background(OmniGradients.primaryAction).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = text, tint = Color.White, modifier = Modifier.size(LocalHomeReferenceMetrics.current.px(13f)))
        Spacer(Modifier.width(LocalHomeReferenceMetrics.current.px(6f)))
        Text(text, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ReferenceSecondaryButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, modifier: Modifier) {
    Row(
        modifier = modifier.clip(RoundedCornerShape(LocalHomeReferenceMetrics.current.px(9f))).background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.78f)).border(1.dp, BorderLow.copy(alpha = 0.75f), RoundedCornerShape(LocalHomeReferenceMetrics.current.px(9f))).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = text, tint = TextPrimary, modifier = Modifier.size(LocalHomeReferenceMetrics.current.px(12f)))
        Spacer(Modifier.width(LocalHomeReferenceMetrics.current.px(6f)))
        Text(text, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun RoundAction(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Box(
        modifier = Modifier.size(metrics.px(32f)).clip(CircleShape).background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.78f)).border(1.dp, BorderLow.copy(alpha = 0.65f), CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = "Playlist action", tint = TextSecondary, modifier = Modifier.size(metrics.px(14f)))
    }
}

@Composable
private fun darkTextFieldColors() = TextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    cursorColor = IrisSoft,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    focusedPlaceholderColor = TextMuted,
    unfocusedPlaceholderColor = TextMuted,
)

@Composable
private fun PlaylistDetailLoading() {
    val metrics = LocalHomeReferenceMetrics.current
    Column(Modifier.fillMaxSize().padding(metrics.px(22f)), verticalArrangement = Arrangement.spacedBy(metrics.px(16f))) {
        Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(22f))) {
            OmniShimmerBlock(Modifier.size(metrics.px(220f)).clip(RoundedCornerShape(metrics.px(10f))))
            Column(verticalArrangement = Arrangement.spacedBy(metrics.px(12f))) {
                OmniShimmerBlock(Modifier.width(metrics.px(280f)).height(metrics.px(34f)))
                OmniShimmerBlock(Modifier.width(metrics.px(420f)).height(metrics.px(20f)))
                OmniShimmerBlock(Modifier.width(metrics.px(300f)).height(metrics.px(30f)))
            }
        }
    }
}

private fun inferredPlaylistTags(title: String, songs: List<SongItem>): List<String> {
    val text = (title + " " + songs.take(8).flatMap { s -> listOf(s.title) + s.artists.map { it.name } + listOfNotNull(s.album?.name) }.joinToString(" ")).lowercase()
    val tags = buildList {
        if (text.contains("chill") || text.contains("lofi") || text.contains("late")) add("Chill")
        if (text.contains("r&b") || text.contains("soul")) add("R&B")
        if (text.contains("night") || text.contains("moon") || text.contains("midnight")) add("Late Night")
        if (text.contains("focus") || text.contains("ambient")) add("Focus")
        if (text.contains("dance") || text.contains("pop")) add("Pop")
    }
    return (tags + listOf("Playlist", "${songs.size} tracks")).distinct().take(6)
}

private fun chooseImageFile(): String? {
    val dialog = FileDialog(Frame(), "Choose playlist cover", FileDialog.LOAD)
    dialog.setFilenameFilter { _, name ->
        name.endsWith(".png", true) || name.endsWith(".jpg", true) || name.endsWith(".jpeg", true)
    }
    dialog.isVisible = true
    val file = dialog.file ?: return null
    val dir = dialog.directory ?: return null
    return File(dir, file).absolutePath
}

private fun formatDurationLong(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes} min"
}

private fun SongItem.durationLabel(): String {
    val value = duration ?: return ""
    return "${value / 60}:${(value % 60).toString().padStart(2, '0')}"
}
