package com.sawti.audio

import com.sawti.domain.model.NeuralPlaybackPolicy
import com.sawti.domain.model.PhonemicCue
import com.sawti.domain.model.SentenceComposer
import com.sawti.domain.model.SentencePiece
import com.sawti.domain.model.WordItem
import com.sawti.speech.SpeechManager
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PlaybackVisual(
    val activeWordId: Long? = null,
    val sentenceIndex: Int? = null,
    val isSpeaking: Boolean = false,
)

/** Priority: caregiver recording -> exact-text neural clip -> dialect-aware Android TTS. */
class AudioDispatcher(
    private val speech: SpeechManager,
    private val player: AudioPlayer,
    private val neural: NeuralAudioResolver? = null,
) {
    private val _playback = MutableStateFlow(PlaybackVisual())
    val playback: StateFlow<PlaybackVisual> = _playback.asStateFlow()
    private var generation = 0L
    private var quranStopper: (() -> Unit)? = null

    fun registerQuranStopper(stopper: () -> Unit) { quranStopper = stopper }
    fun unregisterQuranStopper(stopper: () -> Unit) {
        if (quranStopper === stopper) quranStopper = null
    }

    private fun playNeural(key: String?, text: String, done: () -> Unit,
        canceled: () -> Unit, failed: () -> Unit): Boolean {
        val resolver = neural ?: return false
        val speed = NeuralPlaybackSpeed.forTtsRate(speech.activeSpeechRate)
        return when (val clip = resolver.resolve(speech.activeDialect, key, text)) {
            is NeuralClip.Downloaded ->
                player.play(clip.file.path, onDone = done, onCanceled = canceled,
                    onFailed = failed, speed = speed)
            is NeuralClip.Bundled -> {
                val afd = resolver.openBundled(clip) ?: return false
                player.playAsset(afd, onDone = done, onCanceled = canceled,
                    onFailed = failed, speed = speed)
            }
            null -> false
        }
    }

    fun play(item: WordItem) {
        stopAll()
        val current = generation
        val done = { if (current == generation) _playback.value = PlaybackVisual() }
        val canceled = { if (current == generation) _playback.value = PlaybackVisual() }
        val mark = { if (current == generation)
            _playback.value = PlaybackVisual(activeWordId = item.id, isSpeaking = true) }
        val fallback = {
            if (current == generation) {
                mark()
                speech.speak(item.speechText, onDone = done, onCanceled = canceled)
            }
        }
        // Require the exact projected dialect label, legacy TTS string and neural text.
        // Never let a custom edit or a stale language projection reuse a built-in clip.
        val neuralText = NeuralPlaybackPolicy.forCard(item, speech.activeDialect)
        val startNeural = {
            current == generation && neuralText != null &&
                playNeural(item.semanticKey, neuralText, done, canceled, fallback)
        }
        val recording = item.audioPath?.takeIf { File(it).isFile }
        if (recording != null && player.play(recording,
                onDone = done, onCanceled = canceled,
                onFailed = { if (!startNeural()) fallback() })) {
            mark()
            return
        }
        if (startNeural()) { mark(); return }
        fallback()
    }

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
            val done = { if (current == generation) playPiece(position + 1) }
            val canceled = { if (current == generation) _playback.value = PlaybackVisual() }
            val fallback = {
                if (current == generation) speech.speak(piece.speech,
                    onDone = done, onCanceled = canceled)
            }
            val startNeural = {
                current == generation && piece.neuralDialect == speech.activeDialect &&
                    piece.neuralSpeechText != null &&
                    playNeural(piece.semanticKey, piece.neuralSpeechText,
                        done, canceled, fallback)
            }
            val recording = piece.audioPath?.takeIf { File(it).isFile }
            if (recording != null && player.play(recording,
                    onDone = done, onCanceled = canceled,
                    onFailed = { if (!startNeural()) fallback() })) return
            if (startNeural()) return
            fallback()
        }
        playPiece(0)
    }

    fun hint(item: WordItem) {
        stopAll()
        val cue = item.phonemicCue.ifBlank { PhonemicCue.derive(item.speechText.ifBlank { item.ttsPrompt }) }
        speech.speak(PhonemicCue.forSpeech(cue))
    }

    fun say(text: String) { stopAll(); speech.speak(text) }
    fun playFile(path: String): Boolean { stopAll(); return player.play(path) }
    fun stop() = stopAll()

    fun stopAac() {
        ++generation
        _playback.value = PlaybackVisual()
        speech.stop()
        player.stop()
    }

    private fun stopAll() {
        quranStopper?.invoke()
        stopAac()
    }
}
