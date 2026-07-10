# Nocturne Prism — Visual Difference Severity Model

## Severity Levels

### SEVERITY 0 — MATCH
No meaningful difference visible. Implementation closely reproduces the reference.

### SEVERITY 1 — MICRO
Small difference such as:
- 1–3 px spacing variation
- Slightly different line height
- Small icon-position variation
- Minor font rendering difference (system font hinting)
- Sub-pixel color variation (±5 per channel)

### SEVERITY 2 — MINOR
Visible but not composition-breaking:
- Wrong 4–8 px spacing
- Modestly wrong radius (e.g. 8 px vs 12 px)
- Incorrect muted color (wrong shade of gray)
- Slightly wrong font weight (Medium vs SemiBold)
- Minor border opacity mismatch
- Slightly wrong card aspect ratio

### SEVERITY 3 — MAJOR
Clearly reduces resemblance to the reference:
- Wrong card dimensions (>20% off)
- Wrong player bar height
- Wrong sidebar width (>30 px off)
- Missing section entirely
- Incorrect layout proportions (2-column instead of 3-column)
- Incorrect typography hierarchy (body-size used for titles)
- Wrong navigation structure
- Incorrect background color family (gray instead of navy)

### SEVERITY 4 — CRITICAL
The screen no longer resembles the supplied target:
- Different architectural layout (e.g. tabs at top instead of sidebar)
- Generic Material/Fluent defaults instead of Nocturne Prism styling
- Mobile-style UI enlarged for desktop
- Major components omitted (no sidebar, no player bar)
- Incorrect design direction (teal concept mixed into purple concept)
- Old pre-redesign OmniTune Windows UI remains
- Rainbow/excessive neon gradients replacing the restrained reference palette

## Target for Phase 1 Components

- ZERO SEVERITY 4 differences
- ZERO SEVERITY 3 differences
- MINIMAL SEVERITY 2 differences

## Target for Full Implementation (later phases)

- ZERO SEVERITY 4 differences
- ZERO SEVERITY 3 differences
- MINIMAL SEVERITY 2 differences
- Severity 1 differences documented but acceptable
