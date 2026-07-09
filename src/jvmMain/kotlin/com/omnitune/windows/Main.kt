package com.omnitune.windows

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberTrayState
import com.omnitune.innertube.YouTube
import com.omnitune.windows.playback.VlcjOmniPlayer
import com.omnitune.windows.ui.shell.OmniShell
import com.omnitune.windows.ui.theme.OmniTuneTheme

fun main() = application {
    val trayState = rememberTrayState()
    
    Tray(
        state = trayState,
        icon = rememberVectorPainter(Icons.Default.LibraryMusic),
        menu = {
            Item(
                "Exit OmniTune",
                onClick = ::exitApplication
            )
        }
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "OmniTune"
    ) {
        OmniTuneTheme {
            val player = remember { VlcjOmniPlayer() }
            
            
            OmniShell(player)
        }
    }
}
