package com.omnitune.app.platform

import com.omnitune.innertube.models.Artist
import com.omnitune.innertube.models.SongItem
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.nio.file.Files
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

    @Test
    fun playlistCreateEditAddRemoveReorderDeletePersist() {
        val prefs = Preferences.userRoot().node("omnitune-tests/${UUID.randomUUID()}")
        val root = Files.createTempDirectory("omnitune-playlist-cover-test").toFile()
        val repo = SettingsRepository(prefs, PlatformContext(appDataRoot = root))
        try {
            val one = song("one")
            val two = song("two")
            val three = song("three")
            val sourceCover = File(root, "external-source/source-cover.png").apply {
                parentFile.mkdirs()
                writeBytes(byteArrayOf(1, 2, 3, 4))
            }

            val created = repo.createPlaylist("Late Set", "Night listening", listOf("Chill", "chill", "Late Night"))
            assertEquals("Late Set", created.name)
            assertEquals(listOf("Chill", "Late Night"), created.tags)

            assertEquals(1, repo.addSongToPlaylists(one, setOf(created.id)).size)
            assertEquals(0, repo.addSongToPlaylists(one, setOf(created.id)).size)
            repo.addSongToPlaylists(two, setOf(created.id))
            repo.addSongToPlaylists(three, setOf(created.id))
            assertEquals(listOf("one", "two", "three"), repo.savedQueuePlaylists.single().songs.map { it.id })

            repo.movePlaylistSong(created.id, 2, 0)
            assertEquals(listOf("three", "one", "two"), repo.savedQueuePlaylists.single().songs.map { it.id })

            repo.removeSongFromPlaylist(created.id, "one")
            assertEquals(listOf("three", "two"), repo.savedQueuePlaylists.single().songs.map { it.id })

            repo.updatePlaylistMetadata(
                id = created.id,
                name = "Late Set Edited",
                description = "Updated description",
                tags = listOf("Soul", "Downtempo"),
                coverPath = sourceCover.absolutePath,
            )

            val restored = SettingsRepository(repoPrefs(repo), PlatformContext(appDataRoot = root)).savedQueuePlaylists.single()
            val restoredCover = restored.coverPath?.let(::File)
            assertEquals("Late Set Edited", restored.name)
            assertEquals("Updated description", restored.description)
            assertEquals(listOf("Soul", "Downtempo"), restored.tags)
            assertNotNull(restoredCover)
            assertTrue(restoredCover.isFile)
            assertTrue(restoredCover.canonicalPath.startsWith(File(root, "playlist-covers").canonicalPath))
            assertEquals(sourceCover.readBytes().toList(), restoredCover.readBytes().toList())
            assertEquals(listOf("three", "two"), restored.songs.map { it.id })

            repo.deletePlaylist(created.id)
            assertTrue(repo.savedQueuePlaylists.isEmpty())
        } finally {
            runCatching {
                prefs.removeNode()
                prefs.flush()
            }
            root.deleteRecursively()
        }
    }

    @Test
    fun likedSongsPersistWithTimestampsAndUnlikeRemovesRecord() = isolatedRepository { repo ->
        val first = song("liked-one")
        val second = song("liked-two")

        repo.likeSong(first, likedAt = 100L)
        repo.likeSong(second, likedAt = 200L)
        repo.likeSong(first, likedAt = 300L)

        val restored = SettingsRepository(repoPrefs(repo))
        assertEquals(listOf("liked-two", "liked-one"), restored.likedSongRecords.map { it.song.id })
        assertEquals(listOf(200L, 100L), restored.likedSongRecords.map { it.likedAt })
        assertEquals(setOf("liked-one", "liked-two"), restored.likedSongIds)

        restored.unlikeSong("liked-one")

        val afterUnlike = SettingsRepository(repoPrefs(restored))
        assertEquals(listOf("liked-two"), afterUnlike.likedSongRecords.map { it.song.id })
        assertEquals(setOf("liked-two"), afterUnlike.likedSongIds)
    }

    @Test
    fun sqliteBackedPlaylistsAndLikedSongsPersistAcrossRepositoryRecreation() {
        val prefs = Preferences.userRoot().node("omnitune-tests/${UUID.randomUUID()}")
        val root = Files.createTempDirectory("omnitune-sqlite-settings-test").toFile()
        try {
            val repo = SettingsRepository(prefs, PlatformContext(root))
            val playlist = repo.createPlaylist("SQLite Mix", "Stored in local DB")
            repo.addSongToPlaylists(song("db-one"), setOf(playlist.id))
            repo.likeSong(song("db-liked"), likedAt = 1234L)

            val restored = SettingsRepository(repoPrefs(repo), PlatformContext(root))

            assertTrue(File(root, "omnitune.db").isFile)
            assertEquals(listOf("SQLite Mix"), restored.savedQueuePlaylists.map { it.name })
            assertEquals(listOf("db-one"), restored.savedQueuePlaylists.single().songs.map { it.id })
            assertEquals(listOf("db-liked"), restored.likedSongRecords.map { it.song.id })
            assertEquals(listOf(1234L), restored.likedSongRecords.map { it.likedAt })
        } finally {
            runCatching {
                prefs.removeNode()
                prefs.flush()
            }
            root.deleteRecursively()
        }
    }

    @Test
    fun existingJsonPlaylistsAndLikedSongsMigrateIntoSqlite() {
        val prefs = Preferences.userRoot().node("omnitune-tests/${UUID.randomUUID()}")
        val root = Files.createTempDirectory("omnitune-sqlite-migration-test").toFile()
        try {
            File(root, "savedQueuePlaylists.json").writeText(
                JSONArray()
                    .put(
                        JSONObject()
                            .put("id", "json-playlist")
                            .put("name", "JSON Playlist")
                            .put("createdAt", 10L)
                            .put("description", "migrated")
                            .put("tags", JSONArray().put("Focus"))
                            .put("coverPath", JSONObject.NULL)
                            .put("songs", JSONArray().put(songJson("json-song")))
                    )
                    .toString()
            )
            File(root, "likedSongs.json").writeText(
                JSONArray()
                    .put(JSONObject().put("likedAt", 20L).put("song", songJson("json-liked")))
                    .toString()
            )

            val repo = SettingsRepository(prefs, PlatformContext(root))

            assertEquals(listOf("json-playlist"), repo.savedQueuePlaylists.map { it.id })
            assertEquals(listOf("json-song"), repo.savedQueuePlaylists.single().songs.map { it.id })
            assertEquals(listOf("json-liked"), repo.likedSongRecords.map { it.song.id })

            File(root, "savedQueuePlaylists.json").delete()
            File(root, "likedSongs.json").delete()

            val restoredFromDbOnly = SettingsRepository(repoPrefs(repo), PlatformContext(root))
            assertEquals(listOf("json-playlist"), restoredFromDbOnly.savedQueuePlaylists.map { it.id })
            assertEquals(listOf("json-liked"), restoredFromDbOnly.likedSongRecords.map { it.song.id })
        } finally {
            runCatching {
                prefs.removeNode()
                prefs.flush()
            }
            root.deleteRecursively()
        }
    }

    @Test
    fun sqliteBackedPlaybackHistoryAndSessionsPersistAcrossRepositoryRecreation() {
        val prefs = Preferences.userRoot().node("omnitune-tests/${UUID.randomUUID()}")
        val root = Files.createTempDirectory("omnitune-sqlite-history-test").toFile()
        try {
            val repo = SettingsRepository(prefs, PlatformContext(root))
            val entry = repo.recordMeaningfulPlayback(
                song = song("db-history"),
                startedAt = 10L,
                accumulatedPlayedMs = 31_000L,
                trackDurationMs = 240_000L,
                completed = false,
            )
            assertNotNull(entry)

            val restored = SettingsRepository(repoPrefs(repo), PlatformContext(root))

            assertEquals(listOf("db-history"), restored.playbackHistory.map { it.song.id })
            assertEquals(1, restored.playbackSessions.size)
            assertEquals(31_000L, restored.playbackSessions.first().totalListeningMs)
        } finally {
            runCatching {
                prefs.removeNode()
                prefs.flush()
            }
            root.deleteRecursively()
        }
    }

    @Test
    fun existingJsonPlaybackHistoryAndSessionsMigrateIntoSqlite() {
        val prefs = Preferences.userRoot().node("omnitune-tests/${UUID.randomUUID()}")
        val root = Files.createTempDirectory("omnitune-sqlite-history-migration-test").toFile()
        try {
            File(root, "playbackHistory.json").writeText(
                JSONArray()
                    .put(
                        JSONObject()
                            .put("id", "json-history")
                            .put("playedAt", 10L)
                            .put("startedAt", 9L)
                            .put("accumulatedPlayedMs", 31_000L)
                            .put("trackDurationMs", 240_000L)
                            .put("completed", false)
                            .put("playCount", 1)
                            .put("sessionId", "json-session")
                            .put("song", songJson("json-history-song"))
                    )
                    .toString()
            )
            File(root, "playbackSessions.json").writeText(
                JSONArray()
                    .put(
                        JSONObject()
                            .put("id", "json-session")
                            .put("startedAt", 1L)
                            .put("lastActivityAt", 2L)
                            .put("endedAt", JSONObject.NULL)
                            .put("totalListeningMs", 31_000L)
                            .put("playCount", 1)
                            .put("uniqueTrackCount", 1)
                    )
                    .toString()
            )

            val repo = SettingsRepository(prefs, PlatformContext(root))
            assertEquals(listOf("json-history-song"), repo.playbackHistory.map { it.song.id })
            assertEquals(listOf("json-session"), repo.playbackSessions.map { it.id })

            File(root, "playbackHistory.json").delete()
            File(root, "playbackSessions.json").delete()

            val restoredFromDbOnly = SettingsRepository(repoPrefs(repo), PlatformContext(root))
            assertEquals(listOf("json-history-song"), restoredFromDbOnly.playbackHistory.map { it.song.id })
            assertEquals(listOf("json-session"), restoredFromDbOnly.playbackSessions.map { it.id })
        } finally {
            runCatching {
                prefs.removeNode()
                prefs.flush()
            }
            root.deleteRecursively()
        }
    }

    @Test
    fun corruptJsonStoresFallBackToPreferencesAndArePreserved() {
        val prefs = Preferences.userRoot().node("omnitune-tests/${UUID.randomUUID()}")
        val root = Files.createTempDirectory("omnitune-settings-corrupt-test").toFile()
        try {
            val playlistJson = JSONArray().put(
                JSONObject()
                    .put("id", "playlist-1")
                    .put("name", "Recovered Playlist")
                    .put("createdAt", 1L)
                    .put("songs", JSONArray().put(songJson("recovered-song")))
            ).toString()
            val historyJson = JSONArray().put(
                JSONObject()
                    .put("id", "history-1")
                    .put("playedAt", 2L)
                    .put("startedAt", 1L)
                    .put("accumulatedPlayedMs", 31_000L)
                    .put("trackDurationMs", 240_000L)
                    .put("completed", false)
                    .put("playCount", 1)
                    .put("sessionId", "session-1")
                    .put("song", songJson("history-song"))
            ).toString()
            val sessionsJson = JSONArray().put(
                JSONObject()
                    .put("id", "session-1")
                    .put("startedAt", 1L)
                    .put("lastActivityAt", 2L)
                    .put("endedAt", JSONObject.NULL)
                    .put("totalListeningMs", 31_000L)
                    .put("playCount", 1)
                    .put("uniqueTrackCount", 1)
            ).toString()

            prefs.put("savedQueuePlaylists.v1", playlistJson)
            prefs.put("playbackHistory.v1", historyJson)
            prefs.put("playbackSessions.v1", sessionsJson)
            prefs.flush()
            File(root, "savedQueuePlaylists.json").writeText("{broken")
            File(root, "playbackHistory.json").writeText("{broken")
            File(root, "playbackSessions.json").writeText("{broken")

            val repo = SettingsRepository(prefs, PlatformContext(root))

            assertEquals("Recovered Playlist", repo.savedQueuePlaylists.single().name)
            assertEquals("history-song", repo.playbackHistory.single().song.id)
            assertEquals("session-1", repo.playbackSessions.single().id)
            assertTrue(root.listFiles().orEmpty().any { it.name.startsWith("savedQueuePlaylists.json.corrupt-") })
            assertTrue(root.listFiles().orEmpty().any { it.name.startsWith("playbackHistory.json.corrupt-") })
            assertTrue(root.listFiles().orEmpty().any { it.name.startsWith("playbackSessions.json.corrupt-") })
        } finally {
            runCatching {
                prefs.removeNode()
                prefs.flush()
            }
            root.deleteRecursively()
        }
    }

    @Test
    fun corruptPrimaryJsonUsesValidBackupBeforePreferences() {
        val prefs = Preferences.userRoot().node("omnitune-tests/${UUID.randomUUID()}")
        val root = Files.createTempDirectory("omnitune-settings-backup-test").toFile()
        try {
            prefs.put("savedQueuePlaylists.v1", "[]")
            File(root, "savedQueuePlaylists.json").writeText("{broken")
            File(root, "savedQueuePlaylists.json.bak").writeText(
                JSONArray().put(
                    JSONObject()
                        .put("id", "backup-playlist")
                        .put("name", "Backup Playlist")
                        .put("createdAt", 1L)
                        .put("songs", JSONArray())
                ).toString()
            )

            val repo = SettingsRepository(prefs, PlatformContext(root))

            assertEquals("Backup Playlist", repo.savedQueuePlaylists.single().name)
            assertTrue(root.listFiles().orEmpty().any { it.name.startsWith("savedQueuePlaylists.json.corrupt-") })
        } finally {
            runCatching {
                prefs.removeNode()
                prefs.flush()
            }
            root.deleteRecursively()
        }
    }

    @Test
    fun playlistSaveFailureDoesNotCreateFalseSuccessState() {
        val prefs = Preferences.userRoot().node("omnitune-tests/${UUID.randomUUID()}")
        val root = Files.createTempDirectory("omnitune-settings-write-failure-test").toFile()
        try {
            val repo = SettingsRepository(prefs, PlatformContext(root))

            assertFailsWith<IOException> {
                AtomicFileStore.withFailurePolicyForTest({ file, operation ->
                    if (file.name == "savedQueuePlaylists.json" && operation == AtomicFileStore.Operation.BEFORE_TEMP_WRITE) {
                        throw IOException("Simulated permission denied")
                    }
                }) {
                    repo.saveQueueAsPlaylist("Should Not Save", listOf(song("one")))
                }
            }

            assertTrue(repo.savedQueuePlaylists.isEmpty())
            assertFalse(File(root, "savedQueuePlaylists.json").exists())
        } finally {
            runCatching {
                prefs.removeNode()
                prefs.flush()
            }
            root.deleteRecursively()
        }
    }

    @Test
    fun malformedPlaylistHistoryAndSessionRecordsAreSkippedWithoutDroppingGoodRecords() {
        val prefs = Preferences.userRoot().node("omnitune-tests/${UUID.randomUUID()}")
        val root = Files.createTempDirectory("omnitune-settings-record-recovery-test").toFile()
        try {
            File(root, "savedQueuePlaylists.json").writeText(
                JSONArray()
                    .put(JSONObject().put("id", "valid-playlist").put("name", "Valid").put("createdAt", 1L).put("songs", JSONArray()))
                    .put(JSONObject().put("id", "").put("name", "Missing ID").put("createdAt", 1L).put("songs", JSONArray()))
                    .put(JSONObject().put("id", "valid-playlist").put("name", "Duplicate").put("createdAt", 2L).put("songs", JSONArray()))
                    .put(JSONObject().put("unknown", true))
                    .toString()
            )
            File(root, "playbackHistory.json").writeText(
                JSONArray()
                    .put(JSONObject().put("id", "valid-history").put("playedAt", 1L).put("song", songJson("history-good")))
                    .put(JSONObject().put("id", "bad-history").put("playedAt", 1L).put("song", JSONObject().put("title", "No ID")))
                    .put(JSONObject().put("id", "valid-history").put("playedAt", 2L).put("song", songJson("duplicate-history")))
                    .toString()
            )
            File(root, "playbackSessions.json").writeText(
                JSONArray()
                    .put(JSONObject().put("id", "session-good").put("startedAt", 1L).put("lastActivityAt", 2L).put("endedAt", JSONObject.NULL))
                    .put(JSONObject().put("id", "").put("startedAt", 1L))
                    .put(JSONObject().put("id", "session-good").put("startedAt", 3L).put("lastActivityAt", 4L).put("endedAt", JSONObject.NULL))
                    .toString()
            )

            val repo = SettingsRepository(prefs, PlatformContext(root))

            assertEquals(listOf("valid-playlist"), repo.savedQueuePlaylists.map { it.id })
            assertEquals(listOf("valid-history"), repo.playbackHistory.map { it.id })
            assertEquals(listOf("session-good"), repo.playbackSessions.map { it.id })
        } finally {
            runCatching {
                prefs.removeNode()
                prefs.flush()
            }
            root.deleteRecursively()
        }
    }

    private fun song(id: String): SongItem =
        SongItem(
            id = id,
            title = "Song $id",
            artists = listOf(Artist("Artist $id", "artist-$id")),
            duration = 240,
            thumbnail = "",
        )

    private fun songJson(id: String): JSONObject = JSONObject()
        .put("id", id)
        .put("title", "Song $id")
        .put("thumbnail", "")
        .put("duration", 240)
        .put("explicit", false)
        .put("artists", JSONArray().put(JSONObject().put("name", "Artist $id").put("id", "artist-$id")))
        .put("album", JSONObject.NULL)

    private fun repoPrefs(repo: SettingsRepository): Preferences {
        val field = SettingsRepository::class.java.getDeclaredField("prefs")
        field.isAccessible = true
        return field.get(repo) as Preferences
    }
}
