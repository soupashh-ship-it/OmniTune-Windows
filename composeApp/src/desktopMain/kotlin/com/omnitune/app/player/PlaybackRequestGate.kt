package com.omnitune.app.player

import com.omnitune.innertube.models.SongItem
import java.util.concurrent.atomic.AtomicLong

/**
 * Guards asynchronous playback source resolution.
 *
 * Playback source resolution can finish after the user has already selected a
 * different track. The gate gives every playback request a generation token and
 * verifies both the token and current song identity before native playback or
 * recovery side effects are allowed to proceed.
 */
internal class PlaybackRequestGate {
    private val token = AtomicLong(0L)

    fun nextToken(): Long = token.incrementAndGet()

    fun isCurrent(requestToken: Long, item: SongItem, currentSong: SongItem?): Boolean {
        return requestToken == token.get() && currentSong?.id == item.id
    }
}
