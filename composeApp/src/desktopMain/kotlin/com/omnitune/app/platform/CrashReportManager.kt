package com.omnitune.app.platform

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URI
import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object CrashReportManager {
    private val installed = AtomicBoolean(false)
    private val timestampFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")

    fun install(context: PlatformContext) {
        if (!installed.compareAndSet(false, true)) return
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                recordCrash(context, thread.name, throwable)
            }.onFailure {
                OmniLogger.error("CrashReport", "Failed to write crash report.", it)
            }
            previous?.uncaughtException(thread, throwable)
        }
        OmniLogger.info("CrashReport", "Local crash report handler installed.")
    }

    fun recordCrash(context: PlatformContext, threadName: String, throwable: Throwable): File {
        val reportDir = context.crashReportsDir
        val report = File(reportDir, "crash-${safeTimestamp()}.txt")
        val body = buildString {
            appendLine("OmniTune Windows crash report")
            appendLine("Generated: ${LocalDateTime.now()}")
            appendLine("App version: ${AppInfo.version}")
            appendLine("Thread: $threadName")
            appendLine("OS: ${System.getProperty("os.name")} ${System.getProperty("os.version")} ${System.getProperty("os.arch")}")
            appendLine("Java: ${System.getProperty("java.version")} (${System.getProperty("java.vendor")})")
            appendLine()
            appendLine("Exception:")
            appendLine(throwable.stackTraceToString())
        }
        AtomicFileStore.writeText(report, body)
        return report
    }

    fun hasCrashReports(context: PlatformContext): Boolean =
        context.crashReportsDir
            .listFiles()
            .orEmpty()
            .any { it.isFile && it.name.startsWith("crash-") && it.extension == "txt" }

    fun latestCrashReport(context: PlatformContext): File? =
        context.crashReportsDir
            .listFiles()
            .orEmpty()
            .filter { it.isFile && it.name.startsWith("crash-") && it.extension == "txt" }
            .maxByOrNull { it.lastModified() }

    fun exportDiagnostics(context: PlatformContext): File {
        val exportDir = File(context.crashReportsDir, "exports").apply { mkdirs() }
        val zip = File(exportDir, "omnitune-diagnostics-${safeTimestamp()}.zip")
        ZipOutputStream(FileOutputStream(zip)).use { output ->
            output.addText(
                "README.txt",
                """
                OmniTune Windows diagnostics export

                This ZIP is created only when you explicitly export diagnostics.
                OmniTune does not silently upload this file.

                Included:
                - local app log, if present
                - most recent local crash report, if present
                - basic app/runtime information

                Not included:
                - downloaded songs
                - playlist files
                - liked-song data
                - search history
                - settings files
                """.trimIndent() + System.lineSeparator()
            )
            output.addText(
                "system-info.txt",
                buildString {
                    appendLine("App version: ${AppInfo.version}")
                    appendLine("Release tag: ${AppInfo.releaseTag}")
                    appendLine("OS: ${System.getProperty("os.name")} ${System.getProperty("os.version")} ${System.getProperty("os.arch")}")
                    appendLine("Java: ${System.getProperty("java.version")} (${System.getProperty("java.vendor")})")
                    appendLine("Has crash report: ${hasCrashReports(context)}")
                }
            )
            latestCrashReport(context)?.let { output.addFile("latest-crash-report.txt", it) }
            File(context.logsDir, "omnitune.log")
                .takeIf { it.isFile }
                ?.let { output.addFile("omnitune.log", it) }
        }
        return zip
    }

    fun githubIssueUri(context: PlatformContext): URI {
        val title = encode("Crash or error report")
        val body = encode(
            buildString {
                appendLine("## OmniTune Windows issue report")
                appendLine()
                appendLine("App version: ${AppInfo.version}")
                appendLine("OS: ${System.getProperty("os.name")} ${System.getProperty("os.version")} ${System.getProperty("os.arch")}")
                appendLine("Java: ${System.getProperty("java.version")} (${System.getProperty("java.vendor")})")
                appendLine("Crash report recorded: ${hasCrashReports(context)}")
                latestCrashReport(context)?.let {
                    appendLine("Latest local crash report: ${it.name}")
                }
                appendLine()
                appendLine("Describe what happened:")
                appendLine()
                appendLine("Steps to reproduce:")
                appendLine("1.")
                appendLine("2.")
                appendLine("3.")
                appendLine()
                appendLine("Expected behavior:")
                appendLine()
                appendLine("Actual behavior:")
                appendLine()
                appendLine("If you exported diagnostics from Settings, attach the ZIP manually. OmniTune does not upload diagnostics automatically.")
            }
        )
        return URI("https://github.com/soupashh-ship-it/OmniTune-Windows/issues/new?title=$title&body=$body")
    }

    private fun safeTimestamp(): String =
        LocalDateTime.now().format(timestampFormatter)

    private fun encode(value: String): String =
        URLEncoder.encode(value, Charsets.UTF_8)

    private fun ZipOutputStream.addText(name: String, text: String) {
        putNextEntry(ZipEntry(name))
        write(text.toByteArray(Charsets.UTF_8))
        closeEntry()
    }

    private fun ZipOutputStream.addFile(name: String, file: File) {
        putNextEntry(ZipEntry(name))
        FileInputStream(file).use { input -> input.copyTo(this) }
        closeEntry()
    }
}
