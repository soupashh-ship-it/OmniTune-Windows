# OmniTune Windows canonical UI style extraction

Reference directory: `D:\Ui images for Windows Omnitune`

Generated from direct inspection and pixel sampling of the canonical images. Full sampling output is stored at `docs/qa/canonical-reference-inventory/color-samples.json`.

## Global visual language

The canonical product is a dark navy/indigo glass desktop UI. It is not pure black and not a generic saturated purple/cyan gradient. The shell uses subtle regional variation:

- sidebar average: usually `#101429` to `#13172E`
- top bar average: usually `#020617` to `#0A0E1F`
- content background average: usually `#0F1328` to `#201E3C`
- bottom player average: usually `#15142A` to `#17172C`
- accent family: soft iris/lavender, visually close to `#7D84F7` / `#8085FB`
- selected navigation: layered violet/indigo, not a flat purple block
- player dock: deep navy with a subtle violet center/edge treatment

The references use layered depth:

- dark navy base
- subtle radial blue/violet bloom in content areas
- glass-like cards with low-opacity borders
- muted but readable typography
- soft selected-state illumination
- bottom player separated by elevation and a persistent reservation band

## Sampled regional colors

These are average samples from large stable regions of each canonical reference. They are evidence, not direct one-token replacements for every surface.

| Screen | Sidebar | Top bar | Main/content sample | Content center | Bottom player |
|---|---:|---:|---:|---:|---:|
| Home | `#12152D` | `#0B0E1D` | `#1B1A34` | `#1D173A` | `#17152B` |
| Search & Discovery | `#11142B` | `#020617` | `#1A1831` | `#141632` | `#15142B` |
| Library | `#11152C` | `#090D1B` | `#171829` | `#292437` | `#151429` |
| Playlist Detail | `#11142D` | `#090D1E` | `#141635` | `#191B41` | `#16152B` |
| Artist Detail | `#11152C` | `#080C1C` | `#201E3C` | `#1E1A36` | `#15152C` |
| Album Detail | `#11162D` | `#090E1E` | `#121731` | `#10162F` | `#15162C` |
| Now Playing + Lyrics | `#11152E` | `#0A0E1F` | `#121232` | `#121134` | `#16152C` |
| Downloads & Offline | `#11132C` | `#080C1C` | `#0F1328` | `#080D20` | `#15142A` |
| Queue & Session | `#13172E` | `#0A0E1E` | `#14182D` | `#13162D` | `#17172C` |
| Settings & Personalization | `#101429` | `#0A0E1C` | `#11162A` | `#121729` | `#15152A` |

## Global token implications

Current `OmniReferenceColors` already contains many close values, but future visual work should compare against the canonical samples before changing tokens:

- `WindowBase` / `ContentBase` should remain dark blue-black, not pure black.
- `SidebarBase` should stay around the `#101429`–`#13172E` family.
- `TopBarBase` should stay near `#080C1C` with Search allowing a darker `#020617` top sample.
- `SurfaceBase` should represent deep navy glass, with alpha and overlays used for local card depth.
- `SurfaceRaised` and `SurfaceSelected` should remain subtle; selected states should glow softly rather than flatten.
- `PlayerBase` cannot be set from the average dock sample alone because the final pixel is composited through radial overlays and live controls. Empty-surface player pixels in the canonical references sit closer to `#0B1021`–`#14112D`, while whole-band averages are lifted by controls/artwork.
- Text hierarchy should keep high contrast for primary text, muted cool gray for secondary text, and subdued gray-violet for metadata.

Current implementation note: `OmniReferenceColors.PlayerBase` has been adjusted to `#0B1021` and `PlayerCenter` to `#11132A`. With the existing violet/indigo overlays, fresh empty-surface player samples now land within roughly 1–6 RGB points of the Home, Now Playing, and Downloads canonical references. See `docs/qa/rendered-color-compositing-audit.md`.

Do not collapse all regional colors into one universal dark token. The references show deliberate regional differences.

## Screen extraction

### Home

- Reference: `Image 1.png`
- Dominant background: dark navy/indigo with stronger hero bloom near the center.
- Stable geometry: sidebar, top bar, hero card, Continue Listening rail, Quick Picks, Made for You, Trending, New Releases, bottom player.
- Dynamic regions: artwork, provider titles/artists, queue/listening content, progress.
- Immutable details: hero and content shelves must retain premium layered density; bottom player remains centered.

### Search & Discovery

- Reference: `ChatGPT Image Jul 10, 2026, 03_00_04 PM (2).png`
- Dominant background: `#1A1831` sampled main region with darker `#020617` top bar.
- Stable geometry: search heading, search field, recent chips, category tabs, results table, discovery rail, right-side trending/artists/albums modules.
- Dynamic regions: query text, provider results, artwork, artist/album names.
- Immutable details: Enter/numpad Enter behavior must remain; More/Less must never submit `More` as a query.

### Library

- Reference: `ChatGPT Image Jul 10, 2026, 03_00_04 PM (3).png`
- Dominant background: deep navy with warmer central content sample caused by artwork/shelf color.
- Stable geometry: title, tabs, sort, view toggle, Pinned Collections, Recent Additions, All Songs.
- Dynamic regions: collection artwork, song/album titles, artist names.
- Immutable details: pinned collections and list/grid controls must remain functional, not visual-only.

### Playlist Detail

- Reference: `ChatGPT Image Jul 10, 2026, 03_00_05 PM (4).png`
- Dominant background: dark indigo content area with strong artwork-driven side rail.
- Stable geometry: artwork frame, title block, metadata/action row, track table, right rail panels.
- Dynamic regions: playlist artwork, playlist title, track rows, related playlist contents.
- Immutable details: playback, shuffle, queue actions, downloads, and local queue-created playlists must remain real.

### Artist Detail

- Reference: `ChatGPT Image Jul 10, 2026, 03_00_05 PM (5).png`
- Dominant background: slightly brighter violet/indigo hero region.
- Stable geometry: hero band, identity block, stats card, tab strip, lower columns, right rail modules.
- Dynamic regions: artist image, artist name, provider text, real related rows.
- Immutable details: reference includes external-style stats/social/tour content, but OmniTune must not fabricate followers, socials, monthly listeners, or tour dates. Truthful unavailable states must still occupy polished layout.

### Album Detail

- Reference: `ChatGPT Image Jul 10, 2026, 03_00_06 PM (6).png`
- Dominant background: deep navy with low-brightness center.
- Stable geometry: artwork, album info origin, metadata stack, action row, track list, credits panel, featured/more/fans panels.
- Dynamic regions: album art, album title, track rows, related items.
- Immutable details: producer/studio/engineering credits must not be invented if unavailable.

### Now Playing + Lyrics

- Reference: `ChatGPT Image Jul 10, 2026, 03_00_06 PM (7).png`
- Dominant background: very dark indigo, large left artwork, right glass lyrics panel.
- Stable geometry: Now Playing badge, artwork frame, title/artist block, progress visualization, timeline, transport cluster, lyrics panel, lyrics tabs, lyrics viewport, bottom alignment above player.
- Dynamic regions: artwork content, song title/artist, live progress, lyric words, related rows.
- Immutable details: lyrics panel bounds and typography rhythm are stable; lyric text can differ. Do not fake synced timestamps.

### Downloads & Offline

- Reference: `ChatGPT Image Jul 10, 2026, 03_00_07 PM (10).png`
- Dominant background: darker than most screens, sampled main content around `#0F1328`.
- Stable geometry: title, action buttons, tabs/chips, stat cards, downloaded songs, albums, storage rail, quality rail, download-over rail, auto-download rail.
- Dynamic regions: counts, task rows, file paths, storage bytes, quality state.
- Immutable details: counts/storage/progress must remain real; Smart Offline Mixes must not become fake.

### Queue & Session

- Reference: `ChatGPT Image Jul 10, 2026, 03_00_07 PM (8).png`
- Dominant background: dark navy with compact high-density panels.
- Stable geometry: Up Next, Queue Controls, Session History, Recently Played, Recommendations.
- Dynamic regions: queue rows, history rows, session content, recommendation rows.
- Immutable details: session history must remain persisted/meaningful; queue Save as Playlist must remain real.

### Settings & Personalization

- Reference: `ChatGPT Image Jul 10, 2026, 03_00_07 PM (9).png`
- Dominant background: restrained dark navy with glass cards.
- Stable geometry: Account, Audio Quality, Playback, Appearance, Downloads, Notifications, Shortcuts, About.
- Dynamic regions: current setting values where user-controlled.
- Immutable details: no fake account/lossless/spatial/provider claims. Every visible interactive setting must have real behavior or be informational.

## Dynamic-region policy

Dynamic content may be masked in stable-geometry diffing:

- artwork interiors
- lyrics words
- provider song/album/artist titles
- queue/history/recommendation rows
- live progress
- storage/download counts
- user-selected setting values

Stable content must not be masked:

- panel bounds
- card bounds
- artwork frame bounds
- tab positions
- button geometry
- sidebar/top-bar/player geometry
- text block origin and typography region
- page origins
- right-rail widths
- player reservation

## Current style verdict

The current app is in the same visual family, but future visual fixes must use these canonical images directly. Raw full-screen diffs alone are insufficient; stable geometry, sampled color, and effects must all be considered.
