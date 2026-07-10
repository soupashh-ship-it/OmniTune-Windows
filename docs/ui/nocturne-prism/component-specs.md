# Nocturne Prism — Component Specifications

## OmniSurface
- **Variants:** Base (BgDeep), Raised (BgDark), Card (Surface1), Overlay (PlayerDock)
- **Borders:** Low opacity (#1F1B35) for elevated cards
- **Shadows:** Minimal, usually 4.dp for cards and 8.dp for player dock

## OmniPrimaryButton
- **Height:** 40 px
- **Radius:** 999 px (Pill)
- **Background:** `primaryAction` gradient (Iris to VioletSoft)
- **Hover/Press:** `pressBounce` scale reduction (0.97f)
- **Label:** `labelLarge` bold, white

## OmniSecondaryButton
- **Height:** 40 px (internal padding ~18px horizontal, 10px vertical)
- **Radius:** 8-10 px (small)
- **Background:** Surface3 with 0.6 alpha
- **Border:** 1 px BorderLow

## OmniIconButton
- **Container Size:** 36-40 px circle
- **Icon Size:** 20 px
- **Tint:** TextPrimary or AccentPrimary
- **Press state:** Ripple without bound, scale reduction

## OmniSearchField
- **Height:** 48-52 px
- **Radius:** 12-14 px (small/medium)
- **Background:** Surface2
- **Border:** 1 px BorderLow, transitions to Iris on focus
- **Internal Padding:** 14 px horizontal
- **Shortcut hint:** Right-aligned, Surface3 background, `labelMedium`

## OmniMediaCard
- **Art Ratio:** 1:1 square
- **Radius (card):** 14 px (medium)
- **Radius (art):** 10 px (artworkSmall) or 16 px
- **Play Overlay:** Visible on hover, Iris gradient circle
- **Background:** Surface1

## OmniSongRow
- **Height:** ~48-56 px
- **Internal Spacing:** 8-12 px between elements
- **Art Size:** 40x40 px, 4-6 px radius
- **Current state:** Subtle Surface2 background or active icon indication

## OmniProgressSlider
- **Track Height:** 4 px
- **Thumb:** 12 px circle, Iris color
- **Active Track:** Iris color

## OmniVolumeControl
- **Width:** 100-120 px
- **Interaction:** Hover to reveal thumb or thicker active track
- **Active Track:** Iris color

## OmniSectionHeader
- **Title Size:** `headlineMedium` (20 px, SemiBold)
- **Action Label:** `labelLarge`, IrisSoft color, clickable pill surface
