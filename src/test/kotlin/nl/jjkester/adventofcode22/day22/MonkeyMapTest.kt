package nl.jjkester.adventofcode22.day22

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import kotlinx.coroutines.test.runTest
import nl.jjkester.adventofcode22.StringInput
import nl.jjkester.adventofcode22.day22.MonkeyMap.finalPasswordFlat
import org.junit.jupiter.api.Test

class MonkeyMapTest {

    @Test
    fun testPrepareInput() = runTest {
        val input = StringInput(example)

        val (map, instructions) = MonkeyMap.prepareInput(input)

        assertThat(map)
            .transform { it.toString() }
            .isEqualTo(example.split(System.lineSeparator()).takeWhile { it.isNotBlank() }.joinToString(System.lineSeparator()))

        assertThat(instructions)
            .containsExactly(
                Instruction.Move(10),
                Instruction.TurnRight,
                Instruction.Move(5),
                Instruction.TurnLeft,
                Instruction.Move(5),
                Instruction.TurnRight,
                Instruction.Move(10),
                Instruction.TurnLeft,
                Instruction.Move(4),
                Instruction.TurnRight,
                Instruction.Move(5),
                Instruction.TurnLeft,
                Instruction.Move(5),
            )
    }

    @Test
    fun testFinalPasswordFlat() = runTest {
        val input = StringInput(example)
        val preparedInput = MonkeyMap.prepareInput(input)

        assertThat { preparedInput.finalPasswordFlat() }
            .isSuccess()
            .isEqualTo(6032)
    }

    companion object {
        private val example = """
                    ...#
                    .#..
                    #...
                    ....
            ...#.......#
            ........#...
            ..#....#....
            ..........#.
                    ...#....
                    .....#..
                    .#......
                    ......#.
            
            10R5L5R10L4R5L5
        """.trimIndent()
    }
}
