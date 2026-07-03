package org.openaac.vocal.feature.settings

import org.openaac.vocal.core.domain.model.Board
import org.openaac.vocal.core.domain.model.BoardThemePreset
import org.openaac.vocal.core.domain.model.Phrase
import org.openaac.vocal.core.ui.accessibility.AacSecondaryTouchTarget
import org.openaac.vocal.core.ui.components.AacSecondaryButton
import org.openaac.vocal.core.ui.theme.VocalTheme
import org.openaac.vocal.core.ui.theme.VocalThemePresets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
        onBoardColumnsSelected = viewModel::updateBoardColumns,
        onDismissEditor = viewModel::dismissEditor,
        onSaveEditor = viewModel::saveEditor,
        onEditorLabelChange = viewModel::updateEditorLabel,
        onEditorSpokenTextChange = viewModel::updateEditorSpokenText,
        onEditorRowChange = viewModel::updateEditorRow,
        onEditorColumnChange = viewModel::updateEditorColumn,
        onBoardThemePresetSelected = viewModel::setBoardThemePreset,
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
    onBoardColumnsSelected: (Int) -> Unit,
    onDismissEditor: () -> Unit,
    onSaveEditor: () -> Unit,
    onEditorLabelChange: (String) -> Unit,
    onEditorSpokenTextChange: (String) -> Unit,
    onEditorRowChange: (Int) -> Unit,
    onEditorColumnChange: (Int) -> Unit,
    onBoardThemePresetSelected: (BoardThemePreset) -> Unit,
    onClearMessage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessage = when (uiState.message) {
        SettingsMessage.MissingRequiredFields -> {
            stringResource(R.string.settings_message_missing_required_fields)
        }
        SettingsMessage.PhraseSaved -> stringResource(R.string.settings_message_phrase_saved)
        SettingsMessage.PhraseDeleted -> stringResource(R.string.settings_message_phrase_deleted)
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
            FloatingActionButton(
                onClick = onAddPhrase,
                content = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.settings_add_phrase),
                    )
                },
            )
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
            item(key = "board-column-selector") {
                BoardColumnSelector(
                    options = uiState.boardColumnOptions,
                    selectedColumns = uiState.board?.columns,
                    onColumnsSelected = onBoardColumnsSelected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )
            }

            item(key = "theme-preset-picker") {
                ThemePresetPicker(
                    selectedPreset = uiState.selectedBoardThemePreset,
                    onPresetSelected = onBoardThemePresetSelected,
                )
            }

            items(uiState.phrases, key = { it.id }) { phrase ->
                PhraseListItem(
                    phrase = phrase,
                    onEdit = { onEditPhrase(phrase) },
                    onDelete = { onDeletePhrase(phrase) },
                )
            }
        }
    }

    uiState.editor?.let { editor ->
        PhraseEditorDialog(
            editor = editor,
            onDismiss = onDismissEditor,
            onSave = onSaveEditor,
            onLabelChange = onEditorLabelChange,
            onSpokenTextChange = onEditorSpokenTextChange,
            onRowChange = onEditorRowChange,
            onColumnChange = onEditorColumnChange,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BoardColumnSelector(
    options: List<Int>,
    selectedColumns: Int?,
    onColumnsSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_board_columns_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.settings_board_columns_description),
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { columns ->
                    val optionLabel = stringResource(
                        R.string.settings_board_columns_option,
                        columns,
                    )
                    val optionDescription = stringResource(
                        R.string.settings_board_columns_option_description,
                        columns,
                    )
                    FilterChip(
                        selected = selectedColumns == columns,
                        onClick = { onColumnsSelected(columns) },
                        label = { Text(optionLabel) },
                        enabled = selectedColumns != null,
                        modifier = Modifier
                            .defaultMinSize(
                                minWidth = AacSecondaryTouchTarget,
                                minHeight = AacSecondaryTouchTarget,
                            )
                            .semantics { contentDescription = optionDescription },
                    )
                }
            }
        }
    }
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
            }
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(
                        R.string.settings_edit_phrase,
                        phrase.label,
                    ),
                )
            }
            IconButton(onClick = onDelete) {
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
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onLabelChange: (String) -> Unit,
    onSpokenTextChange: (String) -> Unit,
    onRowChange: (Int) -> Unit,
    onColumnChange: (Int) -> Unit,
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
                    label = { Text(stringResource(R.string.settings_field_label)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = editor.spokenText,
                    onValueChange = onSpokenTextChange,
                    label = { Text(stringResource(R.string.settings_field_spoken_text)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = editor.row.toString(),
                    onValueChange = { value ->
                        onRowChange(value.toIntOrNull() ?: 0)
                    },
                    label = { Text(stringResource(R.string.settings_field_row)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = editor.column.toString(),
                    onValueChange = { value ->
                        onColumnChange(value.toIntOrNull() ?: 0)
                    },
                    label = { Text(stringResource(R.string.settings_field_column)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            AacSecondaryButton(
                label = stringResource(R.string.settings_save),
                onClick = onSave,
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.settings_cancel))
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    VocalTheme {
        SettingsScreen(
            uiState = SettingsUiState(
                board = Board(
                    id = 1,
                    name = "My Board",
                    rows = 3,
                    columns = 4,
                ),
                phrases = listOf(
                    Phrase(1, 1, "Yes", "Yes", 0, 0),
                    Phrase(2, 1, "Help", "I need help", 0, 1),
                ),
            ),
            onAddPhrase = {},
            onEditPhrase = {},
            onDeletePhrase = {},
            onBoardColumnsSelected = {},
            onDismissEditor = {},
            onSaveEditor = {},
            onEditorLabelChange = {},
            onEditorSpokenTextChange = {},
            onEditorRowChange = {},
            onEditorColumnChange = {},
            onBoardThemePresetSelected = {},
            onClearMessage = {},
        )
    }
}
