# Nocturne Prism — Phase 2 Shell Visual Audit

AREA: Window Chrome
REFERENCE SCREEN(S): All
SEVERITY BEFORE FIX: 1
DIFFERENCE: Custom window decorations missing, relying on OS title bar.
FIX APPLIED: Integrated `BgDeep` to visually merge the app content smoothly with the native title bar. Kept OS decorations to maintain native resize/drag reliability without faking chrome.
SEVERITY AFTER FIX: 1
REMAINING DIFFERENCE: Standard OS title bar instead of custom drawn chrome.
REASON: Safest cross-platform approach for Compose Desktop without breaking native window management.

AREA: Sidebar
REFERENCE SCREEN(S): Image #1, #9
SEVERITY BEFORE FIX: 3
DIFFERENCE: Too wide (264px), incorrect highlight color, missing "Your Playlists" section header logic.
FIX APPLIED: Set width to 230px, applied `Elevated2` and `SurfaceSelected` tokens, added Add playlist icon.
SEVERITY AFTER FIX: 0
REMAINING DIFFERENCE: None structural.
REASON: Matches reference measurements.

AREA: Top Command Bar
REFERENCE SCREEN(S): Image #9, #10
SEVERITY BEFORE FIX: 2
DIFFERENCE: Too tall visually, background color clashed with main content leading to a stacked-bar look.
FIX APPLIED: Aligned background to `BgDeep`, tuned padding, constrained search field width.
SEVERITY AFTER FIX: 0
REMAINING DIFFERENCE: Profile avatar relies on dummy text instead of a real image.
REASON: Avoiding fake data generation; no backend profile data exists.

AREA: Main Content Viewport
REFERENCE SCREEN(S): Image #9
SEVERITY BEFORE FIX: 2
DIFFERENCE: Background was `BgInk` causing slight contrast mismatch with the native chrome and sidebar.
FIX APPLIED: Adjusted to `BgDeep` for seamless integration.
SEVERITY AFTER FIX: 0
REMAINING DIFFERENCE: None.
REASON: Seamless background matches reference aesthetics.

AREA: Bottom Player
REFERENCE SCREEN(S): Image #9, #5
SEVERITY BEFORE FIX: 3
DIFFERENCE: Rendered as a floating rounded pill with margin. Progress bar rendered above the transport buttons instead of below/inline.
FIX APPLIED: Converted to a full-width persistent dock (`80.dp` height, `RectangleShape`). Grouped elements into 3 fixed-ratio columns. Moved progress slider beneath transport controls.
SEVERITY AFTER FIX: 0
REMAINING DIFFERENCE: None.
REASON: Fully aligns with reference specifications and playback state.
