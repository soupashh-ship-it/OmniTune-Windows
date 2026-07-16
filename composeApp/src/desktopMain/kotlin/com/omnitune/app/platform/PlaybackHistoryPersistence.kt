package com.omnitune.app.platform

import org.json.JSONArray
import org.json.JSONObject

internal class PlaybackHistoryPersistence(
    private val jsonStore: JsonFileStore,
    private val localDatabase: OmniLocalDatabase? = null,
) {
    init {
        migrateToDatabaseIfNeeded()
    }

    var playbackHistory: List<PlaybackHistoryEntry>
        get() = localDatabase
            ?.takeIf { it.isMigrated(HISTORY_DOMAIN) }
            ?.readPlaybackHistory()
            ?: readPlaybackHistoryFromJson()
        set(value) {
            val distinct = value.distinctBy { it.song.id }.take(200)
            val array = JSONArray()
            distinct
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
                            .put("song", SongJsonCodec.toJson(entry.song))
                    )
                }
            jsonStore.writeJsonStore("playbackHistory.json", "playbackHistory.v1", array.toString())
            localDatabase?.replacePlaybackHistory(distinct)
            localDatabase?.markMigrated(HISTORY_DOMAIN)
        }

    var playbackSessions: List<PlaybackSession>
        get() = localDatabase
            ?.takeIf { it.isMigrated(SESSIONS_DOMAIN) }
            ?.readPlaybackSessions()
            ?: readPlaybackSessionsFromJson()
        set(value) {
            val distinct = value.distinctBy { it.id }.take(100)
            val array = JSONArray()
            distinct
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
            jsonStore.writeJsonStore("playbackSessions.json", "playbackSessions.v1", array.toString())
            localDatabase?.replacePlaybackSessions(distinct)
            localDatabase?.markMigrated(SESSIONS_DOMAIN)
        }

    private fun migrateToDatabaseIfNeeded() {
        val db = localDatabase ?: return
        if (!db.isMigrated(HISTORY_DOMAIN)) {
            db.replacePlaybackHistory(readPlaybackHistoryFromJson())
            db.markMigrated(HISTORY_DOMAIN)
        }
        if (!db.isMigrated(SESSIONS_DOMAIN)) {
            db.replacePlaybackSessions(readPlaybackSessionsFromJson())
            db.markMigrated(SESSIONS_DOMAIN)
        }
    }

    private fun readPlaybackHistoryFromJson(): List<PlaybackHistoryEntry> = jsonStore.readRecoverableJsonList(
        fileName = "playbackHistory.json",
        fallbackPreferenceKey = "playbackHistory.v1",
    ) { stored ->
        val array = JSONArray(stored)
        (0 until array.length()).mapNotNull { index ->
            val obj = array.optJSONObject(index) ?: return@mapNotNull null
            val song = obj.optJSONObject("song")?.let(SongJsonCodec::fromJson) ?: return@mapNotNull null
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

    private fun readPlaybackSessionsFromJson(): List<PlaybackSession> = jsonStore.readRecoverableJsonList(
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

    private companion object {
        const val HISTORY_DOMAIN = "playback_history"
        const val SESSIONS_DOMAIN = "playback_sessions"
    }
}
