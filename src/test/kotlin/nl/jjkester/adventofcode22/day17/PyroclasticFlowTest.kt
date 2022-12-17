package nl.jjkester.adventofcode22.day17

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import kotlinx.coroutines.test.runTest
import nl.jjkester.adventofcode22.StringInput
import nl.jjkester.adventofcode22.day17.PyroclasticFlow.heightAfterRocks
import org.junit.jupiter.api.Test

class PyroclasticFlowTest {

    @Test
    fun testHeightAfterRocksSmall() = runTest {
        val input = StringInput(example)
        val preparedInput = PyroclasticFlow.prepareInput(input)

        assertThat { preparedInput.heightAfterRocks(2022) }
            .isSuccess()
            .isEqualTo(3068)
    }

    @Test
    fun testHeightAfterRocksLarge() = runTest {
        val input = StringInput(example)
        val preparedInput = PyroclasticFlow.prepareInput(input)

        assertThat { preparedInput.heightAfterRocks(1_000_000_000_000) }
            .isSuccess()
            .isEqualTo(1514285714288)
    }

    companion object {
        private const val example = ">>><<><>><<<>><>>><<<>>><<<><<<>><>><<>>"
    }
}
