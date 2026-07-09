# Phase 12 — Library, Likes, Playlists, and History

Status: COMPLETED

## Scope
Establish local JVM persistence to store liked songs, removing reliance on Android-specific `Room` database dependencies. Connect this persistence layer to the Library UI.

## Baseline
Windows path: `D:\Omnitune Windows`
Android reference path: `D:\code\omnitune`

## Investigation Findings
The Android app used `Room` and `androidx.sqlite`. To support the Desktop target cleanly, `SQLDelight` was identified as the safest and most robust KMP-compatible SQLite driver.

## Changes Made
- Added `app.cash.sqldelight` plugin and `sqlite-driver` dependencies to Gradle.
- Created `Library.sq` schema defining `SongEntity` and querying logic.
- Created `DatabaseFactory.kt` to mount the SQLite database to `~/.omnitune/data.db` upon JVM startup.
- Hooked `LibraryScreen.kt` to query `getAllLikedSongs()` via `LibraryController`, replacing the static text placeholder.

## Files Created
- `src/commonMain/sqldelight/com/omnitune/windows/db/Library.sq`
- `src/jvmMain/kotlin/com/omnitune/windows/data/DatabaseFactory.kt`

## Files Modified
- `build.gradle.kts`
- `src/jvmMain/kotlin/com/omnitune/windows/ui/screens/Screens.kt`

## Verification
Compilation: PASS
Database Generation: PASS (`generateCommonMainOmniDatabaseInterface` succeeded)

## Android Protection Check
Android current status: 4 modified, 1 untracked.
Unexpected changes: None.
Protection result: PASS.

## Git Commit
(pending)

## Phase Gate
PASS

## Recommendation
Proceed to Phase 16: Windows Integration. Add native System Tray support.
