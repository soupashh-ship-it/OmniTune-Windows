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
            contentDescription = "Open queue",
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
