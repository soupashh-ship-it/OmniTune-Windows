package com.omnitune.app.platform

import org.json.JSONArray
import org.json.JSONObject
import java.util.prefs.Preferences

internal class LikedSongsPersistence(
    private val prefs: Preferences,
    private val jsonStore: JsonFileStore,
    private val localDatabase: OmniLocalDatabase? = null,
) {
    init {
        migrateToDatabaseIfNeeded()
    }

    var likedSongIds: Set<String>
        get() = localDatabase
            ?.takeIf { it.isMigrated(DOMAIN) }
            ?.readLikedSongs()
            ?.map { it.song.id }
            ?.toSet()
            ?: (prefs.get("likedSongIds", "") ?: "").split(",").filter { it.isNotBlank() }.toSet()
        set(value) { prefs.put("likedSongIds", value.joinToString(",")) }

    var likedSongRecords: List<LikedSongRecord>
        get() = localDatabase
            ?.takeIf { it.isMigrated(DOMAIN) }
            ?.readLikedSongs()
            ?: readLikedSongRecordsFromJson()
        set(value) {
            val distinct = value.distinctBy { it.song.id }
            val array = JSONArray()
            distinct.forEach { record ->
                array.put(
                    JSONObject()
                        .put("likedAt", record.likedAt)
                        .put("song", SongJsonCodec.toJson(record.song))
                )
            }
            jsonStore.writeJsonStore("likedSongs.json", "likedSongs.v1", array.toString())
            localDatabase?.replaceLikedSongs(distinct)
            localDatabase?.markMigrated(DOMAIN)
            likedSongIds = distinct.map { it.song.id }.toSet()
        }

    private fun migrateToDatabaseIfNeeded() {
        val db = localDatabase ?: return
        if (db.isMigrated(DOMAIN)) return
        db.replaceLikedSongs(readLikedSongRecordsFromJson())
        db.markMigrated(DOMAIN)
    }

    private fun readLikedSongRecordsFromJson(): List<LikedSongRecord> {
        val records = jsonStore.readRecoverableJsonList(
            fileName = "likedSongs.json",
            fallbackPreferenceKey = "likedSongs.v1",
        ) { stored ->
            val array = JSONArray(stored)
            (0 until array.length()).mapNotNull { index ->
                val obj = array.optJSONObject(index) ?: return@mapNotNull null
                val song = obj.optJSONObject("song")?.let(SongJsonCodec::fromJson) ?: return@mapNotNull null
                LikedSongRecord(
                    song = song,
                    likedAt = obj.optLong("likedAt", System.currentTimeMillis()),
                ).takeIf { it.song.id.isNotBlank() }
            }.distinctBy { it.song.id }
        }
        return records.ifEmpty { emptyList() }
    }

    private companion object {
        const val DOMAIN = "liked_songs"
    }
}
