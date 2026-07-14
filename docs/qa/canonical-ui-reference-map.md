# OmniTune Windows canonical UI reference map

Reference directory: `D:\Ui images for Windows Omnitune`

Date inventoried: 2026-07-14

These images are the primary visual source of truth for OmniTune Windows visual reconstruction. They are read-only QA references and must not be copied into production resources or bundled into installers.

## Inventory

| Reference File | Screen | Actual Route | Main Component | Resolution | Full-screen reference | Partial/component reference | Notes |
|---|---|---|---|---:|---|---|---|
| `Image 1.png` | Home | `NavScreen.Home` | `HomeView` | 1672×941 | YES | NO | Primary Home reference; includes shared sidebar, top bar, and bottom player. |
| `ChatGPT Image Jul 10, 2026, 03_00_04 PM (2).png` | Search & Discovery | `NavScreen.Search` | `SearchView` | 1672×941 | YES | NO | Search page reference with discovery panels, result sections, right rail, sidebar, top bar, and bottom player. |
| `ChatGPT Image Jul 10, 2026, 03_00_04 PM (3).png` | Library | `NavScreen.Library` | `LibraryView` | 1672×941 | YES | NO | Library reference with pinned collections, recent additions, all songs, tabs, sort, and view toggle. |
| `ChatGPT Image Jul 10, 2026, 03_00_05 PM (4).png` | Playlist Detail | `NavScreen.PlaylistDetail` / loaded playlist route | `PlaylistDetailView` | 1672×941 | YES | NO | Playlist detail reference for hero artwork, playlist identity, track table, and right rail. |
| `ChatGPT Image Jul 10, 2026, 03_00_05 PM (5).png` | Artist Detail | `NavScreen.ArtistDetail` / `player.openArtist(id)` | `ArtistView` | 1672×941 | YES | NO | Artist detail reference. Fake external stats/social/tour data from the reference must not be fabricated in app runtime. |
| `ChatGPT Image Jul 10, 2026, 03_00_06 PM (6).png` | Album Detail | `NavScreen.AlbumDetail` / `player.openAlbum(id)` | `AlbumView` | 1672×941 | YES | NO | Album detail reference. Detailed credits/studio data must remain truthful if provider does not expose it. |
| `ChatGPT Image Jul 10, 2026, 03_00_06 PM (7).png` | Now Playing + Lyrics | `NavScreen.NowPlaying` | `NowPlayingView` | 1672×941 | YES | NO | Now Playing flagship reference with left artwork/transport and right lyrics panel. Lyrics words are dynamic; panel geometry is stable. |
| `ChatGPT Image Jul 10, 2026, 03_00_07 PM (10).png` | Downloads & Offline | `NavScreen.Downloads` | `DownloadsView` | 1672×941 | YES | NO | Downloads reference with stats, task sections, storage/quality/policy right rail. Counts and paths are dynamic. |
| `ChatGPT Image Jul 10, 2026, 03_00_07 PM (8).png` | Queue & Session | `NavScreen.Queue` | `QueueView` | 1672×941 | YES | NO | Queue reference with queue list, controls, session history, recently played, and recommendations. Queue rows are dynamic. |
| `ChatGPT Image Jul 10, 2026, 03_00_07 PM (9).png` | Settings & Personalization | `NavScreen.Settings` | `SettingsView` | 1672×941 | YES | NO | Settings reference with account/audio/playback/appearance/download/notifications/shortcuts/about cards. |

## Shared shell references

Every full-screen image also provides shared reference evidence for:

- sidebar geometry, selected state, library subnav, playlist shelf
- top bar back/forward/search/account controls
- bottom player dock, centered transport band, seek/progress bar, volume control
- Nocturne Prism baseline background and shell effects

There are no separate canonical files in this directory for:

- Browse
- Radio
- Mini Player
- isolated bottom-player-only component
- isolated sidebar-only component
- isolated top-bar-only component

Browse, Radio, and Mini Player should therefore preserve current product styling and borrow shared shell/chrome evidence from the ten full-screen references rather than inventing unrelated visual language.

## Authority rule

If a previous written landmark table conflicts with these actual reference images, the image wins. If an old runtime screenshot conflicts with these actual reference images, the canonical image wins.
