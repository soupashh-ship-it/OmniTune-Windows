package com.omnitune.windows.domain.playback

import com.omnitune.windows.models.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QueueManager {
    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(-1)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    fun setQueue(tracks: List<Track>, startIndex: Int) {
        _queue.value = tracks
        _currentIndex.value = startIndex
    }

    fun next(): Track? {
        val current = _queue.value
        val index = _currentIndex.value
        if (current.isEmpty() || index < 0 || index >= current.lastIndex) return null
        
        _currentIndex.value = index + 1
        return current[_currentIndex.value]
    }

    fun previous(): Track? {
        val current = _queue.value
        val index = _currentIndex.value
        if (current.isEmpty() || index <= 0) return null
        
        _currentIndex.value = index - 1
        return current[_currentIndex.value]
    }
}
