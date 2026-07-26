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

    @Query("SELECT * FROM boards WHERE id = :id")
    fun observeBoard(id: Long): Flow<BoardEntity?>

    @Query("SELECT * FROM boards ORDER BY isDefault DESC, id ASC")
    fun observeAllBoards(): Flow<List<BoardEntity>>

    @Query("SELECT * FROM boards WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultBoard(): BoardEntity?

    @Query("SELECT * FROM boards WHERE id = :id")
    suspend fun getBoard(id: Long): BoardEntity?

    @Query("SELECT * FROM boards WHERE seedKey = :seedKey LIMIT 1")
    suspend fun getBoardBySeedKey(seedKey: String): BoardEntity?

    @Query("SELECT * FROM boards ORDER BY isDefault DESC, id ASC")
    suspend fun getAllBoards(): List<BoardEntity>

    @Query("SELECT COUNT(*) FROM boards")
    suspend fun countBoards(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(board: BoardEntity): Long

    @Update
    suspend fun update(board: BoardEntity)

    @Query("DELETE FROM boards WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM boards")
    suspend fun deleteAll()
}

@Dao
interface PhraseDao {
    @Query("SELECT * FROM phrases WHERE boardId = :boardId ORDER BY sortOrder, id")
    fun observePhrasesForBoard(boardId: Long): Flow<List<PhraseEntity>>

    @Query("SELECT * FROM phrases WHERE boardId = :boardId ORDER BY sortOrder, id")
    suspend fun getPhrasesForBoard(boardId: Long): List<PhraseEntity>

    @Query("SELECT * FROM phrases ORDER BY boardId, sortOrder, id")
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

    @Query("UPDATE phrases SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int)

    @Query("DELETE FROM phrases WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM phrases WHERE boardId = :boardId")
    suspend fun deleteAllForBoard(boardId: Long)

    @Query("DELETE FROM phrases")
    suspend fun deleteAll()

    @Query("SELECT iconPath FROM phrases WHERE iconPath IS NOT NULL")
    suspend fun getAllIconPaths(): List<String>

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM phrases WHERE boardId = :boardId")
    suspend fun maxSortOrder(boardId: Long): Int

    @Query(
        """
        SELECT COUNT(*) FROM phrases
        WHERE boardId = :boardId
            AND targetBoardId IS NOT NULL
            AND label = :label
        """,
    )
    suspend fun countFolderCells(boardId: Long, label: String): Int
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

    @Query("DELETE FROM phrase_groups WHERE boardId = :boardId")
    suspend fun deleteAllForBoard(boardId: Long)

    @Query("DELETE FROM phrase_groups")
    suspend fun deleteAll()
}
