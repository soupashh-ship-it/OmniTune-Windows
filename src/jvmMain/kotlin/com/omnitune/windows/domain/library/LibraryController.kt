package com.omnitune.windows.domain.library

import com.omnitune.windows.db.OmniDatabase
import com.omnitune.windows.db.SongEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LibraryController(
    private val scope: CoroutineScope,
    private val database: OmniDatabase
) {
    private val _likedSongs = MutableStateFlow<List<SongEntity>>(emptyList())
    val likedSongs: StateFlow<List<SongEntity>> = _likedSongs.asStateFlow()

    fun loadLikedSongs() {
        scope.launch {
            _likedSongs.value = database.libraryQueries.getAllLikedSongs().executeAsList()
        }
    }
}
