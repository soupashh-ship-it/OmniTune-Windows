package com.omnitune.app.window
import androidx.compose.foundation.interaction.MutableInteractionSource

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.omnitune.innertube.toHighResThumbnail
import com.omnitune.innertube.models.SongItem

@Composable
fun NowPlayingView(
    player: PlayerViewModel,
    currentSong: SongItem?,
    playbackState: PlaybackState,
    pos: PlayerPosition,
    volume: Int
) {
    var sliderPos by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    val displayPos = if (isDragging && pos.lengthMs > 0) sliderPos else pos.position
    val displayTimeMs = if (isDragging && pos.lengthMs > 0)
        (sliderPos * pos.lengthMs).toLong() else pos.timeMs

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            BgDark
                        )
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            val artScale by animateFloatAsState(
                targetValue = if (playbackState == PlaybackState.PLAYING) 1f else 0.9f,
                animationSpec = OmniMotion.pressSpring()
            )

            Box(
                modifier = Modifier
                    .widthIn(max = 360.dp)
                    .fillMaxWidth(0.7f)
                    .aspectRatio(1f)
                    .scale(artScale)
                    .shadow(12.dp, Shapes.artworkLarge, ambientColor = Color.Black.copy(alpha = 0.4f), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    .clip(Shapes.artworkLarge)
                    .background(BgCard),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = currentSong.thumbnail.toHighResThumbnail(),
                    contentDescription = "${currentSong.title} album art",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                currentSong.title,
                style = MaterialTheme.typography.headlineLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
            Text(
                currentSong.artists.joinToString(", ") { it.name },
                style = MaterialTheme.typography.titleLarge,
                color = TextGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.weight(1f))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(formatTime(displayTimeMs), style = MaterialTheme.typography.bodySmall, color = TextGray)
                Slider(
                    value = displayPos,
                    onValueChange = { v -> sliderPos = v; isDragging = true },
                    onValueChangeFinished = { isDragging = false; if (pos.lengthMs > 0) player.seek((sliderPos * pos.lengthMs).toLong()) },
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                    enabled = pos.lengthMs > 0,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = TextDim.copy(alpha = 0.5f)
                    )
                )
                if (pos.lengthMs > 0) {
                    Text("-${formatTime(pos.lengthMs - displayTimeMs)}", style = MaterialTheme.typography.bodySmall, color = TextGray)
                } else {
                    Spacer(Modifier.width(36.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val prevInteraction = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).clickable(interactionSource = prevInteraction, indication = null) { player.previousTrack() }.pressBounce(prevInteraction),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = TextWhite) }
                Spacer(Modifier.width(28.dp))
                val playInteraction = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(interactionSource = playInteraction, indication = null) { player.togglePlayPause() }
                        .pressBounce(playInteraction),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (playbackState == PlaybackState.PLAYING) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(Modifier.width(28.dp))
                val nextInteraction = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).clickable(interactionSource = nextInteraction, indication = null) { player.nextTrack() }.pressBounce(nextInteraction),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = TextWhite) }
            }

            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Volume", tint = TextGray, modifier = Modifier.size(20.dp))
                Slider(
                    value = volume / 200f,
                    onValueChange = { player.setVolume((it * 200).toInt()) },
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = TextDim.copy(alpha = 0.5f)
                    )
                )
                Text("${volume}%", style = MaterialTheme.typography.bodySmall, color = TextGray, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
            }

            Spacer(Modifier.height(20.dp))
        }
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
