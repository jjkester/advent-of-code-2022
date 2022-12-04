package nl.jjkester.adventofcode22.day01

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import nl.jjkester.adventofcode22.Implementation
import nl.jjkester.adventofcode22.Input
import nl.jjkester.adventofcode22.chunkedBy

/**
 * https://adventofcode.com/2022/day/1
 */
object CalorieCounting : Implementation<Iterable<Int>> {

    /**
     * Input consists of the sums of the calories carried by each elf.
     */
    override suspend fun prepareInput(input: Input): Iterable<Int> = input.readLines()
        .chunkedBy { it.isEmpty() }
        .map { it.sumOf(String::toInt) }
        .toList()

    /**
     * Calculates the highest amount of calories carried by a single elf.
     */
    fun highestAmountCarried(elves: Iterable<Int>): Int = elves.max()

    /**
     * Calculates the sum of the top thee amounts of calories carried by elves.
     */
    fun topThreeCarried(elves: Iterable<Int>): Int = elves.sorted().takeLast(3).sum()
}
