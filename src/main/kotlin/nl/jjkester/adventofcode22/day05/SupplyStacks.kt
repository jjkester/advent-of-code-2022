package nl.jjkester.adventofcode22.day05

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import nl.jjkester.adventofcode22.Implementation
import nl.jjkester.adventofcode22.Input
import nl.jjkester.adventofcode22.chunkedBy
import nl.jjkester.adventofcode22.toPair

/**
 * https://adventofcode.com/2022/day/5
 */
object SupplyStacks : Implementation<Pair<ShipState, Flow<MoveOperation>>> {

    /**
     * Input modeled as a pair of the initial state of the containers on the ship and a flow of the required movements.
     */
    override suspend fun prepareInput(input: Input): Pair<ShipState, Flow<MoveOperation>> {
        val (state, moves) = input.readLines()
            .chunkedBy { it.isEmpty() }
            .toList()
            .toPair()

        return ShipState.parse(state) to moves.map { MoveOperation.parse(it) }.asFlow()
    }

    /**
     * Returns the letters of the crates on top of the stacks after applying the movements.
     *
     * [moveStacked] toggles between moving a stack of crates one-by-one (`false`) and moving a stack as a whole
     * (`true`).
     */
    suspend fun topOfStacks(initialState: ShipState, movements: Flow<MoveOperation>, moveStacked: Boolean): String {
        val state = initialState.toMutableShipState()
        movements.collect { state.move(it, moveStacked) }
        return state.stacks.map { it.last() }.joinToString("")
    }
}

/**
 * Interface for non-mutable state of containers on a ship.
 */
interface ShipState {

    /**
     * Stacks of containers on the ship.
     */
    val stacks: List<List<Char>>

    companion object {
        /**
         * Parses a list of lines to a [ShipState].
         */
        fun parse(value: List<String>): ShipState {
            // Read bottom-up, skip index row
            val levels = value.asReversed().drop(1)
                .map { line -> line.chunked(4) { it.trim().removePrefix("[").removeSuffix("]") } }

            val state = ShipStateImpl(levels.maxOf { it.size })

            levels.forEach { item ->
                item.forEachIndexed { index, itemValue ->
                    if (itemValue.length == 1) {
                        state.stacks[index].add(itemValue.first())
                    } else {
                        require(itemValue.isEmpty())
                    }
                }
            }

            return state
        }
    }
}

/**
 * Interface for a mutable variant of [ShipState] that allows applying movements.
 */
interface MutableShipState : ShipState {

    fun move(operation: MoveOperation, moveStacked: Boolean)
}

/**
 * Implementation of the state of containers on a ship.
 */
class ShipStateImpl private constructor(override val stacks: MutableList<MutableList<Char>>) : MutableShipState {

    /**
     * Creates a new instance that holds the provided [size] number of stacks.
     */
    constructor(size: Int) : this(MutableList<MutableList<Char>>(size) { mutableListOf() })

    /**
     * Creates a new instance, copying the state from the [other] [ShipState].
     */
    constructor(other: ShipState) : this(other.stacks.map { it.toMutableList() }.toMutableList())

    override fun move(operation: MoveOperation, moveStacked: Boolean) {
        stacks[operation.to - 1].addAll(
            sequence {
                repeat(operation.count) {
                    yield(stacks[operation.from - 1].removeLast())
                }
            }.toList().run { if (moveStacked) asReversed() else this }
        )
    }

    override fun toString(): String {
        val height = stacks.maxOf(MutableList<Char>::size)

        return buildString {
            for (level in height downTo 1) {
                stacks.forEachIndexed { index, stack ->
                    append(stack.getOrNull(level - 1)?.let { "[$it]" } ?: "   ")
                    if (index < stacks.lastIndex) append(" ")
                }
                append(System.lineSeparator())
            }
            stacks.forEachIndexed { index, _ ->
                append(" ${index + 1} ")
                if (index < stacks.lastIndex) append(" ")
            }
        }
    }
}

/**
 * Returns a mutable copy of this [ShipState].
 */
fun ShipState.toMutableShipState(): MutableShipState = ShipStateImpl(this)

/**
 * Movement operation of a single or stack of containers.
 */
data class MoveOperation(val count: Int, val from: Int, val to: Int) {

    init {
        require(count > 0) { "Number of containers must be greater than zero" }
        require(from != to) { "Source and destination stack must not be equal" }
    }

    override fun toString(): String = "move $count from $from to $to"

    companion object {
        private val regex = Regex("""^move (?<count>\d+) from (?<from>\d+) to (?<to>\d+)$""")

        /**
         * Parses the readable movement string to a [MoveOperation].
         */
        fun parse(value: String): MoveOperation {
            val matchResult = requireNotNull(regex.matchEntire(value)) {
                "Value '$value' is not a valid MoveOperation"
            }

            return MoveOperation(
                count = matchResult.groups["count"]!!.value.toInt(),
                from = matchResult.groups["from"]!!.value.toInt(),
                to = matchResult.groups["to"]!!.value.toInt()
            )
        }
    }
}
