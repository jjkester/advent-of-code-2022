package nl.jjkester.adventofcode22.day12

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.withIndex
import nl.jjkester.adventofcode22.Implementation
import nl.jjkester.adventofcode22.Input
import org.jetbrains.kotlinx.multik.api.toNDArray
import org.jetbrains.kotlinx.multik.ndarray.data.D2
import org.jetbrains.kotlinx.multik.ndarray.data.NDArray
import org.jetbrains.kotlinx.multik.ndarray.data.get

/**
 * https://adventofcode.com/2022/day/12
 */
object HillClimbingAlgorithm : Implementation<Puzzle> {

    /**
     * Input modeled as a 2d array of heights, the start point, and the end point.
     */
    override suspend fun prepareInput(input: Input): Puzzle {
        var start: Coordinate? = null
        var end: Coordinate? = null

        val grid = input.readLines()
            .withIndex()
            .map { (y, line) ->
                line.asIterable().mapIndexed { x, char ->
                    when (char) {
                        'S' -> 0.also { start = x by y }
                        'E' -> 25.also { end = x by y }
                        in 'a'..'z' -> char.code - 'a'.code
                        else -> error("")
                    }
                }.toIntArray()
            }
            .toList()
            .toTypedArray()
            .toNDArray()

        return Puzzle(
            heightmap = grid,
            start = checkNotNull(start) { "No starting position found" },
            end = checkNotNull(end) { "No finish position found" }
        )
    }

    /**
     * Calculates the lowest number of steps to go from the start to the end points.
     */
    fun Puzzle.shortestSteps(): Int = checkNotNull(heightmap.dijkstra(start)[end]) {
        "End not reachable"
    }

    /**
     * Calculates the lowest number of steps to go from any of the lowest points to the end point.
     */
    fun Puzzle.shortestTrail(): Int = heightmap.dijkstra(end, true)
        .filterKeys { heightmap[it] == 0 }
        .minBy { (_, it) -> it }
        .value

    private fun Heightmap.dijkstra(start: Coordinate, reverseCost: Boolean = false): Map<Coordinate, Int> {
        val visited = mutableSetOf<Coordinate>()
        val found = mutableMapOf(start to listOf(start))
        val queued = mutableSetOf(start)

        while (queued.isNotEmpty()) {
            val node = queued.minBy { found[it]!!.size }
            val pathToNode = found[node]!!

            queued.remove(node)
            visited.add(node)

            candidatesFrom(node, reverseCost).filter { it !in visited }.forEach { adjacentNode ->
                queued.add(adjacentNode)

                val extendedPath = pathToNode + adjacentNode

                found.merge(adjacentNode, extendedPath) { first, second ->
                    listOf(first, second).minBy { it.size }
                }
            }
        }

        return found.mapValues { (_, it) -> it.size - 1 }
    }

    private fun Heightmap.candidatesFrom(coordinate: Coordinate, reverseCost: Boolean): Set<Coordinate> {
        val left = coordinate.run { copy(x = x - 1) }
        val right = coordinate.run { copy(x = x + 1) }
        val up = coordinate.run { copy(y = y - 1) }
        val down = coordinate.run { copy(y = y + 1) }

        return listOf(left, right, up, down).filter { adjacentNode ->
            getOrNull(adjacentNode)
                ?.takeIf {
                    val delta = it - this[coordinate]
                    if (reverseCost) delta >= -1 else delta <= 1
                } != null
        }.toSet()
    }

    private operator fun Heightmap.get(coordinate: Coordinate): Int = this[coordinate.y, coordinate.x]

    private operator fun Heightmap.contains(coordinate: Coordinate): Boolean = this.shape.run {
        coordinate.y in 0 until this[0] && coordinate.x in 0 until this[1]
    }

    private fun Heightmap.getOrNull(coordinate: Coordinate): Int? = if (coordinate in this) {
        this[coordinate]
    } else {
        null
    }
}

/**
 * Puzzle input containing the heightmap and start and end coordinates.
 */
class Puzzle(val heightmap: Heightmap, val start: Coordinate, val end: Coordinate)

/**
 * Type alias for the heightmap, a 2D array of integers (heights).
 */
typealias Heightmap = NDArray<Int, D2>

/**
 * 2D coordinate.
 */
data class Coordinate(val x: Int, val y: Int)

infix fun Int.by(other: Int): Coordinate = Coordinate(this, other)
