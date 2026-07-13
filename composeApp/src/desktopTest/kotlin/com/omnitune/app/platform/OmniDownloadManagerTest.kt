package com.omnitune.app.platform

import com.omnitune.app.service.YouTubeService
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.nio.file.Files
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

    private fun withTempAppData(block: (File) -> Unit) {
        val root = Files.createTempDirectory("omnitune-download-test").toFile()
        try {
            block(root)
        } finally {
            root.deleteRecursively()
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
