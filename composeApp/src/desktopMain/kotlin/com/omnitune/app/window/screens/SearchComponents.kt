package com.omnitune.app.window.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.app.platform.PlaybackState
import com.omnitune.app.window.BorderLow
import com.omnitune.app.window.IrisSoft
import com.omnitune.app.window.LocalHomeReferenceMetrics
import com.omnitune.app.window.LocalOmniMotionPolicy
import com.omnitune.app.window.OmniGradients
import com.omnitune.app.window.OmniReferenceColors
import com.omnitune.app.window.Shapes
import com.omnitune.app.window.Surface1
import com.omnitune.app.window.Surface2
import com.omnitune.app.window.TextMuted
import com.omnitune.app.window.TextPrimary
import com.omnitune.app.window.TextSecondary
import com.omnitune.app.window.components.OmniSearchField
import com.omnitune.innertube.models.AlbumItem
import com.omnitune.innertube.models.ArtistItem
import com.omnitune.innertube.models.PlaylistItem
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.models.YTItem
import com.omnitune.innertube.toHighResThumbnail



@Composable
internal fun SongResultRow(
    song: SongItem,
    isActive: Boolean,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onAddToQueue: () -> Unit,
    onPlayNext: () -> Unit,
    onLike: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(metrics.px(5f)))
            .background(if (isActive) OmniReferenceColors.SurfaceSelected.copy(alpha = 0.65f) else Color.Transparent)
            .clickable(onClick = onPlay)
            .padding(horizontal = metrics.px(4f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = song.thumbnail.toHighResThumbnail(),
            contentDescription = song.title,
            modifier = Modifier
                .size(metrics.px(23f))
                .clip(RoundedCornerShape(metrics.px(4f))),
            contentScale = ContentScale.Crop,
        )
        Spacer(Modifier.width(metrics.px(7f)))
        Column(Modifier.weight(1f)) {
            Text(song.title, color = if (isActive) IrisSoft else TextPrimary, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artists.joinToString(", ") { it.name }, color = TextSecondary, fontSize = 7.8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(song.searchDurationLabel(), color = TextSecondary, fontSize = 8.5.sp, modifier = Modifier.width(metrics.px(28f)))
        Icon(Icons.Default.Add, contentDescription = "Add to queue", tint = TextSecondary, modifier = Modifier.size(metrics.px(13f)).clickable(onClick = onAddToQueue))
        Spacer(Modifier.width(metrics.px(8f)))
        Icon(Icons.Default.Favorite, contentDescription = "Like or unlike song", tint = TextSecondary, modifier = Modifier.size(metrics.px(13f)).clickable(onClick = onLike))
        if (isPlaying) {
            Spacer(Modifier.width(metrics.px(4f)))
            Icon(Icons.Default.GraphicEq, null, tint = IrisSoft, modifier = Modifier.size(metrics.px(10f)))
        }
    }
}


@Composable
internal fun ArtistRow(artist: SearchArtistRow, onClick: () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(metrics.px(18f))
            .clip(RoundedCornerShape(metrics.px(6f)))
            .background(OmniReferenceColors.SurfaceDeepRaised.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(horizontal = metrics.px(6f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = artist.artwork?.toHighResThumbnail(),
            contentDescription = artist.name,
            modifier = Modifier
                .size(metrics.px(16f))
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
        Spacer(Modifier.width(metrics.px(7f)))
        Text(artist.name, color = TextPrimary, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = TextSecondary, modifier = Modifier.size(metrics.px(9f)))
    }
}


@Composable
internal fun ExpandedMediaGrid(items: List<YTItem>, onPlayItem: (YTItem) -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    items.chunked(6).forEach { rowItems ->
        Row(horizontalArrangement = Arrangement.spacedBy(metrics.px(8f))) {
            rowItems.forEach { item ->
                DiscoveryCard(
                    item = item,
                    onClick = { onPlayItem(item) },
                )
            }
        }
    }
}


@Composable
internal fun DiscoveryCard(item: YTItem, onClick: () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Box(
        modifier = Modifier
            .width(metrics.px(94f))
            .height(metrics.px(88f))
            .clip(RoundedCornerShape(metrics.px(7f)))
            .background(Surface1)
            .border(1.dp, BorderLow.copy(alpha = 0.72f), RoundedCornerShape(metrics.px(7f)))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = item.thumbnail?.toHighResThumbnail(),
            contentDescription = item.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(0f to Color.Transparent, 1f to Color(0xE6070A1A)))
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(metrics.px(8f))
        ) {
            Text(item.title, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(searchItemSubtitle(item).ifBlank { searchItemKind(item) }, color = TextSecondary, fontSize = 7.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}


@Composable
internal fun CompactMediaCard(
    item: YTItem,
    width: Dp,
    artwork: Dp,
    onClick: () -> Unit,
) {
    val metrics = LocalHomeReferenceMetrics.current
    Column(
        modifier = Modifier
            .width(width)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(artwork)
                .clip(RoundedCornerShape(metrics.px(5f)))
        ) {
            AsyncImage(
                model = item.thumbnail?.toHighResThumbnail(),
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            if (item is PlaylistItem || item is AlbumItem || item is SongItem) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(metrics.px(3f))
                        .size(metrics.px(13f))
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(metrics.px(8f)))
                }
            }
        }
        Spacer(Modifier.height(metrics.px(4f)))
        Text(item.title, color = TextPrimary, fontSize = 7.7.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(searchItemSubtitle(item).ifBlank { searchItemMeta(item) }, color = TextSecondary, fontSize = 6.8.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}


@Composable
internal fun SearchChip(
    text: String,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    onClick: () -> Unit = {},
) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(
        modifier = Modifier
            .height(metrics.px(20f))
            .clip(RoundedCornerShape(metrics.px(99f)))
            .background(OmniReferenceColors.SurfaceSelected.copy(alpha = if (enabled) 0.7f else 0.35f))
            .border(1.dp, BorderLow.copy(alpha = 0.9f), RoundedCornerShape(metrics.px(99f)))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = metrics.px(9f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, null, tint = IrisSoft, modifier = Modifier.size(metrics.px(10f)))
            Spacer(Modifier.width(metrics.px(4f)))
        }
        Text(text, color = if (enabled) TextPrimary else TextMuted, fontSize = 8.5.sp, maxLines = 1)
    }
}


@Composable
internal fun GenreChip(title: String, icon: ImageVector, onClick: () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Row(
        modifier = Modifier
            .widthIn(min = metrics.px(72f))
            .height(metrics.px(29f))
            .clip(RoundedCornerShape(metrics.px(5f)))
            .background(OmniReferenceColors.SurfaceSelected.copy(alpha = 0.65f))
            .border(1.dp, BorderLow.copy(alpha = 0.8f), RoundedCornerShape(metrics.px(5f)))
            .clickable(onClick = onClick)
            .padding(horizontal = metrics.px(9f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = IrisSoft, modifier = Modifier.size(metrics.px(13f)))
        Spacer(Modifier.width(metrics.px(7f)))
        Text(title, color = TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}


@Composable
internal fun MoreGenresActionChip(
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalHomeReferenceMetrics.current
    val motionPolicy = LocalOmniMotionPolicy.current
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = motionPolicy.standardDurationMs, easing = FastOutSlowInEasing),
        label = "moreGenresChevron",
    )

    Row(
        modifier = modifier
            .widthIn(min = metrics.px(72f))
            .height(metrics.px(29f))
            .clip(RoundedCornerShape(metrics.px(5f)))
            .background(OmniReferenceColors.SurfaceSelected.copy(alpha = 0.65f))
            .border(1.dp, BorderLow.copy(alpha = 0.8f), RoundedCornerShape(metrics.px(5f)))
            .clickable(onClick = onClick)
            .padding(horizontal = metrics.px(9f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowForwardIos,
            null,
            tint = IrisSoft,
            modifier = Modifier
                .size(metrics.px(11f))
                .rotate(chevronRotation),
        )
        Spacer(Modifier.width(metrics.px(7f)))
        Text(
            if (expanded) "Less" else "More",
            color = TextPrimary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
        )
    }
}


@Composable
internal fun ReferencePanel(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(LocalHomeReferenceMetrics.current.px(8f)),
    brush: Brush? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (brush != null) Modifier.background(brush)
                else Modifier.background(OmniReferenceColors.SurfaceBase.copy(alpha = 0.92f))
            )
            .border(1.dp, BorderLow.copy(alpha = 0.7f), shape),
        content = content,
    )
}


@Composable
internal fun PanelHeader(title: String, onSeeAll: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = TextPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        SeeAllAction(onSeeAll)
    }
}


@Composable
internal fun SeeAllAction(onSeeAll: () -> Unit) {
    val metrics = LocalHomeReferenceMetrics.current
    Text(
        "See all",
        color = IrisSoft,
        fontSize = 8.5.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .clip(RoundedCornerShape(metrics.px(4f)))
            .clickable(onClick = onSeeAll)
            .padding(horizontal = metrics.px(4f), vertical = metrics.px(2f)),
    )
}


@Composable
internal fun SectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        title,
        color = TextPrimary,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        modifier = modifier,
    )
}


@Composable
internal fun TrendSparkline(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val points = listOf(0.05f to 0.75f, 0.22f to 0.72f, 0.34f to 0.38f, 0.48f to 0.58f, 0.62f to 0.32f, 0.78f to 0.68f, 0.95f to 0.22f)
        for (i in 0 until points.lastIndex) {
            val a = points[i]
            val b = points[i + 1]
            drawLine(
                color = IrisSoft,
                start = androidx.compose.ui.geometry.Offset(size.width * a.first, size.height * a.second),
                end = androidx.compose.ui.geometry.Offset(size.width * b.first, size.height * b.second),
                strokeWidth = 1.4f,
            )
        }
    }
}

