package org.openaac.vocal.feature.board

import org.openaac.vocal.core.domain.model.Phrase
import org.openaac.vocal.core.ui.components.AacCellButton
import org.openaac.vocal.core.ui.theme.VocalTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = uiState.board?.name ?: stringResource(R.string.board_title_default),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp),
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
                val columns = uiState.board?.columns ?: 3
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(
                        items = uiState.phrases,
                        key = { it.id },
                    ) { phrase ->
                        AacCellButton(
                            label = phrase.label,
                            contentDescription = phrase.spokenText,
                            onClick = { onPhraseSelected(phrase) },
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BoardScreenPreview() {
    VocalTheme {
        BoardScreen(
            uiState = BoardUiState(
                board = org.openaac.vocal.core.domain.model.Board(
                    id = 1,
                    name = "My Board",
                    rows = 3,
                    columns = 3,
                ),
                phrases = listOf(
                    Phrase(1, 1, "Yes", "Yes", 0, 0),
                    Phrase(2, 1, "Help", "I need help", 0, 1),
                ),
                isLoading = false,
            ),
            onPhraseSelected = {},
        )
    }
}
