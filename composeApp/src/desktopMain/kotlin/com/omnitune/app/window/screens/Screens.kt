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
import kotlinx.coroutines.launch
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
            else -> "" to ""
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
            else -> OmniMediaCard(item.title, "", "", Modifier.width(w))
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

@Composable
private fun HomeShimmer() {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(40.dp), verticalArrangement = Arrangement.spacedBy(Spacing.section)) {
        item { OmniShimmerBlock(Modifier.width(280.dp).height(36.dp).clip(Shapes.small)) }
        item { OmniShimmerBlock(Modifier.fillMaxWidth().height(220.dp).clip(Shapes.large)) }
        items(3) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                items(6) { OmniShimmerBlock(Modifier.width(170.dp).height(210.dp).clip(Shapes.medium)) }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// BROWSE / RADIO
// ---------------------------------------------------------------------------

@Composable
private fun ProviderRetryState(title: String, message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.CloudOff, contentDescription = null, tint = TextMuted, modifier = Modifier.size(34.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Text(
                message.ifBlank { "The provider did not return this section." },
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.widthIn(max = 420.dp),
            )
            OmniSurface(
                modifier = Modifier
                    .clip(Shapes.pill)
                    .clickable(onClick = onRetry)
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text("Try again", color = TextPrimary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun BrowseView(player: PlayerViewModel) {
    val service = koinInject<YouTubeService>()
    val scope = rememberCoroutineScope()
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
        scope.launch {
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
            explorePage == null || chartsPage == null -> HomeShimmer()
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
                    genreLoading -> HomeShimmer()
                    genreError != null -> ProviderRetryState(
                        title = "Couldn't load ${selectedGenre!!.title}",
                        message = "This provider section failed to load. Other Browse sections remain available.",
                        onRetry = { genreRetryNonce++ },
                    )
                    genrePage?.items.isNullOrEmpty() -> OmniEmptyState("No browse results", "The provider returned no playable items for this section.")
                    else -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
                            genrePage!!.items
                                .filter { it.items.isNotEmpty() }
                                .forEach { section ->
                                    item {
                                        SectionCarousel(
                                            title = section.title ?: genrePage!!.title ?: selectedGenre!!.title,
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
                        val mg = explorePage!!.moodAndGenres
                        if (mg.isNotEmpty()) {
                            Text("Mood & Genres", style = MaterialTheme.typography.titleMedium, color = TextPrimary, modifier = Modifier.padding(bottom = Spacing.small))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                                items(mg) { g ->
                                    OmniSurface(modifier = Modifier.clickable { selectedGenre = g }.padding(vertical = 10.dp, horizontal = 16.dp)) {
                                        Text(g.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                    }
                                }
                            }
                        }
                    }
                    item {
                        if (explorePage!!.newReleaseAlbums.isNotEmpty()) {
                            SectionCarousel(
                                title = "New Releases",
                                items = explorePage!!.newReleaseAlbums,
                                player = player,
                                currentSong = currentSong,
                                playbackState = playbackState,
                                liked = liked
                            )
                        }
                    }
                    items(chartsPage!!.sections) { section ->
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
    val scope = rememberCoroutineScope()
    
    val currentSong by player.currentSong.collectAsState()
    val playbackState by player.playbackState.collectAsState()
    val liked by player.likedSongs.collectAsState()
    val queue by player.queue.collectAsState()
    val playbackHistory by player.playbackHistory.collectAsState()
    val discoveryTrending by player.discoveryTrending.collectAsState()

    LaunchedEffect(retryNonce) { 
        scope.launch { 
            error = null
            runCatching { service.explore() to service.getChartsPage() }
                .onSuccess {
                    explorePage = it.first
                    chartsPage = it.second
                }
                .onFailure { error = it.message } 
        } 
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 24.dp)) {
        OmniSectionHeader("Radio Hub", modifier = Modifier.padding(bottom = 8.dp))
        Text(
            "Start radio from real playable tracks and provider radio endpoints. OmniTune avoids fake stations.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        when {
            error != null -> ProviderRetryState(
                title = "Radio unavailable",
                message = "OmniTune couldn't load provider radio seeds. Retry will reload real playable seeds.",
                onRetry = { retryNonce++ },
            )
            explorePage == null || chartsPage == null -> HomeShimmer()
            else -> {
                val historySongs = playbackHistory.map { it.song }
                val chartSongs = chartsPage!!.sections.flatMap { it.items }.filterIsInstance<SongItem>()
                val radioSeeds = (listOfNotNull(currentSong) + queue + historySongs + discoveryTrending + chartSongs)
                    .distinctBy { it.id }
                    .take(24)
                val endpointItems = (chartsPage!!.sections.flatMap { it.items } + explorePage!!.newReleaseAlbums)
                    .distinctBy { it.id }
                    .filter {
                        (it is ArtistItem && it.radioEndpoint != null) ||
                            (it is PlaylistItem && it.radioEndpoint != null)
                    }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
                    item {
                        if (radioSeeds.isNotEmpty()) {
                            SectionCarousel(
                                title = "Start from a track",
                                items = radioSeeds,
                                player = player,
                                currentSong = currentSong,
                                playbackState = playbackState,
                                liked = liked,
                                onItemClick = { item ->
                                    if (item is SongItem) player.startRadio(item.id, "song")
                                }
                            )
                        }
                    }
                    item {
                        if (endpointItems.isNotEmpty()) {
                            SectionCarousel(
                                title = "Provider radio endpoints",
                                items = endpointItems,
                                player = player,
                                currentSong = currentSong,
                                playbackState = playbackState,
                                liked = liked,
                                onItemClick = { item ->
                                    when (item) {
                                        is ArtistItem -> item.radioEndpoint?.let(player::startRadio)
                                        is PlaylistItem -> item.radioEndpoint?.let(player::startRadio)
                                        else -> Unit
                                    }
                                }
                            )
                        }
                    }
                    item {
                        if (radioSeeds.isEmpty() && endpointItems.isEmpty()) {
                            OmniEmptyState("No radio seeds yet", "Play music or load provider charts to create real radio queues.")
                        }
                    }
                }
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

