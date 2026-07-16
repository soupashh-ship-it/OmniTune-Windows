package com.omnitune.app.platform

import com.omnitune.innertube.models.Album
import com.omnitune.innertube.models.Artist
import com.omnitune.innertube.models.SongItem
import kotlin.test.Test
import kotlin.test.assertEquals

class SongJsonCodecTest {
    @Test
    fun roundTrip_preservesPersistedSongFields() {
        val song = SongItem(
            id = "track-1",
            title = "Track One",
            artists = listOf(Artist("Artist One", "artist-1")),
            album = Album("Album One", "album-1"),
            duration = 215,
            thumbnail = "https://example.test/thumb.jpg",
            explicit = true,
        )

        val decoded = SongJsonCodec.fromJson(SongJsonCodec.toJson(song))

        assertEquals(song.id, decoded.id)
        assertEquals(song.title, decoded.title)
        assertEquals(song.artists, decoded.artists)
        assertEquals(song.album, decoded.album)
        assertEquals(song.duration, decoded.duration)
        assertEquals(song.thumbnail, decoded.thumbnail)
        assertEquals(song.explicit, decoded.explicit)
    }
}
