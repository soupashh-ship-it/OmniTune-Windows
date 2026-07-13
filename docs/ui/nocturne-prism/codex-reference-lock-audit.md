# Codex reference-lock forensic audit

Source files: target `Image 1.png` is 1672×941 (scaled 0.7703 for the required 1288×733 QA lock); current `Image 2.png` is 1426×804. Bounds below are target lock-space estimates, followed by current screenshot-space bounds.

| Area | Target bounds / proportion | Current bounds / proportion | Difference | Severity | Files responsible | Planned fix |
|---|---|---|---|---|---|---|
| Window frame | 0,0–1288,733; full frame, 13 px radius | 0,0–1426,804; almost square/full bleed | Current border/radius too subtle | Minor | `OmniWindow.kt` | Retain shell clip; tune 1 px violet edge |
| Sidebar | x 0–204; 15.8% | x 0–190; 13.3% | Too narrow proportionally in capture, but code clamps to 230 dp and will be too wide at lock | Critical | `OmniWindow.kt`, `Sidebar.kt`, theme | Use responsive 15.8%, 196–210 dp |
| Branding | x 20–105, y 21–43 | x 20–114, y 28–57 | Current mark is generic note and block is too tall | Major | `Sidebar.kt`, system icon asset | Reuse approved OmniTune icon; compact to target |
| Active nav | x 15–191, y 62–96; 34 high | x 18–166, y 83–122; 39 high | Wrong inset, width, height, and glow | Major | `Sidebar.kt` | 34–36 high full-width indigo selection with 3 px rail |
| Library hierarchy | x 22–181, y 181–354 | x 20–166, y 207–401 | Current spacing is looser and lower | Major | `Sidebar.kt` | Compact nav/subnav rhythm and divider |
| Top bar | x 204–1288, y 0–55; 7.5% h | x 190–1426, y 0–62; 7.7% h | Search and controls are underscaled/misaligned | Major | `OmniWindow.kt`, `OmniTopBar.kt` | Lock 54–56 dp bar and target paddings/search width |
| Global background | main x 204–1288, y 55–650 | flat near-black | Missing visible upper indigo atmosphere | Major | `NocturneBackdrop.kt`, theme | Tune navy base and restrained upper radial light |
| Greeting | x 231–448, y 78–100; 25 px high | x 219–480, y 101–126; 31 px | Current is too large and too low | Major | `HomeView.kt` | 24–25 sp, compact top inset |
| Hero | x 231–927, y 117–342; 54.0% w, 30.7% h | x 219–1413, y 160–411; 83.7% w | Current row consumes full width; target needs adjacent listening panel | Critical | `HomeView.kt` | 65.5/34.5 page split; 224–226 high hero |
| Hero companion rail | x 724–927, y 117–342; 29% of hero | x 1085–1413, y 160–411; 27.5% | Architecture is correct; rows and rail too large | Major | `HomeView.kt` | Preserve split; use ~203 px rail and 4 dense rows |
| Continue Listening | x 953–1257, y 116–356; 23.6% w | absent | Entire required right panel missing | Critical | `HomeView.kt` | Render real 4-item panel beside hero |
| Quick Picks | x 231–862, y 368–497 | x 219–816, y 443–596 | Wrong vertical position and card density | Major | `HomeView.kt` | Six 98×106 lock-space image-led cards |
| Made for You | x 877–1243, y 368–497 | absent | Required section missing from visible viewport | Critical | `HomeView.kt` | Three compact gradient/data cards beside Quick Picks |
| Trending Now | x 231–685, y 522–634 | absent | Required table is below viewport | Critical | `HomeView.kt` | Two dense visible rows in lower-left region |
| New Releases | x 705–1241, y 522–634 | absent | Required rail is below viewport | Critical | `HomeView.kt` | Five compact cards in lower-right region |
| Bottom player | x 8–1279, y 638–721; 98.7% w, 83 high | x 2–1426, y 719–802; 100% w, 83 high | Current empty capture, edge treatment and zones differ | Major | `OmniWindow.kt`, `OmniBottomPlayer.kt` | 82–84 dp inset dock; preserve real player bindings |
| Overall density | Greeting + 3 content rows visible above player | Greeting + hero + Quick Picks only | Large gaps/290 dp hero push half the composition offscreen | Critical | `HomeView.kt`, shell dimensions | Fixed lock-space vertical rhythm with responsive overflow |

Implementation lock: preserve the existing hero/companion split and all real `PlayerViewModel`/YouTube-backed actions; remove fake selected/progress states and avoid fake sidebar playlist metadata.
