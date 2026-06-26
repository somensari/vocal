package org.openaac.vocal.core.data.mapper

import org.openaac.vocal.core.data.local.entity.BoardEntity
import org.openaac.vocal.core.data.local.entity.PhraseEntity
import org.openaac.vocal.core.domain.model.Board
import org.openaac.vocal.core.domain.model.Phrase

internal fun BoardEntity.toDomain(): Board = Board(
    id = id,
    name = name,
    rows = rows,
    columns = columns,
)

internal fun PhraseEntity.toDomain(): Phrase = Phrase(
    id = id,
    boardId = boardId,
    label = label,
    spokenText = spokenText,
    row = row,
    column = column,
    iconPath = iconPath,
    audioPath = audioPath,
)

internal fun Phrase.toEntity(): PhraseEntity = PhraseEntity(
    id = id,
    boardId = boardId,
    label = label,
    spokenText = spokenText,
    row = row,
    column = column,
    iconPath = iconPath,
    audioPath = audioPath,
)
