#!/usr/bin/env python3
"""Check waveform identity: Husary Muallim archive whole Baqarah vs. existing EveryAyah clips.

A matching reciter's name is not enough. No source audio is committed or redistributed.
The report is diagnostic and only validated matches may be used for timed UI.
"""
from __future__ import annotations
import json, os, subprocess, tempfile, urllib.request, math
from pathlib import Path
import numpy as np
from scipy.signal import correlate
OUT=Path("previews/quran-full-playback-test/HUSARY-ALIGNMENT-AUDIT.json")
UA={"User-Agent":"Sawti-Research/1.4"}
SOURCE="https://archive.org/download/Mushaf_Moalim_3/002.mp3"
def download(url,path,rangeHeader=None):
    headers=UA.copy()
    if rangeHeader: headers["Range"]=rangeHeader
    req=urllib.request.Request(url,headers=headers)
    with urllib.request.urlopen(req,timeout=90) as r:
        status=r.status
        length=int(r.headers.get("Content-Length","0"))
        with open(path,"wb") as f:
            while True:
                b=r.read(256*1024)
                if not b:break
                f.write(b)
        return {"status":status,"contentRange":r.headers.get("Content-Range"),"length":Path(path).stat().st_size}
def decode(path):
    return np.frombuffer(subprocess.check_output(["ffmpeg","-nostdin","-hide_banner","-loglevel","error","-i",str(path),"-ac","1","-ar","2000","-f","s16le","-"],timeout=120),dtype=np.int16).astype(np.float32)/32768.
def aligned(a,b):
    # waveform fingerprint: source clip a embedded in full-tail b at the same speed.
    # Try both signs; normalised peak is meaningful ONLY for matching source session.
    m=min(50_000,len(a))
    if m>len(b):return {"score":0.,"timeInTailSec":None}
    start=max(0,(len(a)-m)//2)
    v=a[start:start+m]
    v=v-v.mean()
    if not np.linalg.norm(v):return {"score":0.,"timeInTailSec":None}
    b=b-b.mean()
    cc=correlate(b,v,mode="valid",method="fft")
    # approximate normalization to account for mastering differences
    sq=np.cumsum(np.r_[0,(b*b).astype(np.float64)])
    power=np.maximum(1e-12,sq[m:]-sq[:-m])
    norm=np.sqrt(power)*np.linalg.norm(v)
    c=cc/norm
    i=int(np.nanargmax(np.abs(c)))
    return {"score":round(float(abs(c[i])),4),"timeInTailSec":round((i-start)/2000,3)}
def run():
    out={"source":SOURCE,"method":"resampled waveform correlation (2 kHz), last 8MB of full Baqarah; no approximate timing accepted","ayat":{}}
    with tempfile.TemporaryDirectory() as td:
        td=Path(td)
        tail=td/"end.mp3"
        out["tail"]=download(SOURCE,tail,"bytes=-8388608")
        if out["tail"]["status"]!=206:raise RuntimeError("Host did not honor bounded byte-range")
        samples=decode(tail)
        out["tailAudioSeconds"]=round(len(samples)/2000,3)
        for ayah in (285,286):
            url=f"https://everyayah.com/data/Husary_Muallim_128kbps/002{ayah}.mp3"
            clip=td/f"{ayah}.mp3"
            out["ayat"][str(ayah)]={"reference":url}
            out["ayat"][str(ayah)]["download"]=download(url,clip)
            reference=decode(clip)
            out["ayat"][str(ayah)]["referenceAudioSeconds"]=round(len(reference)/2000,3)
            out["ayat"][str(ayah)].update(aligned(reference,samples))
    OUT.parent.mkdir(parents=True,exist_ok=True)
    OUT.write_text(json.dumps(out,ensure_ascii=False,indent=2)+"\n")
    print("MATCH_REPORT",json.dumps(out,ensure_ascii=False),flush=True)
if __name__=="__main__":run()
