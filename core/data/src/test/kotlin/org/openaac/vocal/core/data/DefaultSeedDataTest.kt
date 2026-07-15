package org.openaac.vocal.core.data

import org.openaac.vocal.core.data.local.DefaultSeedData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.openaac.vocal.core.domain.model.BundledPhraseIcons
import org.openaac.vocal.core.domain.model.MAX_BOARD_PHRASES
import org.openaac.vocal.core.domain.model.MAX_PHRASE_GROUPS

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
    fun starterPhrasesArePreGrouped() {
        val groupNames = DefaultSeedData.starterGroups.map { it.name }.toSet()
        assertTrue(DefaultSeedData.starterGroups.size in 1..MAX_PHRASE_GROUPS)
        DefaultSeedData.starterPhrases.forEach { seed ->
            assertTrue(
                "Expected group for ${seed.label}",
                seed.groupName != null && seed.groupName in groupNames,
            )
        }
    }

    @Test
    fun starterGroupColorIndicesAreDistinctAndInPalette() {
        val indices = DefaultSeedData.starterGroups.map { it.colorIndex }
        assertEquals(indices.toSet().size, indices.size)
        indices.forEach { index ->
            assertTrue(index in 0 until MAX_PHRASE_GROUPS)
        }
    }
}
