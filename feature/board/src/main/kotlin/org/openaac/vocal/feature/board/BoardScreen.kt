package org.openaac.vocal.feature.board

import org.openaac.vocal.core.domain.model.Board
import org.openaac.vocal.core.domain.model.BoardSeedKeys
import org.openaac.vocal.core.domain.model.BundledPhraseIcons
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.openaac.vocal.feature.board.R

private val BoardGridGutter = 4.dp
private val BoardSwitcherHeight = AacSecondaryTouchTarget
private val BoardSwitcherGutter = 4.dp
private val BoardSwitcherCorner = 4.dp

@Composable
fun BoardRoute(
    modifier: Modifier = Modifier,
    viewModel: BoardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BoardScreen(
        uiState = uiState,
        onCellSelected = viewModel::onCellSelected,
        onBoardSelected = viewModel::onBoardSelected,
        resolveCachedIconFilePath = viewModel::resolveCachedIconFilePath,
        modifier = modifier,
    )
}

@Composable
fun BoardScreen(
    uiState: BoardUiState,
    onCellSelected: (Phrase) -> Unit,
    onBoardSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    resolveCachedIconFilePath: (String?) -> String? = { null },
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (uiState.showBoardSwitcher) {
            BoardSwitcherBar(
                boards = uiState.boards,
                selectedBoardId = uiState.board?.id,
                onBoardSelected = onBoardSelected,
            )
        }

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

/**
 * Fixed compact bar for switching boards. Home is always the leftmost control
 * (icon). Hidden entirely when there is only one board so the grid can use
 * the full screen.
 */
@Composable
private fun BoardSwitcherBar(
    boards: List<Board>,
    selectedBoardId: Long?,
    onBoardSelected: (Long) -> Unit,
) {
    val ordered = remember(boards) {
        val home = boards.filter { it.isHome }
        val rest = boards.filterNot { it.isHome }
        home + rest
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(BoardSwitcherGutter),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ordered.forEach { board ->
            BoardSwitcherButton(
                board = board,
                selected = board.id == selectedBoardId,
                onClick = { onBoardSelected(board.id) },
            )
        }
    }
}

@Composable
private fun BoardSwitcherButton(
    board: Board,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val shortLabel = shortBoardBarLabel(board)
    val description = if (board.isHome) {
        stringResource(R.string.board_home_content_description)
    } else {
        stringResource(R.string.board_switcher_content_description, board.name)
    }
    val borderWidth = if (selected) 2.dp else 1.dp
    val containerColor = if (selected) {
        colors.secondaryContainer
    } else {
        colors.surface
    }
    val contentColor = if (selected) {
        colors.onSecondaryContainer
    } else {
        colors.onSurface
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .height(BoardSwitcherHeight)
            .defaultMinSize(minWidth = BoardSwitcherHeight, minHeight = BoardSwitcherHeight)
            .widthIn(max = 120.dp)
            .semantics {
                contentDescription = description
                this.selected = selected
            },
        shape = RoundedCornerShape(BoardSwitcherCorner),
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(borderWidth, colors.outline),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (board.isHome) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
            } else {
                Text(
                    text = shortLabel,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Compact bar label; full [Board.name] remains in TalkBack via contentDescription. */
@Composable
private fun shortBoardBarLabel(board: Board): String = when (board.seedKey) {
    BoardSeedKeys.FOOD_DRINK -> stringResource(R.string.board_switcher_short_food_drink)
    BoardSeedKeys.FEELINGS -> stringResource(R.string.board_switcher_short_feelings)
    BoardSeedKeys.PEOPLE -> stringResource(R.string.board_switcher_short_people)
    else -> board.name.substringBefore(' ').ifBlank { board.name }
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
private fun BoardScreenMultiBoardPreview() {
    VocalTheme {
        BoardScreen(
            uiState = sampleBoardUiState(phraseCount = 5, multiBoard = true),
            onCellSelected = {},
            onBoardSelected = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 500)
@Composable
private fun BoardScreenSingleBoardPreview() {
    VocalTheme {
        BoardScreen(
            uiState = sampleBoardUiState(phraseCount = 8, multiBoard = false),
            onCellSelected = {},
            onBoardSelected = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 500)
@Composable
private fun BoardScreenTopicSelectedPreview() {
    VocalTheme {
        BoardScreen(
            uiState = sampleBoardUiState(phraseCount = 6, multiBoard = true, selectedSeed = BoardSeedKeys.FOOD_DRINK),
            onCellSelected = {},
            onBoardSelected = {},
        )
    }
}

private fun sampleBoardUiState(
    phraseCount: Int,
    multiBoard: Boolean,
    selectedSeed: String = BoardSeedKeys.HOME,
): BoardUiState {
    val boards = if (multiBoard) {
        listOf(
            Board(id = 1, name = "Home", rows = 4, columns = 8, seedKey = BoardSeedKeys.HOME, isHome = true),
            Board(id = 2, name = "Food & Drink", rows = 4, columns = 8, seedKey = BoardSeedKeys.FOOD_DRINK),
            Board(id = 3, name = "Feelings", rows = 4, columns = 8, seedKey = BoardSeedKeys.FEELINGS),
            Board(id = 4, name = "People", rows = 4, columns = 8, seedKey = BoardSeedKeys.PEOPLE),
        )
    } else {
        listOf(
            Board(id = 1, name = "Home", rows = 4, columns = 8, seedKey = BoardSeedKeys.HOME, isHome = true),
        )
    }
    val selected = boards.first { it.seedKey == selectedSeed }
    val groups = listOf(
        PhraseGroup(id = 1, boardId = selected.id, name = "Basics", colorIndex = 0, sortOrder = 0),
        PhraseGroup(id = 2, boardId = selected.id, name = "Needs", colorIndex = 2, sortOrder = 1),
    )
    return BoardUiState(
        board = selected,
        homeBoardId = 1,
        boards = boards,
        phrases = (1..phraseCount).map { index ->
            val zeroBasedIndex = index - 1
            Phrase(
                id = index.toLong(),
                boardId = selected.id,
                label = samplePhraseLabel(index),
                spokenText = samplePhraseSpokenText(index),
                sortOrder = zeroBasedIndex,
                iconPath = samplePhraseIcon(index),
                groupId = when (index) {
                    1, 2, 3 -> 1L
                    4 -> 2L
                    else -> null
                },
                targetBoardId = if (selected.isHome && index == 5) 2L else null,
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
