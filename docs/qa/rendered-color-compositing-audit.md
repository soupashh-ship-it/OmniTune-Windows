# OmniTune rendered color compositing audit

Date: 2026-07-14

Canonical source: `D:\Ui images for Windows Omnitune`

## Scope

This audit explains why setting `OmniReferenceColors.PlayerBase` to the sampled average player color did not produce the expected final rendered pixels.

The bottom player is not a single flat fill. It is drawn by `TargetBottomPlayerSurface` in `composeApp/src/desktopMain/kotlin/com/omnitune/app/window/OmniBottomPlayer.kt` over the app backdrop and then receives layered radial color overlays, border, artwork, icons, transport controls, seek bar, and volume UI.

## Player layer chain

| Layer | Source value | Alpha | Parent | Final contribution |
|---|---:|---:|---|---|
| App backdrop behind player | `OmniReferenceBackdrop` / window base | opaque | root window | Visible only outside or through rounded edges/translucent border regions |
| Player shape and clipping | Rounded player dock shape | n/a | bottom aligned shell | Defines sampled surface bounds |
| Border | `#181C32` | 0.90 | player surface | Raises edge brightness slightly |
| Canvas base | `OmniReferenceColors.PlayerBase` | opaque | player surface | Dominant empty-surface pixel source |
| Left blue radial | `#12345A` | 0.09 | base | Adds small blue lift on left edge |
| Left violet radial | `#5A1B50`, `#38143C` | 0.15 / 0.09 | base | Adds violet warmth near artwork/left control area |
| Right violet radial | `#432162`, `#281944` | 0.16 / 0.09 | base | Adds right-side violet lift |
| Far-right violet radial | `#302058` | 0.11 | base | Adds subtle far-right bloom |
| Content controls | live artwork, buttons, labels, seek bar | opaque/mixed | over surface | Makes full-band averages much brighter than empty-surface samples |

## Finding

The earlier `PlayerBase = #15142B` matched the approximate whole-band player average from the canonical references, but whole-band averages include bright play buttons, icons, text, progress, and artwork.

When sampling empty player surface pixels, the rendered app was too bright/purple in several regions. Therefore the correct token target is darker than the full-band average.

## Fix

Updated tokens in `OmniReferenceColors`:

- `PlayerBase`: `#15142B` → `#0B1021`
- `PlayerCenter`: `#16152C` → `#11132A`

The existing radial overlays remain because the canonical player uses subtle violet/indigo regional variation rather than one flat dock color.

## Fresh empty-surface samples

Representative post-fix samples:

| Screen | Point | Reference | Actual | Delta |
|---|---:|---:|---:|---:|
| Home | 250,870 | `#0C1021` | `#0C1225` | +0,+2,+4 |
| Home | 500,870 | `#13102C` | `#141126` | +1,+1,-6 |
| Home | 1600,870 | `#0F1029` | `#101228` | +1,+2,-1 |
| Now Playing | 250,870 | `#0C0F22` | `#0C1225` | +0,+3,+3 |
| Now Playing | 500,870 | `#11102C` | `#141126` | +3,+1,-6 |
| Now Playing | 1600,870 | `#0F112C` | `#101228` | +1,+1,-4 |
| Downloads | 250,870 | `#0B1021` | `#0C1225` | +1,+2,+4 |
| Downloads | 500,870 | `#120F2B` | `#141126` | +2,+2,-5 |
| Downloads | 1600,870 | `#0E0F29` | `#101228` | +2,+3,-1 |

## Result

Player empty-surface rendering now matches the canonical references closely enough that remaining player-area RGB error is dominated by dynamic content and controls, not by the base dock color.
