package com.omnitune.app.window.screens

import com.omnitune.innertube.models.Album
import com.omnitune.innertube.models.Artist
import com.omnitune.innertube.models.SongItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlaylistDetailHelpersTest {
    @Test
    fun infersHonestPlaylistTagsFromRealMetadata() {
        val songs = listOf(
            song("s1", "Midnight Soul", artist = "Soul Artist", album = "Late Night Sessions"),
            song("s2", "Focus Ambient", artist = "Quiet Artist", album = "Chill Room"),
        )

        val tags = inferPlaylistTags("Late Night Focus", songs)

        assertTrue("Late Night" in tags)
        assertTrue("Focus" in tags)
        assertTrue("Chill" in tags)
        assertTrue("Playlist" in tags)
        assertTrue(tags.size <= 6)
    }

    @Test
    fun formatsPlaylistDurationsConsistently() {
        assertEquals("3h 24m", formatPlaylistDurationLong(12_240))
        assertEquals("42 min", formatPlaylistDurationLong(2_520))
        assertEquals("3:05", song("s3", "Track", duration = 185).playlistDurationLabel())
    }

    private fun song(
        id: String,
        title: String,
        artist: String = "Artist",
        album: String = "Album",
        duration: Int = 180,
    ) = SongItem(
        id = id,
        title = title,
        artists = listOf(Artist(artist, "$id-artist")),
        album = Album(album, "$id-album"),
        duration = duration,
        thumbnail = "https://example.com/$id.jpg",
    )
}
