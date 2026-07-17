package com.omnitune.app.window

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omnitune.app.window.OmniReferenceColors

// ── Primary nav item (icon + label, gradient pill when active) ────────────────
@Composable
internal fun NavItem(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val contentColor = when {
        isActive -> Color.White
        isHovered -> Color(0xFFF4F0E8)
        else -> Color(0xFFE8E3DA).copy(alpha = 0.88f)
    }

    val innerRow = @Composable {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(41.dp)
                .clip(RoundedCornerShape(6.dp))
                .then(if (isHovered && !isActive && enabled) Modifier.background(Color(0xFF0A1128).copy(alpha = 0.72f)) else Modifier)
                .hoverable(interactionSource)
                .clickable(interactionSource = interactionSource, indication = null, enabled = enabled, onClick = onClick)
                .padding(start = 17.dp, end = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(17.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                color = contentColor,
                fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
            )
        }
    }

    Box(modifier = modifier.padding(horizontal = 15.dp).height(41.dp)) {
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
internal fun LibraryHeader(
    expanded: Boolean,
    isActive: Boolean,
    onOpen: () -> Unit,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val contentColor = when {
        isActive -> Color.White
        isHovered -> Color(0xFFF4F0E8)
        else -> Color(0xFFE8E3DA).copy(alpha = 0.88f)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(41.dp)
            .padding(horizontal = 15.dp)
            .clip(RoundedCornerShape(6.dp))
            .then(if (isActive) Modifier.background(OmniReferenceColors.SurfaceSelectedStrong.copy(alpha = 0.58f)) else if (isHovered) Modifier.background(Color(0xFF0A1128).copy(alpha = 0.72f)) else Modifier)
            .hoverable(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onOpen)
            .padding(start = 17.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.LibraryMusic, contentDescription = "Library", tint = contentColor, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(17.dp))
        Text("Library", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp), color = contentColor, fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal, modifier = Modifier.weight(1f))
        Icon(
            if (expanded) Icons.Default.ExpandLess else Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFFE8E3DA),
            modifier = Modifier
                .size(18.dp)
                .clickable(onClick = onToggle)
        )
    }
}

// ── Library sub-item (indented, text only, no icon) ───────────────────────────

@Composable
internal fun LibrarySubItem(
    label: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
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
        modifier = modifier
            .fillMaxWidth()
            .height(37.dp)
            .clip(RoundedCornerShape(6.dp))
            .then(if (isActive) Modifier.background(OmniReferenceColors.SurfaceSelected.copy(alpha = 0.62f)) else if (isHovered) Modifier.background(Color(0xFF0A1128).copy(alpha = 0.72f)) else Modifier)
            .hoverable(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 30.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor,
            fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
            fontSize = 15.sp,
        )
    }
}

@Composable
internal fun OmniReferenceSelectedNavigation(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(6.dp)

    Box(
        modifier = modifier
            .background(
                brush = Brush.horizontalGradient(
                colorStops = arrayOf(
                    0.00f to OmniReferenceColors.NavSelectedStart,
                    0.24f to OmniReferenceColors.NavSelectedLeftMiddle,
                    0.52f to OmniReferenceColors.NavSelectedCenter,
                    0.76f to OmniReferenceColors.NavSelectedRightMiddle,
                    1.00f to OmniReferenceColors.NavSelectedEnd
                )
                ),
                shape = shape
            )
            .border(width = 1.dp, color = Color.White.copy(alpha = 0.02f), shape = shape)
    ) {
        content()
    }
}
