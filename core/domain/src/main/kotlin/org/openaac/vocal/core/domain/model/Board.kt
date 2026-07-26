package org.openaac.vocal.core.domain.model

data class Board(
    val id: Long,
    val name: String,
    val rows: Int,
    val columns: Int,
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

data class Phrase(
    val id: Long,
    val boardId: Long,
    val label: String,
    val spokenText: String,
    val row: Int,
    val column: Int,
    val iconPath: String? = null,
    val audioPath: String? = null,
    /** Null when the phrase is ungrouped. At most one group per phrase. */
    val groupId: Long? = null,
)
