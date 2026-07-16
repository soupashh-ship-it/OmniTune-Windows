package com.omnitune.app.platform

import com.omnitune.innertube.models.SongItem
import kotlinx.coroutines.flow.StateFlow
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

    private val jsonStore = JsonFileStore(prefs, platformContext)
    private val settingsPreferences = SettingsPreferences(prefs)
    private val likedSongsPersistence = LikedSongsPersistence(prefs, jsonStore)
    private val playbackHistoryPersistence = PlaybackHistoryPersistence(jsonStore)
    private val playlistPersistence = PlaylistPersistence(jsonStore, ::flush)

    val miniPlayerAlwaysOnTopFlow: StateFlow<Boolean> = settingsPreferences.miniPlayerAlwaysOnTopFlow
    val appearanceThemeFlow: StateFlow<String> = settingsPreferences.appearanceThemeFlow
    val reduceMotionFlow: StateFlow<Boolean> = settingsPreferences.reduceMotionFlow
    val globalShortcutsFlow: StateFlow<Boolean> = settingsPreferences.globalShortcutsFlow

    companion object {
        const val SESSION_TIMEOUT_MS: Long = 30 * 60 * 1000L
    }

    var volume: Int
        get() = settingsPreferences.volume
        set(value) { settingsPreferences.volume = value }

    var windowWidth: Int
        get() = settingsPreferences.windowWidth
        set(value) { settingsPreferences.windowWidth = value }

    var windowHeight: Int
        get() = settingsPreferences.windowHeight
        set(value) { settingsPreferences.windowHeight = value }

    var recentSearches: List<String>
        get() = settingsPreferences.recentSearches
        set(value) { settingsPreferences.recentSearches = value }

    var likedSongIds: Set<String>
        get() = likedSongsPersistence.likedSongIds
        set(value) { likedSongsPersistence.likedSongIds = value }

    var likedSongRecords: List<LikedSongRecord>
        get() = likedSongsPersistence.likedSongRecords
        set(value) { likedSongsPersistence.likedSongRecords = value }

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
        get() = settingsPreferences.followedArtistIds
        set(value) { settingsPreferences.followedArtistIds = value }

    var pinnedLibraryCollectionIds: Set<String>
        get() = settingsPreferences.pinnedLibraryCollectionIds
        set(value) { settingsPreferences.pinnedLibraryCollectionIds = value }

    var shuffleEnabled: Boolean
        get() = settingsPreferences.shuffleEnabled
        set(value) { settingsPreferences.shuffleEnabled = value }

    var repeatMode: Int
        get() = settingsPreferences.repeatMode
        set(value) { settingsPreferences.repeatMode = value }

    var appearanceTheme: String
        get() = settingsPreferences.appearanceTheme
        set(value) { settingsPreferences.appearanceTheme = value }

    var reduceMotionEnabled: Boolean
        get() = settingsPreferences.reduceMotionEnabled
        set(value) { settingsPreferences.reduceMotionEnabled = value }

    var miniPlayerAlwaysOnTop: Boolean
        get() = settingsPreferences.miniPlayerAlwaysOnTop
        set(value) { settingsPreferences.miniPlayerAlwaysOnTop = value }

    var globalShortcutsEnabled: Boolean
        get() = settingsPreferences.globalShortcutsEnabled
        set(value) { settingsPreferences.globalShortcutsEnabled = value }

    var normalizeVolumePreference: Boolean
        get() = settingsPreferences.normalizeVolumePreference
        set(value) { settingsPreferences.normalizeVolumePreference = value }

    var spatialAudioPreference: Boolean
        get() = settingsPreferences.spatialAudioPreference
        set(value) { settingsPreferences.spatialAudioPreference = value }

    var gaplessPlaybackPreference: Boolean
        get() = settingsPreferences.gaplessPlaybackPreference
        set(value) { settingsPreferences.gaplessPlaybackPreference = value }

    var newMusicNotifications: Boolean
        get() = settingsPreferences.newMusicNotifications
        set(value) { settingsPreferences.newMusicNotifications = value }

    var recommendationNotifications: Boolean
        get() = settingsPreferences.recommendationNotifications
        set(value) { settingsPreferences.recommendationNotifications = value }

    var productUpdateNotifications: Boolean
        get() = settingsPreferences.productUpdateNotifications
        set(value) { settingsPreferences.productUpdateNotifications = value }

    var weeklyDigestNotifications: Boolean
        get() = settingsPreferences.weeklyDigestNotifications
        set(value) { settingsPreferences.weeklyDigestNotifications = value }

    var concertAlertNotifications: Boolean
        get() = settingsPreferences.concertAlertNotifications
        set(value) { settingsPreferences.concertAlertNotifications = value }

    var autoDownloadPlaylists: Boolean
        get() = settingsPreferences.autoDownloadPlaylists
        set(value) { settingsPreferences.autoDownloadPlaylists = value }

    var downloadQualityMode: DownloadQualityMode
        get() = settingsPreferences.downloadQualityMode
        set(value) { settingsPreferences.downloadQualityMode = value }

    fun addRecentSearch(query: String) {
        settingsPreferences.addRecentSearch(query)
    }

    fun clearRecentSearches() {
        settingsPreferences.clearRecentSearches()
    }

    fun flush() {
        prefs.flush()
    }

    var savedQueuePlaylists: List<SavedQueuePlaylist>
        get() = playlistPersistence.savedQueuePlaylists
        set(value) { playlistPersistence.savedQueuePlaylists = value }

    fun saveQueueAsPlaylist(name: String, songs: List<SongItem>): SavedQueuePlaylist {
        return playlistPersistence.saveQueueAsPlaylist(name, songs)
    }

    fun saveSongsAsPlaylist(name: String, songs: List<SongItem>): SavedQueuePlaylist {
        return playlistPersistence.saveSongsAsPlaylist(name, songs)
    }

    fun createPlaylist(name: String, description: String = "", tags: List<String> = emptyList()): SavedQueuePlaylist {
        return playlistPersistence.createPlaylist(name, description, tags)
    }

    fun updatePlaylistMetadata(
        id: String,
        name: String,
        description: String,
        tags: List<String>,
        coverPath: String?,
    ): SavedQueuePlaylist {
        return playlistPersistence.updatePlaylistMetadata(id, name, description, tags, coverPath)
    }

    fun addSongToPlaylists(song: SongItem, playlistIds: Set<String>): List<SavedQueuePlaylist> {
        return playlistPersistence.addSongToPlaylists(song, playlistIds)
    }

    fun removeSongFromPlaylist(playlistId: String, songId: String): SavedQueuePlaylist {
        return playlistPersistence.removeSongFromPlaylist(playlistId, songId)
    }

    fun movePlaylistSong(playlistId: String, from: Int, to: Int): SavedQueuePlaylist {
        return playlistPersistence.movePlaylistSong(playlistId, from, to)
    }

    fun deletePlaylist(playlistId: String) {
        playlistPersistence.deletePlaylist(playlistId)
    }

    var playbackHistory: List<PlaybackHistoryEntry>
        get() = playbackHistoryPersistence.playbackHistory
        set(value) { playbackHistoryPersistence.playbackHistory = value }

    var playbackSessions: List<PlaybackSession>
        get() = playbackHistoryPersistence.playbackSessions
        set(value) { playbackHistoryPersistence.playbackSessions = value }

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

}
