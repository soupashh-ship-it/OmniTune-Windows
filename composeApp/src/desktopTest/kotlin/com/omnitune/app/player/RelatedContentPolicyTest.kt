package com.omnitune.app.player

import com.omnitune.innertube.models.Artist
import com.omnitune.innertube.models.SongItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class RelatedContentPolicyTest {
    @Test
    fun cleanExcludesCurrentTrackDedupesAndCapsResults() {
        val items = listOf(song("current"), song("a"), song("a")) +
            (0..40).map { song("n$it") }

        val cleaned = RelatedContentPolicy.clean(currentSongId = "current", items = items)

        assertFalse(cleaned.any { it.id == "current" })
        assertEquals(cleaned.map { it.id }.distinct(), cleaned.map { it.id })
        assertEquals(RelatedContentPolicy.MaxResults, cleaned.size)
        assertEquals("a", cleaned.first().id)
    }

    private fun song(id: String): SongItem =
        SongItem(
            id = id,
            title = "Song $id",
            artists = listOf(Artist("Artist", "artist")),
            thumbnail = "",
        )
}
