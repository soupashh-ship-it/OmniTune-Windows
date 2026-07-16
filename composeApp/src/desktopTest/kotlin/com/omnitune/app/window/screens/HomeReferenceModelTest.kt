package com.omnitune.app.window.screens

import com.omnitune.innertube.models.Album
import com.omnitune.innertube.models.Artist
import com.omnitune.innertube.models.PlaylistItem
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.pages.HomePage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.time.LocalTime

class HomeReferenceModelTest {
    @Test
    fun buildsStableHomeReferenceModelWithQueuePriorityAndDedupedSections() {
        val artist = Artist("Artist", "artist-id")
        val queueSong = song("queue-song", "Queue Song", artist)
        val quickSong = song("quick-song", "Quick Song", artist)
        val duplicateQuickSong = quickSong.copy(title = "Quick Song")
        val playlist = PlaylistItem(
            id = "playlist-id",
            title = "Night Playlist",
            author = artist,
            songCountText = "12 songs",
            thumbnail = "https://example.com/playlist.jpg",
            playEndpoint = null,
            shuffleEndpoint = null,
            radioEndpoint = null,
        )

        val home = HomePage(
            chips = null,
            sections = listOf(
                HomePage.Section("Quick picks", null, null, null, listOf(quickSong, duplicateQuickSong)),
                HomePage.Section("New releases", null, null, null, listOf(playlist)),
                HomePage.Section("Trending now", null, null, null, listOf(queueSong, quickSong)),
            ),
        )

        val model = buildHomeReferenceModel(
            home = home,
            queueItems = listOf(queueSong),
            heroIndex = 0,
            currentTime = LocalTime.of(8, 0),
        )

        assertEquals("Good morning", model.greeting)
        assertEquals(playlist, model.featuredItem)
        assertEquals(1, model.heroCandidateCount)
        assertEquals(queueSong, model.continueListening.first())
        assertTrue(model.quickPicks.distinctBy { "${it.id}:${it.title}" }.size == model.quickPicks.size)
    }

    private fun song(id: String, title: String, artist: Artist) = SongItem(
        id = id,
        title = title,
        artists = listOf(artist),
        album = Album("Album", "album-id"),
        duration = 180,
        thumbnail = "https://example.com/$id.jpg",
    )
}
