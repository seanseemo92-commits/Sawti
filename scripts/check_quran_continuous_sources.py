#!/usr/bin/env python3
"""Read-only connectivity, exact source, and verse-timing audit for Sawti Quran.

No generated audio enters the APK. Use this report to determine whether precise
source-matched timing exists; do not guess verse boundaries or claim that MP3Quran
and EveryAyah editions are the same recording merely because the reciter matches.
"""
from __future__ import annotations
import json
import ssl
import urllib.request
import urllib.error
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
REPORT = ROOT / "previews" / "quran-full-playback-test" / "SOURCE-TIMING-AUDIT.json"
HOSTS = ("https://www.mp3quran.net", "https://mp3quran.net", "https://apiquran.mp3quran.net")
READERS = {
    "ar.alafasy": ("alafasy", "afs", "mish"),
    "salah.bukhatir": ("bukh", "bukhat", "bu_kh"),
    "husary.muallim": ("husary", "husari", "husr", "hsry"),
}
SURAS = (1, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114)
UA = "Sawti/1.4 (research source alignment, no audio redistribution)"

def fetch(url: str, limit: int = 6_000_000) -> bytes:
    request = urllib.request.Request(url, headers={"User-Agent": UA, "Accept": "application/json,*/*"})
    with urllib.request.urlopen(request, timeout=16) as response:
        if response.status != 200:
            raise ValueError(f"HTTP {response.status}")
        content = response.read(limit + 1)
        if len(content) > limit:
            raise ValueError("JSON over limit")
        return content

def api(path: str):
    errors = []
    for host in HOSTS:
        try:
            raw = fetch(host + "/api/v3/" + path)
            j = json.loads(raw)
            if isinstance(j, (list, dict)):
                return j, host
        except Exception as exc:
            errors.append(f"{host}: {str(exc)[:180]}")
    raise RuntimeError("; ".join(errors))

def listify(value):
    if isinstance(value, list):
        return value
    if isinstance(value, dict):
        for key in ("data", "reciters", "reads", "timings"):
            if isinstance(value.get(key), list):
                return value[key]
    return []

def run():
    report = {"method":"verified MP3Quran API mapping; filenames from actual source listing", "available":{}, "apiErrors":[]}
    try:
        reads, host = api("ayat_timing/reads")
        report["apiHost"] = host
        entries = listify(reads)
        report["readsCount"] = len(entries)
        for key, tokens in READERS.items():
            matches = [v for v in entries if isinstance(v,dict) and
                any(s in (str(v.get("name",""))+" "+str(v.get("folder_url",""))).lower() for s in tokens)]
            report["available"][key] = []
            for v in matches:
                summary={"id":v.get("id"),"name":v.get("name"),"folder_url":v.get("folder_url"),
                    "soar_count":v.get("soar_count")}
                try:
                    read_id=int(v["id"])
                    data,_=api(f"ayat_timing?surah=2&read={read_id}")
                    rows=listify(data)
                    valid={int(x["ayah"]):x for x in rows if isinstance(x,dict) and str(x.get("ayah","")).isdigit() and
                        "start_time" in x and "end_time" in x}
                    summary["hasBaqarahEnd"]=285 in valid and 286 in valid
                    if summary["hasBaqarahEnd"]:
                        summary["alBaqarahEnd"]=[{"ayah":k,"startMs":int(valid[k]["start_time"]),"endMs":int(valid[k]["end_time"])} for k in (285,286)]
                    summary["ayatTimingRows"]=len(valid)
                except Exception as exc:
                    summary["timingError"]=str(exc)[:150]
                report["available"][key].append(summary)
    except Exception as exc:
        report["apiErrors"].append(str(exc))
    # HEAD source availability, bounded: NEVER read/download all of Al-Baqarah in audit.
    urls={
        "ar.alafasy":"https://cdn.mp3quran.net/download/audio/mishary-alafasy/r1/002.mp3",
        "salah.bukhatir":"https://cdn.mp3quran.net/download/audio/salah-bukhatir/r1/002.mp3",
        "husary.muallim":"https://archive.org/download/Mushaf_Moalim_3/002.mp3",
    }
    report["fullBaqarah"]={}
    for key,url in urls.items():
        item={"url":url}
        try:
            req=urllib.request.Request(url, method="HEAD",headers={"User-Agent":UA})
            with urllib.request.urlopen(req,timeout=25) as resp:
                item.update(http=int(resp.status),bytes=int(resp.headers.get("Content-Length","0")),
                    acceptsRanges=resp.headers.get("Accept-Ranges"),
                    contentType=resp.headers.get("Content-Type"))
        except Exception as exc:
            item["error"]=str(exc)[:250]
        report["fullBaqarah"][key]=item
    REPORT.parent.mkdir(parents=True,exist_ok=True)
    REPORT.write_text(json.dumps(report,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
    print("TIMING_AUDIT_SUMMARY",json.dumps({"counts":{k:len(v) for k,v in report["available"].items()},
        "errors":report["apiErrors"],"head":report["fullBaqarah"]},ensure_ascii=False),flush=True)

if __name__ == "__main__":
    run()
