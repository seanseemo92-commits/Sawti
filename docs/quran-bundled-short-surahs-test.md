# Sawti Quran 1.4 test — fully offline short surahs

Scope: all **13** existing short surahs, **67** full ayahs, each recited by
Alafasy, Salah Bukhatir and Husary Muallim (**201** 128-kbps MP3
files; one damaged original requires documented recovery), plus all 13 verified Uthmani response documents.

This is a dev-branch, separately installable Debug test, not a published
production release. The pre-existing 22 segmented cards and their 66 bundled
MP3 files remain unchanged. No reciter selection, verses, wording, timing,
card design, audio focus or repeat behavior is modified.

Run `python3 scripts/prepare_quran_surahs.py` to download the recordings from
EveryAyah (with a Maqra-hosted mirror) and canonical text from AlQuran.cloud.
Run `python3 scripts/verify_quran_surahs.py --decode` before committing.
The generated `app/src/main/assets/quran_surahs/catalog.json` pins each
recording and text by SHA-256. The Android code checks original MP3 checksums
when it copies individual verses from APK assets to private playback storage.
Previously downloaded but differently encoded short-surah files are replaced
with the verified bundled variant on first playback; damaged private copies
are repaired offline. All 13 texts are readable immediately after install.

Sources and credits:
- Text: https://api.alquran.cloud — Uthmani Qur'an text, unchanged.
- Alafasy: EveryAyah / Alafasy_128kbps.
- Salah Bukhatir: EveryAyah / Salaah_AbdulRahman_Bukhatir_128kbps.
- Husary Muallim: EveryAyah / Husary_Muallim_128kbps.
- Mirror: https://huggingface.co/maqra-project
- Original recording rights remain with their respective rights-holders;
  neither EveryAyah nor the mirrors establish a blanket redistribution
  license. Confirm applicable permissions before any public distribution.

The original Husary Muallim 113:5 file has a damaged MP3 frame in EveryAyah
and its mirrors. A single exceptional tolerant decode/re-encode at 128kbps
is permitted *only for this verse*, provided the resulting file decodes
without errors and its duration differs by at most 2%. This is not lossless,
and a person must verify that every word and ending is intact. The generated
catalog records the operation. No other source may silently be re-encoded.

Tests verify offline text, all 201 reader/ayah combinations, local playback,
damaged-file recovery, asset checksums, MP3 decoding and the actual APK
contents. Human listening quality still requires listening review.
