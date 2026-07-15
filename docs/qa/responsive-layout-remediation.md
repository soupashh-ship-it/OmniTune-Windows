# OmniTune Windows Responsive Layout Remediation

Date: 2026-07-15

## Scope

This pass targeted the concrete scaling failure shown in `C:\Users\soupa\Downloads\Omnitune scaling issue` and then checked the same class of defect across major routes at `1187x789`.

## Root causes fixed

### Settings fixed-position canvas

File: `composeApp/src/desktopMain/kotlin/com/omnitune/app/window/SettingsView.kt`

The Settings page used a fixed-height `Box` with absolute `offset` positions and hardcoded card widths/heights for the expanded layout:

- fixed 575dp layout canvas;
- forced three-column layout;
- cards fixed around 171-191dp high;
- row heights fixed around 23-29dp;
- one-line subtitles inside controls.

At `1187x789`, this forced cards into columns that were too narrow and too short. At `1906x1066`, the same fixed composition did not use additional space intelligently.

Fix:

- removed the expanded fixed-position Settings canvas;
- used one adaptive composition path for all widths;
- Settings now chooses 3 / 2 / 1 columns from density-independent available content width;
- bounded the maximum readable Settings content width at fullscreen;
- cards now use `heightIn` and natural content measurement instead of fixed heights;
- Settings rows now reserve enough height for title + description + trailing controls;
- text now wraps/ellipsizes intentionally instead of overlapping controls.

### Artist tab wrapping

File: `composeApp/src/desktopMain/kotlin/com/omnitune/app/window/screens/ArtistView.kt`

The Artist tab row could wrap `Merch` into two lines at `1187x789`.

Fix:

- reduced tab gap slightly;
- used one-line, non-wrapping tab labels with ellipsis fallback.

### Browse genre row edge clipping

File: `composeApp/src/desktopMain/kotlin/com/omnitune/app/window/screens/Screens.kt`

The Browse mood/genre row was a horizontal carousel, but the last chip could sit flush/clipped at the right viewport edge.

Fix:

- added trailing content padding to the `LazyRow` so the last chip can scroll fully into view.

## Safe-area behavior

The shared shell already reserves the bottom-player area in `OmniWindow.kt` by padding the routed content host above the player height. The Settings fix respects this by using scrollable natural-height content rather than a fixed canvas. Last Settings content can scroll above the persistent player.

## Screenshot QA

Reference bug screenshots inspected:

- `C:\Users\soupa\Downloads\Omnitune scaling issue\scrrenshot A.png`
- `C:\Users\soupa\Downloads\Omnitune scaling issue\Screenshot B.png`

New captures:

- `docs/qa/responsive/settings-1187x789-after-home.png`
- `docs/qa/responsive/settings-1187x789-after.png`
- `docs/qa/responsive/settings-1906x1066-after.png`
- `docs/qa/responsive/home-1187x789-check.png`
- `docs/qa/responsive/library-1187x789-check.png`
- `docs/qa/responsive/queue-1187x789-check.png`
- `docs/qa/responsive/downloads-1187x789-check.png`
- `docs/qa/responsive/playlist-1187x789-check.png`
- `docs/qa/responsive/liked-1187x789-recheck.png`
- `docs/qa/responsive/artist-1187x789-after.png`
- `docs/qa/responsive/album-1187x789-check.png`
- `docs/qa/responsive/nowplaying-1187x789-check.png`
- `docs/qa/responsive/search-1187x789-check.png`
- `docs/qa/responsive/browse-1187x789-check.png`
- `docs/qa/responsive/radio-1187x789-check.png`

## Results

Settings at `1187x789`:

- no forced unsafe three-column layout;
- no Settings card child clipping observed;
- no overlapping settings text/control rows observed;
- lower cards are reachable by page scroll;
- bottom player does not hide final Settings content when scrolled.

Settings at `1906x1066`:

- content width is bounded instead of stretching cards across the full main canvas;
- cards retain premium density;
- card content is vertically visible.

Other routes at `1187x789`:

- Home, Library, Queue, Downloads, Playlist, Liked Songs, Album, Now Playing, Search, Browse, and Radio were captured for visual inspection.
- Artist had a verified tab wrapping defect; fixed and recaptured.
- Browse had a verified right-edge carousel padding issue; fixed in code.

## Build and test

Commands run:

- `.\gradlew.bat :composeApp:compileKotlinDesktop`
- `.\gradlew.bat :composeApp:assemble`
- `.\gradlew.bat test`
- `.\gradlew.bat :composeApp:desktopTest`

Desktop test result from XML reports:

- 78 tests
- 0 failures
- 0 errors

## Remaining limitations

- Several older visual screens still contain fixed-position reference-layout code. The captured representative routes did not show the same Settings-class clipping at `1187x789`, so broad rewrites were not performed in this pass.
- QA screenshot capture occasionally returned `:composeApp:run` exit `-1` without a Kotlin stacktrace; rerunning the same route succeeded for Liked Songs. This appears to be capture/runtime helper instability rather than a deterministic route crash.
- DPI scaling at 125% / 150% was audited by code path and density-independent layout usage in the touched areas, but physical DPI-switch captures were not performed in this pass.
