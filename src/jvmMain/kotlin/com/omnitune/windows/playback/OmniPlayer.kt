package com.omnitune.windows.playback

import com.omnitune.windows.models.Track
import kotlinx.coroutines.flow.StateFlow

enum class RepeatMode {
    OFF, ALL, ONE
}

enum class PlayerInitializationState {
    UNINITIALIZED, INITIALIZING, READY, UNAVAILABLE, ERROR, DISPOSED
}

enum class PlaybackState {
    IDLE, PLAYING, PAUSED, BUFFERING, ERROR
}

interface OmniPlayer {
    val initializationState: StateFlow<PlayerInitializationState>
    val playbackState: StateFlow<PlaybackState>
    val currentTrack: StateFlow<Track?>
    val positionMs: StateFlow<Long>
    val durationMs: StateFlow<Long>

    suspend fun play(track: Track, streamUrl: String)
    fun pause()
    fun resume()
    fun seekTo(positionMs: Long)
    fun setShuffle(enabled: Boolean)
    fun setRepeatMode(mode: RepeatMode)
    fun setVolume(volume: Float)
    fun dispose()
}
