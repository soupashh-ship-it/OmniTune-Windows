import com.omnitune.app.platform.SettingsRepository
import com.omnitune.app.platform.PlaybackSession
import com.omnitune.app.platform.PlaybackHistoryEntry
import com.omnitune.innertube.models.SongItem
import com.omnitune.innertube.models.Artist
import kotlin.system.measureTimeMillis

import org.junit.Test

class StressTest {
    @Test
    fun runStressTest() {
        val prefs = java.util.prefs.Preferences.userRoot().node("omnitune_stress_test")
        prefs.clear()
        val tempDir = java.nio.file.Files.createTempDirectory("omnitune_test").toFile()
        val context = com.omnitune.app.platform.PlatformContext(tempDir)
        val repo = SettingsRepository(prefs, context)

        val song = SongItem(id = "test_id", title = "Test Song", artists = listOf(Artist("Artist", "id")), thumbnail = "thumb.jpg", duration = 180)
        
        // 1000 history
        val timeHistory = measureTimeMillis {
            for (i in 1..1000) {
                repo.recordMeaningfulPlayback(song, System.currentTimeMillis() + (i * 1000000L), 180000, 180000, true)
            }
        }
        
        // 100 playlists
        val timePlaylists = measureTimeMillis {
            val lists = (1..100).map { i ->
                com.omnitune.app.platform.SavedQueuePlaylist(
                    id = "list_$i",
                    name = "List $i",
                    songs = (1..50).map { song.copy(id = "song_${i}_$it") },
                    createdAt = System.currentTimeMillis()
                )
            }
            repo.savedQueuePlaylists = lists
            repo.flush()
        }
        val historySize = java.io.File(tempDir, "playbackHistory.json").length()
        val sessionSize = java.io.File(tempDir, "playbackSessions.json").length()
        val playlistSize = java.io.File(tempDir, "savedQueuePlaylists.json").length()
        
        println("STRESS_TEST_RESULTS:")
        println("History 1000 items: $timeHistory ms")
        println("Playlists 100x50 items: $timePlaylists ms")
        println("History JSON size: ${historySize / 1024} KB")
        println("Session JSON size: ${sessionSize / 1024} KB")
        println("Playlist JSON size: ${playlistSize / 1024} KB")
    }
}
