package com.mywords.audio

import com.mywords.domain.model.PhonemicCue
import com.mywords.domain.model.WordItem
import com.mywords.speech.SpeechManager
import java.io.File

/**
 * The single audio entry point for the whole app (TTS + MediaPlayer).
 *
 * It is created once by the Application container and only holds the application context,
 * so it survives Activity recreation (theme switches, rotation) without leaks or cut-offs.
 * Stop-and-play: every call first stops whatever is playing, then starts the new sound.
 */
class AudioDispatcher(
    private val speech: SpeechManager,
    private val player: AudioPlayer,
) {
    /** Main tap: custom recording / imported file if present (and playable), otherwise the vocalised ttsPrompt. */
    fun play(item: WordItem) {
        stopAll()
        val recording = item.audioPath?.takeIf { File(it).exists() }
        if (recording != null && player.play(recording)) return
        speech.speak(item.ttsPrompt)
    }

    /** 💡 button: only the first syllable, so she can try to finish the word herself. */
    fun hint(item: WordItem) {
        stopAll()
        val cue = item.phonemicCue.ifBlank { PhonemicCue.derive(item.ttsPrompt) }
        speech.speak(PhonemicCue.forSpeech(cue))
    }

    /** 💬 button: the cloze carrier phrase ("نَأْكُلُ الـ...") that stops right before the target word. */
    fun sentenceHint(item: WordItem) {
        val prompt = item.sentencePrompt?.takeIf { it.isNotBlank() } ?: return
        stopAll()
        speech.speak(PhonemicCue.forSpeech(prompt))
    }

    /** Speaks arbitrary text (used for the "أين …؟" questions). */
    fun say(text: String) {
        stopAll()
        speech.speak(text)
    }

    /** Plays a caregiver audio file directly (editor preview). */
    fun playFile(path: String): Boolean {
        stopAll()
        return player.play(path)
    }

    fun stop() = stopAll()

    private fun stopAll() {
        speech.stop()
        player.stop()
    }
}
