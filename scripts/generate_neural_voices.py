#!/usr/bin/env python3
"""Generate deterministic, manifest-validated regional neural voice packs.

Run the ExportSpeechCatalogTest first, so this script never guesses Kotlin's
applied dialect revisions, diacritics, or localized dish strings.
"""
import argparse
import asyncio
import json
import re
import sys
from pathlib import Path

VOICES = {
    "LIBYAN": ("ar-LY-ImanNeural", "ar-LY-OmarNeural"),
    "TUNISIAN": ("ar-TN-ReemNeural", "ar-TN-HediNeural"),
    "MOROCCAN": ("ar-MA-MounaNeural", "ar-MA-JamalNeural"),
    "ALGERIAN": ("ar-DZ-AminaNeural", "ar-DZ-IsmaelNeural"),
    "EGYPTIAN": ("ar-EG-SalmaNeural", "ar-EG-ShakirNeural"),
    "SYRIAN": ("ar-SY-AmanyNeural", "ar-SY-LaithNeural"),
    "IRAQI": ("ar-IQ-RanaNeural", "ar-IQ-BasselNeural"),
    "SAUDI": ("ar-SA-ZariyahNeural", "ar-SA-HamedNeural"),
    "STANDARD": ("ar-AE-FatimaNeural", "ar-AE-HamdanNeural"),
}
SAFE_KEY = re.compile(r"^[a-z0-9][a-z0-9._-]*$")


def dialects_arg(value):
    if value.strip().upper() == "ALL":
        return list(VOICES)
    values = [part.strip().upper() for part in value.split(",")]
    if not values or any(v not in VOICES for v in values) or len(set(values)) != len(values):
        raise argparse.ArgumentTypeError("Use ALL or distinct dialects: " + ", ".join(VOICES))
    return values


def load_catalog(path):
    data = json.loads(path.read_text(encoding="utf-8"))
    if data.get("version") != 1 or not isinstance(data.get("entries"), list):
        raise ValueError("Unsupported/invalid exported speech catalog")
    grouped = {name: {} for name in VOICES}
    for row in data["entries"]:
        name, key, spoken = (row.get(field) for field in ("dialect", "semanticKey", "speechText"))
        if name not in VOICES or not isinstance(key, str) or not SAFE_KEY.fullmatch(key) or ".." in key:
            raise ValueError(f"Invalid dialect/key: {name}/{key}")
        if not isinstance(spoken, str) or not spoken.strip():
            raise ValueError(f"Empty speech: {name}/{key}")
        if key in grouped[name]:
            raise ValueError(f"Duplicate semantic key: {name}/{key}")
        grouped[name][key] = spoken
    if any(not items for items in grouped.values()):
        raise ValueError("Speech catalog must have nonempty items for every dialect")
    return grouped


async def synthesize(voice, text, target, semaphore, attempts):
    import edge_tts
    async with semaphore:
        for attempt in range(attempts):
            try:
                partial = target.with_suffix(".mp3.partial")
                partial.unlink(missing_ok=True)
                await edge_tts.Communicate(text, voice).save(str(partial))
                if partial.stat().st_size < 64:
                    raise IOError(f"Empty/short TTS response: {target}")
                partial.replace(target)
                return
            except Exception:
                partial.unlink(missing_ok=True)
                if attempt == attempts - 1:
                    raise
                await asyncio.sleep(min(20, 2 ** (attempt + 1)))


async def generate(args):
    catalog = load_catalog(args.catalog)
    semaphore = asyncio.Semaphore(args.concurrency)
    for dialect in args.dialects:
        voice = VOICES[dialect][0 if args.gender == "female" else 1]
        folder = args.output / dialect.lower()
        folder.mkdir(parents=True, exist_ok=True)
        manifest_path = folder / "manifest.json"
        prior = {}
        if manifest_path.is_file():
            try:
                prior = json.loads(manifest_path.read_text(encoding="utf-8"))
            except (ValueError, OSError):
                pass
        tasks = []
        manifest_entries = {}
        for key, text in sorted(catalog[dialect].items()):
            filename = f"{key}.mp3"
            target = folder / filename
            # A changed text or gender invalidates the previous audio, even if it exists.
            previous = prior.get("entries", {}).get(key, {})
            reusable = (not args.force and target.is_file() and target.stat().st_size > 64
                        and prior.get("voice") == voice and previous.get("speechText") == text)
            if not reusable:
                tasks.append(synthesize(voice, text, target, semaphore, args.attempts))
            manifest_entries[key] = {"file": filename, "speechText": text}
        print(f"{dialect}: voice={voice}, clips={len(manifest_entries)}, generating={len(tasks)}", flush=True)
        # Fail before replacing the authoritative manifest if even one MP3 fails.
        await asyncio.gather(*tasks)
        new_manifest = {"version": 1, "voice": voice, "entries": manifest_entries}
        manifest_path.write_text(json.dumps(new_manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print("All requested regional neural packs completed.", flush=True)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--catalog", type=Path, default=Path("app/build/speech_catalog.json"))
    parser.add_argument("--output", type=Path, default=Path("app/src/main/assets/neural_tts"))
    parser.add_argument("--dialects", type=dialects_arg, default=["LIBYAN"])
    parser.add_argument("--gender", choices=["male", "female"], default="female")
    parser.add_argument("--concurrency", type=int, default=4)
    parser.add_argument("--attempts", type=int, default=4)
    parser.add_argument("--force", action="store_true")
    args = parser.parse_args()
    if not (1 <= args.concurrency <= 8 and 1 <= args.attempts <= 10):
        parser.error("concurrency must be 1..8 and attempts 1..10")
    asyncio.run(generate(args))


if __name__ == "__main__":
    main()
