package com.omnitune.app.platform

import java.io.File
import kotlin.concurrent.thread
import kotlin.system.exitProcess

object UpdateInstallerLauncher {
    fun canInstallSilently(installer: File): Boolean {
        val name = installer.name.lowercase()
        return (name.endsWith(".exe") && name.contains("custom")) || name.endsWith(".msi")
    }

    fun launchSilentUpdate(installer: File, exitDelayMs: Long = 750L): Boolean {
        if (!installer.isFile || !canInstallSilently(installer)) return false

        val command = silentCommand(installer)
        return runCatching {
            ProcessBuilder(command)
                .directory(installer.parentFile)
                .start()

            thread(name = "omnitune-update-exit", isDaemon = false) {
                Thread.sleep(exitDelayMs.coerceAtLeast(0L))
                exitProcess(0)
            }
            true
        }.getOrDefault(false)
    }

    internal fun silentCommand(installer: File): List<String> {
        val path = installer.absolutePath
        return if (installer.name.endsWith(".msi", ignoreCase = true)) {
            listOf("msiexec.exe", "/i", path, "/qn", "/norestart")
        } else {
            listOf(path, "/VERYSILENT", "/SUPPRESSMSGBOXES", "/NORESTART", "/CLOSEAPPLICATIONS", "/RESTARTAPPLICATIONS")
        }
    }
}
