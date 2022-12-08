package nl.jjkester.adventofcode22.day08

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import kotlinx.coroutines.test.runTest
import nl.jjkester.adventofcode22.StringInput
import org.junit.jupiter.api.Test

class TreetopTreeHouseTest {

    @Test
    fun testNumberOfVisibleTrees() = runTest {
        val input = StringInput(example)
        val preparedInput = TreetopTreeHouse.prepareInput(input)

        assertThat { TreetopTreeHouse.numberOfVisibleTrees(preparedInput) }
            .isSuccess()
            .isEqualTo(21)
    }

    @Test
    fun highestScenicScore() = runTest {
        val input = StringInput(example)
        val preparedInput = TreetopTreeHouse.prepareInput(input)

        assertThat { TreetopTreeHouse.highestScenicScore(preparedInput) }
            .isSuccess()
            .isEqualTo(8)
    }

    companion object {
        private val example = """
            30373
            25512
            65332
            33549
            35390
        """.trimIndent()
    }
}
