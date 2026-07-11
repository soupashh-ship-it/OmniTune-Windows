package com.omnitune.app.window.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.omnitune.app.player.PlayerViewModel
import com.omnitune.app.window.*
import com.omnitune.app.window.components.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.AsyncImage

@Composable
fun DownloadsView(player: PlayerViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 28.dp)) {
        // Header
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column {
                Text("Downloads & Offline", style = MaterialTheme.typography.displaySmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("Listen anywhere, anytime. Manage your downloads and offline content.", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = Shapes.pill, color = Surface3, border = BorderStroke(1.dp, BorderLow)) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Quality Settings", color = TextPrimary, style = MaterialTheme.typography.labelMedium)
                    }
                }
                Surface(shape = Shapes.pill, color = Iris.copy(alpha = 0.2f), border = BorderStroke(1.dp, Iris)) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Pause, null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Pause All", color = TextPrimary, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            // Main content column
            Column(modifier = Modifier.weight(1.8f).fillMaxHeight()) {
                // Filter pills
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterPill("All", true)
                    FilterPill("Completed  128", false)
                    FilterPill("In Progress  3", false)
                    FilterPill("Failed  1", false)
                }
                Spacer(Modifier.height(24.dp))
                
                // Summary cards
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SummaryCard(Modifier.weight(1f), "Downloaded", "128", "Items", Icons.Default.MusicNote, Iris.copy(alpha = 0.2f), Iris)
                    SummaryCard(Modifier.weight(1f), "Offline Mixes", "6", "Smart mixes", Icons.Default.GraphicEq, CoolBlue.copy(alpha = 0.2f), CoolBlue)
                    SummaryCard(Modifier.weight(1f), "Downloading", "3", "In progress", Icons.Default.Download, Surface3, CoolBlue)
                    SummaryCard(Modifier.weight(1f), "Failed", "1", "Tap to retry", Icons.Default.Error, ErrorRed.copy(alpha = 0.15f), ErrorRed)
                }
                
                Spacer(Modifier.height(32.dp))
                
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Text("Downloaded Songs", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(16.dp))
                    }
                    
                    items(4) { i ->
                        val titles = listOf("YE YE", "La Chica Yeye (Version Pop)", "Ye 'te Na", "Yeye")
                        val artists = listOf("Faydee, I'm Bax and Pav Dharia", "DANNA", "Oxlade", "Genius.im x66")
                        val times = listOf("3:02", "2:59", "2:45", "2:49")
                        DownloadedSongRow(titles[i], artists[i], times[i])
                        Spacer(Modifier.height(4.dp))
                    }
                    
                    item {
                        Spacer(Modifier.height(32.dp))
                        Text("Downloaded Albums", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            DownloadedAlbumCard(Modifier.weight(1f), "After Hours", "OmniTune", "12 songs · 34 min", true)
                            DownloadedAlbumCard(Modifier.weight(1f), "Aurora", "Chillwave", "10 songs · 29 min", false)
                            DownloadedAlbumCard(Modifier.weight(1f), "Neon Skyline", "Synth Pop", "11 songs · 38 min", false)
                        }
                    }
                    
                    item {
                        Spacer(Modifier.height(32.dp))
                        Text("Smart Offline Mixes", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OfflineMixCard(Modifier.weight(1f), "Daily Mix 1", "50 songs")
                            OfflineMixCard(Modifier.weight(1f), "Focus Flow", "40 songs")
                            OfflineMixCard(Modifier.weight(1f), "Afrobeats Heat", "60 songs")
                            OfflineMixCard(Modifier.weight(1f), "Chill Evenings", "45 songs")
                        }
                    }
                    
                    item { Spacer(Modifier.height(40.dp)) }
                }
            }
            
            // Side panel
            Column(modifier = Modifier.weight(0.7f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                // Device Storage
                OmniSurface(modifier = Modifier.fillMaxWidth(), color = Surface2, border = true) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Saved Storage", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                            Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(24.dp))
                        }
                        Text("4.2 GB", style = MaterialTheme.typography.displayMedium, color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                        Text("This device", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        
                        Spacer(Modifier.height(24.dp))
                        
                        Text("Device Storage", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("This PC", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text("256 GB total", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        }
                        Spacer(Modifier.height(8.dp))
                        
                        // Progress bar
                        Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(Shapes.pill).background(Surface3)) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                Box(modifier = Modifier.fillMaxHeight().weight(0.35f).background(Iris))
                                Box(modifier = Modifier.fillMaxHeight().weight(0.35f).background(CoolBlue))
                                Box(modifier = Modifier.fillMaxHeight().weight(0.30f).background(Color.Transparent))
                            }
                        }
                        
                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("87.6 GB used", style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text("168.4 GB free", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        StorageLegendRow(Iris, "OmniTune", "87.6 GB")
                        StorageLegendRow(CoolBlue, "Other Apps", "96.2 GB")
                        StorageLegendRow(Surface3, "Free", "168.4 GB")
                        
                        Spacer(Modifier.height(24.dp))
                        Surface(shape = Shapes.small, color = Surface3, border = BorderStroke(1.dp, BorderLow), modifier = Modifier.fillMaxWidth()) {
                            Text("Manage Storage", color = TextPrimary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(vertical = 10.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
                
                // Settings
                OmniSurface(modifier = Modifier.fillMaxWidth(), color = Surface2, border = true) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Download Quality", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(16.dp))
                        RadioOption("High (320 kbps)", "Best audio quality", true)
                        Spacer(Modifier.height(12.dp))
                        RadioOption("Medium (256 kbps)", "Balanced quality and size", false)
                        Spacer(Modifier.height(12.dp))
                        RadioOption("Low (128 kbps)", "Smaller file size", false)
                        
                        Spacer(Modifier.height(24.dp))
                        Text("Download Over", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            RadioOption("Wi-Fi Only", null, true)
                            Text("Recommended", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            RadioOption("Wi-Fi & Cellular", null, false)
                            Text("May use data", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        
                        Spacer(Modifier.height(24.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Auto-Download", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(4.dp))
                                Text("Smart Downloads", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                Text("Automatically download music you listen to most.", style = MaterialTheme.typography.bodySmall, color = TextSecondary, lineHeight = 16.sp)
                            }
                            Switch(checked = true, onCheckedChange = {}, colors = SwitchDefaults.colors(checkedTrackColor = Iris, checkedThumbColor = Color.White))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterPill(label: String, isActive: Boolean) {
    val bg = if (isActive) Iris else Surface2
    val border = if (isActive) Color.Transparent else BorderLow
    val textColor = if (isActive) Color.White else TextSecondary
    
    Surface(shape = Shapes.pill, color = bg, border = BorderStroke(1.dp, border), modifier = Modifier.height(36.dp).clickable {}) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 20.dp).fillMaxHeight()) {
            Text(label, color = textColor, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun SummaryCard(modifier: Modifier, title: String, value: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, bgTint: Color, iconTint: Color) {
    OmniSurface(modifier = modifier, color = Surface2, border = true) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text(value, style = MaterialTheme.typography.displaySmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(bgTint), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun DownloadedSongRow(title: String, artist: String, time: String) {
    OmniSurface(modifier = Modifier.fillMaxWidth(), color = Surface2, border = false) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(Shapes.artworkSmall).background(BgCard))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Text(artist, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Text("320 kbps", style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.padding(horizontal = 16.dp))
            Text("Downloaded", style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.padding(horizontal = 16.dp))
            Text(time, style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.padding(horizontal = 16.dp))
            Icon(Icons.Default.MoreHoriz, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun DownloadedAlbumCard(modifier: Modifier, title: String, subtitle: String, meta: String, isFirst: Boolean) {
    OmniSurface(modifier = modifier, color = Surface2, border = true) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.size(60.dp).clip(Shapes.medium).background(if (isFirst) Iris.copy(alpha = 0.5f) else BgCard))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Icon(Icons.Default.MoreHoriz, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1)
                    Spacer(Modifier.height(8.dp))
                    Text(meta, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text("320 kbps    Downloaded", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun OfflineMixCard(modifier: Modifier, title: String, count: String) {
    Column(modifier = modifier) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(Shapes.medium).background(Surface2)) {
            // Placeholder for mix art
            Box(modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).size(32.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.6f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
        Text(count, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}

@Composable
private fun StorageLegendRow(color: Color, label: String, size: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextPrimary, modifier = Modifier.weight(1f))
        Text(size, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}

@Composable
private fun RadioOption(label: String, description: String?, selected: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(if (selected) Iris else Color.Transparent).border(1.dp, if (selected) Color.Transparent else BorderLow, CircleShape))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = if (selected) TextPrimary else TextSecondary)
            if (description != null) {
                Text(description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
    }
}
