package org.openaac.vocal.core.data

import org.openaac.vocal.core.data.local.DefaultSeedData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.openaac.vocal.core.domain.model.BundledPhraseIcons
import org.openaac.vocal.core.domain.model.MAX_BOARD_PHRASES

class DefaultSeedDataTest {
    @Test
    fun starterBoardHasThirtyTwoPhrases() {
        assertEquals(MAX_BOARD_PHRASES, DefaultSeedData.starterPhrases.size)
    }

    @Test
    fun starterPhrasesUseBundledIcons() {
        DefaultSeedData.starterPhrases.forEach { seed ->
            assertTrue(
                "Expected bundled icon for ${seed.label}",
                BundledPhraseIcons.isBundledPath(seed.iconPath),
            )
        }
    }

    @Test
    fun starterPhrasePositionsFitEightByFourGrid() {
        DefaultSeedData.starterPhrases.forEach { seed ->
            assertTrue(seed.row in 0 until DefaultSeedData.DEFAULT_ROWS)
            assertTrue(seed.column in 0 until DefaultSeedData.DEFAULT_COLUMNS)
        }
        val uniqueSlots = DefaultSeedData.starterPhrases.map { it.row to it.column }.toSet()
        assertEquals(MAX_BOARD_PHRASES, uniqueSlots.size)
    }
}
