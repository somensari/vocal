package org.openaac.vocal.core.data.repository

import org.openaac.vocal.core.data.local.DefaultSeedData
import org.openaac.vocal.core.data.local.dao.BoardDao
import org.openaac.vocal.core.data.local.dao.PhraseDao
import org.openaac.vocal.core.data.mapper.toDomain
import org.openaac.vocal.core.data.mapper.toEntity
import org.openaac.vocal.core.domain.model.Board
import org.openaac.vocal.core.domain.model.Phrase
import org.openaac.vocal.core.domain.repository.BoardRepository
import org.openaac.vocal.core.domain.repository.PhraseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BoardRepositoryImpl @Inject constructor(
    private val boardDao: BoardDao,
    private val phraseDao: PhraseDao,
) : BoardRepository {

    override fun observeDefaultBoard(): Flow<Board?> =
        boardDao.observeDefaultBoard().map { entity -> entity?.toDomain() }

    override fun observePhrases(boardId: Long): Flow<List<Phrase>> =
        phraseDao.observePhrasesForBoard(boardId).map { phrases ->
            phrases.map { it.toDomain() }
        }

    override suspend fun ensureDefaultBoard(): Board {
        val board = DefaultSeedData.ensureDefaultBoard(boardDao, phraseDao)
        return board.toDomain()
    }
}

@Singleton
class PhraseRepositoryImpl @Inject constructor(
    private val phraseDao: PhraseDao,
    private val boardDao: BoardDao,
) : PhraseRepository {

    override fun observeAllPhrases(): Flow<List<Phrase>> =
        phraseDao.observeAllPhrases().map { phrases -> phrases.map { it.toDomain() } }

    override suspend fun getPhrase(id: Long): Phrase? =
        phraseDao.getPhrase(id)?.toDomain()

    override suspend fun savePhrase(phrase: Phrase): Long {
        if (phrase.boardId == 0L) {
            val board = DefaultSeedData.ensureDefaultBoard(boardDao, phraseDao)
            return phraseDao.insert(phrase.copy(boardId = board.id).toEntity())
        }
        return phraseDao.insert(phrase.toEntity())
    }

    override suspend fun deletePhrase(id: Long) {
        phraseDao.deleteById(id)
    }
}
