package com.omnitune.app.window
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.filled.LibraryMusic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.omnitune.innertube.models.PlaylistItem
import com.omnitune.innertube.toHighResThumbnail
import com.omnitune.innertube.models.YTItem

@Composable
fun PlaylistsView(
    player: PlayerViewModel,
    onEditableTextFocusChanged: (Boolean) -> Unit = {},
) {
    val results by player.playlistResults.collectAsState()
    val loading by player.playlistLoading.collectAsState()
    val error by player.playlistError.collectAsState()
    var query by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        Text("Playlists", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp),
            color = BgCard,
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
                        Text("Search", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
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

        if (!loading && results.isEmpty() && query.isNotEmpty()) {
            Spacer(Modifier.weight(1f))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Search, null, modifier = Modifier.size(48.dp), tint = TextDim)
                    Spacer(Modifier.height(8.dp))
                    Text("No playlists found", style = MaterialTheme.typography.headlineSmall, color = TextDim)
                }
            }
            Spacer(Modifier.weight(1f))
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                modifier = Modifier.fillMaxSize().padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                items(results) { item ->
                    when (item) {
                        is PlaylistItem -> MacPlaylistCard(
                            item = item,
                            onClick = { player.openPlaylist(item.id) },
                        )
                        else -> Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = Shapes.small,
                            color = BgCard
                        ) {
                            Text(item.title, modifier = Modifier.padding(14.dp), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
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
            .background(if (isHovered) BgCardHover else BgCard)
            .hoverable(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .pressBounce(interactionSource),
        shape = Shapes.medium,
        color = Color.Transparent
    ) {
        Column(modifier = Modifier.padding(Spacing.compact)) {
            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(Shapes.small).background(BgElevated),
                contentAlignment = Alignment.Center
            ) {
                item.thumbnail?.let {
                    AsyncImage(model = it.toHighResThumbnail(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } ?: Icon(Icons.Default.LibraryMusic, null, tint = TextDim, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.height(Spacing.small))
            Text(item.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            item.author?.let { Text(it.name, style = MaterialTheme.typography.bodyMedium, color = TextGray, maxLines = 1, overflow = TextOverflow.Ellipsis) }
            item.songCountText?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = TextDim) }
        }
    }
}
