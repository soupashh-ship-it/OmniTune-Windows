package com.omnitune.app.platform

import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlatformContextMigrationTest {
    @Test
    fun legacyInstallCollisionDataMigratesEvenWhenTargetAlreadyExists() = withTempDirs { legacy, target ->
        target.mkdirs()
        File(legacy, "savedQueuePlaylists.json").writeText("""[{"id":"p1","name":"Legacy","createdAt":1,"songs":[]}]""")
        File(legacy, "playbackHistory.json").writeText("[]")
        File(legacy, "OmniTune.exe").writeText("not user data")
        File(legacy, "runtime").mkdirs()
        File(legacy, "downloads").mkdirs()
        File(legacy, "downloads/song.m4a").writeBytes(byteArrayOf(1, 2, 3))

        val appData = PlatformContext(
            appDataRoot = target,
            legacyHomeRoot = File(legacy.parentFile, "missing-home"),
            legacyLocalAppDataRoot = legacy,
        ).appDataDir

        assertEquals(target.absolutePath, appData.absolutePath)
        assertTrue(File(target, "savedQueuePlaylists.json").isFile)
        assertTrue(File(target, "playbackHistory.json").isFile)
        assertTrue(File(target, "downloads/song.m4a").isFile)
        assertTrue(File(target, ".legacy-migration-v1").isFile)
        assertFalse(File(target, "OmniTune.exe").exists())
        assertFalse(File(target, "runtime").exists())
    }

    @Test
    fun migrationIsIdempotentAndDoesNotOverwriteExistingDestinationData() = withTempDirs { legacy, target ->
        target.mkdirs()
        File(target, "savedQueuePlaylists.json").writeText("""[{"id":"new","name":"Current","createdAt":2,"songs":[]}]""")
        File(legacy, "savedQueuePlaylists.json").writeText("""[{"id":"old","name":"Legacy","createdAt":1,"songs":[]}]""")

        val context = PlatformContext(
            appDataRoot = target,
            legacyHomeRoot = File(legacy.parentFile, "missing-home"),
            legacyLocalAppDataRoot = legacy,
        )

        context.appDataDir
        context.appDataDir

        assertTrue(File(target, ".legacy-migration-v1").isFile)
        assertTrue(File(target, "savedQueuePlaylists.json").readText().contains("Current"))
        assertFalse(File(target, "savedQueuePlaylists.json").readText().contains("Legacy"))
    }

    private fun withTempDirs(block: (File, File) -> Unit) {
        val root = Files.createTempDirectory("omnitune-migration-test").toFile()
        val legacy = File(root, "legacy")
        val target = File(root, "target")
        try {
            legacy.mkdirs()
            block(legacy, target)
        } finally {
            root.deleteRecursively()
        }
    }
}
