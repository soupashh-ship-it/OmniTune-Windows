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

@Composable
internal fun LikedAddToPlaylistSheet(
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
internal fun LikedSongsHero(
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
internal fun LikedHeartArtwork(modifier: Modifier = Modifier) {
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

@Composable internal fun LikedSongsEmptyState(onSearch: () -> Unit, onBrowse: () -> Unit) {
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

@Composable
internal fun LikedSongsRail(records: List<LikedSongRecord>, recommendations: List<PlaylistItem>, totalDuration: Int, uniqueArtists: Int, downloaded: Int, onOpenPlaylist: (String) -> Unit, modifier: Modifier = Modifier) {
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

@Composable internal fun LikedRailCard(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Column(modifier.clip(RoundedCornerShape(metrics.px(10f))).background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.78f)).border(1.dp, BorderLow.copy(alpha = 0.62f), RoundedCornerShape(metrics.px(10f))).padding(metrics.px(12f))) {
        Text(title, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(metrics.px(8f)))
        content()
    }
}

@Composable internal fun LikedStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = IrisSoft, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(label, color = TextSecondary, fontSize = 7.sp, maxLines = 1)
    }
}