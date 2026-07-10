package com.omnitune.app.window.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.app.platform.PlaybackState
import com.omnitune.app.window.*
import com.omnitune.app.window.components.*
import com.omnitune.innertube.models.*
import com.omnitune.innertube.toHighResThumbnail

@Composable
fun SearchView(player: PlayerViewModel, query: String, onQueryChange: (String) -> Unit) {
    val results by player.searchResults.collectAsState()
    val loading by player.searchLoading.collectAsState()
    val error by player.searchError.collectAsState()
    
    val discLoading by player.discoveryLoading.collectAsState()
    val discGenres by player.discoveryGenres.collectAsState()
    val discTrending by player.discoveryTrending.collectAsState()
    val discNew by player.discoveryNew.collectAsState()
    
    val currentSong by player.currentSong.collectAsState()
    val playbackState by player.playbackState.collectAsState()
    val recents = player.recentSearches

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        // Page Title & Subtitle
        item {
            Column {
                Text("Search & Discovery", style = MaterialTheme.typography.displaySmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("Find exactly what you're looking for and discover what moves you.", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
            }
        }

        // Large Page Search Field
        item {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                OmniSearchField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = "Search for songs, artists, albums, playlists...",
                    modifier = Modifier.width(740.dp).height(52.dp),
                    onEnter = { player.search(query) },
                    onEscape = { onQueryChange("") }
                )
            }
        }

        if (query.isBlank() || (loading && results.isEmpty())) {
            // Discovery State
            if (error != null) {
                item { OmniEmptyState("Couldn't search", error ?: "Unknown error") }
            } else if (discLoading) {
                item { DiscoveryShimmer() }
            } else {
                if (recents.isNotEmpty()) {
                    item {
                        OmniSectionHeader("Recent Searches", actionLabel = "Clear", onAction = { player.clearRecentSearches() })
                        Spacer(Modifier.height(16.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(recents) { r ->
                                Box(
                                    modifier = Modifier.clip(Shapes.pill).background(Surface2).border(1.dp, BorderLow, Shapes.pill)
                                        .clickable { onQueryChange(r); player.search(r) }.padding(horizontal = 12.dp, vertical = 6.dp),
                                ) {
                                    Text(r, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }

                if (discGenres.isNotEmpty()) {
                    item {
                        OmniSectionHeader("Explore Genres")
                        Spacer(Modifier.height(16.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(discGenres) { genre ->
                                Box(
                                    modifier = Modifier.clip(Shapes.pill).background(Surface2).border(1.dp, BorderLow, Shapes.pill)
                                        .clickable { onQueryChange(genre.title); player.search(genre.title) }.padding(horizontal = 16.dp, vertical = 8.dp),
                                ) {
                                    Text(genre.title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                        // Left column for Discovery Cards (equivalent to Top Result space)
                        Column(modifier = Modifier.weight(1.5f)) {
                            OmniSectionHeader("Discover Something New", actionLabel = "See all", onAction = {})
                            Spacer(Modifier.height(16.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                items(discNew) { item ->
                                    OmniMediaCard(
                                        title = item.title,
                                        subtitle = (item as? SongItem)?.artists?.firstOrNull()?.name ?: (item as? AlbumItem)?.artists?.firstOrNull()?.name,
                                        artworkUrl = item.thumbnail,
                                        modifier = Modifier.width(160.dp).height(180.dp),
                                        onClick = {
                                            if (item is SongItem) player.playSong(item)
                                            else if (item is AlbumItem) player.openAlbum(item.browseId)
                                            else if (item is PlaylistItem) player.playPlaylist(item.id)
                                        }
                                    )
                                }
                            }
                        }

                        // Right column for Trending Searches panel
                        Column(modifier = Modifier.width(300.dp)) {
                            OmniSectionHeader("Trending Searches", actionLabel = "See all", onAction = {})
                            Spacer(Modifier.height(16.dp))
                            Column(modifier = Modifier.clip(Shapes.medium).background(Surface1).border(1.dp, BorderLow, Shapes.medium).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                discTrending.take(5).forEachIndexed { index, song ->
                                    Row(modifier = Modifier.fillMaxWidth().height(36.dp).clip(Shapes.small).clickable { onQueryChange(song.title); player.search(song.title) }.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text("${index + 1}", color = TextSecondary, style = MaterialTheme.typography.titleMedium, modifier = Modifier.width(20.dp))
                                        Text(song.title, color = TextPrimary, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                        Text("↗", color = IrisSoft, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Search Results State
            if (error != null) {
                item { OmniEmptyState("Couldn't search", error ?: "Unknown error") }
            } else if (loading) {
                item { ResultsShimmer() }
            } else {
                val songs = results.filterIsInstance<SongItem>()
                val artists = results.filterIsInstance<ArtistItem>()
                val albums = results.filterIsInstance<AlbumItem>()
                val playlists = results.filterIsInstance<PlaylistItem>()
                val topResult = results.firstOrNull()

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                        // Top Result
                        Column(modifier = Modifier.width(280.dp)) {
                            OmniSectionHeader("Top Result")
                            Spacer(Modifier.height(16.dp))
                            if (topResult != null) {
                                TopResultCard(topResult, player)
                            }
                        }

                        // Songs
                        Column(modifier = Modifier.weight(1f)) {
                            OmniSectionHeader("Songs", actionLabel = "See all", onAction = {})
                            Spacer(Modifier.height(16.dp))
                            songs.take(5).forEachIndexed { index, song ->
                                OmniSongRow(
                                    item = song,
                                    isActive = song.id == currentSong?.id,
                                    isPlaying = song.id == currentSong?.id && playbackState == PlaybackState.PLAYING,
                                    onClick = { player.playSong(song, index) },
                                    onPlayNext = { player.playNext(song) },
                                    onAddToQueue = { player.addToQueue(song) },
                                    onLike = { player.toggleLike(song.id) },
                                )
                            }
                        }
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                        // Left column for Artists / Albums
                        Column(modifier = Modifier.weight(1.5f)) {
                            if (albums.isNotEmpty()) {
                                OmniSectionHeader("Albums", actionLabel = "See all", onAction = {})
                                Spacer(Modifier.height(16.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    items(albums.take(5)) { album ->
                                        OmniMediaCard(
                                            title = album.title,
                                            subtitle = album.artists?.firstOrNull()?.name,
                                            artworkUrl = album.thumbnail,
                                            modifier = Modifier.width(140.dp).height(190.dp),
                                            onClick = { player.openAlbum(album.browseId) }
                                        )
                                    }
                                }
                                Spacer(Modifier.height(32.dp))
                            }
                            if (playlists.isNotEmpty()) {
                                OmniSectionHeader("Playlists", actionLabel = "See all", onAction = {})
                                Spacer(Modifier.height(16.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    items(playlists.take(5)) { playlist ->
                                        OmniMediaCard(
                                            title = playlist.title,
                                            subtitle = playlist.songCountText ?: playlist.author?.name,
                                            artworkUrl = playlist.thumbnail,
                                            modifier = Modifier.width(140.dp).height(190.dp),
                                            onClick = { player.playPlaylist(playlist.id) }
                                        )
                                    }
                                }
                            }
                        }

                        // Right column for Artists list
                        if (artists.isNotEmpty()) {
                            Column(modifier = Modifier.width(300.dp)) {
                                OmniSectionHeader("Artists", actionLabel = "See all", onAction = {})
                                Spacer(Modifier.height(16.dp))
                                Column(modifier = Modifier.clip(Shapes.medium).background(Surface1).border(1.dp, BorderLow, Shapes.medium).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    artists.take(5).forEach { artist ->
                                        Row(modifier = Modifier.fillMaxWidth().height(48.dp).clip(Shapes.small).clickable { player.openArtist(artist.id) }.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                            AsyncImage(model = artist.thumbnail?.toHighResThumbnail(), contentDescription = null, modifier = Modifier.size(36.dp).clip(androidx.compose.foundation.shape.CircleShape), contentScale = ContentScale.Crop)
                                            Spacer(Modifier.width(12.dp))
                                            Text(artist.title, color = TextPrimary, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                            Text(">", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopResultCard(item: YTItem, player: PlayerViewModel) {
    val title = item.title
    val subtitle = when(item) {
        is SongItem -> "Song • ${item.artists.firstOrNull()?.name}"
        is AlbumItem -> "Album • ${item.artists?.firstOrNull()?.name}"
        is ArtistItem -> "Artist"
        is PlaylistItem -> "Playlist • ${item.songCountText}"
        else -> ""
    }
    
    OmniSurface(
        shape = Shapes.medium,
        color = Surface1,
        modifier = Modifier.fillMaxWidth().height(200.dp),
        onClick = {
            if (item is SongItem) player.playSong(item)
            else if (item is AlbumItem) player.openAlbum(item.browseId)
            else if (item is ArtistItem) player.openArtist(item.id)
            else if (item is PlaylistItem) player.playPlaylist(item.id)
        }
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            AsyncImage(model = item.thumbnail?.toHighResThumbnail(), contentDescription = null, modifier = Modifier.size(80.dp).clip(Shapes.artworkSmall), contentScale = ContentScale.Crop)
            Spacer(Modifier.height(16.dp))
            Text(title, style = MaterialTheme.typography.displaySmall, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                val playActionInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(OmniGradients.primaryAction)
                        .clickable(interactionSource = playActionInteraction, indication = androidx.compose.material3.ripple(bounded = false, radius = 24.dp)) {
                            if (item is SongItem) player.playSong(item)
                            else if (item is AlbumItem) player.playAlbum(item.browseId)
                            else if (item is PlaylistItem) player.playPlaylist(item.id)
                        }
                        .pressBounce(playActionInteraction),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
private fun DiscoveryShimmer() {
    Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
        OmniShimmerBlock(Modifier.width(120.dp).height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for(i in 1..4) OmniShimmerBlock(Modifier.width(80.dp).height(32.dp).clip(Shapes.pill))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            OmniShimmerBlock(Modifier.weight(1.5f).height(180.dp))
            OmniShimmerBlock(Modifier.width(300.dp).height(180.dp))
        }
    }
}

@Composable
private fun ResultsShimmer() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
        Column(modifier = Modifier.width(280.dp)) {
            OmniShimmerBlock(Modifier.width(100.dp).height(24.dp))
            Spacer(Modifier.height(16.dp))
            OmniShimmerBlock(Modifier.fillMaxWidth().height(200.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            OmniShimmerBlock(Modifier.width(100.dp).height(24.dp))
            Spacer(Modifier.height(16.dp))
            for(i in 1..4) {
                OmniShimmerBlock(Modifier.fillMaxWidth().height(48.dp))
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
