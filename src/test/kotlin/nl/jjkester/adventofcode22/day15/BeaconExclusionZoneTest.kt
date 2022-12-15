package nl.jjkester.adventofcode22.day15

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import kotlinx.coroutines.test.runTest
import nl.jjkester.adventofcode22.StringInput
import nl.jjkester.adventofcode22.day15.BeaconExclusionZone.distressSignalTuningFrequency
import nl.jjkester.adventofcode22.day15.BeaconExclusionZone.positionsInRowWhereBeaconCannotBePresent
import org.junit.jupiter.api.Test

class BeaconExclusionZoneTest {

    @Test
    fun testPositionsInRowWhereBeaconCannotBePresent() = runTest {
        val input = StringInput(example)
        val preparedInput = BeaconExclusionZone.prepareInput(input)

        assertThat { preparedInput.positionsInRowWhereBeaconCannotBePresent(10) }
            .isSuccess()
            .isEqualTo(26)
    }

    @Test
    fun testDistressSignalTuningFrequency() = runTest {
        val input = StringInput(example)
        val preparedInput = BeaconExclusionZone.prepareInput(input)

        assertThat { preparedInput.distressSignalTuningFrequency(0..20) }
            .isSuccess()
            .isEqualTo(56000011)
    }

    companion object {
        private val example = """
            Sensor at x=2, y=18: closest beacon is at x=-2, y=15
            Sensor at x=9, y=16: closest beacon is at x=10, y=16
            Sensor at x=13, y=2: closest beacon is at x=15, y=3
            Sensor at x=12, y=14: closest beacon is at x=10, y=16
            Sensor at x=10, y=20: closest beacon is at x=10, y=16
            Sensor at x=14, y=17: closest beacon is at x=10, y=16
            Sensor at x=8, y=7: closest beacon is at x=2, y=10
            Sensor at x=2, y=0: closest beacon is at x=2, y=10
            Sensor at x=0, y=11: closest beacon is at x=2, y=10
            Sensor at x=20, y=14: closest beacon is at x=25, y=17
            Sensor at x=17, y=20: closest beacon is at x=21, y=22
            Sensor at x=16, y=7: closest beacon is at x=15, y=3
            Sensor at x=14, y=3: closest beacon is at x=15, y=3
            Sensor at x=20, y=1: closest beacon is at x=15, y=3
        """.trimIndent()
    }
}
