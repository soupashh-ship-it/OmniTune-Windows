package com.omnitune.app.window.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.hoverable
import com.omnitune.innertube.models.ArtistItem
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.app.platform.PlaybackState
import com.omnitune.app.service.YouTubeService
import com.omnitune.app.window.*
import com.omnitune.app.window.components.*
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
    var page by remember(albumId) { mutableStateOf<AlbumPage?>(null) }
    var error by remember(albumId) { mutableStateOf<String?>(null) }
    val service = koinInject<YouTubeService>()
    val scope = rememberCoroutineScope()

    val currentSong by player.currentSong.collectAsState()
    val playbackState by player.playbackState.collectAsState()

    LaunchedEffect(albumId) {
        if (albumId != null) {
            scope.launch {
                runCatching { service.album(albumId!!) }.onSuccess { page = it }.onFailure { error = it.message }
            }
        }
    }

    if (error != null) {
        OmniEmptyState("Couldn't load Album", error ?: "Unknown error")
        return
    }

    if (page == null) {
        Box(Modifier.fillMaxSize())
        return
    }

    val p = page!!
    val totalDurationSec = p.songs.sumOf { it.duration ?: 0 }
    val totalDurationStr = if (totalDurationSec > 0) "${totalDurationSec / 60} min ${totalDurationSec % 60} sec" else ""
    val artistName = p.album.artists?.firstOrNull()?.name ?: "Unknown Artist"
    val year = p.album.year ?: "2024"
    
    // Derive featured artists
    val mainArtistIds = p.album.artists?.mapNotNull { it.id }?.toSet() ?: emptySet()
    val featuredArtists = p.songs.flatMap { it.artists }.filter { it.id != null && it.id !in mainArtistIds }.distinctBy { it.id }.take(3)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 44.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(40.dp),
    ) {
        item {
            // Top Header: Art + Details
            Row(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                Box(modifier = Modifier.size(300.dp).clip(Shapes.medium).background(Surface1)) {
                    AsyncImage(
                        model = p.album.thumbnail.toHighResThumbnail(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(Modifier.width(40.dp))
                Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                    Text("ALBUM", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    Text(p.album.title, style = MaterialTheme.typography.displayMedium, color = TextPrimary, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(artistName, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Default.Verified, null, tint = IrisSoft, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Default.ChevronRight, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("$year · ${p.songs.size} songs · $totalDurationStr", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Spacer(Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.clip(Shapes.pill).background(IrisSoft).clickable { player.playAlbum(p.album.browseId) }.padding(horizontal = 24.dp, vertical = 12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Play", style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        OmniIconButton(icon = Icons.Default.Add, contentDescription = "Add", onClick = {}, size = 44.dp, background = Surface2)
                        OmniIconButton(icon = Icons.Default.FavoriteBorder, contentDescription = "Like", onClick = {}, size = 44.dp, background = Surface2)
                        OmniIconButton(icon = Icons.Default.Download, contentDescription = "Download", onClick = {}, size = 44.dp, background = Surface2)
                        OmniIconButton(icon = Icons.Default.MoreHoriz, contentDescription = "More", onClick = {}, size = 44.dp, background = Surface2)
                    }
                    Spacer(Modifier.height(24.dp))
                    Text("${p.album.title} is a journey through neon-lit streets and sleepless moments.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, lineHeight = 20.sp)
                    Text("Dreamy synths, nostalgic vocals, and late-night vibes.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, lineHeight = 20.sp)
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                // Left Column: Tracks
                Column(modifier = Modifier.weight(2f)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Text("Tracks", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${p.songs.size} Songs", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.Schedule, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Divider(color = BorderLow)
                    Spacer(Modifier.height(8.dp))
                    
                    p.songs.forEachIndexed { index, song ->
                        val isCurrent = song.id == currentSong?.id
                        val isPlaying = isCurrent && playbackState == PlaybackState.PLAYING
                        AlbumTrackRow(song, index + 1, isCurrent, isPlaying, onPlay = { player.playSong(song, index) })
                        Spacer(Modifier.height(4.dp))
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Text("Show more", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }

                // Right Column: Credits, Featured, More
                Column(modifier = Modifier.weight(1f)) {
                    // Credits
                    SectionHeaderRight("Credits")
                    Spacer(Modifier.height(16.dp))
                    CreditRow(p.album.thumbnail, artistName, "Primary Artist, Producer, Composer")
                    CreditRow(null, "Luna Wave", "Co-Producer, Synthesizer")
                    CreditRow(null, "M. Solaris", "Mixing Engineer")
                    CreditRow(null, "Neon Studios", "Recording Studio")
                    
                    Spacer(Modifier.height(40.dp))
                    
                    // Featured Artists
                    SectionHeaderRight("Featured Artists")
                    Spacer(Modifier.height(16.dp))
                    if (featuredArtists.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            featuredArtists.forEach { artist ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(32.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Surface3)) {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(artist.name.take(1).uppercase(), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
}
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(artist.name, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                }
                            }
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(32.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Surface3))
                                Spacer(Modifier.width(8.dp))
                                Text("Luna Wave", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(32.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Surface3))
                                Spacer(Modifier.width(8.dp))
                                Text("M. Solaris", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(40.dp))
                    
                    // More by this artist
                    SectionHeaderRight("More by this artist")
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MiniAlbumCard(Modifier.weight(1f), "Lumina", "2022")
                        MiniAlbumCard(Modifier.weight(1f), "Echoes", "2021")
                        MiniAlbumCard(Modifier.weight(1f), "Parallel", "2020")
                        MiniAlbumCard(Modifier.weight(1f), "Daydreams", "2019")
                    }
                    
                    Spacer(Modifier.height(40.dp))
                    
                    // Fans also like
                    SectionHeaderRight("Fans also like")
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        FanArtist(Modifier.weight(1f), "The Midnight")
                        FanArtist(Modifier.weight(1f), "Timecop1983")
                        FanArtist(Modifier.weight(1f), "FM-84")
                        FanArtist(Modifier.weight(1f), "Yung Bae")
                        FanArtist(Modifier.weight(1f), "Kavinsky")
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeaderRight(title: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
        Text("Show all", style = MaterialTheme.typography.labelMedium, color = IrisSoft, modifier = Modifier.clickable { })
    }
}

@Composable
private fun CreditRow(imageUrl: String?, name: String, role: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Surface3)) {
            if (imageUrl != null) {
                AsyncImage(model = imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(name, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            Text(role, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
private fun MiniAlbumCard(modifier: Modifier, title: String, year: String) {
    Column(modifier = modifier) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(Shapes.small).background(Surface3))
        Spacer(Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.bodySmall, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(year, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

@Composable
private fun FanArtist(modifier: Modifier, name: String) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(androidx.compose.foundation.shape.CircleShape).background(Surface3))
        Spacer(Modifier.height(8.dp))
        Text(name, style = MaterialTheme.typography.labelSmall, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun AlbumTrackRow(song: SongItem, index: Int, isCurrent: Boolean, isPlaying: Boolean, onPlay: () -> Unit) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val bg = when {
        isHovered -> BgCardHover
        else -> Color.Transparent
    }

    Surface(
        modifier = Modifier.fillMaxWidth().height(48.dp).clip(Shapes.small).background(bg).hoverable(interactionSource).clickable(interactionSource = interactionSource, indication = null, onClick = onPlay),
        color = Color.Transparent
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (isCurrent) {
                Box(modifier = Modifier.width(32.dp), contentAlignment = Alignment.CenterStart) {
                    PlayingIndicatorBox(isActive = true, playWhenReady = isPlaying, color = IrisSoft)
                }
            } else {
                Text(index.toString(), style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.width(32.dp))
            }
            
            Text(song.title, style = MaterialTheme.typography.titleSmall, color = if (isCurrent) IrisSoft else TextPrimary, modifier = Modifier.weight(1.5f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            
            val artistName = song.artists.firstOrNull()?.name ?: ""
            Text(artistName, style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            
            if (isCurrent) {
                Icon(Icons.Default.GraphicEq, null, tint = IrisSoft, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(16.dp))
            }
            
            song.duration?.let { dur ->
                Text("${dur / 60}:${(dur % 60).toString().padStart(2, '0')}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            
            Spacer(Modifier.width(16.dp))
            Icon(Icons.Default.Add, null, tint = if (isHovered) TextPrimary else Color.Transparent, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(16.dp))
            Icon(Icons.Default.MoreHoriz, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        }
    }
}

