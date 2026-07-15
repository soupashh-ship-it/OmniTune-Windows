package com.omnitune.app.window

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun OmniReferenceBackdrop(
    modifier: Modifier = Modifier,
    sidebarWidthDp: androidx.compose.ui.unit.Dp,
    topBarHeightDp: androidx.compose.ui.unit.Dp,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = LocalOmniThemePalette.current
    Box(modifier = modifier) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val sidebarWidth = sidebarWidthDp.toPx()
            val topBarHeight = topBarHeightDp.toPx()

            // 1. ABSOLUTE BASE
            drawRect(
                color = palette.windowBase,
                size = size
            )

            // 2. MAIN CONTENT BASE
            drawRect(
                color = palette.contentBase,
                topLeft = Offset(
                    x = sidebarWidth,
                    y = topBarHeight
                ),
                size = androidx.compose.ui.geometry.Size(
                    width = size.width - sidebarWidth,
                    height = size.height - topBarHeight
                )
            )

            // 3. TOP BAR
            drawRect(
                brush = Brush.horizontalGradient(
                    colorStops = arrayOf(
                        0.00f to palette.topBarStart,
                        0.45f to palette.topBarCenter,
                        1.00f to palette.topBarEnd
                    ),
                    startX = sidebarWidth,
                    endX = size.width
                ),
                topLeft = Offset(
                    x = sidebarWidth,
                    y = 0f
                ),
                size = androidx.compose.ui.geometry.Size(
                    width = size.width - sidebarWidth,
                    height = topBarHeight
                )
            )

            // 6. MAIN LEFT BLUE ILLUMINATION
            val mainBlueCenter = Offset(
                x = sidebarWidth + size.width * 0.012f,
                y = topBarHeight + size.height * 0.25f
            )
            val mainBlueRadius = size.width * 0.30f
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.00f to palette.mainGlow.copy(alpha = 0.25f),
                        0.30f to palette.mainGlow.copy(alpha = 0.11f),
                        0.58f to palette.mainGlow.copy(alpha = 0.045f),
                        1.00f to Color.Transparent
                    ),
                    center = mainBlueCenter,
                    radius = mainBlueRadius
                ),
                center = mainBlueCenter,
                radius = mainBlueRadius
            )
            // 7. EXTREMELY SUBTLE UPPER VIOLET AMBIENCE
            val violetCenter = Offset(
                x = size.width * 0.43f,
                y = size.height * 0.05f
            )
            val violetRadius = size.width * 0.34f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.violetGlow.copy(alpha = 0.075f),
                        palette.violetGlow.copy(alpha = 0.028f),
                        Color.Transparent
                    ),
                    center = violetCenter,
                    radius = violetRadius
                ),
                center = violetCenter,
                radius = violetRadius
            )

            // 8. VERY SUBTLE RIGHT-SIDE NAVY DEPTH
            val rightDepthCenter = Offset(
                x = size.width * 0.93f,
                y = size.height * 0.58f
            )
            val rightDepthRadius = size.width * 0.30f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.rightGlow.copy(alpha = 0.07f),
                        Color.Transparent
                    ),
                    center = rightDepthCenter,
                    radius = rightDepthRadius
                ),
                center = rightDepthCenter,
                radius = rightDepthRadius
            )
            // 9. SUBTLE SIDEBAR DIVIDER
            drawLine(
                color = Color(0xFF11172B).copy(alpha = 0.52f),
                start = Offset(sidebarWidth, 0f),
                end = Offset(sidebarWidth, size.height),
                strokeWidth = 1f
            )

        }
        content()
    }
}
