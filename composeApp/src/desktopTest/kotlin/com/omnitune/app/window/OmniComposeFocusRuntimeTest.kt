package com.omnitune.app.window

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.dp
import com.omnitune.app.player.NavScreen
import com.omnitune.app.player.RepeatMode
import com.omnitune.app.window.components.OmniSearchField
import com.omnitune.innertube.models.Album
import com.omnitune.innertube.models.Artist
import com.omnitune.innertube.models.SongItem
import org.junit.Rule
import org.junit.Test
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.assertEquals

class OmniComposeFocusRuntimeTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun minimalFocusProofTabsForwardAndShiftTabsBackward() {
        rule.setContent {
            FocusProofContent()
        }

        rule.onNodeWithTag("focus-a").assertIsFocused()

        rule.onRoot().performKeyInput {
            pressKey(Key.Tab)
        }
        rule.onNodeWithTag("focus-b").assertIsFocused()

        rule.onRoot().performKeyInput {
            pressKey(Key.Tab)
        }
        rule.onNodeWithTag("focus-c").assertIsFocused()

        rule.onRoot().performKeyInput {
            keyDown(Key.ShiftLeft)
            pressKey(Key.Tab)
            keyUp(Key.ShiftLeft)
        }
        rule.onNodeWithTag("focus-b").assertIsFocused()
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun omniSearchFieldAcceptsTypingAndHandlesEnterAndEscapeAtRuntime() {
        var submitted = 0
        var escaped = 0

        rule.setContent {
            OmniTuneTheme {
                var value by remember { mutableStateOf("") }
                val focusRequester = remember { FocusRequester() }

                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }

                OmniSearchField(
                    value = value,
                    onValueChange = { value = it },
                    modifier = Modifier.testTag("runtime-search-field"),
                    onEnter = { submitted += 1 },
                    onEscape = { escaped += 1 },
                    focusRequester = focusRequester,
                )
            }
        }

        rule.onNodeWithTag("runtime-search-field").assertIsFocused()
        rule.onNodeWithTag("runtime-search-field").performTextInput("n p")
        rule.onNodeWithTag("runtime-search-field").assertTextContains("n p")

        rule.onNodeWithTag("runtime-search-field").performKeyInput {
            pressKey(Key.Enter)
        }
        rule.waitForIdle()
        assertEquals(1, submitted)

        rule.onNodeWithTag("runtime-search-field").performKeyInput {
            pressKey(Key.NumPadEnter)
        }
        rule.waitForIdle()
        assertEquals(2, submitted)

        rule.onNodeWithTag("runtime-search-field").performKeyInput {
            pressKey(Key.Escape)
        }
        rule.waitForIdle()
        assertEquals(1, escaped)
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun modalFocusEntersDialogAndRestoresToOpenerAfterEscape() {
        rule.setContent {
            OmniTuneTheme {
                ModalFocusProofContent()
            }
        }

        rule.onNodeWithTag("modal-opener").assertIsFocused()
        rule.onNodeWithTag("modal-opener").performClick()

        rule.onNodeWithTag("modal-confirm").assertIsFocused()

        rule.onNodeWithTag("modal-confirm").performKeyInput {
            pressKey(Key.Tab)
        }
        rule.onNodeWithTag("modal-cancel").assertIsFocused()

        rule.onNodeWithTag("modal-cancel").performKeyInput {
            keyDown(Key.ShiftLeft)
            pressKey(Key.Tab)
            keyUp(Key.ShiftLeft)
        }
        rule.onNodeWithTag("modal-confirm").assertIsFocused()

        rule.onNodeWithTag("modal-confirm").performKeyInput {
            pressKey(Key.Escape)
        }
        rule.onNodeWithTag("modal-opener").assertIsFocused()
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun productionTopBarGlobalSearchAcceptsTextAndSubmitKeysAtRuntime() {
        var query by mutableStateOf("")
        var submittedQuery: String? = null
        var navigatedToSearch = 0

        rule.setContent {
            TestOmniTheme {
                OmniTopBar(
                    query = query,
                    onQueryChange = { query = it },
                    onSearch = { submittedQuery = it },
                    onNavigateToSearch = { navigatedToSearch += 1 },
                    canGoBack = true,
                    canGoForward = true,
                    onBack = {},
                    onForward = {},
                    modifier = Modifier.width(1100.dp).height(48.dp),
                )
            }
        }

        rule.onNodeWithTag("omni.topbar.globalSearch").performClick()
        rule.onNodeWithTag("omni.topbar.globalSearch").assertIsFocused()
        rule.onNodeWithTag("omni.topbar.globalSearch").performTextInput("n p")
        rule.onNodeWithTag("omni.topbar.globalSearch").assertTextContains("n p")

        rule.onNodeWithTag("omni.topbar.globalSearch").performKeyInput {
            pressKey(Key.Enter)
        }
        rule.waitForIdle()
        assertEquals("n p", submittedQuery)
        assertEquals(1, navigatedToSearch)

        rule.onNodeWithTag("omni.topbar.globalSearch").performKeyInput {
            pressKey(Key.NumPadEnter)
        }
        rule.waitForIdle()
        assertEquals(2, navigatedToSearch)

        rule.onNodeWithTag("omni.topbar.globalSearch").performKeyInput {
            pressKey(Key.Escape)
        }
        rule.waitForIdle()
        assertEquals("", query)
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun productionQueueSaveDialogHandlesTextEnterEscapeAndBlankValidation() {
        var name by mutableStateOf("")
        var saved = 0
        var closed = 0
        var editableFocused = false

        rule.setContent {
            TestOmniTheme {
                QueueSaveAsPlaylistDialog(
                    playlistName = name,
                    onPlaylistNameChange = { name = it },
                    onSave = { if (name.isNotBlank()) saved += 1 },
                    onClose = { closed += 1 },
                    onEditableTextFocusChanged = { editableFocused = it },
                )
            }
        }

        rule.onNodeWithTag("omni.queue.saveDialog.name").assertIsFocused()
        assertEquals(true, editableFocused)
        rule.onNodeWithTag("omni.queue.saveDialog.confirm").assertIsNotEnabled()

        rule.onNodeWithTag("omni.queue.saveDialog.name").performKeyInput {
            pressKey(Key.Enter)
        }
        rule.waitForIdle()
        assertEquals(0, saved)

        rule.onNodeWithTag("omni.queue.saveDialog.name").performTextInput("n p")
        rule.onNodeWithTag("omni.queue.saveDialog.name").assertTextContains("n p")

        rule.onNodeWithTag("omni.queue.saveDialog.name").performKeyInput {
            pressKey(Key.Enter)
        }
        rule.waitForIdle()
        assertEquals(1, saved)

        rule.onNodeWithTag("omni.queue.saveDialog.name").performKeyInput {
            pressKey(Key.NumPadEnter)
        }
        rule.waitForIdle()
        assertEquals(2, saved)

        rule.onNodeWithTag("omni.queue.saveDialog.name").performKeyInput {
            pressKey(Key.Escape)
        }
        rule.waitForIdle()
        assertEquals(1, closed)
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun productionQueueSaveDialogRestoresFocusToOpenerOnEscape() {
        rule.setContent {
            OmniTuneTheme {
                ProductionQueueSaveDialogFocusHarness()
            }
        }

        rule.onNodeWithTag("omni.queue.saveAsPlaylist").assertIsFocused()
        rule.onNodeWithTag("omni.queue.saveAsPlaylist").performClick()
        rule.onNodeWithTag("omni.queue.saveDialog.name").assertIsFocused()

        rule.onNodeWithTag("omni.queue.saveDialog.name").performKeyInput {
            pressKey(Key.Escape)
        }
        rule.onNodeWithTag("omni.queue.saveAsPlaylist").assertIsFocused()
    }

    @Test
    fun productionSidebarNavigationActionsRemainRuntimeReachable() {
        val navigations = mutableListOf<NavScreen>()

        rule.setContent {
            OmniTuneTheme {
                OmniSidebar(
                    activeScreen = NavScreen.Home,
                    hasCurrentSong = false,
                    currentSong = null,
                    likedCount = 0,
                    onNavigate = { navigations += it },
                    width = 230.dp,
                    modifier = Modifier.height(700.dp),
                )
            }
        }

        rule.onNodeWithTag("omni.sidebar.browse").performClick()
        rule.onNodeWithTag("omni.sidebar.radio").performClick()
        rule.onNodeWithTag("omni.sidebar.settings").performClick()

        assertEquals(listOf(NavScreen.Browse, NavScreen.Radio, NavScreen.Settings), navigations)
    }

    @Test
    fun productionSidebarLibrarySubmenuAppearsOnlyWhenExpanded() {
        rule.setContent {
            OmniTuneTheme {
                OmniSidebar(
                    activeScreen = NavScreen.Home,
                    hasCurrentSong = false,
                    currentSong = null,
                    likedCount = 0,
                    onNavigate = {},
                    width = 230.dp,
                    modifier = Modifier.height(700.dp),
                )
            }
        }

        rule.onAllNodesWithTag("omni.sidebar.library.playlists").assertCountEquals(0)
        rule.onNodeWithTag("omni.sidebar.library").performClick()
        rule.onNodeWithTag("omni.sidebar.library.playlists").performClick()
    }

    @Test
    fun productionBottomPlayerControlBandActionsFireOncePerClick() {
        var shuffle = 0
        var previous = 0
        var playPause = 0
        var next = 0
        var repeat = 0

        rule.setContent {
            TestOmniTheme {
                PlayerControlBand(
                    isPlaying = false,
                    positionMs = 1_000,
                    durationMs = 120_000,
                    shuffleEnabled = false,
                    repeatMode = RepeatMode.OFF,
                    currentSong = sampleSong(),
                    onShuffleClick = { shuffle += 1 },
                    onPreviousClick = { previous += 1 },
                    onPlayPauseClick = { playPause += 1 },
                    onNextClick = { next += 1 },
                    onRepeatClick = { repeat += 1 },
                    onSeekFraction = {},
                    modifier = Modifier.width(420.dp).height(80.dp),
                )
            }
        }

        rule.onNodeWithTag("omni.player.shuffle").performClick()
        rule.onNodeWithTag("omni.player.previous").performClick()
        rule.onNodeWithTag("omni.player.playPause").performClick()
        rule.onNodeWithTag("omni.player.next").performClick()
        rule.onNodeWithTag("omni.player.repeat").performClick()

        assertEquals(1, shuffle)
        assertEquals(1, previous)
        assertEquals(1, playPause)
        assertEquals(1, next)
        assertEquals(1, repeat)
    }

    @Test
    fun productionMiniTransportButtonsFireOncePerClick() {
        var previous = 0
        var next = 0

        rule.setContent {
            OmniTuneTheme {
                Row {
                    MiniBtn(Icons.Default.SkipPrevious, enabled = true, tag = "test.miniplayer.previous") { previous += 1 }
                    Spacer(Modifier.width(8.dp))
                    MiniBtn(Icons.Default.SkipNext, enabled = true, tag = "test.miniplayer.next") { next += 1 }
                }
            }
        }

        rule.onNodeWithTag("test.miniplayer.previous").performClick()
        rule.onNodeWithTag("test.miniplayer.next").performClick()

        assertEquals(1, previous)
        assertEquals(1, next)
    }

    @Test
    fun focusedTopBarSearchCanBeCapturedAcrossAllFourThemes() {
        listOf("nocturne", "midnight", "dusk", "aurora").forEach { theme ->
            rule.setContent {
                TestOmniTheme(theme = theme) {
                    OmniTopBar(
                        query = "focus",
                        onQueryChange = {},
                        onSearch = {},
                        onNavigateToSearch = {},
                        canGoBack = true,
                        canGoForward = true,
                        onBack = {},
                        onForward = {},
                        modifier = Modifier.width(1100.dp).height(48.dp),
                    )
                }
            }
            rule.onNodeWithTag("omni.topbar.globalSearch").performClick()
            rule.onNodeWithTag("omni.topbar.globalSearch").assertIsFocused()
            saveRootCapture("docs/qa/focus-themes/$theme/topbar-global-search.png")
        }
    }
}

@Composable
private fun FocusProofContent() {
    val first = remember { FocusRequester() }
    val second = remember { FocusRequester() }
    val third = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        first.requestFocus()
    }

    Column {
        FocusProofNode("focus-a", first, previous = third, next = second)
        FocusProofNode("focus-b", second, previous = first, next = third)
        FocusProofNode("focus-c", third, previous = second, next = first)
    }
}

@Composable
private fun TestOmniTheme(
    theme: String = "nocturne",
    content: @Composable () -> Unit,
) {
    OmniTuneTheme(theme = theme) {
        CompositionLocalProvider(
            LocalHomeReferenceMetrics provides rememberHomeReferenceMetrics(1142.dp),
            content = content,
        )
    }
}

@Composable
private fun FocusProofNode(
    tag: String,
    focusRequester: FocusRequester,
    previous: FocusRequester,
    next: FocusRequester,
) {
    Box(
        modifier = Modifier
            .testTag(tag)
            .size(24.dp)
            .focusRequester(focusRequester)
            .focusProperties {
                this.previous = previous
                this.next = next
            }
            .focusable(),
    )
}

@Composable
private fun ModalFocusProofContent() {
    var open by remember { mutableStateOf(false) }
    val opener = remember { FocusRequester() }
    val confirm = remember { FocusRequester() }
    val cancel = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        opener.requestFocus()
    }

    LaunchedEffect(open) {
        if (!open) {
            opener.requestFocus()
        }
    }

    Button(
        onClick = { open = true },
        modifier = Modifier
            .testTag("modal-opener")
            .focusRequester(opener),
    ) {
        Text("Open")
    }

    if (open) {
        ModalFocusProofDialog(
            confirm = confirm,
            cancel = cancel,
            onClose = { open = false },
        )
    }
}

@Composable
private fun ModalFocusProofDialog(
    confirm: FocusRequester,
    cancel: FocusRequester,
    onClose: () -> Unit,
) {
    LaunchedEffect(Unit) {
        withFrameNanos { }
        confirm.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Save playlist") },
        text = { Text("Confirm save") },
        confirmButton = {
            Button(
                onClick = onClose,
                modifier = Modifier
                    .testTag("modal-confirm")
                    .focusRequester(confirm)
                    .focusProperties {
                        previous = cancel
                        next = cancel
                    },
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onClose,
                modifier = Modifier
                    .testTag("modal-cancel")
                    .focusRequester(cancel)
                    .focusProperties {
                        previous = confirm
                        next = confirm
                    },
            ) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun ProductionQueueSaveDialogFocusHarness() {
    var open by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    val opener = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        opener.requestFocus()
    }

    Button(
        onClick = { open = true },
        modifier = Modifier
            .testTag("omni.queue.saveAsPlaylist")
            .focusRequester(opener),
    ) {
        Text("Save as Playlist")
    }

    if (open) {
        QueueSaveAsPlaylistDialog(
            playlistName = name,
            onPlaylistNameChange = { name = it },
            onSave = { open = false },
            onClose = {
                open = false
                opener.requestFocus()
            },
        )
    }
}

private fun OmniComposeFocusRuntimeTest.saveRootCapture(relativePath: String) {
    val image = rule.onRoot().captureToImage()
    val pixels = image.toPixelMap()
    val buffered = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB)

    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            buffered.setRGB(x, y, pixels[x, y].toArgb())
        }
    }

    val workingDir = File(System.getProperty("user.dir"))
    val repoRoot = if (workingDir.name == "composeApp") workingDir.parentFile else workingDir
    val output = File(repoRoot, relativePath)
    output.parentFile.mkdirs()
    ImageIO.write(buffered, "png", output)
}

private fun sampleSong(): SongItem = SongItem(
    id = "runtime-song",
    title = "Runtime song",
    artists = listOf(Artist(name = "Runtime artist", id = "runtime-artist")),
    album = Album(name = "Runtime album", id = "runtime-album"),
    duration = 180,
    thumbnail = "",
)
