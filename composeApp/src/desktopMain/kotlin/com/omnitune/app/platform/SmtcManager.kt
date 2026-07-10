package com.omnitune.app.platform

class SmtcManager(
    private val onPlay: () -> Unit,
    private val onPause: () -> Unit,
    private val onNext: () -> Unit,
    private val onPrevious: () -> Unit,
    private val onSeek: (Long) -> Unit
) {
    fun initialize() {
        // JMTC initialization - requires JavaMediaTransportControls library
        // TODO: Implement when JMTC library is available
    }

    fun updateMetadata(title: String, artist: String, album: String, thumbnailPath: String?, durationMs: Long) {
        // TODO: Update SMTC metadata
    }

    fun updatePlaybackState(playing: Boolean) {
        // TODO: Update SMTC playback state
    }

    fun updatePosition(positionMs: Long, durationMs: Long) {
        // TODO: Update SMTC timeline position
    }

    fun dispose() {
        // TODO: Cleanup SMTC
    }
}
