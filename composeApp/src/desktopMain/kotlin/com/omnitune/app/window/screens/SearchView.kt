package com.omnitune.app.window.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.app.platform.PlaybackState
import com.omnitune.app.window.BorderLow
import com.omnitune.app.window.IrisSoft
import com.omnitune.app.window.LocalHomeReferenceMetrics
import com.omnitune.app.window.LocalOmniMotionPolicy
import com.omnitune.app.window.OmniGradients
import com.omnitune.app.window.OmniReferenceColors
import com.omnitune.app.window.Shapes
import com.omnitune.app.window.Surface1
import com.omnitune.app.window.Surface2
import com.omnitune.app.window.TextMuted
import com.omnitune.app.window.TextPrimary
import com.omnitune.app.window.TextSecondary
import com.omnitune.app.window.components.OmniSearchField
import com.omnitune.innertube.models.AlbumItem
import com.omnitune.innertube.models.ArtistItem
import com.omnitune.innertube.models.PlaylistItem
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.models.YTItem
import com.omnitune.innertube.toHighResThumbnail

@Composable
fun SearchView(
    player: PlayerViewModel,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchFieldFocusChanged: (Boolean) -> Unit = {},
) {
    val results by player.searchResults.collectAsState()
    val loading by player.searchLoading.collectAsState()
    val error by player.searchError.collectAsState()
    val discoveryGenres by player.discoveryGenres.collectAsState()
    val discoveryTrending by player.discoveryTrending.collectAsState()
    val discoveryNew by player.discoveryNew.collectAsState()
    val currentSong by player.currentSong.collectAsState()
    val playbackState by player.playbackState.collectAsState()
    val recents by player.recentSearches.collectAsState()

    val allItems = remember(results, discoveryTrending, discoveryNew) {
        (results + discoveryTrending + discoveryNew).distinctBy { it.searchStableIdentity() }
    }
    val fallbackItems = remember(discoveryTrending, discoveryNew) {
        (discoveryTrending + discoveryNew).distinctBy { it.searchStableIdentity() }
    }
    val visibleItems = if (results.isNotEmpty()) allItems else fallbackItems
    val songs = (results.filterIsInstance<SongItem>().ifEmpty { visibleItems.filterIsInstance<SongItem>() }).distinctBy { it.id }
    val albums = (results.filterIsInstance<AlbumItem>().ifEmpty { visibleItems.filterIsInstance<AlbumItem>() }).distinctBy { it.id }
    val playlists = (results.filterIsInstance<PlaylistItem>().ifEmpty { visibleItems.filterIsInstance<PlaylistItem>() }).distinctBy { it.id }
    val artists = remember(results, songs, visibleItems) {
        val explicitArtists = results.filterIsInstance<ArtistItem>().map {
            SearchArtistRow(it.id, it.title, it.thumbnail)
        }
        val songArtists = songs.flatMap { song ->
            song.artists.map { artist ->
                SearchArtistRow(artist.id, artist.name, song.thumbnail)
            }
        }
        (explicitArtists + songArtists)
            .filter { it.name.isNotBlank() }
            .distinctBy { it.id ?: it.name }
    }
    val topResult = results.firstOrNull() ?: visibleItems.firstOrNull()
    val discoveryShelf = discoveryNew.ifEmpty { visibleItems }.distinctBy { it.searchStableIdentity() }
    val recentChipTerms = recents.ifEmpty {
        (discoveryTrending.map { it.title } + visibleItems.map { it.title })
            .filter { it.isNotBlank() }
            .distinct()
            .take(6)
    }
    val primaryGenres = listOf("Hip Hop", "Afrobeats", "Pop", "R&B", "Electronic", "Rock", "Indie")
    val fallbackGenres = listOf(
        "Dance", "Soul", "Jazz", "Classical", "Alternative", "Metal", "Reggae", "Latin",
        "K-Pop", "Bollywood", "Punjabi", "Lo-fi", "Ambient", "Country", "Blues", "Folk"
    )
    val genreTitles = (primaryGenres + discoveryGenres.map { it.title } + fallbackGenres)
        .map { it.trim() }
        .filter { it.isNotBlank() && !it.equals("More", ignoreCase = true) }
        .distinctBy { it.lowercase() }
    val trendingTerms = discoveryTrending.map { it.title }.ifEmpty {
        (recents + visibleItems.map { it.title }).distinct().take(5)
    }

    SearchDiscoveryReferenceContent(
        query = query,
        onQueryChange = onQueryChange,
        recents = recentChipTerms,
        genreTitles = genreTitles,
        topResult = topResult,
        songs = songs,
        trendingTerms = trendingTerms,
        artists = artists,
        albums = albums,
        playlists = playlists,
        discoveryShelf = discoveryShelf,
        currentSong = currentSong,
        playbackState = playbackState,
        loading = loading,
        error = error,
        onSearch = { term ->
            onQueryChange(term)
            player.search(term)
        },
        onClearRecents = { player.clearRecentSearches() },
        onPlayItem = { item -> playSearchItem(player, item) },
        onPlaySong = { song, index -> player.playSong(song, index) },
        onAddToQueue = { player.addToQueue(it) },
        onPlayNext = { player.playNext(it) },
        onLike = { player.toggleLike(it.id) },
        onOpenArtist = { id -> if (id != null) player.openArtist(id) },
        onSearchFieldFocusChanged = onSearchFieldFocusChanged,
    )
}
