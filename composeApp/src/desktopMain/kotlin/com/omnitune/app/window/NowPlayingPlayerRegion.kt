package com.omnitune.app.window

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.*
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
import com.omnitune.app.platform.PlayerPosition
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.app.platform.PlaybackState
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.toHighResThumbnail
import com.omnitune.app.window.components.OmniProgressSlider

@Composable
internal fun PlayerRegion(
    modifier: Modifier,
    player: PlayerViewModel,
    currentSong: SongItem,
    playbackState: PlaybackState,
    pos: PlayerPosition,
    displayTimeMs: Long,
    displayPos: Float,
    volume: Int,
    repeat: com.omnitune.app.player.RepeatMode,
    shuffle: Boolean,
    isDragging: Boolean,
    onSliderChange: (Float) -> Unit,
    onSliderFinish: () -> Unit,
) {
    var actionMessage by remember(currentSong.id) { mutableStateOf<String?>(null) }
    val likedIds by player.likedSongs.collectAsState()
    val isLiked = currentSong.id in likedIds

    val metrics = LocalHomeReferenceMetrics.current
    val motionPolicy = LocalOmniMotionPolicy.current

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .offset(y = metrics.px(0f))
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterStart,
        ) {
            NowPlayingIndicator(isPlaying = playbackState == PlaybackState.PLAYING)
        }

        // Artwork — primary visual anchor
        val artScale by animateFloatAsState(
            targetValue = if (motionPolicy.reduced || playbackState == PlaybackState.PLAYING) 1f else 0.94f,
            animationSpec = if (motionPolicy.reduced) tween(motionPolicy.shortDurationMs) else spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
            label = "artScale",
        )
        Box(
            modifier = Modifier
                .offset(y = metrics.px(39f))
                .fillMaxWidth()
                .height(metrics.px(313f))
                .scale(artScale)
                .shadow(
                    elevation = metrics.px(16f),
                    shape = Shapes.artworkLarge,
                    ambientColor = Color.Black.copy(alpha = 0.35f),
                    spotColor = Iris.copy(alpha = 0.18f),
                )
                .clip(Shapes.artworkLarge)
                .background(Surface1),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = currentSong.thumbnail.toHighResThumbnail(),
                contentDescription = "Album artwork",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )

            NpIconButton(
                icon = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                tooltip = if (isLiked) "Unlike" else "Like",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = metrics.px(13f), end = metrics.px(13f)),
                tint = if (isLiked) Color(0xFFFF4F7A) else TextPrimary,
                background = Surface2.copy(alpha = 0.62f),
            ) {
                player.toggleLike(currentSong.id)
                actionMessage = if (isLiked) "Removed from liked songs." else "Added to liked songs."
            }
        }

        // Title and actions
        Row(
            modifier = Modifier
                .offset(y = metrics.px(360f))
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                Text(
                    currentSong.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    currentSong.artists.joinToString(", ") { it.name },
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NpIconButton(icon = Icons.Default.AddCircleOutline, tooltip = "Play next") {
                    player.playNext(currentSong)
                    actionMessage = "Added to play next."
                }
                NpIconButton(icon = Icons.Default.MoreHoriz, tooltip = "More options") {
                    player.navigateTo(com.omnitune.app.player.NavScreen.Queue)
                    actionMessage = "Opened Queue & Session."
                }
            }
        }


        actionMessage?.let {
            Text(
                it,
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .offset(y = metrics.px(404f))
                    .fillMaxWidth(),
            )
        }

        // Progress-derived visualization. This is intentionally not audio-reactive.
        PlaybackProgressVisualizer(
            progress = displayPos,
            modifier = Modifier
                .offset(y = metrics.px(404f))
                .fillMaxWidth()
                .height(metrics.px(28f)),
        )

        // Timeline: position + slider + duration
        Row(
            modifier = Modifier
                .offset(y = metrics.px(444f))
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                formatTime(displayTimeMs),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.width(40.dp),
            )
            OmniProgressSlider(
                fraction = displayPos,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                enabled = pos.lengthMs > 0,
                onSeek = onSliderChange,
                onSeekFinished = onSliderFinish,
            )
            val remaining = if (pos.lengthMs > 0) pos.lengthMs - displayTimeMs else 0L
            Text(
                if (pos.lengthMs > 0) "-${formatTime(remaining)}" else "–:––",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.End,
            )
        }

        // Transport controls: Shuffle | Prev | Play/Pause | Next | Repeat
        Row(
            modifier = Modifier
                .offset(y = metrics.px(476f))
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Shuffle
            val shuffleInteraction = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(interactionSource = shuffleInteraction, indication = null) { player.toggleShuffle() }
                    .pressBounce(shuffleInteraction),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Shuffle,
                    "Shuffle",
                    tint = if (shuffle) IrisSoft else TextSecondary,
                    modifier = Modifier.size(22.dp),
                )
                if (shuffle) {
                    Box(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 2.dp)
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(IrisSoft)
                    )
                }
            }

            Spacer(Modifier.width(24.dp))

            // Previous
            TransportButton(icon = Icons.Default.SkipPrevious, contentDescription = "Previous track", size = 44.dp) { player.previousTrack() }

            Spacer(Modifier.width(20.dp))

            // Central Play/Pause
            val playInteraction = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(OmniGradients.irisToLavender)
                    .clickable(interactionSource = playInteraction, indication = null) { player.togglePlayPause() }
                    .pressBounce(playInteraction, pressedScale = 0.93f),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (playbackState == PlaybackState.PLAYING) Icons.Default.Pause else Icons.Default.PlayArrow,
                    "Play/Pause",
                    tint = Color(0xFF05060A),
                    modifier = Modifier.size(32.dp),
                )
            }

            Spacer(Modifier.width(20.dp))

            // Next
            TransportButton(icon = Icons.Default.SkipNext, contentDescription = "Next track", size = 44.dp) { player.nextTrack() }

            Spacer(Modifier.width(24.dp))

            // Repeat
            val repeatInteraction = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(interactionSource = repeatInteraction, indication = null) { player.cycleRepeat() }
                    .pressBounce(repeatInteraction),
                contentAlignment = Alignment.Center,
            ) {
                val repeatIcon = when (repeat) {
                    com.omnitune.app.player.RepeatMode.ONE -> Icons.Default.RepeatOne
                    else -> Icons.Default.Repeat
                }
                val repeatActive = repeat != com.omnitune.app.player.RepeatMode.OFF
                Icon(
                    repeatIcon,
                    "Repeat",
                    tint = if (repeatActive) IrisSoft else TextSecondary,
                    modifier = Modifier.size(22.dp),
                )
                if (repeatActive) {
                    Box(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 2.dp)
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(IrisSoft)
                    )
                }
            }
        }
    }
}

@Composable
internal fun PlaybackProgressVisualizer(progress: Float, modifier: Modifier) {
    val barCount = 96
    val clampedProgress = progress.coerceIn(0f, 1f)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        repeat(barCount) { index ->
            val base = (((index * 37) % 10) + 4) / 14f
            val position = (index + 1).toFloat() / barCount.toFloat()
            val isElapsed = position <= clampedProgress
            val fraction = if (isElapsed) base else 0.14f
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height((fraction * 28f).coerceAtLeast(3f).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = if (isElapsed) {
                                listOf(Color(0xFFFF4FD8).copy(alpha = 0.80f), IrisSoft.copy(alpha = 0.42f))
                            } else {
                                listOf(Iris.copy(alpha = 0.28f), Surface3.copy(alpha = 0.46f))
                            }
                        )
                    ),
            )
        }
    }
}

@Composable
internal fun TransportButton(icon: ImageVector, contentDescription: String?, size: Dp, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .pressBounce(interaction),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = TextWhite, modifier = Modifier.size(size * 0.65f))
    }
}

@Composable
internal fun NpIconButton(
    icon: ImageVector,
    tooltip: String,
    modifier: Modifier = Modifier,
    tint: Color = TextSecondary,
    background: Color = Color.Transparent,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .pressBounce(interaction),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = tooltip, tint = tint, modifier = Modifier.size(22.dp))
    }
}

internal fun formatTime(ms: Long): String {
    if (ms < 0) return "0:00"
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}
