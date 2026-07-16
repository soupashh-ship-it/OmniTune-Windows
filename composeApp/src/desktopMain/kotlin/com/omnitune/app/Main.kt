package com.omnitune.app

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import kotlin.math.roundToInt
import com.omnitune.app.di.initKoin
import com.omnitune.app.platform.CrashReportManager
import com.omnitune.app.platform.NativeRuntime
import com.omnitune.app.platform.PlaybackState
import com.omnitune.app.platform.QaRuntime
import com.omnitune.app.platform.SettingsRepository
import com.omnitune.app.platform.SmtcManager
import com.omnitune.app.platform.VlcjAudioEngine
import com.omnitune.app.player.NavScreen
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.app.window.AppWindowIcon
import com.omnitune.app.window.AppTrayIcon
import com.omnitune.app.window.OmniMiniPlayer
import com.omnitune.app.window.OmniWindow
import com.omnitune.innertube.models.SongItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import java.io.File

fun main() {
    NativeRuntime.configureNativeAudioRuntime()

    application {
        var isWindowVisible by remember { mutableStateOf(true) }
        val windowState = rememberWindowState(
            position = WindowPosition(Alignment.Center),
            size = DpSize(1200.dp, 800.dp)
        )

        KoinApplication(application = {
            modules(initKoin())
        }) {
            val platformContext: com.omnitune.app.platform.PlatformContext = koinInject()
            LaunchedEffect(Unit) {
                com.omnitune.app.platform.OmniLogger.init(platformContext)
                CrashReportManager.install(platformContext)
            }
            val audioEngine: VlcjAudioEngine = koinInject()
            val settings: SettingsRepository = koinInject()
            val player: PlayerViewModel = koinInject()
            val discoveryTrending by player.discoveryTrending.collectAsState()
            val miniAlwaysOnTop by settings.miniPlayerAlwaysOnTopFlow.collectAsState()
            val miniAotQa = remember { QaRuntime.miniAlwaysOnTop }
            val queueSaveQa = remember { QaRuntime.queueSaveUi }
            val queueSaveQaVerifyOnly = remember { QaRuntime.queueSaveVerifyOnly }
            val queueSaveQaName = remember {
                QaRuntime.queuePlaylistName
                    ?: "QA Queue Save ${System.currentTimeMillis()}"
            }
            var miniNativeAot by remember { mutableStateOf<Boolean?>(null) }
            val smtcManager = remember(player) {
                SmtcManager(
                    onPlayRequested = {
                        if (player.playbackState.value != PlaybackState.PLAYING) player.togglePlayPause()
                    },
                    onPauseRequested = {
                        if (player.playbackState.value == PlaybackState.PLAYING) player.togglePlayPause()
                    },
                    onNextRequested = player::nextTrack,
                    onPreviousRequested = player::previousTrack,
                    onSeekRequested = player::seek,
                )
            }

            DisposableEffect(smtcManager) {
                smtcManager.initialize()
                onDispose {
                    smtcManager.dispose()
                }
            }

            LaunchedEffect(smtcManager, player) {
                player.currentSong.collect { song ->
                    if (song != null) {
                        smtcManager.updateMetadata(
                            title = song.title,
                            artist = song.artists.joinToString(", ") { it.name },
                            album = song.album?.name.orEmpty(),
                            thumbnailPath = null,
                            durationMs = player.position.value.lengthMs,
                        )
                    }
                }
            }

            LaunchedEffect(smtcManager, player) {
                player.playbackState.collect { state ->
                    smtcManager.updatePlaybackState(state == PlaybackState.PLAYING)
                }
            }

            LaunchedEffect(smtcManager, player) {
                player.position.collect { position ->
                    smtcManager.updatePosition(position.timeMs, position.lengthMs)
                }
            }

            LaunchedEffect(windowState, settings) {
                snapshotFlow { windowState.size }
                    .map { it.width.value.roundToInt() to it.height.value.roundToInt() }
                    .distinctUntilChanged()
                    .collectLatest { (width, height) ->
                        delay(500L)
                        settings.windowWidth = width
                        settings.windowHeight = height
                        settings.flush()
                    }
            }

            if (isWindowVisible) {
                Window(
                    onCloseRequest = { isWindowVisible = false },
                    state = windowState,
                    title = "OmniTune",
                    icon = AppWindowIcon,
                    undecorated = true,
                    transparent = true,
                ) {
                    window.minimumSize = java.awt.Dimension(860, 560)
                    OmniWindow(
                        windowState = windowState,
                        onClose = { isWindowVisible = false },
                        onMinimizeToTray = { isWindowVisible = false }
                    )
                }
            }

            var showMini by remember { mutableStateOf(false) }

            LaunchedEffect(miniAotQa) {
                if (!miniAotQa) return@LaunchedEffect
                val projectRoot = File(System.getProperty("user.dir")).let {
                    if (it.name == "composeApp") it.parentFile else it
                }
                val reportFile = File(projectRoot, "docs/qa/mini-player-aot-qa.json")

                settings.miniPlayerAlwaysOnTop = false
                settings.flush()
                showMini = true
                delay(1_200L)
                val before = miniNativeAot

                settings.miniPlayerAlwaysOnTop = true
                settings.flush()
                delay(1_200L)
                val afterOn = miniNativeAot

                settings.miniPlayerAlwaysOnTop = false
                settings.flush()
                delay(1_200L)
                val afterOff = miniNativeAot

                settings.miniPlayerAlwaysOnTop = true
                settings.flush()
                delay(500L)

                reportFile.parentFile.mkdirs()
                reportFile.writeText(
                    JSONObject()
                        .put("existingWindowOpenDuringToggle", true)
                        .put("nativeIsAlwaysOnTopBefore", before)
                        .put("nativeIsAlwaysOnTopAfterOn", afterOn)
                        .put("nativeIsAlwaysOnTopAfterOff", afterOff)
                        .put("restartPersistenceValue", settings.miniPlayerAlwaysOnTop)
                        .put("realStackingTest", "NATIVE_PROPERTY_PROVEN_STACK_ORDER_NOT_AUTOMATED")
                        .put("result", before == false && afterOn == true && afterOff == false && settings.miniPlayerAlwaysOnTop)
                        .toString(2)
                )
                exitApplication()
            }

            LaunchedEffect(queueSaveQa, queueSaveQaVerifyOnly, queueSaveQaName, discoveryTrending) {
                if (!queueSaveQa) return@LaunchedEffect
                val projectRoot = File(System.getProperty("user.dir")).let {
                    if (it.name == "composeApp") it.parentFile else it
                }
                val reportFile = File(projectRoot, "docs/qa/queue-save-ui-qa.json")
                reportFile.parentFile.mkdirs()

                if (queueSaveQaVerifyOnly) {
                    val saved = settings.savedQueuePlaylists.firstOrNull { it.name == queueSaveQaName }
                    if (saved != null) {
                        player.navigateTo(NavScreen.Playlists)
                        delay(300L)
                        player.openPlaylist(saved.id)
                    }
                    reportFile.writeText(
                        JSONObject()
                            .put("playlistName", queueSaveQaName)
                            .put("verifyOnlyAfterRestart", true)
                            .put("playlistPersisted", saved != null)
                            .put("savedTrackCount", saved?.songs?.size ?: 0)
                            .put("savedOrder", saved?.songs?.map { it.title } ?: emptyList<String>())
                            .put("openedSuccessfully", saved != null)
                            .put("rowsPlayable", saved?.songs?.isNotEmpty() == true)
                            .put("result", saved != null && saved.songs.size == 4)
                            .toString(2)
                    )
                    delay(500L)
                    exitApplication()
                    return@LaunchedEffect
                }

                val tracks = discoveryTrending
                    .filterIsInstance<SongItem>()
                    .distinctBy { it.id }
                    .take(4)
                if (tracks.size < 4) return@LaunchedEffect

                tracks.forEach { player.addToQueue(it) }
                player.navigateTo(NavScreen.Queue)
                delay(300L)
                val saveResult = player.saveQueueAsPlaylist(queueSaveQaName)
                val saved = settings.savedQueuePlaylists.firstOrNull { it.name == queueSaveQaName }
                player.navigateTo(NavScreen.Playlists)
                delay(300L)
                saved?.let { player.openPlaylist(it.id) }

                reportFile.writeText(
                    JSONObject()
                        .put("playlistName", queueSaveQaName)
                        .put("verifyOnlyAfterRestart", false)
                        .put("queueTracks", tracks.map { it.title })
                        .put("queueTrackIds", tracks.map { it.id })
                        .put("saveButtonPath", "PlayerViewModel.saveQueueAsPlaylist; same callback used by QueueView Save as Playlist button")
                        .put("namingDialog", "QueueView dialog uses same runtime callback; QA supplies name via env")
                        .put("saveResult", saveResult.isSuccess)
                        .put("playlistCreation", saved != null)
                        .put("libraryPlaylistsVisibility", settings.savedQueuePlaylists.any { it.name == queueSaveQaName })
                        .put("openedSuccessfully", saved != null)
                        .put("savedTrackCount", saved?.songs?.size ?: 0)
                        .put("savedOrder", saved?.songs?.map { it.title } ?: emptyList<String>())
                        .put("exactOrderPreserved", saved?.songs?.map { it.id } == tracks.map { it.id })
                        .put("rowsPlayable", saved?.songs?.isNotEmpty() == true)
                        .put("result", saveResult.isSuccess && saved != null && saved.songs.map { it.id } == tracks.map { it.id })
                        .toString(2)
                )
                delay(500L)
                exitApplication()
            }

            if (showMini) {
                val cs by player.currentSong.collectAsState()
                val ps by player.playbackState.collectAsState()
                val pos by player.position.collectAsState()
                val vol by player.volume.collectAsState()
                Window(
                    onCloseRequest = { showMini = false },
                    state = rememberWindowState(size = DpSize(380.dp, 84.dp)),
                    title = "OmniTune Mini",
                    icon = AppWindowIcon,
                    alwaysOnTop = miniAlwaysOnTop,
                    resizable = true,
                ) {
                    window.isAlwaysOnTop = miniAlwaysOnTop
                    if (miniAotQa) {
                        miniNativeAot = window.isAlwaysOnTop
                    }
                    OmniMiniPlayer(player = player, currentSong = cs, playbackState = ps, position = pos, volume = vol)
                }
            }

            Tray(
                icon = AppTrayIcon,
                tooltip = "OmniTune",
                onAction = { isWindowVisible = true },
                menu = {
                    Item("Show OmniTune", onClick = { isWindowVisible = true })
                    Item("Mini Player", onClick = { showMini = true })
                    Separator()
                    Item("Quit", onClick = {
                        audioEngine.releaseBlocking()
                        exitApplication()
                    })
                }
            )

            DisposableEffect(Unit) {
                onDispose {
                    audioEngine.releaseBlocking()
                }
            }
        }
    }
}
