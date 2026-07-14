# OmniTune Windows keyboard traversal matrix

Date: 2026-07-14

Scope: local feasible audit. This pass verified implemented keyboard routing with deterministic desktop tests, added official Compose Desktop UI-test support, proved focused Compose-runtime Tab/Shift+Tab movement in a minimal runtime surface, and added representative production-component runtime assertions. It did not perform a complete production-screen Tab/Shift+Tab assertion through every pixel surface.

| Screen | Control | Reachable | Visible focus | Activate | Return focus | Result |
|---|---|---|---|---|---|---|
| Shell | Ctrl+K global search | YES | YES, text field focus | Ctrl+K | n/a | PASS |
| Shell | Ctrl+, Settings | YES | Screen route changes | Ctrl+, | n/a | PASS |
| Shell | Media Play/Pause | YES | n/a | MediaPlayPause | n/a | PASS |
| Shell | Media Next/Previous | Not bound in current Compose code | n/a | Keyboard `N`/`P` work; hardware next/previous constants not verified in local Compose runtime | n/a | PARTIAL |
| Shell | Keyboard seek | YES | n/a | Left/Right arrows | n/a | PASS |
| Shell | Repeated key-down guard | YES | n/a | Held one-shot shortcut ignored after first key-down | n/a | PASS |
| Shell | Global shortcut suppression while editable text owns focus | YES for all inventoried desktop text fields | Text field focus | Space/N/P ignored by global router while text field focused | n/a | PASS |
| Search | Standard Enter | YES | Text field focus | Enter key-down | n/a | PASS |
| Search | Numpad Enter | YES | Text field focus | NumPadEnter key-down | n/a | PASS |
| Playlists | Search Enter | YES | Text field focus | Enter/NumPadEnter key-down | n/a | PASS |
| Queue | Save as Playlist dialog | YES | YES, name field focus asserted | Enter/NumPadEnter/Escape runtime-tested on production dialog composable | Opener restoration proven in production-style harness | PASS/PARTIAL |
| Sidebar | Primary navigation | YES | Runtime focus not exhaustively asserted | Pointer activation runtime-tested for Browse, Radio, Settings | n/a | PASS/PARTIAL |
| Settings | Switches/radio controls | YES by pointer; keyboard traversal not fully proven | PARTIAL | Compose controls support activation | PARTIAL | PARTIAL |
| Downloads | Quality radio controls | YES by pointer; keyboard traversal not fully proven | PARTIAL | Compose radio controls support activation | PARTIAL | PARTIAL |
| Mini Player | Transport controls | YES by pointer; keyboard traversal not fully proven | PARTIAL | Button callbacks exist | n/a | PARTIAL |
| Menus/dialogs | Escape close behavior | Some fields support Escape; all menus/dialogs not manually verified | PARTIAL | Escape where wired | PARTIAL | PARTIAL |

## Editable field inventory

| Screen/Dialog | Field | Focus ownership tracked | Space safe | N safe | P safe | Enter behavior | Escape behavior |
|---|---|---:|---:|---:|---:|---|---|
| Top bar | Global search | YES | YES | YES | YES | Submit search via `OmniSearchField` | Clears field |
| Search | Search page input | YES | YES | YES | YES | Submit search via `OmniSearchField` | Clears field |
| Playlists | Playlist search | YES | YES | YES | YES | `Enter`/`NumPadEnter` search via parent key handler | Not custom-wired |
| Queue dialog | Save as Playlist name | YES | YES | YES | YES | Enter/NumPadEnter confirms when nonblank | Escape cancels/closes |

## Source-level findings

- `OmniSearchField` handles `Key.Enter`, `Key.NumPadEnter`, and `Key.Escape` on key-down.
- `OmniWindow` handles `Spacebar`, `MediaPlayPause`, left/right seek, keyboard `N`/`P` next/previous shortcuts, `Ctrl+K`, and `Ctrl+,` through `OmniKeyboardShortcutRouter`.
- `OmniWindow` now passes modal-open state into the shortcut router. Active modals suppress global shell shortcuts, including Ctrl+K/Ctrl+, so modal interaction takes precedence.
- Global shell shortcuts now run on `KeyDown` instead of `KeyUp`. This avoids the specific text-input leakage risk where a focused text field consumes text on key-down but the parent shell still sees a Space key-up and toggles playback.
- Global and Search-page fields now report editable focus ownership to the shell. While those fields own focus, the router ignores Space/N/P global playback shortcuts and keeps Ctrl+K/Ctrl+, behavior explicit.
- Playlists search and Queue Save as Playlist name field now also report editable focus ownership to the shell. The inventory found no other desktop `TextField`, `OutlinedTextField`, or `BasicTextField` usage.
- A pressed-key guard suppresses repeated key-down events for one-shot global commands, preventing held Space/N/P from rapidly toggling playback or skipping tracks.
- Queue Save as Playlist text input now handles Enter/NumPadEnter to confirm a nonblank playlist name and Escape to cancel/close without saving.
- Shared Omni components now render a restrained focus border for common surfaces, primary/secondary buttons, icon buttons, and section-header actions.
- The previous source scan found no empty click handlers in the desktop window code after fixes.
- The previous source scan found no clickable action icons with null content descriptions in the audited desktop window code after fixes.

## Runtime attempts in this pass

Two Windows-level input attempts were made against a live Compose Desktop window:

1. `System.Windows.Forms.SendKeys`
2. `WScript.Shell.SendKeys`

Both launched OmniTune and captured evidence images:

- `docs/qa/keyboard-runtime-ctrlk-space.png`
- `docs/qa/keyboard-runtime-ctrlk-space-wscript.png`

Neither produced observable typed text in the Java/Compose window, so they are not counted as full keyboard runtime PASS. The routing bug risk was still fixed in code, and compile/tests pass.

## Deterministic keyboard tests

`OmniKeyboardShortcutRouterTest` verifies:

- text fields own Space and plain N/P input;
- Ctrl+K and Ctrl+, remain explicit while editable text owns focus;
- modal-open state suppresses shell-level global shortcuts;
- modal-open state takes precedence over editable text and Ctrl shortcuts;
- key-up never triggers one-shot commands;
- repeated key-down events are ignored for one-shot commands;
- key-up without prior key-down is tolerated;
- pressed-key state can be cleared on focus/modal context changes;
- Space, MediaPlayPause, Left/Right, N/P, Ctrl+K, and Ctrl+, map to the expected shell commands when no text field/modal owns focus.

`OmniFocusTraversalModelTest` verifies a deterministic intended focus order for Search, Library, Playlist Detail, Now Playing, Queue, Settings, Downloads, Mini Player, Sidebar, and Bottom Player. This is **PROVEN BY TESTABLE FOCUS MODEL**, not a full Compose runtime focus assertion.

## Compose runtime UI-test proof — 2026-07-14

Minimum official Compose Desktop UI-test capability was added to `desktopTest`:

```kotlin
implementation(compose.desktop.uiTestJUnit4)
implementation(compose.desktop.currentOs)
```

`OmniComposeFocusRuntimeTest` verifies:

- a composable can be rendered under `createComposeRule`;
- nodes can be located by stable test tag;
- focused state can be asserted with `assertIsFocused`;
- Tab can be injected and asserted as forward focus movement;
- Shift+Tab can be injected and asserted as reverse focus movement;
- the real `OmniSearchField` accepts `Space`, `N`, and `P` text input at runtime;
- the real `OmniSearchField` invokes Enter, NumPadEnter, and Escape callbacks at runtime;
- a Compose `AlertDialog`-style modal pattern can move focus into the dialog, traverse dialog actions with Tab/Shift+Tab, close on Escape, and restore focus to the opener.
- production `OmniTopBar` global search accepts typed input and handles Enter/NumPadEnter/Escape under Compose runtime;
- production `QueueSaveAsPlaylistDialog` focuses the name field, rejects blank confirm state, accepts Space/N/P text, handles Enter/NumPadEnter, and closes on Escape;
- a production-style Queue Save as Playlist opener restores focus after Escape closes the production dialog;
- production `OmniSidebar` primary navigation actions remain runtime-reachable;
- production Sidebar library submenu children are absent while collapsed and runtime-reachable after expanding Library;
- production `PlayerControlBand` bottom-player transport actions fire exactly once per click for shuffle, previous, play/pause, next, and repeat;
- production mini-player transport button component fires exactly once per click for previous and next;
- focused top-bar search screenshots are captured for Nocturne, Midnight, Dusk, and Aurora.

Command run:

```powershell
.\gradlew.bat :composeApp:desktopTest --tests com.omnitune.app.window.OmniComposeFocusRuntimeTest
```

Result: PASS.

## Compose runtime focus testing capability

Updated dependency inspection:

- `desktopTest` declares Kotlin test, Compose Desktop UI-test JUnit4, current OS desktop runtime, coroutines, and project dependencies.
- Resolved classpath includes Compose UI runtime artifacts.
- Compose UI-test artifacts are now configured and compile under the current project versions.

Runtime focus testing capability:

| Capability | Status |
|---|---|
| Launch composables under a Compose UI test rule | Supported and proven |
| Locate nodes by semantics/test tag | Supported and proven |
| Request/assert focused state in test | Supported and proven |
| Inject Tab/Shift+Tab/Enter/Space/Escape in Compose test | Supported and partially proven |
| Test pure shortcut/focus policy models | Supported and passing |

Remaining blocker: full production-screen runtime focus assertions require safe seams around PlayerViewModel-backed surfaces. `PlayerViewModel` currently wires concrete VLC, download, provider, and discovery behavior in `init`, so full route mounting is intentionally avoided for focus tests. Representative production components are now proven.

Command run:

```powershell
.\gradlew.bat :composeApp:desktopTest --tests com.omnitune.app.window.OmniKeyboardShortcutRouterTest
.\gradlew.bat :composeApp:desktopTest --tests com.omnitune.app.window.OmniComposeFocusRuntimeTest
```

Result: PASS.

## Remaining runtime work

1. Full keyboard-only walkthrough for PlayerViewModel-backed Home, Browse, Radio, Search route, Library route, Playlist, Artist, Album, Now Playing route, Downloads route, tray, and menus.
2. Introduce a real playback/provider test seam only if full route mounting becomes necessary; avoid further generic focus scaffolding.
3. Verify full `QueueView` route opener/restoration with a safe real `PlayerViewModel` seam if that seam is later introduced.
4. Verify no focus trap at 1672×941, 1366×768, and 1012×643.
5. Verify hardware media next/previous keys when the local Compose version exposes stable key constants or through OS-level media-session integration.

## Verdict

Keyboard traversal: **STRONG PARTIAL WITH DETERMINISTIC ROUTER PROOF AND REPRESENTATIVE PRODUCTION COMPOSE RUNTIME PROOF**.

Core shortcuts and search Enter behavior are implemented; the known Space/key-up leakage risk was fixed; held-key repeat risk is guarded; all inventoried editable fields now suppress shell playback shortcuts while focused. Compose Desktop UI-test focus assertion is proven on minimal and representative production surfaces. Complete runtime Tab/Shift+Tab traversal across every PlayerViewModel-backed production screen remains a local product gap.
