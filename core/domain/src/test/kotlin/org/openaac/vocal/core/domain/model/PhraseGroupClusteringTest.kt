package org.openaac.vocal.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PhraseGroupClusteringTest {

    @Test
    fun clusterPhrasesForBoard_groupsContiguousThenUngrouped() {
        val groups = listOf(
            PhraseGroup(id = 2, boardId = 1, name = "Feelings", colorIndex = 1, sortOrder = 1),
            PhraseGroup(id = 1, boardId = 1, name = "Needs", colorIndex = 0, sortOrder = 0),
        )
        val phrases = listOf(
            phrase(id = 10, label = "Sad", row = 0, column = 1, groupId = 2),
            phrase(id = 11, label = "Water", row = 1, column = 0, groupId = 1),
            phrase(id = 12, label = "Yes", row = 0, column = 0, groupId = null),
            phrase(id = 13, label = "Hungry", row = 0, column = 0, groupId = 1),
            phrase(id = 14, label = "Happy", row = 0, column = 0, groupId = 2),
        )

        val clustered = clusterPhrasesForBoard(phrases, groups, columns = 4)

        assertEquals(
            listOf("Hungry", "Water", "Happy", "Sad", "Yes"),
            clustered.map { it.label },
        )
        assertEquals(0, clustered[0].row)
        assertEquals(0, clustered[0].column)
        assertEquals(0, clustered[3].row)
        assertEquals(3, clustered[3].column)
        assertEquals(1, clustered[4].row)
        assertEquals(0, clustered[4].column)
    }

    @Test
    fun clusterPhrasesForBoard_orphanGroupIdTreatedAsUngrouped() {
        val groups = listOf(
            PhraseGroup(id = 1, boardId = 1, name = "Needs", colorIndex = 0, sortOrder = 0),
        )
        val phrases = listOf(
            phrase(id = 1, label = "Water", row = 0, column = 0, groupId = 1),
            phrase(id = 2, label = "Orphan", row = 0, column = 1, groupId = 99),
        )

        val clustered = clusterPhrasesForBoard(phrases, groups, columns = 2)

        assertEquals(listOf("Water", "Orphan"), clustered.map { it.label })
        assertEquals(1L, clustered[0].groupId)
        assertEquals(99L, clustered[1].groupId)
    }

    @Test
    fun clusterPhrasesForBoard_emptyInput() {
        assertTrue(clusterPhrasesForBoard(emptyList(), emptyList(), columns = 4).isEmpty())
    }

    @Test
    fun maxPhraseGroupsConstant() {
        assertEquals(8, MAX_PHRASE_GROUPS)
    }

    @Test
    fun phrase_defaultGroupIdIsNull() {
        val phrase = Phrase(
            id = 1,
            boardId = 1,
            label = "Yes",
            spokenText = "Yes",
            row = 0,
            column = 0,
        )
        assertNull(phrase.groupId)
    }

    private fun phrase(
        id: Long,
        label: String,
        row: Int,
        column: Int,
        groupId: Long?,
    ): Phrase = Phrase(
        id = id,
        boardId = 1,
        label = label,
        spokenText = label,
        row = row,
        column = column,
        groupId = groupId,
    )
}
