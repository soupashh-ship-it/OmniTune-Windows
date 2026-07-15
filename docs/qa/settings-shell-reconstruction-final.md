# Settings shell reconstruction final

Reference: `C:\Users\soupa\Downloads\settings\Settings Omnitune Windows.png`

Final render: `docs/qa/settings-shell-reconstruction/final/current.png`

Before render: `docs/qa/settings-shell-reconstruction/before/current.png`

## Metrics

Whole-image, unmasked comparison:

- Before mean absolute RGB error: 14.0360
- Final mean absolute RGB error: 13.2427
- Before pixels >20: 11.5972%
- Final pixels >20: 11.5605%

Whole-image metrics include truthful OmniTune content differences versus the supplied visual reference.

## Stable sample results

See `docs/qa/settings-shell-reconstruction/final/sample-deltas.md`.

Notable improvements:

- Top bar stable point: delta improved to `(0, 0, 1)`.
- Search fill: improved to `(3, 2, 3)`.
- Main background: improved from `(4, 4, 14)` to `(1, 2, 5)`.
- Sidebar mid: improved from `(2, 5, 12)` to `(0, 1, 2)`.
- Selected Settings item: improved from `(2, 2, 12)` to `(1, 1, 1)`.
- Card fills: improved to within 1-3 RGB points on sampled regions.
- Bottom-left player violet: improved from `(5, 5, 2)` to `(2, 2, 2)`.
- Bottom-right player violet: improved from `(3, 4, 1)` to `(1, 2, 1)`.

## Remaining visual differences

1. Sidebar upper atmospheric glow is still not an exact pixel match. More aggressive blue/violet tuning made the lower sidebar and overall shell feel worse, so the retained value is a compromise.
2. The reference bottom player contains active song artwork/metadata; the captured OmniTune state is idle. The placeholder was improved, but fake metadata was not added.
3. Whole-screen metrics remain affected by text/content differences and unsupported reference content.

## Verdict

Nocturne shell color fidelity was materially improved without changing product functionality, app version, release state, or unsupported content.
