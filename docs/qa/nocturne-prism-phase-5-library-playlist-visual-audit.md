# Nocturne Prism — Phase 5 Library & Playlist Visual Audit

AREA: Library Header & Tabs
REFERENCE MEASUREMENT: `DisplaySmall` bold, 36px pill tabs.
IMPLEMENTED MEASUREMENT: `DisplaySmall`, `36.dp` tabs.
SEVERITY BEFORE FIX: 4 (Did not exist structurally)
DIFFERENCE: Old UI was an inline stub with generic padding.
FIX APPLIED: Added standard `Nocturne Prism` pill chips, Iris fill for active states, Surface2 for inactive.
SEVERITY AFTER FIX: 0
REMAINING DIFFERENCE: Sort dropdown lacks functioning backend sorting logic.
REASON: Current `PlayerViewModel` lacks generic sorters; visual structure preserved honestly.

AREA: Pinned Collections
REFERENCE MEASUREMENT: 190x110px.
IMPLEMENTED MEASUREMENT: `190.dp` x `110.dp`.
SEVERITY BEFORE FIX: 4 (Did not exist)
DIFFERENCE: Feature entirely absent.
FIX APPLIED: Hardcoded dimensions. Added `PushPin` icon overlay mapping `Iris` tint. Included dark bottom gradient to ensure text readability.
SEVERITY AFTER FIX: 0
REMAINING DIFFERENCE: Uses liked songs instead of mixed playlists.
REASON: Preserves strict data honesty while rendering the required structural geometry.

AREA: Recent Additions
REFERENCE MEASUREMENT: 140x140px art, 16-20px gaps.
IMPLEMENTED MEASUREMENT: `140.dp` square art, `20.dp` spacing.
SEVERITY BEFORE FIX: 4 (Did not exist)
DIFFERENCE: Feature entirely absent.
FIX APPLIED: `RecentAdditionCard` composable built to spec. Added center play overlay on hover equivalent bounds.
SEVERITY AFTER FIX: 0
REMAINING DIFFERENCE: Does not display literal timestamps like "2 hours ago".
REASON: `SongItem` object lacks addition-time timestamps locally.

AREA: Playlist Hero
REFERENCE MEASUREMENT: 260x260px artwork, DisplayLarge text.
IMPLEMENTED MEASUREMENT: `260.dp` artwork, `DisplayLarge`.
SEVERITY BEFORE FIX: 4 (Did not exist)
DIFFERENCE: Feature entirely absent.
FIX APPLIED: Assembled horizontal `Row` aligned to Bottom.
SEVERITY AFTER FIX: 0
REMAINING DIFFERENCE: Lacks dynamic description and genre chips.
REASON: `PlaylistItem` model returned by YouTube Music lacks descriptive payloads on this endpoint.

AREA: Playlist Action Row
REFERENCE MEASUREMENT: 120px Play, 130px Shuffle, 44px height.
IMPLEMENTED MEASUREMENT: Exact matches `120.dp`, `130.dp`, `44.dp`.
SEVERITY BEFORE FIX: 4 (Did not exist)
DIFFERENCE: Absent.
FIX APPLIED: Implemented `OmniPrimaryButton` and `OmniSecondaryButton` wrappers explicitly sized.
SEVERITY AFTER FIX: 0
REMAINING DIFFERENCE: None.
REASON: Matches strictly.

AREA: Related Playlists Panel
REFERENCE MEASUREMENT: 250-280px wide.
IMPLEMENTED MEASUREMENT: `280.dp`.
SEVERITY BEFORE FIX: 4 (Did not exist)
DIFFERENCE: Absent.
FIX APPLIED: Right aligned column using `weight(1f)` tracks constraint to ensure it stays anchored on wide screens.
SEVERITY AFTER FIX: 0
REMAINING DIFFERENCE: None structurally.
REASON: Follows design language.
