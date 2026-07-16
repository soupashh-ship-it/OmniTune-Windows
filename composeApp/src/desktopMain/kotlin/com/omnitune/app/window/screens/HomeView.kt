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
import org.koin.compose.koinInject
import java.time.LocalTime

@Composable
fun HomeView(player: PlayerViewModel) {
    var home by remember { mutableStateOf<HomePage?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val service = koinInject<YouTubeService>()
    val currentSong by player.currentSong.collectAsState()
    val playbackState by player.playbackState.collectAsState()
    val liked by player.likedSongs.collectAsState()

    LaunchedEffect(Unit) {
        runCatching { service.home() }.onSuccess { home = it }.onFailure { error = it.message }
    }

    val loadedHome = home
    Box(Modifier.fillMaxSize()) {
        if (loadedHome == null && error == null) {
            HomeShimmer()
        } else if (error != null) {
            OmniEmptyState("Couldn't load Home", error ?: "Unknown error")
        } else if (loadedHome != null) {
            HomeContent(player, loadedHome, currentSong, playbackState, liked)
        }
    }
}



enum class HomeSectionType {
    FeaturedHero, ContinueListening, QuickPicks, Personalized, Trending, NewReleases, Unknown
}

@Composable
private fun HomeContent(player: PlayerViewModel, home: HomePage, currentSong: SongItem?, playbackState: PlaybackState, liked: Set<String>) {
    val queue by player.queue.collectAsState()

    fun playHomeItem(item: YTItem) {
        when (item) {
            is SongItem -> player.playSong(item)
            is AlbumItem -> player.openAlbum(item.browseId)
            is PlaylistItem -> player.openPlaylist(item.id)
            is ArtistItem -> player.openArtist(item.id)
        }
    }

    var heroIndex by remember(home) { mutableStateOf(0) }
    val referenceModel = remember(home, queue, heroIndex) {
        buildHomeReferenceModel(home = home, queueItems = queue, heroIndex = heroIndex)
    }

    ReferenceLockedHome(
        greeting = referenceModel.greeting,
        featuredItem = referenceModel.featuredItem,
        heroItems = referenceModel.companionItems,
        continueListening = referenceModel.continueListening,
        quickPicks = referenceModel.quickPicks,
        madeForYou = referenceModel.madeForYou,
        trending = referenceModel.trending,
        newReleases = referenceModel.newReleases,
        currentSong = currentSong,
        playbackState = playbackState,
        onHeroPrevious = {
            if (referenceModel.heroCandidateCount > 0) heroIndex = (heroIndex - 1 + referenceModel.heroCandidateCount) % referenceModel.heroCandidateCount
        },
        onHeroNext = {
            if (referenceModel.heroCandidateCount > 0) heroIndex = (heroIndex + 1) % referenceModel.heroCandidateCount
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

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wideLayout = maxWidth >= metrics.px(930f)
        val mediumLayout = maxWidth >= metrics.px(680f)
        val sectionGap = metrics.px(23f)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scroll)
                .padding(
                    start = metrics.px(23f),
                    end = metrics.px(23f),
                    top = metrics.px(19f),
                    bottom = metrics.px(42f),
                ),
            verticalArrangement = Arrangement.spacedBy(metrics.px(11f)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(metrics.px(HomeReferenceSpec.HeroPagerHeight)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = greeting,
                    modifier = Modifier
                        .weight(1f)
                        .offset(y = metrics.px(-5f)),
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

            if (wideLayout) {
                Row(horizontalArrangement = Arrangement.spacedBy(sectionGap)) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(metrics.px(11f)),
                    ) {
                        ReferenceFeaturedHero(
                            item = featuredItem,
                            sideItems = heroItems,
                            onPlayItem = onPlayItem,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(metrics.px(HomeReferenceSpec.HeroHeight)),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(12f))) {
                            ReferenceQuickPicks(
                                items = quickPicks,
                                onPlayItem = onPlayItem,
                                modifier = Modifier.weight(1f),
                            )
                            ReferenceMadeForYou(
                                items = madeForYou,
                                onPlayItem = onPlayItem,
                                modifier = Modifier.widthIn(min = metrics.px(300f), max = metrics.px(360f)),
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(20f))) {
                            ReferenceTrending(
                                items = trending,
                                currentSong = currentSong,
                                playbackState = playbackState,
                                player = player,
                                modifier = Modifier.weight(0.9f),
                            )
                            ReferenceNewReleases(
                                items = newReleases,
                                onPlayItem = onPlayItem,
                                modifier = Modifier.weight(1.1f),
                            )
                        }
                    }
                    ReferenceContinueListening(
                        items = continueListening,
                        currentSong = currentSong,
                        playbackState = playbackState,
                        onPlayItem = onPlayItem,
                        modifier = Modifier
                            .widthIn(min = metrics.px(240f), max = metrics.px(HomeReferenceSpec.ContinueWidth))
                            .heightIn(min = metrics.px(214f)),
                    )
                }
            } else {
                ReferenceFeaturedHero(
                    item = featuredItem,
                    sideItems = heroItems,
                    onPlayItem = onPlayItem,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (mediumLayout) metrics.px(HomeReferenceSpec.HeroHeight) else metrics.px(228f)),
                )
                ReferenceContinueListening(
                    items = continueListening,
                    currentSong = currentSong,
                    playbackState = playbackState,
                    onPlayItem = onPlayItem,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = metrics.px(214f)),
                )
                if (mediumLayout) {
                    Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(12f))) {
                        ReferenceQuickPicks(items = quickPicks, onPlayItem = onPlayItem, modifier = Modifier.weight(1f))
                        ReferenceMadeForYou(items = madeForYou, onPlayItem = onPlayItem, modifier = Modifier.weight(0.72f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(12f))) {
                        ReferenceTrending(
                            items = trending,
                            currentSong = currentSong,
                            playbackState = playbackState,
                            player = player,
                            modifier = Modifier.weight(0.86f),
                        )
                        ReferenceNewReleases(items = newReleases, onPlayItem = onPlayItem, modifier = Modifier.weight(1f))
                    }
                } else {
                    ReferenceQuickPicks(items = quickPicks, onPlayItem = onPlayItem, modifier = Modifier.fillMaxWidth())
                    ReferenceMadeForYou(items = madeForYou, onPlayItem = onPlayItem, modifier = Modifier.fillMaxWidth())
                    ReferenceTrending(
                        items = trending,
                        currentSong = currentSong,
                        playbackState = playbackState,
                        player = player,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    ReferenceNewReleases(items = newReleases, onPlayItem = onPlayItem, modifier = Modifier.fillMaxWidth())
                }
            }
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
                .weight(1f)
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
                        modifier = Modifier.fillMaxWidth(0.82f),
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
                .widthIn(min = metrics.px(136f), max = metrics.px(HomeReferenceSpec.HeroSideWidth))
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(metrics.px(5f)),
        ) {
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(metrics.px(2f)),
        ) {
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
                    else if (item is PlaylistItem) player.openPlaylist(item.id)
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(metrics.px(10f)),
        ) {
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
