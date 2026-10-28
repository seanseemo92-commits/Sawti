#!/usr/bin/env python3
"""Crop user-supplied images to square 512px WebP Android drawable assets.

Install once: python -m pip install Pillow
Usage: python scripts/prepare_webp_assets.py --input-dir path/to/source_photos

Put original files named img_cat_family.jpg, word_banana.png, etc. in
--input-dir. The script finds JPEG, PNG, or WebP sources sharing a desired stem.
It never downloads or generates photos, and it does not run during a build.
"""
from __future__ import annotations

import argparse
from pathlib import Path
from PIL import Image, ImageOps

ASSETS = (
    "img_cat_cuisine", "img_cat_family", "img_cat_daily",
    "img_cat_routine", "img_cat_vegetables", "img_cat_fruits",
    "word_banana", "word_grapes", "word_tea", "word_water",
)
SUPPORTED = {".jpg", ".jpeg", ".png", ".webp"}
DEFAULT_DRAWABLE = Path(__file__).resolve().parents[1] / "app/src/main/res/drawable"


def prepare(input_dir: Path, output_dir: Path, quality: int) -> int:
    if not input_dir.is_dir():
        raise SystemExit(f"Input folder does not exist: {input_dir}")
    output_dir.mkdir(parents=True, exist_ok=True)
    indexed = {
        p.stem.lower(): p for p in sorted(input_dir.iterdir())
        if p.is_file() and p.suffix.lower() in SUPPORTED
    }
    converted = 0
    for stem in ASSETS:
        source = indexed.get(stem)
        if source is None:
            print(f"SKIP {stem}: no source photo in {input_dir}")
            continue
        destination = output_dir / f"{stem}.webp"
        with Image.open(source) as original:
            original = ImageOps.exif_transpose(original)
            # Center crop (not stretch); preserve transparency where present.
            original = original.convert("RGBA" if "A" in original.getbands() else "RGB")
            square = ImageOps.fit(
                original, (512, 512), method=Image.Resampling.LANCZOS,
                centering=(0.5, 0.5),
            )
            square.save(destination, format="WEBP", quality=quality, method=6)
            square.close()
        print(f"OK {destination} (512x512)")
        converted += 1
    print(f"Converted {converted}/{len(ASSETS)} assets")
    return converted


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--input-dir", type=Path, required=True,
                        help="Folder of original JPG/PNG/WebP photos")
    parser.add_argument("--output-dir", type=Path, default=DEFAULT_DRAWABLE,
                        help="Android drawable output folder (default: app/src/main/res/drawable)")
    parser.add_argument("--quality", type=int, default=86,
                        help="WebP quality 1-100 (default 86)")
    args = parser.parse_args()
    if not 1 <= args.quality <= 100:
        parser.error("--quality must be 1-100")
    prepare(args.input_dir.expanduser(), args.output_dir.expanduser(), args.quality)


if __name__ == "__main__":
    main()
