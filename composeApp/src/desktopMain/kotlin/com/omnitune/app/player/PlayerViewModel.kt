package com.omnitune.app.player

import com.omnitune.app.platform.SettingsRepository
import com.omnitune.app.platform.VlcjAudioEngine
import com.omnitune.app.platform.PlaybackState
import com.omnitune.app.platform.PlaybackSession
import com.omnitune.app.platform.DownloadQualityMode
import com.omnitune.app.platform.DownloadRequest
import com.omnitune.app.platform.DownloadTask
import com.omnitune.app.platform.OmniDownloadManager
import com.omnitune.app.platform.completedLocalFileFor
import com.omnitune.app.service.YouTubeService
import com.omnitune.innertube.YouTube
import com.omnitune.innertube.models.Album
import com.omnitune.innertube.models.Artist
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.models.YouTubeClient
import com.omnitune.innertube.models.YTItem
import com.omnitune.innertube.models.response.PlayerResponse
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
import kotlinx.coroutines.async
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlin.coroutines.CoroutineContext

enum class NavScreen {
    Home, Browse, Radio, Library, Search, NowPlaying, Queue, Playlists, Settings,
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

    private data class ActiveListen(
        val song: SongItem,
        val startedAt: Long,
        val durationMs: Long,
        val accumulatedPlayedMs: Long = 0L,
        val lastResumeAt: Long? = null,
    ) {
        fun accumulateUntil(now: Long): ActiveListen {
            val resumedAt = lastResumeAt ?: return this
            return copy(
                accumulatedPlayedMs = accumulatedPlayedMs + (now - resumedAt).coerceAtLeast(0L),
                lastResumeAt = null,
            )
        }
    }

    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO

    private val _navScreen = MutableStateFlow(NavScreen.Home)
    val navScreen: StateFlow<NavScreen> = _navScreen.asStateFlow()

    private val _searchResults = MutableStateFlow<List<YTItem>>(emptyList())
    val searchResults: StateFlow<List<YTItem>> = _searchResults.asStateFlow()

    private val _searchLoading = MutableStateFlow(false)
    val searchLoading: StateFlow<Boolean> = _searchLoading.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()
    private var activeSearchJob: Job? = null
    private var activeSearchToken: Long = 0L

    private val _playlistResults = MutableStateFlow<List<YTItem>>(emptyList())
    val playlistResults: StateFlow<List<YTItem>> = _playlistResults.asStateFlow()

    private val _currentPlaylistId = MutableStateFlow<String?>(null)
    val currentPlaylistId: StateFlow<String?> = _currentPlaylistId.asStateFlow()

    private val _playlistLoading = MutableStateFlow(false)
    val playlistLoading: StateFlow<Boolean> = _playlistLoading.asStateFlow()

    private val _playlistError = MutableStateFlow<String?>(null)
    val playlistError: StateFlow<String?> = _playlistError.asStateFlow()

    private val _currentSong = MutableStateFlow<SongItem?>(null)
    val currentSong: StateFlow<SongItem?> = _currentSong.asStateFlow()

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
    val downloadTasks: StateFlow<List<DownloadTask>> = downloadManager.tasks

    private val _downloadQuality = MutableStateFlow(settings.downloadQualityMode)
    val downloadQuality: StateFlow<DownloadQualityMode> = _downloadQuality.asStateFlow()

    private val _queue = MutableStateFlow<List<SongItem>>(emptyList())
    val queue: StateFlow<List<SongItem>> = _queue.asStateFlow()

    private val _queueIndex = MutableStateFlow(-1)
    val queueIndex: StateFlow<Int> = _queueIndex.asStateFlow()

    private val _shuffle = MutableStateFlow(settings.shuffleEnabled)
    val shuffleMode: StateFlow<Boolean> = _shuffle.asStateFlow()

    private val _repeat = MutableStateFlow(
        when (settings.repeatMode) {
            1 -> RepeatMode.ALL
            2 -> RepeatMode.ONE
            else -> RepeatMode.OFF
        }
    )
    val repeatMode: StateFlow<RepeatMode> = _repeat.asStateFlow()

    private val _liked = MutableStateFlow(settings.likedSongIds)
    val likedSongs: StateFlow<Set<String>> = _liked.asStateFlow()

    private val _recentSearches = MutableStateFlow(settings.recentSearches)
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    private val _savedQueuePlaylists = MutableStateFlow(settings.savedQueuePlaylists)
    val savedQueuePlaylists = _savedQueuePlaylists.asStateFlow()

    private val _playbackHistory = MutableStateFlow(settings.playbackHistory)
    val playbackHistory = _playbackHistory.asStateFlow()
    private val _playbackSessions = MutableStateFlow(settings.playbackSessions)
    val playbackSessions = _playbackSessions.asStateFlow()
    private var activeListen: ActiveListen? = null

    fun clearRecentSearches() {
        settings.clearRecentSearches()
        settings.flush()
        _recentSearches.value = emptyList()
    }

    init {
        audioEngine.onTrackFinished = { onTrackFinished() }
        observePlaybackLifecycle()
        loadDiscoveryData()
    }

    private fun observePlaybackLifecycle() {
        launch {
            playbackState.collect { state ->
                val listen = activeListen ?: return@collect
                val now = System.currentTimeMillis()
                when (state) {
                    PlaybackState.PLAYING -> {
                        activeListen = if (listen.lastResumeAt == null) listen.copy(lastResumeAt = now) else listen
                    }
                    PlaybackState.PAUSED, PlaybackState.BUFFERING, PlaybackState.STOPPED -> {
                        activeListen = listen.accumulateUntil(now)
                    }
                    PlaybackState.ERROR, PlaybackState.IDLE -> {
                        activeListen = listen.accumulateUntil(now)
                        finalizeActiveListen(completed = false)
                    }
                }
            }
        }
    }

    private fun startListenTracking(item: SongItem) {
        finalizeActiveListen(completed = false)
        activeListen = ActiveListen(
            song = item,
            startedAt = System.currentTimeMillis(),
            durationMs = (item.duration?.times(1000L) ?: position.value.lengthMs).coerceAtLeast(0L),
        )
    }

    private fun finalizeActiveListen(completed: Boolean) {
        val listen = activeListen?.accumulateUntil(System.currentTimeMillis()) ?: return
        activeListen = null
        val persisted = settings.recordMeaningfulPlayback(
            song = listen.song,
            startedAt = listen.startedAt,
            accumulatedPlayedMs = listen.accumulatedPlayedMs,
            trackDurationMs = listen.durationMs,
            completed = completed,
        )
        if (persisted != null) {
            _playbackHistory.value = settings.playbackHistory
            _playbackSessions.value = settings.playbackSessions
        }
    }

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
        val mode = _repeat.value
        if (mode == RepeatMode.ONE && _queueIndex.value >= 0) {
            playQueueIndex(_queueIndex.value)
            return
        }
        nextTrack()
    }

    fun toggleShuffle() {
        _shuffle.value = !_shuffle.value
        settings.shuffleEnabled = _shuffle.value
        settings.flush()
    }

    fun cycleRepeat() {
        _repeat.value = when (_repeat.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        settings.repeatMode = when (_repeat.value) {
            RepeatMode.OFF -> 0
            RepeatMode.ALL -> 1
            RepeatMode.ONE -> 2
        }
        settings.flush()
    }

    fun isLiked(id: String): Boolean = _liked.value.contains(id)

    fun toggleLike(id: String) {
        val set = _liked.value.toMutableSet()
        if (set.contains(id)) set.remove(id) else set.add(id)
        _liked.value = set
        settings.likedSongIds = set
        settings.flush()
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

    private val history = mutableListOf<NavScreen>()
    private var historyIndex = -1
    private val _canGoBack = MutableStateFlow(false)
    val canGoBack: StateFlow<Boolean> = _canGoBack.asStateFlow()
    private val _canGoForward = MutableStateFlow(false)
    val canGoForward: StateFlow<Boolean> = _canGoForward.asStateFlow()

    fun navigateTo(screen: NavScreen) {
        if (screen == NavScreen.NowPlaying && _currentSong.value == null) return
        if (historyIndex >= 0 && historyIndex < history.size - 1) {
            history.subList(historyIndex + 1, history.size).clear()
        }
        if (historyIndex < 0 || history[historyIndex] != screen) {
            history.add(screen)
            historyIndex = history.lastIndex
        }
        _navScreen.value = screen
        updateNavFlags()
    }

    fun back() {
        if (historyIndex > 0) {
            historyIndex--
            _navScreen.value = history[historyIndex]
            updateNavFlags()
        }
    }

    fun forward() {
        if (historyIndex < history.size - 1) {
            historyIndex++
            _navScreen.value = history[historyIndex]
            updateNavFlags()
        }
    }

    private fun updateNavFlags() {
        _canGoBack.value = historyIndex > 0
        _canGoForward.value = historyIndex < history.size - 1
    }

    fun search(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        settings.addRecentSearch(trimmed)
        settings.flush()
        _recentSearches.value = settings.recentSearches
        val searchToken = ++activeSearchToken
        activeSearchJob?.cancel()
        activeSearchJob = launch {
            _searchLoading.value = true
            _searchError.value = null
            _searchResults.value = emptyList()
            val searchResults = runCatching {
                val songs = async { runCatching { youTubeService.search(trimmed, YouTube.SearchFilter.FILTER_SONG).items } }
                val artists = async { runCatching { youTubeService.search(trimmed, YouTube.SearchFilter.FILTER_ARTIST).items } }
                val albums = async { runCatching { youTubeService.search(trimmed, YouTube.SearchFilter.FILTER_ALBUM).items } }
                val playlists = async { runCatching { youTubeService.search(trimmed, YouTube.SearchFilter.FILTER_FEATURED_PLAYLIST).items } }
                listOf(songs.await(), artists.await(), albums.await(), playlists.await())
            }

            if (searchToken == activeSearchToken) {
                searchResults.onSuccess { categoryResults ->
                    val successes = categoryResults.mapNotNull { it.getOrNull() }
                    val failures = categoryResults.mapNotNull { it.exceptionOrNull() }
                        .filterNot { it is CancellationException }
                    val combined = successes.flatten()
                        .distinctBy { "${it::class.simpleName}:${it.id}" }

                    _searchResults.value = combined
                    if (combined.isEmpty() && failures.isNotEmpty()) {
                        _searchError.value = failures.first().message ?: "Search failed"
                    }
                }.onFailure {
                    if (it !is CancellationException) {
                        _searchError.value = it.message ?: "Search failed"
                    }
                }
                _searchLoading.value = false
            }
        }
    }

    fun searchPlaylists(query: String) {
        if (query.isBlank()) return
        launch {
            _playlistLoading.value = true
            _playlistError.value = null
            _playlistResults.value = emptyList()
            runCatching {
                val result = youTubeService.search(query, YouTube.SearchFilter.FILTER_FEATURED_PLAYLIST)
                result.items
            }.onSuccess { _playlistResults.value = it }
                .onFailure { _playlistError.value = it.message }
            _playlistLoading.value = false
        }
    }

    private fun resolveFormatUrl(player: PlayerResponse): String? {
        println("[PlayerVM] playabilityStatus: ${player.playabilityStatus.status} reason=${player.playabilityStatus.reason}")

        if (player.streamingData == null) {
            println("[PlayerVM] streamingData is null!")
            return null
        }

        val format = player.streamingData?.adaptiveFormats
            ?.firstOrNull { it.isAudio }
            ?: player.streamingData?.formats?.firstOrNull()

        if (format == null) {
            println("[PlayerVM] no audio format found, adaptiveFormats=${player.streamingData?.adaptiveFormats?.size}, formats=${player.streamingData?.formats?.size}")
            return null
        }

        format.url?.let { return it }

        val cipherString = format.signatureCipher ?: format.cipher
        if (cipherString != null) {
            println("[PlayerVM] format has cipher, parsing...")
            try {
                val params = parseQueryString(cipherString)
                val url = params["url"]
                val sig = params["s"] ?: params["sig"]
                val sp = params["sp"]
                if (url != null && sig != null && sp != null) {
                    val builder = URLBuilder(url)
                    builder.parameters[sp] = sig
                    return builder.toString()
                }
            } catch (_: Exception) {}
        }
        println("[PlayerVM] could not resolve URL for format: mimeType=${format.mimeType}, itag=${format.itag}")
        return null
    }

    fun playArtist(browseId: String) {
        launch {
            val artist = runCatching { youTubeService.artist(browseId) }.getOrNull()
            val songsSection = artist?.sections?.firstOrNull { it.title.contains("Songs", ignoreCase = true) }
            val songs = songsSection?.items?.filterIsInstance<com.omnitune.innertube.models.SongItem>()
            if (!songs.isNullOrEmpty()) {
                _queue.value = songs
                _queueIndex.value = 0
                _currentSong.value = songs[0]
                doPlay(songs[0])
            }
        }
    }

    fun playSong(item: SongItem, index: Int = -1) {
        if (index >= 0) {
            val songs = searchResults.value.filterIsInstance<SongItem>()
            _queue.value = songs
            _queueIndex.value = index
        } else {
            val existing = _queue.value.indexOfFirst { it.id == item.id }
            if (existing >= 0) {
                _queueIndex.value = existing
            } else {
                _queue.value = _queue.value + item
                _queueIndex.value = _queue.value.lastIndex
            }
        }
        _currentSong.value = item
        launch { doPlay(item) }
    }
    fun playAlbum(browseId: String) {
        launch {
            val album = runCatching { youTubeService.album(browseId) }.getOrNull()
            val songs = album?.songs
            if (!songs.isNullOrEmpty()) {
                _queue.value = songs
                _queueIndex.value = 0
                _currentSong.value = songs[0]
                doPlay(songs[0])
            }
        }
    }
    
    fun playPlaylist(playlistId: String) {
        val local = _savedQueuePlaylists.value.firstOrNull { it.id == playlistId }
        if (local != null) {
            val songs = local.songs
            if (songs.isNotEmpty()) {
                _queue.value = songs
                _queueIndex.value = 0
                _currentSong.value = songs[0]
                launch { doPlay(songs[0]) }
            }
            return
        }
        launch {
            val playlist = runCatching { youTubeService.playlist(playlistId) }.getOrNull()
            val songs = playlist?.songs
            if (!songs.isNullOrEmpty()) {
                _queue.value = songs
                _queueIndex.value = 0
                _currentSong.value = songs[0]
                doPlay(songs[0])
            }
        }
    }

    private suspend fun doPlay(item: SongItem) {
        _lyricsResult.value = LyricsResult.Loading
        lyricsJob?.cancel()
        _streamUrl.value = null
        startListenTracking(item)
        downloadManager.completedLocalFileFor(item.id)?.absolutePath?.let { localPath ->
            _streamUrl.value = localPath
            audioEngine.play(localPath)
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
            println("[PlayerVM] trying client: ${client.clientName}")
            val resolved = runCatching {
                val player = youTubeService.getPlayer(item.id, client)
                resolveFormatUrl(player)
            }.getOrNull()
            if (resolved != null) {
                println("[PlayerVM] resolved URL with ${client.clientName}")
                url = resolved
                break
            }
        }
        if (url != null) {
            println("[PlayerVM] playing URL: ${url!!.take(100)}...")
            _streamUrl.value = url
            audioEngine.play(url!!)
            loadLyrics()
        } else {
            finalizeActiveListen(completed = false)
            _lyricsResult.value = LyricsResult.NotFound
            println("[PlayerVM] all clients failed to resolve URL")
        }
    }

    fun playQueueIndex(index: Int) {
        val q = _queue.value
        if (index in q.indices) {
            _queueIndex.value = index
            _currentSong.value = q[index]
            launch { doPlay(q[index]) }
        }
    }

    fun nextTrack() {
        val q = _queue.value
        if (q.isEmpty()) return
        val idx = _queueIndex.value
        if (_shuffle.value && q.size > 1) {
            var next = idx
            while (next == idx) next = (0 until q.size).random()
            playQueueIndex(next)
            return
        }
        if (idx < q.lastIndex) {
            playQueueIndex(idx + 1)
        } else if (_repeat.value == RepeatMode.ALL) {
            playQueueIndex(0)
        }
    }

    fun previousTrack() {
        val q = _queue.value
        if (q.isEmpty()) return
        val idx = _queueIndex.value
        if (_shuffle.value && q.size > 1) {
            var prev = idx
            while (prev == idx) prev = (0 until q.size).random()
            playQueueIndex(prev)
            return
        }
        if (idx > 0) {
            playQueueIndex(idx - 1)
        }
    }

    fun addToQueue(item: SongItem) {
        _queue.value = _queue.value + item
    }

    fun saveQueueAsPlaylist(name: String): Result<String> = runCatching {
        val saved = settings.saveQueueAsPlaylist(name, _queue.value)
        _savedQueuePlaylists.value = settings.savedQueuePlaylists
        saved.name
    }

    fun setDownloadQuality(quality: DownloadQualityMode) {
        _downloadQuality.value = quality
        settings.downloadQualityMode = quality
        settings.flush()
    }

    fun downloadSong(item: SongItem, quality: DownloadQualityMode? = null) {
        launch {
            downloadManager.enqueue(DownloadRequest(item, quality ?: _downloadQuality.value))
        }
    }

    fun downloadSongs(items: List<SongItem>, quality: DownloadQualityMode? = null) {
        launch {
            val selectedQuality = quality ?: _downloadQuality.value
            items.distinctBy { it.id }.forEach { item ->
                downloadManager.enqueue(DownloadRequest(item, selectedQuality))
            }
        }
    }

    fun playDownload(id: String) {
        val task = downloadTasks.value.firstOrNull { it.id == id } ?: return
        val item = SongItem(
            id = task.trackId,
            title = task.title,
            artists = listOf(Artist(task.artist, null)),
            album = task.album?.let { Album(it, "") },
            duration = null,
            thumbnail = task.artworkUrl.orEmpty(),
        )
        playSong(item)
    }

    fun pauseDownload(id: String) {
        launch { downloadManager.pause(id) }
    }

    fun resumeDownload(id: String) {
        launch { downloadManager.resume(id) }
    }

    fun retryDownload(id: String) {
        launch { downloadManager.retry(id) }
    }

    fun deleteDownload(id: String) {
        launch { downloadManager.delete(id) }
    }

    fun pauseAllDownloads() {
        launch { downloadManager.pauseAll() }
    }

    fun resumeAllDownloads() {
        launch { downloadManager.resumeAll() }
    }

    fun playNext(item: SongItem) {
        val list = _queue.value.toMutableList()
        val insertAt = (_queueIndex.value + 1).coerceIn(0, list.size)
        list.add(insertAt, item)
        _queue.value = list
        if (_queueIndex.value >= insertAt) {
            _queueIndex.value = _queueIndex.value + 1
        }
    }

    fun removeFromQueue(index: Int) {
        val list = _queue.value.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _queue.value = list
            if (_queueIndex.value == index) {
                _currentSong.value = null
                _queueIndex.value = -1
                audioEngine.stop()
            } else if (_queueIndex.value > index) {
                _queueIndex.value = _queueIndex.value - 1
            }
        }
    }

    fun moveQueueItem(from: Int, to: Int) {
        val list = _queue.value.toMutableList()
        if (from in list.indices && to in list.indices) {
            val item = list.removeAt(from)
            list.add(to, item)
            _queue.value = list
            
            val currentIdx = _queueIndex.value
            if (currentIdx == from) _queueIndex.value = to
            else if (currentIdx in (from + 1)..to) _queueIndex.value = currentIdx - 1
            else if (currentIdx in to..<from) _queueIndex.value = currentIdx + 1
        }
    }

    fun clearQueue() {
        finalizeActiveListen(completed = false)
        _queue.value = emptyList()
        _queueIndex.value = -1
        _currentSong.value = null
        audioEngine.stop()
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
        audioEngine.stop()
        _currentSong.value = null
        _queueIndex.value = -1
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
        settings.flush()
        audioEngine.setVolume(_volume.value)
    }
}
