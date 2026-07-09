package com.omnitune.windows.domain.search

import com.omnitune.innertube.YouTube
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.models.YouTubeClient
import com.omnitune.windows.models.Artist
import com.omnitune.windows.models.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchController(
    private val scope: CoroutineScope
) {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _results = MutableStateFlow<List<SongItem>>(emptyList())
    val results: StateFlow<List<SongItem>> = _results.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
    }

    fun executeSearch() {
        val currentQuery = _query.value
        if (currentQuery.isBlank()) return

        scope.launch {
            _isSearching.value = true
            try {
                val result = YouTube.search(currentQuery, YouTube.SearchFilter.FILTER_SONG).getOrNull()
                _results.value = result?.items?.filterIsInstance<SongItem>() ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSearching.value = false
            }
        }
    }

    suspend fun resolveStreamUrl(songId: String): String? {
        val response = YouTube.player(songId, client = YouTubeClient.WEB).getOrNull()
        return response?.streamingData?.adaptiveFormats
            ?.firstOrNull { it.mimeType.contains("audio/mp4") || it.mimeType.contains("audio/webm") }
            ?.url
    }

    fun mapToTrack(song: SongItem): Track {
        return Track(
            id = song.id,
            title = song.title,
            artists = song.artists.map { Artist(it.id, it.name) },
            durationSeconds = song.duration ?: 0,
            thumbnailUrl = song.thumbnail
        )
    }
}
