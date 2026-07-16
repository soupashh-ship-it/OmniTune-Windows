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
internal fun AddToPlaylistSheet(
    song: SongItem,
    playlists: List<SavedQueuePlaylist>,
    onClose: () -> Unit,
    onCreatePlaylist: (String, String) -> Result<String>,
    onAdd: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalHomeReferenceMetrics.current
    var query by remember { mutableStateOf("") }
    var selected by remember(song.id, playlists) { mutableStateOf(playlists.filter { it.songs.any { s -> s.id == song.id } }.map { it.id }.toSet()) }
    var creating by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val filtered by remember(playlists, query) {
        derivedStateOf {
            if (query.isBlank()) playlists else playlists.filter { it.name.contains(query, ignoreCase = true) }
        }
    }
    PlaylistSideSheet("Add to Playlist", onClose, modifier) {
        SearchField(query, "Search playlists") { query = it }
        SheetSectionTitle("RECENT PLAYLISTS")
        filtered.take(5).forEach { playlist ->
            PlaylistCheckRow(playlist, checked = playlist.id in selected) {
                selected = if (playlist.id in selected) selected - playlist.id else selected + playlist.id
            }
        }
        Spacer(Modifier.height(metrics.px(8f)))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            SheetSectionTitle("ALL PLAYLISTS", Modifier.weight(1f))
            SheetSmallButton("+ Create New Playlist") { creating = !creating }
        }
        if (creating) {
            TextField(
                value = newName,
                onValueChange = { newName = it },
                placeholder = { Text("Playlist name") },
                singleLine = true,
                colors = darkTextFieldColors(),
                modifier = sheetTextFieldModifier().fillMaxWidth().height(metrics.px(48f)),
            )
            SheetActionButton("Create", Icons.Default.Add, onClick = {
                onCreatePlaylist(newName, "")
                    .onSuccess { id ->
                        selected = selected + id
                        newName = ""
                        creating = false
                    }
                    .onFailure { error = it.message ?: "Could not create playlist." }
            })
        }
        filtered.drop(5).forEach { playlist ->
            PlaylistCheckRow(playlist, checked = playlist.id in selected) {
                selected = if (playlist.id in selected) selected - playlist.id else selected + playlist.id
            }
        }
        Spacer(Modifier.weight(1f))
        error?.let { Text(it, color = Color(0xFFFF7B85), fontSize = 10.sp) }
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(metrics.px(9f)))
                .background(OmniReferenceColors.SurfaceSelected.copy(alpha = 0.38f))
                .border(1.dp, BorderLow.copy(alpha = 0.72f), RoundedCornerShape(metrics.px(9f)))
                .padding(metrics.px(10f)),
        ) {
            Text("ADDING SONG", color = TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(metrics.px(8f)))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(song.thumbnail.toHighResThumbnail(), song.title, Modifier.size(metrics.px(35f)).clip(RoundedCornerShape(metrics.px(5f))), contentScale = ContentScale.Crop)
                Spacer(Modifier.width(metrics.px(8f)))
                Column(Modifier.weight(1f)) {
                    Text(song.title, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(song.artists.joinToString(", ") { it.name }, color = TextSecondary, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(Modifier.height(metrics.px(10f)))
            ReferencePrimaryButton("Add to Playlist", Icons.Default.Check, { onAdd(selected) }, Modifier.fillMaxWidth().height(metrics.px(34f)))
        }
    }
}


@Composable
internal fun EditPlaylistSheet(
    playlist: SavedQueuePlaylist,
    onClose: () -> Unit,
    onSave: (String, String, List<String>, String?) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalHomeReferenceMetrics.current
    var title by remember(playlist.id) { mutableStateOf(playlist.name) }
    var description by remember(playlist.id) { mutableStateOf(playlist.description) }
    var tagText by remember(playlist.id) { mutableStateOf(playlist.tags.joinToString(", ")) }
    var coverPath by remember(playlist.id) { mutableStateOf(playlist.coverPath) }
    PlaylistSideSheet("Edit Playlist", onClose, modifier) {
        SheetSectionTitle("Cover Image")
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(coverPath ?: playlist.songs.firstOrNull()?.thumbnail?.toHighResThumbnail(), playlist.name, Modifier.size(metrics.px(68f)).clip(RoundedCornerShape(metrics.px(8f))).background(Surface1), contentScale = ContentScale.Crop)
            Spacer(Modifier.width(metrics.px(12f)))
            Column(verticalArrangement = Arrangement.spacedBy(metrics.px(7f))) {
                SheetSmallButton("Change Cover") {
                    choosePlaylistCoverFile()?.let { coverPath = it }
                }
                if (coverPath != null) {
                    SheetSmallButton("Reset Cover") { coverPath = null }
                }
            }
        }
        SheetSectionTitle("Title")
        TextField(title, { title = it.take(100) }, singleLine = true, colors = darkTextFieldColors(), modifier = sheetTextFieldModifier().fillMaxWidth().height(metrics.px(48f)))
        SheetSectionTitle("Description")
        TextField(description, { description = it.take(300) }, colors = darkTextFieldColors(), modifier = sheetTextFieldModifier().fillMaxWidth().height(metrics.px(74f)))
        SheetSectionTitle("Mood & Tags")
        TextField(tagText, { tagText = it }, placeholder = { Text("Comma separated tags") }, colors = darkTextFieldColors(), modifier = sheetTextFieldModifier().fillMaxWidth().height(metrics.px(48f)))
        SheetSectionTitle("Arrangement")
        Text("Manual order is supported. Use track menu Move up/down controls to reorder songs; changes persist immediately.", color = TextSecondary, fontSize = 10.sp, lineHeight = 14.sp)
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(8f))) {
            ReferenceSecondaryButton("Cancel", Icons.Default.Close, onClose, Modifier.weight(1f).height(metrics.px(34f)))
            ReferencePrimaryButton("Save Changes", Icons.Default.Check, {
                onSave(title, description, tagText.split(","), coverPath)
            }, Modifier.weight(1f).height(metrics.px(34f)))
        }
        Spacer(Modifier.height(metrics.px(8f)))
        SheetActionButton("Delete Playlist", Icons.Default.Delete, onDelete, destructive = true)
    }
}


@Composable
internal fun PlaylistSideSheet(title: String, onClose: () -> Unit, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Column(
        modifier = modifier
            .width(metrics.px(310f))
            .fillMaxHeight()
            .clip(RoundedCornerShape(topStart = metrics.px(13f), bottomStart = metrics.px(13f)))
            .background(Brush.verticalGradient(listOf(Color(0xFF0A1021), Color(0xFF11112D), Color(0xFF090D1B))))
            .border(1.dp, BorderLow.copy(alpha = 0.76f), RoundedCornerShape(topStart = metrics.px(13f), bottomStart = metrics.px(13f)))
            .padding(metrics.px(18f)),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Icon(Icons.Default.Close, "Close", tint = TextPrimary, modifier = Modifier.size(metrics.px(18f)).clickable(onClick = onClose))
        }
        Spacer(Modifier.height(metrics.px(18f)))
        content()
    }
}


@Composable
internal fun sheetTextFieldModifier(): Modifier {
    val metrics = LocalHomeReferenceMetrics.current
    return Modifier
        .clip(RoundedCornerShape(metrics.px(7f)))
        .background(Color(0xFF11182B))
        .border(1.dp, BorderLow.copy(alpha = 0.64f), RoundedCornerShape(metrics.px(7f)))
}


@Composable
internal fun PlaylistCheckRow(playlist: SavedQueuePlaylist, checked: Boolean, onToggle: () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(Modifier.fillMaxWidth().height(metrics.px(42f)).clickable(onClick = onToggle), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = { onToggle() })
        AsyncImage(playlist.coverPath ?: playlist.songs.firstOrNull()?.thumbnail?.toHighResThumbnail(), playlist.name, Modifier.size(metrics.px(30f)).clip(RoundedCornerShape(metrics.px(5f))).background(Surface1), contentScale = ContentScale.Crop)
        Spacer(Modifier.width(metrics.px(8f)))
        Column(Modifier.weight(1f)) {
            Text(playlist.name, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${playlist.songs.size} songs", color = TextSecondary, fontSize = 9.sp)
        }
    }
}


@Composable
internal fun SearchField(value: String, placeholder: String, onValueChange: (String) -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(
        Modifier.fillMaxWidth().height(metrics.px(36f)).clip(RoundedCornerShape(metrics.px(7f))).background(Color(0xFF11182B)).border(1.dp, BorderLow.copy(alpha = 0.64f), RoundedCornerShape(metrics.px(7f))).padding(horizontal = metrics.px(10f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.Search, null, tint = TextSecondary, modifier = Modifier.size(metrics.px(14f)))
        Spacer(Modifier.width(metrics.px(7f)))
        TextField(value, onValueChange, placeholder = { Text(placeholder) }, singleLine = true, colors = darkTextFieldColors(), modifier = Modifier.weight(1f))
    }
}


@Composable
internal fun SheetSectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, color = TextMuted, fontSize = 8.3.sp, fontWeight = FontWeight.Bold, modifier = modifier.padding(top = LocalHomeReferenceMetrics.current.px(12f), bottom = LocalHomeReferenceMetrics.current.px(5f)))
}


@Composable
internal fun SheetActionButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, destructive: Boolean = false) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(
        Modifier.fillMaxWidth().height(metrics.px(29f)).clip(RoundedCornerShape(metrics.px(7f))).background(if (destructive) Color(0x33FF4C61) else OmniReferenceColors.SurfaceSelected.copy(alpha = 0.58f)).clickable(onClick = onClick).padding(horizontal = metrics.px(10f)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, null, tint = if (destructive) Color(0xFFFF6875) else TextPrimary, modifier = Modifier.size(metrics.px(13f)))
        Spacer(Modifier.width(metrics.px(6f)))
        Text(text, color = if (destructive) Color(0xFFFF6875) else TextPrimary, fontSize = 9.6.sp, fontWeight = FontWeight.SemiBold)
    }
}


@Composable
internal fun SheetSmallButton(text: String, onClick: () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Box(
        Modifier.height(metrics.px(28f)).clip(RoundedCornerShape(metrics.px(7f))).background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.82f)).border(1.dp, BorderLow.copy(alpha = 0.7f), RoundedCornerShape(metrics.px(7f))).clickable(onClick = onClick).padding(horizontal = metrics.px(10f)),
        contentAlignment = Alignment.Center,
    ) { Text(text, color = TextPrimary, fontSize = 9.5.sp) }
}


@Composable
internal fun TinyIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean, onClick: () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Box(
        Modifier.size(metrics.px(25f)).clip(RoundedCornerShape(metrics.px(6f))).background(OmniReferenceColors.SurfaceBase.copy(alpha = if (enabled) 0.8f else 0.28f)).clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, tint = if (enabled) TextSecondary else TextMuted, modifier = Modifier.size(metrics.px(13f))) }
}


@Composable
internal fun darkTextFieldColors() = TextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    cursorColor = IrisSoft,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    focusedPlaceholderColor = TextMuted,
    unfocusedPlaceholderColor = TextMuted,
)


