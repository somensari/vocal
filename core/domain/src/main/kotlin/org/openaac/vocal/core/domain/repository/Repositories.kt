package org.openaac.vocal.core.domain.repository

import org.openaac.vocal.core.domain.model.Board
import org.openaac.vocal.core.domain.model.Phrase
import kotlinx.coroutines.flow.Flow

interface BoardRepository {
    fun observeDefaultBoard(): Flow<Board?>
    fun observePhrases(boardId: Long): Flow<List<Phrase>>
    suspend fun ensureDefaultBoard(): Board
}

interface PhraseRepository {
    fun observeAllPhrases(): Flow<List<Phrase>>
    suspend fun getPhrase(id: Long): Phrase?
    suspend fun savePhrase(phrase: Phrase): Long
    suspend fun deletePhrase(id: Long)
}

interface SpeechRepository {
    suspend fun speak(text: String, audioPath: String?)
}

interface UserPreferencesRepository {
    val speechRate: Flow<Float>
    suspend fun setSpeechRate(rate: Float)
}
