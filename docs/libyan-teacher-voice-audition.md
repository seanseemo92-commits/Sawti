# Teacher-like supportive voice — Libyan Sawti

Feedback 2026-11-16: the Egyptian Shakir candidate has clearer consonants for some Libyan words but feels **sad/depressing**, which is inappropriate for an encouraging communication/learning aid. User prioritizes an approachable, lively, educational voice with clear articulation like a **patient teacher**.

Important:
- Microsoft does not document expressive speaking styles for `ar-EG-ShakirNeural` (or these other tested Arabic standard voices). Pitch and rate alone cannot guarantee joy or teaching prosody.
- Run six six-word demos comparing neutral Shakir, warmer Shakir (+6 Hz and -12% rate), brighter Shakir (+12 Hz and -8% rate), Omar Libyan (+8 Hz, -12%), Salma Egyptian (+7 Hz, -10%) and Hamed Saudi (+7 Hz, -12%).
- Each contains four suspect G words (`action.sit`, `action.sleep`, `food.coffee`, `animal.cow`) plus two highly relevant patient requests (`request.water`, `request.toilet`).
- Compare separately: pronunciation accuracy; emotional tone (warm and lively, not sad); comfortable pace; teaching clarity; natural Libyan sound; same sound across multiple words.
- Do not replace production sound or conflate higher pitch with greater perceived encouragement. If all are unsuitable, evaluate *licensed expressive Arabic TTS* or consented human narrator as next step.

## User choice — 2026-11-16

User listened to the six test reels and selected **#3 (“03_shakir_bright”)** as the satisfactory livelier/educational voice candidate: `ar-EG-ShakirNeural`, Edge TTS `rate="-8%"`, `pitch="+12Hz"`. Preserve the Libyan **display** text unchanged. The six-word listening choice establishes a preferred **delivery profile**, not proof that all 271 Libyan entries have native pronunciation or that every hard-/g/ word is correct. Keep pronunciation validation (including G vs J), timing, warmth, and within-word naturalness as explicit QA gates before replacing production MP3s or changing main. No app assets should be overwritten solely from this choice.

## Specific user phonetic correction — بُقْرَه (2026-11-16)

The user explicitly corrected the **initial vowel** in the Libyan cow word to **ḍamma**: `بُقْرَه` (NOT `بَقْرَة`). This is an authoritative language-user spelling correction even while acoustic realization of /ɡ/ is still unresolved. The previous trial incorrectly began with fatḥa. Nine revised comparison clips now **all start with بُ**, and include the user's exact written form and candidate Egyptian-jeem phonetic surrogates. Do not infer that Egyptian ج always means /ɡ/. Preserve the chosen Shakir lively voice profile at `-8%`, `+12Hz`, with production assets unchanged until a correct-sounding clip is confirmed.

## Listening result — animal.cow approved (2026-11-16)

The user listened to the **damma-corrected** nine-candidate comparison and confirmed **sample #1** — `01_bujrah.mp3` — **correct** for Libyan «بُقْرَه». The approved synthesis is `ar-EG-ShakirNeural`, `rate=-8%`, `pitch=+12Hz`, **speech input `بُجْرَهْ`**. Preserve original displayed label «بقرة»; target pronunciation **بُقْرَه** and spoken TTS surrogate **بُجْرَهْ** serve different roles. Approved exactly one word, **not** an automatic ق→ج rule, and does not approve all other 270 words. The candidate is recorded in `scripts/libyan_shakir_approved_samples.csv`. Do not modify `main`, original PR #53 or production MP3 assets solely on this selection; use the approved audio/text as one verified item in a subsequent reviewed Libyan pack.

## Approved Libyan default voice (2026-11-16)

User explicitly approved **Shakir** for the Libyan dialect as the new preferred built-in voice, preserving the already chosen educational/lively profile: `ar-EG-ShakirNeural`, `rate=-8%`, `pitch=+12Hz`. The source table has exactly 271 Libyan entries, and the voice generator now uses this profile **only for LIBYAN**. Eight other dialects preserve their original speakers and their `-18%` rates. The one native-speaker approved lexical correction is `animal.cow`: visible `بقرة`, user-target pronunciation `بُقْرَه` and synthesized surrogate `بُجْرَهْ`; never apply this graphemic replacement to the entire Libyan catalog. All remaining 270 Libyan spoken forms are pending per-word listening verification; selecting a voice does not prove their accuracy. Generate and verify MP3s before use, and keep changes confined to this branch/PR until a tested app build is accepted.
