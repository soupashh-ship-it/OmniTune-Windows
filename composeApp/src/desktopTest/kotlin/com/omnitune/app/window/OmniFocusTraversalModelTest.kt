package com.omnitune.app.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OmniFocusTraversalModelTest {
    @Test
    fun allRequiredScopesHaveMeaningfulFocusOrder() {
        OmniFocusScope.entries.forEach { scope ->
            val order = OmniFocusTraversalModel.order(scope)
            assertTrue(order.isNotEmpty(), "$scope should have at least one focus node")
            assertEquals(order.size, order.map { it.id }.toSet().size, "$scope should not contain duplicate node ids")
        }
    }

    @Test
    fun tabMovesForwardAndShiftTabReversesForComplexScreens() {
        listOf(
            OmniFocusScope.Search,
            OmniFocusScope.Library,
            OmniFocusScope.PlaylistDetail,
            OmniFocusScope.NowPlaying,
            OmniFocusScope.Queue,
            OmniFocusScope.Settings,
            OmniFocusScope.Downloads,
            OmniFocusScope.MiniPlayer,
        ).forEach { scope ->
            val first = assertNotNull(OmniFocusTraversalModel.next(scope, null), "$scope should expose first focus node")
            val second = OmniFocusTraversalModel.next(scope, first.id)
            if (second != null) {
                assertEquals(first, OmniFocusTraversalModel.previous(scope, second.id), "$scope should reverse from second to first")
            }
        }
    }

    @Test
    fun traversalStopsAtScopeBoundaries() {
        val order = OmniFocusTraversalModel.order(OmniFocusScope.Downloads)

        assertNull(OmniFocusTraversalModel.previous(OmniFocusScope.Downloads, order.first().id))
        assertNull(OmniFocusTraversalModel.next(OmniFocusScope.Downloads, order.last().id))
    }

    @Test
    fun invalidCurrentNodeDoesNotJumpToHiddenOrUnknownControl() {
        assertNull(OmniFocusTraversalModel.next(OmniFocusScope.Queue, "not-a-real-node"))
        assertNull(OmniFocusTraversalModel.previous(OmniFocusScope.Queue, "not-a-real-node"))
    }
}
