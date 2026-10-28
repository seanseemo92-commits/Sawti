from pathlib import Path
from PIL import Image, ImageFilter
import numpy as np

ROOT = Path(".")
RES = ROOT / "app/src/main/res"
NODPI = RES / "drawable-nodpi"
NIGHT = RES / "drawable-night-nodpi"
NODPI.mkdir(parents=True, exist_ok=True)
NIGHT.mkdir(parents=True, exist_ok=True)


def clean_wordmark(source, output, scale=4):
    # Keep the supplied letterforms and colors. This is a higher-density raster
    # with gentler aliasing, NOT a replacement font or a fabricated vector logo.
    img = Image.open(source).convert("RGBA")
    assert img.size == (410, 430), (source, img.size)
    smoothed = img.filter(ImageFilter.GaussianBlur(radius=0.26))
    high = smoothed.resize((img.width * scale, img.height * scale), Image.Resampling.LANCZOS)
    high.putalpha(high.getchannel("A").filter(
        ImageFilter.UnsharpMask(radius=1.0, percent=110, threshold=2)
    ))
    assert high.getchannel("A").getbbox() is not None
    high.save(output, optimize=True)
    print("Generated", output, high.size)


for mode in ("light", "dark"):
    clean_wordmark(
        NODPI / f"sawti_wordmark_centered_{mode}.png",
        NODPI / f"sawti_wordmark_hd_{mode}.png",
    )

# Dark-mode foreground: only the original gold emblem, with no opaque rounded
# icon inside Android's own adaptive icon mask. Light assets are unchanged.
source = Image.open(RES / "drawable/ic_splash_logo_dark.png").convert("RGBA")
bounds = source.getchannel("A").point(lambda value: 255 if value > 8 else 0).getbbox()
assert bounds is not None, "Dark splash emblem missing"
mark = source.crop(bounds)
width = 332
height = round(mark.height * width / mark.width)
if height > 250:
    height = 250
    width = round(mark.width * height / mark.height)
mark = mark.resize((width, height), Image.Resampling.LANCZOS)
foreground = Image.new("RGBA", (512, 512), (0, 0, 0, 0))
foreground.alpha_composite(mark, ((512-width)//2, (512-height)//2))
foreground.save(NIGHT / "sawti_adaptive_brand.png", optimize=True)

# Update night-only legacy launcher icons for API 24-25. Keep all day resources
# completely untouched, including their existing icon artwork.
row = np.linspace(0, 1, 512, dtype=np.float32)
xx, yy = np.meshgrid(row, row)
blend = np.clip(0.65 * xx + 0.35 * yy, 0, 1)
background = np.empty((512, 512, 3), dtype=np.uint8)
for index, (a, b) in enumerate(zip((7, 22, 41), (15, 35, 58))):
    background[..., index] = (a * (1-blend) + b * blend).astype(np.uint8)
full = Image.fromarray(background, "RGB").convert("RGBA")
full.alpha_composite(foreground)
for pixels, dpi in [(48,"mdpi"), (72,"hdpi"), (96,"xhdpi"), (144,"xxhdpi"), (192,"xxxhdpi")]:
    target = RES / f"mipmap-night-{dpi}"
    target.mkdir(parents=True, exist_ok=True)
    icon = full.resize((pixels, pixels), Image.Resampling.LANCZOS)
    for name in ("ic_launcher.png", "ic_launcher_round.png"):
        icon.save(target / name, optimize=True)
        print("Generated", target/name, icon.size)
