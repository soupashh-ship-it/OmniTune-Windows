package com.omnitune.windows.domain.search

import com.omnitune.innertube.YouTube
import com.omnitune.innertube.pages.HomePage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeController(
    private val scope: CoroutineScope
) {
    private val _homePage = MutableStateFlow<HomePage?>(null)
    val homePage: StateFlow<HomePage?> = _homePage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadHome() {
        if (_homePage.value != null || _isLoading.value) return

        scope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = YouTube.home().getOrNull()
                if (result != null) {
                    _homePage.value = result
                } else {
                    _error.value = "Failed to load Home data."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
