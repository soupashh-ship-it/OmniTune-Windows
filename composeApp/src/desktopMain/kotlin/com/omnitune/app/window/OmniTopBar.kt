package com.omnitune.app.window

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.omnitune.app.window.components.OmniIconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.omnitune.app.player.NavScreen
import com.omnitune.app.window.components.OmniSearchField

@Composable
fun OmniTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    canGoBack: Boolean,
    canGoForward: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit,
    focusRequester: FocusRequester? = null,
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    onMinimize: () -> Unit = {},
    onMaximize: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onSearchFocusChanged: (Boolean) -> Unit = {},
) {
    val metrics = LocalHomeReferenceMetrics.current
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = OmniReferenceColors.BorderSoft.copy(alpha = 0.72f),
                    start = androidx.compose.ui.geometry.Offset(0f, size.height - 1f),
                    end = androidx.compose.ui.geometry.Offset(size.width, size.height - 1f),
                    strokeWidth = 1f,
                )
            }
    ) {
        val searchStartX = metrics.px(195f)
        val searchWidth = metrics.px(515f)

        // BACK / FORWARD
        Row(
            modifier = Modifier.offset(x = metrics.px(27f)).align(Alignment.CenterStart),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TargetHeaderNavigationButton(
                enabled = canGoBack,
                onClick = onBack,
                icon = Icons.Default.ChevronLeft,
                contentDescription = "Back"
            )
            TargetHeaderNavigationButton(
                enabled = canGoForward,
                onClick = onForward,
                icon = Icons.Default.ChevronRight,
                contentDescription = "Forward"
            )
        }

        // SEARCH
        Box(
            modifier = Modifier
                .offset(x = searchStartX)
                .align(Alignment.CenterStart)
        ) {
            OmniSearchField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .testTag("omni.topbar.globalSearch")
                    .width(searchWidth)
                    .height(metrics.px(28f)),
                focusRequester = focusRequester,
                onEnter = {
                    onNavigateToSearch()
                    onSearch(query)
                },
                onEscape = { onQueryChange("") },
                onFocusChanged = onSearchFocusChanged,
            )
        }

        // PROFILE & WINDOW CONTROLS
        Row(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OmniIconButton(
                onClick = onOpenSettings,
                icon = Icons.Default.Notifications,
                contentDescription = "Notifications",
                size = 32.dp,
                iconSize = 18.dp
            )
            Spacer(Modifier.width(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onOpenSettings() }
                    .padding(4.dp)
            ) {
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=100&q=80",
                    contentDescription = "Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Surface2)
                        .border(1.dp, BorderLow, CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ExpandMore, contentDescription = "More", tint = TextSecondary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(20.dp))
            WindowControlButton(WindowControlGlyph.Minimize, onMinimize)
            WindowControlButton(WindowControlGlyph.Maximize, onMaximize)
            WindowControlButton(WindowControlGlyph.Close, onClose, close = true)
        }
    }
}

@Composable
private fun TargetHeaderNavigationButton(
    enabled: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val animatedBackground by animateColorAsState(
        targetValue = when {
            !enabled -> Color(0xFF050A16)
            isHovered -> Color(0xFF0A1128)
            else -> Color(0xFF080E1D)
        },
        animationSpec = tween(
            durationMillis = 140
        ),
        label = "headerNavBackground"
    )

    val iconColor = if (enabled) {
        Color(0xFF989CAB)
    } else {
        Color(0xFF56596B)
    }

    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(animatedBackground)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(10.dp),
            tint = iconColor
        )
    }
}


private enum class WindowControlGlyph { Minimize, Maximize, Close }

@Composable
private fun WindowControlButton(glyph: WindowControlGlyph, onClick: () -> Unit, close: Boolean = false) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val background by animateColorAsState(
        targetValue = when {
            close && isHovered -> Color(0x22D65A62)
            isHovered -> Color.White.copy(alpha = 0.045f)
            else -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 110),
        label = "windowControlBackground",
    )
    val strokeColor by animateColorAsState(
        targetValue = when {
            close && isHovered -> Color(0xFFE3B3AD)
            isHovered -> Color(0xFFD7D0C2)
            else -> Color(0xFFB3AA9A)
        },
        animationSpec = tween(durationMillis = 110),
        label = "windowControlStroke",
    )
    Box(
        modifier = Modifier
            .width(40.dp)
            .height(34.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(background)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(16.dp)) {
            val strokeWidth = 1.35.dp.toPx()
            val cx = size.width / 2f
            val cy = size.height / 2f
            when (glyph) {
                WindowControlGlyph.Minimize -> {
                    drawLine(
                        color = strokeColor,
                        start = androidx.compose.ui.geometry.Offset(cx - 5.3.dp.toPx(), cy + 0.25.dp.toPx()),
                        end = androidx.compose.ui.geometry.Offset(cx + 5.3.dp.toPx(), cy + 0.25.dp.toPx()),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round,
                    )
                }
                WindowControlGlyph.Maximize -> {
                    val side = 10.5.dp.toPx()
                    drawRoundRect(
                        color = strokeColor,
                        topLeft = androidx.compose.ui.geometry.Offset(cx - side / 2f, cy - side / 2f),
                        size = Size(side, side),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.8.dp.toPx(), 1.8.dp.toPx()),
                        style = Stroke(width = strokeWidth),
                    )
                }
                WindowControlGlyph.Close -> {
                    val d = 5.0.dp.toPx()
                    drawLine(
                        color = strokeColor,
                        start = androidx.compose.ui.geometry.Offset(cx - d, cy - d),
                        end = androidx.compose.ui.geometry.Offset(cx + d, cy + d),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        color = strokeColor,
                        start = androidx.compose.ui.geometry.Offset(cx + d, cy - d),
                        end = androidx.compose.ui.geometry.Offset(cx - d, cy + d),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round,
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderNavigationButton(
    enabled: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(
                Color(0xFF0B1022).copy(alpha = 0.76f)
            )
            .clickable(
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
