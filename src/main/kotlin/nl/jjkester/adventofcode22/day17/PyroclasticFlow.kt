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
        .apply { simulateRocks(count) }
        .height
}

private class Room(val width: Int, jets: List<Jet>) {
    private var fill: MutableList<BooleanArray> = mutableListOf()

    private var cachedHeight: Long = 0L

    val height: Long
        get() = cachedHeight + fill.size

    private val jets: Iterator<IndexedValue<Jet>> = iterator {
        while (true) {
            jets.withIndex().forEach { yield(it) }
        }
    }

    private val rocks: Iterator<Rock> = iterator {
        while (true) {
            yield(Rock.Minus(2, fill.size + 3))
            yield(Rock.Plus(2, fill.size + 3))
            yield(Rock.InverseL(2, fill.size + 3))
            yield(Rock.Pipe(2, fill.size + 3))
            yield(Rock.Square(2, fill.size + 3))
        }
    }

    private val cache = mutableMapOf<CacheKey, CacheValue>()

    fun simulateRocks(count: Long) {
        var i = 0L
        while (i < count) {
            with(rocks.next()) {
                while (true) {
                    val (jetIndex, jet) = jets.next()
                    if (!move(jet)) {
                        if (i > 1000) {
                            cache.compute(cacheKey(jetIndex)) { _, value ->
                                value?.also {
                                    val iInc = i - it.i
                                    val heightInc = height - it.height

                                    while (i < count - iInc) {
                                        i += iInc
                                        cachedHeight += heightInc
                                    }
                                }
                                CacheValue(i, height)
                            }
                        }
                        break
                    }
                }
                commit()
            }
            i++
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
            if (fill.lastIndex < y) {
                fill.addAll(List(y - fill.lastIndex) { BooleanArray(this@Room.width) })
            }
            fill[y][x] = true
        }
    }

    private fun List<Coordinate>.isOccupied(xOffset: Int, yOffset: Int): Boolean =
        any { it.isOccupied(xOffset, yOffset) }

    private fun Coordinate.isOccupied(xOffset: Int, yOffset: Int): Boolean = when {
        x + xOffset !in 0 until width -> true
        y + yOffset < 0 -> true
        else -> {
            fill.getOrNull(y + yOffset)?.get(x + xOffset) ?: false
        }
    }

    private data class CacheKey(val rockType: String, val jetIndex: Int, val heights: List<Int>)

    private data class CacheValue(val i: Long, val height: Long)

    private fun Rock.cacheKey(jetIndex: Int): CacheKey = CacheKey(
        this::class.simpleName!!,
        jetIndex,
        List(width) { x ->
            (height - ((fill.lastIndex downTo 0).first { y -> fill[y][x] })).toInt()
        }
    )
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
    var y: Int,
    val relativeCoordinates: List<Coordinate>
) {

    init {
        require(relativeCoordinates.minOf { it.x } == 0) {
            "The left edge must start at x=0"
        }
        require(relativeCoordinates.minOf { it.y } == 0) {
            "The bottom edge must start at y=0"
        }
        require(relativeCoordinates.distinct().size == relativeCoordinates.size) {
            "Duplicate coordinates are not allowed"
        }
    }

    val coordinates: List<Coordinate>
        get() = relativeCoordinates.map { (x, y) -> Coordinate(x + this.x, y + this.y) }

    class Minus(x: Int, y: Int) : Rock(
        x = x,
        y = y,
        relativeCoordinates = listOf(0 by 0, 1 by 0, 2 by 0, 3 by 0)
    )

    class Plus(x: Int, y: Int) : Rock(
        x = x,
        y = y,
        relativeCoordinates = listOf(1 by 0, 0 by 1, 1 by 1, 2 by 1, 1 by 2)
    )

    class InverseL(x: Int, y: Int) : Rock(
        x = x,
        y = y,
        relativeCoordinates = listOf(0 by 0, 1 by 0, 2 by 0, 2 by 1, 2 by 2)
    )

    class Pipe(x: Int, y: Int) : Rock(
        x = x,
        y = y,
        relativeCoordinates = listOf(0 by 0, 0 by 1, 0 by 2, 0 by 3)
    )

    class Square(x: Int, y: Int) : Rock(
        x = x,
        y = y,
        relativeCoordinates = listOf(0 by 0, 1 by 0, 0 by 1, 1 by 1)
    )
}

data class Coordinate(val x: Int, val y: Int)

infix fun Int.by(other: Int) = Coordinate(this, other)
