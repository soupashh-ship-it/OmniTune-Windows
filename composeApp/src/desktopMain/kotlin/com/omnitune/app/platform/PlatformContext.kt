package com.omnitune.app.platform

import java.io.File

class PlatformContext(
    private val appDataRoot: File? = null,
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

    private fun defaultAppDataDir(): File = NativeRuntime.defaultLocalAppDataDir()

    private fun migrateLegacyAppDataIfNeeded(target: File) {
        if (target.exists()) return
        val legacyHome = File(System.getProperty("user.home"), ".omnitune")
        val legacyLocalAppData = System.getenv("LOCALAPPDATA")
            ?.takeIf { it.isNotBlank() }
            ?.let { File(it, "OmniTune") }

        runCatching {
            when {
                legacyLocalAppData?.isDirectory == true -> copyKnownUserData(legacyLocalAppData, target)
                legacyHome.isDirectory -> legacyHome.copyRecursively(target, overwrite = false)
                else -> target.mkdirs()
            }
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
            if (file.isFile) file.copyTo(File(target, name), overwrite = false)
        }
        listOf("downloads", "cache", "logs").forEach { name ->
            val dir = File(source, name)
            if (dir.isDirectory) dir.copyRecursively(File(target, name), overwrite = false)
        }
    }
}
