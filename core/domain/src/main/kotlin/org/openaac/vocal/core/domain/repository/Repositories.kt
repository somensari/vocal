package org.openaac.vocal.core.domain.repository

import org.openaac.vocal.core.domain.model.Board
import org.openaac.vocal.core.domain.model.BoardThemePreset
import org.openaac.vocal.core.domain.model.Phrase
import kotlinx.coroutines.flow.Flow

interface BoardRepository {
    fun observeDefaultBoard(): Flow<Board?>
    fun observePhrases(boardId: Long): Flow<List<Phrase>>
    suspend fun ensureDefaultBoard(): Board
    suspend fun updateBoard(board: Board)
}

interface PhraseRepository {
    fun observeAllPhrases(): Flow<List<Phrase>>
    suspend fun getPhrase(id: Long): Phrase?
    suspend fun savePhrase(phrase: Phrase): Long
    suspend fun deletePhrase(id: Long)
}

enum class SpeechError {
    TtsUnavailable,
    LanguageUnsupported,
    SpeakFailed,
}

interface SpeechRepository {
    suspend fun speak(text: String, audioPath: String?): SpeechError?
}

/**
 * Maintainer telemetry without attaching AAC communication content.
 *
 * Implementations may no-op when New Relic (or another backend) is disabled
 * for the build. Callers must never pass phrase text, icon paths, or audio paths.
 */
interface MonitoringRepository {
    fun recordHandledException(
        throwable: Throwable,
        attributes: Map<String, Any> = emptyMap(),
    )

    fun recordBreadcrumb(
        name: String,
        attributes: Map<String, Any> = emptyMap(),
    )

    fun recordCustomEvent(
        eventName: String,
        attributes: Map<String, Any> = emptyMap(),
    )

    fun startInteraction(name: String): String?

    fun endInteraction(interactionId: String?)

    fun setSessionAttribute(name: String, value: String)

    fun setSessionAttribute(name: String, value: Double)

    fun setSessionAttribute(name: String, value: Boolean)

    fun recordMetric(name: String, category: String, value: Double = 1.0)

    fun incrementSessionAttribute(name: String)
}

interface UserPreferencesRepository {
    val speechRate: Flow<Float>
    val boardThemePreset: Flow<BoardThemePreset>
    suspend fun setSpeechRate(rate: Float)
    suspend fun setBoardThemePreset(preset: BoardThemePreset)
}
