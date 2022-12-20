package nl.jjkester.adventofcode22.day19

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import nl.jjkester.adventofcode22.Implementation
import nl.jjkester.adventofcode22.Input
import kotlin.math.ceil
import kotlin.math.max

/**
 * https://adventofcode.com/2022/day/20
 */
object NotEnoughMinerals : Implementation<Flow<Blueprint>> {

    /**
     * Input modeled as a flow of blueprints.
     */
    override suspend fun prepareInput(input: Input): Flow<Blueprint> = input.readLines()
        .map { Blueprint.parse(it) }

    /**
     * Calculates the sum of the quality levels (blueprint number times the maximum number of opened geodes within 24
     * minutes) of the blueprints.
     */
    suspend fun Flow<Blueprint>.sumOfQualityLevels(): Int = coroutineScope {
        toList()
            .map { blueprint ->
                async(Dispatchers.Default) { blueprint.multiplier * blueprint.maxOpenedGeodes(24) }
            }
            .awaitAll()
            .sum()
    }

    /**
     * Calculates the product of the maximum number of opened geodes within 32 minutes for the first three blueprints,
     * or fewer when there are less than three blueprints in total.
     */
    suspend fun Flow<Blueprint>.productOfMaximumNumberOfGeodes(): Int = coroutineScope {
        take(3)
            .toList()
            .map { blueprint ->
                async(Dispatchers.Default) { blueprint.maxOpenedGeodes(32) }
            }
            .awaitAll()
            .reduce(Int::times)
    }

    private fun Blueprint.maxOpenedGeodes(time: Int): Int =
        maxOpenedGeodes(time, Resources.zero, Resources.oreRobotProduction).geodes

    private fun Blueprint.maxOpenedGeodes(
        timeLeft: Int,
        resources: Resources,
        production: Resources
    ): Resources {
        return if (timeLeft > 0) {
            possiblePurchases(resources, production, timeLeft)
                .map { (newRobotCost, newRobotProduction, skipAhead) ->
                    maxOpenedGeodes(
                        timeLeft = timeLeft - skipAhead - 1,
                        resources = resources + production * (skipAhead + 1).coerceAtMost(timeLeft) - newRobotCost,
                        production = production + newRobotProduction
                    )
                }
                .maxBy(Resources::geodes)
        } else {
            resources
        }
    }

    private fun Blueprint.possiblePurchases(
        resources: Resources,
        production: Resources,
        timeLeft: Int
    ): Sequence<RobotOption> = sequence {
        val timeToGeodeRobot = production timesToReach geodeRobotCost - resources
        val timeToObsidianRobot = production timesToReach obsidianRobotCost - resources
        val timeToClayRobot = production timesToReach clayRobotCost - resources
        val timeToOreRobot = production timesToReach oreRobotCost - resources

        timeToGeodeRobot?.also {
            yield(RobotOption(geodeRobotCost, Resources.geodeRobotProduction, it))
        }
        if (isNeeded(maxCosts, resources, production, timeLeft, Resources::obsidian)) {
            timeToObsidianRobot?.also {
                yield(RobotOption(obsidianRobotCost, Resources.obsidianRobotProduction, it))
            }
        }
        if (isNeeded(maxCosts, resources, production, timeLeft, Resources::clay)) {
            timeToClayRobot?.also {
                yield(RobotOption(clayRobotCost, Resources.clayRobotProduction, it))
            }
        }
        if (isNeeded(maxCosts, resources, production, timeLeft, Resources::ore)) {
            timeToOreRobot?.also {
                yield(RobotOption(oreRobotCost, Resources.oreRobotProduction, it))
            }
        }
    }

    /**
     * Calculates how many minutes of production is takes to produce the desired resources, or null when a required
     * resource is not produced.
     */
    private infix fun Resources.timesToReach(other: Resources): Int? = maxOf(
        ore timesToReach other.ore,
        clay timesToReach other.clay,
        obsidian timesToReach other.obsidian,
        geodes timesToReach other.geodes
    ).takeIf { it < Int.MAX_VALUE }

    private infix fun Int.timesToReach(other: Int): Int = when {
        other <= 0 -> 0 // Already there
        this == 0 -> Int.MAX_VALUE // Zero-division, max int is considered infinite
        else -> ceil(other.toDouble() / this).toInt()
    }

    /**
     * Returns whether a robot producing the selected resource is needed.
     *
     * A robot is not needed when the maximum potential expense of the resource is lower than the current potential
     * total production.
     */
    private inline fun isNeeded(
        maxUse: Resources,
        stock: Resources,
        production: Resources,
        timeLeft: Int,
        selector: Resources.() -> Int
    ): Boolean = isNeeded(maxUse.selector(), stock.selector(), production.selector(), timeLeft)

    private fun isNeeded(maxUse: Int, stock: Int, production: Int, timeLeft: Int): Boolean =
        production * timeLeft + stock < timeLeft * maxUse

    private operator fun Resources.plus(other: Resources) =
        Resources(ore + other.ore, clay + other.clay, obsidian + other.obsidian, geodes + other.geodes)

    private operator fun Resources.minus(other: Resources) =
        Resources(ore - other.ore, clay - other.clay, obsidian - other.obsidian, geodes - other.geodes)

    private operator fun Resources.times(other: Int) =
        Resources(ore * other, clay * other, obsidian * other, geodes * other)

    /**
     * Data class modeling the choice of building a robot, potentially in the future.
     */
    private data class RobotOption(val cost: Resources, val gainPerMinute: Resources, val waitTime: Int)
}

/**
 * A blueprint with a multiplier (its 1-based index) and the costs for constructing the robots.
 */
data class Blueprint(
    val multiplier: Int,
    val oreRobotCost: Resources,
    val clayRobotCost: Resources,
    val obsidianRobotCost: Resources,
    val geodeRobotCost: Resources
) {
    /**
     * Maximum cost for any robot.
     */
    val maxCosts = listOf(oreRobotCost, clayRobotCost, obsidianRobotCost, geodeRobotCost).reduce { acc, it ->
        Resources(
            ore = max(acc.ore, it.ore),
            clay = max(acc.clay, it.clay),
            obsidian = max(acc.obsidian, it.obsidian),
            geodes = max(acc.geodes, it.geodes)
        )
    }

    companion object {
        private val regex =
            """Blueprint (?<mul>\d+): Each ore robot costs (?<ore>[\w\s]+). Each clay robot costs (?<clay>[\w\s]+). Each obsidian robot costs (?<obsidian>[\w\s]+). Each geode robot costs (?<geode>[\w\s]+)."""
                .toRegex()
        private val resourceRegex = """(?<num1>\d+) (?<kind1>\w+)( and (?<num2>\d+) (?<kind2>\w+))?""".toRegex()

        /**
         * Parses a line to a blueprint object.
         */
        fun parse(line: String): Blueprint {
            val matches = checkNotNull(regex.matchEntire(line)) { "Not a valid blueprint" }

            return Blueprint(
                multiplier = matches.groups["mul"]!!.value.toInt(),
                oreRobotCost = parseResources(matches.groups["ore"]!!.value),
                clayRobotCost = parseResources(matches.groups["clay"]!!.value),
                obsidianRobotCost = parseResources(matches.groups["obsidian"]!!.value),
                geodeRobotCost = parseResources(matches.groups["geode"]!!.value),
            )
        }

        private fun parseResources(line: String): Resources {
            val matches = checkNotNull(resourceRegex.matchEntire(line)) { "Not a valid resource string" }

            val first = matches.groups["kind1"]!!.value to matches.groups["num1"]!!.value.toInt()
            val second = matches.groups["kind2"]?.value?.let {
                it to matches.groups["num2"]!!.value.toInt()
            }

            fun Pair<String, Int>.takeValueIfKind(kind: String): Int? = takeIf { it.first == kind }?.second

            fun firstOrSecondOrZero(kind: String, first: Pair<String, Int>, second: Pair<String, Int>?): Int =
                first.takeValueIfKind(kind) ?: second?.takeValueIfKind(kind) ?: 0

            return Resources(
                ore = firstOrSecondOrZero("ore", first, second),
                clay = firstOrSecondOrZero("clay", first, second),
                obsidian = firstOrSecondOrZero("obsidian", first, second),
                geodes = 0
            )
        }
    }
}

/**
 * Amount of resources.
 */
data class Resources(val ore: Int, val clay: Int, val obsidian: Int, val geodes: Int) {
    companion object {
        /** No resources at all. */
        val zero: Resources = Resources(0, 0, 0, 0)

        /** Resources produced per minute by a single ore robot. */
        val oreRobotProduction: Resources = Resources(1, 0, 0, 0)

        /** Resources produced per minute by a single clay robot. */
        val clayRobotProduction: Resources = Resources(0, 1, 0, 0)

        /** Resources produced per minute by a single obsidian robot. */
        val obsidianRobotProduction: Resources = Resources(0, 0, 1, 0)

        /** Resources produced per minute by a single geode robot. */
        val geodeRobotProduction: Resources = Resources(0, 0, 0, 1)
    }
}
