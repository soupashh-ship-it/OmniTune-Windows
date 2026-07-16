package com.omnitune.app.platform

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReleaseUpdateCheckerTest {
    @Test
    fun compareVersionsHandlesPatchAndMissingParts() {
        assertTrue(compareVersions("0.2.1", "0.2.0") > 0)
        assertTrue(compareVersions("0.3", "0.2.9") > 0)
        assertEquals(0, compareVersions("v0.2", "0.2.0"))
        assertTrue(compareVersions("0.1.9", "0.2.0") < 0)
    }

    @Test
    fun parserReadsGitHubReleaseMetadata() {
        val checker = ReleaseUpdateChecker(currentVersion = "0.2.0")
        val metadata = checker.parseGitHubRelease(
            """
            {
              "tag_name": "v0.2.1",
              "html_url": "https://github.com/soupashh-ship-it/OmniTune-Windows/releases/tag/v0.2.1",
              "prerelease": true,
              "assets": [
                {
                  "name": "OmniTune-Setup-0.2.1-windows-x64.exe",
                  "browser_download_url": "https://example.invalid/OmniTune-Setup-0.2.1-windows-x64.exe",
                  "size": 123
                },
                {
                  "name": "OmniTune-Setup-0.2.1-windows-x64.exe.sha256",
                  "browser_download_url": "https://example.invalid/OmniTune-Setup-0.2.1-windows-x64.exe.sha256",
                  "size": 100
                }
              ]
            }
            """.trimIndent(),
        )

        assertEquals("0.2.1", metadata.version)
        assertEquals("https://github.com/soupashh-ship-it/OmniTune-Windows/releases/tag/v0.2.1", metadata.htmlUrl)
        assertTrue(metadata.prerelease)
        assertEquals("OmniTune-Setup-0.2.1-windows-x64.exe", metadata.assets.first().name)
        assertEquals("OmniTune-Setup-0.2.1-windows-x64.exe.sha256", metadata.assets.last().name)
    }

    @Test
    fun evaluatorReportsUpdateAvailableOnlyWhenLatestIsNewer() {
        val checker = ReleaseUpdateChecker(currentVersion = "0.2.0")

        val update = checker.evaluate(
            current = "0.2.0",
            latest = ReleaseMetadata(
                "0.2.1",
                "https://example.invalid/release",
                prerelease = false,
                assets = listOf(ReleaseAsset("OmniTune-Setup-0.2.1-windows-x64.exe", "https://example.invalid/installer.exe", 1)),
            ),
        )
        val current = checker.evaluate(
            current = "0.2.0",
            latest = ReleaseMetadata("0.2.0", "https://example.invalid/release", prerelease = false),
        )

        assertTrue(update is UpdateCheckResult.UpdateAvailable)
        assertEquals("OmniTune-Setup-0.2.1-windows-x64.exe", update.installerAsset?.name)
        assertEquals(null, update.checksumAsset?.name)
        assertTrue(current is UpdateCheckResult.Current)
    }

    @Test
    fun parserSelectsNewestReleaseFromArrayIncludingPrerelease() {
        val checker = ReleaseUpdateChecker(currentVersion = "0.2.0")
        val metadata = checker.parseGitHubReleaseResponse(
            """
            [
              { "tag_name": "v0.2.4", "html_url": "https://example.invalid/024", "prerelease": false, "assets": [] },
              { "tag_name": "v0.2.5", "html_url": "https://example.invalid/025", "prerelease": true, "assets": [] }
            ]
            """.trimIndent(),
        )

        assertEquals("0.2.5", metadata.version)
        assertTrue(metadata.prerelease)
    }

    @Test
    fun preferredInstallerChoosesCustomExeFirst() {
        val metadata = ReleaseMetadata(
            version = "0.2.5",
            htmlUrl = "https://example.invalid/release",
            prerelease = true,
            assets = listOf(
                ReleaseAsset("OmniTune-0.2.5-windows-x64.msi", "https://example.invalid/app.msi", 1),
                ReleaseAsset("OmniTune-Setup-0.2.5-windows-x64.exe", "https://example.invalid/app.exe", 1),
                ReleaseAsset("OmniTune-Setup-0.2.5-windows-x64-custom.exe", "https://example.invalid/custom.exe", 1),
                ReleaseAsset("OmniTune-Setup-0.2.5-windows-x64-custom.exe.sha256", "https://example.invalid/custom.exe.sha256", 1),
            ),
        )

        val installer = metadata.preferredWindowsInstallerAsset()
        assertEquals("OmniTune-Setup-0.2.5-windows-x64-custom.exe", installer?.name)
        requireNotNull(installer)
        assertEquals("OmniTune-Setup-0.2.5-windows-x64-custom.exe.sha256", metadata.checksumAssetFor(installer)?.name)
    }

    @Test
    fun updateFileNameSanitizationRemovesUnsafeCharacters() {
        assertEquals(".._bad_OmniTune.exe", sanitizeUpdateFileName("../bad/OmniTune.exe"))
    }
}
