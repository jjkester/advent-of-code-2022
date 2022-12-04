package nl.jjkester.adventofcode22.day03

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.reduce
import nl.jjkester.adventofcode22.Implementation
import nl.jjkester.adventofcode22.Input
import nl.jjkester.adventofcode22.chuncked

/**
 * https://adventofcode.com/2022/day/3
 */
object RucksackReorganization : Implementation<Flow<String>> {

    /**
     * Input modeled as a string containing the types of contents in a single rucksack.
     */
    override suspend fun prepareInput(input: Input): Flow<String> = input.readLines()

    /**
     * Calculates the number of types that appear in both compartments of a single rucksack and sums the priority values
     * of these types.
     */
    suspend fun sumOfTypesInEachCompartment(rucksacks: Flow<String>): Int = rucksacks
        .map { line -> (line.length / 2).let { line.take(it) to line.takeLast(it) } }
        .flatMapConcat { (left, right) -> left.toSet().intersect(right.toSet()).asFlow() }
        .fold(0) { acc, value -> acc + value.priority }

    /**
     * Calculates the badge of each group of three rucksacks (type that appears in all three rucksacks) and sums the
     * priority values.
     */
    suspend fun sumOfBadges(rucksacks: Flow<String>): Int = rucksacks
        .chuncked(3)
        .map { group ->
            group
                .fold(priorityList.toSet()) { acc, s -> acc.intersect(s.toSet()) }
                .sumOf { it.priority }
        }
        .reduce { accumulator, value -> accumulator + value }

    /**
     * Priority of this type.
     */
    private val Char.priority: Int get() = priorityList.indexOf(this) + 1

    private val priorityList: List<Char> = ('a'..'z') + ('A'..'Z')

}
