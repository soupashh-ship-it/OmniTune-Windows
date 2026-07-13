package com.omnitune.app.window.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.app.platform.PlaybackState
import com.omnitune.app.window.BorderLow
import com.omnitune.app.window.IrisSoft
import com.omnitune.app.window.LocalHomeReferenceMetrics
import com.omnitune.app.window.OmniReferenceColors
import com.omnitune.app.window.Surface1
import com.omnitune.app.window.Surface2
import com.omnitune.app.window.TextMuted
import com.omnitune.app.window.TextPrimary
import com.omnitune.app.window.TextSecondary
import com.omnitune.innertube.models.AlbumItem
import com.omnitune.innertube.models.Artist
import com.omnitune.innertube.models.ArtistItem
import com.omnitune.innertube.models.PlaylistItem
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.models.YTItem
import com.omnitune.innertube.toHighResThumbnail

private enum class LibraryTab { Songs, Albums, Artists, Playlists, Downloads, Favorites }
private enum class LibrarySort { RecentlyAdded, Title, Artist }

private data class LibraryArtist(
    val name: String,
    val artwork: String?,
)

@Composable
fun LibraryView(player: PlayerViewModel) {
    val likedIds by player.likedSongs.collectAsState()
    val queue by player.queue.collectAsState()
    val currentSong by player.currentSong.collectAsState()
    val playbackState by player.playbackState.collectAsState()
    val discoveryTrending by player.discoveryTrending.collectAsState()
    val discoveryNew by player.discoveryNew.collectAsState()
    val savedQueuePlaylists by player.savedQueuePlaylists.collectAsState()

    var tab by remember { mutableStateOf(LibraryTab.Songs) }
    var sort by remember { mutableStateOf(LibrarySort.RecentlyAdded) }
    var gridView by remember { mutableStateOf(true) }
    var showAllRecent by remember { mutableStateOf(false) }

    val likedSongs = remember(likedIds, queue, discoveryTrending) {
        (queue + discoveryTrending).filter { likedIds.contains(it.id) }.distinctBy { it.id }
    }
    val librarySongs = remember(queue, likedSongs, discoveryTrending) {
        (likedSongs + queue + discoveryTrending).distinctBy { it.id }
    }
    val sortedSongs = remember(librarySongs, sort) {
        when (sort) {
            LibrarySort.RecentlyAdded -> librarySongs
            LibrarySort.Title -> librarySongs.sortedBy { it.title.lowercase() }
            LibrarySort.Artist -> librarySongs.sortedBy { it.artists.firstOrNull()?.name?.lowercase().orEmpty() }
        }
    }
    val recentItems = remember(discoveryNew, librarySongs) {
        (discoveryNew + librarySongs).distinctBy { it.stableIdentity() }
    }
    val albums = remember(discoveryNew) { discoveryNew.filterIsInstance<AlbumItem>().distinctBy { it.id } }
    val artists = remember(librarySongs, discoveryNew) {
        (discoveryNew.filterIsInstance<ArtistItem>().map { LibraryArtist(it.title, it.thumbnail) } + librarySongs.flatMap { song ->
            song.artists.map { LibraryArtist(it.name, song.thumbnail) }
        }).filter { it.name.isNotBlank() }.distinctBy { it.name.lowercase() }
    }
    val playlists = remember(discoveryNew, savedQueuePlaylists) {
        val local = savedQueuePlaylists.map { saved ->
            PlaylistItem(
                id = saved.id,
                title = saved.name,
                author = Artist("OmniTune", null),
                songCountText = "${saved.songs.size} songs",
                thumbnail = saved.songs.firstOrNull()?.thumbnail,
                playEndpoint = null,
                shuffleEndpoint = null,
                radioEndpoint = null,
                isEditable = true,
            )
        }
        (local + discoveryNew.filterIsInstance<PlaylistItem>()).distinctBy { it.id }
    }
    val visibleRows = when (tab) {
        LibraryTab.Songs -> sortedSongs
        LibraryTab.Favorites -> likedSongs
        else -> sortedSongs
    }

    LibraryReferenceContent(
        activeTab = tab,
        sort = sort,
        gridView = gridView,
        likedCount = likedSongs.size,
        librarySongs = visibleRows,
        recentItems = if (showAllRecent) recentItems else recentItems.take(8),
        albums = albums,
        artists = artists,
        playlists = playlists,
        currentSong = currentSong,
        playbackState = playbackState,
        onTab = { tab = it },
        onSort = {
            sort = when (sort) {
                LibrarySort.RecentlyAdded -> LibrarySort.Title
                LibrarySort.Title -> LibrarySort.Artist
                LibrarySort.Artist -> LibrarySort.RecentlyAdded
            }
        },
        onGridView = { gridView = it },
        onEditPins = {},
        onSeeAllRecent = { showAllRecent = !showAllRecent },
        onPlayItem = { item ->
            when (item) {
                is SongItem -> player.playSong(item)
                is AlbumItem -> player.openAlbum(item.browseId)
                is PlaylistItem -> player.openPlaylist(item.id)
                is ArtistItem -> player.openArtist(item.id)
            }
        },
        onPlaySong = { song, index -> player.playSong(song, index) },
        onAdd = { song -> player.addToQueue(song) },
        onLike = { song -> player.toggleLike(song.id) },
    )
}

@Composable
private fun LibraryReferenceContent(
    activeTab: LibraryTab,
    sort: LibrarySort,
    gridView: Boolean,
    likedCount: Int,
    librarySongs: List<SongItem>,
    recentItems: List<YTItem>,
    albums: List<AlbumItem>,
    artists: List<LibraryArtist>,
    playlists: List<PlaylistItem>,
    currentSong: SongItem?,
    playbackState: PlaybackState,
    onTab: (LibraryTab) -> Unit,
    onSort: () -> Unit,
    onGridView: (Boolean) -> Unit,
    onEditPins: () -> Unit,
    onSeeAllRecent: () -> Unit,
    onPlayItem: (YTItem) -> Unit,
    onPlaySong: (SongItem, Int) -> Unit,
    onAdd: (SongItem) -> Unit,
    onLike: (SongItem) -> Unit,
) {
    val metrics = LocalHomeReferenceMetrics.current
    val scroll = rememberScrollState()
    val left = metrics.px(22f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(metrics.px(560f))
        ) {
            Text(
                "Library",
                color = TextPrimary,
                fontSize = 21.sp,
                lineHeight = 25.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.offset(x = left, y = metrics.px(22f)),
            )

            LibraryTabs(
                activeTab = activeTab,
                onTab = onTab,
                modifier = Modifier
                    .offset(x = left, y = metrics.px(54f))
                    .width(metrics.px(398f))
                    .height(metrics.px(30f)),
            )

            LibrarySortControl(
                sort = sort,
                onClick = onSort,
                modifier = Modifier
                    .offset(x = metrics.px(725f), y = metrics.px(54f))
                    .width(metrics.px(129f))
                    .height(metrics.px(30f)),
            )

            Row(
                modifier = Modifier
                    .offset(x = metrics.px(866f), y = metrics.px(54f))
                    .width(metrics.px(64f))
                    .height(metrics.px(30f)),
                horizontalArrangement = Arrangement.spacedBy(metrics.px(4f)),
            ) {
                ToggleIcon(Icons.Default.ViewList, !gridView, Modifier.weight(1f)) { onGridView(false) }
                ToggleIcon(Icons.Default.GridView, gridView, Modifier.weight(1f)) { onGridView(true) }
            }

            SectionRowHeader(
                title = "Pinned Collections",
                action = "Edit Pins",
                onAction = onEditPins,
                modifier = Modifier.offset(x = left, y = metrics.px(104f)).width(metrics.px(909f)),
            )

            Row(
                modifier = Modifier
                    .offset(x = left, y = metrics.px(128f))
                    .width(metrics.px(915f))
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(metrics.px(15f)),
            ) {
                val pinned = listOf(
                    PinnedCollection("Favorites", "$likedCount songs", Icons.Default.Favorite, librarySongs.firstOrNull()?.thumbnail),
                    PinnedCollection("Queue", "${librarySongs.size} songs", Icons.Default.QueueMusic, librarySongs.getOrNull(1)?.thumbnail),
                    PinnedCollection("Albums", "${albums.size} saved", Icons.Default.Album, albums.firstOrNull()?.thumbnail),
                    PinnedCollection("Artists", "${artists.size} followed", Icons.Default.Person, artists.firstOrNull()?.artwork),
                    PinnedCollection("Playlists", "${playlists.size} saved", Icons.Default.GraphicEq, playlists.firstOrNull()?.thumbnail),
                    PinnedCollection("Downloaded", "0 songs", Icons.Default.Download, null),
                )
                pinned.forEach { pinnedItem ->
                    PinnedCollectionCard(pinnedItem, Modifier.width(metrics.px(141f)).height(metrics.px(102f)))
                }
            }

            SectionRowHeader(
                title = "Recent Additions",
                action = "See all",
                onAction = onSeeAllRecent,
                modifier = Modifier.offset(x = left, y = metrics.px(248f)).width(metrics.px(909f)),
            )

            Row(
                modifier = Modifier
                    .offset(x = left, y = metrics.px(278f))
                    .width(metrics.px(913f))
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(metrics.px(14f)),
            ) {
                recentItems.take(12).forEach { item ->
                    RecentAdditionCard(
                        item = item,
                        onClick = { onPlayItem(item) },
                        modifier = Modifier.width(metrics.px(101f)).height(metrics.px(103f)),
                    )
                }
            }

            Text(
                "All ${activeTab.label}",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.offset(x = left, y = metrics.px(400f)),
            )

            LibraryTableHeader(
                modifier = Modifier
                    .offset(x = left, y = metrics.px(432f))
                    .width(metrics.px(909f))
                    .height(metrics.px(20f)),
            )

            Column(
                modifier = Modifier
                    .offset(x = left, y = metrics.px(458f))
                    .width(metrics.px(909f)),
                verticalArrangement = Arrangement.spacedBy(metrics.px(3f)),
            ) {
                if (librarySongs.isEmpty()) {
                    TruthfulEmptyLibraryRow(activeTab)
                } else {
                    librarySongs.take(8).forEachIndexed { index, song ->
                        val isActive = song.id == currentSong?.id
                        LibrarySongTableRow(
                            song = song,
                            index = index,
                            isActive = isActive,
                            isPlaying = isActive && playbackState == PlaybackState.PLAYING,
                            onPlay = { onPlaySong(song, index) },
                            onAdd = { onAdd(song) },
                            onLike = { onLike(song) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(metrics.px(38f)),
                        )
                    }
                }
            }
        }
    }
}

private data class PinnedCollection(
    val title: String,
    val meta: String,
    val icon: ImageVector,
    val artwork: String?,
)

private val LibraryTab.label: String
    get() = when (this) {
        LibraryTab.Songs -> "Songs"
        LibraryTab.Albums -> "Albums"
        LibraryTab.Artists -> "Artists"
        LibraryTab.Playlists -> "Playlists"
        LibraryTab.Downloads -> "Downloads"
        LibraryTab.Favorites -> "Favorites"
    }

@Composable
private fun LibraryTabs(activeTab: LibraryTab, onTab: (LibraryTab) -> Unit, modifier: Modifier = Modifier) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(metrics.px(6f)))
            .background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.88f))
            .border(1.dp, BorderLow.copy(alpha = 0.72f), RoundedCornerShape(metrics.px(6f)))
            .padding(metrics.px(2f)),
        horizontalArrangement = Arrangement.spacedBy(metrics.px(2f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LibraryTab.entries.forEach { tab ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(metrics.px(5f)))
                    .background(if (tab == activeTab) OmniReferenceColors.Accent else Color.Transparent)
                    .clickable { onTab(tab) },
                contentAlignment = Alignment.Center,
            ) {
                Text(tab.label, color = if (tab == activeTab) Color.White else TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            }
        }
    }
}

@Composable
private fun LibrarySortControl(sort: LibrarySort, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(metrics.px(6f)))
            .background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.86f))
            .border(1.dp, BorderLow.copy(alpha = 0.72f), RoundedCornerShape(metrics.px(6f)))
            .clickable(onClick = onClick)
            .padding(horizontal = metrics.px(10f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            when (sort) {
                LibrarySort.RecentlyAdded -> "Recently Added"
                LibrarySort.Title -> "Title"
                LibrarySort.Artist -> "Artist"
            },
            color = TextPrimary,
            fontSize = 9.sp,
            modifier = Modifier.weight(1f),
        )
        Icon(Icons.Default.ArrowDropDown, null, tint = TextSecondary, modifier = Modifier.size(metrics.px(12f)))
    }
}

@Composable
private fun ToggleIcon(icon: ImageVector, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(metrics.px(6f)))
            .background(if (active) OmniReferenceColors.Accent else OmniReferenceColors.SurfaceBase.copy(alpha = 0.86f))
            .border(1.dp, if (active) Color.Transparent else BorderLow.copy(alpha = 0.72f), RoundedCornerShape(metrics.px(6f)))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = if (active) Color.White else TextSecondary, modifier = Modifier.size(metrics.px(13f)))
    }
}

@Composable
private fun SectionRowHeader(title: String, action: String, onAction: () -> Unit, modifier: Modifier = Modifier) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(modifier = modifier.height(metrics.px(18f)), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = TextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        Text(
            action,
            color = IrisSoft,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .clip(RoundedCornerShape(metrics.px(4f)))
                .clickable(onClick = onAction)
                .padding(horizontal = metrics.px(5f), vertical = metrics.px(2f)),
        )
    }
}

@Composable
private fun PinnedCollectionCard(item: PinnedCollection, modifier: Modifier = Modifier) {
    val metrics = LocalHomeReferenceMetrics.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(metrics.px(8f)))
            .background(Surface1)
            .border(1.dp, BorderLow.copy(alpha = 0.72f), RoundedCornerShape(metrics.px(8f))),
    ) {
        if (item.artwork != null) {
            AsyncImage(
                model = item.artwork.toHighResThumbnail(),
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.radialGradient(listOf(OmniReferenceColors.Accent.copy(alpha = 0.5f), Surface1)))
            )
            Icon(item.icon, null, tint = IrisSoft, modifier = Modifier.align(Alignment.Center).size(metrics.px(32f)))
        }
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0f to Color.Transparent, 1f to Color(0xD8060919))))
        Icon(Icons.Default.PushPin, null, tint = Color.White.copy(alpha = 0.92f), modifier = Modifier.align(Alignment.BottomEnd).padding(metrics.px(10f)).size(metrics.px(12f)))
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(metrics.px(9f)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(metrics.px(25f))
                    .clip(RoundedCornerShape(metrics.px(5f)))
                    .background(OmniReferenceColors.SurfaceSelectedStrong.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(item.icon, null, tint = IrisSoft, modifier = Modifier.size(metrics.px(14f)))
            }
            Spacer(Modifier.width(metrics.px(7f)))
            Column {
                Text(item.title, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(item.meta, color = TextSecondary, fontSize = 7.5.sp, maxLines = 1)
            }
        }
    }
}

@Composable
private fun RecentAdditionCard(item: YTItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val metrics = LocalHomeReferenceMetrics.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(metrics.px(7f)))
            .background(Surface1)
            .border(1.dp, BorderLow.copy(alpha = 0.65f), RoundedCornerShape(metrics.px(7f)))
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = item.thumbnail?.toHighResThumbnail(),
            contentDescription = item.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0f to Color.Transparent, 1f to Color(0xE6070A1A))))
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = metrics.px(7f))
                .size(metrics.px(20f))
                .clip(CircleShape)
                .background(Color(0xFFDCDDF3)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.PlayArrow, null, tint = Color(0xFF101124), modifier = Modifier.size(metrics.px(13f)))
        }
        Column(Modifier.align(Alignment.BottomStart).padding(metrics.px(8f))) {
            Text(item.title, color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(item.librarySubtitle(), color = TextSecondary, fontSize = 7.3.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun LibraryTableHeader(modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text("#", color = TextMuted, fontSize = 8.sp, modifier = Modifier.width(LocalHomeReferenceMetrics.current.px(42f)))
        Text("TITLE", color = TextMuted, fontSize = 8.sp, modifier = Modifier.weight(1.7f))
        Text("ARTIST", color = TextMuted, fontSize = 8.sp, modifier = Modifier.weight(1.45f))
        Text("ALBUM", color = TextMuted, fontSize = 8.sp, modifier = Modifier.weight(1.65f))
        Text("DURATION", color = TextMuted, fontSize = 8.sp, modifier = Modifier.width(LocalHomeReferenceMetrics.current.px(74f)))
        Text("DATE ADDED", color = TextMuted, fontSize = 8.sp, modifier = Modifier.width(LocalHomeReferenceMetrics.current.px(96f)))
        Spacer(Modifier.width(LocalHomeReferenceMetrics.current.px(68f)))
    }
}

@Composable
private fun LibrarySongTableRow(
    song: SongItem,
    index: Int,
    isActive: Boolean,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onAdd: () -> Unit,
    onLike: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(metrics.px(6f)))
            .background(if (isActive) OmniReferenceColors.SurfaceSelected.copy(alpha = 0.7f) else Color.Transparent)
            .border(1.dp, if (isActive) OmniReferenceColors.Accent.copy(alpha = 0.42f) else Color.Transparent, RoundedCornerShape(metrics.px(6f)))
            .clickable(onClick = onPlay)
            .padding(horizontal = metrics.px(6f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isPlaying) {
            Icon(Icons.Default.GraphicEq, null, tint = IrisSoft, modifier = Modifier.width(metrics.px(36f)).size(metrics.px(13f)))
        } else {
            Text("${index + 1}", color = TextSecondary, fontSize = 9.sp, modifier = Modifier.width(metrics.px(36f)))
        }
        AsyncImage(
            model = song.thumbnail.toHighResThumbnail(),
            contentDescription = song.title,
            modifier = Modifier.size(metrics.px(28f)).clip(RoundedCornerShape(metrics.px(4f))),
            contentScale = ContentScale.Crop,
        )
        Spacer(Modifier.width(metrics.px(8f)))
        Text(song.title, color = if (isActive) IrisSoft else TextPrimary, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1.7f))
        Text(song.artists.joinToString(", ") { it.name }, color = TextSecondary, fontSize = 8.8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1.45f))
        Text(song.album?.name ?: "Single", color = TextSecondary, fontSize = 8.8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1.65f))
        Text(song.durationLabel(), color = TextSecondary, fontSize = 8.5.sp, modifier = Modifier.width(metrics.px(74f)))
        Text("Session", color = TextSecondary, fontSize = 8.5.sp, modifier = Modifier.width(metrics.px(96f)))
        Icon(Icons.Default.Favorite, null, tint = IrisSoft, modifier = Modifier.size(metrics.px(13f)).clickable(onClick = onLike))
        Spacer(Modifier.width(metrics.px(14f)))
        Icon(Icons.Default.Add, null, tint = TextSecondary, modifier = Modifier.size(metrics.px(14f)).clickable(onClick = onAdd))
        Spacer(Modifier.width(metrics.px(14f)))
        Icon(Icons.Default.MoreHoriz, null, tint = TextSecondary, modifier = Modifier.size(metrics.px(14f)))
    }
}

@Composable
private fun TruthfulEmptyLibraryRow(activeTab: LibraryTab) {
    val metrics = LocalHomeReferenceMetrics.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(metrics.px(80f))
            .clip(RoundedCornerShape(metrics.px(8f)))
            .background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.7f))
            .border(1.dp, BorderLow.copy(alpha = 0.55f), RoundedCornerShape(metrics.px(8f))),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            when (activeTab) {
                LibraryTab.Downloads -> "No downloaded songs found on this device."
                LibraryTab.Favorites -> "Like songs to add them to Favorites."
                else -> "Play or save songs to populate this library section."
            },
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private fun SongItem.durationLabel(): String {
    val value = duration ?: return ""
    return "${value / 60}:${(value % 60).toString().padStart(2, '0')}"
}

private fun YTItem.stableIdentity(): String = "${id.ifBlank { title }}:$title"

private fun YTItem.librarySubtitle(): String = when (this) {
    is SongItem -> artists.joinToString(", ") { it.name }
    is AlbumItem -> artists?.joinToString(", ") { it.name ?: "" }.orEmpty().ifBlank { "Album" }
    is ArtistItem -> "Artist"
    is PlaylistItem -> author?.name ?: songCountText ?: "Playlist"
}
