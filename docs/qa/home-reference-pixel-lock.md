# OmniTune Home reference pixel-lock QA

Screenshots captured from the running desktop app:

- `docs/qa/home-reference-1142x654-final.png`
- `docs/qa/home-reference-1012x643-final.png`
- `docs/qa/home-reference-functional-after-play.png`
- `docs/qa/home-reference-functional-ctrlk.png`
- `docs/qa/home-reference-1142x654-final-audit.png`
- `docs/qa/home-reference-1012x643-final-audit.png`
- `docs/qa/home-reference-1142x654-final-audit-populated.png`

## 1142 × 654

| Landmark | Target | Actual | Delta | Result |
|---|---:|---:|---:|---|
| sidebar width | 180 px | 180 px | 0 | PASS |
| top bar height | 49 px | 49 px | 0 | PASS |
| greeting X | 204 px | 204 px | 0 | PASS |
| greeting Y | ~69–70 px visible glyph top | 68 px container, 69 px glyph top | 0 | PASS |
| hero X | 204 px | 204 px | 0 | PASS |
| hero Y | 104 px | 104 px | 0 | PASS |
| hero width | 624 px | 624 px | 0 | PASS |
| hero height | 203 px | 203 px | 0 | PASS |
| hero split | 440 / 184 px | 440 / 184 px | 0 | PASS |
| pager X | 780 px | 780 px | 0 | PASS |
| pager Y | ~68 px | 68 px | 0 | PASS |
| pager width | 48 px | 48 px | 0 | PASS |
| pager height | 25 px | 25 px | 0 | PASS |
| pager right-edge equality | 828 px == 828 px | YES | 0 | PASS |
| Continue Listening X | ~852 px | 852 px | 0 | PASS |
| Continue Listening width | ~270 px | 270 px | 0 | PASS |
| Quick Picks count | 6 | 6 | 0 | PASS |
| Made for You count | 3 | 3 | 0 | PASS |
| Trending presence | present | present | 0 | PASS |
| New Releases count | 5 | 5 | 0 | PASS |
| player X | ~4 px | 4 px | 0 | PASS |
| player Y | ~572 px | 572 px | 0 | PASS |
| player width | ~1134 px | 1134 px | 0 | PASS |
| player height | ~75 px | 75 px | 0 | PASS |

## 1012 × 643

Scale: `1012 / 1142 = 0.88616`.

| Landmark | Target | Actual | Delta | Result |
|---|---:|---:|---:|---|
| sidebar width | ~159.5 px | ~160 px | +0.5 | PASS |
| hero X | ~180.8 px | ~181 px | +0.2 | PASS |
| hero Y | ~92.2 px | ~92 px | -0.2 | PASS |
| hero width | ~553.0 px | ~553 px | 0 | PASS |
| hero height | ~179.9 px | ~180 px | +0.1 | PASS |
| player height | ~66.5 px | ~66–67 px | <=0.5 | PASS |
| player anchoring | bottom anchored | bottom anchored | 0 | PASS |
| overlap detected | NO | NO | 0 | PASS |
| clipping detected | NO | NO | 0 | PASS |
| desktop hierarchy preserved | YES | YES | 0 | PASS |

## Visual verdict

Close reference reconstruction with all required Home sections present and responsive scaling working at 1142 × 654 and 1012 × 643.

Remaining measurable differences:

- Real YouTube Music feed artwork/content differs from the supplied visual reference, by requirement.
- The 1142/1012 geometry captures were taken before playback; `home-reference-functional-after-play.png` verifies populated bottom-player state after Home Play Now.
- `home-reference-functional-ctrlk.png` verifies Ctrl+K still routes to Search and focuses the top search field.

## FINAL VISUAL DIFFERENCE AUDIT

Final audit screenshots:

- `docs/qa/home-reference-1142x654-final-audit.png`
- `docs/qa/home-reference-1012x643-final-audit.png`
- `docs/qa/home-reference-1142x654-final-audit-populated.png`

| Element | Target | Actual | Delta / Difference | Status |
|---|---|---|---|---|
| greeting glyph top | y ≈ 69–70 | y = 69 by bitmap threshold | 0 | PASS |
| logo | compact logo in sidebar header | compact 26 px mark, unchanged major position | visually aligned | PASS |
| selected Home | x ≈ 10, y ≈ 54, w ≈ 158, h ≈ 31 | x ≈ 10, y ≈ 54, w ≈ 160, h 31 | width +2 px | PASS |
| top nav buttons | 24 × 24, subdued circles | 24 × 24, subdued circles | no visible mismatch | PASS |
| search | x ≈ 376, y ≈ 11, w ≈ 515, h ≈ 28 | x ≈ 376, y ≈ 11, w ≈ 515, h ≈ 28 | 0 | PASS |
| hero typography | restrained 20 sp title/compact metadata | restrained hierarchy, no oversized title | content text differs by real data | PASS |
| hero controls | 75 × 31 Play Now, 31 × 31 overflow | visually matched | no visible mismatch | PASS |
| Continue Listening row geometry | 4 rows, ~46 px high, ~3 px gap | 4 rows, ~46 px high, ~3 px gap | 0 | PASS |
| Quick Picks geometry | 6 compact cards | 6 compact cards | 0 | PASS |
| Made for You geometry | 3 compact cards with See all | 3 compact cards with See all | See all restored | PASS |
| Trending geometry | compact ranked rows | compact ranked rows | no visible mismatch | PASS |
| New Releases geometry | 5 compact cards with See all | 5 compact cards with See all | See all restored | PASS |
| player internal geometry | populated metadata, controls, seek, volume, queue | verified in populated capture | real track data differs | PASS |
| major colors | near-black navy target palette | main/search/sidebar/player samples remain in target palette | anti-aliased/gradient samples vary | PASS |
