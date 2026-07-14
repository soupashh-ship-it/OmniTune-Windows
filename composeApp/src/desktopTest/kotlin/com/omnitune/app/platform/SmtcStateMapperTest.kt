package com.omnitune.app.platform

import io.github.selemba1000.JMTCPlayingState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SmtcStateMapperTest {
    @Test
    fun playbackStateMapsPlayingAndPausedTruthfully() {
        assertEquals(JMTCPlayingState.PLAYING, SmtcStateMapper.playbackState(true))
        assertEquals(JMTCPlayingState.PAUSED, SmtcStateMapper.playbackState(false))
    }

    @Test
    fun timelineAndPositionClampNegativeValues() {
        val timeline = SmtcStateMapper.timeline(positionMs = -500, durationMs = -1)

        assertEquals(0L, timeline.start)
        assertEquals(0L, timeline.end)
        assertEquals(0L, timeline.seekStart)
        assertEquals(0L, timeline.seekEnd)
        assertEquals(0L, SmtcStateMapper.position(-500))
    }

    @Test
    fun metadataFallsBackWithoutFakingUnknownValues() {
        val props = SmtcStateMapper.musicProperties(
            SmtcMetadata(
                title = "",
                artist = "",
                album = "",
                thumbnailPath = "Z:/not/a/real/file.jpg",
                durationMs = 1000L,
            )
        )

        assertEquals("OmniTune", props.title)
        assertEquals("Unknown artist", props.artist)
        assertEquals("", props.albumTitle)
        assertNull(props.art)
        assertTrue(props.genres.isEmpty())
    }
}
