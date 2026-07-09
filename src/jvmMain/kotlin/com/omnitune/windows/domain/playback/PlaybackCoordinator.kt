package com.omnitune.windows.domain.playback

import com.omnitune.windows.models.Track
import com.omnitune.windows.playback.OmniPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class PlaybackCoordinator(
    private val scope: CoroutineScope,
    val player: OmniPlayer,
    private val streamResolver: StreamResolver = StreamResolver(),
    val queueManager: QueueManager = QueueManager()
) {
    fun play(track: Track) {
        queueManager.setQueue(listOf(track), 0)
        startPlayback(track)
    }

    fun playQueue(tracks: List<Track>, startIndex: Int) {
        queueManager.setQueue(tracks, startIndex)
        val targetTrack = tracks.getOrNull(startIndex)
        if (targetTrack != null) {
            startPlayback(targetTrack)
        }
    }

    fun next() {
        val nextTrack = queueManager.next()
        if (nextTrack != null) {
            startPlayback(nextTrack)
        }
    }

    fun previous() {
        // Implement 5-second threshold later, for now just go back
        val prevTrack = queueManager.previous()
        if (prevTrack != null) {
            startPlayback(prevTrack)
        }
    }

    private fun startPlayback(track: Track) {
        scope.launch {
            val url = streamResolver.resolveStreamUrl(track.id)
            if (url != null) {
                player.play(track, url)
            }
        }
    }
}
