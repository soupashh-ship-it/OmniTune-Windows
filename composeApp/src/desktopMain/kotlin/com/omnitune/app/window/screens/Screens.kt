package com.omnitune.app.window.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import coil3.compose.AsyncImage
import com.omnitune.app.player.NavScreen
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.app.platform.PlaybackState
import com.omnitune.app.service.YouTubeService
import com.omnitune.app.window.*
import com.omnitune.app.window.components.*
import com.omnitune.innertube.models.*
import com.omnitune.innertube.pages.*
import com.omnitune.innertube.toHighResThumbnail
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private fun firstSong(items: List<YTItem>): SongItem? =
    items.firstOrNull { it is SongItem } as? SongItem

// ---------------------------------------------------------------------------
// HOME
// ---------------------------------------------------------------------------


@Composable
private fun SectionCarousel(title: String, items: List<YTItem>, player: PlayerViewModel, currentSong: SongItem?, playbackState: PlaybackState, liked: Set<String>) {
    Column {
        OmniSectionHeader(title, modifier = Modifier.padding(bottom = 12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
            items(items) { item -> CarouselCard(item, player, currentSong, playbackState, liked) }
        }
    }
}

@Composable
private fun CarouselCard(item: YTItem, player: PlayerViewModel, currentSong: SongItem?, playbackState: PlaybackState, liked: Set<String>) {
    val w = 170.dp
    when (item) {
        is SongItem -> CompactSongCard(item, player, currentSong, playbackState, Modifier.width(w))
        is AlbumItem -> OmniMediaCard(item.title, item.artists?.firstOrNull()?.name, item.thumbnail, Modifier.width(w), onPlay = { player.openAlbum(item.browseId) }, onClick = { player.openAlbum(item.browseId) })
        is ArtistItem -> OmniMediaCard(item.title, "Artist", item.thumbnail, Modifier.width(w), onPlay = { player.openArtist(item.id) }, onClick = { player.openArtist(item.id) })
        is PlaylistItem -> OmniMediaCard(item.title, item.author?.name, item.thumbnail, Modifier.width(w), onClick = {})
        else -> OmniMediaCard(item.title, "", item.thumbnail, Modifier.width(w))
    }
}

@Composable
private fun CompactSongCard(item: SongItem, player: PlayerViewModel, currentSong: SongItem?, playbackState: PlaybackState, modifier: Modifier = Modifier) {
    val isActive = item.id == currentSong?.id
    val isPlaying = isActive && playbackState == PlaybackState.PLAYING
    Column(modifier = modifier.clip(Shapes.medium).background(Surface1).border(1.dp, BorderLow, Shapes.medium).clickable { player.playSong(item, -1) }.padding(Spacing.small)) {
        Box(Modifier.fillMaxWidth().aspectRatio(1f).clip(Shapes.artworkMedium)) {
            AsyncImage(model = item.thumbnail.toHighResThumbnail(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            if (isActive) Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
                PlayingIndicatorBox(isActive = true, playWhenReady = isPlaying, color = IrisSoft)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(item.title, style = MaterialTheme.typography.titleSmall, color = if (isActive) IrisSoft else TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(item.artists.joinToString { it.name }, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun HomeShimmer() {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(40.dp), verticalArrangement = Arrangement.spacedBy(Spacing.section)) {
        item { OmniShimmerBlock(Modifier.width(280.dp).height(36.dp).clip(Shapes.small)) }
        item { OmniShimmerBlock(Modifier.fillMaxWidth().height(220.dp).clip(Shapes.large)) }
        items(3) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                items(6) { OmniShimmerBlock(Modifier.width(170.dp).height(210.dp).clip(Shapes.medium)) }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// BROWSE / RADIO
// ---------------------------------------------------------------------------

@Composable
fun BrowseView(player: PlayerViewModel) {
    var genres by remember { mutableStateOf<List<MoodAndGenres.Item>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val service = koinInject<YouTubeService>()
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { scope.launch { runCatching { service.moodAndGenres().flatMap { it.items } }.onSuccess { genres = it }.onFailure { error = it.message } } }

    Column(Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 24.dp)) {
        OmniSectionHeader("Browse", modifier = Modifier.padding(bottom = 16.dp))
        when {
            error != null -> OmniEmptyState("Couldn't load categories", error)
            genres == null -> HomeShimmer()
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                items(genres!!) { g ->
                    OmniSurface(modifier = Modifier.fillMaxWidth().clickable { g.endpoint.browseId?.let { player.openAlbum(it) } }.padding(vertical = 10.dp, horizontal = 16.dp)) {
                        Text(g.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun RadioView(player: PlayerViewModel) {
    var albums by remember { mutableStateOf<List<AlbumItem>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val service = koinInject<YouTubeService>()
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { scope.launch { runCatching { service.newReleaseAlbums() }.onSuccess { albums = it }.onFailure { error = it.message } } }

    Column(Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 24.dp)) {
        OmniSectionHeader("Radio", modifier = Modifier.padding(bottom = 16.dp))
        when {
            error != null -> OmniEmptyState("Radio unavailable", "Tune in later.")
            albums == null -> HomeShimmer()
            else -> LazyVerticalGridCells(albums!!, player)
        }
    }
}

@Composable
private fun LazyVerticalGridCells(items: List<AlbumItem>, player: PlayerViewModel) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        items(items.chunked(4)) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small), modifier = Modifier.fillMaxWidth()) {
                row.forEach { a ->
                    OmniMediaCard(a.title, a.artists?.firstOrNull()?.name, a.thumbnail, Modifier.weight(1f).padding(4.dp), onClick = { player.openAlbum(a.browseId) })
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// LIBRARY
// ---------------------------------------------------------------------------



// ---------------------------------------------------------------------------
// DOWNLOADS
// ---------------------------------------------------------------------------

@Composable
fun DownloadsView(player: PlayerViewModel) {
    Column(Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 24.dp)) {
        OmniSectionHeader("Downloads & Offline", modifier = Modifier.padding(bottom = 16.dp))
        OmniSurface(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), color = Surface1) {
            Column(Modifier.padding(20.dp)) {
                Text("Offline storage", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                Text("Downloaded songs: 0 · No active downloads", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
            }
        }
        Spacer(Modifier.height(16.dp))
        OmniEmptyState("Nothing downloaded yet", "Downloaded tracks and playlists will appear here. Audio downloads require local storage and are not yet enabled.")
    }
}
