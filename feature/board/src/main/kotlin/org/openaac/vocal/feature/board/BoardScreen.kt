package org.openaac.vocal.feature.board

import org.openaac.vocal.core.domain.model.BundledPhraseIcons
import org.openaac.vocal.core.domain.model.Board
import org.openaac.vocal.core.domain.model.Phrase
import org.openaac.vocal.core.domain.model.PhraseGroup
import org.openaac.vocal.core.domain.model.computeBoardGrid
import org.openaac.vocal.core.ui.components.AacCellButton
import org.openaac.vocal.core.ui.theme.VocalTheme
import org.openaac.vocal.core.ui.theme.boardColors
import org.openaac.vocal.core.ui.theme.phraseGroupBackground
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.openaac.vocal.feature.board.R

private val BoardGridGutter = 4.dp

@Composable
fun BoardRoute(
    modifier: Modifier = Modifier,
    viewModel: BoardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BoardScreen(
        uiState = uiState,
        onPhraseSelected = viewModel::onPhraseSelected,
        resolveCachedIconFilePath = viewModel::resolveCachedIconFilePath,
        modifier = modifier,
    )
}

@Composable
fun BoardScreen(
    uiState: BoardUiState,
    onPhraseSelected: (Phrase) -> Unit,
    modifier: Modifier = Modifier,
    resolveCachedIconFilePath: (String?) -> String? = { null },
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Text(
            text = uiState.board?.name ?: stringResource(R.string.board_title_default),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.phrases.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = stringResource(R.string.board_empty_message))
                }
            }

            else -> {
                BoardPhraseGrid(
                    phrases = uiState.phrases,
                    groups = uiState.groups,
                    onPhraseSelected = onPhraseSelected,
                    resolveCachedIconFilePath = resolveCachedIconFilePath,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun BoardPhraseGrid(
    phrases: List<Phrase>,
    groups: List<PhraseGroup>,
    onPhraseSelected: (Phrase) -> Unit,
    resolveCachedIconFilePath: (String?) -> String?,
    modifier: Modifier = Modifier,
) {
    val gridColors = boardColors()
    val grid = computeBoardGrid(phrases.size)
    val groupsById = remember(groups) { groups.associateBy { it.id } }
    // Phrases are already clustered by the ViewModel (column-major positions).
    val phrasesByCell = remember(phrases) {
        phrases.associateBy { it.row to it.column }
    }

    Column(
        modifier = modifier
            .background(gridColors.boardGridBackground)
            .padding(BoardGridGutter),
        verticalArrangement = Arrangement.spacedBy(BoardGridGutter),
    ) {
        for (rowIndex in 0 until grid.rows) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(BoardGridGutter),
            ) {
                for (columnIndex in 0 until grid.columns) {
                    val phrase = phrasesByCell[rowIndex to columnIndex]
                    if (phrase == null) {
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(gridColors.boardGridBackground),
                        )
                    } else {
                        PhraseBoardCell(
                            phrase = phrase,
                            group = phrase.groupId?.let { groupsById[it] },
                            onPhraseSelected = onPhraseSelected,
                            resolveCachedIconFilePath = resolveCachedIconFilePath,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhraseBoardCell(
    phrase: Phrase,
    group: PhraseGroup?,
    onPhraseSelected: (Phrase) -> Unit,
    resolveCachedIconFilePath: (String?) -> String?,
    modifier: Modifier = Modifier,
) {
    val bundledResId = bundledPhraseIconResId(phrase.iconPath)
    val cachedBitmap = remember(phrase.iconPath) {
        if (bundledResId != null) {
            null
        } else {
            resolveCachedIconFilePath(phrase.iconPath)
                ?.let { path -> android.graphics.BitmapFactory.decodeFile(path) }
                ?.asImageBitmap()
        }
    }
    val iconResId = when {
        bundledResId != null -> bundledResId
        cachedBitmap != null -> null
        else -> placeholderPhraseIconResId()
    }
    val contentDescription = if (group != null) {
        stringResource(
            R.string.board_phrase_grouped_content_description,
            phrase.spokenText,
            group.name,
        )
    } else {
        phrase.spokenText
    }
    val tint = phraseGroupBackground(group?.colorIndex)

    AacCellButton(
        label = phrase.label,
        contentDescription = contentDescription,
        iconResId = iconResId,
        iconBitmap = cachedBitmap,
        backgroundColor = tint,
        onClick = { onPhraseSelected(phrase) },
        modifier = modifier,
    )
}

@Preview(showBackground = true, widthDp = 800, heightDp = 500)
@Composable
private fun BoardScreenFivePhrasesPreview() {
    VocalTheme {
        BoardScreen(
            uiState = sampleBoardUiState(phraseCount = 5),
            onPhraseSelected = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 500)
@Composable
private fun BoardScreenNinePhrasesPreview() {
    VocalTheme {
        BoardScreen(
            uiState = sampleBoardUiState(phraseCount = 9),
            onPhraseSelected = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 500)
@Composable
private fun BoardScreenSeventeenPhrasesPreview() {
    VocalTheme {
        BoardScreen(
            uiState = sampleBoardUiState(phraseCount = 17),
            onPhraseSelected = {},
        )
    }
}

private fun sampleBoardUiState(phraseCount: Int): BoardUiState {
    val grid = computeBoardGrid(phraseCount)
    val groups = listOf(
        PhraseGroup(id = 1, boardId = 1, name = "Basics", colorIndex = 0, sortOrder = 0),
        PhraseGroup(id = 2, boardId = 1, name = "Needs", colorIndex = 2, sortOrder = 1),
    )
    return BoardUiState(
        board = Board(
            id = 1,
            name = "My Board",
            rows = 1,
            columns = 1,
        ),
        phrases = (1..phraseCount).map { index ->
            val zeroBasedIndex = index - 1
            Phrase(
                id = index.toLong(),
                boardId = 1,
                label = samplePhraseLabel(index),
                spokenText = samplePhraseSpokenText(index),
                row = zeroBasedIndex % grid.rows,
                column = zeroBasedIndex / grid.rows,
                iconPath = samplePhraseIcon(index),
                groupId = when (index) {
                    1, 2, 3 -> 1L
                    4 -> 2L
                    else -> null
                },
            )
        },
        groups = groups,
        isLoading = false,
    )
}

private fun samplePhraseLabel(index: Int): String = when (index) {
    1 -> "Yes"
    2 -> "No"
    3 -> "Help"
    4 -> "Water"
    5 -> "Stop"
    else -> "Phrase $index"
}

private fun samplePhraseSpokenText(index: Int): String = when (index) {
    3 -> "I need help"
    4 -> "I want water"
    5 -> "Stop please"
    else -> samplePhraseLabel(index)
}

private fun samplePhraseIcon(index: Int): String? = when (index) {
    1 -> BundledPhraseIcons.YES
    2 -> BundledPhraseIcons.NO
    3 -> BundledPhraseIcons.HELP
    4 -> BundledPhraseIcons.WATER
    5 -> BundledPhraseIcons.STOP
    else -> null
}
