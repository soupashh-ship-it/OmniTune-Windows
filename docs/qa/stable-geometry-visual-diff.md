# OmniTune Windows stable-geometry visual diff

Date: 2026-07-14

Canonical viewport: 1672 × 941 physical pixels.

Canonical visual source: `D:\Ui images for Windows Omnitune`. The reference map is documented in `docs/qa/canonical-ui-reference-map.md`; color/effect extraction is documented in `docs/qa/canonical-ui-style-extraction.md`.

This pass separates stable UI geometry from dynamic provider/runtime content. Raw full-screen RGB diffs remain useful, but they overstate structural mismatch when the screen contains live artwork, provider text, lyrics, queue rows, recommendations, download counts, or current playback progress.

## Method

Tooling:

- config: `docs/qa/stable-geometry-masks.json`
- runner: `scripts/qa/run_stable_geometry_diff.py`
- output root: `docs/qa/diff/stable-geometry/`

For each canonical screen the runner records:

- raw full-screen metrics from the existing premium-completion diff
- masked stable-region metrics
- dynamic mask rectangles
- landmark JSON
- largest stable landmark delta
- overlay and heatmap images with masked dynamic regions darkened

Dynamic masks intentionally exclude the unstable content itself, not the surrounding layout. For example, Now Playing masks the artwork image and lyric glyph content, but still measures the artwork frame, lyrics panel bounds, tabs, player reservation, and transport placement.

## Mask validation

The masks in `docs/qa/stable-geometry-masks.json` were audited against the canonical references and landmark bounds during the local runtime-UX pass.

| Screen | Validation result |
|---|---|
| Library | Masks cover shelf/row artwork and provider strings; title, tabs, shelf bounds, row-region bounds, and player reservation remain measured. |
| Playlist Detail | Artwork interior, title text, track text, and right-rail recommendation content are masked; artwork frame, info region, track-list panel, and right-rail bounds remain measured. |
| Artist Detail | Dynamic hero texture/name/provider content is masked; hero bounds, identity block, stats card, tabs, and lower-column bounds remain measured. |
| Album Detail | Artwork/title/track/credits text and related content are masked; artwork frame, info region, track-list panel, credits panel, and related panel bounds remain measured. |
| Now Playing | Artwork pixels, lyric glyphs, provider footer text, and progress bars are masked; badge, artwork frame, transport, lyrics panel, tabs, viewport, and player reservation remain measured. |
| Queue & Session | Dynamic row content is masked; Up Next, Queue Controls, Session History, Recently Played, Recommendations, and panel geometry remain measured. |
| Settings | Setting labels/values are masked; card bounds and lower-card geometry remain measured. |
| Downloads | Download row values, storage values, and quality labels are masked; page title, stat-card band, song/album panels, Device Storage rail, and Auto-Download geometry remain measured. |

No over-masking was found that hides the primary structural deltas reported in the summary. Masks were not broadened to improve scores.

## Current metrics

Stable metrics were established in this pass; there was no previous stable-mask baseline. The raw “before” values are the current unmasked metrics from `docs/qa/diff/premium-completion/`.

| Screen | Raw mean | Raw >20 | Stable mean | Stable >20 | Largest stable landmark delta | Result |
|---|---:|---:|---:|---:|---:|---|
| Library | 17.62 | 19.43% | 13.46 | 13.17% | 0 px | Stable geometry close; dynamic shelf/row content inflates raw score |
| Playlist Detail | 17.93 | 16.52% | 12.67 | 9.96% | 0 px | Stable geometry close; provider artwork/text remains dynamic |
| Artist Detail | 20.07 | 22.85% | 11.85 | 11.28% | 0 px | Stable geometry close; truthful unavailable metadata differs from reference content |
| Album Detail | 19.41 | 14.95% | 10.68 | 7.60% | 0 px | Stable geometry close; artwork/track/credit content remains dynamic |
| Now Playing | 29.64 | 21.33% | 13.92 | 9.58% | 3 px | Stable chrome remains close; a transport-size experiment was rejected because it worsened current rendered metrics |
| Queue & Session | 14.05 | 9.56% | 11.83 | 7.39% | 0 px | Stable geometry close; queue/history rows remain dynamic |
| Settings | 12.40 | 6.82% | 9.52 | 4.48% | 1 px | Lower Shortcuts/About cards restored to canonical height and Y |
| Downloads & Offline | 12.57 | 10.77% | 12.08 | 9.93% | 1 px | Title/stat stack and Device Storage rail aligned to canonical geometry |

## Interpretation

Raw RGB metrics still show measurable differences and the product should not be described as pixel-perfect. The stable-mask results show that most major page landmarks are already close, while the remaining visible differences are concentrated in:

1. Now Playing transport height and live progress/lyrics content; the current practical fix path is further rendered-screenshot analysis rather than shrinking the play button.
2. Downloads live storage/download values and truthful empty-state content.
3. Settings card content/text differences after restoring canonical lower-card geometry.
4. Bottom-player compositing was traced; empty-surface samples now sit within roughly 1–6 RGB channels of the reference on Home, Now Playing, and Downloads.

## Current verdict

CLOSE WITH SMALLER STABLE-GEOMETRY DIFFERENCES.

The eight canonical screens are not pixel-locked. Stable structural mismatch is materially lower than raw full-screen mismatch, and the remaining differences are now measurable by chrome/layout rather than dynamic provider content alone.
