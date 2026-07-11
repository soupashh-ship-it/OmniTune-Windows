package com.omnitune.app.window.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.hoverable
import com.omnitune.app.player.NavScreen
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.app.platform.PlaybackState
import com.omnitune.app.service.YouTubeService
import com.omnitune.app.window.*
import com.omnitune.app.window.components.*
import com.omnitune.innertube.models.*
import com.omnitune.innertube.pages.ArtistPage
import com.omnitune.innertube.toHighResThumbnail
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun ArtistView(player: PlayerViewModel) {
    val artistId by player.currentArtistId.collectAsState()
    var page by remember(artistId) { mutableStateOf<ArtistPage?>(null) }
    var error by remember(artistId) { mutableStateOf<String?>(null) }
    val service = koinInject<YouTubeService>()
    val scope = rememberCoroutineScope()

    val currentSong by player.currentSong.collectAsState()
    val playbackState by player.playbackState.collectAsState()
    val liked by player.likedSongs.collectAsState()

    LaunchedEffect(artistId) {
        if (artistId != null) {
            scope.launch {
                runCatching { service.artist(artistId!!) }.onSuccess { page = it }.onFailure { error = it.message }
        }
        }
    }

    if (error != null) {
        OmniEmptyState("Couldn't load Artist", error ?: "Unknown error")
        return
    }

    if (page == null) {
        Box(Modifier.fillMaxSize())
        return
    }

    val p = page!!
    val songsSection = p.sections.firstOrNull { it.title.contains("Songs", true) }
    val albumsSection = p.sections.firstOrNull { it.title.contains("Albums", true) }
    val singlesSection = p.sections.firstOrNull { it.title.contains("Singles", true) }
    val relatedSection = p.sections.firstOrNull { it.title.contains("Fans might also like", true) || it.title.contains("Similar", true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(40.dp),
    ) {
        item {
            ArtistHero(p.artist, player)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                // Left Column: Popular Songs
                if (songsSection != null && songsSection.items.isNotEmpty()) {
                    Column(modifier = Modifier.weight(2f)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                            Text("Popular", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(16.dp))
                        songsSection.items.filterIsInstance<SongItem>().take(5).forEachIndexed { index, song ->
                            val isCurrent = song.id == currentSong?.id
                            val isPlaying = isCurrent && playbackState == PlaybackState.PLAYING
                            OmniSongRow(
                                item = song,
                                isActive = isCurrent,
                                isPlaying = isPlaying,
                                onClick = { player.playSong(song, index) },
                                onPlayNext = { player.playNext(song) },
                                onAddToQueue = { player.addToQueue(song) },
                                onLike = { player.toggleLike(song.id) },
                                showIndex = true,
                                index = index
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("See more", style = MaterialTheme.typography.labelMedium, color = TextSecondary, modifier = Modifier.clickable { }.padding(8.dp))
                    }
                }

                // Right Column: Latest Release & About
                Column(modifier = Modifier.width(320.dp), verticalArrangement = Arrangement.spacedBy(32.dp)) {
                    val latestRelease = singlesSection?.items?.firstOrNull() as? AlbumItem ?: albumsSection?.items?.firstOrNull() as? AlbumItem
                    if (latestRelease != null) {
                        Column {
                            Text("Latest Release", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(16.dp))
                            LatestReleaseCard(latestRelease, player)
                        }
                    }

                    if (!p.description.isNullOrBlank()) {
                        Column {
                            Text("About", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(16.dp))
                            OmniSurface(shape = Shapes.medium, color = Surface1, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = p.description ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(16.dp),
                                    maxLines = 6,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        if (albumsSection != null && albumsSection.items.isNotEmpty()) {
            item {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Text("Albums", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("See all", style = MaterialTheme.typography.labelMedium, color = IrisSoft, modifier = Modifier.clickable { })
                    }
                    Spacer(Modifier.height(16.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(albumsSection.items.take(8)) { item ->
                            ArtistCarouselCard(item, player, currentSong, playbackState)
                        }
                    }
                }
            }
        }

        if (singlesSection != null && singlesSection.items.isNotEmpty()) {
            item {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Text("Singles & EPs", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("See all", style = MaterialTheme.typography.labelMedium, color = IrisSoft, modifier = Modifier.clickable { })
                    }
                    Spacer(Modifier.height(16.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(singlesSection.items.take(8)) { item ->
                            ArtistCarouselCard(item, player, currentSong, playbackState)
                        }
                    }
                }
            }
        }

        if (relatedSection != null && relatedSection.items.isNotEmpty()) {
            item {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Text("Fans might also like", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("See all", style = MaterialTheme.typography.labelMedium, color = IrisSoft, modifier = Modifier.clickable { })
                    }
                    Spacer(Modifier.height(16.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(relatedSection.items.take(8)) { item ->
                            ArtistCarouselCard(item, player, currentSong, playbackState)
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun ArtistHero(artist: ArtistItem, player: PlayerViewModel) {
    OmniSurface(modifier = Modifier.fillMaxWidth().height(260.dp), color = Surface1, border = true) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = artist.thumbnail ?: "",
                contentDescription = null,
                modifier = Modifier.fillMaxSize().blur(80.dp).scale(1.2f),
                contentScale = ContentScale.Crop,
                alpha = 0.3f
            )
            Row(modifier = Modifier.fillMaxSize().padding(32.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(196.dp).clip(androidx.compose.foundation.shape.CircleShape).background(BgCard)) {
                    AsyncImage(
                        model = artist.thumbnail ?: "",
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(Modifier.width(40.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Verified, null, tint = IrisSoft, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Verified Artist", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(artist.title ?: "", style = MaterialTheme.typography.displayLarge, color = TextPrimary, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(16.dp))
                    Text("14,204,500 monthly listeners", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Spacer(Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.clip(Shapes.pill).background(OmniGradients.primaryAction).clickable { }.padding(horizontal = 32.dp, vertical = 12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Play", style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        OmniIconButton(icon = Icons.Default.PersonAdd, contentDescription = "Follow", onClick = {}, size = 44.dp, background = Surface2)
                        OmniIconButton(icon = Icons.Default.MoreHoriz, contentDescription = "More", onClick = {}, size = 44.dp, background = Surface2)
                    }
                }
            }
        }
    }
}

@Composable
private fun LatestReleaseCard(album: AlbumItem, player: PlayerViewModel) {
    OmniSurface(modifier = Modifier.fillMaxWidth().clickable { player.openAlbum(album.browseId) }, color = Surface1, border = true) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(64.dp).clip(Shapes.small).background(BgCard)) {
                AsyncImage(
                    model = album.thumbnail.toHighResThumbnail(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(album.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(album.year?.toString() ?: "Latest", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun ArtistCarouselCard(item: YTItem, player: PlayerViewModel, currentSong: SongItem?, playbackState: PlaybackState) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Column(
        modifier = Modifier
            .width(160.dp)
            .clip(Shapes.medium)
            .background(if (isHovered) BgCardHover else Color.Transparent)
            .clickable(interactionSource = interactionSource, indication = null) {
                when (item) {
                    is AlbumItem -> player.openAlbum(item.browseId)
                    is PlaylistItem -> player.playPlaylist(item.id)
                    is ArtistItem -> player.openArtist(item.id)
                    is SongItem -> player.playSong(item, 0)
                }
            }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(144.dp)
                .clip(if (item is ArtistItem) androidx.compose.foundation.shape.CircleShape else Shapes.artworkMedium)
                .background(Surface2)
        ) {
            AsyncImage(
                model = item.thumbnail?.toHighResThumbnail(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            if (item is SongItem && item.id == currentSong?.id) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                    PlayingIndicatorBox(isActive = true, playWhenReady = playbackState == PlaybackState.PLAYING, color = Iris)
                }
            } else if (isHovered && item is SongItem) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(48.dp))
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            item.title,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        val sub = when (item) {
            is SongItem -> item.artists.joinToString(", ") { it.name }
            is AlbumItem -> item.year?.toString() ?: "Album"
            is PlaylistItem -> "Playlist"
            is ArtistItem -> "Artist"
            else -> ""
        }
        if (sub.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            Text(
                sub,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
