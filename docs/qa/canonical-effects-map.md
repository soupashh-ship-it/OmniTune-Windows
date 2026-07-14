# OmniTune canonical effects map

Date: 2026-07-14

Canonical source: `D:\Ui images for Windows Omnitune`

This map records visual effects observed directly from the canonical references. It is not a license to add generic glow; effects should stay restrained and evidence-based.

| Screen | Region | Effect | Colors | Direction / position | Opacity | Notes |
|---|---|---|---|---|---:|---|
| Global shell | Sidebar | Deep navy glass column | `#101429`–`#13172E` | vertical shell | opaque | Slightly different from content background; not pure black |
| Global shell | Top bar | Very dark translucent/navy band | `#020617`–`#0A0E1F` | horizontal at top | high | Search reference has darkest top sample |
| Global shell | Content base | Navy/indigo regional bloom | `#0F1328`–`#201E3C` | screen-specific center/right | subtle | Bloom differs by screen and should not be collapsed into one flat color |
| Global shell | Cards/panels | Glass surface with soft border | deep navy + low iris edge | local panels | low | Borders are visible but subdued |
| Global shell | Selected navigation | Layered violet/indigo selection | violet/iris over sidebar | row-local | medium | Selected state is illuminated, not a flat saturated rectangle |
| Bottom player | Dock base | Rounded elevated navy dock | empty pixels near `#0B1021`–`#14112D` | bottom band | opaque | Whole-band average is brighter because controls/artwork lift the sample |
| Bottom player | Dock bloom | Blue/violet radial overlays | `#12345A`, `#5A1B50`, `#432162`, `#302058` | left/right radial | 0.09–0.16 | Implemented in `TargetBottomPlayerSurface` |
| Home | Hero area | Artwork-driven dark overlay | mixed artwork + dark veil | main hero | medium | Dynamic artwork content changes raw diff |
| Search | Results/discovery surfaces | Dark glass cards and chips | navy/indigo with iris accents | content grid | low/medium | Do not let genre/action chips submit fake search text |
| Library | Shelves/cards | Glass cards around dynamic artwork | dark navy + low border | rows/shelves | low | Artwork interiors are dynamic, card bounds are stable |
| Playlist Detail | Header/right rail | Artwork-driven depth | dynamic cover + navy panels | left header/right rail | medium | Preserve real playlist behavior |
| Artist Detail | Hero | Large hero bloom/texture | violet/indigo | top band | medium | External stats/socials are not fabricated |
| Album Detail | Credits/related panels | Subtle glass rail | navy panels + border | right side | low | Credits remain truthful unavailable state when provider lacks data |
| Now Playing | Artwork area | Large artwork with rounded frame | dynamic artwork | left column | opaque image | Geometry and radius are stable; image content is dynamic |
| Now Playing | Lyrics panel | Large rounded glass panel | deep navy with soft border | right column | high | Panel bounds/tabs/padding are stable; lyric words dynamic |
| Now Playing | Progress visualization | Truthful progress-derived bars | iris/violet | left lower column | medium | Not fake spectrum data |
| Downloads | Main content | Darker content background | `#0F1328`, `#080D20` | full content/right side | subtle | Download counts/storage values are real, not canonical mock data |
| Downloads | Right rail | Stacked glass panels | deep navy + border | right column | high | Device Storage widened to canonical rail width |
| Queue | Dense panel grid | Compact glass cards | dark navy + border | three-column layout | low | Queue/history/recommendation rows are dynamic |
| Settings | Card grid | Restrained glass cards | `#11162A`/`#121729` family | 3-column grid | low | Lower cards restored to canonical height at 1672×941 |

## Implementation notes

- Do not add cyan/purple blobs unless a specific canonical region shows that bloom.
- Dynamic artwork can create local warmth/brightness; this should not be moved into global tokens.
- Stable geometry must still be measured even when the content inside the region is masked.
- Nocturne Prism remains the canonical baseline theme for this reconstruction pass.
