package nl.jjkester.adventofcode22.day11

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import nl.jjkester.adventofcode22.StringInput
import nl.jjkester.adventofcode22.day11.MonkeyInTheMiddle.levelOfMonkeyBusiness
import org.junit.jupiter.api.Test

class MonkeyInTheMiddleTest {

    @Test
    fun testPrepareInput() = runTest {
        val input = StringInput(example)

        assertThat { MonkeyInTheMiddle.prepareInput(input).toList() }
            .isSuccess()
            .containsExactly(
                MonkeyNote(
                    monkey = Monkey(0),
                    startingItems = listOf(Item(79), Item(98)),
                    behavior = MonkeyBehavior(
                        carelessness = Carelessness.Linear(19),
                        test = MonkeyBehavior.Test(
                            divisor = 23,
                            positiveTarget = Monkey(2),
                            negativeTarget = Monkey(3)
                        )
                    )
                ),
                MonkeyNote(
                    monkey = Monkey(1),
                    startingItems = listOf(Item(54), Item(65), Item(75), Item(74)),
                    behavior = MonkeyBehavior(
                        carelessness = Carelessness.Additive(6),
                        test = MonkeyBehavior.Test(
                            divisor = 19,
                            positiveTarget = Monkey(2),
                            negativeTarget = Monkey(0)
                        )
                    )
                ),
                MonkeyNote(
                    monkey = Monkey(2),
                    startingItems = listOf(Item(79), Item(60), Item(97)),
                    behavior = MonkeyBehavior(
                        carelessness = Carelessness.Exponential,
                        test = MonkeyBehavior.Test(
                            divisor = 13,
                            positiveTarget = Monkey(1),
                            negativeTarget = Monkey(3)
                        )
                    )
                ),
                MonkeyNote(
                    monkey = Monkey(3),
                    startingItems = listOf(Item(74)),
                    behavior = MonkeyBehavior(
                        carelessness = Carelessness.Additive(3),
                        test = MonkeyBehavior.Test(
                            divisor = 17,
                            positiveTarget = Monkey(0),
                            negativeTarget = Monkey(1)
                        )
                    )
                )
            )
    }

    @Test
    fun testLevelOfMonkeyBusiness20() = runTest {
        val input = StringInput(example)
        val preparedInput = MonkeyInTheMiddle.prepareInput(input)

        assertThat { preparedInput.levelOfMonkeyBusiness(20, true) }
            .isSuccess()
            .isEqualTo(10605)
    }

    @Test
    fun testLevelOfMonkeyBusiness10000() = runTest {
        val input = StringInput(example)
        val preparedInput = MonkeyInTheMiddle.prepareInput(input)

        assertThat { preparedInput.levelOfMonkeyBusiness(10_000, false) }
            .isSuccess()
            .isEqualTo(2713310158)
    }

    companion object {
        private val example = """
            Monkey 0:
              Starting items: 79, 98
              Operation: new = old * 19
              Test: divisible by 23
                If true: throw to monkey 2
                If false: throw to monkey 3

            Monkey 1:
              Starting items: 54, 65, 75, 74
              Operation: new = old + 6
              Test: divisible by 19
                If true: throw to monkey 2
                If false: throw to monkey 0

            Monkey 2:
              Starting items: 79, 60, 97
              Operation: new = old * old
              Test: divisible by 13
                If true: throw to monkey 1
                If false: throw to monkey 3

            Monkey 3:
              Starting items: 74
              Operation: new = old + 3
              Test: divisible by 17
                If true: throw to monkey 0
                If false: throw to monkey 1
        """.trimIndent()
    }
}
