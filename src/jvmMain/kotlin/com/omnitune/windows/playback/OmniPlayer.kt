package com.omnitune.windows.playback

import com.omnitune.windows.models.Track
import kotlinx.coroutines.flow.StateFlow

enum class PlaybackState {
    IDLE, PLAYING, PAUSED, BUFFERING, ERROR
}

enum class RepeatMode {
    OFF, ALL, ONE
}

interface OmniPlayer {
    val playbackState: StateFlow<PlaybackState>
    val currentTrack: StateFlow<Track?>
    val positionMs: StateFlow<Long>
    val durationMs: StateFlow<Long>
    val queue: StateFlow<List<Track>>

    suspend fun play(track: Track, streamUrl: String)
    fun pause()
    fun resume()
    fun seekTo(positionMs: Long)
    fun playNext()
    fun playPrevious()
    fun setQueue(tracks: List<Track>, startIndex: Int)
    fun setShuffle(enabled: Boolean)
    fun setRepeatMode(mode: RepeatMode)
    fun setVolume(volume: Float)
    fun dispose()
}
