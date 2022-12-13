package nl.jjkester.adventofcode22.day13

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.withIndex
import nl.jjkester.adventofcode22.Implementation
import nl.jjkester.adventofcode22.Input
import nl.jjkester.adventofcode22.chunkedBy
import nl.jjkester.adventofcode22.toPair

/**
 * https://adventofcode.com/2022/day/13
 */
object DistressSignal : Implementation<Flow<Pair<Packet, Packet>>> {

    /**
     * Input modeled as a flow of pairs of packets.
     */
    override suspend fun prepareInput(input: Input): Flow<Pair<Packet, Packet>> = input.readLines()
        .chunkedBy { it.isBlank() }
        .map { group -> group.map { PacketParser(it).parse() }.toPair() }

    /**
     * Orders the packets in each pair and sums the indices (1-indexed) of the pairs that were already in the correct
     * order.
     */
    suspend fun Flow<Pair<Packet, Packet>>.sumOfIndicesInRightOrder(): Int = this
        .map { (first, second) -> first.compareTo(second) }
        .withIndex()
        .filter { it.value < 1 }
        .fold(0) { acc, value -> acc + value.index + 1 }

    /**
     * Finds the decoder key by inserting divider packets into the (flattened) packets and sorting them.
     */
    suspend fun Flow<Pair<Packet, Packet>>.decoderKey(): Int {
        val dividerPackets = listOf(dividerPacket(2), dividerPacket(6))

        return flatMapConcat { it.toList().asFlow() }
            .toList()
            .let { it + dividerPackets }
            .sortedWith(Comparator(Packet::compareTo))
            .asSequence()
            .withIndex()
            .filter { it.value in dividerPackets }
            .map { it.index + 1 }
            .reduce(Int::times)
    }

    private fun dividerPacket(value: Int) = Packet.Many(listOf(Packet.Many(listOf(Packet.Single(value)))))

    private class PacketParser(private val packetString: String) {
        private var pointer: Int = 0

        fun parse(): Packet {
            return parseMany()
        }

        private fun parseMany(): Packet.Many {
            val list = mutableListOf<Packet>()

            check(read() == '[')

            while (pointer <= packetString.lastIndex) {
                when (peek()) {
                    '[' -> parseMany().also(list::add)
                    ']' -> break
                    ',' -> read()
                    else -> parseSingle().also(list::add)
                }
            }

            check(read() == ']')

            return Packet.Many(list)
        }

        private fun parseSingle(): Packet.Single = Packet.Single(readWhile { it.isDigit() }.toInt())

        private fun peek(): Char = packetString[pointer]

        private fun read(): Char = packetString[pointer++]

        private fun readWhile(predicate: (Char) -> Boolean): String = buildString {
            while (pointer <= packetString.lastIndex) {
                if (predicate(peek())) append(read()) else break
            }
        }
    }
}

/**
 * Packet consisting of many other packets or a single value.
 */
sealed class Packet {
    data class Many(val values: List<Packet>) : Packet()
    data class Single(val value: Int) : Packet()
}

operator fun Packet.compareTo(other: Packet): Int = when {
    this is Packet.Single && other is Packet.Single -> value.compareTo(other.value)
    this is Packet.Many && other is Packet.Many -> values.zip(other.values)
        .asSequence()
        .map { (first, second) -> first.compareTo(second) }
        .firstOrNull { it != 0 }
        ?: values.size.compareTo(other.values.size)
    else -> this.coerceMany().compareTo(other.coerceMany())
}

fun Packet.coerceMany(): Packet.Many = when (this) {
    is Packet.Many -> this
    is Packet.Single -> Packet.Many(listOf(this))
}
