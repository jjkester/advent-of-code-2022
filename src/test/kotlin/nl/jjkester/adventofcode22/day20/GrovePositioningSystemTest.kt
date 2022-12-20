package nl.jjkester.adventofcode22.day20

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import kotlinx.coroutines.test.runTest
import nl.jjkester.adventofcode22.StringInput
import nl.jjkester.adventofcode22.day20.GrovePositioningSystem.mix
import nl.jjkester.adventofcode22.day20.GrovePositioningSystem.sumOfCoordinates
import org.junit.jupiter.api.Test

class GrovePositioningSystemTest {

    @Test
    fun testMix() {
        val initial = listOf(1L, 2L, -3L, 3L, -2L, 0L, 4L)

        assertThat { initial.mix(1) }
            .isSuccess()
            .containsExactly(1L, 2L, -3L, 4L, 0L, 3L, -2L)
    }

    @Test
    fun testSumOfCoordinatesOne() = runTest {
        val input = StringInput(example)
        val preparedInput = GrovePositioningSystem.prepareInput(input)

        assertThat { preparedInput.sumOfCoordinates() }
            .isSuccess()
            .isEqualTo(3)
    }

    @Test
    fun testSumOfCoordinatesTenDecryptionKey() = runTest {
        val input = StringInput(example)
        val preparedInput = GrovePositioningSystem.prepareInput(input)

        assertThat { preparedInput.sumOfCoordinates(10, 811589153) }
            .isSuccess()
            .isEqualTo(1623178306)
    }

    companion object {
        private val example = """
            1
            2
            -3
            3
            -2
            0
            4
        """.trimIndent()
    }
}
