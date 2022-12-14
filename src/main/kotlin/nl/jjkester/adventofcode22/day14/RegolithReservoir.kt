package nl.jjkester.adventofcode22.day14

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import nl.jjkester.adventofcode22.Implementation
import nl.jjkester.adventofcode22.Input
import org.jetbrains.kotlinx.multik.api.Multik
import org.jetbrains.kotlinx.multik.api.d2array
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.data.set
import java.lang.IndexOutOfBoundsException

/**
 * https://adventofcode.com/2022/day/14
 */
object RegolithReservoir : Implementation<Flow<Line>> {

    /**
     * Input modeled as a flow of lines.
     */
    override suspend fun prepareInput(input: Input): Flow<Line> = input.readLines()
        .map { line ->
            line.split(" -> ")
                .map { point ->
                    point.split(',')
                        .let { it.first().toInt() by it.last().toInt() }
                }
                .let { Line(it) }
        }

    /**
     * Counts the units of sand that can be dropped before they fall below the lowest line.
     */
    suspend fun Flow<Line>.unitsOfSandInCave(): Int = asCave()
        .let { (cave, sand) -> simulateSand(cave, sand) }
        .count()

    /**
     * Counts the units of sand that can be dropped before the sand covers the drop point. Assumes a floor 2 levels
     * below the lowest line.
     */
    suspend fun Flow<Line>.unitsOfSandUntilRest(): Int = asCave()
        .let { (cave, sand) ->
            val sizeY = cave.shape[0] + 2
            val sizeX = cave.shape[1] + (cave.shape[0] * 2) + 2
            val offsetLeft = (sizeX - cave.shape[1]) / 2

            val caveWithFloor = Multik.d2array(sizeY, sizeX) {
                val y = it / sizeX
                val x = (it % sizeX) - offsetLeft

                when {
                    y in 0 until cave.shape[0] && x in 0 until cave.shape[1] -> cave[y, x]
                    y == sizeY - 1 -> Fill.Rock
                    else -> Fill.Air
                }
            }

            simulateSand(caveWithFloor, sand.copy(x = sand.x + offsetLeft))
        }
        .count()

    private suspend fun simulateSand(cave: Cave, sand: Point): Flow<Point> = flow {
        // Loop for dropped sand units
        while (true) {
            val sandFallPath = generateSequence(sand) { previous ->
                when (Fill.Air) {
                    cave[previous.y + 1, previous.x] -> previous.x by previous.y + 1
                    cave[previous.y + 1, previous.x - 1] -> previous.x - 1 by previous.y + 1
                    cave[previous.y + 1, previous.x + 1] -> previous.x + 1 by previous.y + 1
                    else -> null // End sequence
                }
            }

            try {
                val sandLocation = sandFallPath.last()

                cave[sandLocation] = Fill.Sand
                emit(sandLocation)

                if (sandLocation == sand) {
                    break
                }
            } catch (ex: IndexOutOfBoundsException) {
                break
            }
        }
    }

    private suspend fun Flow<Line>.asCave(): Pair<Cave, Point> {
        val cave = Multik.d2array(1000, 1000) { Fill.Air }
        cave[0, 500] = 4

        var min = 500 by 0
        var max = 500 by 0

        collect { line ->
            line.points.windowed(2).forEach { (first, second) ->
                iterateLine(first, second).forEach { point ->
                    // Fill cave
                    cave[point] = Fill.Rock

                    // Update min/max
                    if (point.x <= min.x || point.y <= min.y) {
                        min = min.x.coerceAtMost(point.x) by min.y.coerceAtMost(point.y)
                    }
                    if (point.x >= max.x || point.y >= max.y) {
                        max = max.x.coerceAtLeast(point.x) by max.y.coerceAtLeast(point.y)
                    }
                }
            }
        }

        val sizeX = max.x - min.x + 1
        val sizeY = max.y - min.y + 1

        // Resize
        val resizedCave = Multik.d2array(sizeY, sizeX) { cave[(it / sizeX) + min.y, (it % sizeX) + min.x] }

        return resizedCave to (500 - min.x by 0 - min.y)
    }

    private fun iterateLine(start: Point, end: Point): Sequence<Point> = sequence {
        when {
            start.x == end.x -> {
                (start.y..end.y).forEach { yield(start.copy(y = it)) }
                (end.y..start.y).forEach { yield(start.copy(y = it)) }
            }
            start.y == end.y -> {
                (start.x..end.x).forEach { yield(start.copy(x = it)) }
                (end.x..start.x).forEach { yield(start.copy(x = it)) }
            }
            else -> error("Diagonal lines not supported")
        }
    }
}

/**
 * A point in a coordinate system.
 */
data class Point(val x: Int, val y: Int)

infix fun Int.by(other: Int) = Point(this, other)

/**
 * A line through multiple [points].
 */
data class Line(val points: List<Point>)

/**
 * Cave modeled as an array of bytes.
 */
typealias Cave = D2Array<Byte>

/**
 * Constants to indicate what a point is filled with.
 */
object Fill {
    val Air: Byte = 0
    val Rock: Byte = 1
    val Sand: Byte = 2
}

operator fun Cave.get(point: Point): Byte = this[point.y, point.x]
operator fun Cave.set(point: Point, value: Byte) {
    this[point.y, point.x] = value
}
