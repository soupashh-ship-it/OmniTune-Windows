package com.omnitune.app.player

import com.omnitune.app.platform.SavedQueuePlaylist
import com.omnitune.app.platform.SettingsRepository
import com.omnitune.innertube.models.SongItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class PlayerPlaylistController(
    private val settings: SettingsRepository,
    private val savedQueuePlaylists: MutableStateFlow<List<SavedQueuePlaylist>>,
    private val currentPlaylistId: StateFlow<String?>,
    private val clearCurrentPlaylist: () -> Unit,
    private val navigateTo: (NavScreen) -> Unit,
) {
    fun saveQueueAsPlaylist(name: String, queue: List<SongItem>): Result<String> = runCatching {
        val saved = settings.saveQueueAsPlaylist(name, queue)
        refreshSavedPlaylists()
        saved.name
    }

    fun saveSongsAsPlaylist(name: String, songs: List<SongItem>): Result<String> = runCatching {
        val saved = settings.saveSongsAsPlaylist(name, songs)
        refreshSavedPlaylists()
        saved.name
    }

    fun createPlaylist(name: String, description: String = "", tags: List<String> = emptyList()): Result<String> = runCatching {
        val saved = settings.createPlaylist(name, description, tags)
        refreshSavedPlaylists()
        saved.id
    }

    fun updateSavedPlaylistMetadata(
        id: String,
        name: String,
        description: String,
        tags: List<String>,
        coverPath: String?,
    ): Result<String> = runCatching {
        val updated = settings.updatePlaylistMetadata(id, name, description, tags, coverPath)
        refreshSavedPlaylists()
        updated.name
    }

    fun addSongToSavedPlaylists(song: SongItem, playlistIds: Set<String>): Result<Int> = runCatching {
        val updated = settings.addSongToPlaylists(song, playlistIds)
        refreshSavedPlaylists()
        updated.size
    }

    fun removeSongFromSavedPlaylist(playlistId: String, songId: String): Result<String> = runCatching {
        val updated = settings.removeSongFromPlaylist(playlistId, songId)
        refreshSavedPlaylists()
        updated.name
    }

    fun moveSavedPlaylistSong(playlistId: String, from: Int, to: Int): Result<String> = runCatching {
        val updated = settings.movePlaylistSong(playlistId, from, to)
        refreshSavedPlaylists()
        updated.name
    }

    fun deleteSavedPlaylist(playlistId: String): Result<Unit> = runCatching {
        settings.deletePlaylist(playlistId)
        refreshSavedPlaylists()
        if (currentPlaylistId.value == playlistId) {
            clearCurrentPlaylist()
            navigateTo(NavScreen.Playlists)
        }
    }

    private fun refreshSavedPlaylists() {
        savedQueuePlaylists.value = settings.savedQueuePlaylists
    }
}
