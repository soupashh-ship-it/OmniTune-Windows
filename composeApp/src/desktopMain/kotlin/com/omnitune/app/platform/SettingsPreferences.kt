package com.omnitune.app.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import java.util.prefs.Preferences

internal class SettingsPreferences(
    private val prefs: Preferences,
) {
    private val _miniPlayerAlwaysOnTopFlow = MutableStateFlow(prefs.getBoolean("miniPlayerAlwaysOnTop", true))
    val miniPlayerAlwaysOnTopFlow: StateFlow<Boolean> = _miniPlayerAlwaysOnTopFlow.asStateFlow()

    private val _appearanceThemeFlow = MutableStateFlow(prefs.get("appearanceTheme", "nocturne") ?: "nocturne")
    val appearanceThemeFlow: StateFlow<String> = _appearanceThemeFlow.asStateFlow()

    private val _reduceMotionFlow = MutableStateFlow(prefs.getBoolean("reduceMotionEnabled", false))
    val reduceMotionFlow: StateFlow<Boolean> = _reduceMotionFlow.asStateFlow()

    private val _globalShortcutsFlow = MutableStateFlow(prefs.getBoolean("globalShortcutsEnabled", true))
    val globalShortcutsFlow: StateFlow<Boolean> = _globalShortcutsFlow.asStateFlow()

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
        get() {
            val stored = prefs.get("recentSearches.v2", null)
            if (!stored.isNullOrBlank()) {
                return runCatching {
                    val array = JSONArray(stored)
                    (0 until array.length()).mapNotNull { array.optString(it).takeIf(String::isNotBlank) }
                }.getOrDefault(emptyList())
            }

            return (prefs.get("recentSearches", "") ?: "")
                .split(" ")
                .filter { it.isNotBlank() }
        }
        set(value) {
            val deduped = value
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinctBy { it.lowercase() }
                .take(50)
            prefs.put("recentSearches.v2", JSONArray(deduped).toString())
        }

    var followedArtistIds: Set<String>
        get() = (prefs.get("followedArtistIds.v1", "") ?: "").split(",").filter { it.isNotBlank() }.toSet()
        set(value) { prefs.put("followedArtistIds.v1", value.filter { it.isNotBlank() }.distinct().joinToString(",")) }

    var pinnedLibraryCollectionIds: Set<String>
        get() {
            val stored = prefs.get("pinnedLibraryCollectionIds.v1", null)
            if (stored.isNullOrBlank()) {
                return setOf("favorites", "queue", "albums", "artists", "playlists", "downloads")
            }
            return stored.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
        }
        set(value) {
            val sanitized = value
                .filter { it in setOf("favorites", "queue", "albums", "artists", "playlists", "downloads") }
                .ifEmpty { listOf("favorites") }
            prefs.put("pinnedLibraryCollectionIds.v1", sanitized.joinToString(","))
        }

    var shuffleEnabled: Boolean
        get() = prefs.getBoolean("shuffleEnabled", false)
        set(value) { prefs.putBoolean("shuffleEnabled", value) }

    var repeatMode: Int
        get() = prefs.getInt("repeatMode", 0)
        set(value) { prefs.putInt("repeatMode", value) }

    var appearanceTheme: String
        get() = prefs.get("appearanceTheme", "nocturne") ?: "nocturne"
        set(value) {
            prefs.put("appearanceTheme", value)
            _appearanceThemeFlow.value = value
        }

    var reduceMotionEnabled: Boolean
        get() = prefs.getBoolean("reduceMotionEnabled", false)
        set(value) {
            prefs.putBoolean("reduceMotionEnabled", value)
            _reduceMotionFlow.value = value
        }

    var miniPlayerAlwaysOnTop: Boolean
        get() = prefs.getBoolean("miniPlayerAlwaysOnTop", true)
        set(value) {
            prefs.putBoolean("miniPlayerAlwaysOnTop", value)
            _miniPlayerAlwaysOnTopFlow.value = value
        }

    var globalShortcutsEnabled: Boolean
        get() = prefs.getBoolean("globalShortcutsEnabled", true)
        set(value) {
            prefs.putBoolean("globalShortcutsEnabled", value)
            _globalShortcutsFlow.value = value
        }

    var normalizeVolumePreference: Boolean
        get() = prefs.getBoolean("normalizeVolumePreference", false)
        set(value) { prefs.putBoolean("normalizeVolumePreference", value) }

    var spatialAudioPreference: Boolean
        get() = prefs.getBoolean("spatialAudioPreference", false)
        set(value) { prefs.putBoolean("spatialAudioPreference", value) }

    var gaplessPlaybackPreference: Boolean
        get() = prefs.getBoolean("gaplessPlaybackPreference", true)
        set(value) { prefs.putBoolean("gaplessPlaybackPreference", value) }

    var newMusicNotifications: Boolean
        get() = prefs.getBoolean("newMusicNotifications", true)
        set(value) { prefs.putBoolean("newMusicNotifications", value) }

    var recommendationNotifications: Boolean
        get() = prefs.getBoolean("recommendationNotifications", true)
        set(value) { prefs.putBoolean("recommendationNotifications", value) }

    var productUpdateNotifications: Boolean
        get() = prefs.getBoolean("productUpdateNotifications", true)
        set(value) { prefs.putBoolean("productUpdateNotifications", value) }

    var weeklyDigestNotifications: Boolean
        get() = prefs.getBoolean("weeklyDigestNotifications", true)
        set(value) { prefs.putBoolean("weeklyDigestNotifications", value) }

    var concertAlertNotifications: Boolean
        get() = prefs.getBoolean("concertAlertNotifications", false)
        set(value) { prefs.putBoolean("concertAlertNotifications", value) }

    var autoDownloadPlaylists: Boolean
        get() = prefs.getBoolean("autoDownloadPlaylists", false)
        set(value) { prefs.putBoolean("autoDownloadPlaylists", value) }

    var downloadQualityMode: DownloadQualityMode
        get() = runCatching {
            DownloadQualityMode.valueOf(prefs.get("downloadQualityMode", DownloadQualityMode.PROVIDER_DEFAULT.name))
        }.getOrDefault(DownloadQualityMode.PROVIDER_DEFAULT)
        set(value) { prefs.put("downloadQualityMode", value.name) }

    fun addRecentSearch(query: String) {
        val q = query.trim()
        if (q.isBlank()) return
        recentSearches = (listOf(q) + recentSearches.filterNot { it.equals(q, ignoreCase = true) }).take(50)
    }

    fun clearRecentSearches() {
        recentSearches = emptyList()
    }
}
