# Changelog

All notable changes to OmniTune Windows will be documented here.

## Unreleased

- Bumped Windows installer/package version to `0.1.3` after validating that the already-published 0.1.2 RC did not reliably install VLC runtime files on upgrade.
- Added embedded VLC runtime extraction fallback so installed builds can use bundled libVLC even when jpackage does not lay down `native/vlc` beside the launcher.
- Moved writable application data to `%LOCALAPPDATA%\OmniTuneData` and added migration from the previous `%LOCALAPPDATA%\OmniTune` install/data collision path.
- Current Gradle desktop package version: `0.1.3`.

## 0.1.2 RC 1

- Bumped Windows installer/package version to `0.1.2` for the next public release candidate.
- Fixed manual top-bar search focus by removing the search field from the draggable window region.
- Added provider-backed Browse and Radio surfaces with real endpoint navigation.
- Hardened playback shutdown, local playback volume restoration, reduced-motion coverage, download-index corruption recovery, and desktop keyboard shortcuts.
- Added persistent pinned Library collections and richer real row actions.
- Improved Downloads filtering and real downloaded-album grouping.
- Current Gradle desktop package version: `0.1.2`.

## 0.1.1 RC 1

- Bumped Windows installer/package version to `0.1.1` so users with `0.1.0` already installed can run the new installer as an upgrade instead of hitting Windows Installer error `1638`.
- Prepared the Windows desktop repository for public GitHub publication.
- Added repository documentation, community files, issue templates, CI, and release workflow scaffolding.
- Current Gradle desktop package version: `0.1.1`.
