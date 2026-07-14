# OmniTune Windows local-feasible remaining work final

Date: 2026-07-14

| Area | Status | Evidence | Remaining gap |
|---|---|---|---|
| Legacy migration | PROVEN | `PlatformContextMigrationTest`; allowlisted marker-based migration | Clean older-RC matrix on VM remains deferred. |
| Atomic persistence | PROVEN | `AtomicFileStoreTest`; settings/downloads use atomic writes; simulated temp-write and replace failures preserve the primary | Full physical disk-full validation remains deferred/not performed. |
| Corruption recovery | PROVEN/PARTIAL | Settings and download corrupt-primary + backup tests; mixed valid/malformed record tests preserve good records | Arbitrary unsupported schemas and every possible enum/type variant remain partial. |
| Permission denied | PARTIAL PASS | Atomic write-denial simulation; queue Save as Playlist failure stays open and shows `Couldn't save playlist.` | Full UI failure matrix for history/session/download destination errors remains incomplete. |
| No disk space | PARTIAL PASS | Simulated no-space before atomic replace preserves the primary and backup | Physical disk-full proof and full media-download no-space UX remain incomplete. |
| Keyboard traversal | PARTIAL | `keyboard-traversal-matrix.md`; core shortcuts implemented | Full manual Tab/Shift+Tab traversal not complete. |
| Focus | PARTIAL PASS | Shared Omni components now render a theme-aware focus ring; search focus exists | Full visible focus QA across every custom per-screen clickable/dialog remains pending. |
| Screen-reader semantics | PARTIAL | Action icon descriptions improved; source scan clean for clickable null icons | External NVDA/Narrator test not performed. |
| Semantic interaction audit | PARTIAL | `semantic-interaction-audit.md`; no known dead app-owned controls in matrix | Full runtime walkthrough of every visible control remains pending. |
| Visual pixel-lock | PARTIAL | `docs/qa/diff/premium-completion/*/metrics.json` | Measured differences remain; not reference-locked. |
| Responsive behavior | PARTIAL | Existing captures and prior QA | Fresh responsive capture matrix not rerun after this code pass. |
| Theme consistency | PARTIAL | Existing theme screenshots; Nocturne protected | Fresh all-theme sweep not rerun after this code pass. |
| Clean VM/no Java/no VLC | DEFERRED BY USER | Explicit instruction | Needs clean target later. |
| Physical no-network installed playback | DEFERRED BY USER | Explicit instruction | Needs OS-network-off installed build test later. |
| Multi-hour playback soak | DEFERRED BY USER | Explicit instruction | Needs long-running soak later. |
| Actual code signing | EXTERNAL BLOCKER | Secure hook exists; no certificate available | Requires real code-signing cert. |

## Summary

This local pass closed the concrete migration caveat, strengthened JSON persistence/recovery, added simulated storage-failure tests, and fixed the queue playlist false-success path. It also documented the accessibility, keyboard, semantic interaction, and visual state without overstating runtime proof.

## Remaining app-owned local gaps

1. Full permission-denied and no-disk-space user-facing UX across history/session/download destination paths.
2. Full keyboard traversal and focus restoration QA.
3. Full visual pixel-lock iteration.
4. Full responsive/theme screenshot sweep after visual work.
5. Full runtime semantic interaction walkthrough.

## Deferred items

1. Clean Windows VM/no Java/no VLC.
2. Physical no-network installed-app offline playback.
3. Multi-hour playback soak.

## External blockers

1. Actual Windows code signing certificate unavailable.
