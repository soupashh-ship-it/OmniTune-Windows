package com.omnitune.app.platform

import kotlin.test.Test
import kotlin.test.assertEquals

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
}
