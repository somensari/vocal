package org.openaac.vocal.feature.settings

import org.openaac.vocal.core.domain.model.BoardThemePreset
import org.openaac.vocal.core.domain.model.MAX_BOARD_PHRASES
import org.openaac.vocal.core.domain.model.Phrase
import org.openaac.vocal.core.domain.model.SavePhraseWithSymbolResult
import org.openaac.vocal.core.domain.model.SymbolCacheMaxSizeMb
import org.openaac.vocal.core.domain.model.SymbolCacheUsage
import org.openaac.vocal.core.domain.usecase.CleanSymbolCacheUseCase
import org.openaac.vocal.core.domain.usecase.DeletePhraseUseCase
import org.openaac.vocal.core.domain.usecase.EnsureDefaultBoardUseCase
import org.openaac.vocal.core.domain.usecase.GetSymbolCacheUsageUseCase
import org.openaac.vocal.core.domain.usecase.ObserveAllPhrasesUseCase
import org.openaac.vocal.core.domain.usecase.ObserveBoardThemePresetUseCase
import org.openaac.vocal.core.domain.usecase.ObserveSymbolCacheMaxSizeUseCase
import org.openaac.vocal.core.domain.usecase.SavePhraseWithSymbolUseCase
import org.openaac.vocal.core.domain.usecase.SetBoardThemePresetUseCase
import org.openaac.vocal.core.domain.usecase.SetSymbolCacheMaxSizeUseCase
import org.openaac.vocal.core.domain.repository.SpeechRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
    PhraseSavedSymbolUnavailable,
    PhraseDeleted,
    PhraseLimitReached,
    SpeechTestOk,
    SpeechTestFailed,
    CacheCleaned,
}

data class SettingsUiState(
    val phrases: List<Phrase> = emptyList(),
    val selectedBoardThemePreset: BoardThemePreset = BoardThemePreset.Default,
    val selectedSymbolCacheMaxSize: SymbolCacheMaxSizeMb = SymbolCacheMaxSizeMb.Default,
    val symbolCacheUsage: SymbolCacheUsage = SymbolCacheUsage(0L, SymbolCacheMaxSizeMb.Default.bytes),
    val editor: PhraseEditorState? = null,
    val message: SettingsMessage? = null,
    val showCacheLimitDialog: Boolean = false,
    val isSavingPhrase: Boolean = false,
    val canAddPhrase: Boolean = true,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeAllPhrasesUseCase: ObserveAllPhrasesUseCase,
    observeBoardThemePresetUseCase: ObserveBoardThemePresetUseCase,
    observeSymbolCacheMaxSizeUseCase: ObserveSymbolCacheMaxSizeUseCase,
    private val ensureDefaultBoardUseCase: EnsureDefaultBoardUseCase,
    private val savePhraseWithSymbolUseCase: SavePhraseWithSymbolUseCase,
    private val deletePhraseUseCase: DeletePhraseUseCase,
    private val setBoardThemePresetUseCase: SetBoardThemePresetUseCase,
    private val setSymbolCacheMaxSizeUseCase: SetSymbolCacheMaxSizeUseCase,
    private val getSymbolCacheUsageUseCase: GetSymbolCacheUsageUseCase,
    private val cleanSymbolCacheUseCase: CleanSymbolCacheUseCase,
    private val speechRepository: SpeechRepository,
) : ViewModel() {

    private val _editor = MutableStateFlow<PhraseEditorState?>(null)
    private val _message = MutableStateFlow<SettingsMessage?>(null)
    private val _showCacheLimitDialog = MutableStateFlow(false)
    private val _isSavingPhrase = MutableStateFlow(false)
    private val _symbolCacheUsage = MutableStateFlow(
        SymbolCacheUsage(0L, SymbolCacheMaxSizeMb.Default.bytes),
    )

    private var defaultBoardId: Long = 0

    val uiState: StateFlow<SettingsUiState> = combine(
        combine(
            observeAllPhrasesUseCase(),
            observeBoardThemePresetUseCase(),
            observeSymbolCacheMaxSizeUseCase(),
            _symbolCacheUsage,
        ) { phrases, boardThemePreset, cacheMaxSize, cacheUsage ->
            PhrasesThemeCache(phrases, boardThemePreset, cacheMaxSize, cacheUsage)
        },
        combine(_editor, _message, _showCacheLimitDialog, _isSavingPhrase) { editor, message, limitDialog, saving ->
            EditorFlags(editor, message, limitDialog, saving)
        },
    ) { phrasesThemeCache, editorFlags ->
        SettingsUiState(
            phrases = phrasesThemeCache.phrases,
            selectedBoardThemePreset = phrasesThemeCache.boardThemePreset,
            selectedSymbolCacheMaxSize = phrasesThemeCache.cacheMaxSize,
            symbolCacheUsage = phrasesThemeCache.cacheUsage,
            editor = editorFlags.editor,
            message = editorFlags.message,
            showCacheLimitDialog = editorFlags.showCacheLimitDialog,
            isSavingPhrase = editorFlags.isSavingPhrase,
            canAddPhrase = phrasesThemeCache.phrases.size < MAX_BOARD_PHRASES,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    init {
        viewModelScope.launch {
            defaultBoardId = ensureDefaultBoardUseCase().id
            refreshCacheUsage()
        }
    }

    fun setBoardThemePreset(preset: BoardThemePreset) {
        viewModelScope.launch {
            setBoardThemePresetUseCase(preset)
        }
    }

    fun setSymbolCacheMaxSize(maxSize: SymbolCacheMaxSizeMb) {
        viewModelScope.launch {
            setSymbolCacheMaxSizeUseCase(maxSize)
            refreshCacheUsage()
        }
    }

    fun cleanImageCache() {
        viewModelScope.launch {
            cleanSymbolCacheUseCase()
            refreshCacheUsage()
            _message.value = SettingsMessage.CacheCleaned
        }
    }

    fun dismissCacheLimitDialog() {
        _showCacheLimitDialog.value = false
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
        if (_isSavingPhrase.value) return
        _editor.value = null
    }

    fun saveEditor() {
        if (_isSavingPhrase.value) return
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
            _isSavingPhrase.value = true
            try {
                val result = savePhraseWithSymbolUseCase(
                    Phrase(
                        id = editor.id,
                        boardId = editor.boardId.takeIf { it > 0 } ?: defaultBoardId,
                        label = editor.label.trim(),
                        spokenText = editor.spokenText.trim(),
                        row = editor.row,
                        column = editor.column,
                    ),
                )
                when (result) {
                    is SavePhraseWithSymbolResult.Saved -> {
                        _editor.value = null
                        _message.value = if (result.symbolWarning) {
                            SettingsMessage.PhraseSavedSymbolUnavailable
                        } else {
                            SettingsMessage.PhraseSaved
                        }
                        refreshCacheUsage()
                    }
                    SavePhraseWithSymbolResult.CacheLimitReached -> {
                        _showCacheLimitDialog.value = true
                    }
                }
            } finally {
                _isSavingPhrase.value = false
            }
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

    private suspend fun refreshCacheUsage() {
        _symbolCacheUsage.value = getSymbolCacheUsageUseCase()
    }

    private data class PhrasesThemeCache(
        val phrases: List<Phrase>,
        val boardThemePreset: BoardThemePreset,
        val cacheMaxSize: SymbolCacheMaxSizeMb,
        val cacheUsage: SymbolCacheUsage,
    )

    private data class EditorFlags(
        val editor: PhraseEditorState?,
        val message: SettingsMessage?,
        val showCacheLimitDialog: Boolean,
        val isSavingPhrase: Boolean,
    )

    companion object {
        private const val TEST_SPEECH_PHRASE = "Hello, this is a speech test."
    }
}
