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
        val legacy = File(System.getProperty("user.home"), ".omnitune")
        if (!legacy.isDirectory) return
        runCatching {
            legacy.copyRecursively(target, overwrite = false)
        }.onFailure {
            target.mkdirs()
            File(target, "logs").mkdirs()
            File(target, "logs/startup.log").appendText(
                "Legacy app-data migration from ${legacy.absolutePath} failed: ${it.message}${System.lineSeparator()}"
            )
        }
    }
}
