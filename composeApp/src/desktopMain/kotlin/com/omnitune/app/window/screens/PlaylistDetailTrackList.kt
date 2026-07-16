package com.omnitune.app.window.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.omnitune.app.platform.SavedQueuePlaylist
import com.omnitune.app.platform.QaRuntime
import com.omnitune.app.player.NavScreen
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.app.platform.PlaybackState
import com.omnitune.app.service.YouTubeService
import com.omnitune.app.window.BorderLow
import com.omnitune.app.window.IrisSoft
import com.omnitune.app.window.LocalHomeReferenceMetrics
import com.omnitune.app.window.OmniGradients
import com.omnitune.app.window.OmniReferenceColors
import com.omnitune.app.window.Surface1
import com.omnitune.app.window.TextMuted
import com.omnitune.app.window.TextPrimary
import com.omnitune.app.window.TextSecondary
import com.omnitune.app.window.components.OmniEmptyState
import com.omnitune.app.window.components.OmniShimmerBlock
import com.omnitune.innertube.models.Artist
import com.omnitune.innertube.models.PlaylistItem
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.pages.PlaylistPage
import com.omnitune.innertube.toHighResThumbnail
import org.koin.compose.koinInject



@Composable
internal fun PlaylistTrackHeader() {
    val metrics = LocalHomeReferenceMetrics.current
    Row(Modifier.fillMaxWidth().height(metrics.px(24f)).padding(horizontal = metrics.px(8f)), verticalAlignment = Alignment.CenterVertically) {
        Text("#", color = TextMuted, fontSize = 8.sp, modifier = Modifier.width(metrics.px(34f)))
        Text("TITLE", color = TextMuted, fontSize = 8.sp, modifier = Modifier.weight(1.8f))
        Text("ARTIST", color = TextMuted, fontSize = 8.sp, modifier = Modifier.weight(1.25f))
        Text("ALBUM", color = TextMuted, fontSize = 8.sp, modifier = Modifier.weight(1.2f))
        Text("DURATION", color = TextMuted, fontSize = 8.sp, modifier = Modifier.width(metrics.px(70f)))
        Spacer(Modifier.width(metrics.px(88f)))
    }
}


@Composable
@OptIn(ExperimentalComposeUiApi::class)
internal fun PlaylistTrackRow(
    song: SongItem,
    index: Int,
    selected: Boolean,
    active: Boolean,
    playing: Boolean,
    liked: Boolean,
    editable: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    forceMenu: Boolean = false,
    onSelect: () -> Unit,
    onPlay: () -> Unit,
    onLike: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onAction: (PlaylistTrackAction) -> Unit,
) {
    val metrics = LocalHomeReferenceMetrics.current
    var menuOpen by remember { mutableStateOf(false) }
    var dragAccumulated by remember { mutableStateOf(0f) }
    LaunchedEffect(forceMenu) {
        if (forceMenu) menuOpen = true
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(metrics.px(39f))
            .clip(RoundedCornerShape(metrics.px(7f)))
            .background(
                when {
                    selected -> OmniReferenceColors.SurfaceSelected.copy(alpha = 0.82f)
                    active -> OmniReferenceColors.SurfaceSelected.copy(alpha = 0.58f)
                    else -> OmniReferenceColors.SurfaceBase.copy(alpha = 0.24f)
                }
            )
            .border(
                1.dp,
                if (selected) OmniReferenceColors.Accent.copy(alpha = 0.88f) else Color.Transparent,
                RoundedCornerShape(metrics.px(7f)),
            )
            .onPointerEvent(PointerEventType.Press) {
                if (it.buttons.isSecondaryPressed) menuOpen = true
            }
            .clickable(onClick = onSelect)
            .padding(horizontal = metrics.px(8f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(metrics.px(34f)), contentAlignment = Alignment.CenterStart) {
            if (editable) {
                Icon(
                    Icons.Default.DragIndicator,
                    contentDescription = "Drag to reorder",
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(metrics.px(16f))
                        .pointerInput(canMoveUp, canMoveDown) {
                            detectVerticalDragGestures(
                                onDragStart = { dragAccumulated = 0f },
                                onDragEnd = { dragAccumulated = 0f },
                                onDragCancel = { dragAccumulated = 0f },
                            ) { change, dragAmount ->
                                change.consume()
                                dragAccumulated += dragAmount
                                if (dragAccumulated <= -22f && canMoveUp) {
                                    dragAccumulated = 0f
                                    onAction(PlaylistTrackAction.MoveUp)
                                } else if (dragAccumulated >= 22f && canMoveDown) {
                                    dragAccumulated = 0f
                                    onAction(PlaylistTrackAction.MoveDown)
                                }
                            }
                        },
                )
            } else if (playing) Icon(Icons.Default.GraphicEq, null, tint = IrisSoft, modifier = Modifier.size(metrics.px(13f)))
            else if (active) Icon(Icons.Default.PlayArrow, null, tint = IrisSoft, modifier = Modifier.size(metrics.px(13f)))
            else Text("${index + 1}", color = TextSecondary, fontSize = 9.sp)
        }
        AsyncImage(song.thumbnail.toHighResThumbnail(), song.title, Modifier.size(metrics.px(25f)).clip(RoundedCornerShape(metrics.px(4f))).clickable(onClick = onPlay), contentScale = ContentScale.Crop)
        Spacer(Modifier.width(metrics.px(8f)))
        Column(Modifier.weight(1.8f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(song.title, color = if (active || selected) TextPrimary else TextPrimary, fontSize = 9.8.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (song.explicit) {
                    Spacer(Modifier.width(metrics.px(4f)))
                    Text("E", color = TextMuted, fontSize = 7.sp, modifier = Modifier.clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.12f)).padding(horizontal = 3.dp))
                }
            }
        }
        Text(song.artists.joinToString(", ") { it.name }, color = TextSecondary, fontSize = 8.6.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1.25f))
        Text(song.album?.name ?: "Single", color = TextSecondary, fontSize = 8.6.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1.2f))
        Text(song.playlistDurationLabel(), color = TextSecondary, fontSize = 8.5.sp, modifier = Modifier.width(metrics.px(70f)))
        Icon(if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "Like", tint = if (liked) IrisSoft else TextSecondary, modifier = Modifier.size(metrics.px(13f)).clickable(onClick = onLike))
        Spacer(Modifier.width(metrics.px(17f)))
        Icon(Icons.Default.Add, contentDescription = "Add to playlist", tint = TextSecondary, modifier = Modifier.size(metrics.px(15f)).clickable(onClick = onAddToPlaylist))
        Spacer(Modifier.width(metrics.px(15f)))
        Box {
            Icon(Icons.Default.MoreHoriz, contentDescription = "Track actions", tint = TextSecondary, modifier = Modifier.size(metrics.px(15f)).clickable { menuOpen = true })
            PlaylistTrackContextMenu(
                expanded = menuOpen,
                editable = editable,
                liked = liked,
                hasAlbum = song.album?.id != null,
                hasArtist = song.artists.any { !it.id.isNullOrBlank() },
                onDismiss = { menuOpen = false },
                onAction = {
                    menuOpen = false
                    onAction(it)
                },
            )
        }
    }
}


@Composable
internal fun PlaylistTrackContextMenu(
    expanded: Boolean,
    editable: Boolean,
    liked: Boolean,
    hasAlbum: Boolean,
    hasArtist: Boolean,
    onDismiss: () -> Unit,
    onAction: (PlaylistTrackAction) -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .width(184.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, BorderLow.copy(alpha = 0.76f), RoundedCornerShape(12.dp))
            .background(Color(0xF20E1221))
            .padding(vertical = 7.dp),
        containerColor = Color(0xF20E1221),
        tonalElevation = 0.dp,
        shadowElevation = 14.dp,
    ) {
        PlaylistMenuAction(Icons.Default.Shuffle, "Play next") { onAction(PlaylistTrackAction.PlayNext) }
        PlaylistMenuAction(Icons.Default.Add, "Add to queue") { onAction(PlaylistTrackAction.AddToQueue) }
        PlaylistMenuAction(
            if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            if (liked) "Remove from liked songs" else "Save to liked songs",
            iconTint = IrisSoft,
        ) { onAction(PlaylistTrackAction.ToggleLike) }
        PlaylistMenuAction(Icons.Default.Add, "Add to another playlist", trailing = ">") { onAction(PlaylistTrackAction.AddToPlaylist) }
        PlaylistMenuSeparator()
        PlaylistMenuAction(Icons.Default.Download, "Download") { onAction(PlaylistTrackAction.Download) }
        if (hasAlbum) PlaylistMenuAction(Icons.Default.GraphicEq, "View album") { onAction(PlaylistTrackAction.ViewAlbum) }
        if (hasArtist) PlaylistMenuAction(Icons.Default.Person, "Go to artist") { onAction(PlaylistTrackAction.GoToArtist) }
        if (editable) {
            PlaylistMenuSeparator()
            PlaylistMenuAction(Icons.Default.ArrowUpward, "Move up") { onAction(PlaylistTrackAction.MoveUp) }
            PlaylistMenuAction(Icons.Default.ArrowDownward, "Move down") { onAction(PlaylistTrackAction.MoveDown) }
            PlaylistMenuSeparator()
            PlaylistMenuAction(Icons.Default.Delete, "Remove from playlist", textColor = Color(0xFFFF6575), iconTint = Color(0xFFFF4B61)) {
                onAction(PlaylistTrackAction.Remove)
            }
        }
    }
}


@Composable
internal fun ColumnScope.PlaylistMenuAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    textColor: Color = TextPrimary,
    iconTint: Color = TextSecondary,
    trailing: String? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = iconTint, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        if (trailing != null) {
            Text(trailing, color = TextMuted, fontSize = 12.sp)
        }
    }
}


@Composable
internal fun ColumnScope.PlaylistMenuSeparator() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 5.dp)
            .height(1.dp)
            .background(BorderLow.copy(alpha = 0.44f))
    )
}


@Composable
internal fun ExpandedPlaylistTrack(
    song: SongItem,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onGoToArtist: () -> Unit,
) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(metrics.px(92f))
            .clip(RoundedCornerShape(metrics.px(7f)))
            .background(Brush.horizontalGradient(listOf(Color(0xAA151246), Color(0x66101228), Color(0xAA080D1B))))
            .border(1.dp, OmniReferenceColors.Accent.copy(alpha = 0.38f), RoundedCornerShape(metrics.px(7f)))
            .padding(metrics.px(10f)),
        horizontalArrangement = Arrangement.spacedBy(metrics.px(14f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(song.thumbnail.toHighResThumbnail(), song.title, Modifier.size(metrics.px(75f)).clip(RoundedCornerShape(metrics.px(7f))), contentScale = ContentScale.Crop)
        Column(Modifier.weight(1f)) {
            Text(song.artists.joinToString(", ") { it.name }, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.album?.name ?: "Single", color = TextSecondary, fontSize = 8.7.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(metrics.px(7f)))
            Text("${song.playlistDurationLabel().ifBlank { "Unknown duration" }}${if (song.explicit) "  •  Explicit" else ""}", color = TextSecondary, fontSize = 8.5.sp)
            Spacer(Modifier.height(metrics.px(7f)))
            Box(Modifier.fillMaxWidth().height(metrics.px(18f)), contentAlignment = Alignment.CenterStart) {
                Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(2f))) {
                    repeat(34) { i ->
                        val h = metrics.px((4 + ((i * 7) % 14)).toFloat())
                        Box(Modifier.width(1.dp).height(h).clip(CircleShape).background(if (i % 3 == 0) IrisSoft else Color(0xFF8C7BFF).copy(alpha = 0.45f)))
                    }
                }
            }
        }
        Column(Modifier.weight(0.9f)) {
            Text("Track notes", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(metrics.px(5f)))
            Text(
                "From ${song.album?.name ?: "this playlist"} by ${song.artists.firstOrNull()?.name ?: "Unknown artist"}. Lyrics preview appears in Now Playing when lyrics are available.",
                color = TextSecondary,
                fontSize = 8.5.sp,
                lineHeight = 12.sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(Modifier.width(metrics.px(120f)), verticalArrangement = Arrangement.spacedBy(metrics.px(6f))) {
            SheetActionButton("Play Next", Icons.Default.PlayArrow, onPlayNext)
            SheetActionButton("Add to Queue", Icons.Default.Add, onAddToQueue)
            SheetActionButton("Go to Artist", Icons.Default.Person, onGoToArtist)
        }
    }
}

