package org.openaac.vocal.core.domain.usecase

import org.openaac.vocal.core.domain.model.Board
import org.openaac.vocal.core.domain.model.BoardThemePreset
import org.openaac.vocal.core.domain.model.Phrase
import org.openaac.vocal.core.domain.monitoring.MonitoringEvents
import org.openaac.vocal.core.domain.repository.BoardRepository
import org.openaac.vocal.core.domain.repository.MonitoringRepository
import org.openaac.vocal.core.domain.repository.PhraseRepository
import org.openaac.vocal.core.domain.repository.SpeechError
import org.openaac.vocal.core.domain.repository.SpeechRepository
import org.openaac.vocal.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
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
