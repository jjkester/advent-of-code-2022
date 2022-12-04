package nl.jjkester.adventofcode22.day02

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.reduce
import nl.jjkester.adventofcode22.Implementation
import nl.jjkester.adventofcode22.Input
import nl.jjkester.adventofcode22.toPair

/**
 * https://adventofcode.com/2022/day/2
 */
object RockPaperScissors : Implementation<Flow<Pair<Hand, Instruction>>> {

    /**
     * Input modeled as a pair of the hand sign that the elf will show and the instruction (part 2 concept) for the
     * player.
     */
    override suspend fun prepareInput(input: Input): Flow<Pair<Hand, Instruction>> = input.readLines()
        .map { line ->
            line.split(' ')
                .toPair()
        }
        .map { (left, right) -> Hand.parse(left) to Instruction.parse(right) }

    /**
     * Calculates the total score of the player when interpreting the instruction as a hand sign.
     */
    suspend fun totalScoreWithHandStrategy(strategy: Flow<Pair<Hand, Instruction>>) = strategy
        .map { (left, right) -> turn(left, right.asHand()) }
        .reduce { accumulator, value -> accumulator + value }

    /**
     * Calculates the total score of the player when interpreting the instruction as a win/draw/lose situation.
     */
    suspend fun totalScoreWithOutcomeStrategy(strategy: Flow<Pair<Hand, Instruction>>) = strategy
        .map { (left, right) -> turn(left, left selectCounter right) }
        .reduce { accumulator, value -> accumulator + value }

    /**
     * Calculates the points that the player ([right]) scores in a turn.
     */
    private fun turn(left: Hand, right: Hand): Int = when (left) {
        Hand.Paper -> when (right) {
            Hand.Paper -> Score.draw
            Hand.Rock -> Score.lose
            Hand.Scissors -> Score.win
        }

        Hand.Rock -> when (right) {
            Hand.Paper -> Score.win
            Hand.Rock -> Score.draw
            Hand.Scissors -> Score.lose
        }

        Hand.Scissors -> when (right) {
            Hand.Paper -> Score.lose
            Hand.Rock -> Score.win
            Hand.Scissors -> Score.draw
        }
    } + right.score

    /**
     * Selects the correct hand signal to end up in a win/draw/lose situation as modeled by the [instruction].
     */
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

    /**
     * Holds the score for the three outcomes of a turn.
     */
    private object Score {
        const val lose = 0
        const val draw = 3
        const val win = 6
    }
}

/**
 * Models the instruction for the player to [Win], [Draw] or [Lose] the game.
 */
sealed class Instruction {
    object Lose : Instruction()
    object Draw : Instruction()
    object Win : Instruction()

    /**
     * Hand value corresponding to this instruction when interpreted as hand sign.
     */
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

/**
 * Models a hand sign ([Rock], [Paper] or [Scissors]) with its individual [score] when playing it.
 */
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
