package com.omnitune.app.window

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.border

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

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 28.dp),
    ) {
        Text("Settings & Personalization", style = MaterialTheme.typography.displaySmall, color = TextPrimary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("Customize OmniTune to match your sound and style.", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
        Spacer(Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            // Column 1
            Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                SettingsGroup("Account") {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Surface3))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Local User", color = TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text("Not signed in", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                        Surface(shape = Shapes.small, color = Surface3, border = BorderStroke(1.dp, BorderLow)) {
                            Text("Manage Account", color = TextPrimary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    SettingsRow("Subscription", null) { Text("OmniTune Premium  >", color = TextSecondary, style = MaterialTheme.typography.bodyMedium) }
                    SettingsRow("Manage Devices", null) { Text("1 of 5 active  >", color = TextSecondary, style = MaterialTheme.typography.bodyMedium) }
                }
                
                SettingsGroup("Appearance") {
                    Text("Theme", color = TextPrimary, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        ThemePill("Nocturne Prism", true)
                        ThemePill("Midnight", false)
                        ThemePill("Dusk", false)
                        ThemePill("Aurora", false)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Accent Color", color = TextPrimary, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
                    Text("Choose your vibe", color = TextSecondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ColorCircle(Iris, true)
                        ColorCircle(CoolBlue, false)
                        ColorCircle(SuccessGreen, false)
                        ColorCircle(ErrorRed, false)
                        ColorCircle(VioletSoft, false)
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("UI Preview", color = TextPrimary, style = MaterialTheme.typography.titleSmall)
                            Text("See how OmniTune looks with your style", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                        Box(modifier = Modifier.width(140.dp).height(60.dp).clip(Shapes.medium).background(Surface3))
                    }
                }
                
                SettingsGroup("Keyboard Shortcuts") {
                    SettingsRow("Show all shortcuts", null) { Text(">", color = TextSecondary) }
                    SettingsSwitch("Global Shortcuts", "Use shortcuts when OmniTune is in the background", false) {}
                }
            }
            
            // Column 2
            Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                SettingsGroup("Audio Quality") {
                    Text("Choose your streaming quality", color = TextSecondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        ThemePill("Low", false)
                        ThemePill("Normal", false)
                        ThemePill("High", false)
                        ThemePill("Lossless", true)
                    }
                    Spacer(Modifier.height(16.dp))
                    SettingsRow("Lossless Quality", null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Up to 24-bit / 192 kHz", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.width(8.dp))
                            Surface(shape = Shapes.small, color = SuccessGreen.copy(alpha = 0.2f)) {
                                Text("FLAC", color = SuccessGreen, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                    }
                    SettingsSwitch("Normalize Volume", "Maintain consistent volume across tracks", true) {}
                    SettingsSwitch("Spatial Audio", "Immersive sound experience", false) {}
                }
                
                SettingsGroup("Downloads") {
                    SettingsRow("Download Quality", null) { Text("High (320 kbps) ˅", color = TextSecondary, style = MaterialTheme.typography.bodyMedium) }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Download Location", color = TextPrimary, style = MaterialTheme.typography.titleSmall)
                            Text("C:\\Users\\Local\\Music\\OmniTune", color = TextSecondary, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Surface(shape = Shapes.small, color = Surface3, border = BorderStroke(1.dp, BorderLow)) {
                            Text("Change", color = TextPrimary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    SettingsSwitch("Download over cellular", "Recommended: Off", false) {}
                    SettingsSwitch("Auto download playlists", "Automatically download new songs", true) {}
                }
            }
            
            // Column 3
            Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                SettingsGroup("Playback") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Crossfade", color = TextPrimary, style = MaterialTheme.typography.titleSmall)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Smooth transitions between tracks", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            Switch(checked = true, onCheckedChange = {}, colors = SwitchDefaults.colors(checkedTrackColor = Iris, checkedThumbColor = Color.White))
                        }
                        Spacer(Modifier.height(8.dp))
                        Slider(value = 5f, onValueChange = {}, valueRange = 0f..12f, colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Iris, inactiveTrackColor = Surface3))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("0s", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            Text("5s", color = TextPrimary, style = MaterialTheme.typography.bodySmall)
                            Text("12s", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    SettingsSwitch("Gapless Playback", "Play tracks without any gaps", true) {}
                    SettingsSwitch("Autoplay", "Automatically play similar music", true) {}
                    SettingsSwitch("Mini player always on top", "Keep the compact player above other windows", miniOnTop) { miniOnTop = it; settings.miniPlayerAlwaysOnTop = it; settings.flush() }
                }
                
                SettingsGroup("Notifications") {
                    SettingsSwitch("New Music", "Updates about new releases", true) {}
                    SettingsSwitch("Recommendations", "Personalized music suggestions", true) {}
                    SettingsSwitch("Concert Alerts", "Notify about nearby concerts", false) {}
                    SettingsSwitch("Product Updates", "OmniTune news and updates", true) {}
                }
                
                SettingsGroup("About") {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("OmniTune for Windows", color = TextPrimary, style = MaterialTheme.typography.titleSmall)
                            Text("Version 1.0.0 (Build 10023)", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("What's New", color = IrisSoft, style = MaterialTheme.typography.labelSmall)
                                Text("|", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                                Text("Help Center", color = IrisSoft, style = MaterialTheme.typography.labelSmall)
                                Text("|", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                                Text("Terms of Service", color = IrisSoft, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Box(modifier = Modifier.size(60.dp).clip(Shapes.medium).background(Surface3))
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemePill(label: String, isActive: Boolean) {
    val bg = if (isActive) Iris.copy(alpha = 0.2f) else Color.Transparent
    val border = if (isActive) Iris else BorderLow
    Surface(shape = Shapes.pill, color = bg, border = BorderStroke(1.dp, border), modifier = Modifier.height(32.dp)) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp).fillMaxHeight()) {
            Text(label, color = if (isActive) TextPrimary else TextSecondary, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun ColorCircle(color: Color, isActive: Boolean) {
    Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(color).border(2.dp, if (isActive) Color.White else Color.Transparent, CircleShape))
}

@Composable
private fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    Surface(shape = Shapes.large, color = Surface1, border = BorderStroke(1.dp, BorderLow), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun SettingsRow(label: String, description: String?, trailing: @Composable (() -> Unit)? = null) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextPrimary, style = MaterialTheme.typography.titleSmall)
            if (description != null) {
                Text(description, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
        trailing?.invoke()
    }
}

@Composable
private fun SettingsSwitch(label: String, description: String, value: Boolean, onChanged: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextPrimary, style = MaterialTheme.typography.titleSmall)
            Text(description, color = TextSecondary, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Switch(
            checked = value,
            onCheckedChange = onChanged,
            colors = SwitchDefaults.colors(
                checkedTrackColor = Iris,
                uncheckedTrackColor = Surface3,
                uncheckedBorderColor = Color.Transparent,
                checkedThumbColor = Color.White
            )
        )
    }
}

