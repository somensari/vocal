package org.openaac.vocal.core.data.local

import org.openaac.vocal.core.data.local.entity.BoardEntity
import org.openaac.vocal.core.data.local.entity.PhraseEntity
import org.openaac.vocal.core.data.local.dao.BoardDao
import org.openaac.vocal.core.data.local.dao.PhraseDao

internal object DefaultSeedData {
    const val DEFAULT_BOARD_NAME = "My Board"
    const val DEFAULT_ROWS = 3
    const val DEFAULT_COLUMNS = 3

    val starterPhrases = listOf(
        PhraseSeed("Yes", "Yes", 0, 0),
        PhraseSeed("No", "No", 0, 1),
        PhraseSeed("Help", "I need help", 0, 2),
        PhraseSeed("Water", "I want water", 1, 0),
        PhraseSeed("Bathroom", "I need the bathroom", 1, 1),
        PhraseSeed("Happy", "I am happy", 1, 2),
        PhraseSeed("Sad", "I am sad", 2, 0),
        PhraseSeed("More", "More please", 2, 1),
        PhraseSeed("Stop", "Stop please", 2, 2),
    )

    data class PhraseSeed(
        val label: String,
        val spokenText: String,
        val row: Int,
        val column: Int,
    )

    suspend fun ensureDefaultBoard(boardDao: BoardDao, phraseDao: PhraseDao): BoardEntity {
        val existing = boardDao.getDefaultBoard()
        if (existing != null) return existing

        val boardId = boardDao.insert(
            BoardEntity(
                name = DEFAULT_BOARD_NAME,
                rows = DEFAULT_ROWS,
                columns = DEFAULT_COLUMNS,
                isDefault = true,
            ),
        )
        val board = boardDao.getDefaultBoard()
            ?: BoardEntity(
                id = boardId,
                name = DEFAULT_BOARD_NAME,
                rows = DEFAULT_ROWS,
                columns = DEFAULT_COLUMNS,
                isDefault = true,
            )

        starterPhrases.forEach { seed ->
            phraseDao.insert(
                PhraseEntity(
                    boardId = board.id,
                    label = seed.label,
                    spokenText = seed.spokenText,
                    row = seed.row,
                    column = seed.column,
                ),
            )
        }
        return board
    }
}
