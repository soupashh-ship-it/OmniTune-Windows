package com.omnitune.app.window.screens

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omnitune.app.platform.DownloadQualityMode
import com.omnitune.app.platform.DownloadState
import com.omnitune.app.platform.DownloadTask
import com.omnitune.app.platform.PlatformContext
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.app.window.BorderLow
import com.omnitune.app.window.CoolBlue
import com.omnitune.app.window.ErrorRed
import com.omnitune.app.window.IrisSoft
import com.omnitune.app.window.LocalHomeReferenceMetrics
import com.omnitune.app.window.OmniGradients
import com.omnitune.app.window.OmniReferenceColors
import com.omnitune.app.window.SuccessGreen
import com.omnitune.app.window.Surface3
import com.omnitune.app.window.TextPrimary
import com.omnitune.app.window.TextSecondary
import org.koin.compose.koinInject
import java.io.File

@Composable
fun DownloadsView(player: PlayerViewModel) {
    val platform = koinInject<PlatformContext>()
    val downloadsDir = platform.downloadsDir
    val diskFiles = remember(downloadsDir) {
        downloadsDir.walkTopDown().filter { it.isFile && it.extension != "part" }.toList()
    }
    val root = downloadsDir.toPath().root?.toFile()
    val total = root?.totalSpace ?: downloadsDir.totalSpace
    val free = root?.freeSpace ?: downloadsDir.freeSpace
    val tasks by player.downloadTasks.collectAsState()
    val quality by player.downloadQuality.collectAsState()

    val completed = tasks.filter { it.state == DownloadState.COMPLETED }
    val active = tasks.filter { it.state == DownloadState.QUEUED || it.state == DownloadState.RESOLVING || it.state == DownloadState.DOWNLOADING }
    val failed = tasks.filter { it.state == DownloadState.FAILED }
    val paused = tasks.filter { it.state == DownloadState.PAUSED }

    DownloadsReferenceContent(
        downloadsDir = downloadsDir,
        diskFileCount = diskFiles.size,
        tasks = tasks,
        downloadedBytes = completed.sumOf { it.bytesDownloaded },
        totalBytes = total,
        freeBytes = free,
        quality = quality,
        onQuality = player::setDownloadQuality,
        downloadsPaused = active.isEmpty() && paused.isNotEmpty(),
        onPauseToggle = {
            if (active.isNotEmpty()) player.pauseAllDownloads() else player.resumeAllDownloads()
        },
        completedCount = completed.size,
        activeCount = active.size,
        failedCount = failed.size,
        onPlay = player::playDownload,
        onPause = player::pauseDownload,
        onResume = player::resumeDownload,
        onRetry = player::retryDownload,
        onDelete = player::deleteDownload,
    )
}

@Composable
private fun DownloadsReferenceContent(
    downloadsDir: File,
    diskFileCount: Int,
    tasks: List<DownloadTask>,
    downloadedBytes: Long,
    totalBytes: Long,
    freeBytes: Long,
    quality: DownloadQualityMode,
    onQuality: (DownloadQualityMode) -> Unit,
    downloadsPaused: Boolean,
    onPauseToggle: () -> Unit,
    completedCount: Int,
    activeCount: Int,
    failedCount: Int,
    onPlay: (String) -> Unit,
    onPause: (String) -> Unit,
    onResume: (String) -> Unit,
    onRetry: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    val metrics = LocalHomeReferenceMetrics.current
    val scroll = rememberScrollState()
    val usedBytes = (totalBytes - freeBytes).coerceAtLeast(0)

    Box(Modifier.fillMaxSize().verticalScroll(scroll)) {
        Box(Modifier.fillMaxWidth().height(metrics.px(560f))) {
            Text("Downloads & Offline", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.offset(x = metrics.px(24f), y = metrics.px(22f)))
            Text("Manage persistent download tasks and verified local playback files.", color = TextSecondary, fontSize = 10.sp, modifier = Modifier.offset(x = metrics.px(24f), y = metrics.px(50f)))

            ActionButton("Quality Settings", Icons.Default.Settings, Modifier.offset(x = metrics.px(527f), y = metrics.px(36f)).width(metrics.px(104f)).height(metrics.px(28f))) {
                val options = listOf(DownloadQualityMode.PROVIDER_DEFAULT, DownloadQualityMode.SMALLER_FILE, DownloadQualityMode.PREFER_HIGH)
                onQuality(options[(options.indexOf(quality).coerceAtLeast(0) + 1) % options.size])
            }
            ActionButton(if (downloadsPaused) "Resume All" else "Pause All", Icons.Default.Pause, Modifier.offset(x = metrics.px(642f), y = metrics.px(36f)).width(metrics.px(84f)).height(metrics.px(28f)), onPauseToggle)

            Row(
                modifier = Modifier.offset(x = metrics.px(24f), y = metrics.px(75f)).width(metrics.px(350f)).height(metrics.px(26f)),
                horizontalArrangement = Arrangement.spacedBy(metrics.px(8f)),
            ) {
                listOf("All ${tasks.size}", "Completed $completedCount", "In Progress $activeCount", "Failed $failedCount").forEachIndexed { index, label ->
                    FilterChip(label, index == 0)
                }
            }

            Row(
                modifier = Modifier.offset(x = metrics.px(24f), y = metrics.px(111f)).width(metrics.px(700f)).height(metrics.px(64f)),
                horizontalArrangement = Arrangement.spacedBy(metrics.px(10f)),
            ) {
                StatCard("Downloaded", "$completedCount", "Verified files", Icons.Default.Download, IrisSoft, Modifier.weight(1f))
                StatCard("Offline Mixes", "0", "Removed", Icons.Default.GraphicEq, CoolBlue, Modifier.weight(1f))
                StatCard("Downloading", "$activeCount", "In progress", Icons.Default.Download, CoolBlue, Modifier.weight(1f))
                StatCard("Failed", "$failedCount", if (failedCount == 0) "No failures" else "Retry available", Icons.Default.ErrorOutline, ErrorRed, Modifier.weight(1f))
                StatCard("Saved", formatBytes(downloadedBytes), "This device", Icons.Default.Storage, SuccessGreen, Modifier.weight(1f))
            }

            DownloadsPanel("Downloaded Songs", Modifier.offset(x = metrics.px(24f), y = metrics.px(196f)).width(metrics.px(700f)).height(metrics.px(136f))) {
                if (tasks.isEmpty()) {
                    EmptyDownloadText("No download tasks yet. Use album or playlist download actions to create real offline files.")
                } else {
                    tasks.take(4).forEach { task ->
                        DownloadTaskRow(
                            task = task,
                            onPlay = { onPlay(task.id) },
                            onPause = { onPause(task.id) },
                            onResume = { onResume(task.id) },
                            onRetry = { onRetry(task.id) },
                            onDelete = { onDelete(task.id) },
                        )
                    }
                }
            }

            DownloadsPanel("Downloaded Albums", Modifier.offset(x = metrics.px(24f), y = metrics.px(348f)).width(metrics.px(700f)).height(metrics.px(78f))) {
                EmptyDownloadText("Album grouping appears after downloaded tracks contain reliable album metadata.")
            }

            DownloadsPanel("Smart Offline Mixes", Modifier.offset(x = metrics.px(24f), y = metrics.px(442f)).width(metrics.px(700f)).height(metrics.px(78f))) {
                EmptyDownloadText("Smart offline mixes were removed as unsupported until a real smart-mix engine exists.")
            }

            SidePanel("Device Storage", Modifier.offset(x = metrics.px(735f), y = metrics.px(38f)).width(metrics.px(205f)).height(metrics.px(185f))) {
                SettingsLine("This PC", "${formatBytes(totalBytes)} total")
                StorageBar(used = usedBytes, total = totalBytes)
                SettingsLine(formatBytes(usedBytes) + " used", formatBytes(freeBytes) + " free")
                SettingsLine("Verified downloads", formatBytes(downloadedBytes))
                SettingsLine("Files on disk", "$diskFileCount")
                SettingsLine("Folder", downloadsDir.absolutePath)
            }

            SidePanel("Download Quality", Modifier.offset(x = metrics.px(735f), y = metrics.px(232f)).width(metrics.px(205f)).height(metrics.px(130f))) {
                QualityRadioLine("Provider default", DownloadQualityMode.PROVIDER_DEFAULT, quality, onQuality)
                QualityRadioLine("Smaller cache files", DownloadQualityMode.SMALLER_FILE, quality, onQuality)
                QualityRadioLine("Prefer high bitrate when available", DownloadQualityMode.PREFER_HIGH, quality, onQuality)
            }

            SidePanel("Download Over", Modifier.offset(x = metrics.px(735f), y = metrics.px(372f)).width(metrics.px(205f)).height(metrics.px(85f))) {
                SettingsLine("Policy", "Any network")
                SettingsLine("Metering", "Desktop detection unavailable")
            }

            SidePanel("Auto-Download", Modifier.offset(x = metrics.px(735f), y = metrics.px(467f)).width(metrics.px(205f)).height(metrics.px(68f))) {
                Text("Smart Downloads", color = TextPrimary, fontSize = 9.sp)
                Text("Not interactive: no real smart-mix engine is available.", color = TextSecondary, fontSize = 7.7.sp, lineHeight = 10.sp)
            }
        }
    }
}

@Composable
private fun ActionButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(metrics.px(8f)))
            .then(
                if (text.contains("Pause")) {
                    Modifier.background(brush = OmniGradients.primaryAction)
                } else {
                    Modifier.background(color = OmniReferenceColors.SurfaceBase.copy(alpha = 0.84f))
                }
            )
            .border(1.dp, BorderLow.copy(alpha = 0.65f), RoundedCornerShape(metrics.px(8f)))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, null, tint = TextPrimary, modifier = Modifier.size(metrics.px(11f)))
        Spacer(Modifier.width(metrics.px(5f)))
        Text(text, color = TextPrimary, fontSize = 8.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun FilterChip(label: String, active: Boolean) {
    Box(
        modifier = Modifier
            .height(LocalHomeReferenceMetrics.current.px(24f))
            .clip(RoundedCornerShape(LocalHomeReferenceMetrics.current.px(99f)))
            .background(if (active) OmniReferenceColors.SurfaceSelectedStrong else Color.Transparent)
            .padding(horizontal = LocalHomeReferenceMetrics.current.px(13f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = if (active) TextPrimary else TextSecondary, fontSize = 8.5.sp)
    }
}

@Composable
private fun StatCard(title: String, value: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, modifier: Modifier) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(metrics.px(8f)))
            .background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.82f))
            .border(1.dp, BorderLow.copy(alpha = 0.65f), RoundedCornerShape(metrics.px(8f)))
            .padding(metrics.px(10f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 8.5.sp)
            Text(value, color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(subtitle, color = TextSecondary, fontSize = 7.5.sp, maxLines = 1)
        }
        Box(Modifier.size(metrics.px(28f)).clip(CircleShape).background(tint.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(metrics.px(15f)))
        }
    }
}

@Composable
private fun DownloadsPanel(title: String, modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Column(modifier = modifier) {
        Text(title, color = TextPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(metrics.px(8f)))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(metrics.px(8f)))
                .background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.74f))
                .border(1.dp, BorderLow.copy(alpha = 0.62f), RoundedCornerShape(metrics.px(8f)))
                .padding(metrics.px(10f)),
            content = content,
        )
    }
}

@Composable
private fun DownloadTaskRow(
    task: DownloadTask,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRetry: () -> Unit,
    onDelete: () -> Unit,
) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(Modifier.fillMaxWidth().height(metrics.px(27f)), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(metrics.px(15f)).clip(CircleShape).background(statusColor(task.state).copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Download, null, tint = statusColor(task.state), modifier = Modifier.size(metrics.px(10f)))
        }
        Spacer(Modifier.width(metrics.px(8f)))
        Column(Modifier.weight(1f)) {
            Text(task.title, color = TextPrimary, fontSize = 8.8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(task.artist, color = TextSecondary, fontSize = 7.4.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(taskProgressLabel(task), color = TextSecondary, fontSize = 8.2.sp, maxLines = 1, modifier = Modifier.width(metrics.px(96f)))
        DownloadRowIcon(task, onPlay, onPause, onResume, onRetry)
        Spacer(Modifier.width(metrics.px(10f)))
        Icon(Icons.Default.Delete, null, tint = TextSecondary, modifier = Modifier.size(metrics.px(13f)).clickable(onClick = onDelete))
    }
}

@Composable
private fun DownloadRowIcon(
    task: DownloadTask,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRetry: () -> Unit,
) {
    val (icon, action) = when (task.state) {
        DownloadState.COMPLETED -> Icons.Default.PlayArrow to onPlay
        DownloadState.PAUSED -> Icons.Default.PlayArrow to onResume
        DownloadState.FAILED, DownloadState.CANCELLED -> Icons.Default.Refresh to onRetry
        DownloadState.QUEUED, DownloadState.RESOLVING, DownloadState.DOWNLOADING -> Icons.Default.Pause to onPause
    }
    Icon(icon, null, tint = IrisSoft, modifier = Modifier.size(LocalHomeReferenceMetrics.current.px(13f)).clickable(onClick = action))
}

@Composable
private fun EmptyDownloadText(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, color = TextSecondary, fontSize = 8.8.sp, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun SidePanel(title: String, modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(metrics.px(8f)))
            .background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.82f))
            .border(1.dp, BorderLow.copy(alpha = 0.65f), RoundedCornerShape(metrics.px(8f)))
            .padding(metrics.px(12f)),
    ) {
        Text(title, color = TextPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(metrics.px(9f)))
        content()
    }
}

@Composable
private fun SettingsLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().height(LocalHomeReferenceMetrics.current.px(18f)), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = TextSecondary, fontSize = 8.1.sp, maxLines = 1, modifier = Modifier.weight(1f))
        Text(value, color = TextPrimary, fontSize = 8.1.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun QualityRadioLine(label: String, value: DownloadQualityMode, selected: DownloadQualityMode, onSelect: (DownloadQualityMode) -> Unit) {
    Row(Modifier.fillMaxWidth().height(LocalHomeReferenceMetrics.current.px(24f)).clickable { onSelect(value) }, verticalAlignment = Alignment.CenterVertically) {
        RadioButton(
            selected = selected == value,
            onClick = { onSelect(value) },
            colors = RadioButtonDefaults.colors(selectedColor = IrisSoft, unselectedColor = TextSecondary),
            modifier = Modifier.size(LocalHomeReferenceMetrics.current.px(18f)),
        )
        Spacer(Modifier.width(LocalHomeReferenceMetrics.current.px(8f)))
        Text(label, color = TextPrimary, fontSize = 8.5.sp, maxLines = 1)
    }
}

@Composable
private fun StorageBar(used: Long, total: Long) {
    val fraction = if (total > 0) (used.toDouble() / total.toDouble()).toFloat().coerceIn(0f, 1f) else 0f
    Box(Modifier.fillMaxWidth().height(LocalHomeReferenceMetrics.current.px(5f)).clip(CircleShape).background(Surface3)) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(fraction).background(IrisSoft))
    }
    Spacer(Modifier.height(LocalHomeReferenceMetrics.current.px(8f)))
}

private fun statusColor(state: DownloadState): Color = when (state) {
    DownloadState.COMPLETED -> SuccessGreen
    DownloadState.FAILED, DownloadState.CANCELLED -> ErrorRed
    DownloadState.PAUSED -> TextSecondary
    DownloadState.QUEUED, DownloadState.RESOLVING, DownloadState.DOWNLOADING -> IrisSoft
}

private fun taskProgressLabel(task: DownloadTask): String {
    val prefix = when (task.state) {
        DownloadState.QUEUED -> "Queued"
        DownloadState.RESOLVING -> "Resolving"
        DownloadState.DOWNLOADING -> "Downloading"
        DownloadState.PAUSED -> "Paused"
        DownloadState.COMPLETED -> "Complete"
        DownloadState.FAILED -> "Failed"
        DownloadState.CANCELLED -> "Cancelled"
    }
    val size = when {
        task.totalBytes != null && task.totalBytes > 0L -> "${formatBytes(task.bytesDownloaded)} / ${formatBytes(task.totalBytes)}"
        task.bytesDownloaded > 0L -> formatBytes(task.bytesDownloaded)
        else -> ""
    }
    val format = listOfNotNull(task.actualCodec, task.actualBitrateKbps?.let { "$it kbps" }).joinToString(" · ")
    return listOf(prefix, size, format).filter { it.isNotBlank() }.joinToString(" · ")
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val units = listOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unit = 0
    while (value >= 1024.0 && unit < units.lastIndex) {
        value /= 1024.0
        unit++
    }
    return if (unit == 0) "${bytes} B" else "%.1f %s".format(value, units[unit])
}
