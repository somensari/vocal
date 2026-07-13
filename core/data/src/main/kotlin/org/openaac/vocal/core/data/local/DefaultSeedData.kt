package org.openaac.vocal.core.data.local

import org.openaac.vocal.core.data.local.entity.BoardEntity
import org.openaac.vocal.core.data.local.entity.PhraseEntity
import org.openaac.vocal.core.data.local.dao.BoardDao
import org.openaac.vocal.core.data.local.dao.PhraseDao
import org.openaac.vocal.core.domain.model.BundledPhraseIcons

internal object DefaultSeedData {
    const val DEFAULT_BOARD_NAME = "My Board"
    /** Matches [org.openaac.vocal.core.domain.model.computeBoardGrid] for 32 phrases (8×4). */
    const val DEFAULT_ROWS = 4
    const val DEFAULT_COLUMNS = 8

    /**
     * Thirty-two common AAC starter phrases with bundled icons.
     * Fresh installs seed these offline; no network is required for board use.
     */
    val starterPhrases = listOf(
        // Row 0
        PhraseSeed("Yes", "Yes", 0, 0, BundledPhraseIcons.YES),
        PhraseSeed("No", "No", 0, 1, BundledPhraseIcons.NO),
        PhraseSeed("Help", "I need help", 0, 2, BundledPhraseIcons.HELP),
        PhraseSeed("Please", "Please", 0, 3, BundledPhraseIcons.PLEASE),
        PhraseSeed("Thank you", "Thank you", 0, 4, BundledPhraseIcons.THANK_YOU),
        PhraseSeed("Hello", "Hello", 0, 5, BundledPhraseIcons.HELLO),
        PhraseSeed("Goodbye", "Goodbye", 0, 6, BundledPhraseIcons.GOODBYE),
        PhraseSeed("More", "More please", 0, 7, BundledPhraseIcons.MORE),
        // Row 1
        PhraseSeed("Want", "I want", 1, 0, BundledPhraseIcons.WANT),
        PhraseSeed("Like", "I like this", 1, 1, BundledPhraseIcons.LIKE),
        PhraseSeed("Don't like", "I don't like this", 1, 2, BundledPhraseIcons.DONT_LIKE),
        PhraseSeed("Eat", "I want to eat", 1, 3, BundledPhraseIcons.EAT),
        PhraseSeed("Drink", "I want a drink", 1, 4, BundledPhraseIcons.DRINK),
        PhraseSeed("Water", "I want water", 1, 5, BundledPhraseIcons.WATER),
        PhraseSeed("Hungry", "I am hungry", 1, 6, BundledPhraseIcons.HUNGRY),
        PhraseSeed("Bathroom", "I need the bathroom", 1, 7, BundledPhraseIcons.BATHROOM),
        // Row 2
        PhraseSeed("Happy", "I am happy", 2, 0, BundledPhraseIcons.HAPPY),
        PhraseSeed("Sad", "I am sad", 2, 1, BundledPhraseIcons.SAD),
        PhraseSeed("Tired", "I am tired", 2, 2, BundledPhraseIcons.TIRED),
        PhraseSeed("Hurt", "I am hurt", 2, 3, BundledPhraseIcons.HURT),
        PhraseSeed("Hot", "I am hot", 2, 4, BundledPhraseIcons.HOT),
        PhraseSeed("Cold", "I am cold", 2, 5, BundledPhraseIcons.COLD),
        PhraseSeed("Stop", "Stop please", 2, 6, BundledPhraseIcons.STOP),
        PhraseSeed("Wait", "Please wait", 2, 7, BundledPhraseIcons.WAIT),
        // Row 3
        PhraseSeed("Go", "I want to go", 3, 0, BundledPhraseIcons.GO),
        PhraseSeed("Come", "Come here", 3, 1, BundledPhraseIcons.COME),
        PhraseSeed("Home", "I want to go home", 3, 2, BundledPhraseIcons.HOME),
        PhraseSeed("School", "I want to go to school", 3, 3, BundledPhraseIcons.SCHOOL),
        PhraseSeed("Play", "I want to play", 3, 4, BundledPhraseIcons.PLAY),
        PhraseSeed("Break", "I need a break", 3, 5, BundledPhraseIcons.BREAK),
        PhraseSeed("Finished", "I am finished", 3, 6, BundledPhraseIcons.FINISHED),
        PhraseSeed("Love you", "I love you", 3, 7, BundledPhraseIcons.LOVE_YOU),
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
                spokenText = seed.spokenText,
                iconPath = seed.iconPath,
            )
        }
    }
}
