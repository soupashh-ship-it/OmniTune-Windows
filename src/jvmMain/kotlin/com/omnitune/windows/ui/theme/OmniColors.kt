package com.omnitune.windows.ui.theme

import androidx.compose.ui.graphics.Color

object OmniColors {
    // Backgrounds — tinted dark with a subtle blue undertone
    val OmniBackgroundBase = Color(0xFF06080F)
    val OmniBackgroundElevated = Color(0xFF0D1019)

    val Background = OmniBackgroundBase
    val Surface = Color(0xFF0C101A)
    val SurfaceElevated = OmniBackgroundElevated
    val SurfaceHairline = Color.White.copy(alpha = 0.06f)
    val SurfacePressed = Color.White.copy(alpha = 0.08f)
    val SurfaceQuiet = Color(0xFF0B0F1A).copy(alpha = 0.68f)
    val SurfacePanel = Color(0xFF101522).copy(alpha = 0.78f)

    // Glass surfaces
    val OmniGlassMedium = Color.White.copy(alpha = 0.02f)
    val OmniGlassDock = Color(0xFF0A0E18).copy(alpha = 0.94f)
    val OmniGlassBorderStrong = Color.White.copy(alpha = 0.02f)

    // Accents — single lavender primary
    val OmniAccentPrimary = Color(0xFF8B8FFF)
    val OmniAccentSecondary = Color(0xFF6B7FFF)
    val OmniAccentTertiary = Color(0xFFB8A0FF)
    val OmniAccentMuted = Color(0xFF6B6AAA)
    val OmniAccentSoft = OmniAccentPrimary.copy(alpha = 0.12f)
    val OmniAccentOnPrimary = Color(0xFF05060A)

    // Text
    val TextPrimary = Color(0xFFF3F4F6)
    val TextSecondary = Color(0xFFA1A5B2)
    val TextTertiary = Color(0xFF757A8A)
    val TextDisabled = Color(0xFF4C5265)
    val TextInverse = Color(0xFF05060A)
}
