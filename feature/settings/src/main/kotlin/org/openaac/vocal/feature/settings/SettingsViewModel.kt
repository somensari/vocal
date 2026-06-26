package org.openaac.vocal.feature.settings

import org.openaac.vocal.core.domain.model.Phrase
import org.openaac.vocal.core.domain.usecase.DeletePhraseUseCase
import org.openaac.vocal.core.domain.usecase.EnsureDefaultBoardUseCase
import org.openaac.vocal.core.domain.usecase.ObserveAllPhrasesUseCase
import org.openaac.vocal.core.domain.usecase.SavePhraseUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PhraseEditorState(
    val id: Long = 0,
    val boardId: Long = 0,
    val label: String = "",
    val spokenText: String = "",
    val row: Int = 0,
    val column: Int = 0,
)

data class SettingsUiState(
    val phrases: List<Phrase> = emptyList(),
    val editor: PhraseEditorState? = null,
    val message: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeAllPhrasesUseCase: ObserveAllPhrasesUseCase,
    private val ensureDefaultBoardUseCase: EnsureDefaultBoardUseCase,
    private val savePhraseUseCase: SavePhraseUseCase,
    private val deletePhraseUseCase: DeletePhraseUseCase,
) : ViewModel() {

    private val _editor = MutableStateFlow<PhraseEditorState?>(null)
    private val _message = MutableStateFlow<String?>(null)

    private var defaultBoardId: Long = 0

    val uiState: StateFlow<SettingsUiState> = kotlinx.coroutines.flow.combine(
        observeAllPhrasesUseCase(),
        _editor,
        _message,
    ) { phrases, editor, message ->
        SettingsUiState(
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

    fun dismissEditor() {
        _editor.value = null
    }

    fun saveEditor() {
        val editor = _editor.value ?: return
        if (editor.label.isBlank() || editor.spokenText.isBlank()) {
            _message.value = "Label and spoken text are required."
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
            _message.value = "Phrase saved."
        }
    }

    fun deletePhrase(phrase: Phrase) {
        viewModelScope.launch {
            deletePhraseUseCase(phrase.id)
            _message.value = "Phrase deleted."
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
