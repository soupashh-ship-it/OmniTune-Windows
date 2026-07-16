package com.omnitune.app.platform

import java.util.prefs.Preferences

internal class JsonFileStore(
    private val prefs: Preferences,
    private val platformContext: PlatformContext?
) {
    private fun writeJsonFile(name: String, content: String) {
        platformContext?.appDataDir?.let { dir ->
            runCatching { AtomicFileStore.writeText(java.io.File(dir, name), content) }
                .onFailure { OmniLogger.error("Settings", "Failed to persist $name.", it) }
                .getOrThrow()
        }
    }

    fun writeJsonStore(name: String, fallbackPreferenceKey: String, content: String) {
        writeJsonFile(name, content)
        if (platformContext == null) {
            prefs.put(fallbackPreferenceKey, content)
        }
    }
    
    private fun readJsonFile(name: String): String? {
        return platformContext?.appDataDir?.let { dir ->
            val file = java.io.File(dir, name)
            if (file.exists()) runCatching { file.readText() }.getOrNull() else null
        }
    }

    private fun readJsonBackupFile(name: String): String? {
        return platformContext?.appDataDir?.let { dir ->
            val file = java.io.File(dir, "$name.bak")
            if (file.exists()) runCatching { file.readText() }.getOrNull() else null
        }
    }

    private fun preserveCorruptJsonFile(name: String) {
        platformContext?.appDataDir?.let { dir ->
            val file = java.io.File(dir, name)
            if (!file.exists()) return
            runCatching {
                file.copyTo(
                    java.io.File(dir, "$name.corrupt-${System.currentTimeMillis()}.bak"),
                    overwrite = false,
                )
            }
        }
    }

    fun <T> readRecoverableJsonList(
        fileName: String,
        fallbackPreferenceKey: String,
        parser: (String) -> List<T>,
    ): List<T> {
        val fileContent = readJsonFile(fileName)
        if (fileContent != null) {
            runCatching { return parser(fileContent) }
                .onFailure { preserveCorruptJsonFile(fileName) }
        }

        readJsonBackupFile(fileName)?.let { backupContent ->
            runCatching { return parser(backupContent) }
                .onFailure { OmniLogger.error("Settings", "Backup JSON for $fileName is also unreadable.", it) }
        }

        return runCatching { parser(prefs.get(fallbackPreferenceKey, "[]")) }
            .getOrDefault(emptyList())
    }
}
