package com.omnitune.app.window

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.app.platform.PlaybackState
import com.omnitune.app.platform.PlayerPosition
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.toHighResThumbnail

private data class LyricLine(val timeMs: Long, val text: String)

private fun parseLrc(text: String?): List<LyricLine> {
    if (text.isNullOrBlank()) return emptyList()
    val out = mutableListOf<LyricLine>()
    val regex = Regex("\\[(\\d{2}):(\\d{2})[.:](\\d{2})?]")
    text.lines().forEach { line ->
        val matches = regex.findAll(line)
        val content = line.replace(regex, "").trim()
        if (content.isEmpty()) return@forEach
        matches.forEach { m ->
            val mm = m.groupValues[1].toLongOrNull() ?: 0
            val ss = m.groupValues[2].toLongOrNull() ?: 0
            val xx = m.groupValues[3].toLongOrNull() ?: 0
            out.add(LyricLine((mm * 60 + ss) * 1000 + xx * 10, content))
        }
    }
    return out.sortedBy { it.timeMs }
}

@Composable
fun NowPlayingView(
    player: PlayerViewModel,
    currentSong: SongItem?,
    playbackState: PlaybackState,
    pos: PlayerPosition,
    volume: Int,
) {
    var sliderPos by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var tab by remember { mutableStateOf(0) }

    val displayPos = if (isDragging && pos.lengthMs > 0) sliderPos else pos.position
    val displayTimeMs = if (isDragging && pos.lengthMs > 0) (sliderPos * pos.lengthMs).toLong() else pos.timeMs

    val lyricsText by player.lyricsText.collectAsState()
    val lyricsLoading by player.lyricsLoading.collectAsState()
    val queue by player.queue.collectAsState()

    LaunchedEffect(currentSong?.id) { player.loadLyrics() }
    val lines = remember(lyricsText) { parseLrc(lyricsText) }
    val currentLine = remember(displayTimeMs, lines) {
        if (lines.isEmpty()) -1 else lines.indexOfLast { it.timeMs <= displayTimeMs }.coerceAtLeast(0)
    }

    if (currentSong == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No Track Playing", style = MaterialTheme.typography.headlineSmall, color = TextGray)
                Spacer(Modifier.height(8.dp))
                Text("Search and tap a song to start", style = MaterialTheme.typography.bodyLarge, color = TextDim)
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.radialGradient(colors = listOf(Iris.copy(alpha = 0.10f), BgDark))))
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 24.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            // LEFT
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(8.dp))
                val artScale by animateFloatAsState(if (playbackState == PlaybackState.PLAYING) 1f else 0.92f, spring())
                Box(
                    modifier = Modifier.widthIn(max = 340.dp).fillMaxWidth(0.7f).aspectRatio(1f).scale(artScale)
                        .shadow(16.dp, Shapes.artworkLarge, ambientColor = Color.Black.copy(alpha = 0.4f), spotColor = Iris.copy(alpha = 0.15f))
                        .clip(Shapes.artworkLarge).background(Surface1),
                    contentAlignment = Alignment.Center,
                ) {
                    AsyncImage(model = currentSong.thumbnail.toHighResThumbnail(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
                Spacer(Modifier.height(20.dp))
                Text(currentSong.title, style = MaterialTheme.typography.headlineLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(4.dp))
                Text(currentSong.artists.joinToString(", ") { it.name }, style = MaterialTheme.typography.titleLarge, color = TextGray, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

                Spacer(Modifier.weight(1f))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(formatTime(displayTimeMs), style = MaterialTheme.typography.bodySmall, color = TextGray)
                    com.omnitune.app.window.components.OmniProgressSlider(
                        fraction = displayPos,
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                        enabled = pos.lengthMs > 0,
                        onSeek = { sliderPos = it; isDragging = true },
                        onSeekFinished = { isDragging = false; if (pos.lengthMs > 0) player.seek((sliderPos * pos.lengthMs).toLong()) },
                    )
                    if (pos.lengthMs > 0) Text("-${formatTime(pos.lengthMs - displayTimeMs)}", style = MaterialTheme.typography.bodySmall, color = TextGray)
                    else Spacer(Modifier.width(36.dp))
                }
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    TransportCircle(Icons.Default.SkipPrevious, 40.dp, true) { player.previousTrack() }
                    Spacer(Modifier.width(28.dp))
                    val playInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(OmniGradients.irisToLavender).clickable(interactionSource = playInteraction, indication = null) { player.togglePlayPause() }.pressBounce(playInteraction), contentAlignment = Alignment.Center) {
                        Icon(if (playbackState == PlaybackState.PLAYING) Icons.Default.Pause else Icons.Default.PlayArrow, "Play/Pause", tint = Color(0xFF05060A), modifier = Modifier.size(32.dp))
                    }
                    Spacer(Modifier.width(28.dp))
                    TransportCircle(Icons.Default.SkipNext, 40.dp, true) { player.nextTrack() }
                }
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, "Volume", tint = TextGray, modifier = Modifier.size(20.dp))
                    com.omnitune.app.window.components.OmniVolumeControl(volume = volume, onVolumeChange = { player.setVolume(it) })
                    Text("${volume}%", style = MaterialTheme.typography.bodySmall, color = TextGray, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
                }
                Spacer(Modifier.height(12.dp))
            }

            // RIGHT PANEL
            Column(modifier = Modifier.width(360.dp).fillMaxHeight()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PanelTab("Lyrics", tab == 0) { tab = 0 }
                    PanelTab("Related", tab == 1) { tab = 1 }
                }
                Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxSize().clip(Shapes.large).background(Surface1.copy(alpha = 0.6f)).border(1.dp, BorderLow, Shapes.large).padding(16.dp)) {
                    when (tab) {
                        0 -> LyricsPanel(lines, currentLine, lyricsLoading, lyricsText != null)
                        1 -> RelatedPanel(queue, currentSong, player)
                    }
                }
            }
        }
    }
}

@Composable
private fun PanelTab(label: String, active: Boolean, onClick: () -> Unit) {
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    Box(modifier = Modifier.clip(Shapes.pill).background(if (active) Iris.copy(alpha = 0.18f) else Surface2).border(1.dp, if (active) Iris.copy(alpha = 0.4f) else BorderLow, Shapes.pill).clickable(interactionSource = interaction, indication = null, onClick = onClick).padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(label, color = if (active) IrisSoft else TextSecondary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun LyricsPanel(lines: List<LyricLine>, currentLine: Int, loading: Boolean, hasLyrics: Boolean) {
    if (loading) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(8) { com.omnitune.app.window.components.OmniShimmerBlock(Modifier.fillMaxWidth().height(16.dp).clip(Shapes.small)) }
        }
        return
    }
    if (lines.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Lyrics unavailable for this track", color = TextMuted, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        }
        return
    }
    val state = rememberLazyListState()
    val userScrolling by remember { derivedStateOf { state.isScrollInProgress } }
    LaunchedEffect(currentLine) {
        if (!userScrolling) state.animateScrollToItem(currentLine.coerceAtLeast(0), scrollOffset = -120)
    }
    LazyColumn(state = state, modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Spacer(Modifier.height(8.dp)) }
        itemsIndexed(lines) { i, line ->
            Text(
                line.text,
                style = if (i == currentLine) MaterialTheme.typography.titleMedium.copy(fontSize = 19.sp) else MaterialTheme.typography.bodyLarge,
                color = if (i == currentLine) IrisSoft else if (i < currentLine) TextSecondary.copy(alpha = 0.5f) else TextMuted,
                fontWeight = if (i == currentLine) FontWeight.SemiBold else FontWeight.Normal,
                lineHeight = 26.sp,
            )
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun RelatedPanel(queue: List<SongItem>, currentSong: SongItem?, player: PlayerViewModel) {
    if (queue.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Queue is empty", color = TextMuted, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        }
        return
    }
    val currentId = currentSong?.id
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        itemsIndexed(queue) { i, s ->
            val active = s.id == currentId
            Row(modifier = Modifier.fillMaxWidth().clickable { player.playQueueIndex(i) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(model = s.thumbnail.toHighResThumbnail(), contentDescription = null, modifier = Modifier.size(36.dp).clip(Shapes.artworkSmall), contentScale = ContentScale.Crop)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(s.title, color = if (active) IrisSoft else TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall)
                    Text(s.artists.joinToString { it.name }, color = TextSecondary, maxLines = 1, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun TransportCircle(icon: androidx.compose.ui.graphics.vector.ImageVector, size: androidx.compose.ui.unit.Dp, enabled: Boolean, onClick: () -> Unit) {
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    Box(modifier = Modifier.size(size).clip(CircleShape).clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick).pressBounce(interaction), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = if (enabled) TextWhite else TextDim, modifier = Modifier.size(size * 0.6f))
    }
}

private fun formatTime(ms: Long): String {
    if (ms < 0) return "0:00"
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}
