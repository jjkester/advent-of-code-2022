package nl.jjkester.adventofcode22

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

inline fun <T> Flow<T>.chunkedBy(crossinline predicate: (T) -> Boolean): Flow<List<T>> = flow {
    var buffer = mutableListOf<T>()

    collect { item ->
        if (predicate(item)) {
            emit(buffer)
            buffer = mutableListOf()
        } else {
            buffer.add(item)
        }
    }
}

fun <T> Collection<T>.toPair(): Pair<T, T> {
    check(size == 2) {
        "Collection with more than two items cannot be converted to a pair"
    }
    return first() to last()
}
