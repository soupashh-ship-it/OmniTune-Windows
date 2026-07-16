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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omnitune.innertube.models.SongItem

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
            PlayerSmallIcon(icon = Icons.Filled.Shuffle, contentDescription = "Shuffle",
                tint = if (shuffleEnabled) iconActive else iconMuted,
                enabled = enabled,
                onClick = onShuffleClick,
                size = 15.dp,
                tooltip = "Shuffle",
                tag = "omni.player.shuffle",
            )
            PlayerSmallIcon(icon = Icons.Filled.SkipPrevious, contentDescription = "Previous track",
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
            PlayerSmallIcon(icon = Icons.Filled.SkipNext, contentDescription = "Next track",
                tint = if (enabled) iconBright else iconMuted.copy(alpha = 0.4f),
                enabled = enabled,
                onClick = onNextClick,
                size = 19.dp,
                tooltip = "Next",
                tag = "omni.player.next",
            )
            PlayerSmallIcon(icon = if (repeatMode == com.omnitune.app.player.RepeatMode.ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat, contentDescription = "Repeat",
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
internal fun PlayerPlayPauseButton(
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
internal fun PlayerSmallIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
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
            Icon(icon, contentDescription = contentDescription,
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
internal fun PlayerUtilityIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
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
            Icon(icon, contentDescription = contentDescription,
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
internal fun TransportIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
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
            Icon(icon, contentDescription = contentDescription, tint = if (enabled) tint else tint.copy(alpha = 0.35f), modifier = Modifier.size(size))
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
