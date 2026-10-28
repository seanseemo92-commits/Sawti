"""
Reconstruct clean, resolution-independent Sawti Splash vector assets.

Input: the original centered 410x430 alpha artwork (not the formerly upscaled
1640x1720 PNG). Output: Android VectorDrawable XML with explicit Arabic/Latin
glyph contours and two emblem inks. Morphological closing fills isolated
compression/alpha-key holes while retaining meaningful letter counters.

For reproducibility, no new font, app data, or launcher artwork is touched.
"""
from pathlib import Path
import cv2
import numpy as np
from PIL import Image
from xml.sax.saxutils import escape

RES = Path("app/src/main/res")
SRC = RES / "drawable-nodpi"
DST = RES / "drawable"
DST.mkdir(parents=True, exist_ok=True)
HEIGHT, WIDTH = 430, 410
SCALE = 4

COLORS = {
    "light": {
        "pearl": "#F7F6FD",
        "gold": "#E9C894",
        "title": "#1B2352",
        "latin": "#1B2352",
    },
    "dark": {
        "pearl": "#F6F0E4",
        "gold": "#E6BD7B",
        "title": "#EFE6D6",
        "latin": "#D5B78A",
    },
}


def close_small_holes(mask, max_hole_area=27):
    binary = np.uint8(mask > 0) * 255
    binary = cv2.morphologyEx(
        binary,
        cv2.MORPH_CLOSE,
        cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (3, 3)),
    )
    num, labels, stats, _ = cv2.connectedComponentsWithStats(255 - binary)
    for i in range(1, num):
        x, y, w, h, area = stats[i]
        if (
            x > 0 and y > 0 and
            x + w < WIDTH and y + h < HEIGHT and
            area < max_hole_area
        ):
            binary[labels == i] = 255
    return binary


def path_data(mask):
    # Subpixel contour reconstruction and slight Gaussian smoothing:
    # unlike Lanczos-upscaled PNG, these are true scalable vector paths.
    large = cv2.resize(mask, (WIDTH * SCALE, HEIGHT * SCALE),
                       interpolation=cv2.INTER_CUBIC)
    large = cv2.GaussianBlur(large, (0, 0), sigmaX=1.9)
    _, large = cv2.threshold(large, 127, 255, cv2.THRESH_BINARY)
    contours, _ = cv2.findContours(large, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
    paths = []
    for contour in contours:
        if cv2.contourArea(contour) < 9.0:
            continue
        vertices = cv2.approxPolyDP(contour, epsilon=1.75, closed=True)
        vertices = vertices.reshape(-1, 2)
        if len(vertices) < 3:
            continue
        commands = []
        for j, (x, y) in enumerate(vertices):
            cmd = "M" if j == 0 else "L"
            commands.append(f"{cmd}{x / SCALE:.2f},{y / SCALE:.2f}")
        paths.append("".join(commands) + "Z")
    return " ".join(paths)


def build(mode):
    path = SRC / f"sawti_wordmark_centered_{mode}.png"
    image = np.asarray(Image.open(path).convert("RGBA"), dtype=np.uint8)
    assert image.shape == (HEIGHT, WIDTH, 4), (path, image.shape)

    alpha = image[:, :, 3]
    rgb = image[:, :, :3].astype(np.int16)
    foreground = close_small_holes(alpha > 55)
    # There must be a transparent gap between the emblem and the typography.
    foreground[235:275] = 0

    emblem = foreground.copy()
    emblem[240:] = 0
    if mode == "dark":
        # In the original dark mark, the upper five audio strokes are pearl,
        # while the large ص and surrounding waveform are metallic gold.
        pearl = np.zeros_like(emblem)
        pearl[:81, 197:335] = emblem[:81, 197:335]
        gold = cv2.subtract(emblem, pearl)
    else:
        yy = np.indices((HEIGHT, WIDTH))[0]
        is_gold = (
            (rgb[:, :, 0] - rgb[:, :, 2] > 20) &
            (rgb[:, :, 1] - rgb[:, :, 2] > 1) &
            (yy < 240)
        )
        gold = cv2.medianBlur(np.uint8(is_gold) * 255, 3)
        gold = cv2.dilate(
            gold, cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (3, 3)),
            iterations=1,
        )
        gold = cv2.bitwise_and(gold, emblem)
        pearl = cv2.subtract(emblem, gold)
        count, labels, stats, _ = cv2.connectedComponentsWithStats(pearl)
        for i in range(1, count):
            if stats[i, cv2.CC_STAT_AREA] < 30:
                gold[labels == i] = 255
                pearl[labels == i] = 0

    title = foreground.copy()
    title[:270] = 0
    title[376:] = 0
    latin = foreground.copy()
    latin[:376] = 0

    masks = {"pearl": pearl, "gold": gold, "title": title, "latin": latin}
    rows = [
        '<?xml version="1.0" encoding="utf-8"?>',
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
        '    android:width="410dp" android:height="430dp"',
        '    android:viewportWidth="410" android:viewportHeight="430">',
    ]
    for name, mask in masks.items():
        data = path_data(mask)
        if not data:
            raise ValueError(f"No visible {name} for {mode}")
        rows.extend([
            f'    <path android:fillColor="{COLORS[mode][name]}"',
            '        android:fillType="evenOdd"',
            f'        android:pathData="{escape(data)}" />',
        ])
    rows.append("</vector>")
    output = DST / f"sawti_wordmark_vector_{mode}.xml"
    output.write_text("\n".join(rows) + "\n", encoding="utf-8")
    print(f"Generated {output} ({output.stat().st_size} bytes)")


if __name__ == "__main__":
    for mode in ("light", "dark"):
        build(mode)
