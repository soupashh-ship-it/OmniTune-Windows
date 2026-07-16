# OmniTune Windows Privacy Policy

Last updated: July 16, 2026

OmniTune Windows is an open-source desktop music application for Windows. This policy explains what data the app stores locally, what network requests it may make, and what the project maintainers do not collect.

## Summary

- OmniTune Windows does not currently include user accounts, cloud sync, advertising telemetry, or analytics tracking.
- OmniTune Windows stores app data locally on your Windows device.
- Search, streaming, discovery, artwork, lyrics, update checks, and downloads may contact external services.
- Downloaded media, playlists, liked songs, settings, history, logs, and cache files remain on your device unless you share them yourself.

## Local Data Stored by the App

OmniTune Windows stores runtime data under:

```text
%LOCALAPPDATA%\OmniTuneData
```

Depending on how you use the app, this folder may contain:

- settings and preferences;
- theme and window preferences;
- recent searches;
- liked song records;
- saved playlists;
- queue/session/playback history;
- download metadata;
- downloaded audio files;
- playlist cover images imported into app-managed storage;
- cache files;
- local runtime logs.

This data is used to make the app work locally and to preserve your library, preferences, downloads, and playback state across restarts.

## Network Requests

OmniTune Windows uses provider-backed services for music-related features. When you use features such as search, discovery, playback, artwork loading, lyrics, related content, playlists, albums, artists, downloads, or update checks, the app may make network requests to external services.

These services may receive information necessary to fulfill the request, such as:

- search text you enter;
- track, album, artist, playlist, or video identifiers;
- requests for artwork, metadata, lyrics, or playback sources;
- standard network information such as IP address, user agent, and request timing.

Those external services have their own privacy policies and data handling practices. OmniTune Windows does not control those services.

## Downloads and Offline Playback

If you download tracks for offline playback, OmniTune Windows stores downloaded files and related metadata locally under the app data directory. The app uses these local files for offline/local-first playback where available.

Uninstalling or reinstalling the application is intended to preserve `%LOCALAPPDATA%\OmniTuneData` so that important user data is not accidentally wiped with the program binaries.

## Logs

OmniTune Windows may write local diagnostic logs under:

```text
%LOCALAPPDATA%\OmniTuneData\logs
```

Logs are intended for troubleshooting startup, playback, native runtime, provider, or application errors. Logs are stored locally. They are not automatically uploaded by OmniTune Windows.

If you choose to share logs in a bug report, review them first and remove any information you do not want to share, such as local file paths, track names, or search terms.

## Update Checks

The app may check GitHub Releases to determine whether a newer OmniTune Windows version is available. This contacts GitHub and may send standard request metadata to GitHub. OmniTune Windows does not automatically install updates without user action.

## Code Signing and Release Integrity

OmniTune Windows may use third-party code-signing services, such as SignPath Foundation, to sign release artifacts. Code signing is used to improve installer integrity and user trust. It does not give the signing service access to your local OmniTune app data.

## What the Project Maintainers Do Not Collect

The OmniTune Windows application does not currently send the project maintainers:

- analytics events;
- advertising identifiers;
- account profiles;
- listening history;
- liked songs;
- playlists;
- downloaded files;
- crash reports;
- local logs.

If this changes in the future, this privacy policy should be updated before any such collection is added.

## Data Deletion

You can remove local OmniTune Windows data by deleting:

```text
%LOCALAPPDATA%\OmniTuneData
```

Only do this if you intentionally want to remove local settings, playlists, liked songs, download records, downloaded files, logs, and cache data.

## Children's Privacy

OmniTune Windows is not designed to knowingly collect personal information from children. The app does not provide project-maintainer-operated accounts or cloud data collection.

## Changes to This Policy

This policy may be updated as OmniTune Windows changes. Material privacy-impacting changes should be documented in the repository before release.

## Contact

For privacy-related questions or issues, open an issue in the GitHub repository:

```text
https://github.com/soupashh-ship-it/OmniTune-Windows/issues
```

