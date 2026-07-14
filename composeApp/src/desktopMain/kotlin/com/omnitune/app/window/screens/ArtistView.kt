package com.omnitune.app.window.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.omnitune.innertube.models.AlbumItem
import com.omnitune.innertube.models.ArtistItem
import com.omnitune.innertube.models.PlaylistItem
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.models.YTItem
import com.omnitune.innertube.pages.ArtistPage
import com.omnitune.innertube.toHighResThumbnail
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun ArtistView(player: PlayerViewModel) {
    val artistId by player.currentArtistId.collectAsState()
    val service = koinInject<YouTubeService>()
    val scope = rememberCoroutineScope()
    var page by remember(artistId) { mutableStateOf<ArtistPage?>(null) }
    var error by remember(artistId) { mutableStateOf<String?>(null) }
    val currentSong by player.currentSong.collectAsState()
    val playbackState by player.playbackState.collectAsState()

    LaunchedEffect(artistId) {
        if (artistId != null) {
            scope.launch {
                runCatching { service.artist(artistId!!) }
                    .onSuccess { page = it }
                    .onFailure { error = it.message }
            }
        }
    }

    when {
        artistId == null -> OmniEmptyState("No artist selected", "Open an artist to view details.")
        error != null -> OmniEmptyState("Couldn't load artist", error ?: "Unknown error")
        page == null -> ArtistLoading()
        else -> ArtistReferenceContent(
            page = page!!,
            currentSong = currentSong,
            playbackState = playbackState,
            onPlayArtist = { player.playArtist(artistId!!) },
            onSong = { song, index -> player.playSong(song, index) },
            onAdd = { player.addToQueue(it) },
            onLike = { player.toggleLike(it.id) },
            onOpenItem = { item ->
                when (item) {
                    is SongItem -> player.playSong(item)
                    is AlbumItem -> player.openAlbum(item.browseId)
                    is PlaylistItem -> player.openPlaylist(item.id)
                    is ArtistItem -> player.openArtist(item.id)
                }
            },
        )
    }
}

@Composable
private fun ArtistReferenceContent(
    page: ArtistPage,
    currentSong: SongItem?,
    playbackState: PlaybackState,
    onPlayArtist: () -> Unit,
    onSong: (SongItem, Int) -> Unit,
    onAdd: (SongItem) -> Unit,
    onLike: (SongItem) -> Unit,
    onOpenItem: (YTItem) -> Unit,
) {
    val metrics = LocalHomeReferenceMetrics.current
    val scroll = rememberScrollState()
    val artist = page.artist
    val songs = page.sections.firstOrNull { it.title.contains("Songs", true) }?.items?.filterIsInstance<SongItem>().orEmpty()
    val albums = page.sections.firstOrNull { it.title.contains("Albums", true) }?.items?.filterIsInstance<AlbumItem>().orEmpty()
    val singles = page.sections.firstOrNull { it.title.contains("Singles", true) }?.items.orEmpty()
    val related = page.sections.firstOrNull { it.title.contains("Fans", true) || it.title.contains("Similar", true) }?.items.orEmpty()
    val latest = (singles.firstOrNull() ?: albums.firstOrNull())

    Box(Modifier.fillMaxSize().verticalScroll(scroll)) {
        Box(Modifier.fillMaxWidth().height(metrics.px(560f))) {
            Box(
                Modifier
                    .offset(x = 0.dp, y = 0.dp)
                    .fillMaxWidth()
                    .height(metrics.px(203f))
                    .background(OmniReferenceColors.SurfaceBase)
            ) {
                AsyncImage(
                    model = artist.thumbnail?.toHighResThumbnail(),
                    contentDescription = artist.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.52f,
                )
                Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(0f to Color(0xE8040819), 0.45f to Color(0x74040819), 1f to Color(0xDD040819))))
                Column(Modifier.offset(x = metrics.px(46f), y = metrics.px(36f)).width(metrics.px(520f))) {
                    Text("ARTIST", color = IrisSoft, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(metrics.px(8f)))
                    Text(artist.title, color = TextPrimary, fontSize = 43.sp, lineHeight = 48.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(metrics.px(10f)))
                    Text("${songs.size} songs loaded from provider", color = TextSecondary, fontSize = 11.sp)
                    Spacer(Modifier.height(metrics.px(16f)))
                    Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(8f))) {
                        ReferencePlayButton("Play", onPlayArtist)
                        ReferenceOutlineButton("More", Icons.Default.MoreHoriz) {}
                    }
                }
                ProviderStatsCard(
                    sectionCount = page.sections.size,
                    songs = songs.size,
                    albums = albums.size,
                    modifier = Modifier.offset(x = metrics.px(730f), y = metrics.px(52f)).width(metrics.px(195f)).height(metrics.px(135f)),
                )
            }

            ArtistTabs(Modifier.offset(x = metrics.px(18f), y = metrics.px(191f)).width(metrics.px(395f)).height(metrics.px(31f)))

            Column(
                modifier = Modifier.offset(x = metrics.px(18f), y = metrics.px(236f)).width(metrics.px(374f)),
            ) {
                SectionTitle("Popular", "See all")
                Spacer(Modifier.height(metrics.px(8f)))
                songs.take(5).forEachIndexed { index, song ->
                    val active = song.id == currentSong?.id
                    ArtistPopularRow(
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

            Column(Modifier.offset(x = metrics.px(403f), y = metrics.px(236f)).width(metrics.px(290f))) {
                SectionTitle("Latest Release", "See all")
                Spacer(Modifier.height(metrics.px(8f)))
                if (latest != null) {
                    LatestReleasePanel(latest, onOpenItem)
                }
                Spacer(Modifier.height(metrics.px(14f)))
                SectionTitle("Top Singles", "See all")
                Spacer(Modifier.height(metrics.px(8f)))
                Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(8f)), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    singles.take(4).forEach { item -> SmallMediaCard(item, onOpenItem) }
                }
                Spacer(Modifier.height(metrics.px(14f)))
                SectionTitle("Fans Also Like", "See all")
                Spacer(Modifier.height(metrics.px(8f)))
                Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(12f)), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    related.take(5).forEach { item -> RelatedArtistBubble(item, onOpenItem) }
                }
            }

            Column(Modifier.offset(x = metrics.px(713f), y = metrics.px(236f)).width(metrics.px(220f))) {
                InfoPanel("About ${artist.title}", page.description ?: "No biography is available from the provider for this artist.", metrics.px(188f))
                Spacer(Modifier.height(metrics.px(12f)))
                InfoPanel("On Tour", "No verified upcoming tour data is available in OmniTune.", metrics.px(95f))
            }
        }
    }
}

@Composable
private fun ProviderStatsCard(sectionCount: Int, songs: Int, albums: Int, modifier: Modifier = Modifier) {
    val metrics = LocalHomeReferenceMetrics.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(metrics.px(8f)))
            .background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.78f))
            .border(1.dp, BorderLow.copy(alpha = 0.65f), RoundedCornerShape(metrics.px(8f)))
            .padding(metrics.px(14f)),
        verticalArrangement = Arrangement.spacedBy(metrics.px(8f)),
    ) {
        Text("Provider Metadata", color = TextSecondary, fontSize = 9.sp)
        Text("$songs", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("songs available", color = TextSecondary, fontSize = 8.5.sp)
        Text("$albums albums · $sectionCount sections", color = TextSecondary, fontSize = 8.5.sp)
    }
}

@Composable
private fun ArtistTabs(modifier: Modifier = Modifier) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(metrics.px(18f)), verticalAlignment = Alignment.CenterVertically) {
        listOf("Overview", "Songs", "Albums", "Singles", "About").forEachIndexed { index, label ->
            Text(label, color = if (index == 0) IrisSoft else TextSecondary, fontSize = 9.sp, fontWeight = if (index == 0) FontWeight.SemiBold else FontWeight.Normal)
        }
    }
}

@Composable
private fun ArtistPopularRow(song: SongItem, index: Int, active: Boolean, playing: Boolean, onPlay: () -> Unit, onAdd: () -> Unit, onLike: () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(metrics.px(40f))
            .clip(RoundedCornerShape(metrics.px(6f)))
            .background(if (active) OmniReferenceColors.SurfaceSelected.copy(alpha = 0.72f) else OmniReferenceColors.SurfaceBase.copy(alpha = 0.34f))
            .clickable(onClick = onPlay)
            .padding(horizontal = metrics.px(7f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (playing) Icon(Icons.Default.GraphicEq, null, tint = IrisSoft, modifier = Modifier.width(metrics.px(26f)).size(metrics.px(12f)))
        else Text("${index + 1}", color = TextSecondary, fontSize = 9.sp, modifier = Modifier.width(metrics.px(26f)))
        AsyncImage(song.thumbnail.toHighResThumbnail(), song.title, Modifier.size(metrics.px(28f)).clip(RoundedCornerShape(metrics.px(4f))), contentScale = ContentScale.Crop)
        Spacer(Modifier.width(metrics.px(8f)))
        Column(Modifier.weight(1f)) {
            Text(song.title, color = if (active) IrisSoft else TextPrimary, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artists.joinToString(", ") { it.name }, color = TextSecondary, fontSize = 7.8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(song.durationLabel(), color = TextSecondary, fontSize = 8.5.sp, modifier = Modifier.width(metrics.px(36f)))
        Icon(Icons.Default.Add, null, tint = TextSecondary, modifier = Modifier.size(metrics.px(13f)).clickable(onClick = onAdd))
        Spacer(Modifier.width(metrics.px(12f)))
        Icon(Icons.Default.MoreHoriz, null, tint = TextSecondary, modifier = Modifier.size(metrics.px(13f)).clickable(onClick = onLike))
    }
}

@Composable
private fun LatestReleasePanel(item: YTItem, onOpen: (YTItem) -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(metrics.px(70f))
            .clip(RoundedCornerShape(metrics.px(8f)))
            .background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.72f))
            .border(1.dp, BorderLow.copy(alpha = 0.65f), RoundedCornerShape(metrics.px(8f)))
            .clickable { onOpen(item) }
            .padding(metrics.px(8f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(item.thumbnail?.toHighResThumbnail(), item.title, Modifier.size(metrics.px(50f)).clip(RoundedCornerShape(metrics.px(6f))), contentScale = ContentScale.Crop)
        Spacer(Modifier.width(metrics.px(10f)))
        Column(Modifier.weight(1f)) {
            Text(item.title, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(item.kindLabel(), color = TextSecondary, fontSize = 8.sp)
        }
        Icon(Icons.Default.PlayArrow, null, tint = IrisSoft, modifier = Modifier.size(metrics.px(18f)))
    }
}

@Composable
private fun SmallMediaCard(item: YTItem, onOpen: (YTItem) -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Column(Modifier.width(metrics.px(66f)).clickable { onOpen(item) }) {
        AsyncImage(item.thumbnail?.toHighResThumbnail(), item.title, Modifier.size(metrics.px(62f)).clip(RoundedCornerShape(metrics.px(6f))), contentScale = ContentScale.Crop)
        Spacer(Modifier.height(metrics.px(5f)))
        Text(item.title, color = TextPrimary, fontSize = 7.8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(item.kindLabel(), color = TextSecondary, fontSize = 7.sp, maxLines = 1)
    }
}

@Composable
private fun RelatedArtistBubble(item: YTItem, onOpen: (YTItem) -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Column(Modifier.width(metrics.px(50f)).clickable { onOpen(item) }, horizontalAlignment = Alignment.CenterHorizontally) {
        AsyncImage(item.thumbnail?.toHighResThumbnail(), item.title, Modifier.size(metrics.px(44f)).clip(CircleShape).background(Surface1), contentScale = ContentScale.Crop)
        Spacer(Modifier.height(metrics.px(5f)))
        Text(item.title, color = TextPrimary, fontSize = 7.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun InfoPanel(title: String, body: String, height: androidx.compose.ui.unit.Dp) {
    val metrics = LocalHomeReferenceMetrics.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(metrics.px(8f)))
            .background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.76f))
            .border(1.dp, BorderLow.copy(alpha = 0.65f), RoundedCornerShape(metrics.px(8f)))
            .padding(metrics.px(12f)),
    ) {
        Text(title, color = TextPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(metrics.px(8f)))
        Text(body, color = TextSecondary, fontSize = 8.7.sp, lineHeight = 12.sp, maxLines = 9, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun SectionTitle(title: String, action: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = TextPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        Text(action, color = IrisSoft, fontSize = 8.5.sp)
    }
}

@Composable
private fun ReferencePlayButton(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .width(LocalHomeReferenceMetrics.current.px(94f))
            .height(LocalHomeReferenceMetrics.current.px(34f))
            .clip(RoundedCornerShape(LocalHomeReferenceMetrics.current.px(8f)))
            .background(OmniGradients.primaryAction)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(LocalHomeReferenceMetrics.current.px(14f)))
        Spacer(Modifier.width(LocalHomeReferenceMetrics.current.px(6f)))
        Text(text, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ReferenceOutlineButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .width(LocalHomeReferenceMetrics.current.px(94f))
            .height(LocalHomeReferenceMetrics.current.px(34f))
            .clip(RoundedCornerShape(LocalHomeReferenceMetrics.current.px(8f)))
            .background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.68f))
            .border(1.dp, BorderLow.copy(alpha = 0.65f), RoundedCornerShape(LocalHomeReferenceMetrics.current.px(8f)))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(LocalHomeReferenceMetrics.current.px(14f)))
        Spacer(Modifier.width(LocalHomeReferenceMetrics.current.px(6f)))
        Text(text, color = TextSecondary, fontSize = 10.sp)
    }
}

@Composable
private fun ArtistLoading() {
    val metrics = LocalHomeReferenceMetrics.current
    Column(Modifier.fillMaxSize().padding(metrics.px(22f)), verticalArrangement = Arrangement.spacedBy(metrics.px(12f))) {
        OmniShimmerBlock(Modifier.fillMaxWidth().height(metrics.px(203f)))
        OmniShimmerBlock(Modifier.width(metrics.px(380f)).height(metrics.px(180f)))
    }
}

private fun SongItem.durationLabel(): String {
    val value = duration ?: return ""
    return "${value / 60}:${(value % 60).toString().padStart(2, '0')}"
}

private fun YTItem.kindLabel(): String = when (this) {
    is SongItem -> artists.joinToString(", ") { it.name }
    is AlbumItem -> year?.toString() ?: "Album"
    is ArtistItem -> "Artist"
    is PlaylistItem -> songCountText ?: "Playlist"
}
