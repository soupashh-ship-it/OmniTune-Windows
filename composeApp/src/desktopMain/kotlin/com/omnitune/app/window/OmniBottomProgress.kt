package com.omnitune.app.window

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omnitune.app.window.components.OmniProgressSlider

@Composable
internal fun PlayerThinSeekBar(
    positionMs: Long,
    durationMs: Long,
    onSeekFraction: (Float) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableStateOf(0f) }

    val safeFraction = if (durationMs > 0)
        (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    val displayFraction = if (isDragging) dragFraction else safeFraction

    Box(modifier = modifier.height(14.dp), contentAlignment = Alignment.Center) {
        // Inactive track
        Box(
            modifier = Modifier
                .fillMaxWidth().height(2.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(Color.White.copy(alpha = 0.10f))
        )
        // Active filled portion
        if (displayFraction > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(displayFraction).height(2.dp)
                    .align(Alignment.CenterStart)
                    .clip(RoundedCornerShape(99.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF6B7BFF), Color(0xFF7E72FF))
                        )
                    )
            )
        }
        // Thumb dot
        Box(modifier = Modifier.fillMaxWidth().align(Alignment.CenterStart)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(displayFraction)
                    .align(Alignment.CenterStart)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7E72FF))
                        .align(Alignment.CenterEnd)
                )
            }
        }
        // Invisible functional slider overlaid for interaction
        OmniProgressSlider(
            fraction = displayFraction,
            modifier = Modifier.fillMaxWidth().alpha(0f),
            onSeek = { f -> isDragging = true; dragFraction = f },
            onSeekFinished = {
                isDragging = false
                onSeekFraction(dragFraction)
            },
            enabled = enabled && durationMs > 0,
        )
    }
}

@Composable
internal fun PlayerThinSlider(
    fraction: Float,
    onFractionChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.height(14.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .fillMaxWidth().height(2.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(Color.White.copy(alpha = 0.10f))
        )
        if (fraction > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction).height(2.dp)
                    .align(Alignment.CenterStart)
                    .clip(RoundedCornerShape(99.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF6B7BFF), Color(0xFF7E72FF))
                        )
                    )
            )
        }
        // Thumb
        Box(modifier = Modifier.fillMaxWidth().align(Alignment.CenterStart)) {
            Box(modifier = Modifier.fillMaxWidth(fraction).align(Alignment.CenterStart)) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7E72FF))
                        .align(Alignment.CenterEnd)
                )
            }
        }
        // Invisible real Material slider
        Slider(
            value = fraction,
            onValueChange = onFractionChange,
            modifier = Modifier.fillMaxWidth().alpha(0f),
        )
    }
}

@Composable
internal fun TimeLabel(
    ms: Long,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
) {
    val totalSec = ms / 1000
    val m = totalSec / 60
    val s = totalSec % 60
    Text(
        text = "$m:${s.toString().padStart(2, '0')}",
        color = Color(0xFF727A93),
        fontSize = 10.sp,
        maxLines = 1,
        fontWeight = FontWeight.Normal,
        textAlign = textAlign,
        modifier = modifier,
    )
}
