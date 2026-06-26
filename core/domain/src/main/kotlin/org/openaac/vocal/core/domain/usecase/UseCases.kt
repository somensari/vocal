package org.openaac.vocal.core.domain.usecase

import org.openaac.vocal.core.domain.model.Board
import org.openaac.vocal.core.domain.model.Phrase
import org.openaac.vocal.core.domain.repository.BoardRepository
import org.openaac.vocal.core.domain.repository.PhraseRepository
import org.openaac.vocal.core.domain.repository.SpeechRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveBoardUseCase @Inject constructor(
    private val boardRepository: BoardRepository,
) {
    operator fun invoke(): Flow<Board?> = boardRepository.observeDefaultBoard()
}

class ObserveBoardPhrasesUseCase @Inject constructor(
    private val boardRepository: BoardRepository,
) {
    operator fun invoke(boardId: Long): Flow<List<Phrase>> =
        boardRepository.observePhrases(boardId)
}

class EnsureDefaultBoardUseCase @Inject constructor(
    private val boardRepository: BoardRepository,
) {
    suspend operator fun invoke(): Board = boardRepository.ensureDefaultBoard()
}

class SpeakPhraseUseCase @Inject constructor(
    private val speechRepository: SpeechRepository,
) {
    suspend operator fun invoke(phrase: Phrase) {
        speechRepository.speak(phrase.spokenText, phrase.audioPath)
    }
}

class ObserveAllPhrasesUseCase @Inject constructor(
    private val phraseRepository: PhraseRepository,
) {
    operator fun invoke(): Flow<List<Phrase>> = phraseRepository.observeAllPhrases()
}

class SavePhraseUseCase @Inject constructor(
    private val phraseRepository: PhraseRepository,
) {
    suspend operator fun invoke(phrase: Phrase): Long = phraseRepository.savePhrase(phrase)
}

class DeletePhraseUseCase @Inject constructor(
    private val phraseRepository: PhraseRepository,
) {
    suspend operator fun invoke(id: Long) = phraseRepository.deletePhrase(id)
}
