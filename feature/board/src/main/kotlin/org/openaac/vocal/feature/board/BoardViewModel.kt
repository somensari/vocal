package org.openaac.vocal.feature.board

import org.openaac.vocal.core.domain.model.Board
import org.openaac.vocal.core.domain.model.Phrase
import org.openaac.vocal.core.domain.model.PhraseGroup
import org.openaac.vocal.core.domain.model.isFolderCell
import org.openaac.vocal.core.domain.model.orderedPhrasesForBoard
import org.openaac.vocal.core.domain.monitoring.MonitoringEvents
import org.openaac.vocal.core.domain.repository.MonitoringRepository
import org.openaac.vocal.core.domain.usecase.EnsureDefaultBoardUseCase
import org.openaac.vocal.core.domain.usecase.ObserveAllBoardsUseCase
import org.openaac.vocal.core.domain.usecase.ObserveBoardPhrasesUseCase
import org.openaac.vocal.core.domain.usecase.ObserveBoardUseCase
import org.openaac.vocal.core.domain.usecase.ObserveLastSelectedBoardIdUseCase
import org.openaac.vocal.core.domain.usecase.ObservePhraseGroupsUseCase
import org.openaac.vocal.core.domain.usecase.ResolveLocalSymbolFilePathUseCase
import org.openaac.vocal.core.domain.usecase.SetLastSelectedBoardIdUseCase
import org.openaac.vocal.core.domain.usecase.SpeakPhraseUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BoardUiState(
    val board: Board? = null,
    val homeBoardId: Long? = null,
    /** All boards for the switcher bar (Home first). Empty while loading. */
    val boards: List<Board> = emptyList(),
    val phrases: List<Phrase> = emptyList(),
    val groups: List<PhraseGroup> = emptyList(),
    val isLoading: Boolean = true,
) {
    /** Fixed board bar is shown only when more than one board exists. */
    val showBoardSwitcher: Boolean
        get() = boards.size > 1
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BoardViewModel @Inject constructor(
    private val observeBoardUseCase: ObserveBoardUseCase,
    observeBoardPhrasesUseCase: ObserveBoardPhrasesUseCase,
    observePhraseGroupsUseCase: ObservePhraseGroupsUseCase,
    observeAllBoardsUseCase: ObserveAllBoardsUseCase,
    private val ensureDefaultBoardUseCase: EnsureDefaultBoardUseCase,
    private val observeLastSelectedBoardIdUseCase: ObserveLastSelectedBoardIdUseCase,
    private val setLastSelectedBoardIdUseCase: SetLastSelectedBoardIdUseCase,
    private val speakPhraseUseCase: SpeakPhraseUseCase,
    private val resolveLocalSymbolFilePathUseCase: ResolveLocalSymbolFilePathUseCase,
    private val monitoringRepository: MonitoringRepository,
) : ViewModel() {

    private val homeBoardId = MutableStateFlow<Long?>(null)
    private val activeBoardId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<BoardUiState> = combine(
        activeBoardId.flatMapLatest { boardId ->
            if (boardId == null) {
                flowOf(null)
            } else {
                observeBoardUseCase(boardId)
            }
        },
        activeBoardId.flatMapLatest { boardId ->
            if (boardId == null) {
                flowOf(emptyList())
            } else {
                observeBoardPhrasesUseCase(boardId)
            }
        },
        activeBoardId.flatMapLatest { boardId ->
            if (boardId == null) {
                flowOf(emptyList())
            } else {
                observePhraseGroupsUseCase(boardId)
            }
        },
        homeBoardId,
        observeAllBoardsUseCase(),
    ) { board, phrases, groups, homeId, boards ->
        BoardUiState(
            board = board,
            homeBoardId = homeId,
            boards = boards,
            phrases = orderedPhrasesForBoard(phrases),
            groups = groups,
            isLoading = board == null,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BoardUiState(),
    )

    init {
        viewModelScope.launch {
            val interactionId =
                monitoringRepository.startInteraction(MonitoringEvents.Interaction.EnsureDefaultBoard)
            try {
                val home = ensureDefaultBoardUseCase()
                homeBoardId.value = home.id

                val savedId = observeLastSelectedBoardIdUseCase().first()
                val restored = savedId?.let { id ->
                    observeBoardUseCase(id).first()
                }
                val initialId = restored?.id ?: home.id
                activeBoardId.value = initialId
                if (savedId != initialId) {
                    setLastSelectedBoardIdUseCase(initialId)
                }

                monitoringRepository.recordCustomEvent(
                    eventName = MonitoringEvents.Name.DefaultBoardEnsured,
                    attributes = mapOf(MonitoringEvents.Attr.Success to true),
                )
            } catch (t: Throwable) {
                monitoringRepository.recordHandledException(
                    throwable = t,
                    attributes = mapOf(
                        MonitoringEvents.Attr.Component to "BoardViewModel.ensureDefaultBoard",
                    ),
                )
            } finally {
                monitoringRepository.endInteraction(interactionId)
            }
        }

        viewModelScope.launch {
            uiState
                .map { state ->
                    if (state.isLoading) null else state.phrases.size
                }
                .distinctUntilChanged()
                .collect { phraseCount ->
                    if (phraseCount == null) return@collect
                    monitoringRepository.recordCustomEvent(
                        eventName = MonitoringEvents.Name.BoardReady,
                        attributes = mapOf(
                            MonitoringEvents.Attr.PhraseCount to phraseCount.toDouble(),
                        ),
                    )
                    monitoringRepository.setSessionAttribute(
                        "vocal.phrase_count",
                        phraseCount.toDouble(),
                    )
                    monitoringRepository.recordBreadcrumb(
                        name = "board_ready",
                        attributes = mapOf(
                            MonitoringEvents.Attr.PhraseCount to phraseCount.toDouble(),
                        ),
                    )
                }
        }
    }

    fun onCellSelected(phrase: Phrase) {
        val targetId = phrase.targetBoardId
        if (phrase.isFolderCell && targetId != null) {
            openBoard(targetId)
            return
        }
        speakPhrase(phrase)
    }

    fun onBoardSelected(boardId: Long) {
        openBoard(boardId)
    }

    private fun openBoard(boardId: Long) {
        if (activeBoardId.value == boardId) return
        activeBoardId.value = boardId
        viewModelScope.launch {
            setLastSelectedBoardIdUseCase(boardId)
        }
    }

    private fun speakPhrase(phrase: Phrase) {
        viewModelScope.launch {
            val interactionId =
                monitoringRepository.startInteraction(MonitoringEvents.Interaction.SpeakPhrase)
            try {
                val error = speakPhraseUseCase(phrase)
                monitoringRepository.incrementSessionAttribute("vocal.speak_count")
                monitoringRepository.recordCustomEvent(
                    eventName = MonitoringEvents.Name.SpeakPhrase,
                    attributes = mapOf(
                        MonitoringEvents.Attr.HasRecordedAudio to !phrase.audioPath.isNullOrBlank(),
                        MonitoringEvents.Attr.Success to (error == null),
                    ),
                )
                monitoringRepository.recordMetric(
                    name = "SpeakPhrase",
                    category = "Board",
                )
            } catch (t: Throwable) {
                monitoringRepository.recordHandledException(
                    throwable = t,
                    attributes = mapOf(
                        MonitoringEvents.Attr.Component to "BoardViewModel.onPhraseSelected",
                    ),
                )
            } finally {
                monitoringRepository.endInteraction(interactionId)
            }
        }
    }

    /** Resolves a cached SymboTalk icon to a local filesystem path (offline board use). */
    fun resolveCachedIconFilePath(iconPath: String?): String? =
        resolveLocalSymbolFilePathUseCase(iconPath)
}
