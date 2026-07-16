package com.omnitune.app.player

import com.omnitune.app.platform.LikedSongRecord
import com.omnitune.app.platform.SettingsRepository
import com.omnitune.innertube.models.SongItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class PlayerLibraryStateController(
    private val settings: SettingsRepository,
    private val findKnownSong: (String) -> SongItem?,
) {
    private val _liked = MutableStateFlow(settings.likedSongIds)
    val likedSongs: StateFlow<Set<String>> = _liked.asStateFlow()

    private val _likedSongRecords = MutableStateFlow(settings.likedSongRecords)
    val likedSongRecords: StateFlow<List<LikedSongRecord>> = _likedSongRecords.asStateFlow()

    private val _followedArtists = MutableStateFlow(settings.followedArtistIds)
    val followedArtists: StateFlow<Set<String>> = _followedArtists.asStateFlow()

    private val _pinnedLibraryCollections = MutableStateFlow(settings.pinnedLibraryCollectionIds)
    val pinnedLibraryCollections: StateFlow<Set<String>> = _pinnedLibraryCollections.asStateFlow()

    fun isLiked(id: String): Boolean = _liked.value.contains(id)

    fun toggleLike(id: String) {
        if (_liked.value.contains(id)) {
            settings.unlikeSong(id)
        } else {
            val knownSong = findKnownSong(id)
            if (knownSong != null) {
                settings.likeSong(knownSong)
            } else {
                settings.likedSongIds = settings.likedSongIds + id
            }
        }
        refreshLikedState()
    }

    fun toggleLikeSong(song: SongItem) {
        if (_liked.value.contains(song.id)) {
            settings.unlikeSong(song.id)
        } else {
            settings.likeSong(song)
        }
        refreshLikedState()
    }

    fun unlikeSongs(ids: Set<String>) {
        ids.forEach(settings::unlikeSong)
        refreshLikedState()
    }

    fun toggleFollowArtist(id: String) {
        val set = _followedArtists.value.toMutableSet()
        if (set.contains(id)) set.remove(id) else set.add(id)
        _followedArtists.value = set
        settings.followedArtistIds = set
        settings.flush()
    }

    fun togglePinnedLibraryCollection(id: String) {
        val allowed = setOf("favorites", "queue", "albums", "artists", "playlists", "downloads")
        if (id !in allowed) return
        val current = _pinnedLibraryCollections.value.toMutableSet()
        if (current.contains(id)) {
            if (current.size <= 1) return
            current.remove(id)
        } else {
            current.add(id)
        }
        _pinnedLibraryCollections.value = current
        settings.pinnedLibraryCollectionIds = current
        settings.flush()
    }

    private fun refreshLikedState() {
        _likedSongRecords.value = settings.likedSongRecords
        _liked.value = settings.likedSongIds
        settings.flush()
    }
}
