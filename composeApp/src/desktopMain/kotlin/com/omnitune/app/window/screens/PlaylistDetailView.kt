package com.omnitune.app.window.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
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
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun PlaylistDetailView(player: PlayerViewModel) {
    val playlistId by player.currentPlaylistId.collectAsState()
    val service = koinInject<YouTubeService>()
    val scope = rememberCoroutineScope()
    var page by remember(playlistId) { mutableStateOf<PlaylistPage?>(null) }
    var error by remember(playlistId) { mutableStateOf<String?>(null) }

    val currentSong by player.currentSong.collectAsState()
    val playbackState by player.playbackState.collectAsState()
    val discoveryNew by player.discoveryNew.collectAsState()
    val savedQueuePlaylists by player.savedQueuePlaylists.collectAsState()
    val localPlaylist = remember(savedQueuePlaylists, playlistId) {
        savedQueuePlaylists.firstOrNull { it.id == playlistId }?.let { saved ->
            PlaylistPage(
                playlist = PlaylistItem(
                    id = saved.id,
                    title = saved.name,
                    author = Artist("OmniTune", null),
                    songCountText = "${saved.songs.size} songs",
                    thumbnail = saved.songs.firstOrNull()?.thumbnail,
                    playEndpoint = null,
                    shuffleEndpoint = null,
                    radioEndpoint = null,
                    isEditable = true,
                ),
                songs = saved.songs,
                songsContinuation = null,
                continuation = null,
            )
        }
    }
    val relatedPlaylists = remember(discoveryNew, playlistId) {
        discoveryNew.filterIsInstance<PlaylistItem>().filter { it.id != playlistId }.distinctBy { it.id }
    }

    LaunchedEffect(playlistId) {
        if (playlistId != null && localPlaylist == null) {
            scope.launch {
                runCatching { service.playlist(playlistId!!) }
                    .onSuccess { page = it }
                    .onFailure { error = it.message }
            }
        }
    }

    when {
        playlistId == null -> OmniEmptyState("No playlist selected", "Open a playlist to view its details.")
        error != null -> OmniEmptyState("Couldn't load playlist", error ?: "Unknown error")
        page == null && localPlaylist == null -> PlaylistDetailLoading()
        else -> PlaylistDetailReferenceContent(
            page = localPlaylist ?: page!!,
            relatedPlaylists = relatedPlaylists,
            currentSong = currentSong,
            playbackState = playbackState,
            onPlayPlaylist = { player.playPlaylist((localPlaylist ?: page!!).playlist.id) },
            onShuffle = {
                player.playPlaylist((localPlaylist ?: page!!).playlist.id)
                player.toggleShuffle()
            },
            onSong = { song, index -> player.playSong(song, index) },
            onLikeSong = { player.toggleLike(it.id) },
            onAddSong = { player.addToQueue(it) },
            onPlayNext = { player.playNext(it) },
            onDownloadPlaylist = { player.downloadSongs((localPlaylist ?: page!!).songs) },
            onSavePlaylist = {
                val loaded = localPlaylist ?: page!!
                player.saveSongsAsPlaylist(loaded.playlist.title, loaded.songs)
            },
            onOpenPlaylists = { player.navigateTo(com.omnitune.app.player.NavScreen.Playlists) },
            onOpenRelated = { player.openPlaylist(it.id) },
            onPlayRelated = { player.playPlaylist(it.id) },
        )
    }
}

@Composable
private fun PlaylistDetailReferenceContent(
    page: PlaylistPage,
    relatedPlaylists: List<PlaylistItem>,
    currentSong: SongItem?,
    playbackState: PlaybackState,
    onPlayPlaylist: () -> Unit,
    onShuffle: () -> Unit,
    onSong: (SongItem, Int) -> Unit,
    onLikeSong: (SongItem) -> Unit,
    onAddSong: (SongItem) -> Unit,
    onPlayNext: (SongItem) -> Unit,
    onDownloadPlaylist: () -> Unit,
    onSavePlaylist: () -> Result<String>,
    onOpenPlaylists: () -> Unit,
    onOpenRelated: (PlaylistItem) -> Unit,
    onPlayRelated: (PlaylistItem) -> Unit,
) {
    val metrics = LocalHomeReferenceMetrics.current
    val scroll = rememberScrollState()
    val playlist = page.playlist
    val songs = page.songs
    val duration = songs.sumOf { it.duration ?: 0 }
    var actionMessage by remember(playlist.id) { mutableStateOf<String?>(null) }
    var playlistMenuExpanded by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().verticalScroll(scroll)) {
        Box(Modifier.fillMaxWidth().height(metrics.px(560f))) {
            Text(
                "Playlists  ›  ${playlist.title}",
                color = TextSecondary,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.offset(x = metrics.px(22f), y = metrics.px(16f)).width(metrics.px(420f)),
            )

            AsyncImage(
                model = playlist.thumbnail?.toHighResThumbnail(),
                contentDescription = playlist.title,
                modifier = Modifier
                    .offset(x = metrics.px(24f), y = metrics.px(36f))
                    .width(metrics.px(221f))
                    .height(metrics.px(224f))
                    .clip(RoundedCornerShape(metrics.px(10f)))
                    .background(Surface1)
                    .border(1.dp, BorderLow.copy(alpha = 0.65f), RoundedCornerShape(metrics.px(10f))),
                contentScale = ContentScale.Crop,
            )

            Column(
                modifier = Modifier
                    .offset(x = metrics.px(260f), y = metrics.px(46f))
                    .width(metrics.px(430f))
            ) {
                Text("PLAYLIST", color = TextSecondary, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(metrics.px(10f)))
                Text(playlist.title, color = TextPrimary, fontSize = 30.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(metrics.px(10f)))
                Text("Created by ${playlist.author?.name ?: "OmniTune"}", color = TextSecondary, fontSize = 11.sp, maxLines = 1)
                Spacer(Modifier.height(metrics.px(8f)))
                Text("${playlist.songCountText ?: "${songs.size} songs"}${if (duration > 0) " · ${duration / 60} min" else ""}", color = TextSecondary, fontSize = 10.sp)
                Spacer(Modifier.height(metrics.px(14f)))
                Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(8f))) {
                    TruthChip("Queue-ready")
                    TruthChip("Real tracks")
                    TruthChip("${songs.size} loaded")
                }
                Spacer(Modifier.height(metrics.px(17f)))
                Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(9f)), verticalAlignment = Alignment.CenterVertically) {
                    ReferencePrimaryButton("Play", Icons.Default.PlayArrow, onPlayPlaylist, Modifier.width(metrics.px(82f)).height(metrics.px(30f)))
                    ReferenceSecondaryButton("Shuffle", Icons.Default.Shuffle, onShuffle, Modifier.width(metrics.px(90f)).height(metrics.px(30f)))
                    RoundAction(Icons.Default.FavoriteBorder) {
                        onSavePlaylist()
                            .onSuccess { actionMessage = "Saved playlist: $it" }
                            .onFailure { actionMessage = it.message ?: "Could not save playlist." }
                    }
                    RoundAction(Icons.Default.Download) {
                        onDownloadPlaylist()
                        actionMessage = "Download queued for ${songs.size} loaded tracks."
                    }
                    Box {
                        RoundAction(Icons.Default.MoreHoriz) { playlistMenuExpanded = true }
                        DropdownMenu(expanded = playlistMenuExpanded, onDismissRequest = { playlistMenuExpanded = false }) {
                            DropdownMenuItem(text = { Text("Add all to queue") }, onClick = {
                                playlistMenuExpanded = false
                                songs.forEach(onAddSong)
                                actionMessage = "Added ${songs.size} tracks to queue."
                            })
                            DropdownMenuItem(text = { Text("Save to Library") }, onClick = {
                                playlistMenuExpanded = false
                                onSavePlaylist()
                                    .onSuccess { actionMessage = "Saved playlist: $it" }
                                    .onFailure { actionMessage = it.message ?: "Could not save playlist." }
                            })
                            DropdownMenuItem(text = { Text("Open Playlists") }, onClick = {
                                playlistMenuExpanded = false
                                onOpenPlaylists()
                            })
                        }
                    }
                }
                actionMessage?.let {
                    Spacer(Modifier.height(metrics.px(8f)))
                    Text(it, color = IrisSoft, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            PlaylistTrackHeader(
                modifier = Modifier
                    .offset(x = metrics.px(24f), y = metrics.px(285f))
                    .width(metrics.px(676f))
                    .height(metrics.px(20f)),
            )

            Column(
                modifier = Modifier
                    .offset(x = metrics.px(24f), y = metrics.px(311f))
                    .width(metrics.px(676f)),
                verticalArrangement = Arrangement.spacedBy(metrics.px(3f)),
            ) {
                songs.take(8).forEachIndexed { index, song ->
                    val active = song.id == currentSong?.id
                    PlaylistTrackRow(
                        song = song,
                        index = index,
                        active = active,
                        playing = active && playbackState == PlaybackState.PLAYING,
                        onPlay = { onSong(song, index) },
                        onLike = { onLikeSong(song) },
                        onAdd = { onAddSong(song) },
                        onMore = { onPlayNext(song) },
                        modifier = Modifier.fillMaxWidth().height(metrics.px(34f)),
                    )
                }
            }

            RelatedPlaylistRail(
                title = "More like ${playlist.title}",
                related = relatedPlaylists,
                onOpen = onOpenRelated,
                onPlay = onPlayRelated,
                onSeeAll = onOpenPlaylists,
                modifier = Modifier
                    .offset(x = metrics.px(730f), y = metrics.px(39f))
                    .width(metrics.px(208f))
                    .height(metrics.px(482f)),
            )
        }
    }
}

@Composable
private fun PlaylistTrackHeader(modifier: Modifier = Modifier) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text("#", color = TextMuted, fontSize = 8.sp, modifier = Modifier.width(metrics.px(38f)))
        Text("TITLE", color = TextMuted, fontSize = 8.sp, modifier = Modifier.weight(1.6f))
        Text("ARTIST", color = TextMuted, fontSize = 8.sp, modifier = Modifier.weight(1.3f))
        Text("ALBUM", color = TextMuted, fontSize = 8.sp, modifier = Modifier.weight(1.3f))
        Text("DURATION", color = TextMuted, fontSize = 8.sp, modifier = Modifier.width(metrics.px(70f)))
        Spacer(Modifier.width(metrics.px(72f)))
    }
}

@Composable
private fun PlaylistTrackRow(
    song: SongItem,
    index: Int,
    active: Boolean,
    playing: Boolean,
    onPlay: () -> Unit,
    onLike: () -> Unit,
    onAdd: () -> Unit,
    onMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(metrics.px(6f)))
            .background(if (active) OmniReferenceColors.SurfaceSelected.copy(alpha = 0.75f) else OmniReferenceColors.SurfaceBase.copy(alpha = 0.22f))
            .border(1.dp, if (active) OmniReferenceColors.Accent.copy(alpha = 0.35f) else Color.Transparent, RoundedCornerShape(metrics.px(6f)))
            .clickable(onClick = onPlay)
            .padding(horizontal = metrics.px(7f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (playing) Icon(Icons.Default.GraphicEq, null, tint = IrisSoft, modifier = Modifier.width(metrics.px(32f)).size(metrics.px(12f)))
        else Text("${index + 1}", color = TextSecondary, fontSize = 9.sp, modifier = Modifier.width(metrics.px(32f)))
        AsyncImage(song.thumbnail.toHighResThumbnail(), song.title, Modifier.size(metrics.px(24f)).clip(RoundedCornerShape(metrics.px(4f))), contentScale = ContentScale.Crop)
        Spacer(Modifier.width(metrics.px(7f)))
        Text(song.title, color = if (active) IrisSoft else TextPrimary, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1.6f))
        Text(song.artists.joinToString(", ") { it.name }, color = TextSecondary, fontSize = 8.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1.3f))
        Text(song.album?.name ?: "Single", color = TextSecondary, fontSize = 8.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1.3f))
        Text(song.durationLabel(), color = TextSecondary, fontSize = 8.5.sp, modifier = Modifier.width(metrics.px(70f)))
        Icon(Icons.Default.FavoriteBorder, contentDescription = "Like or unlike song", tint = TextSecondary, modifier = Modifier.size(metrics.px(13f)).clickable(onClick = onLike))
        Spacer(Modifier.width(metrics.px(17f)))
        Icon(Icons.Default.Add, contentDescription = "Add to queue", tint = TextSecondary, modifier = Modifier.size(metrics.px(14f)).clickable(onClick = onAdd))
        Spacer(Modifier.width(metrics.px(17f)))
        Icon(Icons.Default.MoreHoriz, contentDescription = "Song actions", tint = TextSecondary, modifier = Modifier.size(metrics.px(14f)).clickable(onClick = onMore))
    }
}

@Composable
private fun RelatedPlaylistRail(
    title: String,
    related: List<PlaylistItem>,
    onOpen: (PlaylistItem) -> Unit,
    onPlay: (PlaylistItem) -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalHomeReferenceMetrics.current
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Text("See all", color = IrisSoft, fontSize = 8.5.sp, modifier = Modifier.clickable(onClick = onSeeAll))
        }
        Spacer(Modifier.height(metrics.px(12f)))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(metrics.px(8f)))
                .background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.82f))
                .border(1.dp, BorderLow.copy(alpha = 0.65f), RoundedCornerShape(metrics.px(8f)))
                .padding(metrics.px(9f)),
            verticalArrangement = Arrangement.spacedBy(metrics.px(6f)),
        ) {
            related.take(7).forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(metrics.px(48f))
                        .clip(RoundedCornerShape(metrics.px(6f)))
                        .clickable { onOpen(item) },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AsyncImage(
                        item.thumbnail?.toHighResThumbnail(),
                        item.title,
                        Modifier.size(metrics.px(38f)).clip(RoundedCornerShape(metrics.px(5f))),
                        contentScale = ContentScale.Crop,
                    )
                    Spacer(Modifier.width(metrics.px(8f)))
                    Column(Modifier.weight(1f)) {
                        Text(item.title, color = TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(item.songCountText ?: item.author?.name ?: "Playlist", color = TextSecondary, fontSize = 7.7.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Box(
                        modifier = Modifier
                            .size(metrics.px(21f))
                            .clip(CircleShape)
                            .background(Color(0xFFDCDDF3))
                            .clickable { onPlay(item) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.PlayArrow, null, tint = Color(0xFF101124), modifier = Modifier.size(metrics.px(13f)))
                    }
                }
            }
        }
    }
}

@Composable
private fun TruthChip(label: String) {
    val metrics = LocalHomeReferenceMetrics.current
    Box(
        Modifier
            .clip(RoundedCornerShape(metrics.px(99f)))
            .background(OmniReferenceColors.SurfaceSelected.copy(alpha = 0.62f))
            .border(1.dp, BorderLow.copy(alpha = 0.7f), RoundedCornerShape(metrics.px(99f)))
            .padding(horizontal = metrics.px(10f), vertical = metrics.px(4f))
    ) {
        Text(label, color = TextPrimary, fontSize = 8.sp, maxLines = 1)
    }
}

@Composable
private fun ReferencePrimaryButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, modifier: Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(LocalHomeReferenceMetrics.current.px(9f)))
            .background(OmniGradients.primaryAction)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = text, tint = Color.White, modifier = Modifier.size(LocalHomeReferenceMetrics.current.px(13f)))
        Spacer(Modifier.width(LocalHomeReferenceMetrics.current.px(6f)))
        Text(text, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ReferenceSecondaryButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, modifier: Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(LocalHomeReferenceMetrics.current.px(9f)))
            .background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.78f))
            .border(1.dp, BorderLow.copy(alpha = 0.75f), RoundedCornerShape(LocalHomeReferenceMetrics.current.px(9f)))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = text, tint = TextPrimary, modifier = Modifier.size(LocalHomeReferenceMetrics.current.px(12f)))
        Spacer(Modifier.width(LocalHomeReferenceMetrics.current.px(6f)))
        Text(text, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun RoundAction(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Box(
        modifier = Modifier
            .size(metrics.px(30f))
            .clip(CircleShape)
            .background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.78f))
            .border(1.dp, BorderLow.copy(alpha = 0.65f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = "Playlist action", tint = TextSecondary, modifier = Modifier.size(metrics.px(14f)))
    }
}

@Composable
private fun PlaylistDetailLoading() {
    val metrics = LocalHomeReferenceMetrics.current
    Column(Modifier.fillMaxSize().padding(metrics.px(22f)), verticalArrangement = Arrangement.spacedBy(metrics.px(16f))) {
        Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(22f))) {
            OmniShimmerBlock(Modifier.size(metrics.px(220f)).clip(RoundedCornerShape(metrics.px(10f))))
            Column(verticalArrangement = Arrangement.spacedBy(metrics.px(12f))) {
                OmniShimmerBlock(Modifier.width(metrics.px(280f)).height(metrics.px(34f)))
                OmniShimmerBlock(Modifier.width(metrics.px(420f)).height(metrics.px(20f)))
                OmniShimmerBlock(Modifier.width(metrics.px(300f)).height(metrics.px(30f)))
            }
        }
    }
}

private fun SongItem.durationLabel(): String {
    val value = duration ?: return ""
    return "${value / 60}:${(value % 60).toString().padStart(2, '0')}"
}
