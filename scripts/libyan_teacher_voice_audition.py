#!/usr/bin/env python3
"""Non-production pronunciation AND delivery auditions for Sawti.

An encouraging, approachable educational sound must be confirmed by a human;
increasing pitch does not turn a neutral neural voice into a teacher-style model.
No production assets, source catalog, or APK are edited.
"""
import asyncio
import csv
import json
import subprocess
import wave
from pathlib import Path

import edge_tts
import imageio_ffmpeg

CATALOG = Path("scripts/neural_v162_overrides.csv")
OUT = Path("build/libyan-teacher-voice-audition")
KEYS = ("action.sit", "action.sleep", "food.coffee", "animal.cow",
        "request.water", "request.toilet")
# The first three use Shakir to isolate delivery from speaker identity.
# Additional candidates vary the voice to avoid being anchored on Shakir.
PROFILES = (
    ("01_shakir_original", "ar-EG-ShakirNeural", "-18%", "+0Hz", "egypt_jeem"),
    ("02_shakir_warm", "ar-EG-ShakirNeural", "-12%", "+6Hz", "egypt_jeem"),
    ("03_shakir_bright", "ar-EG-ShakirNeural", "-8%", "+12Hz", "egypt_jeem"),
    ("04_omar_gentle", "ar-LY-OmarNeural", "-12%", "+8Hz", "qaf"),
    ("05_salma_bright", "ar-EG-SalmaNeural", "-10%", "+7Hz", "egypt_jeem"),
    ("06_hamed_clear", "ar-SA-HamedNeural", "-12%", "+7Hz", "qaf"),
)
# All clips must be vetted acoustically. The Egyptian jeem approximation
# is not guaranteed hard /ɡ/ for every Libyan phonetic context.
HARD_G = {"action.sit", "action.sleep", "food.coffee", "animal.cow"}


def load_rows():
    with CATALOG.open(encoding="utf-8-sig", newline="") as handle:
        rows = {r["semantic_key"]: r for r in csv.DictReader(handle)
                if r["dialect"] == "LIBYAN"}
    assert len(rows) == 271
    assert set(KEYS) <= set(rows)
    return rows


async def synthesize(text, voice, rate, pitch, destination, semaphore):
    async with semaphore:
        for attempt in range(5):
            tmp = destination.with_suffix(".partial")
            tmp.unlink(missing_ok=True)
            try:
                await edge_tts.Communicate(
                    text, voice=voice, rate=rate, pitch=pitch
                ).save(str(tmp))
                if tmp.stat().st_size <= 1024:
                    raise RuntimeError("Empty or invalid voice response")
                tmp.replace(destination)
                return
            except Exception:
                tmp.unlink(missing_ok=True)
                if attempt == 4:
                    raise
                await asyncio.sleep(min(20, 2 ** (attempt + 1)))


def make_montage(files, output):
    # Using concat demuxer without time scaling; add pauses between speech
    # to make an easy six-word teaching-audition reel.
    concat = output.with_suffix(".txt")
    lines = []
    for index, item in enumerate(files):
        lines.append("file '" + item.resolve().as_posix().replace("'", "'\\''") + "'")
        if index < len(files) - 1:
            lines.append("file '" + (OUT / "silence.mp3").resolve().as_posix() + "'")
    concat.write_text("\n".join(lines) + "\n", encoding="utf-8")
    subprocess.run(
        [imageio_ffmpeg.get_ffmpeg_exe(), "-y", "-v", "error", "-f", "concat", "-safe", "0",
         "-i", str(concat), "-c:a", "libmp3lame", "-q:a", "4", str(output)],
        check=True,
    )
    concat.unlink()


def make_silence():
    audio = OUT / "silence.wav"
    with wave.open(str(audio), "wb") as f:
        f.setnchannels(1)
        f.setsampwidth(2)
        f.setframerate(24000)
        f.writeframes(b"\x00\x00" * int(24000 * 0.65))
    subprocess.run(
        [imageio_ffmpeg.get_ffmpeg_exe(), "-y", "-v", "error", "-i", str(audio),
         "-codec:a", "libmp3lame", "-q:a", "5", str(OUT / "silence.mp3")],
        check=True,
    )
    audio.unlink()


async def main():
    OUT.mkdir(parents=True, exist_ok=True)
    rows = load_rows()
    index, jobs = [], []
    limiter = asyncio.Semaphore(2)
    files_by_profile = {}
    for label, voice, rate, pitch, scheme in PROFILES:
        folder = OUT / label
        folder.mkdir(parents=True, exist_ok=True)
        files_by_profile[label] = []
        for key in KEYS:
            row = rows[key]
            spoken = row["neural_speech_text"]
            if scheme == "egypt_jeem" and key in HARD_G:
                spoken = spoken.replace("ق", "ج")
            dest = folder / (key + ".mp3")
            files_by_profile[label].append(dest)
            jobs.append(synthesize(spoken, voice, rate, pitch, dest, limiter))
            index.append({
                "profile": label,
                "voice_id": voice,
                "rate": rate,
                "pitch": pitch,
                "key": key,
                "display": row["display"],
                "speech_text": spoken,
                "accuracy_verified_by_listener": "NO",
                "teacher_style_verified_by_listener": "NO",
                "file": str(dest.relative_to(OUT)),
            })
    for i in range(0, len(jobs), 12):
        await asyncio.gather(*jobs[i:i+12])
        print(f"Rendered {min(i+12, len(jobs))}/{len(jobs)}", flush=True)
    make_silence()
    for label, clips in files_by_profile.items():
        make_montage(clips, OUT / ("DEMO_" + label + ".mp3"))
    (OUT / "silence.mp3").unlink()
    with (OUT / "INDEX.csv").open("w", newline="", encoding="utf-8-sig") as f:
        w = csv.DictWriter(f, fieldnames=list(index[0]))
        w.writeheader()
        w.writerows(index)
    (OUT / "README.txt").write_text(
        "SAWTI — TEACHER VOICE LISTENING AUDITION (NOT PRODUCTION)\n"
        "6 preview recordings begin with DEMO_; each reads the same 6 words.\n"
        "These variations adjust pitch/rate only, NOT genuine emotional style.\n"
        "Compare warmth, encouragement, clarity, speed, pronunciation of G.\n"
        "For accessible patient speech avoid harsh loudness, rushed rhythm,\n"
        "highly exaggerated intonation, or clipped ending consonants.\n"
        "Voice ar-EG-ShakirNeural has no documented cheerful/teacher style preset.\n"
        "The Egyptian jeem spelling is ONLY an unverified G candidate.\n"
        "All samples must be listened to before publishing.\n",
        encoding="utf-8",
    )
    print(json.dumps({"profiles": len(PROFILES), "words": len(KEYS),
                      "word_clips": len(index), "demonstrations": len(PROFILES)}))


if __name__ == "__main__":
    asyncio.run(main())
