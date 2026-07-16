package com.omnitune.app.platform

import java.io.File

class PlatformContext(
    private val appDataRoot: File? = null,
    private val legacyHomeRoot: File = File(System.getProperty("user.home"), ".omnitune"),
    private val legacyLocalAppDataRoot: File? = System.getenv("LOCALAPPDATA")
        ?.takeIf { it.isNotBlank() }
        ?.let { File(it, "OmniTune") },
) {
    val appDataDir: File by lazy {
        (appDataRoot ?: defaultAppDataDir()).also { target ->
            migrateLegacyAppDataIfNeeded(target)
        }.apply { mkdirs() }
    }

    val cacheDir: File by lazy {
        File(appDataDir, "cache").apply { mkdirs() }
    }

    val downloadsDir: File by lazy {
        File(appDataDir, "downloads").apply { mkdirs() }
    }

    val databasePath: String by lazy {
        File(appDataDir, "omnitune.db").absolutePath
    }

    val logsDir: File by lazy {
        File(appDataDir, "logs").apply { mkdirs() }
    }

    val crashReportsDir: File by lazy {
        File(appDataDir, "crash-reports").apply { mkdirs() }
    }

    private fun defaultAppDataDir(): File = NativeRuntime.defaultLocalAppDataDir()

    private fun migrateLegacyAppDataIfNeeded(target: File) {
        val marker = File(target, ".legacy-migration-v1")
        if (marker.isFile) return

        runCatching {
            target.mkdirs()
            when {
                legacyLocalAppDataRoot?.isDirectory == true -> copyKnownUserData(legacyLocalAppDataRoot, target)
                legacyHomeRoot.isDirectory -> copyKnownUserData(legacyHomeRoot, target)
            }
            AtomicFileStore.writeText(marker, "completed=${System.currentTimeMillis()}")
        }.onFailure {
            target.mkdirs()
            File(target, "logs").mkdirs()
            File(target, "logs/startup.log").appendText(
                "Legacy app-data migration failed: ${it.message}${System.lineSeparator()}"
            )
        }
    }

    private fun copyKnownUserData(source: File, target: File) {
        target.mkdirs()
        val dataFiles = setOf(
            "downloads-index.json",
            "savedQueuePlaylists.json",
            "playbackHistory.json",
            "playbackSessions.json",
            "omnitune.db",
        )
        dataFiles.forEach { name ->
            val file = File(source, name)
            val destination = File(target, name)
            if (file.isFile && !destination.exists()) file.copyTo(destination, overwrite = false)
        }
        listOf("downloads", "cache", "logs").forEach { name ->
            val dir = File(source, name)
            val destination = File(target, name)
            if (dir.isDirectory && !destination.exists()) dir.copyRecursively(destination, overwrite = false)
        }
    }
}
