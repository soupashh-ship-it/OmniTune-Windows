package com.omnitune.app.window

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.omnitune.app.player.LyricLine
import com.omnitune.app.player.LyricsResult
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.app.platform.PlaybackState
import com.omnitune.app.platform.PlayerPosition
import com.omnitune.app.window.components.OmniProgressSlider
import com.omnitune.app.window.components.OmniShimmerBlock
import com.omnitune.app.window.components.OmniVolumeControl
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.toHighResThumbnail
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────────────────────────────────────
// NowPlayingView — Nocturne Prism immersive player + synchronized lyrics
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun NowPlayingView(
    player: PlayerViewModel,
    currentSong: SongItem?,
    playbackState: PlaybackState,
    pos: PlayerPosition,
    volume: Int,
) {
    val lyricsResult by player.lyricsResult.collectAsState()
    val repeat by player.repeatMode.collectAsState()
    val shuffle by player.shuffleMode.collectAsState()
    val queue by player.queue.collectAsState()
    val queueIndex by player.queueIndex.collectAsState()

    // Seek drag state — UI owns the scrub position during drag; engine is not queried
    var sliderPos by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val displayPos = if (isDragging && pos.lengthMs > 0) sliderPos else pos.position
    val displayTimeMs = if (isDragging && pos.lengthMs > 0) (sliderPos * pos.lengthMs).toLong() else pos.timeMs

    if (currentSong == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.MusicNote, null, tint = TextMuted, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(16.dp))
                Text("No Track Playing", style = MaterialTheme.typography.headlineSmall, color = TextGray)
                Spacer(Modifier.height(8.dp))
                Text("Search and tap a song to start playing", style = MaterialTheme.typography.bodyLarge, color = TextDim)
            }
        }
        return
    }

    // Ambient background — subtle radial glow behind artwork (not full-screen blur)
    Box(modifier = Modifier.fillMaxSize()) {
        // Background: deep obsidian + subtle iris ambient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Iris.copy(alpha = 0.09f), BgDeep),
                        radius = 900f,
                    )
                )
        )

        // Main layout: Player (left) + Lyrics (right)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp, vertical = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            // ─── LEFT: Player ────────────────────────────────────────────────────
            PlayerRegion(
                modifier = Modifier.weight(0.55f).fillMaxHeight(),
                player = player,
                currentSong = currentSong,
                playbackState = playbackState,
                pos = pos,
                displayTimeMs = displayTimeMs,
                displayPos = displayPos,
                volume = volume,
                repeat = repeat,
                shuffle = shuffle,
                isDragging = isDragging,
                onSliderChange = { sliderPos = it; isDragging = true },
                onSliderFinish = {
                    isDragging = false
                    if (pos.lengthMs > 0) player.seek((sliderPos * pos.lengthMs).toLong())
                },
            )

            // ─── RIGHT: Lyrics panel ─────────────────────────────────────────────
            LyricsRegion(
                modifier = Modifier.weight(0.45f).fillMaxHeight(),
                lyricsResult = lyricsResult,
                displayTimeMs = displayTimeMs,
                player = player,
                queue = queue,
                currentSong = currentSong,
                queueIndex = queueIndex,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Player Region (Left)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PlayerRegion(
    modifier: Modifier,
    player: PlayerViewModel,
    currentSong: SongItem,
    playbackState: PlaybackState,
    pos: PlayerPosition,
    displayTimeMs: Long,
    displayPos: Float,
    volume: Int,
    repeat: com.omnitune.app.player.RepeatMode,
    shuffle: Boolean,
    isDragging: Boolean,
    onSliderChange: (Float) -> Unit,
    onSliderFinish: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // NOW PLAYING label
        NowPlayingIndicator(isPlaying = playbackState == PlaybackState.PLAYING)

        Spacer(Modifier.height(16.dp))

        // Artwork — primary visual anchor
        val artScale by animateFloatAsState(
            targetValue = if (playbackState == PlaybackState.PLAYING) 1f else 0.94f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
            label = "artScale",
        )
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .fillMaxWidth(0.72f)
                .aspectRatio(1f)
                .scale(artScale)
                .shadow(
                    elevation = 24.dp,
                    shape = Shapes.artworkLarge,
                    ambientColor = Color.Black.copy(alpha = 0.35f),
                    spotColor = Iris.copy(alpha = 0.18f),
                )
                .clip(Shapes.artworkLarge)
                .background(Surface1),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = currentSong.thumbnail.toHighResThumbnail(),
                contentDescription = "Album artwork",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        Spacer(Modifier.height(24.dp))

        // Track title + artist + actions row
        Text(
            currentSong.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            currentSong.artists.joinToString(", ") { it.name },
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))

        // Meta-action row: Favorite | Add | Overflow
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NpIconButton(icon = Icons.Default.FavoriteBorder, tooltip = "Like") { /* NOT SUPPORTED: no like endpoint */ }
            Spacer(Modifier.width(8.dp))
            NpIconButton(icon = Icons.Default.AddCircleOutline, tooltip = "Add to library") { /* NOT SUPPORTED */ }
            Spacer(Modifier.width(8.dp))
            NpIconButton(icon = Icons.Default.MoreHoriz, tooltip = "More options") { /* Overflow menu - future */ }
        }

        Spacer(Modifier.weight(1f))

        // Decorative playback visualizer (honest: animated bars, NOT real waveform)
        PlaybackVisualizer(
            isPlaying = playbackState == PlaybackState.PLAYING,
            modifier = Modifier.fillMaxWidth().height(28.dp).padding(horizontal = 4.dp),
        )

        Spacer(Modifier.height(8.dp))

        // Timeline: position + slider + duration
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                formatTime(displayTimeMs),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.width(40.dp),
            )
            OmniProgressSlider(
                fraction = displayPos,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                enabled = pos.lengthMs > 0,
                onSeek = onSliderChange,
                onSeekFinished = onSliderFinish,
            )
            val remaining = if (pos.lengthMs > 0) pos.lengthMs - displayTimeMs else 0L
            Text(
                if (pos.lengthMs > 0) "-${formatTime(remaining)}" else "–:––",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.End,
            )
        }

        Spacer(Modifier.height(20.dp))

        // Transport controls: Shuffle | Prev | Play/Pause | Next | Repeat
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Shuffle
            val shuffleInteraction = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(interactionSource = shuffleInteraction, indication = null) { player.toggleShuffle() }
                    .pressBounce(shuffleInteraction),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Shuffle,
                    "Shuffle",
                    tint = if (shuffle) IrisSoft else TextSecondary,
                    modifier = Modifier.size(22.dp),
                )
                if (shuffle) {
                    Box(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 2.dp)
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(IrisSoft)
                    )
                }
            }

            Spacer(Modifier.width(24.dp))

            // Previous
            TransportButton(icon = Icons.Default.SkipPrevious, size = 44.dp) { player.previousTrack() }

            Spacer(Modifier.width(20.dp))

            // Central Play/Pause
            val playInteraction = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(OmniGradients.irisToLavender)
                    .clickable(interactionSource = playInteraction, indication = null) { player.togglePlayPause() }
                    .pressBounce(playInteraction, pressedScale = 0.93f),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (playbackState == PlaybackState.PLAYING) Icons.Default.Pause else Icons.Default.PlayArrow,
                    "Play/Pause",
                    tint = Color(0xFF05060A),
                    modifier = Modifier.size(32.dp),
                )
            }

            Spacer(Modifier.width(20.dp))

            // Next
            TransportButton(icon = Icons.Default.SkipNext, size = 44.dp) { player.nextTrack() }

            Spacer(Modifier.width(24.dp))

            // Repeat
            val repeatInteraction = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(interactionSource = repeatInteraction, indication = null) { player.cycleRepeat() }
                    .pressBounce(repeatInteraction),
                contentAlignment = Alignment.Center,
            ) {
                val repeatIcon = when (repeat) {
                    com.omnitune.app.player.RepeatMode.ONE -> Icons.Default.RepeatOne
                    else -> Icons.Default.Repeat
                }
                val repeatActive = repeat != com.omnitune.app.player.RepeatMode.OFF
                Icon(
                    repeatIcon,
                    "Repeat",
                    tint = if (repeatActive) IrisSoft else TextSecondary,
                    modifier = Modifier.size(22.dp),
                )
                if (repeatActive) {
                    Box(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 2.dp)
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(IrisSoft)
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Volume row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.AutoMirrored.Filled.VolumeUp, "Volume", tint = TextMuted, modifier = Modifier.size(18.dp))
            OmniVolumeControl(volume = volume, onVolumeChange = { player.setVolume(it) })
            Text("${volume}%", style = MaterialTheme.typography.bodySmall, color = TextMuted, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Lyrics Region (Right)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LyricsRegion(
    modifier: Modifier,
    lyricsResult: LyricsResult,
    displayTimeMs: Long,
    player: PlayerViewModel,
    queue: List<SongItem>,
    currentSong: SongItem?,
    queueIndex: Int,
) {
    var tab by remember { mutableStateOf(0) }

    Column(modifier = modifier) {
        // Tab header
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PanelTab("LYRICS", tab == 0) { tab = 0 }
            PanelTab("RELATED", tab == 1) { tab = 1 }
        }

        Spacer(Modifier.height(12.dp))

        // Panel body
        Box(
            Modifier
                .fillMaxSize()
                .clip(Shapes.large)
                .background(Surface1.copy(alpha = 0.65f))
                .border(1.dp, BorderLow, Shapes.large)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            when (tab) {
                0 -> LyricsPanel(
                    lyricsResult = lyricsResult,
                    displayTimeMs = displayTimeMs,
                    player = player,
                )
                1 -> QueuePanel(queue = queue, currentSong = currentSong, queueIndex = queueIndex, player = player)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Lyrics Panel — synced, unsynced, loading, empty, error
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LyricsPanel(
    lyricsResult: LyricsResult,
    displayTimeMs: Long,
    player: PlayerViewModel,
) {
    when (lyricsResult) {
        is LyricsResult.Loading -> LyricsLoadingState()

        is LyricsResult.Synced -> {
            SyncedLyricsDisplay(
                lines = lyricsResult.lines,
                displayTimeMs = displayTimeMs,
                player = player,
            )
        }

        is LyricsResult.Unsynced -> {
            UnsyncedLyricsDisplay(text = lyricsResult.text)
        }

        is LyricsResult.NotFound -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.MusicNote, null, tint = TextMuted, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Lyrics aren't available for this track",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        is LyricsResult.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ErrorOutline, null, tint = TextMuted, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Couldn't load lyrics",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Synced lyrics — real timestamp-driven active line, auto-scroll, manual override
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SyncedLyricsDisplay(
    lines: List<LyricLine>,
    displayTimeMs: Long,
    player: PlayerViewModel,
) {
    // Active line: last line whose timestamp <= current position (binary search equivalent via indexOfLast)
    val currentLine by remember(displayTimeMs, lines) {
        derivedStateOf {
            if (lines.isEmpty()) -1
            else {
                var lo = 0
                var hi = lines.size - 1
                var result = -1
                while (lo <= hi) {
                    val mid = (lo + hi) / 2
                    if (lines[mid].timeMs <= displayTimeMs) {
                        result = mid
                        lo = mid + 1
                    } else {
                        hi = mid - 1
                    }
                }
                result.coerceAtLeast(0)
            }
        }
    }

    val listState = rememberLazyListState()

    // Manual scroll override state
    var userIsScrolling by remember { mutableStateOf(false) }
    var lastScrollTime by remember { mutableStateOf(0L) }

    // Detect user scroll
    val isListScrolling = listState.isScrollInProgress
    LaunchedEffect(isListScrolling) {
        if (isListScrolling) {
            userIsScrolling = true
            lastScrollTime = System.currentTimeMillis()
        }
    }

    // Auto-resume after scroll inactivity (5 seconds)
    LaunchedEffect(lastScrollTime) {
        if (lastScrollTime > 0L) {
            delay(5000)
            if (System.currentTimeMillis() - lastScrollTime >= 5000) {
                userIsScrolling = false
            }
        }
    }

    // Auto-scroll to active line when not user-scrolling
    LaunchedEffect(currentLine, userIsScrolling) {
        if (!userIsScrolling && currentLine >= 0 && lines.isNotEmpty()) {
            val targetIndex = (currentLine + 1).coerceAtMost(lines.lastIndex)
            listState.animateScrollToItem(
                index = targetIndex.coerceAtLeast(0),
                scrollOffset = -180,
            )
        }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item { Spacer(Modifier.height(16.dp)) }
            itemsIndexed(lines, key = { i, _ -> i }) { i, line ->
                val isActive = i == currentLine
                val isPast = i < currentLine

                val alpha by animateFloatAsState(
                    targetValue = when {
                        isActive -> 1f
                        isPast -> 0.35f
                        else -> 0.50f
                    },
                    animationSpec = tween(300),
                    label = "lyricAlpha$i",
                )

                val targetFontSize = if (isActive) 22.sp else 15.sp

                Text(
                    line.text,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = targetFontSize),
                    color = if (isActive) TextPrimary else TextPrimary.copy(alpha = alpha),
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    lineHeight = 28.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            // Click lyric line to seek to its timestamp
                            player.seek(line.timeMs)
                            userIsScrolling = false
                        },
                )
            }
            item { Spacer(Modifier.height(80.dp)) }
        }

        // Return to current lyric button — shown only while user has scrolled away
        AnimatedVisibility(
            visible = userIsScrolling,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(300)),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
        ) {
            val interaction = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .clip(Shapes.pill)
                    .background(Surface2.copy(alpha = 0.95f))
                    .border(1.dp, Iris.copy(alpha = 0.3f), Shapes.pill)
                    .clickable(interactionSource = interaction, indication = null) {
                        userIsScrolling = false
                        lastScrollTime = 0L
                    }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = IrisSoft, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Return to current lyric", color = IrisSoft, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Unsynced lyrics — plain text, no fake synchronization
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun UnsyncedLyricsDisplay(text: String) {
    Column(Modifier.fillMaxSize()) {
        Text(
            "Unsynced lyrics",
            color = TextMuted,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Spacer(Modifier.height(4.dp)) }
            text.lines().forEachIndexed { _, line ->
                if (line.isBlank()) {
                    item { Spacer(Modifier.height(8.dp)) }
                } else {
                    item {
                        Text(
                            line,
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 26.sp,
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Lyrics loading skeleton
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LyricsLoadingState() {
    val widths = listOf(0.7f, 0.85f, 0.6f, 0.9f, 0.75f, 0.55f, 0.8f, 0.65f)
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Spacer(Modifier.height(8.dp))
        widths.forEach { w ->
            OmniShimmerBlock(
                modifier = Modifier.fillMaxWidth(w).height(14.dp).clip(Shapes.small),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Queue panel — up next in current session
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun QueuePanel(
    queue: List<SongItem>,
    currentSong: SongItem?,
    queueIndex: Int,
    player: PlayerViewModel,
) {
    if (queue.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Queue is empty", color = TextMuted, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        }
        return
    }
    val currentId = currentSong?.id
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        itemsIndexed(queue) { i, s ->
            val active = s.id == currentId
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Shapes.small)
                    .background(if (active) Iris.copy(alpha = 0.1f) else Color.Transparent)
                    .clickable { player.playQueueIndex(i) }
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsyncImage(
                    model = s.thumbnail.toHighResThumbnail(),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp).clip(Shapes.artworkSmall),
                    contentScale = ContentScale.Crop,
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(s.title, color = if (active) IrisSoft else TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall)
                    Text(s.artists.joinToString { it.name }, color = TextSecondary, maxLines = 1, style = MaterialTheme.typography.bodySmall)
                }
                if (active) {
                    Icon(Icons.Default.PlayArrow, null, tint = IrisSoft, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NOW PLAYING indicator with animated bars
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NowPlayingIndicator(isPlaying: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        // Animated mini equalizer bars — decorative visualizer (NOT real waveform data)
        if (isPlaying) {
            AnimatedEqBars()
            Spacer(Modifier.width(8.dp))
        }
        Text(
            "NOW PLAYING",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
            color = IrisSoft,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun AnimatedEqBars() {
    val infiniteTransition = rememberInfiniteTransition(label = "eqBars")
    val delays = listOf(0, 100, 200, 50, 150)
    val heights = delays.map { delayMs ->
        infiniteTransition.animateFloat(
            initialValue = 3f,
            targetValue = 12f,
            animationSpec = infiniteRepeatable(
                animation = tween(350 + delayMs, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "bar$delayMs",
        )
    }
    Row(
        modifier = Modifier.height(14.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        heights.forEach { h ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(h.value.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(IrisSoft),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Decorative playback visualizer — animated soft bars, NOT real waveform data
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PlaybackVisualizer(isPlaying: Boolean, modifier: Modifier) {
    val barCount = 18
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer")

    // All bars always animated; target height collapses to low when paused
    val barHeights: List<State<Float>> = (0 until barCount).map { i ->
        val period = 500 + (i * 53 % 450)
        infiniteTransition.animateFloat(
            initialValue = if (i % 2 == 0) 0.15f else 0.85f,
            targetValue = if (i % 2 == 0) 0.85f else 0.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(period, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "vbar$i",
        )
    }

    val playingAlpha by animateFloatAsState(
        targetValue = if (isPlaying) 0.75f else 0.3f,
        animationSpec = tween(300),
        label = "vizAlpha",
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        barHeights.forEach { anim ->
            val fraction = if (isPlaying) anim.value else 0.12f
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height((fraction * 28f).coerceAtLeast(3f).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Iris.copy(alpha = playingAlpha), IrisSoft.copy(alpha = playingAlpha * 0.4f))
                        )
                    ),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Small helpers
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PanelTab(label: String, active: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .clip(Shapes.pill)
            .background(if (active) Iris.copy(alpha = 0.18f) else Surface2)
            .border(1.dp, if (active) Iris.copy(alpha = 0.4f) else BorderLow, Shapes.pill)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            label,
            color = if (active) IrisSoft else TextSecondary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun TransportButton(icon: ImageVector, size: Dp, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .pressBounce(interaction),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = TextWhite, modifier = Modifier.size(size * 0.65f))
    }
}

@Composable
private fun NpIconButton(icon: ImageVector, tooltip: String, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .pressBounce(interaction),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, tooltip, tint = TextSecondary, modifier = Modifier.size(22.dp))
    }
}

private fun formatTime(ms: Long): String {
    if (ms < 0) return "0:00"
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}
