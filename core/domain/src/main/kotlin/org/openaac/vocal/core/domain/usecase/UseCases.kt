package org.openaac.vocal.core.domain.usecase

import org.openaac.vocal.core.domain.model.Board
import org.openaac.vocal.core.domain.model.BoardThemePreset
import org.openaac.vocal.core.domain.model.BundledPhraseIcons
import org.openaac.vocal.core.domain.model.MAX_PHRASE_GROUPS
import org.openaac.vocal.core.domain.model.Phrase
import org.openaac.vocal.core.domain.model.PhraseGroup
import org.openaac.vocal.core.domain.model.SavePhraseWithSymbolResult
import org.openaac.vocal.core.domain.model.SymbolCacheMaxSizeMb
import org.openaac.vocal.core.domain.model.SymbolCacheUsage
import org.openaac.vocal.core.domain.model.SymbolMatchResult
import org.openaac.vocal.core.domain.monitoring.MonitoringEvents
import org.openaac.vocal.core.domain.repository.BoardRepository
import org.openaac.vocal.core.domain.repository.MonitoringRepository
import org.openaac.vocal.core.domain.repository.PhraseGroupRepository
import org.openaac.vocal.core.domain.repository.PhraseRepository
import org.openaac.vocal.core.domain.repository.SpeechError
import org.openaac.vocal.core.domain.repository.SpeechRepository
import org.openaac.vocal.core.domain.repository.SymbolCacheRepository
import org.openaac.vocal.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

sealed class CreatePhraseGroupResult {
    data class Created(val groupId: Long) : CreatePhraseGroupResult()
    data object LimitReached : CreatePhraseGroupResult()
    data object BlankName : CreatePhraseGroupResult()
}

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
    private val monitoringRepository: MonitoringRepository,
) {
    suspend operator fun invoke(phrase: Phrase): SpeechError? {
        val error = speechRepository.speak(phrase.spokenText, phrase.audioPath)
        if (error != null) {
            monitoringRepository.recordCustomEvent(
                eventName = MonitoringEvents.Name.SpeechError,
                attributes = mapOf(
                    MonitoringEvents.Attr.Error to error.name,
                    MonitoringEvents.Attr.HasRecordedAudio to !phrase.audioPath.isNullOrBlank(),
                    MonitoringEvents.Attr.Component to "SpeakPhraseUseCase",
                ),
            )
            monitoringRepository.recordBreadcrumb(
                name = "speech_error",
                attributes = mapOf(MonitoringEvents.Attr.Error to error.name),
            )
            monitoringRepository.recordMetric(
                name = "SpeechError_${error.name}",
                category = "Speech",
            )
        }
        return error
    }
}

class ObserveAllPhrasesUseCase @Inject constructor(
    private val phraseRepository: PhraseRepository,
) {
    operator fun invoke(): Flow<List<Phrase>> = phraseRepository.observeAllPhrases()
}

class SavePhraseUseCase @Inject constructor(
    private val phraseRepository: PhraseRepository,
    private val monitoringRepository: MonitoringRepository,
) {
    suspend operator fun invoke(phrase: Phrase): Long {
        val interactionId =
            monitoringRepository.startInteraction(MonitoringEvents.Interaction.SavePhrase)
        val isNew = phrase.id == 0L
        return try {
            phraseRepository.savePhrase(phrase).also {
                monitoringRepository.recordCustomEvent(
                    eventName = MonitoringEvents.Name.PhraseSaved,
                    attributes = mapOf(MonitoringEvents.Attr.IsNew to isNew),
                )
                monitoringRepository.recordMetric(name = "PhraseSaved", category = "Settings")
            }
        } catch (t: Throwable) {
            monitoringRepository.recordHandledException(
                throwable = t,
                attributes = mapOf(
                    MonitoringEvents.Attr.Component to "SavePhraseUseCase",
                ),
            )
            throw t
        } finally {
            monitoringRepository.endInteraction(interactionId)
        }
    }
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
    private val monitoringRepository: MonitoringRepository,
) {
    suspend operator fun invoke(id: Long) {
        val interactionId =
            monitoringRepository.startInteraction(MonitoringEvents.Interaction.DeletePhrase)
        try {
            phraseRepository.deletePhrase(id)
            monitoringRepository.recordCustomEvent(
                eventName = MonitoringEvents.Name.PhraseDeleted,
            )
            monitoringRepository.recordMetric(name = "PhraseDeleted", category = "Settings")
        } catch (t: Throwable) {
            monitoringRepository.recordHandledException(
                throwable = t,
                attributes = mapOf(
                    MonitoringEvents.Attr.Component to "DeletePhraseUseCase",
                ),
            )
            throw t
        } finally {
            monitoringRepository.endInteraction(interactionId)
        }
    }
}

class ObserveBoardThemePresetUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    operator fun invoke(): Flow<BoardThemePreset> = userPreferencesRepository.boardThemePreset
}

class SetBoardThemePresetUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val monitoringRepository: MonitoringRepository,
) {
    suspend operator fun invoke(preset: BoardThemePreset) {
        userPreferencesRepository.setBoardThemePreset(preset)
        monitoringRepository.recordCustomEvent(
            eventName = MonitoringEvents.Name.ThemeChanged,
            attributes = mapOf(MonitoringEvents.Attr.Theme to preset.name),
        )
        monitoringRepository.setSessionAttribute("vocal.theme", preset.name)
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

class ObservePhraseGroupsUseCase @Inject constructor(
    private val phraseGroupRepository: PhraseGroupRepository,
) {
    operator fun invoke(boardId: Long): Flow<List<PhraseGroup>> =
        phraseGroupRepository.observeGroups(boardId)
}

class CreatePhraseGroupUseCase @Inject constructor(
    private val phraseGroupRepository: PhraseGroupRepository,
) {
    suspend operator fun invoke(boardId: Long, name: String): CreatePhraseGroupResult {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return CreatePhraseGroupResult.BlankName
        if (phraseGroupRepository.countGroups(boardId) >= MAX_PHRASE_GROUPS) {
            return CreatePhraseGroupResult.LimitReached
        }
        val existing = phraseGroupRepository.getGroups(boardId)
        val usedColors = existing.map { it.colorIndex }.toSet()
        val colorIndex = (0 until MAX_PHRASE_GROUPS).first { it !in usedColors }
        val sortOrder = (existing.maxOfOrNull { it.sortOrder } ?: -1) + 1
        val id = phraseGroupRepository.saveGroup(
            PhraseGroup(
                id = 0,
                boardId = boardId,
                name = trimmed,
                colorIndex = colorIndex,
                sortOrder = sortOrder,
            ),
        )
        return CreatePhraseGroupResult.Created(id)
    }
}

class RenamePhraseGroupUseCase @Inject constructor(
    private val phraseGroupRepository: PhraseGroupRepository,
) {
    suspend operator fun invoke(groupId: Long, name: String): Boolean {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return false
        val existing = phraseGroupRepository.getGroup(groupId) ?: return false
        phraseGroupRepository.saveGroup(existing.copy(name = trimmed))
        return true
    }
}

class DeletePhraseGroupUseCase @Inject constructor(
    private val phraseGroupRepository: PhraseGroupRepository,
) {
    /** Deletes the group; member phrases remain on the board as ungrouped. */
    suspend operator fun invoke(groupId: Long) {
        phraseGroupRepository.deleteGroup(groupId)
    }
}

class AssignPhraseGroupUseCase @Inject constructor(
    private val phraseGroupRepository: PhraseGroupRepository,
) {
    suspend operator fun invoke(phraseId: Long, groupId: Long?) {
        phraseGroupRepository.assignPhraseToGroup(phraseId, groupId)
    }
}
