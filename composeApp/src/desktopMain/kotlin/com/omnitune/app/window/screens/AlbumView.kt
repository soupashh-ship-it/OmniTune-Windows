package com.omnitune.app.window.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import com.omnitune.innertube.models.AlbumItem
import com.omnitune.innertube.models.Artist
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.pages.AlbumPage
import com.omnitune.innertube.toHighResThumbnail
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun AlbumView(player: PlayerViewModel) {
    val albumId by player.currentAlbumId.collectAsState()
    val service = koinInject<YouTubeService>()
    val scope = rememberCoroutineScope()
    var page by remember(albumId) { mutableStateOf<AlbumPage?>(null) }
    var error by remember(albumId) { mutableStateOf<String?>(null) }
    val currentSong by player.currentSong.collectAsState()
    val playbackState by player.playbackState.collectAsState()

    LaunchedEffect(albumId) {
        if (albumId != null) {
            scope.launch {
                runCatching { service.album(albumId!!) }
                    .onSuccess { page = it }
                    .onFailure { error = it.message }
            }
        }
    }

    when {
        albumId == null -> OmniEmptyState("No album selected", "Open an album to view details.")
        error != null -> OmniEmptyState("Couldn't load album", error ?: "Unknown error")
        page == null -> AlbumLoading()
        else -> AlbumReferenceContent(
            page = page!!,
            currentSong = currentSong,
            playbackState = playbackState,
            onPlayAlbum = { player.playAlbum(page!!.album.browseId) },
            onSong = { song, index -> player.playSong(song, index) },
            onAdd = { player.addToQueue(it) },
            onLike = { player.toggleLike(it.id) },
            onDownloadAlbum = { player.downloadSongs(page!!.songs) },
            onOpenArtist = { id -> if (id != null) player.openArtist(id) },
        )
    }
}

@Composable
private fun AlbumReferenceContent(
    page: AlbumPage,
    currentSong: SongItem?,
    playbackState: PlaybackState,
    onPlayAlbum: () -> Unit,
    onSong: (SongItem, Int) -> Unit,
    onAdd: (SongItem) -> Unit,
    onLike: (SongItem) -> Unit,
    onDownloadAlbum: () -> Unit,
    onOpenArtist: (String?) -> Unit,
) {
    val metrics = LocalHomeReferenceMetrics.current
    val scroll = rememberScrollState()
    val album = page.album
    val songs = page.songs
    val duration = songs.sumOf { it.duration ?: 0 }
    val albumArtists = album.artists.orEmpty()
    val featuredArtists = songs.flatMap { it.artists }.filter { artist ->
        albumArtists.none { it.id == artist.id && it.id != null }
    }.distinctBy { it.id ?: it.name }.take(4)

    Box(Modifier.fillMaxSize().verticalScroll(scroll)) {
        Box(Modifier.fillMaxWidth().height(metrics.px(560f))) {
            AsyncImage(
                model = album.thumbnail.toHighResThumbnail(),
                contentDescription = album.title,
                modifier = Modifier
                    .offset(x = metrics.px(24f), y = metrics.px(24f))
                    .size(metrics.px(238f))
                    .clip(RoundedCornerShape(metrics.px(10f)))
                    .background(Surface1)
                    .border(1.dp, BorderLow.copy(alpha = 0.65f), RoundedCornerShape(metrics.px(10f))),
                contentScale = ContentScale.Crop,
            )

            Column(Modifier.offset(x = metrics.px(288f), y = metrics.px(34f)).width(metrics.px(360f))) {
                Text("ALBUM", color = TextSecondary, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(metrics.px(10f)))
                Text(album.title, color = TextPrimary, fontSize = 31.sp, lineHeight = 35.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(metrics.px(10f)))
                Text(albumArtists.joinToString(", ") { it.name ?: "" }.ifBlank { "Unknown artist" }, color = TextPrimary, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(metrics.px(10f)))
                Text("${album.year ?: "Year unavailable"} · ${songs.size} songs${if (duration > 0) " · ${duration / 60} min ${duration % 60} sec" else ""}", color = TextSecondary, fontSize = 10.sp)
                Spacer(Modifier.height(metrics.px(21f)))
                Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(10f)), verticalAlignment = Alignment.CenterVertically) {
                    AlbumPrimaryButton(onPlayAlbum)
                    AlbumRound(Icons.Default.Add) {}
                    AlbumRound(Icons.Default.FavoriteBorder) {}
                    AlbumRound(Icons.Default.Download, onDownloadAlbum)
                    AlbumRound(Icons.Default.MoreHoriz) {}
                }
                Spacer(Modifier.height(metrics.px(22f)))
                Text("Album metadata is provided by the active music provider. Detailed credits are shown only when available.", color = TextSecondary, fontSize = 9.2.sp, lineHeight = 13.sp)
            }

            Column(Modifier.offset(x = metrics.px(24f), y = metrics.px(282f)).width(metrics.px(590f))) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Tracks", color = TextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.weight(1f))
                    Text("${songs.size} Songs", color = TextSecondary, fontSize = 8.5.sp)
                }
                Spacer(Modifier.height(metrics.px(10f)))
                songs.take(7).forEachIndexed { index, song ->
                    val active = song.id == currentSong?.id
                    AlbumTrackRow(
                        song = song,
                        index = index,
                        active = active,
                        playing = active && playbackState == PlaybackState.PLAYING,
                        onPlay = { onSong(song, index) },
                        onAdd = { onAdd(song) },
                        onLike = { onLike(song) },
                    )
                }
            }

            Column(Modifier.offset(x = metrics.px(650f), y = metrics.px(24f)).width(metrics.px(285f))) {
                AlbumSidePanel("Credits", metrics.px(188f)) {
                    albumArtists.take(4).forEach { artist -> CreditArtistRow(artist, album.thumbnail, onOpenArtist) }
                    if (albumArtists.isEmpty()) Text("Credits unavailable from provider.", color = TextSecondary, fontSize = 9.sp)
                }
                Spacer(Modifier.height(metrics.px(8f)))
                AlbumSidePanel("Featured Artists", metrics.px(74f)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(10f)), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        featuredArtists.forEach { artist -> FeaturedArtistPill(artist, onOpenArtist) }
                    }
                    if (featuredArtists.isEmpty()) Text("No additional featured artists in this album metadata.", color = TextSecondary, fontSize = 8.5.sp)
                }
                Spacer(Modifier.height(metrics.px(10f)))
                AlbumSidePanel("More by this artist", metrics.px(125f)) {
                    Text("Discography navigation opens from the artist page when provider data is available.", color = TextSecondary, fontSize = 8.7.sp, lineHeight = 12.sp)
                }
                Spacer(Modifier.height(metrics.px(10f)))
                AlbumSidePanel("Fans also like", metrics.px(85f)) {
                    Text("Related artist recommendations are not included in this album response.", color = TextSecondary, fontSize = 8.7.sp, lineHeight = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun AlbumTrackRow(song: SongItem, index: Int, active: Boolean, playing: Boolean, onPlay: () -> Unit, onAdd: () -> Unit, onLike: () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(metrics.px(34f))
            .clip(RoundedCornerShape(metrics.px(6f)))
            .background(if (active) OmniReferenceColors.SurfaceSelected.copy(alpha = 0.74f) else Color.Transparent)
            .clickable(onClick = onPlay)
            .padding(horizontal = metrics.px(8f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (playing) Icon(Icons.Default.GraphicEq, null, tint = IrisSoft, modifier = Modifier.width(metrics.px(30f)).size(metrics.px(12f)))
        else Text("${index + 1}", color = TextSecondary, fontSize = 9.sp, modifier = Modifier.width(metrics.px(30f)))
        Text(song.title, color = if (active) IrisSoft else TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1.5f))
        Text(song.artists.joinToString(", ") { it.name }, color = TextSecondary, fontSize = 8.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        Text(song.durationLabel(), color = TextSecondary, fontSize = 8.5.sp, modifier = Modifier.width(metrics.px(46f)))
        Icon(Icons.Default.Add, null, tint = TextSecondary, modifier = Modifier.size(metrics.px(14f)).clickable(onClick = onAdd))
        Spacer(Modifier.width(metrics.px(14f)))
        Icon(Icons.Default.MoreHoriz, null, tint = TextSecondary, modifier = Modifier.size(metrics.px(14f)).clickable(onClick = onLike))
    }
}

@Composable
private fun AlbumSidePanel(title: String, height: androidx.compose.ui.unit.Dp, content: @Composable ColumnScope.() -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(metrics.px(8f)))
            .background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.78f))
            .border(1.dp, BorderLow.copy(alpha = 0.65f), RoundedCornerShape(metrics.px(8f)))
            .padding(metrics.px(13f)),
    ) {
        Row {
            Text(title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text("Show all", color = IrisSoft, fontSize = 8.5.sp)
        }
        Spacer(Modifier.height(metrics.px(10f)))
        content()
    }
}

@Composable
private fun CreditArtistRow(artist: Artist, image: String?, onOpenArtist: (String?) -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(metrics.px(36f))
            .clickable { onOpenArtist(artist.id) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(image?.toHighResThumbnail(), artist.name, Modifier.size(metrics.px(26f)).clip(CircleShape).background(Surface1), contentScale = ContentScale.Crop)
        Spacer(Modifier.width(metrics.px(9f)))
        Column {
            Text(artist.name ?: "Unknown artist", color = TextPrimary, fontSize = 9.2.sp, maxLines = 1)
            Text("Album artist", color = TextSecondary, fontSize = 7.8.sp, maxLines = 1)
        }
    }
}

@Composable
private fun FeaturedArtistPill(artist: Artist, onOpenArtist: (String?) -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(Modifier.clickable { onOpenArtist(artist.id) }, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(metrics.px(24f)).clip(CircleShape).background(OmniReferenceColors.SurfaceSelected), contentAlignment = Alignment.Center) {
            Text((artist.name ?: "?").take(1), color = TextPrimary, fontSize = 9.sp)
        }
        Spacer(Modifier.width(metrics.px(5f)))
        Text(artist.name ?: "Artist", color = TextPrimary, fontSize = 8.5.sp, maxLines = 1)
    }
}

@Composable
private fun AlbumPrimaryButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .width(LocalHomeReferenceMetrics.current.px(76f))
            .height(LocalHomeReferenceMetrics.current.px(31f))
            .clip(RoundedCornerShape(LocalHomeReferenceMetrics.current.px(8f)))
            .background(OmniGradients.primaryAction)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(LocalHomeReferenceMetrics.current.px(13f)))
        Spacer(Modifier.width(LocalHomeReferenceMetrics.current.px(6f)))
        Text("Play", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AlbumRound(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
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
        Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(metrics.px(14f)))
    }
}

@Composable
private fun AlbumLoading() {
    val metrics = LocalHomeReferenceMetrics.current
    Row(Modifier.fillMaxSize().padding(metrics.px(24f)), horizontalArrangement = Arrangement.spacedBy(metrics.px(20f))) {
        OmniShimmerBlock(Modifier.size(metrics.px(238f)).clip(RoundedCornerShape(metrics.px(10f))))
        OmniShimmerBlock(Modifier.width(metrics.px(360f)).height(metrics.px(180f)))
    }
}

private fun SongItem.durationLabel(): String {
    val value = duration ?: return ""
    return "${value / 60}:${(value % 60).toString().padStart(2, '0')}"
}
