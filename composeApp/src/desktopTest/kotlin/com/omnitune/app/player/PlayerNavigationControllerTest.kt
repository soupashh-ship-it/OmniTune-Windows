package com.omnitune.app.player

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlayerNavigationControllerTest {
    @Test
    fun `navigation tracks back and forward history`() {
        val controller = PlayerNavigationController()

        controller.navigateTo(NavScreen.Search, canOpenNowPlaying = false)
        controller.navigateTo(NavScreen.Library, canOpenNowPlaying = false)

        assertEquals(NavScreen.Library, controller.navScreen.value)
        assertTrue(controller.canGoBack.value)
        assertFalse(controller.canGoForward.value)

        controller.back()
        assertEquals(NavScreen.Search, controller.navScreen.value)
        assertFalse(controller.canGoBack.value)
        assertTrue(controller.canGoForward.value)

        controller.forward()
        assertEquals(NavScreen.Library, controller.navScreen.value)
    }

    @Test
    fun `now playing is blocked until a current song exists`() {
        val controller = PlayerNavigationController()

        val blocked = controller.navigateTo(NavScreen.NowPlaying, canOpenNowPlaying = false)
        assertFalse(blocked)
        assertEquals(NavScreen.Home, controller.navScreen.value)

        val allowed = controller.navigateTo(NavScreen.NowPlaying, canOpenNowPlaying = true)
        assertTrue(allowed)
        assertEquals(NavScreen.NowPlaying, controller.navScreen.value)
    }
}
