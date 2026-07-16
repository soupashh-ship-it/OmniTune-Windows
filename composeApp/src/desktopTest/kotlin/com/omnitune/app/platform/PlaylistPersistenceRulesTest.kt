package com.omnitune.app.platform

import com.omnitune.innertube.models.Artist
import com.omnitune.innertube.models.SongItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PlaylistPersistenceRulesTest {
    @Test
    fun sanitizeTags_trimsDeduplicatesAndLimits() {
        val tags = listOf(" Chill ", "chill", "", "Focus", "Late", "Night", "Soul", "Drive", "Work", "Extra")

        assertEquals(
            listOf("Chill", "Focus", "Late", "Night", "Soul", "Drive", "Work", "Extra"),
            PlaylistPersistenceRules.sanitizeTags(tags),
        )
    }

    @Test
    fun addSongToSelectedPlaylists_skipsDuplicatesAndReportsOnlyChangedPlaylists() {
        val song = song("a")
        val playlistWithSong = playlist("one", song)
        val emptyPlaylist = playlist("two")

        val (next, changed) = PlaylistPersistenceRules.addSongToSelectedPlaylists(
            playlists = listOf(playlistWithSong, emptyPlaylist),
            song = song,
            selectedIds = setOf("one", "two"),
        )

        assertEquals(listOf("two"), changed.map { it.id })
        assertEquals(1, next.first { it.id == "one" }.songs.size)
        assertEquals(1, next.first { it.id == "two" }.songs.size)
    }

    @Test
    fun moveSong_validatesBoundsAndPreservesOrder() {
        val playlist = playlist("p", song("a"), song("b"), song("c"))

        assertEquals(listOf("b", "c", "a"), PlaylistPersistenceRules.moveSong(playlist, 0, 2).songs.map { it.id })
        assertFailsWith<IllegalArgumentException> { PlaylistPersistenceRules.moveSong(playlist, 0, 4) }
    }

    private fun playlist(id: String, vararg songs: SongItem): SavedQueuePlaylist =
        SavedQueuePlaylist(
            id = id,
            name = id,
            createdAt = 1L,
            songs = songs.toList(),
        )

    private fun song(id: String): SongItem =
        SongItem(
            id = id,
            title = "Song $id",
            artists = listOf(Artist("Artist", null)),
            thumbnail = "",
        )
}
