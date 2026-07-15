package com.omnitune.app.window

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.IntSize
import org.jetbrains.skia.Image
import kotlin.math.roundToInt

private val OmniTuneIconBitmap: ImageBitmap? by lazy {
    runCatching {
        Thread.currentThread()
            .contextClassLoader
            .getResourceAsStream("omnitune-icon.png")
            ?.use { Image.makeFromEncoded(it.readBytes()).toComposeImageBitmap() }
    }.getOrNull()
}

val AppTrayIcon = object : Painter() {
    override val intrinsicSize = Size(64f, 64f)
    override fun DrawScope.onDraw() {
        val bitmap = OmniTuneIconBitmap
        if (bitmap != null) {
            drawImage(bitmap, dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()))
        } else {
            drawOval(color = Color(0xFF6750A4), size = size)
        }
    }
}

val AppWindowIcon = object : Painter() {
    override val intrinsicSize = Size(64f, 64f)
    override fun DrawScope.onDraw() {
        val bitmap = OmniTuneIconBitmap
        if (bitmap != null) {
            drawImage(bitmap, dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()))
        } else {
            drawOval(color = Color(0xFF6750A4), size = size)
        }
    }
}
