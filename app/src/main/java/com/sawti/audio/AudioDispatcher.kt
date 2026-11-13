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
        fun speakFallback() {
            if (current != generation) return
            _playback.value = PlaybackVisual(activeWordId = item.id, isSpeaking = true)
            speech.speak(item.speechText,
                onDone = { if (current == generation) _playback.value = PlaybackVisual() })
        }
        if (recording != null && player.play(recording,
                onDone = { if (current == generation) _playback.value = PlaybackVisual() },
                onFailed = { speakFallback() })) {
            if (current == generation) _playback.value =
                PlaybackVisual(activeWordId = item.id, isSpeaking = true)
            return
        }
        speakFallback()
    }

    /** Sentence-strip playback is independent of the retired card hint feature. */
    fun speakSentence(pieces: List<SentencePiece>) {
        val speakable = SentenceComposer.spokenPieces(pieces)
        if (speakable.isEmpty()) return
        stopAll()
        val current = generation
        fun playPiece(position: Int) {
            if (current != generation) return
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
