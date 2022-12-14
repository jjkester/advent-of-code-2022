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
import nl.jjkester.adventofcode22.day11.MonkeyInTheMiddle
import nl.jjkester.adventofcode22.day12.HillClimbingAlgorithm
import nl.jjkester.adventofcode22.day13.DistressSignal
import nl.jjkester.adventofcode22.day14.RegolithReservoir

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

        day(11) implementedBy MonkeyInTheMiddle through {
            partOne { it.levelOfMonkeyBusiness(20, true) }
            partTwo { it.levelOfMonkeyBusiness(10_000, false) }
            solveFor("input.txt")
        }

        day(12) implementedBy HillClimbingAlgorithm through {
            partOne { it.shortestSteps() }
            partTwo { it.shortestTrail() }
            solveFor("input.txt")
        }

        day(13) implementedBy DistressSignal through {
            partOne { it.sumOfIndicesInRightOrder() }
            partTwo { it.decoderKey() }
            solveFor("input.txt")
        }

        day(14) implementedBy RegolithReservoir through {
            partOne { it.unitsOfSandInCave() }
            partTwo { it.unitsOfSandUntilRest() }
            solveFor("input.txt")
        }
    }.cli(args)
}
