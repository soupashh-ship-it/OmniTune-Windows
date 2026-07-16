package com.omnitune.app.window

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.omnitune.app.player.LyricLine
import com.omnitune.app.player.LyricsResult
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.app.platform.PlaybackState
import com.omnitune.app.platform.PlayerPosition
import com.omnitune.app.window.components.OmniProgressSlider
import com.omnitune.app.window.components.OmniShimmerBlock
import com.omnitune.app.window.components.OmniVolumeControl
import com.omnitune.innertube.models.AlbumItem
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.toHighResThumbnail
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────────────────────────────────────
// NowPlayingView — Nocturne Prism immersive player + synchronized lyrics
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun NowPlayingView(
    player: PlayerViewModel,
    currentSong: SongItem?,
    playbackState: PlaybackState,
    pos: PlayerPosition,
    volume: Int,
) {
    val lyricsResult by player.lyricsResult.collectAsState()
    val repeat by player.repeatMode.collectAsState()
    val shuffle by player.shuffleMode.collectAsState()
    val queue by player.queue.collectAsState()
    val queueIndex by player.queueIndex.collectAsState()
    val related by player.discoveryRelated.collectAsState()
    val relatedLoading by player.relatedLoading.collectAsState()
    val relatedError by player.relatedError.collectAsState()

    // Seek drag state — UI owns the scrub position during drag; engine is not queried
    var sliderPos by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val displayPos = if (isDragging && pos.lengthMs > 0) sliderPos else pos.position
    val displayTimeMs = if (isDragging && pos.lengthMs > 0) (sliderPos * pos.lengthMs).toLong() else pos.timeMs

    if (currentSong == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.MusicNote, null, tint = TextMuted, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(16.dp))
                Text("No Track Playing", style = MaterialTheme.typography.headlineSmall, color = TextGray)
                Spacer(Modifier.height(8.dp))
                Text("Search and tap a song to start playing", style = MaterialTheme.typography.bodyLarge, color = TextDim)
            }
        }
        return
    }

    // Ambient background — subtle radial glow behind artwork (not full-screen blur)
    Box(modifier = Modifier.fillMaxSize()) {
        // Background: deep obsidian + subtle iris ambient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Iris.copy(alpha = 0.09f), BgDeep),
                        radius = 900f,
                    )
                )
        )

        val metrics = LocalHomeReferenceMetrics.current
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val compact = maxWidth < 860.dp
            if (compact) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(metrics.px(24f)),
                    verticalArrangement = Arrangement.spacedBy(metrics.px(18f)),
                ) {
                    item {
                        PlayerRegion(
                            modifier = Modifier.fillMaxWidth().height(metrics.px(560f)),
                            player = player,
                            currentSong = currentSong,
                            playbackState = playbackState,
                            pos = pos,
                            displayTimeMs = displayTimeMs,
                            displayPos = displayPos,
                            volume = volume,
                            repeat = repeat,
                            shuffle = shuffle,
                            isDragging = isDragging,
                            onSliderChange = { sliderPos = it; isDragging = true },
                            onSliderFinish = {
                                isDragging = false
                                if (pos.lengthMs > 0) player.seek((sliderPos * pos.lengthMs).toLong())
                            },
                        )
                    }
                    item {
                        LyricsRegion(
                            modifier = Modifier.fillMaxWidth().height(metrics.px(520f)),
                            lyricsResult = lyricsResult,
                            displayTimeMs = displayTimeMs,
                            player = player,
                            queue = queue,
                            currentSong = currentSong,
                            queueIndex = queueIndex,
                            related = related,
                            relatedLoading = relatedLoading,
                            relatedError = relatedError,
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxSize().padding(start = metrics.px(24f), top = metrics.px(8f), end = metrics.px(24f), bottom = metrics.px(12f)),
                    horizontalArrangement = Arrangement.spacedBy(metrics.px(42f)),
                ) {
                    PlayerRegion(
                        modifier = Modifier.widthIn(min = metrics.px(360f), max = metrics.px(424f)).fillMaxHeight(),
                        player = player,
                        currentSong = currentSong,
                        playbackState = playbackState,
                        pos = pos,
                        displayTimeMs = displayTimeMs,
                        displayPos = displayPos,
                        volume = volume,
                        repeat = repeat,
                        shuffle = shuffle,
                        isDragging = isDragging,
                        onSliderChange = { sliderPos = it; isDragging = true },
                        onSliderFinish = {
                            isDragging = false
                            if (pos.lengthMs > 0) player.seek((sliderPos * pos.lengthMs).toLong())
                        },
                    )

                    LyricsRegion(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        lyricsResult = lyricsResult,
                        displayTimeMs = displayTimeMs,
                        player = player,
                        queue = queue,
                        currentSong = currentSong,
                        queueIndex = queueIndex,
                        related = related,
                        relatedLoading = relatedLoading,
                        relatedError = relatedError,
                    )
                }
            }
        }
    }
}
