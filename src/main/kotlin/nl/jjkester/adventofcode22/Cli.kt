@file:OptIn(ExperimentalCli::class)

package nl.jjkester.adventofcode22

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.default
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.system.exitProcess
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

fun AdventOfCode.cli(args: Array<String>, programName: String = "adventofcode22") {
    Cli(this, programName).execute(args)
}

private class Cli(private val aoc: AdventOfCode, programName: String) {
    private val parser = ArgParser(programName)

    fun execute(args: Array<String>) {
        listOf<(AdventOfCode) -> Subcommand>(::RunCommand, ::ListCommand)
            .map { it(aoc) }
            .toTypedArray()
            .also { parser.subcommands(*it) }

        parser.parse(args.takeUnless { it.isEmpty() } ?: arrayOf("--help"))
    }
}

private class RunCommand(private val aoc: AdventOfCode) : Subcommand(
    name = "run",
    actionDescription = "Run Advent of Code"
) {
    private val day by argument(
        type = dayOptionChoice(aoc.days),
        fullName = "day",
        description = "Day(s) to run"
    )

    private val benchmark by option(
        type = ArgType.Boolean,
        fullName = "benchmark",
        shortName = "b",
        description = "Time the execution of each day and part"
    ).default(false)

    private val stacktrace by option(
        type = ArgType.Boolean,
        fullName = "stacktrace",
        shortName = "s",
        description = "Print stacktrace when execution fails"
    ).default(false)

    private val timeout by option(
        type = ArgType.String,
        fullName = "timeout",
        shortName = "t",
        description = "Stop execution after number of seconds"
    ).default("2s")

    private val currentPuzzleDay: LocalDate
        get() = Instant.now().atOffset(ZoneOffset.ofHours(-5)).toLocalDate()

    @OptIn(ExperimentalTime::class)
    override fun execute() {
        val days = aoc.days
            .sortedBy { it.date }
            .run {
                when (val option = day) {
                    is DayOption.Day -> filter { it.date.dayOfMonth == option.value }
                    DayOption.Today -> filter { it.date == currentPuzzleDay }
                    DayOption.Latest -> takeLast(1)
                    DayOption.All -> this
                }
            }

        if (days.isEmpty()) {
            exitProcess(1)
        }

        days.forEach { day ->
            println("Day ${day.date.dayOfMonth}")

            day.parts.forEach { part ->
                print("  ${"*".repeat(part.number)}${" ".repeat(2 - part.number)}  ")
                val timeoutDuration = Duration.parse("PT$timeout")
                day.execution.runTimed(part, day.input, timeoutDuration)
                    .onSuccess { (output, duration) ->
                        println(output.format(7))
                        if (benchmark) println("      @ $duration")
                    }
                    .onFailure { exception ->
                        when (exception) {
                            is TimeoutCancellationException -> println("Timeout!")
                            else -> {
                                println("Execution failed!")
                                if (stacktrace) exception.printStackTrace()
                            }
                        }
                    }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun ExecutionUnit<*>.runTimed(part: Part, input: Input, timeout: Duration) = runBlocking {
        runCatching {
            measureTimedValue {
                withTimeout(timeout.toMillis()) {
                    part.run(input)
                }
            }
        }
    }

    companion object {
        fun dayOptionChoice(days: Collection<Day>) = ArgType.Choice(
            choices = days.map { DayOption.Day(it.date.dayOfMonth) } + listOf(DayOption.Latest, DayOption.All),
            toVariant = { string ->
                when (string.lowercase()) {
                    "latest" -> DayOption.Latest
                    "all" -> DayOption.All
                    "today" -> DayOption.Today
                    else -> string.removePrefix("day").toInt()
                        .takeIf { int -> days.any { it.date.dayOfMonth == int } }
                        ?.let { DayOption.Day(it) }
                        ?: error("Invalid day")
                }
            },
            variantToString = {
                when (it) {
                    is DayOption.Day -> it.value.toString()
                    DayOption.Latest -> "latest"
                    DayOption.All -> "all"
                    DayOption.Today -> "today"
                }
            }
        )
    }

    sealed interface DayOption {
        class Day(val value: Int) : DayOption
        object Latest : DayOption
        object All : DayOption
        object Today : DayOption
    }
}

private class ListCommand(private val aoc: AdventOfCode) : Subcommand(
    name = "list",
    actionDescription = "List Advent of Code progress"
) {
    override fun execute() {
        println("Advent of Code ${aoc.year}: ${aoc.days.size}/25 days")

        val days = aoc.days.sortedBy { it.date }

        days.forEach { day ->
            val url = "https://adventofcode.com/${day.date.year}/day/${day.date.dayOfMonth}"

            println("  Day ${day.date.dayOfMonth} (${day.date}) - $url")
            println("    < ${day.input.format()}")

            day.parts.forEach { part ->
                println("      ${"*".repeat(part.number)}${" ".repeat(2 - part.number)}  Part ${part.name}")
            }
        }
    }
}
