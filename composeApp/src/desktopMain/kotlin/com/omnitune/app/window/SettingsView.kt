package com.omnitune.app.window

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storage
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.omnitune.app.player.NavScreen
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.app.platform.DownloadQualityMode
import com.omnitune.app.platform.PlatformContext
import com.omnitune.app.platform.SettingsRepository
import org.koin.compose.koinInject
import java.awt.Desktop
import java.io.File
import java.net.URI

@Composable
fun SettingsView() {
    val settings = koinInject<SettingsRepository>()
    val platform = koinInject<PlatformContext>()
    val player = koinInject<PlayerViewModel>()
    val metrics = LocalHomeReferenceMetrics.current
    val scroll = rememberScrollState()

    var volume by remember { mutableStateOf(settings.volume) }
    var reduceMotion by remember { mutableStateOf(settings.reduceMotionEnabled) }
    var miniOnTop by remember { mutableStateOf(settings.miniPlayerAlwaysOnTop) }
    var theme by remember { mutableStateOf(settings.appearanceTheme) }
    var shuffle by remember { mutableStateOf(settings.shuffleEnabled) }
    var repeat by remember { mutableStateOf(settings.repeatMode) }
    var downloadQuality by remember { mutableStateOf(settings.downloadQualityMode) }
    var normalizeVolume by remember { mutableStateOf(settings.normalizeVolumePreference) }
    var spatialAudio by remember { mutableStateOf(settings.spatialAudioPreference) }
    var gapless by remember { mutableStateOf(settings.gaplessPlaybackPreference) }
    var newMusicNotifications by remember { mutableStateOf(settings.newMusicNotifications) }
    var recommendationNotifications by remember { mutableStateOf(settings.recommendationNotifications) }
    var productUpdateNotifications by remember { mutableStateOf(settings.productUpdateNotifications) }
    var weeklyDigestNotifications by remember { mutableStateOf(settings.weeklyDigestNotifications) }
    var concertAlertNotifications by remember { mutableStateOf(settings.concertAlertNotifications) }
    var autoDownloadPlaylists by remember { mutableStateOf(settings.autoDownloadPlaylists) }
    var globalShortcuts by remember { mutableStateOf(settings.globalShortcutsEnabled) }
    var statusMessage by remember { mutableStateOf("") }
    val qualityModes = listOf(
        DownloadQualityMode.PROVIDER_DEFAULT,
        DownloadQualityMode.SMALLER_FILE,
        DownloadQualityMode.PREFER_HIGH,
    )

    fun restoreDefaults() {
        volume = 100
        reduceMotion = false
        miniOnTop = false
        shuffle = false
        repeat = 0
        theme = "nocturne"
        downloadQuality = DownloadQualityMode.PROVIDER_DEFAULT
        normalizeVolume = false
        spatialAudio = false
        gapless = true
        newMusicNotifications = true
        recommendationNotifications = true
        productUpdateNotifications = true
        weeklyDigestNotifications = true
        concertAlertNotifications = false
        autoDownloadPlaylists = false
        globalShortcuts = true
        settings.volume = volume
        settings.reduceMotionEnabled = reduceMotion
        settings.miniPlayerAlwaysOnTop = miniOnTop
        settings.shuffleEnabled = shuffle
        settings.repeatMode = repeat
        settings.appearanceTheme = theme
        settings.downloadQualityMode = downloadQuality
        settings.normalizeVolumePreference = normalizeVolume
        settings.spatialAudioPreference = spatialAudio
        settings.gaplessPlaybackPreference = gapless
        settings.newMusicNotifications = newMusicNotifications
        settings.recommendationNotifications = recommendationNotifications
        settings.productUpdateNotifications = productUpdateNotifications
        settings.weeklyDigestNotifications = weeklyDigestNotifications
        settings.concertAlertNotifications = concertAlertNotifications
        settings.autoDownloadPlaylists = autoDownloadPlaylists
        settings.globalShortcutsEnabled = globalShortcuts
        settings.flush()
        statusMessage = "Defaults restored"
    }

    val accountCard: @Composable (Modifier) -> Unit = { modifier ->
        SettingsCard("Account", Icons.Default.Person, modifier) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                AvatarBubble()
                Spacer(Modifier.width(metrics.px(11f)))
                Column(Modifier.weight(1f)) {
                    Text("Local Windows User", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    Text("OmniTune local profile", color = TextSecondary, fontSize = 8.5.sp, maxLines = 1)
                    Text(
                        "Local",
                        color = IrisSoft,
                        fontSize = 8.sp,
                        modifier = Modifier
                            .padding(top = metrics.px(4f))
                            .clip(RoundedCornerShape(metrics.px(4f)))
                            .background(IrisSoft.copy(alpha = 0.16f))
                            .padding(horizontal = metrics.px(6f), vertical = metrics.px(2f)),
                    )
                }
                ReferenceButton("Manage", Modifier.width(metrics.px(78f)).height(metrics.px(25f))) {
                    statusMessage = if (openFile(platform.appDataDir)) "Opened app data folder" else "Could not open app data folder"
                }
            }
            Spacer(Modifier.height(metrics.px(11f)))
            SettingsChevronLine("Library profile", "Open local library") { player.navigateTo(NavScreen.Library) }
            SettingsChevronLine("Recent searches", "${settings.recentSearches.size} stored") {
                settings.clearRecentSearches()
                statusMessage = "Recent searches cleared"
            }
            SettingsChevronLine("Connected apps", "Open project releases") {
                statusMessage = if (openUri("https://github.com/soupashh-ship-it/OmniTune-Windows/releases")) "Opened releases page" else "Could not open browser"
            }
        }
    }

    val audioCard: @Composable (Modifier) -> Unit = { modifier ->
        SettingsCard("Audio Quality", Icons.AutoMirrored.Filled.VolumeUp, modifier) {
            Text("Choose your streaming and playback behavior", color = TextSecondary, fontSize = 8.5.sp)
            Spacer(Modifier.height(metrics.px(7f)))
            SettingsSegment(
                label = "",
                options = listOf("Default", "Smaller", "High"),
                selected = qualityModes.indexOf(downloadQuality).coerceAtLeast(0),
            ) {
                downloadQuality = qualityModes[it.coerceIn(qualityModes.indices)]
                settings.downloadQualityMode = downloadQuality
                settings.flush()
                statusMessage = "Download quality set to ${downloadQuality.readableLabel()}"
            }
            SettingsLine("Current mode", downloadQuality.readableLabel())
            SettingsSwitch("Normalize Volume", "Stored preference for future leveling support", normalizeVolume) {
                normalizeVolume = it
                settings.normalizeVolumePreference = it
                settings.flush()
                statusMessage = "Normalize volume preference ${if (it) "enabled" else "disabled"}"
            }
            SettingsSwitch("Spatial Audio", "Persist spatial-output preference for supported playback paths", spatialAudio) {
                spatialAudio = it
                settings.spatialAudioPreference = it
                settings.flush()
                statusMessage = "Spatial audio preference ${if (it) "enabled" else "disabled"}"
            }
        }
    }

    val playbackCard: @Composable (Modifier) -> Unit = { modifier ->
        SettingsCard("Playback", Icons.Default.GraphicEq, modifier) {
            SettingsSwitch("Shuffle default", "Persist shuffle mode", shuffle) {
                shuffle = it
                settings.shuffleEnabled = it
                settings.flush()
            }
            Text("Default volume", color = TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("0", color = TextSecondary, fontSize = 8.sp)
                Slider(
                    value = volume.toFloat(),
                    onValueChange = {
                        volume = it.toInt()
                        settings.volume = volume
                        settings.flush()
                    },
                    valueRange = 0f..200f,
                    modifier = Modifier.weight(1f).padding(horizontal = metrics.px(6f)).height(metrics.px(24f)),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = IrisSoft,
                        inactiveTrackColor = Surface3,
                    ),
                )
                Text("200", color = TextSecondary, fontSize = 8.sp)
            }
            SettingsSwitch("Gapless Playback", "Persist gapless playback preference", gapless) {
                gapless = it
                settings.gaplessPlaybackPreference = it
                settings.flush()
                statusMessage = "Gapless playback preference ${if (it) "enabled" else "disabled"}"
            }
            SettingsSegment("Repeat", listOf("Off", "All", "One"), repeat.coerceIn(0, 2)) {
                repeat = it
                settings.repeatMode = it
                settings.flush()
                statusMessage = "Repeat mode updated"
            }
        }
    }

    val appearanceCard: @Composable (Modifier) -> Unit = { modifier ->
        SettingsCard("Appearance", null, modifier) {
            Text("Theme", color = TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(metrics.px(7f)))
            Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(7f)), modifier = Modifier.fillMaxWidth()) {
                ThemeTile("Nocturne", Brush.linearGradient(listOf(Color(0xFF180C3F), Color(0xFF10152C), Color(0xFF6B3DFF))), theme == "nocturne") {
                    theme = "nocturne"; settings.appearanceTheme = theme; settings.flush()
                }
                ThemeTile("Midnight", Brush.linearGradient(listOf(Color(0xFF09121F), Color(0xFF183C67))), theme == "midnight") {
                    theme = "midnight"; settings.appearanceTheme = theme; settings.flush()
                }
                ThemeTile("Dusk", Brush.linearGradient(listOf(Color(0xFF251336), Color(0xFF7047A8))), theme == "dusk") {
                    theme = "dusk"; settings.appearanceTheme = theme; settings.flush()
                }
                ThemeTile("Aurora", Brush.linearGradient(listOf(Color(0xFF101A3E), Color(0xFF6B2DFF), Color(0xFF07B7CF))), theme == "aurora") {
                    theme = "aurora"; settings.appearanceTheme = theme; settings.flush()
                }
            }
            Spacer(Modifier.height(metrics.px(10f)))
            Text("Accent Color", color = TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(metrics.px(7f)))
            Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(10f))) {
                listOf(IrisSoft, Color(0xFF4B5CFF), CoolBlue, SuccessGreen, Color(0xFFEAB308), Color(0xFFF97316), ErrorRed, Color(0xFFDB2777)).forEachIndexed { index, color ->
                    Box(
                        Modifier
                            .size(metrics.px(15f))
                            .clip(CircleShape)
                            .background(color)
                            .border(if (index == 0) 2.dp else 1.dp, if (index == 0) Color.White else BorderLow, CircleShape),
                    )
                }
            }
            Spacer(Modifier.height(metrics.px(11f)))
            SettingsSwitch("Reduced motion", "Use calmer transitions where supported", reduceMotion) {
                reduceMotion = it
                settings.reduceMotionEnabled = it
                settings.flush()
            }
        }
    }

    val downloadsCard: @Composable (Modifier) -> Unit = { modifier ->
        SettingsCard("Downloads", Icons.Default.Storage, modifier) {
            SettingsChevronLine("Download Quality", downloadQuality.readableLabel()) {
                statusMessage = "Use the Audio Quality selector to change download quality"
            }
            SettingsChevronLine("Download Location", platform.downloadsDir.absolutePath) {
                statusMessage = if (openFile(platform.downloadsDir)) "Opened downloads folder" else "Could not open downloads folder"
            }
            SettingsSwitch("Mini player always on top", "Keep compact player above windows", miniOnTop) {
                miniOnTop = it
                settings.miniPlayerAlwaysOnTop = it
                settings.flush()
                statusMessage = "Mini player always-on-top ${if (it) "enabled" else "disabled"}"
            }
            SettingsSwitch("Auto download playlists", "Persist automatic playlist-download preference", autoDownloadPlaylists) {
                autoDownloadPlaylists = it
                settings.autoDownloadPlaylists = it
                settings.flush()
                statusMessage = "Auto-download playlists preference ${if (it) "enabled" else "disabled"}"
            }
            SettingsChevronLine("Files present", "${platform.downloadsDir.listFiles()?.size ?: 0}") {
                statusMessage = if (openFile(platform.downloadsDir)) "Opened downloads folder" else "Could not open downloads folder"
            }
        }
    }

    val notificationsCard: @Composable (Modifier) -> Unit = { modifier ->
        SettingsCard("Notifications", null, modifier) {
            SettingsSwitch("New Music", "Persist in-app notification preference", newMusicNotifications) {
                newMusicNotifications = it
                settings.newMusicNotifications = it
                settings.flush()
            }
            SettingsSwitch("Recommendations", "Persist recommendation prompt preference", recommendationNotifications) {
                recommendationNotifications = it
                settings.recommendationNotifications = it
                settings.flush()
            }
            SettingsSwitch("Concert Alerts", "Persist concert-alert notification preference", concertAlertNotifications) {
                concertAlertNotifications = it
                settings.concertAlertNotifications = it
                settings.flush()
                statusMessage = "Concert alerts preference ${if (it) "enabled" else "disabled"}"
            }
            SettingsSwitch("Product Updates", "Persist release notice preference", productUpdateNotifications) {
                productUpdateNotifications = it
                settings.productUpdateNotifications = it
                settings.flush()
            }
            SettingsSwitch("Weekly Digest", "Persist listening-summary preference", weeklyDigestNotifications) {
                weeklyDigestNotifications = it
                settings.weeklyDigestNotifications = it
                settings.flush()
            }
        }
    }

    val shortcutsCard: @Composable (Modifier) -> Unit = { modifier ->
        SettingsCard("Keyboard Shortcuts", Icons.Default.Keyboard, modifier) {
            SettingsChevronLine("Show all shortcuts", "Ctrl+K, Space, N/P, ←/→") {
                statusMessage = "Shortcuts: Ctrl+K search, Space play/pause, N/P next/previous, arrows seek"
            }
            SettingsSwitch("Global Shortcuts", "Use shortcuts when OmniTune is focused", globalShortcuts) {
                globalShortcuts = it
                settings.globalShortcutsEnabled = it
                settings.flush()
                statusMessage = "Global shortcuts ${if (it) "enabled" else "disabled"}"
            }
        }
    }

    val aboutCard: @Composable (Modifier) -> Unit = { modifier ->
        SettingsCard("About", null, modifier) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = omniTuneIconPainter(),
                    contentDescription = "OmniTune icon",
                    modifier = Modifier
                        .size(metrics.px(39f))
                        .clip(CircleShape)
                        .border(1.dp, IrisSoft.copy(alpha = 0.55f), CircleShape),
                    contentScale = ContentScale.Crop,
                )
                Spacer(Modifier.width(metrics.px(14f)))
                Column(Modifier.weight(1f)) {
                    Text("OmniTune for Windows", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("Desktop Compose build", color = TextSecondary, fontSize = 8.5.sp)
                    Text(platform.appDataDir.absolutePath, color = IrisSoft, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                ReferenceButton("Check for Updates", Modifier.width(metrics.px(118f)).height(metrics.px(28f))) {
                    statusMessage = if (openUri("https://github.com/soupashh-ship-it/OmniTune-Windows/releases")) {
                        "Opened GitHub releases"
                    } else {
                        "Could not open browser"
                    }
                }
            }
        }
    }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(bottom = metrics.px(26f))
    ) {
        val gap = metrics.px(12f)
        val horizontalPadding = metrics.px(24f)
        val safeContentWidth = maxWidth - (horizontalPadding * 2)
        val columns = when {
            safeContentWidth >= 1040.dp -> 3
            safeContentWidth >= 660.dp -> 2
            else -> 1
        }
        val boundedContentWidth = when (columns) {
            3 -> 1120.dp
            2 -> 820.dp
            else -> 520.dp
        }

        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier = Modifier
                    .widthIn(max = boundedContentWidth)
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = metrics.px(17f)),
            ) {
                if (columns == 1) {
                    Column(Modifier.fillMaxWidth()) {
                        Text("Settings & Personalization", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("Customize OmniTune to match your sound, workflow, and style.", color = TextSecondary, fontSize = 10.sp)
                        if (statusMessage.isNotBlank()) {
                            Spacer(Modifier.height(metrics.px(4f)))
                            Text(statusMessage, color = IrisSoft, fontSize = 8.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Spacer(Modifier.height(metrics.px(10f)))
                        ReferenceButton("Restore Defaults", Modifier.width(metrics.px(128f)).height(metrics.px(28f)), onClick = ::restoreDefaults)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(gap),
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Settings & Personalization", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text("Customize OmniTune to match your sound, workflow, and style.", color = TextSecondary, fontSize = 10.sp)
                            if (statusMessage.isNotBlank()) {
                                Spacer(Modifier.height(metrics.px(4f)))
                                Text(statusMessage, color = IrisSoft, fontSize = 8.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        ReferenceButton("Restore Defaults", Modifier.width(metrics.px(128f)).height(metrics.px(28f)), onClick = ::restoreDefaults)
                    }
                }
                Spacer(Modifier.height(gap))
                ResponsiveSettingsGrid(
                    columns = columns,
                    gap = gap,
                    cards = listOf(
                        { accountCard(Modifier.fillMaxWidth().heightIn(min = 172.dp)) },
                        { audioCard(Modifier.fillMaxWidth().heightIn(min = 178.dp)) },
                        { playbackCard(Modifier.fillMaxWidth().heightIn(min = 214.dp)) },
                        { appearanceCard(Modifier.fillMaxWidth().heightIn(min = 214.dp)) },
                        { downloadsCard(Modifier.fillMaxWidth().heightIn(min = 206.dp)) },
                        { notificationsCard(Modifier.fillMaxWidth().heightIn(min = 196.dp)) },
                        { shortcutsCard(Modifier.fillMaxWidth().heightIn(min = 98.dp)) },
                        { aboutCard(Modifier.fillMaxWidth().heightIn(min = 102.dp)) },
                    ),
                )
            }
        }
    }
}

private fun DownloadQualityMode.readableLabel(): String = when (this) {
    DownloadQualityMode.PROVIDER_DEFAULT -> "Provider default"
    DownloadQualityMode.SMALLER_FILE -> "Smaller files"
    DownloadQualityMode.PREFER_HIGH -> "Prefer high bitrate"
}

@Composable
private fun ResponsiveSettingsGrid(
    columns: Int,
    gap: Dp,
    cards: List<@Composable () -> Unit>,
) {
    cards.chunked(columns.coerceAtLeast(1)).forEach { rowCards ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap),
        ) {
            rowCards.forEach { card ->
                Box(Modifier.weight(1f)) {
                    card()
                }
            }
            repeat(columns - rowCards.size) {
                Spacer(Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(gap))
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: ImageVector?,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val metrics = LocalHomeReferenceMetrics.current
    Column(
        modifier = modifier
            .heightIn(min = metrics.px(92f))
            .clip(RoundedCornerShape(metrics.px(8f)))
            .background(
                Brush.linearGradient(
                    listOf(
                        OmniReferenceColors.SurfaceBase.copy(alpha = 0.96f),
                        OmniReferenceColors.SurfaceAlternate.copy(alpha = 0.92f),
                        OmniReferenceColors.SurfaceDeepRaised.copy(alpha = 0.82f),
                    ),
                ),
            )
            .border(1.dp, BorderLow.copy(alpha = 0.56f), RoundedCornerShape(metrics.px(8f)))
            .padding(metrics.px(13f)),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(metrics.px(13f)))
                Spacer(Modifier.width(metrics.px(7f)))
            }
            Text(title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(metrics.px(9f)))
        content()
    }
}

@Composable
private fun AvatarBubble() {
    val metrics = LocalHomeReferenceMetrics.current
    Box(
        Modifier
            .size(metrics.px(42f))
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Color(0xFFE49A62), Color(0xFF46226D), Color(0xFF111B35))))
            .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text("OU", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RowScope.ThemeTile(label: String, brush: Brush, selected: Boolean, onClick: () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Box(
        Modifier
            .weight(1f)
            .height(metrics.px(42f))
            .clip(RoundedCornerShape(metrics.px(5f)))
            .background(brush)
            .border(
                if (selected) 1.5.dp else 1.dp,
                if (selected) IrisSoft else BorderLow.copy(alpha = 0.6f),
                RoundedCornerShape(metrics.px(5f)),
            )
            .clickable(onClick = onClick)
            .padding(metrics.px(6f)),
    ) {
        Text(label, color = Color.White, fontSize = 7.5.sp, modifier = Modifier.align(Alignment.BottomStart), maxLines = 1)
        if (selected) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .size(metrics.px(11f))
                    .clip(CircleShape)
                    .background(IrisSoft),
                contentAlignment = Alignment.Center,
            ) {
                Text("✓", color = Color.White, fontSize = 7.sp)
            }
        }
    }
}

@Composable
private fun SettingsLine(label: String, value: String) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = metrics.px(25f))
            .padding(vertical = metrics.px(2f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = TextPrimary, fontSize = 8.8.sp, modifier = Modifier.weight(0.62f), maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(value, color = TextSecondary, fontSize = 8.2.sp, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun SettingsChevronLine(label: String, value: String, onClick: (() -> Unit)? = null) {
    val metrics = LocalHomeReferenceMetrics.current
    val rowModifier = if (onClick != null) {
        Modifier.fillMaxWidth().heightIn(min = metrics.px(29f)).clickable(onClick = onClick)
    } else {
        Modifier.fillMaxWidth().heightIn(min = metrics.px(29f))
    }
    Row(
        rowModifier.border(0.dp, Color.Transparent),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, color = TextPrimary, fontSize = 8.8.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(value, color = TextSecondary, fontSize = 7.8.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Text("›", color = TextSecondary, fontSize = 14.sp)
    }
}

@Composable
private fun SettingsSwitch(
    label: String,
    description: String,
    checked: Boolean,
    enabled: Boolean = true,
    onChange: (Boolean) -> Unit,
) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = metrics.px(34f))
            .padding(vertical = metrics.px(3f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, color = if (enabled) TextPrimary else TextSecondary, fontSize = 8.8.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(description, color = TextSecondary, fontSize = 7.6.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onChange,
            modifier = Modifier.size(width = metrics.px(34f), height = metrics.px(22f)),
            colors = SwitchDefaults.colors(
                checkedTrackColor = OmniReferenceColors.Accent,
                checkedThumbColor = Color.White,
                uncheckedTrackColor = Surface3,
                uncheckedThumbColor = Color(0xFFD1D5DB),
                disabledUncheckedTrackColor = Surface3.copy(alpha = 0.65f),
                disabledUncheckedThumbColor = TextSecondary.copy(alpha = 0.8f),
            ),
        )
    }
}

@Composable
private fun SettingsSegment(label: String, options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    if (label.isNotBlank()) {
        Text(label, color = TextPrimary, fontSize = 8.8.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(metrics.px(5f)))
    }
    Row(
        Modifier
            .fillMaxWidth()
            .height(metrics.px(26f))
            .clip(RoundedCornerShape(metrics.px(5f)))
            .background(OmniReferenceColors.SurfaceDeepRaised.copy(alpha = 0.78f))
            .border(1.dp, BorderLow.copy(alpha = 0.55f), RoundedCornerShape(metrics.px(5f))),
    ) {
        options.forEachIndexed { index, option ->
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(metrics.px(5f)))
                    .background(if (index == selected) OmniReferenceColors.SurfaceSelectedStrong.copy(alpha = 0.92f) else Color.Transparent)
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center,
            ) {
                Text(option, color = if (index == selected) TextPrimary else TextSecondary, fontSize = 8.2.sp, maxLines = 1)
            }
        }
    }
    Spacer(Modifier.height(metrics.px(8f)))
}

@Composable
private fun ReferenceButton(label: String, modifier: Modifier, onClick: () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Box(
        modifier
            .clip(RoundedCornerShape(metrics.px(5f)))
            .background(OmniReferenceColors.SurfaceDeepRaised.copy(alpha = 0.72f))
            .border(1.dp, OmniReferenceColors.Accent.copy(alpha = 0.36f), RoundedCornerShape(metrics.px(5f)))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = TextPrimary, fontSize = 8.5.sp, maxLines = 1)
    }
}

private fun openFile(file: File): Boolean {
    return runCatching {
        if (!file.exists()) file.mkdirs()
        if (!Desktop.isDesktopSupported()) return false
        Desktop.getDesktop().open(file)
        true
    }.getOrDefault(false)
}

private fun openUri(uri: String): Boolean {
    return runCatching {
        if (!Desktop.isDesktopSupported()) return false
        Desktop.getDesktop().browse(URI(uri))
        true
    }.getOrDefault(false)
}
