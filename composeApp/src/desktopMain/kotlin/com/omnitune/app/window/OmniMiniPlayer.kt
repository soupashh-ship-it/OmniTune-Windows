package com.omnitune.app.window

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Brush
import coil3.compose.AsyncImage
import com.omnitune.app.player.NavScreen
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.app.platform.PlaybackState
import com.omnitune.app.platform.PlayerPosition
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.toHighResThumbnail
import com.omnitune.app.window.components.OmniVolumeControl

@Composable
fun OmniMiniPlayer(
    player: PlayerViewModel,
    currentSong: SongItem?,
    playbackState: PlaybackState,
    position: PlayerPosition,
    volume: Int,
) {
    val interaction = remember { MutableInteractionSource() }
    Surface(color = BgInk, modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (currentSong != null) {
                AsyncImage(model = currentSong.thumbnail.toHighResThumbnail(), contentDescription = null, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).clickable { player.navigateTo(NavScreen.NowPlaying) }, contentScale = ContentScale.Crop)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f).clickable { player.navigateTo(NavScreen.NowPlaying) }) {
                    Text(currentSong.title, color = TextPrimary, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(currentSong.artists.joinToString { it.name }, color = TextSecondary, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            } else {
                Text("OmniTune", color = TextMuted, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            }
            MiniBtn(Icons.Default.SkipPrevious, enabled = currentSong != null) { player.previousTrack() }
            val playSrc = remember { MutableInteractionSource() }
            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(if (currentSong != null) OmniGradients.irisToLavender else Brush.linearGradient(listOf(TextMuted.copy(alpha = 0.25f), TextMuted.copy(alpha = 0.25f)))).clickable(interactionSource = playSrc, indication = null, enabled = currentSong != null) { player.togglePlayPause() }, contentAlignment = Alignment.Center) {
                Icon(if (playbackState == PlaybackState.PLAYING) Icons.Default.Pause else Icons.Default.PlayArrow, "Play/Pause", tint = Color(0xFF05060A), modifier = Modifier.size(20.dp))
            }
            MiniBtn(Icons.Default.SkipNext, enabled = currentSong != null) { player.nextTrack() }
            Spacer(Modifier.width(8.dp))
            OmniVolumeControl(volume = volume, onVolumeChange = { player.setVolume(it) })
        }
    }
}

@Composable
private fun MiniBtn(icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean, onClick: () -> Unit) {
    val src = remember { MutableInteractionSource() }
    Box(modifier = Modifier.size(34.dp).clip(CircleShape).clickable(interactionSource = src, indication = null, enabled = enabled, onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = if (enabled) TextPrimary else TextMuted.copy(alpha = 0.35f), modifier = Modifier.size(20.dp))
    }
}
