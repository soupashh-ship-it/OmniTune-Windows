package com.omnitune.app.platform

import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

enum class PlaybackState {
    IDLE, BUFFERING, PLAYING, PAUSED, STOPPED, ERROR
}

data class PlayerPosition(
    val timeMs: Long = 0,
    val lengthMs: Long = 0,
    val position: Float = 0f
)

internal fun clampSeekTarget(targetMs: Long, lengthMs: Long): Long {
    val lowerBounded = targetMs.coerceAtLeast(0L)
    return if (lengthMs > 0L) lowerBounded.coerceAtMost(lengthMs) else lowerBounded
}

class VlcjAudioEngine(
    private val scope: CoroutineScope
) {
    private val factory: MediaPlayerFactory
    private val player: MediaPlayer

    private val vlcExecutor: ExecutorService = Executors.newSingleThreadExecutor { r ->
        Thread(r, "vlc-dispatcher").also { it.isDaemon = true }
    }
    private val vlcDispatcher = vlcExecutor.asCoroutineDispatcher()

    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _position = MutableStateFlow(PlayerPosition())
    val position: StateFlow<PlayerPosition> = _position.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    var onTrackFinished: (() -> Unit)? = null

    private var pollJob: Job? = null
    private var volumeJob: Job? = null
    private var seekJob: Job? = null
    @Volatile
    private var desiredVolume: Int = 100
    private val releaseCoordinator = ReleaseCoordinator()
    @Volatile
    private var isReleased = false
    private var isErrorState = false
    private val shutdownHook = Thread {
        releaseBlocking()
    }

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
        
        Runtime.getRuntime().addShutdownHook(shutdownHook)
    }

    private fun releaseSync() {
        if (!releaseCoordinator.begin()) return
        isReleased = true
        try {
            pollJob?.cancel()
            volumeJob?.cancel()
            seekJob?.cancel()
            onTrackFinished = null
            runCatching { player.controls().stop() }
            player.release()
            factory.release()
            _playbackState.value = PlaybackState.STOPPED
        } catch (e: Exception) {
            OmniLogger.error("VlcjAudioEngine", "Failed to release VLC resources cleanly.", e)
        } finally {
            releaseCoordinator.complete()
            runCatching { Runtime.getRuntime().removeShutdownHook(shutdownHook) }
            vlcDispatcher.close()
        }
    }

    private fun startPositionPoller() {
        pollJob?.cancel()
        pollJob = scope.launch(Dispatchers.Default) {
            while (isActive && !releaseCoordinator.releaseRequested) {
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
        if (releaseCoordinator.releaseRequested) return
        scope.launch(vlcDispatcher) {
            if (releaseCoordinator.releaseRequested) return@launch
            isErrorState = false
            _error.value = null
            seekJob?.cancel()
            seekJob = null
            player.audio().setVolume(desiredVolume.coerceIn(0, 200))
            player.media().play(url)
        }
    }

    fun pause() {
        if (releaseCoordinator.releaseRequested) return
        scope.launch(vlcDispatcher) {
            if (releaseCoordinator.releaseRequested) return@launch
            player.controls().pause()
        }
    }

    fun resume() {
        if (releaseCoordinator.releaseRequested) return
        scope.launch(vlcDispatcher) {
            if (releaseCoordinator.releaseRequested) return@launch
            player.controls().play()
        }
    }

    fun stop() {
        if (releaseCoordinator.releaseRequested) return
        scope.launch(vlcDispatcher) {
            if (releaseCoordinator.releaseRequested) return@launch
            player.controls().stop()
            _playbackState.value = PlaybackState.STOPPED
        }
    }

    fun seek(timeMs: Long) {
        if (releaseCoordinator.releaseRequested) return
        seekJob?.cancel()
        seekJob = scope.launch(vlcDispatcher) {
            if (releaseCoordinator.releaseRequested) return@launch
            player.controls().setTime(clampSeekTarget(timeMs, player.status().length()))
        }
    }

    fun seekRelative(deltaMs: Long) {
        if (releaseCoordinator.releaseRequested) return
        scope.launch(vlcDispatcher) {
            if (releaseCoordinator.releaseRequested) return@launch
            val current = player.status().time()
            if (current >= 0) {
                player.controls().setTime(clampSeekTarget(current + deltaMs, player.status().length()))
            }
        }
    }

    fun setVolume(vol: Int) {
        if (releaseCoordinator.releaseRequested) return
        desiredVolume = vol.coerceIn(0, 200)
        volumeJob?.cancel()
        volumeJob = scope.launch(vlcDispatcher) {
            if (releaseCoordinator.releaseRequested) return@launch
            player.audio().setVolume(desiredVolume)
        }
    }

    fun setRate(rate: Float) {
        if (releaseCoordinator.releaseRequested) return
        scope.launch(vlcDispatcher) {
            if (releaseCoordinator.releaseRequested) return@launch
            player.controls().setRate(rate)
        }
    }

    fun release() {
        if (releaseCoordinator.releaseRequested) return
        scope.launch(vlcDispatcher) {
            releaseSync()
        }
    }

    fun releaseBlocking(timeoutMs: Long = 3_000L): Boolean {
        if (releaseCoordinator.releaseCompleted) return true
        if (releaseCoordinator.releaseRequested) return releaseCoordinator.waitForCompletion(timeoutMs)
        val future = try {
            vlcExecutor.submit<Boolean> {
                releaseSync()
                true
            }
        } catch (e: Exception) {
            OmniLogger.error("VlcjAudioEngine", "Could not schedule VLC release.", e)
            return releaseCoordinator.releaseCompleted
        }
        return try {
            future.get(timeoutMs.coerceAtLeast(1L), TimeUnit.MILLISECONDS)
        } catch (e: TimeoutException) {
            OmniLogger.error("VlcjAudioEngine", "Timed out while releasing VLC resources.", e)
            future.cancel(true)
            false
        } catch (e: Exception) {
            OmniLogger.error("VlcjAudioEngine", "Failed while releasing VLC resources.", e)
            false
        }
    }
}

internal class ReleaseCoordinator {
    private val lock = Any()
    private val completedLatch = CountDownLatch(1)

    @Volatile
    var releaseRequested: Boolean = false
        private set

    @Volatile
    var releaseCompleted: Boolean = false
        private set

    fun begin(): Boolean = synchronized(lock) {
        if (releaseRequested || releaseCompleted) {
            false
        } else {
            releaseRequested = true
            true
        }
    }

    fun complete() {
        synchronized(lock) {
            releaseRequested = true
            releaseCompleted = true
            completedLatch.countDown()
        }
    }

    fun waitForCompletion(timeoutMs: Long): Boolean {
        if (releaseCompleted) return true
        completedLatch.await(timeoutMs.coerceAtLeast(1L), TimeUnit.MILLISECONDS)
        return releaseCompleted
    }
}
