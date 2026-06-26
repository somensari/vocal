package org.openaac.vocal.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.openaac.vocal.core.data.local.entity.BoardEntity
import org.openaac.vocal.core.data.local.entity.PhraseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BoardDao {
    @Query("SELECT * FROM boards WHERE isDefault = 1 LIMIT 1")
    fun observeDefaultBoard(): Flow<BoardEntity?>

    @Query("SELECT * FROM boards WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultBoard(): BoardEntity?

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

    @Query("DELETE FROM phrases WHERE id = :id")
    suspend fun deleteById(id: Long)
}
