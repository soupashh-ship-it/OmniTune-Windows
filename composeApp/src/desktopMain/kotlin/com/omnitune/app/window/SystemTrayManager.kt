package com.omnitune.app.window

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter

val AppTrayIcon = object : Painter() {
    override val intrinsicSize = Size(64f, 64f)
    override fun DrawScope.onDraw() {
        drawOval(color = Color(0xFF6750A4), topLeft = Offset.Zero, size = size)
        drawOval(
            color = Color.White,
            topLeft = Offset(size.width * 0.25f, size.height * 0.25f),
            size = Size(size.width * 0.5f, size.height * 0.5f)
        )
    }
}

val AppWindowIcon = object : Painter() {
    override val intrinsicSize = Size(64f, 64f)
    override fun DrawScope.onDraw() {
        drawOval(color = Color(0xFF6750A4), topLeft = Offset.Zero, size = size)
    }
}
