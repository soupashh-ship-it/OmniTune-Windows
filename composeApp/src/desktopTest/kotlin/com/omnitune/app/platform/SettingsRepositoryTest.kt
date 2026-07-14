package com.omnitune.app.platform

import com.omnitune.innertube.models.Artist
import com.omnitune.innertube.models.SongItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import java.util.UUID
import java.util.prefs.Preferences

class SettingsRepositoryTest {
    private fun isolatedRepository(block: (SettingsRepository) -> Unit) {
        val prefs = Preferences.userRoot().node("omnitune-tests/${UUID.randomUUID()}")
        try {
            block(SettingsRepository(prefs))
        } finally {
            runCatching {
                prefs.removeNode()
                prefs.flush()
            }
        }
    }

    @Test
    fun recentSearchHistoryIsMultiWordDedupedAndCapped() = isolatedRepository { repo ->
        repo.addRecentSearch("Blinding Lights")
        repo.addRecentSearch("  blinding lights  ")
        repo.addRecentSearch("After Hours")
        repeat(60) { repo.addRecentSearch("query $it") }

        val restored = SettingsRepository(repoPrefs(repo)).recentSearches

        assertEquals(50, restored.size)
        assertEquals("query 59", restored.first())
        assertFalse(restored.any { it == "Blinding Lights" && restored.any { other -> other == "blinding lights" } })
        assertTrue(restored.all { it.isNotBlank() })
    }

    @Test
    fun settingsPersistAcrossRepositoryRecreation() = isolatedRepository { repo ->
        repo.appearanceTheme = "aurora"
        repo.reduceMotionEnabled = true
        repo.miniPlayerAlwaysOnTop = false
        repo.downloadQualityMode = DownloadQualityMode.PREFER_HIGH
        repo.pinnedLibraryCollectionIds = setOf("favorites", "downloads")
        repo.flush()

        val restored = SettingsRepository(repoPrefs(repo))

        assertEquals("aurora", restored.appearanceTheme)
        assertTrue(restored.reduceMotionEnabled)
        assertFalse(restored.miniPlayerAlwaysOnTop)
        assertEquals(DownloadQualityMode.PREFER_HIGH, restored.downloadQualityMode)
        assertEquals(setOf("favorites", "downloads"), restored.pinnedLibraryCollectionIds)
    }

    @Test
    fun meaningfulPlaybackThresholdAndPersistenceWork() = isolatedRepository { repo ->
        val short = song("short")
        val meaningful = song("meaningful")

        assertFalse(repo.isMeaningfulListen(1_000L, 240_000L))
        assertTrue(repo.isMeaningfulListen(30_000L, 240_000L))
        assertEquals(null, repo.recordMeaningfulPlayback(short, 1_000L, 1_000L, 240_000L, completed = false))

        val entry = repo.recordMeaningfulPlayback(meaningful, 2_000L, 31_000L, 240_000L, completed = false)
        assertNotNull(entry)

        val restored = SettingsRepository(repoPrefs(repo))
        assertEquals(listOf("meaningful"), restored.playbackHistory.map { it.song.id })
        assertEquals(1, restored.playbackSessions.size)
        assertEquals(31_000L, restored.playbackSessions.first().totalListeningMs)
    }

    @Test
    fun pauseResumeDedupesSameSongHistory() = isolatedRepository { repo ->
        val track = song("track-a")

        repo.recordMeaningfulPlayback(track, 1_000L, 35_000L, 240_000L, completed = false)
        repo.recordMeaningfulPlayback(track, 1_000L, 40_000L, 240_000L, completed = false)

        val history = repo.playbackHistory
        assertEquals(1, history.size)
        assertEquals(2, history.first().playCount)
        assertEquals(75_000L, history.first().accumulatedPlayedMs)
    }

    @Test
    fun seekDoesNotInflateListenedDuration() = isolatedRepository { repo ->
        val track = song("seeked")

        val recorded = repo.recordMeaningfulPlayback(track, 1_000L, 5_000L, 240_000L, completed = false)

        assertEquals(null, recorded)
        assertTrue(repo.playbackHistory.isEmpty())
    }

    @Test
    fun sessionBoundaryUsesThirtyMinuteInactivity() = isolatedRepository { repo ->
        val first = song("first")
        val second = song("second")
        val now = System.currentTimeMillis()

        repo.recordMeaningfulPlayback(first, now - 40_000L, 35_000L, 240_000L, completed = false)
        repo.playbackSessions = repo.playbackSessions.map {
            it.copy(lastActivityAt = now - SettingsRepository.SESSION_TIMEOUT_MS - 1_000L)
        }
        repo.recordMeaningfulPlayback(second, now, 35_000L, 240_000L, completed = false)

        assertEquals(2, repo.playbackSessions.size)
    }

    @Test
    fun queueSaveAsPlaylistValidatesAndPreservesOrder() = isolatedRepository { repo ->
        val queue = listOf(song("one"), song("two"), song("three"), song("four"))

        assertFailsWith<IllegalArgumentException> { repo.saveQueueAsPlaylist("", queue) }
        assertFailsWith<IllegalArgumentException> { repo.saveQueueAsPlaylist("Empty", emptyList()) }

        val playlist = repo.saveQueueAsPlaylist("QA Queue", queue)
        assertEquals(queue.map { it.id }, playlist.songs.map { it.id })

        val restored = SettingsRepository(repoPrefs(repo)).savedQueuePlaylists.first()
        assertEquals("QA Queue", restored.name)
        assertEquals(queue.map { it.id }, restored.songs.map { it.id })
    }

    @Test
    fun saveSongsAsPlaylistUsesSamePersistentLibraryStore() = isolatedRepository { repo ->
        val songs = listOf(song("alpha"), song("beta"))

        val saved = repo.saveSongsAsPlaylist("Saved Provider Playlist", songs)
        val restored = SettingsRepository(repoPrefs(repo)).savedQueuePlaylists.single()

        assertEquals(saved.id, restored.id)
        assertEquals("Saved Provider Playlist", restored.name)
        assertEquals(songs.map { it.id }, restored.songs.map { it.id })
    }

    private fun song(id: String): SongItem =
        SongItem(
            id = id,
            title = "Song $id",
            artists = listOf(Artist("Artist $id", "artist-$id")),
            duration = 240,
            thumbnail = "",
        )

    private fun repoPrefs(repo: SettingsRepository): Preferences {
        val field = SettingsRepository::class.java.getDeclaredField("prefs")
        field.isAccessible = true
        return field.get(repo) as Preferences
    }
}
