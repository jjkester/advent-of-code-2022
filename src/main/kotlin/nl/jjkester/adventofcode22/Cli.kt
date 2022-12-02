@file:OptIn(ExperimentalCli::class)

package nl.jjkester.adventofcode22

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.default
import kotlinx.coroutines.runBlocking
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

fun AdventOfCode.cli(args: Array<String>, programName: String = "aoc") {
    val parser = ArgParser(programName)

    val run = RunCommand(this)
    val list = ListCommand(this)

    parser.subcommands(run, list)

    if (parser.parse(args).commandName.isEmpty()) {
        parser.parse(arrayOf("--help"))
    }
}

private class RunCommand(private val aoc: AdventOfCode) : Subcommand("run", "Run Advent of Code") {

    private val latest by option(
        type = ArgType.Boolean,
        fullName = "latest",
        shortName = "l",
        description = "Run the latest day only"
    ).default(false)

    private val benchmark by option(
        type = ArgType.Boolean,
        fullName = "benchmark",
        shortName = "b",
        description = "Time the execution of each day and part"
    ).default(false)

    @OptIn(ExperimentalTime::class)
    override fun execute() {
        val days = aoc.days.sortedBy { it.date }.run {
            if (latest) takeLast(1) else this
        }

        days.forEachIndexed { index, day ->
            println("Day ${day.date.dayOfMonth}")

            day.parts.forEach { part ->
                val (output, duration) = day.execution.runTimed(part, day.input)
                print("  ${"\u2605".repeat(part.number)}${" ".repeat(2 - part.number)} ${output.format()}")
                if (benchmark) println(" [$duration]") else println()
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun ExecutionUnit<*>.runTimed(part: Part, input: Input) = runBlocking {
        measureTimedValue { part.run(input) }
    }
}

private class ListCommand(private val aoc: AdventOfCode) : Subcommand("list", "List Advent of Code progress") {

    override fun execute() {
        println("Advent of Code ${aoc.year}: ${aoc.days.size}/25 days")

        val days = aoc.days.sortedBy { it.date }

        days.forEachIndexed { index, day ->
            println("  Day ${day.date.dayOfMonth} (${day.date}) ${"\u2605".repeat(day.parts.count())}")
            println("    Input: ${day.input.format()}")
        }
    }
}
