package org.openaac.vocal.core.domain.model

const val MAX_BOARD_PHRASES = 32

data class BoardGridSpec(
    val columns: Int,
    val rows: Int,
    val totalSlots: Int,
)

/**
 * Computes a power-of-two board grid by repeatedly doubling the shorter side
 * until there are enough slots for [phraseCount] phrases.
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
