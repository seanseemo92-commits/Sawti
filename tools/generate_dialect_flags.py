"""Render bundled MIT flag-icons SVG sources; no network requests during generation."""
from pathlib import Path
import cairosvg

root = Path(__file__).resolve().parents[1]
target = root / 'app/src/main/res/drawable-nodpi'
target.mkdir(parents=True, exist_ok=True)
for code in ('ly', 'sa', 'eg', 'sy', 'iq', 'tn', 'ma', 'dz'):
    cairosvg.svg2png(url=str(root / 'artwork/dialect-flags' / f'{code}.svg'),
                    write_to=str(target / f'dialect_flag_{code}.png'),
                    output_width=144, output_height=108)
