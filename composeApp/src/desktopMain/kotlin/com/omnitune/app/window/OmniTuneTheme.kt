package com.omnitune.app.window

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val AccentLavender = Color(0xFF8B8FFF)
val AccentLavenderLight = Color(0xFFB0B3FF)
val AccentSecondary = Color(0xFF6B7FFF)

val BgDark = Color(0xFF060912)
val BgSurface = Color(0xFF0C101A)
val BgCard = Color(0xFF131928)
val BgCardHover = Color(0xFF1C2340)
val BgElevated = Color(0xFF0A0E18)
val TextWhite = Color(0xFFF2F3F8)
val TextGray = Color(0xFFA8B0C4)
val TextDim = Color(0xFF7A8299)
val BorderColor = Color(0xFF2A2E3E)
val SurfaceHairline = Color.White.copy(alpha = 0.06f)

// --- Nocturne Prism extended palette (deep obsidian / indigo / iris / violet) ---
val BgDeep = Color(0xFF050812)
val BgInk = Color(0xFF080C18)
val Surface1 = Color(0xFF11172A)
val Surface2 = Color(0xFF151B31)
val Surface3 = Color(0xFF191F38)
val Elevated1 = Color(0xFF1B213B)
val Elevated2 = Color(0xFF202644)

val Iris = Color(0xFF8178FF)
val IrisSoft = Color(0xFF8B84FF)
val Violet = Color(0xFF996EFF)
val VioletSoft = Color(0xFFA77AFF)
val CoolBlue = Color(0xFF5D7FFF)
val CoolBlueSoft = Color(0xFF6B8CFF)

val TextPrimary = Color(0xFFF4F3FA)
val TextSecondary = Color(0xFFA8AEC2)
val TextMuted = Color(0xFF70788F)

val BorderLow = Color(red = 150, green = 160, blue = 210).copy(alpha = 0.10f)
val BorderWhite = Color.White.copy(alpha = 0.06f)
val BorderHover = Color.White.copy(alpha = 0.12f)

val SuccessGreen = Color(0xFF5BD6A0)
val ErrorRed = Color(0xFFFF6363)

object OmniColors {
    val surfaceTinted = Color(0xFF0C101A)
    val surfaceRaised = Color(0xFF131928).copy(alpha = 0.92f)
    val glassSubtle = Color.White.copy(alpha = 0.01f)
    val glassMedium = Color.White.copy(alpha = 0.02f)
    val glassStrong = Color.White.copy(alpha = 0.04f)
    val glassDock = Color(0xFF0A0E18).copy(alpha = 0.94f)
    val glassBorder = Color.White.copy(alpha = 0.02f)

    val accentGlow = AccentLavender.copy(alpha = 0.30f)
    val accentSoft = AccentLavender.copy(alpha = 0.12f)

    // Nocturne Prism aurora lighting (navy/indigo base with iris/violet bloom)
    val auroraBase = Color(0xFF060912)
    val auroraIris = Iris.copy(alpha = 0.16f)
    val auroraViolet = Violet.copy(alpha = 0.12f)
    val auroraBlue = CoolBlue.copy(alpha = 0.10f)
}

object OmniGradients {
    val irisToLavender: Brush
        get() = Brush.linearGradient(
            colors = listOf(Iris, VioletSoft),
        )
    val irisVertical: Brush
        get() = Brush.verticalGradient(
            colors = listOf(Iris.copy(alpha = 0.22f), Violet.copy(alpha = 0.10f)),
        )
    val activeNavGlow: Brush
        get() = Brush.horizontalGradient(
            colors = listOf(Iris.copy(alpha = 0.22f), Violet.copy(alpha = 0.06f)),
        )
    fun heroAurora(seed: Color = Iris): Brush = Brush.radialGradient(
        colors = listOf(
            seed.copy(alpha = 0.22f),
            AuroraVioletFallback.copy(alpha = 0.10f),
            BgDeep,
        )
    )
    val playerScrim: Brush
        get() = Brush.verticalGradient(
            colors = listOf(Color.Transparent, BgDeep.copy(alpha = 0.85f)),
        )
}

private val AuroraVioletFallback = Violet

// Focus ring used across interactive controls for keyboard accessibility.
fun Modifier.focusRing(isFocused: Boolean, color: Color = Iris): Modifier = this.then(
    if (isFocused) Modifier.border(1.5.dp, color.copy(alpha = 0.7f), Shapes.small) else Modifier
)

object Spacing {
    val micro = 4.dp
    val compact = 8.dp
    val small = 12.dp
    val medium = 16.dp
    val large = 20.dp
    val section = 24.dp
    val hero = 32.dp
    val screen = 40.dp
}

object Shapes {
    val tiny = RoundedCornerShape(6.dp)
    val small = RoundedCornerShape(10.dp)
    val medium = RoundedCornerShape(14.dp)
    val large = RoundedCornerShape(20.dp)
    val extraLarge = RoundedCornerShape(28.dp)
    val artworkSmall = RoundedCornerShape(10.dp)
    val artworkMedium = RoundedCornerShape(16.dp)
    val artworkLarge = RoundedCornerShape(24.dp)
    val player = RoundedCornerShape(28.dp)
    val dock = RoundedCornerShape(24.dp)
    val pill = RoundedCornerShape(999.dp)
}

object OmniMotion {
    const val fastFadeMs = 140
    const val screenTransitionMs = 220
    const val sectionTransitionMs = 320

    fun <T> pressSpring() = spring<T>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium,
    )

    fun <T> gentleSpring() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )

    fun screenTween() = tween<Float>(
        durationMillis = screenTransitionMs,
        easing = FastOutSlowInEasing,
    )
}

data class GlassSurfaceStyle(
    val surfaceTint: Color = Color.White,
    val surfaceAlpha: Float = 0.02f,
    val overlayColor: Color? = null,
    val overlayAlpha: Float = 0f,
    val borderColor: Color = Color.White,
    val borderAlpha: Float = 0.02f,
    val borderWidth: Dp = 1.dp,
    val shadowElevation: Dp = 0.dp,
)

object GlassDefaults {
    val miniPlayer = GlassSurfaceStyle(
        surfaceTint = Color(0xFF06090E),
        surfaceAlpha = 0.90f,
        overlayColor = BgElevated,
        overlayAlpha = 0.85f,
        borderColor = Color.White,
        borderAlpha = 0.015f,
        borderWidth = 0.5.dp,
        shadowElevation = 10.dp,
    )

    val card = GlassSurfaceStyle(
        surfaceTint = Color(0xFF0A0E17),
        surfaceAlpha = 0.68f,
        borderColor = Color.White,
        borderAlpha = 0.01f,
        borderWidth = 0.5.dp,
        shadowElevation = 4.dp,
    )

    val navBar = GlassSurfaceStyle(
        surfaceTint = Color(0xFF080B12),
        surfaceAlpha = 0.88f,
        borderColor = Color.White,
        borderAlpha = 0.018f,
        borderWidth = 0.5.dp,
        shadowElevation = 6.dp,
    )
}

@Composable
fun OmniGlassSurface(
    shape: Shape,
    style: GlassSurfaceStyle,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .then(
                if (style.shadowElevation > 0.dp) {
                    Modifier.shadow(
                        elevation = style.shadowElevation,
                        shape = shape,
                        ambientColor = Color.Black.copy(alpha = 0.32f),
                        spotColor = AccentLavender.copy(alpha = 0.06f),
                    )
                } else Modifier
            )
            .clip(shape),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (style.overlayColor != null) {
                        Modifier.background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    style.surfaceTint.copy(alpha = style.surfaceAlpha),
                                    style.overlayColor.copy(alpha = style.overlayAlpha),
                                )
                            )
                        )
                    } else {
                        Modifier.background(
                            style.surfaceTint.copy(alpha = style.surfaceAlpha)
                        )
                    }
                )
                .border(
                    width = style.borderWidth,
                    color = style.borderColor.copy(alpha = style.borderAlpha),
                    shape = shape,
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape),
            content = content,
        )
    }
}

private val DarkColorScheme = darkColorScheme(
    primary = AccentLavender,
    onPrimary = Color(0xFF05060A),
    primaryContainer = AccentLavender.copy(alpha = 0.15f),
    onPrimaryContainer = AccentLavenderLight,
    secondary = AccentSecondary,
    onSecondary = Color(0xFF05060A),
    secondaryContainer = AccentSecondary.copy(alpha = 0.15f),
    onSecondaryContainer = AccentLavenderLight,
    background = BgDark,
    onBackground = TextWhite,
    surface = BgSurface,
    onSurface = TextWhite,
    surfaceVariant = BgCard,
    onSurfaceVariant = TextGray,
    error = Color(0xFFFF6363),
    onError = Color(0xFF05060A),
    outline = BorderColor,
    outlineVariant = SurfaceHairline,
    inverseSurface = TextWhite,
    inverseOnSurface = BgDark,
    surfaceTint = AccentLavender,
)

val OmniTuneTypography = Typography(
    displayLarge = TextStyle(fontSize = 38.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp, lineHeight = 46.sp),
    displayMedium = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.sp, lineHeight = 38.sp),
    displaySmall = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp, lineHeight = 32.sp),
    headlineLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.25).sp, lineHeight = 28.sp),
    headlineMedium = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp, lineHeight = 24.sp),
    headlineSmall = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp, lineHeight = 22.sp),
    titleLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.sp, lineHeight = 20.sp),
    titleMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.sp, lineHeight = 18.sp),
    titleSmall = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.sp, lineHeight = 18.sp),
    bodyLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.sp, lineHeight = 20.sp),
    bodyMedium = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.sp, lineHeight = 18.sp),
    bodySmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.sp, lineHeight = 16.sp),
    labelMedium = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.sp, lineHeight = 14.sp),
    labelSmall = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.sp, lineHeight = 14.sp),
)

@Composable
fun OmniTuneTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = OmniTuneTypography,
        content = content
    )
}

fun Modifier.pressBounce(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.95f
) = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = OmniMotion.pressSpring()
    )
    this.scale(scale)
}
