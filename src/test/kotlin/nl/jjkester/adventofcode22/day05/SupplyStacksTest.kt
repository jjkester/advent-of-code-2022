package nl.jjkester.adventofcode22.day05

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import kotlinx.coroutines.test.runTest
import nl.jjkester.adventofcode22.StringInput
import nl.jjkester.adventofcode22.day05.SupplyStacks.topOfStacks
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SupplyStacksTest {

    @Test
    fun testParseShipState() {
        assertThat { ShipState.parse(shipState.split(System.lineSeparator())) }
            .isSuccess()
            .transform { it.toString() }
            .isEqualTo(shipState)
    }

    @Test
    fun testParseMoveOperations() {
        assertThat { moveOperations.split(System.lineSeparator()).map(MoveOperation.Companion::parse) }
            .isSuccess()
            .transform { it.joinToString(System.lineSeparator()) }
            .isEqualTo(moveOperations)
    }

    @Nested
    inner class TopOfStacks {

        @Test
        fun testOneByOne() = runTest {
            val input = StringInput(example)
            val preparedInput = SupplyStacks.prepareInput(input)

            assertThat { topOfStacks(preparedInput.first, preparedInput.second, false) }
                .isSuccess()
                .isEqualTo("CMZ")
        }

        @Test
        fun testStacked() = runTest {
            val input = StringInput(example)
            val preparedInput = SupplyStacks.prepareInput(input)

            assertThat { topOfStacks(preparedInput.first, preparedInput.second, true) }
                .isSuccess()
                .isEqualTo("MCD")
        }
    }

    companion object {
        val shipState = """
                [D]    
            [N] [C]    
            [Z] [M] [P]
             1   2   3 
        """.trimIndent()

        val moveOperations = """
            move 1 from 2 to 1
            move 3 from 1 to 3
            move 2 from 2 to 1
            move 1 from 1 to 2
        """.trimIndent()

        val example = """
                [D]    
            [N] [C]    
            [Z] [M] [P]
             1   2   3 
            
            move 1 from 2 to 1
            move 3 from 1 to 3
            move 2 from 2 to 1
            move 1 from 1 to 2
        """.trimIndent()
    }
}
