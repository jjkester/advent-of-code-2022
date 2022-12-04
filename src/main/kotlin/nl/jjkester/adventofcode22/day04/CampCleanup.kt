package nl.jjkester.adventofcode22.day04

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.map
import nl.jjkester.adventofcode22.Implementation
import nl.jjkester.adventofcode22.Input
import nl.jjkester.adventofcode22.toPair

/**
 * https://adventofcode.com/2022/day/4
 */
object CampCleanup : Implementation<Flow<Pair<IntRange, IntRange>>> {

    /**
     * Input modeled as a pair of ranges of sections covered by a pair of elves.
     */
    override suspend fun prepareInput(input: Input): Flow<Pair<IntRange, IntRange>> = input.readLines()
        .map { line ->
            line.split(',')
                .map { range -> range.split('-').map(String::toInt) }
                .map { (left, right) -> left..right }
                .toPair()
        }

    /**
     * Calculates the number of pairs for which one section fully contains the other section.
     */
    suspend fun numberOfFullyContainedRanges(assignments: Flow<Pair<IntRange, IntRange>>): Int = assignments
        .count { (left, right) -> left in right || right in left }

    /**
     * Calculates the number of pairs for which one section overlaps the other section.
     */
    suspend fun numberOfOverlappingRanges(assignments: Flow<Pair<IntRange, IntRange>>): Int = assignments
        .count { (left, right) -> left overlaps right }

    /**
     * Whether this range fully contains the [other] range.
     */
    private operator fun IntRange.contains(other: IntRange): Boolean = other.first in this && other.last in this

    /**
     * Whether this range overlaps the [other] range.
     */
    private infix fun IntRange.overlaps(other: IntRange): Boolean {
        return other.first in this || other.last in this || first in other || last in other
    }
}
