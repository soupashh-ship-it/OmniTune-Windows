package com.omnitune.app.player

import com.omnitune.innertube.models.Artist
import com.omnitune.innertube.models.SongItem
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlaybackRequestGateTest {
    @Test
    fun olderRequestCannotApplyAfterNewerTrackSelection() {
        val gate = PlaybackRequestGate()
        val first = song("first")
        val second = song("second")

        val firstToken = gate.nextToken()
        val secondToken = gate.nextToken()

        assertFalse(gate.isCurrent(firstToken, first, second))
        assertTrue(gate.isCurrent(secondToken, second, second))
    }

    @Test
    fun matchingTokenCannotApplyToDifferentCurrentSong() {
        val gate = PlaybackRequestGate()
        val requested = song("requested")
        val current = song("current")

        val token = gate.nextToken()

        assertFalse(gate.isCurrent(token, requested, current))
    }

    private fun song(id: String): SongItem =
        SongItem(
            id = id,
            title = "Song $id",
            artists = listOf(Artist("Artist", null)),
            thumbnail = "",
        )
}
