# Nocturne Prism Global Foundation Correction Map

## 1. Outer Window & Title Bar
- **CURRENT COMPONENT**: `Main.kt` `Window(...)`
- **CURRENT PROBLEM**: Bright white native Windows title bar.
- **REFERENCE TARGET**: Integrated dark chrome, no exterior white frame, custom draggable region.
- **PLANNED CHANGE**: Add `undecorated = true`, `transparent = true`. Build custom title bar within `OmniWindow` using `WindowDraggableArea`.

## 2. Root Frame
- **CURRENT COMPONENT**: `OmniWindow.kt` / `Main.kt`
- **CURRENT PROBLEM**: Needs restrained rounded corners while floating, square when maximized.
- **REFERENCE TARGET**: Dynamic shape based on `windowState.placement`.
- **PLANNED CHANGE**: Read `isMaximized`. Apply `RoundedCornerShape(16.dp)` when floating, `RectangleShape` when maximized. Add subtle violet edge border.

## 3. Global Background
- **CURRENT COMPONENT**: Missing. Screens provide their own opaque fills.
- **CURRENT PROBLEM**: Flat warm dark-purple canvas or missing backdrop.
- **REFERENCE TARGET**: Deep blue-black foundation with cool-blue and iris atmospheric layers.
- **PLANNED CHANGE**: Create `NocturneBackdrop.kt`. Use `NocturneColors` (DeepestBase, Canvas, etc.). Implement 5-layer atmospheric backdrop at root.

## 4. Content Host (Opaque Backgrounds)
- **CURRENT COMPONENT**: Individual screens (`HomeView`, `Search`, etc.)
- **CURRENT PROBLEM**: Solid fills hide the global backdrop.
- **REFERENCE TARGET**: Transparent content over `NocturneBackdrop`.
- **PLANNED CHANGE**: Remove `.background(BgDeep)` or similar from root `Box` or `LazyColumn` in screens.

## 5. Sidebar
- **CURRENT COMPONENT**: `Sidebar.kt`
- **CURRENT PROBLEM**: Flat heavier surface, proportions.
- **REFERENCE TARGET**: Extremely dark obsidian/navy, subtle separation edge, faint atmospheric glow.
- **PLANNED CHANGE**: Apply `sidebarBrush` background, add faint radial glow, ensure right separator line. Adjust width constraints to ~15-16%.

## 6. Top Bar
- **CURRENT COMPONENT**: `OmniTopBar.kt`
- **CURRENT PROBLEM**: Opaque region.
- **REFERENCE TARGET**: Visually merges. Semi-transparent surface over backdrop.
- **PLANNED CHANGE**: Change background to `Color(0xCC020614)`. Adjust height to ~7-8%.

## 7. Bottom Player
- **CURRENT COMPONENT**: `OmniBottomPlayer.kt`
- **CURRENT PROBLEM**: Too thin, not elevated, lacking volume.
- **REFERENCE TARGET**: Elevated, floating, rounded, atmospheric, ~11-12% height.
- **PLANNED CHANGE**: Increase height to ~92.dp. Apply `playerBrush` gradient. Add shadow/glow. Provide padding around it so it floats.

## 8. Root Shell Proportions & Underlap
- **CURRENT COMPONENT**: `OmniWindow.kt`
- **CURRENT PROBLEM**: Player overlaps content, fixed dimensions.
- **REFERENCE TARGET**: BoxWithConstraints based responsive sizing. Content must not be hidden.
- **PLANNED CHANGE**: Use calculated sizes for sidebar, top bar, player. Add `padding(bottom = ...)` to the content region so it doesn't get crushed by the player.

## 9. Motion & Performance
- **CURRENT COMPONENT**: `OmniTuneTheme.kt`, `NocturneBackdrop.kt`
- **CURRENT PROBLEM**: Expensive real-time effects or missing transitions.
- **REFERENCE TARGET**: Restrained timing (Hover: 140-180ms, Pressed: 90-120ms). No global recomposition on playback tick.
- **PLANNED CHANGE**: Keep progress tracking isolated inside `OmniBottomPlayer` and `NowPlayingView`. Use static colors or slow tweens for ambient accents.
