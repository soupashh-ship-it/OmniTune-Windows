package com.omnitune.app.window

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun NowPlayingIndicator(isPlaying: Boolean) {
    val motionPolicy = LocalOmniMotionPolicy.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        // Animated mini equalizer bars — decorative visualizer (NOT real waveform data)
        if (isPlaying && motionPolicy.decorativeMotionEnabled) {
            AnimatedEqBars()
            Spacer(Modifier.width(8.dp))
        } else if (isPlaying) {
            StaticEqBars()
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
internal fun StaticEqBars() {
    Row(
        modifier = Modifier.height(14.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        listOf(9f, 12f, 7f, 10f, 6f).forEach { height ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(height.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(IrisSoft),
            )
        }
    }
}

@Composable
internal fun AnimatedEqBars() {
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
