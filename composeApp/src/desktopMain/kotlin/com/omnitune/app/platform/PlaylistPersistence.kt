package com.omnitune.app.platform

import com.omnitune.innertube.models.SongItem
import org.json.JSONArray
import org.json.JSONObject

internal class PlaylistPersistence(
    private val jsonStore: JsonFileStore,
    private val flush: () -> Unit,
    private val playlistCoverStore: PlaylistCoverStore = PlaylistCoverStore(null),
) {
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
                            playlist.songs.forEach { songs.put(SongJsonCodec.toJson(it)) }
                        })
                )
            }
            jsonStore.writeJsonStore("savedQueuePlaylists.json", "savedQueuePlaylists.v1", array.toString())
        }

    fun saveQueueAsPlaylist(name: String, songs: List<SongItem>): SavedQueuePlaylist =
        saveSongsAsPlaylist(name, songs)

    fun saveSongsAsPlaylist(name: String, songs: List<SongItem>): SavedQueuePlaylist {
        val trimmed = PlaylistPersistenceRules.requirePlaylistName(name)
        require(songs.isNotEmpty()) { "Playlist has no loaded songs." }

        val now = System.currentTimeMillis()
        val playlist = SavedQueuePlaylist(
            id = "local-queue-$now",
            name = trimmed,
            createdAt = now,
            songs = PlaylistPersistenceRules.distinctSongs(songs),
        )
        savedQueuePlaylists = PlaylistPersistenceRules.prependReplacingName(playlist, savedQueuePlaylists)
        flush()
        return playlist
    }

    fun createPlaylist(name: String, description: String = "", tags: List<String> = emptyList()): SavedQueuePlaylist {
        val trimmed = PlaylistPersistenceRules.requirePlaylistName(name)
        val now = System.currentTimeMillis()
        val playlist = SavedQueuePlaylist(
            id = "local-playlist-$now",
            name = trimmed,
            createdAt = now,
            songs = emptyList(),
            description = description.trim(),
            tags = PlaylistPersistenceRules.sanitizeTags(tags),
        )
        savedQueuePlaylists = PlaylistPersistenceRules.prependReplacingName(playlist, savedQueuePlaylists)
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
        val trimmed = PlaylistPersistenceRules.requirePlaylistName(name)
        val playlists = savedQueuePlaylists
        val existing = playlists.firstOrNull { it.id == id } ?: error("Playlist not found.")
        val updated = existing.copy(
            name = trimmed,
            description = description.trim().take(300),
            tags = PlaylistPersistenceRules.sanitizeTags(tags),
            coverPath = playlistCoverStore.importCover(existing.id, coverPath),
        )
        savedQueuePlaylists = PlaylistPersistenceRules.prependReplacingId(updated, playlists)
        flush()
        return updated
    }

    fun addSongToPlaylists(song: SongItem, playlistIds: Set<String>): List<SavedQueuePlaylist> {
        if (playlistIds.isEmpty()) return emptyList()
        val selectedIds = playlistIds.filter { it.isNotBlank() }.toSet()
        val (playlists, updatedPlaylists) = PlaylistPersistenceRules.addSongToSelectedPlaylists(
            playlists = savedQueuePlaylists,
            song = song,
            selectedIds = selectedIds,
        )
        savedQueuePlaylists = playlists
        flush()
        return updatedPlaylists
    }

    fun removeSongFromPlaylist(playlistId: String, songId: String): SavedQueuePlaylist {
        val playlists = savedQueuePlaylists
        val existing = playlists.firstOrNull { it.id == playlistId } ?: error("Playlist not found.")
        val updated = existing.copy(songs = existing.songs.filterNot { it.id == songId })
        savedQueuePlaylists = PlaylistPersistenceRules.prependReplacingId(updated, playlists)
        flush()
        return updated
    }

    fun movePlaylistSong(playlistId: String, from: Int, to: Int): SavedQueuePlaylist {
        val playlists = savedQueuePlaylists
        val existing = playlists.firstOrNull { it.id == playlistId } ?: error("Playlist not found.")
        val updated = PlaylistPersistenceRules.moveSong(existing, from, to)
        savedQueuePlaylists = PlaylistPersistenceRules.prependReplacingId(updated, playlists)
        flush()
        return updated
    }

    fun deletePlaylist(playlistId: String) {
        savedQueuePlaylists = savedQueuePlaylists.filterNot { it.id == playlistId }
        flush()
    }

    private fun readPlaylistList(): List<SavedQueuePlaylist> = jsonStore.readRecoverableJsonList(
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
                songs = (0 until songsJson.length()).mapNotNull {
                    songsJson.optJSONObject(it)?.let(SongJsonCodec::fromJson)
                },
            ).takeIf { it.id.isNotBlank() && it.name.isNotBlank() }
        }.distinctBy { it.id }
    }
}
