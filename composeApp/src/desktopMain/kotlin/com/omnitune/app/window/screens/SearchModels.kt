package com.omnitune.app.window.screens

internal data class SearchArtistRow(
    val id: String?,
    val name: String,
    val artwork: String?,
)

internal enum class SearchDiscoverySection {
    Songs, Trending, Artists, Albums, Playlists, Discovery,
}
