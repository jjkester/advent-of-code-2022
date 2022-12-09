package nl.jjkester.adventofcode22.day09

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toSet
import nl.jjkester.adventofcode22.Implementation
import nl.jjkester.adventofcode22.Input
import nl.jjkester.adventofcode22.toPair
import kotlin.math.absoluteValue

object RopeBridge : Implementation<Flow<Movement>> {

    override suspend fun prepareInput(input: Input): Flow<Movement> = input.readLines()
        .map { Movement.parse(it) }
        .flatMapConcat { (movement, repeat) -> List(repeat) { movement }.asFlow() }

    suspend fun numberOfTailRopePositions(headMovements: Flow<Movement>): Int {
        return tailRopePositions(Rope.start, headMovements).toSet().size
    }

    suspend fun tailRopePositions(start: Rope, headMovements: Flow<Movement>): Flow<Pair<Int, Int>> = flow {
        emit(start.tail)

        headMovements.fold(start) { previous, movement ->
            val head = previous.head.move(movement)

            val tail = if (previous.isDiagonal && head awayFrom previous.tail) {
                when (movement) {
                    Movement.Down, Movement.Up -> head.first to previous.tail.second.coerceIn(head.second.closeRange())
                    Movement.Left, Movement.Right -> previous.tail.first.coerceIn(head.first.closeRange()) to head.second
                }
            } else {
                previous.tail.first.coerceIn(head.first.closeRange()) to previous.tail.second.coerceIn(head.second.closeRange())
            }

            emit(tail)

            Rope(head, tail)
        }
    }

    private fun Int.closeRange(): IntRange = this - 1..this + 1

    private infix fun Pair<Int, Int>.awayFrom(other: Pair<Int, Int>): Boolean =
        (first - other.first).absoluteValue > 1 || (second - other.second).absoluteValue > 1

    private val Rope.isDiagonal: Boolean
        get() =
            (head.first - tail.first).absoluteValue == 1 && (head.second - tail.second).absoluteValue == 1

    private fun Pair<Int, Int>.move(movement: Movement): Pair<Int, Int> = when (movement) {
        Movement.Up -> copy(second = second + 1)
        Movement.Left -> copy(first = first - 1)
        Movement.Right -> copy(first = first + 1)
        Movement.Down -> copy(second = second - 1)
    }
}

sealed class Movement {
    object Up : Movement()
    object Left : Movement()
    object Right : Movement()
    object Down : Movement()

    override fun toString() = this::class.simpleName!!

    companion object {
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

data class Rope(val head: Pair<Int, Int>, val tail: Pair<Int, Int>) {
    companion object {
        val zero = 0 to 0
        val start = Rope(zero, zero)
    }
}
