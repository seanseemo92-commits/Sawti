package com.sawti.audio

import com.sawti.domain.model.PhonemicCue
import com.sawti.domain.model.SentenceComposer
import com.sawti.domain.model.SentencePiece
import com.sawti.domain.model.WordItem
import com.sawti.speech.SpeechManager
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Visual playback follows real player and TTS lifecycle; no guessed timers or always-on loops. */
data class PlaybackVisual(
    val activeWordId: Long? = null,
    val sentenceIndex: Int? = null,
    val isSpeaking: Boolean = false,
)

/** Shared application-scoped voice channel for spoken AAC cards and caregiver recordings. */
class AudioDispatcher(
    private val speech: SpeechManager,
    private val player: AudioPlayer,
) {
    private val _playback = MutableStateFlow(PlaybackVisual())
    val playback: StateFlow<PlaybackVisual> = _playback.asStateFlow()
    private var generation = 0L

    /** Main tap: caregiver recording when available, TTS otherwise. */
    fun play(item: WordItem) {
        stopAll()
        val current = generation
        val recording = item.audioPath?.takeIf { File(it).exists() }
        if (recording != null && player.play(recording, onDone = {
                if (current == generation) _playback.value = PlaybackVisual()
            })) {
            if (current == generation) _playback.value =
                PlaybackVisual(activeWordId = item.id, isSpeaking = true)
            return
        }
        // The card becomes active immediately on a tap, not after a delayed engine callback.
        _playback.value = PlaybackVisual(activeWordId = item.id, isSpeaking = true)
        // All dialects use the same engine language. Only speechText changes;
        // SpeechManager owns the general-arabic language fallback policy.
        speech.speak(
            item.speechText,
            onDone = { if (current == generation) _playback.value = PlaybackVisual() },
        )
    }

    /**
     * Each sentence piece has its own real TTS completion callback, so the active aura
     * follows the actual speaking piece. Never guess word times or depend on onRangeStart
     * (which many Android Arabic TTS engines do not implement).
     */
    fun speakSentence(pieces: List<SentencePiece>) {
        val speakable = SentenceComposer.spokenPieces(pieces)
        if (speakable.isEmpty()) return
        stopAll()
        val current = generation
        fun playPiece(position: Int) {
            if (current != generation) return // interrupted by any other voice/playback
            if (position == speakable.size) {
                _playback.value = PlaybackVisual()
                return
            }
            val piece = speakable[position]
            _playback.value = PlaybackVisual(
                activeWordId = piece.sourceId,
                sentenceIndex = piece.pieceIndex,
                isSpeaking = true,
            )
            // Sequential completion callbacks are independent of unsupported Arabic
            // TTS word-range timings. Each piece becomes active before requesting speech.
            speech.speak(
                piece.speech,
                onDone = { if (current == generation) playPiece(position + 1) },
                onCanceled = { if (current == generation) _playback.value = PlaybackVisual() },
            )
        }
        playPiece(0)
    }

    fun hint(item: WordItem) {
        stopAll()
        val cue = item.phonemicCue.ifBlank { PhonemicCue.derive(item.ttsPrompt) }
        speech.speak(PhonemicCue.forSpeech(cue))
    }

    fun sentenceHint(item: WordItem) {
        val prompt = item.sentencePrompt?.takeIf { it.isNotBlank() } ?: return
        stopAll()
        speech.speak(PhonemicCue.forSpeech(prompt))
    }

    fun say(text: String) {
        stopAll()
        speech.speak(text)
    }

    fun playFile(path: String): Boolean {
        stopAll()
        return player.play(path)
    }

    fun stop() = stopAll()

    private fun stopAll() {
        ++generation
        _playback.value = PlaybackVisual()
        speech.stop()
        player.stop()
    }
}
