package com.omnitune.app.window

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.toHighResThumbnail

@Composable
internal fun PlayerLeftZone(
    currentSong: SongItem?,
    liked: Set<String>,
    player: PlayerViewModel,
    artworkSize: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (currentSong != null) {
            AsyncImage(
                model = currentSong.thumbnail?.toHighResThumbnail(),
                contentDescription = null,
                modifier = Modifier
                    .size(artworkSize)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { player.navigateTo(com.omnitune.app.player.NavScreen.NowPlaying) },
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.width(12.dp))
            Column(
                Modifier
                    .weight(1f)
                    .clickable { player.navigateTo(com.omnitune.app.player.NavScreen.NowPlaying) }
            ) {
                Text(
                    currentSong.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = Color(0xFFF4F3FA),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    currentSong.artists?.joinToString(", ") { it.name ?: "" } ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFA9AEC2),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(10.dp))
            val favOn = liked.contains(currentSong.id)
            TransportIcon(
                if (favOn) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = if (favOn) "Unlike" else "Like",
                tint = if (favOn) Color(0xFF7C6DFF) else Color(0xFFA9AEC2),
                onClick = { player.toggleLike(currentSong.id) },
                size = 18.dp
            )
            Spacer(Modifier.width(4.dp))
            TransportIcon(Icons.Default.MoreHoriz, contentDescription = "More options", tint = Color(0xFFA9AEC2), onClick = { player.navigateTo(com.omnitune.app.player.NavScreen.Queue) }, size = 18.dp)
        } else {
            Box(
                modifier = Modifier
                    .size(artworkSize)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF0B1328),
                                Color(0xFF171238),
                                Color(0xFF090F21),
                            )
                        )
                    )
                    .border(1.dp, OmniReferenceColors.PlayerBorder.copy(alpha = 0.70f), RoundedCornerShape(6.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Box(
                    modifier = Modifier.width(100.dp).height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1A2038).copy(alpha = 0.74f))
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier.width(60.dp).height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF171D32).copy(alpha = 0.62f))
                )
            }
        }
    }
}
