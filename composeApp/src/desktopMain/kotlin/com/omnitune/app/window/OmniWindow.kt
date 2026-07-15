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
import com.omnitune.app.platform.QaRuntime
import com.omnitune.app.platform.SettingsRepository
import com.omnitune.app.window.screens.*
import com.omnitune.innertube.models.AlbumItem
import com.omnitune.innertube.models.ArtistItem
import com.omnitune.innertube.models.PlaylistItem
import com.omnitune.innertube.models.SongItem
import org.koin.compose.koinInject
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
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
    val volume by player.volume.collectAsState()
    val canGoBack by player.canGoBack.collectAsState()
    val canGoForward by player.canGoForward.collectAsState()
    val liked by player.likedSongs.collectAsState()
    val savedPlaylists by player.savedQueuePlaylists.collectAsState()
    val currentPlaylistId by player.currentPlaylistId.collectAsState()
    val discoveryNew by player.discoveryNew.collectAsState()
    val discoveryTrending by player.discoveryTrending.collectAsState()
    val searchResults by player.searchResults.collectAsState()
    val searchLoading by player.searchLoading.collectAsState()
    val searchError by player.searchError.collectAsState()
    val settings: SettingsRepository = koinInject()
    val reducedMotion by settings.reduceMotionFlow.collectAsState()
    val themeMode by settings.appearanceThemeFlow.collectAsState()
    val globalShortcutsEnabled by settings.globalShortcutsFlow.collectAsState()
    val qaTheme = remember { QaRuntime.theme }
    val effectiveTheme = qaTheme.takeIf { it in setOf("nocturne", "midnight", "dusk", "aurora") } ?: themeMode
    var query by remember { mutableStateOf("") }
    val searchFocus = remember { FocusRequester() }
    val qaRoute = remember { QaRuntime.route }
    val qaSearchQuery = remember { QaRuntime.searchQuery }
    var qaRouteApplied by remember(qaRoute) { mutableStateOf(false) }
    var qaSearchStarted by remember(qaSearchQuery) { mutableStateOf(false) }
    var qaSearchReported by remember(qaSearchQuery) { mutableStateOf(false) }
    var editableTextFocused by remember { mutableStateOf(false) }
    var modalOpen by remember { mutableStateOf(false) }

    LaunchedEffect(qaRoute, discoveryNew, discoveryTrending, currentSong) {
        if (qaRoute.isBlank() || qaRouteApplied) return@LaunchedEffect

        when (qaRoute) {
            "home" -> player.navigateTo(NavScreen.Home).also { qaRouteApplied = true }
            "browse" -> player.navigateTo(NavScreen.Browse).also { qaRouteApplied = true }
            "radio" -> player.navigateTo(NavScreen.Radio).also { qaRouteApplied = true }
            "library" -> player.navigateTo(NavScreen.Library).also { qaRouteApplied = true }
            "liked", "likedsongs" -> player.navigateTo(NavScreen.LikedSongs).also { qaRouteApplied = true }
            "search" -> player.navigateTo(NavScreen.Search).also { qaRouteApplied = true }
            "queue" -> player.navigateTo(NavScreen.Queue).also { qaRouteApplied = true }
            "settings" -> player.navigateTo(NavScreen.Settings).also { qaRouteApplied = true }
            "downloads" -> player.navigateTo(NavScreen.Downloads).also { qaRouteApplied = true }
            "playlist" -> {
                val qaPlaylistState = QaRuntime.playlistState
                val savedForState = if (qaPlaylistState in setOf("expanded", "menu", "add", "edit")) {
                    settings.savedQueuePlaylists.firstOrNull { it.songs.isNotEmpty() }?.id
                        ?: settings.savedQueuePlaylists.firstOrNull()?.id
                } else null
                if (savedForState != null) {
                    player.openPlaylist(savedForState)
                    qaRouteApplied = true
                } else discoveryNew.filterIsInstance<PlaylistItem>().firstOrNull()?.let {
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

    LaunchedEffect(qaSearchQuery) {
        if (qaSearchQuery.isBlank() || qaSearchStarted) return@LaunchedEffect
        query = qaSearchQuery
        player.navigateTo(NavScreen.Search)
        player.search(qaSearchQuery)
        qaSearchStarted = true
    }

    LaunchedEffect(qaSearchStarted, searchLoading, searchResults, searchError) {
        if (!qaSearchStarted || qaSearchReported || searchLoading) return@LaunchedEffect
        if (searchResults.isEmpty() && searchError == null) return@LaunchedEffect

        val projectRoot = File(System.getProperty("user.dir")).let {
            if (it.name == "composeApp") it.parentFile else it
        }
        val reportFile = File(projectRoot, "docs/qa/search-runtime-qa.json")
        reportFile.parentFile.mkdirs()
        val songs = searchResults.filterIsInstance<SongItem>()
        val artists = searchResults.filterIsInstance<ArtistItem>()
        val albums = searchResults.filterIsInstance<AlbumItem>()
        val playlists = searchResults.filterIsInstance<PlaylistItem>()
        reportFile.writeText(
            JSONObject()
                .put("query", qaSearchQuery)
                .put("totalResults", searchResults.size)
                .put("songResults", songs.size)
                .put("artistResults", artists.size)
                .put("albumResults", albums.size)
                .put("playlistResults", playlists.size)
                .put("error", searchError ?: JSONObject.NULL)
                .put("firstResults", JSONArray().also { array ->
                    searchResults.take(10).forEach { item ->
                        array.put(
                            JSONObject()
                                .put("type", item::class.simpleName)
                                .put("id", item.id)
                                .put("title", item.title)
                        )
                    }
                })
                .put("result", searchResults.isNotEmpty() && searchError == null)
                .toString(2)
        )
        qaSearchReported = true
    }

    OmniTuneTheme(reducedMotion = reducedMotion, theme = effectiveTheme) {
        val isMaximized = windowState.placement == WindowPlacement.Maximized
        val windowShape = if (isMaximized) RectangleShape else RoundedCornerShape(16.dp)
        val pressedShortcutKeys = remember { OmniPressedKeyTracker() }

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
                    when (event.type) {
                        KeyEventType.KeyUp -> {
                            pressedShortcutKeys.repeatedKeyDownFor(event.key, event.type)
                            false
                        }
                        KeyEventType.KeyDown -> {
                            if (!globalShortcutsEnabled) return@onKeyEvent false
                            val repeated = pressedShortcutKeys.repeatedKeyDownFor(event.key, event.type)
                            when (
                                OmniKeyboardShortcutRouter.commandFor(
                                    OmniKeyboardInput(
                                        key = event.key,
                                        type = event.type,
                                        ctrlPressed = event.isCtrlPressed,
                                        editableTextFocused = editableTextFocused,
                                        modalOpen = modalOpen,
                                        repeatedKeyDown = repeated,
                                    )
                                )
                            ) {
                                OmniKeyboardCommand.TogglePlayPause -> { player.togglePlayPause(); true }
                                OmniKeyboardCommand.SeekBackward -> { player.seekRelative(-5000); true }
                                OmniKeyboardCommand.SeekForward -> { player.seekRelative(5000); true }
                                OmniKeyboardCommand.NextTrack -> { player.nextTrack(); true }
                                OmniKeyboardCommand.PreviousTrack -> { player.previousTrack(); true }
                                OmniKeyboardCommand.FocusSearch -> {
                                    player.navigateTo(NavScreen.Search)
                                    searchFocus.requestFocus()
                                    true
                                }
                                OmniKeyboardCommand.OpenSettings -> {
                                    player.navigateTo(NavScreen.Settings)
                                    true
                                }
                                null -> false
                            }
                        }
                        else -> false
                    }
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
                        savedPlaylists = savedPlaylists,
                        currentPlaylistId = currentPlaylistId,
                        onNavigate = { player.navigateTo(it) },
                        onOpenPlaylist = { player.openPlaylist(it) },
                        onCreatePlaylist = { player.createPlaylist(it) },
                        width = sidebarWidth,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .width(sidebarWidth)
                            .fillMaxHeight()
                            .padding(bottom = playerHeight + playerBottomInset + metrics.px(8f))
                    )

                    Box(
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
                            onOpenSettings = { player.navigateTo(NavScreen.Settings) },
                            onSearchFocusChanged = {
                                editableTextFocused = it
                                pressedShortcutKeys.clear()
                            },
                        )

                        val leftDragX = metrics.px(96f)
                        val leftDragWidth = metrics.px(88f)
                        val rightDragX = metrics.px(730f)
                        val rightDragWidth = (mainWidth - rightDragX - metrics.px(240f)).coerceAtLeast(0.dp)

                        WindowDraggableArea(
                            modifier = Modifier
                                .offset(x = leftDragX)
                                .width(leftDragWidth)
                                .fillMaxHeight()
                        ) {
                            Box(Modifier.fillMaxSize())
                        }

                        if (rightDragWidth > 0.dp) {
                            WindowDraggableArea(
                                modifier = Modifier
                                    .offset(x = rightDragX)
                                    .width(rightDragWidth)
                                    .fillMaxHeight()
                            ) {
                                Box(Modifier.fillMaxSize())
                            }
                        }
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
                                NavScreen.LikedSongs -> LikedSongsView(player)
                                NavScreen.Search -> SearchView(
                                    player,
                                    query,
                                    onQueryChange = { query = it },
                                    onSearchFieldFocusChanged = {
                                        editableTextFocused = it
                                        pressedShortcutKeys.clear()
                                    },
                                )
                                NavScreen.NowPlaying -> NowPlayingRoute(player, currentSong, playbackState, volume)
                                NavScreen.Queue -> QueueView(
                                    player,
                                    onEditableTextFocusChanged = {
                                        editableTextFocused = it
                                        pressedShortcutKeys.clear()
                                    },
                                    onModalOpenChanged = {
                                        modalOpen = it
                                        pressedShortcutKeys.clear()
                                    },
                                )
                                NavScreen.Playlists -> PlaylistsView(
                                    player,
                                    onEditableTextFocusChanged = {
                                        editableTextFocused = it
                                        pressedShortcutKeys.clear()
                                    },
                                )
                                NavScreen.Settings -> SettingsView()
                                NavScreen.Artist -> ArtistView(player)
                                NavScreen.Album -> AlbumView(player)
                                NavScreen.PlaylistDetail -> PlaylistDetailView(player)
                                NavScreen.Downloads -> DownloadsView(player)
                            }
                        }
                    }

                    BottomPlayerRoute(
                        player = player,
                        currentSong = currentSong,
                        playbackState = playbackState,
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

@Composable
private fun NowPlayingRoute(
    player: PlayerViewModel,
    currentSong: SongItem?,
    playbackState: PlaybackState,
    volume: Int,
) {
    val position by player.position.collectAsState()
    NowPlayingView(player, currentSong, playbackState, position, volume)
}

@Composable
private fun BottomPlayerRoute(
    player: PlayerViewModel,
    currentSong: SongItem?,
    playbackState: PlaybackState,
    volume: Int,
    modifier: Modifier = Modifier,
) {
    val position by player.position.collectAsState()
    OmniBottomPlayer(
        player = player,
        currentSong = currentSong,
        playbackState = playbackState,
        position = position,
        volume = volume,
        modifier = modifier,
    )
}
