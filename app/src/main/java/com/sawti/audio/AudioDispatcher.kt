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
        speech.speak(
            item.ttsPrompt,
            onStart = { if (current == generation) _playback.value =
                PlaybackVisual(activeWordId = item.id, isSpeaking = true) },
            onDone = { if (current == generation) _playback.value = PlaybackVisual() },
        )
    }

    /** One real continuous utterance; optional TTS range callbacks follow each spoken chip. */
    fun speakSentence(pieces: List<SentencePiece>) {
        val text = SentenceComposer.spokenSentence(pieces)
        if (text.isBlank()) return
        stopAll()
        val current = generation
        val ranges = SentenceComposer.spokenSegments(pieces)
        fun show(index: Int) {
            if (current == generation && index in pieces.indices) {
                _playback.value = PlaybackVisual(
                    activeWordId = pieces[index].sourceId,
                    sentenceIndex = index,
                    isSpeaking = true,
                )
            }
        }
        speech.speak(
            text,
            onStart = { if (ranges.isNotEmpty()) show(ranges.first().pieceIndex) },
            onRange = { character ->
                ranges.firstOrNull { character >= it.start && character < it.endExclusive }?.let {
                    show(it.pieceIndex)
                }
            },
            onDone = { if (current == generation) _playback.value = PlaybackVisual() },
        )
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
