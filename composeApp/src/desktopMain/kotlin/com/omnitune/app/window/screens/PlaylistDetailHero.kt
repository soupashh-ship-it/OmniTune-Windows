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
internal fun PlaylistHero(
    playlist: PlaylistItem,
    localPlaylist: SavedQueuePlaylist?,
    songs: List<SongItem>,
    description: String,
    tags: List<String>,
    duration: Int,
    coverSize: androidx.compose.ui.unit.Dp,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    onSave: () -> Unit,
    onDownload: () -> Unit,
    onEdit: () -> Unit,
    editable: Boolean,
) {
    val metrics = LocalHomeReferenceMetrics.current
    var moreOpen by remember { mutableStateOf(false) }
    val expandedHero = coverSize >= metrics.px(280f)
    val titleSize = if (expandedHero) 40.sp else if (coverSize >= metrics.px(220f)) 34.sp else 28.sp
    val titleLineHeight = if (expandedHero) 48.sp else if (coverSize >= metrics.px(220f)) 42.sp else 34.sp
    Column {
        Text("Playlists  ›  ${playlist.title}", color = TextSecondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(metrics.px(10f)))
        Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(if (expandedHero) 28f else 22f))) {
            AsyncImage(
                model = localPlaylist?.coverPath ?: playlist.thumbnail?.toHighResThumbnail(),
                contentDescription = playlist.title,
                modifier = Modifier
                    .size(coverSize)
                    .clip(RoundedCornerShape(metrics.px(10f)))
                    .background(Surface1)
                    .border(1.dp, BorderLow.copy(alpha = 0.76f), RoundedCornerShape(metrics.px(10f))),
                contentScale = ContentScale.Crop,
            )
            Column(Modifier.weight(1f).padding(top = metrics.px(8f))) {
                Text(if (editable) "PLAYLIST" else "PROVIDER PLAYLIST", color = TextSecondary, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(metrics.px(8f)))
                Text(playlist.title, color = TextPrimary, fontSize = titleSize, lineHeight = titleLineHeight, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(metrics.px(8f)))
                Text(description, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp, maxLines = if (expandedHero) 3 else 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(metrics.px(8f)))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Created by ${playlist.author?.name ?: "OmniTune"}", color = TextSecondary, fontSize = 11.sp)
                    Spacer(Modifier.width(metrics.px(6f)))
                    Icon(Icons.Default.Check, null, tint = IrisSoft, modifier = Modifier.size(metrics.px(12f)).clip(CircleShape).background(IrisSoft.copy(alpha = 0.18f)).padding(metrics.px(2f)))
                }
                Spacer(Modifier.height(metrics.px(5f)))
                Text("${playlist.songCountText ?: "${songs.size} songs"}${if (duration > 0) " · ${formatPlaylistDurationLong(duration)}" else ""}", color = TextSecondary, fontSize = 10.sp)
                Spacer(Modifier.height(metrics.px(12f)))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(metrics.px(8f))) {
                    tags.take(6).forEach { TruthChip(it) }
                }
                Spacer(Modifier.height(metrics.px(18f)))
                Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(10f)), verticalAlignment = Alignment.CenterVertically) {
                    ReferencePrimaryButton("Play", Icons.Default.PlayArrow, onPlay, Modifier.width(metrics.px(92f)).height(metrics.px(35f)))
                    ReferenceSecondaryButton("Shuffle", Icons.Default.Shuffle, onShuffle, Modifier.width(metrics.px(104f)).height(metrics.px(35f)))
                    if (!editable) RoundAction(Icons.Default.FavoriteBorder, onSave)
                    RoundAction(Icons.Default.Download, onDownload)
                    Box {
                        RoundAction(Icons.Default.MoreHoriz) { moreOpen = true }
                        DropdownMenu(
                            expanded = moreOpen,
                            onDismissRequest = { moreOpen = false },
                            modifier = Modifier
                                .width(metrics.px(178f))
                                .clip(RoundedCornerShape(metrics.px(10f)))
                                .border(1.dp, BorderLow.copy(alpha = 0.72f), RoundedCornerShape(metrics.px(10f)))
                                .background(Color(0xF20E1221)),
                            containerColor = Color(0xF20E1221),
                            tonalElevation = 0.dp,
                            shadowElevation = 12.dp,
                        ) {
                            if (editable) {
                                PlaylistMenuAction(Icons.Default.Edit, "Edit playlist") {
                                    moreOpen = false
                                    onEdit()
                                }
                                PlaylistMenuAction(Icons.Default.Download, "Download playlist") {
                                    moreOpen = false
                                    onDownload()
                                }
                            } else {
                                PlaylistMenuAction(Icons.Default.FavoriteBorder, "Save as playlist") {
                                    moreOpen = false
                                    onSave()
                                }
                                PlaylistMenuAction(Icons.Default.Download, "Download playlist") {
                                    moreOpen = false
                                    onDownload()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
internal fun PlaylistRightRail(
    relatedPlaylists: List<PlaylistItem>,
    tags: List<String>,
    onOpen: (PlaylistItem) -> Unit,
    onPlay: (PlaylistItem) -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier,
) {
    val metrics = LocalHomeReferenceMetrics.current
    Column(modifier, verticalArrangement = Arrangement.spacedBy(metrics.px(10f))) {
        RelatedPlaylistRail("More like this", relatedPlaylists, onOpen, onPlay, onSeeAll, Modifier.fillMaxWidth().height(metrics.px(230f)))
        RailCard("Mood Notes") {
            Text("Best when you want a focused listening session. Tags are generated from available playlist and track metadata.", color = TextSecondary, fontSize = 10.sp, lineHeight = 14.sp)
            Spacer(Modifier.height(metrics.px(9f)))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(metrics.px(6f))) {
                tags.take(5).forEach { TruthChip(it) }
            }
        }
        RailCard("Playlist tools") {
            Text("Use the row menu to add songs, play next, download, move songs in editable playlists, or remove tracks.", color = TextSecondary, fontSize = 10.sp, lineHeight = 14.sp)
        }
    }
}


@Composable
internal fun RelatedPlaylistRail(
    title: String,
    related: List<PlaylistItem>,
    onOpen: (PlaylistItem) -> Unit,
    onPlay: (PlaylistItem) -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalHomeReferenceMetrics.current
    RailCard(title, modifier) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(1f))
            Text("See all", color = IrisSoft, fontSize = 8.5.sp, modifier = Modifier.clickable(onClick = onSeeAll))
        }
        related.take(6).forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth().height(metrics.px(38f)).clickable { onOpen(item) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsyncImage(item.thumbnail?.toHighResThumbnail(), item.title, Modifier.size(metrics.px(30f)).clip(RoundedCornerShape(metrics.px(5f))), contentScale = ContentScale.Crop)
                Spacer(Modifier.width(metrics.px(8f)))
                Column(Modifier.weight(1f)) {
                    Text(item.title, color = TextPrimary, fontSize = 9.4.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(item.songCountText ?: item.author?.name ?: "Playlist", color = TextSecondary, fontSize = 7.8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Box(Modifier.size(metrics.px(21f)).clip(CircleShape).background(Color(0xFFDCDDF3)).clickable { onPlay(item) }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.PlayArrow, null, tint = Color(0xFF101124), modifier = Modifier.size(metrics.px(13f)))
                }
            }
        }
    }
}


@Composable
internal fun RailCard(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(metrics.px(9f)))
            .background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.82f))
            .border(1.dp, BorderLow.copy(alpha = 0.65f), RoundedCornerShape(metrics.px(9f)))
            .padding(metrics.px(12f)),
        verticalArrangement = Arrangement.spacedBy(metrics.px(7f)),
    ) {
        Text(title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        content()
    }
}


@Composable
internal fun TruthChip(label: String) {
    val metrics = LocalHomeReferenceMetrics.current
    Box(
        Modifier.clip(RoundedCornerShape(metrics.px(99f))).background(OmniReferenceColors.SurfaceSelected.copy(alpha = 0.62f)).border(1.dp, BorderLow.copy(alpha = 0.7f), RoundedCornerShape(metrics.px(99f))).padding(horizontal = metrics.px(10f), vertical = metrics.px(4f))
    ) {
        Text(label, color = TextPrimary, fontSize = 8.sp, maxLines = 1)
    }
}


@Composable
internal fun ReferencePrimaryButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, modifier: Modifier) {
    Row(
        modifier = modifier.clip(RoundedCornerShape(LocalHomeReferenceMetrics.current.px(9f))).background(OmniGradients.primaryAction).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = text, tint = Color.White, modifier = Modifier.size(LocalHomeReferenceMetrics.current.px(13f)))
        Spacer(Modifier.width(LocalHomeReferenceMetrics.current.px(6f)))
        Text(text, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}


@Composable
internal fun ReferenceSecondaryButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, modifier: Modifier) {
    Row(
        modifier = modifier.clip(RoundedCornerShape(LocalHomeReferenceMetrics.current.px(9f))).background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.78f)).border(1.dp, BorderLow.copy(alpha = 0.75f), RoundedCornerShape(LocalHomeReferenceMetrics.current.px(9f))).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = text, tint = TextPrimary, modifier = Modifier.size(LocalHomeReferenceMetrics.current.px(12f)))
        Spacer(Modifier.width(LocalHomeReferenceMetrics.current.px(6f)))
        Text(text, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}


@Composable
internal fun RoundAction(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Box(
        modifier = Modifier.size(metrics.px(32f)).clip(CircleShape).background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.78f)).border(1.dp, BorderLow.copy(alpha = 0.65f), CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = "Playlist action", tint = TextSecondary, modifier = Modifier.size(metrics.px(14f)))
    }
}
