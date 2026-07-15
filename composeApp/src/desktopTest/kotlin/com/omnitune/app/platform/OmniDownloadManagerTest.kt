package com.omnitune.app.platform

import com.omnitune.app.service.YouTubeService
import com.omnitune.innertube.models.Artist
import com.omnitune.innertube.models.SongItem
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile
import java.net.InetSocketAddress
import java.nio.file.Files
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OmniDownloadManagerTest {
    @Test
    fun completedValidFileRestoresAsCompletedAndLocalSourceWins() = withTempAppData { root ->
        val downloaded = File(root, "downloads/valid.m4a").apply {
            parentFile.mkdirs()
            writeBytes(byteArrayOf(1, 2, 3, 4))
        }
        writeIndex(root, taskJson(trackId = "track-1", state = DownloadState.COMPLETED, localPath = downloaded.absolutePath, bytes = 4L))

        val manager = FileBackedOmniDownloadManager(PlatformContext(root), YouTubeService())
        val task = manager.completedDownloadFor("track-1")

        assertNotNull(task)
        assertEquals(downloaded.absolutePath, manager.completedLocalFileFor("track-1")?.absolutePath)
    }

    @Test
    fun completedMissingFileRestoresAsFailed() = withTempAppData { root ->
        writeIndex(root, taskJson(trackId = "missing", state = DownloadState.COMPLETED, localPath = File(root, "downloads/missing.m4a").absolutePath))

        val manager = FileBackedOmniDownloadManager(PlatformContext(root), YouTubeService())
        val task = manager.tasks.value.single()

        assertEquals(DownloadState.FAILED, task.state)
        assertNull(manager.completedLocalFileFor("missing"))
    }

    @Test
    fun completedEmptyFileRestoresAsFailed() = withTempAppData { root ->
        val empty = File(root, "downloads/empty.m4a").apply {
            parentFile.mkdirs()
            writeBytes(byteArrayOf())
        }
        writeIndex(root, taskJson(trackId = "empty", state = DownloadState.COMPLETED, localPath = empty.absolutePath, bytes = 0L))

        val manager = FileBackedOmniDownloadManager(PlatformContext(root), YouTubeService())

        assertEquals(DownloadState.FAILED, manager.tasks.value.single().state)
        assertNull(manager.completedLocalFileFor("empty"))
    }

    @Test
    fun activeTaskRestoresAsPaused() = withTempAppData { root ->
        writeIndex(root, taskJson(trackId = "active", state = DownloadState.DOWNLOADING, localPath = null))

        val manager = FileBackedOmniDownloadManager(PlatformContext(root), YouTubeService())

        assertEquals(DownloadState.PAUSED, manager.tasks.value.single().state)
    }

    @Test
    fun deleteRemovesMetadataAndLocalFile() = withTempAppData { root ->
        val file = File(root, "downloads/delete.m4a").apply {
            parentFile.mkdirs()
            writeBytes(byteArrayOf(1))
        }
        writeIndex(root, taskJson(id = "delete-me", trackId = "delete", state = DownloadState.COMPLETED, localPath = file.absolutePath, bytes = 1L))
        val manager = FileBackedOmniDownloadManager(PlatformContext(root), YouTubeService())

        runBlocking {
            manager.delete("delete-me").getOrThrow()
        }

        assertFalse(file.exists())
        assertTrue(manager.tasks.value.isEmpty())
        assertEquals(0, JSONArray(File(root, "downloads-index.json").readText()).length())
    }

    @Test
    fun completedDownloadOutsideManagedDirectoryIsNotTrusted() = withTempAppData { root ->
        val outside = Files.createTempFile("omnitune-outside-download", ".m4a").toFile().apply {
            writeBytes(byteArrayOf(1, 2, 3))
        }
        try {
            writeIndex(root, taskJson(trackId = "outside", state = DownloadState.COMPLETED, localPath = outside.absolutePath, bytes = 3L))

            val manager = FileBackedOmniDownloadManager(PlatformContext(root), YouTubeService())

            assertEquals(DownloadState.FAILED, manager.tasks.value.single().state)
            assertNull(manager.completedLocalFileFor("outside"))
            assertTrue(outside.exists())
        } finally {
            outside.delete()
        }
    }

    @Test
    fun deleteDoesNotRemoveFilesOutsideManagedDownloadsDirectory() = withTempAppData { root ->
        val outside = Files.createTempFile("omnitune-outside-delete", ".m4a").toFile().apply {
            writeBytes(byteArrayOf(1))
        }
        try {
            writeIndex(root, taskJson(id = "delete-outside", trackId = "outside-delete", state = DownloadState.COMPLETED, localPath = outside.absolutePath, bytes = 1L))
            val manager = FileBackedOmniDownloadManager(PlatformContext(root), YouTubeService())

            runBlocking {
                manager.delete("delete-outside").getOrThrow()
            }

            assertTrue(outside.exists())
            assertTrue(manager.tasks.value.isEmpty())
        } finally {
            outside.delete()
        }
    }

    @Test
    fun corruptIndexIsPreservedAndManagerStartsEmpty() = withTempAppData { root ->
        File(root, "downloads-index.json").writeText("{broken")

        val manager = FileBackedOmniDownloadManager(PlatformContext(root), YouTubeService())

        assertTrue(manager.tasks.value.isEmpty())
        assertTrue(root.listFiles().orEmpty().any { it.name.startsWith("downloads-index.corrupt-") })
    }

    @Test
    fun corruptIndexRecoversFromValidBackup() = withTempAppData { root ->
        val downloaded = File(root, "downloads/backup.m4a").apply {
            parentFile.mkdirs()
            writeBytes(byteArrayOf(1, 2))
        }
        File(root, "downloads-index.json").writeText("{broken")
        File(root, "downloads-index.json.bak").writeText(
            JSONArray().put(taskJson(trackId = "backup", state = DownloadState.COMPLETED, localPath = downloaded.absolutePath, bytes = 2L)).toString()
        )

        val manager = FileBackedOmniDownloadManager(PlatformContext(root), YouTubeService())

        assertEquals("backup", manager.tasks.value.single().trackId)
        assertEquals(downloaded.absolutePath, manager.completedLocalFileFor("backup")?.absolutePath)
        assertTrue(root.listFiles().orEmpty().any { it.name.startsWith("downloads-index.corrupt-") })
    }

    @Test
    fun malformedDownloadRecordsAreSkippedWithoutDroppingGoodRecords() = withTempAppData { root ->
        writeIndex(
            root,
            taskJson(id = "valid", trackId = "valid-track", state = DownloadState.PAUSED, localPath = null),
            JSONObject().put("id", "").put("trackId", "bad-track"),
            JSONObject().put("id", "bad-no-track").put("title", "No track").put("artist", "Artist"),
            taskJson(id = "valid", trackId = "duplicate-track", state = DownloadState.PAUSED, localPath = null),
        )

        val manager = FileBackedOmniDownloadManager(PlatformContext(root), YouTubeService())

        assertEquals(listOf("valid-track"), manager.tasks.value.map { it.trackId })
    }

    @Test
    fun downloadWritePermissionErrorsUseConciseUserMessage() {
        assertEquals(
            "Download location is unavailable.",
            userFacingDownloadError(FileNotFoundException("C:\\Downloads\\song.m4a (Access is denied)")),
        )
        assertEquals(
            "Download location is unavailable.",
            userFacingDownloadError(IOException("Permission denied")),
        )
    }

    @Test
    fun downloadWriteNoSpaceErrorsUseConciseUserMessage() {
        assertEquals(
            "Not enough storage space.",
            userFacingDownloadError(IOException("There is not enough space on the disk")),
        )
        assertEquals(
            "Not enough storage space.",
            userFacingDownloadError(IOException("No space left on device")),
        )
    }

    @Test
    fun mediaWritePermissionDeniedFailsTaskWithoutFalseCompletionAndCanRetry() = withTempAppData { root ->
        withMediaServer(byteArrayOf(1, 2, 3, 4, 5, 6)) { url ->
            val writer = ToggleFailingMediaWriter(writeFailure = IOException("Permission denied"))
            val manager = testManager(root, url, 6L, writer)

            val id = runBlocking { manager.enqueue(DownloadRequest(testSong("permission"))).getOrThrow() }
            val failed = runBlocking { manager.waitFor(id) { it.state == DownloadState.FAILED } }

            assertEquals("Download location is unavailable.", failed.errorMessage)
            assertFalse(File(root, "downloads/Title permission-permission.m4a").exists())

            writer.failWrites.set(false)
            runBlocking { manager.retry(id).getOrThrow() }
            val completed = runBlocking { manager.waitFor(id) { it.state == DownloadState.COMPLETED } }

            assertEquals(DownloadState.COMPLETED, completed.state)
            assertEquals(6L, File(completed.localFilePath!!).length())
        }
    }

    @Test
    fun mediaWriteNoSpaceFailsTaskWithoutFalseCompletion() = withTempAppData { root ->
        withMediaServer(byteArrayOf(1, 2, 3, 4)) { url ->
            val manager = testManager(
                root = root,
                url = url,
                contentLength = 4L,
                writer = ToggleFailingMediaWriter(writeFailure = IOException("There is not enough space on the disk")),
            )

            val id = runBlocking { manager.enqueue(DownloadRequest(testSong("nospace"))).getOrThrow() }
            val failed = runBlocking { manager.waitFor(id) { it.state == DownloadState.FAILED } }

            assertEquals("Not enough storage space.", failed.errorMessage)
            assertFalse(File(root, "downloads/Title nospace-nospace.m4a").exists())
            assertNull(manager.completedLocalFileFor("nospace"))
        }
    }

    @Test
    fun resumedPartialWriteFailureRemainsFailedAndKeepsPartialTruthful() = withTempAppData { root ->
        val partial = File(root, "downloads/resume-id.part").apply {
            parentFile.mkdirs()
            writeBytes(byteArrayOf(1, 2))
        }
        writeIndex(root, taskJson(id = "resume-id", trackId = "resume", state = DownloadState.PAUSED, localPath = File(root, "downloads/Title resume-resume.m4a").absolutePath, bytes = 2L))

        withMediaServer(byteArrayOf(1, 2, 3, 4, 5, 6)) { url ->
            val manager = testManager(
                root = root,
                url = url,
                contentLength = 6L,
                writer = ToggleFailingMediaWriter(writeFailure = IOException("Permission denied")),
            )

            runBlocking { manager.resume("resume-id").getOrThrow() }
            val failed = runBlocking { manager.waitFor("resume-id") { it.state == DownloadState.FAILED } }

            assertEquals("Download location is unavailable.", failed.errorMessage)
            assertTrue(partial.exists())
            assertFalse(File(root, "downloads/Title resume-resume.m4a").exists())
        }
    }

    @Test
    fun finalizationFailureDoesNotCreateFalseCompletedDownloadAndRestoresFailed() = withTempAppData { root ->
        withMediaServer(byteArrayOf(1, 2, 3, 4)) { url ->
            val manager = testManager(
                root = root,
                url = url,
                contentLength = 4L,
                writer = ToggleFailingMediaWriter(finalizeFailure = IOException("Permission denied")),
            )

            val id = runBlocking { manager.enqueue(DownloadRequest(testSong("finalize"))).getOrThrow() }
            val failed = runBlocking { manager.waitFor(id) { it.state == DownloadState.FAILED } }

            assertEquals("Download location is unavailable.", failed.errorMessage)
            assertFalse(File(root, "downloads/Title finalize-finalize.m4a").exists())

            val restored = FileBackedOmniDownloadManager(
                PlatformContext(root),
                YouTubeService(),
                formatResolver = StaticDownloadResolver(url, 4L),
            )
            assertEquals(DownloadState.FAILED, restored.tasks.value.single().state)
            assertNull(restored.completedLocalFileFor("finalize"))
        }
    }

    private fun withTempAppData(block: (File) -> Unit) {
        val root = Files.createTempDirectory("omnitune-download-test").toFile()
        try {
            block(root)
        } finally {
            root.deleteRecursively()
        }
    }

    private fun testManager(
        root: File,
        url: String,
        contentLength: Long,
        writer: DownloadMediaWriter,
    ): FileBackedOmniDownloadManager = FileBackedOmniDownloadManager(
        PlatformContext(root),
        YouTubeService(),
        formatResolver = StaticDownloadResolver(url, contentLength),
        mediaWriter = writer,
    )

    private suspend fun FileBackedOmniDownloadManager.waitFor(id: String, predicate: (DownloadTask) -> Boolean): DownloadTask =
        withTimeout(10_000) {
            while (true) {
                val task = tasks.value.first { it.id == id }
                if (predicate(task)) return@withTimeout task
                delay(50)
            }
            error("unreachable")
        }

    private fun testSong(id: String): SongItem = SongItem(
        id = id,
        title = "Title $id",
        artists = listOf(Artist("Artist $id", null)),
        thumbnail = "",
    )

    private class StaticDownloadResolver(
        private val url: String,
        private val contentLength: Long,
    ) : DownloadFormatResolver {
        override suspend fun resolve(videoId: String, quality: DownloadQualityMode): ResolvedDownloadFormat =
            ResolvedDownloadFormat(
                url = url,
                mimeType = "audio/mp4; codecs=\"mp4a.40.2\"",
                bitrate = 128_000,
                averageBitrate = 128_000,
                contentLength = contentLength,
            )
    }

    private class ToggleFailingMediaWriter(
        private val writeFailure: IOException? = null,
        private val finalizeFailure: IOException? = null,
    ) : DownloadMediaWriter {
        val failWrites = AtomicBoolean(writeFailure != null)

        override fun openPartial(partialFile: File, rangeAccepted: Boolean, startBytes: Long): RandomAccessFile =
            DefaultDownloadMediaWriter.openPartial(partialFile, rangeAccepted, startBytes)

        override fun write(output: RandomAccessFile, buffer: ByteArray, offset: Int, length: Int) {
            if (failWrites.get()) throw writeFailure ?: IOException("Injected write failure")
            DefaultDownloadMediaWriter.write(output, buffer, offset, length)
        }

        override fun finalizePartial(partialFile: File, finalFile: File) {
            finalizeFailure?.let { throw it }
            DefaultDownloadMediaWriter.finalizePartial(partialFile, finalFile)
        }
    }

    private fun withMediaServer(bytes: ByteArray, block: (String) -> Unit) {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/media") { exchange ->
            val range = exchange.requestHeaders.getFirst("Range")
            val start = range
                ?.removePrefix("bytes=")
                ?.substringBefore("-")
                ?.toLongOrNull()
                ?.coerceIn(0L, bytes.size.toLong())
                ?.toInt()
                ?: 0
            val body = bytes.copyOfRange(start, bytes.size)
            val status = if (start > 0) 206 else 200
            if (start > 0) exchange.responseHeaders.add("Content-Range", "bytes $start-${bytes.lastIndex}/${bytes.size}")
            exchange.responseHeaders.add("Content-Type", "audio/mp4")
            exchange.sendResponseHeaders(status, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        server.start()
        try {
            block("http://127.0.0.1:${server.address.port}/media")
        } finally {
            server.stop(0)
        }
    }

    private fun writeIndex(root: File, vararg tasks: JSONObject) {
        File(root, "downloads-index.json").apply {
            parentFile.mkdirs()
            writeText(JSONArray().also { array -> tasks.forEach(array::put) }.toString())
        }
    }

    private fun taskJson(
        id: String = "download-id",
        trackId: String,
        state: DownloadState,
        localPath: String?,
        bytes: Long = 0L,
    ): JSONObject = JSONObject()
        .put("id", id)
        .put("trackId", trackId)
        .put("title", "Title $trackId")
        .put("artist", "Artist $trackId")
        .put("album", JSONObject.NULL)
        .put("artworkUrl", JSONObject.NULL)
        .put("localFilePath", localPath ?: JSONObject.NULL)
        .put("state", state.name)
        .put("bytesDownloaded", bytes)
        .put("totalBytes", if (bytes > 0L) bytes else JSONObject.NULL)
        .put("requestedQuality", DownloadQualityMode.PROVIDER_DEFAULT.name)
        .put("actualCodec", JSONObject.NULL)
        .put("actualBitrateKbps", JSONObject.NULL)
        .put("errorMessage", JSONObject.NULL)
        .put("createdAt", 1L)
        .put("updatedAt", 1L)
}
