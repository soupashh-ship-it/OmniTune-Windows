package com.omnitune.app.player

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class PlayerNavigationController {
    private val history = mutableListOf<NavScreen>()
    private var historyIndex = -1

    private val _navScreen = MutableStateFlow(NavScreen.Home)
    val navScreen: StateFlow<NavScreen> = _navScreen.asStateFlow()

    private val _canGoBack = MutableStateFlow(false)
    val canGoBack: StateFlow<Boolean> = _canGoBack.asStateFlow()

    private val _canGoForward = MutableStateFlow(false)
    val canGoForward: StateFlow<Boolean> = _canGoForward.asStateFlow()

    fun navigateTo(screen: NavScreen, canOpenNowPlaying: Boolean): Boolean {
        if (screen == NavScreen.NowPlaying && !canOpenNowPlaying) return false
        if (historyIndex >= 0 && historyIndex < history.size - 1) {
            history.subList(historyIndex + 1, history.size).clear()
        }
        if (historyIndex < 0 || history[historyIndex] != screen) {
            history.add(screen)
            historyIndex = history.lastIndex
        }
        _navScreen.value = screen
        updateNavFlags()
        return true
    }

    fun back() {
        if (historyIndex > 0) {
            historyIndex--
            _navScreen.value = history[historyIndex]
            updateNavFlags()
        }
    }

    fun forward() {
        if (historyIndex < history.size - 1) {
            historyIndex++
            _navScreen.value = history[historyIndex]
            updateNavFlags()
        }
    }

    private fun updateNavFlags() {
        _canGoBack.value = historyIndex > 0
        _canGoForward.value = historyIndex < history.size - 1
    }
}
