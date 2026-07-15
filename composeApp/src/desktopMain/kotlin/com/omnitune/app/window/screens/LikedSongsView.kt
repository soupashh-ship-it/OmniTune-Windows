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

private enum class LikedFilter { All, Recent, Downloaded, Explicit }
private enum class LikedSort { Recent, Oldest, TitleAsc, TitleDesc, Artist, Album, Duration }

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

@Composable
private fun LikedSongsHero(
    count: Int,
    totalDuration: Int,
    activeFilter: LikedFilter,
    searchOpen: Boolean,
    query: String,
    onFilter: (LikedFilter) -> Unit,
    onQuery: (String) -> Unit,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    onDownload: () -> Unit,
    onSearch: () -> Unit,
    collectionMenuOpen: Boolean,
    onCollectionMenu: () -> Unit,
    onDismissCollectionMenu: () -> Unit,
    onSort: (LikedSort) -> Unit,
    onSelectMode: () -> Unit,
) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(Modifier.fillMaxWidth().padding(bottom = metrics.px(10f)), horizontalArrangement = Arrangement.spacedBy(metrics.px(24f))) {
        LikedHeartArtwork(Modifier.size(metrics.px(216f)))
        Column(Modifier.weight(1f).padding(top = metrics.px(12f))) {
            Text("Library  ›  Liked Songs", color = TextSecondary, fontSize = 10.sp)
            Spacer(Modifier.height(metrics.px(18f)))
            Text("YOUR COLLECTION", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
            Text("Liked Songs", color = TextPrimary, fontSize = 38.sp, lineHeight = 43.sp, fontWeight = FontWeight.Bold)
            Text("Every track you loved, saved in one place.", color = TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(metrics.px(12f)))
            Text("Saved by You", color = TextSecondary, fontSize = 11.sp)
            Text("$count songs  •  ${formatDurationLong(totalDuration)}", color = TextSecondary, fontSize = 11.sp)
            Spacer(Modifier.height(metrics.px(13f)))
            Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(8f)), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                LikedChip("All", activeFilter == LikedFilter.All) { onFilter(LikedFilter.All) }
                LikedChip("Recent", activeFilter == LikedFilter.Recent) { onFilter(LikedFilter.Recent) }
                LikedChip("Downloaded", activeFilter == LikedFilter.Downloaded) { onFilter(LikedFilter.Downloaded) }
                LikedChip("Explicit", activeFilter == LikedFilter.Explicit) { onFilter(LikedFilter.Explicit) }
            }
            Spacer(Modifier.height(metrics.px(14f)))
            Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(10f)), verticalAlignment = Alignment.CenterVertically) {
                LikedPrimaryButton("Play", Icons.Default.PlayArrow, onPlay, Modifier.width(metrics.px(84f)).height(metrics.px(32f)))
                LikedSecondaryButton("Shuffle", Icons.Default.Shuffle, onShuffle, Modifier.width(metrics.px(92f)).height(metrics.px(32f)))
                LikedSecondaryButton("Download", Icons.Default.Download, onDownload, Modifier.width(metrics.px(106f)).height(metrics.px(32f)))
                LikedRoundButton(Icons.Default.Search, onSearch)
                Box {
                    LikedRoundButton(Icons.Default.MoreHoriz, onCollectionMenu)
                    DropdownMenu(expanded = collectionMenuOpen, onDismissRequest = onDismissCollectionMenu, containerColor = Color(0xF20E1221), tonalElevation = 0.dp) {
                        LikedMenuAction(Icons.Default.Check, "Enter selection mode", onClick = onSelectMode)
                        LikedMenuAction(Icons.Default.Download, "Download all", onClick = onDownload)
                        LikedMenuSeparator()
                        LikedMenuAction(Icons.Default.GraphicEq, "Sort by recently liked", onClick = { onSort(LikedSort.Recent) })
                        LikedMenuAction(Icons.Default.GraphicEq, "Sort by oldest liked", onClick = { onSort(LikedSort.Oldest) })
                        LikedMenuAction(Icons.Default.GraphicEq, "Sort by title A-Z", onClick = { onSort(LikedSort.TitleAsc) })
                        LikedMenuAction(Icons.Default.GraphicEq, "Sort by artist", onClick = { onSort(LikedSort.Artist) })
                    }
                }
            }
            if (searchOpen) {
                Spacer(Modifier.height(metrics.px(10f)))
                LikedSearchField(query, onQuery, Modifier.width(metrics.px(310f)).height(metrics.px(34f)))
            }
        }
    }
}

@Composable
private fun LikedHeartArtwork(modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFF6F35FF), Color(0xFF24106F), Color(0xFF070B20)),
                )
            )
            .border(1.dp, BorderLow.copy(alpha = 0.7f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text("♡", color = Color(0xFFE9D8FF), fontSize = 118.sp, fontWeight = FontWeight.Light)
        Text("♥", color = Color(0xFF8B5CFF).copy(alpha = 0.86f), fontSize = 58.sp)
    }
}

@Composable
private fun LikedTrackHeader(bulkMode: Boolean) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(Modifier.fillMaxWidth().height(metrics.px(24f)).padding(horizontal = metrics.px(8f)), verticalAlignment = Alignment.CenterVertically) {
        if (bulkMode) Spacer(Modifier.width(metrics.px(28f)))
        Text("#", color = TextMuted, fontSize = 8.sp, modifier = Modifier.width(metrics.px(34f)))
        Text("TITLE", color = TextMuted, fontSize = 8.sp, modifier = Modifier.weight(1.75f))
        Text("ARTIST", color = TextMuted, fontSize = 8.sp, modifier = Modifier.weight(1.0f))
        Text("ALBUM", color = TextMuted, fontSize = 8.sp, modifier = Modifier.weight(1.0f))
        Text("DATE LIKED ↓", color = TextMuted, fontSize = 8.sp, modifier = Modifier.weight(0.9f))
        Text("◷", color = TextMuted, fontSize = 10.sp, modifier = Modifier.width(metrics.px(54f)))
        Spacer(Modifier.width(metrics.px(96f)))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LikedSongRow(
    index: Int,
    record: LikedSongRecord,
    bulkMode: Boolean,
    checked: Boolean,
    selected: Boolean,
    active: Boolean,
    playing: Boolean,
    downloaded: Boolean,
    menuOpen: Boolean,
    onToggleChecked: () -> Unit,
    onSelect: () -> Unit,
    onPlay: () -> Unit,
    onUnlike: () -> Unit,
    onAddPlaylist: () -> Unit,
    onMenu: () -> Unit,
    onDismissMenu: () -> Unit,
    onAction: (LikedTrackAction) -> Unit,
) {
    val metrics = LocalHomeReferenceMetrics.current
    val song = record.song
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(metrics.px(38f))
            .clip(RoundedCornerShape(metrics.px(7f)))
            .background(
                when {
                    checked -> OmniReferenceColors.SurfaceSelected.copy(alpha = 0.86f)
                    selected -> OmniReferenceColors.SurfaceSelected.copy(alpha = 0.72f)
                    active -> OmniReferenceColors.SurfaceSelected.copy(alpha = 0.56f)
                    else -> OmniReferenceColors.SurfaceBase.copy(alpha = 0.26f)
                }
            )
            .border(1.dp, if (selected) OmniReferenceColors.Accent.copy(alpha = 0.86f) else Color.Transparent, RoundedCornerShape(metrics.px(7f)))
            .combinedClickable(onClick = onSelect, onDoubleClick = onPlay, onLongClick = onMenu)
            .pointerInput(onMenu) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed) {
                            onMenu()
                            event.changes.forEach { it.consume() }
                        }
                    }
                }
            }
            .padding(horizontal = metrics.px(8f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (bulkMode) {
            Checkbox(checked = checked, onCheckedChange = { onToggleChecked() }, modifier = Modifier.size(metrics.px(26f)))
        }
        Box(Modifier.width(metrics.px(34f)), contentAlignment = Alignment.CenterStart) {
            if (playing) Icon(Icons.Default.GraphicEq, null, tint = IrisSoft, modifier = Modifier.size(metrics.px(13f)))
            else if (active) Icon(Icons.Default.PlayArrow, null, tint = IrisSoft, modifier = Modifier.size(metrics.px(13f)))
            else Text("${index + 1}", color = TextSecondary, fontSize = 9.sp)
        }
        AsyncImage(song.thumbnail.toHighResThumbnail(), song.title, Modifier.size(metrics.px(25f)).clip(RoundedCornerShape(metrics.px(4f))).clickable(onClick = onPlay), contentScale = ContentScale.Crop)
        Spacer(Modifier.width(metrics.px(8f)))
        Row(Modifier.weight(1.75f), verticalAlignment = Alignment.CenterVertically) {
            Text(song.title, color = TextPrimary, fontSize = 9.8.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (song.explicit) {
                Spacer(Modifier.width(metrics.px(4f)))
                Text("E", color = TextMuted, fontSize = 7.sp, modifier = Modifier.clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.12f)).padding(horizontal = 3.dp))
            }
        }
        Text(song.artists.joinToString(", ") { it.name }, color = TextSecondary, fontSize = 8.6.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1.0f))
        Text(song.album?.name ?: "Single", color = TextSecondary, fontSize = 8.6.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1.0f))
        Text(formatLikedDate(record.likedAt), color = TextSecondary, fontSize = 8.4.sp, maxLines = 1, modifier = Modifier.weight(0.9f))
        Text(song.durationLabel(), color = TextSecondary, fontSize = 8.4.sp, modifier = Modifier.width(metrics.px(54f)))
        Icon(Icons.Default.Favorite, "Remove from liked songs", tint = IrisSoft, modifier = Modifier.size(metrics.px(13f)).clickable(onClick = onUnlike))
        Spacer(Modifier.width(metrics.px(17f)))
        Icon(Icons.Default.Add, "Add to playlist", tint = TextSecondary, modifier = Modifier.size(metrics.px(15f)).clickable(onClick = onAddPlaylist))
        Spacer(Modifier.width(metrics.px(15f)))
        Box {
            Icon(Icons.Default.MoreHoriz, "Track actions", tint = TextSecondary, modifier = Modifier.size(metrics.px(15f)).clickable(onClick = onMenu))
            LikedSongContextMenu(menuOpen, downloaded, song.album?.id != null, song.artists.any { !it.id.isNullOrBlank() }, onDismissMenu, onAction)
        }
    }
}

private enum class LikedTrackAction { PlayNext, AddQueue, RemoveLiked, AddPlaylist, Download, ViewAlbum, GoArtist }

@Composable
private fun LikedSongContextMenu(expanded: Boolean, downloaded: Boolean, hasAlbum: Boolean, hasArtist: Boolean, onDismiss: () -> Unit, onAction: (LikedTrackAction) -> Unit) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.width(214.dp).clip(RoundedCornerShape(12.dp)).border(1.dp, BorderLow.copy(alpha = 0.76f), RoundedCornerShape(12.dp)).background(Color(0xF20E1221)).padding(vertical = 7.dp),
        containerColor = Color(0xF20E1221),
        tonalElevation = 0.dp,
        shadowElevation = 14.dp,
    ) {
        LikedMenuAction(Icons.Default.Shuffle, "Play next", onClick = { onAction(LikedTrackAction.PlayNext) })
        LikedMenuAction(Icons.AutoMirrored.Filled.QueueMusic, "Add to queue", onClick = { onAction(LikedTrackAction.AddQueue) })
        LikedMenuAction(Icons.Default.Favorite, "Remove from Liked Songs", textColor = Color(0xFFFF6575), iconTint = Color(0xFFFF4B61), onClick = { onAction(LikedTrackAction.RemoveLiked) })
        LikedMenuAction(Icons.Default.Add, "Add to playlist", trailing = ">", onClick = { onAction(LikedTrackAction.AddPlaylist) })
        LikedMenuSeparator()
        LikedMenuAction(Icons.Default.Download, if (downloaded) "Downloaded" else "Download", onClick = { onAction(LikedTrackAction.Download) })
        if (hasAlbum) LikedMenuAction(Icons.Default.GraphicEq, "View album", onClick = { onAction(LikedTrackAction.ViewAlbum) })
        if (hasArtist) LikedMenuAction(Icons.Default.Person, "Go to artist", onClick = { onAction(LikedTrackAction.GoArtist) })
    }
}

@Composable
private fun ExpandedLikedSongDetail(song: SongItem, onPlayNext: () -> Unit, onAddQueue: () -> Unit, onArtist: () -> Unit, onAlbum: () -> Unit, onDownload: () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(
        Modifier.fillMaxWidth().height(metrics.px(100f)).clip(RoundedCornerShape(metrics.px(8f)))
            .background(Brush.horizontalGradient(listOf(Color(0xAA151246), Color(0x66101228), Color(0xAA080D1B))))
            .border(1.dp, OmniReferenceColors.Accent.copy(alpha = 0.38f), RoundedCornerShape(metrics.px(8f))).padding(metrics.px(10f)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(metrics.px(14f)),
    ) {
        AsyncImage(song.thumbnail.toHighResThumbnail(), song.title, Modifier.size(metrics.px(72f)).clip(RoundedCornerShape(metrics.px(8f))), contentScale = ContentScale.Crop)
        Column(Modifier.weight(1f)) {
            Text(song.title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artists.joinToString(", ") { it.name }, color = TextSecondary, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${song.album?.name ?: "Single"}  •  ${song.durationLabel()}", color = TextSecondary, fontSize = 8.5.sp)
            Spacer(Modifier.height(metrics.px(7f)))
            Text("Lyrics preview appears in Now Playing when lyrics are available.", color = TextSecondary, fontSize = 8.2.sp, maxLines = 2)
        }
        Column(Modifier.width(metrics.px(126f)), verticalArrangement = Arrangement.spacedBy(metrics.px(6f))) {
            LikedSheetButton("Play Next", Icons.Default.PlayArrow, onPlayNext)
            LikedSheetButton("Add to Queue", Icons.Default.Add, onAddQueue)
            LikedSheetButton("Go to Artist", Icons.Default.Person, onArtist)
            LikedSheetButton("View Album", Icons.Default.GraphicEq, onAlbum, enabled = song.album?.id != null)
        }
    }
}

@Composable
private fun LikedBulkToolbar(selectedCount: Int, onAddPlaylist: () -> Unit, onAddQueue: () -> Unit, onDownload: () -> Unit, onRemove: () -> Unit, onClear: () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(Modifier.fillMaxWidth().height(metrics.px(43f)).clip(RoundedCornerShape(metrics.px(8f))).background(Color(0x99101428)).border(1.dp, BorderLow.copy(alpha = 0.55f), RoundedCornerShape(metrics.px(8f))).padding(horizontal = metrics.px(12f)), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(metrics.px(8f))) {
        Text("$selectedCount selected", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(metrics.px(110f)))
        LikedSecondaryButton("Add to Playlist", Icons.Default.Add, onAddPlaylist, Modifier.width(metrics.px(126f)).height(metrics.px(28f)), selectedCount > 0)
        LikedSecondaryButton("Add to Queue", Icons.AutoMirrored.Filled.QueueMusic, onAddQueue, Modifier.width(metrics.px(112f)).height(metrics.px(28f)), selectedCount > 0)
        LikedSecondaryButton("Download Selected", Icons.Default.Download, onDownload, Modifier.width(metrics.px(138f)).height(metrics.px(28f)), selectedCount > 0)
        LikedSecondaryButton("Remove from Liked Songs", Icons.Default.Favorite, onRemove, Modifier.width(metrics.px(168f)).height(metrics.px(28f)), selectedCount > 0)
        LikedSecondaryButton("Clear Selection", Icons.Default.Close, onClear, Modifier.width(metrics.px(120f)).height(metrics.px(28f)))
    }
}

@Composable
private fun LikedAddToPlaylistSheet(
    songs: List<SongItem>,
    playlists: List<SavedQueuePlaylist>,
    onClose: () -> Unit,
    onCreatePlaylist: (String, String) -> Result<String>,
    onAdd: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalHomeReferenceMetrics.current
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(setOf<String>()) }
    var creating by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    val filtered = playlists.filter { it.name.contains(query, ignoreCase = true) }
    Column(
        modifier.width(metrics.px(272f)).fillMaxHeight().clip(RoundedCornerShape(metrics.px(12f))).background(Color(0xF20E1221)).border(1.dp, BorderLow.copy(alpha = 0.78f), RoundedCornerShape(metrics.px(12f))).padding(metrics.px(16f)),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Add to Playlist", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Icon(Icons.Default.Close, "Close", tint = TextPrimary, modifier = Modifier.size(metrics.px(18f)).clickable(onClick = onClose))
        }
        Spacer(Modifier.height(metrics.px(14f)))
        LikedSearchField(query, { query = it }, Modifier.fillMaxWidth().height(metrics.px(34f)), "Search playlists")
        Spacer(Modifier.height(metrics.px(14f)))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("ALL PLAYLISTS", color = TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            LikedTinyButton("+ Create New Playlist") { creating = !creating }
        }
        if (creating) {
            Spacer(Modifier.height(metrics.px(8f)))
            LikedSearchField(newName, { newName = it.take(80) }, Modifier.fillMaxWidth().height(metrics.px(34f)), "Playlist name")
            Spacer(Modifier.height(metrics.px(6f)))
            LikedSheetButton("Create", Icons.Default.Add, {
                onCreatePlaylist(newName, "").onSuccess { newName = ""; creating = false }
            })
        }
        Spacer(Modifier.height(metrics.px(8f)))
        LazyColumn(Modifier.weight(1f)) {
            itemsIndexed(filtered, key = { _, p -> p.id }) { _, playlist ->
                Row(Modifier.fillMaxWidth().height(metrics.px(42f)).clickable {
                    selected = if (playlist.id in selected) selected - playlist.id else selected + playlist.id
                }, verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(playlist.id in selected, onCheckedChange = {
                        selected = if (playlist.id in selected) selected - playlist.id else selected + playlist.id
                    })
                    Column(Modifier.weight(1f)) {
                        Text(playlist.name, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${playlist.songs.size} songs", color = TextSecondary, fontSize = 8.sp)
                    }
                }
            }
        }
        val first = songs.first()
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(metrics.px(8f))).background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.82f)).padding(metrics.px(8f)), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(first.thumbnail.toHighResThumbnail(), first.title, Modifier.size(metrics.px(38f)).clip(RoundedCornerShape(metrics.px(6f))), contentScale = ContentScale.Crop)
            Spacer(Modifier.width(metrics.px(8f)))
            Column(Modifier.weight(1f)) {
                Text(if (songs.size == 1) first.title else "${songs.size} selected songs", color = TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(if (songs.size == 1) first.artists.joinToString(", ") { it.name } else "Bulk add to playlist", color = TextSecondary, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Spacer(Modifier.height(metrics.px(8f)))
        LikedPrimaryButton("Add to Playlist", Icons.Default.Check, { onAdd(selected) }, Modifier.fillMaxWidth().height(metrics.px(34f)), selected.isNotEmpty())
    }
}

@Composable
private fun LikedSongsRail(records: List<LikedSongRecord>, recommendations: List<PlaylistItem>, totalDuration: Int, uniqueArtists: Int, downloaded: Int, onOpenPlaylist: (String) -> Unit, modifier: Modifier = Modifier) {
    val metrics = LocalHomeReferenceMetrics.current
    Column(modifier, verticalArrangement = Arrangement.spacedBy(metrics.px(10f))) {
        LikedRailCard("Because you liked these", Modifier.fillMaxWidth().height(metrics.px(220f))) {
            recommendations.take(5).forEach { item ->
                Row(Modifier.fillMaxWidth().height(metrics.px(36f)).clickable { onOpenPlaylist(item.id) }, verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(item.thumbnail?.toHighResThumbnail(), item.title, Modifier.size(metrics.px(28f)).clip(RoundedCornerShape(metrics.px(5f))), contentScale = ContentScale.Crop)
                    Spacer(Modifier.width(metrics.px(8f)))
                    Column(Modifier.weight(1f)) {
                        Text(item.title, color = TextPrimary, fontSize = 8.6.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(item.songCountText ?: "Playlist", color = TextSecondary, fontSize = 7.5.sp, maxLines = 1)
                    }
                    Icon(Icons.Default.PlayArrow, null, tint = Color(0xFFD8DCFF), modifier = Modifier.size(metrics.px(21f)).clip(CircleShape).background(Color(0xFFD8DCFF).copy(alpha = 0.16f)).padding(4.dp))
                }
            }
        }
        LikedRailCard("Recently liked artists", Modifier.fillMaxWidth().height(metrics.px(92f))) {
            Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(8f)), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                records.flatMap { record -> record.song.artists.map { it to record.song.thumbnail } }.distinctBy { it.first.name.lowercase() }.take(6).forEach { (artist, art) ->
                    Column(Modifier.width(metrics.px(36f)), horizontalAlignment = Alignment.CenterHorizontally) {
                        AsyncImage(art?.toHighResThumbnail(), artist.name, Modifier.size(metrics.px(30f)).clip(CircleShape), contentScale = ContentScale.Crop)
                        Text(artist.name, color = TextPrimary, fontSize = 6.8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
        LikedRailCard("Your collection", Modifier.fillMaxWidth().height(metrics.px(78f))) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                LikedStat("${records.size}", "Songs")
                LikedStat(formatDurationLong(totalDuration), "Total time")
                LikedStat("$uniqueArtists", "Artists")
                LikedStat("$downloaded", "Downloaded")
            }
        }
    }
}

@Composable private fun LikedRailCard(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Column(modifier.clip(RoundedCornerShape(metrics.px(10f))).background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.78f)).border(1.dp, BorderLow.copy(alpha = 0.62f), RoundedCornerShape(metrics.px(10f))).padding(metrics.px(12f))) {
        Text(title, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(metrics.px(8f)))
        content()
    }
}

@Composable private fun LikedStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = IrisSoft, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(label, color = TextSecondary, fontSize = 7.sp, maxLines = 1)
    }
}

@Composable private fun LikedSongsEmptyState(onSearch: () -> Unit, onBrowse: () -> Unit) {
    Column(Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(14.dp)).background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.55f)).border(1.dp, BorderLow.copy(alpha = 0.55f), RoundedCornerShape(14.dp)).padding(26.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("No liked songs yet", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Heart songs from Search, Browse, Radio, Artist, Album, or Now Playing and they will appear here.", color = TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            LikedPrimaryButton("Search music", Icons.Default.Search, onSearch, Modifier.width(128.dp).height(34.dp))
            LikedSecondaryButton("Browse", Icons.Default.GraphicEq, onBrowse, Modifier.width(108.dp).height(34.dp))
        }
    }
}

@Composable private fun LikedChip(text: String, active: Boolean, onClick: () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    val shape = RoundedCornerShape(metrics.px(9f))
    val chipBase = Modifier.height(metrics.px(25f)).clip(shape).let { base ->
        if (active) base.background(OmniGradients.primaryAction) else base.background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.72f))
    }
    Box(chipBase.border(1.dp, if (active) Color.Transparent else BorderLow.copy(alpha = 0.72f), shape).clickable(onClick = onClick).padding(horizontal = metrics.px(12f)), contentAlignment = Alignment.Center) {
        Text(text, color = if (active) Color.White else TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable private fun LikedPrimaryButton(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier, enabled: Boolean = true) {
    val shape = RoundedCornerShape(9.dp)
    val backgroundModifier = if (enabled) {
        modifier.clip(shape).background(OmniGradients.primaryAction)
    } else {
        modifier.clip(shape).background(Surface3)
    }
    Row(backgroundModifier.clickable(enabled = enabled, onClick = onClick), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(7.dp))
        Text(text, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable private fun LikedSecondaryButton(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier, enabled: Boolean = true) {
    Row(modifier.clip(RoundedCornerShape(9.dp)).background(OmniReferenceColors.SurfaceBase.copy(alpha = if (enabled) 0.78f else 0.34f)).border(1.dp, BorderLow.copy(alpha = 0.72f), RoundedCornerShape(9.dp)).clickable(enabled = enabled, onClick = onClick), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Icon(icon, null, tint = if (enabled) TextPrimary else TextMuted, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(7.dp))
        Text(text, color = if (enabled) TextPrimary else TextMuted, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable private fun LikedRoundButton(icon: ImageVector, onClick: () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Box(Modifier.size(metrics.px(32f)).clip(RoundedCornerShape(metrics.px(9f))).background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.78f)).border(1.dp, BorderLow.copy(alpha = 0.72f), RoundedCornerShape(metrics.px(9f))).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = TextPrimary, modifier = Modifier.size(metrics.px(14f)))
    }
}

@Composable private fun LikedSearchField(value: String, onValue: (String) -> Unit, modifier: Modifier, placeholder: String = "Search liked songs") {
    Row(modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFF11182B)).border(1.dp, BorderLow.copy(alpha = 0.66f), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Search, null, tint = TextSecondary, modifier = Modifier.size(15.dp))
        TextField(value, onValue, placeholder = { Text(placeholder) }, singleLine = true, colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, disabledContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary), modifier = Modifier.weight(1f))
    }
}

@Composable private fun LikedSheetButton(text: String, icon: ImageVector, onClick: () -> Unit, enabled: Boolean = true) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(Modifier.fillMaxWidth().height(metrics.px(29f)).clip(RoundedCornerShape(metrics.px(7f))).background(OmniReferenceColors.SurfaceSelected.copy(alpha = if (enabled) 0.58f else 0.24f)).clickable(enabled = enabled, onClick = onClick).padding(horizontal = metrics.px(10f)), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Icon(icon, null, tint = if (enabled) TextPrimary else TextMuted, modifier = Modifier.size(metrics.px(13f)))
        Spacer(Modifier.width(metrics.px(8f)))
        Text(text, color = if (enabled) TextPrimary else TextMuted, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable private fun LikedTinyButton(text: String, onClick: () -> Unit) {
    Box(Modifier.height(24.dp).clip(RoundedCornerShape(7.dp)).background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.82f)).border(1.dp, BorderLow.copy(alpha = 0.7f), RoundedCornerShape(7.dp)).clickable(onClick = onClick).padding(horizontal = 9.dp), contentAlignment = Alignment.Center) {
        Text(text, color = TextPrimary, fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable private fun LikedMenuAction(icon: ImageVector, label: String, textColor: Color = TextPrimary, iconTint: Color = TextSecondary, trailing: String? = null, onClick: () -> Unit) {
    Row(Modifier.width(210.dp).height(30.dp).clickable(onClick = onClick).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = iconTint, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (trailing != null) Text(trailing, color = TextMuted, fontSize = 12.sp)
    }
}

@Composable private fun LikedMenuSeparator() {
    Box(Modifier.width(210.dp).padding(horizontal = 12.dp, vertical = 5.dp).height(1.dp).background(BorderLow.copy(alpha = 0.44f)))
}

private fun formatLikedDate(value: Long): String {
    if (value <= 0L) return "Saved"
    return DateTimeFormatter.ofPattern("MMM d, yyyy").withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(value))
}

private fun formatDurationLong(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

private fun SongItem.durationLabel(): String {
    val value = duration ?: return ""
    return "${value / 60}:${(value % 60).toString().padStart(2, '0')}"
}
