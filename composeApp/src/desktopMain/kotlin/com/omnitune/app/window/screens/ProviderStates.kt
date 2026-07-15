package com.omnitune.app.window.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.omnitune.app.window.Shapes
import com.omnitune.app.window.Spacing
import com.omnitune.app.window.TextMuted
import com.omnitune.app.window.TextPrimary
import com.omnitune.app.window.TextSecondary
import com.omnitune.app.window.components.OmniShimmerBlock
import com.omnitune.app.window.components.OmniSurface

@Composable
internal fun ProviderHomeShimmer() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(40.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.section),
        contentPadding = PaddingValues(bottom = 98.dp),
    ) {
        item { OmniShimmerBlock(Modifier.width(280.dp).height(36.dp).clip(Shapes.small)) }
        item { OmniShimmerBlock(Modifier.fillMaxWidth().height(220.dp).clip(Shapes.large)) }
        items(3) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                items(6) { OmniShimmerBlock(Modifier.width(170.dp).height(210.dp).clip(Shapes.medium)) }
            }
        }
    }
}

@Composable
internal fun ProviderRetryState(title: String, message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.CloudOff, contentDescription = null, tint = TextMuted, modifier = Modifier.width(34.dp).height(34.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, textAlign = TextAlign.Center)
            Text(
                message.ifBlank { "The provider did not return this section." },
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 420.dp),
            )
            OmniSurface(
                modifier = Modifier
                    .clip(Shapes.pill)
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                onClick = onRetry,
            ) {
                Text("Try again", color = TextPrimary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
