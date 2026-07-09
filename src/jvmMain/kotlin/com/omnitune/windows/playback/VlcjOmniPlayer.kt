package com.omnitune.windows.playback

import com.omnitune.windows.app.DependencyContainer
import com.omnitune.windows.models.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import uk.co.caprica.vlcj.player.component.AudioPlayerComponent

class VlcjOmniPlayer : OmniPlayer {

    private var audioPlayerComponent: AudioPlayerComponent? = null
    private var mediaPlayer: MediaPlayer? = null

    private val _initializationState = MutableStateFlow(PlayerInitializationState.UNINITIALIZED)
    override val initializationState: StateFlow<PlayerInitializationState> = _initializationState.asStateFlow()

    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentTrack = MutableStateFlow<Track?>(null)
    override val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _positionMs = MutableStateFlow(0L)
    override val positionMs: StateFlow<Long> = _positionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    override val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        _initializationState.value = PlayerInitializationState.INITIALIZING
        try {
            audioPlayerComponent = AudioPlayerComponent()
            mediaPlayer = audioPlayerComponent?.mediaPlayer()

            mediaPlayer?.events()?.addMediaPlayerEventListener(object : MediaPlayerEventAdapter() {
                override fun playing(mediaPlayer: MediaPlayer) {
                    _playbackState.value = PlaybackState.PLAYING
                    _durationMs.value = mediaPlayer.status().length()
                    startProgressUpdates()
                }

                override fun paused(mediaPlayer: MediaPlayer) {
                    _playbackState.value = PlaybackState.PAUSED
                    stopProgressUpdates()
                }

                override fun stopped(mediaPlayer: MediaPlayer) {
                    _playbackState.value = PlaybackState.IDLE
                    _positionMs.value = 0L
                    stopProgressUpdates()
                }

                override fun finished(mediaPlayer: MediaPlayer) {
                    _playbackState.value = PlaybackState.IDLE
                    _positionMs.value = 0L
                    stopProgressUpdates()
                    // Auto advance
                    DependencyContainer.playbackCoordinator.next()
                }

                override fun error(mediaPlayer: MediaPlayer) {
                    _playbackState.value = PlaybackState.ERROR
                    stopProgressUpdates()
                }
            })
            _initializationState.value = PlayerInitializationState.READY
        } catch (e: Throwable) {
            e.printStackTrace()
            _initializationState.value = PlayerInitializationState.UNAVAILABLE
            _playbackState.value = PlaybackState.ERROR
        }
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                _positionMs.value = mediaPlayer?.status()?.time() ?: 0L
                delay(200)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
    }

    override suspend fun play(track: Track, streamUrl: String) {
        if (initializationState.value != PlayerInitializationState.READY) return
        _currentTrack.value = track
        _playbackState.value = PlaybackState.BUFFERING
        mediaPlayer?.media()?.play(streamUrl)
    }

    override fun pause() {
        mediaPlayer?.controls()?.pause()
    }

    override fun resume() {
        mediaPlayer?.controls()?.play()
    }

    override fun seekTo(positionMs: Long) {
        mediaPlayer?.controls()?.setTime(positionMs)
        _positionMs.value = positionMs
    }

    override fun setShuffle(enabled: Boolean) {}
    override fun setRepeatMode(mode: RepeatMode) {}

    override fun setVolume(volume: Float) {
        mediaPlayer?.audio()?.setVolume((volume * 100).toInt())
    }

    override fun dispose() {
        stopProgressUpdates()
        _initializationState.value = PlayerInitializationState.DISPOSED
        mediaPlayer?.release()
        audioPlayerComponent?.release()
    }
}