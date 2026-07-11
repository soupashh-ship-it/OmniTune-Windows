# Nocturne Prism Hero, Sidebar, and Player Reference Lock Map

## 1. Featured Hero
- **CURRENT STRUCTURE**: Oversized main artwork, translucent pills for companion items floating on top of the image.
- **TARGET STRUCTURE**: 70/30 split. Left side is cinematic main artwork with dark overlay and Play Now button. Right side is a dedicated dark navy column housing 4 companion tracks.
- **PLANNED CHANGE**: Rebuilt `HomeHeroRow` utilizing a `Row` container with `weight(2.65f)` for the main image and `weight(1f)` for the companion column. Used `NocturneColors.DeepestBase` overlays.
- **FUNCTIONAL RISK**: Low. Playback logic preserved.
- **REGRESSION CHECK**: Run and verify space distribution.

## 2. Left Sidebar
- **CURRENT STRUCTURE**: Flat gradient background. Generic Material music note icon. Basic active pill state. Library children not visually nested.
- **TARGET STRUCTURE**: Deep blue-black foundation with subtle upper-radial atmosphere. Real OmniTune branding. Indigo active tab with 3dp iris left accent. 1dp child guide line for Library sub-items.
- **PLANNED CHANGE**: Replaced flat background with layered `drawBehind` radial glow and vertical gradients. Replaced missing logo asset with custom Compose gradient icon. Added `1.dp` vertical line.
- **FUNCTIONAL RISK**: Low.
- **REGRESSION CHECK**: Verify navigation state highlighting and Library visual hierarchy.

## 3. Persistent Bottom Player
- **CURRENT STRUCTURE**: Thin footer bar. Uniformly spaced controls.
- **TARGET STRUCTURE**: 110-114px tall floating dock (12dp radius). 3 distinct zones (Left metadata, Center transport, Right utilities). Central Play/Pause is dominant (46dp). Thin 2dp timeline positioned underneath.
- **PLANNED CHANGE**: Updated `OmniBottomPlayer` with explicit `weight()` assignments. Moved timeline to center zone. Applied `playerBrush` gradient. Updated `OmniProgressSlider` to support `2.dp` track height.
- **FUNCTIONAL RISK**: Medium. Seek/timeline behavior requires accurate slider tracking.
- **REGRESSION CHECK**: Verify playback seek, pause/play visual changes, and timeline synchronization.
