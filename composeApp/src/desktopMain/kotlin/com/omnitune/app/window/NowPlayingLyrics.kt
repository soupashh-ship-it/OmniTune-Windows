package com.omnitune.app.window

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.omnitune.app.window.components.OmniShimmerBlock
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omnitune.app.player.LyricLine
import com.omnitune.app.player.LyricsResult
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.models.YTItem
import kotlinx.coroutines.delay

@Composable
internal fun LyricsRegion(
    modifier: Modifier,
    lyricsResult: LyricsResult,
    displayTimeMs: Long,
    player: PlayerViewModel,
    queue: List<SongItem>,
    currentSong: SongItem?,
    queueIndex: Int,
    related: List<YTItem>,
    relatedLoading: Boolean,
    relatedError: String?,
) {
    var tab by remember { mutableStateOf(0) }

    Box(
        modifier = modifier
            .clip(Shapes.large)
            .background(Surface1.copy(alpha = 0.70f))
            .border(1.dp, BorderLow, Shapes.large)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Iris.copy(alpha = 0.24f),
                            CoolBlue.copy(alpha = 0.08f),
                            Color.Transparent,
                        ),
                        center = androidx.compose.ui.geometry.Offset(330f, 280f),
                        radius = 420f,
                    )
                )
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.018f),
                            Color.Transparent,
                            BgDeep.copy(alpha = 0.20f),
                        )
                    )
                )
        )
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PanelTab("LYRICS", tab == 0) { tab = 0 }
            PanelTab("RELATED", tab == 1) { tab = 1 }
        }

        Box(
            Modifier
                .fillMaxSize()
                .padding(start = 20.dp, end = 20.dp, top = 64.dp, bottom = 16.dp)
        ) {
            when (tab) {
                0 -> LyricsPanel(
                    lyricsResult = lyricsResult,
                    displayTimeMs = displayTimeMs,
                    player = player,
                )
                1 -> RelatedPanel(
                    related = related,
                    loading = relatedLoading,
                    error = relatedError,
                    player = player,
                    onRetry = player::retryRelated,
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(46.dp)
                .background(Color(0xFF080B1C).copy(alpha = 0.35f))
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("AA", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.weight(1f))
            val syncLabel = when (lyricsResult) {
                is LyricsResult.Synced -> "✓ SYNCED"
                is LyricsResult.Unsynced -> "UNSYNCED"
                is LyricsResult.Loading -> "LOADING"
                is LyricsResult.NotFound -> "NO LYRICS"
                is LyricsResult.Error -> "ERROR"
            }
            Box(
                modifier = Modifier
                    .clip(Shapes.pill)
                    .background(if (lyricsResult is LyricsResult.Synced) Color(0xFF3B2B83).copy(alpha = 0.88f) else Surface2.copy(alpha = 0.70f))
                    .border(1.dp, Iris.copy(alpha = 0.20f), Shapes.pill)
                    .padding(horizontal = 26.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(syncLabel, color = TextPrimary.copy(alpha = 0.86f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.MoreHoriz, contentDescription = "Lyrics options", tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
internal fun LyricsPanel(
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

@Composable
internal fun SyncedLyricsDisplay(
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
    val motionPolicy = LocalOmniMotionPolicy.current
    LaunchedEffect(currentLine, userIsScrolling, motionPolicy) {
        if (!userIsScrolling && currentLine >= 0 && lines.isNotEmpty()) {
            val targetIndex = (currentLine + 1).coerceAtMost(lines.lastIndex)
            if (!motionPolicy.reduced) {
                listState.animateScrollToItem(
                    index = targetIndex.coerceAtLeast(0),
                    scrollOffset = -180,
                )
            } else {
                listState.scrollToItem(
                    index = targetIndex.coerceAtLeast(0),
                    scrollOffset = -180,
                )
            }
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
                    animationSpec = tween(motionPolicy.standardDurationMs),
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
            enter = fadeIn(tween(motionPolicy.shortDurationMs)),
            exit = fadeOut(tween(motionPolicy.shortDurationMs)),
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

@Composable
internal fun UnsyncedLyricsDisplay(text: String) {
    Column(Modifier.fillMaxSize()) {
        Text(
            "Unsynced lyrics",
            color = TextMuted,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        LazyColumn(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item { Spacer(Modifier.height(10.dp)) }
            text.lines().forEachIndexed { _, line ->
                if (line.isBlank()) {
                    item { Spacer(Modifier.height(12.dp)) }
                } else {
                    item {
                        Text(
                            line,
                            color = TextPrimary.copy(alpha = 0.58f),
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
                            lineHeight = 28.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth(0.92f),
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(88.dp)) }
        }
    }
}

@Composable
internal fun LyricsLoadingState() {
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

@Composable
internal fun PanelTab(label: String, active: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .width(116.dp)
            .height(34.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (active) TextPrimary else TextSecondary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
        )
        if (active) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width(94.dp)
                    .height(2.dp)
                    .clip(CircleShape)
                    .background(OmniGradients.irisToLavender)
            )
        }
    }
}
