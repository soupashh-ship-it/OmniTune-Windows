package com.omnitune.app.window

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
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
    width: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    val motionPolicy = LocalOmniMotionPolicy.current
    val librarySubScreens = setOf(NavScreen.Playlists, NavScreen.Album, NavScreen.Artist, NavScreen.Search, NavScreen.Downloads)
    var libraryExpanded by remember { mutableStateOf(activeScreen in librarySubScreens || activeScreen == NavScreen.Library) }

    // Auto-expand when navigating into library sub-screens
    LaunchedEffect(activeScreen) {
        if (activeScreen in librarySubScreens) libraryExpanded = true
    }


    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(width)
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.00f to Color(0xFF090B20),
                        0.20f to Color(0xFF090D23),
                        0.45f to Color(0xFF071127),
                        0.72f to Color(0xFF041028),
                        1.00f to Color(0xFF03102A)
                    )
                )
            )

            val violetCenter = Offset(size.width * 0.10f, size.height * 0.34f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF4A1E72).copy(alpha = 0.22f),
                        Color(0xFF4A1E72).copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = violetCenter,
                    radius = size.width * 0.75f
                ),
                center = violetCenter,
                radius = size.width * 0.75f
            )

            val blueCenter = Offset(size.width * 0.72f, size.height * 0.17f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1D56C9).copy(alpha = 0.18f),
                        Color(0xFF1D56C9).copy(alpha = 0.07f),
                        Color.Transparent
                    ),
                    center = blueCenter,
                    radius = size.width * 0.58f
                ),
                center = blueCenter,
                radius = size.width * 0.58f
            )
        }
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 10.dp, horizontal = 0.dp)
            ) {
            // ── Brand ──────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 10.dp, top = 13.dp, bottom = 1.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(26.dp).clip(CircleShape).background(OmniGradients.primaryAction),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MusicNote, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text("OmniTune", style = MaterialTheme.typography.titleLarge, color = Color(0xFFD7DBEE), fontWeight = FontWeight.Medium, fontSize = 18.sp)
            }

            Spacer(Modifier.height(4.dp))

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
                isActive = activeScreen == NavScreen.Library,
                onOpen = {
                    libraryExpanded = true
                    onNavigate(NavScreen.Library)
                },
                onToggle = {
                    libraryExpanded = !libraryExpanded
                }
            )


            AnimatedVisibility(
                visible = libraryExpanded,
                enter = expandVertically(
                    animationSpec = tween(
                        durationMillis = motionPolicy.standardDurationMs,
                        easing = androidx.compose.animation.core.FastOutSlowInEasing
                    )
                ) + androidx.compose.animation.fadeIn(
                    animationSpec = tween(durationMillis = motionPolicy.shortDurationMs)
                ),
                exit = shrinkVertically(
                    animationSpec = tween(
                        durationMillis = motionPolicy.standardDurationMs,
                        easing = androidx.compose.animation.core.FastOutSlowInEasing
                    )
                ) + androidx.compose.animation.fadeOut(
                    animationSpec = tween(durationMillis = motionPolicy.shortDurationMs)
                )
            ) {
                Row(
                    modifier = Modifier
                        .height(androidx.compose.foundation.layout.IntrinsicSize.Min)
                        .padding(top = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(
                                    Color(0xFF465276).copy(alpha = 0.55f)
                                )
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        LibrarySubItem(label = "Playlists", isActive = activeScreen == NavScreen.Playlists) { onNavigate(NavScreen.Playlists) }
                        LibrarySubItem(label = "Albums", isActive = activeScreen == NavScreen.Album) { onNavigate(NavScreen.Album) }
                        LibrarySubItem(label = "Artists", isActive = activeScreen == NavScreen.Artist) { onNavigate(NavScreen.Artist) }
                        LibrarySubItem(label = "Songs", isActive = activeScreen == NavScreen.Search) { onNavigate(NavScreen.Search) }
                        LibrarySubItem(label = "Downloads", isActive = activeScreen == NavScreen.Downloads) { onNavigate(NavScreen.Downloads) }
                    }
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
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp,
                )
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .clickable { onNavigate(NavScreen.Playlists) },
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
                onClick = { onNavigate(NavScreen.Playlists) }
            )
            PlaylistItem(
                gradientColors = listOf(Color(0xFF4A1DE0), Color(0xFF7B5EA7)),
                label = "Night Drive",
                isActive = false,
                onClick = { onNavigate(NavScreen.Playlists) }
            )
            PlaylistItem(
                gradientColors = listOf(Color(0xFF2D6A9F), Color(0xFF1E3A5F)),
                label = "Focus Flow",
                isActive = false,
                onClick = { onNavigate(NavScreen.Playlists) }
            )
            PlaylistItem(
                gradientColors = listOf(Color(0xFFE67E22), Color(0xFFD4580A)),
                label = "Afrobeats Hits",
                isActive = false,
                onClick = { onNavigate(NavScreen.Playlists) }
            )
            PlaylistItem(
                gradientColors = listOf(Color(0xFF7C5CFC), Color(0xFF5B3EE8)),
                label = "Liked Songs",
                icon = Icons.Default.Favorite,
                isActive = false,
                onClick = { onNavigate(NavScreen.Library) }
            )

            }

            Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF030D23))) {
                HorizontalDivider(color = Color(0xFF8892C7).copy(alpha = 0.08f), thickness = 1.dp)
                Spacer(Modifier.height(8.dp))
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    NavItem(
                        icon = Icons.Default.Settings,
                        label = "Settings",
                        isActive = activeScreen == NavScreen.Settings,
                        onClick = { onNavigate(NavScreen.Settings) }
                    )
                }
            }
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
    val motionPolicy = LocalOmniMotionPolicy.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val contentColor = when {
        isActive -> Color.White
        isHovered -> Color(0xFFD7DBEE).copy(alpha = 0.9f)
        else -> Color(0xFFD7DBEE).copy(alpha = 0.5f)
    }

    val innerRow = @Composable {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(31.dp)
                .clip(RoundedCornerShape(8.dp))
                .then(if (isHovered && !isActive && enabled) Modifier.background(Surface3) else Modifier)
                .hoverable(interactionSource)
                .clickable(interactionSource = interactionSource, indication = null, enabled = enabled, onClick = onClick)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(17.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                color = contentColor,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
            )
        }
    }

    Box(modifier = Modifier.padding(horizontal = 10.dp).height(31.dp)) {
        if (isActive) {
            OmniReferenceSelectedNavigation {
                innerRow()
            }
        } else {
            innerRow()
        }
    }
}

// ── Library expandable header ──────────────────────────────────────────────────
@Composable
private fun LibraryHeader(
    expanded: Boolean,
    isActive: Boolean,
    onOpen: () -> Unit,
    onToggle: () -> Unit,
) {
    val motionPolicy = LocalOmniMotionPolicy.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val chevronAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(
            durationMillis = motionPolicy.standardDurationMs,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "libraryChevronRotation"
    )

    val contentColor = when {
        isActive -> Color.White
        isHovered -> TextPrimary
        else -> TextSecondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(31.dp)
            .clip(RoundedCornerShape(8.dp))
            .then(if (isActive) Modifier.background(Color(0xFF252450)) else if (isHovered) Modifier.background(Surface3) else Modifier)
            .hoverable(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onOpen)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.LibraryMusic, contentDescription = "Library", tint = contentColor, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Text("Library", style = MaterialTheme.typography.bodyMedium, color = contentColor, fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium, modifier = Modifier.weight(1f))
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = contentColor.copy(alpha = 0.7f),
            modifier = Modifier
                .size(16.dp)
                .rotate(chevronAngle)
                .clickable(onClick = onToggle)
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
        isHovered -> Color.White
        else -> Color(0xFFD3D7E8).copy(alpha = 0.6f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .then(if (isActive) Modifier.background(Color.White.copy(alpha=0.05f)) else if (isHovered) Modifier.background(Surface3) else Modifier)
            .hoverable(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = 11.sp,
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
            .height(30.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isActive || isHovered) bgHover else Color.Transparent)
            .hoverable(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Thumbnail placeholder (gradient box)
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(androidx.compose.ui.graphics.Brush.linearGradient(gradientColors)),
            contentAlignment = Alignment.Center,
        ) {
            if (icon != null) {
                Icon(icon, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
            color = if (isActive) Color(0xFFF4F3FA) else Color(0xFFA9AEC2),
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        )
    }
}



@Composable
fun OmniReferenceSelectedNavigation(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .background(
                brush = Brush.horizontalGradient(
                    colorStops = arrayOf(
                        0.00f to Color(0xFF1A0F47),
                        0.22f to Color(0xFF211253),
                        0.50f to Color(0xFF201566),
                        0.76f to Color(0xFF221C7B),
                        1.00f to Color(0xFF18184C)
                    )
                ),
                shape = shape
            )
            .border(
                width = 1.dp,
                color = Color(0xFF615BCE).copy(alpha = 0.15f),
                shape = shape
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(3.dp)
                .fillMaxHeight()
                .background(
                    color = OmniReferenceColors.NavSelectedIndicator,
                    shape = RoundedCornerShape(
                        topStart = 10.dp,
                        bottomStart = 10.dp,
                        topEnd = 4.dp,
                        bottomEnd = 4.dp
                    )
                )
        )

        content()
    }
}
