package com.tenco.core.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** Thin wrapper around Android TextToSpeech for localized voice prompts (accessibility aid). */
@Singleton
class TtsManager @Inject constructor(@ApplicationContext context: Context) {

    private var ready = false
    private var pendingLanguageTag: String? = null

    private val tts = TextToSpeech(context) { status ->
        ready = status == TextToSpeech.SUCCESS
        pendingLanguageTag?.let { applyLanguage(it) }
    }

    fun setLanguage(tag: String) {
        if (ready) applyLanguage(tag) else pendingLanguageTag = tag
    }

    private fun applyLanguage(tag: String) {
        val result = tts.setLanguage(Locale(tag))
        // Fall back to the device default if the locale's voice data isn't installed.
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts.language = Locale.getDefault()
        }
    }

    fun speak(text: String) {
        if (ready) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tenco-tts")
    }
}
