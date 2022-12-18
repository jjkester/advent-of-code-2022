package nl.jjkester.adventofcode22.day18

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.flow.toList
import nl.jjkester.adventofcode22.Implementation
import nl.jjkester.adventofcode22.Input
import org.jetbrains.kotlinx.multik.api.Multik
import org.jetbrains.kotlinx.multik.api.zeros
import org.jetbrains.kotlinx.multik.ndarray.data.D3Array
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.data.set
import org.jetbrains.kotlinx.multik.ndarray.operations.forEachMultiIndexed

/**
 * https://adventofcode.com/2022/day/18
 */
object BoilingBoulders : Implementation<Flow<Cube>> {

    /**
     * Input modeled as a flow of 1x1x1 cubes.
     */
    override suspend fun prepareInput(input: Input): Flow<Cube> = input.readLines()
        .map { Cube.parse(it) }

    /**
     * Calculates the total surface area of the object consisting of the cubes. Optionally subtracts inside pockets
     * from the total surface area because these are not reachable from the outside.
     */
    suspend fun Flow<Cube>.totalOutsideSurfaceArea(subtractInsidePockets: Boolean): Int =
        toList().totalSurfaceArea(subtractInsidePockets)

    private suspend fun Collection<Cube>.totalSurfaceArea(subtractInsidePockets: Boolean): Int =
        takeIf { it.isNotEmpty() }
            ?.let { cubes ->
                computeSurfaceAreas(cubes, subtractInsidePockets)
                    // Merge, because the order is not important
                    .flatMapMerge { result ->
                        when (result) {
                            // When the result is a pocket, compute its surface area and negate the values to subtract them
                            is CubeResult.Pocket -> computeSurfaceAreas(result.area, false)
                                .filterIsInstance<CubeResult.SurfaceArea>()
                                .map { -it.value }

                            // When the result is a surface area, unpack its value
                            is CubeResult.SurfaceArea -> flowOf(result.value)
                        }
                    }
                    // Parallel execution
                    .flowOn(Dispatchers.Default)
                    .reduce(Int::plus)
            }
        // An empty collection of cubes will always have a 0 surface area
            ?: 0

    private fun computeSurfaceAreas(
        filledCubes: Collection<Cube>,
        subtractInsidePockets: Boolean
    ): Flow<CubeResult> = flow {
        // Model the 3d space
        // We accept the slight inefficiency when the cubes do not start at index 0
        // A zero denotes an area we did not classify yet
        val space = Multik.zeros<Byte>(
            filledCubes.maxOf { it.x } + 1,
            filledCubes.maxOf { it.y } + 1,
            filledCubes.maxOf { it.z } + 1
        )

        // Mark a 1 in each area occupied by a cube
        filledCubes.forEach { (x, y, z) ->
            space[x, y, z] = 1
        }

        space.forEachMultiIndexed { (x, y, z), value ->
            // Find all neighbouring coordinates and their values (null when the coordinate is out of bounds)
            val neighbours = getNeighbours(x, y, z).associateWith { space.getOrNull(it) }

            if (value > 0) {
                // Indicates a cube, so we count the surface area by inspecting the neighbours
                // All neighbours that are out of bounds or not a cube count
                emit(CubeResult.SurfaceArea(neighbours.count { (_, it) -> it == null || it < 1 }))
            } else if (subtractInsidePockets && value.toInt() == 0) {
                // We have found an unmarked area, we will try to find its size and mark it
                val area = mutableSetOf<Cube>()
                var reachableFromOutside = false

                fun floodFill(cube: Cube) {
                    val currentValue = space.getOrNull(cube)

                    if (currentValue == null) {
                        // Out of bounds, so this area is reachable from outside the cubes
                        reachableFromOutside = true
                    } else if (currentValue.toInt() == 0) {
                        // Mark -1 in each visited empty area
                        space[cube.x, cube.y, cube.z] = -1

                        // Add the empty cube to the area
                        area.add(cube)

                        // Recursively continue finding the edges of the area
                        getNeighbours(cube.x, cube.y, cube.z).forEach { floodFill(it) }
                    }
                }

                // Visit all unmarked cubes starting from this cube and mark them
                floodFill(Cube(x, y, z))

                if (!reachableFromOutside) {
                    // Remember the area when it is internal
                    emit(CubeResult.Pocket(area))
                }
            }
        }
    }

    private fun D3Array<Byte>.getOrNull(cube: Cube): Byte? = try {
        this[cube.x, cube.y, cube.z]
    } catch (ex: IndexOutOfBoundsException) {
        null
    }

    private fun getNeighbours(x: Int, y: Int, z: Int): Set<Cube> = setOf(
        Cube(x - 1, y, z),
        Cube(x + 1, y, z),
        Cube(x, y - 1, z),
        Cube(x, y + 1, z),
        Cube(x, y, z - 1),
        Cube(x, y, z + 1),
    )

    private sealed interface CubeResult {
        @JvmInline
        value class SurfaceArea(val value: Int) : CubeResult

        @JvmInline
        value class Pocket(val area: Set<Cube>) : CubeResult
    }
}

/**
 * A 1x1x1 cube in a 3D space.
 */
data class Cube(val x: Int, val y: Int, val z: Int) {
    companion object {
        fun parse(line: String): Cube = line.split(',')
            .mapNotNull { it.toIntOrNull() }
            .also { require(it.size == 3) { "Not a valid cube" } }
            .let { (x, y, z) -> Cube(x, y, z) }
    }
}
