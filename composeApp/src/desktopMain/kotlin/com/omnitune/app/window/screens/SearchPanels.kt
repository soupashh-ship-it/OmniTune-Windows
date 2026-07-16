package com.omnitune.app.window.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.app.platform.PlaybackState
import com.omnitune.app.window.BorderLow
import com.omnitune.app.window.IrisSoft
import com.omnitune.app.window.LocalHomeReferenceMetrics
import com.omnitune.app.window.LocalOmniMotionPolicy
import com.omnitune.app.window.OmniGradients
import com.omnitune.app.window.OmniReferenceColors
import com.omnitune.app.window.Shapes
import com.omnitune.app.window.Surface1
import com.omnitune.app.window.Surface2
import com.omnitune.app.window.TextMuted
import com.omnitune.app.window.TextPrimary
import com.omnitune.app.window.TextSecondary
import com.omnitune.app.window.components.OmniSearchField
import com.omnitune.innertube.models.AlbumItem
import com.omnitune.innertube.models.ArtistItem
import com.omnitune.innertube.models.PlaylistItem
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.models.YTItem
import com.omnitune.innertube.toHighResThumbnail

@Composable
internal fun SearchDiscoveryReferenceContent(
    query: String,
    onQueryChange: (String) -> Unit,
    recents: List<String>,
    genreTitles: List<String>,
    topResult: YTItem?,
    songs: List<SongItem>,
    trendingTerms: List<String>,
    artists: List<SearchArtistRow>,
    albums: List<AlbumItem>,
    playlists: List<PlaylistItem>,
    discoveryShelf: List<YTItem>,
    currentSong: SongItem?,
    playbackState: PlaybackState,
    loading: Boolean,
    error: String?,
    onSearch: (String) -> Unit,
    onClearRecents: () -> Unit,
    onPlayItem: (YTItem) -> Unit,
    onPlaySong: (SongItem, Int) -> Unit,
    onAddToQueue: (SongItem) -> Unit,
    onPlayNext: (SongItem) -> Unit,
    onLike: (SongItem) -> Unit,
    onOpenArtist: (String?) -> Unit,
    onSearchFieldFocusChanged: (Boolean) -> Unit = {},
) {
    val metrics = LocalHomeReferenceMetrics.current
    val motionPolicy = LocalOmniMotionPolicy.current
    val scroll = rememberScrollState()
    val left = metrics.px(30f)
    val rightX = metrics.px(659f)
    val panelShape = RoundedCornerShape(metrics.px(8f))
    var genresExpanded by rememberSaveable { mutableStateOf(false) }
    var expandedSection by rememberSaveable { mutableStateOf<SearchDiscoverySection?>(null) }
    val primaryGenres = genreTitles.take(7)
    val additionalGenres = genreTitles.drop(7)
    val contentYOffset by animateDpAsState(
        targetValue = if (genresExpanded) metrics.px(35f) else 0.dp,
        animationSpec = tween(durationMillis = motionPolicy.standardDurationMs, easing = FastOutSlowInEasing),
        label = "genresContentOffset",
    )
    val contentBaseY = metrics.px(207f) + contentYOffset

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (expandedSection == null) metrics.px(540f) + contentYOffset else metrics.px(760f) + contentYOffset)
        ) {
            Column(
                modifier = Modifier.offset(x = left, y = metrics.px(4f))
            ) {
                Text(
                    "Search & Discovery",
                    color = TextPrimary,
                    fontSize = 23.sp,
                    lineHeight = 27.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.25).sp,
                )
                Spacer(Modifier.height(metrics.px(2f)))
                Text(
                    "Find exactly what you're looking for and discover what moves you.",
                    color = TextSecondary,
                    fontSize = 10.5.sp,
                    lineHeight = 14.sp,
                )
            }

            OmniSearchField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = if (loading) "Searching..." else "Search for songs, artists, albums, playlists...",
                modifier = Modifier
                    .offset(x = left, y = metrics.px(51f))
                    .width(metrics.px(607f))
                    .height(metrics.px(36f)),
                onEnter = { onSearch(query) },
                onEscape = { onQueryChange("") },
                onFocusChanged = onSearchFieldFocusChanged,
            )

            Row(
                modifier = Modifier
                    .offset(x = left, y = metrics.px(103f))
                    .width(metrics.px(607f))
                    .height(metrics.px(16f)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionTitle("Recent Searches")
                Spacer(Modifier.weight(1f))
                Text(
                    "Clear",
                    color = IrisSoft,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable(onClick = onClearRecents),
                )
            }

            Row(
                modifier = Modifier
                    .offset(x = left, y = metrics.px(120f))
                    .width(metrics.px(607f))
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(metrics.px(6f)),
            ) {
                if (recents.isEmpty()) {
                    SearchChip("No recent searches yet", enabled = false)
                } else {
                    recents.take(8).forEach { term ->
                        SearchChip(term, icon = Icons.Default.Search) { onSearch(term) }
                    }
                }
            }

            SectionTitle(
                "Explore Genres",
                modifier = Modifier.offset(x = left, y = metrics.px(154f))
            )

            Column(
                modifier = Modifier
                    .offset(x = left, y = metrics.px(170f))
                    .width(metrics.px(607f))
                    .animateContentSize(animationSpec = tween(motionPolicy.standardDurationMs, easing = FastOutSlowInEasing)),
                verticalArrangement = Arrangement.spacedBy(metrics.px(6f)),
            ) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(metrics.px(6f)),
                ) {
                    primaryGenres.forEachIndexed { index, title ->
                        GenreChip(
                            title = title,
                            icon = searchGenreIcon(index),
                            onClick = { onSearch(title) },
                        )
                    }
                    if (additionalGenres.isNotEmpty()) {
                        MoreGenresActionChip(
                            expanded = genresExpanded,
                            onClick = { genresExpanded = !genresExpanded },
                        )
                    }
                }

                AnimatedVisibility(
                    visible = genresExpanded,
                    enter = expandVertically(
                        animationSpec = tween(durationMillis = motionPolicy.standardDurationMs, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(motionPolicy.shortDurationMs)),
                    exit = shrinkVertically(
                        animationSpec = tween(durationMillis = motionPolicy.standardDurationMs, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(motionPolicy.shortDurationMs)),
                ) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(metrics.px(6f)),
                    ) {
                        additionalGenres.forEachIndexed { index, title ->
                            GenreChip(
                                title = title,
                                icon = searchGenreIcon(index + primaryGenres.size),
                                onClick = { onSearch(title) },
                            )
                        }
                    }
                }
            }

            if (error != null) {
                ReferencePanel(
                    modifier = Modifier
                        .offset(x = left, y = contentBaseY)
                        .width(metrics.px(607f))
                        .height(metrics.px(90f)),
                    shape = panelShape,
                ) {
                    Column(Modifier.padding(metrics.px(16f))) {
                        Text("Couldn't load search", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(metrics.px(4f)))
                        Text(error, color = TextSecondary, fontSize = 10.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            } else if (expandedSection != null) {
                val activeExpandedSection = expandedSection ?: return@Box
                ExpandedSearchSectionPanel(
                    section = activeExpandedSection,
                    query = query,
                    songs = songs,
                    trendingTerms = trendingTerms,
                    artists = artists,
                    albums = albums,
                    playlists = playlists,
                    discoveryShelf = discoveryShelf,
                    currentSong = currentSong,
                    playbackState = playbackState,
                    onBack = { expandedSection = null },
                    onSearch = onSearch,
                    onPlayItem = onPlayItem,
                    onPlaySong = onPlaySong,
                    onAddToQueue = onAddToQueue,
                    onPlayNext = onPlayNext,
                    onLike = onLike,
                    onOpenArtist = onOpenArtist,
                    modifier = Modifier
                        .offset(x = left, y = contentBaseY)
                        .width(metrics.px(909f))
                        .height(metrics.px(520f)),
                )
            } else {
                TopResultPanel(
                    item = topResult,
                    onPlayItem = onPlayItem,
                    modifier = Modifier
                        .offset(x = left, y = contentBaseY)
                        .width(metrics.px(257f))
                        .height(metrics.px(184f)),
                )

                SongsPanel(
                    songs = songs,
                    currentSong = currentSong,
                    playbackState = playbackState,
                    onPlaySong = onPlaySong,
                    onAddToQueue = onAddToQueue,
                    onPlayNext = onPlayNext,
                    onLike = onLike,
                    onSeeAll = { expandedSection = SearchDiscoverySection.Songs },
                    modifier = Modifier
                        .offset(x = metrics.px(293f), y = contentBaseY)
                        .width(metrics.px(347f))
                        .height(metrics.px(184f)),
                )

                DiscoverShelf(
                    items = discoveryShelf,
                    onPlayItem = onPlayItem,
                    onSeeAll = { expandedSection = SearchDiscoverySection.Discovery },
                    modifier = Modifier
                        .offset(x = left, y = metrics.px(405f) + contentYOffset)
                        .width(metrics.px(610f))
                        .height(metrics.px(118f)),
                )

                TrendingSearchesPanel(
                    terms = trendingTerms,
                    onSearch = onSearch,
                    onSeeAll = { expandedSection = SearchDiscoverySection.Trending },
                    modifier = Modifier
                        .offset(x = rightX, y = metrics.px(51f))
                        .width(metrics.px(280f))
                        .height(metrics.px(149f)),
                )

                ArtistsPanel(
                    artists = artists,
                    onOpenArtist = onOpenArtist,
                    onSeeAll = { expandedSection = SearchDiscoverySection.Artists },
                    modifier = Modifier
                        .offset(x = rightX, y = metrics.px(207f))
                        .width(metrics.px(280f))
                        .height(metrics.px(99f)),
                )

                AlbumsPanel(
                    albums = albums,
                    fallbackItems = discoveryShelf,
                    onPlayItem = onPlayItem,
                    onSeeAll = { expandedSection = SearchDiscoverySection.Albums },
                    modifier = Modifier
                        .offset(x = rightX, y = metrics.px(314f))
                        .width(metrics.px(280f))
                        .height(metrics.px(108f)),
                )

                PlaylistsPanel(
                    playlists = playlists,
                    fallbackItems = discoveryShelf,
                    onPlayItem = onPlayItem,
                    onSeeAll = { expandedSection = SearchDiscoverySection.Playlists },
                    modifier = Modifier
                        .offset(x = rightX, y = metrics.px(430f))
                        .width(metrics.px(280f))
                        .height(metrics.px(82f)),
                )
            }
        }
    }
}


@Composable
internal fun TopResultPanel(
    item: YTItem?,
    onPlayItem: (YTItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalHomeReferenceMetrics.current
    ReferencePanel(
        modifier = modifier,
        brush = Brush.radialGradient(
            listOf(Color(0xFF33186F).copy(alpha = 0.55f), OmniReferenceColors.SurfaceBase),
            radius = with(metrics.density) { metrics.px(260f).toPx() },
        )
    ) {
        Text(
            "Top Result",
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.offset(x = metrics.px(12f), y = metrics.px(12f)),
        )
        if (item != null) {
            AsyncImage(
                model = item.thumbnail?.toHighResThumbnail(),
                contentDescription = item.title,
                modifier = Modifier
                    .offset(x = metrics.px(12f), y = metrics.px(45f))
                    .size(metrics.px(88f))
                    .clip(RoundedCornerShape(metrics.px(7f))),
                contentScale = ContentScale.Crop,
            )
            Column(
                modifier = Modifier
                    .offset(x = metrics.px(112f), y = metrics.px(58f))
                    .width(metrics.px(112f))
            ) {
                Text(searchItemKind(item).uppercase(), color = TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(metrics.px(9f)))
                Text(item.title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(metrics.px(4f)))
                Text(searchItemSubtitle(item), color = TextSecondary, fontSize = 9.sp, lineHeight = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(searchItemMeta(item), color = TextSecondary, fontSize = 8.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(metrics.px(14f))
                    .size(metrics.px(28f))
                    .clip(CircleShape)
                    .background(Color(0xFFDCDDF3))
                    .clickable { onPlayItem(item) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.PlayArrow, null, tint = Color(0xFF101124), modifier = Modifier.size(metrics.px(18f)))
            }
        }
    }
}


@Composable
internal fun SongsPanel(
    songs: List<SongItem>,
    currentSong: SongItem?,
    playbackState: PlaybackState,
    onPlaySong: (SongItem, Int) -> Unit,
    onAddToQueue: (SongItem) -> Unit,
    onPlayNext: (SongItem) -> Unit,
    onLike: (SongItem) -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalHomeReferenceMetrics.current
    ReferencePanel(modifier = modifier) {
        PanelHeader("Songs", onSeeAll = onSeeAll, modifier = Modifier.padding(start = metrics.px(12f), top = metrics.px(10f), end = metrics.px(12f)))
        Column(
            modifier = Modifier
                .padding(start = metrics.px(12f), end = metrics.px(10f), top = metrics.px(33f))
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(metrics.px(2f)),
        ) {
            songs.take(5).forEachIndexed { index, song ->
                val isActive = song.id == currentSong?.id
                SongResultRow(
                    song = song,
                    isActive = isActive,
                    isPlaying = isActive && playbackState == PlaybackState.PLAYING,
                    onPlay = { onPlaySong(song, index) },
                    onAddToQueue = { onAddToQueue(song) },
                    onPlayNext = { onPlayNext(song) },
                    onLike = { onLike(song) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(metrics.px(27f)),
                )
            }
        }
    }
}


@Composable
internal fun TrendingSearchesPanel(
    terms: List<String>,
    onSearch: (String) -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalHomeReferenceMetrics.current
    val trendValues = listOf(120, 98, 87, 76, 65)
    ReferencePanel(modifier = modifier) {
        PanelHeader("Trending Searches", onSeeAll = onSeeAll, modifier = Modifier.padding(start = metrics.px(12f), top = metrics.px(9f), end = metrics.px(12f)))
        Column(
            modifier = Modifier.padding(start = metrics.px(13f), end = metrics.px(13f), top = metrics.px(34f)),
            verticalArrangement = Arrangement.spacedBy(metrics.px(4f)),
        ) {
            terms.take(5).forEachIndexed { index, term ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(metrics.px(19f))
                        .clip(RoundedCornerShape(metrics.px(4f)))
                        .clickable { onSearch(term) },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("${index + 1}", color = TextSecondary, fontSize = 9.5.sp, modifier = Modifier.width(metrics.px(20f)))
                    Text(term, color = TextPrimary, fontSize = 9.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Text("↗ ${trendValues.getOrElse(index) { 50 }}%", color = TextSecondary, fontSize = 8.5.sp)
                    Spacer(Modifier.width(metrics.px(9f)))
                    TrendSparkline(modifier = Modifier.width(metrics.px(42f)).height(metrics.px(11f)))
                }
            }
        }
    }
}


@Composable
internal fun ArtistsPanel(
    artists: List<SearchArtistRow>,
    onOpenArtist: (String?) -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalHomeReferenceMetrics.current
    ReferencePanel(modifier = modifier) {
        PanelHeader("Artists", onSeeAll = onSeeAll, modifier = Modifier.padding(start = metrics.px(12f), top = metrics.px(8f), end = metrics.px(12f)))
        Column(
            modifier = Modifier.padding(start = metrics.px(12f), end = metrics.px(10f), top = metrics.px(31f)),
            verticalArrangement = Arrangement.spacedBy(metrics.px(4f)),
        ) {
            artists.take(3).forEach { artist ->
                ArtistRow(artist = artist, onClick = { onOpenArtist(artist.id) })
            }
        }
    }
}


@Composable
internal fun AlbumsPanel(
    albums: List<AlbumItem>,
    fallbackItems: List<YTItem>,
    onPlayItem: (YTItem) -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalHomeReferenceMetrics.current
    val items = albums.ifEmpty { fallbackItems.filter { it.thumbnail != null }.take(4) }
    ReferencePanel(modifier = modifier) {
        PanelHeader("Albums", onSeeAll = onSeeAll, modifier = Modifier.padding(start = metrics.px(12f), top = metrics.px(8f), end = metrics.px(12f)))
        Row(
            modifier = Modifier
                .padding(start = metrics.px(12f), top = metrics.px(31f))
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(metrics.px(8f)),
        ) {
            items.take(4).forEach { item ->
                CompactMediaCard(
                    item = item,
                    width = metrics.px(52f),
                    artwork = metrics.px(39f),
                    onClick = { onPlayItem(item) },
                )
            }
        }
    }
}


@Composable
internal fun PlaylistsPanel(
    playlists: List<PlaylistItem>,
    fallbackItems: List<YTItem>,
    onPlayItem: (YTItem) -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalHomeReferenceMetrics.current
    val items = playlists.ifEmpty { fallbackItems.filter { it.thumbnail != null }.take(4) }
    ReferencePanel(modifier = modifier) {
        PanelHeader("Playlists", onSeeAll = onSeeAll, modifier = Modifier.padding(start = metrics.px(12f), top = metrics.px(8f), end = metrics.px(12f)))
        Row(
            modifier = Modifier
                .padding(start = metrics.px(12f), top = metrics.px(31f))
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(metrics.px(7f)),
        ) {
            items.take(4).forEach { item ->
                CompactMediaCard(
                    item = item,
                    width = metrics.px(50f),
                    artwork = metrics.px(30f),
                    onClick = { onPlayItem(item) },
                )
            }
        }
    }
}


@Composable
internal fun ExpandedSearchSectionPanel(
    section: SearchDiscoverySection,
    query: String,
    songs: List<SongItem>,
    trendingTerms: List<String>,
    artists: List<SearchArtistRow>,
    albums: List<AlbumItem>,
    playlists: List<PlaylistItem>,
    discoveryShelf: List<YTItem>,
    currentSong: SongItem?,
    playbackState: PlaybackState,
    onBack: () -> Unit,
    onSearch: (String) -> Unit,
    onPlayItem: (YTItem) -> Unit,
    onPlaySong: (SongItem, Int) -> Unit,
    onAddToQueue: (SongItem) -> Unit,
    onPlayNext: (SongItem) -> Unit,
    onLike: (SongItem) -> Unit,
    onOpenArtist: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalHomeReferenceMetrics.current
    val title = when (section) {
        SearchDiscoverySection.Songs -> "All Songs"
        SearchDiscoverySection.Trending -> "Trending Searches"
        SearchDiscoverySection.Artists -> "All Artists"
        SearchDiscoverySection.Albums -> "All Albums"
        SearchDiscoverySection.Playlists -> "All Playlists"
        SearchDiscoverySection.Discovery -> "Discover Something New"
    }
    val context = query.takeIf { it.isNotBlank() }?.let { "Current context: $it" } ?: "Current discovery context"

    ReferencePanel(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(metrics.px(14f))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(context, color = TextSecondary, fontSize = 8.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(
                    "Back",
                    color = IrisSoft,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(metrics.px(5f)))
                        .clickable(onClick = onBack)
                        .padding(horizontal = metrics.px(7f), vertical = metrics.px(4f)),
                )
            }

            Spacer(Modifier.height(metrics.px(12f)))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(metrics.px(6f)),
            ) {
                when (section) {
                    SearchDiscoverySection.Songs -> {
                        songs.forEachIndexed { index, song ->
                            val isActive = song.id == currentSong?.id
                            SongResultRow(
                                song = song,
                                isActive = isActive,
                                isPlaying = isActive && playbackState == PlaybackState.PLAYING,
                                onPlay = { onPlaySong(song, index) },
                                onAddToQueue = { onAddToQueue(song) },
                                onPlayNext = { onPlayNext(song) },
                                onLike = { onLike(song) },
                                modifier = Modifier.fillMaxWidth().height(metrics.px(31f)),
                            )
                        }
                    }
                    SearchDiscoverySection.Trending -> {
                        trendingTerms.forEachIndexed { index, term ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(metrics.px(28f))
                                    .clip(RoundedCornerShape(metrics.px(6f)))
                                    .background(OmniReferenceColors.SurfaceDeepRaised.copy(alpha = 0.42f))
                                    .clickable { onSearch(term) }
                                    .padding(horizontal = metrics.px(8f)),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("${index + 1}", color = TextSecondary, fontSize = 9.sp, modifier = Modifier.width(metrics.px(24f)))
                                Text(term, color = TextPrimary, fontSize = 9.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                Text("↗", color = IrisSoft, fontSize = 10.sp)
                            }
                        }
                    }
                    SearchDiscoverySection.Artists -> {
                        artists.forEach { artist ->
                            ArtistRow(artist = artist, onClick = { onOpenArtist(artist.id) })
                        }
                    }
                    SearchDiscoverySection.Albums -> {
                        val items: List<YTItem> = albums.ifEmpty { discoveryShelf.filter { it.thumbnail != null } }
                        ExpandedMediaGrid(items = items, onPlayItem = onPlayItem)
                    }
                    SearchDiscoverySection.Playlists -> {
                        val items: List<YTItem> = playlists.ifEmpty { discoveryShelf.filter { it.thumbnail != null } }
                        ExpandedMediaGrid(items = items, onPlayItem = onPlayItem)
                    }
                    SearchDiscoverySection.Discovery -> {
                        ExpandedMediaGrid(items = discoveryShelf.filter { it.thumbnail != null }, onPlayItem = onPlayItem)
                    }
                }
            }
        }
    }
}


@Composable
internal fun DiscoverShelf(
    items: List<YTItem>,
    onPlayItem: (YTItem) -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalHomeReferenceMetrics.current
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            SectionTitle("Discover Something New")
            Spacer(Modifier.weight(1f))
            SeeAllAction(onSeeAll = onSeeAll)
        }
        Spacer(Modifier.height(metrics.px(7f)))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(metrics.px(6f)),
        ) {
            items.filter { it.thumbnail != null }.take(6).forEach { item ->
                DiscoveryCard(item, onClick = { onPlayItem(item) })
            }
        }
    }
}

