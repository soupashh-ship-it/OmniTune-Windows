# Phase 7 — Rebuild Playback Core Correctly

Status: COMPLETED

## Scope
Redesign the playback layer so `Next`, `Previous`, and queue manipulations correctly pass through an authoritative stream resolution step before audio playback begins.

## Baseline
Windows path: `D:\Omnitune Windows`
Android reference path: `D:\code\omnitune`

## Investigation Findings
Previously, `VlcjOmniPlayer.playNext()` just updated the internal `_currentTrack` metadata state but left a note saying `// We don't have streamURL here since we skip resolving it for this stub`.

## Changes Made
- Created `QueueManager.kt` to securely govern playlist arrays and current tracking indices.
- Created `PlaybackCoordinator.kt` as an orchestration layer governing `OmniPlayer`, `StreamResolver`, and `QueueManager`.
- Modified `OmniShell`'s Next/Prev buttons to call `DependencyContainer.playbackCoordinator.next()`.
- When `PlaybackCoordinator.next()` is invoked, it retrieves the next tracked metadata, resolves a fresh `StreamFormat` URL via `innertube`, and seamlessly initiates `OmniPlayer.play()`.

## Files Created
- `src/jvmMain/kotlin/com/omnitune/windows/domain/playback/QueueManager.kt`
- `src/jvmMain/kotlin/com/omnitune/windows/domain/playback/PlaybackCoordinator.kt`

## Files Modified
- `src/jvmMain/kotlin/com/omnitune/windows/app/DependencyContainer.kt`
- `src/jvmMain/kotlin/com/omnitune/windows/playback/OmniPlayer.kt`
- `src/jvmMain/kotlin/com/omnitune/windows/playback/VlcjOmniPlayer.kt`
- `src/jvmMain/kotlin/com/omnitune/windows/ui/screens/Screens.kt`
- `src/jvmMain/kotlin/com/omnitune/windows/ui/shell/OmniShell.kt`

## Verification
Compilation: PASS
Runtime launch: PASS

## Android Protection Check
Android current status: 4 modified, 1 untracked.
Unexpected changes: None.
Protection result: PASS.

## Git Commit
`b4e28fc` (pending)

## Phase Gate
PASS

## Recommendation
Proceed to Phase 8: First Real End-to-End Vertical Slice. Verify that all systems seamlessly link together to fetch and stream actual audio locally.
