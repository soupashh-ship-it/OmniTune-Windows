# Settings shell color compositing audit

## Files audited / changed

- `composeApp/src/desktopMain/kotlin/com/omnitune/app/window/OmniTuneTheme.kt`
- `composeApp/src/desktopMain/kotlin/com/omnitune/app/window/NocturneBackdrop.kt`
- `composeApp/src/desktopMain/kotlin/com/omnitune/app/window/Sidebar.kt`
- `composeApp/src/desktopMain/kotlin/com/omnitune/app/window/OmniTopBar.kt`
- `composeApp/src/desktopMain/kotlin/com/omnitune/app/window/OmniBottomPlayer.kt`
- `composeApp/src/desktopMain/kotlin/com/omnitune/app/window/components/OmniComponents.kt`
- `composeApp/src/desktopMain/kotlin/com/omnitune/app/window/SettingsView.kt`

## Chain

Window/root shell:

`OmniTuneTheme` tokens -> `OmniReferenceBackdrop` -> Sidebar / TopBar / content route -> BottomPlayer.

Search field:

`OmniSearchField` now consumes `OmniReferenceColors.SearchBackground`, `SearchBorder`, `TextMuted`, and accent tokens.

Cards:

Shared surfaces and Settings cards now use the darker reference surface family (`SurfaceBase`, `SurfaceAlternate`, `SurfaceDeepRaised`) with restrained borders.

Bottom player:

`TargetBottomPlayerSurface` remains layered; the base and violet overlay regions were retuned rather than flattened to a single color.

## Retained limitations

- The supplied reference includes real-looking playback artwork/text that is not present when OmniTune is idle. The idle player placeholder was improved, but no fake song metadata was added.
- Whole-image diff includes unsupported Settings content/text differences and is not a pure shell-only score.
