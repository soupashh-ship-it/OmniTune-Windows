# Settings shell color reference analysis

Reference: `C:\Users\soupa\Downloads\settings\Settings Omnitune Windows.png`

Resolution: 1672 x 941.

Purpose: use the supplied Settings image as the primary Nocturne Prism shell-color authority. Unsupported/fake product content in the image was not copied.

## Region samples

Representative dark-region sampling found:

- Top bar: clustered around `#010613`, `#020713`.
- Search field: around `#090E1D`, with darker composited areas around `#060C1A`.
- Main/card shell: common family `#030917`, `#080E1D`, `#090F1F`, `#0A1020`.
- Sidebar base: median around `#050C1D`; upper area has a blue/violet atmospheric region.
- Selected sidebar: deep indigo family, roughly `#1D135D` to `#24247A`.
- Accent violet: restrained `#604CE0` / `#6466D8` family.
- Bottom player: layered dark navy/violet family around `#080E1D`, `#0B0E21`, `#0F0C24`, `#140E27`.

## Interpretation

The reference is not a flat theme. It is a dark navy base with restrained, region-specific compositing:

- top bar is nearly black and blue-biased;
- main surface/card separation is subtle;
- sidebar atmosphere should remain dark and not become neon;
- bottom player needs dark navy base plus low-opacity violet zones.

## Uncertainty notes

Single-pixel samples can hit text, icons, antialiasing, or dynamic content. Stable surface tuning used region samples plus direct visual crop checks, especially for the bottom-left player area.
