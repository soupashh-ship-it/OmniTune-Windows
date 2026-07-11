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
                val referenceSize = size.minDimension

                // Layer 1: Deepest blue-black foundation
                drawRect(
                    color = NocturneColors.DeepestBase,
                )

                // Layer 2: Vertical navy/obsidian tonal transition
                drawRect(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color(0xFF061025),
                            0.38f to Color(0xFF04091C),
                            0.72f to Color(0xFF040819),
                            1.0f to Color(0xFF020511),
                        )
                    )
                )

                // Layer 3: Restrained cool-blue upper atmosphere
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF284CFF).copy(alpha = 0.115f),
                            Color.Transparent,
                        ),
                        center = Offset(
                            x = size.width * 0.42f,
                            y = size.height * 0.14f,
                        ),
                        radius = referenceSize * 0.78f,
                    )
                )

                // Layer 4: Restrained iris/violet ambient light
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            animatedAccent.copy(alpha = 0.09f),
                            Color.Transparent,
                        ),
                        center = Offset(
                            x = size.width * 0.66f,
                            y = size.height * 0.18f,
                        ),
                        radius = referenceSize * 0.64f,
                    )
                )

                // Layer 5: Extremely subtle lower violet ambience near persistent player
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF8D5CFF).copy(alpha = 0.055f),
                            Color.Transparent,
                        ),
                        center = Offset(
                            x = size.width * 0.46f,
                            y = size.height * 0.94f,
                        ),
                        radius = referenceSize * 0.58f,
                    )
                )
            },
        content = content,
    )
}
