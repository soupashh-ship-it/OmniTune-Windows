package com.omnitune.app.window

import kotlin.test.Test
import kotlin.test.assertEquals

class OmniResponsiveLayoutTest {
    @Test
    fun sizeClassUsesDensityIndependentContentWidth() {
        assertEquals(OmniDesktopSizeClass.Compact, OmniResponsiveLayout.sizeClassForContentWidth(520f))
        assertEquals(OmniDesktopSizeClass.Medium, OmniResponsiveLayout.sizeClassForContentWidth(660f))
        assertEquals(OmniDesktopSizeClass.Expanded, OmniResponsiveLayout.sizeClassForContentWidth(1040f))
        assertEquals(OmniDesktopSizeClass.Large, OmniResponsiveLayout.sizeClassForContentWidth(1320f))
    }

    @Test
    fun settingsColumnsAvoidUnsafeThreeColumnCompression() {
        assertEquals(1, OmniResponsiveLayout.settingsColumnCount(520f))
        assertEquals(2, OmniResponsiveLayout.settingsColumnCount(820f))
        assertEquals(3, OmniResponsiveLayout.settingsColumnCount(1040f))
        assertEquals(3, OmniResponsiveLayout.settingsColumnCount(1600f))
    }

    @Test
    fun settingsContentWidthIsCappedToPreserveFullscreenDensity() {
        assertEquals(520f, OmniResponsiveLayout.settingsMaxContentWidth(1))
        assertEquals(820f, OmniResponsiveLayout.settingsMaxContentWidth(2))
        assertEquals(1120f, OmniResponsiveLayout.settingsMaxContentWidth(3))
        assertEquals(1120f, OmniResponsiveLayout.settingsMaxContentWidth(99))
    }
}
