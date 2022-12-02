package nl.jjkester.adventofcode22.day01

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import nl.jjkester.adventofcode22.Implementation
import nl.jjkester.adventofcode22.Input
import nl.jjkester.adventofcode22.chunkedBy

object CalorieCounting : Implementation<Iterable<Int>> {

    override suspend fun prepareInput(input: Input): Iterable<Int> = input.readLines()
        .chunkedBy { it.isEmpty() }
        .map { it.sumOf(String::toInt) }
        .toList()

    fun highestAmountCarried(elves: Iterable<Int>): Int = elves.max()

    fun topThreeCarried(elves: Iterable<Int>): Int = elves.sorted().takeLast(3).sum()
}
