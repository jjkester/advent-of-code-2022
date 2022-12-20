package nl.jjkester.adventofcode22.day19

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import nl.jjkester.adventofcode22.StringInput
import nl.jjkester.adventofcode22.day19.NotEnoughMinerals.productOfMaximumNumberOfGeodes
import nl.jjkester.adventofcode22.day19.NotEnoughMinerals.sumOfQualityLevels
import org.junit.jupiter.api.Test

class NotEnoughMineralsTest {

    @Test
    fun testPrepareInput() = runTest {
        val input = StringInput(example)

        assertThat { NotEnoughMinerals.prepareInput(input).toList() }
            .isSuccess()
            .containsExactly(
                Blueprint(
                    multiplier = 1,
                    oreRobotCost = Resources(4, 0, 0, 0),
                    clayRobotCost = Resources(2, 0, 0, 0),
                    obsidianRobotCost = Resources(3, 14, 0, 0),
                    geodeRobotCost = Resources(2, 0, 7, 0)
                ),
                Blueprint(
                    multiplier = 2,
                    oreRobotCost = Resources(2, 0, 0, 0),
                    clayRobotCost = Resources(3, 0, 0, 0),
                    obsidianRobotCost = Resources(3, 8, 0, 0),
                    geodeRobotCost = Resources(3, 0, 12, 0)
                )
            )
    }

    @Test
    fun testSumOfQualityLevels() = runTest {
        val input = StringInput(example)
        val preparedInput = NotEnoughMinerals.prepareInput(input)

        assertThat { preparedInput.sumOfQualityLevels() }
            .isSuccess()
            .isEqualTo(33)
    }

    @Test
    fun testProductOfMaximumNumberOfGeodes() = runTest {
        val input = StringInput(example)
        val preparedInput = NotEnoughMinerals.prepareInput(input)

        assertThat { preparedInput.productOfMaximumNumberOfGeodes() }
            .isSuccess()
            .isEqualTo(3472)
    }

    companion object {
        private val example = """
            Blueprint 1: Each ore robot costs 4 ore. Each clay robot costs 2 ore. Each obsidian robot costs 3 ore and 14 clay. Each geode robot costs 2 ore and 7 obsidian.
            Blueprint 2: Each ore robot costs 2 ore. Each clay robot costs 3 ore. Each obsidian robot costs 3 ore and 8 clay. Each geode robot costs 3 ore and 12 obsidian.
        """.trimIndent()
    }
}
