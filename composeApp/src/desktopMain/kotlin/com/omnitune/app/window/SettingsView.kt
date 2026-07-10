package com.omnitune.app.window

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.omnitune.app.platform.SettingsRepository
import org.koin.compose.koinInject

@Composable
fun SettingsView() {
    val settings = koinInject<SettingsRepository>()
    var volume by remember { mutableStateOf(settings.volume) }
    var reduceMotion by remember { mutableStateOf(settings.reduceMotionEnabled) }
    var miniOnTop by remember { mutableStateOf(settings.miniPlayerAlwaysOnTop) }

    LazyColumnOrColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 28.dp),
    ) {
        item {
            Text("Settings", style = MaterialTheme.typography.displaySmall, color = TextPrimary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Personalize OmniTune and manage playback", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
            Spacer(Modifier.height(20.dp))
        }

        item {
            SettingsGroup("Account") {
                SettingsRow("Profile", "Local session · not signed in to YouTube Music")
                SettingsRow("Liked songs", "Saved locally on this device")
            }
        }
        item { Spacer(Modifier.height(14.dp)) }
        item {
            SettingsGroup("Audio Quality") {
                SettingsRow("Stream quality", "Follows the YouTube Music source (adaptive)")
                SettingsRow("Volume", null) {
                    Slider(value = volume / 200f, onValueChange = { v -> volume = (v * 200).toInt().coerceIn(0, 200); settings.volume = volume; settings.flush() }, modifier = Modifier.width(200.dp), colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Iris, inactiveTrackColor = Color.White.copy(alpha = 0.1f)))
                    Text("$volume%", color = TextSecondary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(40.dp), textAlign = TextAlign.End)
                }
            }
        }
        item { Spacer(Modifier.height(14.dp)) }
        item {
            SettingsGroup("Playback") {
                SettingsSwitch("Reduce motion", "Minimize animations across the interface", reduceMotion) { reduceMotion = it; settings.reduceMotionEnabled = it; settings.flush() }
                SettingsSwitch("Mini player always on top", "Keep the compact player above other windows", miniOnTop) { miniOnTop = it; settings.miniPlayerAlwaysOnTop = it; settings.flush() }
            }
        }
        item { Spacer(Modifier.height(14.dp)) }
        item {
            SettingsGroup("Appearance") {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(OmniGradients.irisToLavender))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Nocturne Prism", color = TextPrimary, style = MaterialTheme.typography.titleSmall)
                        Text("Deep obsidian & iris — the OmniTune identity", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                    Surface(shape = Shapes.pill, color = Iris.copy(alpha = 0.18f)) {
                        Text("Active", color = IrisSoft, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp))
                    }
                }
            }
        }
        item { Spacer(Modifier.height(14.dp)) }
        item {
            SettingsGroup("Downloads") {
                SettingsRow("Offline storage", "Local audio downloads are not yet enabled")
                SettingsRow("Auto-download", "Unavailable in this build")
            }
        }
        item { Spacer(Modifier.height(14.dp)) }
        item {
            SettingsGroup("Keyboard Shortcuts") {
                ShortcutRow("Space", "Play / Pause")
                ShortcutRow("← / →", "Seek −5s / +5s")
                ShortcutRow("N / P", "Next / Previous track")
                ShortcutRow("Ctrl + K", "Focus search")
            }
        }
        item { Spacer(Modifier.height(14.dp)) }
        item {
            SettingsGroup("About") {
                SettingsRow("OmniTune", "Version 0.11.6 (Nocturne Prism)")
                SettingsRow("Engine", "Compose Multiplatform · VLCj 4.11 · VLC 3.0.21")
                SettingsRow("Data", "YouTube Music (InnerTube) · LrcLib · KuGou")
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun LazyColumnOrColumn(modifier: Modifier, content: LazyListScope.() -> Unit) {
    androidx.compose.foundation.lazy.LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(0.dp), content = content)
}

@Composable
private fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    Surface(shape = Shapes.large, color = Surface1, border = BorderStroke(1.dp, BorderLow), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
            Text(title.uppercase(), style = MaterialTheme.typography.labelLarge, color = IrisSoft, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun SettingsRow(label: String, value: String?, trailing: @Composable (() -> Unit)? = null) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextPrimary, style = MaterialTheme.typography.titleSmall)
            if (value != null) Text(value, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
        trailing?.invoke()
    }
}

@Composable
private fun SettingsSwitch(label: String, description: String, value: Boolean, onChanged: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextPrimary, style = MaterialTheme.typography.titleSmall)
            Text(description, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = value, onCheckedChange = onChanged, colors = SwitchDefaults.colors(checkedTrackColor = Iris, uncheckedTrackColor = Surface3, checkedThumbColor = Color.White))
    }
}

@Composable
private fun ShortcutRow(keys: String, action: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = Shapes.small, color = Surface3) {
            Text(keys, color = TextPrimary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(action, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
    }
}
