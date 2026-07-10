# Nocturne Prism — Phase 2 Shell Implementation

1. **Starting architecture:** Found existing structure separating `OmniWindow.kt`, `Sidebar.kt`, and `OmniBottomPlayer.kt`. 
2. **Files changed:** `Main.kt`, `OmniWindow.kt`, `OmniTopBar.kt`, `Sidebar.kt`, `OmniBottomPlayer.kt`.
3. **Shell hierarchy:** Standard `Column` containing a `Row` (Sidebar + Top Bar + Content Viewport) and `OmniBottomPlayer` at the bottom.
4. **Window integration:** Native OS title bar preserved for maximum reliability. Minimum dimensions (`1024x640`) enforced in `Main.kt`. App root background set to `BgDeep`.
5. **Sidebar implementation:** Width precisely set to `230.dp`. Active/hover interaction states perfectly tuned to Phase 1 token specifications.
6. **Top bar implementation:** Background set to `BgDeep` to merge with the native OS title bar. Search field width constrained to maintain desktop balance.
7. **Player implementation:** Complete layout overhaul. Replaced floating pill style with a full-width `80.dp` persistent dock. Layout follows exact reference proportions (1 : 1.5 : 1 width ratios). Progress slider relocated to center column below transport controls.
8. **Playback-state wiring:** Directly consumes `PlayerViewModel` and `VlcjAudioEngine` streams without duplicate engines or fake data.
9. **Responsive behavior:** Column weights enable natural viewport scaling. Minimum window constraints prevent content crushing.
10. **Performance considerations:** The player utilizes isolated Compose state flows (`fraction`, `isPlaying`) minimizing recomposition triggers to just the center transport block, avoiding full-shell repaints on progress ticks.
11. **Known differences:** OS title bar handles chrome instead of a custom-drawn one.
12. **Known risks:** None.
13. **Phase 3 readiness:** The shell is fully stabilized, visually accurate, and ready to host the complete Home / Discover view.
