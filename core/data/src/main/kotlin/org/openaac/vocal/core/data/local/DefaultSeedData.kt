package org.openaac.vocal.core.data.local

import org.openaac.vocal.core.data.local.entity.BoardEntity
import org.openaac.vocal.core.data.local.entity.PhraseEntity
import org.openaac.vocal.core.data.local.dao.BoardDao
import org.openaac.vocal.core.data.local.dao.PhraseDao
import org.openaac.vocal.core.domain.model.BundledPhraseIcons

internal object DefaultSeedData {
    const val DEFAULT_BOARD_NAME = "My Board"
    const val DEFAULT_ROWS = 3
    const val DEFAULT_COLUMNS = 3

    val starterPhrases = listOf(
        PhraseSeed("Yes", "Yes", 0, 0, BundledPhraseIcons.YES),
        PhraseSeed("No", "No", 0, 1, BundledPhraseIcons.NO),
        PhraseSeed("Help", "I need help", 0, 2, BundledPhraseIcons.HELP),
        PhraseSeed("Water", "I want water", 1, 0, BundledPhraseIcons.WATER),
        PhraseSeed("Bathroom", "I need the bathroom", 1, 1, BundledPhraseIcons.BATHROOM),
        PhraseSeed("Happy", "I am happy", 1, 2, BundledPhraseIcons.HAPPY),
        PhraseSeed("Sad", "I am sad", 2, 0, BundledPhraseIcons.SAD),
        PhraseSeed("More", "More please", 2, 1, BundledPhraseIcons.MORE),
        PhraseSeed("Stop", "Stop please", 2, 2, BundledPhraseIcons.STOP),
    )

    data class PhraseSeed(
        val label: String,
        val spokenText: String,
        val row: Int,
        val column: Int,
        val iconPath: String,
    )

    suspend fun ensureDefaultBoard(boardDao: BoardDao, phraseDao: PhraseDao): BoardEntity {
        val existing = boardDao.getDefaultBoard()
        if (existing != null) {
            backfillStarterPhraseIcons(existing.id, phraseDao)
            return existing
        }

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
                    iconPath = seed.iconPath,
                ),
            )
        }
        return board
    }

    private suspend fun backfillStarterPhraseIcons(boardId: Long, phraseDao: PhraseDao) {
        starterPhrases.forEach { seed ->
            phraseDao.setIconPathIfMissing(
                boardId = boardId,
                label = seed.label,
                row = seed.row,
                column = seed.column,
                iconPath = seed.iconPath,
            )
        }
    }
}
