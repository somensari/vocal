package org.openaac.vocal.feature.board

import org.openaac.vocal.core.domain.model.Board
import org.openaac.vocal.core.domain.model.Phrase
import org.openaac.vocal.core.domain.usecase.EnsureDefaultBoardUseCase
import org.openaac.vocal.core.domain.usecase.ObserveBoardPhrasesUseCase
import org.openaac.vocal.core.domain.usecase.ObserveBoardUseCase
import org.openaac.vocal.core.domain.usecase.SpeakPhraseUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BoardUiState(
    val board: Board? = null,
    val phrases: List<Phrase> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class BoardViewModel @Inject constructor(
    observeBoardUseCase: ObserveBoardUseCase,
    observeBoardPhrasesUseCase: ObserveBoardPhrasesUseCase,
    private val ensureDefaultBoardUseCase: EnsureDefaultBoardUseCase,
    private val speakPhraseUseCase: SpeakPhraseUseCase,
) : ViewModel() {

    private val activeBoardId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<BoardUiState> = combine(
        observeBoardUseCase(),
        activeBoardId.flatMapLatest { boardId ->
            if (boardId == null) {
                flowOf(emptyList())
            } else {
                observeBoardPhrasesUseCase(boardId)
            }
        },
    ) { board, phrases ->
        BoardUiState(
            board = board,
            phrases = phrases,
            isLoading = board == null,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BoardUiState(),
    )

    init {
        viewModelScope.launch {
            val board = ensureDefaultBoardUseCase()
            activeBoardId.value = board.id
        }
    }

    fun onPhraseSelected(phrase: Phrase) {
        viewModelScope.launch {
            speakPhraseUseCase(phrase)
        }
    }
}
