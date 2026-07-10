package com.omnitune.app.window.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.omnitune.app.window.*

@Composable
fun OmniProgressSlider(
    fraction: Float,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onSeek: (Float) -> Unit,
    onSeekFinished: (() -> Unit)? = null,
) {
    Slider(
        value = if (enabled) fraction.coerceIn(0f, 1f) else 0f,
        onValueChange = onSeek,
        onValueChangeFinished = onSeekFinished,
        enabled = enabled,
        modifier = modifier.height(18.dp),
        colors = SliderDefaults.colors(
            thumbColor = Color.White,
            activeTrackColor = if (enabled) Iris else TextMuted.copy(alpha = 0.4f),
            inactiveTrackColor = Color.White.copy(alpha = 0.10f),
        ),
    )
}

@Composable
fun OmniVolumeControl(
    volume: Int,
    maxVolume: Int = 200,
    modifier: Modifier = Modifier,
    onVolumeChange: (Int) -> Unit,
) {
    val fraction = (volume.toFloat() / maxVolume).coerceIn(0f, 1f)
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (volume == 0) Icons.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
            null,
            tint = TextSecondary,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Slider(
            value = fraction,
            onValueChange = { onVolumeChange((it * maxVolume).toInt().coerceIn(0, maxVolume)) },
            modifier = Modifier.width(90.dp).height(18.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Iris,
                inactiveTrackColor = Color.White.copy(alpha = 0.10f),
            ),
        )
    }
}
