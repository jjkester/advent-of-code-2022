package nl.jjkester.adventofcode22.day12

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import kotlinx.coroutines.test.runTest
import nl.jjkester.adventofcode22.StringInput
import nl.jjkester.adventofcode22.day12.HillClimbingAlgorithm.shortestSteps
import nl.jjkester.adventofcode22.day12.HillClimbingAlgorithm.shortestTrail
import org.junit.jupiter.api.Test

class HillClimbingAlgorithmTest {

    @Test
    fun testShortestSteps() = runTest {
        val input = StringInput(example)
        val preparedInput = HillClimbingAlgorithm.prepareInput(input)

        assertThat { preparedInput.shortestSteps() }
            .isSuccess()
            .isEqualTo(31)
    }

    @Test
    fun shortestTrail() = runTest {
        val input = StringInput(example)
        val preparedInput = HillClimbingAlgorithm.prepareInput(input)

        assertThat { preparedInput.shortestTrail() }
            .isSuccess()
            .isEqualTo(29)
    }

    companion object {
        private val example = """
            Sabqponm
            abcryxxl
            accszExk
            acctuvwj
            abdefghi
        """.trimIndent()
    }
}
