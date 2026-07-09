# Phase 4 — Architecture Recovery

Status: COMPLETED

## Scope
Decouple data fetching, stream resolution, and database instantiation from the Compose UI. Introduce controller classes to manage state cleanly using `StateFlow`, eliminating god classes and direct dependencies inside the view layer.

## Baseline
Windows path: `D:\Omnitune Windows`
Android reference path: `D:\code\omnitune`

## Investigation Findings
Previously, `SearchScreen`, `HomeScreen`, and `LibraryScreen` were directly querying the network (via `YouTube.search()` or `YouTube.home()`) and directly initializing the SQLDelight `DatabaseFactory` within `clickable` modifiers and `LaunchedEffect` blocks. This violated proper separation of concerns and could lead to significant memory/coroutine leaks.

## Changes Made
- Created `DependencyContainer.kt` to globally own the `OmniDatabase` and `OmniPlayer` singletons.
- Implemented `HomeController.kt`, `SearchController.kt`, and `LibraryController.kt` in `com.omnitune.windows.domain.*`.
- Refactored `Screens.kt` so each Composable strictly observes `StateFlow` fields emitted by its respective controller, delegating all heavy lifting and lifecycle scopes accordingly.

## Files Created
- `src/jvmMain/kotlin/com/omnitune/windows/app/DependencyContainer.kt`
- `src/jvmMain/kotlin/com/omnitune/windows/domain/search/HomeController.kt`
- `src/jvmMain/kotlin/com/omnitune/windows/domain/search/SearchController.kt`
- `src/jvmMain/kotlin/com/omnitune/windows/domain/library/LibraryController.kt`

## Files Modified
- `src/jvmMain/kotlin/com/omnitune/windows/ui/screens/Screens.kt`

## Verification
Compilation: PASS
Runtime launch: PASS

## Android Protection Check
Android current status: 4 modified, 1 untracked.
Unexpected changes: None.
Protection result: PASS.

## Git Commit
`0e4b85c` (pending)

## Phase Gate
PASS

## Recommendation
Proceed to Phase 5: Remove Production Sanity-Test Code. Clean up hardcoded test searches in `Main.kt` that trigger automatically on launch.
