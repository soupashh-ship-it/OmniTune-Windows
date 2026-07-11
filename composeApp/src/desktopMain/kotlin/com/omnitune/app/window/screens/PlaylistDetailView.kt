package com.omnitune.app.window.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.omnitune.innertube.models.PlaylistItem
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.models.YTItem
import com.omnitune.innertube.pages.PlaylistPage
import com.omnitune.innertube.toHighResThumbnail
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun PlaylistDetailView(player: PlayerViewModel) {
    val playlistId by player.currentPlaylistId.collectAsState()
    var page by remember(playlistId) { mutableStateOf<PlaylistPage?>(null) }
    var error by remember(playlistId) { mutableStateOf<String?>(null) }
    val service = koinInject<YouTubeService>()
    val scope = rememberCoroutineScope()

    val currentSong by player.currentSong.collectAsState()
    val playbackState by player.playbackState.collectAsState()
    val liked by player.likedSongs.collectAsState()
    val discoveryNew by player.discoveryNew.collectAsState() // Honest fallback for Related content

    LaunchedEffect(playlistId) {
        if (playlistId != null) {
            scope.launch {
                runCatching { service.playlist(playlistId!!) }.onSuccess { page = it }.onFailure { error = it.message }
            }
        }
    }

    if (error != null) {
        OmniEmptyState("Couldn't load Playlist", error ?: "Unknown error")
        return
    }

    if (page == null) {
        PlaylistDetailShimmer()
        return
    }

    val playlist = page!!.playlist
    val songs = page!!.songs
    val totalDurationSec = songs.sumOf { it.duration ?: 0 }
    val totalDurationStr = if (totalDurationSec > 0) "${totalDurationSec / 60} min ${totalDurationSec % 60} sec" else null
    val relatedPlaylists = discoveryNew.filterIsInstance<PlaylistItem>().take(6)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        item {
            PlaylistHero(playlist, totalDurationStr, player)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                // Left column for Tracks
                Column(modifier = Modifier.weight(2f)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text("#", color = TextMuted, style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(32.dp))
                        Text("TITLE", color = TextMuted, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(2f))
                        Text("ARTIST", color = TextMuted, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1.5f))
                        Text("ALBUM", color = TextMuted, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1.5f))
                        Text("⏱", color = TextMuted, style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(48.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    songs.forEachIndexed { index, song ->
                        OmniSongRow(
                            item = song,
                            isActive = song.id == currentSong?.id,
                            isPlaying = song.id == currentSong?.id && playbackState == PlaybackState.PLAYING,
                            onClick = { player.playSong(song, index) }, // Actually play from the playlist context
                            onPlayNext = { player.playNext(song) },
                            onAddToQueue = { player.addToQueue(song) },
                            onLike = { player.toggleLike(song.id) }
                        )
                    }
                }

                // Right column for "More like this"
                if (relatedPlaylists.isNotEmpty()) {
                    Column(modifier = Modifier.width(280.dp)) {
                        Text("More like this", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        relatedPlaylists.forEach { related ->
                            RelatedPlaylistCard(related, player)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistHero(playlist: PlaylistItem, totalDurationStr: String?, player: PlayerViewModel) {
    Row(modifier = Modifier.fillMaxWidth().height(260.dp), verticalAlignment = Alignment.Bottom) {
        AsyncImage(
            model = playlist.thumbnail?.toHighResThumbnail(),
            contentDescription = null,
            modifier = Modifier.size(260.dp).clip(Shapes.small),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(32.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("PLAYLIST", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            Text(playlist.title, style = MaterialTheme.typography.displayLarge, color = TextPrimary, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = playlist.author?.name ?: "OmniTune", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text(" • ", color = TextMuted)
                Text(playlist.songCountText ?: "", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                if (totalDurationStr != null) {
                    Text(" • ", color = TextMuted)
                    Text(totalDurationStr, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OmniPrimaryButton(
                    text = "Play",
                    onClick = { player.playPlaylist(playlist.id) }, // START PLAYBACK
                    icon = Icons.Filled.PlayArrow,
                    modifier = Modifier.width(120.dp).height(44.dp)
                )
                OmniSecondaryButton(
                    text = "Shuffle",
                    onClick = { player.playPlaylist(playlist.id); player.toggleShuffle() },
                    modifier = Modifier.width(130.dp).height(44.dp)
                )
                OmniIconButton(icon = Icons.Default.FavoriteBorder, contentDescription = "Favorite", onClick = { })
                OmniIconButton(icon = Icons.Default.MoreHoriz, contentDescription = "More", onClick = { })
            }
        }
    }
}

@Composable
private fun RelatedPlaylistCard(playlist: PlaylistItem, player: PlayerViewModel) {
    OmniSurface(
        modifier = Modifier.fillMaxWidth().height(72.dp).padding(bottom = 8.dp),
        shape = Shapes.small,
        color = Surface1,
        onClick = { player.openPlaylist(playlist.id) } // OPEN DETAIL
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize().padding(8.dp)) {
            AsyncImage(model = playlist.thumbnail?.toHighResThumbnail(), contentDescription = null, modifier = Modifier.size(56.dp).clip(Shapes.small), contentScale = ContentScale.Crop)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(playlist.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(playlist.songCountText ?: playlist.author?.name ?: "", style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            OmniIconButton(icon = Icons.Default.PlayArrow, contentDescription = "Play", size = 36.dp, iconSize = 16.dp, background = Surface2, onClick = { player.playPlaylist(playlist.id) }) // START PLAYBACK
        }
    }
}

@Composable
private fun PlaylistDetailShimmer() {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 32.dp), verticalArrangement = Arrangement.spacedBy(32.dp)) {
        Row(modifier = Modifier.fillMaxWidth().height(260.dp), verticalAlignment = Alignment.Bottom) {
            OmniShimmerBlock(Modifier.size(260.dp).clip(Shapes.small))
            Spacer(Modifier.width(32.dp))
            Column(modifier = Modifier.weight(1f)) {
                OmniShimmerBlock(Modifier.width(80.dp).height(16.dp))
                Spacer(Modifier.height(12.dp))
                OmniShimmerBlock(Modifier.fillMaxWidth().height(48.dp))
                Spacer(Modifier.height(12.dp))
                OmniShimmerBlock(Modifier.width(200.dp).height(20.dp))
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OmniShimmerBlock(Modifier.width(120.dp).height(44.dp).clip(Shapes.pill))
                    OmniShimmerBlock(Modifier.width(130.dp).height(44.dp).clip(Shapes.pill))
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            Column(modifier = Modifier.weight(2f)) {
                for (i in 1..8) {
                    OmniShimmerBlock(Modifier.fillMaxWidth().height(56.dp))
                    Spacer(Modifier.height(8.dp))
                }
            }
            Column(modifier = Modifier.width(280.dp)) {
                for (i in 1..6) {
                    OmniShimmerBlock(Modifier.fillMaxWidth().height(72.dp))
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
