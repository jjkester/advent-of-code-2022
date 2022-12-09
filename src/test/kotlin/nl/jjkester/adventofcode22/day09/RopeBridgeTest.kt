package nl.jjkester.adventofcode22.day09

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import nl.jjkester.adventofcode22.StringInput
import org.junit.jupiter.api.Test

class RopeBridgeTest {

    @Test
    fun testPrepareInput() = runTest {
        val input = StringInput(example1)

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
    fun testRopeKnotPositions2() = runTest {
        val input = StringInput(example1)
        val preparedInput = RopeBridge.prepareInput(input)

        assertThat {
            RopeBridge.ropeKnotPositions(Rope(2), preparedInput)
                .filter { it.index == 1 }
                .map { it.value.x to it.value.y }
                .toList()
        }
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
    fun testRopeKnotPositions10() = runTest {
        val input = StringInput(example2)
        val preparedInput = RopeBridge.prepareInput(input)

        assertThat {
            RopeBridge.ropeKnotPositions(Rope(10), preparedInput)
                .filter { it.index == 9 }
                .map { it.value.x to it.value.y }
                .toList()
                .filterIndexed { index, _ -> index in listOf(0, 5, 13, 21, 24, 41) }
        }
            .isSuccess()
            .containsExactly(
                0 to 0,
                0 to 0,
                0 to 0,
                1 to 3,
                1 to 3,
                5 to 5,
            )
    }

    @Test
    fun testNumberOfTailRopePositions2() = runTest {
        val input = StringInput(example1)
        val preparedInput = RopeBridge.prepareInput(input)

        assertThat { RopeBridge.numberOfTailRopePositions(preparedInput, 2) }
            .isSuccess()
            .isEqualTo(13)
    }

    @Test
    fun testNumberOfTailRopePositions10Small() = runTest {
        val input = StringInput(example1)
        val preparedInput = RopeBridge.prepareInput(input)

        assertThat { RopeBridge.numberOfTailRopePositions(preparedInput, 10) }
            .isSuccess()
            .isEqualTo(1)
    }

    @Test
    fun testNumberOfTailRopePositions10Large() = runTest {
        val input = StringInput(example2)
        val preparedInput = RopeBridge.prepareInput(input)

        assertThat { RopeBridge.numberOfTailRopePositions(preparedInput, 10) }
            .isSuccess()
            .isEqualTo(36)
    }

    companion object {
        private val example1 = """
            R 4
            U 4
            L 3
            D 1
            R 4
            D 1
            L 5
            R 2
        """.trimIndent()

        private val example2 = """
            R 5
            U 8
            L 8
            D 3
            R 17
            D 10
            L 25
            U 20
        """.trimIndent()
    }
}
