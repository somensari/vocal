package org.openaac.vocal.core.data.local

import org.openaac.vocal.core.data.local.dao.BoardDao
import org.openaac.vocal.core.data.local.dao.PhraseDao
import org.openaac.vocal.core.data.local.dao.PhraseGroupDao
import org.openaac.vocal.core.data.local.entity.BoardEntity
import org.openaac.vocal.core.data.local.entity.PhraseEntity
import org.openaac.vocal.core.data.local.entity.PhraseGroupEntity
import org.openaac.vocal.core.domain.model.BundledPhraseIcons

internal object DefaultSeedData {
    const val DEFAULT_BOARD_NAME = "My Board"
    /** Matches [org.openaac.vocal.core.domain.model.computeBoardGrid] for 32 phrases (8×4). */
    const val DEFAULT_ROWS = 4
    const val DEFAULT_COLUMNS = 8

    /**
     * Starter topic groups (within the 8-group cap). Colors are fixed palette indices.
     */
    val starterGroups = listOf(
        GroupSeed(name = "Basics", colorIndex = 0, sortOrder = 0),
        GroupSeed(name = "Social", colorIndex = 1, sortOrder = 1),
        GroupSeed(name = "Needs", colorIndex = 2, sortOrder = 2),
        GroupSeed(name = "Feelings", colorIndex = 3, sortOrder = 3),
        GroupSeed(name = "Places", colorIndex = 4, sortOrder = 4),
    )

    /**
     * Thirty-two common AAC starter phrases with bundled icons and pre-assigned groups.
     * Fresh installs and caregiver reset seed these offline; no network is required.
     *
     * List order becomes [PhraseEntity.sortOrder]; the board places cells column-major
     * (top-to-bottom, then left-to-right) from this order.
     */
    val starterPhrases = listOf(
        // Basics
        PhraseSeed("Yes", "Yes", BundledPhraseIcons.YES, "Basics"),
        PhraseSeed("No", "No", BundledPhraseIcons.NO, "Basics"),
        PhraseSeed("Help", "I need help", BundledPhraseIcons.HELP, "Basics"),
        PhraseSeed("More", "More please", BundledPhraseIcons.MORE, "Basics"),
        PhraseSeed("Stop", "Stop please", BundledPhraseIcons.STOP, "Basics"),
        PhraseSeed("Wait", "Please wait", BundledPhraseIcons.WAIT, "Basics"),
        // Social
        PhraseSeed("Please", "Please", BundledPhraseIcons.PLEASE, "Social"),
        PhraseSeed("Thank you", "Thank you", BundledPhraseIcons.THANK_YOU, "Social"),
        PhraseSeed("Hello", "Hello", BundledPhraseIcons.HELLO, "Social"),
        PhraseSeed("Goodbye", "Goodbye", BundledPhraseIcons.GOODBYE, "Social"),
        PhraseSeed("Love you", "I love you", BundledPhraseIcons.LOVE_YOU, "Social"),
        // Needs
        PhraseSeed("Want", "I want", BundledPhraseIcons.WANT, "Needs"),
        PhraseSeed("Eat", "I want to eat", BundledPhraseIcons.EAT, "Needs"),
        PhraseSeed("Drink", "I want a drink", BundledPhraseIcons.DRINK, "Needs"),
        PhraseSeed("Water", "I want water", BundledPhraseIcons.WATER, "Needs"),
        PhraseSeed("Hungry", "I am hungry", BundledPhraseIcons.HUNGRY, "Needs"),
        PhraseSeed("Bathroom", "I need the bathroom", BundledPhraseIcons.BATHROOM, "Needs"),
        PhraseSeed("Break", "I need a break", BundledPhraseIcons.BREAK, "Needs"),
        // Feelings
        PhraseSeed("Like", "I like this", BundledPhraseIcons.LIKE, "Feelings"),
        PhraseSeed("Don't like", "I don't like this", BundledPhraseIcons.DONT_LIKE, "Feelings"),
        PhraseSeed("Happy", "I am happy", BundledPhraseIcons.HAPPY, "Feelings"),
        PhraseSeed("Sad", "I am sad", BundledPhraseIcons.SAD, "Feelings"),
        PhraseSeed("Tired", "I am tired", BundledPhraseIcons.TIRED, "Feelings"),
        PhraseSeed("Hurt", "I am hurt", BundledPhraseIcons.HURT, "Feelings"),
        PhraseSeed("Hot", "I am hot", BundledPhraseIcons.HOT, "Feelings"),
        PhraseSeed("Cold", "I am cold", BundledPhraseIcons.COLD, "Feelings"),
        // Places
        PhraseSeed("Go", "I want to go", BundledPhraseIcons.GO, "Places"),
        PhraseSeed("Come", "Come here", BundledPhraseIcons.COME, "Places"),
        PhraseSeed("Home", "I want to go home", BundledPhraseIcons.HOME, "Places"),
        PhraseSeed("School", "I want to go to school", BundledPhraseIcons.SCHOOL, "Places"),
        PhraseSeed("Play", "I want to play", BundledPhraseIcons.PLAY, "Places"),
        PhraseSeed("Finished", "I am finished", BundledPhraseIcons.FINISHED, "Places"),
    )

    data class GroupSeed(
        val name: String,
        val colorIndex: Int,
        val sortOrder: Int,
    )

    data class PhraseSeed(
        val label: String,
        val spokenText: String,
        val iconPath: String,
        val groupName: String?,
    )

    suspend fun ensureDefaultBoard(
        boardDao: BoardDao,
        phraseDao: PhraseDao,
        phraseGroupDao: PhraseGroupDao,
    ): BoardEntity {
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

        seedStarterContent(board.id, phraseDao, phraseGroupDao)
        return board
    }

    /**
     * Inserts starter groups and phrases for [boardId]. Caller must clear existing
     * board content before calling when performing a caregiver reset.
     */
    suspend fun seedStarterContent(
        boardId: Long,
        phraseDao: PhraseDao,
        phraseGroupDao: PhraseGroupDao,
    ) {
        val groupIdsByName = linkedMapOf<String, Long>()
        starterGroups.forEach { seed ->
            val id = phraseGroupDao.insert(
                PhraseGroupEntity(
                    boardId = boardId,
                    name = seed.name,
                    colorIndex = seed.colorIndex,
                    sortOrder = seed.sortOrder,
                ),
            )
            groupIdsByName[seed.name] = id
        }

        starterPhrases.forEachIndexed { index, seed ->
            phraseDao.insert(
                PhraseEntity(
                    boardId = boardId,
                    label = seed.label,
                    spokenText = seed.spokenText,
                    sortOrder = index,
                    iconPath = seed.iconPath,
                    groupId = seed.groupName?.let { groupIdsByName[it] },
                ),
            )
        }
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
