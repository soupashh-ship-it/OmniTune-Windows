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
import java.time.LocalTime

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
    val queue by player.queue.collectAsState()

    // Robust Classification
    val usedIds = mutableSetOf<String>()

    // Helper to get unused items
    fun <T: YTItem> filterUnused(items: List<T>, take: Int): List<T> {
        val result = items.filter { it.id !in usedIds }.take(take)
        usedIds.addAll(result.map { it.id })
        return result
    }

    fun classify(sectionTitle: String): HomeSectionType {
        val normalized = sectionTitle.lowercase().trim()
        return when {
            normalized.contains("quick") || normalized.contains("pick") || normalized.contains("listen again") -> HomeSectionType.QuickPicks
            normalized.contains("made for") || normalized.contains("mix") -> HomeSectionType.Personalized
            normalized.contains("new release") || normalized.contains("new album") -> HomeSectionType.NewReleases
            normalized.contains("trending") || normalized.contains("popular") || normalized.contains("chart") -> HomeSectionType.Trending
            normalized.contains("continue") || normalized.contains("jump back") -> HomeSectionType.ContinueListening
            else -> HomeSectionType.Unknown
        }
    }

    val classifiedSections = home.sections.groupBy { section ->
        classify(section.title)
    }

    fun sectionItems(type: HomeSectionType): List<YTItem> =
        classifiedSections[type].orEmpty().flatMap { it.items }

    fun YTItem.stableIdentity(): String = "${id.ifBlank { title }}:${title}"

    fun pickDistinct(take: Int, vararg sources: List<YTItem>): List<YTItem> {
        val result = mutableListOf<YTItem>()
        val candidates = sources.flatMap { it }.distinctBy { it.stableIdentity() }
        candidates
            .distinctBy { it.stableIdentity() }
            .forEach { item ->
                if (result.size < take && usedIds.add(item.stableIdentity())) {
                    result += item
                }
            }
        candidates.forEach { item ->
            if (result.size < take && result.none { it.stableIdentity() == item.stableIdentity() }) {
                result += item
            }
        }
        result.forEach { usedIds.add(it.stableIdentity()) }
        return result
    }

    fun playHomeItem(item: YTItem) {
        when (item) {
            is SongItem -> player.playSong(item)
            is AlbumItem -> player.openAlbum(item.browseId)
            is PlaylistItem -> player.playPlaylist(item.id)
            is ArtistItem -> player.openArtist(item.id)
        }
    }

    val greeting = remember {
        val hour = LocalTime.now().hour
        when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    val queueItems = queue
    val allItems = home.sections.flatMap { it.items }
    val allSongs = allItems.filterIsInstance<SongItem>()
    val mediaItems = allItems.filter { it is AlbumItem || it is PlaylistItem || it is ArtistItem }
    val heroCandidates = mediaItems.ifEmpty { allItems }.filter { it.thumbnail != null }.distinctBy { it.stableIdentity() }
    var heroIndex by remember(home) { mutableStateOf(0) }
    val featuredItem = heroCandidates.getOrNull(heroIndex.coerceIn(0, (heroCandidates.size - 1).coerceAtLeast(0)))
    if (featuredItem != null) usedIds.add(featuredItem.stableIdentity())

    val companionItems = pickDistinct(
        4,
        allSongs,
        sectionItems(HomeSectionType.QuickPicks),
        allItems,
    )

    val continueListening = pickDistinct(
        4,
        queueItems,
        sectionItems(HomeSectionType.ContinueListening),
        allSongs,
        allItems,
    )

    val quickPicks = pickDistinct(
        6,
        sectionItems(HomeSectionType.QuickPicks),
        sectionItems(HomeSectionType.Personalized),
        allItems,
    )

    val madeForYou = pickDistinct(
        3,
        sectionItems(HomeSectionType.Personalized),
        mediaItems,
        allItems,
    )

    val trending = pickDistinct(
        5,
        sectionItems(HomeSectionType.Trending),
        allSongs,
        allItems,
    )

    val newReleases = pickDistinct(
        5,
        sectionItems(HomeSectionType.NewReleases),
        mediaItems,
        allItems,
    )

    ReferenceLockedHome(
        greeting = greeting,
        featuredItem = featuredItem,
        heroItems = companionItems,
        continueListening = continueListening,
        quickPicks = quickPicks,
        madeForYou = madeForYou,
        trending = trending,
        newReleases = newReleases,
        currentSong = currentSong,
        playbackState = playbackState,
        onHeroPrevious = {
            if (heroCandidates.isNotEmpty()) heroIndex = (heroIndex - 1 + heroCandidates.size) % heroCandidates.size
        },
        onHeroNext = {
            if (heroCandidates.isNotEmpty()) heroIndex = (heroIndex + 1) % heroCandidates.size
        },
        onPlayItem = { playHomeItem(it) },
        player = player,
    )
}

@Composable
private fun ReferenceLockedHome(
    greeting: String,
    featuredItem: YTItem?,
    heroItems: List<YTItem>,
    continueListening: List<YTItem>,
    quickPicks: List<YTItem>,
    madeForYou: List<YTItem>,
    trending: List<YTItem>,
    newReleases: List<YTItem>,
    currentSong: SongItem?,
    playbackState: PlaybackState,
    onHeroPrevious: () -> Unit,
    onHeroNext: () -> Unit,
    onPlayItem: (YTItem) -> Unit,
    player: PlayerViewModel,
) {
    val metrics = LocalHomeReferenceMetrics.current
    val scroll = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(metrics.px(523f))
        ) {
            Row(
                modifier = Modifier
                    .offset(x = metrics.px(23f), y = metrics.px(19f))
                    .width(metrics.px(HomeReferenceSpec.HeroWidth))
                    .height(metrics.px(HomeReferenceSpec.HeroPagerHeight)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = greeting,
                    modifier = Modifier.offset(y = metrics.px(-5f)),
                    color = OmniReferenceColors.TextPrimary,
                    fontSize = 20.sp,
                    lineHeight = 23.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.2).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                TargetHeroPager(
                    canGoPrevious = true,
                    canGoNext = true,
                    onPrevious = onHeroPrevious,
                    onNext = onHeroNext,
                    modifier = Modifier
                        .width(metrics.px(HomeReferenceSpec.HeroPagerWidth))
                        .height(metrics.px(HomeReferenceSpec.HeroPagerHeight)),
                )
            }

            ReferenceFeaturedHero(
                item = featuredItem,
                sideItems = heroItems,
                onPlayItem = onPlayItem,
                modifier = Modifier
                    .offset(x = metrics.px(23f), y = metrics.px(55f))
                    .width(metrics.px(HomeReferenceSpec.HeroWidth))
                    .height(metrics.px(HomeReferenceSpec.HeroHeight)),
            )

            ReferenceContinueListening(
                items = continueListening,
                currentSong = currentSong,
                playbackState = playbackState,
                onPlayItem = onPlayItem,
                modifier = Modifier
                    .offset(x = metrics.px(671f), y = metrics.px(55f))
                    .width(metrics.px(HomeReferenceSpec.ContinueWidth))
                    .height(metrics.px(214f)),
            )

            ReferenceQuickPicks(
                items = quickPicks,
                onPlayItem = onPlayItem,
                modifier = Modifier
                    .offset(x = metrics.px(23f), y = metrics.px(280f))
                    .width(metrics.px(565f)),
            )

            ReferenceMadeForYou(
                items = madeForYou,
                onPlayItem = onPlayItem,
                modifier = Modifier
                    .offset(x = metrics.px(600f), y = metrics.px(280f))
                    .width(metrics.px(330f)),
            )

            ReferenceTrending(
                items = trending,
                currentSong = currentSong,
                playbackState = playbackState,
                player = player,
                modifier = Modifier
                    .offset(x = metrics.px(23f), y = metrics.px(419f))
                    .width(metrics.px(398f)),
            )

            ReferenceNewReleases(
                items = newReleases,
                onPlayItem = onPlayItem,
                modifier = Modifier
                    .offset(x = metrics.px(447f), y = metrics.px(419f))
                    .width(metrics.px(477f)),
            )
        }
    }
}

@Composable
private fun ReferenceFeaturedHero(
    item: YTItem?,
    sideItems: List<YTItem>,
    onPlayItem: (YTItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalHomeReferenceMetrics.current
    val shape = RoundedCornerShape(metrics.px(12f))

    Row(
        modifier = modifier
            .clip(shape)
            .background(OmniReferenceColors.SurfaceRaised)
            .border(1.dp, OmniReferenceColors.BorderSoft, shape)
    ) {
        Box(
            modifier = Modifier
                .width(metrics.px(HomeReferenceSpec.HeroMainWidth))
                .fillMaxHeight()
        ) {
            if (item != null) {
                AsyncImage(
                    model = item.thumbnail?.toHighResThumbnail(),
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                0.00f to Color(0xF2040819),
                                0.35f to Color(0xB0040819),
                                0.72f to Color(0x30040819),
                                1.00f to Color(0x16040819),
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.00f to Color.Transparent,
                                0.58f to Color.Transparent,
                                1.00f to Color(0xCC040819),
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = metrics.px(26f), end = metrics.px(24f), bottom = metrics.px(21f)),
                ) {
                    Text(
                        text = when (item) {
                            is AlbumItem -> "FEATURED ALBUM"
                            is PlaylistItem -> "FEATURED PLAYLIST"
                            is ArtistItem -> "FEATURED ARTIST"
                            is SongItem -> "FEATURED TRACK"
                        },
                        color = OmniReferenceColors.TextSecondary,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.9.sp,
                        maxLines = 1,
                    )
                    Spacer(Modifier.height(metrics.px(5f)))
                    Text(
                        text = item.title,
                        color = OmniReferenceColors.TextPrimary,
                        fontSize = 20.sp,
                        lineHeight = 23.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(metrics.px(5f)))
                    Text(
                        text = itemSubtitle(item).ifBlank { "Personalized from your OmniTune home feed" },
                        color = OmniReferenceColors.TextSecondary,
                        fontSize = 10.sp,
                        lineHeight = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(metrics.px(292f)),
                    )
                    Spacer(Modifier.height(metrics.px(12f)))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(metrics.px(75f))
                                .height(metrics.px(31f))
                                .clip(RoundedCornerShape(metrics.px(99f)))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            OmniReferenceColors.AccentSoft,
                                            OmniReferenceColors.Accent,
                                            OmniReferenceColors.AccentBright,
                                        )
                                    )
                                )
                                .clickable { onPlayItem(item) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("Play Now", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(metrics.px(10f)))
                        Box(
                            modifier = Modifier
                                .size(metrics.px(31f))
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                                .clickable { onPlayItem(item) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.MoreHoriz, null, tint = OmniReferenceColors.TextSecondary, modifier = Modifier.size(metrics.px(17f)))
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .width(metrics.px(HomeReferenceSpec.HeroSideWidth))
                .fillMaxHeight()
                .background(OmniReferenceColors.SurfaceDeepRaised.copy(alpha = 0.88f))
                .padding(horizontal = metrics.px(7f), vertical = metrics.px(7f)),
            verticalArrangement = Arrangement.spacedBy(metrics.px(4f)),
        ) {
            sideItems.take(4).forEachIndexed { index, sideItem ->
                ReferenceHeroSideRow(
                    item = sideItem,
                    selected = index == 0,
                    onClick = { onPlayItem(sideItem) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ReferenceHeroSideRow(
    item: YTItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(metrics.px(7f)))
            .background(if (selected) OmniReferenceColors.SurfaceSelected else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = metrics.px(6f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = item.thumbnail?.toHighResThumbnail(),
            contentDescription = item.title,
            modifier = Modifier
                .size(metrics.px(35f))
                .clip(RoundedCornerShape(metrics.px(6f))),
            contentScale = ContentScale.Crop,
        )
        Spacer(Modifier.width(metrics.px(8f)))
        Column(Modifier.weight(1f)) {
            Text(item.title, color = OmniReferenceColors.TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(itemSubtitle(item), color = OmniReferenceColors.TextMuted, fontSize = 8.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ReferenceContinueListening(
    items: List<YTItem>,
    currentSong: SongItem?,
    playbackState: PlaybackState,
    onPlayItem: (YTItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalHomeReferenceMetrics.current
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(metrics.px(20f)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Continue Listening", color = OmniReferenceColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text("See all", color = OmniReferenceColors.AccentSoft, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(metrics.px(2f)))
        Column(verticalArrangement = Arrangement.spacedBy(metrics.px(3f))) {
            items.take(4).forEach { item ->
                val active = item is SongItem && item.id == currentSong?.id
                ReferenceContinueRow(
                    item = item,
                    active = active,
                    playing = active && playbackState == PlaybackState.PLAYING,
                    onClick = { onPlayItem(item) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(metrics.px(46f)),
                )
            }
        }
    }
}

@Composable
private fun ReferenceContinueRow(
    item: YTItem,
    active: Boolean,
    playing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalHomeReferenceMetrics.current
    val shape = RoundedCornerShape(metrics.px(8f))
    Row(
        modifier = modifier
            .clip(shape)
            .background(if (active) OmniReferenceColors.SurfaceSelected else OmniReferenceColors.SurfaceAlternate)
            .border(1.dp, OmniReferenceColors.SurfaceBorder.copy(alpha = 0.8f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = metrics.px(7f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = item.thumbnail?.toHighResThumbnail(),
            contentDescription = item.title,
            modifier = Modifier
                .size(metrics.px(35f))
                .clip(RoundedCornerShape(metrics.px(6f))),
            contentScale = ContentScale.Crop,
        )
        Spacer(Modifier.width(metrics.px(8f)))
        Column(Modifier.weight(1f)) {
            Text(item.title, color = OmniReferenceColors.TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(itemSubtitle(item), color = OmniReferenceColors.TextSecondary, fontSize = 8.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(metrics.px(3f)))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(metrics.px(2f))
                    .clip(RoundedCornerShape(metrics.px(99f)))
                    .background(OmniReferenceColors.SeekTrack)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (active) 0.48f else 0.18f)
                        .fillMaxHeight()
                        .background(OmniReferenceColors.SeekFill)
                )
            }
        }
        Spacer(Modifier.width(metrics.px(6f)))
        Box(
            modifier = Modifier
                .size(metrics.px(22f))
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(Color.White.copy(alpha = 0.09f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(metrics.px(14f)))
        }
    }
}

@Composable
private fun ReferenceQuickPicks(
    items: List<YTItem>,
    onPlayItem: (YTItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalHomeReferenceMetrics.current
    Column(modifier = modifier) {
        ReferenceSectionLabel("Quick Picks")
        Spacer(Modifier.height(metrics.px(6f)))
        Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(5f))) {
            items.take(6).forEach {
                ReferenceQuickPickCard(
                    item = it,
                    onClick = { onPlayItem(it) },
                    modifier = Modifier
                        .width(metrics.px(89f))
                        .height(metrics.px(97f)),
                )
            }
        }
    }
}

@Composable
private fun ReferenceQuickPickCard(item: YTItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val metrics = LocalHomeReferenceMetrics.current
    val shape = RoundedCornerShape(metrics.px(8f))
    Column(
        modifier = modifier
            .clip(shape)
            .background(OmniReferenceColors.SurfaceBase)
            .border(1.dp, OmniReferenceColors.SurfaceBorder.copy(alpha = 0.7f), shape)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = item.thumbnail?.toHighResThumbnail(),
            contentDescription = item.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(metrics.px(61f)),
            contentScale = ContentScale.Crop,
        )
        Column(Modifier.padding(horizontal = metrics.px(6f), vertical = metrics.px(5f))) {
            Text(item.title, color = OmniReferenceColors.TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(itemSubtitle(item), color = OmniReferenceColors.TextSecondary, fontSize = 7.8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ReferenceMadeForYou(
    items: List<YTItem>,
    onPlayItem: (YTItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalHomeReferenceMetrics.current
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ReferenceSectionLabel("Made for You")
            Spacer(Modifier.weight(1f))
            Text("See all", color = OmniReferenceColors.AccentSoft, fontSize = 9.5.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(metrics.px(7f)))
        Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(2f))) {
            items.take(3).forEach {
                ReferenceMadeCard(
                    item = it,
                    onClick = { onPlayItem(it) },
                    modifier = Modifier
                        .width(metrics.px(109f))
                        .height(metrics.px(95f)),
                )
            }
        }
    }
}

@Composable
private fun ReferenceMadeCard(item: YTItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val metrics = LocalHomeReferenceMetrics.current
    val tint = remember(item.id) {
        listOf(Color(0xFF245063), Color(0xFF38235C), Color(0xFF5C233E), Color(0xFF1D264F))[kotlin.math.abs(item.id.hashCode()) % 4]
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(metrics.px(8f)))
            .background(Brush.linearGradient(listOf(tint.copy(alpha = 0.42f), OmniReferenceColors.SurfaceDeepRaised)))
            .border(1.dp, OmniReferenceColors.SurfaceBorder.copy(alpha = 0.62f), RoundedCornerShape(metrics.px(8f)))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = item.thumbnail?.toHighResThumbnail(),
            contentDescription = item.title,
            modifier = Modifier.fillMaxSize().alpha(0.22f),
            contentScale = ContentScale.Crop,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(metrics.px(9f)),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(item.title, color = OmniReferenceColors.TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, lineHeight = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(itemSubtitle(item).ifBlank { "Personalized mix" }, color = OmniReferenceColors.TextSecondary, fontSize = 7.8.sp, lineHeight = 10.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(metrics.px(22f))
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(metrics.px(14f)))
                }
            }
        }
    }
}

@Composable
private fun ReferenceTrending(
    items: List<YTItem>,
    currentSong: SongItem?,
    playbackState: PlaybackState,
    player: PlayerViewModel,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalHomeReferenceMetrics.current
    Column(modifier = modifier) {
        ReferenceSectionLabel("Trending Now")
        Spacer(Modifier.height(metrics.px(7f)))
        items.take(2).forEachIndexed { index, item ->
            val active = item is SongItem && item.id == currentSong?.id
            ReferenceTrendingRow(
                item = item,
                rank = index + 1,
                active = active,
                playing = active && playbackState == PlaybackState.PLAYING,
                onClick = {
                    if (item is SongItem) player.playSong(item)
                    else if (item is AlbumItem) player.openAlbum(item.browseId)
                    else if (item is PlaylistItem) player.playPlaylist(item.id)
                    else if (item is ArtistItem) player.openArtist(item.id)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(metrics.px(41f)),
            )
        }
    }
}

@Composable
private fun ReferenceTrendingRow(
    item: YTItem,
    rank: Int,
    active: Boolean,
    playing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(metrics.px(6f)))
            .background(if (active) OmniReferenceColors.SurfaceSelected.copy(alpha = 0.7f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = metrics.px(5f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(rank.toString(), color = OmniReferenceColors.TextSecondary, fontSize = 10.sp, modifier = Modifier.width(metrics.px(18f)))
        AsyncImage(
            model = item.thumbnail?.toHighResThumbnail(),
            contentDescription = item.title,
            modifier = Modifier
                .size(metrics.px(30f))
                .clip(RoundedCornerShape(metrics.px(5f))),
            contentScale = ContentScale.Crop,
        )
        Spacer(Modifier.width(metrics.px(8f)))
        Column(Modifier.weight(1.15f)) {
            Text(item.title, color = if (active) OmniReferenceColors.AccentSoft else OmniReferenceColors.TextPrimary, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(itemSubtitle(item), color = OmniReferenceColors.TextSecondary, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(itemAlbumLabel(item), color = OmniReferenceColors.TextMuted, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(0.75f))
        Text(durationLabel(item), color = OmniReferenceColors.TextMuted, fontSize = 8.sp, modifier = Modifier.width(metrics.px(30f)))
        Icon(Icons.Default.Add, null, tint = OmniReferenceColors.TextMuted, modifier = Modifier.size(metrics.px(13f)))
        Spacer(Modifier.width(metrics.px(8f)))
        Icon(Icons.Default.MoreHoriz, null, tint = OmniReferenceColors.TextMuted, modifier = Modifier.size(metrics.px(13f)))
    }
}

@Composable
private fun ReferenceNewReleases(
    items: List<YTItem>,
    onPlayItem: (YTItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalHomeReferenceMetrics.current
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ReferenceSectionLabel("New Releases")
            Spacer(Modifier.weight(1f))
            Text("See all", color = OmniReferenceColors.AccentSoft, fontSize = 9.5.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(metrics.px(7f)))
        Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(10f))) {
            items.take(5).forEach {
                ReferenceReleaseCard(
                    item = it,
                    onClick = { onPlayItem(it) },
                    modifier = Modifier
                        .width(metrics.px(86f))
                        .height(metrics.px(116f)),
                )
            }
        }
    }
}

@Composable
private fun ReferenceReleaseCard(item: YTItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val metrics = LocalHomeReferenceMetrics.current
    Column(modifier = modifier.clickable(onClick = onClick)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(metrics.px(70f))
                .clip(RoundedCornerShape(metrics.px(8f)))
        ) {
            AsyncImage(
                model = item.thumbnail?.toHighResThumbnail(),
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(metrics.px(5f))
                    .size(metrics.px(18f))
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(metrics.px(11f)))
            }
        }
        Spacer(Modifier.height(metrics.px(5f)))
        Text(item.title, color = OmniReferenceColors.TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(itemSubtitle(item), color = OmniReferenceColors.TextSecondary, fontSize = 7.8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun ReferenceSectionLabel(title: String) {
    Text(
        text = title,
        color = OmniReferenceColors.TextPrimary,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
    )
}

private fun itemSubtitle(item: YTItem): String = when (item) {
    is SongItem -> item.artists.joinToString(", ") { it.name }
    is AlbumItem -> item.artists?.joinToString(", ") { it.name ?: "" }.orEmpty()
    is PlaylistItem -> item.author?.name ?: item.songCountText ?: "Playlist"
    is ArtistItem -> "Artist"
}

private fun itemAlbumLabel(item: YTItem): String = when (item) {
    is SongItem -> item.album?.name.orEmpty()
    is AlbumItem -> item.year?.toString() ?: "Album"
    is PlaylistItem -> "Playlist"
    is ArtistItem -> "Artist"
}

private fun durationLabel(item: YTItem): String {
    val duration = (item as? SongItem)?.duration ?: return ""
    return "${duration / 60}:${(duration % 60).toString().padStart(2, '0')}"
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
    val motionPolicy = LocalOmniMotionPolicy.current

    val scale by animateFloatAsState(
        if (motionPolicy.decorativeMotionEnabled && isHovered) 1.015f else 1f,
        tween(motionPolicy.shortDurationMs)
    )
    val subtitle = when (item) {
        is SongItem -> item.artists?.joinToString(", ") { it.name ?: "" } ?: ""
        is AlbumItem -> item.artists?.joinToString(", ") { it.name ?: "" } ?: ""
        is PlaylistItem -> "Playlist"
        is ArtistItem -> "Artist"
        else -> ""
    }

    Column(
        modifier = Modifier
            .scale(scale)
            .width(126.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0A0F20))
            .border(
                width = 1.dp,
                color = Color(0xFF181D34).copy(alpha = 0.70f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null) {
                if (item is AlbumItem) player.openAlbum(item.browseId)
                else if (item is PlaylistItem) player.playPlaylist(item.id)
                else if (item is ArtistItem) player.openArtist(item.id)
                else if (item is SongItem) player.playSong(item, 0)
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            AsyncImage(
                model = item.thumbnail?.toHighResThumbnail(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
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

        Column(
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 8.dp
            )
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF9495A7),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
        val d = if (item is SongItem) item.duration else null
        val duration = if (d != null) {
            val mins = d / 60
            val secs = d % 60
            "$mins:${secs.toString().padStart(2, '0')}"
        } else ""
        Text(duration, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
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
        targetValue = if (selected) com.omnitune.app.window.OmniReferenceColors.SurfaceSelected else if (isHovered) com.omnitune.app.window.OmniReferenceColors.SurfaceRaised else com.omnitune.app.window.OmniReferenceColors.SurfaceBase,
        animationSpec = tween(160)
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) Color(0xFF252C58).copy(alpha = 0.60f) else com.omnitune.app.window.OmniReferenceColors.SurfaceBorder.copy(alpha = 0.72f),
        animationSpec = tween(160)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
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
private fun TargetHeroPager(
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .width(48.dp)
            .height(24.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xFF0E1223)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Icon(
            Icons.Default.ChevronLeft,
            contentDescription = "Previous",
            tint = Color.White.copy(alpha=if (canGoPrevious) 0.7f else 0.3f),
            modifier = Modifier.size(16.dp).clickable(enabled = canGoPrevious, onClick = onPrevious)
        )
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = "Next",
            tint = Color.White.copy(alpha=if (canGoNext) 0.7f else 0.3f),
            modifier = Modifier.size(16.dp).clickable(enabled = canGoNext, onClick = onNext)
        )
    }
}

@Composable
private fun HomeHeroRow(album: YTItem?, continueListening: List<YTItem>, player: PlayerViewModel, currentSong: SongItem?, playbackState: PlaybackState) {
    val shape = RoundedCornerShape(14.dp)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        // Left side: Cinematic main hero
        Column(
            modifier = Modifier.weight(2.35f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(25.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Good evening, Alex",
                    color = Color.White,
                    fontSize = 20.sp,
                    lineHeight = 23.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.2).sp,
                    maxLines = 1
                )

                TargetHeroPager(
                    canGoPrevious = true, // Placeholders for now if pagination isn't strictly tracked
                    canGoNext = true,
                    onPrevious = { },
                    onNext = { }
                )
            }

            Spacer(modifier = Modifier.height(13.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
                    .clip(shape)
                    .background(com.omnitune.app.window.OmniReferenceColors.SurfaceRaised)
                    .border(1.dp, com.omnitune.app.window.OmniReferenceColors.SurfaceBorder, shape)
            ) {
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
                    0.00f to Color(0xFF131131).copy(alpha = 0.50f),
                    0.40f to Color.Transparent,
                    1.00f to Color(0xFF1A1535).copy(alpha = 0.70f) // Matches user spec for hero dark overlay
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
                                .background(Brush.horizontalGradient(listOf(Color(0xFF7E84F6), Color(0xFF7D82FB), Color(0xFF8085FB), Color(0xFF787EF8))))
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
                        Box(modifier = Modifier.size(40.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color.White.copy(alpha=0.08f)).clickable {
                            if (album is SongItem) player.playNext(album)
                            else if (album is PlaylistItem) player.openPlaylist(album.id)
                            else if (album is AlbumItem) player.openAlbum(album.browseId)
                        }, contentAlignment=Alignment.Center) {
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

        }

        // Right side: Continue Listening
        Column(
            modifier = Modifier
                .weight(1.0f)
                .height(290.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Continue Listening", style = MaterialTheme.typography.titleMedium, color = Color(0xFFF4F3FA), fontWeight = FontWeight.Bold)
                Text("See all", style = MaterialTheme.typography.labelMedium, color = com.omnitune.app.window.OmniReferenceColors.AccentSoft, modifier = Modifier.clickable { player.navigateTo(com.omnitune.app.player.NavScreen.Queue) })
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxHeight()) {
                continueListening.take(4).forEachIndexed { i, song ->
                    val isSelected = i == 0 // Fake selected state for visual QA, or derive from real state
                    FeaturedCompanionRow(song, selected = isSelected, onClick = {
                        if (song is SongItem) player.playSong(song)
                        else if (song is PlaylistItem) player.playPlaylist(song.id)
                        else if (song is AlbumItem) player.openAlbum(song.browseId)
                    })
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, showSeeAll: Boolean = false, onSeeAll: (() -> Unit)? = null) {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        if (showSeeAll) {
            Text(
                "See all",
                style = MaterialTheme.typography.labelMedium,
                color = IrisSoft,
                modifier = if (onSeeAll != null) Modifier.clickable(onClick = onSeeAll) else Modifier,
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
