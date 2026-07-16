package com.omnitune.app.player

import com.omnitune.app.platform.OmniLogger
import com.omnitune.app.service.YouTubeService
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.models.WatchEndpoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class PlayerRadioController(
    private val scope: CoroutineScope,
    private val youTubeService: YouTubeService,
    private val queueController: PlayerQueueController,
    private val setCurrentSong: (SongItem) -> Unit,
    private val startPlayback: suspend (SongItem) -> Unit,
) {
    private var activeRadioSessionId: Long = 0L
    private var activeRadioEndpoint: WatchEndpoint? = null
    private var activeRadioContinuation: String? = null
    private var radioContinuationJob: Job? = null
    private var radioContinuationFailures: Int = 0

    fun startRadio(seedId: String, type: String) {
        val playlistId = when (type) {
            "song" -> "RDAMVM$seedId"
            "artist" -> "RDEM$seedId"
            "album" -> "RDAMPL$seedId"
            else -> "RDAMVM$seedId"
        }
        val sessionId = beginRadioSession()
        scope.launch {
            runCatching {
                val result = youTubeService.next(WatchEndpoint(videoId = seedId, playlistId = playlistId))
                val songs = RadioSessionPolicy.initialQueue(seedId, result.items)
                if (songs.isNotEmpty()) {
                    if (sessionId != activeRadioSessionId) return@runCatching
                    activeRadioEndpoint = result.endpoint
                    activeRadioContinuation = result.continuation
                    radioContinuationFailures = 0
                    val first = queueController.setQueue(songs) ?: return@runCatching
                    setCurrentSong(first)
                    startPlayback(first)
                    maybeRequestContinuation()
                }
            }.onFailure {
                if (sessionId != activeRadioSessionId) return@onFailure
                activeRadioContinuation = null
                radioContinuationFailures = RadioSessionPolicy.MaxContinuationFailures
                OmniLogger.error("PlayerVM", "Failed to start radio: ${it.message}", it)
            }
        }
    }

    fun startRadio(endpoint: WatchEndpoint) {
        val sessionId = beginRadioSession()
        scope.launch {
            runCatching {
                val result = youTubeService.next(endpoint)
                RadioSessionPolicy.initialQueue(endpoint.videoId, result.items) to result
            }.onSuccess { (songs, result) ->
                if (sessionId != activeRadioSessionId) return@onSuccess
                if (songs.isNotEmpty()) {
                    activeRadioEndpoint = result.endpoint
                    activeRadioContinuation = result.continuation
                    radioContinuationFailures = 0
                    val first = queueController.setQueue(songs) ?: return@onSuccess
                    setCurrentSong(first)
                    startPlayback(first)
                    maybeRequestContinuation()
                }
            }.onFailure {
                if (sessionId != activeRadioSessionId) return@onFailure
                activeRadioContinuation = null
                radioContinuationFailures = RadioSessionPolicy.MaxContinuationFailures
                OmniLogger.error("PlayerVM", "Failed to start endpoint radio: ${it.message}", it)
            }
        }
    }

    fun cancelSession() {
        activeRadioSessionId += 1
        radioContinuationJob?.cancel()
        radioContinuationJob = null
        activeRadioEndpoint = null
        activeRadioContinuation = null
        radioContinuationFailures = 0
    }

    fun maybeRequestContinuation() {
        val endpoint = activeRadioEndpoint ?: return
        val continuation = activeRadioContinuation ?: return
        val sessionId = activeRadioSessionId
        val inFlight = radioContinuationJob?.isActive == true
        if (!RadioSessionPolicy.shouldRequestContinuation(
                queueController.queueItems.value.size,
                queueController.queueIndexState.value,
                inFlight,
                radioContinuationFailures,
            )
        ) return

        radioContinuationJob = scope.launch {
            runCatching { youTubeService.next(endpoint, continuation) }
                .onSuccess { result ->
                    if (sessionId != activeRadioSessionId) return@onSuccess
                    val beforeSize = queueController.queueItems.value.size
                    val nextQueue = RadioSessionPolicy.appendContinuation(
                        queueController.queueItems.value,
                        result.items,
                        queueController.queueIndexState.value,
                    )
                    if (nextQueue.size > beforeSize) {
                        queueController.queueItems.value = nextQueue
                        activeRadioEndpoint = result.endpoint
                        activeRadioContinuation = result.continuation
                        radioContinuationFailures = 0
                    } else {
                        activeRadioContinuation = result.continuation ?: activeRadioContinuation
                        radioContinuationFailures++
                    }
                }.onFailure { throwable ->
                    if (sessionId != activeRadioSessionId) return@onFailure
                    radioContinuationFailures++
                    OmniLogger.error("PlayerVM", "Radio continuation failed: ${throwable.message}", throwable)
                }
        }
    }

    private fun beginRadioSession(): Long {
        cancelSession()
        return activeRadioSessionId
    }
}
