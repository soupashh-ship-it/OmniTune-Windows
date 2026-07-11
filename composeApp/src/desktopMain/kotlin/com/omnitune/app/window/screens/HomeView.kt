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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material.icons.filled.Pause
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha

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

    val allItems = home.sections.flatMap { it.items }
    val allSongs = allItems.filterIsInstance<SongItem>()
    
    // 1. Featured Hero
    val featuredItem = allItems.firstOrNull { (it is AlbumItem || it is PlaylistItem) && it.thumbnail != null && it.id !in usedIds }
    if (featuredItem != null) usedIds.add(featuredItem.id)

    // 2. Companion Rail
    val companionItems = filterUnused(allSongs.ifEmpty { allItems.filter { it is AlbumItem || it is PlaylistItem } }, 4)

    // 3. Continue Listening
    val continueListeningCandidate = classifiedSections[HomeSectionType.ContinueListening]?.items
        ?: allSongs.ifEmpty { allItems }
    val continueListening = filterUnused(continueListeningCandidate, 4)

    // 4. Quick Picks
    val quickPicksCandidate = classifiedSections[HomeSectionType.QuickPicks]?.items ?: allItems
    val quickPicks = filterUnused(quickPicksCandidate, 6)

    // 5. Made For You
    val madeForYouCandidate = classifiedSections[HomeSectionType.Personalized]?.items ?: allItems.filter { it is PlaylistItem || it is AlbumItem }
    val madeForYou = filterUnused(madeForYouCandidate, 3)

    // 6. Trending Now
    val trendingCandidate = classifiedSections[HomeSectionType.Trending]?.items ?: allSongs.ifEmpty { allItems }
    val trending = filterUnused(trendingCandidate, 5)

    // 7. New Releases
    val newReleasesCandidate = classifiedSections[HomeSectionType.NewReleases]?.items ?: allItems.filter { it is AlbumItem || it is PlaylistItem }
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
                HomeHeroRow(featuredItem, companionItems, player, currentSong, playbackState)
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
                            Column(modifier = Modifier.weight(0.45f)) {
                                SectionHeader("Trending Now")
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    trending.forEachIndexed { index, song ->
                                        TrendingRow(song, index + 1, player)
                                    }
                                }
                            }
                        }
                        if (newReleases.isNotEmpty()) {
                            Column(modifier = Modifier.weight(0.55f)) {
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
private fun ContinueListeningRow(item: YTItem, isActive: Boolean, isPlaying: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val shape = RoundedCornerShape(10.dp)
    
    val rowBackground by animateColorAsState(
        targetValue = when {
            isActive -> Color(0xCC11172C)
            isHovered -> Color(0xFF181D35)
            else -> Color(0xA60D1222)
        },
        animationSpec = tween(160),
        label = "ContinueListeningBackground",
    )

    Box(
        modifier = Modifier.fillMaxWidth().height(62.dp)
            .clip(shape)
            .background(rowBackground)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.045f),
                shape = shape,
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        // Left state accent.
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(vertical = 7.dp)
                .width(2.5.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(99.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF8B7CFF),
                            Color(0xFF5D7FFF),
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 10.dp,
                    end = 8.dp,
                    top = 7.dp,
                    bottom = 7.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = item.thumbnail?.toHighResThumbnail(),
                contentDescription = null,
                modifier = Modifier.size(48.dp).clip(Shapes.small),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(9.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = item.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFFF4F3FA),
                    style = MaterialTheme.typography.titleSmall
                )
                
                val artists = when (item) {
                    is SongItem -> item.artists?.joinToString(", ") { it.name ?: "" } ?: ""
                    is AlbumItem -> item.artists?.joinToString(", ") { it.name ?: "" } ?: ""
                    else -> ""
                }

                if (artists.isNotBlank()) {
                    Text(
                        text = artists,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color(0xFFA9AEC2),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.height(5.dp))

                // Progress
                val progress = if (isActive && isPlaying) 0.45f else null
                if (progress != null) {
                    val safeProgress = progress.coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(
                                Color.White.copy(alpha = 0.09f)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(safeProgress)
                                .clip(RoundedCornerShape(99.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = if (isActive) {
                                            listOf(
                                                Color(0xFF8B7CFF),
                                                Color(0xFF6B8CFF)
                                            )
                                        } else {
                                            listOf(
                                                Color(0xFF7568E8),
                                                Color(0xFF617FE0)
                                            )
                                        }
                                    )
                                )
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Color.White.copy(alpha=0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (isActive && isPlaying) {
                    Icon(Icons.Default.Pause, null, tint = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
@Composable
private fun QuickPicksCard(item: YTItem, player: PlayerViewModel) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val scale by animateFloatAsState(if (isHovered) 1.015f else 1f, tween(160))

    Box(
        modifier = Modifier
            .width(130.dp)
            .height(140.dp)
            .scale(scale)
            .clip(Shapes.medium)
            .border(1.dp, Color.White.copy(alpha = 0.05f), Shapes.medium)
            .clickable(interactionSource = interactionSource, indication = null) {
                if (item is AlbumItem) player.openAlbum(item.browseId)
                else if (item is PlaylistItem) player.playPlaylist(item.id)
                else if (item is ArtistItem) player.openArtist(item.id)
                else if (item is SongItem) player.playSong(item, 0)
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
        if (isHovered) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(40.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
    }
}


@Composable
private fun MadeForYouCard(item: YTItem, player: PlayerViewModel) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val bgTint = remember(item.id) {
        val colors = listOf(
            Color(0xFF245063), // muted teal
            Color(0xFF38235C), // smoked violet
            Color(0xFF5C233E), // deep rose
            Color(0xFF1D264F)  // cool indigo
        )
        colors[kotlin.math.abs(item.id.hashCode()) % colors.size]
    }

    Box(
        modifier = Modifier
            .width(160.dp)
            .height(130.dp)
            .clip(Shapes.medium)
            .background(Brush.linearGradient(
                colors = listOf(
                    bgTint.copy(alpha = 0.35f),
                    Color(0xFF11172A),
                    Color(0xFF0B1020),
                )
            ))
            .border(1.dp, Color.White.copy(alpha = 0.06f), Shapes.medium)
            .clickable(interactionSource = interactionSource, indication = null) {
                if (item is PlaylistItem) player.playPlaylist(item.id)
                else if (item is AlbumItem) player.openAlbum(item.browseId)
            }
    ) {
        AsyncImage(
            model = item.thumbnail?.toHighResThumbnail(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().alpha(0.15f),
            contentScale = ContentScale.Crop
        )
        
        Column(modifier = Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(item.title, style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp), color = TextWhite, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                val sub = when (item) {
                    is PlaylistItem -> "Based on your recent listening"
                    is AlbumItem -> item.artists?.joinToString(", ") { it.name ?: "" } ?: ""
                    else -> ""
                }
                Text(sub, style = MaterialTheme.typography.labelSmall, color = TextSecondary, maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).padding(end = 8.dp))
                
                Box(modifier = Modifier.size(32.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
@Composable
private fun TrendingRow(item: YTItem, rank: Int, player: PlayerViewModel) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier = Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(6.dp))
            .background(if (isHovered) Color.White.copy(alpha = 0.035f) else Color.Transparent)
            .clickable(interactionSource = interactionSource, indication = null) { 
                if (item is SongItem) player.playSong(item) 
            }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(rank.toString(), style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.width(24.dp))
        AsyncImage(model = item.thumbnail?.toHighResThumbnail(), contentDescription = null, modifier = Modifier.size(36.dp).clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            val artists = when (item) {
                is SongItem -> item.artists?.joinToString(", ") { it.name ?: "" } ?: ""
                is AlbumItem -> item.artists?.joinToString(", ") { it.name ?: "" } ?: ""
                else -> ""
            }
            Text(artists, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        val albumName = when (item) {
            is SongItem -> item.album?.name ?: ""
            is AlbumItem -> "Album"
            is PlaylistItem -> "Playlist"
            is ArtistItem -> "Artist"
            else -> ""
        }
        Text(albumName, style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.weight(0.8f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text("3:14", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Spacer(Modifier.width(16.dp))
        Icon(Icons.Default.Add, contentDescription = "Add", tint = TextSecondary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(16.dp))
        Icon(Icons.Default.MoreHoriz, contentDescription = "More", tint = TextSecondary, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun NewReleaseCard(item: YTItem, player: PlayerViewModel) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val title = item.title
    val subtitle = when(item) {
        is AlbumItem -> item.artists?.joinToString(", ") { it.name ?: "" } ?: ""
        is SongItem -> item.artists?.joinToString(", ") { it.name ?: "" } ?: ""
        is PlaylistItem -> "Playlist"
        is ArtistItem -> "Artist"
        else -> ""
    }
    
    Box(modifier = Modifier.width(120.dp).height(150.dp).clip(RoundedCornerShape(8.dp)).background(Color.Transparent)
        .clickable(interactionSource = interactionSource, indication = null) { 
            if (item is AlbumItem) player.openAlbum(item.browseId)
            else if (item is SongItem) player.playSong(item)
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(8.dp))) {
                AsyncImage(model = item.thumbnail?.toHighResThumbnail(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                if (isHovered) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}



@Composable
private fun FeaturedCompanionRow(item: YTItem, selected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val background by animateColorAsState(
        targetValue = if (selected) Color(0xFF171D49) else if (isHovered) Color(0x33171D49) else Color.Transparent,
        animationSpec = tween(160)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = item.thumbnail?.toHighResThumbnail(),
            contentDescription = null,
            modifier = Modifier.size(46.dp).clip(Shapes.small),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color(0xFFF4F3FA), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            val sub = when (item) {
                is SongItem -> item.artists?.joinToString(", ") { it.name ?: "" } ?: ""
                is AlbumItem -> item.artists?.joinToString(", ") { it.name ?: "" } ?: ""
                is PlaylistItem -> "Playlist"
                is ArtistItem -> "Artist"
                else -> ""
            }
            Text(sub, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color(0xFFA9AEC2), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun HomeHeroRow(album: YTItem?, continueListening: List<YTItem>, player: PlayerViewModel, currentSong: SongItem?, playbackState: PlaybackState) {
    val shape = RoundedCornerShape(14.dp)
    
    Row(modifier = Modifier.fillMaxWidth().height(290.dp).clip(shape).border(1.dp, Color.White.copy(alpha = 0.055f), shape)) {
        // Left side: Cinematic main hero
        Box(modifier = Modifier.weight(2.65f).fillMaxHeight().background(Color(0xFF0C1122))) {
            if (album != null) {
                AsyncImage(
                    model = album.thumbnail?.toHighResThumbnail(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(
                    0.00f to Color(0xF2050918),
                    0.28f to Color(0xC8050918),
                    0.55f to Color(0x70050918),
                    0.82f to Color(0x18050918),
                    1.00f to Color(0x22050918)
                )))
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(
                    0.00f to Color.Transparent,
                    0.65f to Color.Transparent,
                    1.00f to Color(0x99040916)
                )))
                
                Column(modifier = Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Bottom) {
                    Text("FEATURED PLAYLIST", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA9AEC2), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(album.title, style = MaterialTheme.typography.displayMedium.copy(fontSize = 34.sp), color = Color(0xFFF4F3FA), fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(8.dp))
                    val subText = when (album) {
                        is AlbumItem -> album.artists?.joinToString(", ") { it.name ?: "" } ?: ""
                        is PlaylistItem -> "Personalized mix based on your listening."
                        else -> ""
                    }
                    Text(subText, style = MaterialTheme.typography.bodyLarge, color = Color(0xFFA9AEC2), maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 400.dp))
                    Spacer(Modifier.height(24.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(Shapes.pill)
                                .background(Brush.horizontalGradient(listOf(Color(0xFF7180FF), Color(0xFF8D70FF))))
                                .clickable { 
                                    if (album is AlbumItem) player.openAlbum(album.browseId)
                                    else if (album is PlaylistItem) player.playPlaylist(album.id)
                                    else if (album is SongItem) player.playSong(album)
                                }
                                .padding(horizontal = 28.dp, vertical = 12.dp)
                        ) {
                            Text("Play Now", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(16.dp))
                        Box(modifier = Modifier.size(40.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color.White.copy(alpha=0.08f)).clickable { }, contentAlignment=Alignment.Center) {
                            Icon(Icons.Default.MoreHoriz, null, tint = Color(0xFFA9AEC2), modifier = Modifier.size(22.dp))
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize().background(BgCard), contentAlignment = Alignment.Center) {
                    Text("No featured content", color = Color(0xFFA9AEC2))
                }
            }
        }
        
        // Vertical divider
        Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(Color.White.copy(alpha = 0.045f)))
        
        // Right side: Companion rail
        Column(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF0C1122)).padding(vertical = 12.dp, horizontal = 8.dp), verticalArrangement = Arrangement.Center) {
            continueListening.take(4).forEachIndexed { i, song ->
                val isSelected = i == 0 // Fake selected state for visual QA, or derive from real state
                FeaturedCompanionRow(song, selected = isSelected, onClick = { 
                    if (song is SongItem) player.playSong(song)
                    else if (song is PlaylistItem) player.playPlaylist(song.id)
                    else if (song is AlbumItem) player.openAlbum(song.browseId)
                })
                if (i < 3) {
                    Box(modifier = Modifier.fillMaxWidth().padding(start = 68.dp, top = 4.dp, bottom = 4.dp).height(1.dp).background(Color.White.copy(alpha = 0.045f)))
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
