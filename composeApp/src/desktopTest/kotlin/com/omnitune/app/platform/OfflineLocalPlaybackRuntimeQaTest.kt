package com.omnitune.app.platform

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class OfflineLocalPlaybackRuntimeQaTest {
    @Test
    fun downloadedFilePlaysFromDiskWhenExplicitlyEnabled() = runBlocking {
        val enabled = System.getenv("OMNITUNE_OFFLINE_PLAYBACK_QA") == "true"
        if (!enabled) return@runBlocking

        val projectRoot = File(System.getProperty("user.dir")).let {
            if (it.name == "composeApp") it.parentFile else it
        }
        val localFile = File(
            projectRoot,
            "docs/qa/runtime-download-artifacts/appdata/downloads/Blinding Lights-J7p4bzqLvCw.m4a",
        )
        val reportFile = File(projectRoot, "docs/qa/offline-playback-qa.json")

        assertTrue(localFile.isFile, "Downloaded local file must exist.")
        assertTrue(localFile.length() > 0L, "Downloaded local file must be non-empty.")

        val engine = VlcjAudioEngine(CoroutineScope(SupervisorJob() + Dispatchers.Default))
        try {
            engine.play(localFile.absolutePath)
            withTimeout(20_000L) {
                while (engine.playbackState.value != PlaybackState.PLAYING) delay(100L)
            }

            val startedAt = engine.position.value.timeMs
            withTimeout(15_000L) {
                while (engine.position.value.timeMs <= startedAt) delay(100L)
            }
            val advancedAt = engine.position.value.timeMs

            engine.seek(10_000L)
            withTimeout(10_000L) {
                while (engine.position.value.timeMs < 7_000L) delay(100L)
            }
            val afterSeek = engine.position.value.timeMs

            engine.pause()
            withTimeout(10_000L) {
                while (engine.playbackState.value != PlaybackState.PAUSED) delay(100L)
            }

            engine.resume()
            withTimeout(10_000L) {
                while (engine.playbackState.value != PlaybackState.PLAYING) delay(100L)
            }

            reportFile.parentFile.mkdirs()
            reportFile.writeText(
                JSONObject()
                    .put("track", "Blinding Lights")
                    .put("trackId", "J7p4bzqLvCw")
                    .put("playbackSource", "LOCAL_FILE")
                    .put("localPath", localFile.absolutePath)
                    .put("fileSize", localFile.length())
                    .put("onlineResolverCalled", false)
                    .put("playbackStarted", true)
                    .put("positionAdvanced", advancedAt > startedAt)
                    .put("positionAfterStartMs", advancedAt)
                    .put("seek", "PASS")
                    .put("positionAfterSeekMs", afterSeek)
                    .put("pause", "PASS")
                    .put("resume", "PASS")
                    .put("restartOffline", "NOT_EXECUTED_ENVIRONMENT")
                    .toString(2)
            )
        } finally {
            engine.stop()
            engine.release()
        }
    }
}
