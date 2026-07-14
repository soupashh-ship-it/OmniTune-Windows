package com.omnitune.app.window

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType

enum class OmniKeyboardCommand {
    TogglePlayPause,
    SeekBackward,
    SeekForward,
    NextTrack,
    PreviousTrack,
    FocusSearch,
    OpenSettings,
}

data class OmniKeyboardInput(
    val key: Key,
    val type: KeyEventType,
    val ctrlPressed: Boolean = false,
    val editableTextFocused: Boolean = false,
    val modalOpen: Boolean = false,
    val repeatedKeyDown: Boolean = false,
)

class OmniPressedKeyTracker {
    private val pressed = mutableSetOf<Key>()

    fun repeatedKeyDownFor(key: Key, type: KeyEventType): Boolean {
        return when (type) {
            KeyEventType.KeyDown -> !pressed.add(key)
            KeyEventType.KeyUp -> {
                pressed.remove(key)
                false
            }
            else -> false
        }
    }

    fun clear() {
        pressed.clear()
    }
}

object OmniKeyboardShortcutRouter {
    fun commandFor(input: OmniKeyboardInput): OmniKeyboardCommand? {
        if (input.type != KeyEventType.KeyDown) return null
        if (input.repeatedKeyDown) return null

        if (input.modalOpen) {
            return when {
                input.ctrlPressed && input.key == Key.K -> null
                input.ctrlPressed && input.key == Key.Comma -> null
                else -> null
            }
        }

        if (input.editableTextFocused) {
            return when {
                input.ctrlPressed && input.key == Key.K -> OmniKeyboardCommand.FocusSearch
                input.ctrlPressed && input.key == Key.Comma -> OmniKeyboardCommand.OpenSettings
                else -> null
            }
        }

        return when (input.key) {
            Key.Spacebar,
            Key.MediaPlayPause -> OmniKeyboardCommand.TogglePlayPause
            Key.DirectionLeft -> OmniKeyboardCommand.SeekBackward
            Key.DirectionRight -> OmniKeyboardCommand.SeekForward
            Key.N -> OmniKeyboardCommand.NextTrack
            Key.P -> OmniKeyboardCommand.PreviousTrack
            Key.K -> if (input.ctrlPressed) OmniKeyboardCommand.FocusSearch else null
            Key.Comma -> if (input.ctrlPressed) OmniKeyboardCommand.OpenSettings else null
            else -> null
        }
    }
}
