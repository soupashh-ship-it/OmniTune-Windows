package com.omnitune.app.window

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun SettingsView() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        Text("OmniTune", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Text("Version 0.11.6", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(40.dp))

        SettingsCard(title = "About") {
            Text(
                "Open-source YouTube Music player for Windows. Built with Compose Multiplatform, VLCj, and Ktor.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start
            )
        }

        Spacer(Modifier.height(12.dp))

        SettingsCard(title = "Audio Engine") {
            Text("VLC 3.0.21 · VLCj 4.11.0", style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(12.dp))

        SettingsCard(title = "API") {
            Text("YouTube Music (InnerTube) · LastFM · LrcLib · KuGou", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        shape = RoundedCornerShape(12.dp),
        color = BgCard,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}
