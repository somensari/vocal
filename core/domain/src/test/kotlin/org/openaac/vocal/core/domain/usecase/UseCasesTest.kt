package org.openaac.vocal.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.openaac.vocal.core.domain.model.Board
import org.openaac.vocal.core.domain.model.BoardThemePreset
import org.openaac.vocal.core.domain.model.Phrase
import org.openaac.vocal.core.domain.repository.BoardRepository
import org.openaac.vocal.core.domain.repository.PhraseRepository
import org.openaac.vocal.core.domain.repository.SpeechError
import org.openaac.vocal.core.domain.repository.SpeechRepository
import org.openaac.vocal.core.domain.repository.UserPreferencesRepository

class UseCasesTest {

    @Test
    fun speakPhraseUseCase_usesSpokenTextNotLabel() = runTest {
        val speech = FakeSpeechRepository()
        val phrase = Phrase(
            id = 1,
            boardId = 1,
            label = "Water",
            spokenText = "I want water",
            row = 0,
            column = 0,
            audioPath = "/files/water.m4a",
        )

        SpeakPhraseUseCase(speech).invoke(phrase)

        assertEquals("I want water", speech.lastText)
        assertEquals("/files/water.m4a", speech.lastAudioPath)
    }

    @Test
    fun savePhraseUseCase_delegatesToRepository() = runTest {
        val phrases = FakePhraseRepository()
        val phrase = Phrase(
            id = 0,
            boardId = 1,
            label = "Yes",
            spokenText = "Yes",
            row = 0,
            column = 0,
        )

        val id = SavePhraseUseCase(phrases).invoke(phrase)

        assertEquals(42L, id)
        assertEquals(phrase, phrases.lastSaved)
    }

    @Test
    fun deletePhraseUseCase_delegatesToRepository() = runTest {
        val phrases = FakePhraseRepository()

        DeletePhraseUseCase(phrases).invoke(7L)

        assertEquals(7L, phrases.lastDeletedId)
    }

    @Test
    fun setBoardThemePresetUseCase_persistsPreset() = runTest {
        val prefs = FakeUserPreferencesRepository()

        SetBoardThemePresetUseCase(prefs).invoke(BoardThemePreset.HighContrast)

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
            Phrase(id = 1, boardId = 2, label = "Yes", spokenText = "Yes", row = 0, column = 0),
        )
        boards.phrasesByBoardId[2L] = phrases

        val result = ObserveBoardPhrasesUseCase(boards).invoke(2L).first()

        assertEquals(phrases, result)
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

    private class FakePhraseRepository : PhraseRepository {
        var lastSaved: Phrase? = null
        var lastDeletedId: Long? = null

        override fun observeAllPhrases(): Flow<List<Phrase>> = flowOf(emptyList())

        override suspend fun getPhrase(id: Long): Phrase? = null

        override suspend fun savePhrase(phrase: Phrase): Long {
            lastSaved = phrase
            return 42L
        }

        override suspend fun deletePhrase(id: Long) {
            lastDeletedId = id
        }
    }

    private class FakeUserPreferencesRepository : UserPreferencesRepository {
        var lastPreset: BoardThemePreset? = null

        override val speechRate: Flow<Float> = flowOf(1f)

        override val boardThemePreset: Flow<BoardThemePreset> = flowOf(BoardThemePreset.Default)

        override suspend fun setSpeechRate(rate: Float) = Unit

        override suspend fun setBoardThemePreset(preset: BoardThemePreset) {
            lastPreset = preset
        }
    }

    private class FakeBoardRepository : BoardRepository {
        var defaultBoard: Board? = null
        val phrasesByBoardId = mutableMapOf<Long, List<Phrase>>()

        override fun observeDefaultBoard(): Flow<Board?> = flowOf(defaultBoard)

        override fun observePhrases(boardId: Long): Flow<List<Phrase>> =
            flowOf(phrasesByBoardId[boardId].orEmpty())

        override suspend fun ensureDefaultBoard(): Board =
            defaultBoard ?: error("default board not set")

        override suspend fun updateBoard(board: Board) = Unit
    }
}
