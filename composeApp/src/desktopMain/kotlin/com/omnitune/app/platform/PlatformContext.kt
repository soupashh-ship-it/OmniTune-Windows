package com.omnitune.app.platform

import java.io.File

class PlatformContext {
    val appDataDir: File by lazy {
        File(System.getProperty("user.home"), ".omnitune").apply { mkdirs() }
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
}
