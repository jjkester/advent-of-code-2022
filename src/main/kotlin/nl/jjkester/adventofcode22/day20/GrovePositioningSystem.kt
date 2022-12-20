package nl.jjkester.adventofcode22.day20

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import nl.jjkester.adventofcode22.Implementation
import nl.jjkester.adventofcode22.Input

/**
 * https://adventofcode.com/2022/day/20
 */
object GrovePositioningSystem : Implementation<Flow<Long>> {

    /**
     * Input modeled as a flow of numbers.
     */
    override suspend fun prepareInput(input: Input): Flow<Long> = input.readLines()
        .map(String::toLong)

    /**
     * Finds the 1000th, 2000th, and 3000th value after zero (wrapping the list) after mixing a number of times and sums
     * them together. Multiplies each value with the decryption key before mixing the values.
     */
    suspend fun Flow<Long>.sumOfCoordinates(mixes: Int = 1, decryptionKey: Long = 1): Long = toList()
        .map { it * decryptionKey }
        .mix(mixes)
        .let { numbers ->
            numbers.indexOf(0).let { zero ->
                listOf(
                    numbers[(zero + 1000) % numbers.size],
                    numbers[(zero + 2000) % numbers.size],
                    numbers[(zero + 3000) % numbers.size],
                )
            }
        }
        .sum()

    /**
     * Returns a copy of this list that has been mixed the specified number of times.
     */
    fun List<Long>.mix(times: Int): List<Long> {
        val items = map(::Container).toMutableList()

        // Iterate over a copy to preserve the original order
        val originalOrder = items.toList()

        repeat(times) {
            originalOrder.forEach { item ->
                val index = items.indexOf(item)
                items.removeAt(index)
                val newIndex = ((index + item.value) % items.size)
                    .let { if (it <= 0) items.size + it else it }
                    .toInt()
                items.add(newIndex, item)
            }
        }

        return items.map(Container::value)
    }

    /**
     * Class to preserve item identity while mixing lists that contain the same number more than once.
     */
    private class Container(val value: Long)
}
