package nl.jjkester.adventofcode22.day21

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import kotlinx.coroutines.test.runTest
import nl.jjkester.adventofcode22.StringInput
import nl.jjkester.adventofcode22.day21.MonkeyMatch.humanValue
import nl.jjkester.adventofcode22.day21.MonkeyMatch.rootValue
import org.junit.jupiter.api.Test

class MonkeyMatchTest {

    @Test
    fun testRootValue() = runTest {
        val input = StringInput(example)
        val preparedInput = MonkeyMatch.prepareInput(input)

        assertThat { preparedInput.rootValue() }
            .isSuccess()
            .isEqualTo(152)
    }

    @Test
    fun testHumanValue() = runTest {
        val input = StringInput(example)
        val preparedInput = MonkeyMatch.prepareInput(input)

        assertThat { preparedInput.humanValue() }
            .isSuccess()
            .isEqualTo(301)
    }

    companion object {
        private val example = """
            root: pppw + sjmn
            dbpl: 5
            cczh: sllz + lgvd
            zczc: 2
            ptdq: humn - dvpt
            dvpt: 3
            lfqf: 4
            humn: 5
            ljgn: 2
            sjmn: drzm * dbpl
            sllz: 4
            pppw: cczh / lfqf
            lgvd: ljgn * ptdq
            drzm: hmdt - zczc
            hmdt: 32
        """.trimIndent()
    }
}
