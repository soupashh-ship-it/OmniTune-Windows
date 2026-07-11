package com.omnitune.app.window

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
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.app.platform.PlaybackState
import com.omnitune.app.platform.PlayerPosition
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.toHighResThumbnail
import com.omnitune.app.window.components.OmniProgressSlider

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

    val artworkSize = 50.dp

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
            // ── LEFT: track info (24%) ───────────────────────────────────────
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
                    Column(
                        Modifier.weight(1f)
                            .clickable { player.navigateTo(com.omnitune.app.player.NavScreen.NowPlaying) }
                    ) {
                        Text(
                            currentSong.title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = Color(0xFFF4F3FA),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            currentSong.artists?.joinToString(", ") { it.name ?: "" } ?: "",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFA9AEC2),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
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
                    Box(
                        modifier = Modifier
                            .size(artworkSize)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Box(
                            modifier = Modifier.width(100.dp).height(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                        )
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier.width(60.dp).height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                        )
                    }
                }
            }

            // ── CENTER + RIGHT: reference 607×41 control band (76%) ─────────
            PlayerControlBand(
                isPlaying = playbackState == PlaybackState.PLAYING,
                positionMs = position.timeMs,
                durationMs = position.lengthMs,
                shuffleEnabled = shuffle,
                repeatMode = repeatMode,
                volume = volume,
                currentSong = currentSong,
                onShuffleClick = { player.toggleShuffle() },
                onPreviousClick = { player.previousTrack() },
                onPlayPauseClick = { player.togglePlayPause() },
                onNextClick = { player.nextTrack() },
                onRepeatClick = { player.cycleRepeat() },
                onSeekFraction = { f ->
                    if (position.lengthMs > 0) player.seek((f * position.lengthMs).toLong())
                },
                onVolumeChange = { player.setVolume(it) },
                onQueueClick = { player.navigateTo(com.omnitune.app.player.NavScreen.Queue) },
                modifier = Modifier.weight(0.76f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PlayerControlBand — exact reproduction of 607×41 reference
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PlayerControlBand(
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    shuffleEnabled: Boolean,
    repeatMode: com.omnitune.app.player.RepeatMode,
    volume: Int,
    currentSong: SongItem?,
    onShuffleClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onSeekFraction: (Float) -> Unit,
    onVolumeChange: (Int) -> Unit,
    onQueueClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val enabled = currentSong != null
    val iconMuted = Color(0xFF6D748D)
    val iconActive = Color(0xFF7C6DFF)
    val iconBright = Color(0xFFAEB4C8)

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth().height(72.dp)
    ) {
        val bw = maxWidth

        // ── TRANSPORT ROW ────────────────────────────────────────────────────
        // Centered horizontally. Reference x-center of play button ≈ 0.346 of
        // the 607px crop. The crop itself starts at ~24% of full width, so
        // the play button sits at ~0.24 + 0.76*0.346 ≈ 0.503 of full width —
        // i.e., the true horizontal center. We just center this Row.
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp)
                .height(34.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Shuffle
            PlayerSmallIcon(
                icon = Icons.Filled.Shuffle,
                tint = if (shuffleEnabled) iconActive else iconMuted,
                enabled = enabled,
                onClick = onShuffleClick,
                size = 16.dp,
            )
            // Previous
            PlayerSmallIcon(
                icon = Icons.Filled.SkipPrevious,
                tint = if (enabled) iconBright else iconMuted.copy(alpha = 0.4f),
                enabled = enabled,
                onClick = onPreviousClick,
                size = 20.dp,
            )
            // Play/Pause — large white circle
            PlayerPlayPauseButton(
                isPlaying = isPlaying,
                enabled = enabled,
                onClick = onPlayPauseClick,
            )
            // Next
            PlayerSmallIcon(
                icon = Icons.Filled.SkipNext,
                tint = if (enabled) iconBright else iconMuted.copy(alpha = 0.4f),
                enabled = enabled,
                onClick = onNextClick,
                size = 20.dp,
            )
            // Repeat
            PlayerSmallIcon(
                icon = if (repeatMode == com.omnitune.app.player.RepeatMode.ONE)
                    Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                tint = if (repeatMode != com.omnitune.app.player.RepeatMode.OFF) iconActive else iconMuted,
                enabled = enabled,
                onClick = onRepeatClick,
                size = 16.dp,
            )
        }

        // ── RIGHT UTILITIES ──────────────────────────────────────────────────
        // Reference: first utility at ~0.774 of band width, volume icon ~0.820,
        // slider 0.853–0.982. In our 76%-wide band these are close to the end.
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 14.dp, end = 4.dp)
                .height(28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PlayerUtilityIcon(
                icon = Icons.AutoMirrored.Filled.QueueMusic,
                tint = iconMuted,
                onClick = onQueueClick,
                size = 16.dp,
            )
            // Volume icon
            Icon(
                if (volume == 0) Icons.AutoMirrored.Filled.VolumeOff
                else Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Volume",
                tint = iconMuted,
                modifier = Modifier.size(16.dp)
            )
            // Volume slider — thin, blue-iris fill, ~78dp wide
            PlayerThinSlider(
                fraction = (volume / 100f).coerceIn(0f, 1f),
                onFractionChange = { onVolumeChange((it * 100).toInt()) },
                modifier = Modifier.width(80.dp),
            )
        }

        // ── TIMELINE ROW ─────────────────────────────────────────────────────
        // Sits in the lower band. Spans from left edge to just before right
        // utilities. Reference: track from ~x=27 to ~x=400 in the 607px crop.
        // That is 0.044→0.659 of crop width. Since crop = 76% of full player,
        // we use padding to leave room at the right for the utility cluster.
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(bottom = 8.dp, start = 0.dp, end = (bw * 0.28f)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TimeLabel(positionMs)
            Spacer(Modifier.width(8.dp))
            PlayerThinSeekBar(
                positionMs = positionMs,
                durationMs = durationMs,
                onSeekFraction = onSeekFraction,
                enabled = enabled,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            TimeLabel(durationMs)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sub-components
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PlayerPlayPauseButton(
    isPlaying: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(
                if (!enabled) Color.White.copy(alpha = 0.12f)
                else if (isHovered) Color.White
                else Color(0xFFECECF4)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .pressBounce(interactionSource),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = "Play/Pause",
            tint = Color(0xFF0D0F1E),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun PlayerSmallIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(size + 14.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .pressBounce(interactionSource),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(size))
    }
}

@Composable
private fun PlayerUtilityIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    Box(
        modifier = Modifier
            .size(size + 10.dp)
            .clip(CircleShape)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon, null,
            tint = if (isHovered) Color(0xFFAEB4C8) else tint,
            modifier = Modifier.size(size)
        )
    }
}

@Composable
private fun PlayerThinSeekBar(
    positionMs: Long,
    durationMs: Long,
    onSeekFraction: (Float) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableStateOf(0f) }

    val safeFraction = if (durationMs > 0)
        (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    val displayFraction = if (isDragging) dragFraction else safeFraction

    Box(modifier = modifier.height(14.dp), contentAlignment = Alignment.Center) {
        // Inactive track
        Box(
            modifier = Modifier
                .fillMaxWidth().height(2.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(Color.White.copy(alpha = 0.10f))
        )
        // Active filled portion
        if (displayFraction > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(displayFraction).height(2.dp)
                    .align(Alignment.CenterStart)
                    .clip(RoundedCornerShape(99.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF6B7BFF), Color(0xFF7E72FF))
                        )
                    )
            )
        }
        // Thumb dot
        Box(modifier = Modifier.fillMaxWidth().align(Alignment.CenterStart)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(displayFraction)
                    .align(Alignment.CenterStart)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7E72FF))
                        .align(Alignment.CenterEnd)
                )
            }
        }
        // Invisible functional slider overlaid for interaction
        OmniProgressSlider(
            fraction = displayFraction,
            modifier = Modifier.fillMaxWidth().alpha(0f),
            onSeek = { f -> isDragging = true; dragFraction = f },
            onSeekFinished = {
                isDragging = false
                onSeekFraction(dragFraction)
            },
            enabled = enabled && durationMs > 0,
        )
    }
}

@Composable
private fun PlayerThinSlider(
    fraction: Float,
    onFractionChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.height(14.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .fillMaxWidth().height(2.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(Color.White.copy(alpha = 0.10f))
        )
        if (fraction > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction).height(2.dp)
                    .align(Alignment.CenterStart)
                    .clip(RoundedCornerShape(99.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF6B7BFF), Color(0xFF7E72FF))
                        )
                    )
            )
        }
        // Thumb
        Box(modifier = Modifier.fillMaxWidth().align(Alignment.CenterStart)) {
            Box(modifier = Modifier.fillMaxWidth(fraction).align(Alignment.CenterStart)) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7E72FF))
                        .align(Alignment.CenterEnd)
                )
            }
        }
        // Invisible real Material slider
        Slider(
            value = fraction,
            onValueChange = onFractionChange,
            modifier = Modifier.fillMaxWidth().alpha(0f),
        )
    }
}

@Composable
private fun TimeLabel(ms: Long) {
    val totalSec = ms / 1000
    val m = totalSec / 60
    val s = totalSec % 60
    Text(
        text = "$m:${s.toString().padStart(2, '0')}",
        color = Color(0xFF727A93),
        fontSize = 10.sp,
        maxLines = 1,
        fontWeight = FontWeight.Normal,
    )
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
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .pressBounce(interactionSource),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = if (enabled) tint else tint.copy(alpha = 0.35f), modifier = Modifier.size(size))
    }
}
