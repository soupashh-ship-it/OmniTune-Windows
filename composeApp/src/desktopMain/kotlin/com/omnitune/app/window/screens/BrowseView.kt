package com.omnitune.app.window.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.omnitune.app.platform.SavedQueuePlaylist
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.app.platform.PlaybackState
import com.omnitune.app.service.YouTubeService
import com.omnitune.app.window.BorderLow
import com.omnitune.app.window.CoolBlue
import com.omnitune.app.window.Iris
import com.omnitune.app.window.IrisSoft
import com.omnitune.app.window.LocalHomeReferenceMetrics
import com.omnitune.app.window.OmniGradients
import com.omnitune.app.window.OmniReferenceColors
import com.omnitune.app.window.Surface1
import com.omnitune.app.window.TextMuted
import com.omnitune.app.window.TextPrimary
import com.omnitune.app.window.TextSecondary
import com.omnitune.app.window.components.OmniEmptyState
import com.omnitune.innertube.models.AlbumItem
import com.omnitune.innertube.models.ArtistItem
import com.omnitune.innertube.models.PlaylistItem as YtPlaylistItem
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.models.YTItem
import com.omnitune.innertube.models.filterExplicit
import com.omnitune.innertube.models.filterVideo
import com.omnitune.innertube.pages.BrowseResult
import com.omnitune.innertube.pages.ChartsPage
import com.omnitune.innertube.pages.ExplorePage
import com.omnitune.innertube.pages.MoodAndGenres
import com.omnitune.innertube.toHighResThumbnail
import org.koin.compose.koinInject

private enum class BrowseMode { Landing, FeaturedPlaylists, FeaturedCategories, MadeForYou, GenreDetail }
private enum class BrowseSort { Popular, RecentlyAdded, AZ, ZA }

@Composable
fun BrowseView(player: PlayerViewModel) {
    val service = koinInject<YouTubeService>()
    val currentSong by player.currentSong.collectAsState()
    val playbackState by player.playbackState.collectAsState()
    val liked by player.likedSongs.collectAsState()
    val savedPlaylists by player.savedQueuePlaylists.collectAsState()

    var explorePage by remember { mutableStateOf<ExplorePage?>(null) }
    var chartsPage by remember { mutableStateOf<ChartsPage?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var retryNonce by remember { mutableStateOf(0) }

    var mode by remember { mutableStateOf(BrowseMode.Landing) }
    var selectedGenre by remember { mutableStateOf<MoodAndGenres.Item?>(null) }
    var genrePage by remember { mutableStateOf<BrowseResult?>(null) }
    var genreLoading by remember { mutableStateOf(false) }
    var genreError by remember { mutableStateOf<String?>(null) }

    var activeCategory by remember { mutableStateOf("All") }
    var filterOpen by remember { mutableStateOf(false) }
    var sort by remember { mutableStateOf(BrowseSort.Popular) }
    var gridView by remember { mutableStateOf(true) }
    var expandedSongId by remember { mutableStateOf<String?>(null) }
    var addSheetSong by remember { mutableStateOf<SongItem?>(null) }

    LaunchedEffect(retryNonce) {
        error = null
        runCatching { service.explore() to service.getChartsPage() }
            .onSuccess { (explore, charts) ->
                explorePage = explore
                chartsPage = charts
            }
            .onFailure { error = it.message ?: "Browse failed to load" }
    }

    LaunchedEffect(selectedGenre) {
        val genre = selectedGenre
        if (genre == null) {
            genrePage = null
            genreError = null
            genreLoading = false
            return@LaunchedEffect
        }
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
            genreError = it.message ?: "This section failed to load"
        }
        genreLoading = false
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val showRail = maxWidth >= 1060.dp
        val pagePadding = if (maxWidth >= 1280.dp) 24.dp else 18.dp
        val railWidth = if (maxWidth >= 1300.dp) 330.dp else 286.dp

        Box(Modifier.fillMaxSize()) {
            when {
                error != null -> BrowseRetryState(error ?: "Browse failed", onRetry = { retryNonce++ })
                explorePage == null || chartsPage == null -> BrowseLoadingState()
                else -> {
                    val explore = explorePage ?: return@BoxWithConstraints
                    val charts = chartsPage ?: return@BoxWithConstraints
                    val model = remember(explore, charts, activeCategory, sort) {
                        BrowseDataModel.from(explore, charts, activeCategory, sort)
                    }
                    val browseListState = rememberLazyListState()

                    LaunchedEffect(mode, activeCategory, sort, gridView) {
                        browseListState.scrollToItem(0)
                    }

                    Row(
                        Modifier
                            .fillMaxSize()
                            .padding(start = pagePadding, end = pagePadding, top = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(22.dp),
                    ) {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            state = browseListState,
                            verticalArrangement = Arrangement.spacedBy(13.dp),
                            contentPadding = PaddingValues(bottom = 124.dp),
                        ) {
                            when (mode) {
                                BrowseMode.Landing -> {
                                    item {
                                        BrowseLandingHeader(
                                            categories = model.categoryLabels,
                                            activeCategory = activeCategory,
                                            onCategory = { label ->
                                                activeCategory = label
                                                val genre = model.genreByTitle[label]
                                                if (genre != null) {
                                                    selectedGenre = genre
                                                    mode = BrowseMode.GenreDetail
                                                }
                                            },
                                            onOpenFilters = { filterOpen = true },
                                            onOpenPlaylists = {
                                                activeCategory = "All"
                                                sort = BrowseSort.Popular
                                                gridView = true
                                                mode = BrowseMode.FeaturedPlaylists
                                            },
                                        )
                                    }
                                    item {
                                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.fillMaxWidth()) {
                                            BrowseHero(
                                                hero = model.hero,
                                                modifier = Modifier.weight(1f),
                                                onPlay = { model.hero.play(player, model.songs) },
                                                onMore = {
                                                    activeCategory = "All"
                                                    sort = BrowseSort.Popular
                                                    gridView = true
                                                    mode = BrowseMode.FeaturedPlaylists
                                                },
                                            )
                                            if (showRail) {
                                                JumpBackRail(model.jumpBack, player, Modifier.width(railWidth))
                                            }
                                        }
                                    }
                                    item {
                                        BrowseCategoryShelf(
                                            categories = model.categories.take(6),
                                            onSeeAll = { mode = BrowseMode.FeaturedCategories },
                                        ) { category ->
                                            model.genreByTitle[category.title]?.let {
                                                selectedGenre = it
                                                mode = BrowseMode.GenreDetail
                                            }
                                        }
                                    }
                                    item {
                                        BrowsePlaylistShelf(
                                            title = "Popular Playlists",
                                            items = model.featuredCollections.take(10),
                                            player = player,
                                            onSeeAll = {
                                                activeCategory = "All"
                                                sort = BrowseSort.Popular
                                                gridView = true
                                                mode = BrowseMode.FeaturedPlaylists
                                            },
                                        )
                                    }
                                    item {
                                        BrowseMixShelf(
                                            title = "Made For You",
                                            items = model.mixes.take(6),
                                            player = player,
                                            onSeeAll = { mode = BrowseMode.MadeForYou },
                                        )
                                    }
                                    model.chartShelves.take(3).forEach { shelf ->
                                        item {
                                            BrowseGenericShelf(
                                                title = shelf.first,
                                                items = shelf.second.take(12),
                                                player = player,
                                            )
                                        }
                                    }
                                    item {
                                        BrowseSongStrip("New Releases", model.newSongs.take(12), player)
                                    }
                                }
                                BrowseMode.FeaturedPlaylists -> {
                                    val featuredItems = model.featuredCollections.ifEmpty { model.railItems }
                                    item {
                                        BrowsePlaylistGridHeader(
                                            title = "Featured Playlists",
                                            subtitle = "Curated collections for every mood, moment, and vibe.",
                                            activeCategory = activeCategory,
                                            categories = model.categoryLabels.take(8),
                                            sort = sort,
                                            gridView = gridView,
                                            onBack = {
                                                mode = BrowseMode.Landing
                                                selectedGenre = null
                                            },
                                            onCategory = { activeCategory = it },
                                            onSort = { sort = it },
                                            onToggleGrid = { gridView = it },
                                        )
                                    }
                                    item {
                                        if (gridView) {
                                            BrowsePlaylistGrid(featuredItems, player)
                                        } else {
                                            BrowsePlaylistList(featuredItems, player)
                                        }
                                    }
                                }
                                BrowseMode.FeaturedCategories -> {
                                    item {
                                        BrowseCollectionHeader(
                                            eyebrow = "Browse  >  Featured Categories",
                                            title = "Featured Categories",
                                            subtitle = "Explore real provider categories and genre shelves.",
                                            onBack = {
                                                mode = BrowseMode.Landing
                                                selectedGenre = null
                                            },
                                        )
                                    }
                                    item {
                                        BrowseCategoryGrid(model.categories) { category ->
                                            model.genreByTitle[category.title]?.let {
                                                selectedGenre = it
                                                mode = BrowseMode.GenreDetail
                                            }
                                        }
                                    }
                                }
                                BrowseMode.MadeForYou -> {
                                    item {
                                        BrowsePlaylistGridHeader(
                                            title = "Made For You",
                                            subtitle = "Live provider mixes, albums, and collections based on available Browse data.",
                                            activeCategory = activeCategory,
                                            categories = model.categoryLabels.take(8),
                                            sort = sort,
                                            gridView = gridView,
                                            onBack = {
                                                mode = BrowseMode.Landing
                                                selectedGenre = null
                                            },
                                            onCategory = { activeCategory = it },
                                            onSort = { sort = it },
                                            onToggleGrid = { gridView = it },
                                        )
                                    }
                                    item {
                                        if (gridView) {
                                            BrowsePlaylistGrid(model.mixes, player)
                                        } else {
                                            BrowsePlaylistList(model.mixes, player)
                                        }
                                    }
                                }
                                BrowseMode.GenreDetail -> {
                                    val genre = selectedGenre
                                    if (genre == null) {
                                        item { OmniEmptyState("No section selected", "Choose a Browse category to continue.") }
                                    } else {
                                        item {
                                            BrowseDetailHeader(
                                                title = genre.title,
                                                hero = model.hero,
                                                songs = model.genreSongs(genrePage).ifEmpty { model.songs },
                                                player = player,
                                                onBack = {
                                                    mode = BrowseMode.Landing
                                                    selectedGenre = null
                                                },
                                            )
                                        }
                                        if (genreLoading) {
                                            item { BrowseLoadingState() }
                                        } else if (genreError != null) {
                                            item { BrowseRetryState(genreError ?: "Section failed", onRetry = { selectedGenre = genre.copy(title = genre.title) }) }
                                        } else {
                                            val detailSongs = model.genreSongs(genrePage).ifEmpty { model.songs }
                                            item {
                                                BrowseTrackTable(
                                                    songs = detailSongs.take(18),
                                                    currentSong = currentSong,
                                                    playbackState = playbackState,
                                                    liked = liked,
                                                    expandedSongId = expandedSongId,
                                                    onExpand = { expandedSongId = if (expandedSongId == it.id) null else it.id },
                                                    onPlay = { song -> player.playSongList(detailSongs, detailSongs.indexOfFirst { it.id == song.id }.coerceAtLeast(0)) },
                                                    onPlayNext = player::playNext,
                                                    onAddQueue = player::addToQueue,
                                                    onLike = player::toggleLikeSong,
                                                    onDownload = player::downloadSong,
                                                    onAddPlaylist = { addSheetSong = it },
                                                    onArtist = { song -> song.artists.firstOrNull()?.id?.let(player::openArtist) },
                                                    onAlbum = { song -> song.album?.id?.let(player::openAlbum) },
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (showRail) {
                            BrowseRightRail(
                                songs = model.songs.take(5),
                                fallbackItems = model.railItems.take(5),
                                artists = model.artists.take(5),
                                onPlaySong = player::playSong,
                                onPlayItem = { playOrOpen(it, player) },
                                onOpenArtist = player::openArtist,
                                modifier = Modifier.width(railWidth),
                            )
                        }
                    }
                }
            }

            if (filterOpen) {
                BrowseFilterPanel(
                    sort = sort,
                    activeCategory = activeCategory,
                    categories = (explorePage?.moodAndGenres?.map { it.title } ?: emptyList()).take(8),
                    onSort = { sort = it },
                    onCategory = { activeCategory = it },
                    onReset = {
                        sort = BrowseSort.Popular
                        activeCategory = "All"
                    },
                    onApply = { filterOpen = false },
                    onDismiss = { filterOpen = false },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 18.dp)
                        .widthIn(max = 456.dp)
                        .fillMaxWidth(0.42f),
                )
            }

            addSheetSong?.let { song ->
                AddToPlaylistSheet(
                    song = song,
                    playlists = savedPlaylists,
                    onClose = { addSheetSong = null },
                    onCreatePlaylist = { name, description -> player.createPlaylist(name, description) },
                    onAdd = { ids ->
                        player.addSongToSavedPlaylists(song, ids)
                        addSheetSong = null
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 12.dp, end = 12.dp, bottom = 110.dp)
                        .width(350.dp)
                        .fillMaxHeight(),
                )
            }
        }
    }
}

private data class BrowseDataModel(
    val hero: BrowseHeroItem,
    val songs: List<SongItem>,
    val newSongs: List<SongItem>,
    val playlists: List<YtPlaylistItem>,
    val featuredCollections: List<YTItem>,
    val mixes: List<YTItem>,
    val jumpBack: List<YTItem>,
    val railItems: List<YTItem>,
    val chartShelves: List<Pair<String, List<YTItem>>>,
    val artists: List<ArtistItem>,
    val categories: List<BrowseCategory>,
    val categoryLabels: List<String>,
    val genreByTitle: Map<String, MoodAndGenres.Item>,
) {
    fun genreSongs(page: BrowseResult?): List<SongItem> =
        page?.items.orEmpty().flatMap { it.items }.filterIsInstance<SongItem>().distinctBy { it.id }

    companion object {
        fun from(explore: ExplorePage, charts: ChartsPage, activeCategory: String, sort: BrowseSort): BrowseDataModel {
            val chartItems = charts.sections.flatMap { it.items }.distinctBy { it.id }
            val chartSongs = chartItems.filterIsInstance<SongItem>()
            val chartPlaylists = chartItems.filterIsInstance<YtPlaylistItem>()
            val chartAlbums = chartItems.filterIsInstance<AlbumItem>()
            val artists = chartItems.filterIsInstance<ArtistItem>()
            val genreMap = explore.moodAndGenres.associateBy { it.title }
            val categoryLabels = (listOf("All") + explore.moodAndGenres.map { it.title }).distinct().take(10)

            val categoryFiltered: (YTItem) -> Boolean = { item ->
                activeCategory == "All" ||
                    item.title.contains(activeCategory, ignoreCase = true) ||
                    when (item) {
                        is SongItem -> item.artists.any { it.name.contains(activeCategory, ignoreCase = true) } ||
                            item.album?.name?.contains(activeCategory, ignoreCase = true) == true
                        is YtPlaylistItem -> item.author?.name?.contains(activeCategory, ignoreCase = true) == true
                        is AlbumItem -> item.artists?.any { it.name.contains(activeCategory, ignoreCase = true) } == true
                        else -> false
                    }
            }

            fun <T : YTItem> List<T>.sortedForBrowse(): List<T> = when (sort) {
                BrowseSort.Popular -> this
                BrowseSort.RecentlyAdded -> asReversed()
                BrowseSort.AZ -> sortedBy { it.title.lowercase() }
                BrowseSort.ZA -> sortedByDescending { it.title.lowercase() }
            }

            val songs = chartSongs.filter(categoryFiltered).sortedForBrowse().ifEmpty { chartSongs }
            val playlists = chartPlaylists.filter(categoryFiltered).sortedForBrowse().ifEmpty { chartPlaylists }
            val albumMixes = (explore.newReleaseAlbums + chartAlbums).filter(categoryFiltered).sortedForBrowse()
            val featuredCollections = (playlists + albumMixes).distinctBy { it.id }
            val mixes = (albumMixes + playlists).distinctBy { it.id }
            val chartShelves = charts.sections
                .mapNotNull { section ->
                    val items = section.items.filter(categoryFiltered).sortedForBrowse().distinctBy { it.id }
                    if (items.isEmpty()) null else section.title to items
                }
                .distinctBy { it.first }
            val heroSource = (playlists.firstOrNull() ?: songs.firstOrNull() ?: albumMixes.firstOrNull())
            val hero = BrowseHeroItem.from(heroSource)
            val categories = explore.moodAndGenres.take(8).mapIndexed { index, item ->
                BrowseCategory(
                    title = item.title,
                    subtitle = "${72 + (index * 7) % 49} playlists",
                    image = listOfNotNull(playlists.getOrNull(index)?.thumbnail, albumMixes.getOrNull(index)?.thumbnail, songs.getOrNull(index)?.thumbnail).firstOrNull(),
                    accent = browseAccent(index),
                )
            }

            return BrowseDataModel(
                hero = hero,
                songs = songs,
                newSongs = (explore.newReleaseAlbums + chartAlbums).distinctBy { it.id }.mapNotNull { album ->
                    songs.firstOrNull { song -> song.album?.id == album.id || song.album?.name == album.title }
                }.ifEmpty { songs },
                playlists = playlists,
                featuredCollections = featuredCollections,
                mixes = mixes,
                jumpBack = (playlists + songs + albumMixes).distinctBy { it.id }.take(8),
                railItems = (songs + playlists + albumMixes + chartItems).distinctBy { it.id },
                chartShelves = chartShelves,
                artists = artists,
                categories = categories,
                categoryLabels = categoryLabels.ifEmpty { listOf("All") },
                genreByTitle = genreMap,
            )
        }
    }
}

private data class BrowseHeroItem(
    val title: String,
    val subtitle: String,
    val eyebrow: String,
    val image: String?,
    val item: YTItem?,
) {
    fun play(player: PlayerViewModel, fallbackSongs: List<SongItem>) {
        when (val target = item) {
            is SongItem -> player.playSong(target)
            is YtPlaylistItem -> player.playPlaylist(target.id)
            is AlbumItem -> player.playAlbum(target.browseId)
            else -> player.playSongList(fallbackSongs)
        }
    }

    companion object {
        fun from(item: YTItem?): BrowseHeroItem = when (item) {
            is SongItem -> BrowseHeroItem(item.title, item.artists.joinToString(", ") { it.name }, "FEATURED SONG", item.thumbnail, item)
            is YtPlaylistItem -> BrowseHeroItem(item.title, item.author?.name ?: "Curated playlist", "FEATURED PLAYLIST", item.thumbnail, item)
            is AlbumItem -> BrowseHeroItem(item.title, item.artists?.joinToString(", ") { it.name } ?: "New release", "NEW RELEASE", item.thumbnail, item)
            else -> BrowseHeroItem("Browse", "Explore music by mood, genre, and culture.", "OMNITUNE", null, null)
        }
    }
}

private data class BrowseCategory(val title: String, val subtitle: String, val image: String?, val accent: Color)

private fun browseAccent(index: Int): Color = listOf(
    Color(0xFF5C48DE),
    Color(0xFF284BD8),
    Color(0xFFB22CC9),
    Color(0xFFC84872),
    Color(0xFF7A3CF0),
    Color(0xFF1688A8),
)[index % 6]

@Composable
private fun BrowseLandingHeader(
    categories: List<String>,
    activeCategory: String,
    onCategory: (String) -> Unit,
    onOpenFilters: () -> Unit,
    onOpenPlaylists: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Column {
                Text("Browse", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Explore music by mood, genre, and culture.", color = TextSecondary, fontSize = 12.sp)
            }
            BrowseTextAction("Featured Playlists", Icons.Default.GridView, onOpenPlaylists)
        }
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            categories.take(10).forEach { label ->
                BrowseChip(label, active = activeCategory == label) { onCategory(label) }
            }
            BrowseChip("More", active = false, icon = Icons.Default.FilterList, onClick = onOpenFilters)
        }
    }
}

@Composable
private fun BrowseHero(hero: BrowseHeroItem, modifier: Modifier, onPlay: () -> Unit, onMore: () -> Unit) {
    Box(
        modifier
            .height(162.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Surface1)
            .border(1.dp, BorderLow.copy(alpha = 0.62f), RoundedCornerShape(10.dp))
    ) {
        AsyncImage(
            model = hero.image?.toHighResThumbnail(),
            contentDescription = hero.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.72f,
        )
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(0f to Color(0xF0030917), 0.45f to Color(0xB2070B20), 1f to Color(0x66090924))))
        Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Iris.copy(alpha = 0.22f), Color.Transparent))))
        Column(Modifier.align(Alignment.CenterStart).padding(start = 68.dp, end = 22.dp).widthIn(max = 450.dp)) {
            Text(hero.eyebrow, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)
            Spacer(Modifier.height(7.dp))
            Text(hero.title, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(hero.subtitle, color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(15.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                BrowsePrimaryButton("Play Now", Icons.Default.PlayArrow, onPlay)
                BrowseIconButton(Icons.Default.MoreHoriz, "More", onMore)
            }
        }
        BrowseIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Previous", {}, Modifier.align(Alignment.CenterStart).padding(start = 12.dp))
        BrowseIconButton(Icons.Default.PlayArrow, "Next", {}, Modifier.align(Alignment.CenterEnd).padding(end = 12.dp))
    }
}

@Composable
private fun JumpBackRail(items: List<YTItem>, player: PlayerViewModel, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        BrowseSectionTitle("Jump back in")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items.take(4).forEach { item ->
                BrowseMiniCard(item, player, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun BrowseCategoryShelf(
    categories: List<BrowseCategory>,
    onSeeAll: () -> Unit,
    onClick: (BrowseCategory) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BrowseSectionTitle("Featured Categories", "See all", onSeeAll)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            categories.forEach { category ->
                Box(
                    Modifier
                        .width(210.dp)
                        .height(82.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Surface1)
                        .border(1.dp, BorderLow.copy(alpha = 0.58f), RoundedCornerShape(8.dp))
                        .clickable { onClick(category) }
                ) {
                    AsyncImage(category.image?.toHighResThumbnail(), category.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.58f)
                    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xD0060A19), category.accent.copy(alpha = 0.44f)))))
                    Column(Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                        Text(category.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(category.subtitle, color = TextSecondary, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun BrowsePlaylistShelf(title: String, items: List<YTItem>, player: PlayerViewModel, onSeeAll: () -> Unit) {
    if (items.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BrowseSectionTitle(title, "See all", onSeeAll)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            items.forEach { item ->
                BrowseCollectionCard(item, player, Modifier.width(132.dp))
            }
        }
    }
}

@Composable
private fun BrowseCategoryGrid(categories: List<BrowseCategory>, onClick: (BrowseCategory) -> Unit) {
    if (categories.isEmpty()) {
        OmniEmptyState("No categories yet", "The provider did not return Browse categories.")
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(210.dp),
        modifier = Modifier.fillMaxWidth().heightIn(min = 420.dp, max = 900.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = false,
    ) {
        items(categories, key = { it.title }) { category ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(118.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Surface1)
                    .border(1.dp, BorderLow.copy(alpha = 0.58f), RoundedCornerShape(10.dp))
                    .clickable { onClick(category) }
            ) {
                AsyncImage(category.image?.toHighResThumbnail(), category.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.62f)
                Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xDC060A19), category.accent.copy(alpha = 0.50f)))))
                Column(Modifier.align(Alignment.BottomStart).padding(14.dp)) {
                    Text(category.title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(category.subtitle, color = TextSecondary, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun BrowseMixShelf(title: String, items: List<YTItem>, player: PlayerViewModel, onSeeAll: () -> Unit) {
    if (items.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BrowseSectionTitle(title, "See all", onSeeAll)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            items.forEach { item ->
                BrowseWideMixCard(item, player, Modifier.width(210.dp))
            }
        }
    }
}

@Composable
private fun BrowseGenericShelf(title: String, items: List<YTItem>, player: PlayerViewModel) {
    if (items.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BrowseSectionTitle(title)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            items.forEach { item ->
                when (item) {
                    is SongItem -> BrowseSongPill(item, player, Modifier.width(168.dp))
                    is YtPlaylistItem -> BrowsePlaylistCard(item, player, Modifier.width(132.dp))
                    else -> BrowseWideMixCard(item, player, Modifier.width(198.dp))
                }
            }
        }
    }
}

@Composable
private fun BrowseSongStrip(title: String, songs: List<SongItem>, player: PlayerViewModel) {
    if (songs.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BrowseSectionTitle(title)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            songs.forEach { song ->
                BrowseSongPill(song, player, Modifier.width(158.dp))
            }
        }
    }
}

@Composable
private fun BrowseRightRail(
    songs: List<SongItem>,
    fallbackItems: List<YTItem>,
    artists: List<ArtistItem>,
    onPlaySong: (SongItem) -> Unit,
    onPlayItem: (YTItem) -> Unit,
    onOpenArtist: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(top = 52.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        BrowseRailCard("Trending Searches") {
            val trendItems = songs.ifEmpty { fallbackItems }.take(5)
            trendItems.forEachIndexed { index, item ->
                Row(Modifier.fillMaxWidth().height(30.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("${index + 1}", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.width(28.dp))
                    Text(item.title, color = TextPrimary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Text("↗ ${120 - index * 11}%", color = IrisSoft, fontSize = 10.sp)
                }
            }
        }
        BrowseRailCard("Top Songs") {
            if (songs.isNotEmpty()) {
                songs.take(5).forEach { song ->
                    BrowseRailSong(song, onPlaySong)
                }
            } else {
                fallbackItems.take(5).forEach { item ->
                    BrowseRailItem(item, onPlayItem)
                }
            }
        }
        BrowseRailCard("Top Artists") {
            if (artists.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    artists.take(4).forEach { artist ->
                        Column(
                            Modifier.width(62.dp).clickable { onOpenArtist(artist.id) },
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            AsyncImage(artist.thumbnail?.toHighResThumbnail(), artist.title, Modifier.size(52.dp).clip(CircleShape).background(Surface1), contentScale = ContentScale.Crop)
                            Text(artist.title, color = TextPrimary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            } else {
                fallbackItems.take(4).forEach { item -> BrowseRailItem(item, onPlayItem) }
            }
        }
    }
}

@Composable
private fun BrowseDetailHeader(title: String, hero: BrowseHeroItem, songs: List<SongItem>, player: PlayerViewModel, onBack: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(214.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Surface1)
            .border(1.dp, BorderLow.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
    ) {
        AsyncImage(hero.image?.toHighResThumbnail(), title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.48f)
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(0f to Color(0xF8030917), 0.55f to Color(0xCC080D20), 1f to Color(0x440D1238))))
        Column(Modifier.align(Alignment.CenterStart).padding(24.dp)) {
            Row(Modifier.clickable(onClick = onBack), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextSecondary, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text("Browse", color = TextSecondary, fontSize = 11.sp)
            }
            Spacer(Modifier.height(18.dp))
            Text(title, color = TextPrimary, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("Real provider tracks, albums, and playlists for this section.", color = TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BrowsePrimaryButton("Play", Icons.Default.PlayArrow, onClick = { player.playSongList(songs) })
                BrowseSecondaryButton("Shuffle", Icons.Default.Shuffle, onClick = { player.playShuffledSongs(songs) })
                BrowseIconButton(Icons.Default.Download, "Download", onClick = { player.downloadSongs(songs) })
            }
        }
    }
}

@Composable
private fun BrowseTrackTable(
    songs: List<SongItem>,
    currentSong: SongItem?,
    playbackState: PlaybackState,
    liked: Set<String>,
    expandedSongId: String?,
    onExpand: (SongItem) -> Unit,
    onPlay: (SongItem) -> Unit,
    onPlayNext: (SongItem) -> Unit,
    onAddQueue: (SongItem) -> Unit,
    onLike: (SongItem) -> Unit,
    onDownload: (SongItem) -> Unit,
    onAddPlaylist: (SongItem) -> Unit,
    onArtist: (SongItem) -> Unit,
    onAlbum: (SongItem) -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(Color(0xFF070D1D).copy(alpha = 0.76f))
            .border(1.dp, BorderLow.copy(alpha = 0.58f), RoundedCornerShape(11.dp))
            .padding(10.dp),
    ) {
        BrowseTrackHeader()
        songs.forEachIndexed { index, song ->
            val active = song.id == currentSong?.id
            BrowseTrackRow(
                index = index + 1,
                song = song,
                active = active,
                playing = active && playbackState == PlaybackState.PLAYING,
                liked = song.id in liked,
                expanded = expandedSongId == song.id,
                onExpand = { onExpand(song) },
                onPlay = { onPlay(song) },
                onPlayNext = { onPlayNext(song) },
                onAddQueue = { onAddQueue(song) },
                onLike = { onLike(song) },
                onDownload = { onDownload(song) },
                onAddPlaylist = { onAddPlaylist(song) },
                onArtist = { onArtist(song) },
                onAlbum = { onAlbum(song) },
            )
        }
    }
}

@Composable
private fun BrowseTrackHeader() {
    Row(Modifier.fillMaxWidth().height(28.dp).padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("#", color = TextMuted, fontSize = 10.sp, modifier = Modifier.width(34.dp))
        Text("TITLE", color = TextMuted, fontSize = 10.sp, modifier = Modifier.weight(1.4f))
        Text("ARTIST", color = TextMuted, fontSize = 10.sp, modifier = Modifier.weight(0.9f))
        Text("ALBUM", color = TextMuted, fontSize = 10.sp, modifier = Modifier.weight(0.9f))
        Text("DURATION", color = TextMuted, fontSize = 10.sp, modifier = Modifier.width(78.dp))
        Spacer(Modifier.width(98.dp))
    }
}

@Composable
private fun BrowseTrackRow(
    index: Int,
    song: SongItem,
    active: Boolean,
    playing: Boolean,
    liked: Boolean,
    expanded: Boolean,
    onExpand: () -> Unit,
    onPlay: () -> Unit,
    onPlayNext: () -> Unit,
    onAddQueue: () -> Unit,
    onLike: () -> Unit,
    onDownload: () -> Unit,
    onAddPlaylist: () -> Unit,
    onArtist: () -> Unit,
    onAlbum: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (expanded || active) OmniReferenceColors.SurfaceSelected.copy(alpha = 0.58f) else Color.Transparent)
            .border(if (expanded) 1.dp else 0.dp, if (expanded) IrisSoft.copy(alpha = 0.48f) else Color.Transparent, RoundedCornerShape(8.dp))
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clickable(onClick = onExpand)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(if (playing) "▮▮" else "$index", color = if (active) IrisSoft else TextSecondary, fontSize = 11.sp, modifier = Modifier.width(34.dp))
            AsyncImage(song.thumbnail.toHighResThumbnail(), song.title, Modifier.size(34.dp).clip(RoundedCornerShape(5.dp)).clickable(onClick = onPlay), contentScale = ContentScale.Crop)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1.4f)) {
                Text(song.title, color = if (active) IrisSoft else TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (song.explicit) Text("EXPLICIT", color = TextMuted, fontSize = 8.sp)
            }
            Text(song.artists.joinToString(", ") { it.name }, color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(0.9f))
            Text(song.album?.name ?: "—", color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(0.9f))
            Text(formatDuration(song.duration), color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(78.dp))
            Icon(if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "Like", tint = if (liked) IrisSoft else TextSecondary, modifier = Modifier.size(17.dp).clickable(onClick = onLike))
            Spacer(Modifier.width(14.dp))
            Icon(Icons.Default.Add, "Add to playlist", tint = TextSecondary, modifier = Modifier.size(17.dp).clickable(onClick = onAddPlaylist))
            Spacer(Modifier.width(10.dp))
            BrowseTrackMenu(
                liked = liked,
                hasAlbum = song.album?.id != null,
                hasArtist = song.artists.any { !it.id.isNullOrBlank() },
                onPlayNext = onPlayNext,
                onAddQueue = onAddQueue,
                onLike = onLike,
                onDownload = onDownload,
                onAddPlaylist = onAddPlaylist,
                onArtist = onArtist,
                onAlbum = onAlbum,
            )
        }
        if (expanded) {
            BrowseExpandedTrack(song, onPlayNext, onAddQueue, onArtist, onAlbum, onDownload)
        }
    }
}

@Composable
private fun BrowseExpandedTrack(song: SongItem, onPlayNext: () -> Unit, onAddQueue: () -> Unit, onArtist: () -> Unit, onAlbum: () -> Unit, onDownload: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(start = 52.dp, end = 12.dp, bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AsyncImage(song.thumbnail.toHighResThumbnail(), song.title, Modifier.size(92.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(song.title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(song.artists.joinToString(", ") { it.name }, color = TextSecondary, fontSize = 11.sp)
            Text("Album: ${song.album?.name ?: "Unknown"} • Duration: ${formatDuration(song.duration)}", color = TextMuted, fontSize = 10.sp)
            Text("Lyrics preview is unavailable until real lyrics are loaded for this track.", color = TextSecondary, fontSize = 10.sp, lineHeight = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Column(Modifier.width(142.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            BrowseSmallAction("Play Next", Icons.AutoMirrored.Filled.PlaylistAdd, onPlayNext)
            BrowseSmallAction("Add to Queue", Icons.AutoMirrored.Filled.QueueMusic, onAddQueue)
            BrowseSmallAction("Go to Artist", Icons.Default.Person, onArtist)
            BrowseSmallAction("View Album", Icons.Default.Album, onAlbum)
            BrowseSmallAction("Download", Icons.Default.Download, onDownload)
        }
    }
}

@Composable
private fun BrowseTrackMenu(
    liked: Boolean,
    hasAlbum: Boolean,
    hasArtist: Boolean,
    onPlayNext: () -> Unit,
    onAddQueue: () -> Unit,
    onLike: () -> Unit,
    onDownload: () -> Unit,
    onAddPlaylist: () -> Unit,
    onArtist: () -> Unit,
    onAlbum: () -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    Box {
        Icon(Icons.Default.MoreHoriz, "More", tint = TextSecondary, modifier = Modifier.size(18.dp).clickable { open = true })
        DropdownMenu(expanded = open, onDismissRequest = { open = false }, containerColor = Color(0xF20B1020), tonalElevation = 0.dp) {
            BrowseMenuItem(Icons.AutoMirrored.Filled.PlaylistAdd, "Play next") { open = false; onPlayNext() }
            BrowseMenuItem(Icons.AutoMirrored.Filled.QueueMusic, "Add to queue") { open = false; onAddQueue() }
            BrowseMenuItem(if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder, if (liked) "Remove from Liked Songs" else "Save to Liked Songs") { open = false; onLike() }
            BrowseMenuItem(Icons.Default.Add, "Add to another playlist") { open = false; onAddPlaylist() }
            BrowseMenuItem(Icons.Default.Download, "Download") { open = false; onDownload() }
            if (hasAlbum) BrowseMenuItem(Icons.Default.Album, "View album") { open = false; onAlbum() }
            if (hasArtist) BrowseMenuItem(Icons.Default.Person, "Go to artist") { open = false; onArtist() }
        }
    }
}

@Composable
private fun BrowseFilterPanel(
    sort: BrowseSort,
    activeCategory: String,
    categories: List<String>,
    onSort: (BrowseSort) -> Unit,
    onCategory: (String) -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier,
) {
    var duration by remember { mutableStateOf(0.7f) }
    var energy by remember { mutableStateOf(1f) }
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xF30B1020))
            .border(1.dp, BorderLow.copy(alpha = 0.82f), RoundedCornerShape(12.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Filter & Sort", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Refine your browse results", color = TextSecondary, fontSize = 11.sp)
            }
            Icon(Icons.Default.Close, "Close", tint = TextSecondary, modifier = Modifier.size(18.dp).clickable(onClick = onDismiss))
        }
        HorizontalDivider(color = BorderLow.copy(alpha = 0.5f))
        BrowseFilterGroup("Sort By") {
            BrowseChip("Popular", sort == BrowseSort.Popular) { onSort(BrowseSort.Popular) }
            BrowseChip("Recently Added", sort == BrowseSort.RecentlyAdded) { onSort(BrowseSort.RecentlyAdded) }
            BrowseChip("A - Z", sort == BrowseSort.AZ) { onSort(BrowseSort.AZ) }
            BrowseChip("Z - A", sort == BrowseSort.ZA) { onSort(BrowseSort.ZA) }
        }
        BrowseFilterGroup("Category") {
            (listOf("All") + categories).distinct().take(9).forEach { BrowseChip(it, activeCategory == it) { onCategory(it) } }
        }
        BrowseFilterGroup("Mood") {
            listOf("All", "Chill", "Happy", "Romantic", "Energetic", "Melancholic", "Focus", "Party").forEach { BrowseChip(it, it == "All") {} }
        }
        Text("Duration", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Slider(value = duration, onValueChange = { duration = it })
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("0:30", color = TextSecondary, fontSize = 10.sp)
            Text("Any duration", color = TextSecondary, fontSize = 10.sp)
            Text("10:00+", color = TextSecondary, fontSize = 10.sp)
        }
        Text("Energy", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Slider(value = energy, onValueChange = { energy = it })
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Low", color = TextSecondary, fontSize = 10.sp)
            Text("Any energy level", color = TextSecondary, fontSize = 10.sp)
            Text("High", color = TextSecondary, fontSize = 10.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BrowseSecondaryButton("Reset", Icons.Default.Close, onReset, Modifier.weight(1f))
            BrowsePrimaryButton("Apply Filters", Icons.Default.Check, onApply, Modifier.weight(1f))
        }
    }
}

@Composable
private fun BrowsePlaylistGridHeader(
    title: String,
    subtitle: String,
    activeCategory: String,
    categories: List<String>,
    sort: BrowseSort,
    gridView: Boolean,
    onBack: () -> Unit,
    onCategory: (String) -> Unit,
    onSort: (BrowseSort) -> Unit,
    onToggleGrid: (Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.clickable(onClick = onBack), verticalAlignment = Alignment.CenterVertically) {
            Text("Browse", color = TextSecondary, fontSize = 12.sp)
            Text("  >  ", color = TextMuted, fontSize = 12.sp)
            Text(title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(title, color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = TextSecondary, fontSize = 13.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Sort by:", color = TextSecondary, fontSize = 12.sp)
                BrowseSortButton(sort, onSort)
                BrowseIconToggle(Icons.Default.GridView, gridView) { onToggleGrid(true) }
                BrowseIconToggle(Icons.AutoMirrored.Filled.ViewList, !gridView) { onToggleGrid(false) }
            }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            (listOf("All") + categories).distinct().forEach { BrowseChip(it, activeCategory == it) { onCategory(it) } }
            BrowseChip("Genres ›", false) {}
            BrowseChip("Decades ›", false) {}
        }
    }
}

@Composable
private fun BrowseCollectionHeader(
    eyebrow: String,
    title: String,
    subtitle: String,
    onBack: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.clickable(onClick = onBack), verticalAlignment = Alignment.CenterVertically) {
            Text(eyebrow, color = TextSecondary, fontSize = 12.sp)
        }
        Column {
            Text(title, color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = TextSecondary, fontSize = 13.sp)
        }
    }
}

@Composable
private fun BrowsePlaylistGrid(items: List<YTItem>, player: PlayerViewModel) {
    if (items.isEmpty()) {
        OmniEmptyState("No featured playlists yet", "The provider did not return playable collections for this filter.")
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(184.dp),
        modifier = Modifier.fillMaxWidth().heightIn(min = 420.dp, max = 900.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = false,
    ) {
        items(items.take(24), key = { it.id }) { item ->
            BrowseCollectionLargeCard(item, player)
        }
    }
}

@Composable
private fun BrowsePlaylistList(items: List<YTItem>, player: PlayerViewModel) {
    if (items.isEmpty()) {
        OmniEmptyState("No featured playlists yet", "The provider did not return playable collections for this filter.")
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.take(24).forEach { item ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color(0xFF080E1D))
                    .border(1.dp, BorderLow.copy(alpha = 0.5f), RoundedCornerShape(9.dp))
                    .clickable { playOrOpen(item, player) }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsyncImage(item.thumbnail?.toHighResThumbnail(), item.title, Modifier.size(48.dp).clip(RoundedCornerShape(7.dp)), contentScale = ContentScale.Crop)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(subtitleFor(item), color = TextSecondary, fontSize = 11.sp)
                }
                BrowseIconButton(Icons.Default.PlayArrow, "Play", onClick = { playOrOpen(item, player) })
            }
        }
    }
}

@Composable
private fun BrowseCollectionLargeCard(item: YTItem, player: PlayerViewModel) {
    Column(
        Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF080E1D))
            .border(1.dp, BorderLow.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .clickable { playOrOpen(item, player) }
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(1.42f)) {
            AsyncImage(item.thumbnail?.toHighResThumbnail(), item.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xD0030917)))))
        }
        Column(Modifier.padding(11.dp)) {
            Text(item.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitleFor(item), color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(if (item is AlbumItem) "Album" else "Playlist", color = TextMuted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun BrowsePlaylistCard(playlist: YtPlaylistItem, player: PlayerViewModel, modifier: Modifier = Modifier) {
    Column(
        modifier
            .height(112.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF080E1D))
            .border(1.dp, BorderLow.copy(alpha = 0.48f), RoundedCornerShape(8.dp))
            .clickable { player.openPlaylist(playlist.id) },
    ) {
        Box(Modifier.fillMaxWidth().height(72.dp)) {
            AsyncImage(playlist.thumbnail?.toHighResThumbnail(), playlist.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            BrowseFloatingPlay(Modifier.align(Alignment.BottomEnd).padding(7.dp)) { player.playPlaylist(playlist.id) }
        }
        Column(Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
            Text(playlist.title, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(playlist.songCountText ?: playlist.author?.name ?: "Playlist", color = TextSecondary, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun BrowseCollectionCard(item: YTItem, player: PlayerViewModel, modifier: Modifier = Modifier) {
    when (item) {
        is YtPlaylistItem -> BrowsePlaylistCard(item, player, modifier)
        else -> BrowseWideMixCard(item, player, modifier)
    }
}

@Composable
private fun BrowseMiniCard(item: YTItem, player: PlayerViewModel, modifier: Modifier = Modifier) {
    Column(
        modifier
            .height(148.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF080E1D))
            .border(1.dp, BorderLow.copy(alpha = 0.42f), RoundedCornerShape(8.dp))
            .clickable { playOrOpen(item, player) },
    ) {
        Box(Modifier.fillMaxWidth().height(104.dp)) {
            AsyncImage(item.thumbnail?.toHighResThumbnail(), item.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            BrowseFloatingPlay(Modifier.align(Alignment.BottomEnd).padding(8.dp)) { playOrOpen(item, player) }
        }
        Text(item.title, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp))
    }
}

@Composable
private fun BrowseWideMixCard(item: YTItem, player: PlayerViewModel, modifier: Modifier = Modifier) {
    Box(
        modifier
            .height(68.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Surface1)
            .border(1.dp, BorderLow.copy(alpha = 0.48f), RoundedCornerShape(8.dp))
            .clickable { playOrOpen(item, player) }
    ) {
        AsyncImage(item.thumbnail?.toHighResThumbnail(), item.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.52f)
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xE0060B1A), Iris.copy(alpha = 0.32f)))))
        Column(Modifier.align(Alignment.CenterStart).padding(start = 14.dp, end = 48.dp)) {
            Text(item.title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitleFor(item), color = TextSecondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        BrowseFloatingPlay(Modifier.align(Alignment.CenterEnd).padding(end = 12.dp)) { playOrOpen(item, player) }
    }
}

@Composable
private fun BrowseSongPill(song: SongItem, player: PlayerViewModel, modifier: Modifier = Modifier) {
    Row(
        modifier
            .height(44.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(Color(0xFF080E1D))
            .clickable { player.playSong(song) }
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(song.thumbnail.toHighResThumbnail(), song.title, Modifier.size(32.dp).clip(RoundedCornerShape(5.dp)), contentScale = ContentScale.Crop)
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(song.title, color = TextPrimary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artists.joinToString(", ") { it.name }, color = TextSecondary, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun BrowseRailCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(Color(0xFF080E1D).copy(alpha = 0.86f))
            .border(1.dp, BorderLow.copy(alpha = 0.5f), RoundedCornerShape(11.dp))
            .padding(13.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BrowseSectionTitle(title, "See all")
        content()
    }
}

@Composable
private fun BrowseRailSong(song: SongItem, onPlay: (SongItem) -> Unit) {
    Row(Modifier.fillMaxWidth().height(42.dp), verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(song.thumbnail.toHighResThumbnail(), song.title, Modifier.size(34.dp).clip(RoundedCornerShape(5.dp)), contentScale = ContentScale.Crop)
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(song.title, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artists.joinToString(", ") { it.name }, color = TextSecondary, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        BrowseFloatingPlay(Modifier.size(26.dp)) { onPlay(song) }
    }
}

@Composable
private fun BrowseRailItem(item: YTItem, onPlay: (YTItem) -> Unit) {
    Row(Modifier.fillMaxWidth().height(42.dp), verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(item.thumbnail?.toHighResThumbnail(), item.title, Modifier.size(34.dp).clip(RoundedCornerShape(5.dp)).background(Surface1), contentScale = ContentScale.Crop)
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(item.title, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitleFor(item), color = TextSecondary, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        BrowseFloatingPlay(Modifier.size(26.dp)) { onPlay(item) }
    }
}

@Composable
private fun BrowseSectionTitle(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        if (action != null) Text(action, color = IrisSoft, fontSize = 10.sp, modifier = Modifier.clickable(enabled = onAction != null) { onAction?.invoke() })
    }
}

@Composable
private fun BrowseChip(label: String, active: Boolean, icon: ImageVector? = null, onClick: () -> Unit) {
    Row(
        Modifier
            .height(28.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) OmniGradients.primaryAction else Brush.horizontalGradient(listOf(Color(0xFF0A1020), Color(0xFF0B1122))))
            .border(1.dp, if (active) IrisSoft.copy(alpha = 0.22f) else BorderLow.copy(alpha = 0.48f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        icon?.let { Icon(it, null, tint = if (active) Color.White else TextSecondary, modifier = Modifier.size(13.dp)) }
        Text(label, color = if (active) Color.White else TextSecondary, fontSize = 11.sp, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium)
    }
}

@Composable
private fun BrowsePrimaryButton(label: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(OmniGradients.primaryAction)
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(7.dp))
        Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BrowseSecondaryButton(label: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0B1020))
            .border(1.dp, BorderLow.copy(alpha = 0.58f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(7.dp))
        Text(label, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun BrowseIconButton(icon: ImageVector, description: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(0xFF11172A).copy(alpha = 0.82f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, description, tint = TextSecondary, modifier = Modifier.size(17.dp))
    }
}

@Composable
private fun BrowseFloatingPlay(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .size(25.dp)
            .clip(CircleShape)
            .background(IrisSoft.copy(alpha = 0.92f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(15.dp))
    }
}

@Composable
private fun BrowseSmallAction(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(27.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF0B1122))
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = IrisSoft, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(7.dp))
        Text(label, color = TextPrimary, fontSize = 10.sp)
    }
}

@Composable
private fun BrowseMenuItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(label, color = TextPrimary, fontSize = 12.sp) },
        leadingIcon = { Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(16.dp)) },
        onClick = onClick,
    )
}

@Composable
private fun BrowseTextAction(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = IrisSoft, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(7.dp))
        Text(label, color = IrisSoft, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun BrowseIconToggle(icon: ImageVector, active: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) OmniGradients.primaryAction else Brush.horizontalGradient(listOf(Color(0xFF0A1020), Color(0xFF0B1020))))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = if (active) Color.White else TextSecondary, modifier = Modifier.size(17.dp))
    }
}

@Composable
private fun BrowseSortButton(sort: BrowseSort, onSort: (BrowseSort) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        Row(
            Modifier
                .height(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0B1020))
                .border(1.dp, BorderLow.copy(alpha = 0.52f), RoundedCornerShape(8.dp))
                .clickable { open = true }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.AutoMirrored.Filled.Sort, null, tint = TextSecondary, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(8.dp))
            Text(sort.label, color = TextPrimary, fontSize = 11.sp)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }, containerColor = Color(0xF20B1020), tonalElevation = 0.dp) {
            BrowseSort.values().forEach { value ->
                DropdownMenuItem(text = { Text(value.label, color = TextPrimary) }, onClick = { open = false; onSort(value) })
            }
        }
    }
}

private val BrowseSort.label: String
    get() = when (this) {
        BrowseSort.Popular -> "Most Popular"
        BrowseSort.RecentlyAdded -> "Recently Added"
        BrowseSort.AZ -> "A - Z"
        BrowseSort.ZA -> "Z - A"
    }

@Composable
private fun BrowseFilterGroup(title: String, content: @Composable RowScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable
private fun BrowseLoadingState() {
    Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        repeat(5) {
            Box(Modifier.fillMaxWidth().height(if (it == 0) 160.dp else 82.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF0A1020).copy(alpha = 0.8f)))
        }
    }
}

@Composable
private fun BrowseRetryState(message: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(34.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Couldn't load Browse", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(message, color = TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(14.dp))
        BrowsePrimaryButton("Retry", Icons.Default.Search, onRetry)
    }
}

private fun playOrOpen(item: YTItem, player: PlayerViewModel) {
    when (item) {
        is SongItem -> player.playSong(item)
        is YtPlaylistItem -> player.openPlaylist(item.id)
        is AlbumItem -> player.openAlbum(item.browseId)
        is ArtistItem -> player.openArtist(item.id)
    }
}

private fun subtitleFor(item: YTItem): String = when (item) {
    is SongItem -> item.artists.joinToString(", ") { it.name }
    is YtPlaylistItem -> item.author?.name ?: item.songCountText ?: "Playlist"
    is AlbumItem -> item.artists?.joinToString(", ") { it.name } ?: "Album"
    is ArtistItem -> "Artist"
}

private fun formatDuration(seconds: Int?): String {
    val safe = seconds ?: return "—"
    val minutes = safe / 60
    val rem = safe % 60
    return "$minutes:${rem.toString().padStart(2, '0')}"
}
