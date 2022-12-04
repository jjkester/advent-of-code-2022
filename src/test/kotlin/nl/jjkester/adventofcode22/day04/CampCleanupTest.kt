package nl.jjkester.adventofcode22.day04

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import kotlinx.coroutines.test.runTest
import nl.jjkester.adventofcode22.StringInput
import org.junit.jupiter.api.Test


class CampCleanupTest {

    @Test
    fun testNumberOfFullyContainedRanges() = runTest {
        val input = StringInput(example)
        val preparedInput = CampCleanup.prepareInput(input)

        assertThat { CampCleanup.numberOfFullyContainedRanges(preparedInput) }
            .isSuccess()
            .isEqualTo(2)
    }

    @Test
    fun testNumberOfOverlappingRanges() = runTest {
        val input = StringInput(example)
        val preparedInput = CampCleanup.prepareInput(input)

        assertThat { CampCleanup.numberOfOverlappingRanges(preparedInput) }
            .isSuccess()
            .isEqualTo(4)
    }

    companion object {
        private val example = """
            2-4,6-8
            2-3,4-5
            5-7,7-9
            2-8,3-7
            6-6,4-6
            2-6,4-8
        """.trimIndent()
    }
}
