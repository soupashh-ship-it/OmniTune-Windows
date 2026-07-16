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
              "prerelease": true
            }
            """.trimIndent(),
        )

        assertEquals("0.2.1", metadata.version)
        assertEquals("https://github.com/soupashh-ship-it/OmniTune-Windows/releases/tag/v0.2.1", metadata.htmlUrl)
        assertTrue(metadata.prerelease)
    }

    @Test
    fun evaluatorReportsUpdateAvailableOnlyWhenLatestIsNewer() {
        val checker = ReleaseUpdateChecker(currentVersion = "0.2.0")

        val update = checker.evaluate(
            current = "0.2.0",
            latest = ReleaseMetadata("0.2.1", "https://example.invalid/release", prerelease = false),
        )
        val current = checker.evaluate(
            current = "0.2.0",
            latest = ReleaseMetadata("0.2.0", "https://example.invalid/release", prerelease = false),
        )

        assertTrue(update is UpdateCheckResult.UpdateAvailable)
        assertTrue(current is UpdateCheckResult.Current)
    }
}
