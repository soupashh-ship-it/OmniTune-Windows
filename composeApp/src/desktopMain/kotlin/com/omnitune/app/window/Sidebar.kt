package com.omnitune.app.window

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val librarySubScreens = setOf(NavScreen.Playlists, NavScreen.Album, NavScreen.Artist, NavScreen.Search, NavScreen.Downloads)
    var libraryExpanded by remember { mutableStateOf(activeScreen in librarySubScreens || activeScreen == NavScreen.Library) }

    // Auto-expand when navigating into library sub-screens
    LaunchedEffect(activeScreen) {
        if (activeScreen in librarySubScreens) libraryExpanded = true
    }

    val sidebarBrush = androidx.compose.ui.graphics.Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFF0C0A1A),
            0.35f to Color(0xFF090A18),
            0.72f to Color(0xFF070A16),
            1.0f to Color(0xFF060914),
        )
    )

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(OmniLayout.sidebarWidth)
            .background(sidebarBrush)
            .drawBehind {
                drawLine(
                    color = Color.White.copy(alpha = 0.055f),
                    start = Offset(size.width - 1f, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width - 1f, size.height),
                    strokeWidth = 1f,
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp, horizontal = 12.dp)
        ) {
            // ── Brand ──────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(OmniGradients.primaryAction),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MusicNote, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text("OmniTune", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            Spacer(Modifier.height(16.dp))

            // ── Primary Nav: Home, Browse, Radio ──────────────────────────────
            NavItem(
                icon = Icons.Default.Home,
                label = "Home",
                isActive = activeScreen == NavScreen.Home,
                onClick = { onNavigate(NavScreen.Home) }
            )
            Spacer(Modifier.height(2.dp))
            NavItem(
                icon = Icons.Default.Search,
                label = "Browse",
                isActive = activeScreen == NavScreen.Browse,
                onClick = { onNavigate(NavScreen.Browse) }
            )
            Spacer(Modifier.height(2.dp))
            NavItem(
                icon = Icons.Default.Radio,
                label = "Radio",
                isActive = activeScreen == NavScreen.Radio,
                onClick = { onNavigate(NavScreen.Radio) }
            )
            Spacer(Modifier.height(2.dp))

            // ── Library (collapsible) ─────────────────────────────────────────
            LibraryHeader(
                expanded = libraryExpanded,
                isActive = activeScreen == NavScreen.Library && !libraryExpanded,
                onClick = {
                    libraryExpanded = !libraryExpanded
                    if (!libraryExpanded) onNavigate(NavScreen.Library)
                }
            )

            AnimatedVisibility(
                visible = libraryExpanded,
                enter = expandVertically(animationSpec = tween(200)),
                exit = shrinkVertically(animationSpec = tween(200)),
            ) {
                Column(modifier = Modifier.padding(top = 2.dp)) {
                    LibrarySubItem(label = "Playlists", isActive = activeScreen == NavScreen.Playlists) { onNavigate(NavScreen.Playlists) }
                    LibrarySubItem(label = "Albums", isActive = activeScreen == NavScreen.Album) { onNavigate(NavScreen.Album) }
                    LibrarySubItem(label = "Artists", isActive = activeScreen == NavScreen.Artist) { onNavigate(NavScreen.Artist) }
                    LibrarySubItem(label = "Songs", isActive = activeScreen == NavScreen.Search) { onNavigate(NavScreen.Search) }
                    LibrarySubItem(label = "Downloads", isActive = activeScreen == NavScreen.Downloads) { onNavigate(NavScreen.Downloads) }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Your Playlists ────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "Your Playlists",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                )
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .clickable { },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Playlist", tint = TextMuted, modifier = Modifier.size(14.dp))
                }
            }

            Spacer(Modifier.height(4.dp))

            // Playlist entries — using gradient boxes as placeholder thumbnails
            PlaylistItem(
                gradientColors = listOf(Color(0xFFFF6B35), Color(0xFFFF8C42)),
                label = "Chill Mornings",
                isActive = false,
                onClick = {}
            )
            PlaylistItem(
                gradientColors = listOf(Color(0xFF4A1DE0), Color(0xFF7B5EA7)),
                label = "Night Drive",
                isActive = false,
                onClick = {}
            )
            PlaylistItem(
                gradientColors = listOf(Color(0xFF2D6A9F), Color(0xFF1E3A5F)),
                label = "Focus Flow",
                isActive = false,
                onClick = {}
            )
            PlaylistItem(
                gradientColors = listOf(Color(0xFFE67E22), Color(0xFFD4580A)),
                label = "Afrobeats Hits",
                isActive = false,
                onClick = {}
            )
            PlaylistItem(
                gradientColors = listOf(Color(0xFF7C5CFC), Color(0xFF5B3EE8)),
                label = "Liked Songs",
                icon = Icons.Default.Favorite,
                isActive = false,
                onClick = {}
            )

            Spacer(Modifier.weight(1f))

            // ── Settings at bottom ────────────────────────────────────────────
            HorizontalDivider(color = SurfaceHairline, thickness = 1.dp)
            Spacer(Modifier.height(8.dp))
            NavItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                isActive = activeScreen == NavScreen.Settings,
                onClick = { onNavigate(NavScreen.Settings) }
            )
        }
    }
}

// ── Primary nav item (icon + label, gradient pill when active) ────────────────
@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val contentColor = when {
        isActive -> Color.White
        isHovered -> TextPrimary
        else -> TextSecondary
    }

    val activeBrush = androidx.compose.ui.graphics.Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF31226D),
            Color(0xFF281C64),
            Color(0xFF211952),
        )
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = 18.dp)
            .clip(RoundedCornerShape(8.dp))
            .then(
                when {
                    isActive -> Modifier.background(activeBrush)
                    isHovered && enabled -> Modifier.background(Surface3)
                    else -> Modifier
                }
            )
            .hoverable(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isActive) {
            Box(modifier = Modifier.width(3.dp).height(24.dp).clip(RoundedCornerShape(1.5.dp)).background(Color(0xFF8B7CFF)))
            Spacer(Modifier.width(12.dp))
        }
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}

// ── Library expandable header ──────────────────────────────────────────────────
@Composable
private fun LibraryHeader(
    expanded: Boolean,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val chevronAngle by animateFloatAsState(if (expanded) 90f else 0f, label = "chevron")

    val contentColor = when {
        isActive -> Color.White
        isHovered -> TextPrimary
        else -> TextSecondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .then(if (isActive) Modifier.background(OmniGradients.primaryAction) else if (isHovered) Modifier.background(Surface3) else Modifier)
            .hoverable(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.LibraryMusic, contentDescription = "Library", tint = contentColor, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Text("Library", style = MaterialTheme.typography.bodyMedium, color = contentColor, fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium, modifier = Modifier.weight(1f))
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = contentColor.copy(alpha = 0.7f),
            modifier = Modifier.size(16.dp).rotate(chevronAngle)
        )
    }
}

// ── Library sub-item (indented, text only, no icon) ───────────────────────────
@Composable
private fun LibrarySubItem(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val contentColor = when {
        isActive -> Color.White
        isHovered -> TextPrimary
        else -> TextSecondary.copy(alpha = 0.8f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .clip(RoundedCornerShape(6.dp))
            .then(if (isActive) Modifier.background(Surface2) else if (isHovered) Modifier.background(Surface3) else Modifier)
            .hoverable(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(start = 42.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isActive) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(IrisSoft)
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 13.sp,
        )
    }
}


// ── Playlist item (thumbnail + label) ─────────────────────────────────────────
@Composable
private fun PlaylistItem(
    gradientColors: List<Color>,
    label: String,
    isActive: Boolean,
    icon: ImageVector? = null,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val bgHover = Color.White.copy(alpha = 0.04f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive || isHovered) bgHover else Color.Transparent)
            .hoverable(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Thumbnail placeholder (gradient box)
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(androidx.compose.ui.graphics.Brush.linearGradient(gradientColors)),
            contentAlignment = Alignment.Center,
        ) {
            if (icon != null) {
                Icon(icon, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isActive) Color(0xFFF4F3FA) else Color(0xFFA9AEC2),
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        )
    }
}

