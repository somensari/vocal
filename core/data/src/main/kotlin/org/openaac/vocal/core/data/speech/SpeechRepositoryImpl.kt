package org.openaac.vocal.core.data.speech

import android.content.Context
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
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
) : SpeechRepository {

    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    suspend fun initialize() {
        if (textToSpeech != null) return
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                textToSpeech = TextToSpeech(context) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        textToSpeech?.language = Locale.getDefault()
                        isTtsReady = true
                    }
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                }
            }
        }
    }

    override suspend fun speak(text: String, audioPath: String?) {
        if (!audioPath.isNullOrBlank()) {
            val played = playRecordedAudio(audioPath)
            if (played) return
        }
        speakWithTts(text)
    }

    private suspend fun speakWithTts(text: String) {
        initialize()
        val rate = userPreferencesRepository.speechRate.first()
        withContext(Dispatchers.Main) {
            textToSpeech?.setSpeechRate(rate)
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "vocal-phrase")
        }
    }

    private suspend fun playRecordedAudio(audioPath: String): Boolean =
        withContext(Dispatchers.IO) {
            val file = File(audioPath)
            if (!file.exists()) return@withContext false

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
                        player.setOnErrorListener { mp, _, _ ->
                            mp.release()
                            if (continuation.isActive) continuation.resume(false)
                            true
                        }
                        player.prepare()
                        player.start()
                    } catch (_: Exception) {
                        player.release()
                        if (continuation.isActive) continuation.resume(false)
                    }
                }
            }
        }
}
