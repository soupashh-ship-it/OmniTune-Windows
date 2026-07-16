package com.omnitune.app.window

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.innertube.models.AlbumItem
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.models.YTItem
import com.omnitune.innertube.toHighResThumbnail
import com.omnitune.app.window.components.OmniShimmerBlock

@Composable
internal fun RelatedPanel(
    related: List<YTItem>,
    loading: Boolean,
    error: String?,
    player: PlayerViewModel,
    onRetry: () -> Unit,
) {
    if (loading) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(5) {
                OmniShimmerBlock(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .clip(Shapes.small),
                )
            }
        }
        return
    }
    if (error != null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.CloudOff, null, tint = TextMuted, modifier = Modifier.size(32.dp))
                Text(error, color = TextMuted, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                Box(
                    modifier = Modifier
                        .clip(Shapes.pill)
                        .background(Surface2.copy(alpha = 0.86f))
                        .border(1.dp, Iris.copy(alpha = 0.20f), Shapes.pill)
                        .clickable(onClick = onRetry)
                        .padding(horizontal = 18.dp, vertical = 9.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Retry", color = TextPrimary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        return
    }
    if (related.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Related content is not available yet for this track.", color = TextMuted, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        }
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(related) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Shapes.small)
                    .clickable {
                        when (item) {
                            is SongItem -> player.playSong(item)
                            is AlbumItem -> player.openAlbum(item.browseId)
                            is com.omnitune.innertube.models.ArtistItem -> player.openArtist(item.id)
                            is com.omnitune.innertube.models.PlaylistItem -> player.openPlaylist(item.id)
                        }
                    }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsyncImage(
                    model = when (item) {
                        is SongItem -> item.thumbnail.toHighResThumbnail()
                        is AlbumItem -> item.thumbnail.toHighResThumbnail()
                        is com.omnitune.innertube.models.ArtistItem -> item.thumbnail?.toHighResThumbnail()
                        is com.omnitune.innertube.models.PlaylistItem -> item.thumbnail?.toHighResThumbnail()
                    },
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).clip(Shapes.artworkSmall),
                    contentScale = ContentScale.Crop,
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.title, color = TextPrimary, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    val subtitle = when (item) {
                        is SongItem -> item.artists.joinToString { it.name }
                        is AlbumItem -> item.artists?.joinToString { it.name } ?: "Album"
                        is com.omnitune.innertube.models.ArtistItem -> "Artist"
                        is com.omnitune.innertube.models.PlaylistItem -> item.author?.name ?: "Playlist"
                    }
                    Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
