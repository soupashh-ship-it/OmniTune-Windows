# OmniTune Windows reference-screen QA report

Canonical viewport: 1672 × 941 physical pixels.

This continuation pass produced trustworthy runtime screenshots for all eight remaining target screens through a debug-only route bootstrap controlled by `OMNITUNE_QA_ROUTE`. The bootstrap is disabled in normal runtime when the environment variable is absent.

Final verdict for this pass: **CLOSE BUT MEASURABLE DIFFERENCES REMAIN**.

The screens are runtime-reachable and truthful-data safe, but pixel diffs and visible inspection show remaining layout/style deltas. Therefore this report does not claim full reference lock.

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
| Library | `docs/qa/library-reference-1672x941-final.png` | YES | YES |
| Playlist Detail | `docs/qa/playlist-detail-reference-1672x941-final.png` | YES | YES |
| Artist Detail | `docs/qa/artist-detail-reference-1672x941-final.png` | YES | YES |
| Album Detail | `docs/qa/album-detail-reference-1672x941-final.png` | YES | YES |
| Now Playing + Lyrics | `docs/qa/now-playing-reference-1672x941-final.png` | YES | YES |
| Queue & Session | `docs/qa/queue-session-reference-1672x941-final.png` | YES | YES |
| Settings & Personalization | `docs/qa/settings-reference-1672x941-final.png` | YES | YES |
| Downloads & Offline | `docs/qa/downloads-reference-1672x941-final.png` | YES | YES |

## Pixel diff metrics

Dynamic artwork/text was not masked in this pass, so values are conservative and include expected differences from real data. The output folders contain `overlay.png`, `heatmap.png`, and `metrics.json`.

| Screen | Diff output | Mean abs RGB error | Pixels >20 | Result |
|---|---|---:|---:|---|
| Library | `docs/qa/diff/library/` | 17.10 | 19.38% | Measurable differences |
| Playlist Detail | `docs/qa/diff/playlist-detail/` | 18.18 | 17.50% | Measurable differences |
| Artist Detail | `docs/qa/diff/artist-detail/` | 21.44 | 25.67% | Measurable differences |
| Album Detail | `docs/qa/diff/album-detail/` | 14.97 | 14.13% | Measurable differences |
| Now Playing | `docs/qa/diff/now-playing/` | 14.18 | 14.01% | Measurable differences |
| Queue & Session | `docs/qa/diff/queue-session/` | 13.25 | 10.32% | Closest, still measurable after vertical correction |
| Settings | `docs/qa/diff/settings/` | 11.69 | 8.76% | Closest, still measurable after vertical correction |
| Downloads | `docs/qa/diff/downloads/` | 13.75 | 12.82% | Measurable differences |

## Landmark QA

Tolerance target: ±2 physical pixels where technically practical. Current measurements are based on final screenshots and implemented reference-space coordinates. Several screens intentionally differ in content because fake reference data/capabilities were not reproduced.

### Library

| Landmark | Target | Actual | Delta | Result |
|---|---|---|---:|---|
| Title | x≈294, y≈101 | x≈296, y≈110 | +2/+9 | Y high |
| Tab strip | x≈297, y≈147, w≈584, h≈43 | x≈298, y≈153, w≈582, h≈42 | +1/+6/-2/-1 | Y high |
| Sort | x≈1325, y≈147, w≈189, h≈43 | x≈1326, y≈152, w≈188, h≈43 | +1/+5/-1/0 | Y high |
| View toggle | x≈1533, y≈148, w≈93, h≈42 | x≈1532, y≈152, w≈94, h≈43 | -1/+4/+1/+1 | Y high |
| Pinned shelf | y≈252, h≈150 | y≈260, h≈148 | +8/-2 | Y high |
| Recent shelf | y≈465 | y≈480 | +15 | Needs correction |
| All Songs | y≈636 | y≈665 | +29 | Needs correction |

### Playlist Detail

| Landmark | Target | Actual | Delta | Result |
|---|---|---|---:|---|
| Artwork | x≈298, y≈124, w≈323, h≈328 | x≈296, y≈132, w≈322, h≈320 | -2/+8/-1/-8 | Close but short |
| Info region | x≈644, y≈146 | x≈652, y≈160 | +8/+14 | Needs correction |
| Track list | y≈503 | y≈496 | -7 | Close |
| Right rail | x≈1332, y≈168, w≈305, h≈661 | x≈1334, y≈191, w≈306, h≈617 | +2/+23/+1/-44 | Needs correction |

### Artist Detail

| Landmark | Target | Actual | Delta | Result |
|---|---|---|---:|---|
| Hero | y≈70, bottom≈361 | y≈70, bottom≈370 | 0/+9 | Close |
| Identity | x≈331, y≈134 | x≈334, y≈142 | +3/+8 | Y high |
| Stats card | x≈1333, y≈149, w≈285, h≈200 | x≈1340, y≈161, w≈276, h≈196 | +7/+12/-9/-4 | Needs correction |
| Tabs | y≈366–417 | y≈389–412 | +23/-5 | Needs correction |
| Lower columns | x≈289/854/1307 | x≈296/864/1318 | +7/+10/+11 | Slight right shift |

### Album Detail

| Landmark | Target | Actual | Delta | Result |
|---|---|---|---:|---|
| Artwork | x≈300, y≈113, w≈349, h≈342 | x≈300, y≈108, w≈347, h≈346 | 0/-5/-2/+4 | Close |
| Info | x≈675, y≈118 | x≈686, y≈130 | +11/+12 | Needs correction |
| Track list | x≈296, y≈492 | x≈300, y≈490 | +4/-2 | Close |
| Credits | x≈1219, y≈103, w≈415, h≈276 | x≈1216, y≈107, w≈416, h≈274 | -3/+4/+1/-2 | Close |
| Related panels | x≈1203, y≈509 | x≈1216, y≈517 | +13/+8 | Needs correction |

### Now Playing + Lyrics

| Landmark | Target | Actual | Delta | Result |
|---|---|---|---:|---|
| Badge | x≈297, y≈84 | x≈600, y≈108 | +303/+24 | Major difference |
| Artwork | x≈297, y≈139, right≈917 | x≈548, y≈136, w≈228 | +251/-3 | Major difference |
| Transport | y≈768–825 | y≈695–735 | -73/-90 | Major difference |
| Lyrics panel | x≈983, y≈81, w≈663, h≈751 | x≈1049, y≈148, w≈584, h≈628 | +66/+67/-79/-123 | Needs reconstruction |
| Lyrics behavior | real synced/unsynced | Unsynced lyrics shown truthfully | n/a | Truthful |

### Queue & Session

| Landmark | Target | Actual | Delta | Result |
|---|---|---|---:|---|
| Up Next | x≈299, y≈169, w≈693, h≈506 | x≈299, y≈169, w≈694, h≈488 | 0/0/+1/-18 | Height short |
| Queue Controls | x≈299, y≈685, w≈693, h≈115 | x≈299, y≈685, w≈694, h≈104 | 0/0/+1/-11 | Height short |
| Session History | x≈1002, y≈169, w≈311, h≈376 | x≈1006, y≈169, w≈311, h≈359 | +4/0/0/-17 | Height short |
| Recently Played | x≈1001, y≈555, w≈312, h≈251 | x≈1006, y≈555, w≈311, h≈232 | +5/0/-1/-19 | Height short |
| Recommendations | x≈1323, y≈169, w≈318, h≈641 | x≈1330, y≈169, w≈318, h≈607 | +7/0/0/-34 | Height short |

### Settings

| Landmark | Target | Actual | Delta | Result |
|---|---|---|---:|---|
| Account | x≈301, y≈173, w≈434, h≈249 | x≈299, y≈173, w≈433, h≈249 | -2/0/-1/0 | Pass |
| Audio | x≈750, y≈173, w≈433, h≈249 | x≈754, y≈173, w≈433, h≈249 | +4/0/0/0 | X right |
| Playback | x≈1199, y≈173, w≈432, h≈250 | x≈1210, y≈173, w≈432, h≈249 | +11/0/0/-1 | X right |
| Appearance | x≈301, y≈435, w≈434, h≈257 | x≈299, y≈435, w≈433, h≈257 | -2/0/-1/0 | Pass |
| Downloads | x≈750, y≈435, w≈433, h≈257 | x≈754, y≈435, w≈433, h≈257 | +4/0/0/0 | X right |
| Notifications | x≈1199, y≈435, w≈432, h≈257 | x≈1210, y≈435, w≈432, h≈257 | +11/0/0/0 | X right |
| Shortcuts | x≈301, y≈707, w≈434, h≈120 | x≈299, y≈707, w≈433, h≈120 | -2/0/-1/0 | Pass |
| About | x≈750, y≈707, w≈881, h≈120 | x≈754, y≈707, w≈889, h≈120 | +4/0/+8/0 | X/width delta |
| Fake claims | none | none observed | n/a | Pass |

### Downloads

| Landmark | Target | Actual | Delta | Result |
|---|---|---|---:|---|
| Title | x≈294, y≈95 | x≈181, y≈69 in 1012 responsive; canonical capture top-shifted | n/a | Needs correction |
| Stat cards | y≈214, h≈93 | y≈152, h≈94 in final canonical capture | -62/+1 | Major Y issue |
| Songs panel | y≈340 | y≈309 | -31 | Needs correction |
| Albums panel | y≈575 | y≈532 | -43 | Needs correction |
| Device storage | x≈1337, y≈120, w≈313, h≈267 | x≈1340, y≈70, w≈300, h≈246 | +3/-50/-13/-21 | Top-shifted |
| Download quality | y≈396, h≈190 | y≈330, h≈188 | -66/-2 | Top-shifted |
| Download over | y≈595, h≈116 | y≈535, h≈124 | -60/+8 | Top-shifted |
| Auto-download | y≈720, h≈96 | y≈676, h≈97 | -44/+1 | Top-shifted |
| Real storage data | real filesystem | yes | n/a | Pass truthfulness |

## Responsive QA

| Viewport | Capture | Overlap | Clipping | Notes |
|---|---|---|---|---|
| 1672×941 | all eight final captures | Mixed | Mixed | canonical captures exist |
| 1366×768 | `docs/qa/queue-responsive-1366x768.png` | NO major overlap | NO major clipping | Queue remains usable |
| 1012×643 | `docs/qa/downloads-responsive-1012x643.png` | NO major overlap | Minor right-rail vertical compression | Downloads remains usable with compact density |

## Regression captures

| Screen | Capture | Status |
|---|---|---|
| Home | `docs/qa/home-regression-1672x941-final-continuation.png` | Captured |
| Search & Discovery | `docs/qa/search-regression-1672x941-final-continuation.png` | Captured |
| Bottom player | Visible in all captures | Center composition preserved in captures |

## Truthfulness limitations preserved

These were not faked:

1. persistent session history repository
2. queue save-as-playlist API
3. real album producer/studio credits
4. artist social statistics and tour dates
5. offline smart-mix/download engine metadata
6. lossless/spatial/subscription capabilities

## Known visual differences

1. Now Playing still differs materially from the reference composition; artwork and lyrics panel are too centered/small compared with target.
2. Downloads canonical capture is vertically shifted upward relative to reference and needs a page-level Y correction.
3. Queue major panels are consistently ~20–33 px lower than target.
4. Settings card grid is close but consistently 13–18 px lower and right column is ~11 px right of target.
5. Library lower sections sit too low; All Songs is about 29 px below target.
6. Playlist right rail is too short and starts too low.
7. Artist lower region and tab strip sit too low.
8. Album info and related right panels sit slightly down/right.

## Current verdict

CLOSE BUT MEASURABLE DIFFERENCES REMAIN
