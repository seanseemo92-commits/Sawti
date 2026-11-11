#!/usr/bin/env python3
"""Stage all existing short-surah text/recitations in APK assets for offline testing.

All 201 MP3s are copied byte-for-byte from the verse-by-verse EveryAyah sets
used by the Quran 1.4 passage test; none is cut, concatenated or normalized.
This script deliberately fails closed if any verse/recording is missing.
Human recitation and redistribution rights review remains required.
"""
from __future__ import annotations
import concurrent.futures
import hashlib
import json
import subprocess
import time
import urllib.error
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "app/src/main/assets/quran_surahs"
CHAPTERS = {1: 7, 112: 4, 113: 5, 114: 6, 108: 3, 103: 3, 109: 6,
            110: 3, 111: 5, 105: 5, 106: 4, 107: 7, 104: 9}
FIRST = {1: 1, 103: 6177, 104: 6180, 105: 6189, 106: 6194, 107: 6198,
         108: 6205, 109: 6208, 110: 6214, 111: 6217, 112: 6222,
         113: 6226, 114: 6231}
READERS = {
    "ar.alafasy": ("Alafasy_128kbps", "mishari-alafasy-128kbps"),
    "salah.bukhatir": ("Salaah_AbdulRahman_Bukhatir_128kbps", "salah-bukhatir-128kbps"),
    "husary.muallim": ("Husary_Muallim_128kbps", "mahmoud-al-husary-muallim-128kbps"),
}
USER_AGENT = "Sawti-Quran-Offline-Test/1.4 (source verification)"
MAX_AUDIO_BYTES = 12 * 1024 * 1024


def sha256(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def download(url: str, limit: int) -> bytes:
    request = urllib.request.Request(url, headers={
        "User-Agent": USER_AGENT,
        "Accept": "audio/mpeg,application/json,*/*",
    })
    with urllib.request.urlopen(request, timeout=40) as response:
        if int(response.headers.get("Content-Length", "0")) > limit:
            raise ValueError(f"Too large: {url}")
        data = response.read(limit + 1)
    if not 100 < len(data) <= limit:
        raise ValueError(f"Empty or oversized source: {url}")
    return data


def validated_mp3(path: Path) -> bool:
    if not path.is_file() or not 1024 < path.stat().st_size <= MAX_AUDIO_BYTES:
        return False
    try:
        probe = subprocess.check_output([
            "ffprobe", "-v", "error", "-select_streams", "a:0",
            "-show_entries", "stream=codec_name", "-of", "default=noprint_wrappers=1:nokey=1",
            str(path),
        ], text=True, timeout=30).strip()
        if probe != "mp3":
            return False
        decoded = subprocess.run([
            "ffmpeg", "-nostdin", "-v", "error", "-xerror", "-i", str(path),
            "-f", "null", "-",
        ], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, timeout=40)
        return decoded.returncode == 0
    except (subprocess.CalledProcessError, subprocess.TimeoutExpired):
        return False


def lossless_remux(source: Path) -> bool:
    """Try discarding malformed framing without decoding/re-encoding good audio."""
    repaired = source.with_suffix(".remux.mp3")
    try:
        subprocess.run([
            "ffmpeg", "-nostdin", "-y", "-v", "error", "-err_detect", "ignore_err",
            "-i", str(source), "-map", "0:a:0", "-c:a", "copy",
            "-write_xing", "0", str(repaired),
        ], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, timeout=40)
        if validated_mp3(repaired) and repaired.stat().st_size >= source.stat().st_size * 0.90:
            repaired.replace(source)
            return True
        return False
    except (subprocess.CalledProcessError, subprocess.TimeoutExpired, OSError):
        return False
    finally:
        repaired.unlink(missing_ok=True)


def recover_single_known_corrupt_frame(source: Path) -> bool:
    """One documented exception: Husary Muallim 113:5 source has a bad MP3 frame.

    Preserve reciter and audible sequence, decode with one error tolerated, and
    encode a fresh 128kbps mono MP3. This is NOT a lossless operation and needs
    human listening before public distribution.
    """
    output = source.with_suffix(".repaired.mp3")
    try:
        before = float(subprocess.check_output([
            "ffprobe", "-v", "error", "-show_entries", "format=duration",
            "-of", "csv=p=0", str(source)
        ], text=True, timeout=20))
        subprocess.run([
            "ffmpeg", "-nostdin", "-y", "-v", "error",
            "-err_detect", "ignore_err", "-i", str(source),
            "-map", "0:a:0", "-ac", "1", "-ar", "44100",
            "-c:a", "libmp3lame", "-b:a", "128k", str(output),
        ], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, timeout=40)
        after = float(subprocess.check_output([
            "ffprobe", "-v", "error", "-show_entries", "format=duration",
            "-of", "csv=p=0", str(output)
        ], text=True, timeout=20))
        if before < 2 or not (0.98 * before <= after <= 1.02 * before):
            return False
        if not validated_mp3(output):
            return False
        output.replace(source)
        return True
    except (subprocess.CalledProcessError, subprocess.TimeoutExpired, ValueError, OSError):
        return False
    finally:
        output.unlink(missing_ok=True)


def store_mp3(chapter: int, ayah: int, reader: str):
    folder, mirror = READERS[reader]
    filename = f"{chapter:03d}{ayah:03d}.mp3"
    target = OUT / reader / filename
    target.parent.mkdir(parents=True, exist_ok=True)
    urls = [
        f"https://everyayah.com/data/{folder}/{filename}",
        f"https://huggingface.co/datasets/maqra-project/{mirror}/resolve/main/{filename}",
        f"https://verse.mp3quran.net/data/{folder}/{filename}",
    ]
    processing = "unaltered"
    if not validated_mp3(target):
        failures = []
        for url in urls:
            for trial in range(3):
                try:
                    data = download(url, MAX_AUDIO_BYTES)
                    temporary = target.with_suffix(".download")
                    temporary.write_bytes(data)
                    if not validated_mp3(temporary):
                        if lossless_remux(temporary):
                            processing = "lossless_remux"
                        elif (reader, chapter, ayah) == ("husary.muallim", 113, 5) and recover_single_known_corrupt_frame(temporary):
                            processing = "reencoded_known_corrupt_frame"
                        else:
                            raise ValueError(f"Unrepairable MP3 decode error: {url}")
                    temporary.replace(target)
                    break
                except Exception as exc:
                    failures.append(f"{url} attempt {trial+1}: {exc}")
                    target.with_suffix(".download").unlink(missing_ok=True)
                    time.sleep(.8 * (trial + 1))
            if validated_mp3(target):
                break
        if not validated_mp3(target):
            raise RuntimeError(f"Could not bundle {reader} {filename}: " + "; ".join(failures))
    return reader, chapter, ayah, {
        "asset": f"quran_surahs/{reader}/{filename}",
        "sha256": sha256(target.read_bytes()),
        "source": f"https://everyayah.com/data/{folder}/{filename}",
        "processing": processing,
    }


def fetch_text(chapter: int, expected: int):
    source = f"https://api.alquran.cloud/v1/surah/{chapter}/quran-uthmani"
    last_error = None
    for attempt in range(4):
        try:
            raw = download(source, 256 * 1024)
            parsed = json.loads(raw)
            record = parsed["data"]
            assert parsed["status"].upper() == "OK"
            assert record["number"] == chapter and record["numberOfAyahs"] == expected
            verses = record["ayahs"]
            assert len(verses) == expected
            for index, verse in enumerate(verses, 1):
                assert verse["number"] == FIRST[chapter] + index - 1
                assert verse["numberInSurah"] == index and verse["text"].strip()
            target = OUT / "text" / f"surah_{chapter}.json"
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(raw)
            return sha256(raw)
        except Exception as exc:
            last_error = exc
            time.sleep(.8 * (attempt + 1))
    raise RuntimeError(f"Could not validate Uthmani text for {chapter}") from last_error


def main():
    assert sum(CHAPTERS.values()) == 67
    assert set(CHAPTERS) == set(FIRST)
    texts = {str(chapter): fetch_text(chapter, count) for chapter, count in CHAPTERS.items()}
    tasks = [(chapter, verse, reader)
             for chapter, count in CHAPTERS.items()
             for verse in range(1, count + 1)
             for reader in READERS]
    clips = {}
    # Low concurrency avoids overloading the volunteer Quran audio mirrors.
    with concurrent.futures.ThreadPoolExecutor(max_workers=4) as executor:
        futures = [executor.submit(store_mp3, *task) for task in tasks]
        for i, job in enumerate(concurrent.futures.as_completed(futures), 1):
            reader, chapter, verse, metadata = job.result()
            clips[(chapter, verse, reader)] = metadata
            if i % 20 == 0 or i == len(tasks):
                print(f"Downloaded and checked {i}/{len(tasks)} Quran recordings", flush=True)
    assert len(clips) == 201
    verses = []
    for chapter, count in CHAPTERS.items():
        for verse in range(1, count + 1):
            verses.append({
                "surah": chapter, "ayah": verse, "global": FIRST[chapter] + verse - 1,
                "audio": {reader: clips[(chapter, verse, reader)] for reader in READERS},
            })
    manifest = {"version": 1, "textSha256": texts, "verses": verses}
    (OUT / "catalog.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    repairs = [(x["surah"], x["ayah"], name, audio["processing"])
               for x in verses for name, audio in x["audio"].items()
               if audio["processing"] != "unaltered"]
    print("Recorded deviations from original bitstreams:", repairs, flush=True)
    print("Packaged 13 Uthmani texts and 201 128kbps ayah clips; one source needed a documented repair; human review pending.")


if __name__ == "__main__":
    main()
