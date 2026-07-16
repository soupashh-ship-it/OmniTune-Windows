package com.omnitune.app.window.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.omnitune.app.platform.DownloadState
import com.omnitune.app.platform.LikedSongRecord
import com.omnitune.app.platform.SavedQueuePlaylist
import com.omnitune.app.player.NavScreen
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.app.platform.PlaybackState
import com.omnitune.app.window.BorderLow
import com.omnitune.app.window.IrisSoft
import com.omnitune.app.window.LocalHomeReferenceMetrics
import com.omnitune.app.window.OmniGradients
import com.omnitune.app.window.OmniReferenceColors
import com.omnitune.app.window.Surface3
import com.omnitune.app.window.TextMuted
import com.omnitune.app.window.TextPrimary
import com.omnitune.app.window.TextSecondary
import com.omnitune.innertube.models.AlbumItem
import com.omnitune.innertube.models.Artist
import com.omnitune.innertube.models.PlaylistItem
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.toHighResThumbnail
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.random.Random

@Composable
internal fun LikedTrackHeader(bulkMode: Boolean) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(Modifier.fillMaxWidth().height(metrics.px(24f)).padding(horizontal = metrics.px(8f)), verticalAlignment = Alignment.CenterVertically) {
        if (bulkMode) Spacer(Modifier.width(metrics.px(28f)))
        Text("#", color = TextMuted, fontSize = 8.sp, modifier = Modifier.width(metrics.px(34f)))
        Text("TITLE", color = TextMuted, fontSize = 8.sp, modifier = Modifier.weight(1.75f))
        Text("ARTIST", color = TextMuted, fontSize = 8.sp, modifier = Modifier.weight(1.0f))
        Text("ALBUM", color = TextMuted, fontSize = 8.sp, modifier = Modifier.weight(1.0f))
        Text("DATE LIKED ↓", color = TextMuted, fontSize = 8.sp, modifier = Modifier.weight(0.9f))
        Text("◷", color = TextMuted, fontSize = 10.sp, modifier = Modifier.width(metrics.px(54f)))
        Spacer(Modifier.width(metrics.px(96f)))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun LikedSongRow(
    index: Int,
    record: LikedSongRecord,
    bulkMode: Boolean,
    checked: Boolean,
    selected: Boolean,
    active: Boolean,
    playing: Boolean,
    downloaded: Boolean,
    menuOpen: Boolean,
    onToggleChecked: () -> Unit,
    onSelect: () -> Unit,
    onPlay: () -> Unit,
    onUnlike: () -> Unit,
    onAddPlaylist: () -> Unit,
    onMenu: () -> Unit,
    onDismissMenu: () -> Unit,
    onAction: (LikedTrackAction) -> Unit,
) {
    val metrics = LocalHomeReferenceMetrics.current
    val song = record.song
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(metrics.px(38f))
            .clip(RoundedCornerShape(metrics.px(7f)))
            .background(
                when {
                    checked -> OmniReferenceColors.SurfaceSelected.copy(alpha = 0.86f)
                    selected -> OmniReferenceColors.SurfaceSelected.copy(alpha = 0.72f)
                    active -> OmniReferenceColors.SurfaceSelected.copy(alpha = 0.56f)
                    else -> OmniReferenceColors.SurfaceBase.copy(alpha = 0.26f)
                }
            )
            .border(1.dp, if (selected) OmniReferenceColors.Accent.copy(alpha = 0.86f) else Color.Transparent, RoundedCornerShape(metrics.px(7f)))
            .combinedClickable(onClick = onSelect, onDoubleClick = onPlay, onLongClick = onMenu)
            .pointerInput(onMenu) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed) {
                            onMenu()
                            event.changes.forEach { it.consume() }
                        }
                    }
                }
            }
            .padding(horizontal = metrics.px(8f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (bulkMode) {
            Checkbox(checked = checked, onCheckedChange = { onToggleChecked() }, modifier = Modifier.size(metrics.px(26f)))
        }
        Box(Modifier.width(metrics.px(34f)), contentAlignment = Alignment.CenterStart) {
            if (playing) Icon(Icons.Default.GraphicEq, null, tint = IrisSoft, modifier = Modifier.size(metrics.px(13f)))
            else if (active) Icon(Icons.Default.PlayArrow, null, tint = IrisSoft, modifier = Modifier.size(metrics.px(13f)))
            else Text("${index + 1}", color = TextSecondary, fontSize = 9.sp)
        }
        AsyncImage(song.thumbnail.toHighResThumbnail(), song.title, Modifier.size(metrics.px(25f)).clip(RoundedCornerShape(metrics.px(4f))).clickable(onClick = onPlay), contentScale = ContentScale.Crop)
        Spacer(Modifier.width(metrics.px(8f)))
        Row(Modifier.weight(1.75f), verticalAlignment = Alignment.CenterVertically) {
            Text(song.title, color = TextPrimary, fontSize = 9.8.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (song.explicit) {
                Spacer(Modifier.width(metrics.px(4f)))
                Text("E", color = TextMuted, fontSize = 7.sp, modifier = Modifier.clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.12f)).padding(horizontal = 3.dp))
            }
        }
        Text(song.artists.joinToString(", ") { it.name }, color = TextSecondary, fontSize = 8.6.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1.0f))
        Text(song.album?.name ?: "Single", color = TextSecondary, fontSize = 8.6.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1.0f))
        Text(formatLikedDate(record.likedAt), color = TextSecondary, fontSize = 8.4.sp, maxLines = 1, modifier = Modifier.weight(0.9f))
        Text(song.likedDurationLabel(), color = TextSecondary, fontSize = 8.4.sp, modifier = Modifier.width(metrics.px(54f)))
        Icon(Icons.Default.Favorite, "Remove from liked songs", tint = IrisSoft, modifier = Modifier.size(metrics.px(13f)).clickable(onClick = onUnlike))
        Spacer(Modifier.width(metrics.px(17f)))
        Icon(Icons.Default.Add, "Add to playlist", tint = TextSecondary, modifier = Modifier.size(metrics.px(15f)).clickable(onClick = onAddPlaylist))
        Spacer(Modifier.width(metrics.px(15f)))
        Box {
            Icon(Icons.Default.MoreHoriz, "Track actions", tint = TextSecondary, modifier = Modifier.size(metrics.px(15f)).clickable(onClick = onMenu))
            LikedSongContextMenu(menuOpen, downloaded, song.album?.id != null, song.artists.any { !it.id.isNullOrBlank() }, onDismissMenu, onAction)
        }
    }
}

@Composable
internal fun ExpandedLikedSongDetail(song: SongItem, onPlayNext: () -> Unit, onAddQueue: () -> Unit, onArtist: () -> Unit, onAlbum: () -> Unit, onDownload: () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(
        Modifier.fillMaxWidth().height(metrics.px(100f)).clip(RoundedCornerShape(metrics.px(8f)))
            .background(Brush.horizontalGradient(listOf(Color(0xAA151246), Color(0x66101228), Color(0xAA080D1B))))
            .border(1.dp, OmniReferenceColors.Accent.copy(alpha = 0.38f), RoundedCornerShape(metrics.px(8f))).padding(metrics.px(10f)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(metrics.px(14f)),
    ) {
        AsyncImage(song.thumbnail.toHighResThumbnail(), song.title, Modifier.size(metrics.px(72f)).clip(RoundedCornerShape(metrics.px(8f))), contentScale = ContentScale.Crop)
        Column(Modifier.weight(1f)) {
            Text(song.title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artists.joinToString(", ") { it.name }, color = TextSecondary, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${song.album?.name ?: "Single"}  •  ${song.likedDurationLabel()}", color = TextSecondary, fontSize = 8.5.sp)
            Spacer(Modifier.height(metrics.px(7f)))
            Text("Lyrics preview appears in Now Playing when lyrics are available.", color = TextSecondary, fontSize = 8.2.sp, maxLines = 2)
        }
        Column(Modifier.width(metrics.px(126f)), verticalArrangement = Arrangement.spacedBy(metrics.px(6f))) {
            LikedSheetButton("Play Next", Icons.Default.PlayArrow, onPlayNext)
            LikedSheetButton("Add to Queue", Icons.Default.Add, onAddQueue)
            LikedSheetButton("Go to Artist", Icons.Default.Person, onArtist)
            LikedSheetButton("View Album", Icons.Default.GraphicEq, onAlbum, enabled = song.album?.id != null)
        }
    }
}

internal fun formatLikedDate(value: Long): String {
    if (value <= 0L) return "Saved"
    return DateTimeFormatter.ofPattern("MMM d, yyyy").withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(value))
}

internal fun formatDurationLong(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

internal fun SongItem.likedDurationLabel(): String {
    val value = duration ?: return ""
    return "${value / 60}:${(value % 60).toString().padStart(2, '0')}"
}