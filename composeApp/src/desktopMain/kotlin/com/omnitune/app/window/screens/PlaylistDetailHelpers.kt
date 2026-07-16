package com.omnitune.app.window.screens

import com.omnitune.innertube.models.SongItem
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

internal enum class PlaylistTrackAction {
    PlayNext,
    AddToQueue,
    ToggleLike,
    AddToPlaylist,
    Download,
    ViewAlbum,
    GoToArtist,
    Remove,
    MoveUp,
    MoveDown,
}

internal fun inferPlaylistTags(title: String, songs: List<SongItem>): List<String> {
    val text = (title + " " + songs.take(8).flatMap { song ->
        listOf(song.title) + song.artists.map { it.name } + listOfNotNull(song.album?.name)
    }.joinToString(" ")).lowercase()
    val tags = buildList {
        if (text.contains("chill") || text.contains("lofi") || text.contains("late")) add("Chill")
        if (text.contains("r&b") || text.contains("soul")) add("R&B")
        if (text.contains("night") || text.contains("moon") || text.contains("midnight")) add("Late Night")
        if (text.contains("focus") || text.contains("ambient")) add("Focus")
        if (text.contains("dance") || text.contains("pop")) add("Pop")
    }
    return (tags + listOf("Playlist", "${songs.size} tracks")).distinct().take(6)
}

internal fun choosePlaylistCoverFile(): String? {
    val dialog = FileDialog(Frame(), "Choose playlist cover", FileDialog.LOAD)
    dialog.setFilenameFilter { _, name ->
        name.endsWith(".png", true) || name.endsWith(".jpg", true) || name.endsWith(".jpeg", true)
    }
    dialog.isVisible = true
    val file = dialog.file ?: return null
    val dir = dialog.directory ?: return null
    return File(dir, file).absolutePath
}

internal fun formatPlaylistDurationLong(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes} min"
}

internal fun SongItem.playlistDurationLabel(): String {
    val value = duration ?: return ""
    return "${value / 60}:${(value % 60).toString().padStart(2, '0')}"
}
