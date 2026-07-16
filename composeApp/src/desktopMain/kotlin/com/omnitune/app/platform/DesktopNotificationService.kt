package com.omnitune.app.platform

import java.awt.Image
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon

class DesktopNotificationService {
    private var ownedTrayIcon: TrayIcon? = null

    fun isSupported(): Boolean =
        runCatching { SystemTray.isSupported() }.getOrDefault(false)

    fun show(title: String, message: String): Boolean {
        if (!isSupported()) return false
        return runCatching {
            val icon = existingTrayIcon() ?: ownedTrayIcon ?: createTrayIcon().also {
                SystemTray.getSystemTray().add(it)
                ownedTrayIcon = it
            }
            icon.displayMessage(title.take(64), message.take(240), TrayIcon.MessageType.INFO)
            true
        }.onFailure {
            OmniLogger.error("Notifications", "Failed to display desktop notification.", it)
        }.getOrDefault(false)
    }

    private fun existingTrayIcon(): TrayIcon? =
        SystemTray.getSystemTray()
            .trayIcons
            .firstOrNull { it.toolTip?.contains("OmniTune", ignoreCase = true) == true }
            ?: SystemTray.getSystemTray().trayIcons.firstOrNull()

    private fun createTrayIcon(): TrayIcon {
        val image = loadIconImage()
        return TrayIcon(image, "OmniTune").apply {
            isImageAutoSize = true
        }
    }

    private fun loadIconImage(): Image {
        val stream = Thread.currentThread().contextClassLoader.getResourceAsStream("omnitune-icon.png")
        val bytes = stream?.use { it.readBytes() }
        return if (bytes != null) {
            Toolkit.getDefaultToolkit().createImage(bytes)
        } else {
            java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB)
        }
    }
}
