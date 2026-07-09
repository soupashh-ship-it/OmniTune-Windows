package com.omnitune.windows.domain.playback

import com.omnitune.innertube.YouTube
import com.omnitune.innertube.models.YouTubeClient
import com.omnitune.innertube.models.response.PlayerResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class StreamFormat(
    val url: String,
    val mimeType: String,
    val bitrate: Int,
    val contentLength: Long
)

class StreamResolver {

    suspend fun resolveStreamUrl(songId: String): String? = withContext(Dispatchers.IO) {
        val response = YouTube.player(songId, client = YouTubeClient.WEB).getOrNull()
            ?: return@withContext null

        selectBestFormat(response)?.url
    }

    private fun selectBestFormat(response: PlayerResponse): StreamFormat? {
        val formats = response.streamingData?.adaptiveFormats ?: return null

        // Priority 1: Audio-only formats (mp4/m4a or webm/opus)
        val audioFormats = formats.filter {
            it.mimeType.startsWith("audio/")
        }.map {
            StreamFormat(
                url = it.url ?: "",
                mimeType = it.mimeType,
                bitrate = it.bitrate ?: 0,
                contentLength = it.contentLength ?: 0L
            )
        }.filter { it.url.isNotBlank() }

        if (audioFormats.isEmpty()) return null

        // Priority 2: Opus over AAC if available and bitrate is acceptable
        val opusFormats = audioFormats.filter { it.mimeType.contains("opus", ignoreCase = true) }
        val aacFormats = audioFormats.filter { it.mimeType.contains("mp4", ignoreCase = true) }

        // Select highest bitrate Opus, fallback to highest bitrate AAC
        val bestOpus = opusFormats.maxByOrNull { it.bitrate }
        val bestAac = aacFormats.maxByOrNull { it.bitrate }

        return bestOpus ?: bestAac ?: audioFormats.firstOrNull()
    }
}
