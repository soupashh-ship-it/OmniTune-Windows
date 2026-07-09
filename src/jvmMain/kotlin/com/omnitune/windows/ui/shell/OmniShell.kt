package com.omnitune.windows.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.omnitune.windows.playback.OmniPlayer
import com.omnitune.windows.playback.PlaybackState
import com.omnitune.windows.ui.theme.OmniColors
import com.omnitune.windows.ui.screens.HomeScreen
import com.omnitune.windows.ui.screens.SearchScreen
import com.omnitune.windows.ui.screens.LibraryScreen
import com.omnitune.windows.ui.screens.SettingsScreen

enum class Screen { Home, Search, Library, Settings }

@Composable
fun OmniShell(player: OmniPlayer) {
    var currentScreen by remember { mutableStateOf(Screen.Home) }

    Row(modifier = Modifier.fillMaxSize().background(OmniColors.Background)) {
        // Sidebar
        Column(
            modifier = Modifier
                .width(220.dp)
                .fillMaxHeight()
                .background(OmniColors.SurfaceElevated)
                .padding(vertical = 24.dp)
        ) {
            Text(
                text = "OmniTune",
                color = OmniColors.TextPrimary,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            SidebarItem("Home", Icons.Default.Home, currentScreen == Screen.Home) { currentScreen = Screen.Home }
            SidebarItem("Search", Icons.Default.Search, currentScreen == Screen.Search) { currentScreen = Screen.Search }
            SidebarItem("Library", Icons.Default.LibraryMusic, currentScreen == Screen.Library) { currentScreen = Screen.Library }
            Spacer(modifier = Modifier.weight(1f))
            SidebarItem("Settings", Icons.Default.Settings, currentScreen == Screen.Settings) { currentScreen = Screen.Settings }
        }

        // Main Content Area & Bottom Player
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (currentScreen) {
                    Screen.Home -> HomeScreen()
                    Screen.Search -> SearchScreen(player)
                    Screen.Library -> LibraryScreen()
                    Screen.Settings -> SettingsScreen()
                }
            }
            
            // Bottom Playback Bar
            val currentTrack by player.currentTrack.collectAsState()
            val playbackState by player.playbackState.collectAsState()
            val positionMs by player.positionMs.collectAsState()
            val durationMs by player.durationMs.collectAsState()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(OmniColors.SurfacePanel)
            ) {
                if (currentTrack != null) {
                    Row(modifier = Modifier.align(Alignment.CenterStart).padding(horizontal = 16.dp)) {
                        Text(
                            text = "${currentTrack?.title} - ${currentTrack?.artists?.joinToString { it.name }}",
                            color = OmniColors.TextPrimary
                        )
                    }
                    Column(modifier = Modifier.align(Alignment.Center).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Row {
                            Button(onClick = { player.playPrevious() }, modifier = Modifier.padding(end = 8.dp)) {
                                Text("Prev")
                            }
                            Button(onClick = { 
                                if (playbackState == PlaybackState.PLAYING) player.pause() 
                                else player.resume() 
                            }) {
                                Text(if (playbackState == PlaybackState.PLAYING) "Pause" else "Play")
                            }
                            Button(onClick = { player.playNext() }, modifier = Modifier.padding(start = 8.dp)) {
                                Text("Next")
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = formatTime(positionMs), color = OmniColors.TextSecondary, modifier = Modifier.padding(end = 8.dp))
                            androidx.compose.material3.Slider(
                                value = if (durationMs > 0) positionMs.toFloat() / durationMs.toFloat() else 0f,
                                onValueChange = { player.seekTo((it * durationMs).toLong()) },
                                modifier = Modifier.width(300.dp),
                                colors = androidx.compose.material3.SliderDefaults.colors(
                                    thumbColor = OmniColors.OmniAccentPrimary,
                                    activeTrackColor = OmniColors.OmniAccentPrimary,
                                    inactiveTrackColor = OmniColors.SurfaceElevated
                                )
                            )
                            Text(text = formatTime(durationMs), color = OmniColors.TextSecondary, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                } else {
                    Text(
                        text = "Player Controls",
                        color = OmniColors.TextSecondary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun SidebarItem(title: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (selected) OmniColors.OmniAccentSoft else OmniColors.OmniBackgroundBase.copy(alpha = 0f)
    val contentColor = if (selected) OmniColors.OmniAccentPrimary else OmniColors.TextSecondary

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Icon(icon, contentDescription = title, tint = contentColor, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, color = contentColor)
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}