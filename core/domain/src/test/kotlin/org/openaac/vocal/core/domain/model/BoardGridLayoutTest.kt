package org.openaac.vocal.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardGridLayoutTest {

    @Test
    fun computeBoardGrid_onePhrase() {
        val grid = computeBoardGrid(1)
        assertEquals(BoardGridSpec(1, 1, 1), grid)
    }

    @Test
    fun computeBoardGrid_twoPhrases() {
        val grid = computeBoardGrid(2)
        assertEquals(BoardGridSpec(2, 1, 2), grid)
    }

    @Test
    fun computeBoardGrid_threePhrases() {
        val grid = computeBoardGrid(3)
        assertEquals(BoardGridSpec(2, 2, 4), grid)
    }

    @Test
    fun computeBoardGrid_fivePhrases() {
        val grid = computeBoardGrid(5)
        assertEquals(BoardGridSpec(4, 2, 8), grid)
    }

    @Test
    fun computeBoardGrid_eightPhrases() {
        val grid = computeBoardGrid(8)
        assertEquals(BoardGridSpec(4, 2, 8), grid)
    }

    @Test
    fun computeBoardGrid_ninePhrases() {
        val grid = computeBoardGrid(9)
        assertEquals(BoardGridSpec(4, 4, 16), grid)
    }

    @Test
    fun computeBoardGrid_sixteenPhrases() {
        val grid = computeBoardGrid(16)
        assertEquals(BoardGridSpec(4, 4, 16), grid)
    }

    @Test
    fun computeBoardGrid_seventeenPhrases() {
        val grid = computeBoardGrid(17)
        assertEquals(BoardGridSpec(8, 4, 32), grid)
    }

    @Test
    fun computeBoardGrid_thirtyOnePhrases() {
        val grid = computeBoardGrid(31)
        assertEquals(BoardGridSpec(8, 4, 32), grid)
    }

    @Test
    fun computeBoardGrid_thirtyTwoPhrases() {
        val grid = computeBoardGrid(32)
        assertEquals(BoardGridSpec(8, 4, 32), grid)
    }

    @Test
    fun computeBoardGrid_zeroPhrases() {
        val grid = computeBoardGrid(0)
        assertEquals(BoardGridSpec(1, 1, 1), grid)
    }

    @Test
    fun computeBoardGrid_clampsBelowZero() {
        val grid = computeBoardGrid(-1)
        assertEquals(BoardGridSpec(1, 1, 1), grid)
    }

    @Test
    fun computeBoardGrid_clampsAboveMax() {
        val grid = computeBoardGrid(40)
        assertEquals(BoardGridSpec(8, 4, 32), grid)
    }

    @Test
    fun computeBoardGrid_clampsAboveCustomMax() {
        val grid = computeBoardGrid(32, maxPhrases = 16)
        assertEquals(BoardGridSpec(4, 4, 16), grid)
    }

    @Test
    fun computeBoardGrid_totalSlotsNeverLessThanPhraseCount() {
        for (count in 1..32) {
            val grid = computeBoardGrid(count)
            assertTrue(grid.totalSlots >= count)
        }
    }
}
