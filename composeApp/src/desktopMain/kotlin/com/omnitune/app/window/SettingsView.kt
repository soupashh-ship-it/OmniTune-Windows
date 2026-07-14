package com.omnitune.app.window

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omnitune.app.platform.PlatformContext
import com.omnitune.app.platform.SettingsRepository
import org.koin.compose.koinInject

@Composable
fun SettingsView() {
    val settings = koinInject<SettingsRepository>()
    val platform = koinInject<PlatformContext>()
    var volume by remember { mutableStateOf(settings.volume) }
    var reduceMotion by remember { mutableStateOf(settings.reduceMotionEnabled) }
    var miniOnTop by remember { mutableStateOf(settings.miniPlayerAlwaysOnTop) }
    var theme by remember { mutableStateOf(settings.appearanceTheme) }
    var shuffle by remember { mutableStateOf(settings.shuffleEnabled) }
    var repeat by remember { mutableStateOf(settings.repeatMode) }
    val metrics = LocalHomeReferenceMetrics.current
    val scroll = rememberScrollState()

    Box(Modifier.fillMaxSize().verticalScroll(scroll)) {
        Box(Modifier.fillMaxWidth().height(metrics.px(560f))) {
            Text("Settings & Personalization", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.offset(x = metrics.px(24f), y = metrics.px(17f)))
            Text("Customize OmniTune using real local desktop preferences.", color = TextSecondary, fontSize = 10.sp, modifier = Modifier.offset(x = metrics.px(24f), y = metrics.px(44f)))

            SettingsCard("Account", Icons.Default.Person, Modifier.offset(x = metrics.px(24f), y = metrics.px(69f)).width(metrics.px(296f)).height(metrics.px(170f))) {
                SettingsLine("Profile", "Local Windows user")
                SettingsLine("Sign-in", "No account provider configured")
                SettingsLine("Recent searches", "${settings.recentSearches.size} stored")
            }

            SettingsCard("Audio", Icons.Default.VolumeUp, Modifier.offset(x = metrics.px(332f), y = metrics.px(69f)).width(metrics.px(296f)).height(metrics.px(170f))) {
                Text("Default volume: $volume%", color = TextSecondary, fontSize = 9.sp)
                Slider(
                    value = volume.toFloat(),
                    onValueChange = {
                        volume = it.toInt()
                        settings.volume = volume
                        settings.flush()
                    },
                    valueRange = 0f..200f,
                    colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = IrisSoft, inactiveTrackColor = Surface3),
                )
                SettingsLine("Quality", "Provider stream quality; no lossless claim")
            }

            SettingsCard("Playback", Icons.Default.GraphicEq, Modifier.offset(x = metrics.px(638f), y = metrics.px(69f)).width(metrics.px(296f)).height(metrics.px(170f))) {
                SettingsSwitch("Shuffle default", "Persist shuffle mode", shuffle) {
                    shuffle = it
                    settings.shuffleEnabled = it
                    settings.flush()
                }
                SettingsSegment("Repeat mode", listOf("Off", "All", "One"), repeat.coerceIn(0, 2)) {
                    repeat = it
                    settings.repeatMode = it
                    settings.flush()
                }
                SettingsSwitch("Mini player always on top", "Keep compact player above windows", miniOnTop) {
                    miniOnTop = it
                    settings.miniPlayerAlwaysOnTop = it
                    settings.flush()
                }
            }

            SettingsCard("Appearance", null, Modifier.offset(x = metrics.px(24f), y = metrics.px(247f)).width(metrics.px(296f)).height(metrics.px(176f))) {
                SettingsSegment("Theme", listOf("Nocturne", "Midnight", "Dusk", "Aurora"), when (theme) {
                    "midnight" -> 1
                    "dusk" -> 2
                    "aurora" -> 3
                    else -> 0
                }) {
                    theme = listOf("nocturne", "midnight", "dusk", "aurora")[it]
                    settings.appearanceTheme = theme
                    settings.flush()
                }
                SettingsSwitch("Reduced motion", "Use calmer transitions where supported", reduceMotion) {
                    reduceMotion = it
                    settings.reduceMotionEnabled = it
                    settings.flush()
                }
                Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(8f))) {
                    listOf(IrisSoft, CoolBlue, SuccessGreen, ErrorRed, VioletSoft).forEachIndexed { index, color ->
                        Box(Modifier.size(metrics.px(17f)).clip(CircleShape).background(color).border(if (index == 0) 2.dp else 1.dp, if (index == 0) Color.White else BorderLow, CircleShape))
                    }
                }
            }

            SettingsCard("Downloads", Icons.Default.Storage, Modifier.offset(x = metrics.px(332f), y = metrics.px(247f)).width(metrics.px(296f)).height(metrics.px(176f))) {
                SettingsLine("Offline engine", "Persistent local downloads enabled")
                SettingsLine("Quality", settings.downloadQualityMode.readableLabel())
                SettingsLine("Download folder", platform.downloadsDir.absolutePath)
                SettingsLine("Files present", "${platform.downloadsDir.listFiles()?.size ?: 0}")
            }

            SettingsCard("Notifications", null, Modifier.offset(x = metrics.px(638f), y = metrics.px(247f)).width(metrics.px(296f)).height(metrics.px(176f))) {
                SettingsLine("New music", "No notification provider configured")
                SettingsLine("Product updates", "Shown only inside app surfaces")
                SettingsLine("System tray", "Available from desktop shell")
            }

            SettingsCard("Keyboard Shortcuts", Icons.Default.Keyboard, Modifier.offset(x = metrics.px(25f), y = metrics.px(433.5f)).width(metrics.px(296f)).height(metrics.px(82f))) {
                SettingsLine("Ctrl + K", "Focus global search")
                SettingsLine("Space / ← / →", "Play-pause and seek")
            }

            SettingsCard("About", null, Modifier.offset(x = metrics.px(332f), y = metrics.px(433.5f)).width(metrics.px(602f)).height(metrics.px(82f))) {
                SettingsLine("OmniTune for Windows", "Desktop Compose build")
                SettingsLine("Data directory", platform.appDataDir.absolutePath)
            }
        }
    }
}

private fun com.omnitune.app.platform.DownloadQualityMode.readableLabel(): String = when (this) {
    com.omnitune.app.platform.DownloadQualityMode.PROVIDER_DEFAULT -> "Provider default"
    com.omnitune.app.platform.DownloadQualityMode.SMALLER_FILE -> "Smaller files"
    com.omnitune.app.platform.DownloadQualityMode.PREFER_HIGH -> "Prefer high bitrate"
}

@Composable
private fun SettingsCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector?, modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(metrics.px(8f)))
            .background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.84f))
            .border(1.dp, BorderLow.copy(alpha = 0.68f), RoundedCornerShape(metrics.px(8f)))
            .padding(metrics.px(14f)),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, null, tint = IrisSoft, modifier = Modifier.size(metrics.px(14f)))
                Spacer(Modifier.width(metrics.px(7f)))
            }
            Text(title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(metrics.px(10f)))
        content()
    }
}

@Composable
private fun SettingsLine(label: String, value: String) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(Modifier.fillMaxWidth().height(metrics.px(24f)), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = TextPrimary, fontSize = 9.sp, modifier = Modifier.weight(0.62f), maxLines = 1)
        Text(value, color = TextSecondary, fontSize = 8.5.sp, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun SettingsSwitch(label: String, description: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().height(LocalHomeReferenceMetrics.current.px(34f)), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label, color = TextPrimary, fontSize = 9.sp, maxLines = 1)
            Text(description, color = TextSecondary, fontSize = 7.7.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(checkedTrackColor = IrisSoft, checkedThumbColor = Color.White, uncheckedTrackColor = Surface3),
        )
    }
}

@Composable
private fun SettingsSegment(label: String, options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Text(label, color = TextPrimary, fontSize = 9.sp)
    Spacer(Modifier.height(metrics.px(5f)))
    Row(
        Modifier
            .fillMaxWidth()
            .height(metrics.px(26f))
            .clip(RoundedCornerShape(metrics.px(5f)))
            .background(Surface3.copy(alpha = 0.45f))
            .border(1.dp, BorderLow.copy(alpha = 0.55f), RoundedCornerShape(metrics.px(5f))),
    ) {
        options.forEachIndexed { index, option ->
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(metrics.px(5f)))
                    .background(if (index == selected) OmniReferenceColors.SurfaceSelectedStrong else Color.Transparent)
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center,
            ) {
                Text(option, color = if (index == selected) TextPrimary else TextSecondary, fontSize = 8.5.sp, maxLines = 1)
            }
        }
    }
    Spacer(Modifier.height(metrics.px(8f)))
}
