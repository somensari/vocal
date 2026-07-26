package org.openaac.vocal.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "boards",
)
data class BoardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val rows: Int,
    val columns: Int,
    val isDefault: Boolean = false,
    /** Stable seed key (home, food_drink, …); null for future custom boards. */
    val seedKey: String? = null,
)

@Entity(
    tableName = "phrase_groups",
    foreignKeys = [
        ForeignKey(
            entity = BoardEntity::class,
            parentColumns = ["id"],
            childColumns = ["boardId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("boardId")],
)
data class PhraseGroupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val boardId: Long,
    val name: String,
    val colorIndex: Int,
    val sortOrder: Int,
)

@Entity(
    tableName = "phrases",
    foreignKeys = [
        ForeignKey(
            entity = BoardEntity::class,
            parentColumns = ["id"],
            childColumns = ["boardId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = PhraseGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = BoardEntity::class,
            parentColumns = ["id"],
            childColumns = ["targetBoardId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("boardId"), Index("groupId"), Index("targetBoardId")],
)
data class PhraseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val boardId: Long,
    val label: String,
    val spokenText: String,
    val sortOrder: Int,
    val iconPath: String? = null,
    val audioPath: String? = null,
    val groupId: Long? = null,
    /** Non-null for folder cells that open another board instead of speaking. */
    val targetBoardId: Long? = null,
)
