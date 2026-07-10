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
import com.omnitune.app.window.AppWindowIcon
import com.omnitune.app.window.AppTrayIcon
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
                ) {
                    OmniWindow(
                        onMinimizeToTray = { isWindowVisible = false }
                    )
                }
            }

            Tray(
                icon = AppTrayIcon,
                tooltip = "OmniTune",
                onAction = { isWindowVisible = true },
                menu = {
                    Item("Show OmniTune", onClick = { isWindowVisible = true })
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
