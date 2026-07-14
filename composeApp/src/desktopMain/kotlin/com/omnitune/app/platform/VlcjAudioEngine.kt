package com.omnitune.app.platform

import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.Executors

enum class PlaybackState {
    IDLE, BUFFERING, PLAYING, PAUSED, STOPPED, ERROR
}

data class PlayerPosition(
    val timeMs: Long = 0,
    val lengthMs: Long = 0,
    val position: Float = 0f
)

class VlcjAudioEngine(
    private val scope: CoroutineScope
) {
    private val factory: MediaPlayerFactory
    private val player: MediaPlayer

    private val vlcDispatcher = Executors.newSingleThreadExecutor { r ->
        Thread(r, "vlc-dispatcher").also { it.isDaemon = true }
    }.asCoroutineDispatcher()

    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _position = MutableStateFlow(PlayerPosition())
    val position: StateFlow<PlayerPosition> = _position.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    var onTrackFinished: (() -> Unit)? = null

    private var pollJob: Job? = null
    @Volatile
    private var isReleased = false
    private var isErrorState = false

    init {
        factory = MediaPlayerFactory(
            "--intf", "dummy",
            "--no-metadata-network-access",
            "--quiet",
            "--network-caching=2000"
        )
        player = factory.mediaPlayers().newMediaPlayer()
        setupEventListeners()
        startPositionPoller()
        
        Runtime.getRuntime().addShutdownHook(Thread {
            releaseSync()
        })
    }

    private fun releaseSync() {
        if (isReleased) return
        isReleased = true
        try {
            pollJob?.cancel()
            runCatching { player.controls().stop() }
            player.release()
            factory.release()
            _playbackState.value = PlaybackState.STOPPED
            vlcDispatcher.close()
        } catch (e: Exception) {
            OmniLogger.error("VlcjAudioEngine", "Failed to release VLC resources cleanly.", e)
        }
    }

    private fun startPositionPoller() {
        pollJob?.cancel()
        pollJob = scope.launch(Dispatchers.Default) {
            while (isActive && !isReleased) {
                val state = _playbackState.value
                if (state == PlaybackState.PLAYING || state == PlaybackState.PAUSED) {
                    runCatching {
                        val status = player.status()
                        _position.value = PlayerPosition(
                            timeMs = status.time(),
                            lengthMs = status.length(),
                            position = status.position()
                        )
                    }
                }
                delay(100)
            }
        }
    }

    private fun setupEventListeners() {
        player.events().addMediaPlayerEventListener(object : MediaPlayerEventAdapter() {
            override fun playing(mp: MediaPlayer) {
                isErrorState = false
                _playbackState.value = PlaybackState.PLAYING
            }

            override fun paused(mp: MediaPlayer) {
                _playbackState.value = PlaybackState.PAUSED
            }

            override fun stopped(mp: MediaPlayer) {
                _playbackState.value = PlaybackState.STOPPED
            }

            override fun finished(mp: MediaPlayer) {
                _playbackState.value = PlaybackState.STOPPED
                if (!isErrorState) {
                    onTrackFinished?.invoke()
                }
            }

            override fun error(mp: MediaPlayer) {
                isErrorState = true
                _playbackState.value = PlaybackState.ERROR
                _error.value = "Playback error occurred"
            }

            override fun timeChanged(mp: MediaPlayer, newTimeMs: Long) {
                _position.value = PlayerPosition(
                    timeMs = newTimeMs,
                    lengthMs = mp.status().length(),
                    position = mp.status().position()
                )
            }

            override fun lengthChanged(mp: MediaPlayer, newLengthMs: Long) {
                _position.value = _position.value.copy(lengthMs = newLengthMs)
            }

            override fun buffering(mp: MediaPlayer, cachePercent: Float) {
                if (cachePercent < 100f && _playbackState.value != PlaybackState.PLAYING) {
                    _playbackState.value = PlaybackState.BUFFERING
                }
            }
        })
    }

    fun play(url: String) {
        if (isReleased) return
        scope.launch(vlcDispatcher) {
            if (isReleased) return@launch
            isErrorState = false
            _error.value = null
            player.media().play(url)
        }
    }

    fun pause() {
        if (isReleased) return
        scope.launch(vlcDispatcher) {
            if (isReleased) return@launch
            player.controls().pause()
        }
    }

    fun resume() {
        if (isReleased) return
        scope.launch(vlcDispatcher) {
            if (isReleased) return@launch
            player.controls().play()
        }
    }

    fun stop() {
        if (isReleased) return
        scope.launch(vlcDispatcher) {
            if (isReleased) return@launch
            player.controls().stop()
            _playbackState.value = PlaybackState.STOPPED
        }
    }

    fun seek(timeMs: Long) {
        if (isReleased) return
        scope.launch(vlcDispatcher) {
            if (isReleased) return@launch
            player.controls().setTime(timeMs.coerceAtLeast(0))
        }
    }

    fun seekRelative(deltaMs: Long) {
        if (isReleased) return
        scope.launch(vlcDispatcher) {
            if (isReleased) return@launch
            val current = player.status().time()
            if (current >= 0) {
                player.controls().setTime((current + deltaMs).coerceAtLeast(0))
            }
        }
    }

    fun setVolume(vol: Int) {
        if (isReleased) return
        scope.launch(vlcDispatcher) {
            if (isReleased) return@launch
            player.audio().setVolume(vol.coerceIn(0, 200))
        }
    }

    fun setRate(rate: Float) {
        if (isReleased) return
        scope.launch(vlcDispatcher) {
            if (isReleased) return@launch
            player.controls().setRate(rate)
        }
    }

    fun release() {
        if (isReleased) return
        runBlocking {
            withContext(vlcDispatcher) {
                releaseSync()
            }
        }
    }
}
