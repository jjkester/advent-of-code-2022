package nl.jjkester.adventofcode22

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun <T> Flow<T>.chuncked(size: Int, dropLatest: Boolean = false): Flow<List<T>> = flow {
    var buffer = mutableListOf<T>()

    collect { item ->
        buffer.add(item)

        if (buffer.size == size) {
            emit(buffer)
            buffer = mutableListOf()
        }
    }

    if (!dropLatest && buffer.isNotEmpty()) {
        emit(buffer)
    }
}

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

    if (buffer.isNotEmpty()) {
        emit(buffer)
    }
}

fun <T> Flow<T>.windowed(size: Int, partialWindows: Boolean = false): Flow<List<T>> = flow {
    val buffer = mutableListOf<T>()

    collect { item ->
        buffer.add(item)

        if (partialWindows || buffer.size == size) {
            emit(buffer.toMutableList())
            if (buffer.size == size) buffer.removeFirst()
        }
    }

    while (partialWindows && buffer.isNotEmpty()) {
        buffer.removeFirst()
        emit(buffer.toMutableList())
    }
}

inline fun <T, reified R : T> Collection<T>.partitionIsInstance(): Pair<List<R>, List<T>> {
    val first = mutableListOf<R>()
    val second = mutableListOf<T>()

    forEach {
        if (it is R) first.add(it) else second.add(it)
    }

    return first to second
}

fun <T> Collection<T>.toPair(): Pair<T, T> {
    check(size == 2) {
        "Collection with more than two items cannot be converted to a pair"
    }
    return first() to last()
}
