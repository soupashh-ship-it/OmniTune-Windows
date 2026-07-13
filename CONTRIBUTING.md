# Contributing

## Development Environment

- Windows 10 or Windows 11
- JDK 21
- VLC 3.x installed locally for playback/runtime QA
- Git

## Setup

```powershell
git clone https://github.com/soupashh-ship-it/OmniTune-Windows.git
cd OmniTune-Windows
.\gradlew.bat --version
```

## Build and Test

```powershell
.\gradlew.bat :composeApp:desktopTest
.\gradlew.bat build -x :innertube:test
```

Run provider/network tests intentionally:

```powershell
.\gradlew.bat :innertube:test
```

Run the desktop app:

```powershell
.\gradlew.bat :composeApp:run
```

## Branches and Commits

Use descriptive branch names, for example:

- `fix/playback-vlc-discovery`
- `feat/download-retry-state`
- `docs/installer-troubleshooting`

Commit messages should describe the real change. Avoid unrelated edits in the same pull request.

## Pull Requests

Include:

- scope and motivation
- commands run
- screenshots for UI changes
- notes about playback, offline mode, queue/session persistence, search, downloads, and installer behavior when relevant

Do not include:

- credentials
- tokens
- local `.env` files
- `local.properties`
- build outputs
- generated installers
- downloaded media artifacts
- IDE workspace state

## Regression Areas

Changes should avoid regressions in:

- playback and VLC/libVLC startup
- search and discovery
- queue behavior
- saved queue playlists
- playback history/session persistence
- downloads and offline local-file playback
- lyrics fallbacks
- settings persistence
- mini-player behavior
- Windows packaging
