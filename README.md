# Advent of Code 2022

My Advent of Code 2022 solutions in Kotlin.

## Challenge

This year's challenge is to use coroutines wherever feasible.
In practice, most of the solutions will use `Flow`.

I did have to create some custom operators for flows to make this work.
Generally, these operators would have been available for `Iterable` and/or `Sequence`.

## Features

- DSL for defining where solutions and inputs are
- CLI using `kotlinx-cli`

## Build and run

To build the code, run `./gradlew installDist`.
This will create an executable in `build/install/adventofcode2022/bin`.

Running the program without any argument will print instructions for use.
