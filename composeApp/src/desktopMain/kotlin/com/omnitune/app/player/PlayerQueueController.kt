package com.omnitune.app.player

import com.omnitune.app.platform.SettingsRepository
import com.omnitune.innertube.models.SongItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class QueueRemovalResult(
    val removedCurrent: Boolean,
    val removedBeforeCurrent: Boolean,
)

internal class PlayerQueueController(
    private val settings: SettingsRepository,
) {
    internal val queueItems = MutableStateFlow<List<SongItem>>(emptyList())
    val queue: StateFlow<List<SongItem>> = queueItems.asStateFlow()

    internal val queueIndexState = MutableStateFlow(-1)
    val queueIndex: StateFlow<Int> = queueIndexState.asStateFlow()

    internal val shuffleState = MutableStateFlow(settings.shuffleEnabled)
    val shuffleMode: StateFlow<Boolean> = shuffleState.asStateFlow()

    internal val repeatState = MutableStateFlow(
        when (settings.repeatMode) {
            1 -> RepeatMode.ALL
            2 -> RepeatMode.ONE
            else -> RepeatMode.OFF
        }
    )
    val repeatMode: StateFlow<RepeatMode> = repeatState.asStateFlow()

    private val shuffleBackStack = mutableListOf<Int>()
    private val shuffleForwardStack = mutableListOf<Int>()

    fun toggleShuffle() {
        shuffleState.value = !shuffleState.value
        clearShuffleHistory()
        settings.shuffleEnabled = shuffleState.value
        settings.flush()
    }

    fun cycleRepeat() {
        repeatState.value = when (repeatState.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        settings.repeatMode = when (repeatState.value) {
            RepeatMode.OFF -> 0
            RepeatMode.ALL -> 1
            RepeatMode.ONE -> 2
        }
        settings.flush()
    }

    fun setQueue(items: List<SongItem>, startIndex: Int = 0): SongItem? {
        if (items.isEmpty()) return null
        val safeIndex = startIndex.coerceIn(items.indices)
        queueItems.value = items
        queueIndexState.value = safeIndex
        clearShuffleHistory()
        return items[safeIndex]
    }

    fun setDistinctQueue(items: List<SongItem>, startIndex: Int = 0): SongItem? =
        setQueue(items.distinctBy { it.id }, startIndex)

    fun setShuffledQueue(items: List<SongItem>): SongItem? =
        setQueue(items.distinctBy { it.id }.shuffled(), 0)

    fun selectExistingOrAppend(item: SongItem): SongItem {
        val existing = queueItems.value.indexOfFirst { it.id == item.id }
        if (existing >= 0) {
            queueIndexState.value = existing
        } else {
            queueItems.value = queueItems.value + item
            queueIndexState.value = queueItems.value.lastIndex
        }
        clearShuffleHistory()
        return item
    }

    fun selectIndex(index: Int): SongItem? {
        val q = queueItems.value
        if (index !in q.indices) return null
        queueIndexState.value = index
        clearShuffleHistory()
        return q[index]
    }

    fun navigateToIndex(index: Int): SongItem? {
        val q = queueItems.value
        if (index !in q.indices) return null
        queueIndexState.value = index
        return q[index]
    }

    fun nextIndex(): Int? {
        val q = queueItems.value
        if (q.isEmpty()) return null
        val idx = queueIndexState.value
        if (shuffleState.value && q.size > 1) {
            if (idx !in q.indices) return q.indices.random()
            val next = shuffleForwardStack.removeLastOrNull()
                ?.takeIf { it in q.indices && it != idx }
                ?: run {
                    var candidate = idx
                    while (candidate == idx) candidate = q.indices.random()
                    candidate
                }
            shuffleBackStack.add(idx)
            return next
        }
        return when {
            idx < q.lastIndex -> idx + 1
            repeatState.value == RepeatMode.ALL -> 0
            else -> null
        }
    }

    fun previousIndex(): Int? {
        val q = queueItems.value
        if (q.isEmpty()) return null
        val idx = queueIndexState.value
        if (shuffleState.value && q.size > 1) {
            val previous = shuffleBackStack.removeLastOrNull()
                ?.takeIf { it in q.indices && it != idx }
                ?: return null
            if (idx in q.indices) shuffleForwardStack.add(idx)
            return previous
        }
        return if (idx > 0) idx - 1 else null
    }

    fun addToQueue(item: SongItem) {
        queueItems.value = queueItems.value + item
        clearShuffleHistory()
    }

    fun playNext(item: SongItem) {
        val list = queueItems.value.toMutableList()
        val insertAt = (queueIndexState.value + 1).coerceIn(0, list.size)
        list.add(insertAt, item)
        queueItems.value = list
        if (queueIndexState.value >= insertAt) {
            queueIndexState.value = queueIndexState.value + 1
        }
        clearShuffleHistory()
    }

    fun removeFromQueue(index: Int): QueueRemovalResult? {
        val list = queueItems.value.toMutableList()
        if (index !in list.indices) return null
        list.removeAt(index)
        queueItems.value = list
        clearShuffleHistory()
        return when {
            queueIndexState.value == index -> {
                queueIndexState.value = -1
                QueueRemovalResult(removedCurrent = true, removedBeforeCurrent = false)
            }
            queueIndexState.value > index -> {
                queueIndexState.value = queueIndexState.value - 1
                QueueRemovalResult(removedCurrent = false, removedBeforeCurrent = true)
            }
            else -> QueueRemovalResult(removedCurrent = false, removedBeforeCurrent = false)
        }
    }

    fun moveQueueItem(from: Int, to: Int) {
        val list = queueItems.value.toMutableList()
        if (from !in list.indices || to !in list.indices) return
        val item = list.removeAt(from)
        list.add(to, item)
        queueItems.value = list
        clearShuffleHistory()

        val currentIdx = queueIndexState.value
        if (currentIdx == from) queueIndexState.value = to
        else if (currentIdx in (from + 1)..to) queueIndexState.value = currentIdx - 1
        else if (currentIdx in to..<from) queueIndexState.value = currentIdx + 1
    }

    fun clearQueue() {
        queueItems.value = emptyList()
        queueIndexState.value = -1
        clearShuffleHistory()
    }

    private fun clearShuffleHistory() {
        shuffleBackStack.clear()
        shuffleForwardStack.clear()
    }
}
