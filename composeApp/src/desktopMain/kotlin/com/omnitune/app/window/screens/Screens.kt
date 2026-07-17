package com.omnitune.app.window.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import coil3.compose.AsyncImage
import com.omnitune.app.player.NavScreen
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.app.platform.PlaybackState
import com.omnitune.app.service.YouTubeService
import com.omnitune.app.window.*
import com.omnitune.app.window.components.*
import com.omnitune.innertube.models.*
import com.omnitune.innertube.pages.*
import com.omnitune.innertube.toHighResThumbnail
import org.koin.compose.koinInject

private fun firstSong(items: List<YTItem>): SongItem? =
    items.firstOrNull { it is SongItem } as? SongItem

// ---------------------------------------------------------------------------
// HOME
// ---------------------------------------------------------------------------


@Composable
private fun SectionCarousel(title: String, items: List<YTItem>, player: PlayerViewModel, currentSong: SongItem?, playbackState: PlaybackState, liked: Set<String>, onItemClick: ((YTItem) -> Unit)? = null) {
    Column {
        OmniSectionHeader(title, modifier = Modifier.padding(bottom = 12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
            items(items) { item -> CarouselCard(item, player, currentSong, playbackState, liked, onItemClick) }
        }
    }
}

@Composable
private fun CarouselCard(item: YTItem, player: PlayerViewModel, currentSong: SongItem?, playbackState: PlaybackState, liked: Set<String>, onItemClick: ((YTItem) -> Unit)? = null) {
    val w = 170.dp
    if (onItemClick != null) {
        val (subtitle, thumb) = when (item) {
            is SongItem -> (item.artists.firstOrNull()?.name ?: "") to item.thumbnail
            is AlbumItem -> (item.artists?.firstOrNull()?.name ?: "") to item.thumbnail
            is ArtistItem -> "Artist" to item.thumbnail
            is PlaylistItem -> (item.author?.name ?: "") to item.thumbnail
        }
        OmniMediaCard(
            item.title,
            subtitle,
            thumb,
            Modifier.width(w),
            onPlay = { onItemClick(item) },
            onClick = { onItemClick(item) }
        )
    } else {
        when (item) {
            is SongItem -> CompactSongCard(item, player, currentSong, playbackState, Modifier.width(w))
            is AlbumItem -> OmniMediaCard(item.title, item.artists?.firstOrNull()?.name, item.thumbnail, Modifier.width(w), onPlay = { player.openAlbum(item.browseId) }, onClick = { player.openAlbum(item.browseId) })
            is ArtistItem -> OmniMediaCard(item.title, "Artist", item.thumbnail, Modifier.width(w), onPlay = { player.openArtist(item.id) }, onClick = { player.openArtist(item.id) })
            is PlaylistItem -> OmniMediaCard(item.title, item.author?.name, item.thumbnail, Modifier.width(w), onClick = { player.openPlaylist(item.id) }, onPlay = { player.playPlaylist(item.id) })
        }
    }
}

@Composable
private fun CompactSongCard(item: SongItem, player: PlayerViewModel, currentSong: SongItem?, playbackState: PlaybackState, modifier: Modifier = Modifier) {
    val isActive = item.id == currentSong?.id
    val isPlaying = isActive && playbackState == PlaybackState.PLAYING
    Column(modifier = modifier.clip(Shapes.medium).background(Surface1).border(1.dp, BorderLow, Shapes.medium).clickable { player.playSong(item, -1) }.padding(Spacing.small)) {
        Box(Modifier.fillMaxWidth().aspectRatio(1f).clip(Shapes.artworkMedium)) {
            AsyncImage(model = item.thumbnail.toHighResThumbnail(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            if (isActive) Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
                PlayingIndicatorBox(isActive = true, playWhenReady = isPlaying, color = IrisSoft)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(item.title, style = MaterialTheme.typography.titleSmall, color = if (isActive) IrisSoft else TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(item.artists.joinToString { it.name }, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// ---------------------------------------------------------------------------
// BROWSE / RADIO
// ---------------------------------------------------------------------------

@Composable
private fun LegacyBrowseView(player: PlayerViewModel) {
    val service = koinInject<YouTubeService>()
    var explorePage by remember { mutableStateOf<ExplorePage?>(null) }
    var chartsPage by remember { mutableStateOf<ChartsPage?>(null) }
    var selectedGenre by remember { mutableStateOf<MoodAndGenres.Item?>(null) }
    var genrePage by remember { mutableStateOf<BrowseResult?>(null) }
    var genreLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var genreError by remember { mutableStateOf<String?>(null) }
    var browseRetryNonce by remember { mutableStateOf(0) }
    var genreRetryNonce by remember { mutableStateOf(0) }

    val currentSong by player.currentSong.collectAsState()
    val playbackState by player.playbackState.collectAsState()
    val liked by player.likedSongs.collectAsState()

    LaunchedEffect(browseRetryNonce) {
        error = null
        runCatching {
            val explore = service.explore()
            val charts = service.getChartsPage()
            explore to charts
        }.onSuccess { (explore, charts) ->
            explorePage = explore
            chartsPage = charts
        }.onFailure {
            error = it.message
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            OmniSectionHeader(selectedGenre?.title ?: "Browse")
            if (selectedGenre != null) {
                OmniSurface(
                    modifier = Modifier
                        .clip(Shapes.pill)
                        .clickable {
                            selectedGenre = null
                            genrePage = null
                            genreError = null
                            genreLoading = false
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Back to Browse", color = TextPrimary, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        when {
            error != null -> ProviderRetryState(
                title = "Couldn't load Browse",
                message = "Some provider sections are unavailable. Retry will reload the live Browse shelves.",
                onRetry = { browseRetryNonce++ },
            )
            explorePage == null || chartsPage == null -> ProviderHomeShimmer()
            selectedGenre != null -> {
                LaunchedEffect(selectedGenre, genreRetryNonce) {
                    val genre = selectedGenre ?: return@LaunchedEffect
                    genreLoading = true
                    genreError = null
                    genrePage = null
                    runCatching {
                        service.browse(genre.endpoint.browseId, genre.endpoint.params)
                            .filterExplicit()
                            .filterVideo()
                    }.onSuccess {
                        genrePage = it
                    }.onFailure {
                        genreError = it.message ?: "Genre failed to load"
                    }
                    genreLoading = false
                }

                when {
                    genreLoading -> ProviderHomeShimmer()
                    genreError != null -> ProviderRetryState(
                        title = "Couldn't load ${selectedGenre?.title ?: "section"}",
                        message = "This provider section failed to load. Other Browse sections remain available.",
                        onRetry = { genreRetryNonce++ },
                    )
                    genrePage?.items.isNullOrEmpty() -> OmniEmptyState("No browse results", "The provider returned no playable items for this section.")
                    else -> {
                        val loadedGenre = selectedGenre
                        val loadedGenrePage = genrePage
                        if (loadedGenre == null || loadedGenrePage == null) return@Column
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
                            loadedGenrePage.items
                                .filter { it.items.isNotEmpty() }
                                .forEach { section ->
                                    item {
                                        SectionCarousel(
                                            title = section.title ?: loadedGenrePage.title ?: loadedGenre.title,
                                            items = section.items.distinctBy { it.id },
                                            player = player,
                                            currentSong = currentSong,
                                            playbackState = playbackState,
                                            liked = liked,
                                        )
                                    }
                                }
                        }
                    }
                }
            }
            else -> {
                val loadedExplorePage = explorePage ?: return@Column
                val loadedChartsPage = chartsPage ?: return@Column
                LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
                    item {
                        OmniSurface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(118.dp)
                                .clip(Shapes.large)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Iris.copy(alpha = 0.20f), Surface1.copy(alpha = 0.72f), CoolBlue.copy(alpha = 0.12f))
                                    )
                                )
                                .padding(24.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.Center) {
                                Text("Explore music from the active provider", color = TextPrimary, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(6.dp))
                                Text("Genres, new releases, charts and playlists are loaded live. Failed shelves do not hide working ones.", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    item {
                        val mg = loadedExplorePage.moodAndGenres
                        if (mg.isNotEmpty()) {
                            Text("Mood & Genres", style = MaterialTheme.typography.titleMedium, color = TextPrimary, modifier = Modifier.padding(bottom = Spacing.small))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                                contentPadding = PaddingValues(end = 48.dp),
                            ) {
                                items(mg) { g ->
                                    OmniSurface(modifier = Modifier.clickable { selectedGenre = g }.padding(vertical = 10.dp, horizontal = 16.dp)) {
                                        Text(g.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                    }
                                }
                            }
                        }
                    }
                    item {
                        if (loadedExplorePage.newReleaseAlbums.isNotEmpty()) {
                            SectionCarousel(
                                title = "New Releases",
                                items = loadedExplorePage.newReleaseAlbums,
                                player = player,
                                currentSong = currentSong,
                                playbackState = playbackState,
                                liked = liked
                            )
                        }
                    }
                    items(loadedChartsPage.sections) { section ->
                        SectionCarousel(
                            title = section.title,
                            items = section.items,
                            player = player,
                            currentSong = currentSong,
                            playbackState = playbackState,
                            liked = liked
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RadioView(player: PlayerViewModel) {
    var explorePage by remember { mutableStateOf<ExplorePage?>(null) }
    var chartsPage by remember { mutableStateOf<ChartsPage?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var retryNonce by remember { mutableStateOf(0) }
    val service = koinInject<YouTubeService>()
    
    val currentSong by player.currentSong.collectAsState()
    val playbackState by player.playbackState.collectAsState()
    val liked by player.likedSongs.collectAsState()
    val queue by player.queue.collectAsState()
    val playbackHistory by player.playbackHistory.collectAsState()
    val discoveryTrending by player.discoveryTrending.collectAsState()

    LaunchedEffect(retryNonce) {
        error = null
        runCatching { service.explore() to service.getChartsPage() }
            .onSuccess {
                explorePage = it.first
                chartsPage = it.second
            }
            .onFailure { error = it.message }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 34.dp, vertical = 10.dp)) {
        when {
            error != null -> ProviderRetryState(
                title = "Radio unavailable",
                message = "OmniTune couldn't load provider radio seeds. Retry will reload real playable seeds.",
                onRetry = { retryNonce++ },
            )
            explorePage == null || chartsPage == null -> ProviderHomeShimmer()
            else -> {
                val loadedExplorePage = explorePage ?: return@Column
                val loadedChartsPage = chartsPage ?: return@Column
                val historySongs = playbackHistory.map { it.song }
                val chartSongs = loadedChartsPage.sections.flatMap { it.items }.filterIsInstance<SongItem>()
                val radioSeeds = (listOfNotNull(currentSong) + queue + historySongs + discoveryTrending + chartSongs)
                    .distinctBy { it.id }
                    .take(24)
                val heroSeed = radioSeeds.firstOrNull()
                val endpointItems = (loadedChartsPage.sections.flatMap { it.items } + loadedExplorePage.newReleaseAlbums)
                    .distinctBy { it.id }
                    .filter {
                        (it is ArtistItem && it.radioEndpoint != null) ||
                            (it is PlaylistItem && it.radioEndpoint != null)
                    }
                var expandedRadioSections by remember { mutableStateOf(setOf<String>()) }
                fun toggleRadioSection(title: String) {
                    expandedRadioSections = if (title in expandedRadioSections) {
                        expandedRadioSections - title
                    } else {
                        expandedRadioSections + title
                    }
                }

                if (radioSeeds.isEmpty() && endpointItems.isEmpty()) {
                    OmniEmptyState("No radio seeds yet", "Play music or load provider charts to create real radio queues.")
                } else {
                    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(30.dp)) {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(bottom = 98.dp),
                        ) {
                            item {
                                Text("Radio", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(5.dp))
                                Text(
                                    "Lean back and tune into live stations, mood streams, and artist radio.",
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                )
                            }
                            item {
                                RadioHeroCard(
                                    seed = heroSeed,
                                    onListen = {
                                        heroSeed?.let { player.startRadio(it.id, "song") }
                                    },
                                    onAdd = {
                                        heroSeed?.let(player::addToQueue)
                                    },
                                    onMore = {
                                        heroSeed?.let { player.startRadio(it.id, "song") }
                                    },
                                )
                            }
                            item {
                                RadioShelf(
                                    title = "Live Stations",
                                    items = radioSeeds,
                                    expanded = "Live Stations" in expandedRadioSections,
                                    onToggleExpanded = { toggleRadioSection("Live Stations") },
                                    onPlay = { player.startRadio(it.id, "song") },
                                )
                            }
                            item {
                                val artistSeeds = endpointItems.take(8)
                                if (artistSeeds.isNotEmpty()) {
                                    RadioEndpointShelf(
                                        title = "Artist Radio",
                                        items = artistSeeds,
                                        expanded = "Artist Radio" in expandedRadioSections,
                                        onToggleExpanded = { toggleRadioSection("Artist Radio") },
                                        onPlay = { item ->
                                            when (item) {
                                                is ArtistItem -> item.radioEndpoint?.let(player::startRadio)
                                                is PlaylistItem -> item.radioEndpoint?.let(player::startRadio)
                                                else -> Unit
                                            }
                                        },
                                    )
                                }
                            }
                            item {
                                RadioShelf(
                                    title = "Mood Stations",
                                    items = radioSeeds.drop(8).ifEmpty { radioSeeds.take(6) },
                                    expanded = "Mood Stations" in expandedRadioSections,
                                    onToggleExpanded = { toggleRadioSection("Mood Stations") },
                                    onPlay = { player.startRadio(it.id, "song") },
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.width(360.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            RadioRailPanel(
                                "Recently Tuned",
                                radioSeeds,
                                expanded = "Recently Tuned" in expandedRadioSections,
                                onToggleExpanded = { toggleRadioSection("Recently Tuned") },
                            ) { player.startRadio(it.id, "song") }
                            RadioHostsPanel(
                                endpointItems,
                                expanded = "Featured Hosts" in expandedRadioSections,
                                onToggleExpanded = { toggleRadioSection("Featured Hosts") },
                            ) { item ->
                                when (item) {
                                    is ArtistItem -> item.radioEndpoint?.let(player::startRadio)
                                    is PlaylistItem -> item.radioEndpoint?.let(player::startRadio)
                                    else -> Unit
                                }
                            }
                            RadioRailPanel(
                                "What's Live Now",
                                radioSeeds.drop(5).ifEmpty { radioSeeds.take(4) },
                                expanded = "What's Live Now" in expandedRadioSections,
                                onToggleExpanded = { toggleRadioSection("What's Live Now") },
                            ) { player.startRadio(it.id, "song") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RadioHeroCard(seed: SongItem?, onListen: () -> Unit, onAdd: () -> Unit, onMore: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(232.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(OmniReferenceColors.SurfaceBase)
            .border(1.dp, OmniReferenceColors.Accent.copy(alpha = 0.28f), RoundedCornerShape(12.dp))
    ) {
        AsyncImage(
            model = seed?.thumbnail?.toHighResThumbnail(),
            contentDescription = seed?.title ?: "Radio",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.42f,
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        0f to Color(0xF0030917),
                        0.42f to Color(0xC8050B1A),
                        0.72f to Color(0x77110E32),
                        1f to Color(0xB4050B18),
                    )
                )
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF5C48DE).copy(alpha = 0.26f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(780f, 40f),
                        radius = 520f,
                    )
                )
        )
        Column(Modifier.align(Alignment.CenterStart).padding(start = 20.dp).width(420.dp)) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFE6225B))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text("LIVE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(seed?.title ?: "Midnight FM", color = TextPrimary, fontSize = 27.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.width(14.dp))
                Icon(Icons.Default.GraphicEq, null, tint = OmniReferenceColors.Accent, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                seed?.artists?.joinToString(", ") { it.name } ?: "Live radio from your listening activity.",
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier
                        .height(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(OmniGradients.primaryAction)
                        .clickable(enabled = seed != null, onClick = onListen)
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Listen Live", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                RadioRoundButton(Icons.Default.Add, "Add to queue", enabled = seed != null, onClick = onAdd)
                RadioRoundButton(Icons.Default.MoreHoriz, "Start radio", enabled = seed != null, onClick = onMore)
            }
        }
    }
}

@Composable
private fun RadioRoundButton(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, enabled: Boolean = true, onClick: () -> Unit = {}) {
    Box(
        Modifier
            .size(42.dp)
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(OmniReferenceColors.SurfaceDeepRaised.copy(alpha = 0.86f))
            .border(1.dp, BorderLow.copy(alpha = 0.72f), androidx.compose.foundation.shape.CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, description, tint = if (enabled) TextSecondary else TextMuted, modifier = Modifier.size(19.dp))
    }
}

@Composable
private fun RadioShelf(title: String, items: List<SongItem>, expanded: Boolean, onToggleExpanded: () -> Unit, onPlay: (SongItem) -> Unit) {
    if (items.isEmpty()) return
    val visibleItems = items.take(if (expanded) items.size else 8)
    Column {
        RadioSectionHeader(title, if (expanded) "Show less" else "See all", enabled = items.size > 8, onAction = onToggleExpanded)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            items(visibleItems) { item ->
                RadioStationCard(item, onPlay)
            }
        }
    }
}

@Composable
private fun RadioEndpointShelf(title: String, items: List<YTItem>, expanded: Boolean, onToggleExpanded: () -> Unit, onPlay: (YTItem) -> Unit) {
    val visibleItems = items.take(if (expanded) items.size else 8)
    Column {
        RadioSectionHeader(title, if (expanded) "Show less" else "See all", enabled = items.size > 8, onAction = onToggleExpanded)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(visibleItems) { item ->
                RadioEndpointCard(item, onPlay)
            }
        }
    }
}

@Composable
private fun RadioSectionHeader(title: String, action: String, enabled: Boolean, onAction: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        Text(
            action,
            color = if (enabled) OmniReferenceColors.AccentSoft else TextMuted,
            fontSize = 12.sp,
            modifier = Modifier.clickable(enabled = enabled, onClick = onAction),
        )
    }
}

@Composable
private fun RadioStationCard(item: SongItem, onPlay: (SongItem) -> Unit) {
    Column(
        Modifier
            .width(144.dp)
            .height(170.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.86f))
            .border(1.dp, BorderLow.copy(alpha = 0.62f), RoundedCornerShape(10.dp))
            .clickable { onPlay(item) }
            .padding(10.dp)
    ) {
        Box(Modifier.fillMaxWidth().height(86.dp).clip(RoundedCornerShape(8.dp))) {
            AsyncImage(item.thumbnail.toHighResThumbnail(), item.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xB0030917)))))
            Icon(
                Icons.Default.PlayArrow,
                null,
                tint = Color(0xFFE1E4FF),
                modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp).size(24.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color(0xFFE1E4FF).copy(alpha = 0.18f)).padding(4.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(item.title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(item.artists.joinToString(", ") { it.name }, color = TextSecondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.weight(1f))
        Icon(Icons.Default.GraphicEq, null, tint = OmniReferenceColors.Accent, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun RadioEndpointCard(item: YTItem, onPlay: (YTItem) -> Unit) {
    Column(
        Modifier
            .width(144.dp)
            .height(142.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.82f))
            .border(1.dp, BorderLow.copy(alpha = 0.62f), RoundedCornerShape(10.dp))
            .clickable { onPlay(item) }
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(item.thumbnail?.toHighResThumbnail(), item.title, Modifier.size(70.dp).clip(androidx.compose.foundation.shape.CircleShape), contentScale = ContentScale.Crop)
        Spacer(Modifier.height(8.dp))
        Text(item.title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text("Inspired radio", color = TextSecondary, fontSize = 10.sp)
    }
}

@Composable
private fun RadioRailPanel(title: String, items: List<SongItem>, expanded: Boolean, onToggleExpanded: () -> Unit, onPlay: (SongItem) -> Unit) {
    val visibleItems = items.take(if (expanded) items.size else 5)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.82f))
            .border(1.dp, BorderLow.copy(alpha = 0.62f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text(
                if (expanded) "Show less" else "See all",
                color = if (items.size > 5) OmniReferenceColors.AccentSoft else TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.clickable(enabled = items.size > 5, onClick = onToggleExpanded),
            )
        }
        Spacer(Modifier.height(12.dp))
        visibleItems.forEach { item ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clickable { onPlay(item) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsyncImage(item.thumbnail.toHighResThumbnail(), item.title, Modifier.size(34.dp).clip(RoundedCornerShape(6.dp)), contentScale = ContentScale.Crop)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(item.artists.joinToString(", ") { it.name }, color = TextSecondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Icon(Icons.Default.PlayArrow, null, tint = Color(0xFFD8DCFF), modifier = Modifier.size(25.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color(0xFFD8DCFF).copy(alpha = 0.16f)).padding(5.dp))
            }
        }
    }
}

@Composable
private fun RadioHostsPanel(items: List<YTItem>, expanded: Boolean, onToggleExpanded: () -> Unit, onPlay: (YTItem) -> Unit) {
    val visibleItems = items.take(if (expanded) items.size else 4)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.82f))
            .border(1.dp, BorderLow.copy(alpha = 0.62f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Featured Hosts", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text(
                if (expanded) "Show less" else "See all",
                color = if (items.size > 4) OmniReferenceColors.AccentSoft else TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.clickable(enabled = items.size > 4, onClick = onToggleExpanded),
            )
        }
        Spacer(Modifier.height(12.dp))
        visibleItems.forEach { item ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clickable { onPlay(item) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsyncImage(item.thumbnail?.toHighResThumbnail(), item.title, Modifier.size(34.dp).clip(RoundedCornerShape(6.dp)), contentScale = ContentScale.Crop)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Provider radio", color = TextSecondary, fontSize = 10.sp)
                }
                Box(Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFE6225B)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text("LIVE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.GraphicEq, null, tint = OmniReferenceColors.Accent, modifier = Modifier.size(18.dp))
            }
        }
    }
}
@Composable
private fun LazyVerticalGridCells(items: List<AlbumItem>, player: PlayerViewModel) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        items(items.chunked(4)) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small), modifier = Modifier.fillMaxWidth()) {
                row.forEach { a ->
                    OmniMediaCard(a.title, a.artists?.firstOrNull()?.name, a.thumbnail, Modifier.weight(1f).padding(4.dp), onClick = { player.openAlbum(a.browseId) })
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// LIBRARY
// ---------------------------------------------------------------------------



// ---------------------------------------------------------------------------
// DOWNLOADS
// ---------------------------------------------------------------------------

