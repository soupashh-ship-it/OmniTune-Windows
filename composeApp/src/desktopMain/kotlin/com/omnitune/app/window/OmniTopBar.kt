package com.omnitune.app.window

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
            .height(60.dp)
            .background(BgDark),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.width(16.dp))
        NavArrow(Icons.AutoMirrored.Filled.ArrowBack, canGoBack, onBack)
        Spacer(Modifier.width(6.dp))
        NavArrow(Icons.AutoMirrored.Filled.ArrowForward, canGoForward, onForward)
        Spacer(Modifier.width(16.dp))

        Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
            OmniSearchField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(0.62f),
                focusRequester = focusRequester,
                onEnter = {
                    onNavigateToSearch()
                    onSearch(query)
                },
                onEscape = { onQueryChange("") },
            )
        }

        // Profile / notifications area
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Surface2)
                .border(1.dp, BorderLow, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("O", color = IrisSoft, style = MaterialTheme.typography.titleSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
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
            .size(34.dp)
            .clip(CircleShape)
            .background(if (isHovered && enabled) Surface1 else Color.Transparent)
            .clickable(interactionSource = interactionSource, indication = null, enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = if (enabled) TextPrimary else TextMuted.copy(alpha = 0.4f), modifier = Modifier.size(20.dp))
    }
}
