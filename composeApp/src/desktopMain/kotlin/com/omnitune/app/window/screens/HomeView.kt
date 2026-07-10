package com.omnitune.app.window.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
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

@Composable
fun HomeView(player: PlayerViewModel) {
    var home by remember { mutableStateOf<HomePage?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val service = koinInject<YouTubeService>()
    val scope = rememberCoroutineScope()
    val currentSong by player.currentSong.collectAsState()
    val playbackState by player.playbackState.collectAsState()
    val liked by player.likedSongs.collectAsState()

    LaunchedEffect(Unit) {
        scope.launch {
            runCatching { service.home() }.onSuccess { home = it }.onFailure { error = it.message }
        }
    }

    Box(Modifier.fillMaxSize()) {
        if (home == null && error == null) {
            HomeShimmer()
        } else if (error != null) {
            OmniEmptyState("Couldn't load Home", error ?: "Unknown error")
        } else {
            HomeContent(player, home!!, currentSong, playbackState, liked)
        }
    }
}

@Composable
private fun HomeContent(player: PlayerViewModel, home: HomePage, currentSong: SongItem?, playbackState: PlaybackState, liked: Set<String>) {
    val featured = home.sections.firstNotNullOfOrNull { s -> s.items.firstOrNull { it is AlbumItem } as? AlbumItem }
    
    // We try to map real YouTube Music sections to the mockup sections.
    val quickPicks = home.sections.firstOrNull { it.title.contains("Quick picks", ignoreCase = true) }
    val newReleases = home.sections.firstOrNull { it.title.contains("New releases", ignoreCase = true) }
    val trending = home.sections.firstOrNull { it.title.contains("Trending", ignoreCase = true) }
        ?: home.sections.firstOrNull { it.title.contains("Top", ignoreCase = true) }
    val mixedForYou = home.sections.firstOrNull { it.title.contains("Mixed for you", ignoreCase = true) || it.title.contains("Recommended", ignoreCase = true) }
    val history = home.sections.firstOrNull { it.title.contains("Listen again", ignoreCase = true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(start = 32.dp, end = 24.dp, top = 32.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        item {
            val name = "Listener"
            Text("Good ${greeting()}, $name", style = MaterialTheme.typography.displayMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth().height(260.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                if (featured != null) {
                    FeaturedHero(featured, player, modifier = Modifier.weight(1.8f))
                }
                if (history != null && history.items.isNotEmpty()) {
                    ContinueListeningPanel(history, player, currentSong, playbackState, modifier = Modifier.weight(1f))
                }
            }
        }

        if (quickPicks != null && quickPicks.items.isNotEmpty()) {
            item {
                OmniSectionHeader(quickPicks.title, actionLabel = "See all", onAction = {})
                Spacer(Modifier.height(16.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(quickPicks.items.take(8)) { item ->
                        QuickPickCard(item, player)
                    }
                }
            }
        }

        if (mixedForYou != null && mixedForYou.items.isNotEmpty()) {
            item {
                OmniSectionHeader("Made for You", actionLabel = "See all", onAction = {})
                Spacer(Modifier.height(16.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(mixedForYou.items.take(6)) { item ->
                        MadeForYouCard(item, player)
                    }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                if (trending != null && trending.items.isNotEmpty()) {
                    val songs = trending.items.filterIsInstance<SongItem>().take(5)
                    if (songs.isNotEmpty()) {
                        Column(modifier = Modifier.weight(1.5f)) {
                            OmniSectionHeader(trending.title, actionLabel = "See all", onAction = {})
                            Spacer(Modifier.height(16.dp))
                            songs.forEachIndexed { index, song ->
                                TrendingRow(song, index + 1, currentSong?.id == song.id, playbackState, player)
                            }
                        }
                    }
                }
                
                if (newReleases != null && newReleases.items.isNotEmpty()) {
                    Column(modifier = Modifier.weight(1f)) {
                        OmniSectionHeader(newReleases.title, actionLabel = "See all", onAction = {})
                        Spacer(Modifier.height(16.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(newReleases.items.filterIsInstance<AlbumItem>().take(4)) { album ->
                                NewReleaseCard(album, player)
                            }
                        }
                    }
                }
            }
        }
        
        // Fallback for sections that didn't match the specific mapping
        val renderedTitles = listOfNotNull(quickPicks?.title, newReleases?.title, trending?.title, mixedForYou?.title, history?.title)
        val remaining = home.sections.filter { it.title !in renderedTitles }
        items(remaining) { section ->
            if (section.items.isEmpty()) return@items
            OmniSectionHeader(section.title, actionLabel = "See all", onAction = {})
            Spacer(Modifier.height(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(section.items) { item ->
                    GenericCarouselCard(item, player)
                }
            }
        }
    }
}

@Composable
private fun greeting(): String {
    val h = java.time.LocalTime.now().hour
    return when { h < 12 -> "morning"; h < 18 -> "afternoon"; else -> "evening" }
}

@Composable
private fun FeaturedHero(album: AlbumItem, player: PlayerViewModel, modifier: Modifier = Modifier) {
    OmniSurface(
        shape = Shapes.large,
        color = BgDeep,
        elevation = 0.dp,
        border = false,
        modifier = modifier.fillMaxHeight(),
    ) {
        Box(Modifier.fillMaxSize().background(OmniGradients.heroAmbient())) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f).fillMaxHeight()) {
                    AsyncImage(model = album.thumbnail.toHighResThumbnail(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(colors = listOf(BgDeep.copy(alpha = 0.95f), BgDeep.copy(alpha = 0.6f), Color.Transparent))))
                    Column(Modifier.align(Alignment.BottomStart).padding(24.dp)) {
                        Text("FEATURED PLAYLIST", style = MaterialTheme.typography.labelMedium, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(album.title, style = MaterialTheme.typography.displayMedium, color = TextPrimary, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        album.artists?.firstOrNull()?.let { Text(it.name, style = MaterialTheme.typography.titleMedium, color = TextSecondary) }
                        Spacer(Modifier.height(20.dp))
                        Row {
                            OmniPrimaryButton("Play Now", onClick = { player.playAlbum(album.browseId) }, icon = Icons.Filled.PlayArrow)
                            Spacer(Modifier.width(12.dp))
                            OmniSecondaryButton("More", onClick = { player.openAlbum(album.browseId) })
                        }
                    }
                }
                
                // Companion Rail
                Column(Modifier.width(280.dp).fillMaxHeight().background(Surface1.copy(alpha = 0.5f)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tracks", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                    Spacer(Modifier.height(4.dp))
                    for (i in 1..4) {
                        OmniShimmerBlock(Modifier.fillMaxWidth().height(40.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ContinueListeningPanel(section: com.omnitune.innertube.pages.HomePage.Section, player: PlayerViewModel, currentSong: SongItem?, playbackState: PlaybackState, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxHeight()) {
        OmniSectionHeader("Continue Listening", actionLabel = "See all", onAction = {})
        Spacer(Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            section.items.take(4).forEach { item ->
                val title = item.title
                val subtitle = when(item) {
                    is SongItem -> item.artists.firstOrNull()?.name
                    is AlbumItem -> item.artists?.firstOrNull()?.name
                    is PlaylistItem -> item.author?.name
                    else -> null
                }
                val art = item.thumbnail
                val isPlaying = currentSong?.id == item.id && playbackState == PlaybackState.PLAYING
                
                OmniSurface(
                    shape = Shapes.small,
                    color = if (currentSong?.id == item.id) com.omnitune.app.window.SurfaceSelected else Surface1,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    onClick = {
                        if (item is SongItem) player.playSong(item)
                        else if (item is AlbumItem) player.openAlbum(item.browseId)
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 12.dp)) {
                        AsyncImage(model = art, contentDescription = null, modifier = Modifier.size(56.dp).clip(Shapes.small), contentScale = ContentScale.Crop)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Filled.PlayArrow, null, tint = TextSecondary, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickPickCard(item: YTItem, player: PlayerViewModel) {
    val title = item.title
    val subtitle = when(item) {
        is SongItem -> item.artists.firstOrNull()?.name
        is AlbumItem -> item.artists?.firstOrNull()?.name
        is PlaylistItem -> item.author?.name
        else -> null
    }
    Column(modifier = Modifier.width(150.dp).clickable { 
        if (item is SongItem) player.playSong(item)
        else if (item is AlbumItem) player.openAlbum(item.browseId)
    }) {
        AsyncImage(model = item.thumbnail?.toHighResThumbnail(), contentDescription = null, modifier = Modifier.size(150.dp).clip(Shapes.small), contentScale = ContentScale.Crop)
        Spacer(Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun MadeForYouCard(item: YTItem, player: PlayerViewModel) {
    OmniMediaCard(
        title = item.title,
        subtitle = (item as? PlaylistItem)?.author?.name ?: (item as? AlbumItem)?.artists?.firstOrNull()?.name,
        artworkUrl = item.thumbnail,
        modifier = Modifier.width(180.dp).height(220.dp),
        onClick = { if (item is AlbumItem) player.openAlbum(item.browseId) else if (item is PlaylistItem) player.playPlaylist(item.id) }
    )
}

@Composable
private fun TrendingRow(item: SongItem, rank: Int, isActive: Boolean, playbackState: PlaybackState, player: PlayerViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp).clip(Shapes.small).background(if (isActive) com.omnitune.app.window.SurfaceSelected else Color.Transparent).clickable { player.playSong(item) }.padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(rank.toString(), style = MaterialTheme.typography.titleMedium, color = TextSecondary, modifier = Modifier.width(24.dp))
        Spacer(Modifier.width(8.dp))
        AsyncImage(model = item.thumbnail, contentDescription = null, modifier = Modifier.size(40.dp).clip(Shapes.tiny), contentScale = ContentScale.Crop)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.album?.name ?: item.artists.firstOrNull()?.name ?: "", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(12.dp))
        Text(item.duration?.let { String.format("%d:%02d", it / 60, it % 60) } ?: "", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

@Composable
private fun NewReleaseCard(item: AlbumItem, player: PlayerViewModel) {
    OmniMediaCard(
        title = item.title,
        subtitle = item.artists?.firstOrNull()?.name,
        artworkUrl = item.thumbnail,
        modifier = Modifier.width(140.dp).height(190.dp),
        onClick = { player.openAlbum(item.browseId) }
    )
}

@Composable
private fun GenericCarouselCard(item: YTItem, player: PlayerViewModel) {
    OmniMediaCard(
        title = item.title,
        subtitle = (item as? SongItem)?.artists?.firstOrNull()?.name ?: (item as? AlbumItem)?.artists?.firstOrNull()?.name,
        artworkUrl = item.thumbnail,
        modifier = Modifier.width(150.dp),
        onClick = {
            if (item is SongItem) player.playSong(item)
            else if (item is AlbumItem) player.openAlbum(item.browseId)
        }
    )
}

@Composable
private fun HomeShimmer() {
    Column(modifier = Modifier.fillMaxSize().padding(start = 32.dp, end = 24.dp, top = 32.dp, bottom = 40.dp), verticalArrangement = Arrangement.spacedBy(32.dp)) {
        OmniShimmerBlock(Modifier.width(300.dp).height(40.dp))
        Row(modifier = Modifier.fillMaxWidth().height(260.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            OmniShimmerBlock(Modifier.weight(1.8f).fillMaxHeight())
            OmniShimmerBlock(Modifier.weight(1f).fillMaxHeight())
        }
        Column {
            OmniShimmerBlock(Modifier.width(150.dp).height(24.dp))
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                for(i in 1..6) OmniShimmerBlock(Modifier.size(150.dp))
            }
        }
    }
}
