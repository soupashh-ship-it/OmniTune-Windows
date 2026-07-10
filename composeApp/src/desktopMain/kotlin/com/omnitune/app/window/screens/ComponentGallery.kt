package com.omnitune.app.window.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.omnitune.app.window.OmniTuneTheme
import com.omnitune.app.window.BgDeep
import com.omnitune.app.window.components.*
import com.omnitune.innertube.models.Artist
import com.omnitune.innertube.models.SongItem

@Composable
fun ComponentGallery() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BgDeep).padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            OmniSectionHeader("Buttons & Controls", actionLabel = "See all", onAction = {})
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OmniPrimaryButton(text = "Play Now", icon = Icons.Default.PlayArrow, onClick = {})
                OmniSecondaryButton(text = "Shuffle", onClick = {})
                OmniIconButton(icon = Icons.Default.PlayArrow, contentDescription = "Play", onClick = {})
            }
        }
        item {
            OmniSectionHeader("Search Field")
        }
        item {
            OmniSearchField(
                value = "",
                onValueChange = {},
                modifier = Modifier.width(400.dp)
            )
        }
        item {
            OmniSectionHeader("Media Cards")
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OmniMediaCard(
                    title = "After Hours",
                    subtitle = "OmniTune",
                    artworkUrl = null,
                    modifier = Modifier.width(180.dp)
                )
                OmniMediaCard(
                    title = "Neon Skyline",
                    subtitle = "Synth Pop",
                    artworkUrl = null,
                    modifier = Modifier.width(140.dp)
                )
            }
        }
        item {
            OmniSectionHeader("Song Rows")
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val dummySong = SongItem(
                    id = "1", title = "YE YE", artists = listOf(Artist("Faydee", null)),
                    duration = 182, thumbnail = "", explicit = false
                )
                OmniSongRow(item = dummySong, isActive = true, isPlaying = true, onClick = {})
                OmniSongRow(item = dummySong.copy(title = "La Chica Yeye"), isActive = false, isPlaying = false, onClick = {})
            }
        }
        item {
            OmniSectionHeader("Progress & Volume")
        }
        item {
            Row(modifier = Modifier.width(400.dp), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                OmniProgressSlider(fraction = 0.4f, onSeek = {}, modifier = Modifier.weight(1f))
                OmniVolumeControl(volume = 120, onVolumeChange = {}, modifier = Modifier.width(120.dp))
            }
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Nocturne Prism Component Gallery"
    ) {
        OmniTuneTheme {
            ComponentGallery()
        }
    }
}
