package org.openaac.vocal.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.openaac.vocal.core.domain.model.Board
import org.openaac.vocal.core.domain.model.BoardThemePreset
import org.openaac.vocal.core.domain.model.MAX_PHRASE_GROUPS
import org.openaac.vocal.core.domain.model.Phrase
import org.openaac.vocal.core.domain.model.PhraseGroup
import org.openaac.vocal.core.domain.model.SymbolCacheMaxSizeMb
import org.openaac.vocal.core.domain.repository.BoardRepository
import org.openaac.vocal.core.domain.repository.MonitoringRepository
import org.openaac.vocal.core.domain.repository.PhraseGroupRepository
import org.openaac.vocal.core.domain.repository.PhraseRepository
import org.openaac.vocal.core.domain.repository.SpeechError
import org.openaac.vocal.core.domain.repository.SpeechRepository
import org.openaac.vocal.core.domain.repository.UserPreferencesRepository

class UseCasesTest {

    @Test
    fun speakPhraseUseCase_usesSpokenTextNotLabel() = runTest {
        val speech = FakeSpeechRepository()
        val monitoring = FakeMonitoringRepository()
        val phrase = Phrase(
            id = 1,
            boardId = 1,
            label = "Water",
            spokenText = "I want water",
            sortOrder = 0,
            audioPath = "/files/water.m4a",
        )

        SpeakPhraseUseCase(speech, monitoring).invoke(phrase)

        assertEquals("I want water", speech.lastText)
        assertEquals("/files/water.m4a", speech.lastAudioPath)
    }

    @Test
    fun savePhraseUseCase_delegatesToRepository() = runTest {
        val phrases = FakePhraseRepository()
        val monitoring = FakeMonitoringRepository()
        val phrase = Phrase(
            id = 0,
            boardId = 1,
            label = "Yes",
            spokenText = "Yes",
            sortOrder = 0,
        )

        val id = SavePhraseUseCase(phrases, monitoring).invoke(phrase)

        assertEquals(42L, id)
        assertEquals(phrase, phrases.lastSaved)
    }

    @Test
    fun deletePhraseUseCase_delegatesToRepository() = runTest {
        val phrases = FakePhraseRepository()
        val monitoring = FakeMonitoringRepository()

        DeletePhraseUseCase(phrases, monitoring).invoke(7L)

        assertEquals(7L, phrases.lastDeletedId)
    }

    @Test
    fun setBoardThemePresetUseCase_persistsPreset() = runTest {
        val prefs = FakeUserPreferencesRepository()
        val monitoring = FakeMonitoringRepository()

        SetBoardThemePresetUseCase(prefs, monitoring).invoke(BoardThemePreset.HighContrast)

        assertEquals(BoardThemePreset.HighContrast, prefs.lastPreset)
    }

    @Test
    fun ensureDefaultBoardUseCase_returnsBoardFromRepository() = runTest {
        val boards = FakeBoardRepository()
        val expected = Board(id = 1, name = "My Board", rows = 4, columns = 4)
        boards.defaultBoard = expected

        val board = EnsureDefaultBoardUseCase(boards).invoke()

        assertEquals(expected, board)
    }

    @Test
    fun observeBoardPhrasesUseCase_emitsPhrasesForBoardId() = runTest {
        val boards = FakeBoardRepository()
        val phrases = listOf(
            Phrase(id = 1, boardId = 2, label = "Yes", spokenText = "Yes", sortOrder = 0),
        )
        boards.phrasesByBoardId[2L] = phrases

        val result = ObserveBoardPhrasesUseCase(boards).invoke(2L).first()

        assertEquals(phrases, result)
    }

    @Test
    fun createPhraseGroupUseCase_blocksNinthGroup() = runTest {
        val groups = FakePhraseGroupRepository()
        groups.groups = (1..MAX_PHRASE_GROUPS).map { index ->
            PhraseGroup(
                id = index.toLong(),
                boardId = 1,
                name = "Group $index",
                colorIndex = index - 1,
                sortOrder = index - 1,
            )
        }

        val result = CreatePhraseGroupUseCase(groups).invoke(1L, "Overflow")

        assertEquals(CreatePhraseGroupResult.LimitReached, result)
    }

    @Test
    fun createPhraseGroupUseCase_assignsUnusedColorIndex() = runTest {
        val groups = FakePhraseGroupRepository()
        groups.groups = listOf(
            PhraseGroup(id = 1, boardId = 1, name = "A", colorIndex = 0, sortOrder = 0),
            PhraseGroup(id = 2, boardId = 1, name = "B", colorIndex = 2, sortOrder = 1),
        )

        val result = CreatePhraseGroupUseCase(groups).invoke(1L, "C")

        val created = result as CreatePhraseGroupResult.Created
        assertEquals(1, groups.lastSaved?.colorIndex)
        assertEquals(created.groupId, groups.lastSaved?.id)
    }

    @Test
    fun deletePhraseGroupUseCase_delegatesToRepository() = runTest {
        val groups = FakePhraseGroupRepository()

        DeletePhraseGroupUseCase(groups).invoke(9L)

        assertEquals(9L, groups.lastDeletedId)
    }

    private class FakeSpeechRepository : SpeechRepository {
        var lastText: String? = null
        var lastAudioPath: String? = null

        override suspend fun speak(text: String, audioPath: String?): SpeechError? {
            lastText = text
            lastAudioPath = audioPath
            return null
        }
    }

    private class FakeMonitoringRepository : MonitoringRepository {
        override fun recordHandledException(
            throwable: Throwable,
            attributes: Map<String, Any>,
        ) = Unit

        override fun recordBreadcrumb(
            name: String,
            attributes: Map<String, Any>,
        ) = Unit

        override fun recordCustomEvent(
            eventName: String,
            attributes: Map<String, Any>,
        ) = Unit

        override fun startInteraction(name: String): String? = "interaction"

        override fun endInteraction(interactionId: String?) = Unit

        override fun setInteractionName(name: String) = Unit

        override fun setSessionAttribute(name: String, value: String) = Unit

        override fun setSessionAttribute(name: String, value: Double) = Unit

        override fun setSessionAttribute(name: String, value: Boolean) = Unit

        override fun recordMetric(name: String, category: String, value: Double) = Unit

        override fun incrementSessionAttribute(name: String) = Unit
    }

    private class FakePhraseRepository : PhraseRepository {
        var lastSaved: Phrase? = null
        var lastDeletedId: Long? = null
        var lastReorderIds: List<Long>? = null

        override fun observeAllPhrases(): Flow<List<Phrase>> = flowOf(emptyList())

        override suspend fun getPhrase(id: Long): Phrase? = null

        override suspend fun savePhrase(phrase: Phrase): Long {
            lastSaved = phrase
            return 42L
        }

        override suspend fun deletePhrase(id: Long) {
            lastDeletedId = id
        }

        override suspend fun getAllIconPaths(): List<String> = emptyList()

        override suspend fun reorderPhrases(orderedPhraseIds: List<Long>) {
            lastReorderIds = orderedPhraseIds
        }
    }

    private class FakeUserPreferencesRepository : UserPreferencesRepository {
        var lastPreset: BoardThemePreset? = null
        var savedLastSelectedBoardId: Long? = null

        override val speechRate: Flow<Float> = flowOf(1f)

        override val boardThemePreset: Flow<BoardThemePreset> = flowOf(BoardThemePreset.Default)

        override val symbolCacheMaxSizeMb: Flow<SymbolCacheMaxSizeMb> =
            flowOf(SymbolCacheMaxSizeMb.Default)

        override val lastSelectedBoardId: Flow<Long?> = flowOf(null)

        override suspend fun setSpeechRate(rate: Float) = Unit

        override suspend fun setBoardThemePreset(preset: BoardThemePreset) {
            lastPreset = preset
        }

        override suspend fun setSymbolCacheMaxSizeMb(maxSize: SymbolCacheMaxSizeMb) = Unit

        override suspend fun setLastSelectedBoardId(boardId: Long?) {
            savedLastSelectedBoardId = boardId
        }
    }

    private class FakeBoardRepository : BoardRepository {
        var defaultBoard: Board? = null
        val boardsById = mutableMapOf<Long, Board>()
        val phrasesByBoardId = mutableMapOf<Long, List<Phrase>>()

        override fun observeDefaultBoard(): Flow<Board?> = flowOf(defaultBoard)

        override fun observeBoard(boardId: Long): Flow<Board?> =
            flowOf(boardsById[boardId] ?: defaultBoard?.takeIf { it.id == boardId })

        override fun observeAllBoards(): Flow<List<Board>> =
            flowOf(listOfNotNull(defaultBoard) + boardsById.values.filter { it.id != defaultBoard?.id })

        override fun observePhrases(boardId: Long): Flow<List<Phrase>> =
            flowOf(phrasesByBoardId[boardId].orEmpty())

        override suspend fun ensureDefaultBoard(): Board =
            defaultBoard ?: error("default board not set")

        override suspend fun getBoard(boardId: Long): Board? =
            boardsById[boardId] ?: defaultBoard?.takeIf { it.id == boardId }

        override suspend fun updateBoard(board: Board) = Unit

        override suspend fun resetToStarterBoard() = Unit
    }

    private class FakePhraseGroupRepository : PhraseGroupRepository {
        var groups: List<PhraseGroup> = emptyList()
        var lastSaved: PhraseGroup? = null
        var lastDeletedId: Long? = null
        private var nextId = 100L

        override fun observeGroups(boardId: Long): Flow<List<PhraseGroup>> =
            flowOf(groups.filter { it.boardId == boardId })

        override suspend fun getGroups(boardId: Long): List<PhraseGroup> =
            groups.filter { it.boardId == boardId }

        override suspend fun getGroup(id: Long): PhraseGroup? =
            groups.firstOrNull { it.id == id }

        override suspend fun countGroups(boardId: Long): Int =
            groups.count { it.boardId == boardId }

        override suspend fun saveGroup(group: PhraseGroup): Long {
            val id = if (group.id == 0L) nextId++ else group.id
            lastSaved = group.copy(id = id)
            return id
        }

        override suspend fun deleteGroup(id: Long) {
            lastDeletedId = id
        }

        override suspend fun assignPhraseToGroup(phraseId: Long, groupId: Long?) = Unit
    }
}
