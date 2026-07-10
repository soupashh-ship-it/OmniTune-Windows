# Nocturne Prism — Phase 2 Shell Implementation Map

AREA: Window Chrome
CURRENT FILE: `Main.kt`
CURRENT COMPONENT: `Window`
STATE SOURCE: `WindowState`, `SettingsRepository`
REFERENCE TARGET: Deep navy root background, native window controls, constrained minimum size.
REUSE STRATEGY: Use `OmniTuneTheme` and standard `Surface` at the root. Set minimum window dimensions to prevent layout collisions.
RISK: LOW
PLANNED CHANGE: Implemented minimum size (`1024x640`) in `Main.kt`. App is utilizing `BgDeep` via `OmniWindow`.

AREA: Root Layout Structure
CURRENT FILE: `OmniWindow.kt`
CURRENT COMPONENT: `OmniWindow`
STATE SOURCE: `PlayerViewModel`
REFERENCE TARGET: Unified layout avoiding stacked title bars; seamless integration with OS chrome.
REUSE STRATEGY: Standard Column/Row weight-based structural setup.
RISK: HIGH
PLANNED CHANGE: The viewport is structured as a `Column` holding a `Row` (Sidebar + Top Bar/Content Column) and the `OmniBottomPlayer`. Background color for content host is set directly to `BgDeep` to blend with the title bar.

AREA: Sidebar
CURRENT FILE: `Sidebar.kt`
CURRENT COMPONENT: `OmniSidebar`
STATE SOURCE: `PlayerViewModel.navScreen`, `likedCount`
REFERENCE TARGET: 230 px wide, deep indigo surface (`SidebarBackground`), active item highlighting (`Elevated2` fill).
REUSE STRATEGY: Modified existing `Sidebar.kt`.
RISK: MEDIUM
PLANNED CHANGE: Adjusted hardcoded width from `264.dp` to `OmniLayout.sidebarWidth` (`230.dp`). Added correct section header for "Your Playlists" and an Add button icon. Active items now use correct Nocturne Prism semantics.

AREA: Top Bar
CURRENT FILE: `OmniTopBar.kt`
CURRENT COMPONENT: `OmniTopBar`
STATE SOURCE: `PlayerViewModel`
REFERENCE TARGET: Seamless 56-64 px bar with central search box and navigation arrows.
REUSE STRATEGY: Modified existing `OmniTopBar`.
RISK: LOW
PLANNED CHANGE: Set background to `BgDeep` to prevent stacked-chrome visual disconnects. Adjusted padding and constrained the search field to 60% of the viewport width.

AREA: Global Search
CURRENT FILE: `OmniTopBar.kt`
CURRENT COMPONENT: `OmniSearchField`
STATE SOURCE: `PlayerViewModel.search`
REFERENCE TARGET: Ctrl+K focus hint, centered position.
REUSE STRATEGY: Handled via `OmniComponents` and `OmniWindow.kt` keyboard event interception.
RISK: LOW
PLANNED CHANGE: `OmniWindow` successfully intercepts `Ctrl+K` globally to focus the field and invoke `NavScreen.Search`.

AREA: Bottom Player
CURRENT FILE: `OmniBottomPlayer.kt`
CURRENT COMPONENT: `OmniBottomPlayer`
STATE SOURCE: `PlayerViewModel`, `VlcjAudioEngine` (via VM)
REFERENCE TARGET: Persistent 80 px dock spanning full width, split into 3 segments (Info, Transport + Progress, Utilities).
REUSE STRATEGY: Completely refactored.
RISK: HIGH (requires exact UI state binding)
PLANNED CHANGE: Redesigned layout to drop the floating-pill padding and instead use `GlassDefaults.playerDock`. Divided layout into 1:1.5:1 ratio `Row`. Center transport column handles `TimeText` and `OmniProgressSlider` inline below transport controls.
