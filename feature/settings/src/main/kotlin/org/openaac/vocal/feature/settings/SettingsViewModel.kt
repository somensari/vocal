package org.openaac.vocal.feature.settings

import org.openaac.vocal.core.domain.model.BoardThemePreset
import org.openaac.vocal.core.domain.model.MAX_BOARD_PHRASES
import org.openaac.vocal.core.domain.model.Phrase
import org.openaac.vocal.core.domain.usecase.DeletePhraseUseCase
import org.openaac.vocal.core.domain.usecase.EnsureDefaultBoardUseCase
import org.openaac.vocal.core.domain.usecase.ObserveAllPhrasesUseCase
import org.openaac.vocal.core.domain.usecase.ObserveBoardThemePresetUseCase
import org.openaac.vocal.core.domain.usecase.ObserveBoardUseCase
import org.openaac.vocal.core.domain.usecase.SavePhraseUseCase
import org.openaac.vocal.core.domain.usecase.SetBoardThemePresetUseCase
import org.openaac.vocal.core.domain.repository.SpeechRepository
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
    PhraseLimitReached,
    SpeechTestOk,
    SpeechTestFailed,
}

data class SettingsUiState(
    val phrases: List<Phrase> = emptyList(),
    val selectedBoardThemePreset: BoardThemePreset = BoardThemePreset.Default,
    val editor: PhraseEditorState? = null,
    val message: SettingsMessage? = null,
    val canAddPhrase: Boolean = true,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeAllPhrasesUseCase: ObserveAllPhrasesUseCase,
    observeBoardThemePresetUseCase: ObserveBoardThemePresetUseCase,
    private val ensureDefaultBoardUseCase: EnsureDefaultBoardUseCase,
    private val savePhraseUseCase: SavePhraseUseCase,
    private val deletePhraseUseCase: DeletePhraseUseCase,
    private val setBoardThemePresetUseCase: SetBoardThemePresetUseCase,
    private val speechRepository: SpeechRepository,
) : ViewModel() {

    private val _editor = MutableStateFlow<PhraseEditorState?>(null)
    private val _message = MutableStateFlow<SettingsMessage?>(null)

    private var defaultBoardId: Long = 0

    val uiState: StateFlow<SettingsUiState> = combine(
        observeAllPhrasesUseCase(),
        observeBoardThemePresetUseCase(),
        _editor,
        _message,
    ) { phrases, boardThemePreset, editor, message ->
        SettingsUiState(
            phrases = phrases,
            selectedBoardThemePreset = boardThemePreset,
            editor = editor,
            message = message,
            canAddPhrase = phrases.size < MAX_BOARD_PHRASES,
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

    fun setBoardThemePreset(preset: BoardThemePreset) {
        viewModelScope.launch {
            setBoardThemePresetUseCase(preset)
        }
    }

    fun startAddPhrase() {
        if (uiState.value.phrases.size >= MAX_BOARD_PHRASES) {
            _message.value = SettingsMessage.PhraseLimitReached
            return
        }
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
            _message.value = SettingsMessage.MissingRequiredFields
            return
        }

        val isNewPhrase = editor.id == 0L
        if (isNewPhrase && uiState.value.phrases.size >= MAX_BOARD_PHRASES) {
            _message.value = SettingsMessage.PhraseLimitReached
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

    fun testSpeech() {
        viewModelScope.launch {
            val error = speechRepository.speak(
                text = TEST_SPEECH_PHRASE,
                audioPath = null,
            )
            _message.value = if (error == null) {
                SettingsMessage.SpeechTestOk
            } else {
                SettingsMessage.SpeechTestFailed
            }
        }
    }

    companion object {
        private const val TEST_SPEECH_PHRASE = "Hello, this is a speech test."
    }
}
