package nl.jjkester.adventofcode22.day10

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.withIndex
import nl.jjkester.adventofcode22.Implementation
import nl.jjkester.adventofcode22.Input
import nl.jjkester.adventofcode22.chuncked

/**
 * https://adventofcode.com/2022/day/10
 */
object CathodeRayTube : Implementation<Flow<Instruction>> {

    /**
     * Input modeled as a flow of CPU instructions.
     */
    override suspend fun prepareInput(input: Input): Flow<Instruction> = input.readLines()
        .map { Instruction.parse(it) }

    /**
     * Calculates the sum of the signal strengths on every line of screen output, at the 20th pixel.
     */
    suspend fun Flow<Instruction>.sumOfSignalStrengths(): Long = runProgram()
        .withIndex()
        .filter { it.index % 40 == 20 }
        .map { it.signalStrength }
        .fold(0L) { acc, value -> acc + value }

    /**
     * Renders the screen output from the program.
     */
    suspend fun Flow<Instruction>.renderScreen(): String = runProgram()
        .dropWhile { it == Register.uninitialized }
        .drawScreen()
        .toList()
        .joinToString(System.lineSeparator()) { line ->
            line.joinToString("") { if (it) "#" else "." }
        }

    /**
     * Transforms the program output to CRT pixel on/off values.
     */
    private fun Flow<Register>.drawScreen(): Flow<BooleanArray> = chuncked(40, true)
        .map { line ->
            line.mapIndexed { pixel, spritePosition ->
                val sprite = spritePosition.value - 1..spritePosition.value + 1
                pixel in sprite
            }.toBooleanArray()
        }

    /**
     * Runs the program, transforming the instructions to register values at every tick.
     */
    suspend fun Flow<Instruction>.runProgram(): Flow<Register> = flow {
        val initialState = Register(1)

        // Dummy value for indexing purposes
        emit(Register.uninitialized)

        fold(initialState) { register, instruction ->
            // Repeat current register value the appropriate amount of times
            repeat(instruction.cycles) {
                emit(register)
            }

            // Continue with updated register value
            register.apply(instruction)
        }
    }

    private fun Register.apply(instruction: Instruction): Register = when (instruction) {
        is Instruction.Add -> Register(value + instruction.value)
        Instruction.NoOp -> this
    }

    private val IndexedValue<Register>.signalStrength: Int get() = value.value * index
}

/**
 * Program instruction. Each type of instruction can take a number of [cycles] to complete.
 */
sealed class Instruction(val cycles: Int) {
    /**
     * Do nothing and wait.
     */
    object NoOp : Instruction(1) {
        override fun toString(): String = NoOp::class.simpleName!!
    }

    /**
     * Add the [value] to the [Register].
     */
    data class Add(val value: Int) : Instruction(2)

    companion object {
        /**
         * Parses the [line] to an instruction.
         */
        fun parse(line: String): Instruction {
            val segments = line.split(' ')

            return when (val cmd = segments.first()) {
                "noop" -> NoOp
                "addx" -> Add(segments.last().toInt())
                else -> error("Command '$cmd' is not known")
            }
        }
    }
}

/**
 * Value of a CPU register.
 */
@JvmInline
value class Register(val value: Int) {
    companion object {
        val uninitialized = Register(Int.MIN_VALUE)
    }
}
