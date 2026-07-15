package com.omnitune.app.window

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource

/**
 * Loads the packaged OmniTune icon from the desktop classpath.
 *
 * Compose marks string-based classpath resource loading as deprecated in favor
 * of generated multiplatform resources. This desktop app currently packages the
 * icon as a runtime classpath asset, so the deprecated call is intentionally
 * isolated here instead of repeated across UI files.
 */
@Suppress("DEPRECATION")
@Composable
internal fun omniTuneIconPainter(): Painter = painterResource("omnitune-icon.png")
