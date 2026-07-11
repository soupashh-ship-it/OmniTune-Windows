package com.omnitune.app.window

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun NocturneBackdrop(
    modifier: Modifier = Modifier,
    dynamicAccent: Color = NocturneColors.Iris,
    content: @Composable BoxScope.() -> Unit,
) {
    val animatedAccent by animateColorAsState(
        targetValue = dynamicAccent,
        animationSpec = tween(durationMillis = 900),
        label = "NocturneAmbientAccent",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                val minDimension = size.minDimension

                // 1. Deep blue-black base.
                drawRect(
                    color = Color(0xFF020511)
                )

                // 2. Very subtle vertical tonal depth.
                drawRect(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to Color(0xFF050A1D),
                            0.25f to Color(0xFF05091B),
                            0.60f to Color(0xFF050819),
                            1.00f to Color(0xFF020511),
                        )
                    )
                )

                // 3. Soft cool-blue upper atmosphere.
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF2446D8).copy(alpha = 0.075f),
                            Color.Transparent,
                        ),
                        center = Offset(
                            x = size.width * 0.47f,
                            y = size.height * 0.12f,
                        ),
                        radius = minDimension * 0.82f,
                    )
                )

                // 4. Restrained iris atmosphere.
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            animatedAccent.copy(alpha = 0.045f),
                            Color.Transparent,
                        ),
                        center = Offset(
                            x = size.width * 0.73f,
                            y = size.height * 0.20f,
                        ),
                        radius = minDimension * 0.65f,
                    )
                )

                // 5. Very faint lower-player ambience.
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF805EFF).copy(alpha = 0.035f),
                            Color.Transparent,
                        ),
                        center = Offset(
                            x = size.width * 0.46f,
                            y = size.height * 0.94f,
                        ),
                        radius = minDimension * 0.55f,
                    )
                )
            },
        content = content,
    )
}
