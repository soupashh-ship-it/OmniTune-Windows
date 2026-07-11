package com.omnitune.app.window.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.filled.Add
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



enum class HomeSectionType {
    FeaturedHero, ContinueListening, QuickPicks, Personalized, Trending, NewReleases, Unknown
}

@Composable
private fun HomeContent(player: PlayerViewModel, home: HomePage, currentSong: SongItem?, playbackState: PlaybackState, liked: Set<String>) {
    // Robust Classification
    val usedIds = mutableSetOf<String>()
    
    // Helper to get unused items
    fun <T: YTItem> filterUnused(items: List<T>, take: Int): List<T> {
        val result = items.filter { it.id !in usedIds }.take(take)
        usedIds.addAll(result.map { it.id })
        return result
    }

    val classifiedSections = home.sections.associateBy { section ->
        val normalized = section.title.lowercase().trim()
        when {
            normalized.contains("quick") || normalized.contains("pick") || normalized.contains("listen again") -> HomeSectionType.QuickPicks
            normalized.contains("made for") || normalized.contains("mix") -> HomeSectionType.Personalized
            normalized.contains("new release") || normalized.contains("new album") -> HomeSectionType.NewReleases
            normalized.contains("trending") || normalized.contains("popular") || normalized.contains("chart") -> HomeSectionType.Trending
            normalized.contains("continue") || normalized.contains("jump back") -> HomeSectionType.ContinueListening
            else -> HomeSectionType.Unknown
        }
    }

    // 1. Featured Hero
    // Preferred: A playlist or album from "Personalized" or "Unknown" that has artwork.
    val featuredCandidate = home.sections
        .flatMap { it.items }
        .filterIsInstance<AlbumItem>()
        .firstOrNull { it.thumbnail != null && it.id !in usedIds }
        ?: home.sections.flatMap { it.items }.filterIsInstance<PlaylistItem>().firstOrNull { it.thumbnail != null && it.id !in usedIds }
    
    val featuredItem = featuredCandidate
    if (featuredItem != null) usedIds.add(featuredItem.id)

    // 2. Companion Rail (inside Hero row)
    // Preferred: 4 songs from anywhere
    val companionSongs = filterUnused(home.sections.flatMap { it.items }.filterIsInstance<SongItem>(), 4)

    // 3. Continue Listening
    val continueListeningCandidate = classifiedSections[HomeSectionType.ContinueListening]?.items?.filterIsInstance<SongItem>() 
        ?: home.sections.flatMap { it.items }.filterIsInstance<SongItem>()
    val continueListening = filterUnused(continueListeningCandidate, 4)

    // 4. Quick Picks
    val quickPicksCandidate = classifiedSections[HomeSectionType.QuickPicks]?.items 
        ?: home.sections.flatMap { it.items }.filter { it is AlbumItem || it is PlaylistItem || it is SongItem }
    val quickPicks = filterUnused(quickPicksCandidate, 6)

    // 5. Made For You
    val madeForYouCandidate = classifiedSections[HomeSectionType.Personalized]?.items 
        ?: home.sections.flatMap { it.items }.filter { it is PlaylistItem || it is AlbumItem }
    val madeForYou = filterUnused(madeForYouCandidate, 3)

    // 6. Trending Now
    val trendingCandidate = classifiedSections[HomeSectionType.Trending]?.items?.filterIsInstance<SongItem>()
        ?: home.sections.flatMap { it.items }.filterIsInstance<SongItem>()
    val trending = filterUnused(trendingCandidate, 5)

    // 7. New Releases
    val newReleasesCandidate = classifiedSections[HomeSectionType.NewReleases]?.items?.filter { it is AlbumItem || it is PlaylistItem }
        ?: home.sections.flatMap { it.items }.filter { it is AlbumItem || it is PlaylistItem }
    val newReleases = filterUnused(newReleasesCandidate, 5)

    BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 24.dp)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Good evening, Alex", style = MaterialTheme.typography.displaySmall, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 32.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OmniIconButton(onClick = {}, icon = Icons.Default.ChevronLeft, contentDescription = "Back", size = 32.dp, iconSize = 24.dp, tint = TextPrimary)
                        OmniIconButton(onClick = {}, icon = Icons.Default.ChevronRight, contentDescription = "Forward", size = 32.dp, iconSize = 24.dp, tint = TextPrimary)
                    }
                }
            }
            
            // ─── HERO & CONTINUE LISTENING ROW ─────────────────────────────
            item {
                Row(modifier = Modifier.fillMaxWidth().height(260.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    // Left side: Featured playlist/album card
                    OmniGlassSurface(
                        shape = Shapes.large,
                        style = GlassDefaults.card,
                        modifier = Modifier.weight(0.65f).fillMaxHeight()
                            .let { if (featuredItem != null) it.clickable { 
                                if (featuredItem is AlbumItem) player.openAlbum(featuredItem.browseId)
                                else if (featuredItem is PlaylistItem) player.playPlaylist(featuredItem.id)
                            } else it }
                    ) {
                        if (featuredItem != null) {
                            AsyncImage(
                                model = featuredItem.thumbnail?.toHighResThumbnail(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(
                                0f to Color(0xEE070B18),
                                0.35f to Color(0x99070B18),
                                0.70f to Color(0x33070B18),
                                1.0f to Color(0x66070B18)
                            )))
                            
                            Row(modifier = Modifier.fillMaxSize()) {
                                Column(modifier = Modifier.weight(1f).fillMaxHeight().padding(28.dp), verticalArrangement = Arrangement.Bottom) {
                                    Text("FEATURED", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                    Spacer(Modifier.height(8.dp))
                                    Text(featuredItem.title, style = MaterialTheme.typography.displayMedium, color = TextWhite, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Spacer(Modifier.height(4.dp))
                                    val subText = when (featuredItem) {
                                        is AlbumItem -> featuredItem.artists?.joinToString(", ") { it.name ?: "" } ?: ""
                                        is PlaylistItem -> "Personalized mix based on your listening"
                                        else -> ""
                                    }
                                    Text(subText, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Spacer(Modifier.height(20.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .clip(Shapes.pill)
                                                .background(OmniGradients.primaryAction)
                                                .padding(horizontal = 24.dp, vertical = 10.dp)
                                        ) {
                                            Text("Play Now", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(Modifier.width(16.dp))
                                        Box(modifier = Modifier.size(36.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color.White.copy(alpha=0.1f)), contentAlignment=Alignment.Center) {
                                            Icon(Icons.Default.MoreHoriz, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                                
                                // Companion Rail
                                if (companionSongs.isNotEmpty()) {
                                    Column(modifier = Modifier.width(220.dp).fillMaxHeight().background(Color.Black.copy(alpha = 0.2f)).padding(12.dp), verticalArrangement = Arrangement.Center) {
                                        companionSongs.forEachIndexed { i, song ->
                                            val isCurrent = song.id == currentSong?.id
                                            val isPlaying = isCurrent && playbackState == PlaybackState.PLAYING
                                            ContinueListeningRow(song, isActive = isCurrent, isPlaying = isPlaying, onClick = { player.playSong(song, i) })
                                            if (i < companionSongs.lastIndex) Spacer(Modifier.height(8.dp))
                                        }
                                    }
                                }
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxSize().background(BgCard), contentAlignment = Alignment.Center) {
                                Text("No featured content", color = TextSecondary)
                            }
                        }
                    }
                    
                    // Right side: Continue Listening
                    Column(modifier = Modifier.weight(0.35f).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                            Text("Continue Listening", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text("See all", style = MaterialTheme.typography.labelMedium, color = IrisSoft, modifier = Modifier.clickable { })
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            continueListening.forEachIndexed { i, song ->
                                val isCurrent = song.id == currentSong?.id
                                val isPlaying = isCurrent && playbackState == PlaybackState.PLAYING
                                ContinueListeningRow(song, isActive = isCurrent, isPlaying = isPlaying, onClick = { player.playSong(song, i) })
                            }
                        }
                    }
                }
            }
            
            // ─── QUICK PICKS & MADE FOR YOU ROW ────────────────────────────
            if (quickPicks.isNotEmpty() || madeForYou.isNotEmpty()) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        if (quickPicks.isNotEmpty()) {
                            Column(modifier = Modifier.weight(0.65f)) {
                                SectionHeader("Quick Picks")
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    items(quickPicks) { item -> QuickPicksCard(item, player) }
                                }
                            }
                        }
                        if (madeForYou.isNotEmpty()) {
                            Column(modifier = Modifier.weight(0.35f)) {
                                SectionHeader("Made for You", showSeeAll = true)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    items(madeForYou) { item -> MadeForYouCard(item, player) }
                                }
                            }
                        }
                    }
                }
            }
            
            // ─── TRENDING NOW & NEW RELEASES ROW ───────────────────────────
            if (trending.isNotEmpty() || newReleases.isNotEmpty()) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        if (trending.isNotEmpty()) {
                            Column(modifier = Modifier.weight(0.4f)) {
                                SectionHeader("Trending Now")
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    trending.forEachIndexed { index, song ->
                                        TrendingRow(song, index + 1, player)
                                    }
                                }
                            }
                        }
                        if (newReleases.isNotEmpty()) {
                            Column(modifier = Modifier.weight(0.6f)) {
                                SectionHeader("New Releases", showSeeAll = true)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(newReleases) { item -> NewReleaseCard(item, player) }
                                }
                            }
                        }
                    }
                }
            }
            
            item { Spacer(Modifier.height(40.dp)) }
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
            .background(if (isHovered || isActive) Surface3 else Surface1)
            .border(1.dp, if (isActive) Iris.copy(alpha=0.5f) else Color.Transparent, Shapes.medium)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.thumbnail?.toHighResThumbnail(),
            contentDescription = null,
            modifier = Modifier.size(40.dp).clip(Shapes.small),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(item.artists?.joinToString(", ") { it.name ?: "" } ?: "", style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (isHovered || isActive) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Color.White.copy(alpha=0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun QuickPicksCard(item: YTItem, player: PlayerViewModel) {
    OmniGlassSurface(
        shape = Shapes.medium,
        style = GlassDefaults.card,
        modifier = Modifier
            .width(130.dp)
            .height(130.dp)
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
            0.5f to Color.Transparent,
            1f to Color.Black.copy(alpha = 0.95f)
        )))
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.Bottom) {
            Text(item.title, style = MaterialTheme.typography.titleSmall, color = TextWhite, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            val sub = when (item) {
                is SongItem -> item.artists?.joinToString(", ") { it.name ?: "" }
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
    OmniGlassSurface(
        shape = Shapes.medium,
        style = GlassDefaults.card,
        modifier = Modifier
            .width(160.dp)
            .height(130.dp)
            .clickable {
                if (item is PlaylistItem) player.playPlaylist(item.id)
                else if (item is AlbumItem) player.openAlbum(item.browseId)
            }
    ) {
        // We'll use a strong dark gradient for Made For You cards if thumbnail isn't ideal,
        // but let's just use the thumbnail darkened.
        AsyncImage(
            model = item.thumbnail?.toHighResThumbnail(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1B1736).copy(alpha = 0.85f)))
        
        Column(modifier = Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(item.title, style = MaterialTheme.typography.titleSmall, color = TextWhite, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                val sub = when (item) {
                    is PlaylistItem -> "Based on your recent listening"
                    is AlbumItem -> item.artists?.joinToString(", ") { it.name ?: "" } ?: ""
                    else -> ""
                }
                Text(sub, style = MaterialTheme.typography.labelMedium, color = TextSecondary, maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).padding(end = 8.dp))
                
                Box(modifier = Modifier.size(28.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(16.dp))
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



@Composable
private fun SectionHeader(title: String, showSeeAll: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        if (showSeeAll) {
            Text("See all", style = MaterialTheme.typography.labelMedium, color = IrisSoft, modifier = Modifier.clickable { })
        }
    }
}

@Composable
private fun TrendingRow(item: SongItem, rank: Int, player: PlayerViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(6.dp)).clickable { player.playSong(item) }.padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(rank.toString(), style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.width(24.dp))
        AsyncImage(model = item.thumbnail?.toHighResThumbnail(), contentDescription = null, modifier = Modifier.size(36.dp).clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(item.artists?.joinToString(", ") { it.name ?: "" } ?: "", style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(item.album?.name ?: "", style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.weight(0.8f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text("3:14", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Spacer(Modifier.width(16.dp))
        Icon(Icons.Default.Add, contentDescription = "Add", tint = TextSecondary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(16.dp))
        Icon(Icons.Default.MoreHoriz, contentDescription = "More", tint = TextSecondary, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun NewReleaseCard(item: YTItem, player: PlayerViewModel) {
    val title = item.title
    val subtitle = when(item) {
        is AlbumItem -> item.artists?.joinToString(", ") { it.name ?: "" } ?: ""
        is SongItem -> item.artists?.joinToString(", ") { it.name ?: "" } ?: ""
        is PlaylistItem -> "Playlist"
        is ArtistItem -> "Artist"
        else -> ""
    }
    
    Box(modifier = Modifier.width(120.dp).height(120.dp).clip(RoundedCornerShape(8.dp)).clickable { 
        if (item is AlbumItem) player.openAlbum(item.browseId)
        else if (item is SongItem) player.playSong(item)
    }) {
        AsyncImage(model = item.thumbnail?.toHighResThumbnail(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)))))
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
