package nl.jjkester.adventofcode22.day09

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import nl.jjkester.adventofcode22.Implementation
import nl.jjkester.adventofcode22.Input
import nl.jjkester.adventofcode22.toPair
import kotlin.math.absoluteValue

/**
 * https://adventofcode.com/2022/day/9
 */
object RopeBridge : Implementation<Flow<Movement>> {

    /**
     * Input modeled as a flow of individual movements of the head knot.
     */
    override suspend fun prepareInput(input: Input): Flow<Movement> = input.readLines()
        .map { Movement.parse(it) }
        .flatMapConcat { (movement, repeat) -> List(repeat) { movement }.asFlow() }

    /**
     * Calculates the number of positions that the tail knot will visit when applying the movements to the head knot.
     */
    suspend fun numberOfTailRopePositions(headMovements: Flow<Movement>, knots: Int): Int =
        ropeKnotPositions(Rope(knots), headMovements)
            .filter { it.index == knots - 1 }
            .toSet()
            .size

    /**
     * Flow emitting the changing positions of each knot in the rope. Applies the provided movements sequentially to the
     * provided rope.
     */
    suspend fun ropeKnotPositions(start: Rope, headMovements: Flow<Movement>): Flow<IndexedValue<Knot>> = flow {
        start.knots.withIndex().forEach { emit(it) }

        headMovements.fold(start) { rope, movement ->
            val effects = flow {
                // Move head
                val head = rope.head.move(movement).also { emit(IndexedValue(0, it)) }

                // Move remaining knots
                rope.knots.drop(1).foldIndexed(head) { index, preceding, knot ->
                    preceding.positionImmediateFollower(knot).also { emit(IndexedValue(index + 1, it)) }
                }
            }

            emitAll(effects)

            Rope(effects.map { it.value }.toList())
        }
    }

    private fun Knot.positionImmediateFollower(follower: Knot): Knot = when {
        x == follower.x -> follower.copy(y = follower.y.coerceIn(y.adjacent))
        y == follower.y -> follower.copy(x = follower.x.coerceIn(x.adjacent))
        else -> {
            val dx = (x - follower.x).absoluteValue
            val dy = (y - follower.y).absoluteValue

            when {
                dx == 2 && dy == 2 -> Knot((x + follower.x) / 2, (y + follower.y) / 2)
                dx == 2 -> copy(x = follower.x.coerceIn(x.adjacent))
                dy == 2 -> copy(y = follower.y.coerceIn(y.adjacent))
                else -> follower
            }
        }
    }

    private val Int.adjacent: IntRange
        get() = this - 1..this + 1

    private fun Knot.move(movement: Movement): Knot = when (movement) {
        Movement.Up -> copy(y = y + 1)
        Movement.Left -> copy(x = x - 1)
        Movement.Right -> copy(x = x + 1)
        Movement.Down -> copy(y = y - 1)
    }

    /**
     * Prints the rope state.
     *
     * Ranges for example: -11 until 15, -5 until 15
     */
    @Suppress("unused")
    private fun Rope.print(horizontal: IntRange, vertical: IntRange) {
        val data = vertical.map { y ->
            horizontal.map { x ->
                when (val index = knots.indexOfFirst { it.x == x && it.y == y }) {
                    -1 -> if (x == 0 && y == 0) "s" else "."
                    0 -> "H"
                    else -> "$index"
                }
            }
        }

        data.asReversed().forEach {
            println(it.joinToString(""))
        }
    }

    /**
     * Prints the knots as #.
     *
     * Intended to print the positions that were visited by the tail of the rope.
     *
     * Ranges for example: -11 until 15, -5 until 15
     */
    @Suppress("unused")
    private fun Iterable<Knot>.print(horizontal: IntRange, vertical: IntRange) {
        val data = vertical.map { y ->
            horizontal.map { x ->
                if (find { it.x == x && it.y == y } == null) {
                    if (x == 0 && y == 0) "s" else "."
                } else {
                    "#"
                }
            }
        }

        data.asReversed().forEach {
            println(it.joinToString(""))
        }
    }
}

/**
 * Movement of the head knot.
 */
sealed class Movement {
    object Up : Movement()
    object Left : Movement()
    object Right : Movement()
    object Down : Movement()

    override fun toString() = this::class.simpleName!!

    companion object {
        /**
         * Parses the [line] to a movement of the head knot.
         */
        fun parse(line: String): Pair<Movement, Int> {
            val (char, int) = line.split(' ').toPair()

            val movement = when (char) {
                "U" -> Up
                "L" -> Left
                "R" -> Right
                "D" -> Down
                else -> error("Instruction '$char' is not a valid movement")
            }
            val times = checkNotNull(int.toIntOrNull()) { "Repetition '$int' is not a valid number" }
            require(times > 0) { "Repetition '$times' cannot be performed" }

            return movement to times
        }
    }
}

/**
 * Model of the rope with a variable number of knots.
 */
data class Rope(val knots: List<Knot>) {
    init {
        require(knots.isNotEmpty()) { "A rope must contain at least 1 knot" }
    }
}

/**
 * Creates a new rope with the provided number of knots. All knots will start at the zero position.
 */
fun Rope(size: Int): Rope {
    require(size > 0) { "A rope must contain at least 1 knot" }
    return Rope(List(size) { Knot(0, 0) })
}

/**
 * Head knot of the rope.
 */
val Rope.head: Knot get() = knots.first()

/**
 * Representation of a knot in a certain position.
 */
data class Knot(val x: Int, val y: Int)
