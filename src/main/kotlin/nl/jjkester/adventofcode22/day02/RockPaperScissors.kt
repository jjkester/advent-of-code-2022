package nl.jjkester.adventofcode22.day02

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.reduce
import nl.jjkester.adventofcode22.Implementation
import nl.jjkester.adventofcode22.Input
import nl.jjkester.adventofcode22.toPair

object RockPaperScissors : Implementation<Flow<Pair<Hand, Instruction>>> {

    override suspend fun prepareInput(input: Input): Flow<Pair<Hand, Instruction>> = input.readLines()
        .map { line ->
            line.split(' ')
                .toPair()
        }
        .map { (left, right) -> Hand.parse(left) to Instruction.parse(right) }

    suspend fun totalScoreWithHandStrategy(strategy: Flow<Pair<Hand, Instruction>>) = strategy
        .map { (left, right) -> turn(left, right.asHand()) }
        .reduce { accumulator, value -> accumulator + value }

    suspend fun totalScoreWithOutcomeStrategy(strategy: Flow<Pair<Hand, Instruction>>) = strategy
        .map { (left, right) -> turn(left, left selectCounter right) }
        .reduce { accumulator, value -> accumulator + value }

    private fun turn(left: Hand, right: Hand): Int = (left scoreAgainst right) + right.score

    private infix fun Hand.selectCounter(instruction: Instruction): Hand = when (this) {
        Hand.Paper -> when (instruction) {
            Instruction.Lose -> Hand.Rock
            Instruction.Draw -> Hand.Paper
            Instruction.Win -> Hand.Scissors
        }
        Hand.Rock -> when (instruction) {
            Instruction.Lose -> Hand.Scissors
            Instruction.Draw -> Hand.Rock
            Instruction.Win -> Hand.Paper
        }
        Hand.Scissors -> when (instruction) {
            Instruction.Lose -> Hand.Paper
            Instruction.Draw -> Hand.Scissors
            Instruction.Win -> Hand.Rock
        }
    }

    private infix fun Hand.scoreAgainst(other: Hand): Int = when (this) {
        Hand.Paper -> when (other) {
            Hand.Paper -> Score.draw
            Hand.Rock -> Score.lose
            Hand.Scissors -> Score.win
        }
        Hand.Rock -> when (other) {
            Hand.Paper -> Score.win
            Hand.Rock -> Score.draw
            Hand.Scissors -> Score.lose
        }
        Hand.Scissors -> when (other) {
            Hand.Paper -> Score.lose
            Hand.Rock -> Score.win
            Hand.Scissors -> Score.draw
        }
    }

    private operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>): Pair<Int, Int> =
        this.first + other.first to this.second + other.second

    private object Score {
        const val lose = 0
        const val draw = 3
        const val win = 6
    }
}

sealed class Instruction {
    object Lose : Instruction()
    object Draw : Instruction()
    object Win : Instruction()

    fun asHand(): Hand = when (this) {
        Lose -> Hand.Rock
        Draw -> Hand.Paper
        Win -> Hand.Scissors
    }

    companion object {
        fun parse(value: String) = when (value) {
            "X" -> Lose
            "Y" -> Draw
            "Z" -> Win
            else -> error("Char $value does not represent a Hand")
        }
    }
}

sealed class Hand(val score: Int) {
    object Rock : Hand(1)
    object Paper : Hand(2)
    object Scissors : Hand(3)

    companion object {
        fun parse(value: String) = when (value) {
            "A" -> Rock
            "B" -> Paper
            "C" -> Scissors
            else -> error("Char $value does not represent a Hand")
        }
    }
}
