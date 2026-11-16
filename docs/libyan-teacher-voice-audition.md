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
