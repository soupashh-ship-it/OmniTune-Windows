package com.omnitune.app.platform

import com.omnitune.app.service.YouTubeService
import com.omnitune.innertube.models.SongItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NetworkDownloadRuntimeQaTest {
    @Test
    fun providerBackedDownloadCompletesWhenExplicitlyEnabled() = runBlocking {
        val enabled = System.getProperty("omnitune.networkQa") == "true" ||
            System.getenv("OMNITUNE_NETWORK_QA") == "true"
        if (!enabled) return@runBlocking

        val projectRoot = File(System.getProperty("user.dir")).let {
            if (it.name == "composeApp") it.parentFile else it
        }
        val appData = File(projectRoot, "docs/qa/runtime-download-artifacts/appdata").apply {
            deleteRecursively()
            mkdirs()
        }
        val reportFile = File(projectRoot, "docs/qa/runtime-download-qa.json")
        try {
            val service = YouTubeService()
            val song = service.search("Blinding Lights").items.filterIsInstance<SongItem>().first()
            val manager = FileBackedOmniDownloadManager(PlatformContext(appData), service)
            val id = manager.enqueue(DownloadRequest(song, DownloadQualityMode.PROVIDER_DEFAULT)).getOrThrow()

            var completed: DownloadTask? = null
            withTimeout(180_000L) {
                while (completed == null) {
                    val task = manager.tasks.value.first { it.id == id }
                    when (task.state) {
                        DownloadState.COMPLETED -> completed = task
                        DownloadState.FAILED, DownloadState.CANCELLED -> error("Download ended as ${task.state}: ${task.errorMessage}")
                        else -> delay(500L)
                    }
                }
            }
            val completedTask = completed ?: error("Download did not complete.")

            val local = completedTask.verifiedLocalFile()
            assertNotNull(local)
            assertTrue(local.length() > 0L)

            val restored = FileBackedOmniDownloadManager(PlatformContext(appData), service)
            val restoredTask = restored.completedDownloadFor(song.id)
            assertNotNull(restoredTask)
            assertEquals(local.absolutePath, restored.completedLocalFileFor(song.id)?.absolutePath)

            reportFile.parentFile.mkdirs()
            reportFile.writeText(
                JSONObject()
                    .put("track", song.title)
                    .put("trackId", song.id)
                    .put("requestedQuality", completedTask.requestedQuality.name)
                    .put("actualCodec", completedTask.actualCodec ?: JSONObject.NULL)
                    .put("actualBitrateKbps", completedTask.actualBitrateKbps ?: JSONObject.NULL)
                    .put("totalBytes", completedTask.totalBytes ?: JSONObject.NULL)
                    .put("finalFilePath", local.absolutePath)
                    .put("finalFileSize", local.length())
                    .put("restartRestored", true)
                    .put("localSourceSelected", restored.completedLocalFileFor(song.id) != null)
                    .toString(2)
            )
        } finally {
            // Keep the artifact directory for QA evidence.
        }
    }
}
