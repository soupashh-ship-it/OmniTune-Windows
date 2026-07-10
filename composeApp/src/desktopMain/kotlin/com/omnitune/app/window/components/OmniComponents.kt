package com.omnitune.app.window.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.ui.draw.shadow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import coil3.compose.AsyncImage
import com.omnitune.app.window.*
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.toHighResThumbnail

// ---------------------------------------------------------------------------
// Surfaces
// ---------------------------------------------------------------------------

@Composable
fun OmniSurface(
    modifier: Modifier = Modifier,
    shape: Shape = Shapes.medium,
    color: Color = Surface1,
    border: Boolean = true,
    elevation: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    Surface(
        modifier = modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = shape,
        color = color,
        shadowElevation = elevation,
        border = if (border) BorderStroke(1.dp, BorderLow) else null,
        content = { Box(Modifier.fillMaxSize(), content = content) },
    )
}

// ---------------------------------------------------------------------------
// Buttons
// ---------------------------------------------------------------------------

@Composable
fun OmniIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentDescription: String?,
    tint: Color = TextPrimary,
    enabled: Boolean = true,
    size: Dp = 36.dp,
    iconSize: Dp = 20.dp,
    background: Color? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(if (background != null) Modifier.background(background) else Modifier)
            .clickable(interactionSource = interactionSource, indication = null, enabled = enabled, onClick = onClick)
            .pressBounce(interactionSource),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = if (enabled) tint else TextMuted, modifier = Modifier.size(iconSize))
    }
}

@Composable
fun OmniPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    leadingIcon: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .clip(Shapes.small)
            .background(if (enabled) OmniGradients.irisToLavender else Brush.linearGradient(listOf(TextMuted.copy(alpha = 0.25f), TextMuted.copy(alpha = 0.25f))))
            .clickable(interactionSource = interactionSource, indication = null, enabled = enabled, onClick = onClick)
            .pressBounce(interactionSource)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (icon != null && leadingIcon) {
                Icon(icon, null, tint = Color(0xFF05060A), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(text, color = Color(0xFF05060A), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
fun OmniSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .clip(Shapes.small)
            .background(Surface3.copy(alpha = 0.6f))
            .border(1.dp, BorderLow, Shapes.small)
            .clickable(interactionSource = interactionSource, indication = null, enabled = enabled, onClick = onClick)
            .pressBounce(interactionSource)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = if (enabled) TextPrimary else TextMuted, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.titleSmall)
    }
}

// ---------------------------------------------------------------------------
// Section header
// ---------------------------------------------------------------------------

@Composable
fun OmniSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.weight(1f))
        if (actionLabel != null && onAction != null) {
            val interactionSource = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .clip(Shapes.pill)
                    .clickable(interactionSource = interactionSource, indication = null, onClick = onAction)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(actionLabel, color = IrisSoft, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Search field
// ---------------------------------------------------------------------------

@Composable
fun OmniSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search OmniTune",
    hint: String? = "Ctrl K",
    onEnter: (() -> Unit)? = null,
    onEscape: (() -> Unit)? = null,
    focusRequester: FocusRequester? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    var isFocused by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        if (isFocused) Iris.copy(alpha = 0.55f) else BorderLow,
        tween(160),
    )
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .clip(Shapes.small)
            .background(Surface2)
            .border(1.dp, borderColor, Shapes.small)
            .onFocusChanged { isFocused = it.isFocused }
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .onKeyEvent { ev ->
                if (ev.type == androidx.compose.ui.input.key.KeyEventType.KeyDown) {
                    when (ev.key) {
                        Key.Enter -> { onEnter?.invoke(); true }
                        Key.Escape -> { onEscape?.invoke(); true }
                        else -> false
                    }
                } else false
            }
            .padding(horizontal = 14.dp),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
        cursorBrush = Brush.verticalGradient(listOf(Iris, Violet)),
        decorationBox = { inner ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (leadingIcon != null) leadingIcon() else Icon(Icons.Default.Search, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(placeholder, color = TextMuted, style = MaterialTheme.typography.bodyLarge)
                    }
                    inner()
                }
                if (trailingIcon != null) trailingIcon()
                else if (hint != null && !isFocused) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(Shapes.small)
                            .background(Surface3.copy(alpha = 0.7f))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(hint, color = TextMuted, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        },
    )
}

// ---------------------------------------------------------------------------
// Media cards (album / playlist / artist)
// ---------------------------------------------------------------------------

@Composable
fun OmniMediaCard(
    title: String,
    subtitle: String?,
    artworkUrl: String?,
    modifier: Modifier = Modifier,
    shape: Shape = Shapes.artworkMedium,
    showPlayOnHover: Boolean = true,
    onClick: () -> Unit = {},
    onPlay: () -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    Column(
        modifier = modifier
            .clip(Shapes.medium)
            .background(Surface1)
            .border(1.dp, BorderLow, Shapes.medium)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(Spacing.small),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(shape),
        ) {
            AsyncImage(
                model = artworkUrl?.toHighResThumbnail(),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            if (showPlayOnHover) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = if (isHovered) 0.25f else 0f)),
                    contentAlignment = Alignment.BottomEnd,
                ) {
                    if (isHovered) {
                        val playInteraction = remember { MutableInteractionSource() }
                        Box(
                            modifier = Modifier
                                .padding(12.dp)
                                .size(44.dp)
                                .shadow(8.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.4f), spotColor = Iris.copy(alpha = 0.3f))
                                .clip(CircleShape)
                                .background(OmniGradients.irisToLavender)
                                .clickable(interactionSource = playInteraction, indication = null) { onPlay() }
                                .pressBounce(playInteraction),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.PlayArrow, null, tint = Color(0xFF05060A), modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (subtitle != null) {
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Song row (reusable across search / playlist / album / queue)
// ---------------------------------------------------------------------------

@Composable
fun OmniSongRow(
    item: SongItem,
    isActive: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: String? = item.duration?.let { "${it / 60}:${(it % 60).toString().padStart(2, '0')}" },
    showIndex: Boolean = false,
    index: Int = 0,
    onPlayNext: (() -> Unit)? = null,
    onAddToQueue: (() -> Unit)? = null,
    onLike: (() -> Unit)? = null,
    onOverflow: (() -> Unit)? = null,
) {
    val artists = item.artists.joinToString(", ") { it.name }
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val bg by animateColorAsState(
        when {
            isActive -> Iris.copy(alpha = 0.10f)
            isHovered -> Surface3.copy(alpha = 0.55f)
            else -> Color.Transparent
        },
        tween(140),
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(Shapes.small)
            .background(bg)
            .hoverable(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .pressBounce(interactionSource)
            .padding(horizontal = Spacing.small, vertical = Spacing.compact),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp)) {
            if (showIndex && !isActive) {
                Text("${index + 1}", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
            } else {
                AsyncImage(
                    model = item.thumbnail.toHighResThumbnail(),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(Shapes.artworkSmall),
                    contentScale = ContentScale.Crop,
                )
                if (isActive) {
                    Box(
                        Modifier.size(40.dp).clip(Shapes.artworkSmall).background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        PlayingIndicatorBox(isActive = true, playWhenReady = isPlaying, color = IrisSoft)
                    }
                }
            }
        }
        Spacer(Modifier.width(Spacing.small))
        Column(Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.titleSmall, color = if (isActive) IrisSoft else TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (artists.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(artists, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        trailing?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = TextMuted, modifier = Modifier.padding(horizontal = Spacing.small))
        }
        if (isHovered) {
            Row {
                onPlayNext?.let { OmniIconButton(it, icon = Icons.AutoMirrored.Filled.QueueMusic, contentDescription = "Play next", size = 32.dp, iconSize = 18.dp, tint = TextSecondary) }
                onAddToQueue?.let { OmniIconButton(it, icon = Icons.Default.Add, contentDescription = "Add to queue", size = 32.dp, iconSize = 18.dp, tint = TextSecondary) }
                onLike?.let { OmniIconButton(it, icon = Icons.Default.FavoriteBorder, contentDescription = "Like", size = 32.dp, iconSize = 18.dp, tint = TextSecondary) }
                onOverflow?.let { OmniIconButton(it, icon = Icons.Default.MoreHoriz, contentDescription = "More", size = 32.dp, iconSize = 18.dp, tint = TextSecondary) }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Empty / loading states
// ---------------------------------------------------------------------------

@Composable
fun OmniEmptyState(
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Icon(icon, null, tint = TextMuted, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(16.dp))
        }
        Text(title, style = MaterialTheme.typography.headlineSmall, color = TextSecondary, textAlign = TextAlign.Center)
        if (description != null) {
            Spacer(Modifier.height(8.dp))
            Text(description, style = MaterialTheme.typography.bodyLarge, color = TextMuted, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun OmniShimmerBlock(
    modifier: Modifier = Modifier,
    shape: Shape = Shapes.small,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(Surface3.copy(alpha = 0.5f)),
    )
}
