package com.omnitune.app.service

import com.omnitune.innertube.YouTube
import com.omnitune.innertube.YouTube.SearchFilter
import com.omnitune.innertube.models.YouTubeClient
import com.omnitune.innertube.models.response.PlayerResponse
import com.omnitune.innertube.pages.*
import com.omnitune.innertube.models.YTItem
import com.omnitune.innertube.models.AlbumItem
import com.omnitune.innertube.models.BrowseEndpoint
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.models.WatchEndpoint
import com.omnitune.innertube.pages.NextResult
import com.omnitune.innertube.pages.ChartsPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class YouTubeService {
    private var initialized = false
    private val initMutex = Mutex()

    private suspend fun ensureInitialized() {
        if (!initialized) initMutex.withLock {
            if (!initialized) {
                val visitorData = YouTube.visitorData().getOrThrow()
                YouTube.visitorData = visitorData
                initialized = true
            }
        }
    }

    suspend fun search(
        query: String,
        filter: SearchFilter = SearchFilter.FILTER_SONG
    ): SearchResult = withContext(Dispatchers.IO) {
        ensureInitialized()
        YouTube.search(query, filter).getOrThrow()
    }

    suspend fun searchSuggestions(query: String): List<String> = withContext(Dispatchers.IO) {
        ensureInitialized()
        YouTube.searchSuggestions(query).getOrNull()?.queries ?: emptyList()
    }

    suspend fun getPlayer(
        videoId: String,
        client: YouTubeClient = YouTubeClient.WEB_REMIX
    ): PlayerResponse = withContext(Dispatchers.IO) {
        ensureInitialized()
        YouTube.player(videoId, client = client).getOrThrow()
    }

    suspend fun home(continuation: String? = null, params: String? = null): HomePage = withContext(Dispatchers.IO) {
        ensureInitialized()
        YouTube.home(continuation, params).getOrThrow()
    }

    suspend fun browse(browseId: String, params: String? = null): BrowseResult = withContext(Dispatchers.IO) {
        ensureInitialized()
        YouTube.browse(browseId, params).getOrThrow()
    }

    suspend fun explore(): ExplorePage = withContext(Dispatchers.IO) {
        ensureInitialized()
        YouTube.explore().getOrThrow()
    }

    suspend fun moodAndGenres(): List<MoodAndGenres> = withContext(Dispatchers.IO) {
        ensureInitialized()
        YouTube.moodAndGenres().getOrThrow()
    }

    suspend fun newReleaseAlbums(): List<AlbumItem> = withContext(Dispatchers.IO) {
        ensureInitialized()
        YouTube.newReleaseAlbums().getOrThrow()
    }

    suspend fun album(browseId: String, withSongs: Boolean = true): AlbumPage = withContext(Dispatchers.IO) {
        ensureInitialized()
        YouTube.album(browseId, withSongs).getOrThrow()
    }

    suspend fun albumSongs(playlistId: String, album: AlbumItem? = null): List<SongItem> = withContext(Dispatchers.IO) {
        ensureInitialized()
        YouTube.albumSongs(playlistId, album).getOrThrow()
    }

    suspend fun artist(browseId: String): ArtistPage = withContext(Dispatchers.IO) {
        ensureInitialized()
        YouTube.artist(browseId).getOrThrow()
    }

    suspend fun playlist(playlistId: String): PlaylistPage = withContext(Dispatchers.IO) {
        ensureInitialized()
        YouTube.playlist(playlistId).getOrThrow()
    }

    suspend fun lyrics(endpoint: BrowseEndpoint): String? = withContext(Dispatchers.IO) {
        ensureInitialized()
        YouTube.lyrics(endpoint).getOrThrow()
    }

    suspend fun related(endpoint: BrowseEndpoint): RelatedPage = withContext(Dispatchers.IO) {
        ensureInitialized()
        YouTube.related(endpoint).getOrThrow()
    }

    suspend fun next(endpoint: WatchEndpoint, continuation: String? = null): NextResult = withContext(Dispatchers.IO) {
        ensureInitialized()
        YouTube.next(endpoint, continuation).getOrThrow()
    }

    suspend fun getChartsPage(continuation: String? = null): ChartsPage = withContext(Dispatchers.IO) {
        ensureInitialized()
        YouTube.getChartsPage(continuation).getOrThrow()
    }

    suspend fun queue(videoIds: List<String>? = null, playlistId: String? = null): List<SongItem> = withContext(Dispatchers.IO) {
        ensureInitialized()
        YouTube.queue(videoIds, playlistId).getOrThrow()
    }
}
