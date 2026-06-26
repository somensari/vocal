package org.openaac.vocal.core.domain.model

data class Board(
    val id: Long,
    val name: String,
    val rows: Int,
    val columns: Int,
)

data class Phrase(
    val id: Long,
    val boardId: Long,
    val label: String,
    val spokenText: String,
    val row: Int,
    val column: Int,
    val iconPath: String? = null,
    val audioPath: String? = null,
)
