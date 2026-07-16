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

internal enum class LikedTrackAction { PlayNext, AddQueue, RemoveLiked, AddPlaylist, Download, ViewAlbum, GoArtist }

@Composable
internal fun LikedSongContextMenu(expanded: Boolean, downloaded: Boolean, hasAlbum: Boolean, hasArtist: Boolean, onDismiss: () -> Unit, onAction: (LikedTrackAction) -> Unit) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.width(214.dp).clip(RoundedCornerShape(12.dp)).border(1.dp, BorderLow.copy(alpha = 0.76f), RoundedCornerShape(12.dp)).background(Color(0xF20E1221)).padding(vertical = 7.dp),
        containerColor = Color(0xF20E1221),
        tonalElevation = 0.dp,
        shadowElevation = 14.dp,
    ) {
        LikedMenuAction(Icons.Default.Shuffle, "Play next", onClick = { onAction(LikedTrackAction.PlayNext) })
        LikedMenuAction(Icons.AutoMirrored.Filled.QueueMusic, "Add to queue", onClick = { onAction(LikedTrackAction.AddQueue) })
        LikedMenuAction(Icons.Default.Favorite, "Remove from Liked Songs", textColor = Color(0xFFFF6575), iconTint = Color(0xFFFF4B61), onClick = { onAction(LikedTrackAction.RemoveLiked) })
        LikedMenuAction(Icons.Default.Add, "Add to playlist", trailing = ">", onClick = { onAction(LikedTrackAction.AddPlaylist) })
        LikedMenuSeparator()
        LikedMenuAction(Icons.Default.Download, if (downloaded) "Downloaded" else "Download", onClick = { onAction(LikedTrackAction.Download) })
        if (hasAlbum) LikedMenuAction(Icons.Default.GraphicEq, "View album", onClick = { onAction(LikedTrackAction.ViewAlbum) })
        if (hasArtist) LikedMenuAction(Icons.Default.Person, "Go to artist", onClick = { onAction(LikedTrackAction.GoArtist) })
    }
}

@Composable internal fun LikedMenuAction(icon: ImageVector, label: String, textColor: Color = TextPrimary, iconTint: Color = TextSecondary, trailing: String? = null, onClick: () -> Unit) {
    Row(Modifier.width(210.dp).height(30.dp).clickable(onClick = onClick).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = iconTint, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (trailing != null) Text(trailing, color = TextMuted, fontSize = 12.sp)
    }
}

@Composable internal fun LikedMenuSeparator() {
    Box(Modifier.width(210.dp).padding(horizontal = 12.dp, vertical = 5.dp).height(1.dp).background(BorderLow.copy(alpha = 0.44f)))
}

@Composable internal fun LikedPrimaryButton(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier, enabled: Boolean = true) {
    val shape = RoundedCornerShape(9.dp)
    val backgroundModifier = if (enabled) {
        modifier.clip(shape).background(OmniGradients.primaryAction)
    } else {
        modifier.clip(shape).background(Surface3)
    }
    Row(backgroundModifier.clickable(enabled = enabled, onClick = onClick), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(7.dp))
        Text(text, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable internal fun LikedSecondaryButton(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier, enabled: Boolean = true) {
    Row(modifier.clip(RoundedCornerShape(9.dp)).background(OmniReferenceColors.SurfaceBase.copy(alpha = if (enabled) 0.78f else 0.34f)).border(1.dp, BorderLow.copy(alpha = 0.72f), RoundedCornerShape(9.dp)).clickable(enabled = enabled, onClick = onClick), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Icon(icon, null, tint = if (enabled) TextPrimary else TextMuted, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(7.dp))
        Text(text, color = if (enabled) TextPrimary else TextMuted, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable internal fun LikedRoundButton(icon: ImageVector, onClick: () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Box(Modifier.size(metrics.px(32f)).clip(RoundedCornerShape(metrics.px(9f))).background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.78f)).border(1.dp, BorderLow.copy(alpha = 0.72f), RoundedCornerShape(metrics.px(9f))).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = TextPrimary, modifier = Modifier.size(metrics.px(14f)))
    }
}

@Composable internal fun LikedSearchField(value: String, onValue: (String) -> Unit, modifier: Modifier, placeholder: String = "Search liked songs") {
    Row(modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFF11182B)).border(1.dp, BorderLow.copy(alpha = 0.66f), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Search, null, tint = TextSecondary, modifier = Modifier.size(15.dp))
        TextField(value, onValue, placeholder = { Text(placeholder) }, singleLine = true, colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, disabledContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary), modifier = Modifier.weight(1f))
    }
}

@Composable internal fun LikedSheetButton(text: String, icon: ImageVector, onClick: () -> Unit, enabled: Boolean = true) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(Modifier.fillMaxWidth().height(metrics.px(29f)).clip(RoundedCornerShape(metrics.px(7f))).background(OmniReferenceColors.SurfaceSelected.copy(alpha = if (enabled) 0.58f else 0.24f)).clickable(enabled = enabled, onClick = onClick).padding(horizontal = metrics.px(10f)), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Icon(icon, null, tint = if (enabled) TextPrimary else TextMuted, modifier = Modifier.size(metrics.px(13f)))
        Spacer(Modifier.width(metrics.px(8f)))
        Text(text, color = if (enabled) TextPrimary else TextMuted, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable internal fun LikedTinyButton(text: String, onClick: () -> Unit) {
    Box(Modifier.height(24.dp).clip(RoundedCornerShape(7.dp)).background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.82f)).border(1.dp, BorderLow.copy(alpha = 0.7f), RoundedCornerShape(7.dp)).clickable(onClick = onClick).padding(horizontal = 9.dp), contentAlignment = Alignment.Center) {
        Text(text, color = TextPrimary, fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable internal fun LikedChip(text: String, active: Boolean, onClick: () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    val shape = RoundedCornerShape(metrics.px(9f))
    val chipBase = Modifier.height(metrics.px(25f)).clip(shape).let { base ->
        if (active) base.background(OmniGradients.primaryAction) else base.background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.72f))
    }
    Box(chipBase.border(1.dp, if (active) Color.Transparent else BorderLow.copy(alpha = 0.72f), shape).clickable(onClick = onClick).padding(horizontal = metrics.px(12f)), contentAlignment = Alignment.Center) {
        Text(text, color = if (active) Color.White else TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
internal fun LikedBulkToolbar(selectedCount: Int, onAddPlaylist: () -> Unit, onAddQueue: () -> Unit, onDownload: () -> Unit, onRemove: () -> Unit, onClear: () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(Modifier.fillMaxWidth().height(metrics.px(43f)).clip(RoundedCornerShape(metrics.px(8f))).background(Color(0x99101428)).border(1.dp, BorderLow.copy(alpha = 0.55f), RoundedCornerShape(metrics.px(8f))).padding(horizontal = metrics.px(12f)), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(metrics.px(8f))) {
        Text("$selectedCount selected", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(metrics.px(110f)))
        LikedSecondaryButton("Add to Playlist", Icons.Default.Add, onAddPlaylist, Modifier.width(metrics.px(126f)).height(metrics.px(28f)), selectedCount > 0)
        LikedSecondaryButton("Add to Queue", Icons.AutoMirrored.Filled.QueueMusic, onAddQueue, Modifier.width(metrics.px(112f)).height(metrics.px(28f)), selectedCount > 0)
        LikedSecondaryButton("Download Selected", Icons.Default.Download, onDownload, Modifier.width(metrics.px(138f)).height(metrics.px(28f)), selectedCount > 0)
        LikedSecondaryButton("Remove from Liked Songs", Icons.Default.Favorite, onRemove, Modifier.width(metrics.px(168f)).height(metrics.px(28f)), selectedCount > 0)
        LikedSecondaryButton("Clear Selection", Icons.Default.Close, onClear, Modifier.width(metrics.px(120f)).height(metrics.px(28f)))
    }
}