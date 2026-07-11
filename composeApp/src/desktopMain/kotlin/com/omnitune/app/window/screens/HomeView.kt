package com.omnitune.app.window.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.hoverable
import com.omnitune.app.player.NavScreen
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

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(40.dp)
    ) {
        if (featured != null) {
            item {
                HomeHero(featured, player)
            }
        }
        
        items(home.sections) { section ->
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Text(section.title, style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text("See all", style = MaterialTheme.typography.labelMedium, color = IrisSoft, modifier = Modifier.clickable { })
                }
                Spacer(Modifier.height(16.dp))
                
                if (section.items.firstOrNull() is SongItem) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        section.items.take(4).filterIsInstance<SongItem>().forEachIndexed { i, song ->
                            val isCurrent = song.id == currentSong?.id
                            val isPlaying = isCurrent && playbackState == PlaybackState.PLAYING
                            OmniSongRow(
                                item = song,
                                isActive = isCurrent,
                                isPlaying = isPlaying,
                                onClick = { player.playSong(song, i) },
                                onPlayNext = { player.playNext(song) },
                                onAddToQueue = { player.addToQueue(song) },
                                onLike = { player.toggleLike(song.id) },
                                showIndex = false,
                                index = i
                            )
                        }
                    }
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(section.items) { item ->
                            CarouselCard(item, player, currentSong, playbackState, liked)
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun HomeHero(album: AlbumItem, player: PlayerViewModel) {
    OmniSurface(modifier = Modifier.fillMaxWidth().height(220.dp), color = Surface1, border = true) {
        Row(modifier = Modifier.fillMaxSize().padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(172.dp).clip(Shapes.artworkLarge).background(BgCard)) {
                AsyncImage(
                    model = album.thumbnail.toHighResThumbnail(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(Modifier.width(32.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text("Featured Album", style = MaterialTheme.typography.labelLarge, color = IrisSoft, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(album.title, style = MaterialTheme.typography.displaySmall, color = TextPrimary, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val artists = album.artists?.joinToString(", ") { it.name ?: "" } ?: ""
                if (!artists.isNullOrEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(artists ?: "", style = MaterialTheme.typography.titleMedium, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(24.dp))
                Row {
                    Box(
                        modifier = Modifier
                            .clip(Shapes.pill)
                            .background(OmniGradients.primaryAction)
                            .clickable { player.openAlbum(album.browseId) }
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Play Album", style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CarouselCard(item: YTItem, player: PlayerViewModel, currentSong: SongItem?, playbackState: PlaybackState, liked: Set<String>) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Column(
        modifier = Modifier
            .width(160.dp)
            .clip(Shapes.medium)
            .background(if (isHovered) BgCardHover else Color.Transparent)
            .clickable(interactionSource = interactionSource, indication = null) {
                when (item) {
                    is AlbumItem -> player.openAlbum(item.browseId)
                    is PlaylistItem -> player.playPlaylist(item.id)
                    is ArtistItem -> player.openArtist(item.id)
                    is SongItem -> player.playSong(item, 0)
                }
            }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(144.dp)
                .clip(if (item is ArtistItem) androidx.compose.foundation.shape.CircleShape else Shapes.artworkMedium)
                .background(Surface2)
        ) {
            AsyncImage(
                model = item.thumbnail?.toHighResThumbnail(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            if (item is SongItem && item.id == currentSong?.id) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                    PlayingIndicatorBox(isActive = true, playWhenReady = playbackState == PlaybackState.PLAYING, color = Iris)
                }
            } else if (isHovered && item is SongItem) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(48.dp))
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            item.title,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        val sub = when (item) {
            is SongItem -> item.artists?.joinToString(", ") { it.name }
            is AlbumItem -> "Album • " + item.artists?.joinToString(", ") { it.name ?: "" } ?: ""
            is PlaylistItem -> "Playlist"
            is ArtistItem -> "Artist"
            else -> ""
        }
        if (!sub.isNullOrEmpty()) {
            Spacer(Modifier.height(2.dp))
            Text(
                sub,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HomeShimmer() {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(40.dp)) {
        Box(modifier = Modifier.fillMaxWidth().height(220.dp).clip(Shapes.large).background(Surface2))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(5) {
                Column {
                    Box(modifier = Modifier.size(160.dp).clip(Shapes.artworkMedium).background(Surface2))
                    Spacer(Modifier.height(12.dp))
                    Box(modifier = Modifier.width(120.dp).height(16.dp).clip(Shapes.small).background(Surface2))
                    Spacer(Modifier.height(4.dp))
                    Box(modifier = Modifier.width(80.dp).height(12.dp).clip(Shapes.small).background(Surface2))
                }
            }
        }
    }
}

