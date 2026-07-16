package com.omnitune.app.platform

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VlcjAudioEngineTest {
    @Test
    fun seekTargetClampsNegativeToZero() {
        assertEquals(0L, clampSeekTarget(-5_000L, 180_000L))
    }

    @Test
    fun seekTargetClampsBeyondKnownDuration() {
        assertEquals(180_000L, clampSeekTarget(240_000L, 180_000L))
    }

    @Test
    fun seekTargetAllowsBeyondUnknownDuration() {
        assertEquals(240_000L, clampSeekTarget(240_000L, 0L))
    }

    @Test
    fun releaseCoordinatorAllowsOnlyOneReleaseStart() {
        val coordinator = ReleaseCoordinator()

        assertTrue(coordinator.begin())
        assertFalse(coordinator.begin())
        assertTrue(coordinator.releaseRequested)
        assertFalse(coordinator.releaseCompleted)
    }

    @Test
    fun releaseCoordinatorMarksCompletionIdempotently() {
        val coordinator = ReleaseCoordinator()

        assertTrue(coordinator.begin())
        coordinator.complete()
        coordinator.complete()

        assertTrue(coordinator.releaseRequested)
        assertTrue(coordinator.releaseCompleted)
        assertFalse(coordinator.begin())
    }

    @Test
    fun releaseCoordinatorWaitsForInProgressCompletion() {
        val coordinator = ReleaseCoordinator()

        assertTrue(coordinator.begin())
        val worker = Thread {
            Thread.sleep(25)
            coordinator.complete()
        }
        worker.start()

        assertTrue(coordinator.waitForCompletion(1_000))
        worker.join()
    }

    @Test
    fun releaseCoordinatorWaitTimesOutWhenReleaseDoesNotComplete() {
        val coordinator = ReleaseCoordinator()

        assertTrue(coordinator.begin())

        assertFalse(coordinator.waitForCompletion(10))
    }
}
