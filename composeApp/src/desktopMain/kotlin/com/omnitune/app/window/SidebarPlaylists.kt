package com.omnitune.app.window

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.omnitune.innertube.toHighResThumbnail

// ── Playlist item (thumbnail + label) ─────────────────────────────────────────
@Composable
internal fun PlaylistItem(
    gradientColors: List<Color>,
    label: String,
    isActive: Boolean,
    icon: ImageVector? = null,
    thumbnail: String? = null,
    likedSongs: Boolean = false,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val bgHover = Color.White.copy(alpha = 0.045f)
    val rowShape = RoundedCornerShape(7.dp)
    val activeBackground = androidx.compose.ui.graphics.Brush.horizontalGradient(
        listOf(Color(0xFF321770), Color(0xFF4B22C8), Color(0xFF321A9A))
    )

    Row(
        modifier = Modifier
            .padding(start = 15.dp, end = 20.dp, bottom = if (likedSongs) 0.dp else 8.dp)
            .fillMaxWidth()
            .height(if (likedSongs) 41.dp else 32.dp)
            .clip(rowShape)
            .then(
                when {
                    likedSongs -> Modifier.background(activeBackground)
                    isActive || isHovered -> Modifier.background(bgHover)
                    else -> Modifier
                }
            )
            .hoverable(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(start = if (likedSongs) 10.dp else 0.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Thumbnail placeholder (gradient box)
        Box(
            modifier = Modifier
                .size(if (likedSongs) 31.dp else 25.dp)
                .clip(RoundedCornerShape(if (likedSongs) 7.dp else 5.dp))
                .background(androidx.compose.ui.graphics.Brush.linearGradient(gradientColors)),
            contentAlignment = Alignment.Center,
        ) {
            if (!thumbnail.isNullOrBlank()) {
                AsyncImage(
                    model = thumbnail.toHighResThumbnail(),
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else if (icon != null) {
                Icon(icon, null, tint = Color.White.copy(alpha = 0.94f), modifier = Modifier.size(if (likedSongs) 18.dp else 16.dp))
            }
        }
        Spacer(Modifier.width(14.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
            color = if (isActive || likedSongs) Color(0xFFF4F0E8) else Color(0xFFE8E3DA).copy(alpha = 0.92f),
            fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        )
    }
}

internal fun playlistGradient(index: Int): List<Color> = when (index % 4) {
    0 -> listOf(Color(0xFFFF7A2F), Color(0xFF6A38FF))
    1 -> listOf(Color(0xFF4A1DE0), Color(0xFF111B46))
    2 -> listOf(Color(0xFF315B8F), Color(0xFF0C1833))
    else -> listOf(Color(0xFFE67E22), Color(0xFF4E1B7A))
}
