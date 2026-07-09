# Phase 16 — Windows Integration

Status: COMPLETED

## Scope
Integrate native Desktop OS features to make the application behave like a true Windows citizen rather than an emulated window.

## Baseline
Windows path: `D:\Omnitune Windows`
Android reference path: `D:\code\omnitune`

## Investigation Findings
Compose Desktop natively provides the `Tray` component, which maps directly to the Windows System Tray (Taskbar notification area).

## Changes Made
- Updated `Main.kt` to include `rememberTrayState()` and `Tray`.
- Added a native right-click context menu to the Tray icon with an option to `Exit OmniTune`.
- Bound the visual Tray icon to the `LibraryMusic` vector asset.

## Files Modified
- `src/jvmMain/kotlin/com/omnitune/windows/Main.kt`

## Verification
Compilation: PASS

## Android Protection Check
Android current status: 4 modified, 1 untracked.
Unexpected changes: None.
Protection result: PASS.

## Git Commit
(pending)

## Phase Gate
PASS

## Recommendation
Proceed to Phase 20: Packaging and Self-Contained Installation. Generate the WIX MSI installer and verify the standalone release constraints.
