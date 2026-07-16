# OmniTune Windows Full Project Issue Ledger

Audit date: 2026-07-16  
Scope: verification/reporting only. No production remediation performed.

## Issue counts

| Severity | Count |
|---|---:|
| CRITICAL | 0 |
| HIGH | 3 |
| MEDIUM | 15 |
| LOW | 9 |
| INFORMATIONAL | 7 |

| Classification | Count |
|---|---:|
| CONFIRMED DEFECT | 4 |
| HIGH-CONFIDENCE RISK | 8 |
| POSSIBLE ISSUE | 2 |
| TECHNICAL DEBT | 7 |
| PRODUCT GAP | 9 |
| IMPROVEMENT | 4 |

## REL-001

Category: Release engineering  
Title: Installed-version confusion remains a release blocker  
Classification: PRODUCT GAP  
Severity: HIGH  
Confidence: HIGH

Affected files:

- `composeApp/build.gradle.kts`
- `gradle.properties`
- GitHub release/distribution process

Affected symbols:

- `omnitune.version`
- `nativeDistributions`
- Windows shortcut/install identity

Evidence:

- Current source version is `omnitune.version=0.2.0`.
- Build packaging uses `packageName = "OmniTune"` and a stable `upgradeUuid`.
- Previous user-installed environment had multiple app identities/locations and launched older 0.1.4-era binaries.
- No source evidence of a native auto-updater or installed-version cleanup path exists.

Reproduction steps:

1. Install an older OmniTune build.
2. Install or download 0.2.0 beta.
3. Launch through an old shortcut or stale install path.
4. Observe that the user may run an old executable while believing current source fixes are installed.

Expected result:

- A user should have one obvious installed OmniTune identity and launch the newest intended build.

Actual result:

- Release/install workflow can leave stale binaries and shortcuts.

Root cause:

- Packaging exists, but upgrade/uninstall identity, update discovery, and installed smoke validation are not yet productized.

User impact:

- Users can install a release and still see old behavior, undermining trust.

Engineering impact:

- Bug reports become ambiguous because source HEAD, GitHub release, and installed executable can differ.

Recommended fix:

- Add installed-app smoke checks to release flow.
- Make version display prominent in Settings/About and installer metadata.
- Publish stable releases distinctly from prereleases.
- Validate upgrade from older identities and document uninstall cleanup for legacy packages.
- Add a future native updater or explicit update flow.

Regression risk: MEDIUM  
Estimated effort: M  
Dependencies: release pipeline, installer validation VM  
Test strategy: clean VM install, old-version upgrade install, shortcut target verification, Settings version verification  
Status: OPEN

## REL-002

Category: Release engineering / Windows trust  
Title: Windows installer is unsigned  
Classification: PRODUCT GAP  
Severity: HIGH  
Confidence: HIGH

Affected files:

- `composeApp/build.gradle.kts`
- release pipeline

Affected symbols:

- Windows native distribution configuration

Evidence:

- Packaging creates MSI/EXE, but no signing configuration or signing pipeline was found.

Reproduction steps:

1. Build or download the installer.
2. Inspect signature/SmartScreen behavior on Windows.

Expected result:

- Public Windows builds should be code signed.

Actual result:

- No code-signing evidence in build configuration.

Root cause:

- Signing pipeline and certificate management are not implemented.

User impact:

- SmartScreen and trust warnings make the app feel unsafe.

Engineering impact:

- Public distribution remains beta-grade.

Recommended fix:

- Add code-signing certificate workflow.
- Sign EXE/MSI artifacts in CI/release scripts.
- Verify signature in release checklist.

Regression risk: LOW  
Estimated effort: M  
Dependencies: certificate/provider choice  
Test strategy: signature inspection and clean-machine install  
Status: OPEN

## REL-003

Category: Build and packaging  
Title: Packaging depends on local VLC path unless environment is prepared  
Classification: HIGH-CONFIDENCE RISK  
Severity: MEDIUM  
Confidence: HIGH

Affected files:

- `composeApp/build.gradle.kts`

Affected symbols:

- `bundledVlcHome`
- `prepareWindowsAppResources`

Evidence:

- `bundledVlcHome` defaults to `C:/Program Files/VideoLAN/VLC`.
- `prepareWindowsAppResources` requires `libvlc.dll`, `libvlccore.dll`, and `plugins` at that path or `VLC_HOME`.

Reproduction steps:

1. Build release packaging on a machine without VLC installed and without `VLC_HOME`.
2. Run packaging task.

Expected result:

- Release packaging should be reproducible from repository-controlled dependencies or documented CI provisioning.

Actual result:

- Packaging depends on local machine state.

Root cause:

- VLC redistributable is copied from a local installation.

User impact:

- Not directly user-facing unless release build is incomplete.

Engineering impact:

- Release reproducibility is weak.

Recommended fix:

- Add a documented VLC runtime provisioning step or checked release artifact source.
- Add CI verification that packaged VLC resources exist.
- Keep legal/license notices aligned with the bundled runtime.

Regression risk: LOW  
Estimated effort: M  
Dependencies: VLC redistribution decision  
Test strategy: packaging in clean VM/CI agent without system VLC  
Status: OPEN

## PLAYBACK-001

Category: Playback lifecycle  
Title: `VlcjAudioEngine.release()` blocks the caller with `runBlocking`  
Classification: HIGH-CONFIDENCE RISK  
Severity: MEDIUM  
Confidence: HIGH

Affected files:

- `composeApp/src/desktopMain/kotlin/com/omnitune/app/platform/VlcjAudioEngine.kt`

Affected symbols:

- `VlcjAudioEngine.release`
- `releaseSync`
- `vlcDispatcher`

Evidence:

- `release()` calls `runBlocking { withContext(vlcDispatcher) { releaseSync() } }`.
- `releaseSync()` stops VLC, releases player/factory, updates state, and closes dispatcher.
- If release is called from a UI or shutdown-sensitive path, native release can block that caller.

Reproduction steps:

1. Start playback.
2. Close the app during native media activity.
3. Observe shutdown responsiveness under slow VLC release conditions.

Expected result:

- Shutdown should release resources safely without freezing UI indefinitely.

Actual result:

- Source structure allows caller blocking until VLC release completes.

Root cause:

- Synchronous native release path is exposed through `runBlocking`.

User impact:

- Potential shutdown hang or perceived freeze.

Engineering impact:

- Harder to reason about lifecycle and dispatcher closure.

Recommended fix:

- Introduce an explicit suspend release path and a bounded shutdown path.
- Keep shutdown hook release synchronous only where necessary.
- Add a timeout/failure-safe release test around fake media engine abstraction if practical.

Regression risk: HIGH  
Estimated effort: M  
Dependencies: playback lifecycle tests  
Test strategy: playback close/shutdown smoke, fake engine release delay test  
Status: OPEN

## PLAYBACK-002

Category: Playback correctness  
Title: Stale async playback-resolution protection is present but lacks direct controller-level regression test evidence  
Classification: IMPROVEMENT  
Severity: MEDIUM  
Confidence: HIGH

Affected files:

- `PlayerViewModel.kt`
- `PlaybackRequestGate.kt`
- `composeApp/src/desktopTest/kotlin`

Affected symbols:

- `PlaybackRequestGate`
- playback stream/source resolution path

Evidence:

- Source-level gate exists and validates request token plus current song identity.
- No direct test named for stale playback resolution was found in the current desktop test list.

Reproduction steps:

1. Use fake resolver returning Track A after Track B.
2. Trigger A then B quickly.
3. Assert A cannot start after B.

Expected result:

- A dedicated test should prevent regression of this high-risk race.

Actual result:

- Protection exists, but regression evidence is indirect.

Root cause:

- Playback orchestration is still difficult to isolate from `PlayerViewModel`.

User impact:

- Current source appears protected; future regressions would be high impact.

Engineering impact:

- Race protection depends on source review rather than targeted test.

Recommended fix:

- Add a fake resolver/audio-engine test around request ordering.

Regression risk: MEDIUM  
Estimated effort: M  
Dependencies: test seam around playback resolver/audio engine  
Test strategy: delayed A/B resolution test  
Status: OPEN

## QUEUE-001

Category: Queue semantics  
Title: Shuffle next/previous is random each call and does not preserve a deterministic shuffled order  
Classification: PRODUCT GAP  
Severity: MEDIUM  
Confidence: HIGH

Affected files:

- `PlayerQueueController.kt`

Affected symbols:

- `nextIndex`
- `previousIndex`
- `shuffleState`

Evidence:

- In shuffle mode, `nextIndex()` picks a random index different from current.
- `previousIndex()` also picks a random index different from current.
- There is no persisted shuffled order or back-stack.

Reproduction steps:

1. Enable shuffle with a queue of several songs.
2. Press Next several times.
3. Press Previous.

Expected result:

- Premium music apps usually preserve a shuffled sequence so Previous returns to the prior played item.

Actual result:

- Previous can choose a random different item.

Root cause:

- Shuffle mode is implemented as random index selection rather than a shuffled queue/history.

User impact:

- Previous/Next feels inconsistent in shuffle mode.

Engineering impact:

- Queue invariants remain simple, but product behavior is weaker.

Recommended fix:

- Introduce a shuffled order and played-history cursor.
- Add queue tests for next/previous shuffle semantics.

Regression risk: MEDIUM  
Estimated effort: M  
Dependencies: queue behavior decision  
Test strategy: deterministic random/fixed shuffled order tests  
Status: OPEN

## DOWNLOAD-001

Category: Downloads / offline  
Title: Download lifecycle is strong, but installed offline proof remains unverified  
Classification: PRODUCT GAP  
Severity: MEDIUM  
Confidence: HIGH

Affected files:

- `OmniDownloadManager.kt`
- `NativeRuntime.kt`
- release QA process

Affected symbols:

- `completedDownloadFor`
- `resolveVlcRuntime`
- installed distribution

Evidence:

- Source has managed-root validation and local-first lookup.
- No retained evidence in current audit proves a clean installed app can play downloaded files offline without system Java/VLC/network.

Reproduction steps:

1. Install OmniTune on a clean Windows machine/VM.
2. Download a track.
3. Disconnect network.
4. Restart app and play the downloaded track.

Expected result:

- Offline playback works from installed shortcut with bundled runtime.

Actual result:

- Not proven in this audit.

Root cause:

- Test suite covers parts of this flow but not a physical installed offline scenario.

User impact:

- Offline reliability remains unproven for real users.

Engineering impact:

- Release confidence gap.

Recommended fix:

- Add manual/automated installed-app offline proof to release checklist.

Regression risk: LOW  
Estimated effort: M  
Dependencies: clean VM or physical test machine  
Test strategy: installed-app offline smoke  
Status: OPEN

## DOWNLOAD-002

Category: Downloads / networking  
Title: Download HTTP response handling does not explicitly reject non-success response codes before streaming  
Classification: POSSIBLE ISSUE  
Severity: MEDIUM  
Confidence: MEDIUM

Affected files:

- `OmniDownloadManager.kt`

Affected symbols:

- `download`

Evidence:

- Code reads `connection.responseCode` to determine range support.
- It then proceeds to `connection.inputStream.use` without an explicit accepted-code check in the inspected block.
- Some `HttpURLConnection` failures throw when reading `inputStream`, but explicit user-facing classification would be cleaner.

Reproduction steps:

1. Use a fake resolver returning a URL that responds 403/404/500.
2. Enqueue a download.
3. Inspect final state and error message.

Expected result:

- Non-2xx/non-206 responses should become clear download failures.

Actual result:

- Likely fails through `inputStream`, but handling is implicit.

Root cause:

- Response code is used for range logic, not explicit status validation.

User impact:

- Potential vague download errors.

Engineering impact:

- Harder provider/network failure diagnosis.

Recommended fix:

- Validate `HTTP_OK`/`HTTP_PARTIAL` before streaming and map common statuses to clear messages.

Regression risk: LOW  
Estimated effort: S  
Dependencies: fake HTTP test  
Test strategy: 403/404/500 download tests  
Status: OPEN

## PERSIST-001

Category: Playlist persistence  
Title: Playlist custom cover stores arbitrary external absolute file path  
Classification: CONFIRMED DEFECT  
Severity: MEDIUM  
Confidence: CONFIRMED

Affected files:

- `PlaylistDetailHelpers.kt`
- `PlaylistPersistence.kt`
- `SettingsRepository.kt`

Affected symbols:

- `choosePlaylistCoverFile`
- playlist edit cover flow

Evidence:

- `choosePlaylistCoverFile()` opens `FileDialog`.
- It returns `File(dir, file).absolutePath`.
- The helper does not copy the image into OmniTune app data or validate future availability.

Reproduction steps:

1. Edit a playlist.
2. Choose a cover from Downloads/Desktop.
3. Move or delete that source image.
4. Reopen the playlist.

Expected result:

- Custom playlist cover should remain available or fail gracefully from managed app storage.

Actual result:

- Persisted cover can point to a missing external file.

Root cause:

- The app persists the selected path directly instead of importing/copying the asset.

User impact:

- Playlist artwork can disappear after file moves/cleanup.

Engineering impact:

- Persistence depends on external unmanaged files.

Recommended fix:

- Copy selected cover into `%LOCALAPPDATA%\OmniTuneData\playlist-covers`.
- Sanitize extension and file name.
- Persist managed relative ID/path.
- Add migration/fallback for existing external paths.

Regression risk: MEDIUM  
Estimated effort: M  
Dependencies: playlist persistence migration  
Test strategy: cover import/delete-source/reload test  
Status: OPEN

## PERSIST-002

Category: Persistence architecture  
Title: JSON/preferences persistence is improved but still not a scalable local music data model  
Classification: TECHNICAL DEBT  
Severity: MEDIUM  
Confidence: HIGH

Affected files:

- `SettingsRepository.kt`
- `PlaylistPersistence.kt`
- `LikedSongsPersistence.kt`
- `PlaybackHistoryPersistence.kt`
- `OmniDownloadManager.kt`

Affected symbols:

- settings facade and JSON-backed stores

Evidence:

- Persistence has been split into helper classes and uses atomic file writes where important.
- The data model remains file/preferences-backed rather than indexed/transactional.
- Playlist, liked-song, history, and download state are separate file/preference structures.

Reproduction steps:

- Not a current crash; observe architecture and storage design.

Expected result:

- A premium large-library app should eventually use indexed transactional local storage for track/album/artist/playlist state.

Actual result:

- Current model is workable for beta but less robust for large libraries and multi-entity transactions.

Root cause:

- Product evolved from lightweight settings/files into library management.

User impact:

- Large data sets may become slower and migration harder.

Engineering impact:

- Cross-entity consistency is harder to prove.

Recommended fix:

- Plan an incremental SQLite/local DB migration for library entities, playlists, liked songs, history, and downloads.

Regression risk: HIGH  
Estimated effort: XL  
Dependencies: migration plan and regression suite  
Test strategy: migration tests, reload tests, transaction rollback tests  
Status: OPEN

## PROVIDER-001

Category: Provider/parser reliability  
Title: Provider facade remains very large and schema-fragile  
Classification: TECHNICAL DEBT  
Severity: MEDIUM  
Confidence: HIGH

Affected files:

- `innertube/src/main/kotlin/com/omnitune/innertube/YouTube.kt`
- `innertube/src/main/kotlin/com/omnitune/innertube/InnerTube.kt`

Affected symbols:

- provider parsing and API methods

Evidence:

- `YouTube.kt` is 1283 lines.
- `InnerTube.kt` is 665 lines.
- Provider tests exist but only 5 provider tests were observed in current XML reports.
- A TODO remains for album explicit badge extraction.

Reproduction steps:

- Trigger provider response changes or malformed fixtures.

Expected result:

- Parser failures should be isolated and fixture-covered.

Actual result:

- Core provider parsing is concentrated in large files with shallow fixture coverage.

Root cause:

- Multiple provider responsibilities live in one large facade/parser area.

User impact:

- External provider changes can break search/playback/discovery.

Engineering impact:

- Parser maintenance remains high-risk.

Recommended fix:

- Add fixture suite for search, album, artist, playlist, next, related, and malformed data.
- Extract parser units only after fixture coverage exists.

Regression risk: MEDIUM  
Estimated effort: L  
Dependencies: fixture corpus  
Test strategy: provider fixtures for happy path and malformed responses  
Status: OPEN

## PROVIDER-002

Category: Provider/parser null safety  
Title: Previously documented production `!!` parser debt appears fixed  
Classification: IMPROVEMENT  
Severity: INFORMATIONAL  
Confidence: HIGH

Affected files:

- `innertube/src/main/kotlin`
- `composeApp/src`

Affected symbols:

- production Kotlin sources

Evidence:

- `rg -n "!!"` found no production matches.
- The only observed match was in `OmniDownloadManagerTest.kt`.

Reproduction steps:

- Static source scan.

Expected result:

- Production parser should avoid force-null assertions.

Actual result:

- No production force-null assertions found.

Root cause:

- Prior remediation appears to have removed them.

User impact:

- Reduced crash risk.

Engineering impact:

- Previous debt should be closed, but malformed fixture coverage is still needed.

Recommended fix:

- Mark previous parser `!!` item fixed.
- Continue with fixture-backed parser hardening.

Regression risk: LOW  
Estimated effort: XS  
Dependencies: none  
Test strategy: keep static scan or detekt rule if introduced later  
Status: ALREADY FIXED

## UI-001

Category: Responsive layout / DPI  
Title: Fixed reference-size layout metrics remain widespread outside Settings  
Classification: HIGH-CONFIDENCE RISK  
Severity: MEDIUM  
Confidence: HIGH

Affected files:

- `OmniWindow.kt`
- `QueueView.kt`
- `PlaylistDetailTrackList.kt`
- `LikedSongsTable.kt`
- `SearchComponents.kt`
- `AlbumView.kt`
- `ArtistView.kt`
- `DownloadsView.kt`
- `SettingsView.kt`

Affected symbols:

- `LocalHomeReferenceMetrics.current`
- `metrics.px(...)`
- fixed `.height(...)`, `.width(...)`, tiny `sp` values

Evidence:

- Static scan found hundreds of fixed `metrics.px` widths/heights and many font sizes below 9sp.
- Examples include 7.5sp/6.6sp in `QueueView.kt`, 8sp table headers in playlist/liked tables, fixed 38-39px row heights, and fixed button widths.
- `OmniResponsiveLayout.kt` currently only exposes settings column logic and size class by width.

Reproduction steps:

1. Resize the app to 1187x789 or 1012x643.
2. Inspect Queue, Playlist Detail, Liked Songs, Search, Album, Artist, Downloads.
3. Check for clipping, tiny text, and table overflow.

Expected result:

- Screens should adapt through shared responsive rules.

Actual result:

- Source still depends heavily on reference-size metrics and screen-specific fixed dimensions.

Root cause:

- Earlier reference image reconstruction encoded many sizes directly.

User impact:

- Smaller windows and DPI scaling can become cramped or unreadable.

Engineering impact:

- Responsive fixes must be repeated per screen.

Recommended fix:

- Expand `OmniResponsiveLayout` into a real global content/safe-area/table/card contract.
- Replace fixed table/action sizing with adaptive column models.
- Add responsive decision tests beyond Settings.

Regression risk: MEDIUM  
Estimated effort: L  
Dependencies: visual QA matrix  
Test strategy: responsive unit tests plus manual 1906/1672/1366/1280/1187/1012 checks  
Status: OPEN

## UI-002

Category: Accessibility / UI semantics  
Title: Many clickable icon controls lack accessible labels  
Classification: CONFIRMED DEFECT  
Severity: MEDIUM  
Confidence: CONFIRMED

Affected files:

- `SidebarNav.kt`
- `SearchComponents.kt`
- `PlaylistDetailTrackList.kt`
- `PlaylistDetailSheets.kt`
- `LikedSongsActions.kt`
- `QueueView.kt`
- `OmniMiniPlayer.kt`
- `OmniBottomTransport.kt`
- additional UI files

Affected symbols:

- `Icon(..., null, ...)`
- clickable icon wrappers

Evidence:

- Static scan found many `Icon(..., null)` instances inside clickable rows/buttons or controls.
- Some icons are decorative and valid, but several are action affordances without semantic labels/tooltips.

Reproduction steps:

1. Navigate with keyboard or screen reader.
2. Focus icon-only actions in player, queue, playlist, liked songs, search, mini-player.

Expected result:

- Interactive icon-only controls should expose labels.

Actual result:

- Several controls rely on visuals only.

Root cause:

- Visual reconstruction favored icon rendering over semantic wrappers.

User impact:

- Screen reader and keyboard-only users cannot reliably identify actions.

Engineering impact:

- Accessibility validation remains incomplete.

Recommended fix:

- Audit icon-only actions.
- Use semantic buttons/tooltips/content descriptions for interactive icons.
- Keep decorative icons explicitly decorative.

Regression risk: LOW  
Estimated effort: M  
Dependencies: accessibility checklist  
Test strategy: Compose semantics tests where practical, keyboard walkthrough, screen reader smoke  
Status: OPEN

## UI-003

Category: Responsive layout  
Title: Settings responsive layout improved but still uses very small type and fixed component heights  
Classification: HIGH-CONFIDENCE RISK  
Severity: MEDIUM  
Confidence: HIGH

Affected files:

- `SettingsView.kt`
- `OmniResponsiveLayout.kt`

Affected symbols:

- `SettingsCard`
- `SettingsSwitch`
- `SettingsLine`
- `ReferenceButton`
- settings column logic

Evidence:

- Settings now uses `OmniResponsiveLayout.settingsColumnCount`.
- The card content still uses many 7.6sp-8.8sp labels and fixed heights such as 24dp-29dp controls.
- Some settings toggles store preferences for future support rather than active behavior.

Reproduction steps:

1. Open Settings at 1187x789, 1012x643, and 150% display scaling.
2. Inspect text readability and control hit targets.

Expected result:

- Settings should remain readable and usable at supported desktop sizes/DPI.

Actual result:

- Source suggests risk of tiny text and cramped hit targets.

Root cause:

- Reference-screenshot scaling remains embedded in settings components.

User impact:

- Settings can feel visually precise but not accessible/premium at DPI scaling.

Engineering impact:

- Responsive correctness still depends on manual QA.

Recommended fix:

- Move settings typography/control dimensions to accessible tokens.
- Add minimum hit target rules and responsive tests.

Regression risk: MEDIUM  
Estimated effort: M  
Dependencies: typography/accessibility pass  
Test strategy: manual DPI and window-size QA  
Status: OPEN

## UX-001

Category: Feature honesty  
Title: Several Settings toggles persist preferences but do not implement runtime behavior  
Classification: CONFIRMED DEFECT  
Severity: MEDIUM  
Confidence: CONFIRMED

Affected files:

- `SettingsView.kt`
- `SettingsPreferences.kt`
- `SettingsRepository.kt`

Affected symbols:

- `normalizeVolumePreference`
- `spatialAudioPreference`
- `gaplessPlaybackPreference`
- notification preferences
- `autoDownloadPlaylists`

Evidence:

- Settings toggles for Normalize Volume, Spatial Audio, Gapless Playback, notifications, weekly digest, concert alerts, and auto-download playlists write preferences.
- Descriptions explicitly say “Stored preference” or “Persist ... preference”.
- No source usage was found that applies several of these preferences to the audio engine, notifications, or automatic download behavior.

Reproduction steps:

1. Toggle Spatial Audio or Normalize Volume.
2. Play audio.
3. Observe no actual engine behavior change.
4. Toggle Concert Alerts or Weekly Digest.
5. Observe no notification service.

Expected result:

- Visible controls should either perform real behavior or be presented as future/disabled capability.

Actual result:

- Several controls are active but only persist a flag.

Root cause:

- Reference/UI settings outpaced backend capability.

User impact:

- Users believe features exist when they do not.

Engineering impact:

- Product honesty and support burden risk.

Recommended fix:

- Either fully implement backend behavior or hide/disable controls with clear “coming later” treatment.
- Prioritize removing fake active toggles before public release.

Regression risk: MEDIUM  
Estimated effort: M-L depending feature  
Dependencies: audio/notification/download architecture  
Test strategy: behavior tests per setting  
Status: OPEN

## UX-002

Category: Updates  
Title: Check for Updates opens GitHub releases instead of performing update detection/install  
Classification: PRODUCT GAP  
Severity: LOW  
Confidence: CONFIRMED

Affected files:

- `SettingsView.kt`
- `AppInfo.kt`

Affected symbols:

- `Check for Updates`
- `openUri(AppInfo.releasesUrl)`

Evidence:

- Settings About button calls `openUri(AppInfo.releasesUrl)`.
- No version check, latest-release comparison, download, installer handoff, or updater service exists.

Reproduction steps:

1. Open Settings/About.
2. Click Check for Updates.

Expected result:

- A native update check should tell the user whether installed version is current.

Actual result:

- Browser opens GitHub releases.

Root cause:

- Update flow is not implemented.

User impact:

- Users may not know whether they are current and may download wrong prerelease/latest asset.

Engineering impact:

- Release/install confusion persists.

Recommended fix:

- Implement a real update-check service using GitHub releases or a signed manifest.

Regression risk: LOW  
Estimated effort: M  
Dependencies: release-channel policy  
Test strategy: mock latest/current version tests  
Status: OPEN

## UX-003

Category: Playlist metadata honesty  
Title: Playlist mood/tag chips are inferred heuristically from title and songs  
Classification: PRODUCT GAP  
Severity: LOW  
Confidence: CONFIRMED

Affected files:

- `PlaylistDetailHelpers.kt`

Affected symbols:

- `inferPlaylistTags`

Evidence:

- `inferPlaylistTags` classifies playlists using substring checks such as `chill`, `lofi`, `night`, `r&b`, `soul`, `focus`, `dance`, and `pop`.
- It always appends `"Playlist"` and `"${songs.size} tracks"`.

Reproduction steps:

1. Open playlist with title/tracks containing matching words.
2. Observe generated chips.

Expected result:

- Mood chips should use real metadata or be clearly generic.

Actual result:

- Chips can imply unsupported mood classification.

Root cause:

- Visual reference included tags but real metadata support is limited.

User impact:

- Minor misleading metadata.

Engineering impact:

- Potential mismatch with no-fake-data policy.

Recommended fix:

- Use only provider/user playlist tags when available.
- Otherwise show neutral metadata chips such as playlist type/count/duration.

Regression risk: LOW  
Estimated effort: S  
Dependencies: playlist metadata model  
Test strategy: helper tests for no unsupported mood inference  
Status: OPEN

## ARCH-001

Category: Architecture  
Title: `PlayerViewModel` remains a broad facade after controller extraction  
Classification: TECHNICAL DEBT  
Severity: MEDIUM  
Confidence: HIGH

Affected files:

- `PlayerViewModel.kt`

Affected symbols:

- `PlayerViewModel`

Evidence:

- File is 778 lines.
- It still owns engine coordination, current song state, listen tracking, liked songs, followed artists, pinned library collections, discovery loading, route entity IDs, and delegation to controllers.
- Good extractions already exist for queue/search/download/radio/related/navigation/playlist.

Reproduction steps:

- Source inspection.

Expected result:

- High-risk playback state should be isolated behind testable orchestration boundaries.

Actual result:

- `PlayerViewModel` is improved but remains a central integration hub.

Root cause:

- It is the app-level player facade consumed by many UI screens.

User impact:

- Indirect: higher regression risk when adding player/library features.

Engineering impact:

- Harder to test playback/listening/library interactions independently.

Recommended fix:

- Continue incremental extraction only after tests:
  - listen/session tracker
  - liked/followed/library state controller
  - discovery loader
  - playback engine coordinator

Regression risk: HIGH  
Estimated effort: L  
Dependencies: characterization tests  
Test strategy: player facade tests and controller unit tests  
Status: OPEN

## ARCH-002

Category: Architecture  
Title: Several major UI files still combine layout, interaction, and data shaping  
Classification: TECHNICAL DEBT  
Severity: MEDIUM  
Confidence: HIGH

Affected files:

- `HomeView.kt`
- `LibraryView.kt`
- `SearchPanels.kt`
- `Screens.kt`
- `SettingsView.kt`
- `QueueView.kt`
- `ArtistView.kt`
- `DownloadsView.kt`

Affected symbols:

- major screen composables and local helper composables

Evidence:

- Multiple UI files remain 500-927 lines.
- Several files contain composables, local data derivation, interaction handlers, formatting, and layout constants together.

Reproduction steps:

- Source inspection.

Expected result:

- Complex screens should have stable component boundaries for tests, responsiveness, and reuse.

Actual result:

- Some screen logic remains large and tightly coupled.

Root cause:

- Fast visual/product iteration.

User impact:

- Indirect: regressions and inconsistent UI behavior.

Engineering impact:

- Slower maintenance and harder review.

Recommended fix:

- Extract screen sections incrementally when changing them.
- Avoid one broad rewrite.

Regression risk: MEDIUM  
Estimated effort: L  
Dependencies: screen-level tests and visual QA  
Test strategy: compile plus targeted screen interaction tests  
Status: OPEN

## ARCH-003

Category: Architecture / testability  
Title: QA runtime hooks are compiled into production source  
Classification: TECHNICAL DEBT  
Severity: LOW  
Confidence: HIGH

Affected files:

- `QaRuntime.kt`
- `Main.kt`
- `OmniWindow.kt`
- `NativeRuntime.kt`
- playlist-detail screen files

Affected symbols:

- `OMNITUNE_QA_*`
- report writes under `docs/qa`

Evidence:

- Production desktop source reads `OMNITUNE_QA_ROUTE`, `OMNITUNE_QA_SEARCH_QUERY`, `OMNITUNE_QA_REQUIRE_BUNDLED_VLC`, etc.
- `Main.kt` and `OmniWindow.kt` write JSON QA reports under `docs/qa` when env hooks are active.

Reproduction steps:

1. Launch production source with QA env vars.
2. Observe QA routing/report behavior.

Expected result:

- Runtime QA hooks should be isolated from public production behavior or explicitly gated.

Actual result:

- Hooks are compiled into production source.

Root cause:

- Runtime QA relied on app-internal hooks.

User impact:

- Low in normal use because env vars must be set.

Engineering impact:

- Production/test boundary remains blurred.

Recommended fix:

- Move QA hooks behind a build flag/source set or explicit diagnostic mode.

Regression risk: LOW  
Estimated effort: M  
Dependencies: QA workflow replacement  
Test strategy: QA route tests still pass in diagnostic build  
Status: OPEN

## PERF-001

Category: Compose performance  
Title: Some screens collect broad player state and perform derived list work inside composition  
Classification: HIGH-CONFIDENCE RISK  
Severity: MEDIUM  
Confidence: MEDIUM

Affected files:

- `LikedSongsView.kt`
- `DownloadsView.kt`
- `PlaylistDetailView.kt`
- `QueueView.kt`
- `HomeView.kt`

Affected symbols:

- screen composables collecting `PlayerViewModel` StateFlows
- list filtering/sorting/aggregation in composables

Evidence:

- Liked Songs derives visible records from records/filter/sort/query/downloaded IDs in composition.
- Downloads walks download directory inside `remember(downloadsDir)`.
- Many screens directly collect player-wide state from the facade.

Reproduction steps:

1. Use a large liked-song/download/playlist collection.
2. Trigger playback progress, download updates, and filtering.
3. Profile recomposition and allocations.

Expected result:

- High-frequency state should not recompose unrelated large screen content.

Actual result:

- Source suggests broad coupling remains in several screens.

Root cause:

- UI directly consumes broad facade state and locally derives collections.

User impact:

- Potential scroll/responsiveness degradation with large collections.

Engineering impact:

- Performance problems are harder to localize.

Recommended fix:

- Introduce view-state derivation in controllers or memoized models.
- Isolate progress-only state to bottom/now-playing components.

Regression risk: MEDIUM  
Estimated effort: M-L  
Dependencies: profiler and large-data fixtures  
Test strategy: large-list profiling and recomposition tracing  
Status: OPEN

## TEST-001

Category: Test coverage  
Title: No automated screenshot or multi-window visual regression suite  
Classification: PRODUCT GAP  
Severity: MEDIUM  
Confidence: HIGH

Affected files:

- `composeApp/src/desktopTest/kotlin`
- QA scripts/docs

Affected symbols:

- visual QA workflow

Evidence:

- Tests include responsive decision logic but not automated screenshot comparison.
- Existing `docs/qa/screenshots/` is untracked generated evidence, not an automated gating suite.

Reproduction steps:

- Run tests; no screenshot diff gate is invoked.

Expected result:

- Reference-heavy UI should have repeatable visual regression checks.

Actual result:

- Visual correctness relies mostly on manual screenshots.

Root cause:

- UI evolved through visual passes faster than test tooling.

User impact:

- Layout/color regressions can ship unnoticed.

Engineering impact:

- Expensive manual QA.

Recommended fix:

- Add deterministic screenshot capture for core screens at agreed sizes.
- Store approved references intentionally, preferably with artifact/LFS policy.

Regression risk: LOW  
Estimated effort: L  
Dependencies: stable test renderer/window capture strategy  
Test strategy: screenshot diff thresholds  
Status: OPEN

## TEST-002

Category: Test coverage  
Title: Clean installed-app, offline, and soak tests are not part of the baseline gate  
Classification: PRODUCT GAP  
Severity: MEDIUM  
Confidence: HIGH

Affected files:

- release QA process
- `docs/audit`

Affected symbols:

- release validation checklist

Evidence:

- Baseline gate compiles/tests and launch smoke passes.
- No current retained evidence proves clean VM/no Java/no VLC, physical offline installed app, or multi-hour playback soak.

Reproduction steps:

- Inspect current automated test outputs and release docs.

Expected result:

- Premium Windows release should include installed runtime proof and soak coverage.

Actual result:

- These remain manual/unverified gaps.

Root cause:

- Current test suite is repo-level, not full installed-product QA.

User impact:

- Installed release may fail in environments not represented by developer machine.

Engineering impact:

- Release confidence gap.

Recommended fix:

- Add release checklist and scripts for installed smoke, offline smoke, and soak runs.

Regression risk: LOW  
Estimated effort: M-L  
Dependencies: VM/test machine  
Test strategy: documented release sign-off artifacts  
Status: OPEN

## TEST-003

Category: Test coverage  
Title: Provider fixture coverage is shallow compared with provider surface area  
Classification: PRODUCT GAP  
Severity: MEDIUM  
Confidence: HIGH

Affected files:

- `innertube/src/test/kotlin`
- `innertube/src/main/kotlin`

Affected symbols:

- provider parsers and fixtures

Evidence:

- XML reports show 5 `innertube` tests.
- Provider code owns search, home, browse, next, related, player, album, artist, playlists.

Reproduction steps:

- Run `.\gradlew.bat :innertube:test`.

Expected result:

- External provider parser should have fixtures across all important response types.

Actual result:

- Coverage exists but is not broad.

Root cause:

- Fixture corpus is still early.

User impact:

- Provider changes can break major app areas.

Engineering impact:

- Refactoring parser code remains risky.

Recommended fix:

- Add fixture corpus and malformed response tests before provider refactor.

Regression risk: LOW  
Estimated effort: L  
Dependencies: captured/sanitized provider fixtures  
Test strategy: fixture-backed parser tests  
Status: OPEN

## ACCESS-001

Category: Accessibility  
Title: Keyboard/focus support exists but screen-reader and high-contrast validation are not proven  
Classification: PRODUCT GAP  
Severity: MEDIUM  
Confidence: HIGH

Affected files:

- `OmniKeyboardShortcutRouter.kt`
- `OmniFocusTraversalModel.kt`
- screen composables

Affected symbols:

- keyboard shortcuts
- focus traversal
- icon actions

Evidence:

- Keyboard router and focus traversal tests exist.
- Many icon-only controls still lack content descriptions.
- No retained evidence of screen-reader, high-contrast, or 125%/150% DPI accessibility validation.

Reproduction steps:

1. Use keyboard-only navigation through all screens.
2. Use Windows Narrator/NVDA.
3. Use high contrast and display scaling.

Expected result:

- Core controls should be reachable and understandable.

Actual result:

- Not fully proven; source contains semantic gaps.

Root cause:

- Accessibility was partially implemented but not fully audited.

User impact:

- Accessibility and usability gaps for keyboard/screen-reader/DPI users.

Engineering impact:

- Accessibility regressions can go unnoticed.

Recommended fix:

- Add an accessibility checklist, semantic label audit, and keyboard-only walkthrough.

Regression risk: LOW  
Estimated effort: M  
Dependencies: manual assistive tech validation  
Test strategy: semantics tests plus manual screen reader/high contrast pass  
Status: OPEN

## SECURITY-001

Category: Security / external actions  
Title: `openUri` does not restrict schemes, though current call sites appear hardcoded  
Classification: POSSIBLE ISSUE  
Severity: LOW  
Confidence: MEDIUM

Affected files:

- `SettingsView.kt`

Affected symbols:

- `openUri`

Evidence:

- `openUri(uri: String)` calls `Desktop.getDesktop().browse(URI(uri))`.
- Current observed call sites pass `AppInfo.releaseUrl`/`releasesUrl`, not user-controlled strings.

Reproduction steps:

- Add or reach a call site passing an untrusted URI.

Expected result:

- External URI opening helpers should restrict to safe schemes.

Actual result:

- Helper itself does not enforce scheme.

Root cause:

- Current helper assumes trusted call sites.

User impact:

- Low with current call sites.

Engineering impact:

- Future call sites could accidentally open unsafe URI schemes.

Recommended fix:

- Restrict to `https`/`http` if this helper remains generic.

Regression risk: LOW  
Estimated effort: XS  
Dependencies: none  
Test strategy: helper unit test if extracted  
Status: OPEN

## REPO-001

Category: Repository hygiene  
Title: Generated QA screenshots are untracked in the worktree  
Classification: IMPROVEMENT  
Severity: LOW  
Confidence: CONFIRMED

Affected files:

- `docs/qa/screenshots/`

Affected symbols:

- QA evidence artifacts

Evidence:

- `git status --short` shows `?? docs/qa/screenshots/`.
- Directory contains 20 PNG files totaling about 5.7 MB.

Reproduction steps:

- Run `git status --short`.

Expected result:

- Generated artifacts should have a clear policy: commit as intentional references, store through LFS, or ignore.

Actual result:

- Artifact state is ambiguous.

Root cause:

- Visual QA created evidence outside a finalized artifact policy.

User impact:

- None directly.

Engineering impact:

- Risk of accidental binary bloat or lost useful evidence.

Recommended fix:

- Decide artifact policy.
- Add `.gitignore` or Git LFS/reference directory rules accordingly.

Regression risk: LOW  
Estimated effort: S  
Dependencies: repo policy decision  
Test strategy: git status stays intentional  
Status: OPEN

## REPO-002

Category: Repository hygiene / secrets  
Title: `local.properties` is tracked, even though it contains credential placeholders  
Classification: HIGH-CONFIDENCE RISK  
Severity: LOW  
Confidence: HIGH

Affected files:

- `local.properties`

Affected symbols:

- `lastfm.apiKey`
- `lastfm.secret`

Evidence:

- `git ls-files local.properties` returned `local.properties`.
- File contains blank `lastfm.apiKey=` and `lastfm.secret=` placeholders and a comment saying not to commit secrets.

Reproduction steps:

- Run `git ls-files local.properties`.

Expected result:

- Local machine secrets should not be tracked in a working config file.

Actual result:

- The placeholder file is tracked.

Root cause:

- Template and local config are the same file.

User impact:

- Low now because values are blank.

Engineering impact:

- Future contributor could accidentally commit secrets.

Recommended fix:

- Move template to `local.properties.example`.
- Ignore real `local.properties`.

Regression risk: LOW  
Estimated effort: S  
Dependencies: build config if it reads local.properties  
Test strategy: build still works with absent local.properties or documented setup  
Status: OPEN

## BUILD-001

Category: Dependency hygiene  
Title: Version catalog contains unused dependencies/plugins  
Classification: TECHNICAL DEBT  
Severity: LOW  
Confidence: HIGH

Affected files:

- `gradle/libs.versions.toml`

Affected symbols:

- `room`
- `hilt`
- `ktor-client-okhttp`
- `coroutines-test`
- `junit`
- `newpipe-extractor`
- possibly others

Evidence:

- Version catalog defines dependencies/plugins not referenced by `composeApp/build.gradle.kts` in inspected build config.

Reproduction steps:

- Compare `libs.versions.toml` aliases to build script usage.

Expected result:

- Version catalog should avoid stale dependencies unless intentionally reserved.

Actual result:

- Several aliases appear unused.

Root cause:

- Historical experiments or planned migrations.

User impact:

- None direct.

Engineering impact:

- Dependency surface is harder to audit.

Recommended fix:

- Remove unused catalog entries or mark them as planned.

Regression risk: LOW  
Estimated effort: S  
Dependencies: full Gradle alias usage check  
Test strategy: build after cleanup  
Status: OPEN

## BUILD-002

Category: Build reproducibility  
Title: `maven.pkg.jetbrains.space/public/p/compose/dev` repository remains configured  
Classification: IMPROVEMENT  
Severity: LOW  
Confidence: MEDIUM

Affected files:

- `settings.gradle.kts`

Affected symbols:

- dependency repositories

Evidence:

- The Compose dev Maven repository is configured in both plugin and dependency repositories.

Reproduction steps:

- Inspect `settings.gradle.kts`.

Expected result:

- Stable app builds should use only required repositories.

Actual result:

- Dev repository remains configured.

Root cause:

- Compose Multiplatform historical repository setup.

User impact:

- None direct.

Engineering impact:

- Slightly broader dependency resolution surface.

Recommended fix:

- Verify whether Compose 1.8.0 still requires this repository; remove if not required.

Regression risk: LOW  
Estimated effort: XS  
Dependencies: dependency resolution check  
Test strategy: clean dependency resolution/build  
Status: OPEN

## DOCS-001

Category: Documentation  
Title: Historical audit and QA docs can drift from current source  
Classification: TECHNICAL DEBT  
Severity: LOW  
Confidence: HIGH

Affected files:

- `docs/audit/baseline-report.md`
- `docs/audit/technical-debt-ledger.md`
- `docs/audit/regression-matrix.md`
- `docs/audit/final-engineering-report.md`
- QA docs under `docs/qa`

Affected symbols:

- audit claims and baselines

Evidence:

- Existing audit docs predate HEAD `951d421` and mention issues later refactored.
- Current source has no production `!!` despite earlier docs listing parser `!!` debt.

Reproduction steps:

- Compare historical docs to current source.

Expected result:

- Current audit docs should clearly distinguish historical state from current state.

Actual result:

- Without current verification, old debt can be mistaken as still open.

Root cause:

- Fast-moving project with many QA passes.

User impact:

- Planning confusion.

Engineering impact:

- Work may target already-fixed issues.

Recommended fix:

- Archive historical docs or add “superseded by” pointers.

Regression risk: LOW  
Estimated effort: S  
Dependencies: docs policy  
Test strategy: docs review  
Status: OPEN

## PRODUCT-001

Category: Product functionality  
Title: Full premium account/cloud/social ecosystem is missing  
Classification: PRODUCT GAP  
Severity: INFORMATIONAL  
Confidence: HIGH

Affected files:

- Settings/account UI
- future backend/service architecture

Affected symbols:

- account profile
- social/followers/collaboration
- cloud sync

Evidence:

- Settings shows “Local Windows User” and “OmniTune local profile”.
- No login/cloud account service was found.

Reproduction steps:

- Inspect Settings/account and source services.

Expected result:

- Premium cross-device music app would provide account/cloud capabilities if marketed that way.

Actual result:

- Current app is local-first.

Root cause:

- Product scope is beta/local desktop.

User impact:

- Missing cross-device and social features.

Engineering impact:

- Requires backend/product decisions, not just UI.

Recommended fix:

- Keep UI honest as local-only unless backend is implemented.

Regression risk: HIGH if rushed  
Estimated effort: XL  
Dependencies: backend/auth/legal/privacy  
Test strategy: auth/session/sync tests  
Status: OPEN

## PRODUCT-002

Category: Product functionality  
Title: No real native update service  
Classification: PRODUCT GAP  
Severity: MEDIUM  
Confidence: HIGH

Affected files:

- Settings/About
- release process

Affected symbols:

- Check for Updates

Evidence:

- Update action opens GitHub releases.
- No updater pipeline found.

Reproduction steps:

- Click Check for Updates.

Expected result:

- App checks latest version and guides safe installation.

Actual result:

- Browser opens releases page.

Root cause:

- Native updater not built.

User impact:

- Update confusion.

Engineering impact:

- Releases require manual user handling.

Recommended fix:

- Implement signed manifest update check before automatic installer execution.

Regression risk: MEDIUM  
Estimated effort: L  
Dependencies: signing/release channel  
Test strategy: fake manifest/current-latest tests  
Status: OPEN

## PRODUCT-003

Category: Product functionality  
Title: Windows notifications are represented as preferences but not implemented as notification behavior  
Classification: CONFIRMED DEFECT  
Severity: MEDIUM  
Confidence: CONFIRMED

Affected files:

- `SettingsView.kt`
- `SettingsPreferences.kt`

Affected symbols:

- `newMusicNotifications`
- `recommendationNotifications`
- `concertAlertNotifications`
- `productUpdateNotifications`
- `weeklyDigestNotifications`

Evidence:

- Notification toggles persist preferences.
- No Windows notification service or scheduled digest/alert implementation was found.

Reproduction steps:

1. Toggle notification settings.
2. Use app normally.
3. Observe no actual Windows notification behavior tied to those toggles.

Expected result:

- Notification settings should control real notifications.

Actual result:

- Settings persist only.

Root cause:

- UI was built ahead of notification infrastructure.

User impact:

- Misleading settings.

Engineering impact:

- Feature-honesty risk.

Recommended fix:

- Hide/disable notification settings or implement a real notification subsystem.

Regression risk: MEDIUM  
Estimated effort: M-L  
Dependencies: Windows notification integration  
Test strategy: notification service tests and manual Windows toast verification  
Status: OPEN

## PRODUCT-004

Category: Product functionality  
Title: True drag-and-drop playlist reordering is not verified  
Classification: PRODUCT GAP  
Severity: LOW  
Confidence: MEDIUM

Affected files:

- `PlaylistDetailTrackList.kt`
- `PlaylistDetailHelpers.kt`
- `PlaylistPersistenceRules.kt`

Affected symbols:

- `MoveUp`
- `MoveDown`
- playlist reorder

Evidence:

- Playlist action enum contains `MoveUp` and `MoveDown`.
- Persistence rule supports moving by indices.
- No source evidence of true desktop drag-and-drop reorder was verified during this audit.

Reproduction steps:

1. Open editable playlist.
2. Try dragging a track row.

Expected result:

- If reference/product requires drag reorder, row drag should persist final order.

Actual result:

- Move up/down path appears to be the real reorder path.

Root cause:

- Safer implementation chose explicit move actions over drag and drop.

User impact:

- Playlist editing feels less premium.

Engineering impact:

- Drag implementation would require careful UI and persistence tests.

Recommended fix:

- Either implement real drag reorder or document explicit move controls as current supported behavior.

Regression risk: MEDIUM  
Estimated effort: M  
Dependencies: Compose desktop drag/drop support decision  
Test strategy: reorder persistence tests and manual drag QA  
Status: OPEN

## PRODUCT-005

Category: Product functionality  
Title: Spatial/lossless/crossfade-style audio enhancements are not implemented as real audio pipeline features  
Classification: PRODUCT GAP  
Severity: MEDIUM  
Confidence: HIGH

Affected files:

- `SettingsView.kt`
- `SettingsPreferences.kt`
- `VlcjAudioEngine.kt`

Affected symbols:

- `spatialAudioPreference`
- `normalizeVolumePreference`
- `gaplessPlaybackPreference`

Evidence:

- Preferences exist and UI controls are active.
- `VlcjAudioEngine` exposes play/pause/seek/volume/rate/release but no inspected spatial/normalization/gapless implementation.

Reproduction steps:

1. Toggle audio enhancement preferences.
2. Inspect playback behavior.

Expected result:

- Audio settings should materially affect engine behavior.

Actual result:

- They appear to be stored preferences only.

Root cause:

- Settings UI exists before engine feature implementation.

User impact:

- Misleading premium audio controls.

Engineering impact:

- Feature claims become risky.

Recommended fix:

- Remove/hide active controls until backend exists, or implement engine behavior with tests.

Regression risk: MEDIUM-HIGH  
Estimated effort: L  
Dependencies: audio engine capability research  
Test strategy: engine behavior tests/manual audio verification  
Status: OPEN

## PRODUCT-006

Category: Product functionality  
Title: Crash/error reporting is absent  
Classification: PRODUCT GAP  
Severity: LOW  
Confidence: HIGH

Affected files:

- platform/logging/release process

Affected symbols:

- `OmniLogger`
- startup logs

Evidence:

- `OmniLogger` logs locally/stdout.
- No crash reporting or opt-in telemetry pipeline was found.

Reproduction steps:

- Inspect logging and release configuration.

Expected result:

- Public beta should at least preserve actionable logs and expose a support bundle/export path.

Actual result:

- Local logging exists, no reporting/export workflow verified.

Root cause:

- Privacy/product decision not implemented.

User impact:

- Users cannot easily submit useful crash info.

Engineering impact:

- Harder production diagnosis.

Recommended fix:

- Add opt-in crash/log export, not silent telemetry.

Regression risk: LOW  
Estimated effort: M  
Dependencies: privacy policy  
Test strategy: crash/log export test  
Status: OPEN

## PRODUCT-007

Category: Windows integration  
Title: SMTC exists but full Windows shell/hardware-key validation is not retained in current audit evidence  
Classification: PRODUCT GAP  
Severity: LOW  
Confidence: MEDIUM

Affected files:

- `SmtcManager.kt`
- tests

Affected symbols:

- SMTC integration

Evidence:

- SMTC mapping tests exist.
- No current runtime evidence retained for Windows shell flyout/hardware media keys.

Reproduction steps:

1. Play a track.
2. Use Windows media overlay/hardware keys.
3. Verify title/artwork/playback commands.

Expected result:

- Windows shell integration works in real installed app.

Actual result:

- Not proven in this audit.

Root cause:

- Automated tests cover mapping, not OS integration.

User impact:

- Possible shell integration issues.

Engineering impact:

- Release QA gap.

Recommended fix:

- Add manual release checklist and smoke evidence for SMTC.

Regression risk: LOW  
Estimated effort: S  
Dependencies: Windows test machine  
Test strategy: hardware media key/manual SMTC test  
Status: OPEN

## CODE-001

Category: Logging/code quality  
Title: `OmniLogger` can print to stdout in production runtime  
Classification: IMPROVEMENT  
Severity: LOW  
Confidence: HIGH

Affected files:

- `OmniLogger.kt`

Affected symbols:

- `println(str)`

Evidence:

- Static scan found `println(str)` in `OmniLogger.kt`.

Reproduction steps:

- Enable stdout logging mode if configured.

Expected result:

- Production logging should be intentional and controllable.

Actual result:

- Stdout logging path remains.

Root cause:

- Simple logger implementation.

User impact:

- Low for packaged GUI app.

Engineering impact:

- Logs may be noisy in development.

Recommended fix:

- Keep if needed for diagnostics, otherwise gate by debug flag.

Regression risk: LOW  
Estimated effort: XS  
Dependencies: logging policy  
Test strategy: logger unit/smoke test  
Status: OPEN

## CODE-002

Category: Dead code / incomplete code  
Title: Provider TODO remains for explicit album badge extraction  
Classification: IMPROVEMENT  
Severity: INFORMATIONAL  
Confidence: CONFIRMED

Affected files:

- `innertube/src/main/kotlin/com/omnitune/innertube/YouTube.kt`

Affected symbols:

- album explicit badge extraction

Evidence:

- Static scan found TODO: `explicit = false, // TODO: Extract explicit badge for albums from YouTube response`.

Reproduction steps:

- Inspect provider album parsing.

Expected result:

- Album explicit metadata should be parsed when provider supplies it.

Actual result:

- Album explicit badge currently defaults false in that path.

Root cause:

- Parser support incomplete.

User impact:

- Explicit metadata can be missing for album results.

Engineering impact:

- Minor parser debt.

Recommended fix:

- Add fixture with explicit album metadata and parser extraction.

Regression risk: LOW  
Estimated effort: S  
Dependencies: fixture data  
Test strategy: explicit album fixture test  
Status: OPEN

## QA-001

Category: Runtime verification limitation  
Title: This audit could not retain screenshot evidence because desktop capture targeted the wrong foreground window  
Classification: INFORMATIONAL  
Severity: INFORMATIONAL  
Confidence: CONFIRMED

Affected files:

- `docs/audit/evidence/README.md`

Affected symbols:

- runtime screenshot evidence

Evidence:

- App launch smoke succeeded.
- Attempted screen capture produced unrelated foreground content.
- The artifact was deleted and not retained.

Reproduction steps:

- Run window screenshot capture while another app overlays the OmniTune window.

Expected result:

- Capture should target OmniTune only.

Actual result:

- Capture was unreliable in this environment.

Root cause:

- Windows z-order/foreground capture limitations in current automation.

User impact:

- None in app.

Engineering impact:

- Audit visual evidence is limited.

Recommended fix:

- Use app-window-specific screenshot automation or controlled desktop session for future visual QA.

Regression risk: LOW  
Estimated effort: M  
Dependencies: screenshot tooling  
Test strategy: verify captured window title and content before retaining evidence  
Status: OPEN
