@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

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
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.ui.platform.testTag
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
    val metrics = LocalHomeReferenceMetrics.current

    val artworkSize = metrics.px(49f)

    TargetBottomPlayerSurface(modifier = modifier) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            PlayerLeftZone(
                currentSong = currentSong,
                liked = liked,
                player = player,
                artworkSize = artworkSize,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = metrics.px(13f))
                    .width(metrics.px(330f)),
            )

            PlayerControlBand(
                isPlaying = playbackState == PlaybackState.PLAYING,
                positionMs = position.timeMs,
                durationMs = position.lengthMs,
                shuffleEnabled = shuffle,
                repeatMode = repeatMode,
                currentSong = currentSong,
                onShuffleClick = { player.toggleShuffle() },
                onPreviousClick = { player.previousTrack() },
                onPlayPauseClick = { player.togglePlayPause() },
                onNextClick = { player.nextTrack() },
                onRepeatClick = { player.cycleRepeat() },
                onSeekFraction = { f ->
                    if (position.lengthMs > 0) player.seek((f * position.lengthMs).toLong())
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(metrics.px(420f)),
            )

            PlayerRightZone(
                volume = volume,
                onVolumeChange = { player.setVolume(it) },
                onQueueClick = { player.navigateTo(com.omnitune.app.player.NavScreen.Queue) },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = metrics.px(16f)),
            )
        }
    }
}

@Composable
private fun PlayerLeftZone(
    currentSong: SongItem?,
    liked: Set<String>,
    player: PlayerViewModel,
    artworkSize: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
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
                Modifier
                    .weight(1f)
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
            TransportIcon(Icons.Default.MoreHoriz, tint = Color(0xFFA9AEC2), onClick = { player.navigateTo(com.omnitune.app.player.NavScreen.Queue) }, size = 18.dp)
        } else {
            Box(
                modifier = Modifier
                    .size(artworkSize)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF0B1328),
                                Color(0xFF171238),
                                Color(0xFF090F21),
                            )
                        )
                    )
                    .border(1.dp, OmniReferenceColors.PlayerBorder.copy(alpha = 0.70f), RoundedCornerShape(6.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Box(
                    modifier = Modifier.width(100.dp).height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1A2038).copy(alpha = 0.74f))
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier.width(60.dp).height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF171D32).copy(alpha = 0.62f))
                )
            }
        }
    }
}

@Composable
private fun PlayerRightZone(
    volume: Int,
    onVolumeChange: (Int) -> Unit,
    onQueueClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconMuted = Color(0xFF6D748D)

    Row(
        modifier = modifier.height(28.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PlayerUtilityIcon(
            icon = Icons.AutoMirrored.Filled.QueueMusic,
            tint = iconMuted,
            onClick = onQueueClick,
            size = 16.dp,
            tooltip = "Queue & Session"
        )
        Icon(
            if (volume == 0) Icons.AutoMirrored.Filled.VolumeOff
            else Icons.AutoMirrored.Filled.VolumeUp,
            contentDescription = "Volume",
            tint = iconMuted,
            modifier = Modifier.size(16.dp)
        )
        PlayerThinSlider(
            fraction = (volume / 100f).coerceIn(0f, 1f),
            onFractionChange = { onVolumeChange((it * 100).toInt()) },
            modifier = Modifier.width(80.dp),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PlayerControlBand — exact reproduction of 607×41 reference
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun PlayerControlBand(
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    shuffleEnabled: Boolean,
    repeatMode: com.omnitune.app.player.RepeatMode,
    currentSong: SongItem?,
    onShuffleClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onSeekFraction: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val enabled = currentSong != null
    val iconMuted = Color(0xFF6D748D)
    val iconActive = Color(0xFF7C6DFF)
    val iconBright = Color(0xFFAEB4C8)
    val metrics = LocalHomeReferenceMetrics.current

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            PlayerSmallIcon(
                icon = Icons.Filled.Shuffle,
                tint = if (shuffleEnabled) iconActive else iconMuted,
                enabled = enabled,
                onClick = onShuffleClick,
                size = 15.dp,
                tooltip = "Shuffle",
                tag = "omni.player.shuffle",
            )
            PlayerSmallIcon(
                icon = Icons.Filled.SkipPrevious,
                tint = if (enabled) iconBright else iconMuted.copy(alpha = 0.4f),
                enabled = enabled,
                onClick = onPreviousClick,
                size = 19.dp,
                tooltip = "Previous",
                tag = "omni.player.previous",
            )
            PlayerPlayPauseButton(
                isPlaying = isPlaying,
                enabled = enabled,
                onClick = onPlayPauseClick,
                tooltip = "Play / Pause",
                tag = "omni.player.playPause",
            )
            PlayerSmallIcon(
                icon = Icons.Filled.SkipNext,
                tint = if (enabled) iconBright else iconMuted.copy(alpha = 0.4f),
                enabled = enabled,
                onClick = onNextClick,
                size = 19.dp,
                tooltip = "Next",
                tag = "omni.player.next",
            )
            PlayerSmallIcon(
                icon = if (repeatMode == com.omnitune.app.player.RepeatMode.ONE)
                    Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                tint = if (repeatMode != com.omnitune.app.player.RepeatMode.OFF) iconActive else iconMuted,
                enabled = enabled,
                onClick = onRepeatClick,
                size = 15.dp,
                tooltip = "Repeat",
                tag = "omni.player.repeat",
            )
        }

        Spacer(Modifier.height(5.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TimeLabel(
                ms = positionMs,
                modifier = Modifier.width(metrics.px(26f)),
                textAlign = TextAlign.End,
            )
            Spacer(Modifier.width(metrics.px(10f)))
            PlayerThinSeekBar(
                positionMs = positionMs,
                durationMs = durationMs,
                onSeekFraction = onSeekFraction,
                enabled = enabled,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(metrics.px(10f)))
            TimeLabel(
                ms = durationMs,
                modifier = Modifier.width(metrics.px(26f)),
                textAlign = TextAlign.Start,
            )
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
    tooltip: String? = null,
    tag: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val metrics = LocalHomeReferenceMetrics.current
    val box = @Composable {
        Box(
            modifier = Modifier
                .size(metrics.px(31f))
                .then(if (tag != null) Modifier.testTag(tag) else Modifier)
                .clip(CircleShape)
                .background(
                    if (!enabled) com.omnitune.app.window.OmniReferenceColors.PlayerPrimaryControl.copy(alpha = 0.4f)
                    else if (isHovered) Color.White
                    else com.omnitune.app.window.OmniReferenceColors.PlayerPrimaryControl
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
                tint = com.omnitune.app.window.OmniReferenceColors.PlayerPrimaryControlIcon,
                modifier = Modifier.size(metrics.px(18f))
            )
        }
    }
    if (tooltip != null) {
        androidx.compose.foundation.TooltipArea(
            tooltip = {
                Box(modifier = Modifier.background(Color(0xE60A0C16), RoundedCornerShape(4.dp)).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(tooltip, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            },
            delayMillis = 400
        ) { box() }
    } else box()
}

@Composable
private fun PlayerSmallIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp,
    tooltip: String? = null,
    tag: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val metrics = LocalHomeReferenceMetrics.current
    val box = @Composable {
        Box(
            modifier = Modifier
                .size(metrics.px(28f))
                .then(if (tag != null) Modifier.testTag(tag) else Modifier)
                .clip(CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                null,
                tint = if (isHovered && enabled) Color.White else tint,
                modifier = Modifier.size(size)
            )
        }
    }
    if (tooltip != null) {
        androidx.compose.foundation.TooltipArea(
            tooltip = {
                Box(modifier = Modifier.background(Color(0xE60A0C16), RoundedCornerShape(4.dp)).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(tooltip, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            },
            delayMillis = 400
        ) { box() }
    } else box()
}

@Composable
private fun PlayerUtilityIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp,
    tooltip: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val box = @Composable {
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
    if (tooltip != null) {
        androidx.compose.foundation.TooltipArea(
            tooltip = {
                Box(modifier = Modifier.background(Color(0xE60A0C16), RoundedCornerShape(4.dp)).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(tooltip, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            },
            delayMillis = 400
        ) { box() }
    } else box()
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
private fun TimeLabel(
    ms: Long,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
) {
    val totalSec = ms / 1000
    val m = totalSec / 60
    val s = totalSec % 60
    Text(
        text = "$m:${s.toString().padStart(2, '0')}",
        color = Color(0xFF727A93),
        fontSize = 10.sp,
        maxLines = 1,
        fontWeight = FontWeight.Normal,
        textAlign = textAlign,
        modifier = modifier,
    )
}

@Composable
private fun TransportIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit,
    enabled: Boolean = true,
    size: androidx.compose.ui.unit.Dp = 22.dp,
    tooltip: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val box = @Composable {
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

    if (tooltip != null) {
        androidx.compose.foundation.TooltipArea(
            tooltip = {
                Box(
                    modifier = Modifier
                        .background(Color(0xE60A0C16), RoundedCornerShape(4.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(tooltip, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            },
            delayMillis = 400
        ) {
            box()
        }
    } else {
        box()
    }
}


@Composable
fun TargetBottomPlayerSurface(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier.border(
            width = 1.dp,
            color = OmniReferenceColors.PlayerBorder.copy(alpha = 0.90f),
            shape = shape
        )
    ) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier.matchParentSize()
        ) {
            val cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                16.dp.toPx(),
                16.dp.toPx()
            )

            // BASE
            drawRoundRect(
                color = OmniReferenceColors.PlayerBase,
                cornerRadius = cornerRadius
            )

            // VERY WEAK FAR-LEFT BLUE DEPTH
            val leftBlueCenter = Offset(
                x = size.width * 0.08f,
                y = size.height * 0.52f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.00f to Color(0xFF12345A).copy(alpha = 0.06f),
                        1.00f to Color.Transparent
                    ),
                    center = leftBlueCenter,
                    radius = size.width * 0.16f
                ),
                center = leftBlueCenter,
                radius = size.width * 0.16f
            )

            // RESTRAINED LEFT-MIDDLE MAGENTA/VIOLET
            val leftVioletCenter = Offset(
                x = size.width * 0.32f,
                y = size.height * 0.53f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.00f to OmniReferenceColors.PlayerVioletStrong.copy(alpha = 0.28f),
                        0.42f to OmniReferenceColors.PlayerViolet.copy(alpha = 0.16f),
                        1.00f to Color.Transparent
                    ),
                    center = leftVioletCenter,
                    radius = size.width * 0.28f
                ),
                center = leftVioletCenter,
                radius = size.width * 0.28f
            )

            // TARGET RIGHT-SIDE VIOLET AROUND 75–80%
            val rightVioletCenter = Offset(
                x = size.width * 0.70f,
                y = size.height * 0.54f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.00f to Color(0xFF37153E).copy(alpha = 0.32f),
                        0.45f to Color(0xFF281944).copy(alpha = 0.12f),
                        1.00f to Color.Transparent
                    ),
                    center = rightVioletCenter,
                    radius = size.width * 0.24f
                ),
                center = rightVioletCenter,
                radius = size.width * 0.24f
            )

            // FAR-RIGHT SUBTLE VIOLET DEPTH
            val farRightCenter = Offset(
                x = size.width * 0.96f,
                y = size.height * 0.58f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.00f to Color(0xFF302058).copy(alpha = 0.08f),
                        1.00f to Color.Transparent
                    ),
                    center = farRightCenter,
                    radius = size.width * 0.15f
                ),
                center = farRightCenter,
                radius = size.width * 0.15f
            )
        }

        content()
    }
}
