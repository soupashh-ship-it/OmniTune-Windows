package com.omnitune.app.player

import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.models.YTItem

internal object RelatedContentPolicy {
    const val MaxResults: Int = 32

    fun clean(currentSongId: String, items: List<YTItem>): List<YTItem> =
        items
            .filterNot { it is SongItem && it.id == currentSongId }
            .distinctBy { it.id }
            .take(MaxResults)
}
