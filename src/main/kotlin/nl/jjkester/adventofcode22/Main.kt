@file:JvmName("Main")

package nl.jjkester.adventofcode22

import nl.jjkester.adventofcode22.day01.CalorieCounting
import nl.jjkester.adventofcode22.day02.RockPaperScissors
import nl.jjkester.adventofcode22.day03.RucksackReorganization
import nl.jjkester.adventofcode22.day04.CampCleanup
import nl.jjkester.adventofcode22.day05.SupplyStacks
import nl.jjkester.adventofcode22.day06.TuningTrouble
import nl.jjkester.adventofcode22.day07.NoSpaceLeftOnDevice
import nl.jjkester.adventofcode22.day08.TreetopTreeHouse
import nl.jjkester.adventofcode22.day09.RopeBridge
import nl.jjkester.adventofcode22.day10.CathodeRayTube

fun main(args: Array<String>) {
    adventOfCode(2022) {
        day(1) implementedBy CalorieCounting through {
            partOne { highestAmountCarried(it) }
            partTwo { topThreeCarried(it) }
            solveFor("input.txt")
        }

        day(2) implementedBy RockPaperScissors through {
            partOne { totalScoreWithHandStrategy(it) }
            partTwo { totalScoreWithOutcomeStrategy(it) }
            solveFor("input.txt")
        }

        day(3) implementedBy RucksackReorganization through {
            partOne { sumOfTypesInEachCompartment(it) }
            partTwo { sumOfBadges(it) }
            solveFor("input.txt")
        }

        day(4) implementedBy CampCleanup through {
            partOne { numberOfFullyContainedRanges(it) }
            partTwo { numberOfOverlappingRanges(it) }
            solveFor("input.txt")
        }

        day(5) implementedBy SupplyStacks through {
            partOne { topOfStacks(it.first, it.second, false) }
            partTwo { topOfStacks(it.first, it.second, true) }
            solveFor("input.txt")
        }

        day(6) implementedBy TuningTrouble through {
            partOne { offsetToFirstDistinctGroup(it, 4) }
            partTwo { offsetToFirstDistinctGroup(it, 14) }
            solveFor("input.txt")
        }

        day(7) implementedBy NoSpaceLeftOnDevice through {
            partOne { totalSizeOfSmallDirectories(it) }
            partTwo { sizeOfDirectoryToDelete(it) }
            solveFor("input.txt")
        }

        day(8) implementedBy TreetopTreeHouse through {
            partOne { numberOfVisibleTrees(it) }
            partTwo { highestScenicScore(it) }
            solveFor("input.txt")
        }

        day(9) implementedBy RopeBridge through {
            partOne { numberOfTailRopePositions(it, 2) }
            partTwo { numberOfTailRopePositions(it, 10) }
            solveFor("input.txt")
        }

        day(10) implementedBy CathodeRayTube through {
            partOne { it.sumOfSignalStrengths() }
            partTwo { it.renderScreen() }
            solveFor("input.txt")
        }
    }.cli(args)
}
