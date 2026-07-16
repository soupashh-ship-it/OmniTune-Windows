package com.omnitune.app.platform

import java.io.File

internal class PlaylistCoverStore(
    private val platformContext: PlatformContext?,
) {
    fun importCover(playlistId: String, sourcePath: String?): String? {
        val trimmed = sourcePath?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val context = platformContext ?: return trimmed
        val source = File(trimmed)
        if (!source.isFile) return trimmed

        val coversDir = File(context.appDataDir, "playlist-covers").apply { mkdirs() }
        if (isInside(source, coversDir)) return source.absolutePath

        val extension = source.extension.lowercase().takeIf { it in allowedExtensions } ?: return trimmed
        val target = File(coversDir, "${safeFileName(playlistId)}.$extension")
        source.copyTo(target, overwrite = true)
        return target.absolutePath
    }

    private fun isInside(file: File, root: File): Boolean {
        val rootPath = runCatching { root.canonicalFile.toPath() }.getOrElse { return false }
        val filePath = runCatching { file.canonicalFile.toPath() }.getOrElse { return false }
        return filePath.startsWith(rootPath)
    }

    private fun safeFileName(value: String): String =
        value.replace(Regex("[^A-Za-z0-9._-]"), "_").take(80).ifBlank { "playlist-cover" }

    private companion object {
        val allowedExtensions = setOf("png", "jpg", "jpeg", "webp")
    }
}
