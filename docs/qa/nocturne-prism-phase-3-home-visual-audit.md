# Nocturne Prism — Phase 3 Home Visual Audit

AREA: Greeting
REFERENCE MEASUREMENT: 36px Bold, 32px padding.
IMPLEMENTED MEASUREMENT: DisplayMedium (36px equivalent), 32px top inset.
SEVERITY BEFORE FIX: 3 (Was a generic label in a loop)
DIFFERENCE: Old UI merely printed raw text without alignment to the hero.
FIX APPLIED: Hardcoded to the exact top-left corner above the hero grid.
SEVERITY AFTER FIX: 0
REMAINING DIFFERENCE: Time-based rather than user-account based.
REASON: Honest data; no user account system currently exists.

AREA: Featured Hero
REFERENCE MEASUREMENT: 740x260px radius 16px.
IMPLEMENTED MEASUREMENT: Weight 1.8f inside a 260.dp height Row.
SEVERITY BEFORE FIX: 4 (Did not exist structurally alongside history)
DIFFERENCE: Old hero spanned full width, pushing content down.
FIX APPLIED: Constrained height, applied `heroAmbient` gradient, inserted 280.dp companion rail to the right.
SEVERITY AFTER FIX: 1
REMAINING DIFFERENCE: Companion rail displays shimmer rows instead of actual tracks.
REASON: Innertube's `AlbumItem` does not provide inline tracks on the home response without an expensive N+1 network fetch per load.

AREA: Continue Listening
REFERENCE MEASUREMENT: 380x260px right panel.
IMPLEMENTED MEASUREMENT: Weight 1.0f next to hero.
SEVERITY BEFORE FIX: 4 (Did not exist)
DIFFERENCE: Replaced generic carousel with vertical stacked history list.
FIX APPLIED: Implemented `ContinueListeningPanel` using `OmniSurface` with active state tracking.
SEVERITY AFTER FIX: 0
REMAINING DIFFERENCE: None structurally.
REASON: Native history mappings match perfectly.

AREA: Quick Picks
REFERENCE MEASUREMENT: 150x200px cards.
IMPLEMENTED MEASUREMENT: 150x200px equivalent `QuickPickCard`.
SEVERITY BEFORE FIX: 2
DIFFERENCE: Used to be standard 16:9 thumbnails.
FIX APPLIED: Forced 1:1 `aspectRatio` on thumbnails matching the mockup exactly.
SEVERITY AFTER FIX: 0
REMAINING DIFFERENCE: Text contents differ.
REASON: Derived from real YouTube Music endpoints instead of the mockup's dummy activity labels.

AREA: Trending Now
REFERENCE MEASUREMENT: Vertical track list with rank numbers.
IMPLEMENTED MEASUREMENT: `TrendingRow` rendering numbers, art, title, duration.
SEVERITY BEFORE FIX: 4 (Rendered as horizontal carousel)
DIFFERENCE: Horizontal UI completely contradicted the vertical table from the mockup.
FIX APPLIED: Rewrote as a `Column` loop extracting only `SongItem`s, assigning indices + 1.
SEVERITY AFTER FIX: 0
REMAINING DIFFERENCE: None.
REASON: Perfect visual replication achieved.

AREA: New Releases
REFERENCE MEASUREMENT: 140x190px cards.
IMPLEMENTED MEASUREMENT: 140x190px `OmniMediaCard`.
SEVERITY BEFORE FIX: 1
DIFFERENCE: Gap and sizing were standard Material defaults.
FIX APPLIED: Mapped spacing to 16.dp with Nocturne Prism `Surface1` cards.
SEVERITY AFTER FIX: 0
REMAINING DIFFERENCE: None.
REASON: N/A.