# Nocturne Prism — Phase 3 Home Implementation Map

AREA: Home Root
CURRENT FILE: `composeApp/src/desktopMain/kotlin/com/omnitune/app/window/screens/Screens.kt`
CURRENT COMPONENT: `HomeView`, `HomeContent`
STATE SOURCE: `YouTubeService.home()`, `PlayerViewModel`
REFERENCE TARGET: Complex multi-region layout with explicit top/bottom padding and custom section renderings.
REUSE STRATEGY: Maintain `YouTubeService.home()` data fetch, but completely overhaul `HomeContent`.
FUNCTIONAL DEPENDENCY: YouTube API `HomePage` response.
RISK: High. Mapping generic YouTube Music sections exactly to the mockup sections requires stable section names or heuristics.
PLANNED CHANGE: Rewrite `HomeContent` to explicitly extract and position "Featured", "Quick Picks", "Made for you", "New releases", and "Trending" sections based on the `home.sections` data, rather than a generic vertical loop of carousels.

AREA: Greeting
CURRENT FILE: `Screens.kt`
CURRENT COMPONENT: `greeting()`
STATE SOURCE: Time of day.
REFERENCE TARGET: "Good evening, Alex" style, DisplayMedium bold text, 32px top margin.
REUSE STRATEGY: Keep time-aware generic greeting logic (e.g. "Good evening, Listener").
FUNCTIONAL DEPENDENCY: Local time.
RISK: Low.
PLANNED CHANGE: Adjust padding and typography to match reference specs exactly.

AREA: Featured Hero
CURRENT FILE: `Screens.kt`
CURRENT COMPONENT: `FeaturedHero`
STATE SOURCE: First `AlbumItem` found in home sections.
REFERENCE TARGET: Large 740x260px container, cinematic artwork with dark gradient overlay, Play Now button, and a companion rail on the right side containing track listings.
REUSE STRATEGY: Rewrite to support the companion rail and exact geometry.
FUNCTIONAL DEPENDENCY: Needs a playlist or album object.
RISK: Medium.
PLANNED CHANGE: Extract `OmniFeaturedHero` component. Place artwork on the left with a smooth `heroAmbient` gradient mask. Place companion tracks (if available from data, else generic recent tracks or dummy placeholders if allowed for UI preview) in a rail on the right.

AREA: Continue Listening
CURRENT FILE: None (Currently not separated).
CURRENT COMPONENT: N/A
STATE SOURCE: A history section from `YouTubeService` or local history.
REFERENCE TARGET: Right-side panel parallel to Hero.
REUSE STRATEGY: Extract from "Listen again" or history sections.
FUNCTIONAL DEPENDENCY: Available data.
RISK: High.
PLANNED CHANGE: Implement `OmniContinueListeningPanel`. Map it to the right of the Hero inside a Row. Use actual recently played items.

AREA: Quick Picks
CURRENT FILE: `Screens.kt`
CURRENT COMPONENT: `SectionCarousel`
STATE SOURCE: Section titled "Quick picks".
REFERENCE TARGET: 6-column grid or dense row. 150x200px cards.
REUSE STRATEGY: Build specific `OmniQuickPickCard`.
FUNCTIONAL DEPENDENCY: "Quick picks" section.
RISK: Low.
PLANNED CHANGE: Find "Quick picks" in `home.sections`. Render using a `LazyRow` with 16px gaps and custom 150x150 square artwork cards.

AREA: Made for You
CURRENT FILE: `Screens.kt`
CURRENT COMPONENT: `SectionCarousel`
STATE SOURCE: Section titled "Mixed for you" or similar.
REFERENCE TARGET: 3-column. 180x220px cards.
REUSE STRATEGY: Build `OmniPersonalizedMixCard`.
FUNCTIONAL DEPENDENCY: Availability of personalization data.
RISK: Low.
PLANNED CHANGE: Render personalized mixes with a specific geometry matching the reference.

AREA: Trending Now / Popular
CURRENT FILE: `Screens.kt`
CURRENT COMPONENT: `SectionCarousel`
STATE SOURCE: Trending or generic section.
REFERENCE TARGET: Vertical list / table layout (rank, artwork, title, duration).
REUSE STRATEGY: Use `OmniSongRow`.
FUNCTIONAL DEPENDENCY: Trending songs data.
RISK: Medium.
PLANNED CHANGE: Render as a `Column` of `OmniSongRow`s instead of a horizontal carousel.

AREA: New Releases
CURRENT FILE: `Screens.kt`
CURRENT COMPONENT: `SectionCarousel`
STATE SOURCE: "New releases" section.
REFERENCE TARGET: Horizontal carousel, 140x190px cards.
REUSE STRATEGY: Refine `OmniMediaCard`.
FUNCTIONAL DEPENDENCY: New releases data.
RISK: Low.
PLANNED CHANGE: Apply specific reference dimensions to standard `OmniMediaCard`.
