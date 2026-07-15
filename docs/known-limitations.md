# OmniTune Windows known limitations

Date: 2026-07-15

This document classifies remaining known items at development freeze. These are not release blockers unless new evidence shows they break a primary user flow.

## Non-blocking known limitations

1. Now Playing retains a small known stable visual residual, including the previously measured transport-height difference.
2. Full PlayerViewModel-backed production-route Compose runtime focus coverage is not exhaustive.
3. Broader four-theme focused-state screenshot coverage is not exhaustive.
4. Physical Windows SMTC shell/flyout validation is incomplete unless separately performed on the target machine.
5. Provider behavior can change externally; representative provider QA is a smoke signal, not a permanent guarantee.
6. Browse, Radio, and Related live failure coverage is not exhaustive beyond implemented retry/stale-safety/bounds.
7. Some external artist/album facts are unavailable from reliable provider data and are not fabricated.
8. The app is unsigned without a legitimate Authenticode certificate, so Windows SmartScreen warnings may occur.

## Deferred by user

1. Clean Windows VM / no Java / no VLC validation.
2. Physical no-network installed-app offline proof.
3. Multi-hour playback soak.

## External blockers

1. Legitimate Windows code-signing certificate unavailable.
2. External screen-reader runtime validation unavailable/not performed.

## Explicitly not claimed

- Pixel-perfect 1:1 visual match.
- Permanent provider reliability.
- Physical hardware media-key proof unless separately tested.
- Clean-machine installer proof.
- Physical offline installed-app proof.
- Multi-hour playback soak stability.
- Signed Windows release.
