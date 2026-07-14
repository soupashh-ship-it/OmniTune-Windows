# OmniTune Windows storage failure UX

Date: 2026-07-14

This document summarizes the current local storage-failure hardening status.

| Area | Evidence | Result |
|---|---|---|
| Atomic JSON temp-write failure | `AtomicFileStoreTest.simulatedPermissionDeniedBeforeTempWritePreservesPrimary` | PASS |
| Atomic JSON replace/no-space failure | `AtomicFileStoreTest.simulatedNoSpaceBeforeReplacePreservesPrimaryAndCreatesBackup` | PASS |
| Queue Save as Playlist write failure | `SettingsRepositoryTest.playlistSaveFailureDoesNotCreateFalseSuccessState`; dialog reports `Couldn't save playlist.` | PASS |
| Playlist/history/session JSON write propagation | Shared `SettingsRepository.writeJsonFile()` now propagates atomic write failures instead of silently swallowing them | PARTIAL PASS |
| Downloads index write failure | Shared atomic index write path preserves prior primary and logs failure | PARTIAL PASS |
| Media write permission denied message | `OmniDownloadManagerTest.downloadWritePermissionErrorsUseConciseUserMessage` maps permission/path failures to `Download location is unavailable.` | PASS |
| Media write no-space message | `OmniDownloadManagerTest.downloadWriteNoSpaceErrorsUseConciseUserMessage` maps no-space failures to `Not enough storage space.` | PASS |
| Download media write permission denial | `OmniDownloadManagerTest.mediaWritePermissionDeniedFailsTaskWithoutFalseCompletionAndCanRetry` executes the real manager download path through a local HTTP server and injected writer failure | PASS |
| Download media write ENOSPC/no-space | `OmniDownloadManagerTest.mediaWriteNoSpaceFailsTaskWithoutFalseCompletion` executes the real manager download path through a local HTTP server and simulated no-space write failure | PASS |
| Resume-time media write failure | `OmniDownloadManagerTest.resumedPartialWriteFailureRemainsFailedAndKeepsPartialTruthful` verifies partial resume failure does not become completed | PASS |
| Finalization rename/move failure | `OmniDownloadManagerTest.finalizationFailureDoesNotCreateFalseCompletedDownloadAndRestoresFailed` verifies finalization failure is failed and remains failed after manager recreation | PASS |

## User-facing policy

- User-triggered playlist save failures must not show false success.
- Previous valid JSON state must survive failed writes.
- Raw `IOException` text should stay in technical logs, not normal UI.
- Disk-full testing in this pass is simulated through injected no-space write failures, not physical disk-full proof.

## Remaining gap

Physical disk-full and destructive permission-denied testing were intentionally not performed. The app-owned download path now has controlled end-to-end failure execution coverage for permission denial, no-space, resume-time write failure, and finalization failure without corrupting user data or producing false completed downloads.
