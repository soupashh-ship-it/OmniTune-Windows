package com.omnitune.app.platform

import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.sql.Connection
import java.sql.DriverManager

internal class OmniLocalDatabase(
    private val databaseFile: File,
) {
    private val lock = Any()

    init {
        databaseFile.parentFile?.mkdirs()
        Class.forName("org.sqlite.JDBC")
        withConnection { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate("PRAGMA journal_mode=WAL")
                statement.executeUpdate("PRAGMA foreign_keys=ON")
                statement.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS metadata (
                        key TEXT PRIMARY KEY,
                        value TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                statement.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS liked_songs (
                        track_id TEXT PRIMARY KEY,
                        liked_at INTEGER NOT NULL,
                        song_json TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                statement.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS playlists (
                        id TEXT PRIMARY KEY,
                        name TEXT NOT NULL,
                        created_at INTEGER NOT NULL,
                        description TEXT NOT NULL,
                        tags_json TEXT NOT NULL,
                        cover_path TEXT
                    )
                    """.trimIndent()
                )
                statement.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS playlist_songs (
                        playlist_id TEXT NOT NULL,
                        position INTEGER NOT NULL,
                        song_id TEXT NOT NULL,
                        song_json TEXT NOT NULL,
                        PRIMARY KEY (playlist_id, position),
                        FOREIGN KEY (playlist_id) REFERENCES playlists(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                statement.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS playback_history (
                        id TEXT PRIMARY KEY,
                        played_at INTEGER NOT NULL,
                        started_at INTEGER NOT NULL,
                        accumulated_played_ms INTEGER NOT NULL,
                        track_duration_ms INTEGER NOT NULL,
                        completed INTEGER NOT NULL,
                        play_count INTEGER NOT NULL,
                        session_id TEXT NOT NULL,
                        song_id TEXT NOT NULL,
                        song_json TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                statement.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS playback_sessions (
                        id TEXT PRIMARY KEY,
                        started_at INTEGER NOT NULL,
                        last_activity_at INTEGER NOT NULL,
                        ended_at INTEGER,
                        total_listening_ms INTEGER NOT NULL,
                        play_count INTEGER NOT NULL,
                        unique_track_count INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                statement.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS downloads (
                        id TEXT PRIMARY KEY,
                        track_id TEXT NOT NULL,
                        title TEXT NOT NULL,
                        artist TEXT NOT NULL,
                        album TEXT,
                        artwork_url TEXT,
                        local_file_path TEXT,
                        state TEXT NOT NULL,
                        bytes_downloaded INTEGER NOT NULL,
                        total_bytes INTEGER,
                        requested_quality TEXT NOT NULL,
                        actual_codec TEXT,
                        actual_bitrate_kbps INTEGER,
                        error_message TEXT,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }

    fun isMigrated(domain: String): Boolean =
        metadata("migrated.$domain") == "true"

    fun markMigrated(domain: String) {
        setMetadata("migrated.$domain", "true")
    }

    fun readLikedSongs(): List<LikedSongRecord> = synchronized(lock) {
        withConnection { connection ->
            connection.prepareStatement(
                "SELECT liked_at, song_json FROM liked_songs ORDER BY liked_at DESC, rowid DESC"
            ).use { statement ->
                statement.executeQuery().use { rows ->
                    buildList {
                        while (rows.next()) {
                            val song = runCatching {
                                SongJsonCodec.fromJson(JSONObject(rows.getString("song_json")))
                            }.getOrNull() ?: continue
                            add(
                                LikedSongRecord(
                                    song = song,
                                    likedAt = rows.getLong("liked_at"),
                                )
                            )
                        }
                    }.distinctBy { it.song.id }
                }
            }
        }
    }

    fun replaceLikedSongs(records: List<LikedSongRecord>) = synchronized(lock) {
        withConnection { connection ->
            connection.autoCommit = false
            try {
                connection.createStatement().use { it.executeUpdate("DELETE FROM liked_songs") }
                connection.prepareStatement(
                    "INSERT INTO liked_songs(track_id, liked_at, song_json) VALUES(?, ?, ?)"
                ).use { statement ->
                    records.distinctBy { it.song.id }.forEach { record ->
                        statement.setString(1, record.song.id)
                        statement.setLong(2, record.likedAt)
                        statement.setString(3, SongJsonCodec.toJson(record.song).toString())
                        statement.addBatch()
                    }
                    statement.executeBatch()
                }
                connection.commit()
            } catch (t: Throwable) {
                connection.rollback()
                throw t
            } finally {
                connection.autoCommit = true
            }
        }
    }

    fun readPlaylists(): List<SavedQueuePlaylist> = synchronized(lock) {
        withConnection { connection ->
            val songsByPlaylist = readPlaylistSongs(connection)
            connection.prepareStatement(
                """
                SELECT id, name, created_at, description, tags_json, cover_path
                FROM playlists
                ORDER BY created_at DESC, rowid DESC
                """.trimIndent()
            ).use { statement ->
                statement.executeQuery().use { rows ->
                    buildList {
                        while (rows.next()) {
                            val id = rows.getString("id")
                            add(
                                SavedQueuePlaylist(
                                    id = id,
                                    name = rows.getString("name"),
                                    createdAt = rows.getLong("created_at"),
                                    description = rows.getString("description").orEmpty(),
                                    tags = parseTags(rows.getString("tags_json")),
                                    coverPath = rows.getString("cover_path")?.takeIf { !rows.wasNull() && it.isNotBlank() },
                                    songs = songsByPlaylist[id].orEmpty(),
                                )
                            )
                        }
                    }.distinctBy { it.id }
                }
            }
        }
    }

    fun replacePlaylists(playlists: List<SavedQueuePlaylist>) = synchronized(lock) {
        withConnection { connection ->
            connection.autoCommit = false
            try {
                connection.createStatement().use {
                    it.executeUpdate("DELETE FROM playlist_songs")
                    it.executeUpdate("DELETE FROM playlists")
                }
                connection.prepareStatement(
                    """
                    INSERT INTO playlists(id, name, created_at, description, tags_json, cover_path)
                    VALUES(?, ?, ?, ?, ?, ?)
                    """.trimIndent()
                ).use { playlistStatement ->
                    connection.prepareStatement(
                        """
                        INSERT INTO playlist_songs(playlist_id, position, song_id, song_json)
                        VALUES(?, ?, ?, ?)
                        """.trimIndent()
                    ).use { songStatement ->
                        playlists.distinctBy { it.id }.forEach { playlist ->
                            playlistStatement.setString(1, playlist.id)
                            playlistStatement.setString(2, playlist.name)
                            playlistStatement.setLong(3, playlist.createdAt)
                            playlistStatement.setString(4, playlist.description)
                            playlistStatement.setString(5, JSONArray().also { array -> playlist.tags.forEach(array::put) }.toString())
                            playlistStatement.setString(6, playlist.coverPath)
                            playlistStatement.addBatch()

                            playlist.songs.forEachIndexed { index, song ->
                                songStatement.setString(1, playlist.id)
                                songStatement.setInt(2, index)
                                songStatement.setString(3, song.id)
                                songStatement.setString(4, SongJsonCodec.toJson(song).toString())
                                songStatement.addBatch()
                            }
                        }
                        playlistStatement.executeBatch()
                        songStatement.executeBatch()
                    }
                }
                connection.commit()
            } catch (t: Throwable) {
                connection.rollback()
                throw t
            } finally {
                connection.autoCommit = true
            }
        }
    }

    fun readPlaybackHistory(): List<PlaybackHistoryEntry> = synchronized(lock) {
        withConnection { connection ->
            connection.prepareStatement(
                """
                SELECT id, played_at, started_at, accumulated_played_ms, track_duration_ms,
                       completed, play_count, session_id, song_json
                FROM playback_history
                ORDER BY played_at DESC, rowid DESC
                LIMIT 200
                """.trimIndent()
            ).use { statement ->
                statement.executeQuery().use { rows ->
                    buildList {
                        while (rows.next()) {
                            val song = runCatching {
                                SongJsonCodec.fromJson(JSONObject(rows.getString("song_json")))
                            }.getOrNull() ?: continue
                            add(
                                PlaybackHistoryEntry(
                                    id = rows.getString("id"),
                                    playedAt = rows.getLong("played_at"),
                                    song = song,
                                    startedAt = rows.getLong("started_at"),
                                    accumulatedPlayedMs = rows.getLong("accumulated_played_ms"),
                                    trackDurationMs = rows.getLong("track_duration_ms"),
                                    completed = rows.getInt("completed") == 1,
                                    playCount = rows.getInt("play_count"),
                                    sessionId = rows.getString("session_id"),
                                )
                            )
                        }
                    }.distinctBy { it.id }
                }
            }
        }
    }

    fun replacePlaybackHistory(entries: List<PlaybackHistoryEntry>) = synchronized(lock) {
        withConnection { connection ->
            connection.autoCommit = false
            try {
                connection.createStatement().use { it.executeUpdate("DELETE FROM playback_history") }
                connection.prepareStatement(
                    """
                    INSERT INTO playback_history(
                        id, played_at, started_at, accumulated_played_ms, track_duration_ms,
                        completed, play_count, session_id, song_id, song_json
                    ) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """.trimIndent()
                ).use { statement ->
                    entries.distinctBy { it.song.id }.take(200).forEach { entry ->
                        statement.setString(1, entry.id)
                        statement.setLong(2, entry.playedAt)
                        statement.setLong(3, entry.startedAt)
                        statement.setLong(4, entry.accumulatedPlayedMs)
                        statement.setLong(5, entry.trackDurationMs)
                        statement.setInt(6, if (entry.completed) 1 else 0)
                        statement.setInt(7, entry.playCount)
                        statement.setString(8, entry.sessionId)
                        statement.setString(9, entry.song.id)
                        statement.setString(10, SongJsonCodec.toJson(entry.song).toString())
                        statement.addBatch()
                    }
                    statement.executeBatch()
                }
                connection.commit()
            } catch (t: Throwable) {
                connection.rollback()
                throw t
            } finally {
                connection.autoCommit = true
            }
        }
    }

    fun readPlaybackSessions(): List<PlaybackSession> = synchronized(lock) {
        withConnection { connection ->
            connection.prepareStatement(
                """
                SELECT id, started_at, last_activity_at, ended_at, total_listening_ms, play_count, unique_track_count
                FROM playback_sessions
                ORDER BY last_activity_at DESC, rowid DESC
                LIMIT 100
                """.trimIndent()
            ).use { statement ->
                statement.executeQuery().use { rows ->
                    buildList {
                        while (rows.next()) {
                            val endedAt = rows.getLong("ended_at").let { value ->
                                if (rows.wasNull()) null else value
                            }
                            add(
                                PlaybackSession(
                                    id = rows.getString("id"),
                                    startedAt = rows.getLong("started_at"),
                                    lastActivityAt = rows.getLong("last_activity_at"),
                                    endedAt = endedAt,
                                    totalListeningMs = rows.getLong("total_listening_ms"),
                                    playCount = rows.getInt("play_count"),
                                    uniqueTrackCount = rows.getInt("unique_track_count"),
                                )
                            )
                        }
                    }.distinctBy { it.id }
                }
            }
        }
    }

    fun replacePlaybackSessions(sessions: List<PlaybackSession>) = synchronized(lock) {
        withConnection { connection ->
            connection.autoCommit = false
            try {
                connection.createStatement().use { it.executeUpdate("DELETE FROM playback_sessions") }
                connection.prepareStatement(
                    """
                    INSERT INTO playback_sessions(
                        id, started_at, last_activity_at, ended_at, total_listening_ms, play_count, unique_track_count
                    ) VALUES(?, ?, ?, ?, ?, ?, ?)
                    """.trimIndent()
                ).use { statement ->
                    sessions.distinctBy { it.id }.take(100).forEach { session ->
                        statement.setString(1, session.id)
                        statement.setLong(2, session.startedAt)
                        statement.setLong(3, session.lastActivityAt)
                        if (session.endedAt == null) {
                            statement.setNull(4, java.sql.Types.INTEGER)
                        } else {
                            statement.setLong(4, session.endedAt)
                        }
                        statement.setLong(5, session.totalListeningMs)
                        statement.setInt(6, session.playCount)
                        statement.setInt(7, session.uniqueTrackCount)
                        statement.addBatch()
                    }
                    statement.executeBatch()
                }
                connection.commit()
            } catch (t: Throwable) {
                connection.rollback()
                throw t
            } finally {
                connection.autoCommit = true
            }
        }
    }

    fun readDownloads(): List<DownloadTask> = synchronized(lock) {
        withConnection { connection ->
            connection.prepareStatement(
                """
                SELECT id, track_id, title, artist, album, artwork_url, local_file_path, state,
                       bytes_downloaded, total_bytes, requested_quality, actual_codec,
                       actual_bitrate_kbps, error_message, created_at, updated_at
                FROM downloads
                ORDER BY updated_at DESC, rowid DESC
                """.trimIndent()
            ).use { statement ->
                statement.executeQuery().use { rows ->
                    buildList {
                        while (rows.next()) {
                            add(
                                DownloadTask(
                                    id = rows.getString("id"),
                                    trackId = rows.getString("track_id"),
                                    title = rows.getString("title"),
                                    artist = rows.getString("artist"),
                                    album = rows.getNullableString("album"),
                                    artworkUrl = rows.getNullableString("artwork_url"),
                                    localFilePath = rows.getNullableString("local_file_path"),
                                    state = runCatching { DownloadState.valueOf(rows.getString("state")) }.getOrDefault(DownloadState.FAILED),
                                    bytesDownloaded = rows.getLong("bytes_downloaded"),
                                    totalBytes = rows.getNullableLong("total_bytes"),
                                    requestedQuality = runCatching { DownloadQualityMode.valueOf(rows.getString("requested_quality")) }.getOrDefault(DownloadQualityMode.PROVIDER_DEFAULT),
                                    actualCodec = rows.getNullableString("actual_codec"),
                                    actualBitrateKbps = rows.getNullableInt("actual_bitrate_kbps"),
                                    errorMessage = rows.getNullableString("error_message"),
                                    createdAt = rows.getLong("created_at"),
                                    updatedAt = rows.getLong("updated_at"),
                                )
                            )
                        }
                    }.distinctBy { it.id }
                }
            }
        }
    }

    fun replaceDownloads(tasks: List<DownloadTask>) = synchronized(lock) {
        withConnection { connection ->
            connection.autoCommit = false
            try {
                connection.createStatement().use { it.executeUpdate("DELETE FROM downloads") }
                connection.prepareStatement(
                    """
                    INSERT INTO downloads(
                        id, track_id, title, artist, album, artwork_url, local_file_path, state,
                        bytes_downloaded, total_bytes, requested_quality, actual_codec,
                        actual_bitrate_kbps, error_message, created_at, updated_at
                    ) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """.trimIndent()
                ).use { statement ->
                    tasks.distinctBy { it.id }.forEach { task ->
                        statement.setString(1, task.id)
                        statement.setString(2, task.trackId)
                        statement.setString(3, task.title)
                        statement.setString(4, task.artist)
                        statement.setString(5, task.album)
                        statement.setString(6, task.artworkUrl)
                        statement.setString(7, task.localFilePath)
                        statement.setString(8, task.state.name)
                        statement.setLong(9, task.bytesDownloaded)
                        if (task.totalBytes == null) statement.setNull(10, java.sql.Types.INTEGER) else statement.setLong(10, task.totalBytes)
                        statement.setString(11, task.requestedQuality.name)
                        statement.setString(12, task.actualCodec)
                        if (task.actualBitrateKbps == null) statement.setNull(13, java.sql.Types.INTEGER) else statement.setInt(13, task.actualBitrateKbps)
                        statement.setString(14, task.errorMessage)
                        statement.setLong(15, task.createdAt)
                        statement.setLong(16, task.updatedAt)
                        statement.addBatch()
                    }
                    statement.executeBatch()
                }
                connection.commit()
            } catch (t: Throwable) {
                connection.rollback()
                throw t
            } finally {
                connection.autoCommit = true
            }
        }
    }

    private fun readPlaylistSongs(connection: Connection): Map<String, List<com.omnitune.innertube.models.SongItem>> {
        return connection.prepareStatement(
            """
            SELECT playlist_id, song_json
            FROM playlist_songs
            ORDER BY playlist_id ASC, position ASC
            """.trimIndent()
        ).use { statement ->
            statement.executeQuery().use { rows ->
                val map = linkedMapOf<String, MutableList<com.omnitune.innertube.models.SongItem>>()
                while (rows.next()) {
                    val playlistId = rows.getString("playlist_id")
                    val song = runCatching {
                        SongJsonCodec.fromJson(JSONObject(rows.getString("song_json")))
                    }.getOrNull() ?: continue
                    map.getOrPut(playlistId) { mutableListOf() }.add(song)
                }
                map
            }
        }
    }

    private fun metadata(key: String): String? = synchronized(lock) {
        withConnection { connection ->
            connection.prepareStatement("SELECT value FROM metadata WHERE key = ?").use { statement ->
                statement.setString(1, key)
                statement.executeQuery().use { rows ->
                    if (rows.next()) rows.getString("value") else null
                }
            }
        }
    }

    private fun setMetadata(key: String, value: String) = synchronized(lock) {
        withConnection { connection ->
            connection.prepareStatement(
                "INSERT INTO metadata(key, value) VALUES(?, ?) ON CONFLICT(key) DO UPDATE SET value = excluded.value"
            ).use { statement ->
                statement.setString(1, key)
                statement.setString(2, value)
                statement.executeUpdate()
            }
        }
    }

    private fun parseTags(json: String?): List<String> = runCatching {
        val array = JSONArray(json ?: "[]")
        (0 until array.length()).mapNotNull { array.optString(it).takeIf(String::isNotBlank) }
    }.getOrDefault(emptyList())

    private fun <T> withConnection(block: (Connection) -> T): T =
        DriverManager.getConnection("jdbc:sqlite:${databaseFile.absolutePath}").use(block)
}

private fun java.sql.ResultSet.getNullableString(column: String): String? {
    val value = getString(column)
    return if (wasNull()) null else value
}

private fun java.sql.ResultSet.getNullableLong(column: String): Long? {
    val value = getLong(column)
    return if (wasNull()) null else value
}

private fun java.sql.ResultSet.getNullableInt(column: String): Int? {
    val value = getInt(column)
    return if (wasNull()) null else value
}
