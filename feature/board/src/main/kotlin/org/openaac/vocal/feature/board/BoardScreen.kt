package org.openaac.vocal.feature.board

import org.openaac.vocal.core.domain.model.BundledPhraseIcons
import org.openaac.vocal.core.domain.model.Phrase
import org.openaac.vocal.core.domain.model.computeBoardGrid
import org.openaac.vocal.core.ui.components.AacCellButton
import org.openaac.vocal.core.ui.theme.VocalTheme
import org.openaac.vocal.core.ui.theme.boardColors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        modifier = modifier,
    )
}

@Composable
fun BoardScreen(
    uiState: BoardUiState,
    onPhraseSelected: (Phrase) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gridColors = boardColors()

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
                val grid = computeBoardGrid(uiState.phrases.size)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
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
                                val slotIndex = rowIndex * grid.columns + columnIndex
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                ) {
                                    if (slotIndex < uiState.phrases.size) {
                                        val phrase = uiState.phrases[slotIndex]
                                        AacCellButton(
                                            label = phrase.label,
                                            contentDescription = phrase.spokenText,
                                            iconResId = bundledPhraseIconResId(phrase.iconPath),
                                            onClick = { onPhraseSelected(phrase) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 500)
@Composable
private fun BoardScreenFivePhrasesPreview() {
    VocalTheme {
        BoardScreen(
            uiState = BoardUiState(
                board = org.openaac.vocal.core.domain.model.Board(
                    id = 1,
                    name = "My Board",
                    rows = 3,
                    columns = 4,
                ),
                phrases = listOf(
                    Phrase(1, 1, "Yes", "Yes", 0, 0, iconPath = BundledPhraseIcons.YES),
                    Phrase(2, 1, "No", "No", 0, 1, iconPath = BundledPhraseIcons.NO),
                    Phrase(3, 1, "Help", "I need help", 0, 2, iconPath = BundledPhraseIcons.HELP),
                    Phrase(4, 1, "Water", "I want water", 0, 3, iconPath = BundledPhraseIcons.WATER),
                    Phrase(5, 1, "Stop", "Stop please", 1, 0, iconPath = BundledPhraseIcons.STOP),
                ),
                isLoading = false,
            ),
            onPhraseSelected = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 500)
@Composable
private fun BoardScreenNinePhrasesPreview() {
    VocalTheme {
        BoardScreen(
            uiState = BoardUiState(
                board = org.openaac.vocal.core.domain.model.Board(
                    id = 1,
                    name = "My Board",
                    rows = 3,
                    columns = 4,
                ),
                phrases = (1..9).map { index ->
                    Phrase(
                        index.toLong(),
                        1,
                        "Phrase $index",
                        "Phrase $index",
                        index / 4,
                        index % 4,
                    )
                },
                isLoading = false,
            ),
            onPhraseSelected = {},
        )
    }
}
