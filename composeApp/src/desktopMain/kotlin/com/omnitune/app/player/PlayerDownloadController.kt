package com.omnitune.app.player

import com.omnitune.app.platform.DownloadQualityMode
import com.omnitune.app.platform.DownloadRequest
import com.omnitune.app.platform.DownloadTask
import com.omnitune.app.platform.OmniDownloadManager
import com.omnitune.app.platform.SettingsRepository
import com.omnitune.innertube.models.Album
import com.omnitune.innertube.models.Artist
import com.omnitune.innertube.models.SongItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class PlayerDownloadController(
    private val scope: CoroutineScope,
    private val settings: SettingsRepository,
    private val downloadManager: OmniDownloadManager,
    private val playSong: (SongItem) -> Unit,
) {
    val downloadTasks: StateFlow<List<DownloadTask>> = downloadManager.tasks

    private val _downloadQuality = MutableStateFlow(settings.downloadQualityMode)
    val downloadQuality: StateFlow<DownloadQualityMode> = _downloadQuality.asStateFlow()

    fun setDownloadQuality(quality: DownloadQualityMode) {
        _downloadQuality.value = quality
        settings.downloadQualityMode = quality
        settings.flush()
    }

    fun downloadSong(item: SongItem, quality: DownloadQualityMode? = null) {
        scope.launch {
            downloadManager.enqueue(DownloadRequest(item, quality ?: _downloadQuality.value))
        }
    }

    fun downloadSongs(items: List<SongItem>, quality: DownloadQualityMode? = null) {
        scope.launch {
            val selectedQuality = quality ?: _downloadQuality.value
            items.distinctBy { it.id }.forEach { item ->
                downloadManager.enqueue(DownloadRequest(item, selectedQuality))
            }
        }
    }

    fun playDownload(id: String) {
        val task = downloadTasks.value.firstOrNull { it.id == id } ?: return
        playSong(
            SongItem(
                id = task.trackId,
                title = task.title,
                artists = listOf(Artist(task.artist, null)),
                album = task.album?.let { Album(it, "") },
                duration = null,
                thumbnail = task.artworkUrl.orEmpty(),
            )
        )
    }

    fun pauseDownload(id: String) {
        scope.launch { downloadManager.pause(id) }
    }

    fun resumeDownload(id: String) {
        scope.launch { downloadManager.resume(id) }
    }

    fun retryDownload(id: String) {
        scope.launch { downloadManager.retry(id) }
    }

    fun deleteDownload(id: String) {
        scope.launch { downloadManager.delete(id) }
    }

    fun pauseAllDownloads() {
        scope.launch { downloadManager.pauseAll() }
    }

    fun resumeAllDownloads() {
        scope.launch { downloadManager.resumeAll() }
    }
}
