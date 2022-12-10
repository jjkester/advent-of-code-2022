package nl.jjkester.adventofcode22.day10

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import assertk.assertions.startsWith
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import nl.jjkester.adventofcode22.StringInput
import nl.jjkester.adventofcode22.day10.CathodeRayTube.renderScreen
import nl.jjkester.adventofcode22.day10.CathodeRayTube.runProgram
import nl.jjkester.adventofcode22.day10.CathodeRayTube.sumOfSignalStrengths
import org.junit.jupiter.api.Test

class CathodeRayTubeTest {

    @Test
    fun testRunProgram() = runTest {
        val input = StringInput(example)
        val preparedInput = CathodeRayTube.prepareInput(input)

        assertThat { preparedInput.runProgram().toList() }
            .isSuccess()
            .startsWith(
                Register.uninitialized,
                Register(1),
                Register(1),
                Register(16)
            )
    }

    @Test
    fun testSumOfSignalStrengths() = runTest {
        val input = StringInput(example)
        val preparedInput = CathodeRayTube.prepareInput(input)

        assertThat { preparedInput.sumOfSignalStrengths() }
            .isSuccess()
            .isEqualTo(13140)
    }

    @Test
    fun testRenderScreen() = runTest {
        val input = StringInput(example)
        val preparedInput = CathodeRayTube.prepareInput(input)

        assertThat { preparedInput.renderScreen().also { println(it) } }
            .isSuccess()
            .isEqualTo("""
                ##..##..##..##..##..##..##..##..##..##..
                ###...###...###...###...###...###...###.
                ####....####....####....####....####....
                #####.....#####.....#####.....#####.....
                ######......######......######......####
                #######.......#######.......#######.....
            """.trimIndent())
    }

    companion object {
        private val example = """
            addx 15
            addx -11
            addx 6
            addx -3
            addx 5
            addx -1
            addx -8
            addx 13
            addx 4
            noop
            addx -1
            addx 5
            addx -1
            addx 5
            addx -1
            addx 5
            addx -1
            addx 5
            addx -1
            addx -35
            addx 1
            addx 24
            addx -19
            addx 1
            addx 16
            addx -11
            noop
            noop
            addx 21
            addx -15
            noop
            noop
            addx -3
            addx 9
            addx 1
            addx -3
            addx 8
            addx 1
            addx 5
            noop
            noop
            noop
            noop
            noop
            addx -36
            noop
            addx 1
            addx 7
            noop
            noop
            noop
            addx 2
            addx 6
            noop
            noop
            noop
            noop
            noop
            addx 1
            noop
            noop
            addx 7
            addx 1
            noop
            addx -13
            addx 13
            addx 7
            noop
            addx 1
            addx -33
            noop
            noop
            noop
            addx 2
            noop
            noop
            noop
            addx 8
            noop
            addx -1
            addx 2
            addx 1
            noop
            addx 17
            addx -9
            addx 1
            addx 1
            addx -3
            addx 11
            noop
            noop
            addx 1
            noop
            addx 1
            noop
            noop
            addx -13
            addx -19
            addx 1
            addx 3
            addx 26
            addx -30
            addx 12
            addx -1
            addx 3
            addx 1
            noop
            noop
            noop
            addx -9
            addx 18
            addx 1
            addx 2
            noop
            noop
            addx 9
            noop
            noop
            noop
            addx -1
            addx 2
            addx -37
            addx 1
            addx 3
            noop
            addx 15
            addx -21
            addx 22
            addx -6
            addx 1
            noop
            addx 2
            addx 1
            noop
            addx -10
            noop
            noop
            addx 20
            addx 1
            addx 2
            addx 2
            addx -6
            addx -11
            noop
            noop
            noop
        """.trimIndent()
    }
}
