package nl.jjkester.adventofcode22.day14

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import kotlinx.coroutines.test.runTest
import nl.jjkester.adventofcode22.StringInput
import nl.jjkester.adventofcode22.day14.RegolithReservoir.unitsOfSandInCave
import nl.jjkester.adventofcode22.day14.RegolithReservoir.unitsOfSandUntilRest
import org.junit.jupiter.api.Test

class RegolithReservoirTest {

    @Test
    fun testUnitsOfSandInCave() = runTest {
        val input = StringInput(example)
        val preparedInput = RegolithReservoir.prepareInput(input)

        assertThat { preparedInput.unitsOfSandInCave() }
            .isSuccess()
            .isEqualTo(24)
    }

    @Test
    fun testUnitsOfSandUntilRest() = runTest {
        val input = StringInput(example)
        val preparedInput = RegolithReservoir.prepareInput(input)

        assertThat { preparedInput.unitsOfSandUntilRest() }
            .isSuccess()
            .isEqualTo(93)
    }

    companion object {
        private val example = """
            498,4 -> 498,6 -> 496,6
            503,4 -> 502,4 -> 502,9 -> 494,9
        """.trimIndent()
    }
}
