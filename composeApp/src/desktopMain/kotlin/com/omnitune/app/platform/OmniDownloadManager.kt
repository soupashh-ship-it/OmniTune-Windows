package com.omnitune.app.platform

import com.omnitune.app.service.YouTubeService
import com.omnitune.innertube.models.Album
import com.omnitune.innertube.models.Artist
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.models.YouTubeClient
import com.omnitune.innertube.models.response.PlayerResponse
import io.ktor.http.URLBuilder
import io.ktor.http.parseQueryString
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

enum class DownloadState {
    QUEUED,
    RESOLVING,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED,
}

enum class DownloadQualityMode {
    PROVIDER_DEFAULT,
    SMALLER_FILE,
    PREFER_HIGH,
}

data class DownloadRequest(
    val song: SongItem,
    val requestedQuality: DownloadQualityMode = DownloadQualityMode.PROVIDER_DEFAULT,
)

data class DownloadTask(
    val id: String,
    val trackId: String,
    val title: String,
    val artist: String,
    val album: String?,
    val artworkUrl: String?,
    val localFilePath: String?,
    val state: DownloadState,
    val bytesDownloaded: Long,
    val totalBytes: Long?,
    val requestedQuality: DownloadQualityMode,
    val actualCodec: String?,
    val actualBitrateKbps: Int?,
    val errorMessage: String?,
    val createdAt: Long,
    val updatedAt: Long,
)

interface OmniDownloadManager {
    val tasks: StateFlow<List<DownloadTask>>

    suspend fun enqueue(request: DownloadRequest): Result<String>
    suspend fun pause(id: String): Result<Unit>
    suspend fun resume(id: String): Result<Unit>
    suspend fun retry(id: String): Result<Unit>
    suspend fun cancel(id: String): Result<Unit>
    suspend fun delete(id: String): Result<Unit>
    suspend fun pauseAll(): Result<Unit>
    suspend fun resumeAll(): Result<Unit>
    fun completedDownloadFor(trackId: String): DownloadTask?
}

fun DownloadTask.verifiedLocalFile(): File? =
    localFilePath
        ?.let(::File)
        ?.takeIf { state == DownloadState.COMPLETED && it.isFile && it.length() > 0L }

fun OmniDownloadManager.completedLocalFileFor(trackId: String): File? =
    completedDownloadFor(trackId)?.verifiedLocalFile()

class FileBackedOmniDownloadManager(
    private val platform: PlatformContext,
    private val youTubeService: YouTubeService,
) : OmniDownloadManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeJobs = ConcurrentHashMap<String, Job>()
    private val indexFile = File(platform.appDataDir, "downloads-index.json")
    private val downloadDir = platform.downloadsDir

    private val _tasks = MutableStateFlow<List<DownloadTask>>(restoreTasks())
    override val tasks: StateFlow<List<DownloadTask>> = _tasks.asStateFlow()

    override suspend fun enqueue(request: DownloadRequest): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val existing = completedDownloadFor(request.song.id)
            if (existing != null) return@runCatching existing.id

            val now = System.currentTimeMillis()
            val task = DownloadTask(
                id = "download-${request.song.id}-$now",
                trackId = request.song.id,
                title = request.song.title,
                artist = request.song.artists.joinToString(", ") { it.name }.ifBlank { "Unknown artist" },
                album = request.song.album?.name,
                artworkUrl = request.song.thumbnail,
                localFilePath = null,
                state = DownloadState.QUEUED,
                bytesDownloaded = 0L,
                totalBytes = null,
                requestedQuality = request.requestedQuality,
                actualCodec = null,
                actualBitrateKbps = null,
                errorMessage = null,
                createdAt = now,
                updatedAt = now,
            )
            upsert(task)
            start(task.id, request.song)
            task.id
        }
    }

    override suspend fun pause(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            activeJobs.remove(id)?.cancel()
            update(id) { it.copy(state = DownloadState.PAUSED, updatedAt = System.currentTimeMillis()) }
        }
    }

    override suspend fun resume(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val task = find(id) ?: error("Download task not found.")
            val song = task.toSongItem()
            update(id) { it.copy(state = DownloadState.QUEUED, errorMessage = null, updatedAt = System.currentTimeMillis()) }
            start(id, song)
        }
    }

    override suspend fun retry(id: String): Result<Unit> = resume(id)

    override suspend fun cancel(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            activeJobs.remove(id)?.cancel()
            update(id) { it.copy(state = DownloadState.CANCELLED, updatedAt = System.currentTimeMillis()) }
        }
    }

    override suspend fun delete(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            activeJobs.remove(id)?.cancel()
            find(id)?.localFilePath?.let { File(it).delete() }
            File(partialPath(id)).delete()
            _tasks.value = _tasks.value.filterNot { it.id == id }
            persist()
        }
    }

    override suspend fun pauseAll(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            _tasks.value.filter { it.state == DownloadState.DOWNLOADING || it.state == DownloadState.RESOLVING || it.state == DownloadState.QUEUED }
                .forEach { pause(it.id).getOrThrow() }
        }
    }

    override suspend fun resumeAll(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            _tasks.value.filter { it.state == DownloadState.PAUSED || it.state == DownloadState.FAILED || it.state == DownloadState.CANCELLED }
                .forEach { resume(it.id).getOrThrow() }
        }
    }

    override fun completedDownloadFor(trackId: String): DownloadTask? =
        _tasks.value.firstOrNull {
            it.trackId == trackId &&
                it.verifiedLocalFile() != null
        }

    private fun start(id: String, song: SongItem) {
        activeJobs[id]?.cancel()
        activeJobs[id] = scope.launch {
            try {
                var lastFailure: Throwable? = null
                repeat(3) { attempt ->
                    try {
                        download(id, song)
                        return@launch
                    } catch (c: CancellationException) {
                        throw c
                    } catch (t: Throwable) {
                        lastFailure = t
                        if (attempt < 2) {
                            update(id) {
                                it.copy(
                                    state = DownloadState.QUEUED,
                                    errorMessage = "Retrying after ${t.message ?: t::class.simpleName ?: "download error"}",
                                    updatedAt = System.currentTimeMillis(),
                                )
                            }
                            delay(1_000L * (attempt + 1))
                        }
                    }
                }
                throw lastFailure ?: error("Download failed.")
            } catch (_: CancellationException) {
                // pause/cancel already wrote state
            } catch (t: Throwable) {
                update(id) {
                    it.copy(
                        state = DownloadState.FAILED,
                        errorMessage = t.message ?: t::class.simpleName ?: "Download failed",
                        updatedAt = System.currentTimeMillis(),
                    )
                }
            } finally {
                activeJobs.remove(id)
            }
        }
    }

    private suspend fun download(id: String, song: SongItem) {
        update(id) { it.copy(state = DownloadState.RESOLVING, errorMessage = null, updatedAt = System.currentTimeMillis()) }
        val resolved = resolveDownloadFormat(song.id, find(id)?.requestedQuality ?: DownloadQualityMode.PROVIDER_DEFAULT)
            ?: error("No downloadable audio format available.")

        val extension = extensionFor(resolved.mimeType)
        val finalFile = File(downloadDir, "${safeFileName(song.title)}-${song.id}.$extension")
        val partialFile = File(partialPath(id))
        val existingBytes = partialFile.takeIf { it.exists() }?.length() ?: 0L
        val connection = (URL(resolved.url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 20_000
            readTimeout = 30_000
            if (existingBytes > 0L) setRequestProperty("Range", "bytes=$existingBytes-")
        }

        val responseCode = connection.responseCode
        val rangeAccepted = existingBytes > 0L && responseCode == HttpURLConnection.HTTP_PARTIAL
        val startBytes = if (rangeAccepted) existingBytes else 0L
        if (existingBytes > 0L && !rangeAccepted) partialFile.delete()
        val totalBytes = when {
            resolved.contentLength != null -> resolved.contentLength
            connection.contentLengthLong > 0 && rangeAccepted -> startBytes + connection.contentLengthLong
            connection.contentLengthLong > 0 -> connection.contentLengthLong
            else -> null
        }

        update(id) {
            it.copy(
                state = DownloadState.DOWNLOADING,
                totalBytes = totalBytes,
                bytesDownloaded = startBytes,
                localFilePath = finalFile.absolutePath,
                actualCodec = codecFor(resolved.mimeType),
                actualBitrateKbps = (resolved.averageBitrate ?: resolved.bitrate).takeIf { b -> b > 0 }?.div(1000),
                updatedAt = System.currentTimeMillis(),
            )
        }

        RandomAccessFile(partialFile, "rw").use { output ->
            if (rangeAccepted) output.seek(startBytes) else output.setLength(0L)
            connection.inputStream.use { input ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var downloaded = startBytes
                var lastPersist = 0L
                while (currentCoroutineContext().isActive) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    output.write(buffer, 0, read)
                    downloaded += read
                    val now = System.currentTimeMillis()
                    if (now - lastPersist > 500L) {
                        update(id) { it.copy(bytesDownloaded = downloaded, updatedAt = now) }
                        lastPersist = now
                    }
                }
            }
        }

        if (!partialFile.renameTo(finalFile)) {
            partialFile.copyTo(finalFile, overwrite = true)
            partialFile.delete()
        }
        if (!finalFile.isFile || finalFile.length() <= 0L) error("Downloaded file verification failed.")

        update(id) {
            it.copy(
                state = DownloadState.COMPLETED,
                bytesDownloaded = finalFile.length(),
                totalBytes = totalBytes ?: finalFile.length(),
                localFilePath = finalFile.absolutePath,
                errorMessage = null,
                updatedAt = System.currentTimeMillis(),
            )
        }
    }

    private suspend fun resolveDownloadFormat(videoId: String, quality: DownloadQualityMode): ResolvedFormat? {
        val clients = listOf(
            YouTubeClient.WEB_REMIX,
            YouTubeClient.WEB,
            YouTubeClient.ANDROID_VR_NO_AUTH,
            YouTubeClient.IOS,
            YouTubeClient.ANDROID_MUSIC,
        )
        for (client in clients) {
            val player = runCatching { youTubeService.getPlayer(videoId, client) }.getOrNull() ?: continue
            val formats = player.streamingData?.adaptiveFormats?.filter { it.isAudio }.orEmpty()
            val selected = when (quality) {
                DownloadQualityMode.SMALLER_FILE -> formats.minByOrNull { it.bitrate }
                DownloadQualityMode.PREFER_HIGH -> formats.maxByOrNull { it.bitrate }
                DownloadQualityMode.PROVIDER_DEFAULT -> formats.firstOrNull()
            } ?: continue
            val url = resolveFormatUrl(selected) ?: continue
            return ResolvedFormat(
                url = url,
                mimeType = selected.mimeType,
                bitrate = selected.bitrate,
                averageBitrate = selected.averageBitrate,
                contentLength = selected.contentLength,
            )
        }
        return null
    }

    private fun resolveFormatUrl(format: PlayerResponse.StreamingData.Format): String? {
        format.url?.let { return it }
        val cipherString = format.signatureCipher ?: format.cipher ?: return null
        return runCatching {
            val params = parseQueryString(cipherString)
            val url = params["url"]
            val sig = params["s"] ?: params["sig"]
            val sp = params["sp"]
            if (url != null && sig != null && sp != null) {
                URLBuilder(url).apply { parameters[sp] = sig }.toString()
            } else null
        }.getOrNull()
    }

    private fun restoreTasks(): List<DownloadTask> {
        val restored = runCatching {
            if (!indexFile.isFile) return@runCatching emptyList()
            val array = JSONArray(indexFile.readText())
            (0 until array.length()).mapNotNull { array.optJSONObject(it)?.toTask() }
        }.getOrDefault(emptyList())

        return restored.map { task ->
            when (task.state) {
                DownloadState.COMPLETED -> {
                    val valid = task.localFilePath?.let { File(it).isFile && File(it).length() > 0L } == true
                    if (valid) task.copy(bytesDownloaded = File(task.localFilePath!!).length())
                    else task.copy(state = DownloadState.FAILED, errorMessage = "Downloaded file is missing or empty.")
                }
                DownloadState.DOWNLOADING, DownloadState.RESOLVING, DownloadState.QUEUED -> task.copy(state = DownloadState.PAUSED)
                else -> task
            }
        }.also { restoredTasks ->
            runCatching {
                indexFile.parentFile?.mkdirs()
                val array = JSONArray()
                restoredTasks.forEach { array.put(it.toJson()) }
                indexFile.writeText(array.toString())
            }
        }
    }

    private fun find(id: String): DownloadTask? = _tasks.value.firstOrNull { it.id == id }

    private fun upsert(task: DownloadTask) {
        _tasks.value = listOf(task) + _tasks.value.filterNot { it.id == task.id }
        persist()
    }

    private fun update(id: String, transform: (DownloadTask) -> DownloadTask) {
        _tasks.value = _tasks.value.map { if (it.id == id) transform(it) else it }
        persist()
    }

    private fun persist() {
        indexFile.parentFile?.mkdirs()
        val array = JSONArray()
        _tasks.value.forEach { array.put(it.toJson()) }
        indexFile.writeText(array.toString())
    }

    private fun partialPath(id: String): String = File(downloadDir, "$id.part").absolutePath

    private data class ResolvedFormat(
        val url: String,
        val mimeType: String,
        val bitrate: Int,
        val averageBitrate: Int?,
        val contentLength: Long?,
    )
}

private fun DownloadTask.toJson(): JSONObject = JSONObject()
    .put("id", id)
    .put("trackId", trackId)
    .put("title", title)
    .put("artist", artist)
    .put("album", album ?: JSONObject.NULL)
    .put("artworkUrl", artworkUrl ?: JSONObject.NULL)
    .put("localFilePath", localFilePath ?: JSONObject.NULL)
    .put("state", state.name)
    .put("bytesDownloaded", bytesDownloaded)
    .put("totalBytes", totalBytes ?: JSONObject.NULL)
    .put("requestedQuality", requestedQuality.name)
    .put("actualCodec", actualCodec ?: JSONObject.NULL)
    .put("actualBitrateKbps", actualBitrateKbps ?: JSONObject.NULL)
    .put("errorMessage", errorMessage ?: JSONObject.NULL)
    .put("createdAt", createdAt)
    .put("updatedAt", updatedAt)

private fun JSONObject.toTask(): DownloadTask = DownloadTask(
    id = optString("id"),
    trackId = optString("trackId"),
    title = optString("title"),
    artist = optString("artist"),
    album = optNullableString("album"),
    artworkUrl = optNullableString("artworkUrl"),
    localFilePath = optNullableString("localFilePath"),
    state = runCatching { DownloadState.valueOf(optString("state")) }.getOrDefault(DownloadState.FAILED),
    bytesDownloaded = optLong("bytesDownloaded"),
    totalBytes = optNullableLong("totalBytes"),
    requestedQuality = runCatching { DownloadQualityMode.valueOf(optString("requestedQuality")) }.getOrDefault(DownloadQualityMode.PROVIDER_DEFAULT),
    actualCodec = optNullableString("actualCodec"),
    actualBitrateKbps = optNullableInt("actualBitrateKbps"),
    errorMessage = optNullableString("errorMessage"),
    createdAt = optLong("createdAt"),
    updatedAt = optLong("updatedAt"),
)

private fun DownloadTask.toSongItem(): SongItem = SongItem(
    id = trackId,
    title = title,
    artists = listOf(Artist(artist, null)),
    album = album?.let { Album(it, "") },
    duration = null,
    thumbnail = artworkUrl.orEmpty(),
)

private fun JSONObject.optNullableString(key: String): String? =
    if (isNull(key)) null else optString(key).takeIf { it.isNotBlank() }

private fun JSONObject.optNullableLong(key: String): Long? =
    if (isNull(key)) null else optLong(key)

private fun JSONObject.optNullableInt(key: String): Int? =
    if (isNull(key)) null else optInt(key)

private fun safeFileName(value: String): String =
    value.replace(Regex("[^A-Za-z0-9._ -]"), "_").take(80).ifBlank { "track" }

private fun extensionFor(mimeType: String): String = when {
    mimeType.contains("mp4", ignoreCase = true) -> "m4a"
    mimeType.contains("webm", ignoreCase = true) -> "webm"
    mimeType.contains("mpeg", ignoreCase = true) -> "mp3"
    else -> "audio"
}

private fun codecFor(mimeType: String): String? =
    Regex("codecs=\"([^\"]+)\"").find(mimeType)?.groupValues?.getOrNull(1)
        ?: mimeType.substringBefore(";").takeIf { it.isNotBlank() }
