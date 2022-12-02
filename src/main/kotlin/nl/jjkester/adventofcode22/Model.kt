package nl.jjkester.adventofcode22

import java.time.LocalDate
import java.time.Year

interface Implementation<T> {
    suspend fun prepareInput(input: Input): T
}

class AdventOfCode(
    val year: Year,
    val days: Set<Day>
)

data class Day(
    val date: LocalDate,
    val input: Input,
    val parts: Parts,
    val execution: ExecutionUnit<*>
)

data class Parts(
    val one: Part.One?,
    val two: Part.Two?
) : Iterable<Part> {
    override fun iterator(): Iterator<Part> = iterator {
        one?.let { yield(it) }
        two?.let { yield(it) }
    }
}

sealed interface Part {
    object One : Part
    object Two : Part
}

val Part.number: Int
    get() = when (this) {
        is Part.One -> 1
        is Part.Two -> 2
    }

val Part.name: String
    get() = when (this) {
        is Part.One -> "one"
        is Part.Two -> "two"
    }

class ExecutionUnit<T>(
    val parser: suspend (Input) -> T,
    val partOne: suspend (T) -> Output,
    val partTwo: suspend (T) -> Output,
) {

    suspend fun runPartOne(input: Input): Output = partOne(parser(input))

    suspend fun runPartTwo(input: Input): Output = partTwo(parser(input))

    suspend fun Part.run(input: Input): Output = when (this) {
        Part.One -> runPartOne(input)
        Part.Two -> runPartTwo(input)
    }
}
