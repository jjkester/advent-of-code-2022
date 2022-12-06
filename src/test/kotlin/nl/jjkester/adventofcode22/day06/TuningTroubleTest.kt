package nl.jjkester.adventofcode22.day06

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import kotlinx.coroutines.test.runTest
import nl.jjkester.adventofcode22.StringInput
import org.junit.jupiter.api.Test

class TuningTroubleTest {

    @Test
    fun testOffsetToStartPacketMarker() = runTest {
        val input = StringInput(example)
        val preparedInput = TuningTrouble.prepareInput(input)

        assertThat { TuningTrouble.offsetToFirstDistinctGroup(preparedInput, 4) }
            .isSuccess()
            .isEqualTo(7)
    }

    @Test
    fun testOffsetToStartMessageMarker() = runTest {
        val input = StringInput(example)
        val preparedInput = TuningTrouble.prepareInput(input)

        assertThat { TuningTrouble.offsetToFirstDistinctGroup(preparedInput, 14) }
            .isSuccess()
            .isEqualTo(19)
    }

    companion object {
        private val example = "mjqjpqmgbljsphdztnvjfqwrcgsmlb"
    }
}
