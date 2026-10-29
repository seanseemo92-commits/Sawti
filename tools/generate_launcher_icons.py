#!/usr/bin/env python3
"""Regenerate every legacy Sawti launcher size from its native conversation vectors.

The Android 26+ adaptive icon directly uses the same theme-specific VectorDrawable
and the gradient in ic_launcher_background.xml. These are fallback PNGs for older
Android launchers and OEM icon caches, not a second source of brand artwork.

Usage: pip install pillow numpy cairosvg && python tools/generate_launcher_icons.py
"""
from io import BytesIO
from pathlib import Path
import xml.etree.ElementTree as ET

import cairosvg
import numpy as np
from PIL import Image, ImageDraw, ImageFilter

RES = Path("app/src/main/res")
NS = {"a": "http://schemas.android.com/apk/res/android"}
ANDROID = "{http://schemas.android.com/apk/res/android}"
DENSITIES = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
BACKGROUND = {
    "light": [(189, 233, 255), (115, 120, 243), (163, 98, 235)],
    "dark": [(7, 26, 56), (23, 39, 87), (53, 34, 106)],
}


def vector_svg(mode):
    file = RES / ("drawable-night" if mode == "dark" else "drawable") / "sawti_adaptive_brand.xml"
    root = ET.parse(file).getroot()
    rows = ['<svg xmlns="http://www.w3.org/2000/svg" width="108" height="108" viewBox="0 0 108 108">']
    for node in root.findall("path"):
        attrs = node.attrib
        d = attrs[ANDROID + "pathData"]
        fill = attrs.get(ANDROID + "fillColor", "none")
        if fill == "@android:color/transparent":
            fill = "none"
        opacity = attrs.get(ANDROID + "fillAlpha", "1")
        stroke = attrs.get(ANDROID + "strokeColor")
        stroke_width = attrs.get(ANDROID + "strokeWidth", "1")
        cap = attrs.get(ANDROID + "strokeLineCap", "butt")
        join = attrs.get(ANDROID + "strokeLineJoin", "miter")
        # All path data and colors belong to our checked-in XML vectors.
        row = f'<path d="{d}" fill="{fill}" fill-opacity="{opacity}"'
        if stroke:
            row += f' stroke="{stroke}" stroke-width="{stroke_width}" stroke-linecap="{cap}" stroke-linejoin="{join}"'
        row += '/>'
        rows.append(row)
    rows.append("</svg>")
    return "\n".join(rows).encode("utf-8")


def foreground(mode, pixels=512):
    png = cairosvg.svg2png(bytestring=vector_svg(mode), output_width=pixels, output_height=pixels)
    img = Image.open(BytesIO(png)).convert("RGBA")
    if not img.getchannel("A").getbbox():
        raise RuntimeError("Vector foreground is empty")
    return img


def background(mode, pixels=512):
    # Diagonal multi-stop gradient (matching the full-bleed Android vector icon).
    y, x = np.mgrid[0:pixels, 0:pixels].astype(np.float32)
    t = (0.55 * x + 0.45 * y) / max(1, pixels - 1)
    colors = BACKGROUND[mode]
    split = np.clip(t * 2, 0, 2)
    left = np.minimum(split, 1)[..., None]
    right = np.maximum(split - 1, 0)[..., None]
    c0, c1, c2 = [np.asarray(c, dtype=np.float32) for c in colors]
    rgb = np.where(
        (split < 1)[..., None],
        c0 * (1 - left) + c1 * left,
        c1 * (1 - right) + c2 * right,
    )
    return Image.fromarray(rgb.clip(0, 255).astype(np.uint8), "RGB").convert("RGBA")


def compose_icon(mode, pixels, rounded):
    # Render at higher than target resolution for anti-aliased edges.
    oversample = 4
    n = pixels * oversample
    img = background(mode, n)
    img.alpha_composite(foreground(mode, n))
    if rounded:
        mask = Image.new("L", (n, n), 0)
        draw = ImageDraw.Draw(mask)
        draw.ellipse((0, 0, n - 1, n - 1), fill=255)
        img.putalpha(mask)
    else:
        # Standard old Android launcher image, without an extra bezel/inset.
        mask = Image.new("L", (n, n), 0)
        ImageDraw.Draw(mask).rounded_rectangle(
            (0, 0, n - 1, n - 1), radius=int(n * .19), fill=255
        )
        img.putalpha(mask)
    return img.resize((pixels, pixels), Image.Resampling.LANCZOS)


def save_icon(path, mode, size, rounded):
    path.parent.mkdir(parents=True, exist_ok=True)
    img = compose_icon(mode, size, rounded)
    img.save(path, optimize=True)
    assert path.is_file() and path.stat().st_size > 500
    print("OK", path, img.size)


def main():
    # Native Android resource icon matches device theme on Android 26+.
    # Explicit night PNGs also cover Android 24/25 + vendor launchers.
    for dpi, px in DENSITIES.items():
        normal = RES / f"mipmap-{dpi}"
        night = RES / f"mipmap-night-{dpi}"
        for rounded, filename in [(False, "ic_launcher.png"), (True, "ic_launcher_round.png")]:
            save_icon(normal / filename, "light", px, rounded)
            save_icon(night / filename, "dark", px, rounded)
            save_icon(normal / filename.replace(".png", "_night.png"), "dark", px, rounded)

    # Human-readable comparison file (not a runtime Android resource).
    left = compose_icon("light", 512, False)
    right = compose_icon("dark", 512, False)
    sheet = Image.new("RGBA", (1024, 512), "#f0f3ff")
    sheet.alpha_composite(left, (0, 0))
    sheet.alpha_composite(right, (512, 0))
    sheet.convert("RGB").save("sawti_icon_comparison.png", optimize=True)
    print("Regenerated all 30 launcher raster files from the original vector sources.")


if __name__ == "__main__":
    main()
