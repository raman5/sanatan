package com.bhakti.app.core.media

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * Speaks a japa aloud once per counter tap - always in Hindi. Used for both
 * Naam Japa (the deity's name) and mantra japa (the full mantra line).
 * Plays [recordedUrl] (backend-hosted, optional, set in Firestore) when
 * there is one, otherwise speaks [devanagariText] with Google's
 * text-to-speech in Hindi (hi-IN). Google's engine is requested explicitly
 * because some phones default to an OEM engine with no Hindi voice.
 *
 * No English fallback by design: if the Hindi voice isn't on the phone yet,
 * the first tap opens the system's voice-download screen instead.
 *
 * Both paths use the media stream, so they follow the phone's media volume.
 * Each tap restarts the chant rather than queueing, so fast tapping never
 * leaves a backlog playing after the user stops.
 */
class JapaChanter(
    context: Context,
    private val devanagariText: String,
    recordedUrl: String?
) {
    private val appContext = context.applicationContext
    private var ttsReady = false
    private var hindiReady = false

    private val tts: TextToSpeech? = if (recordedUrl == null) {
        TextToSpeech(appContext, { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsReady = true
                hindiReady = setHindi()
            }
        }, GOOGLE_TTS_ENGINE)
    } else null

    private var player: MediaPlayer? = null
    private var playerReady = false

    init {
        if (recordedUrl != null) {
            player = MediaPlayer().apply {
                setOnPreparedListener { playerReady = true }
                setDataSource(recordedUrl)
                prepareAsync()
            }
        }
    }

    private fun setHindi(): Boolean {
        val engine = tts ?: return false
        val result = engine.setLanguage(Locale("hi", "IN"))
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) return false
        // Prefer an on-device Hindi voice so chanting works offline and without lag.
        engine.voices
            ?.filter { it.locale.language == "hi" && !it.isNetworkConnectionRequired }
            ?.minByOrNull { it.latency }
            ?.let { engine.voice = it }
        return true
    }

    fun chant() {
        val p = player
        if (p != null) {
            if (!playerReady) return
            if (p.isPlaying) p.pause()
            p.seekTo(0)
            p.start()
            return
        }
        if (!ttsReady) return
        if (!hindiReady) {
            // Re-check first: the user may have just come back from downloading it.
            hindiReady = setHindi()
            if (!hindiReady) {
                promptHindiVoiceInstall()
                return
            }
        }
        tts?.speak(devanagariText, TextToSpeech.QUEUE_FLUSH, null, "japa")
    }

    private fun promptHindiVoiceInstall() {
        if (installPrompted) return
        installPrompted = true
        Toast.makeText(appContext, "Install the Hindi voice to hear the japa", Toast.LENGTH_LONG).show()
        runCatching {
            appContext.startActivity(
                Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
                    .setPackage(GOOGLE_TTS_ENGINE)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        player?.release()
        player = null
    }

    private companion object {
        const val GOOGLE_TTS_ENGINE = "com.google.android.tts"

        /** Once per app session, so a missing voice doesn't hijack every tap. */
        var installPrompted = false
    }
}

/** Remembers a [JapaChanter] for this text/clip, released automatically when the screen leaves. */
@Composable
fun rememberJapaChanter(devanagariText: String, recordedUrl: String?): JapaChanter {
    val context = LocalContext.current
    val chanter = remember(devanagariText, recordedUrl) {
        JapaChanter(context, devanagariText, recordedUrl)
    }
    DisposableEffect(chanter) { onDispose { chanter.release() } }
    return chanter
}
