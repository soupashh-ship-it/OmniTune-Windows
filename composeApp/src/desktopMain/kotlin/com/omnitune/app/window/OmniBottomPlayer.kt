package com.omnitune.app.window

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.app.player.RepeatMode
import com.omnitune.app.platform.PlaybackState
import com.omnitune.app.platform.PlayerPosition
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.toHighResThumbnail
import com.omnitune.app.window.components.OmniVolumeControl

@Composable
fun OmniBottomPlayer(
    player: PlayerViewModel,
    currentSong: SongItem?,
    playbackState: PlaybackState,
    position: PlayerPosition,
    volume: Int,
    modifier: Modifier = Modifier,
) {
    val shuffle by player.shuffleMode.collectAsState()
    val repeatMode by player.repeatMode.collectAsState()
    val liked by player.likedSongs.collectAsState()

    var sliderPos by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val fraction = if (position.lengthMs > 0) (position.position.toFloat() / position.lengthMs).coerceIn(0f, 1f) else 0f
    val displayFraction = if (isDragging) sliderPos else fraction
    val springProgress by animateFloatAsState(displayFraction, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow), label = "bar")

    OmniGlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 88.dp)
            .padding(horizontal = Spacing.small, vertical = Spacing.compact),
        shape = Shapes.dock,
        style = GlassDefaults.miniPlayer,
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
            if (currentSong != null && position.lengthMs > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TimeText(position.timeMs)
                    com.omnitune.app.window.components.OmniProgressSlider(
                        fraction = displayFraction,
                        modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
                        onSeek = { f ->
                            isDragging = true
                            sliderPos = f
                        },
                        onSeekFinished = {
                            isDragging = false
                            if (position.lengthMs > 0) player.seek((sliderPos * position.lengthMs).toLong())
                        },
                        enabled = true,
                    )
                    TimeText(position.lengthMs, remaining = true)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(48.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // LEFT: track info
                if (currentSong != null) {
                    AsyncImage(
                        model = currentSong.thumbnail.toHighResThumbnail(),
                        contentDescription = null,
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)).clickable { player.navigateTo(com.omnitune.app.player.NavScreen.NowPlaying) },
                        contentScale = ContentScale.Crop,
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.widthIn(max = 200.dp).clickable { player.navigateTo(com.omnitune.app.player.NavScreen.NowPlaying) }) {
                        Text(currentSong.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(currentSong.artists.joinToString(", ") { it.name }, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Spacer(Modifier.width(8.dp))
                    val favOn = liked.contains(currentSong.id)
                    TransportIcon(
                        if (favOn) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        tint = if (favOn) IrisSoft else TextSecondary,
                        onClick = { player.toggleLike(currentSong.id) },
                    )
                    TransportIcon(Icons.Default.MoreHoriz, tint = TextSecondary, onClick = {})
                } else {
                    Text("OmniTune", style = MaterialTheme.typography.titleMedium, color = TextMuted, modifier = Modifier.padding(start = 4.dp))
                }

                Spacer(Modifier.weight(1f))

                // CENTER: transport
                val transportColor = if (currentSong != null) TextPrimary else TextMuted.copy(alpha = 0.4f)
                TransportIcon(Icons.Filled.Shuffle, tint = if (shuffle) IrisSoft else transportColor, enabled = currentSong != null, onClick = { player.toggleShuffle() })
                Spacer(Modifier.width(14.dp))
                TransportIcon(Icons.Filled.SkipPrevious, tint = transportColor, enabled = currentSong != null, onClick = { player.previousTrack() }, size = 26.dp)
                Spacer(Modifier.width(12.dp))
                val playInteraction = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape)
                        .background(if (currentSong != null) OmniGradients.irisToLavender else Brush.linearGradient(listOf(TextMuted.copy(alpha = 0.25f), TextMuted.copy(alpha = 0.25f))))
                        .clickable(interactionSource = playInteraction, indication = null, enabled = currentSong != null, onClick = { player.togglePlayPause() })
                        .pressBounce(playInteraction),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(if (playbackState == PlaybackState.PLAYING) Icons.Filled.Pause else Icons.Filled.PlayArrow, "Play/Pause", tint = Color(0xFF05060A), modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                TransportIcon(Icons.Filled.SkipNext, tint = transportColor, enabled = currentSong != null, onClick = { player.nextTrack() }, size = 26.dp)
                Spacer(Modifier.width(14.dp))
                val repeatTint = if (repeatMode != RepeatMode.OFF) IrisSoft else transportColor
                TransportIcon(
                    if (repeatMode == RepeatMode.ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                    tint = repeatTint,
                    enabled = currentSong != null,
                    onClick = { player.cycleRepeat() },
                )

                Spacer(Modifier.weight(1f))

                // RIGHT: lyrics / queue / volume
                TransportIcon(Icons.Filled.Lyrics, tint = TextSecondary, onClick = { player.navigateTo(com.omnitune.app.player.NavScreen.NowPlaying) })
                Spacer(Modifier.width(10.dp))
                TransportIcon(Icons.AutoMirrored.Filled.QueueMusic, tint = TextSecondary, onClick = { player.navigateTo(com.omnitune.app.player.NavScreen.Queue) })
                Spacer(Modifier.width(14.dp))
                OmniVolumeControl(volume = volume, onVolumeChange = { player.setVolume(it) })
            }
        }
    }
}

@Composable
private fun TransportIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit,
    enabled: Boolean = true,
    size: androidx.compose.ui.unit.Dp = 22.dp,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier.size(36.dp).clip(CircleShape).clickable(interactionSource = interactionSource, indication = null, enabled = enabled, onClick = onClick).pressBounce(interactionSource),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = if (enabled) tint else TextMuted.copy(alpha = 0.35f), modifier = Modifier.size(size))
    }
}

@Composable
private fun TimeText(ms: Long, remaining: Boolean = false) {
    val totalSec = ((if (remaining) -ms else ms) / 1000)
    val sign = if (remaining && ms > 0) "-" else ""
    val s = kotlin.math.abs(totalSec)
    val m = s / 60
    val sec = s % 60
    Text("$sign${m}:${sec.toString().padStart(2, '0')}", style = MaterialTheme.typography.bodySmall, color = TextMuted, modifier = Modifier.padding(horizontal = 2.dp))
}
