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
object NocturneColors {
    val DeepestBase = Color(0xFF020511)
    val Canvas = Color(0xFF040819)
    val CanvasTop = Color(0xFF061025)
    val SidebarBase = Color(0xFF080A16)
    val TopChrome = Color(0xFF020614)
    val Surface = Color(0xB80D1222)
    val SurfaceStrong = Color(0xE60F1426)
    val SurfaceHover = Color(0xFF181D35)
    val Iris = Color(0xFF7D6EFF)
    val Violet = Color(0xFF9A6BFF)
    val CoolBlue = Color(0xFF4C6FFF)
    val PrimaryText = Color(0xFFF4F3FA)
    val SecondaryText = Color(0xFFA9AEC2)
    val MutedText = Color(0xFF737B93)
    val BorderSubtle = Color(0x14FFFFFF)
    val BorderHover = Color(0x24FFFFFF)
}

val BgDeep = NocturneColors.DeepestBase
val BgInk = NocturneColors.Canvas
val BgDark = NocturneColors.DeepestBase
val BgCard = NocturneColors.Surface
val BgCardHover = NocturneColors.SurfaceHover
val BgElevated = NocturneColors.SurfaceStrong
val SurfaceSelected = NocturneColors.SurfaceStrong

val BgSurface = NocturneColors.Surface
val SidebarBg = NocturneColors.SidebarBase
val SidebarBackground = NocturneColors.SidebarBase
val PlayerDock = Color(0xFF0A0F1F)
val Surface1 = NocturneColors.Surface
val Surface2 = NocturneColors.SurfaceStrong
val Surface3 = NocturneColors.SurfaceHover
val Elevated1 = NocturneColors.SurfaceStrong
val Elevated2 = NocturneColors.SurfaceHover

val Iris = NocturneColors.Iris
val IrisSoft = NocturneColors.Violet
val Violet = NocturneColors.Violet
val VioletSoft = Color(0xFFA855F7)
val CoolBlue = NocturneColors.CoolBlue

val AccentLavender = Iris
val AccentLavenderLight = VioletSoft
val AccentSecondary = IrisSoft

val TextWhite = NocturneColors.PrimaryText
val TextPrimary = NocturneColors.PrimaryText
val TextSecondary = NocturneColors.SecondaryText
val TextMuted = NocturneColors.MutedText
val TextDim = Color(0xFF4A485B)
val TextGray = NocturneColors.SecondaryText

val BorderColor = NocturneColors.BorderSubtle
val SurfaceHairline = NocturneColors.BorderSubtle
val BorderLow = NocturneColors.BorderSubtle
val BorderWhite = NocturneColors.BorderSubtle
val BorderHover = NocturneColors.BorderHover

val SuccessGreen = Color(0xFF5BD6A0)
val ErrorRed = Color(0xFFFF6363)

object OmniGradients {
    val primaryAction: Brush
        get() = Brush.horizontalGradient(
            colors = listOf(Iris, VioletSoft)
        )
    val irisToLavender: Brush get() = primaryAction
    val irisVertical: Brush
        get() = Brush.verticalGradient(
            colors = listOf(Iris.copy(alpha = 0.22f), Violet.copy(alpha = 0.10f)),
        )
    val activeNavGlow: Brush
        get() = Brush.horizontalGradient(
            colors = listOf(Iris.copy(alpha = 0.15f), Color.Transparent)
        )
    fun heroAmbient(seed: Color = Iris): Brush = Brush.radialGradient(
        colors = listOf(
            seed.copy(alpha = 0.15f),
            BgDeep
        )
    )
}

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

    // Semantic
    val PagePadding = 32.dp
    val SidebarPadding = 16.dp
    val PlayerPadding = 20.dp
    val SectionGap = 32.dp
    val CardGap = 16.dp
    val ControlGap = 12.dp
    val SongRowPadding = 8.dp
}

object Radii {
    val radiusXS = 4.dp
    val radiusSmall = 8.dp
    val radiusMedium = 12.dp
    val radiusLarge = 16.dp
    val radiusXL = 20.dp
    val pill = 999.dp
}

object Shapes {
    val tiny = RoundedCornerShape(Radii.radiusXS)
    val small = RoundedCornerShape(Radii.radiusSmall)
    val medium = RoundedCornerShape(Radii.radiusMedium)
    val large = RoundedCornerShape(Radii.radiusLarge)
    val extraLarge = RoundedCornerShape(Radii.radiusXL)
    val artworkSmall = RoundedCornerShape(Radii.radiusSmall)
    val artworkMedium = RoundedCornerShape(16.dp)
    val artworkLarge = RoundedCornerShape(24.dp)
    val player = RoundedCornerShape(Radii.radiusXL)
    val dock = RoundedCornerShape(24.dp)
    val pill = RoundedCornerShape(Radii.pill)
}

object OmniLayout {
    val sidebarWidth = 230.dp
    val topBarHeight = 56.dp
    val bottomPlayerHeight = 80.dp
    val mainContentMaxWidth = 1200.dp
    val minWindowWidth = 1024.dp
    val minWindowHeight = 640.dp
}

object OmniMotion {
    const val fastFadeMs = 140
    const val fastMs = 140
    const val normalMs = 200
    const val slowMs = 300
    const val screenTransitionMs = 220
    const val sectionTransitionMs = 320

    fun <T> pressSpring() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
    )
    
    fun <T> gentleSpring() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow,
    )

    fun screenTween() = tween<Float>(
        durationMillis = normalMs,
        easing = FastOutSlowInEasing,
    )
}
data class GlassSurfaceStyle(
    val surfaceTint: Color,
    val surfaceAlpha: Float = 1.0f,
    val overlayColor: Color? = null,
    val overlayAlpha: Float = 0f,
    val borderColor: Color = BorderLow,
    val borderAlpha: Float = 1.0f,
    val borderWidth: Dp = 1.dp,
    val shadowElevation: Dp = 0.dp,
)

object GlassDefaults {
    val base = GlassSurfaceStyle(
        surfaceTint = BgDeep,
        borderAlpha = 0f,
        shadowElevation = 0.dp
    )
    val card = GlassSurfaceStyle(
        surfaceTint = Surface1,
        borderColor = BorderLow,
        borderWidth = 1.dp,
        shadowElevation = 4.dp
    )
    val navBar = GlassSurfaceStyle(
        surfaceTint = SidebarBackground,
        borderAlpha = 0f,
        shadowElevation = 0.dp
    )
    val playerDock = GlassSurfaceStyle(
        surfaceTint = PlayerDock,
        borderColor = BorderLow,
        borderWidth = 1.dp,
        shadowElevation = 8.dp
    )
    val miniPlayer = GlassSurfaceStyle(
        surfaceTint = PlayerDock,
        surfaceAlpha = 0.96f,
        overlayColor = Elevated1,
        overlayAlpha = 0.85f,
        borderColor = Color.White,
        borderAlpha = 0.02f,
        borderWidth = 0.5.dp,
        shadowElevation = 10.dp,
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
                        ambientColor = Color.Black.copy(alpha = 0.5f),
                        spotColor = Iris.copy(alpha = 0.1f),
                    )
                } else Modifier
            )
            .clip(shape)
            .background(style.surfaceTint.copy(alpha = style.surfaceAlpha))
            .border(
                width = style.borderWidth,
                color = style.borderColor.copy(alpha = style.borderAlpha),
                shape = shape,
            ),
    ) {
        content()
    }
}

private val DarkColorScheme = darkColorScheme(
    primary = Iris,
    onPrimary = Color.White,
    primaryContainer = Iris.copy(alpha = 0.15f),
    onPrimaryContainer = VioletSoft,
    secondary = IrisSoft,
    onSecondary = Color.White,
    secondaryContainer = Surface1,
    onSecondaryContainer = TextPrimary,
    background = BgDeep,
    onBackground = TextPrimary,
    surface = Surface1,
    onSurface = TextPrimary,
    surfaceVariant = Surface2,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = Color.White,
    outline = BorderLow,
    outlineVariant = BorderWhite,
)

val OmniTuneTypography = Typography(
    displayLarge = TextStyle(fontSize = 44.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp, lineHeight = 52.sp),
    displayMedium = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.sp, lineHeight = 44.sp),
    displaySmall = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.sp, lineHeight = 38.sp),
    headlineLarge = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.25).sp, lineHeight = 34.sp),
    headlineMedium = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp, lineHeight = 26.sp),
    headlineSmall = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp, lineHeight = 24.sp),
    titleLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp, lineHeight = 22.sp),
    titleMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.sp, lineHeight = 20.sp),
    titleSmall = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.sp, lineHeight = 18.sp),
    bodyLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.sp, lineHeight = 20.sp),
    bodyMedium = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.sp, lineHeight = 18.sp),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp, lineHeight = 16.sp),
    labelMedium = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp, lineHeight = 14.sp),
    labelSmall = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp, lineHeight = 14.sp),
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
    pressedScale: Float = 0.97f
) = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = OmniMotion.pressSpring()
    )
    this.scale(scale)
}
