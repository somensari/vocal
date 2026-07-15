package org.openaac.vocal.feature.settings

import org.openaac.vocal.core.domain.model.BoardThemePreset
import org.openaac.vocal.core.domain.model.Phrase
import org.openaac.vocal.core.domain.model.PhraseGroup
import org.openaac.vocal.core.domain.model.SymbolCacheMaxSizeMb
import org.openaac.vocal.core.ui.accessibility.AacSecondaryTouchTarget
import org.openaac.vocal.core.ui.components.AacSecondaryButton
import org.openaac.vocal.core.ui.theme.VocalTheme
import org.openaac.vocal.core.ui.theme.VocalThemePresets
import org.openaac.vocal.core.ui.theme.phraseGroupBackground
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

            items(uiState.phrases, key = { it.id }) { phrase ->
                PhraseListItem(
                    phrase = phrase,
                    groupName = uiState.groups.firstOrNull { it.id == phrase.groupId }?.name,
                    onEdit = { onEditPhrase(phrase) },
                    onDelete = { onDeletePhrase(phrase) },
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

@Composable
private fun PhraseListItem(
    phrase: Phrase,
    groupName: String?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = phrase.label, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = stringResource(R.string.settings_phrase_speaks, phrase.spokenText),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = stringResource(
                        R.string.settings_phrase_position,
                        phrase.row,
                        phrase.column,
                    ),
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = if (groupName != null) {
                        stringResource(R.string.settings_phrase_group, groupName)
                    } else {
                        stringResource(R.string.settings_phrase_ungrouped)
                    },
                    style = MaterialTheme.typography.labelLarge,
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
            onBoardThemePresetSelected = {},
            onSymbolCacheMaxSizeSelected = {},
            onCleanImageCache = {},
            onDismissCacheLimitDialog = {},
            onTestSpeech = {},
            onClearMessage = {},
        )
    }
}
