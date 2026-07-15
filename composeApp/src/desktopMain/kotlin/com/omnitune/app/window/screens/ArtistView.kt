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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import org.koin.compose.koinInject

@Composable
fun ArtistView(player: PlayerViewModel) {
    val artistId by player.currentArtistId.collectAsState()
    val service = koinInject<YouTubeService>()
    var page by remember(artistId) { mutableStateOf<ArtistPage?>(null) }
    var error by remember(artistId) { mutableStateOf<String?>(null) }
    val currentSong by player.currentSong.collectAsState()
    val playbackState by player.playbackState.collectAsState()
    val followedArtists by player.followedArtists.collectAsState()

    LaunchedEffect(artistId) {
        val id = artistId ?: return@LaunchedEffect
        runCatching { service.artist(id) }
            .onSuccess { page = it }
            .onFailure { error = it.message }
    }

    val loadedPage = page
    when {
        artistId == null -> OmniEmptyState("No artist selected", "Open an artist to view details.")
        error != null -> OmniEmptyState("Couldn't load artist", error ?: "Unknown error")
        loadedPage == null -> ArtistLoading()
        else -> ArtistReferenceContent(
            page = loadedPage,
            currentSong = currentSong,
            playbackState = playbackState,
            followed = followedArtists.contains(loadedPage.artist.id),
            onPlayArtist = { player.playArtist(loadedPage.artist.id) },
            onSong = { song, index -> player.playSong(song, index) },
            onAdd = { player.addToQueue(it) },
            onLike = { player.toggleLike(it.id) },
            onToggleFollow = { player.toggleFollowArtist(loadedPage.artist.id) },
            onStartRadio = {
                loadedPage.artist.radioEndpoint?.let(player::startRadio) ?: artistId?.let { player.startRadio(it, "artist") }
            },
            onAddTopSongs = { songs -> songs.forEach(player::addToQueue) },
            onDownloadTopSongs = { songs -> player.downloadSongs(songs) },
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
    followed: Boolean,
    onPlayArtist: () -> Unit,
    onSong: (SongItem, Int) -> Unit,
    onAdd: (SongItem) -> Unit,
    onLike: (SongItem) -> Unit,
    onToggleFollow: () -> Unit,
    onStartRadio: () -> Unit,
    onAddTopSongs: (List<SongItem>) -> Unit,
    onDownloadTopSongs: (List<SongItem>) -> Unit,
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
    var expandedSection by remember(artist.id) { mutableStateOf<String?>(null) }
    var artistMenuOpen by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().verticalScroll(scroll)) {
        Box(Modifier.fillMaxWidth().height(metrics.px(620f))) {
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
                    alpha = 0.74f,
                )
                Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(0f to Color(0xF0030917), 0.40f to Color(0xA8030917), 0.72f to Color(0x55150B2D), 1f to Color(0xAA030917))))
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0f to Color.Transparent, 0.78f to Color(0x66030917), 1f to Color(0xF0030917))))
                Column(Modifier.offset(x = metrics.px(48f), y = metrics.px(42f)).width(metrics.px(420f))) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(metrics.px(13f)).clip(CircleShape).background(IrisSoft), contentAlignment = Alignment.Center) {
                            Text("✓", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(metrics.px(8f)))
                        Text("Verified Artist", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(metrics.px(12f)))
                    Text(artist.title.uppercase(), color = TextPrimary, fontSize = 58.sp, lineHeight = 64.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, letterSpacing = 3.sp)
                    Spacer(Modifier.height(metrics.px(10f)))
                    Text("Pop • R&B • Soul", color = TextSecondary, fontSize = 15.sp)
                    Spacer(Modifier.height(metrics.px(4f)))
                    Text("${songs.size} provider songs · ${albums.size} albums", color = TextSecondary, fontSize = 14.sp)
                    Spacer(Modifier.height(metrics.px(18f)))
                    Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(12f))) {
                        ReferencePlayButton("Play", onPlayArtist)
                        ReferenceOutlineButton(if (followed) "Following" else "Follow", if (followed) Icons.Default.Favorite else Icons.Default.FavoriteBorder, onToggleFollow)
                        Box {
                            ReferenceIconOnlyButton(Icons.Default.MoreHoriz) { artistMenuOpen = true }
                            DropdownMenu(
                                expanded = artistMenuOpen,
                                onDismissRequest = { artistMenuOpen = false },
                                containerColor = Color(0xF20E1221),
                                tonalElevation = 0.dp,
                            ) {
                                DropdownMenuItem(text = { Text("Start artist radio") }, onClick = {
                                    artistMenuOpen = false
                                    onStartRadio()
                                })
                                DropdownMenuItem(text = { Text("Add top songs to queue") }, enabled = songs.isNotEmpty(), onClick = {
                                    artistMenuOpen = false
                                    onAddTopSongs(songs)
                                })
                                DropdownMenuItem(text = { Text("Download top songs") }, enabled = songs.isNotEmpty(), onClick = {
                                    artistMenuOpen = false
                                    onDownloadTopSongs(songs)
                                })
                            }
                        }
                    }
                }
                ProviderStatsCard(
                    sectionCount = page.sections.size,
                    songs = songs.size,
                    albums = albums.size,
                    modifier = Modifier.offset(x = metrics.px(730f), y = metrics.px(60f)).width(metrics.px(190f)).height(metrics.px(136f)),
                )
            }

            ArtistTabs(Modifier.offset(x = metrics.px(33f), y = metrics.px(213f)).width(metrics.px(380f)).height(metrics.px(42f)))

            Column(
                modifier = Modifier.offset(x = metrics.px(18f), y = metrics.px(250f)).width(metrics.px(382f)),
            ) {
                SectionTitle(
                    "Popular",
                    if (expandedSection == "popular") "Show less" else "See all",
                    enabled = songs.size > 5,
                    onAction = { expandedSection = if (expandedSection == "popular") null else "popular" },
                )
                Spacer(Modifier.height(metrics.px(8f)))
                songs.take(if (expandedSection == "popular") 12 else 5).forEachIndexed { index, song ->
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

            Column(Modifier.offset(x = metrics.px(415f), y = metrics.px(250f)).width(metrics.px(283f))) {
                SectionTitle("Latest Release", "Open", enabled = latest != null, onAction = { latest?.let(onOpenItem) })
                Spacer(Modifier.height(metrics.px(8f)))
                if (latest != null) {
                    LatestReleasePanel(latest, onOpenItem)
                }
                Spacer(Modifier.height(metrics.px(14f)))
                SectionTitle(
                    "Top Singles",
                    if (expandedSection == "singles") "Show less" else "See all",
                    enabled = singles.size > 4,
                    onAction = { expandedSection = if (expandedSection == "singles") null else "singles" },
                )
                Spacer(Modifier.height(metrics.px(8f)))
                Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(10f)), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    singles.take(if (expandedSection == "singles") singles.size else 4).forEach { item -> SmallMediaCard(item, onOpenItem) }
                }
                Spacer(Modifier.height(metrics.px(14f)))
                SectionTitle(
                    "Fans Also Like",
                    if (expandedSection == "related") "Show less" else "See all",
                    enabled = related.size > 5,
                    onAction = { expandedSection = if (expandedSection == "related") null else "related" },
                )
                Spacer(Modifier.height(metrics.px(8f)))
                Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(12f)), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    related.take(if (expandedSection == "related") related.size else 5).forEach { item -> RelatedArtistBubble(item, onOpenItem) }
                }
            }

            Column(Modifier.offset(x = metrics.px(712f), y = metrics.px(250f)).width(metrics.px(215f))) {
                InfoPanel("About ${artist.title}", page.description ?: "No biography is available from the provider for this artist.", metrics.px(170f))
                Spacer(Modifier.height(metrics.px(12f)))
                InfoPanel("On Tour", "No verified upcoming tour data is available in OmniTune.", metrics.px(76f))
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
            .background(
                Brush.horizontalGradient(
                    listOf(
                        OmniReferenceColors.SurfaceBase.copy(alpha = 0.94f),
                        Color(0xDD170F32),
                    )
                )
            )
            .border(1.dp, BorderLow.copy(alpha = 0.78f), RoundedCornerShape(metrics.px(8f)))
            .padding(metrics.px(14f)),
        verticalArrangement = Arrangement.spacedBy(metrics.px(8f)),
    ) {
        Text("Provider catalog", color = TextSecondary, fontSize = 12.sp)
        Text("$songs", color = TextPrimary, fontSize = 27.sp, fontWeight = FontWeight.Medium)
        Text("Albums", color = TextSecondary, fontSize = 12.sp)
        Text("$albums", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Medium)
        Text("$albums albums · $sectionCount sections", color = TextSecondary, fontSize = 10.sp)
    }
}

@Composable
private fun ArtistTabs(modifier: Modifier = Modifier) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(metrics.px(12f)), verticalAlignment = Alignment.CenterVertically) {
        listOf("Overview", "Songs", "Albums", "Singles", "About", "Concerts", "Merch").forEachIndexed { index, label ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    label,
                    color = if (index == 0) IrisSoft else TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = if (index == 0) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
                if (index == 0) {
                    Spacer(Modifier.height(metrics.px(7f)))
                    Box(Modifier.width(metrics.px(58f)).height(1.dp).background(IrisSoft))
                }
            }
        }
    }
}

@Composable
private fun ArtistPopularRow(song: SongItem, index: Int, active: Boolean, playing: Boolean, onPlay: () -> Unit, onAdd: () -> Unit, onLike: () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(metrics.px(42f))
            .clip(RoundedCornerShape(metrics.px(6f)))
            .background(if (active) OmniReferenceColors.SurfaceSelected.copy(alpha = 0.72f) else OmniReferenceColors.SurfaceBase.copy(alpha = 0.34f))
            .clickable(onClick = onPlay)
            .padding(horizontal = metrics.px(7f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (playing) Icon(Icons.Default.GraphicEq, null, tint = IrisSoft, modifier = Modifier.width(metrics.px(26f)).size(metrics.px(12f)))
        else Text("${index + 1}", color = TextSecondary, fontSize = 9.sp, modifier = Modifier.width(metrics.px(26f)))
        AsyncImage(song.thumbnail.toHighResThumbnail(), song.title, Modifier.size(metrics.px(29f)).clip(RoundedCornerShape(metrics.px(5f))), contentScale = ContentScale.Crop)
        Spacer(Modifier.width(metrics.px(8f)))
        Column(Modifier.weight(1f)) {
            Text(song.title, color = if (active) IrisSoft else TextPrimary, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artists.joinToString(", ") { it.name }, color = TextSecondary, fontSize = 7.8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(song.durationLabel(), color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(metrics.px(48f)))
        Icon(Icons.Default.Add, contentDescription = "Add to queue", tint = TextSecondary, modifier = Modifier.size(metrics.px(13f)).clickable(onClick = onAdd))
        Spacer(Modifier.width(metrics.px(12f)))
        Icon(Icons.Default.FavoriteBorder, contentDescription = "Like or unlike song", tint = TextSecondary, modifier = Modifier.size(metrics.px(13f)).clickable(onClick = onLike))
    }
}

@Composable
private fun LatestReleasePanel(item: YTItem, onOpen: (YTItem) -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(metrics.px(72f))
            .clip(RoundedCornerShape(metrics.px(8f)))
            .background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.72f))
            .border(1.dp, BorderLow.copy(alpha = 0.65f), RoundedCornerShape(metrics.px(8f)))
            .clickable { onOpen(item) }
            .padding(metrics.px(8f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(item.thumbnail?.toHighResThumbnail(), item.title, Modifier.size(metrics.px(54f)).clip(RoundedCornerShape(metrics.px(7f))), contentScale = ContentScale.Crop)
        Spacer(Modifier.width(metrics.px(10f)))
        Column(Modifier.weight(1f)) {
            Text(item.title, color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(item.kindLabel(), color = TextSecondary, fontSize = 12.sp)
        }
        Icon(Icons.Default.PlayArrow, null, tint = IrisSoft, modifier = Modifier.size(metrics.px(18f)))
    }
}

@Composable
private fun SmallMediaCard(item: YTItem, onOpen: (YTItem) -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Column(Modifier.width(metrics.px(66f)).clickable { onOpen(item) }) {
        AsyncImage(item.thumbnail?.toHighResThumbnail(), item.title, Modifier.size(metrics.px(63f)).clip(RoundedCornerShape(metrics.px(7f))), contentScale = ContentScale.Crop)
        Spacer(Modifier.height(metrics.px(5f)))
        Text(item.title, color = TextPrimary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(item.kindLabel(), color = TextSecondary, fontSize = 9.sp, maxLines = 1)
    }
}

@Composable
private fun RelatedArtistBubble(item: YTItem, onOpen: (YTItem) -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Column(Modifier.width(metrics.px(43f)).clickable { onOpen(item) }, horizontalAlignment = Alignment.CenterHorizontally) {
        AsyncImage(item.thumbnail?.toHighResThumbnail(), item.title, Modifier.size(metrics.px(38f)).clip(CircleShape).background(Surface1), contentScale = ContentScale.Crop)
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
private fun SectionTitle(title: String, action: String, enabled: Boolean = true, onAction: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = TextPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        Text(
            action,
            color = if (enabled) IrisSoft else TextMuted,
            fontSize = 8.5.sp,
            modifier = Modifier.clickable(enabled = enabled, onClick = onAction),
        )
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
private fun ReferenceIconOnlyButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(LocalHomeReferenceMetrics.current.px(34f))
            .clip(RoundedCornerShape(LocalHomeReferenceMetrics.current.px(8f)))
            .background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.68f))
            .border(1.dp, BorderLow.copy(alpha = 0.65f), RoundedCornerShape(LocalHomeReferenceMetrics.current.px(8f)))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(LocalHomeReferenceMetrics.current.px(14f)))
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
