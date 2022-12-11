package nl.jjkester.adventofcode22.day11

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import nl.jjkester.adventofcode22.Implementation
import nl.jjkester.adventofcode22.Input
import nl.jjkester.adventofcode22.chunkedBy

/**
 * https://adventofcode.com/2022/day/11
 */
object MonkeyInTheMiddle : Implementation<Flow<MonkeyNote>> {

    /**
     * Input modeled as notes about monkeys and their specific behavior.
     */
    override suspend fun prepareInput(input: Input): Flow<MonkeyNote> = input.readLines()
        .chunkedBy { it.isEmpty() }
        .map { MonkeyNote.parse(it.joinToString(System.lineSeparator())) }

    /**
     * Calculates the level of monkey business after a number of [rounds]. When [reduceWorryLevel] is true, worry is
     * reduced after a monkey inspected an item.
     */
    suspend fun Flow<MonkeyNote>.levelOfMonkeyBusiness(rounds: Int, reduceWorryLevel: Boolean): Long =
        MonkeyTracker(toList(), 3.takeIf { reduceWorryLevel })
            .apply { repeat(rounds) { round() } }
            .inspectedItemCounts
            .values
            .sortedDescending()
            .take(2)
            .reduce(Long::times)

    private class MonkeyTracker(monkeys: Collection<MonkeyNote>, private val relief: Int?) {

        private val monkeys: Map<Monkey, TrackedMonkey> = monkeys
            .associate { it.monkey to TrackedMonkey(it) }
            .toSortedMap()

        private val worryReductionFactor: Int = monkeys
            .map { it.behavior.test.divisor }
            .reduce(Int::times)

        var completedRounds: Int = 0
            private set

        val inspectedItemCounts: Map<Monkey, Long>
            get() = monkeys.mapValues { (_, it) -> it.itemsInspected }

        fun round() {
            monkeys.values.forEach { it.turn() }
            completedRounds += 1
        }

        private fun TrackedMonkey.turn() {
            while (items.isNotEmpty()) {
                items.removeFirst()
                    .inspect()
                    .reduceWorry()
                    .apply { throwTo(chooseMonkey().tracked()) }
            }
        }

        private fun Monkey.tracked(): TrackedMonkey = checkNotNull(monkeys[this]) { "Monkey $value is not tracked" }

        private fun Item.reduceWorry(): Item = Item(
            worryLevel
                .let { if (relief != null) it / relief else it }
                .rem(worryReductionFactor)
        )

        private fun Item.throwTo(monkey: TrackedMonkey) {
            monkey.items.add(this)
        }

        private class TrackedMonkey(monkeyNote: MonkeyNote) {
            private val behavior = monkeyNote.behavior
            val items: MutableList<Item> = monkeyNote.startingItems.toMutableList()
            var itemsInspected: Long = 0L

            fun Item.inspect(): Item = Item(increaseWorry(worryLevel))
                .also { itemsInspected += 1 }

            fun Item.chooseMonkey(): Monkey = with(behavior.test) {
                if (worryLevel % divisor == 0L) {
                    positiveTarget
                } else {
                    negativeTarget
                }
            }

            private fun increaseWorry(value: Long): Long = when (val carelessness = behavior.carelessness) {
                is Carelessness.Additive -> value + carelessness.value
                is Carelessness.Linear -> value * carelessness.value
                Carelessness.Exponential -> value * value
            }
        }
    }
}

/**
 * Reference to a specific monkey.
 */
@JvmInline
value class Monkey(val value: Int) : Comparable<Monkey> {
    override fun compareTo(other: Monkey): Int = value.compareTo(other.value)
}

/**
 * Describes the behavior of a monkey.
 */
data class MonkeyBehavior(val carelessness: Carelessness, val test: Test) {

    /**
     * Test used by a monkey to determine where to throw an item to.
     */
    data class Test(val divisor: Int, val positiveTarget: Monkey, val negativeTarget: Monkey)
}

/**
 * Observations of a monkey, including its behavior.
 */
data class MonkeyNote(
    val monkey: Monkey,
    val startingItems: List<Item>,
    val behavior: MonkeyBehavior
) {
    companion object {
        @Suppress("RegExpRepeatedSpace")
        private val template = """
            Monkey (?<id>\d):
              Starting items: (?<startingItems>(\d+, )*)(?<startingItem>\d+)
              Operation: new = old (?<operation>[+*]) (?<operationValue>\d+|old)
              Test: divisible by (?<divisor>\d+)
                If true: throw to monkey (?<positiveId>\d)
                If false: throw to monkey (?<negativeId>\d)
        """.trimIndent().toRegex()

        /**
         * Parses the notes on a monkey.
         */
        fun parse(lines: String): MonkeyNote {
            val data = checkNotNull(template.matchEntire(lines)) { "Input does not match template" }.groups

            return MonkeyNote(
                monkey = Monkey(data["id"]!!.value.toInt()),
                startingItems = (data["startingItems"]!!.value.split(", ") + data["startingItem"]!!.value)
                    .filter { it.isNotBlank() }
                    .map { Item(it.toLong()) },
                behavior = MonkeyBehavior(
                    carelessness = Carelessness.parse(data["operation"]!!.value, data["operationValue"]!!.value),
                    test = MonkeyBehavior.Test(
                        divisor = data["divisor"]!!.value.toInt(),
                        positiveTarget = Monkey(data["positiveId"]!!.value.toInt()),
                        negativeTarget = Monkey(data["negativeId"]!!.value.toInt())
                    )
                )
            )
        }
    }

}

/**
 * The carelessness of a monkey.
 */
sealed class Carelessness {
    /**
     * Additive carelessness, where item inspection by a monkey will increase the level of worry with the [value]
     */
    data class Additive(val value: Int) : Carelessness()

    /**
     * Linear carelessness, where item inspection by a monkey will multiply the level of worry by the [value].
     */
    data class Linear(val value: Int) : Carelessness()

    /**
     * Exponential carelessness, where item inspection by a monkey will square the level of worry.
     */
    object Exponential : Carelessness() {
        override fun toString(): String = this::class.simpleName!!
    }

    companion object {
        /**
         * Parses carelessness from the provided [operation] and [value].
         */
        fun parse(operation: String, value: String): Carelessness = when {
            operation == "*" && value == "old" -> Exponential
            operation == "+" -> Additive(value.toInt())
            operation == "*" -> Linear(value.toInt())
            else -> error("Unsupported operation '$operation'")
        }
    }
}

/**
 * Item with a [worryLevel] that is indicative of the preciousness of the item.
 */
@JvmInline
value class Item(val worryLevel: Long)
