#!/usr/bin/env python3
"""Produce isolated listening trials for Libyan /ɡ/ vs qaf.

NOT a production audio generator: this deliberately never overwrites
app/src/main/assets/neural_tts or authoritative v1.6.2 table rows.
Text transformations do not guarantee an acoustic /ɡ/; user listening is needed.
"""
import asyncio
import csv
import json
from pathlib import Path

import edge_tts

WORDS = [
    ("action.sit", "نقعمز", "نْقَعْمِزْ", "نْجَعْمِزْ", "نْكَعْمِزْ"),
    ("action.sleep", "نرقد", "نُرْقُدْ", "نُرْجُدْ", "نُرْكُدْ"),
    ("action.stand", "نوقف", "نُوقَفْ", "نُوجَفْ", "نُوكَفْ"),
    ("food.coffee", "قهوة", "قَهْوَة", "جَهْوَة", "كَهْوَة"),
    ("body.heart", "قلب", "قَلْبْ", "جَلْبْ", "كَلْبْ"),
    ("animal.cow", "بقرة", "بَقْرَة", "بَجْرَة", "بَكْرَة"),
    ("animal.cat", "قطة", "قَطُّوسَة", "جَطُّوسَة", "كَطُّوسَة"),
]
TRIALS = [
    ("iman_qaf_current", "ar-LY-ImanNeural", 2),
    ("omar_qaf", "ar-LY-OmarNeural", 2),
    ("iman_kaf_control", "ar-LY-ImanNeural", 4),
    ("egyptian_jeem_g_candidate", "ar-EG-SalmaNeural", 3),
]
RATE = "-18%"
OUT = Path("build/libyan-g-listening-trials")


async def generate():
    OUT.mkdir(parents=True, exist_ok=True)
    sem = asyncio.Semaphore(5)
    metadata = []
    async def render(index, record, trial):
        key, display = record[0], record[1]
        label, voice, field = trial
        speech = record[field]
        # Filenames explicitly distinguish dialect and spelling.
        filename = f"{index:02d}_{key.replace('.', '_')}_{label}.mp3"
        target = OUT / filename
        async with sem:
            for attempt in range(3):
                try:
                    await edge_tts.Communicate(speech, voice, rate=RATE).save(str(target))
                    if target.stat().st_size <= 1024:
                        raise RuntimeError(f"Too small: {target}")
                    return
                except Exception:
                    target.unlink(missing_ok=True)
                    if attempt == 2:
                        raise
                    await asyncio.sleep(2 ** attempt)
    jobs = []
    for i, word in enumerate(WORDS, 1):
        for trial in TRIALS:
            filename = f"{i:02d}_{word[0].replace('.', '_')}_{trial[0]}.mp3"
            metadata.append({
                "file": filename, "semantic_key": word[0], "display": word[1],
                "target_sound": "voiced velar stop /ɡ/ (Libyan g)",
                "candidate": trial[0], "voice_id": trial[1],
                "tts_input": word[trial[2]], "rate": RATE,
                "acoustically_verified": "NO",
            })
            jobs.append(render(i, word, trial))
    await asyncio.gather(*jobs)
    with (OUT / "trials.csv").open("w", newline="", encoding="utf-8-sig") as out:
        writer = csv.DictWriter(out, fieldnames=list(metadata[0]))
        writer.writeheader()
        writer.writerows(metadata)
    (OUT / "README.txt").write_text(
        "LIBYAN /G/ LISTENING TRIALS — NOT PRODUCTION AUDIO\n"
        "Listen to the 4 choices per word. None is assumed correct.\n"
        "iman_qaf_current: existing Iman voice with standard ق.\n"
        "omar_qaf: Omar Libyan voice with standard ق.\n"
        "iman_kaf_control: Iman voice with ك, voiceless control (not true /g/).\n"
        "egyptian_jeem_g_candidate: Egyptian Salma with ج (different voice).\n"
        "These trials only evaluate audible possibilities; some candidates will be wrong.\n"
        "Keep all v1.6.2 app audio untouched until a native speaker confirms /g/.\n"
        "For exact IPA, official Azure Speech SSML may be evaluated with credentials\n"
        "and a live validation of locale support; edge-tts does not support phoneme SSML.\n",
        encoding="utf-8"
    )
    print(json.dumps({"samples": len(metadata), "words": len(WORDS)}, ensure_ascii=False))


if __name__ == "__main__":
    asyncio.run(generate())
