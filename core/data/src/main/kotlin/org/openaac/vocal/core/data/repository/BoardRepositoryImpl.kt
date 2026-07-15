package org.openaac.vocal.core.data.repository

import org.openaac.vocal.core.data.local.DefaultSeedData
import org.openaac.vocal.core.data.local.dao.BoardDao
import org.openaac.vocal.core.data.local.dao.PhraseDao
import org.openaac.vocal.core.data.local.dao.PhraseGroupDao
import org.openaac.vocal.core.data.mapper.toDomain
import org.openaac.vocal.core.data.mapper.toEntity
import org.openaac.vocal.core.domain.model.Board
import org.openaac.vocal.core.domain.model.Phrase
import org.openaac.vocal.core.domain.model.PhraseGroup
import org.openaac.vocal.core.domain.repository.BoardRepository
import org.openaac.vocal.core.domain.repository.PhraseGroupRepository
import org.openaac.vocal.core.domain.repository.PhraseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BoardRepositoryImpl @Inject constructor(
    private val boardDao: BoardDao,
    private val phraseDao: PhraseDao,
    private val phraseGroupDao: PhraseGroupDao,
) : BoardRepository {

    override fun observeDefaultBoard(): Flow<Board?> =
        boardDao.observeDefaultBoard().map { entity -> entity?.toDomain() }

    override fun observePhrases(boardId: Long): Flow<List<Phrase>> =
        phraseDao.observePhrasesForBoard(boardId).map { phrases ->
            phrases.map { it.toDomain() }
        }

    override suspend fun ensureDefaultBoard(): Board {
        val board = DefaultSeedData.ensureDefaultBoard(boardDao, phraseDao, phraseGroupDao)
        return board.toDomain()
    }

    override suspend fun updateBoard(board: Board) {
        val existing = boardDao.getBoard(board.id) ?: return
        boardDao.update(board.toEntity(isDefault = existing.isDefault))
    }
}

@Singleton
class PhraseRepositoryImpl @Inject constructor(
    private val phraseDao: PhraseDao,
    private val boardDao: BoardDao,
    private val phraseGroupDao: PhraseGroupDao,
) : PhraseRepository {

    override fun observeAllPhrases(): Flow<List<Phrase>> =
        phraseDao.observeAllPhrases().map { phrases -> phrases.map { it.toDomain() } }

    override suspend fun getPhrase(id: Long): Phrase? =
        phraseDao.getPhrase(id)?.toDomain()

    override suspend fun savePhrase(phrase: Phrase): Long {
        if (phrase.boardId == 0L) {
            val board = DefaultSeedData.ensureDefaultBoard(boardDao, phraseDao, phraseGroupDao)
            return phraseDao.insert(phrase.copy(boardId = board.id).toEntity())
        }
        return phraseDao.insert(phrase.toEntity())
    }

    override suspend fun deletePhrase(id: Long) {
        phraseDao.deleteById(id)
    }

    override suspend fun getAllIconPaths(): List<String> = phraseDao.getAllIconPaths()
}

@Singleton
class PhraseGroupRepositoryImpl @Inject constructor(
    private val phraseGroupDao: PhraseGroupDao,
    private val phraseDao: PhraseDao,
) : PhraseGroupRepository {

    override fun observeGroups(boardId: Long): Flow<List<PhraseGroup>> =
        phraseGroupDao.observeGroupsForBoard(boardId).map { groups ->
            groups.map { it.toDomain() }
        }

    override suspend fun getGroups(boardId: Long): List<PhraseGroup> =
        phraseGroupDao.getGroupsForBoard(boardId).map { it.toDomain() }

    override suspend fun getGroup(id: Long): PhraseGroup? =
        phraseGroupDao.getGroup(id)?.toDomain()

    override suspend fun countGroups(boardId: Long): Int =
        phraseGroupDao.countGroupsForBoard(boardId)

    override suspend fun saveGroup(group: PhraseGroup): Long {
        return if (group.id == 0L) {
            phraseGroupDao.insert(group.toEntity())
        } else {
            phraseGroupDao.update(group.toEntity())
            group.id
        }
    }

    override suspend fun deleteGroup(id: Long) {
        // Clear assignments first so phrases remain even if FK SET NULL is unavailable.
        phraseDao.clearGroupAssignments(id)
        phraseGroupDao.deleteById(id)
    }

    override suspend fun assignPhraseToGroup(phraseId: Long, groupId: Long?) {
        phraseDao.setGroupId(phraseId, groupId)
    }
}
