package org.openaac.vocal.feature.settings

import org.openaac.vocal.core.domain.model.BoardThemePreset
import org.openaac.vocal.core.domain.model.MAX_BOARD_PHRASES
import org.openaac.vocal.core.domain.model.MAX_PHRASE_GROUPS
import org.openaac.vocal.core.domain.model.Phrase
import org.openaac.vocal.core.domain.model.PhraseGroup
import org.openaac.vocal.core.domain.model.SavePhraseWithSymbolResult
import org.openaac.vocal.core.domain.model.SymbolCacheMaxSizeMb
import org.openaac.vocal.core.domain.model.SymbolCacheUsage
import org.openaac.vocal.core.domain.usecase.AssignPhraseGroupUseCase
import org.openaac.vocal.core.domain.usecase.CleanSymbolCacheUseCase
import org.openaac.vocal.core.domain.monitoring.MonitoringEvents
import org.openaac.vocal.core.domain.usecase.CreatePhraseGroupResult
import org.openaac.vocal.core.domain.usecase.CreatePhraseGroupUseCase
import org.openaac.vocal.core.domain.usecase.DeletePhraseGroupUseCase
import org.openaac.vocal.core.domain.usecase.DeletePhraseUseCase
import org.openaac.vocal.core.domain.usecase.EnsureDefaultBoardUseCase
import org.openaac.vocal.core.domain.usecase.GetSymbolCacheUsageUseCase
import org.openaac.vocal.core.domain.usecase.ObserveAllPhrasesUseCase
import org.openaac.vocal.core.domain.usecase.ObserveBoardThemePresetUseCase
import org.openaac.vocal.core.domain.usecase.ObservePhraseGroupsUseCase
import org.openaac.vocal.core.domain.usecase.ObserveSymbolCacheMaxSizeUseCase
import org.openaac.vocal.core.domain.usecase.RenamePhraseGroupUseCase
import org.openaac.vocal.core.domain.usecase.SavePhraseWithSymbolUseCase
import org.openaac.vocal.core.domain.usecase.SetBoardThemePresetUseCase
import org.openaac.vocal.core.domain.usecase.SetSymbolCacheMaxSizeUseCase
import org.openaac.vocal.core.domain.repository.MonitoringRepository
import org.openaac.vocal.core.domain.repository.SpeechRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
    val groupId: Long? = null,
)

data class GroupEditorState(
    val id: Long = 0,
    val name: String = "",
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
    GroupCreated,
    GroupRenamed,
    GroupDeleted,
    GroupLimitReached,
    GroupNameRequired,
    PhraseMoved,
}

data class SettingsUiState(
    val phrases: List<Phrase> = emptyList(),
    val groups: List<PhraseGroup> = emptyList(),
    val selectedBoardThemePreset: BoardThemePreset = BoardThemePreset.Default,
    val selectedSymbolCacheMaxSize: SymbolCacheMaxSizeMb = SymbolCacheMaxSizeMb.Default,
    val symbolCacheUsage: SymbolCacheUsage = SymbolCacheUsage(0L, SymbolCacheMaxSizeMb.Default.bytes),
    val editor: PhraseEditorState? = null,
    val groupEditor: GroupEditorState? = null,
    /** Phrase currently choosing a destination group via the non-gesture Move dialog. */
    val movePhrase: Phrase? = null,
    val message: SettingsMessage? = null,
    val showCacheLimitDialog: Boolean = false,
    val isSavingPhrase: Boolean = false,
    val canAddPhrase: Boolean = true,
    val canAddGroup: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeAllPhrasesUseCase: ObserveAllPhrasesUseCase,
    observeBoardThemePresetUseCase: ObserveBoardThemePresetUseCase,
    observeSymbolCacheMaxSizeUseCase: ObserveSymbolCacheMaxSizeUseCase,
    observePhraseGroupsUseCase: ObservePhraseGroupsUseCase,
    private val ensureDefaultBoardUseCase: EnsureDefaultBoardUseCase,
    private val savePhraseWithSymbolUseCase: SavePhraseWithSymbolUseCase,
    private val deletePhraseUseCase: DeletePhraseUseCase,
    private val createPhraseGroupUseCase: CreatePhraseGroupUseCase,
    private val renamePhraseGroupUseCase: RenamePhraseGroupUseCase,
    private val deletePhraseGroupUseCase: DeletePhraseGroupUseCase,
    private val assignPhraseGroupUseCase: AssignPhraseGroupUseCase,
    private val setBoardThemePresetUseCase: SetBoardThemePresetUseCase,
    private val setSymbolCacheMaxSizeUseCase: SetSymbolCacheMaxSizeUseCase,
    private val getSymbolCacheUsageUseCase: GetSymbolCacheUsageUseCase,
    private val cleanSymbolCacheUseCase: CleanSymbolCacheUseCase,
    private val speechRepository: SpeechRepository,
    private val monitoringRepository: MonitoringRepository,
) : ViewModel() {

    private val _editor = MutableStateFlow<PhraseEditorState?>(null)
    private val _groupEditor = MutableStateFlow<GroupEditorState?>(null)
    private val _movePhrase = MutableStateFlow<Phrase?>(null)
    private val _message = MutableStateFlow<SettingsMessage?>(null)
    private val _showCacheLimitDialog = MutableStateFlow(false)
    private val _isSavingPhrase = MutableStateFlow(false)
    private val _symbolCacheUsage = MutableStateFlow(
        SymbolCacheUsage(0L, SymbolCacheMaxSizeMb.Default.bytes),
    )
    private val _boardId = MutableStateFlow<Long?>(null)

    private var defaultBoardId: Long = 0

    val uiState: StateFlow<SettingsUiState> = combine(
        combine(
            observeAllPhrasesUseCase(),
            _boardId.flatMapLatest { boardId ->
                if (boardId == null) flowOf(emptyList()) else observePhraseGroupsUseCase(boardId)
            },
            observeBoardThemePresetUseCase(),
            observeSymbolCacheMaxSizeUseCase(),
            _symbolCacheUsage,
        ) { phrases, groups, boardThemePreset, cacheMaxSize, cacheUsage ->
            PhrasesGroupsThemeCache(phrases, groups, boardThemePreset, cacheMaxSize, cacheUsage)
        },
        combine(
            _editor,
            _groupEditor,
            _movePhrase,
            _message,
            combine(_showCacheLimitDialog, _isSavingPhrase) { limitDialog, saving ->
                limitDialog to saving
            },
        ) { editor, groupEditor, movePhrase, message, limitAndSaving ->
            EditorFlags(
                editor = editor,
                groupEditor = groupEditor,
                movePhrase = movePhrase,
                message = message,
                showCacheLimitDialog = limitAndSaving.first,
                isSavingPhrase = limitAndSaving.second,
            )
        },
    ) { phrasesGroupsThemeCache, editorFlags ->
        SettingsUiState(
            phrases = phrasesGroupsThemeCache.phrases,
            groups = phrasesGroupsThemeCache.groups,
            selectedBoardThemePreset = phrasesGroupsThemeCache.boardThemePreset,
            selectedSymbolCacheMaxSize = phrasesGroupsThemeCache.cacheMaxSize,
            symbolCacheUsage = phrasesGroupsThemeCache.cacheUsage,
            editor = editorFlags.editor,
            groupEditor = editorFlags.groupEditor,
            movePhrase = editorFlags.movePhrase,
            message = editorFlags.message,
            showCacheLimitDialog = editorFlags.showCacheLimitDialog,
            isSavingPhrase = editorFlags.isSavingPhrase,
            canAddPhrase = phrasesGroupsThemeCache.phrases.size < MAX_BOARD_PHRASES,
            canAddGroup = phrasesGroupsThemeCache.groups.size < MAX_PHRASE_GROUPS,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    init {
        viewModelScope.launch {
            defaultBoardId = ensureDefaultBoardUseCase().id
            _boardId.value = defaultBoardId
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
            groupId = phrase.groupId,
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

    fun updateEditorGroupId(groupId: Long?) {
        _editor.update { it?.copy(groupId = groupId) }
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
                        groupId = editor.groupId,
                    ),
                )
                when (result) {
                    is SavePhraseWithSymbolResult.Saved -> {
                        // Ensure group assignment is applied for both new and edited phrases.
                        assignPhraseGroupUseCase(result.phraseId, editor.groupId)
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

    fun startAddGroup() {
        if (uiState.value.groups.size >= MAX_PHRASE_GROUPS) {
            _message.value = SettingsMessage.GroupLimitReached
            return
        }
        _groupEditor.value = GroupEditorState()
        _message.value = null
    }

    fun startRenameGroup(group: PhraseGroup) {
        _groupEditor.value = GroupEditorState(id = group.id, name = group.name)
        _message.value = null
    }

    fun updateGroupEditorName(value: String) {
        _groupEditor.update { it?.copy(name = value) }
    }

    fun dismissGroupEditor() {
        _groupEditor.value = null
    }

    fun saveGroupEditor() {
        val editor = _groupEditor.value ?: return
        val trimmed = editor.name.trim()
        if (trimmed.isEmpty()) {
            _message.value = SettingsMessage.GroupNameRequired
            return
        }
        viewModelScope.launch {
            if (editor.id == 0L) {
                when (createPhraseGroupUseCase(defaultBoardId, trimmed)) {
                    is CreatePhraseGroupResult.Created -> {
                        _groupEditor.value = null
                        _message.value = SettingsMessage.GroupCreated
                    }
                    CreatePhraseGroupResult.LimitReached -> {
                        _message.value = SettingsMessage.GroupLimitReached
                    }
                    CreatePhraseGroupResult.BlankName -> {
                        _message.value = SettingsMessage.GroupNameRequired
                    }
                }
            } else {
                val renamed = renamePhraseGroupUseCase(editor.id, trimmed)
                if (renamed) {
                    _groupEditor.value = null
                    _message.value = SettingsMessage.GroupRenamed
                } else {
                    _message.value = SettingsMessage.GroupNameRequired
                }
            }
        }
    }

    fun deleteGroup(group: PhraseGroup) {
        viewModelScope.launch {
            deletePhraseGroupUseCase(group.id)
            _message.value = SettingsMessage.GroupDeleted
        }
    }

    /** Opens the non-gesture "Move to group…" chooser for [phrase]. */
    fun startMovePhrase(phrase: Phrase) {
        _movePhrase.value = phrase
        _message.value = null
    }

    fun dismissMovePhrase() {
        _movePhrase.value = null
    }

    /**
     * Moves [phraseId] into [groupId] (or ungrouped when null).
     * Used by both drag-and-drop and the Move dialog.
     */
    fun movePhraseToGroup(phraseId: Long, groupId: Long?) {
        viewModelScope.launch {
            assignPhraseGroupUseCase(phraseId, groupId)
            _movePhrase.value = null
            _message.value = SettingsMessage.PhraseMoved
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun testSpeech() {
        viewModelScope.launch {
            val interactionId =
                monitoringRepository.startInteraction(MonitoringEvents.Interaction.TestSpeech)
            try {
                val error = speechRepository.speak(
                    text = TEST_SPEECH_PHRASE,
                    audioPath = null,
                )
                val success = error == null
                _message.value = if (success) {
                    SettingsMessage.SpeechTestOk
                } else {
                    SettingsMessage.SpeechTestFailed
                }
                monitoringRepository.recordCustomEvent(
                    eventName = MonitoringEvents.Name.SpeechTest,
                    attributes = buildMap {
                        put(MonitoringEvents.Attr.Success, success)
                        if (error != null) {
                            put(MonitoringEvents.Attr.Error, error.name)
                        }
                    },
                )
            } catch (t: Throwable) {
                _message.value = SettingsMessage.SpeechTestFailed
                monitoringRepository.recordHandledException(
                    throwable = t,
                    attributes = mapOf(
                        MonitoringEvents.Attr.Component to "SettingsViewModel.testSpeech",
                    ),
                )
            } finally {
                monitoringRepository.endInteraction(interactionId)
            }
        }
    }

    private suspend fun refreshCacheUsage() {
        _symbolCacheUsage.value = getSymbolCacheUsageUseCase()
    }

    private data class PhrasesGroupsThemeCache(
        val phrases: List<Phrase>,
        val groups: List<PhraseGroup>,
        val boardThemePreset: BoardThemePreset,
        val cacheMaxSize: SymbolCacheMaxSizeMb,
        val cacheUsage: SymbolCacheUsage,
    )

    private data class EditorFlags(
        val editor: PhraseEditorState?,
        val groupEditor: GroupEditorState?,
        val movePhrase: Phrase?,
        val message: SettingsMessage?,
        val showCacheLimitDialog: Boolean,
        val isSavingPhrase: Boolean,
    )

    companion object {
        private const val TEST_SPEECH_PHRASE = "Hello, this is a speech test."
    }
}
