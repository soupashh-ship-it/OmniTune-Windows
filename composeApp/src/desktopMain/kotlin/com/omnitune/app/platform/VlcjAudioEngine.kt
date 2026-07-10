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
    }

    private fun startPositionPoller() {
        pollJob?.cancel()
        pollJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                val state = _playbackState.value
                if (state == PlaybackState.PLAYING || state == PlaybackState.PAUSED) {
                    val status = player.status()
                    _position.value = PlayerPosition(
                        timeMs = status.time(),
                        lengthMs = status.length(),
                        position = status.position()
                    )
                }
                delay(100)
            }
        }
    }

    private fun setupEventListeners() {
        player.events().addMediaPlayerEventListener(object : MediaPlayerEventAdapter() {
            override fun playing(mp: MediaPlayer) {
                _playbackState.value = PlaybackState.PLAYING
                mp.submit { mp.audio().setVolume(100) }
            }

            override fun paused(mp: MediaPlayer) {
                _playbackState.value = PlaybackState.PAUSED
            }

            override fun stopped(mp: MediaPlayer) {
                _playbackState.value = PlaybackState.STOPPED
            }

            override fun finished(mp: MediaPlayer) {
                _playbackState.value = PlaybackState.STOPPED
                onTrackFinished?.invoke()
            }

            override fun error(mp: MediaPlayer) {
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
        scope.launch(vlcDispatcher) {
            _error.value = null
            player.media().play(url)
        }
    }

    fun pause() {
        scope.launch(vlcDispatcher) {
            player.controls().pause()
        }
    }

    fun resume() {
        scope.launch(vlcDispatcher) {
            player.controls().play()
        }
    }

    fun stop() {
        scope.launch(vlcDispatcher) {
            player.controls().stop()
            _playbackState.value = PlaybackState.STOPPED
        }
    }

    fun seek(timeMs: Long) {
        scope.launch(vlcDispatcher) {
            player.controls().setTime(timeMs.coerceAtLeast(0))
        }
    }

    fun seekRelative(deltaMs: Long) {
        scope.launch(vlcDispatcher) {
            val current = player.status().time()
            if (current >= 0) {
                player.controls().setTime((current + deltaMs).coerceAtLeast(0))
            }
        }
    }

    fun setVolume(vol: Int) {
        scope.launch(vlcDispatcher) {
            player.audio().setVolume(vol.coerceIn(0, 200))
        }
    }

    fun setRate(rate: Float) {
        scope.launch(vlcDispatcher) {
            player.controls().setRate(rate)
        }
    }

    fun release() {
        scope.launch(vlcDispatcher) {
            player.release()
            factory.release()
        }
    }
}
