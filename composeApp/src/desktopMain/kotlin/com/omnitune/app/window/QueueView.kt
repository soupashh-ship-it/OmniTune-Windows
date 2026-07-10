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

    Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Up Next", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            if (queue.isNotEmpty()) {
                FilledTonalButton(
                    onClick = { player.clearQueue() },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Clear, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Clear", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text("${queue.size} track${if (queue.size != 1) "s" else ""} in queue", style = MaterialTheme.typography.bodyLarge, color = TextGray)

        if (queue.isEmpty()) {
            Spacer(Modifier.weight(1f))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Queue is empty", style = MaterialTheme.typography.headlineSmall, color = TextDim)
                    Spacer(Modifier.height(8.dp))
                    Text("Search and play a song to get started", style = MaterialTheme.typography.bodyLarge, color = TextDim)
                }
            }
            Spacer(Modifier.weight(1f))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                itemsIndexed(queue) { idx, item ->
                    QueueRow(
                        item = item,
                        index = idx,
                        isCurrent = idx == queueIndex,
                        isPlaying = idx == queueIndex && playbackState == PlaybackState.PLAYING,
                        canMoveUp = idx > 0,
                        canMoveDown = idx < queue.size - 1,
                        onPlay = { player.playQueueIndex(idx) },
                        onRemove = { player.removeFromQueue(idx) },
                        onMoveUp = { player.moveQueueItem(idx, idx - 1) },
                        onMoveDown = { player.moveQueueItem(idx, idx + 1) }
                    )
                }
            }
        }
    }
}

@Composable
private fun QueueRow(
    item: SongItem,
    index: Int,
    isCurrent: Boolean,
    isPlaying: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onPlay: () -> Unit,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val artists = item.artists.joinToString(", ") { it.name }
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val bg = when {
        isCurrent -> AccentLavender.copy(alpha = 0.2f)
        isHovered -> BgCardHover
        else -> Color.Transparent
    }

    Surface(
        modifier = Modifier.fillMaxWidth().clip(Shapes.small).background(bg).hoverable(interactionSource).clickable(interactionSource = interactionSource, indication = null, onClick = onPlay).pressBounce(interactionSource),
        shape = Shapes.small,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(Spacing.compact),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                        PlayingIndicatorBox(isActive = true, playWhenReady = isPlaying, color = AccentLavender)
                    }
                }
            }
            Spacer(Modifier.width(Spacing.small))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (artists.isNotEmpty()) {
                    Text(artists, style = MaterialTheme.typography.bodyMedium, color = TextGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            item.duration?.let { dur ->
                Text("${dur / 60}:${(dur % 60).toString().padStart(2, '0')}", style = MaterialTheme.typography.bodySmall, color = TextDim)
            }
            Spacer(Modifier.width(8.dp))
            if (isHovered || isCurrent) {
                IconButton(onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.KeyboardArrowUp, null, tint = if (canMoveUp) TextDim else Color.Transparent, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = if (canMoveDown) TextDim else Color.Transparent, modifier = Modifier.size(16.dp))
                }
            }
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Delete, null, tint = TextDim, modifier = Modifier.size(16.dp))
            }
        }
    }
}
