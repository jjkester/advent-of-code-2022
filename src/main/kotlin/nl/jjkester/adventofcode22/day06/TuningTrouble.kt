package nl.jjkester.adventofcode22.day06

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.withIndex
import nl.jjkester.adventofcode22.Implementation
import nl.jjkester.adventofcode22.Input
import nl.jjkester.adventofcode22.windowed

/**
 * https://adventofcode.com/2022/day/6
 */
object TuningTrouble : Implementation<Flow<Char>> {

    /**
     * Input modeled as a sequence of characters.
     */
    override suspend fun prepareInput(input: Input): Flow<Char> = input.readChars()

    /**
     * Calculates the point at which the first group of the provided [size] with all distinct characters starts.
     */
    suspend fun offsetToFirstDistinctGroup(data: Flow<Char>, size: Int): Int = data
        .windowed(size)
        .withIndex()
        .filter { it.value.toSet().size == size }
        .first()
        .index + size
}
