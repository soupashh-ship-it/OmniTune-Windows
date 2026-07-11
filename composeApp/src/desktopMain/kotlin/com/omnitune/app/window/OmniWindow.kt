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
import com.omnitune.app.window.screens.*
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
    var query by remember { mutableStateOf("") }
    val searchFocus = remember { FocusRequester() }

    OmniTuneTheme {
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
            val sidebarWidth = (maxWidth * 0.157f).coerceIn(184.dp, 262.dp)
            val topBarHeight = (maxHeight * 0.074f).coerceIn(52.dp, 72.dp)
            val playerHeight = (maxHeight * 0.117f).coerceIn(92.dp, 112.dp)

            NocturneBackdrop {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = playerHeight)
                ) {
                    OmniSidebar(
                        activeScreen = navScreen,
                        hasCurrentSong = currentSong != null,
                        currentSong = currentSong,
                        likedCount = liked.size,
                        onNavigate = { player.navigateTo(it) },
                    )
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        WindowDraggableArea {
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
                                modifier = Modifier.fillMaxWidth().height(topBarHeight),
                                onClose = onClose,
                                onMinimize = onMinimizeToTray,
                                onMaximize = {
                                    if (isMaximized) windowState.placement = WindowPlacement.Floating
                                    else windowState.placement = WindowPlacement.Maximized
                                }
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                        ) {
                            AnimatedContent(
                                targetState = navScreen,
                                transitionSpec = {
                                    (fadeIn(animationSpec = OmniMotion.screenTween()) + scaleIn(initialScale = 0.99f, animationSpec = OmniMotion.screenTween())) togetherWith fadeOut(animationSpec = tween(OmniMotion.fastFadeMs))
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
                        .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
                        .fillMaxWidth()
                        .height(playerHeight - 12.dp)
                )
            }
        }
}
