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
    val tags = localPlaylist?.tags?.ifEmpty { inferPlaylistTags(playlist.title, songs) } ?: inferPlaylistTags(playlist.title, songs)
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
                                    PlaylistTrackAction.PlayNext -> onPlayNext(song)
                                    PlaylistTrackAction.AddToQueue -> onAddSong(song)
                                    PlaylistTrackAction.ToggleLike -> onLikeSong(song)
                                    PlaylistTrackAction.AddToPlaylist -> addSheetSong = song
                                    PlaylistTrackAction.Download -> onDownloadSong(song)
                                    PlaylistTrackAction.ViewAlbum -> song.album?.id?.let(onOpenAlbum)
                                    PlaylistTrackAction.GoToArtist -> song.artists.firstOrNull()?.id?.let(onOpenArtist)
                                    PlaylistTrackAction.Remove -> localPlaylist?.let { onRemoveSong(it.id, song.id) }
                                    PlaylistTrackAction.MoveUp -> localPlaylist?.let { onMoveSong(it.id, index, index - 1) }
                                    PlaylistTrackAction.MoveDown -> localPlaylist?.let { onMoveSong(it.id, index, index + 1) }
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