package com.omnitune.app.platform

import io.github.selemba1000.JMTC
import io.github.selemba1000.JMTCButtonCallback
import io.github.selemba1000.JMTCCallbacks
import io.github.selemba1000.JMTCEnabledButtons
import io.github.selemba1000.JMTCMediaType
import io.github.selemba1000.JMTCMusicProperties
import io.github.selemba1000.JMTCPlayingState
import io.github.selemba1000.JMTCSeekCallback
import io.github.selemba1000.JMTCSettings
import io.github.selemba1000.JMTCTimelineProperties
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

data class SmtcMetadata(
    val title: String,
    val artist: String,
    val album: String,
    val thumbnailPath: String?,
    val durationMs: Long,
)

internal object SmtcStateMapper {
    fun playbackState(playing: Boolean): JMTCPlayingState =
        if (playing) JMTCPlayingState.PLAYING else JMTCPlayingState.PAUSED

    fun timeline(positionMs: Long, durationMs: Long): JMTCTimelineProperties =
        JMTCTimelineProperties(
            0L,
            durationMs.coerceAtLeast(0L),
            0L,
            durationMs.coerceAtLeast(0L),
        )

    fun position(positionMs: Long): Long =
        positionMs.coerceAtLeast(0L)

    fun musicProperties(metadata: SmtcMetadata): JMTCMusicProperties =
        JMTCMusicProperties(
            metadata.title.ifBlank { "OmniTune" },
            metadata.artist.ifBlank { "Unknown artist" },
            metadata.album,
            metadata.artist,
            emptyArray(),
            0,
            0,
            metadata.thumbnailPath
                ?.let(::File)
                ?.takeIf { it.isFile },
        )
}

class SmtcManager(
    private val onPlayRequested: () -> Unit,
    private val onPauseRequested: () -> Unit,
    private val onNextRequested: () -> Unit,
    private val onPreviousRequested: () -> Unit,
    private val onSeekRequested: (Long) -> Unit
) {
    private var session: JMTC? = null
    private val dispatchingCallback = AtomicBoolean(false)

    fun initialize() {
        if (session != null) return
        runCatching {
            JMTC.getInstance(JMTCSettings("OmniTune", "omnitune")).also { smtc ->
                smtc.setEnabled(true)
                smtc.setMediaType(JMTCMediaType.Music)
                smtc.setEnabledButtons(JMTCEnabledButtons(true, true, false, true, true))
                smtc.setCallbacks(JMTCCallbacks().apply {
                    onPlay = JMTCButtonCallback { dispatch(onPlayRequested) }
                    onPause = JMTCButtonCallback { dispatch(onPauseRequested) }
                    onNext = JMTCButtonCallback { dispatch(onNextRequested) }
                    onPrevious = JMTCButtonCallback { dispatch(onPreviousRequested) }
                    onSeek = JMTCSeekCallback { position -> dispatch { onSeekRequested(position ?: 0L) } }
                })
                session = smtc
            }
        }.onFailure {
            OmniLogger.error("SMTC", "Failed to initialize Windows media controls: ${it.message}", it)
        }
    }

    fun updateMetadata(title: String, artist: String, album: String, thumbnailPath: String?, durationMs: Long) {
        val smtc = session ?: return
        runCatching {
            val metadata = SmtcMetadata(title, artist, album, thumbnailPath, durationMs)
            smtc.setMediaProperties(SmtcStateMapper.musicProperties(metadata))
            smtc.setTimelineProperties(SmtcStateMapper.timeline(0L, durationMs))
            smtc.updateDisplay()
        }.onFailure {
            OmniLogger.error("SMTC", "Failed to update media metadata: ${it.message}", it)
        }
    }

    fun updatePlaybackState(playing: Boolean) {
        val smtc = session ?: return
        runCatching {
            smtc.setPlayingState(SmtcStateMapper.playbackState(playing))
            smtc.updateDisplay()
        }.onFailure {
            OmniLogger.error("SMTC", "Failed to update playback state: ${it.message}", it)
        }
    }

    fun updatePosition(positionMs: Long, durationMs: Long) {
        val smtc = session ?: return
        runCatching {
            smtc.setTimelineProperties(SmtcStateMapper.timeline(positionMs, durationMs))
            smtc.setPosition(SmtcStateMapper.position(positionMs))
        }.onFailure {
            OmniLogger.error("SMTC", "Failed to update timeline position: ${it.message}", it)
        }
    }

    fun dispose() {
        val smtc = session ?: return
        session = null
        runCatching {
            smtc.setPlayingState(JMTCPlayingState.CLOSED)
            smtc.resetDisplay()
            smtc.setEnabled(false)
        }.onFailure {
            OmniLogger.error("SMTC", "Failed to dispose media controls: ${it.message}", it)
        }
    }

    private fun dispatch(callback: () -> Unit) {
        if (!dispatchingCallback.compareAndSet(false, true)) return
        try {
            callback()
        } finally {
            dispatchingCallback.set(false)
        }
    }
}
