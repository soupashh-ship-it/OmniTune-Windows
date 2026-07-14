# OmniTune Windows accessibility, focus, and keyboard audit

Date: 2026-07-14

Scope: feasible local audit pass. This does not replace a full assistive-technology lab pass, but it records source-level and runtime-adjacent evidence for the current desktop UI.

## Summary

| Area | Result | Evidence | Notes |
|---|---|---|---|
| Empty click handlers | PASS | `rg "onClick = \\{\\s*\\}"` returned no matches in desktop window/Main sources after fixes | Unsupported Artist and Album actions were removed instead of left as dead buttons. |
| Clickable action icons with null descriptions | PASS | `rg` scan for clickable `Icon(..., null)` returned no matches after fixes | Decorative/non-action artwork may still use null descriptions intentionally. |
| Global search shortcut | PASS | `OmniWindow.kt` handles `Ctrl+K` and focuses the top search field | Existing behavior preserved. |
| Settings shortcut | PASS | `OmniWindow.kt` handles `Ctrl+,` and navigates Settings | Existing behavior preserved. |
| Standard Enter search | PASS | `OmniSearchField` handles `Key.Enter` on key-down | Existing provider-backed search path preserved. |
| Numpad Enter search | PASS | `OmniSearchField` handles `Key.NumPadEnter` on key-down | Existing provider-backed search path preserved. |
| Media key play/pause | PASS | `OmniWindow.kt` handles `Key.MediaPlayPause` | Existing playback path preserved. |
| Keyboard seek | PASS | `OmniWindow.kt` handles left/right arrow relative seeks | Existing playback path preserved. |
| Text-input shortcut safety | PASS | All inventoried desktop text fields report editable focus ownership into `OmniKeyboardShortcutRouter` | Space/N/P global playback shortcuts are suppressed while editable text owns focus. |
| Held-key repeat suppression | PASS | `OmniPressedKeyTracker` and deterministic tests | One-shot global commands ignore repeated key-down until key-up or focus context reset. |
| Focus traversal intent | PARTIAL PASS | `OmniFocusTraversalModelTest`; `OmniComposeFocusRuntimeTest` | Intended Tab/Shift+Tab order is deterministic and tested; Compose-runtime Tab/Shift+Tab focus assertion now passes; representative production components are runtime-tested; full PlayerViewModel-backed screen traversal remains pending. |
| Modal shortcut containment | PASS | `OmniWindow` passes `modalOpen` into `OmniKeyboardShortcutRouter` | Active modals suppress shell shortcuts such as Ctrl+K/Ctrl+, Space, N/P, and seek keys. |
| Save as Playlist Enter/Escape | PASS/PARTIAL | Production `QueueSaveAsPlaylistDialog` runtime test | Name field focus, Space/N/P text, blank validation, Enter, NumPadEnter, Escape, and opener restoration are runtime-tested through the production dialog composable and production-style opener harness. |
| Mini player controls | PARTIAL PASS | Production `MiniBtn` transport component is runtime-tested for one action per click | Full `OmniMiniPlayer` route still depends on `PlayerViewModel` and was not mounted end-to-end in Compose runtime tests. |
| Focus ring coverage | PARTIAL PASS | Shared Omni surfaces/buttons/icon buttons/section actions now render a theme-aware focus border; search focus is explicit | Full tab-order and focus-ring visual verification across every custom per-screen clickable remains pending. |
| Contrast | PARTIAL | Nocturne palette uses high contrast primary text and muted secondary text | Full automated contrast pass across all themes remains pending. |
| Reduced motion | PASS/PARTIAL | Central `OmniMotionPolicy` exists and key transitions consume it | Full animation inventory was previously improved, but not exhaustively runtime-tested here. |

## Changes made in this pass

- Removed the dead Artist Detail hero-level `More` button.
- Removed dead Album Detail header `Add`, `Favorite`, and `More` buttons that had no real album-level backend action.
- Replaced misleading row action icons:
  - Artist track like action now uses a favorite icon instead of a More icon.
  - Album track like action now uses a favorite icon instead of a More icon.
  - Queue row removal now uses a delete icon instead of a More/Play icon.
  - Search result row secondary action now exposes Like instead of a fake More action.
- Added action content descriptions for clickable row icons in Library, Playlist Detail, Album Detail, Artist Detail, Queue, Search, and Downloads.
- Added deterministic shortcut routing tests for text-field ownership, modal suppression, key-up suppression, repeat suppression, and stuck-key clearing.
- Added focus ownership coverage for global search, Search page input, Playlists search, and Queue Save as Playlist name input.
- Added a deterministic focus-order model for Search, Library, Playlist Detail, Now Playing, Queue, Settings, Downloads, Mini Player, Sidebar, and Bottom Player.
- Added modal-open routing so global shortcuts do not steal focus while Queue Save as Playlist is open.
- Added Enter/NumPadEnter confirmation and Escape cancellation for Queue Save as Playlist.
- Added official Compose Desktop UI-test support to `desktopTest` and proved runtime focus assertion/key injection with `OmniComposeFocusRuntimeTest`.
- Proved the real `OmniSearchField` accepts Space/N/P text input and fires Enter/NumPadEnter/Escape callbacks under Compose UI test.
- Proved a Compose `AlertDialog` modal-focus pattern for first focus, Tab/Shift+Tab between modal actions, Escape close, and opener focus restoration.
- Proved production `OmniTopBar` global search accepts input and handles Enter/NumPadEnter/Escape under Compose runtime.
- Extracted and tagged production `QueueSaveAsPlaylistDialog`; proved field focus, blank validation, Enter/NumPadEnter/Escape, and opener restoration in a production-style harness.
- Added stable runtime tags to representative Sidebar navigation controls and proved Browse/Radio/Settings activation.
- Proved Sidebar Library submenu runtime behavior: submenu children are absent while collapsed and reachable after Library is expanded.
- Proved production bottom-player `PlayerControlBand` transport callbacks fire exactly once per action for shuffle, previous, play/pause, next, and repeat.
- Proved production mini-player transport button component callbacks fire exactly once for previous and next.
- Documented PlayerViewModel-backed route mounting blockers in `docs/qa/production-route-runtime-testability.md`; full route tests are intentionally not forced through VLC/provider/download initialization side effects.
- Captured focused top-bar search evidence for Nocturne, Midnight, Dusk, and Aurora under `docs/qa/focus-themes/`.

## Remaining accessibility gaps

1. Full manual screen-reader pass was not performed.
2. Full Compose-runtime keyboard-only navigation through every PlayerViewModel-backed production panel/modal was not performed.
3. Full WCAG contrast measurement across all four themes was not performed.
4. Full Windows scaling/accessibility text-size matrix was not rerun.

## Verdict

Local accessibility/focus/keyboard pass: **STRONG PARTIAL PASS WITH REPRESENTATIVE PRODUCTION COMPOSE RUNTIME PROOF**.

Additional 2026-07-14 update: shared Omni component focus visibility was strengthened without changing layout geometry. This improves common buttons, icon buttons, clickable surfaces, and section-header actions, but does not prove every custom per-screen clickable has a visible focus treatment.

No dead empty click handlers or clickable null-described action icons remain in the audited desktop window sources. Compose-runtime focus testing is now available and representative production components are proven, but full PlayerViewModel-backed screen traversal and assistive-technology runtime validation are still required before calling this complete.
