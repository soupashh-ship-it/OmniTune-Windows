package com.omnitune.app.platform

import java.io.File
import java.time.Instant

object NativeRuntime {
    data class VlcRuntimeSelection(
        val source: String,
        val directory: File,
        val pluginsDirectory: File,
    )

    fun configureNativeAudioRuntime(): VlcRuntimeSelection? {
        val selected = resolveVlcRuntime()
        if (selected != null) {
            System.setProperty("jna.library.path", selected.directory.absolutePath)
            System.setProperty("VLC_PLUGIN_PATH", selected.pluginsDirectory.absolutePath)
            log("Using VLC runtime from ${selected.source}: ${selected.directory.absolutePath}")
            log("VLC libvlc=${File(selected.directory, "libvlc.dll").absolutePath}")
            log("VLC libvlccore=${File(selected.directory, "libvlccore.dll").absolutePath}")
            log("VLC plugins=${selected.pluginsDirectory.absolutePath}")
        } else {
            log("No VLC runtime found. Checked packaged native/vlc, VLC_HOME, and the standard Windows VLC install path.")
        }
        return selected
    }

    fun resolveVlcRuntime(): VlcRuntimeSelection? {
        val appDir = resolveApplicationDirectory()
        val requireBundled = System.getenv("OMNITUNE_QA_REQUIRE_BUNDLED_VLC") == "true"
        val candidates = buildList {
            add("packaged" to File(appDir, "native/vlc"))
            appDir.parentFile?.let { add("packaged-parent" to File(it, "native/vlc")) }
            extractEmbeddedVlcRuntime()?.let { add("embedded-resource" to it) }
            if (!requireBundled) {
                add("working-directory" to File(System.getProperty("user.dir"), "native/vlc"))
                add("working-directory-app" to File(System.getProperty("user.dir"), "app/native/vlc"))
                System.getenv("VLC_HOME")?.takeIf { it.isNotBlank() }?.let { add("VLC_HOME" to File(it)) }
                add("system-vlc" to File("C:/Program Files/VideoLAN/VLC"))
            }
        }

        return candidates
            .mapNotNull { (source, dir) ->
                val plugins = File(dir, "plugins")
                if (File(dir, "libvlc.dll").isFile && File(dir, "libvlccore.dll").isFile && plugins.isDirectory) {
                    VlcRuntimeSelection(source, dir, plugins)
                } else {
                    null
                }
            }
            .firstOrNull()
    }

    private fun extractEmbeddedVlcRuntime(): File? {
        val classLoader = NativeRuntime::class.java.classLoader
        val manifestStream = classLoader.getResourceAsStream("native/vlc-manifest.txt") ?: return null
        val entries = manifestStream.bufferedReader().useLines { lines ->
            lines.map { it.trim() }
                .filter { it.isNotBlank() && it.startsWith("native/vlc/") }
                .toList()
        }
        if (entries.isEmpty()) return null

        val targetRoot = File(defaultLocalAppDataDir(), "native/vlc-runtime")
        val validExisting = File(targetRoot, "libvlc.dll").isFile &&
            File(targetRoot, "libvlccore.dll").isFile &&
            File(targetRoot, "plugins").isDirectory
        if (validExisting) return targetRoot

        runCatching {
            targetRoot.mkdirs()
            entries.forEach { resourcePath ->
                val relativePath = resourcePath.removePrefix("native/vlc/")
                val target = File(targetRoot, relativePath)
                target.parentFile?.mkdirs()
                classLoader.getResourceAsStream(resourcePath)?.use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                } ?: error("Missing embedded VLC resource: $resourcePath")
            }
        }.onFailure {
            log("Failed to extract embedded VLC runtime: ${it.message ?: it::class.java.name}")
            return null
        }

        return targetRoot.takeIf {
            File(it, "libvlc.dll").isFile &&
                File(it, "libvlccore.dll").isFile &&
                File(it, "plugins").isDirectory
        }
    }

    fun resolveApplicationDirectory(): File {
        val codeSource = NativeRuntime::class.java.protectionDomain.codeSource?.location
        val location = runCatching { codeSource?.toURI()?.let(::File) }.getOrNull()
        val base = when {
            location == null -> File(System.getProperty("user.dir"))
            location.isFile -> location.parentFile
            else -> location
        }
        return generateSequence(base) { it.parentFile }
            .firstOrNull { File(it, "native/vlc").exists() || File(it, "app").exists() || File(it, "runtime").exists() }
            ?: base
    }

    fun log(message: String) {
        runCatching {
            val logDir = File(defaultLocalAppDataDir(), "logs").apply { mkdirs() }
            File(logDir, "startup.log").appendText("${Instant.now()} $message${System.lineSeparator()}")
        }
    }

    internal fun defaultLocalAppDataDir(): File {
        val localAppData = System.getenv("LOCALAPPDATA")?.takeIf { it.isNotBlank() }
        return if (localAppData != null) {
            File(localAppData, "OmniTuneData")
        } else {
            File(System.getProperty("user.home"), "AppData/Local/OmniTuneData")
        }
    }
}
