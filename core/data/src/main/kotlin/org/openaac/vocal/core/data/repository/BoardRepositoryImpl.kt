package org.openaac.vocal.core.data.repository

import android.content.Context
import androidx.room.withTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import org.openaac.vocal.core.data.local.DefaultSeedData
import org.openaac.vocal.core.data.local.VocalDatabase
import org.openaac.vocal.core.data.local.dao.BoardDao
import org.openaac.vocal.core.data.local.dao.PhraseDao
import org.openaac.vocal.core.data.local.dao.PhraseGroupDao
import org.openaac.vocal.core.data.local.entity.PhraseEntity
import org.openaac.vocal.core.data.mapper.toDomain
import org.openaac.vocal.core.data.mapper.toEntity
import org.openaac.vocal.core.domain.model.Board
import org.openaac.vocal.core.domain.model.BundledPhraseIcons
import org.openaac.vocal.core.domain.model.Phrase
import org.openaac.vocal.core.domain.model.PhraseGroup
import org.openaac.vocal.core.domain.model.SymbolCachePaths
import org.openaac.vocal.core.domain.repository.BoardRepository
import org.openaac.vocal.core.domain.repository.PhraseGroupRepository
import org.openaac.vocal.core.domain.repository.PhraseRepository
import org.openaac.vocal.core.domain.repository.SymbolCacheRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BoardRepositoryImpl @Inject constructor(
    private val database: VocalDatabase,
    private val boardDao: BoardDao,
    private val phraseDao: PhraseDao,
    private val phraseGroupDao: PhraseGroupDao,
    private val symbolCacheRepository: SymbolCacheRepository,
    @ApplicationContext private val context: Context,
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

    override suspend fun resetToStarterBoard() {
        database.withTransaction {
            val board = DefaultSeedData.ensureDefaultBoard(boardDao, phraseDao, phraseGroupDao)
            val existingPhrases = phraseDao.getPhrasesForBoard(board.id)
            deleteCustomPhraseMedia(existingPhrases)

            // Clear assignments then delete groups/phrases so FK SET NULL is not needed mid-wipe.
            phraseDao.deleteAllForBoard(board.id)
            phraseGroupDao.deleteAllForBoard(board.id)

            DefaultSeedData.seedStarterContent(board.id, phraseDao, phraseGroupDao)
        }

        // Drop orphaned SymboTalk cache files that belonged to removed custom phrases.
        val referenced = phraseDao.getAllIconPaths()
        symbolCacheRepository.deleteUnreferencedCache(referenced)
    }

    private fun deleteCustomPhraseMedia(phrases: List<PhraseEntity>) {
        phrases.forEach { phrase ->
            deleteLocalIconFile(phrase.iconPath)
            deleteLocalAudioFile(phrase.audioPath)
        }
    }

    private fun deleteLocalIconFile(iconPath: String?) {
        if (iconPath.isNullOrBlank() || BundledPhraseIcons.isBundledPath(iconPath)) return
        if (SymbolCachePaths.isCachedSymbolPath(iconPath)) {
            symbolCacheRepository.resolveLocalFilePath(iconPath)
                ?.let { path -> File(path).takeIf { it.isFile }?.delete() }
            return
        }
        // Absolute custom file paths (rare); never delete outside the app files tree.
        val file = File(iconPath)
        val filesDir = context.filesDir.absolutePath
        if (file.isFile && file.absolutePath.startsWith(filesDir)) {
            file.delete()
        }
    }

    private fun deleteLocalAudioFile(audioPath: String?) {
        if (audioPath.isNullOrBlank()) return
        val file = File(audioPath)
        val filesDir = context.filesDir.absolutePath
        if (file.isFile && file.absolutePath.startsWith(filesDir)) {
            file.delete()
        }
    }
}

@Singleton
class PhraseRepositoryImpl @Inject constructor(
    private val database: VocalDatabase,
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
            val withBoard = phrase.copy(boardId = board.id)
            return insertWithSortOrder(withBoard)
        }
        return insertWithSortOrder(phrase)
    }

    private suspend fun insertWithSortOrder(phrase: Phrase): Long {
        val toSave = if (phrase.id == 0L) {
            val nextOrder = phraseDao.maxSortOrder(phrase.boardId) + 1
            phrase.copy(sortOrder = nextOrder)
        } else {
            phrase
        }
        return phraseDao.insert(toSave.toEntity())
    }

    override suspend fun deletePhrase(id: Long) {
        phraseDao.deleteById(id)
    }

    override suspend fun getAllIconPaths(): List<String> = phraseDao.getAllIconPaths()

    override suspend fun reorderPhrases(orderedPhraseIds: List<Long>) {
        database.withTransaction {
            orderedPhraseIds.forEachIndexed { index, id ->
                phraseDao.updateSortOrder(id, index)
            }
        }
    }
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
