package com.omnitune.app.platform

import com.omnitune.innertube.models.Album
import com.omnitune.innertube.models.Artist
import com.omnitune.innertube.models.SongItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.prefs.Preferences

data class SavedQueuePlaylist(
    val id: String,
    val name: String,
    val createdAt: Long,
    val songs: List<SongItem>,
    val description: String = "",
    val tags: List<String> = emptyList(),
    val coverPath: String? = null,
)

data class LikedSongRecord(
    val song: SongItem,
    val likedAt: Long,
)

data class PlaybackHistoryEntry(
    val id: String,
    val playedAt: Long,
    val song: SongItem,
    val startedAt: Long = playedAt,
    val accumulatedPlayedMs: Long = 0L,
    val trackDurationMs: Long = 0L,
    val completed: Boolean = false,
    val playCount: Int = 1,
    val sessionId: String = "",
)

data class PlaybackSession(
    val id: String,
    val startedAt: Long,
    val lastActivityAt: Long,
    val endedAt: Long?,
    val totalListeningMs: Long,
    val playCount: Int,
    val uniqueTrackCount: Int,
)

class SettingsRepository(
    private val prefs: Preferences = Preferences.userNodeForPackage(SettingsRepository::class.java),
    private val platformContext: PlatformContext? = null,
) {
    private fun writeJsonFile(name: String, content: String) {
        platformContext?.appDataDir?.let { dir ->
            runCatching { AtomicFileStore.writeText(java.io.File(dir, name), content) }
                .onFailure { OmniLogger.error("Settings", "Failed to persist $name.", it) }
                .getOrThrow()
        }
    }

    private fun writeJsonStore(name: String, fallbackPreferenceKey: String, content: String) {
        writeJsonFile(name, content)
        if (platformContext == null) {
            prefs.put(fallbackPreferenceKey, content)
        }
    }
    
    private fun readJsonFile(name: String): String? {
        return platformContext?.appDataDir?.let { dir ->
            val file = java.io.File(dir, name)
            if (file.exists()) runCatching { file.readText() }.getOrNull() else null
        }
    }

    private fun readJsonBackupFile(name: String): String? {
        return platformContext?.appDataDir?.let { dir ->
            val file = java.io.File(dir, "$name.bak")
            if (file.exists()) runCatching { file.readText() }.getOrNull() else null
        }
    }

    private fun preserveCorruptJsonFile(name: String) {
        platformContext?.appDataDir?.let { dir ->
            val file = java.io.File(dir, name)
            if (!file.exists()) return
            runCatching {
                file.copyTo(
                    java.io.File(dir, "$name.corrupt-${System.currentTimeMillis()}.bak"),
                    overwrite = false,
                )
            }
        }
    }

    private fun <T> readRecoverableJsonList(
        fileName: String,
        fallbackPreferenceKey: String,
        parser: (String) -> List<T>,
    ): List<T> {
        val fileContent = readJsonFile(fileName)
        if (fileContent != null) {
            runCatching { return parser(fileContent) }
                .onFailure { preserveCorruptJsonFile(fileName) }
        }

        readJsonBackupFile(fileName)?.let { backupContent ->
            runCatching { return parser(backupContent) }
                .onFailure { OmniLogger.error("Settings", "Backup JSON for $fileName is also unreadable.", it) }
        }

        return runCatching { parser(prefs.get(fallbackPreferenceKey, "[]")) }
            .getOrDefault(emptyList())
    }

    private val _miniPlayerAlwaysOnTopFlow = MutableStateFlow(prefs.getBoolean("miniPlayerAlwaysOnTop", true))
    val miniPlayerAlwaysOnTopFlow: StateFlow<Boolean> = _miniPlayerAlwaysOnTopFlow.asStateFlow()
    private val _appearanceThemeFlow = MutableStateFlow(prefs.get("appearanceTheme", "nocturne") ?: "nocturne")
    val appearanceThemeFlow: StateFlow<String> = _appearanceThemeFlow.asStateFlow()
    private val _reduceMotionFlow = MutableStateFlow(prefs.getBoolean("reduceMotionEnabled", false))
    val reduceMotionFlow: StateFlow<Boolean> = _reduceMotionFlow.asStateFlow()
    private val _globalShortcutsFlow = MutableStateFlow(prefs.getBoolean("globalShortcutsEnabled", true))
    val globalShortcutsFlow: StateFlow<Boolean> = _globalShortcutsFlow.asStateFlow()

    companion object {
        const val SESSION_TIMEOUT_MS: Long = 30 * 60 * 1000L
    }

    var volume: Int
        get() = prefs.getInt("volume", 100).coerceIn(0, 200)
        set(value) { prefs.putInt("volume", value.coerceIn(0, 200)) }

    var windowWidth: Int
        get() = prefs.getInt("windowWidth", 1200)
        set(value) { prefs.putInt("windowWidth", value) }

    var windowHeight: Int
        get() = prefs.getInt("windowHeight", 800)
        set(value) { prefs.putInt("windowHeight", value) }

    var recentSearches: List<String>
        get() {
            val stored = prefs.get("recentSearches.v2", null)
            if (!stored.isNullOrBlank()) {
                return runCatching {
                    val array = JSONArray(stored)
                    (0 until array.length()).mapNotNull { array.optString(it).takeIf(String::isNotBlank) }
                }.getOrDefault(emptyList())
            }

            // Backward compatibility with the older space-separated format. Multi-word
            // queries were not recoverable in that format, but preserving tokens avoids
            // dropping existing user data during upgrade.
            return (prefs.get("recentSearches", "") ?: "")
                .split(" ")
                .filter { it.isNotBlank() }
        }
        set(value) {
            val deduped = value
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinctBy { it.lowercase() }
                .take(50)
            prefs.put("recentSearches.v2", JSONArray(deduped).toString())
        }

    var likedSongIds: Set<String>
        get() = (prefs.get("likedSongIds", "") ?: "").split(",").filter { it.isNotBlank() }.toSet()
        set(value) { prefs.put("likedSongIds", value.joinToString(",")) }

    var likedSongRecords: List<LikedSongRecord>
        get() = readLikedSongRecords()
        set(value) {
            val distinct = value.distinctBy { it.song.id }
            val array = JSONArray()
            distinct.forEach { record ->
                array.put(
                    JSONObject()
                        .put("likedAt", record.likedAt)
                        .put("song", record.song.toJson())
                )
            }
            writeJsonStore("likedSongs.json", "likedSongs.v1", array.toString())
            likedSongIds = distinct.map { it.song.id }.toSet()
        }

    fun likeSong(song: SongItem, likedAt: Long = System.currentTimeMillis()): LikedSongRecord {
        val current = likedSongRecords
        val existing = current.firstOrNull { it.song.id == song.id }
        if (existing != null) return existing
        val record = LikedSongRecord(song, likedAt)
        likedSongRecords = listOf(record) + current
        return record
    }

    fun unlikeSong(songId: String) {
        likedSongRecords = likedSongRecords.filterNot { it.song.id == songId }
        likedSongIds = likedSongIds - songId
    }

    var followedArtistIds: Set<String>
        get() = (prefs.get("followedArtistIds.v1", "") ?: "").split(",").filter { it.isNotBlank() }.toSet()
        set(value) { prefs.put("followedArtistIds.v1", value.filter { it.isNotBlank() }.distinct().joinToString(",")) }

    var pinnedLibraryCollectionIds: Set<String>
        get() {
            val stored = prefs.get("pinnedLibraryCollectionIds.v1", null)
            if (stored.isNullOrBlank()) {
                return setOf("favorites", "queue", "albums", "artists", "playlists", "downloads")
            }
            return stored.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
        }
        set(value) {
            val sanitized = value
                .filter { it in setOf("favorites", "queue", "albums", "artists", "playlists", "downloads") }
                .ifEmpty { listOf("favorites") }
            prefs.put("pinnedLibraryCollectionIds.v1", sanitized.joinToString(","))
        }

    var shuffleEnabled: Boolean
        get() = prefs.getBoolean("shuffleEnabled", false)
        set(value) { prefs.putBoolean("shuffleEnabled", value) }

    var repeatMode: Int
        get() = prefs.getInt("repeatMode", 0)
        set(value) { prefs.putInt("repeatMode", value) }

    var appearanceTheme: String
        get() = prefs.get("appearanceTheme", "nocturne") ?: "nocturne"
        set(value) {
            prefs.put("appearanceTheme", value)
            _appearanceThemeFlow.value = value
        }

    var reduceMotionEnabled: Boolean
        get() = prefs.getBoolean("reduceMotionEnabled", false)
        set(value) {
            prefs.putBoolean("reduceMotionEnabled", value)
            _reduceMotionFlow.value = value
        }

    var miniPlayerAlwaysOnTop: Boolean
        get() = prefs.getBoolean("miniPlayerAlwaysOnTop", true)
        set(value) {
            prefs.putBoolean("miniPlayerAlwaysOnTop", value)
            _miniPlayerAlwaysOnTopFlow.value = value
        }

    var globalShortcutsEnabled: Boolean
        get() = prefs.getBoolean("globalShortcutsEnabled", true)
        set(value) {
            prefs.putBoolean("globalShortcutsEnabled", value)
            _globalShortcutsFlow.value = value
        }

    var normalizeVolumePreference: Boolean
        get() = prefs.getBoolean("normalizeVolumePreference", false)
        set(value) { prefs.putBoolean("normalizeVolumePreference", value) }

    var spatialAudioPreference: Boolean
        get() = prefs.getBoolean("spatialAudioPreference", false)
        set(value) { prefs.putBoolean("spatialAudioPreference", value) }

    var gaplessPlaybackPreference: Boolean
        get() = prefs.getBoolean("gaplessPlaybackPreference", true)
        set(value) { prefs.putBoolean("gaplessPlaybackPreference", value) }

    var newMusicNotifications: Boolean
        get() = prefs.getBoolean("newMusicNotifications", true)
        set(value) { prefs.putBoolean("newMusicNotifications", value) }

    var recommendationNotifications: Boolean
        get() = prefs.getBoolean("recommendationNotifications", true)
        set(value) { prefs.putBoolean("recommendationNotifications", value) }

    var productUpdateNotifications: Boolean
        get() = prefs.getBoolean("productUpdateNotifications", true)
        set(value) { prefs.putBoolean("productUpdateNotifications", value) }

    var weeklyDigestNotifications: Boolean
        get() = prefs.getBoolean("weeklyDigestNotifications", true)
        set(value) { prefs.putBoolean("weeklyDigestNotifications", value) }

    var concertAlertNotifications: Boolean
        get() = prefs.getBoolean("concertAlertNotifications", false)
        set(value) { prefs.putBoolean("concertAlertNotifications", value) }

    var autoDownloadPlaylists: Boolean
        get() = prefs.getBoolean("autoDownloadPlaylists", false)
        set(value) { prefs.putBoolean("autoDownloadPlaylists", value) }

    var downloadQualityMode: DownloadQualityMode
        get() = runCatching {
            DownloadQualityMode.valueOf(prefs.get("downloadQualityMode", DownloadQualityMode.PROVIDER_DEFAULT.name))
        }.getOrDefault(DownloadQualityMode.PROVIDER_DEFAULT)
        set(value) { prefs.put("downloadQualityMode", value.name) }

    fun addRecentSearch(query: String) {
        val q = query.trim()
        if (q.isBlank()) return
        val updated = (listOf(q) + recentSearches.filterNot { it.equals(q, ignoreCase = true) }).take(50)
        recentSearches = updated
    }

    fun clearRecentSearches() {
        recentSearches = emptyList()
    }

    fun flush() {
        prefs.flush()
    }

    var savedQueuePlaylists: List<SavedQueuePlaylist>
        get() = readPlaylistList()
        set(value) {
            val array = JSONArray()
            value.forEach { playlist ->
                array.put(
                    JSONObject()
                        .put("id", playlist.id)
                        .put("name", playlist.name)
                        .put("createdAt", playlist.createdAt)
                        .put("description", playlist.description)
                        .put("tags", JSONArray().also { tags ->
                            playlist.tags.forEach { tags.put(it) }
                        })
                        .put("coverPath", playlist.coverPath ?: JSONObject.NULL)
                        .put("songs", JSONArray().also { songs ->
                            playlist.songs.forEach { songs.put(it.toJson()) }
                        })
                )
            }
            val json = array.toString()
            writeJsonStore("savedQueuePlaylists.json", "savedQueuePlaylists.v1", json)
        }

    fun saveQueueAsPlaylist(name: String, songs: List<SongItem>): SavedQueuePlaylist {
        return saveSongsAsPlaylist(name, songs)
    }

    fun saveSongsAsPlaylist(name: String, songs: List<SongItem>): SavedQueuePlaylist {
        val trimmed = name.trim()
        require(trimmed.isNotBlank()) { "Playlist name cannot be empty." }
        require(songs.isNotEmpty()) { "Playlist has no loaded songs." }

        val now = System.currentTimeMillis()
        val playlist = SavedQueuePlaylist(
            id = "local-queue-$now",
            name = trimmed,
            createdAt = now,
            songs = songs.distinctBy { it.id },
        )
        savedQueuePlaylists = listOf(playlist) + savedQueuePlaylists.filterNot { it.name.equals(trimmed, ignoreCase = true) }
        flush()
        return playlist
    }

    fun createPlaylist(name: String, description: String = "", tags: List<String> = emptyList()): SavedQueuePlaylist {
        val trimmed = name.trim()
        require(trimmed.isNotBlank()) { "Playlist name cannot be empty." }
        val now = System.currentTimeMillis()
        val playlist = SavedQueuePlaylist(
            id = "local-playlist-$now",
            name = trimmed,
            createdAt = now,
            songs = emptyList(),
            description = description.trim(),
            tags = sanitizePlaylistTags(tags),
        )
        savedQueuePlaylists = listOf(playlist) + savedQueuePlaylists.filterNot { it.name.equals(trimmed, ignoreCase = true) }
        flush()
        return playlist
    }

    fun updatePlaylistMetadata(
        id: String,
        name: String,
        description: String,
        tags: List<String>,
        coverPath: String?,
    ): SavedQueuePlaylist {
        val trimmed = name.trim()
        require(trimmed.isNotBlank()) { "Playlist name cannot be empty." }
        val playlists = savedQueuePlaylists
        val existing = playlists.firstOrNull { it.id == id } ?: error("Playlist not found.")
        val updated = existing.copy(
            name = trimmed,
            description = description.trim().take(300),
            tags = sanitizePlaylistTags(tags),
            coverPath = coverPath?.trim()?.takeIf { it.isNotBlank() },
        )
        savedQueuePlaylists = listOf(updated) + playlists.filterNot { it.id == id }
        flush()
        return updated
    }

    fun addSongToPlaylists(song: SongItem, playlistIds: Set<String>): List<SavedQueuePlaylist> {
        if (playlistIds.isEmpty()) return emptyList()
        val selectedIds = playlistIds.filter { it.isNotBlank() }.toSet()
        val updatedPlaylists = mutableListOf<SavedQueuePlaylist>()
        savedQueuePlaylists = savedQueuePlaylists.map { playlist ->
            if (playlist.id !in selectedIds) {
                playlist
            } else if (playlist.songs.any { it.id == song.id }) {
                playlist
            } else {
                val updated = playlist.copy(songs = playlist.songs + song)
                updatedPlaylists += updated
                updated
            }
        }
        flush()
        return updatedPlaylists
    }

    fun removeSongFromPlaylist(playlistId: String, songId: String): SavedQueuePlaylist {
        val playlists = savedQueuePlaylists
        val existing = playlists.firstOrNull { it.id == playlistId } ?: error("Playlist not found.")
        val updated = existing.copy(songs = existing.songs.filterNot { it.id == songId })
        savedQueuePlaylists = listOf(updated) + playlists.filterNot { it.id == playlistId }
        flush()
        return updated
    }

    fun movePlaylistSong(playlistId: String, from: Int, to: Int): SavedQueuePlaylist {
        val playlists = savedQueuePlaylists
        val existing = playlists.firstOrNull { it.id == playlistId } ?: error("Playlist not found.")
        require(from in existing.songs.indices && to in existing.songs.indices) { "Invalid playlist order index." }
        val mutable = existing.songs.toMutableList()
        val song = mutable.removeAt(from)
        mutable.add(to, song)
        val updated = existing.copy(songs = mutable)
        savedQueuePlaylists = listOf(updated) + playlists.filterNot { it.id == playlistId }
        flush()
        return updated
    }

    fun deletePlaylist(playlistId: String) {
        savedQueuePlaylists = savedQueuePlaylists.filterNot { it.id == playlistId }
        flush()
    }

    private fun sanitizePlaylistTags(tags: List<String>): List<String> =
        tags.map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .take(8)

    var playbackHistory: List<PlaybackHistoryEntry>
        get() = readPlaybackHistory()
        set(value) {
            val array = JSONArray()
            value
                .distinctBy { it.song.id }
                .take(200)
                .forEach { entry ->
                    array.put(
                        JSONObject()
                            .put("id", entry.id)
                            .put("playedAt", entry.playedAt)
                            .put("startedAt", entry.startedAt)
                            .put("accumulatedPlayedMs", entry.accumulatedPlayedMs)
                            .put("trackDurationMs", entry.trackDurationMs)
                            .put("completed", entry.completed)
                            .put("playCount", entry.playCount)
                            .put("sessionId", entry.sessionId)
                            .put("song", entry.song.toJson())
                    )
            }
            val json = array.toString()
            writeJsonStore("playbackHistory.json", "playbackHistory.v1", json)
        }

    var playbackSessions: List<PlaybackSession>
        get() = readPlaybackSessions()
        set(value) {
            val array = JSONArray()
            value
                .distinctBy { it.id }
                .take(100)
                .forEach { session ->
                    array.put(
                        JSONObject()
                            .put("id", session.id)
                            .put("startedAt", session.startedAt)
                            .put("lastActivityAt", session.lastActivityAt)
                            .put("endedAt", session.endedAt ?: JSONObject.NULL)
                            .put("totalListeningMs", session.totalListeningMs)
                            .put("playCount", session.playCount)
                            .put("uniqueTrackCount", session.uniqueTrackCount)
                    )
            }
            val json = array.toString()
            writeJsonStore("playbackSessions.json", "playbackSessions.v1", json)
        }

    fun isMeaningfulListen(accumulatedPlayedMs: Long, durationMs: Long): Boolean {
        val threshold = when {
            durationMs <= 0L -> 30_000L
            durationMs < 20_000L -> maxOf(5_000L, durationMs / 2)
            else -> minOf(30_000L, durationMs / 2)
        }
        return accumulatedPlayedMs >= threshold
    }

    fun recordMeaningfulPlayback(
        song: SongItem,
        startedAt: Long,
        accumulatedPlayedMs: Long,
        trackDurationMs: Long,
        completed: Boolean,
    ): PlaybackHistoryEntry? {
        if (!isMeaningfulListen(accumulatedPlayedMs, trackDurationMs) && !completed) return null

        val now = System.currentTimeMillis()
        val session = currentOrNewSession(now)
        val previous = playbackHistory.firstOrNull { it.song.id == song.id }
        val entry = PlaybackHistoryEntry(
            id = previous?.id ?: "${song.id}-$now",
            playedAt = now,
            startedAt = previous?.startedAt ?: startedAt,
            song = song,
            accumulatedPlayedMs = (previous?.accumulatedPlayedMs ?: 0L) + accumulatedPlayedMs,
            trackDurationMs = trackDurationMs,
            completed = completed || previous?.completed == true,
            playCount = (previous?.playCount ?: 0) + 1,
            sessionId = session.id,
        )
        playbackHistory = listOf(entry) + playbackHistory.filterNot { it.song.id == song.id }.take(199)
        updateSession(session, now, accumulatedPlayedMs, playbackHistory.count { it.sessionId == session.id })
        flush()
        return entry
    }

    @Deprecated("Use recordMeaningfulPlayback after a listen crosses the threshold.")
    fun recordPlayback(song: SongItem) {
        recordMeaningfulPlayback(
            song = song,
            startedAt = System.currentTimeMillis(),
            accumulatedPlayedMs = 30_000L,
            trackDurationMs = song.duration?.times(1000L) ?: 0L,
            completed = false,
        )
    }

    fun clearPlaybackHistory() {
        playbackHistory = emptyList()
        playbackSessions = emptyList()
        flush()
    }

    private fun currentOrNewSession(now: Long): PlaybackSession {
        val current = playbackSessions.firstOrNull()
        return if (current == null || now - current.lastActivityAt > SESSION_TIMEOUT_MS) {
            PlaybackSession(
                id = "session-$now",
                startedAt = now,
                lastActivityAt = now,
                endedAt = null,
                totalListeningMs = 0L,
                playCount = 0,
                uniqueTrackCount = 0,
            )
        } else current
    }

    private fun updateSession(session: PlaybackSession, now: Long, listenedMs: Long, uniqueTrackCount: Int) {
        val updated = session.copy(
            lastActivityAt = now,
            totalListeningMs = session.totalListeningMs + listenedMs.coerceAtLeast(0L),
            playCount = session.playCount + 1,
            uniqueTrackCount = maxOf(session.uniqueTrackCount, uniqueTrackCount),
        )
        playbackSessions = listOf(updated) + playbackSessions.filterNot { it.id == session.id }
    }

    private fun readPlaylistList(): List<SavedQueuePlaylist> = readRecoverableJsonList(
        fileName = "savedQueuePlaylists.json",
        fallbackPreferenceKey = "savedQueuePlaylists.v1",
    ) { stored ->
        val array = JSONArray(stored)
        (0 until array.length()).mapNotNull { index ->
            val obj = array.optJSONObject(index) ?: return@mapNotNull null
            val songsJson = obj.optJSONArray("songs") ?: JSONArray()
            SavedQueuePlaylist(
                id = obj.optString("id"),
                name = obj.optString("name"),
                createdAt = obj.optLong("createdAt"),
                description = obj.optString("description"),
                tags = obj.optJSONArray("tags")?.let { tagsJson ->
                    (0 until tagsJson.length()).mapNotNull { tagsJson.optString(it).takeIf(String::isNotBlank) }
                }.orEmpty(),
                coverPath = obj.optString("coverPath").takeIf { it.isNotBlank() },
                songs = (0 until songsJson.length()).mapNotNull { songsJson.optJSONObject(it)?.toSongItem() },
            ).takeIf { it.id.isNotBlank() && it.name.isNotBlank() }
        }.distinctBy { it.id }
    }

    private fun readLikedSongRecords(): List<LikedSongRecord> {
        val records = readRecoverableJsonList(
            fileName = "likedSongs.json",
            fallbackPreferenceKey = "likedSongs.v1",
        ) { stored ->
            val array = JSONArray(stored)
            (0 until array.length()).mapNotNull { index ->
                val obj = array.optJSONObject(index) ?: return@mapNotNull null
                val song = obj.optJSONObject("song")?.toSongItem() ?: return@mapNotNull null
                LikedSongRecord(
                    song = song,
                    likedAt = obj.optLong("likedAt", System.currentTimeMillis()),
                ).takeIf { it.song.id.isNotBlank() }
            }.distinctBy { it.song.id }
        }
        if (records.isNotEmpty()) return records
        return emptyList()
    }

    private fun readPlaybackHistory(): List<PlaybackHistoryEntry> = readRecoverableJsonList(
        fileName = "playbackHistory.json",
        fallbackPreferenceKey = "playbackHistory.v1",
    ) { stored ->
        val array = JSONArray(stored)
        (0 until array.length()).mapNotNull { index ->
            val obj = array.optJSONObject(index) ?: return@mapNotNull null
            val song = obj.optJSONObject("song")?.toSongItem() ?: return@mapNotNull null
            PlaybackHistoryEntry(
                id = obj.optString("id"),
                playedAt = obj.optLong("playedAt"),
                song = song,
                startedAt = obj.optLong("startedAt", obj.optLong("playedAt")),
                accumulatedPlayedMs = obj.optLong("accumulatedPlayedMs"),
                trackDurationMs = obj.optLong("trackDurationMs"),
                completed = obj.optBoolean("completed", false),
                playCount = obj.optInt("playCount", 1),
                sessionId = obj.optString("sessionId"),
            ).takeIf { it.id.isNotBlank() && it.song.id.isNotBlank() }
        }.distinctBy { it.id }
    }

    private fun readPlaybackSessions(): List<PlaybackSession> = readRecoverableJsonList(
        fileName = "playbackSessions.json",
        fallbackPreferenceKey = "playbackSessions.v1",
    ) { stored ->
        val array = JSONArray(stored)
        (0 until array.length()).mapNotNull { index ->
            val obj = array.optJSONObject(index) ?: return@mapNotNull null
            PlaybackSession(
                id = obj.optString("id"),
                startedAt = obj.optLong("startedAt"),
                lastActivityAt = obj.optLong("lastActivityAt"),
                endedAt = if (obj.isNull("endedAt")) null else obj.optLong("endedAt"),
                totalListeningMs = obj.optLong("totalListeningMs"),
                playCount = obj.optInt("playCount"),
                uniqueTrackCount = obj.optInt("uniqueTrackCount"),
            ).takeIf { it.id.isNotBlank() }
        }.distinctBy { it.id }
    }

    private fun SongItem.toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("title", title)
        .put("thumbnail", thumbnail)
        .put("duration", duration ?: JSONObject.NULL)
        .put("explicit", explicit)
        .put("artists", JSONArray().also { array ->
            artists.forEach { artist ->
                array.put(JSONObject().put("name", artist.name).put("id", artist.id ?: JSONObject.NULL))
            }
        })
        .put(
            "album",
            album?.let { JSONObject().put("name", it.name).put("id", it.id) } ?: JSONObject.NULL
        )

    private fun JSONObject.toSongItem(): SongItem {
        val artistsJson = optJSONArray("artists") ?: JSONArray()
        val parsedArtists = (0 until artistsJson.length()).mapNotNull { index ->
            artistsJson.optJSONObject(index)?.let {
                Artist(
                    name = it.optString("name"),
                    id = it.optString("id").takeIf(String::isNotBlank),
                )
            }
        }.ifEmpty { listOf(Artist("Unknown artist", null)) }
        val albumJson = optJSONObject("album")
        return SongItem(
            id = optString("id"),
            title = optString("title"),
            artists = parsedArtists,
            album = albumJson?.let { Album(it.optString("name"), it.optString("id")) },
            duration = if (isNull("duration")) null else optInt("duration"),
            thumbnail = optString("thumbnail"),
            explicit = optBoolean("explicit", false),
        )
    }
}
