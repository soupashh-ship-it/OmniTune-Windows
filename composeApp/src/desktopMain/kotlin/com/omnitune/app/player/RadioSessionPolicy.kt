package com.omnitune.app.player

import com.omnitune.innertube.models.SongItem

internal object RadioSessionPolicy {
    const val InitialQueueLimit: Int = 50
    const val MaxQueueSize: Int = 80
    const val ContinuationThreshold: Int = 6
    const val RecentRepeatWindow: Int = 6
    const val MaxContinuationFailures: Int = 3

    fun stableTrackKey(song: SongItem): String =
        song.id.ifBlank {
            "${song.title.trim().lowercase()}|${song.artists.joinToString("|") { it.name.trim().lowercase() }}"
        }

    fun initialQueue(seedId: String?, incoming: List<SongItem>): List<SongItem> {
        val deduped = incoming.distinctBy(::stableTrackKey)
        if (deduped.size <= 1) return deduped.take(InitialQueueLimit)

        val seedKey = seedId?.trim()?.takeIf { it.isNotEmpty() }
        return deduped
            .filterNot { seedKey != null && stableTrackKey(it) == seedKey }
            .ifEmpty { deduped }
            .take(InitialQueueLimit)
    }

    fun shouldRequestContinuation(
        queueSize: Int,
        currentIndex: Int,
        inFlight: Boolean,
        consecutiveFailures: Int,
    ): Boolean {
        if (queueSize <= 0 || currentIndex < 0) return false
        if (inFlight) return false
        if (consecutiveFailures >= MaxContinuationFailures) return false
        val remaining = queueSize - currentIndex - 1
        return remaining <= ContinuationThreshold
    }

    fun appendContinuation(
        existing: List<SongItem>,
        incoming: List<SongItem>,
        currentIndex: Int,
    ): List<SongItem> {
        if (incoming.isEmpty()) return existing

        val existingKeys = existing.mapTo(mutableSetOf(), ::stableTrackKey)
        val recentKeys = existing
            .drop((currentIndex - RecentRepeatWindow + 1).coerceAtLeast(0))
            .take(RecentRepeatWindow)
            .mapTo(mutableSetOf(), ::stableTrackKey)

        val appendable = incoming
            .distinctBy(::stableTrackKey)
            .filterNot { stableTrackKey(it) in existingKeys }
            .filterNot { stableTrackKey(it) in recentKeys }

        if (appendable.isEmpty()) return existing

        val appendCapacity = (MaxQueueSize - existing.size).coerceAtLeast(0)
        if (appendCapacity == 0) return existing
        return existing + appendable.take(appendCapacity)
    }
}
