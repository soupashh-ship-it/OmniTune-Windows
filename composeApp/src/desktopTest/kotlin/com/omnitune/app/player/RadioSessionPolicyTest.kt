package com.omnitune.app.player

import com.omnitune.innertube.models.Artist
import com.omnitune.innertube.models.SongItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RadioSessionPolicyTest {
    @Test
    fun initialQueueDedupesAndExcludesCurrentSeedWhenAlternativesExist() {
        val queue = RadioSessionPolicy.initialQueue(
            seedId = "seed",
            incoming = listOf(song("seed"), song("a"), song("a"), song("b")),
        )

        assertEquals(listOf("a", "b"), queue.map { it.id })
    }

    @Test
    fun initialQueueKeepsOnlySeedWhenProviderReturnsNoAlternatives() {
        val queue = RadioSessionPolicy.initialQueue(
            seedId = "seed",
            incoming = listOf(song("seed")),
        )

        assertEquals(listOf("seed"), queue.map { it.id })
    }

    @Test
    fun continuationStartsOnlyNearEndAndOnlyWhenNotAlreadyInFlight() {
        assertFalse(RadioSessionPolicy.shouldRequestContinuation(queueSize = 50, currentIndex = 10, inFlight = false, consecutiveFailures = 0))
        assertTrue(RadioSessionPolicy.shouldRequestContinuation(queueSize = 50, currentIndex = 44, inFlight = false, consecutiveFailures = 0))
        assertFalse(RadioSessionPolicy.shouldRequestContinuation(queueSize = 50, currentIndex = 44, inFlight = true, consecutiveFailures = 0))
        assertFalse(RadioSessionPolicy.shouldRequestContinuation(queueSize = 50, currentIndex = 44, inFlight = false, consecutiveFailures = RadioSessionPolicy.MaxContinuationFailures))
    }

    @Test
    fun appendContinuationDedupesSuppressesRecentRepeatsAndRespectsBound() {
        val existing = (0 until 78).map { song("s$it") }
        val appended = RadioSessionPolicy.appendContinuation(
            existing = existing,
            incoming = listOf(song("s76"), song("s77"), song("n1"), song("n2"), song("n3")),
            currentIndex = 77,
        )

        assertEquals(RadioSessionPolicy.MaxQueueSize, appended.size)
        assertEquals(listOf("n1", "n2"), appended.takeLast(2).map { it.id })
    }

    @Test
    fun allDuplicateContinuationLeavesExistingQueueUnchanged() {
        val existing = listOf(song("a"), song("b"), song("c"))
        val appended = RadioSessionPolicy.appendContinuation(
            existing = existing,
            incoming = listOf(song("a"), song("b")),
            currentIndex = 2,
        )

        assertEquals(existing.map { it.id }, appended.map { it.id })
    }

    private fun song(id: String): SongItem =
        SongItem(
            id = id,
            title = "Song $id",
            artists = listOf(Artist("Artist", "artist")),
            thumbnail = "",
        )
}
