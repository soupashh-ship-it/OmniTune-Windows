# OmniTune Windows Phase 2 Playback and Queue Remediation Report

Date: 2026-07-16  
Project root: `D:\Omnitune Windoww`  
Branch: `main`

## Overall result

PARTIAL SUCCESS

Completed in this pass:

- Added direct stale-playback request gate regression coverage.
- Improved shuffle queue previous/next semantics by preserving shuffle back/forward history.
- Added queue tests for shuffle history behavior.

Deferred:

- `VlcjAudioEngine.release()` still uses a blocking release path. This remains high-risk to change without a dedicated media-engine lifecycle seam and shutdown tests.

## Playback request gate

Problem:

- The previous audit identified stale async source-resolution as a critical class of playback bug.
- Source-level protection already existed through `PlaybackRequestGate`, but direct regression coverage was missing.

Changed:

- Added `PlaybackRequestGateTest`.

Verified behavior:

- An older request token cannot apply after a newer track selection.
- A matching token still cannot apply to a different current song.

Files:

- `composeApp/src/desktopTest/kotlin/com/omnitune/app/player/PlaybackRequestGateTest.kt`

## Shuffle queue semantics

Problem:

- Shuffle next/previous previously selected random indexes independently.
- Pressing Previous in shuffle mode could jump to another random track instead of the track that actually played before.

Changed:

- `PlayerQueueController` now keeps shuffle back/forward stacks.
- `nextIndex()` records the current shuffled index before advancing.
- `previousIndex()` returns the actual prior shuffled item when available.
- `nextIndex()` after a shuffle Previous can return the forward item.
- Explicit queue replacement/selection/mutation clears shuffle history.

Files:

- `composeApp/src/desktopMain/kotlin/com/omnitune/app/player/PlayerQueueController.kt`
- `composeApp/src/desktopMain/kotlin/com/omnitune/app/player/PlayerViewModel.kt`
- `composeApp/src/desktopTest/kotlin/com/omnitune/app/player/PlayerQueueControllerTest.kt`

Implementation note:

- `PlayerViewModel.playQueueIndex()` now uses `navigateToIndex()` instead of explicit `selectIndex()` so next/previous playback navigation does not erase shuffle history.
- Explicit user selection still uses `selectIndex()` and clears shuffle history.

## Tests run

```powershell
.\gradlew.bat :composeApp:desktopTest --tests "com.omnitune.app.player.*"
```

Result:

- PASS

Notes:

- The first run after an interrupted Gradle process printed a Kotlin incremental-cache/daemon warning but still completed successfully through fallback compilation.
- `.\gradlew.bat --stop` was run.
- The targeted player tests were rerun cleanly and passed.

## Remaining playback item

`VlcjAudioEngine.release()` still deserves a focused lifecycle pass.

Recommended next safe order:

1. Introduce a testable media-engine lifecycle abstraction or fake VLC release seam.
2. Add delayed-release and double-release tests.
3. Make shutdown release bounded without risking native resource leaks.
4. Verify close/shutdown while playing, paused, buffering, and error states.
