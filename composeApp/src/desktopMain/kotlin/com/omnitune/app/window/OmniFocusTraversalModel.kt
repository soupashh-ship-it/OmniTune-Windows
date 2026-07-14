package com.omnitune.app.window

enum class OmniFocusScope {
    Search,
    Library,
    PlaylistDetail,
    NowPlaying,
    Queue,
    Settings,
    Downloads,
    MiniPlayer,
    Sidebar,
    BottomPlayer,
}

data class OmniFocusNode(
    val id: String,
    val description: String,
)

object OmniFocusTraversalModel {
    private val orders: Map<OmniFocusScope, List<OmniFocusNode>> = mapOf(
        OmniFocusScope.Search to listOf(
            "global-search" to "Global search field",
            "search-page-input" to "Search page input",
            "recent-searches" to "Recent search chips",
            "clear-recents" to "Clear recent searches",
            "genre-chips" to "Genre chips",
            "result-actions" to "Result play/add/open actions",
            "bottom-player" to "Bottom player controls",
        ),
        OmniFocusScope.Library to listOf(
            "library-tabs" to "Library tabs",
            "sort" to "Sort control",
            "view-toggle" to "View toggle",
            "pinned-collections" to "Pinned collection actions",
            "song-actions" to "Song row actions",
        ),
        OmniFocusScope.PlaylistDetail to listOf(
            "primary-actions" to "Play/shuffle/download actions",
            "track-actions" to "Track row actions",
            "right-rail" to "Right rail content actions",
        ),
        OmniFocusScope.NowPlaying to listOf(
            "lyrics-tabs" to "Lyrics and Related tabs",
            "like-actions" to "Like and more actions",
            "seek" to "Seek control",
            "transport" to "Transport controls",
            "lyrics-controls" to "Lyrics panel controls",
            "bottom-player" to "Bottom player controls",
        ),
        OmniFocusScope.Queue to listOf(
            "queue-rows" to "Queue row actions",
            "queue-controls" to "Clear, shuffle, repeat, save actions",
            "history" to "Session history actions",
            "recommendations" to "Recommendation actions",
        ),
        OmniFocusScope.Settings to listOf(
            "account" to "Account/settings card actions",
            "audio" to "Audio controls",
            "playback" to "Playback controls",
            "appearance" to "Theme and reduced-motion controls",
            "downloads" to "Download settings controls",
            "shortcuts-about" to "Shortcut/about actions",
        ),
        OmniFocusScope.Downloads to listOf(
            "top-actions" to "Quality settings and Pause All",
            "download-tabs" to "Download filter tabs",
            "task-actions" to "Download task actions",
            "quality" to "Quality radio controls",
            "right-rail" to "Supplemental right rail controls",
        ),
        OmniFocusScope.MiniPlayer to listOf(
            "artwork-title" to "Open now playing",
            "transport" to "Mini player transport",
            "close" to "Close mini player",
        ),
        OmniFocusScope.Sidebar to listOf(
            "primary-nav" to "Primary navigation",
            "library-subnav" to "Library subnavigation",
            "user-playlists" to "User playlist shortcuts",
            "settings" to "Settings shortcut",
        ),
        OmniFocusScope.BottomPlayer to listOf(
            "track-summary" to "Open now playing",
            "like-more" to "Like and more controls",
            "transport" to "Playback transport",
            "seek" to "Seek bar",
            "queue-volume" to "Queue and volume controls",
        ),
    ).mapValues { (_, entries) -> entries.map { OmniFocusNode(it.first, it.second) } }

    fun order(scope: OmniFocusScope): List<OmniFocusNode> = orders.getValue(scope)

    fun next(scope: OmniFocusScope, currentId: String?): OmniFocusNode? {
        val order = order(scope)
        if (order.isEmpty()) return null
        if (currentId == null) return order.first()
        val index = order.indexOfFirst { it.id == currentId }
        return if (index == -1 || index == order.lastIndex) null else order[index + 1]
    }

    fun previous(scope: OmniFocusScope, currentId: String?): OmniFocusNode? {
        val order = order(scope)
        if (order.isEmpty() || currentId == null) return null
        val index = order.indexOfFirst { it.id == currentId }
        return if (index <= 0) null else order[index - 1]
    }
}
