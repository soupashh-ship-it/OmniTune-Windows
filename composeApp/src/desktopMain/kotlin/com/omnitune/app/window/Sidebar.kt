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
import androidx.compose.material.icons.filled.Add
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
        NavEntry(NavScreen.Downloads, "Downloads & Offline", Icons.Default.Download),
    )

    Surface(
        modifier = Modifier.fillMaxHeight().width(com.omnitune.app.window.OmniLayout.sidebarWidth),
        color = SidebarBackground,
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
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Your Playlists", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                Icon(Icons.Default.Add, contentDescription = "Add Playlist", tint = TextMuted, modifier = Modifier.size(16.dp))
            }
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

    val contentColor = if (isActive) Color.White else if (isHovered) com.omnitune.app.window.TextPrimary else com.omnitune.app.window.TextSecondary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (compact) 36.dp else 40.dp)
            .clip(Shapes.small)
            .then(
                if (isActive) Modifier.background(OmniGradients.primaryAction)
                else if (isHovered && enabled) Modifier.background(com.omnitune.app.window.Surface3)
                else Modifier
            )
            .clickable(interactionSource = interactionSource, indication = null, enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isActive) {
            // Add subtle left accent
            Box(modifier = Modifier.width(3.dp).height(16.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(1.5.dp)).background(Color.White))
            Spacer(Modifier.width(9.dp))
        }
        Icon(
            imageVector = entry.icon,
            contentDescription = entry.label,
            tint = contentColor,
            modifier = Modifier.size(if (compact) 18.dp else 20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            entry.label,
            style = if (isActive && !compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            color = contentColor,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
        )
        if (trailing != null) {
            Spacer(Modifier.weight(1f))
            Text(trailing, style = MaterialTheme.typography.labelMedium, color = if (isActive) Color.White.copy(alpha = 0.7f) else TextMuted)
        }
    }
}
