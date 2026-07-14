# OmniTune Windows corruption-recovery matrix

Date: 2026-07-14

Scope: local persistence stores that can affect startup, library state, playback history, sessions, and downloads.

## Matrix

| Store | Corruption scenario | Expected behavior | Evidence | Result |
|---|---|---|---|---|
| Atomic JSON writes | Direct primary truncation during save | Write temp file, fsync, preserve `.bak`, replace primary | `AtomicFileStoreTest` | PASS |
| `downloads-index.json` | Invalid JSON | Preserve corrupt file as `downloads-index.corrupt-*.json`, start with empty download index, do not crash | `OmniDownloadManagerTest.corruptIndexIsPreservedAndManagerStartsEmpty` | PASS |
| `downloads-index.json` | Corrupt primary + valid `.bak` | Preserve corrupt primary and recover tasks from `.bak` | `OmniDownloadManagerTest.corruptIndexRecoversFromValidBackup` | PASS |
| `downloads-index.json` | Valid records mixed with malformed records | Preserve valid records, skip malformed/blank required-field records, dedupe IDs | `OmniDownloadManagerTest.malformedDownloadRecordsAreSkippedWithoutDroppingGoodRecords` | PASS |
| `downloads-index.json` | Completed task points to missing file | Restore task as `FAILED`, reject local playback source | Existing desktop tests | PASS |
| `downloads-index.json` | Completed task points to empty file | Restore task as `FAILED`, reject local playback source | Existing desktop tests | PASS |
| `downloads-index.json` | Active task at startup | Restore active task as `PAUSED` instead of falsely running | Existing desktop tests | PASS |
| `savedQueuePlaylists.json` | Invalid JSON with Preferences fallback available | Preserve corrupt JSON backup, load fallback playlist data | `SettingsRepositoryTest.corruptJsonStoresFallBackToPreferencesAndArePreserved` | PASS |
| `savedQueuePlaylists.json` | Corrupt primary + valid `.bak` | Preserve corrupt primary and recover from `.bak` before Preferences | `SettingsRepositoryTest.corruptPrimaryJsonUsesValidBackupBeforePreferences` | PASS |
| `savedQueuePlaylists.json` | Valid records mixed with malformed/duplicate records | Preserve valid playlist, skip missing ID/name, dedupe IDs | `SettingsRepositoryTest.malformedPlaylistHistoryAndSessionRecordsAreSkippedWithoutDroppingGoodRecords` | PASS |
| `playbackHistory.json` | Invalid JSON with Preferences fallback available | Preserve corrupt JSON backup, load fallback history data | `SettingsRepositoryTest.corruptJsonStoresFallBackToPreferencesAndArePreserved` | PASS |
| `playbackHistory.json` | Valid records mixed with malformed/duplicate records | Preserve valid history, skip missing entry/song IDs, dedupe IDs | `SettingsRepositoryTest.malformedPlaylistHistoryAndSessionRecordsAreSkippedWithoutDroppingGoodRecords` | PASS |
| `playbackSessions.json` | Invalid JSON with Preferences fallback available | Preserve corrupt JSON backup, load fallback session data | `SettingsRepositoryTest.corruptJsonStoresFallBackToPreferencesAndArePreserved` | PASS |
| `playbackSessions.json` | Valid records mixed with malformed/duplicate records | Preserve valid session, skip missing ID, dedupe IDs | `SettingsRepositoryTest.malformedPlaylistHistoryAndSessionRecordsAreSkippedWithoutDroppingGoodRecords` | PASS |
| Recent searches Preferences JSON | Invalid/corrupt string | Fall back to empty history rather than crash | Existing safe parser behavior | PASS |
| Invalid theme enum/string | Unknown values do not crash theme rendering; default path remains Nocturne-compatible | Source audit; final build/test required | PARTIAL |
| Missing download directory | Manager recreates required directories on demand | Source audit; runtime edge case not manually forced | PARTIAL |
| Permission denied for app-data writes | Failure is not fully user-facing yet | Not implemented as a full UX recovery path | FAIL |

## Implementation notes

- `FileBackedOmniDownloadManager` already preserved corrupt download indexes and restored invalid completed tasks truthfully.
- `AtomicFileStore` now writes important JSON state through temp-file + fsync + replace semantics and keeps a last-known `.bak`.
- `SettingsRepository` now performs recoverable JSON reads:
  1. Try app-data JSON file.
  2. If JSON parsing fails, preserve a timestamped `.bak` copy.
  3. Try the last-known atomic `.bak`.
  4. Fall back to the corresponding Java Preferences JSON key.
  5. If fallback also fails, return an empty list rather than crashing.
- `FileBackedOmniDownloadManager` now attempts recovery from `downloads-index.json.bak` when the primary index is corrupt.

## Validation

Command:

```powershell
.\gradlew.bat :composeApp:desktopTest
```

Result: PASS after adding the recovery tests.

## Remaining gaps

1. Permission-denied and no-disk-space error UX is not complete.
2. Full malformed preference enum matrix was not exhaustively tested.
3. Corruption recovery is backend-tested; not all failure states have polished user-facing recovery UI.
4. Record-level recovery is intentional for playlists, playback history, playback sessions, and downloads index. Malformed root JSON remains file-level/backup-level recovery.

## Verdict

Corruption recovery: **PARTIAL PASS**.

Boot-blocking corruption in primary JSON stores is now covered with backup recovery, and common malformed-record cases preserve good records; storage-permission, disk-full, and arbitrary schema repair remain release-hardening work.
