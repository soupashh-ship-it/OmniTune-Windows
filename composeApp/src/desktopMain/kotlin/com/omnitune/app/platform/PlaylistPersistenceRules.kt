package com.omnitune.app.platform

import com.omnitune.innertube.models.SongItem

internal object PlaylistPersistenceRules {
    fun requirePlaylistName(name: String): String {
        val trimmed = name.trim()
        require(trimmed.isNotBlank()) { "Playlist name cannot be empty." }
        return trimmed
    }

    fun sanitizeTags(tags: List<String>): List<String> =
        tags.map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .take(8)

    fun distinctSongs(songs: List<SongItem>): List<SongItem> =
        songs.distinctBy { it.id }

    fun prependReplacingName(
        playlist: SavedQueuePlaylist,
        playlists: List<SavedQueuePlaylist>,
    ): List<SavedQueuePlaylist> =
        listOf(playlist) + playlists.filterNot { it.name.equals(playlist.name, ignoreCase = true) }

    fun prependReplacingId(
        playlist: SavedQueuePlaylist,
        playlists: List<SavedQueuePlaylist>,
    ): List<SavedQueuePlaylist> =
        listOf(playlist) + playlists.filterNot { it.id == playlist.id }

    fun addSongToSelectedPlaylists(
        playlists: List<SavedQueuePlaylist>,
        song: SongItem,
        selectedIds: Set<String>,
    ): Pair<List<SavedQueuePlaylist>, List<SavedQueuePlaylist>> {
        if (selectedIds.isEmpty()) return playlists to emptyList()

        val updatedPlaylists = mutableListOf<SavedQueuePlaylist>()
        val next = playlists.map { playlist ->
            if (playlist.id !in selectedIds || playlist.songs.any { it.id == song.id }) {
                playlist
            } else {
                playlist.copy(songs = playlist.songs + song).also { updatedPlaylists += it }
            }
        }
        return next to updatedPlaylists
    }

    fun moveSong(
        playlist: SavedQueuePlaylist,
        from: Int,
        to: Int,
    ): SavedQueuePlaylist {
        require(from in playlist.songs.indices && to in playlist.songs.indices) { "Invalid playlist order index." }
        val mutable = playlist.songs.toMutableList()
        val song = mutable.removeAt(from)
        mutable.add(to, song)
        return playlist.copy(songs = mutable)
    }
}
