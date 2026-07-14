#!/usr/bin/env python3
"""Generate stable-geometry visual diffs for OmniTune reference screens.

This wrapper separates dynamic provider content from stable chrome/layout by
applying per-screen masks from docs/qa/stable-geometry-masks.json.
"""

from __future__ import annotations

import argparse
import json
import subprocess
import sys
from pathlib import Path


def load_json(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def rect_delta(reference: list[int], actual: list[int]) -> dict:
    x1, y1, w1, h1 = reference
    x2, y2, w2, h2 = actual
    return {
        "dx": x2 - x1,
        "dy": y2 - y1,
        "dw": w2 - w1,
        "dh": h2 - h1,
        "largest_abs_delta": max(abs(x2 - x1), abs(y2 - y1), abs(w2 - w1), abs(h2 - h1)),
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--config", default="docs/qa/stable-geometry-masks.json", type=Path)
    parser.add_argument("--output-root", default="docs/qa/diff/stable-geometry", type=Path)
    parser.add_argument("--compare-script", default="scripts/qa/compare_reference_ui.py", type=Path)
    args = parser.parse_args()

    config = load_json(args.config)
    output_root = args.output_root
    output_root.mkdir(parents=True, exist_ok=True)

    summary: dict[str, object] = {
        "config": str(args.config),
        "outputRoot": str(output_root),
        "screens": {},
    }

    for screen, screen_config in config["screens"].items():
        raw_metrics_path = Path(screen_config["rawMetrics"])
        raw_metrics = load_json(raw_metrics_path)
        reference = Path(raw_metrics["reference"])
        actual = Path(screen_config["actual"])
        screen_out = output_root / screen
        screen_out.mkdir(parents=True, exist_ok=True)

        masks = [mask["rect"] for mask in screen_config.get("dynamicMasks", [])]
        command = [
            sys.executable,
            str(args.compare_script),
            "--reference",
            str(reference),
            "--actual",
            str(actual),
            "--output-dir",
            str(screen_out),
        ]
        for rect in masks:
            command.extend(["--mask", ",".join(str(value) for value in rect)])

        subprocess.run(command, check=True)
        stable_metrics = load_json(screen_out / "metrics.json")

        landmarks = []
        largest_landmark_delta = 0
        for landmark in screen_config.get("landmarks", []):
            delta = rect_delta(landmark["reference"], landmark["actual"])
            largest_landmark_delta = max(largest_landmark_delta, delta["largest_abs_delta"])
            landmarks.append(
                {
                    "name": landmark["name"],
                    "reference": landmark["reference"],
                    "actual": landmark["actual"],
                    "delta": delta,
                }
            )

        screen_summary = {
            "reference": str(reference),
            "actual": str(actual),
            "raw": {
                "mean_absolute_rgb_error": raw_metrics["mean_absolute_rgb_error"],
                "pct_pixels_over_20": raw_metrics["pct_pixels_over_20"],
                "pct_pixels_over_40": raw_metrics["pct_pixels_over_40"],
            },
            "stable": {
                "mean_absolute_rgb_error": stable_metrics["mean_absolute_rgb_error"],
                "pct_pixels_over_20": stable_metrics["pct_pixels_over_20"],
                "pct_pixels_over_40": stable_metrics["pct_pixels_over_40"],
                "measured_pixels": stable_metrics["measured_pixels"],
                "masked_regions": stable_metrics["masked_regions"],
            },
            "dynamicMasks": screen_config.get("dynamicMasks", []),
            "landmarks": landmarks,
            "largestStableLandmarkDeltaPx": largest_landmark_delta,
        }
        summary["screens"][screen] = screen_summary
        (screen_out / "landmarks.json").write_text(json.dumps(landmarks, indent=2), encoding="utf-8")

    (output_root / "summary.json").write_text(json.dumps(summary, indent=2), encoding="utf-8")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
