package com.omnitune.app.window

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.omnitune.app.platform.PlaybackSession
import com.omnitune.app.platform.PlaybackState
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.toHighResThumbnail

@Composable
fun QueueView(player: PlayerViewModel) {
    val queue by player.queue.collectAsState()
    val queueIndex by player.queueIndex.collectAsState()
    val currentSong by player.currentSong.collectAsState()
    val playbackState by player.playbackState.collectAsState()
    val repeatMode by player.repeatMode.collectAsState()
    val recommendations by player.discoveryTrending.collectAsState()
    val playbackHistory by player.playbackHistory.collectAsState()
    val playbackSessions by player.playbackSessions.collectAsState()
    var message by remember { mutableStateOf<String?>(null) }
    var recRotation by remember { mutableIntStateOf(0) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var playlistName by remember { mutableStateOf("") }

    val realRecommendations = remember(recommendations, queue, recRotation) {
        val queuedIds = queue.map { it.id }.toSet()
        recommendations
            .filterNot { it.id in queuedIds }
            .let { if (it.isEmpty()) it else it.drop(recRotation % it.size) + it.take(recRotation % it.size) }
            .take(8)
    }

    QueueReferenceContent(
        queue = queue,
        queueIndex = queueIndex,
        currentSong = currentSong,
        playbackState = playbackState,
        repeatModeLabel = repeatMode.name.lowercase().replaceFirstChar { it.titlecase() },
        history = playbackHistory.map { it.song },
        sessions = playbackSessions,
        recommendations = realRecommendations,
        message = message,
        onClear = {
            player.clearQueue()
            message = "Queue cleared."
        },
        onSave = {
            if (queue.isEmpty()) {
                message = "Queue is empty. Add tracks before saving."
            } else {
                playlistName = "Queue ${java.time.LocalDateTime.now().toString().replace('T', ' ').substringBeforeLast(':')}"
                showSaveDialog = true
            }
        },
        onShuffle = {
            player.toggleShuffle()
            message = "Shuffle toggled."
        },
        onRepeat = {
            player.cycleRepeat()
            message = "Repeat changed."
        },
        onSeeAllHistory = {
            message = "Showing latest persisted playback history."
        },
        onPlayQueueIndex = player::playQueueIndex,
        onPlayHistoryItem = { player.playSong(it) },
        onRemove = player::removeFromQueue,
        onAddRecommendation = {
            player.addToQueue(it)
            message = "Added to queue."
        },
        onAddAllRecommendations = {
            realRecommendations.forEach(player::addToQueue)
            message = "Added ${realRecommendations.size} real recommendations to queue."
        },
        onRefreshRecommendations = {
            recRotation += 1
            message = "Recommendations refreshed from loaded discovery data."
        },
    )

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save queue as playlist") },
            text = {
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    singleLine = true,
                    label = { Text("Playlist name") },
                )
            },
            confirmButton = {
                TextButton(
                    enabled = playlistName.isNotBlank(),
                    onClick = {
                        player.saveQueueAsPlaylist(playlistName)
                            .onSuccess { message = "Saved playlist: $it" }
                            .onFailure { message = it.message ?: "Could not save playlist." }
                        showSaveDialog = false
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun QueueReferenceContent(
    queue: List<SongItem>,
    queueIndex: Int,
    currentSong: SongItem?,
    playbackState: PlaybackState,
    repeatModeLabel: String,
    history: List<SongItem>,
    sessions: List<PlaybackSession>,
    recommendations: List<SongItem>,
    message: String?,
    onClear: () -> Unit,
    onSave: () -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onSeeAllHistory: () -> Unit,
    onPlayQueueIndex: (Int) -> Unit,
    onPlayHistoryItem: (SongItem) -> Unit,
    onRemove: (Int) -> Unit,
    onAddRecommendation: (SongItem) -> Unit,
    onAddAllRecommendations: () -> Unit,
    onRefreshRecommendations: () -> Unit,
) {
    val metrics = LocalHomeReferenceMetrics.current
    val scroll = rememberScrollState()
    val remaining = if (queueIndex >= 0) queue.drop(queueIndex + 1) else queue
    val totalSeconds = queue.mapNotNull { it.duration }.sum()

    Box(Modifier.fillMaxSize().verticalScroll(scroll)) {
        Box(Modifier.fillMaxWidth().height(metrics.px(560f))) {
            Text("Queue & Session", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.offset(x = metrics.px(24f), y = metrics.px(22f)))
            Text("Manage the active playback queue and real loaded recommendations.", color = TextSecondary, fontSize = 10.sp, modifier = Modifier.offset(x = metrics.px(24f), y = metrics.px(50f)))
            message?.let {
                Text(it, color = IrisSoft, fontSize = 8.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.offset(x = metrics.px(430f), y = metrics.px(31f)).width(metrics.px(250f)))
            }

            ReferencePanel(
                title = "Up Next",
                subtitle = "${remaining.size} remaining · ${formatDuration(totalSeconds)} total",
                modifier = Modifier.offset(x = metrics.px(24f), y = metrics.px(65f)).width(metrics.px(474f)).height(metrics.px(334f)),
                header = {
                    QueueHeaderAction(Icons.Default.Delete, "Clear", onClear)
                    QueueHeaderAction(Icons.Default.PlaylistAdd, "Save as Playlist", onSave)
                    QueueHeaderAction(Icons.Default.Shuffle, "Shuffle", onShuffle)
                    QueueHeaderAction(Icons.Default.Repeat, "Repeat", onRepeat)
                },
            ) {
                if (queue.isEmpty()) {
                    QueueEmpty("Queue is empty", "Play or add a track to populate this session.")
                } else {
                    queue.take(8).forEachIndexed { index, item ->
                        QueueSongRow(
                            item = item,
                            index = index,
                            isCurrent = index == queueIndex,
                            isPlaying = index == queueIndex && playbackState == PlaybackState.PLAYING,
                            onPlay = { onPlayQueueIndex(index) },
                            onRemove = { onRemove(index) },
                        )
                    }
                }
            }

            ReferencePanel(
                title = "Queue Controls",
                subtitle = null,
                modifier = Modifier.offset(x = metrics.px(24f), y = metrics.px(418f)).width(metrics.px(474f)).height(metrics.px(78f)),
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(metrics.px(8f))) {
                    QueueControl(Icons.Default.GraphicEq, "Crossfade", "Not exposed", IrisSoft, Modifier.weight(1f))
                    QueueControl(Icons.Default.AutoAwesome, "Autoplay", "Discovery", CoolBlue, Modifier.weight(1f))
                    QueueControl(Icons.Default.Tune, "Mix Mood", "Queue-based", SuccessGreen, Modifier.weight(1f))
                    QueueControl(Icons.Default.AllInclusive, "Repeat", repeatModeLabel, Iris, Modifier.weight(1f))
                }
            }

            ReferencePanel(
                title = "Session History",
                subtitle = if (sessions.isEmpty()) "No persisted sessions" else "${sessions.size} persisted sessions",
                modifier = Modifier.offset(x = metrics.px(506f), y = metrics.px(65f)).width(metrics.px(213f)).height(metrics.px(246f)),
                seeAll = {
                    Text("See all", color = IrisSoft, fontSize = 8.5.sp, modifier = Modifier.clickable(onClick = onSeeAllHistory))
                },
            ) {
                sessions.take(4).forEach { session ->
                    SessionCard(session)
                }
                if (sessions.isEmpty()) {
                    QueueEmpty("No session history", "Play a track for at least one session to build persisted history.")
                }
            }

            ReferencePanel(
                title = "Recently Played",
                subtitle = "Derived from current queue",
                modifier = Modifier.offset(x = metrics.px(506f), y = metrics.px(329f)).width(metrics.px(213f)).height(metrics.px(166f)),
            ) {
                history.drop(1).take(3).forEach {
                    CompactSongCard(it, "From persisted playback history", onClick = { onPlayHistoryItem(it) })
                }
                if (history.drop(1).isEmpty()) {
                    QueueEmpty("No recent tracks", "Recent tracks persist after playback.")
                }
            }

            ReferencePanel(
                title = "After This Queue Ends",
                subtitle = "Real discovery recommendations",
                modifier = Modifier.offset(x = metrics.px(728f), y = metrics.px(65f)).width(metrics.px(217f)).height(metrics.px(422f)),
                trailingIcon = Icons.Default.AutoAwesome,
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(metrics.px(7f))) {
                    if (recommendations.isEmpty()) {
                        QueueEmpty("No recommendations loaded", "Open Home/Search once discovery finishes loading.")
                    } else {
                        recommendations.take(7).forEach { item ->
                            RecommendationRow(item, onAdd = { onAddRecommendation(item) })
                        }
                    }
                }
                Spacer(Modifier.height(metrics.px(10f)))
                QueuePrimaryButton("Add All to Queue", enabled = recommendations.isNotEmpty(), onClick = onAddAllRecommendations)
                Spacer(Modifier.height(metrics.px(8f)))
                Row(
                    Modifier.fillMaxWidth().height(metrics.px(25f)).clip(RoundedCornerShape(metrics.px(7f))).clickable(onClick = onRefreshRecommendations),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Refresh, null, tint = TextSecondary, modifier = Modifier.size(metrics.px(11f)))
                    Spacer(Modifier.width(metrics.px(6f)))
                    Text("Refresh Recommendations", color = TextSecondary, fontSize = 8.5.sp)
                }
            }
        }
    }
}

@Composable
private fun ReferencePanel(
    title: String,
    subtitle: String?,
    modifier: Modifier,
    header: @Composable RowScope.() -> Unit = {},
    seeAll: (@Composable () -> Unit)? = null,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val metrics = LocalHomeReferenceMetrics.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(metrics.px(9f)))
            .background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.78f))
            .border(1.dp, BorderLow.copy(alpha = 0.64f), RoundedCornerShape(metrics.px(9f)))
            .padding(metrics.px(12f)),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                if (subtitle != null) {
                    Text(subtitle, color = TextSecondary, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(8f)), verticalAlignment = Alignment.CenterVertically) {
                header()
                seeAll?.invoke()
                trailingIcon?.let { Icon(it, null, tint = IrisSoft, modifier = Modifier.size(metrics.px(15f))) }
            }
        }
        Spacer(Modifier.height(metrics.px(12f)))
        content()
    }
}

@Composable
private fun QueueHeaderAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(
        modifier = Modifier.height(metrics.px(18f)).clip(RoundedCornerShape(metrics.px(5f))).clickable(onClick = onClick).padding(horizontal = metrics.px(4f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(metrics.px(10f)))
        Spacer(Modifier.width(metrics.px(4f)))
        Text(label, color = TextSecondary, fontSize = 7.7.sp, maxLines = 1)
    }
}

@Composable
private fun QueueSongRow(
    item: SongItem,
    index: Int,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onRemove: () -> Unit,
) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(metrics.px(33f))
            .clip(RoundedCornerShape(metrics.px(7f)))
            .background(if (isCurrent) OmniReferenceColors.SurfaceSelected.copy(alpha = 0.82f) else Color.Transparent)
            .clickable(onClick = onPlay)
            .padding(horizontal = metrics.px(7f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.DragIndicator, null, tint = TextMuted, modifier = Modifier.size(metrics.px(10f)))
        Text("${index + 1}", color = if (isCurrent) IrisSoft else TextMuted, fontSize = 8.sp, modifier = Modifier.width(metrics.px(20f)), textAlign = TextAlign.Center)
        Artwork(item.thumbnail, metrics.px(24f))
        Spacer(Modifier.width(metrics.px(8f)))
        Column(Modifier.weight(1.25f)) {
            Text(item.title, color = if (isCurrent) IrisSoft else TextPrimary, fontSize = 8.8.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(item.artists.joinToString(", ") { it.name }, color = TextSecondary, fontSize = 7.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(item.album?.name ?: "Single", color = TextSecondary, fontSize = 7.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(0.82f))
        Text(formatDuration(item.duration ?: 0), color = TextSecondary, fontSize = 7.5.sp, modifier = Modifier.width(metrics.px(32f)), textAlign = TextAlign.End)
        Icon(if (isPlaying) Icons.Default.PlayArrow else Icons.Default.MoreHoriz, null, tint = TextSecondary, modifier = Modifier.size(metrics.px(13f)).clickable(onClick = onRemove))
    }
}

@Composable
private fun QueueControl(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String, tint: Color, modifier: Modifier) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(metrics.px(7f)))
            .background(OmniReferenceColors.SurfaceRaised.copy(alpha = 0.65f))
            .border(1.dp, BorderLow.copy(alpha = 0.45f), RoundedCornerShape(metrics.px(7f)))
            .padding(horizontal = metrics.px(8f), vertical = metrics.px(5f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(metrics.px(14f)))
        Spacer(Modifier.width(metrics.px(6f)))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(title, color = TextPrimary, fontSize = 7.2.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(value, color = TextSecondary, fontSize = 6.6.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun CompactSongCard(item: SongItem, meta: String, onClick: () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(
        modifier = Modifier.fillMaxWidth().height(metrics.px(38f)).clip(RoundedCornerShape(metrics.px(7f))).background(OmniReferenceColors.SurfaceRaised.copy(alpha = 0.54f)).clickable(onClick = onClick).padding(metrics.px(7f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Artwork(item.thumbnail, metrics.px(24f))
        Spacer(Modifier.width(metrics.px(8f)))
        Column(Modifier.weight(1f)) {
            Text(item.title, color = TextPrimary, fontSize = 8.4.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(meta, color = TextSecondary, fontSize = 7.2.sp, maxLines = 1)
        }
        Icon(Icons.Default.PlayArrow, null, tint = IrisSoft, modifier = Modifier.size(metrics.px(12f)))
    }
    Spacer(Modifier.height(metrics.px(7f)))
}

@Composable
private fun SessionCard(session: PlaybackSession) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(metrics.px(38f))
            .clip(RoundedCornerShape(metrics.px(7f)))
            .background(OmniReferenceColors.SurfaceRaised.copy(alpha = 0.54f))
            .padding(metrics.px(7f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(metrics.px(24f)).clip(CircleShape).background(IrisSoft.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.GraphicEq, null, tint = IrisSoft, modifier = Modifier.size(metrics.px(12f)))
        }
        Spacer(Modifier.width(metrics.px(8f)))
        Column(Modifier.weight(1f)) {
            Text("${session.playCount} plays · ${session.uniqueTrackCount} tracks", color = TextPrimary, fontSize = 8.4.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text("${formatMillis(session.totalListeningMs)} listened", color = TextSecondary, fontSize = 7.2.sp, maxLines = 1)
        }
    }
    Spacer(Modifier.height(metrics.px(7f)))
}

@Composable
private fun RecommendationRow(item: SongItem, onAdd: () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(Modifier.fillMaxWidth().height(metrics.px(31f)), verticalAlignment = Alignment.CenterVertically) {
        Artwork(item.thumbnail, metrics.px(24f))
        Spacer(Modifier.width(metrics.px(8f)))
        Column(Modifier.weight(1f)) {
            Text(item.title, color = TextPrimary, fontSize = 8.3.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(item.artists.joinToString(", ") { it.name }, color = TextSecondary, fontSize = 7.2.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Row(
            Modifier.height(metrics.px(20f)).clip(RoundedCornerShape(metrics.px(6f))).background(OmniReferenceColors.SurfaceSelected.copy(alpha = 0.54f)).clickable(onClick = onAdd).padding(horizontal = metrics.px(7f)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Add, null, tint = IrisSoft, modifier = Modifier.size(metrics.px(9f)))
            Spacer(Modifier.width(metrics.px(3f)))
            Text("Add", color = IrisSoft, fontSize = 7.4.sp)
        }
    }
}

@Composable
private fun QueuePrimaryButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(metrics.px(28f))
            .clip(RoundedCornerShape(metrics.px(8f)))
            .then(if (enabled) Modifier.background(brush = OmniGradients.primaryAction) else Modifier.background(Surface3))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = TextPrimary, fontSize = 8.6.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun QueueEmpty(title: String, subtitle: String) {
    Box(Modifier.fillMaxWidth().height(LocalHomeReferenceMetrics.current.px(58f)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            Text(subtitle, color = TextSecondary, fontSize = 7.5.sp, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun Artwork(url: String?, size: androidx.compose.ui.unit.Dp) {
    Box(Modifier.size(size).clip(RoundedCornerShape(LocalHomeReferenceMetrics.current.px(5f))).background(Surface3), contentAlignment = Alignment.Center) {
        if (!url.isNullOrBlank()) {
            AsyncImage(
                model = url.toHighResThumbnail(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(Icons.Default.GraphicEq, null, tint = TextMuted, modifier = Modifier.size(size * 0.48f))
        }
    }
}

private fun formatDuration(seconds: Int): String {
    if (seconds <= 0) return "--:--"
    val mins = seconds / 60
    val secs = seconds % 60
    return "$mins:${secs.toString().padStart(2, '0')}"
}

private fun formatMillis(ms: Long): String {
    if (ms <= 0L) return "0:00"
    val totalSeconds = ms / 1000L
    val mins = totalSeconds / 60L
    val secs = totalSeconds % 60L
    return "$mins:${secs.toString().padStart(2, '0')}"
}
