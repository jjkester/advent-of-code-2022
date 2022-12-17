package nl.jjkester.adventofcode22.day17

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.toList
import nl.jjkester.adventofcode22.Implementation
import nl.jjkester.adventofcode22.Input

/**
 * https://adventofcode.com/2022/day/17
 */
object PyroclasticFlow : Implementation<Flow<Jet>> {

    /**
     * Input modeled as a flow of jets.
     */
    override suspend fun prepareInput(input: Input): Flow<Jet> = input.readChars()
        .takeWhile { it !in System.lineSeparator() }
        .map { Jet.parse(it) }

    /**
     * Calculates the height of the rocks after the [count] of rocks has dropped.
     */
    suspend fun Flow<Jet>.heightAfterRocks(count: Long): Long = Room(7, toList())
        .apply {
            (0L until count).forEach {
                nextRock()
                if (it % 100 == 0L) optimize()
            }
        }
        .height
}

private class Room(val width: Int, jets: List<Jet>) {
    private var fill: MutableList<BooleanArray> = mutableListOf()

    private var optimizedHeight: Long = 0L

    val height: Long
        get() = optimizedHeight + fill.size

    private val jets: Iterator<Jet> = iterator {
        while (true) {
            jets.forEach { yield(it) }
        }
    }

    private val rocks: Iterator<Rock> = iterator {
        while (true) {
            yield(Rock.Minus(2, height + 3))
            yield(Rock.Plus(2, height + 3))
            yield(Rock.InverseL(2, height + 3))
            yield(Rock.Pipe(2, height + 3))
            yield(Rock.Square(2, height + 3))
        }
    }

    fun nextRock() {
        with(rocks.next()) {
            while (true) {
                if (!move(jets.next())) break
            }
            commit()
        }
    }

    fun optimize() {
        val completedRow = fill.asSequence().withIndex().findLast { (_, row) ->
            row.all { it }
        }

        if (completedRow != null) {
            fill = fill.subList(completedRow.index + 1, fill.size)
            optimizedHeight += completedRow.index + 1
        }
    }

    private fun Rock.move(jet: Jet): Boolean {
        val xOffset = jet.xOffset

        if (!coordinates.isOccupied(xOffset, 0)) {
            x += xOffset
        }

        return if (!coordinates.isOccupied(0, -1)) {
            y -= 1
            true
        } else {
            false
        }
    }

    private fun Rock.commit() {
        coordinates.forEach { (x, y) ->
            if (fill.lastIndex < y - optimizedHeight) {
                fill.addAll(List((y - optimizedHeight - fill.lastIndex).toInt()) { BooleanArray(this@Room.width) })
            }
            fill[(y - optimizedHeight).toInt()][x] = true
        }
    }

    private fun List<Coordinate>.isOccupied(xOffset: Int, yOffset: Int): Boolean =
        any { it.isOccupied(xOffset, yOffset) }

    private fun Coordinate.isOccupied(xOffset: Int, yOffset: Int): Boolean = when {
        x + xOffset !in 0 until width -> true
        y + yOffset < optimizedHeight -> true
        else -> {
            fill.getOrNull((y + yOffset - optimizedHeight).toInt())?.get(x + xOffset) ?: false
        }
    }
}

/**
 * Directional jet moving a rock on the x-axis with the specified [xOffset].
 */
sealed class Jet(val xOffset: Int) {
    object Left : Jet(-1)
    object Right : Jet(1)

    companion object {
        fun parse(char: Char): Jet = when (char) {
            '<' -> Left
            '>' -> Right
            else -> error("Invalid jet: '$char'")
        }
    }
}

/**
 * A rock on a mutable x, y position.
 */
sealed class Rock private constructor(
    var x: Int,
    var y: Long,
    val relativeCoordinates: List<Coordinate>
) {

    init {
        require(relativeCoordinates.minOf { it.x } == 0) {
            "The left edge must start at x=0"
        }
        require(relativeCoordinates.minOf { it.y } == 0L) {
            "The bottom edge must start at y=0"
        }
        require(relativeCoordinates.distinct().size == relativeCoordinates.size) {
            "Duplicate coordinates are not allowed"
        }
    }

    val coordinates: List<Coordinate>
        get() = relativeCoordinates.map { (x, y) -> Coordinate(x + this.x, y + this.y) }

    class Minus(x: Int, y: Long) : Rock(
        x = x,
        y = y,
        relativeCoordinates = listOf(0 by 0, 1 by 0, 2 by 0, 3 by 0)
    )

    class Plus(x: Int, y: Long) : Rock(
        x = x,
        y = y,
        relativeCoordinates = listOf(1 by 0, 0 by 1, 1 by 1, 2 by 1, 1 by 2)
    )

    class InverseL(x: Int, y: Long) : Rock(
        x = x,
        y = y,
        relativeCoordinates = listOf(0 by 0, 1 by 0, 2 by 0, 2 by 1, 2 by 2)
    )

    class Pipe(x: Int, y: Long) : Rock(
        x = x,
        y = y,
        relativeCoordinates = listOf(0 by 0, 0 by 1, 0 by 2, 0 by 3)
    )

    class Square(x: Int, y: Long) : Rock(
        x = x,
        y = y,
        relativeCoordinates = listOf(0 by 0, 1 by 0, 0 by 1, 1 by 1)
    )
}

data class Coordinate(val x: Int, val y: Long)

infix fun Int.by(other: Long) = Coordinate(this, other)
