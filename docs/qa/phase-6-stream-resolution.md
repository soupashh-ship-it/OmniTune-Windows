# Phase 6 — Stream Resolution as a Real Subsystem

Status: COMPLETED

## Scope
Create a dedicated `StreamResolver` backend component to select the highest-quality Opus/M4A audio format dynamically based on explicit codec and bitrate heuristics, replacing the naive `firstOrNull` inline check inside the UI.

## Baseline
Windows path: `D:\Omnitune Windows`
Android reference path: `D:\code\omnitune`

## Investigation Findings
Previously, the `SearchScreen` was blindly extracting the first MP4/WebM string available in the response array inside its click handler.

## Changes Made
- Created `StreamResolver.kt` in `com.omnitune.windows.domain.playback`.
- Implemented `selectBestFormat()` which explicitly filters for audio-only payloads, prioritizes `Opus`, and falls back to `AAC/MP4`, sorting candidates by bitrate to ensure the highest quality stream is selected.
- Wired `SearchController.kt` to delegate to this new resolver.

## Files Created
- `src/jvmMain/kotlin/com/omnitune/windows/domain/playback/StreamResolver.kt`

## Files Modified
- `src/jvmMain/kotlin/com/omnitune/windows/domain/search/SearchController.kt`

## Verification
Compilation: PASS
Runtime launch: PASS

## Android Protection Check
Android current status: 4 modified, 1 untracked.
Unexpected changes: None.
Protection result: PASS.

## Git Commit
`63bd198` (pending)

## Phase Gate
PASS

## Recommendation
Proceed to Phase 7: Rebuild Playback Core Correctly. Update the player queue contract and ensure `Next`/`Prev` genuinely loads the next stream URL.
