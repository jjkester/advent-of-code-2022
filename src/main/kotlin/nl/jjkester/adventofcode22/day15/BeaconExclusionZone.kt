package nl.jjkester.adventofcode22.day15

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import nl.jjkester.adventofcode22.Implementation
import nl.jjkester.adventofcode22.Input
import kotlin.math.absoluteValue

/**
 * https://adventofcode.com/2022/day/15
 */
object BeaconExclusionZone : Implementation<Flow<Pair<Sensor, Beacon>>> {

    /**
     * Input modeled as a flow of pairs of a sensor and a beacon.
     */
    override suspend fun prepareInput(input: Input): Flow<Pair<Sensor, Beacon>> = input.readLines()
        .map { line ->
            checkNotNull(parseRegex.matchEntire(line)) { "Line does not match a sensor report" }
                .run {
                    Sensor(getInt("sx"), getInt("sy")) to Beacon(getInt("bx"), getInt("by"))
                }
        }


    /**
     * Calculates the number of positions in the given row where a beacon cannot be present.
     */
    suspend fun Flow<Pair<Sensor, Beacon>>.positionsInRowWhereBeaconCannotBePresent(row: Int): Int {
        val ranges = toList()

        val cols = ranges.fold(IntRange.EMPTY) { acc, (sensor, beacon) ->
            val distance = sensor distanceTo beacon
            acc.first.coerceAtMost(sensor.x - distance)..acc.last.coerceAtLeast(sensor.x + distance)
        }

        return cols.count { col ->
            Beacon(col, row).let { beacon -> ranges.any { beacon != it.second && beacon in it } }
        }
    }

    /**
     * Calculates the tuning frequency of the distress signal by finding the only place in the rectangular [searchRange]
     * where a beacon may be present.
     */
    suspend fun Flow<Pair<Sensor, Beacon>>.distressSignalTuningFrequency(searchRange: IntRange): Long = toList()
        .run {
            searchRange.asSequence()
                .map { y -> mapNotNull { it.rangeInRow(y, searchRange) }.merge() }
                .withIndex()
                .single { it.value.size == 2 && it.value.last().first - it.value.first().last == 2 }
                .let { (index, it) -> Beacon(it.first().last + 1, index) }
                .let { (x, y) -> x * 4_000_000L + y }
        }

    private fun Pair<Sensor, Beacon>.rangeInRow(row: Int, limit: IntRange): IntRange? = let { (sensor, beacon) ->
        val distance = sensor distanceTo beacon
        val xDistance = distance - (sensor.y - row).absoluteValue

        return if (xDistance >= 0) {
            val lower = (sensor.x - xDistance).coerceAtLeast(limit.first)
            val upper = (sensor.x + xDistance).coerceAtMost(limit.last)
            lower..upper
        } else {
            null
        }
    }

    private fun List<IntRange>.merge(): List<IntRange> = this
        .sortedWith(Comparator.comparingInt(IntRange::first).thenComparingInt(IntRange::last))
        .run {
            mutableListOf(first()).also { combinedRanges ->
                drop(1).forEach { range ->
                    val previous = combinedRanges.last()

                    when {
                        range.last <= previous.last -> Unit
                        range.first <= previous.last + 1 -> combinedRanges.add(combinedRanges.removeLast().first..range.last)
                        else -> combinedRanges.add(range)
                    }
                }
            }
        }

    private operator fun Pair<Sensor, Beacon>.contains(other: Position): Boolean =
        first distanceTo other <= first distanceTo second

    private infix fun Position.distanceTo(other: Position): Int =
        (x - other.x).absoluteValue + (y - other.y).absoluteValue

    private fun MatchResult.getInt(key: String) = groups[key]!!.value.toInt()

    private val parseRegex = """
            ^Sensor at x=(?<sx>-?\d+), y=(?<sy>-?\d+): closest beacon is at x=(?<bx>-?\d+), y=(?<by>-?\d+)$
        """.trimIndent().toRegex()
}

/**
 * Interface of a position.
 */
interface Position {
    val x: Int
    val y: Int
}

/**
 * Sensor with a position.
 */
data class Sensor(override val x: Int, override val y: Int) : Position

/**
 * Beacon with a position.
 */
data class Beacon(override val x: Int, override val y: Int) : Position
