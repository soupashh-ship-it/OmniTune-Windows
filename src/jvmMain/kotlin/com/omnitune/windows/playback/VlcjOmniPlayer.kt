package com.omnitune.windows.playback

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

    private val audioPlayerComponent = AudioPlayerComponent()
    private val mediaPlayer = audioPlayerComponent.mediaPlayer()

    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentTrack = MutableStateFlow<Track?>(null)
    override val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _positionMs = MutableStateFlow(0L)
    override val positionMs: StateFlow<Long> = _positionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    override val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    override val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        mediaPlayer.events().addMediaPlayerEventListener(object : MediaPlayerEventAdapter() {
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
                playNext()
            }

            override fun error(mediaPlayer: MediaPlayer) {
                _playbackState.value = PlaybackState.ERROR
                stopProgressUpdates()
            }
        })
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                _positionMs.value = mediaPlayer.status().time()
                delay(200)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
    }

    override suspend fun play(track: Track, streamUrl: String) {
        _currentTrack.value = track
        _playbackState.value = PlaybackState.BUFFERING
        mediaPlayer.media().play(streamUrl)
    }

    override fun pause() {
        mediaPlayer.controls().pause()
    }

    override fun resume() {
        mediaPlayer.controls().play()
    }

    override fun seekTo(positionMs: Long) {
        mediaPlayer.controls().setTime(positionMs)
        _positionMs.value = positionMs
    }

    override fun playNext() {
        val current = _queue.value
        val track = _currentTrack.value
        if (current.isNotEmpty() && track != null) {
            val currentIndex = current.indexOf(track)
            if (currentIndex in 0 until current.lastIndex) {
                val nextTrack = current[currentIndex + 1]
                // We don't have streamURL here since we skip resolving it for this stub
                _currentTrack.value = nextTrack
            }
        }
    }

    override fun playPrevious() {
        val current = _queue.value
        val track = _currentTrack.value
        if (current.isNotEmpty() && track != null) {
            val currentIndex = current.indexOf(track)
            if (currentIndex > 0) {
                val prevTrack = current[currentIndex - 1]
                _currentTrack.value = prevTrack
            }
        }
    }

    override fun setQueue(tracks: List<Track>, startIndex: Int) {
        _queue.value = tracks
    }

    override fun setShuffle(enabled: Boolean) {}
    override fun setRepeatMode(mode: RepeatMode) {}

    override fun setVolume(volume: Float) {
        mediaPlayer.audio().setVolume((volume * 100).toInt())
    }

    override fun dispose() {
        stopProgressUpdates()
        mediaPlayer.release()
        audioPlayerComponent.release()
    }
}