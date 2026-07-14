# OmniTune Windows storage failure UX audit

Date: 2026-07-14

Scope: permission-denied and no-space failure behavior in the current local environment.

## Result summary

| Scenario | Simulation method | Current behavior | Result |
|---|---|---|---|
| JSON primary truncation/interrupted write | Unit-level atomic file tests | Temp write + fsync + replace prevents truncating primary before successful replacement | PASS |
| Valid primary + stale temp | Unit-level atomic file test | Stale temp does not block later valid write | PASS |
| Corrupt primary + valid backup | Unit-level settings/download tests | Backup recovery works | PASS |
| Playlist JSON write denied | `AtomicFileStore.withFailurePolicyForTest` | Save throws through `SettingsRepository`; queue dialog keeps dialog open and shows `Couldn't save playlist.` | PASS |
| Playlist/history/session write denied | Simulated at atomic write layer for playlist; history/session use same store path | Backend logs write failures and propagates file write failures instead of silently reporting success | PARTIAL |
| Downloads index write denied | Simulated at atomic write layer | Atomic write failure throws/logs and preserves prior primary | PASS/PARTIAL |
| Download destination permission denied | Not safely simulated in this pass | Download manager reports task failure for thrown write errors, but full concise UX proof pending | PARTIAL |
| Disk full / ENOSPC | `AtomicFileStore.withFailurePolicyForTest` before replace | Existing primary and backup survive simulated no-space failure; caller receives failure | PARTIAL PASS |

## What changed

- Important JSON stores now use `AtomicFileStore`.
- Atomic writes keep a last-known `.bak`.
- Settings and downloads can recover from corrupt primary files using backups.
- Settings JSON write failures now propagate instead of being silently swallowed.
- Queue Save as Playlist no longer closes the dialog on save failure.
- Simulated no-space before atomic replacement is covered by `AtomicFileStoreTest`.

## What is not claimed

- No physical disk-full proof was performed.
- No destructive permission changes were made to real user directories.
- No complete user-facing failure message matrix was proven for every history/session/download edge case.
- No media-download ENOSPC path was physically or fully UI-tested.

## Verdict

Storage failure UX: **PARTIAL PASS**.

Backend persistence is safer and playlist false-success was fixed; polished permission-denied/no-space UX for every write/download path remains future hardening work.
