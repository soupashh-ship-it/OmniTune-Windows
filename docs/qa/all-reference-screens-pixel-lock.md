# OmniTune Windows reference-screen QA report

Canonical viewport: 1672 × 941 physical pixels.

Canonical visual source: `D:\Ui images for Windows Omnitune`.

The canonical source directory has now been inventoried in `docs/qa/canonical-ui-reference-map.md`, with sampled color/effect guidance in `docs/qa/canonical-ui-style-extraction.md`. If older written landmark data conflicts with the actual canonical images, the image wins.

This continuation pass produced trustworthy runtime screenshots for all eight remaining target screens through a debug-only route bootstrap controlled by `OMNITUNE_QA_ROUTE`. The bootstrap is disabled in normal runtime when the environment variable is absent.

Final verdict for this pass: **CLOSE BUT MEASURABLE DIFFERENCES REMAIN**.

The screens are runtime-reachable and truthful-data safe. This pass materially improved Now Playing, refreshed all eight captures, and regenerated pixel metrics. Several remaining differences are caused by real provider-backed artwork/text and by refusing to reproduce fake reference metadata such as artist followers, socials, and tour dates.

## Route map

| Screen | Runtime route/action |
|---|---|
| Library | `NavScreen.Library`; sidebar Library parent now opens Library, chevron toggles expansion |
| Playlist Detail | `player.openPlaylist(id)` using first real loaded `PlaylistItem` from discovery data |
| Artist Detail | `player.openArtist(id)` using first real artist ID from discovery/loaded song artists |
| Album Detail | `player.openAlbum(id)` using first real loaded `AlbumItem` or album metadata from discovery songs |
| Now Playing | Starts playback from first real discovery song when none is active, then navigates to `NavScreen.NowPlaying` |
| Queue | `NavScreen.Queue` |
| Settings | `NavScreen.Settings` |
| Downloads | `NavScreen.Downloads` |

## Runtime captures

| Screen | Capture | Verified 1672×941 | Runtime route verified |
|---|---|---:|---:|
| Library | `docs/qa/premium-completion/library-1672x941.png` | YES | YES |
| Playlist Detail | `docs/qa/premium-completion/playlist-detail-1672x941.png` | YES | YES |
| Artist Detail | `docs/qa/premium-completion/artist-detail-1672x941.png` | YES | YES |
| Album Detail | `docs/qa/premium-completion/album-detail-1672x941.png` | YES | YES |
| Now Playing + Lyrics | `docs/qa/premium-completion/now-playing-1672x941.png` | YES | YES |
| Queue & Session | `docs/qa/premium-completion/queue-session-1672x941.png` | YES | YES |
| Settings & Personalization | `docs/qa/premium-completion/settings-1672x941.png` | YES | YES |
| Downloads & Offline | `docs/qa/premium-completion/downloads-1672x941.png` | YES | YES |

## Pixel diff metrics

Pixel comparison was rerun on July 14, 2026 against the eight supplied reference screenshots. Dynamic artwork/text was not masked in this pass, so values are intentionally conservative and include expected differences from real provider data. The output folders contain `overlay.png`, `heatmap.png`, and `metrics.json`.

| Screen | Diff output | Mean abs RGB error | Pixels >10 | Pixels >20 | Pixels >40 | Result |
|---|---|---:|---:|---:|---:|---|
| Library | `docs/qa/diff/premium-completion/library/` | 17.62 | 26.67% | 19.43% | 12.55% | Improved; not pixel-locked |
| Playlist Detail | `docs/qa/diff/premium-completion/playlist-detail/` | 17.93 | 34.23% | 16.52% | 9.89% | Improved; not pixel-locked |
| Artist Detail | `docs/qa/diff/premium-completion/artist-detail/` | 20.07 | 37.36% | 22.85% | 13.61% | Truthful provider data differs from fake reference metadata |
| Album Detail | `docs/qa/diff/premium-completion/album-detail/` | 19.41 | 20.24% | 14.95% | 11.97% | Roughly unchanged; dynamic content remains |
| Now Playing | `docs/qa/diff/premium-completion/now-playing/` | 29.64 | 32.38% | 21.33% | 16.50% | Still largest raw mismatch; stable geometry close but dynamic artwork/lyrics dominate |
| Queue & Session | `docs/qa/diff/premium-completion/queue-session/` | 14.05 | 17.20% | 9.56% | 6.71% | Mixed: mean rose, high-error pixels fell |
| Settings | `docs/qa/diff/premium-completion/settings/` | 12.40 | 16.96% | 6.82% | 5.11% | Lower cards now match canonical geometry; raw text/content still differs |
| Downloads | `docs/qa/diff/premium-completion/downloads/` | 12.57 | 20.30% | 10.77% | 6.59% | Stable geometry improved; truthful empty/storage state differs from mock reference content |

## Stable-geometry diff metrics

This pass added stable-region diffing so raw provider/content differences do not get confused with layout/chrome differences. Dynamic masks are defined in `docs/qa/stable-geometry-masks.json`; output is under `docs/qa/diff/stable-geometry/`; methodology is documented in `docs/qa/stable-geometry-visual-diff.md`.

Stable metrics were established in this pass, so there is no earlier stable-mask baseline. Raw metrics remain the conservative full-screen values above.

| Screen | Raw mean | Raw >20 | Stable mean | Stable >20 | Largest stable landmark delta | Result |
|---|---:|---:|---:|---:|---:|---|
| Library | 17.62 | 19.43% | 13.46 | 13.17% | 0 px | Stable geometry close |
| Playlist Detail | 17.93 | 16.52% | 12.67 | 9.96% | 0 px | Stable geometry close |
| Artist Detail | 20.07 | 22.85% | 11.85 | 11.28% | 0 px | Stable geometry close; dynamic truthful data differs |
| Album Detail | 19.41 | 14.95% | 10.68 | 7.60% | 0 px | Stable geometry close |
| Now Playing | 29.64 | 21.33% | 13.92 | 9.58% | 3 px | Stable chrome close; a transport-size experiment was rejected because it worsened current rendered metrics |
| Queue & Session | 14.05 | 9.56% | 11.83 | 7.39% | 0 px | Stable geometry close |
| Settings | 12.40 | 6.82% | 9.52 | 4.48% | 1 px | Lower cards restored to canonical height/Y |
| Downloads | 12.57 | 10.77% | 12.08 | 9.93% | 1 px | Title/stat stack and Device Storage rail aligned |

## Landmark QA

Tolerance target: ±2 physical pixels where technically practical. Current measurements are based on final screenshots and implemented reference-space coordinates. Several screens intentionally differ in content because fake reference data/capabilities were not reproduced.

### Library

| Landmark | Target | Actual | Delta | Result |
|---|---|---|---:|---|
| Title | x≈294, y≈101 | x≈294, y≈101 | 0/0 | Pass |
| Tab strip | x≈297, y≈147, w≈584, h≈43 | x≈297, y≈147, w≈584, h≈43 | 0/0/0/0 | Pass |
| Sort | x≈1325, y≈147, w≈189, h≈43 | x≈1325, y≈147, w≈189, h≈43 | 0/0/0/0 | Pass |
| View toggle | x≈1533, y≈148, w≈93, h≈42 | x≈1533, y≈148, w≈93, h≈42 | 0/0/0/0 | Pass |
| Pinned shelf | y≈252, h≈150 | y≈252, h≈150 | 0/0 | Pass |
| Recent shelf | y≈465 | y≈465 | 0 | Pass |
| All Songs | y≈636 | y≈636 | 0 | Pass |

### Playlist Detail

| Landmark | Target | Actual | Delta | Result |
|---|---|---|---:|---|
| Artwork | x≈298, y≈124, w≈323, h≈328 | x≈298, y≈124, w≈323, h≈328 | 0/0/0/0 | Pass |
| Info region | x≈644, y≈146 | x≈644, y≈146 | 0/0 | Pass |
| Track list | y≈503 | y≈503 | 0 | Pass |
| Right rail | x≈1332, y≈168, w≈305, h≈661 | x≈1332, y≈168, w≈305, h≈661 | 0/0/0/0 | Pass |

### Artist Detail

| Landmark | Target | Actual | Delta | Result |
|---|---|---|---:|---|
| Hero | y≈70, bottom≈361 | y≈70, bottom≈361 | 0/0 | Pass |
| Identity | x≈331, y≈134 | x≈331, y≈134 | 0/0 | Pass |
| Stats card | x≈1333, y≈149, w≈285, h≈200 | x≈1333, y≈149, w≈285, h≈200 | 0/0/0/0 | Pass |
| Tabs | y≈366–417 | y≈366–417 | 0/0 | Pass |
| Lower columns | x≈289/854/1307 | x≈289/854/1307 | 0/0/0 | Pass |

### Album Detail

| Landmark | Target | Actual | Delta | Result |
|---|---|---|---:|---|
| Artwork | x≈300, y≈113, w≈349, h≈342 | x≈300, y≈113, w≈349, h≈342 | 0/0/0/0 | Pass |
| Info | x≈675, y≈118 | x≈675, y≈118 | 0/0 | Pass |
| Track list | x≈296, y≈492 | x≈296, y≈492 | 0/0 | Pass |
| Credits | x≈1219, y≈103, w≈415, h≈276 | x≈1219, y≈103, w≈415, h≈276 | 0/0/0/0 | Pass |
| Related panels | x≈1203, y≈509 | x≈1203, y≈509 | 0/0 | Pass |

### Now Playing + Lyrics

| Landmark | Target | Actual | Delta | Result |
|---|---|---|---:|---|
| Badge | x≈297, y≈84 | x≈299, y≈84 | +2/0 | Pass |
| Artwork | x≈297, y≈139, right≈917 | x≈299, y≈141, right≈920 | +2/+2/+3 | Close |
| Transport | y≈768–825 | y≈768–828 | 0/+3 | Close; remaining difference is control-band height |
| Lyrics panel | x≈983, y≈81, w≈663, h≈751 | x≈983, y≈81, w≈663, h≈751 | 0/0/0/0 | Pass |
| Lyrics behavior | real synced/unsynced | Unsynced lyrics shown truthfully | n/a | Truthful |

### Queue & Session

| Landmark | Target | Actual | Delta | Result |
|---|---|---|---:|---|
| Up Next | x≈299, y≈169, w≈693, h≈506 | x≈299, y≈169, w≈693, h≈506 | 0/0/0/0 | Pass |
| Queue Controls | x≈299, y≈685, w≈693, h≈115 | x≈299, y≈685, w≈693, h≈115 | 0/0/0/0 | Pass |
| Session History | x≈1002, y≈169, w≈311, h≈376 | x≈1002, y≈169, w≈311, h≈376 | 0/0/0/0 | Pass |
| Recently Played | x≈1001, y≈555, w≈312, h≈251 | x≈1001, y≈555, w≈312, h≈251 | 0/0/0/0 | Pass |
| Recommendations | x≈1323, y≈169, w≈318, h≈641 | x≈1323, y≈169, w≈318, h≈641 | 0/0/0/0 | Pass |

### Settings

| Landmark | Target | Actual | Delta | Result |
|---|---|---|---:|---|
| Account | x≈301, y≈173, w≈434, h≈249 | x≈301, y≈173, w≈434, h≈249 | 0/0/0/0 | Pass |
| Audio | x≈750, y≈173, w≈433, h≈249 | x≈750, y≈173, w≈433, h≈249 | 0/0/0/0 | Pass |
| Playback | x≈1199, y≈173, w≈432, h≈250 | x≈1199, y≈173, w≈432, h≈250 | 0/0/0/0 | Pass |
| Appearance | x≈301, y≈435, w≈434, h≈257 | x≈301, y≈435, w≈434, h≈257 | 0/0/0/0 | Pass |
| Downloads | x≈750, y≈435, w≈433, h≈257 | x≈750, y≈435, w≈433, h≈257 | 0/0/0/0 | Pass |
| Notifications | x≈1199, y≈435, w≈432, h≈257 | x≈1199, y≈435, w≈432, h≈257 | 0/0/0/0 | Pass |
| Shortcuts | x≈301, y≈707, w≈434, h≈120 | x≈301, y≈707, w≈433, h≈120 | 0/0/-1/0 | Pass |
| About | x≈750, y≈707, w≈881, h≈120 | x≈750, y≈707, w≈881, h≈120 | 0/0/0/0 | Pass |
| Fake claims | none | none observed | n/a | Pass |

### Downloads

| Landmark | Target | Actual | Delta | Result |
|---|---|---|---:|---|
| Title | x≈294, y≈95 | x≈295, y≈95 | +1/0 | Pass |
| Stat cards | y≈214, h≈93 | y≈215, h≈93 | +1/0 | Pass |
| Songs panel | y≈340 | y≈340 | 0 | Pass |
| Albums panel | y≈575 | y≈575 | 0 | Pass |
| Device storage | x≈1337, y≈120, w≈313, h≈267 | x≈1337, y≈120, w≈313, h≈267 | 0/0/0/0 | Pass |
| Download quality | y≈396, h≈190 | y≈397, h≈190 | +1/0 | Pass |
| Download over | y≈595, h≈116 | y≈598, h≈124 | +3/+8 | Close |
| Auto-download | y≈720, h≈96 | y≈721, h≈96 | +1/0 | Pass |
| Real storage data | real filesystem | yes | n/a | Pass truthfulness |

## Responsive QA

| Viewport | Capture | Overlap | Clipping | Notes |
|---|---|---|---|---|
| 1672×941 | all eight final captures | NO major overlap observed in refreshed captures | Dynamic content differences remain | canonical captures refreshed |
| 1366×768 | `docs/qa/premium-completion/responsive/*-1366x768.png` | NO major overlap observed in spot checks | NO major clipping observed in spot checks | Home/Search/Library/Now Playing/Settings/Downloads refreshed |
| 1012×643 | `docs/qa/premium-completion/responsive/*-1012x643.png` | NO app-breaking overlap observed | Downloads right rail freshly recaptured; dense but readable/reachable, no layout change retained | Home/Search/Library/Now Playing/Settings/Downloads have prior coverage; Downloads was freshly recertified |

### Downloads responsive recertification

Fresh Downloads captures were generated during the local desktop-UX pass:

- `docs/qa/premium-completion/responsive/downloads-1672x941.png`
- `docs/qa/premium-completion/responsive/downloads-1366x768.png`
- `docs/qa/premium-completion/responsive/downloads-1012x643.png`

At 1012×643 the right rail is dense, but the primary download content remains readable, the rail controls remain reachable, and the bottom player does not collide with the content. No responsive layout change was retained because the fresh evidence did not show app-breaking clipping or inaccessible controls.

## Regression captures

| Screen | Capture | Status |
|---|---|---|
| Home | `docs/qa/premium-completion/home-regression-1672x941.png` | Captured |
| Search & Discovery | `docs/qa/premium-completion/search-regression-1672x941.png` | Captured |
| Bottom player | Visible in all captures | Center composition preserved in captures |

## Four-theme capture sweep

Fresh captures were generated for Nocturne, Midnight, Dusk, and Aurora across Home, Search, Library, Now Playing, Settings, and Downloads:

- `docs/qa/premium-completion/themes/nocturne-*-1672x941.png`
- `docs/qa/premium-completion/themes/midnight-*-1672x941.png`
- `docs/qa/premium-completion/themes/dusk-*-1672x941.png`
- `docs/qa/premium-completion/themes/aurora-*-1672x941.png`

Spot checks covered Aurora Now Playing, Dusk Settings, and Midnight Downloads. No unreadable text or major layout break was observed in those checked captures. This is a visual capture sweep, not a full automated contrast certification.

## Truthfulness limitations preserved

These were not faked:

1. real album producer/studio credits when the provider does not expose them
2. artist social statistics and tour dates
3. Smart Offline Mixes, which remain truthfully unsupported instead of fake
4. lossless/spatial/subscription capabilities

## Known visual differences

1. Now Playing remains the largest measured mismatch by mean absolute RGB error.
2. Artist and Playlist pages still have high unmasked percentage differences, partly due provider-backed dynamic content and partly due remaining visual composition differences.
3. Landmark tables reflect implemented reference-space coordinates, but screenshot-level pixel comparison shows the pages are not fully reference-locked.
4. No broad masks were applied; the current diff metrics are conservative.
5. Now Playing was improved by expanding the library section on that route, adding a richer truthful lyrics panel treatment, adding a truthful synced/unsynced footer, increasing progress-derived visualization density, and lowering the transport cluster.
6. This pass traced the player compositing chain and corrected the base/center tokens so empty player-surface samples are within roughly 1–6 RGB points of the references on Home, Now Playing, and Downloads.

## Current verdict

CLOSE BUT MEASURABLE DIFFERENCES REMAIN
