package com.omnitune.app.player

import com.omnitune.app.platform.SettingsRepository
import com.omnitune.app.service.YouTubeService
import com.omnitune.innertube.YouTube
import com.omnitune.innertube.models.YTItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class PlayerSearchController(
    private val scope: CoroutineScope,
    private val youTubeService: YouTubeService,
    private val settings: SettingsRepository,
    private val recentSearches: MutableStateFlow<List<String>>,
) {
    private val _searchResults = MutableStateFlow<List<YTItem>>(emptyList())
    val searchResults: StateFlow<List<YTItem>> = _searchResults.asStateFlow()

    private val _searchLoading = MutableStateFlow(false)
    val searchLoading: StateFlow<Boolean> = _searchLoading.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

    private val _playlistResults = MutableStateFlow<List<YTItem>>(emptyList())
    val playlistResults: StateFlow<List<YTItem>> = _playlistResults.asStateFlow()

    private val _playlistLoading = MutableStateFlow(false)
    val playlistLoading: StateFlow<Boolean> = _playlistLoading.asStateFlow()

    private val _playlistError = MutableStateFlow<String?>(null)
    val playlistError: StateFlow<String?> = _playlistError.asStateFlow()

    private var activeSearchJob: Job? = null
    private var activeSearchToken: Long = 0L
    private var activePlaylistSearchJob: Job? = null
    private var activePlaylistSearchToken: Long = 0L

    fun search(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return

        settings.addRecentSearch(trimmed)
        settings.flush()
        recentSearches.value = settings.recentSearches

        val searchToken = ++activeSearchToken
        activeSearchJob?.cancel()
        activeSearchJob = scope.launch {
            _searchLoading.value = true
            _searchError.value = null
            _searchResults.value = emptyList()
            val searchResults = runCatching {
                val songs = async { runCatching { youTubeService.search(trimmed, YouTube.SearchFilter.FILTER_SONG).items } }
                val artists = async { runCatching { youTubeService.search(trimmed, YouTube.SearchFilter.FILTER_ARTIST).items } }
                val albums = async { runCatching { youTubeService.search(trimmed, YouTube.SearchFilter.FILTER_ALBUM).items } }
                val playlists = async { runCatching { youTubeService.search(trimmed, YouTube.SearchFilter.FILTER_FEATURED_PLAYLIST).items } }
                listOf(songs.await(), artists.await(), albums.await(), playlists.await())
            }

            if (searchToken == activeSearchToken) {
                searchResults.onSuccess { categoryResults ->
                    val successes = categoryResults.mapNotNull { it.getOrNull() }
                    val failures = categoryResults.mapNotNull { it.exceptionOrNull() }
                        .filterNot { it is CancellationException }
                    val combined = successes.flatten()
                        .distinctBy { "${it::class.simpleName}:${it.id}" }

                    _searchResults.value = combined
                    if (combined.isEmpty() && failures.isNotEmpty()) {
                        _searchError.value = failures.first().message ?: "Search failed"
                    }
                }.onFailure {
                    if (it !is CancellationException) {
                        _searchError.value = it.message ?: "Search failed"
                    }
                }
                _searchLoading.value = false
            }
        }
    }

    fun searchPlaylists(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return

        val searchToken = ++activePlaylistSearchToken
        activePlaylistSearchJob?.cancel()
        activePlaylistSearchJob = scope.launch {
            _playlistLoading.value = true
            _playlistError.value = null
            _playlistResults.value = emptyList()
            runCatching {
                val result = youTubeService.search(trimmed, YouTube.SearchFilter.FILTER_FEATURED_PLAYLIST)
                result.items
            }.onSuccess {
                if (searchToken == activePlaylistSearchToken) {
                    _playlistResults.value = it.distinctBy { item -> "${item::class.simpleName}:${item.id}" }
                }
            }.onFailure {
                if (searchToken == activePlaylistSearchToken && it !is CancellationException) {
                    _playlistError.value = it.message
                }
            }
            if (searchToken == activePlaylistSearchToken) {
                _playlistLoading.value = false
            }
        }
    }
}
