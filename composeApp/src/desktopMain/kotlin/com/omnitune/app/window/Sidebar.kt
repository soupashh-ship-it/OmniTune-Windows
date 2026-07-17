package com.omnitune.app.window

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.GridView
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.omnitune.app.platform.SavedQueuePlaylist
import com.omnitune.app.player.NavScreen
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.toHighResThumbnail


@Composable
fun OmniSidebar(
    activeScreen: NavScreen,
    hasCurrentSong: Boolean,
    currentSong: SongItem?,
    likedCount: Int,
    savedPlaylists: List<SavedQueuePlaylist>,
    currentPlaylistId: String?,
    onNavigate: (NavScreen) -> Unit,
    onOpenPlaylist: (String) -> Unit,
    onCreatePlaylist: (String) -> Result<String>,
    width: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    val motionPolicy = LocalOmniMotionPolicy.current
    val librarySubScreens = setOf(NavScreen.Playlists, NavScreen.Album, NavScreen.Artist, NavScreen.Songs, NavScreen.Downloads, NavScreen.NowPlaying, NavScreen.LikedSongs)
    var libraryExpanded by remember { mutableStateOf(true) }
    var createDialogOpen by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var createError by remember { mutableStateOf<String?>(null) }

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
                        0.00f to Color(0xFF050B18),
                        0.20f to Color(0xFF050C1B),
                        0.45f to Color(0xFF050D1E),
                        0.72f to Color(0xFF050D1D),
                        1.00f to Color(0xFF050D1D)
                    )
                )
            )

            val violetCenter = Offset(size.width * 0.10f, size.height * 0.34f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF2C1552).copy(alpha = 0.12f),
                        Color(0xFF241344).copy(alpha = 0.045f),
                        Color.Transparent
                    ),
                    center = violetCenter,
                    radius = size.width * 0.68f
                ),
                center = violetCenter,
                radius = size.width * 0.68f
            )

            val blueCenter = Offset(size.width * 0.72f, size.height * 0.17f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF2F36B0).copy(alpha = 0.26f),
                        Color(0xFF182A72).copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = blueCenter,
                    radius = size.width * 0.62f
                ),
                center = blueCenter,
                radius = size.width * 0.62f
            )
        }
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 18.dp, horizontal = 0.dp)
            ) {
            // ── Brand ──────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.padding(start = 23.dp, end = 18.dp, top = 2.dp, bottom = 17.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = omniTuneIconPainter(),
                    contentDescription = "OmniTune",
                    modifier = Modifier.size(30.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
                Spacer(Modifier.width(12.dp))
                Text("OmniTune", style = MaterialTheme.typography.titleLarge, color = Color(0xFFF7F4EE), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }

            // ── Primary Nav: Home, Browse, Radio ──────────────────────────────
            NavItem(
                icon = Icons.Default.Home,
                label = "Home",
                isActive = activeScreen == NavScreen.Home,
                onClick = { onNavigate(NavScreen.Home) },
                modifier = Modifier.testTag("omni.sidebar.home"),
            )
            Spacer(Modifier.height(3.dp))
            NavItem(
                icon = Icons.Default.GridView,
                label = "Browse",
                isActive = activeScreen == NavScreen.Browse,
                onClick = { onNavigate(NavScreen.Browse) },
                modifier = Modifier.testTag("omni.sidebar.browse"),
            )
            Spacer(Modifier.height(3.dp))
            NavItem(
                icon = Icons.Default.Radio,
                label = "Radio",
                isActive = activeScreen == NavScreen.Radio,
                onClick = { onNavigate(NavScreen.Radio) },
                modifier = Modifier.testTag("omni.sidebar.radio"),
            )
            Spacer(Modifier.height(3.dp))

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
                },
                modifier = Modifier.testTag("omni.sidebar.library"),
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
                        .padding(top = 7.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(41.dp)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(
                                    Color(0xFF415072).copy(alpha = 0.42f)
                                )
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        LibrarySubItem(label = "Playlists", isActive = activeScreen == NavScreen.Playlists, modifier = Modifier.testTag("omni.sidebar.library.playlists")) { onNavigate(NavScreen.Playlists) }
                        LibrarySubItem(label = "Albums", isActive = activeScreen == NavScreen.Album, modifier = Modifier.testTag("omni.sidebar.library.albums")) { onNavigate(NavScreen.Album) }
                        LibrarySubItem(label = "Artists", isActive = activeScreen == NavScreen.Artist, modifier = Modifier.testTag("omni.sidebar.library.artists")) { onNavigate(NavScreen.Artist) }
                        LibrarySubItem(label = "Songs", isActive = activeScreen == NavScreen.Songs, modifier = Modifier.testTag("omni.sidebar.library.songs")) { onNavigate(NavScreen.Songs) }
                        LibrarySubItem(label = "Downloads", isActive = activeScreen == NavScreen.Downloads, modifier = Modifier.testTag("omni.sidebar.library.downloads")) { onNavigate(NavScreen.Downloads) }
                    }
                }
            }


            Spacer(Modifier.height(15.dp))

            HorizontalDivider(
                modifier = Modifier.padding(start = 25.dp, end = 29.dp),
                color = Color(0xFF56617E).copy(alpha = 0.28f),
                thickness = 1.dp,
            )

            Spacer(Modifier.height(12.dp))

            // ── Your Playlists ────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 25.dp, end = 29.dp, top = 0.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "Your Playlists",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFB7B9C9),
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    letterSpacing = 0.sp,
                )
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .clickable {
                            createError = null
                            newPlaylistName = ""
                            createDialogOpen = true
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Playlist", tint = Color(0xFFB7B9C9), modifier = Modifier.size(18.dp))
                }
            }

            savedPlaylists.take(4).forEachIndexed { index, playlist ->
                PlaylistItem(
                    gradientColors = playlistGradient(index),
                    label = playlist.name,
                    thumbnail = playlist.coverPath ?: playlist.songs.firstOrNull()?.thumbnail,
                    isActive = activeScreen == NavScreen.PlaylistDetail && currentPlaylistId == playlist.id,
                    onClick = { onOpenPlaylist(playlist.id) }
                )
            }
            PlaylistItem(
                gradientColors = listOf(Color(0xFF7C5CFC), Color(0xFF5B3EE8)),
                label = "Liked Songs",
                icon = Icons.Default.Favorite,
                isActive = activeScreen == NavScreen.LikedSongs,
                likedSongs = true,
                onClick = { onNavigate(NavScreen.LikedSongs) }
            )

            }

            Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF050D1D))) {
                HorizontalDivider(color = Color(0xFF8892C7).copy(alpha = 0.08f), thickness = 1.dp)
                Spacer(Modifier.height(8.dp))
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    NavItem(
                        icon = Icons.Default.Settings,
                        label = "Settings",
                        isActive = activeScreen == NavScreen.Settings,
                        onClick = { onNavigate(NavScreen.Settings) },
                        modifier = Modifier.testTag("omni.sidebar.settings"),
                    )
                }
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
            title = { Text("Create playlist", color = TextPrimary) },
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
                        Text(it, color = Color(0xFFFF7684), fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCreatePlaylist(newPlaylistName)
                            .onSuccess {
                                createDialogOpen = false
                                createError = null
                                onOpenPlaylist(it)
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
