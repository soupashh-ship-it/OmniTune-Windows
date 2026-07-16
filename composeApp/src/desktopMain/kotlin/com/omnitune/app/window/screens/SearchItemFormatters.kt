package com.omnitune.app.window.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.innertube.models.AlbumItem
import com.omnitune.innertube.models.ArtistItem
import com.omnitune.innertube.models.PlaylistItem
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.models.YTItem

internal fun playSearchItem(player: PlayerViewModel, item: YTItem) {
    when (item) {
        is SongItem -> player.playSong(item)
        is AlbumItem -> player.openAlbum(item.browseId)
        is PlaylistItem -> player.openPlaylist(item.id)
        is ArtistItem -> player.openArtist(item.id)
    }
}

internal fun YTItem.searchStableIdentity(): String = "${id.ifBlank { title }}:$title"

internal fun searchItemKind(item: YTItem): String = when (item) {
    is SongItem -> "Song"
    is AlbumItem -> "Album"
    is ArtistItem -> "Artist"
    is PlaylistItem -> "Playlist"
}

internal fun searchItemSubtitle(item: YTItem): String = when (item) {
    is SongItem -> item.artists.joinToString(", ") { it.name }
    is AlbumItem -> item.artists?.joinToString(", ") { it.name ?: "" }.orEmpty()
    is ArtistItem -> "Artist"
    is PlaylistItem -> item.author?.name ?: item.songCountText.orEmpty()
}

internal fun searchItemMeta(item: YTItem): String = when (item) {
    is SongItem -> listOfNotNull("Single", item.album?.name, item.searchDurationLabel()).joinToString(" · ")
    is AlbumItem -> listOfNotNull(item.year?.toString(), "Album").joinToString(" · ")
    is ArtistItem -> "Artist"
    is PlaylistItem -> item.songCountText ?: "Playlist"
}

internal fun SongItem.searchDurationLabel(): String {
    val duration = duration ?: return ""
    return "${duration / 60}:${(duration % 60).toString().padStart(2, '0')}"
}

internal fun searchGenreIcon(index: Int): ImageVector = when (index % 6) {
    0 -> Icons.Default.GraphicEq
    1 -> Icons.Default.Bolt
    2 -> Icons.Default.Favorite
    3 -> Icons.AutoMirrored.Filled.TrendingUp
    4 -> Icons.Default.GraphicEq
    else -> Icons.Default.Search
}
