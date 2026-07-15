# OmniTune Windows development freeze

Date: 2026-07-15

Active feature development for OmniTune Windows is frozen at the current source state.

This document is a closure record, not a roadmap. The product is considered ready for the user's release/quality decision after final validation, packaging, and local commit.

## Current implemented product areas

- Home, Browse, Radio, Search, Library, Playlist Detail, Artist, Album
- Now Playing, Lyrics, Related, Queue, Downloads, Settings
- Mini Player and Bottom Player
- Four themes: Nocturne Prism, Midnight, Dusk, Aurora
- Provider-backed search and discovery surfaces
- Queue Save as Playlist and playlist persistence
- Playback history, sessions, and meaningful-listening tracking
- Playback recovery, seek clamping, VLC-backed playback, local-first playback
- Download pause/resume/retry/delete, restoration, and media-write failure handling
- Atomic JSON persistence, backup recovery, corruption handling, and legacy migration
- Radio session identity, dedupe, recent-repeat suppression, continuation, stale-session protection, and queue bounds
- Related loading/failure/retry, stale-track protection, dedupe, current-track exclusion, and result cap
- Windows media-session/SMTC implementation through the existing JMTC dependency
- Keyboard shortcut routing, repeat suppression, editable-text safety, and Compose Desktop runtime UI-test capability
- EXE/MSI packaging with private Java runtime and embedded VLC fallback
- `%LOCALAPPDATA%\OmniTuneData` app-data separation
- Canonical visual-reference and stable-geometry QA tooling

## Deferred by user

1. Clean Windows VM / no Java / no VLC validation.
2. Physical no-network installed-app offline proof.
3. Multi-hour playback soak.

## External blockers

1. Legitimate Windows code-signing certificate is unavailable.
2. External screen-reader runtime validation may remain unavailable/not performed.

## Non-blocking known limitations

- Current source version is whatever is recorded in `gradle.properties`; this freeze pass does not change it.
- Provider behavior can change externally and cannot be permanently guaranteed.
- Physical Windows SMTC shell/flyout validation is not claimed unless separately performed.
- Full PlayerViewModel-backed route Compose-runtime focus coverage is not exhaustive.
- Now Playing retains a small known stable visual residual rather than risk regression.
- Broader live Browse/Radio/Related failure coverage is not exhaustive.

## Freeze rule

After this point, only release blockers should be fixed:

- compile failure
- critical test regression
- app launch failure
- fundamental playback/search/installer failure
- primary-flow crash
- introduced data corruption risk
- missing completed source state

Do not continue feature polishing, visual chasing, additional QA framework expansion, or optional architecture work during the freeze.
