# Left to Do — OmniTune for Windows

## Current State
- Search YouTube Music works, tapping a song plays audio via VLCj
- Play/Pause/Stop, seek slider, system tray minimize
- VLC 3.0.21 at `C:\Program Files\VideoLAN\VLC`
- ANDROID_VR client works for stream URLs (WEB_REMIX/WEB return UNPLAYABLE)

---

## Audio & Playback

- [ ] **Seek on YouTube streams is unreliable** — VLC may not support seeking on all adaptive streams; test edge cases
- [ ] **Proper signatureCipher/cipher deobfuscation** — current cipher URL parsing is naive (works for now, will break when YouTube changes format). Use `NewPipeUtils.getStreamUrl()` or `YoutubeJavaScriptPlayerManager.deobfuscateSignature` properly
- [ ] **Volume slider** — `player.setVolume()` is wired but no UI control
- [ ] **Next/Previous track** — no queue, no autoplay next
- [ ] **Track queue / playlist management**
- [ ] **Repeat / Shuffle** modes

## UI

- [ ] **Album art** — thumbnails from search results not shown in song rows or playback bar
- [ ] **Now-playing view** — dedicated screen showing large album art, full progress, track info
- [ ] **Volume controls** — slider or knob UI
- [ ] **Keyboard shortcuts** — Space = play/pause, arrows = seek, Ctrl+←/→ = prev/next
- [ ] **Search result polish** — show duration, artist, album art in each row
- [ ] **Loading states** — spinner during search, buffering indicator
- [ ] **Error states** — better error display for search/playback failures

## Features

- [ ] **Playlists** — browse, create, play YouTube Music playlists (`YouTube.playlist()` exists)
- [ ] **Lyrics** — betterlyrics/lrclib modules exist but not wired
- [ ] **LastFM scrobbling** — lastfm module wired but not integrated
- [ ] **Kugou lyrics** — kugou module exists, not wired
- [ ] **Canvas / visualizer** — canvas module exists, not wired
- [ ] **Audio device selection** — choose output device
- [ ] **Equalizer** — VLC supports it but not exposed

## Infrastructure

- [ ] **Logging** — replace `println()` with proper logger (SLF4J)
- [ ] **Error handling** — unify error reporting across services
- [ ] **Configuration persistence** — save volume, last playlist, window size
- [ ] **Graceful VLCj shutdown** — `release()` on close to avoid dangling threads
- [ ] **poToken / visitorData refresh** — tokens expire; need refresh logic
- [ ] **YouTube client fallback** — ANDROID_VR may break; test/use multiple clients with proper fallback

## Known Issues

- WEB_REMIX and WEB clients return `UNPLAYABLE - Video unavailable` for player requests. ANDROID_VR works but may have lower bitrate
- VLC process leftover if app killed without calling `release()`
- System tray "Quit" calls `exitApplication()` but VLC native resources may leak

---

## Build / Dependencies

- [ ] **Pin versions** — lock all transitive dependency versions
- [ ] **ProGuard / R8** — not configured for desktop (may be needed for distribution)
- [ ] **Packaging** — `.exe` / `.msi` installer via `compose.desktop.application`
- [ ] **CI** — GitHub Actions for build verification
