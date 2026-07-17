package com.omnitune.app.player

import com.omnitune.app.platform.SettingsRepository
import com.omnitune.app.platform.VlcjAudioEngine
import com.omnitune.app.platform.PlaybackState
import com.omnitune.app.platform.DownloadQualityMode
import com.omnitune.app.platform.OmniDownloadManager
import com.omnitune.app.platform.completedLocalFileFor
import com.omnitune.app.service.YouTubeService
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.models.YouTubeClient
import com.omnitune.innertube.models.YTItem
import com.omnitune.innertube.models.response.PlayerResponse
import com.omnitune.innertube.models.WatchEndpoint
import com.omnitune.lrclib.LrcLib
import io.ktor.http.URLBuilder
import io.ktor.http.parseQueryString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

enum class NavScreen {
    Home, Browse, Radio, Library, Songs, LikedSongs, Search, NowPlaying, Queue, Playlists, Settings,
    Artist, Album, Downloads, PlaylistDetail
}

enum class RepeatMode { OFF, ALL, ONE }

data class LyricLine(val timeMs: Long, val text: String)

sealed class LyricsResult {
    object Loading : LyricsResult()
    data class Synced(val lines: List<LyricLine>) : LyricsResult()
    data class Unsynced(val text: String) : LyricsResult()
    object NotFound : LyricsResult()
    data class Error(val message: String) : LyricsResult()
}

fun parseLrc(text: String?): List<LyricLine> {
    if (text.isNullOrBlank()) return emptyList()
    val out = mutableListOf<LyricLine>()
    val regex = Regex("\\[(\\d{2}):(\\d{2})[.:](\\d{2,3})?]")
    text.lines().forEach { line ->
        val matches = regex.findAll(line)
        val content = line.replace(regex, "").trim()
        if (content.isEmpty() && matches.none()) return@forEach
        matches.forEach { m ->
            val mm = m.groupValues[1].toLongOrNull() ?: 0
            val ss = m.groupValues[2].toLongOrNull() ?: 0
            var xxStr = m.groupValues[3]
            if (xxStr.length == 2) xxStr += "0"
            val xx = xxStr.toLongOrNull() ?: 0
            out.add(LyricLine((mm * 60 + ss) * 1000 + xx, content))
        }
    }
    return out.sortedBy { it.timeMs }
}

class PlayerViewModel(
    private val youTubeService: YouTubeService,
    private val audioEngine: VlcjAudioEngine,
    private val settings: SettingsRepository,
    private val downloadManager: OmniDownloadManager,
) : CoroutineScope {

    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO

    private var consecutiveErrors = 0
    private val navigation = PlayerNavigationController()
    val navScreen: StateFlow<NavScreen> = navigation.navScreen

    private val _currentPlaylistId = MutableStateFlow<String?>(null)
    val currentPlaylistId: StateFlow<String?> = _currentPlaylistId.asStateFlow()

    private val _currentSong = MutableStateFlow<SongItem?>(null)
    val currentSong: StateFlow<SongItem?> = _currentSong.asStateFlow()
    private var volumePersistenceJob: Job? = null

    private val _streamUrl = MutableStateFlow<String?>(null)
    val streamUrl: StateFlow<String?> = _streamUrl.asStateFlow()
    private val _discoveryGenres = MutableStateFlow<List<com.omnitune.innertube.pages.MoodAndGenres.Item>>(emptyList())
    val discoveryGenres: StateFlow<List<com.omnitune.innertube.pages.MoodAndGenres.Item>> = _discoveryGenres.asStateFlow()
    
    private val _discoveryTrending = MutableStateFlow<List<SongItem>>(emptyList())
    val discoveryTrending: StateFlow<List<SongItem>> = _discoveryTrending.asStateFlow()
    
    private val _discoveryNew = MutableStateFlow<List<YTItem>>(emptyList())
    val discoveryNew: StateFlow<List<YTItem>> = _discoveryNew.asStateFlow()

    private val _discoveryLoading = MutableStateFlow(false)
    val discoveryLoading: StateFlow<Boolean> = _discoveryLoading.asStateFlow()


    private val _volume = MutableStateFlow(settings.volume)
    val volume: StateFlow<Int> = _volume.asStateFlow()

    val playbackState: StateFlow<PlaybackState> = audioEngine.playbackState
    val position = audioEngine.position
    val playerError: StateFlow<String?> = audioEngine.error
    private val downloadController = PlayerDownloadController(
        scope = this,
        settings = settings,
        downloadManager = downloadManager,
        playSong = { playSong(it) },
    )
    val downloadTasks = downloadController.downloadTasks
    val downloadQuality: StateFlow<DownloadQualityMode> = downloadController.downloadQuality

    private val queueController = PlayerQueueController(settings)
    val queue: StateFlow<List<SongItem>> = queueController.queue
    val queueIndex: StateFlow<Int> = queueController.queueIndex
    val shuffleMode: StateFlow<Boolean> = queueController.shuffleMode
    val repeatMode: StateFlow<RepeatMode> = queueController.repeatMode
    private val relatedController = PlayerRelatedController(
        scope = this,
        youTubeService = youTubeService,
        currentSong = { _currentSong.value },
        currentPlaylistId = { _currentPlaylistId.value },
    )
    val discoveryRelated: StateFlow<List<YTItem>> = relatedController.discoveryRelated
    val relatedLoading: StateFlow<Boolean> = relatedController.relatedLoading
    val relatedError: StateFlow<String?> = relatedController.relatedError
    private val radioController = PlayerRadioController(
        scope = this,
        youTubeService = youTubeService,
        queueController = queueController,
        setCurrentSong = { _currentSong.value = it },
        startPlayback = { startPlaybackFromController(it) },
    )

    private val libraryState = PlayerLibraryStateController(settings, ::findKnownSong)
    val likedSongs: StateFlow<Set<String>> = libraryState.likedSongs
    val likedSongRecords = libraryState.likedSongRecords
    val followedArtists: StateFlow<Set<String>> = libraryState.followedArtists
    val pinnedLibraryCollections: StateFlow<Set<String>> = libraryState.pinnedLibraryCollections

    private val _recentSearches = MutableStateFlow(settings.recentSearches)
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()
    private val searchController = PlayerSearchController(
        scope = this,
        youTubeService = youTubeService,
        settings = settings,
        recentSearches = _recentSearches,
    )
    val searchResults: StateFlow<List<YTItem>> = searchController.searchResults
    val searchLoading: StateFlow<Boolean> = searchController.searchLoading
    val searchError: StateFlow<String?> = searchController.searchError
    val playlistResults: StateFlow<List<YTItem>> = searchController.playlistResults
    val playlistLoading: StateFlow<Boolean> = searchController.playlistLoading
    val playlistError: StateFlow<String?> = searchController.playlistError

    private val _savedQueuePlaylists = MutableStateFlow(settings.savedQueuePlaylists)
    val savedQueuePlaylists = _savedQueuePlaylists.asStateFlow()
    private val playlistController = PlayerPlaylistController(
        settings = settings,
        savedQueuePlaylists = _savedQueuePlaylists,
        currentPlaylistId = _currentPlaylistId,
        clearCurrentPlaylist = { _currentPlaylistId.value = null },
        navigateTo = { screen -> navigateTo(screen) },
    )

    private val playbackRequestGate = PlaybackRequestGate()

    private val listenTracker = PlayerListenTracker(
        scope = this,
        settings = settings,
        playbackState = playbackState,
        onPlaybackRecovered = { consecutiveErrors = 0 },
        onPlaybackError = {
            consecutiveErrors++
            if (consecutiveErrors < 3) {
                nextTrack()
            }
        },
    )
    val playbackHistory = listenTracker.playbackHistory
    val playbackSessions = listenTracker.playbackSessions

    fun clearRecentSearches() {
        settings.clearRecentSearches()
        settings.flush()
        _recentSearches.value = emptyList()
    }

    init {
        audioEngine.onTrackFinished = { onTrackFinished() }
        audioEngine.setVolume(_volume.value)
        loadDiscoveryData()
    }

    private fun startListenTracking(item: SongItem) {
        listenTracker.start(
            song = item,
            durationMs = item.duration?.times(1000L) ?: position.value.lengthMs,
        )
    }

    private fun finalizeActiveListen(completed: Boolean) {
        listenTracker.finalize(completed)
    }

    private fun nextPlaybackRequestToken(): Long = playbackRequestGate.nextToken()

    private fun isCurrentPlaybackRequest(token: Long, item: SongItem): Boolean =
        playbackRequestGate.isCurrent(token, item, _currentSong.value)

    private fun loadDiscoveryData() {
        launch {
            _discoveryLoading.value = true
            val trendingSongs = runCatching { youTubeService.search("trending hits").items.filterIsInstance<SongItem>().take(10) }.getOrDefault(emptyList())
            val genres = runCatching { youTubeService.moodAndGenres().flatMap { it.items }.shuffled().take(12) }.getOrDefault(emptyList())
            val newContent = runCatching { youTubeService.home().sections.flatMap { it.items }.shuffled().take(10) }.getOrDefault(emptyList())
            
            _discoveryTrending.value = trendingSongs
            _discoveryGenres.value = genres
            _discoveryNew.value = newContent
            _discoveryLoading.value = false
        }
    }

    fun onTrackFinished() {
        finalizeActiveListen(completed = true)
        val mode = queueController.repeatState.value
        if (mode == RepeatMode.ONE && queueController.queueIndexState.value >= 0) {
            playQueueIndex(queueController.queueIndexState.value)
            return
        }
        nextTrack()
    }

    fun toggleShuffle() {
        queueController.toggleShuffle()
    }

    fun cycleRepeat() {
        queueController.cycleRepeat()
    }

    fun isLiked(id: String): Boolean = libraryState.isLiked(id)

    fun toggleLike(id: String) {
        libraryState.toggleLike(id)
    }

    fun toggleLikeSong(song: SongItem) {
        libraryState.toggleLikeSong(song)
    }

    fun unlikeSongs(ids: Set<String>) {
        libraryState.unlikeSongs(ids)
    }

    private fun findKnownSong(id: String): SongItem? {
        return sequenceOf(
            listOfNotNull(_currentSong.value),
            queueController.queueItems.value,
            _discoveryTrending.value,
            _discoveryNew.value.filterIsInstance<SongItem>(),
            searchResults.value.filterIsInstance<SongItem>(),
            playlistResults.value.filterIsInstance<SongItem>(),
            _savedQueuePlaylists.value.flatMap { it.songs },
        ).flatten().firstOrNull { it.id == id }
    }

    fun toggleFollowArtist(id: String) {
        libraryState.toggleFollowArtist(id)
    }

    fun togglePinnedLibraryCollection(id: String) {
        libraryState.togglePinnedLibraryCollection(id)
    }

    private val _currentArtistId = MutableStateFlow<String?>(null)
    val currentArtistId: StateFlow<String?> = _currentArtistId.asStateFlow()
    private val _currentAlbumId = MutableStateFlow<String?>(null)
    val currentAlbumId: StateFlow<String?> = _currentAlbumId.asStateFlow()

    fun openArtist(id: String) {
        _currentArtistId.value = id
        navigateTo(NavScreen.Artist)
    }

    fun openPlaylist(id: String) {
        _currentPlaylistId.value = id
        navigateTo(NavScreen.PlaylistDetail)
    }

    fun openAlbum(id: String) {
        _currentAlbumId.value = id
        navigateTo(NavScreen.Album)
    }


    private val _lyricsResult = MutableStateFlow<LyricsResult>(LyricsResult.NotFound)
    val lyricsResult: StateFlow<LyricsResult> = _lyricsResult.asStateFlow()
    
    private var lyricsJob: Job? = null

    fun loadLyrics() {
        val song = _currentSong.value ?: return
        lyricsJob?.cancel()
        lyricsJob = launch {
            val capturedId = song.id
            _lyricsResult.value = LyricsResult.Loading
            runCatching {
                com.omnitune.lrclib.LrcLib.getLyrics(
                    song.title,
                    song.artists.firstOrNull()?.name ?: "",
                    song.duration ?: -1
                )
            }
                .onSuccess { track ->
                    if (!isActive || _currentSong.value?.id != capturedId) return@onSuccess
                    val synced = track.syncedLyrics
                    val plain = track.plainLyrics
                    if (synced != null) {
                        _lyricsResult.value = LyricsResult.Synced(parseLrc(synced))
                    } else if (plain != null) {
                        _lyricsResult.value = LyricsResult.Unsynced(plain)
                    } else {
                        _lyricsResult.value = LyricsResult.NotFound
                    }
                }
                .onFailure {
                    if (isActive && _currentSong.value?.id == capturedId) {
                        _lyricsResult.value = LyricsResult.NotFound
                    }
                }
        }
    }

    val canGoBack: StateFlow<Boolean> = navigation.canGoBack
    val canGoForward: StateFlow<Boolean> = navigation.canGoForward

    fun navigateTo(screen: NavScreen) {
        navigation.navigateTo(screen, canOpenNowPlaying = _currentSong.value != null)
    }

    fun back() {
        navigation.back()
    }

    fun forward() {
        navigation.forward()
    }

    fun search(query: String) {
        searchController.search(query)
    }

    fun searchPlaylists(query: String) {
        searchController.searchPlaylists(query)
    }

    private fun resolveFormatUrl(player: PlayerResponse, videoId: String, client: YouTubeClient): String? {
        com.omnitune.app.platform.OmniLogger.info("PlayerVM", "playabilityStatus: ${player.playabilityStatus.status} reason=${player.playabilityStatus.reason}")

        if (player.streamingData == null) {
            com.omnitune.app.platform.OmniLogger.info("PlayerVM", "streamingData is null!")
            return null
        }

        val formats = (player.streamingData?.adaptiveFormats?.filter { it.isAudio } ?: emptyList()) +
            (player.streamingData?.formats ?: emptyList())

        if (formats.isEmpty()) {
            com.omnitune.app.platform.OmniLogger.info("PlayerVM", "no audio format found")
            return null
        }

        for (format in formats) {
            val url = com.omnitune.innertube.pages.NewPipeUtils.getStreamUrl(format, videoId, client).getOrNull()
            if (url != null) {
                return url
            }
        }

        com.omnitune.app.platform.OmniLogger.info("PlayerVM", "could not resolve URL for any format")
        return null
    }

    fun playArtist(browseId: String) {
        launch {
            val artist = runCatching { youTubeService.artist(browseId) }.getOrNull()
            val songsSection = artist?.sections?.firstOrNull { it.title.contains("Songs", ignoreCase = true) }
            val songs = songsSection?.items?.filterIsInstance<com.omnitune.innertube.models.SongItem>()
            if (!songs.isNullOrEmpty()) {
                val first = queueController.setQueue(songs) ?: return@launch
                _currentSong.value = first
                val token = nextPlaybackRequestToken()
                doPlay(first, token)
            }
        }
    }

    fun playSong(item: SongItem, index: Int = -1) {
        if (index >= 0) {
            val songs = searchResults.value.filterIsInstance<SongItem>()
            queueController.setQueue(songs, index)
        } else {
            queueController.selectExistingOrAppend(item)
        }
        _currentSong.value = item
        val token = nextPlaybackRequestToken()
        launch { doPlay(item, token) }
    }

    fun playSongList(items: List<SongItem>, startIndex: Int = 0) {
        val songs = items.distinctBy { it.id }
        if (songs.isEmpty()) return
        val selected = queueController.setQueue(songs, startIndex) ?: return
        _currentSong.value = selected
        val token = nextPlaybackRequestToken()
        launch { doPlay(selected, token) }
    }

    fun playShuffledSongs(items: List<SongItem>) {
        val selected = queueController.setShuffledQueue(items) ?: return
        _currentSong.value = selected
        val token = nextPlaybackRequestToken()
        launch { doPlay(selected, token) }
    }
    fun playAlbum(browseId: String) {
        launch {
            val album = runCatching { youTubeService.album(browseId) }.getOrNull()
            val songs = album?.songs
            if (!songs.isNullOrEmpty()) {
                val first = queueController.setQueue(songs) ?: return@launch
                _currentSong.value = first
                val token = nextPlaybackRequestToken()
                doPlay(first, token)
            }
        }
    }
    
    fun playPlaylist(playlistId: String) {
        val local = _savedQueuePlaylists.value.firstOrNull { it.id == playlistId }
        if (local != null) {
            val songs = local.songs
            if (songs.isNotEmpty()) {
                val first = queueController.setQueue(songs) ?: return
                _currentSong.value = first
                val token = nextPlaybackRequestToken()
                launch { doPlay(first, token) }
            }
            return
        }
        launch {
            val playlist = runCatching { youTubeService.playlist(playlistId) }.getOrNull()
            val songs = playlist?.songs
            if (!songs.isNullOrEmpty()) {
                val first = queueController.setQueue(songs) ?: return@launch
                _currentSong.value = first
                val token = nextPlaybackRequestToken()
                doPlay(first, token)
            }
        }
    }

    private suspend fun startPlaybackFromController(item: SongItem) {
        val token = nextPlaybackRequestToken()
        doPlay(item, token)
    }

    private suspend fun doPlay(item: SongItem, requestToken: Long) {
        if (!isCurrentPlaybackRequest(requestToken, item)) return
        _lyricsResult.value = LyricsResult.Loading
        lyricsJob?.cancel()
        _streamUrl.value = null
        startListenTracking(item)
        relatedController.loadRelatedFor(item)
        downloadManager.completedLocalFileFor(item.id)?.absolutePath?.let { localPath ->
            if (!isCurrentPlaybackRequest(requestToken, item)) return
            _streamUrl.value = localPath
            audioEngine.play(localPath)
            audioEngine.setVolume(_volume.value)
            loadLyrics()
            return
        }
        val clients = listOf(
            YouTubeClient.WEB_REMIX,
            YouTubeClient.WEB,
            YouTubeClient.ANDROID_VR_NO_AUTH,
            YouTubeClient.IOS,
            YouTubeClient.ANDROID_MUSIC,
        )
        var url: String? = null
        for (client in clients) {
            com.omnitune.app.platform.OmniLogger.info("PlayerVM", "trying client: ${client.clientName}")
            val resolved = runCatching {
                val player = youTubeService.getPlayer(item.id, client)
                resolveFormatUrl(player, item.id, client)
            }.getOrNull()
            if (resolved != null) {
                com.omnitune.app.platform.OmniLogger.info("PlayerVM", "resolved URL with ${client.clientName}")
                url = resolved
                break
            }
        }
        val resolvedUrl = url
        if (resolvedUrl != null) {
            if (!isCurrentPlaybackRequest(requestToken, item)) return
            com.omnitune.app.platform.OmniLogger.info("PlayerVM", "playing URL: ${resolvedUrl.take(100)}...")
            _streamUrl.value = resolvedUrl
            audioEngine.play(resolvedUrl)
            audioEngine.setVolume(_volume.value)
            loadLyrics()
        } else {
            if (!isCurrentPlaybackRequest(requestToken, item)) return
            finalizeActiveListen(completed = false)
            _lyricsResult.value = LyricsResult.NotFound
            com.omnitune.app.platform.OmniLogger.error("PlayerVM", "all clients failed to resolve URL")
            consecutiveErrors++
            if (consecutiveErrors < 3) {
                nextTrack()
            }
        }
    }

    fun retryRelated() {
        relatedController.retryRelated()
    }

    fun playQueueIndex(index: Int) {
        val song = queueController.navigateToIndex(index) ?: return
        _currentSong.value = song
        val token = nextPlaybackRequestToken()
        launch { doPlay(song, token) }
        radioController.maybeRequestContinuation()
    }

    fun nextTrack() {
        queueController.nextIndex()?.let(::playQueueIndex)
    }

    fun previousTrack() {
        queueController.previousIndex()?.let(::playQueueIndex)
    }

    fun addToQueue(item: SongItem) {
        queueController.addToQueue(item)
    }

    fun saveQueueAsPlaylist(name: String): Result<String> =
        playlistController.saveQueueAsPlaylist(name, queueController.queueItems.value)

    fun saveSongsAsPlaylist(name: String, songs: List<SongItem>): Result<String> =
        playlistController.saveSongsAsPlaylist(name, songs)

    fun createPlaylist(name: String, description: String = "", tags: List<String> = emptyList()): Result<String> =
        playlistController.createPlaylist(name, description, tags)

    fun updateSavedPlaylistMetadata(
        id: String,
        name: String,
        description: String,
        tags: List<String>,
        coverPath: String?,
    ): Result<String> =
        playlistController.updateSavedPlaylistMetadata(id, name, description, tags, coverPath)

    fun addSongToSavedPlaylists(song: SongItem, playlistIds: Set<String>): Result<Int> =
        playlistController.addSongToSavedPlaylists(song, playlistIds)

    fun removeSongFromSavedPlaylist(playlistId: String, songId: String): Result<String> =
        playlistController.removeSongFromSavedPlaylist(playlistId, songId)

    fun moveSavedPlaylistSong(playlistId: String, from: Int, to: Int): Result<String> =
        playlistController.moveSavedPlaylistSong(playlistId, from, to)

    fun deleteSavedPlaylist(playlistId: String): Result<Unit> =
        playlistController.deleteSavedPlaylist(playlistId)

    fun setDownloadQuality(quality: DownloadQualityMode) {
        downloadController.setDownloadQuality(quality)
    }

    fun downloadSong(item: SongItem, quality: DownloadQualityMode? = null) {
        downloadController.downloadSong(item, quality)
    }

    fun downloadSongs(items: List<SongItem>, quality: DownloadQualityMode? = null) {
        downloadController.downloadSongs(items, quality)
    }

    fun playDownload(id: String) {
        downloadController.playDownload(id)
    }

    fun pauseDownload(id: String) {
        downloadController.pauseDownload(id)
    }

    fun resumeDownload(id: String) {
        downloadController.resumeDownload(id)
    }

    fun retryDownload(id: String) {
        downloadController.retryDownload(id)
    }

    fun deleteDownload(id: String) {
        downloadController.deleteDownload(id)
    }

    fun pauseAllDownloads() {
        downloadController.pauseAllDownloads()
    }

    fun resumeAllDownloads() {
        downloadController.resumeAllDownloads()
    }

    fun playNext(item: SongItem) {
        queueController.playNext(item)
    }

    fun removeFromQueue(index: Int) {
        val result = queueController.removeFromQueue(index) ?: return
        if (result.removedCurrent) {
            _currentSong.value = null
            audioEngine.stop()
        }
    }

    fun moveQueueItem(from: Int, to: Int) {
        queueController.moveQueueItem(from, to)
    }

    fun clearQueue() {
        finalizeActiveListen(completed = false)
        radioController.cancelSession()
        queueController.clearQueue()
        _currentSong.value = null
        audioEngine.stop()
    }

    fun startRadio(seedId: String, type: String) {
        radioController.startRadio(seedId, type)
    }

    fun startRadio(endpoint: WatchEndpoint) {
        radioController.startRadio(endpoint)
    }

    fun togglePlayPause() {
        when (playbackState.value) {
            PlaybackState.PLAYING -> audioEngine.pause()
            PlaybackState.PAUSED -> audioEngine.resume()
            else -> {}
        }
    }

    fun stop() {
        finalizeActiveListen(completed = false)
        radioController.cancelSession()
        audioEngine.stop()
        _currentSong.value = null
        queueController.queueIndexState.value = -1
        _streamUrl.value = null
    }

    fun seek(timeMs: Long) {
        audioEngine.seek(timeMs)
    }

    fun seekRelative(deltaMs: Long) {
        audioEngine.seekRelative(deltaMs)
    }

    fun setVolume(vol: Int) {
        _volume.value = vol.coerceIn(0, 200)
        settings.volume = _volume.value
        volumePersistenceJob?.cancel()
        volumePersistenceJob = launch {
            delay(250)
            runCatching { settings.flush() }
        }
        audioEngine.setVolume(_volume.value)
    }
}
