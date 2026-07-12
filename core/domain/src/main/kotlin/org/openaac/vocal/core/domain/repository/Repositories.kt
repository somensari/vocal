package org.openaac.vocal.core.domain.repository

import org.openaac.vocal.core.domain.model.Board
import org.openaac.vocal.core.domain.model.BoardThemePreset
import org.openaac.vocal.core.domain.model.Phrase
import org.openaac.vocal.core.domain.model.SymbolCacheMaxSizeMb
import org.openaac.vocal.core.domain.model.SymbolCacheUsage
import org.openaac.vocal.core.domain.model.SymbolMatchResult
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
    suspend fun getAllIconPaths(): List<String>
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

/**
 * SymboTalk search + on-disk image cache used only during caregiver configuration.
 *
 * Board runtime must never call this repository; cells render from local paths only.
 */
interface SymbolCacheRepository {
    suspend fun matchAndCacheSymbol(label: String): SymbolMatchResult
    suspend fun getCacheUsageBytes(): Long
    suspend fun deleteUnreferencedCache(referencedIconPaths: Collection<String>): Int
    fun resolveLocalFilePath(iconPath: String?): String?
}

interface UserPreferencesRepository {
    val speechRate: Flow<Float>
    val boardThemePreset: Flow<BoardThemePreset>
    val symbolCacheMaxSizeMb: Flow<SymbolCacheMaxSizeMb>
    suspend fun setSpeechRate(rate: Float)
    suspend fun setBoardThemePreset(preset: BoardThemePreset)
    suspend fun setSymbolCacheMaxSizeMb(maxSize: SymbolCacheMaxSizeMb)
}
