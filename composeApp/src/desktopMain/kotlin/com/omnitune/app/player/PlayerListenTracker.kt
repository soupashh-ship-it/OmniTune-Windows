package com.omnitune.app.player

import com.omnitune.app.platform.PlaybackHistoryEntry
import com.omnitune.app.platform.PlaybackSession
import com.omnitune.app.platform.PlaybackState
import com.omnitune.app.platform.SettingsRepository
import com.omnitune.innertube.models.SongItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class PlayerListenTracker(
    scope: CoroutineScope,
    private val settings: SettingsRepository,
    playbackState: StateFlow<PlaybackState>,
    private val onPlaybackRecovered: () -> Unit,
    private val onPlaybackError: () -> Unit,
) {
    private data class ActiveListen(
        val song: SongItem,
        val startedAt: Long,
        val durationMs: Long,
        val accumulatedPlayedMs: Long = 0L,
        val lastResumeAt: Long? = null,
    ) {
        fun accumulateUntil(now: Long): ActiveListen {
            val resumedAt = lastResumeAt ?: return this
            return copy(
                accumulatedPlayedMs = accumulatedPlayedMs + (now - resumedAt).coerceAtLeast(0L),
                lastResumeAt = null,
            )
        }
    }

    private val _playbackHistory = MutableStateFlow(settings.playbackHistory)
    val playbackHistory: StateFlow<List<PlaybackHistoryEntry>> = _playbackHistory.asStateFlow()

    private val _playbackSessions = MutableStateFlow(settings.playbackSessions)
    val playbackSessions: StateFlow<List<PlaybackSession>> = _playbackSessions.asStateFlow()

    private var activeListen: ActiveListen? = null

    init {
        scope.launch {
            playbackState.collect { state ->
                val listen = activeListen ?: return@collect
                val now = System.currentTimeMillis()
                when (state) {
                    PlaybackState.PLAYING -> {
                        onPlaybackRecovered()
                        activeListen = if (listen.lastResumeAt == null) listen.copy(lastResumeAt = now) else listen
                    }
                    PlaybackState.PAUSED, PlaybackState.BUFFERING, PlaybackState.STOPPED -> {
                        activeListen = listen.accumulateUntil(now)
                    }
                    PlaybackState.ERROR, PlaybackState.IDLE -> {
                        activeListen = listen.accumulateUntil(now)
                        finalize(completed = false)
                        if (state == PlaybackState.ERROR) {
                            onPlaybackError()
                        }
                    }
                }
            }
        }
    }

    fun start(song: SongItem, durationMs: Long) {
        finalize(completed = false)
        activeListen = ActiveListen(
            song = song,
            startedAt = System.currentTimeMillis(),
            durationMs = durationMs.coerceAtLeast(0L),
        )
    }

    fun finalize(completed: Boolean) {
        val listen = activeListen?.accumulateUntil(System.currentTimeMillis()) ?: return
        activeListen = null
        val persisted = settings.recordMeaningfulPlayback(
            song = listen.song,
            startedAt = listen.startedAt,
            accumulatedPlayedMs = listen.accumulatedPlayedMs,
            trackDurationMs = listen.durationMs,
            completed = completed,
        )
        if (persisted != null) {
            _playbackHistory.value = settings.playbackHistory
            _playbackSessions.value = settings.playbackSessions
        }
    }
}
