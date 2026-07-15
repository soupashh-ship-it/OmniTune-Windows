# Final Performance and Smoothness Report

Date: 2026-07-15

Project: `D:\Omnitune Windoww`

## Result

OmniTune Windows received a targeted smoothness optimization focused on the reported P0 issue: manual window resizing not feeling smooth.

The retained changes remove avoidable work from the hottest interaction paths without changing product scope, version, canonical geometry, themes, playback behavior, provider behavior, downloads, packaging, or release state.

## Biggest original bottlenecks

1. Window resize persisted settings on every size event.
2. Playback position was collected too high in the Compose tree.
3. SMTC position updates used Compose root state collection instead of side-effect flow collection.

## Resize bottleneck

Previous behavior:

- Every `windowState.size` change wrote `settings.windowWidth`.
- Every `windowState.size` change wrote `settings.windowHeight`.
- Every size change immediately called `settings.flush()`.

Retained fix:

- Window content still responds immediately to size changes.
- Persistent width/height storage now waits until resize changes settle for 500 ms.
- Only the final settled size is flushed.

Expected user-visible effect:

- Less UI-thread and filesystem pressure during manual resize.
- Resize should feel more direct because persistence no longer competes with live layout.

## Recomposition scope

Previous behavior:

- `OmniWindow` collected playback position at shell level.
- The main app collected playback position as Compose state for SMTC updates.

Retained fix:

- SMTC now collects `currentSong`, `playbackState`, and `position` from coroutines inside `LaunchedEffect`.
- `OmniWindow` no longer collects `player.position` at root.
- `NowPlayingRoute` and `BottomPlayerRoute` collect position locally.

Expected user-visible effect:

- Playback progress updates should no longer invalidate unrelated shell content.
- Sidebar, Top Bar, non-player screens, and route container work are less exposed to high-frequency progress ticks.

## UI-thread blocking

Confirmed and fixed:

- Avoidable settings flush during live resize.

Not changed:

- VLC release and native playback paths were not changed because no release-blocking UI-thread failure was found in this pass.
- Provider/network behavior was not changed.
- Artwork loading was not changed because no concrete resize-triggered reload defect was confirmed.

## Artwork

No artwork code changes were made.

Reason:

- The main confirmed resize issue was persistence/recomposition, not artwork reload.
- Avoiding speculative artwork pipeline changes reduced risk to playback and canonical visuals.

## Backdrop / effects

No visual/effect simplification was made.

Reason:

- Canonical geometry and appearance are already close.
- The safe performance fixes addressed confirmed avoidable work without flattening the design.

## Scroll

No list implementation changes were made.

Reason:

- No concrete scroll regression was discovered during this targeted pass.

## Navigation

No navigation animation change was made.

Reason:

- The requested primary issue was resize smoothness.
- Existing navigation tests remained green.

## Bottom Player

Changed:

- Playback position collection is now scoped inside the bottom player wrapper.

Preserved:

- True centering.
- Geometry.
- Playback callbacks.
- Runtime UI tests.

## Now Playing

Changed:

- Playback position collection is now scoped inside the Now Playing route wrapper.

Preserved:

- Current 3 px transport-height visual residual.
- No speculative visual adjustment was made.

## Lyrics

No lyrics code changes were made.

Reason:

- No concrete lyrics jank source was isolated in this pass.
- The safer high-value fix was limiting root-level progress recomposition.

## Downloads

No downloads code changes were made.

Reason:

- Downloads subsystem remains protected.
- No new download-progress jank defect was confirmed.

## Startup

No startup sequencing changes were made.

Reason:

- The closure build already had launch smoke evidence.
- The current pass did not identify startup as the primary bottleneck.

## Animations

Added:

- None.

Removed:

- None.

Shortened:

- None.

Reduced motion:

- Preserved; no new motion behavior was introduced.

## Evidence

Validated commands:

- `.\gradlew.bat :composeApp:compileKotlinDesktop` — PASS
- `.\gradlew.bat :composeApp:assemble` — PASS
- `.\gradlew.bat test` — PASS
- `.\gradlew.bat :composeApp:desktopTest` — PASS
- `.\gradlew.bat :composeApp:desktopTest --tests com.omnitune.app.window.OmniComposeFocusRuntimeTest` — PASS

Desktop test result:

- 74 tests
- 0 failures
- 0 errors

Targeted runtime UI test result:

- 11 tests
- 0 failures
- 0 errors

Measured limits:

- No FPS number is claimed.
- No automated visual frame-time profiler was added.
- Improvement is supported by removal of confirmed per-resize disk flushing and broad high-frequency Compose state collection.

## Remaining non-blocking performance limitations

1. Manual resize was not converted into an automated frame-time benchmark.
2. Further effect/artwork caching may be possible if future profiling shows a concrete bottleneck.
3. Some continuous responsive metric recalculation remains by design so the layout follows window size immediately.

## Final smoothness assessment

- Window resize: substantially improved at the confirmed source-level bottleneck.
- Scrolling: no change; no concrete defect fixed.
- Navigation: no change; existing behavior preserved.
- Playback UI: improved recomposition scope for progress updates.
- Lyrics: indirectly helped by narrower progress invalidation, but no dedicated lyrics optimization retained.
- Artwork: unchanged.
- Animations: unchanged; no unnecessary motion added.
- Startup: unchanged.
- Overall: targeted improvement with low regression risk.
