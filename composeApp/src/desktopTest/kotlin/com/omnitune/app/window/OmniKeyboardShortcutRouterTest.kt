package com.omnitune.app.window

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OmniKeyboardShortcutRouterTest {
    @Test
    fun textFieldsOwnSpaceAndPlainLetters() {
        assertNull(command(Key.Spacebar, editableTextFocused = true))
        assertNull(command(Key.N, editableTextFocused = true))
        assertNull(command(Key.P, editableTextFocused = true))
    }

    @Test
    fun ctrlShortcutsStillWorkFromEditableTextWhereAllowed() {
        assertEquals(OmniKeyboardCommand.FocusSearch, command(Key.K, ctrlPressed = true, editableTextFocused = true))
        assertEquals(OmniKeyboardCommand.OpenSettings, command(Key.Comma, ctrlPressed = true, editableTextFocused = true))
    }

    @Test
    fun modalSuppressesGlobalShortcuts() {
        assertNull(command(Key.Spacebar, modalOpen = true))
        assertNull(command(Key.N, modalOpen = true))
        assertNull(command(Key.P, modalOpen = true))
        assertNull(command(Key.K, ctrlPressed = true, modalOpen = true))
        assertNull(command(Key.Comma, ctrlPressed = true, modalOpen = true))
        assertNull(command(Key.MediaPlayPause, modalOpen = true))
        assertNull(command(Key.DirectionLeft, modalOpen = true))
        assertNull(command(Key.DirectionRight, modalOpen = true))
    }

    @Test
    fun modalPrecedenceWinsOverEditableTextAndCtrlShortcuts() {
        assertNull(command(Key.Spacebar, editableTextFocused = true, modalOpen = true))
        assertNull(command(Key.K, ctrlPressed = true, editableTextFocused = true, modalOpen = true))
        assertNull(command(Key.Comma, ctrlPressed = true, editableTextFocused = true, modalOpen = true))
    }

    @Test
    fun keyUpNeverTriggersOneShotCommands() {
        assertNull(
            OmniKeyboardShortcutRouter.commandFor(
                OmniKeyboardInput(key = Key.Spacebar, type = KeyEventType.KeyUp)
            )
        )
    }

    @Test
    fun repeatedKeyDownIsIgnoredForOneShotCommands() {
        assertNull(command(Key.Spacebar, repeatedKeyDown = true))
        assertNull(command(Key.N, repeatedKeyDown = true))
        assertNull(command(Key.P, repeatedKeyDown = true))
        assertNull(command(Key.MediaPlayPause, repeatedKeyDown = true))
        assertNull(command(Key.DirectionLeft, repeatedKeyDown = true))
        assertNull(command(Key.DirectionRight, repeatedKeyDown = true))
    }

    @Test
    fun globalPlaybackAndNavigationShortcutsMapToCommands() {
        assertEquals(OmniKeyboardCommand.TogglePlayPause, command(Key.Spacebar))
        assertEquals(OmniKeyboardCommand.TogglePlayPause, command(Key.MediaPlayPause))
        assertEquals(OmniKeyboardCommand.SeekBackward, command(Key.DirectionLeft))
        assertEquals(OmniKeyboardCommand.SeekForward, command(Key.DirectionRight))
        assertEquals(OmniKeyboardCommand.NextTrack, command(Key.N))
        assertEquals(OmniKeyboardCommand.PreviousTrack, command(Key.P))
        assertEquals(OmniKeyboardCommand.FocusSearch, command(Key.K, ctrlPressed = true))
        assertEquals(OmniKeyboardCommand.OpenSettings, command(Key.Comma, ctrlPressed = true))
    }

    @Test
    fun pressedKeyTrackerAllowsOneCommandPerPressCycle() {
        val tracker = OmniPressedKeyTracker()

        assertFalse(tracker.repeatedKeyDownFor(Key.Spacebar, KeyEventType.KeyDown))
        assertTrue(tracker.repeatedKeyDownFor(Key.Spacebar, KeyEventType.KeyDown))
        assertFalse(tracker.repeatedKeyDownFor(Key.Spacebar, KeyEventType.KeyUp))
        assertFalse(tracker.repeatedKeyDownFor(Key.Spacebar, KeyEventType.KeyDown))
    }

    @Test
    fun pressedKeyTrackerToleratesKeyUpWithoutPriorKeyDown() {
        val tracker = OmniPressedKeyTracker()

        assertFalse(tracker.repeatedKeyDownFor(Key.N, KeyEventType.KeyUp))
        assertFalse(tracker.repeatedKeyDownFor(Key.N, KeyEventType.KeyDown))
    }

    @Test
    fun pressedKeyTrackerCanClearOnFocusOrModalContextChange() {
        val tracker = OmniPressedKeyTracker()

        assertFalse(tracker.repeatedKeyDownFor(Key.P, KeyEventType.KeyDown))
        assertTrue(tracker.repeatedKeyDownFor(Key.P, KeyEventType.KeyDown))
        tracker.clear()
        assertFalse(tracker.repeatedKeyDownFor(Key.P, KeyEventType.KeyDown))
    }

    private fun command(
        key: Key,
        ctrlPressed: Boolean = false,
        editableTextFocused: Boolean = false,
        modalOpen: Boolean = false,
        repeatedKeyDown: Boolean = false,
    ): OmniKeyboardCommand? =
        OmniKeyboardShortcutRouter.commandFor(
            OmniKeyboardInput(
                key = key,
                type = KeyEventType.KeyDown,
                ctrlPressed = ctrlPressed,
                editableTextFocused = editableTextFocused,
                modalOpen = modalOpen,
                repeatedKeyDown = repeatedKeyDown,
            )
        )
}
