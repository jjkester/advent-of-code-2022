package nl.jjkester.adventofcode22

import java.io.File
import java.time.LocalDate
import java.time.Month
import java.time.Year
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@DslMarker
@Target(AnnotationTarget.CLASS)
annotation class AdventOfCodeDsl

@AdventOfCodeDsl
class AdventOfCodeScope(private val year: Year) {

    private val days = mutableSetOf<Day>()

    fun day(number: Int): DayContinuation {
        require(number in 1..25) { "Day $number is not in the advent period" }
        return DayContinuation(year.atMonth(Month.DECEMBER).atDay(number))
    }

    infix fun <T, I : Implementation<T>> DayContinuation.implementedBy(implementation: I): ImplementationContinuation<I> {
        return ImplementationContinuation(date, implementation)
    }

    infix fun <T, I : Implementation<T>> ImplementationContinuation<I>.through(block: DayScope<T, I>.() -> Unit) {
        days.add(DayScope(date, implementation).apply(block).finalize())
    }

    fun finalize(): AdventOfCode = AdventOfCode(year, days)

    class DayContinuation(val date: LocalDate)
    class ImplementationContinuation<I>(val date: LocalDate, val implementation: I)
}

@AdventOfCodeDsl
class DayScope<T, I : Implementation<T>>(private val date: LocalDate, private val implementation: I) {
    private var input: Input by dslValue()
    private var partOne: (suspend (T) -> Output)? by dslValue(null)
    private var partTwo: (suspend (T) -> Output)? by dslValue(null)

    fun partOne(block: suspend I.(T) -> Any) {
        partOne = { implementation.block(it).asOutput() }
    }

    fun partTwo(block: suspend I.(T) -> Any) {
        partTwo = { implementation.block(it).asOutput() }
    }

    fun solveFor(filename: String) {
        input = getResourceInputForFilename(filename)
    }

    private fun <T> T.asOutput(): Output = when (this) {
        is Number -> NumberOutput(this)
        is String -> StringOutput(this)
        is Unit -> EmptyOutput
        else -> StringOutput(this.toString())
    }

    private fun getResourceInputForFilename(filename: String) =
        ResourceInput("${File.separator}day${date.dayOfMonth.toString().padStart(2, '0')}${File.separator}$filename")

    fun finalize(): Day = Day(
        date = date,
        input = input,
        parts = Parts(
            one = Part.One.takeIf { partOne != null },
            two = Part.Two.takeIf { partTwo != null }
        ),
        execution = ExecutionUnit(
            parser = implementation::prepareInput,
            partOne = partOne ?: { EmptyOutput },
            partTwo = partTwo ?: { EmptyOutput },
        )
    )
}

fun adventOfCode(year: Int, block: AdventOfCodeScope.() -> Unit): AdventOfCode {
    require(year in 2015..2022)

    return AdventOfCodeScope(Year.of(year))
        .apply(block)
        .finalize()
}

private class DslDelegate<T> private constructor(
    private var state: State<T>,
    private val overwritable: Boolean
) : ReadWriteProperty<Any, T> {

    constructor(overwritable: Boolean) : this(State.NotProvided(), overwritable)

    constructor(initialValue: T) : this(State.Provided(initialValue), true)

    override fun getValue(thisRef: Any, property: KProperty<*>): T = state.let {
        check(it is State.Provided) {
            "Required value ${property.name} has not been provided"
        }
        it.value
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        check(overwritable || state is State.NotProvided) {
            "Value ${property.name} cannot be set twice"
        }
        state = State.Provided(value)
    }

    private sealed interface State<T> {
        class NotProvided<T> : State<T>
        class Provided<T>(val value: T) : State<T>
    }
}

private fun <T> dslValue(overwritable: Boolean = false): ReadWriteProperty<Any, T> = DslDelegate(overwritable)

private fun <T> dslValue(initialValue: T): ReadWriteProperty<Any, T> = DslDelegate(initialValue)
