package com.omnitune.app.window
import androidx.compose.material.icons.filled.KeyboardArrowDown
import com.omnitune.app.platform.PlaybackState
import androidx.compose.material.icons.filled.KeyboardArrowUp
import com.omnitune.innertube.toHighResThumbnail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import com.omnitune.app.window.components.*
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.innertube.models.SongItem

@Composable
fun QueueView(player: PlayerViewModel) {
    val queue by player.queue.collectAsState()
    val queueIndex by player.queueIndex.collectAsState()
    val currentSong by player.currentSong.collectAsState()
    val playbackState by player.playbackState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 28.dp)) {
        // Header
        Text("Queue & Session", style = MaterialTheme.typography.displaySmall, color = TextPrimary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("Manage your listening queue, session history, and discover what's next.", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
        Spacer(Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            // Column 1: Up Next
            Column(modifier = Modifier.weight(1.2f).fillMaxHeight()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Up Next", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.width(12.dp))
                        val remaining = queue.drop(queueIndex + 1).size
                        Text("$remaining songs", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        QueueHeaderAction(Icons.Default.Delete, "Clear") { player.clearQueue() }
                        QueueHeaderAction(Icons.Default.PlaylistAdd, "Save as Playlist") {}
                        QueueHeaderAction(Icons.Default.Shuffle, "Shuffle") {}
                        QueueHeaderAction(Icons.Default.Repeat, "Repeat") {}
                    }
                }
                Spacer(Modifier.height(16.dp))

                if (queue.isEmpty()) {
                    OmniEmptyState("Queue is empty", "Search and play a song to get started")
                } else {
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        itemsIndexed(queue) { idx, item ->
                            val isCurrent = idx == queueIndex
                            val isPlaying = isCurrent && playbackState == PlaybackState.PLAYING
                            QueueRow(
                                item = item,
                                index = idx,
                                isCurrent = isCurrent,
                                isPlaying = isPlaying,
                                onPlay = { player.playQueueIndex(idx) },
                                onRemove = { player.removeFromQueue(idx) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text("Queue Controls", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QueueControlCard(Modifier.weight(1f), Icons.Default.GraphicEq, "Crossfade", "5 seconds", Iris)
                    QueueControlCard(Modifier.weight(1f), Icons.Default.Bolt, "Autoplay", "Suggested", CoolBlue)
                    QueueControlCard(Modifier.weight(1f), Icons.Default.Tune, "Mix Mood", "Balanced", SuccessGreen)
                    QueueControlCard(Modifier.weight(1f), Icons.Default.AllInclusive, "Gapless Playback", "Enabled", IrisSoft)
                }
            }

            // Column 2: Session History & Recently Played
            Column(modifier = Modifier.weight(0.7f).fillMaxHeight()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Session History", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text("See all", style = MaterialTheme.typography.labelMedium, color = IrisSoft, modifier = Modifier.clickable { })
                }
                Spacer(Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SessionHistoryCard("After Hours", "Playlist · 18 songs", "Played just now")
                    SessionHistoryCard("Midnight Drive", "Playlist · 24 songs", "Played 1h ago")
                    SessionHistoryCard("Afrobeats Heat", "Playlist · 35 songs", "Played 3h ago")
                    SessionHistoryCard("Chill Mornings", "Playlist · 22 songs", "Played yesterday")
                }
                
                Spacer(Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Recently Played", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text("See all", style = MaterialTheme.typography.labelMedium, color = IrisSoft, modifier = Modifier.clickable { })
                }
                Spacer(Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SessionHistoryCard("Timeless", "Album · 15 songs", null)
                    SessionHistoryCard("Mr Money With The Vibe", "Album · 12 songs", null)
                    SessionHistoryCard("Peace Be Unto You (PBUY)", "Album · 10 songs", null)
                }
            }

            // Column 3: After This Queue Ends
            OmniSurface(modifier = Modifier.weight(0.7f).fillMaxHeight(), color = Surface1, border = true) {
                Column(modifier = Modifier.padding(20.dp).fillMaxHeight()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("After This Queue Ends", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Text("Smart recommendations based on your queue and listening history.", style = MaterialTheme.typography.bodySmall, color = TextSecondary, lineHeight = 16.sp)
                        }
                        Icon(Icons.Default.AutoAwesome, null, tint = IrisSoft, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.height(20.dp))
                    
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        RecommendationRow("SOWETO", "Victony")
                        RecommendationRow("5AM", "Kizz Daniel")
                        RecommendationRow("Rush", "Ayra Starr")
                        RecommendationRow("Unavailable", "Davido")
                        RecommendationRow("City Boys", "Adekunle Gold")
                        RecommendationRow("Jealous", "Fireboy DML")
                        RecommendationRow("Lonely at the Top", "Asake")
                        RecommendationRow("I Need You", "Omah Lay")
                    }
                    
                    Spacer(Modifier.height(20.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(44.dp).clip(Shapes.medium).background(OmniGradients.primaryAction).clickable { }, contentAlignment = Alignment.Center) {
                        Text("Add All to Queue", color = Color.White, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Refresh Recommendations", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueHeaderAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(modifier = Modifier.clickable(onClick = onClick), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
    }
}

@Composable
private fun QueueControlCard(modifier: Modifier, icon: ImageVector, title: String, subtitle: String, color: Color) {
    OmniSurface(modifier = modifier, color = Surface2, border = true) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SessionHistoryCard(title: String, subtitle: String, time: String?) {
    OmniSurface(modifier = Modifier.fillMaxWidth(), color = Surface2, border = true) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(Shapes.artworkSmall).background(Surface3))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (time != null) {
                    Text(time, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
            }
            OmniIconButton(icon = Icons.Default.PlayArrow, contentDescription = "Play", onClick = {}, size = 32.dp, background = Surface3)
        }
    }
}

@Composable
private fun RecommendationRow(title: String, artist: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).clip(Shapes.artworkSmall).background(Surface3))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(artist, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text("Add", style = MaterialTheme.typography.labelMedium, color = TextSecondary, modifier = Modifier.padding(horizontal = 8.dp))
        Icon(Icons.Default.MoreHoriz, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun QueueRow(
    item: SongItem,
    index: Int,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onRemove: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val bg = when {
        isCurrent -> Iris.copy(alpha = 0.1f)
        isHovered -> BgCardHover
        else -> Color.Transparent
    }

    Surface(
        modifier = Modifier.fillMaxWidth()
            .then(if (isCurrent) Modifier.border(1.dp, Iris.copy(alpha = 0.3f), Shapes.small) else Modifier)
            .clip(Shapes.small)
            .background(bg)
            .hoverable(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onPlay)
            .pressBounce(interactionSource),
        shape = Shapes.small,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text((index + 1).toString(), style = MaterialTheme.typography.bodyMedium, color = if (isCurrent) IrisSoft else TextDim, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier.size(40.dp).clip(Shapes.artworkSmall).background(BgCard),
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
            Spacer(Modifier.width(12.dp))
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
                    Text(artists, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            
            Text(item.title, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.weight(0.8f).padding(horizontal = 8.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            
            item.duration?.let { dur ->
                Text("${dur / 60}:${(dur % 60).toString().padStart(2, '0')}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Spacer(Modifier.width(12.dp))
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.MoreHoriz, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

