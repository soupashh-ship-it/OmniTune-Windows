package com.omnitune.app.player

import com.omnitune.app.platform.OmniLogger
import com.omnitune.app.service.YouTubeService
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.models.WatchEndpoint
import com.omnitune.innertube.models.YTItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class PlayerRelatedController(
    private val scope: CoroutineScope,
    private val youTubeService: YouTubeService,
    private val currentSong: () -> SongItem?,
    private val currentPlaylistId: () -> String?,
) {
    private val _discoveryRelated = MutableStateFlow<List<YTItem>>(emptyList())
    val discoveryRelated: StateFlow<List<YTItem>> = _discoveryRelated.asStateFlow()

    private val _relatedLoading = MutableStateFlow(false)
    val relatedLoading: StateFlow<Boolean> = _relatedLoading.asStateFlow()

    private val _relatedError = MutableStateFlow<String?>(null)
    val relatedError: StateFlow<String?> = _relatedError.asStateFlow()

    private var relatedJob: Job? = null
    private var relatedRequestToken: Long = 0L

    fun retryRelated() {
        currentSong()?.let(::loadRelatedFor)
    }

    fun loadRelatedFor(item: SongItem) {
        val token = ++relatedRequestToken
        relatedJob?.cancel()
        _discoveryRelated.value = emptyList()
        _relatedLoading.value = true
        _relatedError.value = null
        relatedJob = scope.launch {
            runCatching {
                val nextRes = youTubeService.next(WatchEndpoint(videoId = item.id, playlistId = currentPlaylistId()))
                val relEndpoint = nextRes.relatedEndpoint ?: return@runCatching emptyList<YTItem>()
                val page = youTubeService.related(relEndpoint)
                RelatedContentPolicy.clean(
                    currentSongId = item.id,
                    items = page.songs + page.albums + page.artists + page.playlists,
                )
            }.onSuccess { related ->
                if (token != relatedRequestToken || currentSong()?.id != item.id) return@onSuccess
                _discoveryRelated.value = related
                _relatedLoading.value = false
                _relatedError.value = null
            }.onFailure { throwable ->
                if (throwable is CancellationException) return@onFailure
                if (token != relatedRequestToken || currentSong()?.id != item.id) return@onFailure
                _discoveryRelated.value = emptyList()
                _relatedLoading.value = false
                _relatedError.value = "Couldn't load related tracks."
                OmniLogger.error("PlayerVM", "Failed to load related tracks: ${throwable.message}", throwable)
            }
        }
    }
}
