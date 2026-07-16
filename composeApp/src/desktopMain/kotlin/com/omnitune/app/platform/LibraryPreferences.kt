package com.omnitune.app.platform

import java.util.prefs.Preferences

internal class LibraryPreferences(
    private val prefs: Preferences,
) {
    private val allowedPinnedCollections = setOf("favorites", "queue", "albums", "artists", "playlists", "downloads")

    var followedArtistIds: Set<String>
        get() = (prefs.get("followedArtistIds.v1", "") ?: "")
            .split(",")
            .filter { it.isNotBlank() }
            .toSet()
        set(value) {
            prefs.put("followedArtistIds.v1", value.filter { it.isNotBlank() }.distinct().joinToString(","))
        }

    var pinnedLibraryCollectionIds: Set<String>
        get() {
            val stored = prefs.get("pinnedLibraryCollectionIds.v1", null)
            if (stored.isNullOrBlank()) {
                return allowedPinnedCollections
            }
            return stored.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
        }
        set(value) {
            val sanitized = value
                .filter { it in allowedPinnedCollections }
                .ifEmpty { listOf("favorites") }
            prefs.put("pinnedLibraryCollectionIds.v1", sanitized.joinToString(","))
        }
}
