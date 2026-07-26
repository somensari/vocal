package org.openaac.vocal.core.data

import org.openaac.vocal.core.data.local.DefaultSeedData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.openaac.vocal.core.domain.model.BoardSeedKeys
import org.openaac.vocal.core.domain.model.BundledPhraseIcons
import org.openaac.vocal.core.domain.model.MAX_BOARD_PHRASES
import org.openaac.vocal.core.domain.model.MAX_BOARDS
import org.openaac.vocal.core.domain.model.MAX_PHRASE_GROUPS

class DefaultSeedDataTest {
    @Test
    fun starterSetHasTwoToFourBoards() {
        assertTrue(DefaultSeedData.starterBoards.size in 2..MAX_BOARDS)
        assertEquals(BoardSeedKeys.ALL.toSet(), DefaultSeedData.starterBoards.map { it.seedKey }.toSet())
        assertEquals(1, DefaultSeedData.starterBoards.count { it.isHome })
    }

    @Test
    fun starterSpeakablePhrasesFitPerBoardCap() {
        DefaultSeedData.starterBoards.forEach { board ->
            assertTrue(
                "${board.name} exceeds phrase cap",
                board.phrases.size <= MAX_BOARD_PHRASES,
            )
        }
        assertEquals(32, DefaultSeedData.starterPhrases.size)
    }

    @Test
    fun homeBoardHasFolderCellsToTopicBoards() {
        val home = DefaultSeedData.starterBoards.first { it.isHome }
        val folders = home.phrases.filter { it.targetSeedKey != null }
        assertEquals(3, folders.size)
        assertEquals(
            setOf(BoardSeedKeys.FOOD_DRINK, BoardSeedKeys.FEELINGS, BoardSeedKeys.PEOPLE),
            folders.map { it.targetSeedKey }.toSet(),
        )
        folders.forEach { folder ->
            assertEquals(BundledPhraseIcons.FOLDER, folder.iconPath)
        }
    }

    @Test
    fun topicBoardsHaveNoNestedFolders() {
        DefaultSeedData.starterBoards.filter { !it.isHome }.forEach { board ->
            assertTrue(
                "${board.name} must not nest folders",
                board.phrases.none { it.targetSeedKey != null },
            )
        }
    }

    @Test
    fun starterPhrasesUseBundledIcons() {
        DefaultSeedData.starterBoards.forEach { board ->
            board.phrases.forEach { seed ->
                assertTrue(
                    "Expected bundled icon for ${seed.label}",
                    BundledPhraseIcons.isBundledPath(seed.iconPath),
                )
            }
        }
    }

    @Test
    fun starterSpeakablePhrasesArePreGrouped() {
        DefaultSeedData.starterBoards.forEach { board ->
            val groupNames = board.groups.map { it.name }.toSet()
            assertTrue(board.groups.size in 1..MAX_PHRASE_GROUPS)
            board.phrases.filter { it.targetSeedKey == null }.forEach { seed ->
                assertTrue(
                    "Expected group for ${seed.label}",
                    seed.groupName != null && seed.groupName in groupNames,
                )
            }
        }
    }

    @Test
    fun starterGroupColorIndicesAreDistinctAndInPalettePerBoard() {
        DefaultSeedData.starterBoards.forEach { board ->
            val indices = board.groups.map { it.colorIndex }
            assertEquals(indices.toSet().size, indices.size)
            indices.forEach { index ->
                assertTrue(index in 0 until MAX_PHRASE_GROUPS)
            }
        }
    }
}
