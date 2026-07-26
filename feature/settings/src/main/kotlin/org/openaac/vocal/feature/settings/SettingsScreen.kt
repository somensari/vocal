package org.openaac.vocal.feature.settings

import android.content.ClipData
import android.content.ClipDescription
import org.openaac.vocal.core.domain.model.BoardThemePreset
import org.openaac.vocal.core.domain.model.Phrase
import org.openaac.vocal.core.domain.model.PhraseGroup
import org.openaac.vocal.core.domain.model.SymbolCacheMaxSizeMb
import org.openaac.vocal.core.ui.accessibility.AacSecondaryTouchTarget
import org.openaac.vocal.core.ui.components.AacSecondaryButton
import org.openaac.vocal.core.ui.theme.VocalTheme
import org.openaac.vocal.core.ui.theme.VocalThemePresets
import org.openaac.vocal.core.ui.theme.phraseGroupBackground
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private const val PhraseDragLabel = "vocal.phraseId"

@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(
        uiState = uiState,
        onAddPhrase = viewModel::startAddPhrase,
        onEditPhrase = viewModel::startEditPhrase,
        onDeletePhrase = viewModel::deletePhrase,
        onDismissEditor = viewModel::dismissEditor,
        onSaveEditor = viewModel::saveEditor,
        onEditorLabelChange = viewModel::updateEditorLabel,
        onEditorSpokenTextChange = viewModel::updateEditorSpokenText,
        onEditorRowChange = viewModel::updateEditorRow,
        onEditorColumnChange = viewModel::updateEditorColumn,
        onEditorGroupChange = viewModel::updateEditorGroupId,
        onAddGroup = viewModel::startAddGroup,
        onRenameGroup = viewModel::startRenameGroup,
        onDeleteGroup = viewModel::deleteGroup,
        onDismissGroupEditor = viewModel::dismissGroupEditor,
        onSaveGroupEditor = viewModel::saveGroupEditor,
        onGroupEditorNameChange = viewModel::updateGroupEditorName,
        onMovePhrase = viewModel::startMovePhrase,
        onDismissMovePhrase = viewModel::dismissMovePhrase,
        onConfirmMovePhrase = viewModel::movePhraseToGroup,
        onBoardThemePresetSelected = viewModel::setBoardThemePreset,
        onSymbolCacheMaxSizeSelected = viewModel::setSymbolCacheMaxSize,
        onCleanImageCache = viewModel::cleanImageCache,
        onDismissCacheLimitDialog = viewModel::dismissCacheLimitDialog,
        onTestSpeech = viewModel::testSpeech,
        onClearMessage = viewModel::clearMessage,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onAddPhrase: () -> Unit,
    onEditPhrase: (Phrase) -> Unit,
    onDeletePhrase: (Phrase) -> Unit,
    onDismissEditor: () -> Unit,
    onSaveEditor: () -> Unit,
    onEditorLabelChange: (String) -> Unit,
    onEditorSpokenTextChange: (String) -> Unit,
    onEditorRowChange: (Int) -> Unit,
    onEditorColumnChange: (Int) -> Unit,
    onEditorGroupChange: (Long?) -> Unit,
    onAddGroup: () -> Unit,
    onRenameGroup: (PhraseGroup) -> Unit,
    onDeleteGroup: (PhraseGroup) -> Unit,
    onDismissGroupEditor: () -> Unit,
    onSaveGroupEditor: () -> Unit,
    onGroupEditorNameChange: (String) -> Unit,
    onMovePhrase: (Phrase) -> Unit,
    onDismissMovePhrase: () -> Unit,
    onConfirmMovePhrase: (phraseId: Long, groupId: Long?) -> Unit,
    onBoardThemePresetSelected: (BoardThemePreset) -> Unit,
    onSymbolCacheMaxSizeSelected: (SymbolCacheMaxSizeMb) -> Unit,
    onCleanImageCache: () -> Unit,
    onDismissCacheLimitDialog: () -> Unit,
    onTestSpeech: () -> Unit,
    onClearMessage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessage = when (uiState.message) {
        SettingsMessage.MissingRequiredFields -> {
            stringResource(R.string.settings_message_missing_required_fields)
        }
        SettingsMessage.PhraseSaved -> stringResource(R.string.settings_message_phrase_saved)
        SettingsMessage.PhraseSavedSymbolUnavailable -> {
            stringResource(R.string.settings_message_phrase_saved_symbol_unavailable)
        }
        SettingsMessage.PhraseDeleted -> stringResource(R.string.settings_message_phrase_deleted)
        SettingsMessage.PhraseLimitReached -> stringResource(R.string.settings_message_phrase_limit_reached)
        SettingsMessage.SpeechTestOk -> stringResource(R.string.settings_message_speech_test_ok)
        SettingsMessage.SpeechTestFailed -> stringResource(R.string.settings_message_speech_test_failed)
        SettingsMessage.CacheCleaned -> stringResource(R.string.settings_message_cache_cleaned)
        SettingsMessage.GroupCreated -> stringResource(R.string.settings_message_group_created)
        SettingsMessage.GroupRenamed -> stringResource(R.string.settings_message_group_renamed)
        SettingsMessage.GroupDeleted -> stringResource(R.string.settings_message_group_deleted)
        SettingsMessage.GroupLimitReached -> stringResource(R.string.settings_message_group_limit_reached)
        SettingsMessage.GroupNameRequired -> stringResource(R.string.settings_message_group_name_required)
        SettingsMessage.PhraseMoved -> stringResource(R.string.settings_message_phrase_moved)
        null -> null
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onClearMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
            )
        },
        floatingActionButton = {
            val addPhraseDescription = if (uiState.canAddPhrase) {
                stringResource(R.string.settings_add_phrase)
            } else {
                stringResource(R.string.settings_add_phrase_limit_reached)
            }
            if (uiState.canAddPhrase) {
                FloatingActionButton(onClick = onAddPhrase) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = addPhraseDescription,
                    )
                }
            } else {
                DisabledAddPhraseFab(
                    contentDescription = addPhraseDescription,
                    onClick = onAddPhrase,
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "theme-preset-picker") {
                ThemePresetPicker(
                    selectedPreset = uiState.selectedBoardThemePreset,
                    onPresetSelected = onBoardThemePresetSelected,
                )
            }

            item(key = "speech-test") {
                SpeechTestSection(onTestSpeech = onTestSpeech)
            }

            item(key = "phrase-groups") {
                PhraseGroupsSection(
                    groups = uiState.groups,
                    canAddGroup = uiState.canAddGroup,
                    onAddGroup = onAddGroup,
                    onRenameGroup = onRenameGroup,
                    onDeleteGroup = onDeleteGroup,
                )
            }

            item(key = "symbol-cache") {
                SymbolCacheSection(
                    selectedMaxSize = uiState.selectedSymbolCacheMaxSize,
                    usedMegabytes = uiState.symbolCacheUsage.usedMegabytes,
                    maxMegabytes = uiState.symbolCacheUsage.maxMegabytes,
                    onMaxSizeSelected = onSymbolCacheMaxSizeSelected,
                    onCleanImageCache = onCleanImageCache,
                )
            }

            item(key = "attribution") {
                AttributionSection()
            }

            item(key = "organize-phrases") {
                OrganizePhrasesSection(
                    phrases = uiState.phrases,
                    groups = uiState.groups,
                    onEditPhrase = onEditPhrase,
                    onDeletePhrase = onDeletePhrase,
                    onMovePhrase = onMovePhrase,
                    onDropPhrase = onConfirmMovePhrase,
                )
            }
        }
    }

    uiState.editor?.let { editor ->
        PhraseEditorDialog(
            editor = editor,
            groups = uiState.groups,
            isSaving = uiState.isSavingPhrase,
            onDismiss = onDismissEditor,
            onSave = onSaveEditor,
            onLabelChange = onEditorLabelChange,
            onSpokenTextChange = onEditorSpokenTextChange,
            onRowChange = onEditorRowChange,
            onColumnChange = onEditorColumnChange,
            onGroupChange = onEditorGroupChange,
        )
    }

    uiState.groupEditor?.let { editor ->
        GroupEditorDialog(
            editor = editor,
            onDismiss = onDismissGroupEditor,
            onSave = onSaveGroupEditor,
            onNameChange = onGroupEditorNameChange,
        )
    }

    uiState.movePhrase?.let { phrase ->
        MovePhraseDialog(
            phrase = phrase,
            groups = uiState.groups,
            onDismiss = onDismissMovePhrase,
            onConfirm = { groupId -> onConfirmMovePhrase(phrase.id, groupId) },
        )
    }

    if (uiState.showCacheLimitDialog) {
        CacheLimitDialog(onDismiss = onDismissCacheLimitDialog)
    }
}

@Composable
private fun DisabledAddPhraseFab(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .clearAndSetSemantics {
                this.contentDescription = contentDescription
                role = Role.Button
                disabled()
            },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
        )
    }
}

@Composable
private fun PhraseGroupsSection(
    groups: List<PhraseGroup>,
    canAddGroup: Boolean,
    onAddGroup: () -> Unit,
    onRenameGroup: (PhraseGroup) -> Unit,
    onDeleteGroup: (PhraseGroup) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_groups_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(R.string.settings_groups_description),
                style = MaterialTheme.typography.bodyLarge,
            )
            groups.forEach { group ->
                GroupListItem(
                    group = group,
                    onRename = { onRenameGroup(group) },
                    onDelete = { onDeleteGroup(group) },
                )
            }
            val addGroupDescription = if (canAddGroup) {
                stringResource(R.string.settings_add_group)
            } else {
                stringResource(R.string.settings_add_group_limit_reached)
            }
            AacSecondaryButton(
                label = stringResource(R.string.settings_add_group),
                onClick = onAddGroup,
                contentDescription = addGroupDescription,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = AacSecondaryTouchTarget),
            )
        }
    }
}

@Composable
private fun GroupListItem(
    group: PhraseGroup,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    val tint = phraseGroupBackground(group.colorIndex)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = AacSecondaryTouchTarget),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = tint ?: MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp),
                    ),
            )
            Text(
                text = group.name,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        IconButton(
            onClick = onRename,
            modifier = Modifier.defaultMinSize(
                minWidth = AacSecondaryTouchTarget,
                minHeight = AacSecondaryTouchTarget,
            ),
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = stringResource(R.string.settings_edit_group, group.name),
            )
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier.defaultMinSize(
                minWidth = AacSecondaryTouchTarget,
                minHeight = AacSecondaryTouchTarget,
            ),
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.settings_delete_group, group.name),
            )
        }
    }
}

@Composable
private fun GroupEditorDialog(
    editor: GroupEditorState,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onNameChange: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (editor.id == 0L) {
                    stringResource(R.string.settings_add_group_title)
                } else {
                    stringResource(R.string.settings_rename_group_title)
                },
            )
        },
        text = {
            OutlinedTextField(
                value = editor.name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.settings_field_group_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        },
        confirmButton = {
            AacSecondaryButton(
                label = stringResource(R.string.settings_save),
                onClick = onSave,
                contentDescription = stringResource(R.string.settings_save),
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.defaultMinSize(minHeight = AacSecondaryTouchTarget),
            ) {
                Text(stringResource(R.string.settings_cancel))
            }
        },
    )
}

@Composable
private fun SpeechTestSection(
    onTestSpeech: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_speech_test_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.settings_speech_test_description),
                style = MaterialTheme.typography.bodyMedium,
            )
            AacSecondaryButton(
                label = stringResource(R.string.settings_speech_test_button),
                onClick = onTestSpeech,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SymbolCacheSection(
    selectedMaxSize: SymbolCacheMaxSizeMb,
    usedMegabytes: Int,
    maxMegabytes: Int,
    onMaxSizeSelected: (SymbolCacheMaxSizeMb) -> Unit,
    onCleanImageCache: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_symbol_cache_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(R.string.settings_symbol_cache_description),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(
                    R.string.settings_symbol_cache_usage,
                    usedMegabytes,
                    maxMegabytes,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = stringResource(R.string.settings_symbol_cache_max_title),
                style = MaterialTheme.typography.titleMedium,
            )
            SymbolCacheMaxSizeMb.entries.forEach { option ->
                SymbolCacheMaxSizeOption(
                    option = option,
                    selected = option == selectedMaxSize,
                    onClick = { onMaxSizeSelected(option) },
                )
            }
            Text(
                text = stringResource(R.string.settings_symbol_cache_clean_description),
                style = MaterialTheme.typography.bodyMedium,
            )
            AacSecondaryButton(
                label = stringResource(R.string.settings_symbol_cache_clean_button),
                onClick = onCleanImageCache,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = AacSecondaryTouchTarget),
            )
        }
    }
}

@Composable
private fun SymbolCacheMaxSizeOption(
    option: SymbolCacheMaxSizeMb,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val label = stringResource(R.string.settings_symbol_cache_max_option, option.megabytes)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = AacSecondaryTouchTarget)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun AttributionSection() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_attribution_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(R.string.settings_attribution_body),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun CacheLimitDialog(
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_cache_limit_dialog_title)) },
        text = { Text(stringResource(R.string.settings_cache_limit_dialog_message)) },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.defaultMinSize(minHeight = AacSecondaryTouchTarget),
            ) {
                Text(stringResource(R.string.settings_cache_limit_dialog_dismiss))
            }
        },
    )
}

@Composable
private fun ThemePresetPicker(
    selectedPreset: BoardThemePreset,
    onPresetSelected: (BoardThemePreset) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_theme_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(R.string.settings_theme_description),
                style = MaterialTheme.typography.bodyLarge,
            )
            VocalThemePresets.all.forEach { preset ->
                ThemePresetOption(
                    preset = preset,
                    selected = preset == selectedPreset,
                    onClick = { onPresetSelected(preset) },
                )
            }
        }
    }
}

@Composable
private fun ThemePresetOption(
    preset: BoardThemePreset,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Text(
            text = stringResource(preset.labelResId()),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

private fun BoardThemePreset.labelResId(): Int = when (this) {
    BoardThemePreset.DefaultBlue -> R.string.settings_theme_default_blue
    BoardThemePreset.HighContrast -> R.string.settings_theme_high_contrast
    BoardThemePreset.SoftPastel -> R.string.settings_theme_soft_pastel
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OrganizePhrasesSection(
    phrases: List<Phrase>,
    groups: List<PhraseGroup>,
    onEditPhrase: (Phrase) -> Unit,
    onDeletePhrase: (Phrase) -> Unit,
    onMovePhrase: (Phrase) -> Unit,
    onDropPhrase: (phraseId: Long, groupId: Long?) -> Unit,
) {
    val orderedGroups = remember(groups) {
        groups.sortedWith(compareBy<PhraseGroup> { it.sortOrder }.thenBy { it.id })
    }
    val groupIds = remember(orderedGroups) { orderedGroups.map { it.id }.toSet() }
    val phrasesByGroup = remember(phrases, orderedGroups) {
        orderedGroups.associate { group ->
            group.id to phrases
                .filter { it.groupId == group.id }
                .sortedWith(compareBy<Phrase> { it.row }.thenBy { it.column }.thenBy { it.id })
        }
    }
    val ungroupedPhrases = remember(phrases, groupIds) {
        phrases
            .filter { it.groupId == null || it.groupId !in groupIds }
            .sortedWith(compareBy<Phrase> { it.row }.thenBy { it.column }.thenBy { it.id })
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_organize_phrases_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = stringResource(R.string.settings_organize_phrases_description),
            style = MaterialTheme.typography.bodyLarge,
        )
        orderedGroups.forEach { group ->
            PhraseDropGroupCard(
                title = group.name,
                colorIndex = group.colorIndex,
                phrases = phrasesByGroup[group.id].orEmpty(),
                emptyMessage = stringResource(R.string.settings_group_section_empty),
                dropGroupId = group.id,
                onEditPhrase = onEditPhrase,
                onDeletePhrase = onDeletePhrase,
                onMovePhrase = onMovePhrase,
                onDropPhrase = onDropPhrase,
            )
        }
        PhraseDropGroupCard(
            title = stringResource(R.string.settings_ungrouped_section),
            colorIndex = null,
            phrases = ungroupedPhrases,
            emptyMessage = stringResource(R.string.settings_ungrouped_section_empty),
            dropGroupId = null,
            onEditPhrase = onEditPhrase,
            onDeletePhrase = onDeletePhrase,
            onMovePhrase = onMovePhrase,
            onDropPhrase = onDropPhrase,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhraseDropGroupCard(
    title: String,
    colorIndex: Int?,
    phrases: List<Phrase>,
    emptyMessage: String,
    dropGroupId: Long?,
    onEditPhrase: (Phrase) -> Unit,
    onDeletePhrase: (Phrase) -> Unit,
    onMovePhrase: (Phrase) -> Unit,
    onDropPhrase: (phraseId: Long, groupId: Long?) -> Unit,
) {
    val isDragOverState = remember { mutableStateOf(false) }
    val dropTarget = remember(dropGroupId, onDropPhrase) {
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) {
                isDragOverState.value = false
            }

            override fun onEntered(event: DragAndDropEvent) {
                isDragOverState.value = true
            }

            override fun onExited(event: DragAndDropEvent) {
                isDragOverState.value = false
            }

            override fun onEnded(event: DragAndDropEvent) {
                isDragOverState.value = false
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                isDragOverState.value = false
                val phraseId = event.phraseIdOrNull() ?: return false
                onDropPhrase(phraseId, dropGroupId)
                return true
            }
        }
    }
    val isDragOver = isDragOverState.value
    val tint = phraseGroupBackground(colorIndex)
    val borderColor = if (isDragOver) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .dragAndDropTarget(
                shouldStartDragAndDrop = { event ->
                    ClipDescription.MIMETYPE_TEXT_PLAIN in event.mimeTypes()
                },
                target = dropTarget,
            )
            .border(
                width = if (isDragOver) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (tint != null) {
                        Modifier.background(tint.copy(alpha = 0.45f))
                    } else {
                        Modifier
                    },
                )
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = if (isDragOver) {
                    stringResource(R.string.settings_drop_target_active, title)
                } else {
                    title
                },
                style = MaterialTheme.typography.titleMedium,
            )
            if (phrases.isEmpty()) {
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                phrases.forEach { phrase ->
                    PhraseListItem(
                        phrase = phrase,
                        onEdit = { onEditPhrase(phrase) },
                        onDelete = { onDeletePhrase(phrase) },
                        onMove = { onMovePhrase(phrase) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhraseListItem(
    phrase: Phrase,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMove: () -> Unit,
) {
    val dragHandleDescription = stringResource(R.string.settings_drag_phrase_handle, phrase.label)
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .dragAndDropSource {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            startTransfer(
                                DragAndDropTransferData(
                                    clipData = ClipData.newPlainText(
                                        PhraseDragLabel,
                                        phrase.id.toString(),
                                    ),
                                ),
                            )
                        },
                        onDrag = { _, _ -> },
                    )
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = dragHandleDescription,
                modifier = Modifier
                    .defaultMinSize(
                        minWidth = AacSecondaryTouchTarget,
                        minHeight = AacSecondaryTouchTarget,
                    )
                    .padding(8.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = phrase.label, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(R.string.settings_phrase_speaks, phrase.spokenText),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            IconButton(
                onClick = onMove,
                modifier = Modifier.defaultMinSize(
                    minWidth = AacSecondaryTouchTarget,
                    minHeight = AacSecondaryTouchTarget,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = stringResource(
                        R.string.settings_move_phrase,
                        phrase.label,
                    ),
                )
            }
            IconButton(
                onClick = onEdit,
                modifier = Modifier.defaultMinSize(
                    minWidth = AacSecondaryTouchTarget,
                    minHeight = AacSecondaryTouchTarget,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(
                        R.string.settings_edit_phrase,
                        phrase.label,
                    ),
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.defaultMinSize(
                    minWidth = AacSecondaryTouchTarget,
                    minHeight = AacSecondaryTouchTarget,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(
                        R.string.settings_delete_phrase,
                        phrase.label,
                    ),
                )
            }
        }
    }
}

@Composable
private fun MovePhraseDialog(
    phrase: Phrase,
    groups: List<PhraseGroup>,
    onDismiss: () -> Unit,
    onConfirm: (groupId: Long?) -> Unit,
) {
    var selectedGroupId by remember(phrase.id, phrase.groupId) {
        mutableStateOf(phrase.groupId)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_move_phrase_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.settings_move_phrase_message, phrase.label),
                    style = MaterialTheme.typography.bodyLarge,
                )
                GroupAssignmentOption(
                    label = stringResource(R.string.settings_group_none),
                    selected = selectedGroupId == null,
                    enabled = true,
                    onClick = { selectedGroupId = null },
                )
                groups.forEach { group ->
                    GroupAssignmentOption(
                        label = group.name,
                        selected = selectedGroupId == group.id,
                        enabled = true,
                        onClick = { selectedGroupId = group.id },
                    )
                }
            }
        },
        confirmButton = {
            AacSecondaryButton(
                label = stringResource(R.string.settings_move_phrase_button),
                onClick = { onConfirm(selectedGroupId) },
                contentDescription = stringResource(
                    R.string.settings_move_phrase,
                    phrase.label,
                ),
                modifier = Modifier.defaultMinSize(minHeight = AacSecondaryTouchTarget),
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.defaultMinSize(minHeight = AacSecondaryTouchTarget),
            ) {
                Text(stringResource(R.string.settings_cancel))
            }
        },
    )
}

private fun DragAndDropEvent.phraseIdOrNull(): Long? {
    val clipData = toAndroidDragEvent().clipData ?: return null
    if (clipData.itemCount <= 0) return null
    return clipData.getItemAt(0).text?.toString()?.toLongOrNull()
}

@Composable
private fun PhraseEditorDialog(
    editor: PhraseEditorState,
    groups: List<PhraseGroup>,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onLabelChange: (String) -> Unit,
    onSpokenTextChange: (String) -> Unit,
    onRowChange: (Int) -> Unit,
    onColumnChange: (Int) -> Unit,
    onGroupChange: (Long?) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (editor.id == 0L) {
                    stringResource(R.string.settings_add_phrase_title)
                } else {
                    stringResource(R.string.settings_edit_phrase_title)
                },
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = editor.label,
                    onValueChange = onLabelChange,
                    enabled = !isSaving,
                    label = { Text(stringResource(R.string.settings_field_label)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = editor.spokenText,
                    onValueChange = onSpokenTextChange,
                    enabled = !isSaving,
                    label = { Text(stringResource(R.string.settings_field_spoken_text)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = editor.row.toString(),
                    onValueChange = { value ->
                        onRowChange(value.toIntOrNull() ?: 0)
                    },
                    enabled = !isSaving,
                    label = { Text(stringResource(R.string.settings_field_row)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = editor.column.toString(),
                    onValueChange = { value ->
                        onColumnChange(value.toIntOrNull() ?: 0)
                    },
                    enabled = !isSaving,
                    label = { Text(stringResource(R.string.settings_field_column)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = stringResource(R.string.settings_field_group),
                    style = MaterialTheme.typography.titleMedium,
                )
                GroupAssignmentOption(
                    label = stringResource(R.string.settings_group_none),
                    selected = editor.groupId == null,
                    enabled = !isSaving,
                    onClick = { onGroupChange(null) },
                )
                groups.forEach { group ->
                    GroupAssignmentOption(
                        label = group.name,
                        selected = editor.groupId == group.id,
                        enabled = !isSaving,
                        onClick = { onGroupChange(group.id) },
                    )
                }
                if (isSaving) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.defaultMinSize(
                                minWidth = 24.dp,
                                minHeight = 24.dp,
                            ),
                        )
                        Text(text = stringResource(R.string.settings_saving_phrase))
                    }
                }
            }
        },
        confirmButton = {
            if (!isSaving) {
                AacSecondaryButton(
                    label = stringResource(R.string.settings_save),
                    onClick = onSave,
                    contentDescription = stringResource(R.string.settings_save),
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving,
                modifier = Modifier.defaultMinSize(minHeight = AacSecondaryTouchTarget),
            ) {
                Text(stringResource(R.string.settings_cancel))
            }
        },
    )
}

@Composable
private fun GroupAssignmentOption(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = AacSecondaryTouchTarget)
            .selectable(
                selected = selected,
                enabled = enabled,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            enabled = enabled,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    VocalTheme {
        SettingsScreen(
            uiState = SettingsUiState(
                phrases = listOf(
                    Phrase(1, 1, "Yes", "Yes", 0, 0, groupId = 1),
                    Phrase(2, 1, "Help", "I need help", 0, 1, groupId = 1),
                ),
                groups = listOf(
                    PhraseGroup(1, 1, "Basics", colorIndex = 0, sortOrder = 0),
                ),
            ),
            onAddPhrase = {},
            onEditPhrase = {},
            onDeletePhrase = {},
            onDismissEditor = {},
            onSaveEditor = {},
            onEditorLabelChange = {},
            onEditorSpokenTextChange = {},
            onEditorRowChange = {},
            onEditorColumnChange = {},
            onEditorGroupChange = {},
            onAddGroup = {},
            onRenameGroup = {},
            onDeleteGroup = {},
            onDismissGroupEditor = {},
            onSaveGroupEditor = {},
            onGroupEditorNameChange = {},
            onMovePhrase = {},
            onDismissMovePhrase = {},
            onConfirmMovePhrase = { _, _ -> },
            onBoardThemePresetSelected = {},
            onSymbolCacheMaxSizeSelected = {},
            onCleanImageCache = {},
            onDismissCacheLimitDialog = {},
            onTestSpeech = {},
            onClearMessage = {},
        )
    }
}
