package org.openaac.vocal.core.domain.repository

import org.openaac.vocal.core.domain.model.Board
import org.openaac.vocal.core.domain.model.BoardThemePreset
import org.openaac.vocal.core.domain.model.Phrase
import org.openaac.vocal.core.domain.model.PhraseGroup
import org.openaac.vocal.core.domain.model.SymbolCacheMaxSizeMb
import org.openaac.vocal.core.domain.model.SymbolCacheUsage
import org.openaac.vocal.core.domain.model.SymbolMatchResult
import kotlinx.coroutines.flow.Flow

interface BoardRepository {
    /** Observes the Home board (folder navigation root). */
    fun observeDefaultBoard(): Flow<Board?>

    fun observeBoard(boardId: Long): Flow<Board?>

    fun observeAllBoards(): Flow<List<Board>>

    fun observePhrases(boardId: Long): Flow<List<Phrase>>

    /**
     * Ensures the seeded Home + topic boards exist (up to [org.openaac.vocal.core.domain.model.MAX_BOARDS])
     * and returns the Home board.
     */
    suspend fun ensureDefaultBoard(): Board

    suspend fun getBoard(boardId: Long): Board?

    suspend fun updateBoard(board: Board)

    /**
     * Full replace: deletes all phrases (and their custom local icon/audio files)
     * and groups on every board, then restores the seeded Home + folder board set.
     */
    suspend fun resetToStarterBoard()
}

interface PhraseRepository {
    fun observeAllPhrases(): Flow<List<Phrase>>
    suspend fun getPhrase(id: Long): Phrase?
    suspend fun savePhrase(phrase: Phrase): Long
    suspend fun deletePhrase(id: Long)
    suspend fun getAllIconPaths(): List<String>

    /** Persists list order; [orderedPhraseIds] becomes sortOrder 0..n-1. */
    suspend fun reorderPhrases(orderedPhraseIds: List<Long>)
}

interface PhraseGroupRepository {
    fun observeGroups(boardId: Long): Flow<List<PhraseGroup>>
    suspend fun getGroups(boardId: Long): List<PhraseGroup>
    suspend fun getGroup(id: Long): PhraseGroup?
    suspend fun countGroups(boardId: Long): Int
    suspend fun saveGroup(group: PhraseGroup): Long
    /** Deletes the group and clears [Phrase.groupId] on member phrases (phrases remain). */
    suspend fun deleteGroup(id: Long)
    suspend fun assignPhraseToGroup(phraseId: Long, groupId: Long?)
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

    /** Names the in-flight default interaction (used for Compose screen changes). */
    fun setInteractionName(name: String)

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
    /** Last board opened on the main AAC screen; null means fall back to Home. */
    val lastSelectedBoardId: Flow<Long?>
    suspend fun setSpeechRate(rate: Float)
    suspend fun setBoardThemePreset(preset: BoardThemePreset)
    suspend fun setSymbolCacheMaxSizeMb(maxSize: SymbolCacheMaxSizeMb)
    suspend fun setLastSelectedBoardId(boardId: Long?)
}
