package com.omnitune.app.window.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.omnitune.app.platform.DownloadState
import com.omnitune.app.platform.LikedSongRecord
import com.omnitune.app.platform.SavedQueuePlaylist
import com.omnitune.app.player.NavScreen
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.app.platform.PlaybackState
import com.omnitune.app.window.BorderLow
import com.omnitune.app.window.IrisSoft
import com.omnitune.app.window.LocalHomeReferenceMetrics
import com.omnitune.app.window.OmniGradients
import com.omnitune.app.window.OmniReferenceColors
import com.omnitune.app.window.Surface3
import com.omnitune.app.window.TextMuted
import com.omnitune.app.window.TextPrimary
import com.omnitune.app.window.TextSecondary
import com.omnitune.innertube.models.AlbumItem
import com.omnitune.innertube.models.Artist
import com.omnitune.innertube.models.PlaylistItem
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.toHighResThumbnail
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.random.Random

internal enum class LikedFilter { All, Recent, Downloaded, Explicit }
internal enum class LikedSort { Recent, Oldest, TitleAsc, TitleDesc, Artist, Album, Duration }

@Composable
fun LikedSongsView(player: PlayerViewModel) {
    val records by player.likedSongRecords.collectAsState()
    val likedIds by player.likedSongs.collectAsState()
    val queue by player.queue.collectAsState()
    val discoveryTrending by player.discoveryTrending.collectAsState()
    val discoveryNew by player.discoveryNew.collectAsState()
    val savedPlaylists by player.savedQueuePlaylists.collectAsState()
    val downloadTasks by player.downloadTasks.collectAsState()
    val currentSong by player.currentSong.collectAsState()
    val playbackState by player.playbackState.collectAsState()

    val migratedRecords = remember(records, likedIds, queue, discoveryTrending, discoveryNew, savedPlaylists) {
        val known = (queue + discoveryTrending + discoveryNew.filterIsInstance<SongItem>() + savedPlaylists.flatMap { it.songs })
            .distinctBy { it.id }
        val existingIds = records.map { it.song.id }.toSet()
        records + known
            .filter { it.id in likedIds && it.id !in existingIds }
            .map { LikedSongRecord(it, 0L) }
    }.distinctBy { it.song.id }

    LikedSongsContent(
        records = migratedRecords,
        savedPlaylists = savedPlaylists,
        downloadedIds = downloadTasks.filter { it.state == DownloadState.COMPLETED }.map { it.trackId }.toSet(),
        recommendations = discoveryNew.filterIsInstance<PlaylistItem>().take(8),
        currentSong = currentSong,
        playbackState = playbackState,
        onPlayList = { songs, index -> player.playSongList(songs, index) },
        onShuffle = { player.playShuffledSongs(it) },
        onPlayNext = { player.playNext(it) },
        onAddQueue = { player.addToQueue(it) },
        onAddQueueBulk = { songs -> songs.forEach(player::addToQueue) },
        onUnlike = { player.unlikeSongs(setOf(it)) },
        onUnlikeBulk = { ids -> player.unlikeSongs(ids) },
        onDownload = { player.downloadSong(it) },
        onDownloadBulk = { player.downloadSongs(it) },
        onOpenArtist = { id -> player.openArtist(id) },
        onOpenAlbum = { id -> player.openAlbum(id) },
        onOpenPlaylist = { id -> player.openPlaylist(id) },
        onCreatePlaylist = { name, description -> player.createPlaylist(name, description) },
        onAddToPlaylists = { song, ids -> player.addSongToSavedPlaylists(song, ids) },
        onNavigateSearch = { player.navigateTo(NavScreen.Search) },
        onNavigateBrowse = { player.navigateTo(NavScreen.Browse) },
    )
}

@Composable
private fun LikedSongsContent(
    records: List<LikedSongRecord>,
    savedPlaylists: List<SavedQueuePlaylist>,
    downloadedIds: Set<String>,
    recommendations: List<PlaylistItem>,
    currentSong: SongItem?,
    playbackState: PlaybackState,
    onPlayList: (List<SongItem>, Int) -> Unit,
    onShuffle: (List<SongItem>) -> Unit,
    onPlayNext: (SongItem) -> Unit,
    onAddQueue: (SongItem) -> Unit,
    onAddQueueBulk: (List<SongItem>) -> Unit,
    onUnlike: (String) -> Unit,
    onUnlikeBulk: (Set<String>) -> Unit,
    onDownload: (SongItem) -> Unit,
    onDownloadBulk: (List<SongItem>) -> Unit,
    onOpenArtist: (String) -> Unit,
    onOpenAlbum: (String) -> Unit,
    onOpenPlaylist: (String) -> Unit,
    onCreatePlaylist: (String, String) -> Result<String>,
    onAddToPlaylists: (SongItem, Set<String>) -> Result<Int>,
    onNavigateSearch: () -> Unit,
    onNavigateBrowse: () -> Unit,
) {
    val metrics = LocalHomeReferenceMetrics.current
    var filter by remember { mutableStateOf(LikedFilter.All) }
    var sort by remember { mutableStateOf(LikedSort.Recent) }
    var searchOpen by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var selectedSongId by remember { mutableStateOf<String?>(null) }
    var menuSongId by remember { mutableStateOf<String?>(null) }
    var addSheetSongs by remember { mutableStateOf<List<SongItem>>(emptyList()) }
    var bulkMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var collectionMenu by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }

    val visibleRecords by remember(records, filter, sort, query, downloadedIds) {
        derivedStateOf {
            records
                .filter {
                    when (filter) {
                        LikedFilter.All -> true
                        LikedFilter.Recent -> it.likedAt > 0L && it.likedAt >= System.currentTimeMillis() - 30L * 24L * 60L * 60L * 1000L
                        LikedFilter.Downloaded -> it.song.id in downloadedIds
                        LikedFilter.Explicit -> it.song.explicit
                    }
                }
                .filter {
                    val q = query.trim().lowercase()
                    q.isBlank() ||
                        it.song.title.lowercase().contains(q) ||
                        it.song.artists.any { artist -> artist.name.lowercase().contains(q) } ||
                        (it.song.album?.name?.lowercase()?.contains(q) == true)
                }
                .let { list ->
                    when (sort) {
                        LikedSort.Recent -> list.sortedByDescending { it.likedAt }
                        LikedSort.Oldest -> list.sortedBy { if (it.likedAt == 0L) Long.MAX_VALUE else it.likedAt }
                        LikedSort.TitleAsc -> list.sortedBy { it.song.title.lowercase() }
                        LikedSort.TitleDesc -> list.sortedByDescending { it.song.title.lowercase() }
                        LikedSort.Artist -> list.sortedBy { it.song.artists.firstOrNull()?.name?.lowercase().orEmpty() }
                        LikedSort.Album -> list.sortedBy { it.song.album?.name?.lowercase().orEmpty() }
                        LikedSort.Duration -> list.sortedByDescending { it.song.duration ?: 0 }
                    }
                }
        }
    }
    val visibleSongs = visibleRecords.map { it.song }
    val totalDuration = remember(records) { records.sumOf { it.song.duration ?: 0 } }
    val uniqueArtists = remember(records) { records.flatMap { it.song.artists }.map { it.name.lowercase() }.toSet().size }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val showRail = maxWidth > 1040.dp
        Row(
            Modifier.fillMaxSize().padding(start = metrics.px(18f), top = metrics.px(14f), end = metrics.px(18f)),
            horizontalArrangement = Arrangement.spacedBy(metrics.px(20f)),
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                contentPadding = PaddingValues(bottom = metrics.px(142f)),
                verticalArrangement = Arrangement.spacedBy(metrics.px(6f)),
            ) {
                item {
                    LikedSongsHero(
                        count = records.size,
                        totalDuration = totalDuration,
                        activeFilter = filter,
                        searchOpen = searchOpen,
                        query = query,
                        onFilter = { filter = it },
                        onQuery = { query = it },
                        onPlay = { if (visibleSongs.isNotEmpty()) onPlayList(visibleSongs, 0) },
                        onShuffle = { onShuffle(visibleSongs) },
                        onDownload = {
                            val pending = visibleSongs.filterNot { it.id in downloadedIds }
                            onDownloadBulk(pending)
                            statusMessage = if (pending.isEmpty()) {
                                "All visible liked songs are already downloaded."
                            } else {
                                "Download queued for ${pending.size} liked song${if (pending.size == 1) "" else "s"}."
                            }
                        },
                        onSearch = { searchOpen = !searchOpen; if (!searchOpen) query = "" },
                        collectionMenuOpen = collectionMenu,
                        onCollectionMenu = { collectionMenu = true },
                        onDismissCollectionMenu = { collectionMenu = false },
                        onSort = {
                            sort = it
                            collectionMenu = false
                        },
                        onSelectMode = {
                            bulkMode = true
                            collectionMenu = false
                        },
                    )
                }
                if (statusMessage.isNotBlank()) {
                    item { Text(statusMessage, color = IrisSoft, fontSize = 10.sp, modifier = Modifier.padding(start = metrics.px(4f))) }
                }
                if (records.isEmpty()) {
                    item {
                        LikedSongsEmptyState(onNavigateSearch, onNavigateBrowse)
                    }
                } else {
                    if (bulkMode) {
                        item {
                            LikedBulkToolbar(
                                selectedCount = selectedIds.size,
                                onAddPlaylist = { addSheetSongs = visibleSongs.filter { it.id in selectedIds } },
                                onAddQueue = { onAddQueueBulk(visibleSongs.filter { it.id in selectedIds }) },
                                onDownload = { onDownloadBulk(visibleSongs.filter { it.id in selectedIds }) },
                                onRemove = {
                                    onUnlikeBulk(selectedIds)
                                    selectedIds = emptySet()
                                    bulkMode = false
                                },
                                onClear = {
                                    selectedIds = emptySet()
                                    bulkMode = false
                                },
                            )
                        }
                    }
                    item { LikedTrackHeader(bulkMode) }
                    itemsIndexed(visibleRecords, key = { _, record -> record.song.id }) { index, record ->
                        val song = record.song
                        val active = currentSong?.id == song.id
                        val expanded = selectedSongId == song.id
                        val checked = song.id in selectedIds
                        Column(Modifier.animateContentSize()) {
                            LikedSongRow(
                                index = index,
                                record = record,
                                bulkMode = bulkMode,
                                checked = checked,
                                selected = expanded,
                                active = active,
                                playing = active && playbackState == PlaybackState.PLAYING,
                                downloaded = song.id in downloadedIds,
                                menuOpen = menuSongId == song.id,
                                onToggleChecked = {
                                    selectedIds = if (checked) selectedIds - song.id else selectedIds + song.id
                                },
                                onSelect = { selectedSongId = if (expanded) null else song.id },
                                onPlay = { onPlayList(visibleSongs, index) },
                                onUnlike = { onUnlike(song.id) },
                                onAddPlaylist = { addSheetSongs = listOf(song) },
                                onMenu = { menuSongId = song.id },
                                onDismissMenu = { menuSongId = null },
                                onAction = { action ->
                                    menuSongId = null
                                    when (action) {
                                        LikedTrackAction.PlayNext -> onPlayNext(song)
                                        LikedTrackAction.AddQueue -> onAddQueue(song)
                                        LikedTrackAction.RemoveLiked -> onUnlike(song.id)
                                        LikedTrackAction.AddPlaylist -> addSheetSongs = listOf(song)
                                        LikedTrackAction.Download -> onDownload(song)
                                        LikedTrackAction.ViewAlbum -> song.album?.id?.let(onOpenAlbum)
                                        LikedTrackAction.GoArtist -> song.artists.firstOrNull()?.id?.let(onOpenArtist)
                                    }
                                },
                            )
                            if (expanded) {
                                ExpandedLikedSongDetail(
                                    song = song,
                                    onPlayNext = { onPlayNext(song) },
                                    onAddQueue = { onAddQueue(song) },
                                    onArtist = { song.artists.firstOrNull()?.id?.let(onOpenArtist) },
                                    onAlbum = { song.album?.id?.let(onOpenAlbum) },
                                    onDownload = { onDownload(song) },
                                )
                            }
                        }
                    }
                }
            }
            if (showRail) {
                LikedSongsRail(
                    records = records,
                    recommendations = recommendations,
                    totalDuration = totalDuration,
                    uniqueArtists = uniqueArtists,
                    downloaded = records.count { it.song.id in downloadedIds },
                    onOpenPlaylist = onOpenPlaylist,
                    modifier = Modifier.width(metrics.px(235f)).fillMaxHeight(),
                )
            }
        }

        if (addSheetSongs.isNotEmpty()) {
            LikedAddToPlaylistSheet(
                songs = addSheetSongs,
                playlists = savedPlaylists,
                onClose = { addSheetSongs = emptyList() },
                onCreatePlaylist = onCreatePlaylist,
                onAdd = { ids ->
                    var added = 0
                    addSheetSongs.forEach { song ->
                        onAddToPlaylists(song, ids).onSuccess { added += it }
                    }
                    statusMessage = "Added $added playlist update${if (added == 1) "" else "s"}."
                    addSheetSongs = emptyList()
                },
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 0.dp, end = metrics.px(2f), bottom = metrics.px(112f)),
            )
        }
    }
}
