package com.omnitune.app.player

import com.omnitune.app.platform.SettingsRepository
import com.omnitune.app.platform.VlcjAudioEngine
import com.omnitune.app.platform.PlaybackState
import com.omnitune.app.service.YouTubeService
import com.omnitune.innertube.YouTube
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
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

enum class NavScreen {
    Home, Browse, Radio, Library, Search, NowPlaying, Queue, Playlists, Settings,
    Artist, Album, Downloads
}

enum class RepeatMode { OFF, ALL, ONE }

class PlayerViewModel(
    private val youTubeService: YouTubeService,
    private val audioEngine: VlcjAudioEngine,
    private val settings: SettingsRepository
) : CoroutineScope {

    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO

    private val _navScreen = MutableStateFlow(NavScreen.Search)
    val navScreen: StateFlow<NavScreen> = _navScreen.asStateFlow()

    private val _searchResults = MutableStateFlow<List<YTItem>>(emptyList())
    val searchResults: StateFlow<List<YTItem>> = _searchResults.asStateFlow()

    private val _searchLoading = MutableStateFlow(false)
    val searchLoading: StateFlow<Boolean> = _searchLoading.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

    private val _playlistResults = MutableStateFlow<List<YTItem>>(emptyList())
    val playlistResults: StateFlow<List<YTItem>> = _playlistResults.asStateFlow()

    private val _playlistLoading = MutableStateFlow(false)
    val playlistLoading: StateFlow<Boolean> = _playlistLoading.asStateFlow()

    private val _playlistError = MutableStateFlow<String?>(null)
    val playlistError: StateFlow<String?> = _playlistError.asStateFlow()

    private val _currentSong = MutableStateFlow<SongItem?>(null)
    val currentSong: StateFlow<SongItem?> = _currentSong.asStateFlow()

    private val _streamUrl = MutableStateFlow<String?>(null)
    val streamUrl: StateFlow<String?> = _streamUrl.asStateFlow()

    private val _volume = MutableStateFlow(settings.volume)
    val volume: StateFlow<Int> = _volume.asStateFlow()

    val playbackState: StateFlow<PlaybackState> = audioEngine.playbackState
    val position = audioEngine.position
    val playerError: StateFlow<String?> = audioEngine.error

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

    val recentSearches: List<String> get() = settings.recentSearches

    init {
        audioEngine.onTrackFinished = { onTrackFinished() }
    }

    fun onTrackFinished() {
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

    fun openAlbum(id: String) {
        _currentAlbumId.value = id
        navigateTo(NavScreen.Album)
    }

    private val _lyricsText = MutableStateFlow<String?>(null)
    val lyricsText: StateFlow<String?> = _lyricsText.asStateFlow()
    private val _lyricsLoading = MutableStateFlow(false)
    val lyricsLoading: StateFlow<Boolean> = _lyricsLoading.asStateFlow()

    fun loadLyrics() {
        val song = _currentSong.value ?: return
        if (_lyricsText.value != null && _currentSong.value?.id == song.id) return
        launch {
            _lyricsLoading.value = true
            runCatching { LrcLib.getLyrics(song.title, song.artists.firstOrNull()?.name ?: "", song.duration ?: -1) }
                .onSuccess { _lyricsText.value = it.getOrNull() }
                .onFailure { _lyricsText.value = null }
            _lyricsLoading.value = false
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
        if (query.isBlank()) return
        settings.addRecentSearch(query)
        launch {
            _searchLoading.value = true
            _searchError.value = null
            _searchResults.value = emptyList()
            runCatching {
                val result = youTubeService.search(query)
                result.items
            }.onSuccess { _searchResults.value = it }
                .onFailure { _searchError.value = it.message }
            _searchLoading.value = false
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

    private suspend fun doPlay(item: SongItem) {
        _streamUrl.value = null
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
        } else {
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
