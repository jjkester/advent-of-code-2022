package nl.jjkester.adventofcode22.day22

import kotlinx.coroutines.flow.toList
import nl.jjkester.adventofcode22.Implementation
import nl.jjkester.adventofcode22.Input
import nl.jjkester.adventofcode22.chunkedBy
import nl.jjkester.adventofcode22.toPair
import org.jetbrains.kotlinx.multik.api.Multik
import org.jetbrains.kotlinx.multik.api.d2array
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.data.set
import org.jetbrains.kotlinx.multik.ndarray.data.view
import org.jetbrains.kotlinx.multik.ndarray.operations.indexOfFirst
import org.jetbrains.kotlinx.multik.ndarray.operations.indexOfLast

/**
 * https://adventofcode.com/2022/day/22
 */
object MonkeyMap : Implementation<Pair<WraparoundMap, List<Instruction>>> {

    /**
     * Input modeled as a pair of a map and a list of instructions.
     */
    override suspend fun prepareInput(input: Input): Pair<WraparoundMap, List<Instruction>> {
        val (rawMap, rawInstructions) = input.readLines()
            .chunkedBy { it.isBlank() }
            .toList()
            .toPair()

        return WraparoundMap(rawMap) to Instruction.parse(rawInstructions.single())
    }

    /**
     * Calculates the final password by traversing the map according to the instructions and computing the formula for
     * the end position.
     */
    fun Pair<WraparoundMap, List<Instruction>>.finalPasswordFlat(): Int = second
        .fold(first.start) { position, instruction -> first.move(position, instruction) }
        .let { (it.x + 1) * 4 + (it.y + 1) * 1000 + it.facing.ordinal }
}

/**
 * A wraparound map with obstacles.
 */
class WraparoundMap(lines: List<String>) {
    private val map: D2Array<Byte> = Multik.d2array(lines.maxOf { it.length }, lines.size) { -1 }

    init {
        lines.forEachIndexed { y, line ->
            line.forEachIndexed { x, char ->
                map[x, y] = when (char) {
                    '.' -> 0
                    '#' -> 1
                    else -> -1
                }
            }
        }
    }

    val start = Position(
        x = map.view(0, 1).indexOfFirst { it.toInt() == 0 },
        y = 0,
        facing = Direction.Right
    )

    fun move(position: Position, instruction: Instruction): Position = when (instruction) {
        Instruction.TurnLeft -> position.copy(facing = position.facing.turnLeft())
        Instruction.TurnRight -> position.copy(facing = position.facing.turnRight())
        is Instruction.Move -> {
            var pos = position
            for (i in 1..instruction.distance) {
                var moved = pos.moveOne()

                if (moved.x !in 0 until map.shape[0] || moved.y !in 0 until map.shape[1] || map[moved.x, moved.y] < 0) {
                    // Out of bounds, wrap around
                    moved = when (pos.facing) {
                        Direction.Down -> pos.copy(y = map[pos.x].indexOfFirst { it >= 0 })
                        Direction.Left -> pos.copy(x = map.view(pos.y, 1).indexOfLast { it >= 0 })
                        Direction.Up -> pos.copy(y = map[pos.x].indexOfLast { it >= 0 })
                        Direction.Right -> pos.copy(x = map.view(pos.y, 1).indexOfFirst { it >= 0 })
                    }
                }

                if (map[moved.x, moved.y] > 0) {
                    break
                }

                pos = moved
            }
            pos
        }
    }

    private operator fun Array<IntRange>.contains(position: Position): Boolean =
        position.x in first() && position.y in last()

    private fun Position.moveOne(): Position = copy(x = x + facing.x, y = y + facing.y)

    private fun Direction.turnLeft(): Direction = when (this) {
        Direction.Down -> Direction.Right
        Direction.Left -> Direction.Down
        Direction.Up -> Direction.Left
        Direction.Right -> Direction.Up
    }

    private fun Direction.turnRight(): Direction = when (this) {
        Direction.Down -> Direction.Left
        Direction.Left -> Direction.Up
        Direction.Up -> Direction.Right
        Direction.Right -> Direction.Down
    }

    override fun toString(): String = (0 until map.shape[1]).joinToString(System.lineSeparator()) { y ->
        (0 until map.shape[0]).joinToString("") { x ->
            val it = map[x, y]
            when {
                it < 0 -> " "
                it > 0 -> "#"
                else -> "."
            }
        }.trimEnd()
    }
}

/**
 * Instruction to move a number of places or to  rotate left or right.
 */
sealed class Instruction {
    data class Move(val distance: Int) : Instruction()
    object TurnLeft : Instruction() {
        override fun toString(): String = "TurnLeft"
    }

    object TurnRight : Instruction() {
        override fun toString(): String = "TurnRight"
    }

    companion object {
        private val regex = """(\d+|[LR])""".toRegex()

        fun parse(line: String): List<Instruction> = regex.findAll(line)
            .map { match ->
                when (val substring = match.value) {
                    "L" -> TurnLeft
                    "R" -> TurnRight
                    else -> Move(substring.toInt())
                }
            }
            .toList()
    }
}

/**
 * Direction to face.
 */
enum class Direction(val x: Int, val y: Int) {
    Right(1, 0),
    Down(0, 1),
    Left(-1, 0),
    Up(0, -1)
}

/**
 * Position on the map, including a direction.
 */
data class Position(val x: Int, val y: Int, val facing: Direction)
