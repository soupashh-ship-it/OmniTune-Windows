package com.omnitune.app.window.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.app.platform.PlaybackState
import com.omnitune.app.window.*
import com.omnitune.app.window.components.*
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.toHighResThumbnail

@Composable
fun LibraryView(player: PlayerViewModel) {
    var tab by remember { mutableStateOf(0) }
    val tabs = listOf("Songs", "Albums", "Artists", "Playlists", "Downloads", "Favorites")
    val liked by player.likedSongs.collectAsState()
    val queue by player.queue.collectAsState()
    val currentSong by player.currentSong.collectAsState()
    val playbackState by player.playbackState.collectAsState()
    
    val likedSongs = remember(liked, queue) { queue.filter { liked.contains(it.id) }.distinctBy { it.id } }
    val recentAdditions = remember(likedSongs) { likedSongs.reversed().take(10) }
    val pinned = remember(likedSongs) { likedSongs.take(4) } // Honest fallback for pinned collections

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 44.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        // TOP HEADER
        item {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Library", style = MaterialTheme.typography.displaySmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Sort Control
                    Surface(shape = Shapes.small, color = Surface2, border = BorderStroke(1.dp, BorderLow)) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Recently Added", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowDropDown, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    // View Toggle
                    OmniIconButton(icon = Icons.Default.GridView, contentDescription = "Grid View", onClick = {}, size = 36.dp, background = SurfaceSelected)
                    OmniIconButton(icon = Icons.Default.ViewList, contentDescription = "List View", onClick = {}, size = 36.dp)
                }
            }
            Spacer(Modifier.height(24.dp))
            // TABS
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tabs.forEachIndexed { i, t ->
                    val active = i == tab
                    Box(modifier = Modifier.clip(Shapes.pill).background(if (active) Iris else Surface2).border(1.dp, if (active) Color.Transparent else BorderLow, Shapes.pill).clickable { tab = i }.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        Text(t, color = if (active) Color.White else TextSecondary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        if (tab == 0) { // SONGS TAB
            if (likedSongs.isEmpty()) {
                item { OmniEmptyState("Library is empty", "Tap the heart on any track to add it to your library.") }
            } else {
                // PINNED COLLECTIONS
                if (pinned.isNotEmpty()) {
                    item {
                        OmniSectionHeader("Pinned Collections", actionLabel = "Edit Pins", onAction = {})
                        Spacer(Modifier.height(16.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(pinned) { song ->
                                PinnedCollectionCard(song, player)
                            }
                        }
                    }
                }

                // RECENT ADDITIONS
                if (recentAdditions.isNotEmpty()) {
                    item {
                        OmniSectionHeader("Recent Additions", actionLabel = "See all", onAction = {})
                        Spacer(Modifier.height(16.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            items(recentAdditions) { song ->
                                RecentAdditionCard(song, player)
                            }
                        }
                    }
                }

                // ALL SONGS TABLE
                item {
                    OmniSectionHeader("All Songs")
                    Spacer(Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text("#", color = TextMuted, style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(32.dp))
                        Text("TITLE", color = TextMuted, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(2f))
                        Text("ARTIST", color = TextMuted, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1.5f))
                        Text("ALBUM", color = TextMuted, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1.5f))
                        Text("⏱", color = TextMuted, style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(48.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                }
                
                itemsIndexed(likedSongs) { index, song ->
                    OmniSongRow(
                        item = song,
                        isActive = song.id == currentSong?.id,
                        isPlaying = song.id == currentSong?.id && playbackState == PlaybackState.PLAYING,
                        onClick = { player.playSong(song, index) },
                        onPlayNext = { player.playNext(song) },
                        onAddToQueue = { player.addToQueue(song) },
                        onLike = { player.toggleLike(song.id) }
                    )
                }
            }
        } else {
            item { OmniEmptyState("Nothing here yet", "This collection will populate as you use the app.") }
        }
    }
}

@Composable
private fun PinnedCollectionCard(song: SongItem, player: PlayerViewModel) {
    OmniSurface(
        shape = Shapes.medium,
        color = Surface1,
        modifier = Modifier.width(190.dp).height(110.dp),
        onClick = { player.playSong(song) }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(model = song.thumbnail.toHighResThumbnail(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)))))
            Icon(Icons.Default.PushPin, null, tint = Iris, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).size(20.dp))
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                Text(song.title, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(song.artists.firstOrNull()?.name ?: "Unknown", style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1)
            }
        }
    }
}

@Composable
private fun RecentAdditionCard(song: SongItem, player: PlayerViewModel) {
    Column(modifier = Modifier.width(140.dp).clickable { player.playSong(song) }) {
        Box(modifier = Modifier.size(140.dp).clip(Shapes.small)) {
            AsyncImage(model = song.thumbnail.toHighResThumbnail(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)))
            // Hover play button could go here, simplified for robust rendering
            Box(modifier = Modifier.align(Alignment.Center).size(36.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Iris), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(song.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(song.artists.firstOrNull()?.name ?: "Unknown", style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
