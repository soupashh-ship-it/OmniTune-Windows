package com.omnitune.app.window

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

private const val HOME_REFERENCE_WIDTH_PX = 1142f

@Immutable
data class HomeReferenceMetrics(
    val scale: Float,
    val density: Density,
) {
    fun px(referencePixels: Float): Dp = with(density) {
        (referencePixels * scale).toDp()
    }
}

@Composable
fun rememberHomeReferenceMetrics(availableWidth: Dp): HomeReferenceMetrics {
    val density = LocalDensity.current
    val availableWidthPx = with(density) { availableWidth.toPx() }
    val scale = availableWidthPx / HOME_REFERENCE_WIDTH_PX

    return remember(availableWidthPx, density) {
        HomeReferenceMetrics(scale = scale, density = density)
    }
}

val LocalHomeReferenceMetrics = compositionLocalOf<HomeReferenceMetrics> {
    error("Home reference metrics were not provided")
}

internal object HomeReferenceSpec {
    const val CanvasWidth = 1142f
    const val CanvasHeight = 654f

    const val SidebarWidth = 180f

    const val TopBarX = 181f
    const val TopBarHeight = 49f

    const val MainContentLeft = 204f

    const val GreetingX = 204f
    const val GreetingVisibleTop = 69f

    const val HeroX = 204f
    const val HeroY = 104f
    const val HeroWidth = 624f
    const val HeroHeight = 203f

    const val HeroMainWidth = 440f
    const val HeroSideWidth = 184f

    const val HeroPagerX = 780f
    const val HeroPagerY = 68f
    const val HeroPagerWidth = 48f
    const val HeroPagerHeight = 25f

    const val ContinueX = 852f
    const val ContinueY = 104f
    const val ContinueWidth = 270f

    const val QuickPicksHeadingY = 329f
    const val QuickPicksCardY = 349f

    const val MadeForYouHeadingX = 781f
    const val MadeForYouHeadingY = 329f
    const val MadeForYouCardY = 351f

    const val TrendingX = 204f
    const val TrendingHeadingY = 468f

    const val NewReleasesX = 628f
    const val NewReleasesHeadingY = 468f

    const val PlayerX = 4f
    const val PlayerY = 572f
    const val PlayerWidth = 1134f
    const val PlayerHeight = 75f
    const val PlayerBottomMargin = 7f
}
