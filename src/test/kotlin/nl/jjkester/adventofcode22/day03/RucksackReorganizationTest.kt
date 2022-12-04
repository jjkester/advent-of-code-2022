package nl.jjkester.adventofcode22.day03

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import kotlinx.coroutines.test.runTest
import nl.jjkester.adventofcode22.StringInput
import org.junit.jupiter.api.Test

class RucksackReorganizationTest {

    @Test
    fun testSumOfTypesInEachCompartment() = runTest {
        val input = StringInput(example)
        val preparedInput = RucksackReorganization.prepareInput(input)

        assertThat { RucksackReorganization.sumOfTypesInEachCompartment(preparedInput) }
            .isSuccess()
            .isEqualTo(157)
    }

    @Test
    fun testSumOfBadges() = runTest {
        val input = StringInput(example)
        val preparedInput = RucksackReorganization.prepareInput(input)

        assertThat { RucksackReorganization.sumOfBadges(preparedInput) }
            .isSuccess()
            .isEqualTo(70)
    }

    companion object {
        private val example = """
            vJrwpWtwJgWrhcsFMMfFFhFp
            jqHRNqRjqzjGDLGLrsFMfFZSrLrFZsSL
            PmmdzqPrVvPwwTWBwg
            wMqvLMZHhHMvwLHjbvcjnnSBnvTQFn
            ttgJtRGJQctTZtZT
            CrZsJsPPZsGzwwsLwLmpwMDw
        """.trimIndent()
    }
}
