package com.omnitune.windows.models

data class Track(
    val id: String,
    val title: String,
    val artists: List<Artist>,
    val durationSeconds: Int,
    val thumbnailUrl: String? = null,
    val album: Album? = null,
    val explicit: Boolean = false,
    val liked: Boolean = false
)

data class Artist(
    val id: String?,
    val name: String,
    val thumbnailUrl: String? = null
)

data class Album(
    val id: String,
    val title: String
)

data class QueueEntity(
    val track: Track,
    val queueId: Long = 0
)

data class Playlist(
    val id: String,
    val title: String,
    val trackCount: Int = 0
)

data class Lyrics(
    val text: String,
    val isSynced: Boolean = false,
    val lines: List<LyricsLine> = emptyList()
)

data class LyricsLine(
    val text: String,
    val timeMs: Long
)
