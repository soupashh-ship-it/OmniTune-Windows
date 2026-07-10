package com.omnitune.app.window

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.omnitune.app.player.NavScreen
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.toHighResThumbnail

private data class NavEntry(
    val screen: NavScreen,
    val label: String,
    val icon: ImageVector,
    val enabled: Boolean = true,
)

@Composable
fun OmniSidebar(
    activeScreen: NavScreen,
    hasCurrentSong: Boolean,
    currentSong: SongItem?,
    likedCount: Int,
    onNavigate: (NavScreen) -> Unit,
) {
    val primary = listOf(
        NavEntry(NavScreen.Home, "Home", Icons.Default.Home),
        NavEntry(NavScreen.Browse, "Browse", Icons.Default.ViewModule),
        NavEntry(NavScreen.Radio, "Radio", Icons.Default.Radio),
        NavEntry(NavScreen.Library, "Library", Icons.Default.LibraryMusic),
    )
    val librarySub = listOf(
        NavEntry(NavScreen.Playlists, "Playlists", Icons.Default.ViewModule),
        NavEntry(NavScreen.Album, "Albums", Icons.Default.Album),
        NavEntry(NavScreen.Artist, "Artists", Icons.Default.People),
        NavEntry(NavScreen.Search, "Songs", Icons.Default.MusicNote),
        NavEntry(NavScreen.Downloads, "Downloads", Icons.Default.Download),
    )

    Surface(
        modifier = Modifier.fillMaxHeight().width(264.dp),
        color = BgInk,
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(vertical = 16.dp, horizontal = 12.dp)) {
            // Brand
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(26.dp).clip(RoundedCornerShape(8.dp))
                        .background(OmniGradients.irisToLavender),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.MusicNote, null, tint = Color(0xFF05060A), modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text("OmniTune", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(18.dp))

            primary.forEach { entry ->
                SidebarItem(entry, entry.screen == activeScreen, enabled = entry.enabled, onClick = { onNavigate(entry.screen) })
                Spacer(Modifier.height(4.dp))
            }

            Spacer(Modifier.height(18.dp))
            SectionLabel("Library")
            Spacer(Modifier.height(6.dp))
            librarySub.forEach { entry ->
                SidebarItem(entry, entry.screen == activeScreen, compact = true, onClick = { onNavigate(entry.screen) })
                Spacer(Modifier.height(2.dp))
            }

            Spacer(Modifier.height(18.dp))
            SectionLabel("Your Collection")
            Spacer(Modifier.height(6.dp))
            SidebarItem(
                NavEntry(NavScreen.Search, "Liked Songs", Icons.Default.Favorite, enabled = true),
                false,
                compact = true,
                trailing = if (likedCount > 0) likedCount.toString() else null,
                onClick = { onNavigate(NavScreen.Search) },
            )

            Spacer(Modifier.weight(1f))

            HorizontalDivider(color = SurfaceHairline, thickness = 1.dp)
            Spacer(Modifier.height(8.dp))
            SidebarItem(
                NavEntry(NavScreen.Settings, "Settings", Icons.Default.Settings),
                NavScreen.Settings == activeScreen,
                onClick = { onNavigate(NavScreen.Settings) },
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = TextMuted,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
    )
}

@Composable
private fun SidebarItem(
    entry: NavEntry,
    isActive: Boolean,
    enabled: Boolean = true,
    compact: Boolean = false,
    trailing: String? = null,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val height = if (compact) 38.dp else 42.dp

    val bg by animateColorAsState(
        when {
            isActive -> Iris.copy(alpha = 0.16f)
            isHovered && enabled -> Surface1
            else -> Color.Transparent
        },
        tween(180),
    )
    val textColor by animateColorAsState(
        when {
            isActive -> IrisSoft
            !enabled -> TextMuted.copy(alpha = 0.4f)
            else -> TextPrimary
        },
        tween(180),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(Shapes.small)
            .background(bg)
            .hoverable(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, enabled = enabled, onClick = onClick)
            .pressBounce(interactionSource),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isActive) {
            Box(Modifier.height(20.dp).width(3.dp).clip(Shapes.pill).background(IrisSoft))
            Spacer(Modifier.width(10.dp))
        } else {
            Spacer(Modifier.width(13.dp))
        }
        Icon(entry.icon, entry.label, tint = textColor, modifier = Modifier.size(if (compact) 18.dp else 20.dp))
        Spacer(Modifier.width(if (compact) 11.dp else 12.dp))
        Text(entry.label, style = if (compact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleSmall, color = textColor, maxLines = 1)
        if (trailing != null) {
            Spacer(Modifier.weight(1f))
            Text(trailing, style = MaterialTheme.typography.labelMedium, color = TextMuted)
            Spacer(Modifier.width(12.dp))
        }
    }
}
