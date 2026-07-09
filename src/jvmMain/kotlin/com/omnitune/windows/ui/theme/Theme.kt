package com.omnitune.windows.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val OmniColorScheme = darkColorScheme(
    primary = OmniColors.OmniAccentPrimary,
    onPrimary = OmniColors.OmniAccentOnPrimary,
    primaryContainer = OmniColors.OmniAccentSoft,
    secondary = OmniColors.OmniAccentSecondary,
    background = OmniColors.OmniBackgroundBase,
    onBackground = OmniColors.TextPrimary,
    surface = OmniColors.Surface,
    onSurface = OmniColors.TextPrimary,
    surfaceVariant = OmniColors.SurfaceElevated,
    onSurfaceVariant = OmniColors.TextSecondary
)

@Composable
fun OmniTuneTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = OmniColorScheme,
        content = content
    )
}
