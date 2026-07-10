# Nocturne Prism — Phase 4 Search Implementation Map

AREA: Search Page Root
CURRENT FILE: `composeApp/src/desktopMain/kotlin/com/omnitune/app/window/screens/Screens.kt` (lines 41-110)
CURRENT COMPONENT: `SearchView`
STATE SOURCE: `PlayerViewModel`
REFERENCE TARGET: Multi-column desktop layout with distinct states for Discovery (empty query) and Search Results.
REUSE STRATEGY: Extract `SearchView` into its own file (`SearchView.kt`). Retain `query` hoisting in `OmniWindow.kt`.
RISK: High layout complexity.
PLANNED CHANGE: Create `SearchView.kt`. Implement `LazyColumn` root container. Toggle between `DiscoveryContent` and `ResultsContent` based on query.

AREA: Page Search Field
CURRENT FILE: `Screens.kt`
CURRENT COMPONENT: `OmniSearchField`
STATE SOURCE: `query` state hoisted in `OmniWindow`
REFERENCE TARGET: Large 740px wide, centered field.
REUSE STRATEGY: Extend `OmniSearchField` usage.
RISK: Low.
PLANNED CHANGE: Wrap `OmniSearchField` in a constrained `Box` with max width of 740px.

AREA: Recent Searches
CURRENT FILE: `Screens.kt`
CURRENT COMPONENT: `LazyRow`
STATE SOURCE: `PlayerViewModel.recentSearches`
REFERENCE TARGET: Compact pill chips with a "Clear" action.
REUSE STRATEGY: Improve existing implementation.
RISK: Low.
PLANNED CHANGE: Render as `OmniChip` components. Hook "Clear" to a new `clearRecentSearches` method in `SettingsRepository` and `PlayerViewModel`.

AREA: Explore Genres
CURRENT FILE: None
CURRENT COMPONENT: N/A
STATE SOURCE: `YouTubeService.moodAndGenres()`
REFERENCE TARGET: Pill chips with icons.
REUSE STRATEGY: Implement new section.
RISK: Medium.
PLANNED CHANGE: Fetch moods/genres on load. Render as `OmniChip` horizontal list.

AREA: Trending Panel
CURRENT FILE: None
CURRENT COMPONENT: N/A
STATE SOURCE: Generic recommendations or search fallback.
REFERENCE TARGET: Right-aligned column with compact rows.
REUSE STRATEGY: Use standard row layouts.
RISK: Medium (data might not support percentages).
PLANNED CHANGE: Use popular search terms or generic track recommendations. Omit fake percentage labels.

AREA: Top Result
CURRENT FILE: None
CURRENT COMPONENT: N/A
STATE SOURCE: First result from `PlayerViewModel.searchResults`.
REFERENCE TARGET: 280x200px prominent card.
REUSE STRATEGY: Implement `OmniTopResultCard`.
RISK: Low.
PLANNED CHANGE: Conditionally render `OmniTopResultCard` based on the first item's type.

AREA: Songs Panel
CURRENT FILE: `Screens.kt`
CURRENT COMPONENT: `OmniSongRow`
STATE SOURCE: `searchResults` filtered by `SongItem`.
REFERENCE TARGET: Standard tracklist.
REUSE STRATEGY: Reuse `OmniSongRow`.
RISK: Low.
PLANNED CHANGE: Extract top 4-5 songs into a list next to the Top Result card.

AREA: Artists / Albums / Playlists
CURRENT FILE: `Screens.kt`
CURRENT COMPONENT: `ListItemCard`
STATE SOURCE: `searchResults` filtered by type.
REFERENCE TARGET: Right panel groupings.
REUSE STRATEGY: Replace `ListItemCard` with specific right-panel layouts (36px artist circles, 80px album art).
RISK: Medium.
PLANNED CHANGE: Filter results by type and map them into the 300px right column.

AREA: Discover Something New
CURRENT FILE: None
CURRENT COMPONENT: N/A
STATE SOURCE: Similar to Home page recommendations.
REFERENCE TARGET: Horizontal row of discovery cards.
REUSE STRATEGY: Implement `LazyRow` of `OmniMediaCard`.
RISK: Low.
PLANNED CHANGE: Fetch fallback discovery items if available.
