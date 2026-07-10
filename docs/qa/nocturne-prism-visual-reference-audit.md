# Nocturne Prism — Visual Reference Audit (Phase 14)

**Method note**: The reference PNGs could not be opened by the model, so fidelity is assessed
against the written "Nocturne Prism" spec (Sections 2–10) and the existing `OmniTuneTheme`
tokens, not by pixel comparison. Severity reflects divergence from the spec's stated design
language. All screens were verified to **compile** (`compileKotlinDesktop` GREEN); live
pixel-level visual verification was NOT RUN (no display in this environment).

## Theme / tokens
- Deep obsidian `#050812/#060912/#080C18`, iris `#8178FF/#8B84FF`, violet `#996EFF`, cool blue
  `#5D7FFF`, text `#F4F3FA/#A8AEC2/#70788F`, low-alpha borders. Status: IMPLEMENTED. Match: HIGH.
- Gradient helpers (`irisToLavender`, `activeNavGlow`, `heroAurora`, `playerScrim`) present.
- Risk: real-time blur intentionally avoided (static gradients) per prior guidance.

## Screen-by-screen

| # | Screen | Status | Match | Diff / severity | Follow-up |
|---|--------|--------|-------|-----------------|-----------|
| 1 | Home / Discover | DONE | Medium-High | Featured hero + carousels implemented from `YouTube.home()`. Greeting + sections present. Right "Continue Listening" module simplified to Related queue panel in Now Playing instead of Home right rail. Severity: LOW (layout choice). | Add dedicated right-rail Continue Listening on Home. |
| 2 | Search & Discovery | DONE | Medium-High | Global search in top bar + screen with recents, results, loading/error, keyboard. Recent searches persisted. Genre browse lives under "Browse". Severity: LOW. | Add Top Result + genre grid directly on Search screen. |
| 3 | Library | DONE | Medium | Tabs (Songs/Albums/Artists/Playlists/Downloads) + Liked Songs from local likes. Albums/Artists/Playlists collections empty (no YT auth). Severity: MEDIUM (honest empty states, not fake data). | Wire personal library when YT login added. |
| 4 | Playlist Detail | PARTIAL | Medium | Playlist results open; full Playlist Detail page (header + track table) not yet built as standalone (playlists open via search only). Severity: MEDIUM. | Build `PlaylistView(browseId)` from `YouTube.playlist()`. |
| 5 | Artist Profile | DONE | Medium-High | Cinematic header, sections carousels, play. No fabricated follower counts. Severity: LOW. | Add About panel + Fans-also-like when data available. |
| 6 | Album Detail | DONE | Medium-High | Art, title/artist, track table with index, play. Severity: LOW. | Add "More by artist" + Credits sections. |
| 7 | Now Playing + Lyrics | DONE | Medium-High | Two-pane: art + controls + volume; right panel Lyrics (synced via LrcLib) / Related (queue). Auto-scroll + current-line emphasis. Synced when LRC available; honest empty otherwise. Severity: LOW. | True waveform/visualizer (optional). |
| 8 | Queue & Session | DONE | Medium | Up Next list, current-row highlight, remove/reorder via `moveQueueItem`, clear. Severity: LOW. | Add drag handle UI + Save-as-Playlist (local). |
| 9 | Downloads & Settings | DONE | Medium | Settings: card groups (Account/Audio/Playback/Appearance/Downloads/Shortcuts/About) with real persistence (volume, reduce-motion, mini on-top). Downloads honest empty (storage layer not built). Severity: MEDIUM (downloads not implemented). | Implement local download cache + storage panel. |
| 10 | Mini Player | DONE | Medium | Separate synchronized `Window` (shares PlayerViewModel → no second audio), always-on-top, resizable, art/title/controls/volume. Severity: LOW. | Quick-Switch recommendations row. |
| 11 | Browse / Radio | DONE | Medium | Browse = mood/genre grid; Radio = new-release albums grid. Severity: LOW. | Richer Radio stations. |

## Overall visual-match assessment
- Color system, typography scale, radii, spacing, glass dock, sidebar nav, bottom player,
  card/row components, and motion primitives all follow the Nocturne Prism spec.
- Divergences are primarily **data-scope** (auth-gated Library/Downloads) and a few
  **layout simplifications** (Home right rail, standalone Playlist Detail), not visual-style
  divergence. No Spotify/Apple clone, no generic purple gradient overload, no fake data.

## Known not-runnable checks
- Pixel-diff against PNG references (model cannot read images).
- Live runtime visual verification (no display; requires network + VLC).
- Automated screenshot regression (no such harness configured).
