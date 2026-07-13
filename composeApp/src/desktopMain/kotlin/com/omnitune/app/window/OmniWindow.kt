package com.omnitune.app.window

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import com.omnitune.app.player.NavScreen
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.app.platform.PlaybackState
import com.omnitune.app.platform.SettingsRepository
import com.omnitune.app.window.screens.*
import com.omnitune.innertube.models.AlbumItem
import com.omnitune.innertube.models.ArtistItem
import com.omnitune.innertube.models.PlaylistItem
import org.koin.compose.koinInject
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Color
import org.koin.compose.koinInject

@Composable
fun WindowScope.OmniWindow(
    windowState: WindowState,
    onClose: () -> Unit,
    onMinimizeToTray: () -> Unit
) {
    val player: PlayerViewModel = koinInject()
    val navScreen by player.navScreen.collectAsState()
    val currentSong by player.currentSong.collectAsState()
    val playbackState by player.playbackState.collectAsState()
    val pos by player.position.collectAsState()
    val volume by player.volume.collectAsState()
    val canGoBack by player.canGoBack.collectAsState()
    val canGoForward by player.canGoForward.collectAsState()
    val liked by player.likedSongs.collectAsState()
    val discoveryNew by player.discoveryNew.collectAsState()
    val discoveryTrending by player.discoveryTrending.collectAsState()
    val settings: SettingsRepository = koinInject()
    val reducedMotion by settings.reduceMotionFlow.collectAsState()
    val themeMode by settings.appearanceThemeFlow.collectAsState()
    val qaTheme = remember { System.getenv("OMNITUNE_QA_THEME")?.trim()?.lowercase().orEmpty() }
    val effectiveTheme = qaTheme.takeIf { it in setOf("nocturne", "midnight", "dusk", "aurora") } ?: themeMode
    var query by remember { mutableStateOf("") }
    val searchFocus = remember { FocusRequester() }
    val qaRoute = remember { System.getenv("OMNITUNE_QA_ROUTE")?.trim()?.lowercase().orEmpty() }
    var qaRouteApplied by remember(qaRoute) { mutableStateOf(false) }

    LaunchedEffect(qaRoute, discoveryNew, discoveryTrending, currentSong) {
        if (qaRoute.isBlank() || qaRouteApplied) return@LaunchedEffect

        when (qaRoute) {
            "home" -> player.navigateTo(NavScreen.Home).also { qaRouteApplied = true }
            "library" -> player.navigateTo(NavScreen.Library).also { qaRouteApplied = true }
            "search" -> player.navigateTo(NavScreen.Search).also { qaRouteApplied = true }
            "queue" -> player.navigateTo(NavScreen.Queue).also { qaRouteApplied = true }
            "settings" -> player.navigateTo(NavScreen.Settings).also { qaRouteApplied = true }
            "downloads" -> player.navigateTo(NavScreen.Downloads).also { qaRouteApplied = true }
            "playlist" -> {
                discoveryNew.filterIsInstance<PlaylistItem>().firstOrNull()?.let {
                    player.openPlaylist(it.id)
                    qaRouteApplied = true
                }
            }
            "artist" -> {
                val artistId = discoveryNew.filterIsInstance<ArtistItem>().firstOrNull()?.id
                    ?: discoveryTrending.asSequence()
                        .flatMap { it.artists.asSequence() }
                        .firstOrNull { !it.id.isNullOrBlank() }
                        ?.id
                artistId?.let {
                    player.openArtist(it)
                    qaRouteApplied = true
                }
            }
            "album" -> {
                val albumId = discoveryNew.filterIsInstance<AlbumItem>().firstOrNull()?.browseId
                    ?: discoveryTrending.firstOrNull { it.album?.id?.isNotBlank() == true }?.album?.id
                albumId?.let {
                    player.openAlbum(it)
                    qaRouteApplied = true
                }
            }
            "nowplaying" -> {
                val song = currentSong ?: discoveryTrending.firstOrNull()
                song?.let {
                    if (currentSong == null) player.playSong(it)
                    player.navigateTo(NavScreen.NowPlaying)
                    qaRouteApplied = true
                }
            }
        }
    }

    OmniTuneTheme(reducedMotion = reducedMotion, theme = effectiveTheme) {
        val isMaximized = windowState.placement == WindowPlacement.Maximized
        val windowShape = if (isMaximized) RectangleShape else RoundedCornerShape(16.dp)

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .clip(windowShape)
                .border(
                    width = if (isMaximized) 0.dp else 1.dp,
                    color = Color(0xFF7768FF).copy(alpha = 0.32f),
                    shape = windowShape,
                )
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyUp) {
                        when (event.key) {
                            Key.Spacebar -> { player.togglePlayPause(); true }
                            Key.DirectionLeft -> { player.seekRelative(-5000); true }
                            Key.DirectionRight -> { player.seekRelative(5000); true }
                            Key.N -> { player.nextTrack(); true }
                            Key.P -> { player.previousTrack(); true }
                            Key.K -> {
                                if (event.isCtrlPressed) {
                                    player.navigateTo(NavScreen.Search)
                                    searchFocus.requestFocus()
                                    true
                                } else false
                            }
                            else -> false
                        }
                    } else false
                }
        ) {
            val shellHeight = maxHeight
            val metrics = rememberHomeReferenceMetrics(maxWidth)
            CompositionLocalProvider(LocalHomeReferenceMetrics provides metrics) {
                val motionPolicy = LocalOmniMotionPolicy.current
                val sidebarWidth = metrics.px(HomeReferenceSpec.SidebarWidth)
                val topBarHeight = metrics.px(HomeReferenceSpec.TopBarHeight)
                val playerHeight = metrics.px(HomeReferenceSpec.PlayerHeight)
                val playerBottomInset = metrics.px(HomeReferenceSpec.PlayerBottomMargin)
                val playerHorizontalInset = metrics.px(HomeReferenceSpec.PlayerX)
                val mainWidth = maxWidth - sidebarWidth

                OmniReferenceBackdrop(
                    sidebarWidthDp = sidebarWidth,
                    topBarHeightDp = topBarHeight,
                ) {
                    OmniSidebar(
                        activeScreen = navScreen,
                        hasCurrentSong = currentSong != null,
                        currentSong = currentSong,
                        likedCount = liked.size,
                        onNavigate = { player.navigateTo(it) },
                        width = sidebarWidth,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .width(sidebarWidth)
                            .fillMaxHeight()
                            .padding(bottom = playerHeight + playerBottomInset + metrics.px(8f))
                    )

                    WindowDraggableArea(
                        modifier = Modifier
                            .offset(x = sidebarWidth)
                            .width(mainWidth)
                            .height(topBarHeight)
                    ) {
                        OmniTopBar(
                            query = query,
                            onQueryChange = { query = it },
                            onSearch = { player.search(it) },
                            onNavigateToSearch = { player.navigateTo(NavScreen.Search) },
                            canGoBack = canGoBack,
                            canGoForward = canGoForward,
                            onBack = { player.back() },
                            onForward = { player.forward() },
                            focusRequester = searchFocus,
                            modifier = Modifier.fillMaxSize(),
                            onClose = onClose,
                            onMinimize = onMinimizeToTray,
                            onMaximize = {
                                if (isMaximized) windowState.placement = WindowPlacement.Floating
                                else windowState.placement = WindowPlacement.Maximized
                            },
                            onOpenSettings = { player.navigateTo(NavScreen.Settings) }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .offset(x = sidebarWidth, y = topBarHeight)
                            .width(mainWidth)
                            .height(shellHeight - topBarHeight)
                            .padding(bottom = playerHeight + playerBottomInset + metrics.px(8f))
                    ) {
                        AnimatedContent(
                            targetState = navScreen,
                            transitionSpec = {
                                (fadeIn(animationSpec = OmniMotion.screenTween(motionPolicy)) + scaleIn(initialScale = 0.99f, animationSpec = OmniMotion.screenTween(motionPolicy))) togetherWith fadeOut(animationSpec = tween(motionPolicy.shortDurationMs))
                            },
                            label = "nav",
                        ) { screen ->
                            when (screen) {
                                NavScreen.Home -> HomeView(player)
                                NavScreen.Browse -> BrowseView(player)
                                NavScreen.Radio -> RadioView(player)
                                NavScreen.Library -> LibraryView(player)
                                NavScreen.Search -> SearchView(player, query, onQueryChange = { query = it })
                                NavScreen.NowPlaying -> NowPlayingView(player, currentSong, playbackState, pos, volume)
                                NavScreen.Queue -> QueueView(player)
                                NavScreen.Playlists -> PlaylistsView(player)
                                NavScreen.Settings -> SettingsView()
                                NavScreen.Artist -> ArtistView(player)
                                NavScreen.Album -> AlbumView(player)
                                NavScreen.PlaylistDetail -> PlaylistDetailView(player)
                                NavScreen.Downloads -> DownloadsView(player)
                            }
                        }
                    }

                    OmniBottomPlayer(
                    player = player,
                    currentSong = currentSong,
                    playbackState = playbackState,
                    position = pos,
                    volume = volume,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(
                            start = playerHorizontalInset,
                            end = playerHorizontalInset,
                            bottom = playerBottomInset,
                        )
                        .fillMaxWidth()
                        .height(playerHeight)
                    )
                }
            }
        }
    }
}
