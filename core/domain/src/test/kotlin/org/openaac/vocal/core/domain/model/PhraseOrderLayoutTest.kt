package org.openaac.vocal.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PhraseOrderLayoutTest {

    @Test
    fun orderedPhrasesForBoard_sortsBySortOrderThenId() {
        val phrases = listOf(
            phrase(id = 10, label = "Sad", sortOrder = 2),
            phrase(id = 11, label = "Water", sortOrder = 0),
            phrase(id = 12, label = "Yes", sortOrder = 1),
            phrase(id = 13, label = "Hungry", sortOrder = 1),
        )

        val ordered = orderedPhrasesForBoard(phrases)

        assertEquals(listOf("Water", "Yes", "Hungry", "Sad"), ordered.map { it.label })
    }

    @Test
    fun orderedPhrasesForBoard_emptyInput() {
        assertTrue(orderedPhrasesForBoard(emptyList()).isEmpty())
    }

    @Test
    fun columnMajorIndex_fillsTopToBottomThenLeftToRight() {
        // 2 rows × 3 columns:
        // (0,0)=0  (0,1)=2  (0,2)=4
        // (1,0)=1  (1,1)=3  (1,2)=5
        assertEquals(0, columnMajorIndex(row = 0, column = 0, rows = 2))
        assertEquals(1, columnMajorIndex(row = 1, column = 0, rows = 2))
        assertEquals(2, columnMajorIndex(row = 0, column = 1, rows = 2))
        assertEquals(3, columnMajorIndex(row = 1, column = 1, rows = 2))
        assertEquals(4, columnMajorIndex(row = 0, column = 2, rows = 2))
        assertEquals(5, columnMajorIndex(row = 1, column = 2, rows = 2))
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
            sortOrder = 0,
        )
        assertNull(phrase.groupId)
    }

    private fun phrase(
        id: Long,
        label: String,
        sortOrder: Int,
        groupId: Long? = null,
    ): Phrase = Phrase(
        id = id,
        boardId = 1,
        label = label,
        spokenText = label,
        sortOrder = sortOrder,
        groupId = groupId,
    )
}
