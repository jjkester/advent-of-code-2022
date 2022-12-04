package nl.jjkester.adventofcode22.day02

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import nl.jjkester.adventofcode22.StringInput
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RockPaperScissorsTest {

    @Test
    fun testTotalScoreWithHandsStrategy() = runTest {
        val input = StringInput(example)
        val preparedInput = RockPaperScissors.prepareInput(input)

        assertThat { RockPaperScissors.totalScoreWithHandStrategy(preparedInput) }
            .isSuccess()
            .isEqualTo(15)
    }

    @Test
    fun testTotalScoreWithOutcomeStrategy() = runTest {
        val input = StringInput(example)
        val preparedInput = RockPaperScissors.prepareInput(input)

        assertThat { RockPaperScissors.totalScoreWithOutcomeStrategy(preparedInput) }
            .isSuccess()
            .isEqualTo(12)
    }

    companion object {
        private val example = """
            A Y
            B X
            C Z
        """.trimIndent()
    }
}
