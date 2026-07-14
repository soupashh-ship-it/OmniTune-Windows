package com.omnitune.app.platform

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object AtomicFileStore {
    @Volatile
    private var failurePolicy: ((File, Operation) -> Unit)? = null

    enum class Operation {
        BEFORE_TEMP_WRITE,
        BEFORE_REPLACE,
    }

    fun <T> withFailurePolicyForTest(policy: (File, Operation) -> Unit, block: () -> T): T {
        val previous = failurePolicy
        failurePolicy = policy
        return try {
            block()
        } finally {
            failurePolicy = previous
        }
    }

    fun writeText(target: File, content: String) {
        target.parentFile?.mkdirs()
        val temp = File(target.parentFile ?: File("."), "${target.name}.tmp-${System.currentTimeMillis()}-${System.nanoTime()}")
        val backup = File(target.parentFile ?: File("."), "${target.name}.bak")

        try {
            failurePolicy?.invoke(target, Operation.BEFORE_TEMP_WRITE)
            FileOutputStream(temp).use { stream ->
                stream.write(content.toByteArray(StandardCharsets.UTF_8))
                stream.fd.sync()
            }
            if (target.isFile) {
                runCatching {
                    Files.copy(
                        target.toPath(),
                        backup.toPath(),
                        StandardCopyOption.REPLACE_EXISTING,
                    )
                }.onFailure {
                    OmniLogger.error("AtomicFileStore", "Failed to update backup for ${target.name}.", it)
                }
            }
            failurePolicy?.invoke(target, Operation.BEFORE_REPLACE)
            moveReplacing(temp, target)
        } catch (t: Throwable) {
            runCatching { temp.delete() }
            OmniLogger.error("AtomicFileStore", "Atomic write failed for ${target.absolutePath}.", t)
            throw if (t is IOException) t else IOException("Atomic write failed for ${target.name}", t)
        }
    }

    private fun moveReplacing(source: File, target: File) {
        try {
            Files.move(
                source.toPath(),
                target.toPath(),
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING,
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(
                source.toPath(),
                target.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
            )
        }
    }
}
