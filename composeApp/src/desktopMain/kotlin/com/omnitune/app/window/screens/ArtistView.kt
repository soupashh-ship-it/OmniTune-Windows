package com.omnitune.app.window.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
        ArtistProfileShimmer()
        return
    }

    val p = page!!
    val songsSection = p.sections.firstOrNull { it.title.contains("Songs", true) }
    val albumsSection = p.sections.firstOrNull { it.title.contains("Albums", true) }
    val singlesSection = p.sections.firstOrNull { it.title.contains("Singles", true) }
    val relatedSection = p.sections.firstOrNull { it.title.contains("Fans might also like", true) || it.title.contains("Similar", true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 32.dp),
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
                        OmniSectionHeader(songsSection.title, actionLabel = "See all", onAction = {})
                        Spacer(Modifier.height(16.dp))
                        songsSection.items.filterIsInstance<SongItem>().take(5).forEachIndexed { index, song ->
                            OmniSongRow(
                                item = song,
                                isActive = song.id == currentSong?.id,
                                isPlaying = song.id == currentSong?.id && playbackState == PlaybackState.PLAYING,
                                onClick = { player.playSong(song, index) },
                                onPlayNext = { player.playNext(song) },
                                onAddToQueue = { player.addToQueue(song) },
                                onLike = { player.toggleLike(song.id) },
                                showIndex = true,
                                index = index
                            )
                        }
                    }
                }

                // Right Column: Latest Release & About
                Column(modifier = Modifier.width(320.dp), verticalArrangement = Arrangement.spacedBy(32.dp)) {
                    val latestRelease = singlesSection?.items?.firstOrNull() as? AlbumItem ?: albumsSection?.items?.firstOrNull() as? AlbumItem
                    if (latestRelease != null) {
                        Column {
                            OmniSectionHeader("Latest Release")
                            Spacer(Modifier.height(16.dp))
                            LatestReleaseCard(latestRelease, player)
                        }
                    }

                    if (!p.description.isNullOrBlank()) {
                        Column {
                            OmniSectionHeader("About")
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
                    OmniSectionHeader(albumsSection.title, actionLabel = "See all", onAction = {})
                    Spacer(Modifier.height(16.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(albumsSection.items.filterIsInstance<AlbumItem>().take(8)) { album ->
                            OmniMediaCard(
                                title = album.title,
                                subtitle = album.year?.toString() ?: "Album",
                                artworkUrl = album.thumbnail,
                                modifier = Modifier.width(160.dp).height(210.dp),
                                onClick = { player.openAlbum(album.browseId) }
                            )
                        }
                    }
                }
            }
        }

        if (singlesSection != null && singlesSection.items.isNotEmpty()) {
            item {
                Column {
                    OmniSectionHeader(singlesSection.title, actionLabel = "See all", onAction = {})
                    Spacer(Modifier.height(16.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(singlesSection.items.filterIsInstance<AlbumItem>().take(8)) { single ->
                            OmniMediaCard(
                                title = single.title,
                                subtitle = single.year?.toString() ?: "Single",
                                artworkUrl = single.thumbnail,
                                modifier = Modifier.width(140.dp).height(190.dp),
                                onClick = { player.openAlbum(single.browseId) }
                            )
                        }
                    }
                }
            }
        }

        if (relatedSection != null && relatedSection.items.isNotEmpty()) {
            item {
                Column {
                    OmniSectionHeader(relatedSection.title, actionLabel = "See all", onAction = {})
                    Spacer(Modifier.height(16.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(relatedSection.items.filterIsInstance<ArtistItem>().take(8)) { artist ->
                            SimilarArtistCard(artist, player)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtistHero(artist: ArtistItem, player: PlayerViewModel) {
    val artUrl = artist.thumbnail?.toHighResThumbnail()
    Box(modifier = Modifier.fillMaxWidth().height(320.dp).clip(Shapes.extraLarge)) {
        // Blurred backdrop
        AsyncImage(
            model = artUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(320.dp).blur(40.dp).scale(1.2f),
            contentScale = ContentScale.Crop,
            alpha = 0.4f
        )
        // Gradient overlay
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, BgDeep.copy(alpha = 0.9f)))))
        
        Row(modifier = Modifier.fillMaxSize().padding(32.dp), verticalAlignment = Alignment.Bottom) {
            // Foreground sharp portrait
            AsyncImage(
                model = artUrl,
                contentDescription = null,
                modifier = Modifier.size(180.dp).clip(androidx.compose.foundation.shape.CircleShape).border(1.dp, BorderWhite, androidx.compose.foundation.shape.CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(32.dp))
            Column(modifier = Modifier.weight(1f)) {
                Spacer(Modifier.height(8.dp))
                Text(artist.title, style = MaterialTheme.typography.displayLarge, color = TextPrimary, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OmniPrimaryButton(text = "Play", onClick = { player.playArtist(artist.id) }, icon = Icons.Filled.PlayArrow, modifier = Modifier.width(120.dp))
                    OmniSecondaryButton(text = "Follow", onClick = {})
                    OmniIconButton(icon = Icons.Default.MoreHoriz, contentDescription = "More", onClick = {})
                }
            }
        }
    }
}

@Composable
private fun LatestReleaseCard(album: AlbumItem, player: PlayerViewModel) {
    OmniSurface(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        shape = Shapes.medium,
        color = Surface1,
        onClick = { player.openAlbum(album.browseId) }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize().padding(12.dp)) {
            AsyncImage(model = album.thumbnail.toHighResThumbnail(), contentDescription = null, modifier = Modifier.size(76.dp).clip(Shapes.small), contentScale = ContentScale.Crop)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(album.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(album.year?.toString() ?: "New Release", style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1)
            }
        }
    }
}

@Composable
private fun SimilarArtistCard(artist: ArtistItem, player: PlayerViewModel) {
    Column(
        modifier = Modifier.width(140.dp).clickable { player.openArtist(artist.id) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = artist.thumbnail?.toHighResThumbnail(),
            contentDescription = null,
            modifier = Modifier.size(120.dp).clip(androidx.compose.foundation.shape.CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(12.dp))
        Text(artist.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun ArtistProfileShimmer() {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 32.dp), verticalArrangement = Arrangement.spacedBy(40.dp)) {
        OmniShimmerBlock(Modifier.fillMaxWidth().height(320.dp).clip(Shapes.extraLarge))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            Column(modifier = Modifier.weight(2f)) {
                OmniShimmerBlock(Modifier.width(150.dp).height(24.dp))
                Spacer(Modifier.height(16.dp))
                for (i in 1..5) {
                    OmniShimmerBlock(Modifier.fillMaxWidth().height(56.dp))
                    Spacer(Modifier.height(8.dp))
                }
            }
            Column(modifier = Modifier.width(320.dp)) {
                OmniShimmerBlock(Modifier.width(120.dp).height(24.dp))
                Spacer(Modifier.height(16.dp))
                OmniShimmerBlock(Modifier.fillMaxWidth().height(100.dp))
                Spacer(Modifier.height(32.dp))
                OmniShimmerBlock(Modifier.width(80.dp).height(24.dp))
                Spacer(Modifier.height(16.dp))
                OmniShimmerBlock(Modifier.fillMaxWidth().height(140.dp))
            }
        }
    }
}
