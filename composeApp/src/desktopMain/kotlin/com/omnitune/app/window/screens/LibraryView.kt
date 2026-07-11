package com.omnitune.app.window.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.hoverable
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

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 44.dp, vertical = 32.dp),
    ) {
        // TOP HEADER
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Library", style = MaterialTheme.typography.displaySmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("${likedSongs.size} liked songs", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
            }
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
                Box(
                    modifier = Modifier
                        .clip(Shapes.pill)
                        .background(if (active) Iris else Surface2)
                        .border(1.dp, if (active) Color.Transparent else BorderLow, Shapes.pill)
                        .clickable { tab = i }
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(t, color = if (active) Color.White else TextSecondary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                }
            }
        }
        Spacer(Modifier.height(32.dp))

        if (tab == 0) { // SONGS TAB
            if (likedSongs.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    OmniEmptyState("Library is empty", "Tap the heart on any track to add it to your library.")
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    itemsIndexed(likedSongs) { index, song ->
                        val isCurrent = song.id == currentSong?.id
                        val isPlaying = isCurrent && playbackState == PlaybackState.PLAYING
                        LibrarySongRow(
                            item = song,
                            index = index,
                            isCurrent = isCurrent,
                            isPlaying = isPlaying,
                            onPlay = { player.playSong(song, index) },
                            onUnlike = { player.toggleLike(song.id) }
                        )
                    }
                }
            }
        } else {
            // Other empty tabs
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                when (tab) {
                    1 -> OmniEmptyState("No albums yet", "Save albums to your library for easy access.")
                    2 -> OmniEmptyState("No artists yet", "Follow artists to see their latest releases here.")
                    3 -> OmniEmptyState("No playlists", "Create a playlist or save ones you discover.")
                    4 -> OmniEmptyState("No downloads", "Download music to listen offline.")
                    else -> OmniEmptyState("Nothing here", "Check back later.")
                }
            }
        }
    }
}

@Composable
private fun LibrarySongRow(
    item: SongItem,
    index: Int,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onUnlike: () -> Unit
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val bg = when {
        isHovered -> BgCardHover
        else -> Surface1
    }

    Surface(
        modifier = Modifier.fillMaxWidth()
            .then(if (isCurrent) Modifier.border(1.dp, Iris.copy(alpha = 0.3f), Shapes.small) else Modifier)
            .clip(Shapes.small)
            .background(bg)
            .hoverable(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onPlay),
        shape = Shapes.small,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Iris bar indicator for current song
            Box(modifier = Modifier.width(4.dp).height(32.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(2.dp)).background(if (isCurrent) Iris else Color.Transparent))
            Spacer(Modifier.width(12.dp))
            
            Box(
                modifier = Modifier.size(48.dp).clip(Shapes.artworkSmall).background(BgCard),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = item.thumbnail.toHighResThumbnail(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (isCurrent) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                        PlayingIndicatorBox(isActive = true, playWhenReady = isPlaying, color = Iris)
                    }
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isCurrent) IrisSoft else TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val artists = item.artists.joinToString(", ") { it.name }
                if (artists.isNotEmpty()) {
                    Text(artists, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            
            // Album name
            Text(item.title, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.weight(0.8f).padding(horizontal = 16.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            
            item.duration?.let { dur ->
                Text("${dur / 60}:${(dur % 60).toString().padStart(2, '0')}", style = MaterialTheme.typography.bodySmall, color = TextDim)
            }
            Spacer(Modifier.width(16.dp))
            IconButton(onClick = onUnlike, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Favorite, null, tint = IrisSoft, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.MoreHoriz, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
            }
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
