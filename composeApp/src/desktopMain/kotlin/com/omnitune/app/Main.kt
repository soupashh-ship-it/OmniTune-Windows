package com.omnitune.app

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import kotlin.math.roundToInt
import com.omnitune.app.di.initKoin
import com.omnitune.app.platform.SettingsRepository
import com.omnitune.app.platform.VlcjAudioEngine
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.app.window.AppWindowIcon
import com.omnitune.app.window.AppTrayIcon
import com.omnitune.app.window.OmniMiniPlayer
import com.omnitune.app.window.OmniWindow
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

fun main() {
    System.setProperty("jna.library.path", "C:\\Program Files\\VideoLAN\\VLC")
    System.setProperty("VLC_PLUGIN_PATH", "C:\\Program Files\\VideoLAN\\VLC\\plugins")

    application {
        var isWindowVisible by remember { mutableStateOf(true) }
        val windowState = rememberWindowState(
            position = WindowPosition(Alignment.Center),
            size = DpSize(1200.dp, 800.dp)
        )

        KoinApplication(application = {
            modules(initKoin())
        }) {
            val audioEngine: VlcjAudioEngine = koinInject()
            val settings: SettingsRepository = koinInject()

            val winDpSize = windowState.size
            LaunchedEffect(winDpSize) {
                settings.windowWidth = winDpSize.width.value.roundToInt()
                settings.windowHeight = winDpSize.height.value.roundToInt()
                settings.flush()
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
                    window.minimumSize = java.awt.Dimension(1024, 640)
                    OmniWindow(
                        windowState = windowState,
                        onClose = { isWindowVisible = false },
                        onMinimizeToTray = { isWindowVisible = false }
                    )
                }
            }

            var showMini by remember { mutableStateOf(false) }
            if (showMini) {
                val p: PlayerViewModel = koinInject()
                val cs by p.currentSong.collectAsState()
                val ps by p.playbackState.collectAsState()
                val pos by p.position.collectAsState()
                val vol by p.volume.collectAsState()
                Window(
                    onCloseRequest = { showMini = false },
                    state = rememberWindowState(size = DpSize(380.dp, 84.dp)),
                    title = "OmniTune Mini",
                    icon = AppWindowIcon,
                    alwaysOnTop = settings.miniPlayerAlwaysOnTop,
                    resizable = true,
                ) {
                    OmniMiniPlayer(player = p, currentSong = cs, playbackState = ps, position = pos, volume = vol)
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
                        audioEngine.release()
                        exitApplication()
                    })
                }
            )

            DisposableEffect(Unit) {
                onDispose {
                    audioEngine.release()
                }
            }
        }
    }
}
