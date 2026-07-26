package org.openaac.vocal.feature.board

import org.openaac.vocal.core.domain.model.BundledPhraseIcons
import org.openaac.vocal.core.domain.model.Board
import org.openaac.vocal.core.domain.model.Phrase
import org.openaac.vocal.core.domain.model.PhraseGroup
import org.openaac.vocal.core.domain.model.columnMajorIndex
import org.openaac.vocal.core.domain.model.computeBoardGrid
import org.openaac.vocal.core.domain.model.isFolderCell
import org.openaac.vocal.core.domain.model.orderedPhrasesForBoard
import org.openaac.vocal.core.ui.accessibility.AacSecondaryTouchTarget
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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
        onCellSelected = viewModel::onCellSelected,
        onGoHome = viewModel::onGoHome,
        resolveCachedIconFilePath = viewModel::resolveCachedIconFilePath,
        modifier = modifier,
    )
}

@Composable
fun BoardScreen(
    uiState: BoardUiState,
    onCellSelected: (Phrase) -> Unit,
    onGoHome: () -> Unit,
    modifier: Modifier = Modifier,
    resolveCachedIconFilePath: (String?) -> String? = { null },
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        BoardTopBar(
            boardName = uiState.board?.name ?: stringResource(R.string.board_title_default),
            showHomeControl = uiState.showHomeControl,
            onGoHome = onGoHome,
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
                    onCellSelected = onCellSelected,
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
private fun BoardTopBar(
    boardName: String,
    showHomeControl: Boolean,
    onGoHome: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showHomeControl) {
            val homeDescription = stringResource(R.string.board_home_content_description)
            IconButton(
                onClick = onGoHome,
                modifier = Modifier
                    .defaultMinSize(
                        minWidth = AacSecondaryTouchTarget,
                        minHeight = AacSecondaryTouchTarget,
                    )
                    .semantics { contentDescription = homeDescription },
            ) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
        Text(
            text = boardName,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun BoardPhraseGrid(
    phrases: List<Phrase>,
    groups: List<PhraseGroup>,
    onCellSelected: (Phrase) -> Unit,
    resolveCachedIconFilePath: (String?) -> String?,
    modifier: Modifier = Modifier,
) {
    val gridColors = boardColors()
    val grid = computeBoardGrid(phrases.size)
    val groupsById = remember(groups) { groups.associateBy { it.id } }
    // List order is the sole placement input; fill top-to-bottom, then left-to-right.
    val sortedPhrases = remember(phrases) { orderedPhrasesForBoard(phrases) }

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
                    val slotIndex = columnMajorIndex(
                        row = rowIndex,
                        column = columnIndex,
                        rows = grid.rows,
                    )
                    val phrase = sortedPhrases.getOrNull(slotIndex)
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
                            onCellSelected = onCellSelected,
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
    onCellSelected: (Phrase) -> Unit,
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
    val contentDescription = when {
        phrase.isFolderCell -> stringResource(
            R.string.board_folder_content_description,
            phrase.label,
        )
        group != null -> stringResource(
            R.string.board_phrase_grouped_content_description,
            phrase.spokenText,
            group.name,
        )
        else -> phrase.spokenText
    }
    // Folder cells stay ungrouped (no color tint) so icon + label carry meaning.
    val tint = if (phrase.isFolderCell) null else phraseGroupBackground(group?.colorIndex)

    AacCellButton(
        label = phrase.label,
        contentDescription = contentDescription,
        iconResId = iconResId,
        iconBitmap = cachedBitmap,
        backgroundColor = tint,
        onClick = { onCellSelected(phrase) },
        modifier = modifier,
    )
}

@Preview(showBackground = true, widthDp = 800, heightDp = 500)
@Composable
private fun BoardScreenFivePhrasesPreview() {
    VocalTheme {
        BoardScreen(
            uiState = sampleBoardUiState(phraseCount = 5),
            onCellSelected = {},
            onGoHome = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 500)
@Composable
private fun BoardScreenTopicWithHomePreview() {
    VocalTheme {
        BoardScreen(
            uiState = sampleBoardUiState(phraseCount = 6, isHome = false),
            onCellSelected = {},
            onGoHome = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 500)
@Composable
private fun BoardScreenSeventeenPhrasesPreview() {
    VocalTheme {
        BoardScreen(
            uiState = sampleBoardUiState(phraseCount = 17),
            onCellSelected = {},
            onGoHome = {},
        )
    }
}

private fun sampleBoardUiState(phraseCount: Int, isHome: Boolean = true): BoardUiState {
    val groups = listOf(
        PhraseGroup(id = 1, boardId = 1, name = "Basics", colorIndex = 0, sortOrder = 0),
        PhraseGroup(id = 2, boardId = 1, name = "Needs", colorIndex = 2, sortOrder = 1),
    )
    return BoardUiState(
        board = Board(
            id = if (isHome) 1 else 2,
            name = if (isHome) "Home" else "Food & Drink",
            rows = 1,
            columns = 1,
            seedKey = if (isHome) "home" else "food_drink",
            isHome = isHome,
        ),
        homeBoardId = 1,
        phrases = (1..phraseCount).map { index ->
            val zeroBasedIndex = index - 1
            Phrase(
                id = index.toLong(),
                boardId = if (isHome) 1 else 2,
                label = samplePhraseLabel(index),
                spokenText = samplePhraseSpokenText(index),
                sortOrder = zeroBasedIndex,
                iconPath = samplePhraseIcon(index),
                groupId = when (index) {
                    1, 2, 3 -> 1L
                    4 -> 2L
                    else -> null
                },
                targetBoardId = if (isHome && index == 5) 2L else null,
            )
        },
        groups = groups,
        isLoading = false,
        showHomeControl = !isHome,
    )
}

private fun samplePhraseLabel(index: Int): String = when (index) {
    1 -> "Yes"
    2 -> "No"
    3 -> "Help"
    4 -> "Water"
    5 -> "Food & Drink"
    else -> "Phrase $index"
}

private fun samplePhraseSpokenText(index: Int): String = when (index) {
    3 -> "I need help"
    4 -> "I want water"
    5 -> "Food & Drink"
    else -> samplePhraseLabel(index)
}

private fun samplePhraseIcon(index: Int): String? = when (index) {
    1 -> BundledPhraseIcons.YES
    2 -> BundledPhraseIcons.NO
    3 -> BundledPhraseIcons.HELP
    4 -> BundledPhraseIcons.WATER
    5 -> BundledPhraseIcons.FOLDER
    else -> null
}
