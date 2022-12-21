package nl.jjkester.adventofcode22.day21

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import nl.jjkester.adventofcode22.Implementation
import nl.jjkester.adventofcode22.Input

/**
 * https://adventofcode.com/2022/day/21
 */
object MonkeyMatch : Implementation<Flow<Monkey>> {

    /**
     * Input modeled as a flow of monkeys.
     */
    override suspend fun prepareInput(input: Input): Flow<Monkey> = input.readLines()
        .map { Monkey.parse(it) }

    /**
     * Calculates the value of the monkey with name "root".
     */
    suspend fun Flow<Monkey>.rootValue(): Long = toList()
        .associateBy(Monkey::name)
        .valueOf("root")

    /**
     * Calculates the value that the monkey with name "humn" should have, assuming that both monkeys observed by the
     * monkey with name "root" should have the same value.
     */
    suspend fun Flow<Monkey>.humanValue(): Long = toTree().run {
        pathToHuman()
            .toList()
            .fold(value) { result, node ->
                val leftValue = node.left.value
                val rightValue = node.right.value
                val singleValue = listOfNotNull(leftValue, rightValue).single()

                when (node.operation) {
                    MathOperation.Add -> result - singleValue
                    MathOperation.Subtract -> if (leftValue != null) {
                        leftValue - result
                    } else {
                        checkNotNull(rightValue)
                        rightValue + result
                    }
                    MathOperation.Multiply -> result / singleValue
                    MathOperation.Divide -> if (leftValue != null) {
                        leftValue / result
                    } else {
                        checkNotNull(rightValue)
                        rightValue * result
                    }
                }
            }
    }

    private fun Node.Root.pathToHuman(): Flow<Node.Operation> = flow {
        var node: Node = listOf(left, right).single { it.value == null }

        while (node is Node.Operation) {
            emit(node)
            node = listOf(node.left, node.right).single { it.value == null }
        }
    }

    private suspend fun Flow<Monkey>.toTree(): Node.Root = toList()
        .associateBy(Monkey::name)
        .run {
            checkNotNull(get("root") as? Monkey.Operation) { "Unknown or invalid root monkey" }
                .let { monkey ->
                    Node.Root(toSubtree(monkey.left), toSubtree(monkey.right))
                }
        }

    private fun Map<String, Monkey>.toSubtree(name: String): Node = checkNotNull(get(name)) { "Unknown monkey" }
        .let { monkey ->
            when (monkey) {
                is Monkey.Number -> when (name) {
                    "humn" -> Node.Human
                    else -> Node.Number(monkey.number)
                }

                is Monkey.Operation -> {
                    Node.Operation(toSubtree(monkey.left), toSubtree(monkey.right), monkey.operation)
                }
            }
        }

    private fun Map<String, Monkey>.valueOf(name: String): Long = checkNotNull(get(name)) { "Unknown monkey" }
        .let { monkey ->
            when (monkey) {
                is Monkey.Number -> monkey.number
                is Monkey.Operation -> monkey.operation(valueOf(monkey.left), valueOf(monkey.right))
            }
        }

    private fun MathOperation.safe(left: Long?, right: Long?): Long? = if (left != null && right != null) {
        this(left, right)
    } else {
        null
    }

    private sealed class Node {

        abstract val value: Long?

        class Root(
            val left: Node,
            val right: Node,
        ) : Node() {
            override val value: Long = listOfNotNull(left.value, right.value).single()
        }

        object Human : Node() {
            override val value = null
        }

        class Operation(
            val left: Node,
            val right: Node,
            val operation: MathOperation
        ) : Node() {
            override val value = operation.safe(left.value, right.value)
        }

        class Number(override val value: Long) : Node()
    }
}

/**
 * Behavior of a single monkey.
 */
sealed class Monkey {

    abstract val name: String

    /**
     * Monkey that has a fixed value.
     */
    data class Number(override val name: String, val number: Long) : Monkey()

    /**
     * Monkey that combines the values of two other monkeys.
     */
    data class Operation(
        override val name: String,
        val left: String,
        val right: String,
        val operation: MathOperation
    ) : Monkey()

    companion object {
        private val regex = """(?<name>\w+): ((?<left>\w+) (?<op>[+\-*/]) (?<right>\w+)|(?<num>-?\d+))""".toRegex()

        /**
         * Parses a monkey from string.
         */
        fun parse(line: String): Monkey {
            val matches = checkNotNull(regex.matchEntire(line)) { "Not a valid monkey" }

            val name = matches.groups["name"]!!.value
            val left = matches.groups["left"]?.value
            val op = matches.groups["op"]?.value?.let { MathOperation.parse(it.single()) }
            val right = matches.groups["right"]?.value
            val num = matches.groups["num"]?.value?.toLong()

            return if (left != null && op != null && right != null) {
                Operation(name, left, right, op)
            } else {
                Number(name, num!!)
            }
        }
    }
}

/**
 * Math operations that can be used by monkeys.
 */
enum class MathOperation(private val fn: (Long, Long) -> Long) {
    Add(Long::plus),
    Subtract(Long::minus),
    Multiply(Long::times),
    Divide(Long::div);

    operator fun invoke(left: Long, right: Long): Long = fn(left, right)

    companion object {
        fun parse(char: Char): MathOperation = when (char) {
            '+' -> Add
            '-' -> Subtract
            '*' -> Multiply
            '/' -> Divide
            else -> error("Char '$char' is not a supported math operation")
        }
    }
}
