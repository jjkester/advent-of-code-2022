package nl.jjkester.adventofcode22.day16

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import nl.jjkester.adventofcode22.Implementation
import nl.jjkester.adventofcode22.Input

/**
 * https://adventofcode.com/2022/day/16
 */
object ProboscideaVolcanium : Implementation<Flow<Valve>> {

    /**
     * Input modeled as a flow of valves, each with an id, flow rates, and ids of valves at the other end of the tunnels
     * connected to this valve.
     */
    override suspend fun prepareInput(input: Input): Flow<Valve> = input.readLines()
        .map { Valve.parse(it) }

    /**
     * Calculates the maximum pressure that can be released before the volcano erupts.
     */
    suspend fun Flow<Valve>.maxPressureReleaseBeforeEruption(): Int = toList().run {
        RecursiveOptimizer(this).optimizeMaxPressure(single { it.id == "AA" }, 30)
    }

    private fun List<Valve>.floydWarshall(): Map<Pair<Valve, Valve>, Int> =
        mutableMapOf<Pair<Valve, Valve>, Int>().also { distances ->
            forEach { valve ->
                valve.tunnels.forEach { tunnel ->
                    val otherValve = single { it.id == tunnel }
                    distances[valve to otherValve] = 1
                    distances[otherValve to valve] = 1
                }
            }
            forEach { valve ->
                distances[valve to valve] = 0
            }
            forEach { k ->
                forEach { i ->
                    forEach inner@{ j ->
                        val ij = distances[i to j] ?: Int.MAX_VALUE
                        val ik = distances[i to k] ?: return@inner
                        val kj = distances[k to j] ?: return@inner

                        if (ij > ik + kj) {
                            distances[i to j] = ik + kj
                        }
                    }
                }
            }
        }

    private operator fun Map<Pair<Valve, Valve>, Int>.get(first: Valve, second: Valve): Int? =
        get(first to second) ?: get(second to first)

    private operator fun Map<Pair<Valve, Valve>, Int>.get(from: Valve): Map<Valve, Int> =
        filter { (key, _) -> key.first == from || key.second == from }
            .mapKeys { (key, _) -> if (key.first == from) key.second else key.first }

    private class RecursiveOptimizer(valves: List<Valve>) {
        private val distances = valves.floydWarshall()

        suspend fun optimizeMaxPressure(startValve: Valve, time: Int): Int = coroutineScope {
            withContext(Dispatchers.Default) {
                optimizeMaxPressure(startValve, time, emptySet())
            }
        }

        private suspend fun optimizeMaxPressure(
            currentValve: Valve,
            timeLeft: Int,
            openValves: Set<Valve>
        ): Int = coroutineScope {
            distances[currentValve]
                .filter { (valve, _) -> valve.flowRate > 0 && valve !in openValves }
                .map { (valve, distance) ->
                    val timeLeftAfterOpening = timeLeft - distance - 1

                    if (timeLeftAfterOpening > 0) {
                        val totalValvePressure = valve.flowRate * timeLeftAfterOpening

                        async {
                            val nextContribution = optimizeMaxPressure(
                                valve,
                                timeLeftAfterOpening,
                                openValves + valve
                            )
                            totalValvePressure + nextContribution
                        }
                    } else {
                        CompletableDeferred(0)
                    }
                }
                .awaitAll()
                .maxOrNull()
                ?: 0
        }
    }
}

/**
 * A valve with an [id], [flowRate] and a list of ids of other valves that are reachable through [tunnels].
 */
data class Valve(val id: String, val flowRate: Int, val tunnels: List<String>) {
    companion object {
        private val regex = """
            ^Valve (?<v>[A-Z]{2}) has flow rate=(?<r>\d+); tunnel(s)? lead(s)? to valve(s)? (?<t>[A-Z]{2})(?<ts>(, [A-Z]{2})*)$
            """.trimIndent().toRegex()

        fun parse(line: String): Valve {
            val matches = checkNotNull(regex.matchEntire(line)) { "Not a valve and tunnels: '$line'" }

            val firstTunnel = matches.groups["t"]!!.value
            val remainingTunnels = matches.groups["ts"]!!.value.split(", ").filter { it.isNotBlank() }

            return Valve(
                id = matches.groups["v"]!!.value,
                flowRate = matches.groups["r"]!!.value.toInt(),
                tunnels = listOf(firstTunnel) + remainingTunnels
            )
        }
    }
}
