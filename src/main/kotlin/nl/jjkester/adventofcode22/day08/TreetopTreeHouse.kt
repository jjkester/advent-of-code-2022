package nl.jjkester.adventofcode22.day08

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import nl.jjkester.adventofcode22.Implementation
import nl.jjkester.adventofcode22.Input

/**
 * https://adventofcode.com/2022/day/8
 */
object TreetopTreeHouse : Implementation<Grid> {

    /**
     * Input modeled as a grid of heights, which is a matrix of integers.
     */
    override suspend fun prepareInput(input: Input): Grid = input.readLines()
        .map { it.map(Char::digitToInt).toIntArray() }
        .toList()
        .also { rows -> require(rows.distinctBy { it.size }.size == 1) { "Not a proper grid" } }
        .toTypedArray()

    /**
     * Calculates the number of trees that are visible from outside the grid.
     */
    fun numberOfVisibleTrees(trees: Grid): Int {
        val visible = mutableSetOf<Pair<Int, Int>>()

        fun addOrRaise(x: Int, y: Int, max: Int, tree: Int): Int = if (max < tree) {
            visible.add(x to y)
            tree
        } else {
            max
        }

        val treesRotated = Array(trees[0].size) { IntArray(trees.size) }

        // Horizontal
        trees.forEachIndexed { y, row ->
            // Left to right
            row.foldIndexed(-1) { x, max, tree ->
                treesRotated[x][y] = tree // Prepare for columns
                addOrRaise(x, y, max, tree)
            }

            // Right to left
            row.foldRightIndexed(-1) { x, tree, max ->
                addOrRaise(x, y, max, tree)
            }
        }

        // Vertical
        treesRotated.forEachIndexed { x, row ->
            // Top to bottom
            row.foldIndexed(-1) { y, max, tree ->
                addOrRaise(x, y, max, tree)
            }

            // Bottom to top
            row.foldRightIndexed(-1) { y, tree, max ->
                addOrRaise(x, y, max, tree)
            }
        }

        return visible.size
    }

    /**
     * Computes the scenic score (multiplication of view distances from a tree house in the tree) of all trees and
     * returns the highest one.
     */
    fun highestScenicScore(trees: Grid): Long {
        fun scenicScore(x: Int, y: Int, max: Int): Long {
            fun List<Int>.visibleFrom(height: Int) = takeWhileIncludingEdge { it < height }

            val toRight = trees[y].drop(x + 1).visibleFrom(max)
            val toLeft = trees[y].slice(0 until x).asReversed().visibleFrom(max)
            val toTop = (y - 1 downTo 0).map { trees[it][x] }.visibleFrom(max)
            val toBottom = (y + 1..trees[0].lastIndex).map { trees[it][x] }.visibleFrom(max)

            return toRight.size.toLong() * toLeft.size * toTop.size * toBottom.size
        }

        return trees.foldIndexed(0L) { y, bestRow, row ->
            bestRow.coerceAtLeast(
                row.foldIndexed(0L) { x, bestOfRow, tree ->
                    bestOfRow.coerceAtLeast(scenicScore(x, y, tree))
                }
            )
        }
    }

    private inline fun <T> List<T>.takeWhileIncludingEdge(predicate: (T) -> Boolean): List<T> {
        val list = ArrayList<T>()
        for (item in this) {
            list.add(item)
            if (!predicate(item))
                break
        }
        return list
    }
}

/**
 * The grid of trees is modeled as an integer matrix.
 */
typealias Grid = Array<IntArray>
