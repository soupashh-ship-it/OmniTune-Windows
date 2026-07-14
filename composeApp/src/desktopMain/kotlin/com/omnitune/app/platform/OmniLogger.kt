package com.omnitune.app.platform

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object OmniLogger {
    private var logFile: File? = null
    private val stdoutEnabled: Boolean =
        System.getenv("OMNITUNE_LOG_STDOUT")?.equals("true", ignoreCase = true) == true

    fun init(context: PlatformContext) {
        logFile = File(context.logsDir, "omnitune.log")
        info("OmniLogger", "initialized.")
    }

    fun info(tag: String, message: String) = log("INFO", tag, message)
    fun error(tag: String, message: String, t: Throwable? = null) = log("ERROR", tag, message, t)

    private fun log(level: String, tag: String, message: String, t: Throwable? = null) {
        val time = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val thread = Thread.currentThread().name
        val str = "[$time] [$thread] [$level] [$tag] $message" + (t?.let { "\n${it.stackTraceToString()}" } ?: "")
        if (stdoutEnabled) println(str)
        runCatching {
            logFile?.appendText("$str\n")
        }
    }
}
