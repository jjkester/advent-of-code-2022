package nl.jjkester.adventofcode22.day18

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import kotlinx.coroutines.test.runTest
import nl.jjkester.adventofcode22.StringInput
import nl.jjkester.adventofcode22.day18.BoilingBoulders.totalOutsideSurfaceArea
import org.junit.jupiter.api.Test

class BoilingBouldersTest {

    @Test
    fun testTotalSurfaceAreaIncludingPockets() = runTest {
        val input = StringInput(example)
        val preparedInput = BoilingBoulders.prepareInput(input)

        assertThat { preparedInput.totalOutsideSurfaceArea(false) }
            .isSuccess()
            .isEqualTo(64)
    }

    @Test
    fun testTotalSurfaceAreaExcludingPockets() = runTest {
        val input = StringInput(example)
        val preparedInput = BoilingBoulders.prepareInput(input)

        assertThat { preparedInput.totalOutsideSurfaceArea(true) }
            .isSuccess()
            .isEqualTo(58)
    }

    companion object {
        private val example = """
            2,2,2
            1,2,2
            3,2,2
            2,1,2
            2,3,2
            2,2,1
            2,2,3
            2,2,4
            2,2,6
            1,2,5
            3,2,5
            2,1,5
            2,3,5
        """.trimIndent()
    }
}
