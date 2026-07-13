#!/usr/bin/env python3
"""Compare an OmniTune UI capture against a reference screenshot.

Outputs:
  - overlay.png: 50% reference / actual blend
  - heatmap.png: absolute RGB difference heatmap
  - metrics.json: MAE/max/threshold percentages

Optional masks are rectangular regions in physical pixels:
  --mask x,y,w,h --mask x,y,w,h
Masked pixels are excluded from metrics and darkened in the heatmap.
"""

from __future__ import annotations

import argparse
import json
from pathlib import Path

import numpy as np
from PIL import Image


def parse_mask(value: str) -> tuple[int, int, int, int]:
    parts = value.split(",")
    if len(parts) != 4:
        raise argparse.ArgumentTypeError("mask must be x,y,w,h")
    try:
        x, y, w, h = (int(part.strip()) for part in parts)
    except ValueError as exc:
        raise argparse.ArgumentTypeError("mask values must be integers") from exc
    if w <= 0 or h <= 0:
        raise argparse.ArgumentTypeError("mask width/height must be positive")
    return x, y, w, h


def load_rgb(path: Path) -> Image.Image:
    return Image.open(path).convert("RGB")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--reference", required=True, type=Path)
    parser.add_argument("--actual", required=True, type=Path)
    parser.add_argument("--output-dir", required=True, type=Path)
    parser.add_argument("--mask", action="append", default=[], type=parse_mask)
    args = parser.parse_args()

    reference = load_rgb(args.reference)
    actual = load_rgb(args.actual)
    args.output_dir.mkdir(parents=True, exist_ok=True)

    if reference.size != actual.size:
        metrics = {
            "reference": str(args.reference),
            "actual": str(args.actual),
            "dimensions_match": False,
            "reference_size": list(reference.size),
            "actual_size": list(actual.size),
        }
        (args.output_dir / "metrics.json").write_text(json.dumps(metrics, indent=2), encoding="utf-8")
        raise SystemExit(f"dimension mismatch: reference={reference.size} actual={actual.size}")

    ref_arr = np.asarray(reference, dtype=np.int16)
    act_arr = np.asarray(actual, dtype=np.int16)
    diff = np.abs(ref_arr - act_arr).astype(np.uint8)
    per_pixel = diff.mean(axis=2)

    valid = np.ones(per_pixel.shape, dtype=bool)
    for x, y, w, h in args.mask:
        x0 = max(0, x)
        y0 = max(0, y)
        x1 = min(per_pixel.shape[1], x + w)
        y1 = min(per_pixel.shape[0], y + h)
        if x0 < x1 and y0 < y1:
            valid[y0:y1, x0:x1] = False

    measured = per_pixel[valid]
    if measured.size == 0:
        raise SystemExit("all pixels are masked")

    overlay = Image.blend(reference, actual, 0.5)
    overlay.save(args.output_dir / "overlay.png")

    heat = np.zeros((*per_pixel.shape, 3), dtype=np.uint8)
    scaled = np.clip(per_pixel * 5, 0, 255).astype(np.uint8)
    heat[..., 0] = scaled
    heat[..., 1] = np.clip(scaled.astype(np.int16) // 3, 0, 255).astype(np.uint8)
    heat[..., 2] = 255 - scaled
    heat[~valid] = np.array([16, 16, 16], dtype=np.uint8)
    Image.fromarray(heat, "RGB").save(args.output_dir / "heatmap.png")

    metrics = {
        "reference": str(args.reference),
        "actual": str(args.actual),
        "dimensions_match": True,
        "size": list(reference.size),
        "masked_regions": [list(mask) for mask in args.mask],
        "measured_pixels": int(measured.size),
        "mean_absolute_rgb_error": float(measured.mean()),
        "max_pixel_mean_error": float(measured.max()),
        "pct_pixels_over_10": float((measured > 10).mean() * 100.0),
        "pct_pixels_over_20": float((measured > 20).mean() * 100.0),
        "pct_pixels_over_40": float((measured > 40).mean() * 100.0),
    }
    (args.output_dir / "metrics.json").write_text(json.dumps(metrics, indent=2), encoding="utf-8")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
