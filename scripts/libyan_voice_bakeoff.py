#!/usr/bin/env python3
"""Non-production neural voice bake-off for Libyan words; no APK or asset mutations.

All samples derive from the authoritative v1.6.2 CSV. 19 candidate /g/ words
plus 9 control terms are rendered using ten Arabic neural voices. Acoustic
correctness cannot be inferred from file integrity alone.
"""
import asyncio
import csv
import json
import re
from pathlib import Path

import edge_tts

CSV = Path("scripts/neural_v162_overrides.csv")
OUT = Path("build/libyan-voice-bakeoff")
RATE = "-18%"
HARD_G_KEYS = (
    "family.male_relative", "dish.pasta_masqiya", "dish.maqroudh",
    "dish.arabic_coffee", "routine.evening_coffee", "vegetable.coriander",
    "vegetable.cauliflower", "vegetable.pumpkin", "fruit.melon",
    "food.coffee", "clothing.cap", "body.neck", "body.heart",
    "action.sleep", "action.sit", "action.stand", "action.read",
    "animal.cat", "animal.cow",
)
CONTROL_KEYS = (
    "family.father", "request.water", "request.toilet", "dish.bazin",
    "dish.mbakbaka", "dish.osban", "food.water", "animal.rooster",
    "greeting.how_are_you",
)
# Egyptian ج is a *candidate* for /g/, not a guarantee.
# Preserve the regular Arabic dialect text for other voices to test how
# different regional phonologies treat written ق.
VOICES = (
    ("libya_iman_qaf", "ar-LY-ImanNeural", "qaf"),
    ("libya_omar_qaf", "ar-LY-OmarNeural", "qaf"),
    ("egypt_salma_jeem", "ar-EG-SalmaNeural", "egypt_jeem"),
    ("egypt_shakir_jeem", "ar-EG-ShakirNeural", "egypt_jeem"),
    ("iraq_rana_qaf", "ar-IQ-RanaNeural", "qaf"),
    ("iraq_bassel_qaf", "ar-IQ-BasselNeural", "qaf"),
    ("tunisia_reem_qaf", "ar-TN-ReemNeural", "qaf"),
    ("tunisia_hedi_qaf", "ar-TN-HediNeural", "qaf"),
    ("saudi_zariyah_qaf", "ar-SA-ZariyahNeural", "qaf"),
    ("saudi_hamed_qaf", "ar-SA-HamedNeural", "qaf"),
)


def catalog():
    with CSV.open(encoding="utf-8-sig", newline="") as handle:
        records = {
            row["semantic_key"]: row
            for row in csv.DictReader(handle) if row["dialect"] == "LIBYAN"
        }
    assert len(records) == 271, f"Expected 271 Libyan entries, got {len(records)}"
    requested = set(HARD_G_KEYS) | set(CONTROL_KEYS)
    assert len(requested) == 28 and requested <= records.keys()
    return records


async def save_audio(text, voice, target, limiter):
    async with limiter:
        for trial in range(4):
            partial = target.with_suffix(".mp3.partial")
            partial.unlink(missing_ok=True)
            try:
                await edge_tts.Communicate(text, voice, rate=RATE).save(str(partial))
                if partial.stat().st_size <= 1024:
                    raise IOError("Invalid small generated MP3")
                partial.replace(target)
                return
            except Exception:
                partial.unlink(missing_ok=True)
                if trial == 3:
                    raise
                await asyncio.sleep(min(12, 2 ** (trial + 1)))


async def main():
    records = catalog()
    limiter = asyncio.Semaphore(5)
    OUT.mkdir(parents=True, exist_ok=True)
    jobs = []
    index = []
    for candidate, voice, spelling in VOICES:
        folder = OUT / candidate
        folder.mkdir(parents=True, exist_ok=True)
        for key in HARD_G_KEYS + CONTROL_KEYS:
            row = records[key]
            target = row["neural_speech_text"]
            if key in HARD_G_KEYS and spelling == "egypt_jeem":
                # For these 19 specific records, the ق originally stood for
                # the intended g; this is not a universal Libyan qaf rule.
                target = target.replace("ق", "ج")
            file = f"{key}.mp3"
            path = folder / file
            jobs.append(save_audio(target, voice, path, limiter))
            index.append({
                "candidate": candidate,
                "voice_id": voice,
                "key": key,
                "display": row["display"],
                "text_sent_to_tts": target,
                "needs_hard_g": "YES" if key in HARD_G_KEYS else "NO",
                "speaker_confirmed_g": "NO",
                "audio_path": f"{candidate}/{file}",
            })
    # Small batches, to keep TTS concurrency bounded and detect failures early.
    for offset in range(0, len(jobs), 35):
        await asyncio.gather(*jobs[offset:offset + 35])
        print(f"Completed {min(offset + 35, len(jobs))}/{len(jobs)}", flush=True)
    assert len(index) == 280
    with (OUT / "INDEX.csv").open("w", encoding="utf-8-sig", newline="") as f:
        w = csv.DictWriter(f, fieldnames=list(index[0]))
        w.writeheader()
        w.writerows(index)
    (OUT / "README.txt").write_text(
        "SAWTI — Libyan pronunciation bake-off; 280 AUDIO TRIALS\n"
        "All samples are listening candidates, NOT verified native Libyan speech.\n"
        "19 words test hard /g/ and 9 additional words test ordinary Libyan diction.\n"
        "Compare all ten folders across both sets, not /g/ alone.\n"
        "Egyptian samples use ج in hard-g words; other voices use ق.\n"
        "Correct /g/ cannot be proven from MP3 size or using a particular voice.\n"
        "Mark each word hard-g correctness, other consonants, vowels and naturalness.\n"
        "Do not replace Android assets with these samples without listening QA.\n",
        encoding="utf-8",
    )
    print(json.dumps({"voices": len(VOICES), "hard_g": len(HARD_G_KEYS),
                      "controls": len(CONTROL_KEYS), "clips": len(index)}))


if __name__ == "__main__":
    asyncio.run(main())
