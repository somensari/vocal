package org.openaac.vocal.core.domain.usecase

import org.openaac.vocal.core.domain.model.Board
import org.openaac.vocal.core.domain.model.BoardThemePreset
import org.openaac.vocal.core.domain.model.BundledPhraseIcons
import org.openaac.vocal.core.domain.model.Phrase
import org.openaac.vocal.core.domain.model.SavePhraseWithSymbolResult
import org.openaac.vocal.core.domain.model.SymbolCacheMaxSizeMb
import org.openaac.vocal.core.domain.model.SymbolCacheUsage
import org.openaac.vocal.core.domain.model.SymbolMatchResult
import org.openaac.vocal.core.domain.repository.BoardRepository
import org.openaac.vocal.core.domain.repository.PhraseRepository
import org.openaac.vocal.core.domain.repository.SpeechRepository
import org.openaac.vocal.core.domain.repository.SymbolCacheRepository
import org.openaac.vocal.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ObserveBoardUseCase @Inject constructor(
    private val boardRepository: BoardRepository,
) {
    operator fun invoke(): Flow<Board?> = boardRepository.observeDefaultBoard()
}

class ObserveBoardPhrasesUseCase @Inject constructor(
    private val boardRepository: BoardRepository,
) {
    operator fun invoke(boardId: Long): Flow<List<Phrase>> =
        boardRepository.observePhrases(boardId)
}

class EnsureDefaultBoardUseCase @Inject constructor(
    private val boardRepository: BoardRepository,
) {
    suspend operator fun invoke(): Board = boardRepository.ensureDefaultBoard()
}

class UpdateBoardUseCase @Inject constructor(
    private val boardRepository: BoardRepository,
) {
    suspend operator fun invoke(board: Board) = boardRepository.updateBoard(board)
}

class SpeakPhraseUseCase @Inject constructor(
    private val speechRepository: SpeechRepository,
) {
    suspend operator fun invoke(phrase: Phrase) {
        speechRepository.speak(phrase.spokenText, phrase.audioPath)
    }
}

class ObserveAllPhrasesUseCase @Inject constructor(
    private val phraseRepository: PhraseRepository,
) {
    operator fun invoke(): Flow<List<Phrase>> = phraseRepository.observeAllPhrases()
}

class SavePhraseUseCase @Inject constructor(
    private val phraseRepository: PhraseRepository,
) {
    suspend operator fun invoke(phrase: Phrase): Long = phraseRepository.savePhrase(phrase)
}

/**
 * Saves a phrase and auto-matches a SymboTalk symbol from the label when needed.
 *
 * Network is used only here (caregiver configuration). Failures still save the
 * phrase with a placeholder icon. Cache-limit blocks the save entirely.
 */
class SavePhraseWithSymbolUseCase @Inject constructor(
    private val phraseRepository: PhraseRepository,
    private val symbolCacheRepository: SymbolCacheRepository,
) {
    suspend operator fun invoke(phrase: Phrase): SavePhraseWithSymbolResult {
        val existing = phrase.id.takeIf { it > 0L }?.let { phraseRepository.getPhrase(it) }
        val labelUnchanged = existing != null &&
            existing.label.equals(phrase.label, ignoreCase = false)
        val existingIconPath = existing?.iconPath

        val (iconPath, symbolWarning) = when {
            labelUnchanged && !existingIconPath.isNullOrBlank() -> {
                existingIconPath to false
            }
            else -> when (val match = symbolCacheRepository.matchAndCacheSymbol(phrase.label)) {
                is SymbolMatchResult.Matched -> match.iconPath to false
                SymbolMatchResult.Unavailable -> BundledPhraseIcons.PLACEHOLDER to true
                SymbolMatchResult.CacheLimitReached -> {
                    return SavePhraseWithSymbolResult.CacheLimitReached
                }
            }
        }

        val phraseId = phraseRepository.savePhrase(phrase.copy(iconPath = iconPath))
        return SavePhraseWithSymbolResult.Saved(
            phraseId = phraseId,
            symbolWarning = symbolWarning,
        )
    }
}

class DeletePhraseUseCase @Inject constructor(
    private val phraseRepository: PhraseRepository,
) {
    suspend operator fun invoke(id: Long) = phraseRepository.deletePhrase(id)
}

class ObserveBoardThemePresetUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    operator fun invoke(): Flow<BoardThemePreset> = userPreferencesRepository.boardThemePreset
}

class SetBoardThemePresetUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    suspend operator fun invoke(preset: BoardThemePreset) {
        userPreferencesRepository.setBoardThemePreset(preset)
    }
}

class ObserveSymbolCacheMaxSizeUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    operator fun invoke(): Flow<SymbolCacheMaxSizeMb> =
        userPreferencesRepository.symbolCacheMaxSizeMb
}

class SetSymbolCacheMaxSizeUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    suspend operator fun invoke(maxSize: SymbolCacheMaxSizeMb) {
        userPreferencesRepository.setSymbolCacheMaxSizeMb(maxSize)
    }
}

class GetSymbolCacheUsageUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val symbolCacheRepository: SymbolCacheRepository,
) {
    suspend operator fun invoke(): SymbolCacheUsage {
        val maxSize = userPreferencesRepository.symbolCacheMaxSizeMb.first()
        return SymbolCacheUsage(
            usedBytes = symbolCacheRepository.getCacheUsageBytes(),
            maxBytes = maxSize.bytes,
        )
    }
}

class CleanSymbolCacheUseCase @Inject constructor(
    private val phraseRepository: PhraseRepository,
    private val symbolCacheRepository: SymbolCacheRepository,
) {
    suspend operator fun invoke(): Int {
        val referenced = phraseRepository.getAllIconPaths()
        return symbolCacheRepository.deleteUnreferencedCache(referenced)
    }
}

class ResolveLocalSymbolFilePathUseCase @Inject constructor(
    private val symbolCacheRepository: SymbolCacheRepository,
) {
    operator fun invoke(iconPath: String?): String? =
        symbolCacheRepository.resolveLocalFilePath(iconPath)
}
