package org.openaac.vocal.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.openaac.vocal.core.data.local.entity.BoardEntity
import org.openaac.vocal.core.data.local.entity.PhraseEntity
import org.openaac.vocal.core.data.local.entity.PhraseGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BoardDao {
    @Query("SELECT * FROM boards WHERE isDefault = 1 LIMIT 1")
    fun observeDefaultBoard(): Flow<BoardEntity?>

    @Query("SELECT * FROM boards WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultBoard(): BoardEntity?

    @Query("SELECT * FROM boards WHERE id = :id")
    suspend fun getBoard(id: Long): BoardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(board: BoardEntity): Long

    @Update
    suspend fun update(board: BoardEntity)
}

@Dao
interface PhraseDao {
    @Query("SELECT * FROM phrases WHERE boardId = :boardId ORDER BY row, column")
    fun observePhrasesForBoard(boardId: Long): Flow<List<PhraseEntity>>

    @Query("SELECT * FROM phrases ORDER BY boardId, row, column")
    fun observeAllPhrases(): Flow<List<PhraseEntity>>

    @Query("SELECT * FROM phrases WHERE id = :id")
    suspend fun getPhrase(id: Long): PhraseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(phrase: PhraseEntity): Long

    @Query(
        """
        UPDATE phrases
        SET iconPath = :iconPath
        WHERE boardId = :boardId
            AND label = :label
            AND spokenText = :spokenText
            AND iconPath IS NULL
        """,
    )
    suspend fun setIconPathIfMissing(
        boardId: Long,
        label: String,
        spokenText: String,
        iconPath: String,
    )

    @Query("UPDATE phrases SET groupId = :groupId WHERE id = :phraseId")
    suspend fun setGroupId(phraseId: Long, groupId: Long?)

    @Query("UPDATE phrases SET groupId = NULL WHERE groupId = :groupId")
    suspend fun clearGroupAssignments(groupId: Long)

    @Query("DELETE FROM phrases WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT iconPath FROM phrases WHERE iconPath IS NOT NULL")
    suspend fun getAllIconPaths(): List<String>
}

@Dao
interface PhraseGroupDao {
    @Query("SELECT * FROM phrase_groups WHERE boardId = :boardId ORDER BY sortOrder, id")
    fun observeGroupsForBoard(boardId: Long): Flow<List<PhraseGroupEntity>>

    @Query("SELECT * FROM phrase_groups WHERE boardId = :boardId ORDER BY sortOrder, id")
    suspend fun getGroupsForBoard(boardId: Long): List<PhraseGroupEntity>

    @Query("SELECT * FROM phrase_groups WHERE id = :id")
    suspend fun getGroup(id: Long): PhraseGroupEntity?

    @Query("SELECT COUNT(*) FROM phrase_groups WHERE boardId = :boardId")
    suspend fun countGroupsForBoard(boardId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(group: PhraseGroupEntity): Long

    @Update
    suspend fun update(group: PhraseGroupEntity)

    @Query("DELETE FROM phrase_groups WHERE id = :id")
    suspend fun deleteById(id: Long)
}
