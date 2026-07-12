package org.openaac.vocal.core.data.speech

import android.content.Context
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import org.openaac.vocal.core.domain.monitoring.MonitoringEvents
import org.openaac.vocal.core.domain.repository.MonitoringRepository
import org.openaac.vocal.core.domain.repository.SpeechError
import org.openaac.vocal.core.domain.repository.SpeechRepository
import org.openaac.vocal.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class SpeechRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val monitoringRepository: MonitoringRepository,
) : SpeechRepository {

    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private suspend fun initialize(): SpeechError? {
        if (textToSpeech != null) {
            return if (isTtsReady) null else SpeechError.TtsUnavailable
        }
        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                textToSpeech = TextToSpeech(context) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        textToSpeech?.setOnUtteranceProgressListener(
                            object : UtteranceProgressListener() {
                                override fun onStart(utteranceId: String?) {
                                    Log.d(TAG, "TTS started: $utteranceId")
                                }

                                override fun onDone(utteranceId: String?) {
                                    Log.d(TAG, "TTS finished: $utteranceId")
                                }

                                @Deprecated("Deprecated in Java")
                                override fun onError(utteranceId: String?) {
                                    Log.w(TAG, "TTS error (legacy): $utteranceId")
                                }

                                override fun onError(utteranceId: String?, errorCode: Int) {
                                    Log.w(TAG, "TTS error: $utteranceId code=$errorCode")
                                }
                            },
                        )
                        when (val languageResult = textToSpeech?.setLanguage(Locale.getDefault())) {
                            TextToSpeech.LANG_MISSING_DATA,
                            TextToSpeech.LANG_NOT_SUPPORTED,
                            -> {
                                Log.w(TAG, "TTS language unavailable: $languageResult")
                                isTtsReady = false
                                monitoringRepository.recordBreadcrumb(
                                    name = "tts_language_unsupported",
                                )
                                if (continuation.isActive) {
                                    continuation.resume(SpeechError.LanguageUnsupported)
                                }
                                return@TextToSpeech
                            }
                        }
                        isTtsReady = true
                        Log.d(TAG, "TTS initialized")
                        if (continuation.isActive) continuation.resume(null)
                    } else {
                        Log.e(TAG, "TTS init failed with status=$status")
                        isTtsReady = false
                        monitoringRepository.recordBreadcrumb(
                            name = "tts_init_failed",
                            attributes = mapOf("status" to status.toDouble()),
                        )
                        if (continuation.isActive) {
                            continuation.resume(SpeechError.TtsUnavailable)
                        }
                    }
                }
            }
        }
    }

    override suspend fun speak(text: String, audioPath: String?): SpeechError? {
        if (!audioPath.isNullOrBlank()) {
            val played = playRecordedAudio(audioPath)
            if (played) return null
            Log.w(TAG, "Recorded audio failed, falling back to TTS")
            monitoringRepository.recordBreadcrumb(
                name = "recorded_audio_fallback_tts",
            )
        }
        return speakWithTts(text)
    }

    private suspend fun speakWithTts(text: String): SpeechError? {
        val initError = initialize()
        if (initError != null) return initError

        val rate = userPreferencesRepository.speechRate.first()
        return withContext(Dispatchers.Main) {
            val tts = textToSpeech
            if (tts == null || !isTtsReady) {
                Log.w(TAG, "speak() called before TTS was ready")
                return@withContext SpeechError.TtsUnavailable
            }
            tts.setSpeechRate(rate)
            when (val result = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)) {
                TextToSpeech.SUCCESS -> {
                    Log.d(TAG, "speak() queued")
                    null
                }
                else -> {
                    Log.e(TAG, "speak() failed with result=$result")
                    SpeechError.SpeakFailed
                }
            }
        }
    }

    private suspend fun playRecordedAudio(audioPath: String): Boolean =
        withContext(Dispatchers.IO) {
            val file = File(audioPath)
            if (!file.exists()) {
                Log.w(TAG, "Recorded audio missing: $audioPath")
                return@withContext false
            }

            withContext(Dispatchers.Main) {
                suspendCancellableCoroutine { continuation ->
                    val player = MediaPlayer()
                    continuation.invokeOnCancellation { player.release() }
                    try {
                        player.setDataSource(file.absolutePath)
                        player.setOnCompletionListener {
                            player.release()
                            if (continuation.isActive) continuation.resume(true)
                        }
                        player.setOnErrorListener { mp, what, extra ->
                            Log.w(TAG, "MediaPlayer error what=$what extra=$extra")
                            mp.release()
                            if (continuation.isActive) continuation.resume(false)
                            true
                        }
                        player.prepare()
                        player.start()
                    } catch (e: Exception) {
                        Log.w(TAG, "MediaPlayer failed for recorded audio", e)
                        monitoringRepository.recordHandledException(
                            throwable = e,
                            attributes = mapOf(
                                MonitoringEvents.Attr.Component to "SpeechRepositoryImpl.playRecordedAudio",
                            ),
                        )
                        player.release()
                        if (continuation.isActive) continuation.resume(false)
                    }
                }
            }
        }

    companion object {
        private const val TAG = "VocalSpeech"
        private const val UTTERANCE_ID = "vocal-phrase"
    }
}
