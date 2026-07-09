package com.omnitune.windows.ui.screens
import com.omnitune.innertube.models.YouTubeClient
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.omnitune.innertube.YouTube
import com.omnitune.innertube.models.SongItem
import com.omnitune.windows.data.DatabaseFactory
import com.omnitune.windows.models.Artist
import com.omnitune.windows.models.Track
import com.omnitune.windows.playback.OmniPlayer
import com.omnitune.windows.ui.theme.OmniColors
import kotlinx.coroutines.launch

@Composable
fun HomeScreen() {
    var homePage by remember { mutableStateOf<com.omnitune.innertube.pages.HomePage?>(null) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        homePage = YouTube.home().getOrNull()
    }

    if (homePage == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = OmniColors.OmniAccentPrimary)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(homePage!!.sections) { section ->
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = OmniColors.TextPrimary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                // Simple horizontal list of items
                androidx.compose.foundation.lazy.LazyRow(modifier = Modifier.fillMaxWidth()) {
                    items(section.items) { item ->
                        Column(modifier = Modifier.width(150.dp).padding(end = 16.dp)) {
                            val thumbnailUrl = when (item) {
                                is com.omnitune.innertube.models.SongItem -> item.thumbnail
                                is com.omnitune.innertube.models.AlbumItem -> item.thumbnail
                                is com.omnitune.innertube.models.PlaylistItem -> item.thumbnail
                                is com.omnitune.innertube.models.ArtistItem -> item.thumbnail
                                else -> null
                            }
                            if (thumbnailUrl != null) {
                                AsyncImage(
                                    model = thumbnailUrl,
                                    contentDescription = "Thumbnail",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(150.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(150.dp)
                                        .background(OmniColors.SurfaceElevated)
                                )
                            }
                            val title = when (item) {
                                is com.omnitune.innertube.models.SongItem -> item.title
                                is com.omnitune.innertube.models.AlbumItem -> item.title
                                is com.omnitune.innertube.models.PlaylistItem -> item.title
                                is com.omnitune.innertube.models.ArtistItem -> item.title
                                else -> "Unknown"
                            }
                            Text(title, color = OmniColors.TextPrimary, maxLines = 1)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SearchScreen(player: OmniPlayer) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<SongItem>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search Songs") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedTextColor = OmniColors.TextPrimary,
                unfocusedTextColor = OmniColors.TextPrimary,
                focusedContainerColor = OmniColors.SurfaceElevated,
                unfocusedContainerColor = OmniColors.SurfaceElevated
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    isSearching = true
                    val result = YouTube.search(query, YouTube.SearchFilter.FILTER_SONG).getOrNull()
                    results = result?.items?.filterIsInstance<SongItem>() ?: emptyList()
                    isSearching = false
                }
            },
            enabled = !isSearching && query.isNotBlank()
        ) {
            Text(if (isSearching) "Searching..." else "Search")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(results) { song ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch {
                                val playerResponse = YouTube.player(song.id, client = YouTubeClient.WEB).getOrNull()
                                val streamUrl = playerResponse?.streamingData?.adaptiveFormats
                                    ?.firstOrNull { it.mimeType.contains("audio/mp4") || it.mimeType.contains("audio/webm") }
                                    ?.url
                                
                                if (streamUrl != null) {
                                    val track = Track(
                                        id = song.id,
                                        title = song.title,
                                        artists = song.artists.map { Artist(it.id, it.name) },
                                        durationSeconds = song.duration ?: 0
                                    )
                                    player.play(track, streamUrl)
                                }
                            }
                        }
                        .padding(16.dp)
                ) {
                    Column {
                        Text(song.title, color = OmniColors.TextPrimary)
                        Text(song.artists.joinToString { it.name }, color = OmniColors.TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryScreen() {
    val database = remember { DatabaseFactory.createDatabase() }
    var likedSongs by remember { mutableStateOf<List<com.omnitune.windows.db.SongEntity>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        likedSongs = database.libraryQueries.getAllLikedSongs().executeAsList()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Liked Songs", style = MaterialTheme.typography.headlineMedium, color = OmniColors.TextPrimary)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (likedSongs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No liked songs yet.", color = OmniColors.TextSecondary)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(likedSongs) { song ->
                    Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        if (song.thumbnailUrl != null) {
                            AsyncImage(
                                model = song.thumbnailUrl,
                                contentDescription = null,
                                modifier = Modifier.size(50.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                        Column {
                            Text(song.title, color = OmniColors.TextPrimary)
                            Text(song.artists, color = OmniColors.TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Settings Screen", color = OmniColors.TextSecondary)
    }
}
