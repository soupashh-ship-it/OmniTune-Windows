package com.omnitune.app.platform

import java.util.prefs.Preferences

class SettingsRepository {
    private val prefs = Preferences.userNodeForPackage(SettingsRepository::class.java)

    var volume: Int
        get() = prefs.getInt("volume", 100).coerceIn(0, 200)
        set(value) { prefs.putInt("volume", value.coerceIn(0, 200)) }

    var windowWidth: Int
        get() = prefs.getInt("windowWidth", 1200)
        set(value) { prefs.putInt("windowWidth", value) }

    var windowHeight: Int
        get() = prefs.getInt("windowHeight", 800)
        set(value) { prefs.putInt("windowHeight", value) }

    var recentSearches: List<String>
        get() = (prefs.get("recentSearches", "") ?: "").split(" ").filter { it.isNotBlank() }
        set(value) { prefs.put("recentSearches", value.takeLast(8).joinToString(" ")) }

    var likedSongIds: Set<String>
        get() = (prefs.get("likedSongIds", "") ?: "").split(",").filter { it.isNotBlank() }.toSet()
        set(value) { prefs.put("likedSongIds", value.joinToString(",")) }

    var shuffleEnabled: Boolean
        get() = prefs.getBoolean("shuffleEnabled", false)
        set(value) { prefs.putBoolean("shuffleEnabled", value) }

    var repeatMode: Int
        get() = prefs.getInt("repeatMode", 0)
        set(value) { prefs.putInt("repeatMode", value) }

    var appearanceTheme: String
        get() = prefs.get("appearanceTheme", "nocturne") ?: "nocturne"
        set(value) { prefs.put("appearanceTheme", value) }

    var reduceMotionEnabled: Boolean
        get() = prefs.getBoolean("reduceMotionEnabled", false)
        set(value) { prefs.putBoolean("reduceMotionEnabled", value) }

    var miniPlayerAlwaysOnTop: Boolean
        get() = prefs.getBoolean("miniPlayerAlwaysOnTop", true)
        set(value) { prefs.putBoolean("miniPlayerAlwaysOnTop", value) }

    fun addRecentSearch(query: String) {
        val q = query.trim()
        if (q.isBlank()) return
        val updated = (listOf(q) + recentSearches.filter { it != q }).take(8)
        recentSearches = updated
    }

    fun flush() {
        prefs.flush()
    }
}
