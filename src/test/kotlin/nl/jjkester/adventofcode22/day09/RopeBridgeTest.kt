package nl.jjkester.adventofcode22.day09

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import nl.jjkester.adventofcode22.StringInput
import org.junit.jupiter.api.Test

class RopeBridgeTest {

    @Test
    fun testPrepareInput() = runTest {
        val input = StringInput(example)

        assertThat { RopeBridge.prepareInput(input).toList() }
            .isSuccess()
            .containsExactly(
                Movement.Right,
                Movement.Right,
                Movement.Right,
                Movement.Right,
                Movement.Up,
                Movement.Up,
                Movement.Up,
                Movement.Up,
                Movement.Left,
                Movement.Left,
                Movement.Left,
                Movement.Down,
                Movement.Right,
                Movement.Right,
                Movement.Right,
                Movement.Right,
                Movement.Down,
                Movement.Left,
                Movement.Left,
                Movement.Left,
                Movement.Left,
                Movement.Left,
                Movement.Right,
                Movement.Right
            )
    }

    @Test
    fun testTailRopePositions() = runTest {
        val input = StringInput(example)
        val preparedInput = RopeBridge.prepareInput(input)

        assertThat { RopeBridge.tailRopePositions(Rope.start, preparedInput).toList() }
            .isSuccess()
            .containsExactly(
                0 to 0,
                0 to 0,
                1 to 0,
                2 to 0,
                3 to 0,
                3 to 0,
                4 to 1,
                4 to 2,
                4 to 3,
                4 to 3,
                3 to 4,
                2 to 4,
                2 to 4,
                2 to 4,
                2 to 4,
                3 to 3,
                4 to 3,
                4 to 3,
                4 to 3,
                4 to 3,
                3 to 2,
                2 to 2,
                1 to 2,
                1 to 2,
                1 to 2,
            )
    }

    @Test
    fun testNumberOfTailRopePositions() = runTest {
        val input = StringInput(example)
        val preparedInput = RopeBridge.prepareInput(input)

        assertThat { RopeBridge.numberOfTailRopePositions(preparedInput) }
            .isSuccess()
            .isEqualTo(13)
    }

    companion object {
        private val example = """
            R 4
            U 4
            L 3
            D 1
            R 4
            D 1
            L 5
            R 2
        """.trimIndent()
    }
}
