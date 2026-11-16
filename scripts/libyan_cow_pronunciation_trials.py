#!/usr/bin/env python3
"""Non-production listening variations for Libyan word 'بقرة' only.

Voice and delivery are *fixed* to the user-selected teacher candidate:
ar-EG-ShakirNeural, rate -8%, pitch +12Hz. Only phonetic spellings differ.
Only human Libyan listening can validate /ɡ/ + vowel and rhythm accuracy.
"""
import asyncio
import csv
import json
from pathlib import Path
import edge_tts

SOURCE = Path("scripts/neural_v162_overrides.csv")
OUT = Path("build/libyan-cow-pronunciation-trials")
VOICE = "ar-EG-ShakirNeural"
RATE = "-8%"
PITCH = "+12Hz"

CANDIDATES = (
    ("01_existing_bajra", "بَجْرَة", "النص الحالي في عينة شاكر 3"),
    ("02_bajara", "بَجَرَة", "فتحة بعد الجيم بدلاً من السكون"),
    ("03_bajrah", "بَجْرَهْ", "سكون بعد الجيم ونهاية هاء ساكنة"),
    ("04_bajarah", "بَجَرَهْ", "فتحة بعد الجيم ونهاية هاء ساكنة"),
    ("05_bajra_long_a", "بَجْرَا", "ألف في النهاية للمقارنة"),
    ("06_bajra_no_ta", "بَجْرَ", "نهاية مفتوحة بدون تاء مربوطة"),
    ("07_baqra_control", "بَقْرَة", "قاف عربية للمقارنة - ليست بالضرورة G"),
    ("08_baqara_control", "بَقَرَة", "قاف وفتحة للمقارنة - ليست بالضرورة G"),
    ("09_bakra_control", "بَكْرَة", "كاف للمقارنة - ليست G"),
)

async def save(variant, text, limiter):
    async with limiter:
        path = OUT / (variant + ".mp3")
        for attempt in range(4):
            partial = path.with_suffix(".partial")
            partial.unlink(missing_ok=True)
            try:
                await edge_tts.Communicate(text, voice=VOICE, rate=RATE, pitch=PITCH).save(str(partial))
                if partial.stat().st_size <= 1024:
                    raise IOError("Short or empty TTS response")
                partial.replace(path)
                return
            except Exception:
                partial.unlink(missing_ok=True)
                if attempt == 3:
                    raise
                await asyncio.sleep(min(16, 2 ** attempt))

async def main():
    with SOURCE.open(encoding="utf-8-sig", newline="") as inp:
        rows = [r for r in csv.DictReader(inp)
                if r["dialect"] == "LIBYAN" and r["semantic_key"] == "animal.cow"]
    assert len(rows) == 1 and rows[0]["display"] == "بقرة"
    assert rows[0]["neural_speech_text"] == "بَقْرَة"
    OUT.mkdir(parents=True, exist_ok=True)
    limiter = asyncio.Semaphore(2)
    await asyncio.gather(*(save(key, text, limiter) for key,text,_ in CANDIDATES))
    with (OUT / "INDEX.csv").open("w", newline="", encoding="utf-8-sig") as f:
        writer = csv.DictWriter(f, fieldnames=["number","candidate","tts_text","label","voice","rate","pitch","validated_by_libyan_speaker"])
        writer.writeheader()
        for i,(name, spoken, label) in enumerate(CANDIDATES, 1):
            writer.writerow(dict(number=i,candidate=name,tts_text=spoken,label=label,
                                 voice=VOICE, rate=RATE,pitch=PITCH,
                                 validated_by_libyan_speaker="NO"))
    (OUT / "README.txt").write_text(
        "SAWTI: nine isolated 'بقرة' pronunciation candidates.\n"
        "Same teacher voice ar-EG-ShakirNeural (rate -8%, pitch +12Hz).\n"
        "All are hypotheses, none automatically declared correct.\n"
        "Some are intentionally bad controls (/q/, /k/, wrong ending).\n"
        "Do not replace application audio or change displayed spelling until user approves.\n",
        encoding="utf-8",
    )
    print(json.dumps({"samples":len(CANDIDATES),"voice":VOICE,"rate":RATE,"pitch":PITCH}))

if __name__ == "__main__":
    asyncio.run(main())
