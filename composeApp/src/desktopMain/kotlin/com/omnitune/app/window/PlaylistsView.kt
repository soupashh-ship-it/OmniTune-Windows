package com.omnitune.app.window
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.app.platform.SavedQueuePlaylist
import com.omnitune.innertube.models.Artist
import com.omnitune.innertube.models.PlaylistItem
import com.omnitune.innertube.toHighResThumbnail

@Composable
fun PlaylistsView(
    player: PlayerViewModel,
    onEditableTextFocusChanged: (Boolean) -> Unit = {},
) {
    val results by player.playlistResults.collectAsState()
    val loading by player.playlistLoading.collectAsState()
    val error by player.playlistError.collectAsState()
    val savedPlaylists by player.savedQueuePlaylists.collectAsState()
    var query by remember { mutableStateOf("") }
    var createDialogOpen by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var createError by remember { mutableStateOf<String?>(null) }
    var discoverySearchStarted by remember { mutableStateOf(false) }

    val localPlaylists = remember(savedPlaylists) {
        savedPlaylists.map { it.toPlaylistItem() }
    }
    val providerPlaylists = remember(results) {
        results.filterIsInstance<PlaylistItem>()
    }
    LaunchedEffect(savedPlaylists.isEmpty(), query) {
        if (savedPlaylists.isEmpty() && query.isBlank() && !discoverySearchStarted) {
            discoverySearchStarted = true
            player.searchPlaylists("music playlists")
        }
    }
    val visibleLocalPlaylists = remember(localPlaylists, query) {
        if (query.isBlank()) localPlaylists
        else localPlaylists.filter { it.title.contains(query, ignoreCase = true) }
    }
    val visiblePlaylists = remember(visibleLocalPlaylists, providerPlaylists, query, savedPlaylists) {
        val combined = when {
            query.isBlank() && savedPlaylists.isEmpty() -> providerPlaylists
            query.isBlank() -> visibleLocalPlaylists
            else -> visibleLocalPlaylists + providerPlaylists
        }
        combined.distinctBy { it.id }
    }

    Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text("Playlists", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = TextWhite)
                Text(
                    "Your saved playlists and online playlist search",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray,
                )
            }
            Button(
                onClick = { createDialogOpen = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Create playlist")
            }
        }
        Spacer(Modifier.height(20.dp))

        Surface(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp),
            color = BgCard.copy(alpha = 0.92f),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLow.copy(alpha = 0.55f)),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp)) {
                Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp), tint = TextGray)
                Spacer(Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f).onKeyEvent { event ->
                    if ((event.key == Key.Enter || event.key == Key.NumPadEnter) && event.type == KeyEventType.KeyDown) {
                        player.searchPlaylists(query)
                        true
                    } else false
                }) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .fillMaxSize()
                            .onFocusChanged { onEditableTextFocusChanged(it.isFocused) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextWhite),
                        decorationBox = { innerTextField ->
                            if (query.isEmpty()) {
                                Text("Find playlists...", style = MaterialTheme.typography.bodyLarge, color = TextDim)
                            }
                            innerTextField()
                        }
                    )
                }
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                } else {
                    TextButton(onClick = { player.searchPlaylists(query) }, enabled = query.isNotBlank()) {
                        Text("Search online", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        val errMsg = error
        if (errMsg != null) {
            Spacer(Modifier.height(12.dp))
            Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)) {
                Text(errMsg, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        }

        if (!loading && visiblePlaylists.isEmpty()) {
            Spacer(Modifier.weight(1f))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.LibraryMusic, null, modifier = Modifier.size(48.dp), tint = TextDim)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (query.isBlank()) "No playlists yet" else "No playlists found",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextDim,
                    )
                    Text(
                        if (query.isBlank()) "Create a playlist or save tracks into one from a song menu."
                        else "Try another name or search online playlists.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray,
                    )
                    if (query.isBlank()) {
                        TextButton(onClick = { createDialogOpen = true }) {
                            Text("Create playlist")
                        }
                        TextButton(onClick = {
                            discoverySearchStarted = true
                            player.searchPlaylists("music playlists")
                        }) {
                            Text("Load online playlists")
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
        } else {
            if (savedPlaylists.isNotEmpty()) {
                Spacer(Modifier.height(18.dp))
                Text("Your Playlists", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextWhite)
            } else if (query.isBlank()) {
                Spacer(Modifier.height(18.dp))
                Text("Featured Online Playlists", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextWhite)
            } else if (query.isNotBlank()) {
                Spacer(Modifier.height(18.dp))
                Text("Playlist Results", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextWhite)
            }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 184.dp),
                modifier = Modifier.fillMaxSize().padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                items(visiblePlaylists, key = { it.id }) { item ->
                    MacPlaylistCard(
                        item = item,
                        onClick = { player.openPlaylist(item.id) },
                    )
                }
            }
        }

        if (createDialogOpen) {
            AlertDialog(
                onDismissRequest = {
                    createDialogOpen = false
                    createError = null
                },
                containerColor = Color(0xFF0D1325),
                title = { Text("Create playlist", color = TextWhite) },
                text = {
                    Column {
                        TextField(
                            value = newPlaylistName,
                            onValueChange = {
                                newPlaylistName = it.take(100)
                                createError = null
                            },
                            singleLine = true,
                            placeholder = { Text("Playlist name") },
                        )
                        createError?.let {
                            Spacer(Modifier.height(8.dp))
                            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            player.createPlaylist(newPlaylistName)
                                .onSuccess { id ->
                                    createDialogOpen = false
                                    createError = null
                                    newPlaylistName = ""
                                    player.openPlaylist(id)
                                }
                                .onFailure { createError = it.message ?: "Could not create playlist." }
                        },
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            createDialogOpen = false
                            createError = null
                        },
                    ) {
                        Text("Cancel")
                    }
                },
            )
        }
    }
}

@Composable
private fun MacPlaylistCard(item: PlaylistItem, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Shapes.medium)
            .background(if (isHovered) Color(0xFF121936) else Color(0xFF0B1022))
            .border(
                1.dp,
                if (isHovered) BorderLow.copy(alpha = 0.9f) else BorderLow.copy(alpha = 0.58f),
                Shapes.medium,
            )
            .hoverable(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .pressBounce(interactionSource),
        shape = Shapes.medium,
        color = Color.Transparent
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(BgElevated)
                    .border(1.dp, BorderLow.copy(alpha = 0.52f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                item.thumbnail?.let {
                    AsyncImage(model = it.toHighResThumbnail(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } ?: Icon(Icons.Default.LibraryMusic, null, tint = TextDim, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(
                item.title,
                style = MaterialTheme.typography.titleSmall,
                color = TextWhite,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            item.author?.let {
                Text(
                    it.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            item.songCountText?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextDim,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private fun SavedQueuePlaylist.toPlaylistItem(): PlaylistItem =
    PlaylistItem(
        id = id,
        title = name,
        author = Artist("OmniTune", null),
        songCountText = "${songs.size} ${if (songs.size == 1) "song" else "songs"}",
        thumbnail = coverPath ?: songs.firstOrNull()?.thumbnail,
        playEndpoint = null,
        shuffleEndpoint = null,
        radioEndpoint = null,
        isEditable = true,
    )
