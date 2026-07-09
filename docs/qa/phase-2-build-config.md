# Phase 2 — Build Configuration and Dependency Truth

Status: COMPLETED

## Scope
Audit `build.gradle.kts` and `libs.versions.toml` to establish a single, coherent source of dependency truth for the Windows project and remove irrelevant platforms.

## Baseline
Windows path: `D:\Omnitune Windows`
Android reference path: `D:\code\omnitune`

## Investigation Findings
The repository contained a `gradle/libs.versions.toml` file copied from the Android project. However, `build.gradle.kts` used hardcoded string dependencies for libraries (Ktor, Coil, SQLDelight). Additionally, the native distributions block targeted `.dmg` and `.deb`, which are unnecessary for a strictly Windows-focused application.

## Changes Made
- Deleted `gradle/libs.versions.toml` to eliminate Android leftovers and ambiguity. `build.gradle.kts` is now the sole source of truth.
- Removed `TargetFormat.Dmg` and `TargetFormat.Deb` from `nativeDistributions` to prevent premature optimization.

## Files Deleted
- `gradle/libs.versions.toml`

## Files Modified
- `build.gradle.kts`

## Verification
Compilation: PASS (`./gradlew compileKotlinJvm` succeeds).
Unit tests: N/A

## Exact Commands Run
- `rm gradle/libs.versions.toml`
- `cmd /c gradlew.bat compileKotlinJvm`
- `git commit -m "build: normalize desktop dependency configuration"`

## Android Protection Check
Android current status: 4 modified, 1 untracked.
Unexpected changes: None.
Protection result: PASS.

## Git Commit
`e9de43c`

## Phase Gate
PASS

## Recommendation
Proceed to Phase 3: Fix App Startup and Playback Native Dependency Blocker. Create graceful initialization states for the `VlcjOmniPlayer` so the app doesn't crash on cold launch.
