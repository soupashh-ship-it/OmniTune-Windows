package com.omnitune.app.platform

import java.io.File
import java.nio.file.Files
import java.util.zip.ZipFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CrashReportManagerTest {
    @Test
    fun recordCrashWritesLocalCrashReport() = withTempAppData { root ->
        val context = PlatformContext(root)

        val report = CrashReportManager.recordCrash(
            context = context,
            threadName = "test-thread",
            throwable = IllegalStateException("Simulated crash"),
        )

        assertTrue(report.isFile)
        val text = report.readText()
        assertTrue(text.contains("OmniTune Windows crash report"))
        assertTrue(text.contains("test-thread"))
        assertTrue(text.contains("Simulated crash"))
        assertTrue(CrashReportManager.hasCrashReports(context))
        assertEquals(report.absolutePath, CrashReportManager.latestCrashReport(context)?.absolutePath)
    }

    @Test
    fun exportDiagnosticsIncludesPrivacyNoteCrashReportAndLog() = withTempAppData { root ->
        val context = PlatformContext(root)
        OmniLogger.init(context)
        OmniLogger.info("CrashReportTest", "Diagnostic log marker")
        CrashReportManager.recordCrash(context, "test-thread", RuntimeException("Exported crash"))

        val export = CrashReportManager.exportDiagnostics(context)

        assertTrue(export.isFile)
        ZipFile(export).use { zip ->
            assertNotNull(zip.getEntry("README.txt"))
            assertNotNull(zip.getEntry("system-info.txt"))
            assertNotNull(zip.getEntry("latest-crash-report.txt"))
            assertNotNull(zip.getEntry("omnitune.log"))

            val readme = zip.getInputStream(zip.getEntry("README.txt")).bufferedReader().readText()
            assertTrue(readme.contains("does not silently upload"))
            assertTrue(readme.contains("Not included:"))

            val crash = zip.getInputStream(zip.getEntry("latest-crash-report.txt")).bufferedReader().readText()
            assertTrue(crash.contains("Exported crash"))
        }
    }

    @Test
    fun githubIssueUriUsesPublicRepoAndDoesNotEmbedDiagnostics() = withTempAppData { root ->
        val context = PlatformContext(root)
        CrashReportManager.recordCrash(context, "test-thread", RuntimeException("Do not embed stack attachment"))

        val uri = CrashReportManager.githubIssueUri(context).toString()

        assertTrue(uri.startsWith("https://github.com/soupashh-ship-it/OmniTune-Windows/issues/new?"))
        assertTrue(uri.contains("Crash+or+error+report"))
        assertTrue(uri.contains("attach+the+ZIP+manually"))
        assertTrue(!uri.contains("Do not embed stack attachment"))
    }

    private fun withTempAppData(block: (File) -> Unit) {
        val root = Files.createTempDirectory("omnitune-crash-report-test").toFile()
        try {
            block(root)
        } finally {
            root.deleteRecursively()
        }
    }
}
