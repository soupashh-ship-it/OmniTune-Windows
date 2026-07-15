package com.omnitune.app.platform

/**
 * Centralized access to internal runtime QA switches.
 *
 * These environment variables are intentionally kept for screenshot/runtime QA
 * workflows, but all direct reads should go through this object so normal
 * production code paths stay explicit and auditable.
 */
internal object QaRuntime {
    val miniAlwaysOnTop: Boolean
        get() = flag("OMNITUNE_QA_MINI_AOT")

    val queueSaveUi: Boolean
        get() = flag("OMNITUNE_QA_QUEUE_SAVE_UI")

    val queueSaveVerifyOnly: Boolean
        get() = flag("OMNITUNE_QA_QUEUE_SAVE_VERIFY_ONLY")

    val queuePlaylistName: String?
        get() = value("OMNITUNE_QA_QUEUE_PLAYLIST_NAME")

    val requireBundledVlc: Boolean
        get() = flag("OMNITUNE_QA_REQUIRE_BUNDLED_VLC")

    val theme: String
        get() = value("OMNITUNE_QA_THEME")?.lowercase().orEmpty()

    val route: String
        get() = value("OMNITUNE_QA_ROUTE")?.lowercase().orEmpty()

    val searchQuery: String
        get() = value("OMNITUNE_QA_SEARCH_QUERY").orEmpty()

    val playlistState: String
        get() = value("OMNITUNE_QA_PLAYLIST_STATE")?.lowercase().orEmpty()

    private fun flag(name: String): Boolean = value(name) == "true"

    private fun value(name: String): String? = System.getenv(name)?.trim()?.takeIf { it.isNotBlank() }
}
