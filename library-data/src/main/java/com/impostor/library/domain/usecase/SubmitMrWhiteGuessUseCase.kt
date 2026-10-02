package com.impostor.library.domain.usecase

import com.impostor.library.data.repository.GameRepository
import java.text.Normalizer
import java.util.Locale

/** Exact comparison after case, whitespace and Unicode normalization; accents remain meaningful. */
object GuessWordRules {
    fun matches(guess: String, secret: String): Boolean = normalize(guess) == normalize(secret)
    private fun normalize(value: String) = Normalizer.normalize(value.trim(), Normalizer.Form.NFC)
        .replace(Regex("\\s+"), " ").lowercase(Locale.ROOT)
}

class SubmitMrWhiteGuessUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(roundId: Int, playerId: Int, text: String) =
        repository.submitMrWhiteGuess(roundId, playerId, text)
}
