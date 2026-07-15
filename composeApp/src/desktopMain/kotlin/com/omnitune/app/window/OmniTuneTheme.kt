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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
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
object OmniReferenceColors {
    // ---------------------------------------------------------
    // GLOBAL WINDOW / SHELL
    // ---------------------------------------------------------
    val WindowBase = Color(0xFF020714)
    val MainBase = Color(0xFF030917)
    val MainBaseAlt = Color(0xFF030917)
    val ContentBase = Color(0xFF030917)
    val ContentDeep = Color(0xFF020816)
    val TopBarBase = Color(0xFF010613)
    val TopBarSecondary = Color(0xFF020713)

    // ---------------------------------------------------------
    // SIDEBAR
    // ---------------------------------------------------------
    val SidebarTop = Color(0xFF050B18)
    val SidebarBase = Color(0xFF050C1D)
    val SidebarMiddle = Color(0xFF050D1E)
    val SidebarBottom = Color(0xFF050D1D)
    val SidebarBlueTint = Color(0xFF0F1642)

    // ---------------------------------------------------------
    // MAIN CONTENT ILLUMINATION
    // ---------------------------------------------------------
    val ContentBluePeak = Color(0xFF07183B)
    val ContentBlueCore = Color(0xFF0C326E)
    val ContentBlueSecondary = Color(0xFF06112B)
    val ContentVioletAmbient = Color(0xFF100C2F)

    // ---------------------------------------------------------
    // GENERAL SURFACES
    // ---------------------------------------------------------
    val SurfaceLowest = Color(0xFF080E1D)
    val SurfaceBase = Color(0xFF090F1F)
    val SurfaceAlternate = Color(0xFF080F1D)
    val SurfaceRaised = Color(0xFF0A1128)
    val SurfaceDeepRaised = Color(0xFF080E1D)
    val SurfaceSelected = Color(0xFF10143D)
    val SurfaceSelectedStrong = Color(0xFF1F1471)
    val Border = Color(0xFF12182A)
    val BorderSoft = Color(0xFF0E1424)
    val SurfaceBorder = Color(0xFF12182A)
    val Divider = Color(0xFF101629)

    // ---------------------------------------------------------
    // SEARCH
    // ---------------------------------------------------------
    val SearchBackground = Color(0xFF090E1D)
    val SearchBackgroundAlternate = Color(0xFF080E1C)
    val SearchBorder = Color(0xFF121827)
    val SearchBorderTop = Color(0xFF141A2A)
    val SearchBorderBottom = Color(0xFF0D1322)

    // ---------------------------------------------------------
    // SELECTED NAVIGATION
    // ---------------------------------------------------------
    val NavSelectedStart = Color(0xFF1D135D)
    val NavSelectedLeftMiddle = Color(0xFF1C1670)
    val NavSelectedCenter = Color(0xFF1F1471)
    val NavSelectedRightMiddle = Color(0xFF24247A)
    val NavSelectedEnd = Color(0xFF17194D)
    val NavSelectedIndicator = Color(0xFF604CE0)

    // ---------------------------------------------------------
    // PRIMARY ACCENT
    // ---------------------------------------------------------
    val Accent = Color(0xFF604CE0)
    val AccentSoft = Color(0xFF6466D8)
    val AccentBright = Color(0xFF6C6CDE)
    val AccentEnd = Color(0xFF5C48DE)

    // ---------------------------------------------------------
    // TEXT
    // ---------------------------------------------------------
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFA9AEC2)
    val TextMuted = Color(0xFF727A93)
    val TextDisabled = Color(0xFF50586E)

    // ---------------------------------------------------------
    // BOTTOM PLAYER
    // ---------------------------------------------------------
    val PlayerBase = Color(0xFF080E1D)
    val PlayerCenter = Color(0xFF070D1B)
    val PlayerViolet = Color(0xFF0F0C24)
    val PlayerVioletStrong = Color(0xFF1B0E31)
    val PlayerRightViolet = Color(0xFF0F0E23)
    val PlayerBorder = Color(0xFF12182A)

    // ---------------------------------------------------------
    // PLAYBACK SLIDERS
    // ---------------------------------------------------------
    val SeekFill = Color(0xFF6466D8)
    val SeekFillBright = Color(0xFF6C6CDE)
    val SeekTrack = Color(0xFF232737)
    val VolumeFill = Color(0xFF6466D8)

    // ---------------------------------------------------------
    // PRIMARY PLAYER CONTROL
    // ---------------------------------------------------------
    val PlayerPrimaryControl = Color(0xFFD6D7E1)
    val PlayerPrimaryControlIcon = Color(0xFF0B0F20)
}

val BgDeep = OmniReferenceColors.WindowBase
val BgInk = OmniReferenceColors.ContentBase
val BgDark = OmniReferenceColors.ContentBase
val BgCard = OmniReferenceColors.SurfaceBase
val BgCardHover = OmniReferenceColors.SurfaceRaised
val BgElevated = OmniReferenceColors.SurfaceRaised
val SurfaceSelected = OmniReferenceColors.SurfaceSelected
val NavActiveBackground = OmniReferenceColors.NavSelectedStart

val BgSurface = OmniReferenceColors.SurfaceBase
val SidebarBg = OmniReferenceColors.SidebarBase
val PlayerDock = OmniReferenceColors.PlayerBase
val Surface1 = OmniReferenceColors.SurfaceBase
val Surface2 = OmniReferenceColors.SurfaceAlternate
val Surface3 = OmniReferenceColors.SurfaceRaised
val Elevated1 = OmniReferenceColors.SurfaceRaised
val Elevated2 = OmniReferenceColors.SurfaceDeepRaised
val Iris = OmniReferenceColors.Accent
val IrisSoft = OmniReferenceColors.AccentSoft
val Violet = OmniReferenceColors.AccentBright
val VioletSoft = OmniReferenceColors.AccentEnd
val CoolBlue = OmniReferenceColors.ContentBlueCore

val AccentLavender = OmniReferenceColors.Accent
val AccentLavenderLight = OmniReferenceColors.AccentSoft
val AccentSecondary = OmniReferenceColors.AccentBright

val TextWhite = OmniReferenceColors.TextPrimary
val TextPrimary = OmniReferenceColors.TextPrimary
val TextSecondary = OmniReferenceColors.TextSecondary
val TextMuted = OmniReferenceColors.TextMuted
val TextDim = OmniReferenceColors.TextDisabled
val TextGray = OmniReferenceColors.TextSecondary

val BorderColor = OmniReferenceColors.SurfaceBorder
val SurfaceHairline = OmniReferenceColors.SurfaceBorder
val BorderLow = OmniReferenceColors.SurfaceBorder
val BorderWhite = OmniReferenceColors.SurfaceBorder
val BorderHover = Color(0x24FFFFFF)

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
    val sidebarWidth = 252.dp
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

    fun screenTween(policy: OmniMotionPolicy = OmniMotionPolicy.Default) = tween<Float>(
        durationMillis = policy.standardDurationMs,
        easing = FastOutSlowInEasing,
    )
}

@Immutable
data class OmniMotionPolicy(
    val reduced: Boolean,
    val standardDurationMs: Int,
    val shortDurationMs: Int,
    val decorativeMotionEnabled: Boolean,
) {
    companion object {
        val Default = OmniMotionPolicy(
            reduced = false,
            standardDurationMs = OmniMotion.normalMs,
            shortDurationMs = OmniMotion.fastFadeMs,
            decorativeMotionEnabled = true,
        )
        val Reduced = OmniMotionPolicy(
            reduced = true,
            standardDurationMs = 1,
            shortDurationMs = 1,
            decorativeMotionEnabled = false,
        )
    }
}

val LocalOmniMotionPolicy = staticCompositionLocalOf { OmniMotionPolicy.Default }

@Immutable
data class OmniThemePalette(
    val windowBase: Color,
    val contentBase: Color,
    val topBarStart: Color,
    val topBarCenter: Color,
    val topBarEnd: Color,
    val mainGlow: Color,
    val violetGlow: Color,
    val rightGlow: Color,
    val accent: Color,
)

private val NocturnePrismPalette = OmniThemePalette(
    windowBase = OmniReferenceColors.WindowBase,
    contentBase = OmniReferenceColors.ContentBase,
    topBarStart = Color(0xFF020713),
    topBarCenter = Color(0xFF010613),
    topBarEnd = Color(0xFF010612),
    mainGlow = Color(0xFF07183B),
    violetGlow = Color(0xFF160D3A),
    rightGlow = Color(0xFF06112B),
    accent = OmniReferenceColors.Accent,
)

private val MidnightPalette = OmniThemePalette(
    windowBase = Color(0xFF01040E),
    contentBase = Color(0xFF020712),
    topBarStart = Color(0xFF020612),
    topBarCenter = Color(0xFF01040D),
    topBarEnd = Color(0xFF00030A),
    mainGlow = Color(0xFF082047),
    violetGlow = Color(0xFF101A4A),
    rightGlow = Color(0xFF06132E),
    accent = Color(0xFF6F8CFF),
)

private val DuskPalette = OmniThemePalette(
    windowBase = Color(0xFF080412),
    contentBase = Color(0xFF0A0616),
    topBarStart = Color(0xFF0A0616),
    topBarCenter = Color(0xFF070411),
    topBarEnd = Color(0xFF05030D),
    mainGlow = Color(0xFF33204E),
    violetGlow = Color(0xFF5B2256),
    rightGlow = Color(0xFF2E173F),
    accent = Color(0xFFC088FF),
)

private val AuroraPalette = OmniThemePalette(
    windowBase = Color(0xFF02080E),
    contentBase = Color(0xFF031017),
    topBarStart = Color(0xFF031018),
    topBarCenter = Color(0xFF020B12),
    topBarEnd = Color(0xFF01070C),
    mainGlow = Color(0xFF0A4B5F),
    violetGlow = Color(0xFF253067),
    rightGlow = Color(0xFF0A5A52),
    accent = Color(0xFF62D6D0),
)

val LocalOmniThemePalette = staticCompositionLocalOf { NocturnePrismPalette }

private fun paletteForTheme(theme: String): OmniThemePalette = when (theme.lowercase()) {
    "midnight" -> MidnightPalette
    "dusk" -> DuskPalette
    "aurora" -> AuroraPalette
    else -> NocturnePrismPalette
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
        surfaceTint = OmniReferenceColors.SidebarMiddle,
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
fun OmniTuneTheme(
    reducedMotion: Boolean = false,
    theme: String = "nocturne",
    content: @Composable () -> Unit,
) {
    val motionPolicy = if (reducedMotion) OmniMotionPolicy.Reduced else OmniMotionPolicy.Default
    val palette = paletteForTheme(theme)
    val scheme = darkColorScheme(
        primary = palette.accent,
        onPrimary = Color.White,
        primaryContainer = palette.accent.copy(alpha = 0.15f),
        onPrimaryContainer = palette.accent,
        secondary = palette.accent,
        onSecondary = Color.White,
        secondaryContainer = Surface1,
        onSecondaryContainer = TextPrimary,
        background = palette.windowBase,
        onBackground = TextPrimary,
        surface = palette.contentBase,
        onSurface = TextPrimary,
        surfaceVariant = Surface2,
        onSurfaceVariant = TextSecondary,
        error = ErrorRed,
        onError = Color.White,
        outline = BorderLow,
        outlineVariant = BorderWhite,
    )
    CompositionLocalProvider(
        LocalOmniMotionPolicy provides motionPolicy,
        LocalOmniThemePalette provides palette,
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = OmniTuneTypography,
            content = content
        )
    }
}

fun Modifier.pressBounce(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.97f
) = composed {
    val motionPolicy = LocalOmniMotionPolicy.current
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (motionPolicy.decorativeMotionEnabled && isPressed) pressedScale else 1f,
        animationSpec = OmniMotion.pressSpring()
    )
    this.scale(scale)
}
