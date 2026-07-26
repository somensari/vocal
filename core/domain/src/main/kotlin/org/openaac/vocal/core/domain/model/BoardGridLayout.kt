package org.openaac.vocal.core.domain.model

/** Maximum number of phrases supported by the board grid in v1. */
const val MAX_BOARD_PHRASES = 32

/** Describes the power-of-two dimensions needed to display a board's phrase slots. */
data class BoardGridSpec(
    val columns: Int,
    val rows: Int,
    val totalSlots: Int,
)

/**
 * Computes a power-of-two board grid by repeatedly doubling the shorter side
 * until there are enough slots for [phraseCount] phrases, clamped to [maxPhrases].
 */
fun computeBoardGrid(
    phraseCount: Int,
    maxPhrases: Int = MAX_BOARD_PHRASES,
): BoardGridSpec {
    val count = phraseCount.coerceIn(0, maxPhrases)
    if (count == 0) {
        return BoardGridSpec(columns = 1, rows = 1, totalSlots = 1)
    }

    var columns = 1
    var rows = 1
    while (columns * rows < count) {
        if (columns <= rows) {
            columns *= 2
        } else {
            rows *= 2
        }
    }

    return BoardGridSpec(
        columns = columns,
        rows = rows,
        totalSlots = columns * rows,
    )
}

/**
 * Orders phrases for flat-board display with same-group clustering.
 *
 * Layout rule (documented for caregivers/implementers):
 * 1. Groups appear in ascending [PhraseGroup.sortOrder] (then id).
 * 2. Within each group, phrases stay contiguous, ordered by stored column/row then id.
 * 3. Ungrouped phrases follow all grouped clusters, ordered the same way.
 * 4. Positions are assigned top-to-bottom, then left-to-right (column-major)
 *    into a [rows] × [columns] grid.
 *
 * The board remains a flat grid (no folder navigation or section headers).
 * Returned phrases carry updated [Phrase.row]/[Phrase.column] for rendering only.
 */
fun clusterPhrasesForBoard(
    phrases: List<Phrase>,
    groups: List<PhraseGroup>,
    columns: Int,
    rows: Int,
): List<Phrase> {
    if (phrases.isEmpty()) return emptyList()
    val safeColumns = columns.coerceAtLeast(1)
    val safeRows = rows.coerceAtLeast(1)
    val groupsById = groups.associateBy { it.id }
    val orderedGroups = groups.sortedWith(
        compareBy<PhraseGroup> { it.sortOrder }.thenBy { it.id },
    )

    // Match column-major fill so re-clustering preserves visual sequence.
    val withinGroupComparator = compareBy<Phrase> { it.column }
        .thenBy { it.row }
        .thenBy { it.id }

    val ordered = buildList {
        for (group in orderedGroups) {
            val members = phrases
                .filter { it.groupId == group.id }
                .sortedWith(withinGroupComparator)
            addAll(members)
        }
        val ungrouped = phrases
            .filter { phrase ->
                phrase.groupId == null || phrase.groupId !in groupsById
            }
            .sortedWith(withinGroupComparator)
        addAll(ungrouped)
    }

    return ordered.mapIndexed { index, phrase ->
        phrase.copy(
            row = index % safeRows,
            column = index / safeRows,
        )
    }
}
