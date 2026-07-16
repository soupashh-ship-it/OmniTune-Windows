package com.omnitune.app.platform

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UpdateInstallerLauncherTest {
    @Test
    fun silentInstallIsAllowedForCustomExeAndMsiOnly() {
        assertTrue(UpdateInstallerLauncher.canInstallSilently(File("OmniTune-Setup-0.2.6-windows-x64-custom.exe")))
        assertTrue(UpdateInstallerLauncher.canInstallSilently(File("OmniTune-0.2.6-windows-x64.msi")))
        assertFalse(UpdateInstallerLauncher.canInstallSilently(File("OmniTune-Setup-0.2.6-windows-x64.exe")))
    }

    @Test
    fun customExeUsesInnoSilentArguments() {
        val command = UpdateInstallerLauncher.silentCommand(File("C:/Updates/OmniTune-Setup-0.2.6-windows-x64-custom.exe"))

        assertEquals("C:\\Updates\\OmniTune-Setup-0.2.6-windows-x64-custom.exe", command.first())
        assertTrue("/VERYSILENT" in command)
        assertTrue("/CLOSEAPPLICATIONS" in command)
    }

    @Test
    fun msiUsesMsiexecSilentArguments() {
        val command = UpdateInstallerLauncher.silentCommand(File("C:/Updates/OmniTune-0.2.6-windows-x64.msi"))

        assertEquals("msiexec.exe", command[0])
        assertEquals("/i", command[1])
        assertTrue("/qn" in command)
        assertTrue("/norestart" in command)
    }
}
