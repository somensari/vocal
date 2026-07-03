package org.openaac.vocal.feature.settings

import org.openaac.vocal.core.domain.model.Board
import org.openaac.vocal.core.domain.model.Phrase
import org.openaac.vocal.core.domain.usecase.DeletePhraseUseCase
import org.openaac.vocal.core.domain.usecase.EnsureDefaultBoardUseCase
import org.openaac.vocal.core.domain.usecase.ObserveAllPhrasesUseCase
import org.openaac.vocal.core.domain.usecase.ObserveBoardUseCase
import org.openaac.vocal.core.domain.usecase.SavePhraseUseCase
import org.openaac.vocal.core.domain.usecase.UpdateBoardUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private val BoardColumnOptions = listOf(1, 2, 4, 8)

data class PhraseEditorState(
    val id: Long = 0,
    val boardId: Long = 0,
    val label: String = "",
    val spokenText: String = "",
    val row: Int = 0,
    val column: Int = 0,
)

enum class SettingsMessage {
    MissingRequiredFields,
    PhraseSaved,
    PhraseDeleted,
}

data class SettingsUiState(
    val board: Board? = null,
    val boardColumnOptions: List<Int> = BoardColumnOptions,
    val phrases: List<Phrase> = emptyList(),
    val editor: PhraseEditorState? = null,
    val message: SettingsMessage? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeBoardUseCase: ObserveBoardUseCase,
    observeAllPhrasesUseCase: ObserveAllPhrasesUseCase,
    private val ensureDefaultBoardUseCase: EnsureDefaultBoardUseCase,
    private val updateBoardUseCase: UpdateBoardUseCase,
    private val savePhraseUseCase: SavePhraseUseCase,
    private val deletePhraseUseCase: DeletePhraseUseCase,
) : ViewModel() {

    private val _editor = MutableStateFlow<PhraseEditorState?>(null)
    private val _message = MutableStateFlow<SettingsMessage?>(null)

    private var defaultBoardId: Long = 0

    val uiState: StateFlow<SettingsUiState> = combine(
        observeBoardUseCase(),
        observeAllPhrasesUseCase(),
        _editor,
        _message,
    ) { board, phrases, editor, message ->
        SettingsUiState(
            board = board,
            phrases = phrases,
            editor = editor,
            message = message,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    init {
        viewModelScope.launch {
            defaultBoardId = ensureDefaultBoardUseCase().id
        }
    }

    fun startAddPhrase() {
        _editor.value = PhraseEditorState(boardId = defaultBoardId)
        _message.value = null
    }

    fun startEditPhrase(phrase: Phrase) {
        _editor.value = PhraseEditorState(
            id = phrase.id,
            boardId = phrase.boardId,
            label = phrase.label,
            spokenText = phrase.spokenText,
            row = phrase.row,
            column = phrase.column,
        )
        _message.value = null
    }

    fun updateEditorLabel(value: String) {
        _editor.update { it?.copy(label = value) }
    }

    fun updateEditorSpokenText(value: String) {
        _editor.update { it?.copy(spokenText = value) }
    }

    fun updateEditorRow(value: Int) {
        _editor.update { it?.copy(row = value.coerceAtLeast(0)) }
    }

    fun updateEditorColumn(value: Int) {
        _editor.update { it?.copy(column = value.coerceAtLeast(0)) }
    }

    fun updateBoardColumns(columns: Int) {
        if (columns !in BoardColumnOptions) return

        viewModelScope.launch {
            val board = uiState.value.board ?: ensureDefaultBoardUseCase()
            defaultBoardId = board.id
            if (board.columns != columns) {
                updateBoardUseCase(board.copy(columns = columns))
            }
        }
    }

    fun dismissEditor() {
        _editor.value = null
    }

    fun saveEditor() {
        val editor = _editor.value ?: return
        if (editor.label.isBlank() || editor.spokenText.isBlank()) {
            _message.value = SettingsMessage.MissingRequiredFields
            return
        }

        viewModelScope.launch {
            savePhraseUseCase(
                Phrase(
                    id = editor.id,
                    boardId = editor.boardId.takeIf { it > 0 } ?: defaultBoardId,
                    label = editor.label.trim(),
                    spokenText = editor.spokenText.trim(),
                    row = editor.row,
                    column = editor.column,
                ),
            )
            _editor.value = null
            _message.value = SettingsMessage.PhraseSaved
        }
    }

    fun deletePhrase(phrase: Phrase) {
        viewModelScope.launch {
            deletePhraseUseCase(phrase.id)
            _message.value = SettingsMessage.PhraseDeleted
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
