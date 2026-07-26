package org.openaac.vocal.core.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.openaac.vocal.core.data.local.entity.BoardEntity
import org.openaac.vocal.core.data.local.entity.PhraseEntity
import org.openaac.vocal.core.data.local.entity.PhraseGroupEntity
import org.openaac.vocal.core.domain.model.Board
import org.openaac.vocal.core.domain.model.BoardSeedKeys
import org.openaac.vocal.core.domain.model.Phrase
import org.openaac.vocal.core.domain.model.PhraseGroup
import org.openaac.vocal.core.domain.model.isFolderCell

class MapperTest {

    @Test
    fun boardEntity_roundTripsToDomain() {
        val entity = BoardEntity(
            id = 1,
            name = "Home",
            rows = 4,
            columns = 4,
            isDefault = true,
            seedKey = BoardSeedKeys.HOME,
        )
        val domain = entity.toDomain()
        assertEquals(
            Board(
                id = 1,
                name = "Home",
                rows = 4,
                columns = 4,
                seedKey = BoardSeedKeys.HOME,
                isHome = true,
            ),
            domain,
        )
        assertEquals(entity, domain.toEntity(isDefault = true))
    }

    @Test
    fun board_toEntity_preservesIsDefaultFlag() {
        val board = Board(id = 2, name = "Alt", rows = 2, columns = 2)
        assertEquals(false, board.toEntity(isDefault = false).isDefault)
        assertEquals(true, board.toEntity(isDefault = true).isDefault)
    }

    @Test
    fun phraseGroupEntity_roundTripsToDomain() {
        val entity = PhraseGroupEntity(
            id = 3,
            boardId = 1,
            name = "Needs",
            colorIndex = 2,
            sortOrder = 1,
        )
        val domain = entity.toDomain()
        assertEquals(
            PhraseGroup(id = 3, boardId = 1, name = "Needs", colorIndex = 2, sortOrder = 1),
            domain,
        )
        assertEquals(entity, domain.toEntity())
    }

    @Test
    fun phraseEntity_roundTripsOptionalFields() {
        val entity = PhraseEntity(
            id = 10,
            boardId = 1,
            label = "Water",
            spokenText = "I want water",
            sortOrder = 1,
            iconPath = "vocal://bundled-icons/starter/water",
            audioPath = "/data/user/0/org.openaac.vocal/files/water.m4a",
            groupId = 5,
        )
        val domain = entity.toDomain()
        assertEquals(entity, domain.toEntity())
        assertEquals(5L, domain.groupId)
        assertEquals(1, domain.sortOrder)
        assertNull(domain.targetBoardId)
    }

    @Test
    fun phraseEntity_folderCellMapsTargetBoardId() {
        val entity = PhraseEntity(
            id = 11,
            boardId = 1,
            label = "Food & Drink",
            spokenText = "Food & Drink",
            sortOrder = 12,
            iconPath = "vocal://bundled-icons/starter/folder",
            targetBoardId = 2,
        )
        val domain = entity.toDomain()
        assertEquals(entity, domain.toEntity())
        assertEquals(2L, domain.targetBoardId)
        assertTrue(domain.isFolderCell)
    }

    @Test
    fun phraseEntity_nullOptionalsMapToNull() {
        val entity = PhraseEntity(
            id = 0,
            boardId = 1,
            label = "Yes",
            spokenText = "Yes",
            sortOrder = 0,
        )
        val domain = entity.toDomain()
        assertNull(domain.iconPath)
        assertNull(domain.audioPath)
        assertNull(domain.groupId)
        assertNull(domain.targetBoardId)
    }
}
