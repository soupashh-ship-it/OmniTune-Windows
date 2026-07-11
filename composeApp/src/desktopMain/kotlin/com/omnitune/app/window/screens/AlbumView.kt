package com.omnitune.app.window.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.app.platform.PlaybackState
import com.omnitune.app.service.YouTubeService
import com.omnitune.app.window.*
import com.omnitune.app.window.components.*
import com.omnitune.innertube.models.AlbumItem
import com.omnitune.innertube.models.Artist
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.pages.AlbumPage
import com.omnitune.innertube.toHighResThumbnail
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun AlbumView(player: PlayerViewModel) {
    val albumId by player.currentAlbumId.collectAsState()
    var page by remember(albumId) { mutableStateOf<AlbumPage?>(null) }
    var error by remember(albumId) { mutableStateOf<String?>(null) }
    val service = koinInject<YouTubeService>()
    val scope = rememberCoroutineScope()

    val currentSong by player.currentSong.collectAsState()
    val playbackState by player.playbackState.collectAsState()
    val liked by player.likedSongs.collectAsState()

    LaunchedEffect(albumId) {
        if (albumId != null) {
            scope.launch {
                runCatching { service.album(albumId!!) }.onSuccess { page = it }.onFailure { error = it.message }
            }
        }
    }

    if (error != null) {
        OmniEmptyState("Couldn't load Album", error ?: "Unknown error")
        return
    }

    if (page == null) {
        AlbumDetailShimmer()
        return
    }

    val p = page!!
    val totalDurationSec = p.songs.sumOf { it.duration ?: 0 }
    val totalDurationStr = if (totalDurationSec > 0) "${totalDurationSec / 60} min ${totalDurationSec % 60} sec" else null
    
    // Derive featured artists
    val mainArtistIds = p.album.artists?.mapNotNull { it.id }?.toSet() ?: emptySet()
    val featuredArtists = p.songs.flatMap { it.artists }.filter { it.id != null && it.id !in mainArtistIds }.distinctBy { it.id }.take(6)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(40.dp),
    ) {
        item {
            AlbumHero(p.album, p.songs.size, totalDurationStr, player)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                // Left Column: Tracks
                Column(modifier = Modifier.weight(2.5f)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text("#", color = TextMuted, style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(32.dp))
                        Text("TITLE", color = TextMuted, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(2f))
                        Text("ARTIST", color = TextMuted, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1.5f))
                        Text("⏱", color = TextMuted, style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(48.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    p.songs.forEachIndexed { index, song ->
                        OmniSongRow(
                            item = song,
                            isActive = song.id == currentSong?.id,
                            isPlaying = song.id == currentSong?.id && playbackState == PlaybackState.PLAYING,
                            onClick = { player.playSong(song, index) },
                            onPlayNext = { player.playNext(song) },
                            onAddToQueue = { player.addToQueue(song) },
                            onLike = { player.toggleLike(song.id) },
                            showIndex = true,
                            index = index
                        )
                    }
                }

                // Right Column: Featured Artists & Other Versions
                Column(modifier = Modifier.width(280.dp), verticalArrangement = Arrangement.spacedBy(32.dp)) {
                    if (featuredArtists.isNotEmpty()) {
                        Column {
                            OmniSectionHeader("Featured Artists")
                            Spacer(Modifier.height(16.dp))
                            featuredArtists.forEach { artist ->
                                FeaturedArtistRow(artist, player)
                            }
                        }
                    }
                    
                    Column {
                        OmniSectionHeader("Credits")
                        Spacer(Modifier.height(16.dp))
                        OmniSurface(shape = Shapes.medium, color = Surface1, modifier = Modifier.fillMaxWidth().height(80.dp)) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Credit information is currently unavailable.", style = MaterialTheme.typography.bodySmall, color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(16.dp))
                            }
                        }
                    }

                    if (p.otherVersions.isNotEmpty()) {
                        Column {
                            OmniSectionHeader("Other Versions")
                            Spacer(Modifier.height(16.dp))
                            p.otherVersions.take(3).forEach { version ->
                                OtherVersionCard(version, player)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlbumHero(album: AlbumItem, songCount: Int, totalDurationStr: String?, player: PlayerViewModel) {
    Row(modifier = Modifier.fillMaxWidth().height(260.dp), verticalAlignment = Alignment.Bottom) {
        AsyncImage(
            model = album.thumbnail.toHighResThumbnail(),
            contentDescription = null,
            modifier = Modifier.size(260.dp).clip(Shapes.small),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(32.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("ALBUM", style = MaterialTheme.typography.labelLarge, color = TextSecondary, letterSpacing = 1.sp)
                if (album.explicit) {
                    OmniBadge("E")
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(album.title, style = MaterialTheme.typography.displayLarge, color = TextPrimary, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                album.artists?.firstOrNull()?.let { 
                    Text(it.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { it.id?.let { id -> player.openArtist(id) } })
                    Text(" • ", color = TextMuted)
                }
                album.year?.let { 
                    Text(it.toString(), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Text(" • ", color = TextMuted)
                }
                Text("$songCount songs", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                if (totalDurationStr != null) {
                    Text(" • ", color = TextMuted)
                    Text(totalDurationStr, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            }
            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OmniPrimaryButton(text = "Play", onClick = { player.playAlbum(album.browseId) }, icon = Icons.Filled.PlayArrow, modifier = Modifier.width(120.dp).height(44.dp))
                OmniIconButton(icon = Icons.Default.Add, contentDescription = "Add", onClick = { })
                OmniIconButton(icon = Icons.Default.FavoriteBorder, contentDescription = "Favorite", onClick = { })
                OmniIconButton(icon = Icons.Default.MoreHoriz, contentDescription = "More", onClick = { })
            }
        }
    }
}

@Composable
private fun FeaturedArtistRow(artist: Artist, player: PlayerViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp).clip(Shapes.small).clickable { artist.id?.let { player.openArtist(it) } }.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Surface2), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Person, null, tint = TextMuted)
        }
        Spacer(Modifier.width(12.dp))
        Text(artist.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun OtherVersionCard(album: AlbumItem, player: PlayerViewModel) {
    OmniSurface(
        modifier = Modifier.fillMaxWidth().height(80.dp).padding(bottom = 8.dp),
        shape = Shapes.small,
        color = Surface1,
        onClick = { player.openAlbum(album.browseId) }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize().padding(8.dp)) {
            AsyncImage(model = album.thumbnail.toHighResThumbnail(), contentDescription = null, modifier = Modifier.size(64.dp).clip(Shapes.small), contentScale = ContentScale.Crop)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(album.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(album.year?.toString() ?: "Album", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun AlbumDetailShimmer() {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 32.dp), verticalArrangement = Arrangement.spacedBy(40.dp)) {
        Row(modifier = Modifier.fillMaxWidth().height(260.dp), verticalAlignment = Alignment.Bottom) {
            OmniShimmerBlock(Modifier.size(260.dp).clip(Shapes.small))
            Spacer(Modifier.width(32.dp))
            Column(modifier = Modifier.weight(1f)) {
                OmniShimmerBlock(Modifier.width(60.dp).height(16.dp))
                Spacer(Modifier.height(8.dp))
                OmniShimmerBlock(Modifier.width(400.dp).height(48.dp))
                Spacer(Modifier.height(12.dp))
                OmniShimmerBlock(Modifier.width(250.dp).height(20.dp))
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OmniShimmerBlock(Modifier.width(120.dp).height(44.dp).clip(Shapes.pill))
                    for (i in 1..3) OmniShimmerBlock(Modifier.size(44.dp).clip(androidx.compose.foundation.shape.CircleShape))
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            Column(modifier = Modifier.weight(2.5f)) {
                for (i in 1..8) {
                    OmniShimmerBlock(Modifier.fillMaxWidth().height(56.dp))
                    Spacer(Modifier.height(8.dp))
                }
            }
            Column(modifier = Modifier.width(280.dp)) {
                OmniShimmerBlock(Modifier.width(150.dp).height(24.dp))
                Spacer(Modifier.height(16.dp))
                for (i in 1..4) {
                    OmniShimmerBlock(Modifier.fillMaxWidth().height(56.dp))
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
