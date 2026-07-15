# OmniTune Windows Technical Audit Baseline

Date: 2026-07-15

## Repository State

- Project path: `D:\Omnitune Windoww`
- Branch: `main`
- Starting HEAD: `cc40f64822789ee2be5016935cfafb99742719f7`
- Remote: `origin https://github.com/soupashh-ship-it/OmniTune-Windows.git`
- Worktree: dirty before audit. Existing user/development work was preserved; no reset, clean, checkout, or destructive git operation was used.

## Current Dirty Worktree Summary

Tracked modifications existed in production source, tests, resources, QA scripts, and QA screenshots. Untracked work included the new Liked Songs screen, icon asset, QA reports, route reconstruction artifacts, playlist reference artifacts, and Liked Songs captures.

The dirty state is treated as intentional accumulated work and remains protected.

## Build System

- Gradle wrapper project.
- Kotlin: `2.1.20`
- Compose Multiplatform: `1.8.0`
- JVM toolchain: `21`
- Main desktop module: `:composeApp`
- Supporting modules: `:innertube`, `:kugou`, `:lrclib`, `:lastfm`, `:simpmusic`, `:betterlyrics`, `:kizzy`, `:canvas`
- Application entry point: `com.omnitune.app.MainKt`
- Windows package targets: EXE and MSI through Compose Desktop native distributions.

## Baseline Commands

Executed against the current dirty source state:

```powershell
.\gradlew.bat :composeApp:compileKotlinDesktop
.\gradlew.bat :composeApp:assemble
.\gradlew.bat test
.\gradlew.bat :composeApp:desktopTest
```

## Baseline Results

- `:composeApp:compileKotlinDesktop`: PASS
- `:composeApp:assemble`: PASS
- root `test`: PASS
- `:composeApp:desktopTest`: PASS

No baseline build blocker was found.

## Major Entry Points and Owners

- Application shell: `composeApp/src/desktopMain/kotlin/com/omnitune/app/Main.kt`
- Window root: `OmniWindow.kt`
- Navigation/sidebar: `Sidebar.kt`, `PlayerViewModel.NavScreen`
- Playback owner: `PlayerViewModel.kt`
- Native playback engine: `VlcjAudioEngine.kt`
- Search/provider façade: `YouTubeService.kt`
- Provider implementation: `innertube/src/main/kotlin/com/omnitune/innertube/YouTube.kt`
- Downloads: `OmniDownloadManager.kt`
- Persistence/settings/playlists/history/liked songs: `SettingsRepository.kt`, `AtomicFileStore.kt`
- Core route screens: Home, Search, Library, Playlist Detail, Liked Songs, Artist, Album, Queue, Downloads, Settings, Now Playing
- Test root: `composeApp/src/desktopTest/kotlin`

## Baseline Warnings Observed

- Some deprecated Material icon aliases remain.
- `painterResource(String)` deprecation remains in existing icon-loading paths.
- Several provider parsers still contain force-null assertions.
- Several large composable/view-model files exceed 700–1500 lines and need careful phased ownership review.

## Environment Limitations

- No clean Windows VM validation was performed.
- No physical no-network installed-app proof was performed.
- No multi-hour playback soak was performed.
- No external screen-reader runtime validation was performed.
- No legitimate Windows code-signing certificate is available.
