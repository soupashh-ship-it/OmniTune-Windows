package com.omnitune.app.platform

import org.json.JSONArray
import org.json.JSONObject
import java.util.prefs.Preferences

internal class LikedSongsPersistence(
    private val prefs: Preferences,
    private val jsonStore: JsonFileStore,
) {
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
                        .put("song", SongJsonCodec.toJson(record.song))
                )
            }
            jsonStore.writeJsonStore("likedSongs.json", "likedSongs.v1", array.toString())
            likedSongIds = distinct.map { it.song.id }.toSet()
        }

    fun readLikedSongRecords(): List<LikedSongRecord> {
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
}
