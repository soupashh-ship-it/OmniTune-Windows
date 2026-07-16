package com.omnitune.app.platform

import com.omnitune.innertube.models.Album
import com.omnitune.innertube.models.Artist
import com.omnitune.innertube.models.SongItem
import org.json.JSONArray
import org.json.JSONObject

internal object SongJsonCodec {
    fun toJson(song: SongItem): JSONObject = JSONObject()
        .put("id", song.id)
        .put("title", song.title)
        .put("thumbnail", song.thumbnail)
        .put("duration", song.duration ?: JSONObject.NULL)
        .put("explicit", song.explicit)
        .put("artists", JSONArray().also { array ->
            song.artists.forEach { artist ->
                array.put(JSONObject().put("name", artist.name).put("id", artist.id ?: JSONObject.NULL))
            }
        })
        .put(
            "album",
            song.album?.let { JSONObject().put("name", it.name).put("id", it.id) } ?: JSONObject.NULL
        )

    fun fromJson(obj: JSONObject): SongItem {
        val artistsJson = obj.optJSONArray("artists") ?: JSONArray()
        val parsedArtists = (0 until artistsJson.length()).mapNotNull { index ->
            artistsJson.optJSONObject(index)?.let {
                Artist(
                    name = it.optString("name"),
                    id = it.optString("id").takeIf(String::isNotBlank),
                )
            }
        }.ifEmpty { listOf(Artist("Unknown artist", null)) }
        val albumJson = obj.optJSONObject("album")
        return SongItem(
            id = obj.optString("id"),
            title = obj.optString("title"),
            artists = parsedArtists,
            album = albumJson?.let { Album(it.optString("name"), it.optString("id")) },
            duration = if (obj.isNull("duration")) null else obj.optInt("duration"),
            thumbnail = obj.optString("thumbnail"),
            explicit = obj.optBoolean("explicit", false),
        )
    }
}
