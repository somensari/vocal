package org.openaac.vocal.core.domain.model

/** Maximum number of boards supported in v1 (Home + up to 3 topic boards). */
const val MAX_BOARDS = 4

/**
 * Stable seed identities for the v1 Home + folder board set.
 *
 * Used to find and re-seed boards without relying on display names alone.
 */
object BoardSeedKeys {
    const val HOME = "home"
    const val FOOD_DRINK = "food_drink"
    const val FEELINGS = "feelings"
    const val PEOPLE = "people"

    val ALL = listOf(HOME, FOOD_DRINK, FEELINGS, PEOPLE)
}

data class Board(
    val id: Long,
    val name: String,
    val rows: Int,
    val columns: Int,
    /** Stable seed key such as [BoardSeedKeys.HOME], or null for future custom boards. */
    val seedKey: String? = null,
    /** True for the Home entry board (folder navigation root). */
    val isHome: Boolean = false,
)

/** Maximum number of phrase groups allowed on a single board. */
const val MAX_PHRASE_GROUPS = 8

/**
 * A flat topic group for related phrases on a board.
 *
 * Groups are not nested. [colorIndex] indexes a fixed palette of up to
 * [MAX_PHRASE_GROUPS] subtle background tints (0-based).
 */
data class PhraseGroup(
    val id: Long,
    val boardId: Long,
    val name: String,
    val colorIndex: Int,
    val sortOrder: Int,
)

/**
 * A cell on a board: either a speakable AAC phrase or a folder that opens another board.
 *
 * [sortOrder] is the sole caregiver-defined placement input. The main board
 * fills the auto grid top-to-bottom, then left-to-right (column-major) by this
 * order. Groups only tint cells; they do not reorder placement.
 *
 * When [targetBoardId] is non-null, tapping navigates to that board and does
 * **not** speak. Folder vs phrase is distinguished by icon/label, not color alone.
 */
data class Phrase(
    val id: Long,
    val boardId: Long,
    val label: String,
    val spokenText: String,
    val sortOrder: Int,
    val iconPath: String? = null,
    val audioPath: String? = null,
    /** Null when the phrase is ungrouped. At most one group per phrase. */
    val groupId: Long? = null,
    /**
     * When non-null, this cell is a folder that opens the given board id.
     * Speakable phrases leave this null.
     */
    val targetBoardId: Long? = null,
)

/** True when this cell navigates to another board instead of speaking. */
val Phrase.isFolderCell: Boolean
    get() = targetBoardId != null
