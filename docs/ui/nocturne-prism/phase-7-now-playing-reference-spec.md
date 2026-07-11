# Nocturne Prism — Phase 7 Now Playing Reference Spec

REFERENCE RESOLUTION: 1568×882 (supplied reference image)
APPLICATION WIDTH: ~1440 px
APPLICATION HEIGHT: ~810 px
SIDEBAR WIDTH: 230 px
TOP BAR HEIGHT: 56 px
BOTTOM PLAYER HEIGHT: 80 px

---

## LAYOUT STRUCTURE

PAGE LEFT PADDING: 40 px
PAGE RIGHT PADDING: 40 px
PAGE TOP PADDING: 32 px
PAGE BOTTOM PADDING: 32 px
GAP BETWEEN PLAYER AND LYRICS: 32 px

MAIN PLAYER REGION WIDTH: approximately 55% of content area (weight(1f))
LYRICS PANEL WIDTH: 360 dp fixed

---

## ARTWORK

ARTWORK WIDTH: ~320 dp max (fillMaxWidth 72%)
ARTWORK HEIGHT: square (aspectRatio 1f)
ARTWORK RADIUS: artworkLarge = 24 dp
ARTWORK SHADOW ELEVATION: 24 dp
ARTWORK SHADOW AMBIENT: Color.Black alpha 0.35
ARTWORK SHADOW SPOT: Iris alpha 0.18
ARTWORK SCALE ANIMATION: 1.0 when playing, 0.94 when paused (spring animation)
CONFIDENCE: HIGH

---

## METADATA

NOW PLAYING LABEL SIZE: labelSmall, letterSpacing 2sp, IrisSoft color, UPPERCASE
NOW PLAYING INDICATOR: animated eq bars (5 bars) when playing
TRACK TITLE SIZE: headlineMedium, FontWeight.Bold, centered, maxLines 1
ARTIST TEXT SIZE: titleMedium, TextSecondary, centered, maxLines 1
EXPLICIT BADGE: NOT SHOWN (data unavailable from API)
CONFIDENCE: HIGH

---

## ACTION ROW

ACTION BUTTON SIZE: 36 dp circles
ACTION ICON SIZE: 22 dp
ACTIONS: FavoriteBorder | AddCircleOutline | MoreHoriz
CONFIDENCE: HIGH (layout); MEDIUM (functionality — like/add not wired)

---

## PLAYBACK VISUALIZER

TYPE: DECORATIVE — 18 animated bars, NOT real waveform data
HEIGHT: 28 dp max per bar
WIDTH PER BAR: 4 dp
BAR RADIUS: 2 dp
COLOR: Iris → IrisSoft vertical gradient
ANIMATION: InfiniteTransition, varying periods 500-950ms
PAUSED STATE: bars drop to 12% height, alpha reduces to 0.3
CONFIDENCE: HIGH (as decorative element per spec)

---

## TIMELINE

TIMELINE WIDTH: fillMaxWidth (weight(1f) in Row)
TIMELINE HEIGHT: OmniProgressSlider default (~4 dp track)
CURRENT TIME POSITION: Left of slider, 40 dp width
DURATION POSITION: Right of slider (remaining: "-MM:SS"), 40 dp width
TIME FONT: bodySmall, TextSecondary
CONFIDENCE: HIGH

---

## TRANSPORT CONTROLS

CENTRAL PLAY BUTTON DIAMETER: 60 dp
CENTRAL PLAY BUTTON SHAPE: Circle
CENTRAL PLAY BUTTON FILL: OmniGradients.irisToLavender
CENTRAL PLAY BUTTON ICON SIZE: 32 dp, Color(0xFF05060A)
CENTRAL PLAY BUTTON SCALE: 0.93f on press

TRANSPORT BUTTON SIZE: 44 dp circles
TRANSPORT ICON SIZE: ~29 dp (44dp * 0.65)
TRANSPORT BUTTON GAP: 20 dp between prev/play and play/next

SHUFFLE BUTTON SIZE: 36 dp
SHUFFLE BUTTON GAP: 24 dp from previous button
REPEAT BUTTON SIZE: 36 dp  
REPEAT BUTTON GAP: 24 dp from next button

ACTIVE INDICATOR DOT: 4 dp circle, IrisSoft, centered below icon
CONFIDENCE: HIGH

---

## LYRICS PANEL

LYRICS PANEL WIDTH: 360 dp
LYRICS PANEL RADIUS: Shapes.large = 16 dp
LYRICS PANEL BACKGROUND: Surface1 alpha 0.65
LYRICS PANEL BORDER: 1 dp, BorderLow
LYRICS PANEL PADDING: horizontal 20 dp, vertical 16 dp

LYRICS HEADER HEIGHT: Row of pill tabs (Lyrics | Queue)
TAB PILL PADDING: horizontal 16 dp, vertical 8 dp
TAB PILL ACTIVE FILL: Iris alpha 0.18
TAB PILL ACTIVE BORDER: Iris alpha 0.4
TAB PILL ACTIVE TEXT: IrisSoft
TAB PILL INACTIVE FILL: Surface2
TAB PILL INACTIVE TEXT: TextSecondary
CONFIDENCE: HIGH

ACTIVE LYRIC FONT SIZE: 19 sp
ACTIVE LYRIC WEIGHT: SemiBold
ACTIVE LYRIC COLOR: IrisSoft
INACTIVE LYRIC FONT SIZE: 16 sp (bodyLarge default)
INACTIVE LYRIC WEIGHT: Normal
PAST LYRIC ALPHA: 0.45
FUTURE LYRIC ALPHA: 0.65
LYRIC LINE GAP: 18 dp
ACTIVE LYRIC FOCAL POSITION: scrollOffset -180 (keeps active in upper-center)

CONFIDENCE: HIGH

---

## RETURN TO CURRENT LYRIC

SHAPE: Shapes.pill
BACKGROUND: Surface2 alpha 0.95
BORDER: 1 dp, Iris alpha 0.3
PADDING: horizontal 14 dp, vertical 7 dp
POSITION: BottomCenter of lyrics panel, padding 12 dp
VISIBILITY: Animated fade in/out; only visible while userIsScrolling = true
ICON: KeyboardArrowDown, 16 dp, IrisSoft
TEXT: "Return to current lyric", IrisSoft, labelMedium
CONFIDENCE: HIGH

---

## CLICK-TO-SEEK

BEHAVIOR: Clicking a synced lyric line calls player.seek(line.timeMs)
AVAILABLE: YES (synced lyrics only)
CONFIDENCE: HIGH

---

## RACE CONDITION PROTECTION

MECHANISM: lyricsJob?.cancel() on new track + capturedId guard in onSuccess
PROTECTION: Both coroutine cancellation AND stale-ID validation
CONFIDENCE: HIGH

---

## VOLUME

VOLUME CONTROL: OmniVolumeControl (existing Phase 1 component)
VOLUME ICON: VolumeUp, 18 dp, TextMuted
VOLUME PERCENTAGE: bodySmall, TextMuted, 36 dp width
CONFIDENCE: HIGH

---

## RESPONSIVE BREAKPOINTS

AT ALL SIZES: Sidebar (230dp) + TopBar (56dp) + BottomPlayer (80dp) frame always present
PLAYER + LYRICS SIDE BY SIDE: preserved at all supported widths
ARTWORK SIZING: widthIn(max=320dp) + fillMaxWidth(0.72f) — naturally adapts
LYRICS PANEL: fixed 360dp — may become slightly tight at 1280x720
AT 1280x720: content area ≈ 820dp wide; player ≈ 428dp, lyrics ≈ 360dp — usable
CONFIDENCE: MEDIUM (not runtime verified at all resolutions)
