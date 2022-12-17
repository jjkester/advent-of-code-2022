package nl.jjkester.adventofcode22.day16

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import kotlinx.coroutines.test.runTest
import nl.jjkester.adventofcode22.StringInput
import nl.jjkester.adventofcode22.day16.ProboscideaVolcanium.maxPressureReleaseBeforeEruption
import org.junit.jupiter.api.Test

class ProboscideaVolcaniumTest {

    @Test
    fun testMaxPressureReleaseBeforeEruptionWithoutElephant() = runTest {
        val input = StringInput(example)
        val preparedInput = ProboscideaVolcanium.prepareInput(input)

        assertThat { preparedInput.maxPressureReleaseBeforeEruption(false) }
            .isSuccess()
            .isEqualTo(1651)
    }

    @Test
    fun testMaxPressureReleaseBeforeEruptionWithElephant() = runTest {
        val input = StringInput(example)
        val preparedInput = ProboscideaVolcanium.prepareInput(input)

        assertThat { preparedInput.maxPressureReleaseBeforeEruption(true) }
            .isSuccess()
            .isEqualTo(1707)
    }

    companion object {
        private val example = """
            Valve AA has flow rate=0; tunnels lead to valves DD, II, BB
            Valve BB has flow rate=13; tunnels lead to valves CC, AA
            Valve CC has flow rate=2; tunnels lead to valves DD, BB
            Valve DD has flow rate=20; tunnels lead to valves CC, AA, EE
            Valve EE has flow rate=3; tunnels lead to valves FF, DD
            Valve FF has flow rate=0; tunnels lead to valves EE, GG
            Valve GG has flow rate=0; tunnels lead to valves FF, HH
            Valve HH has flow rate=22; tunnel leads to valve GG
            Valve II has flow rate=0; tunnels lead to valves AA, JJ
            Valve JJ has flow rate=21; tunnel leads to valve II
        """.trimIndent()
    }
}
