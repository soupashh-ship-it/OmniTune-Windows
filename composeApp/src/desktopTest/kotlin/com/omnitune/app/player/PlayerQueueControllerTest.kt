package com.omnitune.app.player

import com.omnitune.app.platform.SettingsRepository
import com.omnitune.innertube.models.Artist
import com.omnitune.innertube.models.SongItem
import java.util.prefs.Preferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlayerQueueControllerTest {
    @Test
    fun playNext_insertsAfterCurrentWithoutMovingCurrent() {
        val controller = PlayerQueueController(testSettings())
        controller.setQueue(listOf(song("a"), song("b")), 0)

        controller.playNext(song("c"))

        assertEquals(listOf("a", "c", "b"), controller.queue.value.map { it.id })
        assertEquals(0, controller.queueIndex.value)
    }

    @Test
    fun removeFromQueue_reportsCurrentRemovalAndAdjustsIndex() {
        val controller = PlayerQueueController(testSettings())
        controller.setQueue(listOf(song("a"), song("b"), song("c")), 1)

        val beforeCurrent = controller.removeFromQueue(0)
        assertEquals(0, controller.queueIndex.value)
        assertFalse(beforeCurrent?.removedCurrent == true)
        assertTrue(beforeCurrent?.removedBeforeCurrent == true)

        val current = controller.removeFromQueue(0)
        assertEquals(-1, controller.queueIndex.value)
        assertTrue(current?.removedCurrent == true)
    }

    @Test
    fun moveQueueItem_preservesCurrentSongByIdentity() {
        val controller = PlayerQueueController(testSettings())
        controller.setQueue(listOf(song("a"), song("b"), song("c")), 1)

        controller.moveQueueItem(1, 2)

        assertEquals(listOf("a", "c", "b"), controller.queue.value.map { it.id })
        assertEquals(2, controller.queueIndex.value)
    }

    @Test
    fun shufflePrevious_returnsPriorShuffledItemInsteadOfRandomItem() {
        val controller = PlayerQueueController(testSettings())
        controller.setQueue(listOf(song("a"), song("b"), song("c"), song("d")), 0)
        controller.toggleShuffle()

        val firstNext = controller.nextIndex()
        assertTrue(firstNext != null)
        controller.navigateToIndex(firstNext!!)

        val secondNext = controller.nextIndex()
        assertTrue(secondNext != null)
        controller.navigateToIndex(secondNext!!)

        val previous = controller.previousIndex()

        assertEquals(firstNext, previous)
    }

    @Test
    fun shuffleNext_afterPreviousReturnsForwardItem() {
        val controller = PlayerQueueController(testSettings())
        controller.setQueue(listOf(song("a"), song("b"), song("c"), song("d")), 0)
        controller.toggleShuffle()

        val next = controller.nextIndex()
        assertTrue(next != null)
        controller.navigateToIndex(next!!)

        val previous = controller.previousIndex()
        assertEquals(0, previous)
        controller.navigateToIndex(previous!!)

        assertEquals(next, controller.nextIndex())
    }

    private fun testSettings(): SettingsRepository =
        SettingsRepository(Preferences.userRoot().node("/omnitune-tests/player-queue-${System.nanoTime()}"))

    private fun song(id: String): SongItem =
        SongItem(
            id = id,
            title = "Song $id",
            artists = listOf(Artist("Artist", null)),
            thumbnail = "",
        )
}
