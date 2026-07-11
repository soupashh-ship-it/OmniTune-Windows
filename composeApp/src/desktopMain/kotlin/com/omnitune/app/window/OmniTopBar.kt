package com.omnitune.app.window

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.omnitune.app.window.components.OmniIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.omnitune.app.player.NavScreen
import com.omnitune.app.window.components.OmniSearchField

@Composable
fun OmniTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    canGoBack: Boolean,
    canGoForward: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit,
    focusRequester: FocusRequester? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(BgDeep),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.width(16.dp))
        NavArrow(Icons.Default.ChevronLeft, canGoBack, onBack)
        Spacer(Modifier.width(6.dp))
        NavArrow(Icons.Default.ChevronRight, canGoForward, onForward)
        Spacer(Modifier.width(16.dp))

        Box(Modifier.weight(1f).fillMaxHeight().padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
            OmniSearchField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(0.6f),
                focusRequester = focusRequester,
                onEnter = {
                    onNavigateToSearch()
                    onSearch(query)
                },
                onEscape = { onQueryChange("") },
            )
        }

        // Profile / notifications area
        Row(verticalAlignment = Alignment.CenterVertically) {
            OmniIconButton(
                onClick = {},
                icon = Icons.Default.Notifications,
                contentDescription = "Notifications",
                size = 32.dp,
                iconSize = 18.dp
            )
            Spacer(Modifier.width(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable {  }
                    .padding(4.dp)
            ) {
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=100&q=80",
                    contentDescription = "Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Surface2)
                        .border(1.dp, BorderLow, CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ExpandMore, contentDescription = "More", tint = TextSecondary, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.width(16.dp))
    }
}

@Composable
private fun NavArrow(icon: ImageVector, enabled: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (isHovered && enabled) Surface3 else Surface2)
            .clickable(interactionSource = interactionSource, indication = null, enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = if (enabled) TextPrimary else TextMuted.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
    }
}
