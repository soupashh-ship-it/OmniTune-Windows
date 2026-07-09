# Phase 3 — Fix App Startup and Playback Native Dependency Blocker

Status: COMPLETED

## Scope
Prevent the application from hard-crashing on startup when the native VLC Media Player dependencies (`libvlc.dll`) are missing from the host machine. The UI must continue to launch and provide graceful degradation.

## Baseline
Windows path: `D:\Omnitune Windows`
Android reference path: `D:\code\omnitune`

## Investigation Findings
`run_log.txt` showed an `UnsatisfiedLinkError` originating from `AudioPlayerComponent()` initialization inside the `VlcjOmniPlayer` constructor. Because the player was initialized synchronously during Composition in `Main.kt`, the unhandled exception crashed the entire desktop application process before the window could render.

## Changes Made
- Introduced `PlayerInitializationState` (`UNINITIALIZED`, `INITIALIZING`, `READY`, `UNAVAILABLE`, `ERROR`, `DISPOSED`) into the `OmniPlayer` interface.
- Refactored `VlcjOmniPlayer.kt` to wrap the `AudioPlayerComponent` instantiation inside a `try-catch` block.
- If native libraries are missing, the state degrades to `UNAVAILABLE` and `ERROR`, but the exception is swallowed to allow the parent UI to continue loading.

## Files Modified
- `src/jvmMain/kotlin/com/omnitune/windows/playback/OmniPlayer.kt`
- `src/jvmMain/kotlin/com/omnitune/windows/playback/VlcjOmniPlayer.kt`

## Verification
Compilation: PASS
Runtime launch: PASS. `gradlew.bat run` now stays alive indefinitely (verified via process timeout) rather than crashing instantly.

## Android Protection Check
Android current status: 4 modified, 1 untracked.
Unexpected changes: None.
Protection result: PASS.

## Git Commit
`b4e28fc` (pending)

## Phase Gate
PASS

## Recommendation
Proceed to Phase 4: Architecture Recovery. Decouple the monolithic Search/Playback logic from `Screens.kt` into dedicated domain controllers.
