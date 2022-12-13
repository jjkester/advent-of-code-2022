package nl.jjkester.adventofcode22.day13

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import assertk.assertions.startsWith
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import nl.jjkester.adventofcode22.StringInput
import nl.jjkester.adventofcode22.day13.DistressSignal.decoderKey
import nl.jjkester.adventofcode22.day13.DistressSignal.sumOfIndicesInRightOrder
import org.junit.jupiter.api.Test

class DistressSignalTest {

    @Test
    fun testPrepareInput() = runTest {
        val input = StringInput(example)

        assertThat { DistressSignal.prepareInput(input).toList() }
            .isSuccess()
            .startsWith(
                L(V(1), V(1), V(3), V(1), V(1)) to L(V(1), V(1), V(5), V(1), V(1)),
                L(L(V(1)), L(V(2), V(3), V(4))) to L(L(V(1)), V(4))
            )
    }

    @Test
    fun testSumOfIndicesInRightOrder() = runTest {
        val input = StringInput(example)
        val preparedInput = DistressSignal.prepareInput(input)

        assertThat { preparedInput.sumOfIndicesInRightOrder() }
            .isSuccess()
            .isEqualTo(13)
    }

    @Test
    fun testDecoderKey() = runTest {
        val input = StringInput(example)
        val preparedInput = DistressSignal.prepareInput(input)

        assertThat { preparedInput.decoderKey() }
            .isSuccess()
            .isEqualTo(140)
    }

    companion object {
        private val example = """
            [1,1,3,1,1]
            [1,1,5,1,1]

            [[1],[2,3,4]]
            [[1],4]

            [9]
            [[8,7,6]]

            [[4,4],4,4]
            [[4,4],4,4,4]

            [7,7,7,7]
            [7,7,7]

            []
            [3]

            [[[]]]
            [[]]

            [1,[2,[3,[4,[5,6,7]]]],8,9]
            [1,[2,[3,[4,[5,6,0]]]],8,9]
        """.trimIndent()

        private fun V(i: Int) = Packet.Single(i)
        private fun L(vararg i: Packet) = Packet.Many(i.toList())
    }
}
