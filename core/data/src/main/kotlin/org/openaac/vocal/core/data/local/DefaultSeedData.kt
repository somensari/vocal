package org.openaac.vocal.core.data.local

import org.openaac.vocal.core.data.local.dao.BoardDao
import org.openaac.vocal.core.data.local.dao.PhraseDao
import org.openaac.vocal.core.data.local.dao.PhraseGroupDao
import org.openaac.vocal.core.data.local.entity.BoardEntity
import org.openaac.vocal.core.data.local.entity.PhraseEntity
import org.openaac.vocal.core.data.local.entity.PhraseGroupEntity
import org.openaac.vocal.core.domain.model.BoardSeedKeys
import org.openaac.vocal.core.domain.model.BundledPhraseIcons
import org.openaac.vocal.core.domain.model.MAX_BOARDS

/**
 * Offline seed for the v1 Home + folder board set (up to [MAX_BOARDS] boards).
 *
 * Home includes speakable phrases and folder cells that open topic boards.
 * Topic boards contain speakable phrases only (no nested folders).
 */
internal object DefaultSeedData {
    const val HOME_BOARD_NAME = "Home"
    const val FOOD_DRINK_BOARD_NAME = "Food & Drink"
    const val FEELINGS_BOARD_NAME = "Feelings"
    const val PEOPLE_BOARD_NAME = "People"

    /** Legacy single-board name from earlier releases. */
    const val LEGACY_BOARD_NAME = "My Board"

    /** Matches [org.openaac.vocal.core.domain.model.computeBoardGrid] for dense boards (8×4). */
    const val DEFAULT_ROWS = 4
    const val DEFAULT_COLUMNS = 8

    data class GroupSeed(
        val name: String,
        val colorIndex: Int,
        val sortOrder: Int,
    )

    data class PhraseSeed(
        val label: String,
        val spokenText: String,
        val iconPath: String,
        val groupName: String? = null,
        /** When set, cell is a folder that opens the board with this seed key. */
        val targetSeedKey: String? = null,
    )

    data class BoardSeed(
        val seedKey: String,
        val name: String,
        val isHome: Boolean,
        val groups: List<GroupSeed>,
        val phrases: List<PhraseSeed>,
    )

    val starterBoards: List<BoardSeed> = listOf(
        BoardSeed(
            seedKey = BoardSeedKeys.HOME,
            name = HOME_BOARD_NAME,
            isHome = true,
            groups = listOf(
                GroupSeed(name = "Basics", colorIndex = 0, sortOrder = 0),
                GroupSeed(name = "Social", colorIndex = 1, sortOrder = 1),
                GroupSeed(name = "Needs", colorIndex = 2, sortOrder = 2),
            ),
            phrases = listOf(
                PhraseSeed("Yes", "Yes", BundledPhraseIcons.YES, "Basics"),
                PhraseSeed("No", "No", BundledPhraseIcons.NO, "Basics"),
                PhraseSeed("Help", "I need help", BundledPhraseIcons.HELP, "Basics"),
                PhraseSeed("More", "More please", BundledPhraseIcons.MORE, "Basics"),
                PhraseSeed("Stop", "Stop please", BundledPhraseIcons.STOP, "Basics"),
                PhraseSeed("Wait", "Please wait", BundledPhraseIcons.WAIT, "Basics"),
                PhraseSeed("Please", "Please", BundledPhraseIcons.PLEASE, "Social"),
                PhraseSeed("Thank you", "Thank you", BundledPhraseIcons.THANK_YOU, "Social"),
                PhraseSeed("Hello", "Hello", BundledPhraseIcons.HELLO, "Social"),
                PhraseSeed("Want", "I want", BundledPhraseIcons.WANT, "Needs"),
                PhraseSeed("Bathroom", "I need the bathroom", BundledPhraseIcons.BATHROOM, "Needs"),
                PhraseSeed("Break", "I need a break", BundledPhraseIcons.BREAK, "Needs"),
                // Folder cells — navigate only; icon + label distinguish from phrases.
                PhraseSeed(
                    label = FOOD_DRINK_BOARD_NAME,
                    spokenText = FOOD_DRINK_BOARD_NAME,
                    iconPath = BundledPhraseIcons.FOLDER,
                    targetSeedKey = BoardSeedKeys.FOOD_DRINK,
                ),
                PhraseSeed(
                    label = FEELINGS_BOARD_NAME,
                    spokenText = FEELINGS_BOARD_NAME,
                    iconPath = BundledPhraseIcons.FOLDER,
                    targetSeedKey = BoardSeedKeys.FEELINGS,
                ),
                PhraseSeed(
                    label = PEOPLE_BOARD_NAME,
                    spokenText = PEOPLE_BOARD_NAME,
                    iconPath = BundledPhraseIcons.FOLDER,
                    targetSeedKey = BoardSeedKeys.PEOPLE,
                ),
            ),
        ),
        BoardSeed(
            seedKey = BoardSeedKeys.FOOD_DRINK,
            name = FOOD_DRINK_BOARD_NAME,
            isHome = false,
            groups = listOf(
                GroupSeed(name = "Food & Drink", colorIndex = 2, sortOrder = 0),
            ),
            phrases = listOf(
                PhraseSeed("Eat", "I want to eat", BundledPhraseIcons.EAT, "Food & Drink"),
                PhraseSeed("Drink", "I want a drink", BundledPhraseIcons.DRINK, "Food & Drink"),
                PhraseSeed("Water", "I want water", BundledPhraseIcons.WATER, "Food & Drink"),
                PhraseSeed("Hungry", "I am hungry", BundledPhraseIcons.HUNGRY, "Food & Drink"),
                PhraseSeed("Like", "I like this", BundledPhraseIcons.LIKE, "Food & Drink"),
                PhraseSeed("Don't like", "I don't like this", BundledPhraseIcons.DONT_LIKE, "Food & Drink"),
            ),
        ),
        BoardSeed(
            seedKey = BoardSeedKeys.FEELINGS,
            name = FEELINGS_BOARD_NAME,
            isHome = false,
            groups = listOf(
                GroupSeed(name = "Feelings", colorIndex = 3, sortOrder = 0),
            ),
            phrases = listOf(
                PhraseSeed("Happy", "I am happy", BundledPhraseIcons.HAPPY, "Feelings"),
                PhraseSeed("Sad", "I am sad", BundledPhraseIcons.SAD, "Feelings"),
                PhraseSeed("Tired", "I am tired", BundledPhraseIcons.TIRED, "Feelings"),
                PhraseSeed("Hurt", "I am hurt", BundledPhraseIcons.HURT, "Feelings"),
                PhraseSeed("Hot", "I am hot", BundledPhraseIcons.HOT, "Feelings"),
                PhraseSeed("Cold", "I am cold", BundledPhraseIcons.COLD, "Feelings"),
            ),
        ),
        BoardSeed(
            seedKey = BoardSeedKeys.PEOPLE,
            name = PEOPLE_BOARD_NAME,
            isHome = false,
            groups = listOf(
                GroupSeed(name = "People & Places", colorIndex = 4, sortOrder = 0),
            ),
            phrases = listOf(
                PhraseSeed("Love you", "I love you", BundledPhraseIcons.LOVE_YOU, "People & Places"),
                PhraseSeed("Goodbye", "Goodbye", BundledPhraseIcons.GOODBYE, "People & Places"),
                PhraseSeed("Come", "Come here", BundledPhraseIcons.COME, "People & Places"),
                PhraseSeed("Go", "I want to go", BundledPhraseIcons.GO, "People & Places"),
                PhraseSeed("Home", "I want to go home", BundledPhraseIcons.HOME, "People & Places"),
                PhraseSeed("School", "I want to go to school", BundledPhraseIcons.SCHOOL, "People & Places"),
                PhraseSeed("Play", "I want to play", BundledPhraseIcons.PLAY, "People & Places"),
                PhraseSeed("Finished", "I am finished", BundledPhraseIcons.FINISHED, "People & Places"),
            ),
        ),
    )

    init {
        check(starterBoards.size in 2..MAX_BOARDS) {
            "v1 seeds 2–$MAX_BOARDS boards; found ${starterBoards.size}"
        }
        check(starterBoards.count { it.isHome } == 1)
        check(starterBoards.map { it.seedKey }.toSet() == BoardSeedKeys.ALL.toSet())
    }

    /** All speakable starter phrases across boards (excludes folder cells). */
    val starterPhrases: List<PhraseSeed>
        get() = starterBoards.flatMap { board ->
            board.phrases.filter { it.targetSeedKey == null }
        }

    val starterGroups: List<GroupSeed>
        get() = starterBoards.flatMap { it.groups }

    /**
     * Ensures Home + topic boards exist with folder links. Safe to call on every launch.
     *
     * Fresh installs get the full seeded set. Existing single-board installs are upgraded
     * in place: the default board becomes Home, missing topic boards are added, and
     * folder cells are appended when absent (existing custom phrases are kept).
     */
    suspend fun ensureDefaultBoard(
        boardDao: BoardDao,
        phraseDao: PhraseDao,
        phraseGroupDao: PhraseGroupDao,
    ): BoardEntity {
        val existingHome = boardDao.getDefaultBoard()
        if (existingHome == null) {
            return seedAllBoards(boardDao, phraseDao, phraseGroupDao)
        }

        upgradeLegacyHomeIfNeeded(existingHome, boardDao)
        ensureTopicBoards(boardDao, phraseDao, phraseGroupDao)
        ensureFolderCellsOnHome(boardDao, phraseDao)
        backfillStarterPhraseIcons(boardDao, phraseDao)

        return boardDao.getDefaultBoard()
            ?: error("Home board missing after ensure")
    }

    /**
     * Wipes all boards and re-seeds the Home + folder starter set.
     * Caller is responsible for deleting custom media and clearing the symbol cache.
     */
    suspend fun resetAllBoards(
        boardDao: BoardDao,
        phraseDao: PhraseDao,
        phraseGroupDao: PhraseGroupDao,
    ): BoardEntity {
        phraseDao.deleteAll()
        phraseGroupDao.deleteAll()
        boardDao.deleteAll()
        return seedAllBoards(boardDao, phraseDao, phraseGroupDao)
    }

    private suspend fun seedAllBoards(
        boardDao: BoardDao,
        phraseDao: PhraseDao,
        phraseGroupDao: PhraseGroupDao,
    ): BoardEntity {
        val boardIdsBySeedKey = linkedMapOf<String, Long>()
        starterBoards.forEach { seed ->
            val id = boardDao.insert(
                BoardEntity(
                    name = seed.name,
                    rows = DEFAULT_ROWS,
                    columns = DEFAULT_COLUMNS,
                    isDefault = seed.isHome,
                    seedKey = seed.seedKey,
                ),
            )
            boardIdsBySeedKey[seed.seedKey] = id
        }

        starterBoards.forEach { seed ->
            val boardId = boardIdsBySeedKey.getValue(seed.seedKey)
            seedBoardContent(boardId, seed, boardIdsBySeedKey, phraseDao, phraseGroupDao)
        }

        return boardDao.getDefaultBoard()
            ?: error("Home board missing after seed")
    }

    private suspend fun seedBoardContent(
        boardId: Long,
        seed: BoardSeed,
        boardIdsBySeedKey: Map<String, Long>,
        phraseDao: PhraseDao,
        phraseGroupDao: PhraseGroupDao,
    ) {
        val groupIdsByName = linkedMapOf<String, Long>()
        seed.groups.forEach { group ->
            val id = phraseGroupDao.insert(
                PhraseGroupEntity(
                    boardId = boardId,
                    name = group.name,
                    colorIndex = group.colorIndex,
                    sortOrder = group.sortOrder,
                ),
            )
            groupIdsByName[group.name] = id
        }

        seed.phrases.forEachIndexed { index, phrase ->
            val targetBoardId = phrase.targetSeedKey?.let { key ->
                boardIdsBySeedKey[key]
                    ?: error("Folder target board missing for seed key $key")
            }
            phraseDao.insert(
                PhraseEntity(
                    boardId = boardId,
                    label = phrase.label,
                    spokenText = phrase.spokenText,
                    sortOrder = index,
                    iconPath = phrase.iconPath,
                    groupId = phrase.groupName?.let { groupIdsByName[it] },
                    targetBoardId = targetBoardId,
                ),
            )
        }
    }

    /**
     * Inserts starter groups and phrases for a single board seed (used by tests /
     * callers that already created the board row). Prefer [ensureDefaultBoard] / reset.
     */
    suspend fun seedStarterContent(
        boardId: Long,
        phraseDao: PhraseDao,
        phraseGroupDao: PhraseGroupDao,
        boardDao: BoardDao,
    ) {
        val board = boardDao.getBoard(boardId) ?: return
        val seed = starterBoards.firstOrNull { it.seedKey == board.seedKey }
            ?: starterBoards.first { it.isHome }
        val boardIdsBySeedKey = boardDao.getAllBoards()
            .mapNotNull { entity -> entity.seedKey?.let { it to entity.id } }
            .toMap()
        seedBoardContent(boardId, seed, boardIdsBySeedKey, phraseDao, phraseGroupDao)
    }

    private suspend fun upgradeLegacyHomeIfNeeded(
        home: BoardEntity,
        boardDao: BoardDao,
    ) {
        var updated = home
        if (home.seedKey == null) {
            updated = updated.copy(seedKey = BoardSeedKeys.HOME)
        }
        if (home.name == LEGACY_BOARD_NAME) {
            updated = updated.copy(name = HOME_BOARD_NAME)
        }
        if (updated != home) {
            boardDao.update(updated)
        }
    }

    private suspend fun ensureTopicBoards(
        boardDao: BoardDao,
        phraseDao: PhraseDao,
        phraseGroupDao: PhraseGroupDao,
    ) {
        val existingKeys = boardDao.getAllBoards().mapNotNull { it.seedKey }.toSet()
        starterBoards.filter { !it.isHome && it.seedKey !in existingKeys }.forEach { seed ->
            if (boardDao.countBoards() >= MAX_BOARDS) return
            val id = boardDao.insert(
                BoardEntity(
                    name = seed.name,
                    rows = DEFAULT_ROWS,
                    columns = DEFAULT_COLUMNS,
                    isDefault = false,
                    seedKey = seed.seedKey,
                ),
            )
            val boardIdsBySeedKey = boardDao.getAllBoards()
                .mapNotNull { entity -> entity.seedKey?.let { it to entity.id } }
                .toMap()
            seedBoardContent(id, seed, boardIdsBySeedKey, phraseDao, phraseGroupDao)
        }
    }

    private suspend fun ensureFolderCellsOnHome(
        boardDao: BoardDao,
        phraseDao: PhraseDao,
    ) {
        val home = boardDao.getDefaultBoard() ?: return
        val boardIdsBySeedKey = boardDao.getAllBoards()
            .mapNotNull { entity -> entity.seedKey?.let { it to entity.id } }
            .toMap()
        val homeSeed = starterBoards.first { it.isHome }
        var nextOrder = phraseDao.maxSortOrder(home.id) + 1
        homeSeed.phrases.filter { it.targetSeedKey != null }.forEach { folder ->
            val targetId = folder.targetSeedKey?.let { boardIdsBySeedKey[it] } ?: return@forEach
            if (phraseDao.countFolderCells(home.id, folder.label) > 0) return@forEach
            phraseDao.insert(
                PhraseEntity(
                    boardId = home.id,
                    label = folder.label,
                    spokenText = folder.spokenText,
                    sortOrder = nextOrder++,
                    iconPath = folder.iconPath,
                    targetBoardId = targetId,
                ),
            )
        }
    }

    private suspend fun backfillStarterPhraseIcons(
        boardDao: BoardDao,
        phraseDao: PhraseDao,
    ) {
        val boardsByKey = boardDao.getAllBoards()
            .mapNotNull { entity -> entity.seedKey?.let { it to entity } }
            .toMap()
        starterBoards.forEach { seed ->
            val board = boardsByKey[seed.seedKey] ?: return@forEach
            seed.phrases.forEach { phrase ->
                phraseDao.setIconPathIfMissing(
                    boardId = board.id,
                    label = phrase.label,
                    spokenText = phrase.spokenText,
                    iconPath = phrase.iconPath,
                )
            }
        }
    }
}
