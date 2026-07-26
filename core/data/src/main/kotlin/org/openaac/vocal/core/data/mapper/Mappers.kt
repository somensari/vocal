package org.openaac.vocal.core.data.mapper

import org.openaac.vocal.core.data.local.entity.BoardEntity
import org.openaac.vocal.core.data.local.entity.PhraseEntity
import org.openaac.vocal.core.data.local.entity.PhraseGroupEntity
import org.openaac.vocal.core.domain.model.Board
import org.openaac.vocal.core.domain.model.Phrase
import org.openaac.vocal.core.domain.model.PhraseGroup

internal fun BoardEntity.toDomain(): Board = Board(
    id = id,
    name = name,
    rows = rows,
    columns = columns,
    seedKey = seedKey,
    isHome = isDefault,
)

internal fun Board.toEntity(isDefault: Boolean = isHome): BoardEntity = BoardEntity(
    id = id,
    name = name,
    rows = rows,
    columns = columns,
    isDefault = isDefault,
    seedKey = seedKey,
)

internal fun PhraseGroupEntity.toDomain(): PhraseGroup = PhraseGroup(
    id = id,
    boardId = boardId,
    name = name,
    colorIndex = colorIndex,
    sortOrder = sortOrder,
)

internal fun PhraseGroup.toEntity(): PhraseGroupEntity = PhraseGroupEntity(
    id = id,
    boardId = boardId,
    name = name,
    colorIndex = colorIndex,
    sortOrder = sortOrder,
)

internal fun PhraseEntity.toDomain(): Phrase = Phrase(
    id = id,
    boardId = boardId,
    label = label,
    spokenText = spokenText,
    sortOrder = sortOrder,
    iconPath = iconPath,
    audioPath = audioPath,
    groupId = groupId,
    targetBoardId = targetBoardId,
)

internal fun Phrase.toEntity(): PhraseEntity = PhraseEntity(
    id = id,
    boardId = boardId,
    label = label,
    spokenText = spokenText,
    sortOrder = sortOrder,
    iconPath = iconPath,
    audioPath = audioPath,
    groupId = groupId,
    targetBoardId = targetBoardId,
)
