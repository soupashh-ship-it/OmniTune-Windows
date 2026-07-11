package com.omnitune.app.window

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    
    
    val playerHeight = 92.dp // Typical derived height
    val artworkSize = 50.dp
    val centralButtonSize = 46.dp

    
    val playerBrush = Brush.horizontalGradient(
        colorStops = arrayOf(
            0.00f to Color(0xFF0A0D1A),
            0.22f to Color(0xFF0B0E1D),
            0.42f to Color(0xFF110E24),
            0.53f to Color(0xFF17102D),
            0.66f to Color(0xFF110F26),
            0.82f to Color(0xFF0B0E1D),
            1.00f to Color(0xFF090C18),
        )
    )

    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(playerBrush)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.045f),
                shape = shape,
            )
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // LEFT: track info (24%)
            Row(
                modifier = Modifier.weight(0.24f).padding(end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (currentSong != null) {
                    AsyncImage(
                        model = currentSong.thumbnail?.toHighResThumbnail(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(artworkSize)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { player.navigateTo(com.omnitune.app.player.NavScreen.NowPlaying) },
                        contentScale = ContentScale.Crop,
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f).clickable { player.navigateTo(com.omnitune.app.player.NavScreen.NowPlaying) }) {
                        Text(currentSong.title, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp), color = Color(0xFFF4F3FA), maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(2.dp))
                        Text(currentSong.artists?.joinToString(", ") { it.name ?: "" } ?: "", style = MaterialTheme.typography.labelMedium, color = Color(0xFFA9AEC2), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Spacer(Modifier.width(10.dp))
                    val favOn = liked.contains(currentSong.id)
                    TransportIcon(
                        if (favOn) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        tint = if (favOn) Color(0xFF7C6DFF) else Color(0xFFA9AEC2),
                        onClick = { player.toggleLike(currentSong.id) },
                        size = 18.dp
                    )
                    Spacer(Modifier.width(4.dp))
                    TransportIcon(Icons.Default.MoreHoriz, tint = Color(0xFFA9AEC2), onClick = {}, size = 18.dp)
                } else {
                    Box(modifier = Modifier.size(artworkSize).clip(RoundedCornerShape(6.dp)).background(Color.White.copy(alpha = 0.05f)))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Box(modifier = Modifier.width(100.dp).height(12.dp).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.05f)))
                        Spacer(Modifier.height(8.dp))
                        Box(modifier = Modifier.width(60.dp).height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.05f)))
                    }
                }
            }

            // CENTER: transport controls + timeline (52%)
            Column(
                modifier = Modifier.weight(0.52f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val transportColor = if (currentSong != null) Color(0xFFF4F3FA) else Color(0xFF737B93).copy(alpha = 0.4f)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    TransportIcon(Icons.Filled.Shuffle, tint = if (shuffle) Color(0xFF7C6DFF) else Color(0xFFA9AEC2), enabled = currentSong != null, onClick = { player.toggleShuffle() }, size = 18.dp)
                    TransportIcon(Icons.Filled.SkipPrevious, tint = transportColor, enabled = currentSong != null, onClick = { player.previousTrack() }, size = 24.dp)
                    
                    val playInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    val isHovered by playInteraction.collectIsHoveredAsState()
                    Box(
                        modifier = Modifier
                            .size(centralButtonSize)
                            .clip(CircleShape)
                            .background(if (currentSong != null) (if (isHovered) Color(0xFFFFFFFF) else Color(0xFFECECF4)) else Color(0xFF737B93).copy(alpha = 0.25f))
                            .clickable(interactionSource = playInteraction, indication = null, enabled = currentSong != null, onClick = { player.togglePlayPause() })
                            .pressBounce(playInteraction),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (playbackState == PlaybackState.PLAYING) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color(0xFF151726),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    
                    TransportIcon(Icons.Filled.SkipNext, tint = transportColor, enabled = currentSong != null, onClick = { player.nextTrack() }, size = 24.dp)
                    val repeatTint = if (repeatMode != com.omnitune.app.player.RepeatMode.OFF) Color(0xFF7C6DFF) else Color(0xFFA9AEC2)
                    TransportIcon(
                        if (repeatMode == com.omnitune.app.player.RepeatMode.ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                        tint = repeatTint,
                        enabled = currentSong != null,
                        onClick = { player.cycleRepeat() },
                        size = 18.dp
                    )
                }
                
                Spacer(Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(0.95f), verticalAlignment = Alignment.CenterVertically) {
                    TimeText(if (isDragging) (sliderPos * position.lengthMs).toLong() else position.timeMs)
                    Spacer(Modifier.width(10.dp))
                    
                    // Thin refined timeline
                    Box(modifier = Modifier.weight(1f).height(16.dp), contentAlignment = Alignment.Center) {
                        com.omnitune.app.window.components.OmniProgressSlider(
                            fraction = displayFraction,
                            modifier = Modifier.fillMaxWidth(),
                            onSeek = { f ->
                                isDragging = true
                                sliderPos = f
                            },
                            onSeekFinished = {
                                isDragging = false
                                if (position.lengthMs > 0) player.seek((sliderPos * position.lengthMs).toLong())
                            },
                            enabled = currentSong != null,
                            trackHeight = 2.dp,
                            thumbSize = 8.dp,
                            activeBrush = Brush.horizontalGradient(listOf(Color(0xFF6F7FFF), Color(0xFF7E72FF))),
                            inactiveColor = Color.White.copy(alpha = 0.11f)
                        )
                    }
                    
                    Spacer(Modifier.width(10.dp))
                    TimeText(position.lengthMs)
                }
            }

            // RIGHT: lyrics / queue / volume (24%)
            Row(
                modifier = Modifier.weight(0.24f).padding(start = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TransportIcon(Icons.Filled.Lyrics, tint = Color(0xFFA9AEC2), onClick = { player.navigateTo(com.omnitune.app.player.NavScreen.NowPlaying) }, size = 18.dp)
                Spacer(Modifier.width(14.dp))
                TransportIcon(Icons.AutoMirrored.Filled.QueueMusic, tint = Color(0xFFA9AEC2), onClick = { player.navigateTo(com.omnitune.app.player.NavScreen.Queue) }, size = 18.dp)
                Spacer(Modifier.width(18.dp))
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
