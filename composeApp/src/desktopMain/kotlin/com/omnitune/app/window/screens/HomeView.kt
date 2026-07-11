package com.omnitune.app.window.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.hoverable
import com.omnitune.app.player.NavScreen
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.omnitune.innertube.pages.*
import com.omnitune.innertube.toHighResThumbnail
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun HomeView(player: PlayerViewModel) {
    var home by remember { mutableStateOf<HomePage?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val service = koinInject<YouTubeService>()
    val scope = rememberCoroutineScope()
    val currentSong by player.currentSong.collectAsState()
    val playbackState by player.playbackState.collectAsState()
    val liked by player.likedSongs.collectAsState()

    LaunchedEffect(Unit) {
        scope.launch {
            runCatching { service.home() }.onSuccess { home = it }.onFailure { error = it.message }
        }
    }

    Box(Modifier.fillMaxSize()) {
        if (home == null && error == null) {
            HomeShimmer()
        } else if (error != null) {
            OmniEmptyState("Couldn't load Home", error ?: "Unknown error")
        } else {
            HomeContent(player, home!!, currentSong, playbackState, liked)
        }
    }
}

@Composable
private fun HomeContent(player: PlayerViewModel, home: HomePage, currentSong: SongItem?, playbackState: PlaybackState, liked: Set<String>) {
    val featured = home.sections.firstNotNullOfOrNull { s -> s.items.firstOrNull { it is AlbumItem } as? AlbumItem }
    val continueListening = home.sections.firstOrNull { it.items.firstOrNull() is SongItem }?.items?.filterIsInstance<SongItem>()?.take(4) ?: emptyList()
    val remainingSections = home.sections.filterNot { it.items.firstOrNull() is SongItem && it.items.filterIsInstance<SongItem>().take(4) == continueListening }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(40.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Good evening, Alex", style = MaterialTheme.typography.displaySmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OmniIconButton(onClick = {}, icon = Icons.Default.ChevronLeft, contentDescription = "Back", size = 32.dp, iconSize = 24.dp, tint = TextPrimary)
                    OmniIconButton(onClick = {}, icon = Icons.Default.ChevronRight, contentDescription = "Forward", size = 32.dp, iconSize = 24.dp, tint = TextPrimary)
                }
            }
        }
        
        item {
            HomeHeroRow(featured, continueListening, player, currentSong, playbackState)
        }
        
        itemsIndexed(remainingSections) { index, section ->
            val isQuickPicks = section.title.contains("Quick picks", ignoreCase = true) || index == 0
            val isMadeForYou = section.title.contains("Made for you", ignoreCase = true) || section.title.contains("Daily", ignoreCase = true) || index == 1

            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Text(section.title, style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text("See all", style = MaterialTheme.typography.labelMedium, color = IrisSoft, modifier = Modifier.clickable { })
                }
                Spacer(Modifier.height(16.dp))
                
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(section.items) { item ->
                        when {
                            isQuickPicks -> QuickPicksCard(item, player)
                            isMadeForYou -> MadeForYouCard(item, player)
                            else -> CarouselCard(item, player, currentSong, playbackState, liked)
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun HomeHeroRow(album: AlbumItem?, continueListening: List<SongItem>, player: PlayerViewModel, currentSong: SongItem?, playbackState: PlaybackState) {
    Row(modifier = Modifier.fillMaxWidth().height(260.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        // Left side: Featured playlist/album card
        OmniGlassSurface(
            shape = Shapes.large,
            style = GlassDefaults.card,
            modifier = Modifier.weight(0.6f).fillMaxHeight()
                .let { if (album != null) it.clickable { player.openAlbum(album.browseId) } else it }
        ) {
            if (album != null) {
                AsyncImage(
                    model = album.thumbnail?.toHighResThumbnail(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Dark gradient overlay
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.5f to Color.Black.copy(alpha = 0.3f),
                    1f to Color.Black.copy(alpha = 0.9f)
                )))
                
                Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Bottom) {
                    Text("FEATURED PLAYLIST", style = MaterialTheme.typography.labelMedium, color = TextSecondary, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(album.title, style = MaterialTheme.typography.displayMedium, color = TextWhite, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    Text(album.artists?.joinToString(", ") { it.name ?: "" } ?: "", style = MaterialTheme.typography.titleMedium, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(Shapes.pill)
                                .background(OmniGradients.primaryAction)
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text("Play Now", style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(16.dp))
                        Icon(Icons.Default.MoreHoriz, null, tint = TextSecondary, modifier = Modifier.size(24.dp))
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize().background(BgCard), contentAlignment = Alignment.Center) {
                    Text("No featured album", color = TextSecondary)
                }
            }
        }
        
        // Right side: Continue Listening
        Column(modifier = Modifier.weight(0.4f).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
            Text("Continue Listening", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                continueListening.forEachIndexed { i, song ->
                    val isCurrent = song.id == currentSong?.id
                    val isPlaying = isCurrent && playbackState == PlaybackState.PLAYING
                    ContinueListeningRow(song, isActive = isCurrent, isPlaying = isPlaying, onClick = { player.playSong(song, i) })
                }
            }
        }
    }
}

@Composable
private fun ContinueListeningRow(item: SongItem, isActive: Boolean, isPlaying: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp)
            .clip(Shapes.medium)
            .background(if (isHovered || isActive) Surface2 else Surface1)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp).clip(Shapes.small).background(Surface3)) {
            AsyncImage(
                model = item.thumbnail?.toHighResThumbnail(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            if (isHovered || isActive) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                    if (isActive && isPlaying) {
                        PlayingIndicatorBox(isActive = true, playWhenReady = true, color = Iris)
                    } else {
                        Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.titleSmall, color = if (isActive) Iris else TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(item.artists?.joinToString(", ") { it.name } ?: "", style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun QuickPicksCard(item: YTItem, player: PlayerViewModel) {
    OmniGlassSurface(
        shape = Shapes.medium,
        style = GlassDefaults.card,
        modifier = Modifier
            .width(150.dp)
            .height(105.dp)
            .clickable {
                when (item) {
                    is AlbumItem -> player.openAlbum(item.browseId)
                    is PlaylistItem -> player.playPlaylist(item.id)
                    is ArtistItem -> player.openArtist(item.id)
                    is SongItem -> player.playSong(item, 0)
                }
            }
    ) {
        AsyncImage(
            model = item.thumbnail?.toHighResThumbnail(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(
            0f to Color.Transparent,
            0.4f to Color.Transparent,
            1f to Color.Black.copy(alpha = 0.9f)
        )))
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.Bottom) {
            Text(item.title, style = MaterialTheme.typography.titleSmall, color = TextWhite, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            val sub = when (item) {
                is SongItem -> item.artists?.joinToString(", ") { it.name }
                is AlbumItem -> item.artists?.joinToString(", ") { it.name ?: "" }
                else -> null
            }
            if (!sub.isNullOrEmpty()) {
                Text(sub, style = MaterialTheme.typography.labelSmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun MadeForYouCard(item: YTItem, player: PlayerViewModel) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    OmniGlassSurface(
        shape = Shapes.medium,
        style = GlassDefaults.card,
        modifier = Modifier
            .width(140.dp)
            .height(130.dp)
            .clickable(interactionSource = interactionSource, indication = null) {
                when (item) {
                    is AlbumItem -> player.openAlbum(item.browseId)
                    is PlaylistItem -> player.playPlaylist(item.id)
                    is ArtistItem -> player.openArtist(item.id)
                    is SongItem -> player.playSong(item, 0)
                }
            }
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Surface3, Surface2))).padding(12.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium, color = TextWhite, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            
            Box(modifier = Modifier.align(Alignment.BottomEnd)) {
                if (isHovered) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(OmniGradients.primaryAction)
                            .shadow(4.dp, androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CarouselCard(item: YTItem, player: PlayerViewModel, currentSong: SongItem?, playbackState: PlaybackState, liked: Set<String>) {
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
            is SongItem -> item.artists?.joinToString(", ") { it.name }
            is AlbumItem -> "Album • " + item.artists?.joinToString(", ") { it.name ?: "" } ?: ""
            is PlaylistItem -> "Playlist"
            is ArtistItem -> "Artist"
            else -> ""
        }
        if (!sub.isNullOrEmpty()) {
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

@Composable
private fun HomeShimmer() {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(40.dp)) {
        Box(modifier = Modifier.fillMaxWidth().height(220.dp).clip(Shapes.large).background(Surface2))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(5) {
                Column {
                    Box(modifier = Modifier.size(160.dp).clip(Shapes.artworkMedium).background(Surface2))
                    Spacer(Modifier.height(12.dp))
                    Box(modifier = Modifier.width(120.dp).height(16.dp).clip(Shapes.small).background(Surface2))
                    Spacer(Modifier.height(4.dp))
                    Box(modifier = Modifier.width(80.dp).height(12.dp).clip(Shapes.small).background(Surface2))
                }
            }
        }
    }
}

