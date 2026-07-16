package com.omnitune.app.platform

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.io.File

class AppInfoTest {
    @Test
    fun generatedVersionMetadataIsAvailableAtRuntime() {
        val expectedVersion = findGradleProperties()
            .readLines()
            .first { it.startsWith("omnitune.version=") }
            .substringAfter("=")
            .trim()

        assertEquals(expectedVersion, AppInfo.version)
        assertEquals("v$expectedVersion", AppInfo.releaseTag)
        assertTrue(AppInfo.releaseUrl.endsWith("/releases/tag/v$expectedVersion"))
        assertTrue(AppInfo.releasesUrl.endsWith("/releases"))
        assertTrue(AppInfo.latestReleaseApiUrl.endsWith("/releases/latest"))
    }

    private fun findGradleProperties(): File {
        return generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .map { File(it, "gradle.properties") }
            .firstOrNull { it.isFile }
            ?: error("Could not locate gradle.properties from ${System.getProperty("user.dir")}")
    }
}
