# Nocturne Prism — Reference Measurements

All measurements derived from reference images at 1568×882 px (≈16:9).
Values normalized to approximate logical pixels assuming 1440×810 visible application content within a standard Windows frame.

## Global Shell (consistent across all screens)

| Element               | Value       | Confidence |
|----------------------|-------------|------------|
| Sidebar width        | 230 px      | HIGH       |
| Top bar height       | 56–64 px    | HIGH       |
| Bottom player height | 80–90 px    | HIGH       |
| Content left margin  | 32–40 px    | MEDIUM     |
| Content right margin | 24–40 px    | MEDIUM     |
| Minimum window size  | 1024×640    | LOW        |

---

## SCREEN: Home / Discover (Image #9)

REFERENCE RESOLUTION: 1568×882
SIDEBAR WIDTH: 230 px
TOP BAR HEIGHT: 56 px
BOTTOM PLAYER HEIGHT: 80 px
MAIN LEFT PADDING: 32 px
MAIN RIGHT PADDING: 24 px
CONTENT MAX WIDTH: unconstrained (fills available)
PRIMARY GRID: 6-column (Quick Picks)
SECONDARY GRID: 3-column (Made for You)
MAJOR GAP: 28–32 px (between sections)
MINOR GAP: 16 px (between cards)
CARD RADIUS: 8–12 px
CONTROL RADIUS: 10–12 px
BUTTON HEIGHT: 40 px
SONG ROW HEIGHT: 44–48 px
ALBUM ART SIZE: 140×140 px (New Releases cards)
MAIN TITLE SIZE: 32–36 px ("Good evening, Alex")
SECTION TITLE SIZE: 18–20 px ("Quick Picks", "Trending Now")
BODY SIZE: 14 px
METADATA SIZE: 12–13 px

### Region: Featured Hero
x: 32 px from sidebar
y: ~80 px from top bar
width: ~740 px
height: ~260 px
radius: 12–16 px
CONFIDENCE: MEDIUM

### Region: Continue Listening (right panel)
x: ~1080 px from left
y: ~70 px from top bar
width: ~300 px
height: ~310 px
card height: ~56 px per item
art size: 48×48 px
CONFIDENCE: MEDIUM

### Region: Quick Picks
y: ~370 px from top bar
card width: ~150 px
card height: ~200 px (art + text)
art size: ~150×150 px square
card radius: 8 px
gap: 16 px
CONFIDENCE: HIGH

### Region: Made for You
card width: ~180 px
card height: ~220 px
card radius: 12 px
play button: 36 px diameter, iris gradient
CONFIDENCE: MEDIUM

### Region: Trending Now
row height: 48 px
number width: 24 px
art size: 40×40 px
CONFIDENCE: HIGH

### Region: New Releases
card width: ~140 px
card height: ~190 px (art + text)
art radius: 8 px
explicit badge: 16×16 px
CONFIDENCE: HIGH

---

## SCREEN: Search & Discovery (Image #10)

REFERENCE RESOLUTION: 1568×882
SIDEBAR WIDTH: 230 px
TOP BAR HEIGHT: 56 px
BOTTOM PLAYER HEIGHT: 80 px

### Region: Page Search Field
x: centered in main content
width: ~740 px
height: 48–52 px
radius: 12–14 px
icon size: 20 px
placeholder size: 14 px
bg: Surface2 (#151B31)
CONFIDENCE: HIGH

### Region: Recent Searches
chip height: 32 px
chip radius: 16 px (pill)
chip padding: 12 px horizontal
gap: 8 px
CONFIDENCE: HIGH

### Region: Explore Genres
chip height: 36 px
chip radius: 18 px (pill)
icon size: 18 px
gap: 10 px
CONFIDENCE: HIGH

### Region: Top Result Card
width: ~280 px
height: ~200 px
radius: 12 px
art size: ~120×120 px
play button: 48 px diameter
CONFIDENCE: MEDIUM

### Region: Songs Results
row height: 44–48 px
art size: 36×36 px
title size: 14 px
duration size: 13 px
CONFIDENCE: HIGH

### Region: Right Panel (Trending/Artists/Albums/Playlists)
width: ~300 px
artist circle: 36 px diameter
album card: ~80×80 px art
playlist card: ~60×60 px art
trending row height: 32–36 px
CONFIDENCE: MEDIUM

### Region: Discover Something New
card width: ~160 px
card height: ~180 px
radius: 12 px
CONFIDENCE: MEDIUM

---

## SCREEN: Library (Image #1)

REFERENCE RESOLUTION: 1568×882

### Region: Tab Row
tab height: 36 px
tab radius: 18 px (pill)
active tab: iris fill (#8178FF)
inactive tab: Surface3 (#191F38)
gap: 8 px
CONFIDENCE: HIGH

### Region: Pinned Collections
card width: 190–195 px
card height: 110–115 px
radius: 12 px
CONFIDENCE: HIGH

### Region: Recent Additions
art size: 140×140 px
art radius: 8 px
play overlay: 36 px
CONFIDENCE: HIGH

### Region: All Songs Table
row height: 56 px
art size: 40×40 px
art radius: 4 px
column header size: 11–12 px uppercase
CONFIDENCE: HIGH

---

## SCREEN: Playlist Detail (Image #2)

REFERENCE RESOLUTION: 1568×882

### Region: Playlist Header
art size: 260×260 px
art radius: 8 px
title size: 40–48 px bold
type label: 11–12 px uppercase
CONFIDENCE: HIGH

### Region: Action Row
Play button: 120 px wide, 44 px tall, pill radius
Shuffle button: 130 px wide, 44 px tall
Icon buttons: 44×44 px circle
CONFIDENCE: HIGH

### Region: Mood Chips
height: 32 px
radius: 16 px
CONFIDENCE: HIGH

### Region: Track Table
row height: 56–60 px
art size: 40×40 px
CONFIDENCE: HIGH

### Region: Right Sidebar (More like...)
width: ~250 px
thumbnail: 56×56 px
radius: 8 px
CONFIDENCE: MEDIUM

---

## SCREEN: Queue & Session (Image #6)

REFERENCE RESOLUTION: 1568×882

### Layout: 3-column
Left (Up Next): ~540 px
Center (Session History + Recently Played): ~340 px
Right (After Queue Ends): ~300 px

### Region: Up Next
queue row height: 52–56 px
art size: 40×40 px
number width: 24 px
current row highlight: subtle Surface2 bg
CONFIDENCE: HIGH

### Region: Queue Controls
chip width: ~130 px
chip height: ~48 px
chip radius: 10 px
gap: 16 px
CONFIDENCE: MEDIUM

### Region: Session History Cards
art size: 56×56 px
card height: 60 px
play button: 32 px
CONFIDENCE: HIGH

### Region: Recommendation Cards (After Queue)
art size: 40×40 px
card height: 48 px
"Add" button: ~56 px wide, 28 px tall, pill
CONFIDENCE: HIGH

---

## SCREEN: Settings & Personalization (Image #7)

REFERENCE RESOLUTION: 1568×882

### Layout: 3-column card grid
Column 1: Account, Appearance, Keyboard Shortcuts
Column 2: Audio Quality, Downloads, About
Column 3: Playback, Notifications

### Region: Settings Cards
radius: 16–20 px
padding: 20–24 px
bg: Surface1 (#11172A)
border: 1 px BorderLow
CONFIDENCE: HIGH

### Region: Audio Quality Segment
segment height: 36 px
segment radius: 8 px
active bg: iris
inactive bg: Surface3
CONFIDENCE: HIGH

### Region: Toggle Switch
width: ~44 px
height: ~24 px
on color: iris
CONFIDENCE: HIGH

### Region: Theme Chips
height: 36 px
radius: 18 px (pill)
CONFIDENCE: HIGH

### Region: Accent Color Circles
diameter: 28 px
gap: 8 px
CONFIDENCE: HIGH

---

## SCREEN: Downloads & Offline (Image #3/8)

REFERENCE RESOLUTION: 1568×882

### Region: Summary Stat Cards (top)
5 cards across
card height: ~90 px
card radius: 12 px
gradient bg (each card different hue)
CONFIDENCE: HIGH

### Region: Filter Tabs
tab height: 32 px
tab radius: 16 px (pill)
active: iris
CONFIDENCE: HIGH

### Region: Downloaded Songs Table
row height: 52 px
art size: 40×40 px
bitrate badge: text inline
CONFIDENCE: HIGH

### Region: Downloaded Albums
card width: ~200 px
card height: ~140 px
radius: 12 px
CONFIDENCE: MEDIUM

### Region: Smart Offline Mixes
card width: ~160 px
card height: ~120 px
radius: 12 px
CONFIDENCE: MEDIUM

### Region: Right Panel (Device Storage + Quality)
width: ~280 px
storage bar: full width, 8 px tall
radio button size: 16 px
CONFIDENCE: MEDIUM

---

## SCREEN: Now Playing + Lyrics (Image #5)

REFERENCE RESOLUTION: 1568×882

### Region: Album Art
width: ~520 px
height: ~410 px
radius: 12–16 px
CONFIDENCE: MEDIUM

### Region: Transport Controls
play/pause button: 64 px diameter
skip buttons: 36 px
shuffle/repeat: 24 px
progress bar: 4 px track, 12 px thumb
CONFIDENCE: HIGH

### Region: Lyrics Panel
width: ~530 px
active line size: 32 px bold
inactive line size: 18–20 px
line spacing: 50–60 px
"SYNCED" badge: pill, iris bg
tab labels: 16 px
CONFIDENCE: HIGH

---

## Bottom Player Bar (all screens)

| Element                | Value           | Confidence |
|-----------------------|-----------------|------------|
| Total height          | 80 px           | HIGH       |
| Album art             | 52×52 px        | HIGH       |
| Art radius            | 6 px            | HIGH       |
| Art-to-text gap       | 12 px           | HIGH       |
| Song title size       | 13–14 px medium | HIGH       |
| Artist size           | 12 px regular   | HIGH       |
| Play/Pause button     | 42–48 px circle | HIGH       |
| Skip buttons          | 28–30 px        | HIGH       |
| Shuffle/Repeat icons  | 22 px           | HIGH       |
| Progress bar height   | 4 px            | HIGH       |
| Progress thumb        | 12 px           | HIGH       |
| Volume slider width   | 100–120 px      | HIGH       |
| Volume icon           | 20 px           | HIGH       |
| Left/Right padding    | 16–20 px        | HIGH       |
