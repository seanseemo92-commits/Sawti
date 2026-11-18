package com.sawti.audio

import android.util.Log
import com.sawti.domain.model.CardDetails
import com.sawti.domain.model.DialectSpeechCatalog
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

/**
 * Bundled cards: caregiver recording -> verified local Habibi MP3 -> STOP.
 * Custom cards: caregiver recording -> Android SpeechManager.
 * Dynamic questions/hints: Android SpeechManager (not bundled cards).
 *
 * A missing/corrupt Habibi clip NEVER invokes Android TTS, Microsoft Edge
 * or a generated fallback, including during sentence-strip playback.
 */
class AudioDispatcher(
    private val speech: SpeechManager,
    private val player: AudioPlayer,
    private val habibi: HabibiAudioResolver? = null,
) {
    private val _playback = MutableStateFlow(PlaybackVisual())
    val playback: StateFlow<PlaybackVisual> = _playback.asStateFlow()
    private var generation = 0L
    private var quranStopper: (() -> Unit)? = null

    fun registerQuranStopper(stopper: () -> Unit) { quranStopper = stopper }
    fun unregisterQuranStopper(stopper: () -> Unit) {
        if (quranStopper === stopper) quranStopper = null
    }

    private fun playHabibi(
        key: String?,
        text: String?,
        done: () -> Unit,
        canceled: () -> Unit,
        failed: () -> Unit,
    ): Boolean {
        val resolver = habibi ?: return false
        val prepared = text?.takeIf { it.isNotBlank() } ?: return false
        val afd = resolver.open(speech.activeDialect, key, prepared) ?: return false
        val speed = NeuralPlaybackSpeed.forTtsRate(speech.activeSpeechRate)
        return player.playAsset(afd, onDone = done, onCanceled = canceled,
            onFailed = failed, speed = speed)
    }

    fun play(item: WordItem) {
        stopAll()
        val current = generation
        val done = { if (current == generation) _playback.value = PlaybackVisual() }
        val canceled = { if (current == generation) _playback.value = PlaybackVisual() }
        val mark = {
            if (current == generation) {
                _playback.value = PlaybackVisual(activeWordId = item.id, isSpeaking = true)
            }
        }
        val isCustom = item.semanticKey == null || item.categoryId.startsWith("custom_") ||
            item.customTextFields and (CardDetails.LABEL or CardDetails.SPEECH) != 0
        val onMissing = {
            if (current == generation) {
                Log.e("SawtiHabibi", "Built-in audio missing/invalid: " +
                    speech.activeDialect.name + "/" + item.semanticKey)
                canceled()
            }
        }
        val startHabibi = {
            if (current != generation) false
            else playHabibi(
                item.semanticKey,
                DialectSpeechCatalog.habibiTextFor(item, speech.activeDialect),
                done, canceled, onMissing,
            )
        }
        val recorded = item.audioPath?.takeIf { File(it).isFile }
        if (recorded != null && player.play(
                recorded, onDone = done, onCanceled = canceled,
                onFailed = {
                    if (isCustom) {
                        speech.speak(item.speechText, onDone = done, onCanceled = canceled)
                    } else if (!startHabibi()) onMissing()
                })) {
            mark()
            return
        }
        if (isCustom) {
            mark()
            speech.speak(item.speechText, onDone = done, onCanceled = canceled)
            return
        }
        if (startHabibi()) mark() else onMissing()
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
                activeWordId = piece.sourceId, sentenceIndex = piece.pieceIndex,
                isSpeaking = true,
            )
            val done = { if (current == generation) playPiece(position + 1) }
            val canceled = { if (current == generation) _playback.value = PlaybackVisual() }
            val onMissing = {
                Log.e("SawtiHabibi", "Sentence contains missing/invalid audio: " +
                    speech.activeDialect.name + "/" + piece.semanticKey)
                canceled()
            }
            val fallbackCustom = {
                if (current == generation) speech.speak(
                    piece.speech, onDone = done, onCanceled = canceled)
            }
            val playNative = {
                if (current != generation) false
                else if (piece.isCustom) false
                else playHabibi(
                    piece.semanticKey,
                    piece.neuralSpeechText.takeIf {
                        piece.neuralDialect == speech.activeDialect
                    },
                    done, canceled, onMissing,
                )
            }
            val recorded = piece.audioPath?.takeIf { File(it).isFile }
            if (recorded != null && player.play(
                    recorded, onDone = done, onCanceled = canceled,
                    onFailed = {
                        if (piece.isCustom) fallbackCustom()
                        else if (!playNative()) onMissing()
                    })) return
            if (piece.isCustom) fallbackCustom()
            else if (!playNative()) onMissing()
        }
        playPiece(0)
    }

    /** Spoken hints/questions are dynamic, not built-in card playback. */
    fun hint(item: WordItem) {
        stopAll()
        val cue = item.phonemicCue.ifBlank {
            PhonemicCue.derive(item.speechText.ifBlank { item.ttsPrompt })
        }
        speech.speak(PhonemicCue.forSpeech(cue))
    }

    fun say(text: String) {
        // Settings previews the built-in water card through this existing API.
        // It must obey the same local-only policy even when its asset is absent.
        val preview = DialectPreview.matching(text, speech.activeDialect)
        if (preview != null) play(preview)
        else { stopAll(); speech.speak(text) } // Dynamic questions, not card audio.
    }
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
