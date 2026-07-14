# OmniTune Windows Delta Audit

| Area | Current Implementation | Evidence | Remaining Gap | Priority | Action |
|---|---|---|---|---|---|
| Now Playing / Lyrics | Layout corrected, synced lyrics, manual scroll suppression, high-res artwork implemented. | `NowPlayingView.kt`, `80-percent-product-completion-final.md` | Auto-scroll ignores `OmniMotionPolicy`. Related tab shows Queue instead of real data. | P2 | Fix `animateScrollToItem` to check policy. Fetch `NextResult` in `doPlay` to get `relatedEndpoint` and populate a new `RelatedPanel`. |
| Desktop UX | Space, Left, Right keys work. Bottom player has tooltips. | `OmniWindow.kt`, `OmniBottomPlayer.kt` | Media keys (Play/Pause, Next, Prev) not bound. | P2 | Add `Key.MediaPlayPause`, `Key.MediaNextTrack`, etc. to `OmniWindow` event listener. |
| Playback Stability | Single-thread dispatcher works. Shutdown hook added. | `VlcjAudioEngine.kt`, `PlayerViewModel.kt` | Infinite retry loop if VLC emits `error()` followed by `finished()` on unplayable track with Repeat One. | P0 | Filter `finished()` on error state. Add `consecutiveErrors` to `PlayerViewModel` to auto-skip but bound at 3 failures. |
| Persistence / Library | File-backed JSON implemented. | `SettingsRepository.kt` | Library Pinned Collections (Favorites, Playlists) are visual placeholders with no `onClick`. | P1 | Wire up `onClick` navigation and tab switching to `PinnedCollectionCard`. |
| Downloads / Offline | Persistent tasks, pause/resume, URL refresh implemented. | `OmniDownloadManager.kt` | Corrupt partial files marked as COMPLETED if length > 0. Naive `signatureCipher` parsing breaks URL resolution. | P1 | Check `file.length() == task.totalBytes`. Use `NewPipeUtils.getStreamUrl()`. |
| Browse / Radio | Provider-backed data fetching implemented. | `Screens.kt` | - | - | PASS |
