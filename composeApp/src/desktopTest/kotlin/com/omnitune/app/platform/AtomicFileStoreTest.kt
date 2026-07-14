package com.omnitune.app.platform

import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AtomicFileStoreTest {
    @Test
    fun writeCreatesPrimaryWithoutLeavingTempFiles() {
        withTempDir { root ->
            val file = File(root, "state.json")

            AtomicFileStore.writeText(file, """{"ok":true}""")

            assertEquals("""{"ok":true}""", file.readText())
            assertFalse(root.listFiles().orEmpty().any { it.name.startsWith("state.json.tmp-") })
        }
    }

    @Test
    fun secondWriteKeepsLastKnownBackup() {
        withTempDir { root ->
            val file = File(root, "state.json")

            AtomicFileStore.writeText(file, "old")
            AtomicFileStore.writeText(file, "new")

            assertEquals("new", file.readText())
            assertEquals("old", File(root, "state.json.bak").readText())
            assertFalse(root.listFiles().orEmpty().any { it.name.startsWith("state.json.tmp-") })
        }
    }

    @Test
    fun staleTempFileDoesNotBlockFutureWrite() {
        withTempDir { root ->
            val file = File(root, "state.json")
            File(root, "state.json.tmp-stale").writeText("partial")

            AtomicFileStore.writeText(file, "valid")

            assertEquals("valid", file.readText())
            assertTrue(File(root, "state.json.tmp-stale").isFile)
        }
    }

    @Test
    fun simulatedPermissionDeniedBeforeTempWritePreservesPrimary() {
        withTempDir { root ->
            val file = File(root, "state.json").apply { writeText("good") }

            assertFailsWith<IOException> {
                AtomicFileStore.withFailurePolicyForTest({ _, operation ->
                    if (operation == AtomicFileStore.Operation.BEFORE_TEMP_WRITE) {
                        throw IOException("Simulated permission denied")
                    }
                }) {
                    AtomicFileStore.writeText(file, "bad")
                }
            }

            assertEquals("good", file.readText())
            assertFalse(root.listFiles().orEmpty().any { it.name.startsWith("state.json.tmp-") })
        }
    }

    @Test
    fun simulatedNoSpaceBeforeReplacePreservesPrimaryAndCreatesBackup() {
        withTempDir { root ->
            val file = File(root, "state.json").apply { writeText("good") }

            assertFailsWith<IOException> {
                AtomicFileStore.withFailurePolicyForTest({ _, operation ->
                    if (operation == AtomicFileStore.Operation.BEFORE_REPLACE) {
                        throw IOException("Simulated no space left on device")
                    }
                }) {
                    AtomicFileStore.writeText(file, "bad")
                }
            }

            assertEquals("good", file.readText())
            assertEquals("good", File(root, "state.json.bak").readText())
            assertFalse(root.listFiles().orEmpty().any { it.name.startsWith("state.json.tmp-") })
        }
    }

    private fun withTempDir(block: (File) -> Unit) {
        val root = Files.createTempDirectory("omnitune-atomic-file-test").toFile()
        try {
            block(root)
        } finally {
            root.deleteRecursively()
        }
    }
}
