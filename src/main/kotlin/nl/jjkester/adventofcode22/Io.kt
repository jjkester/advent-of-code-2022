package nl.jjkester.adventofcode22

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

interface Input {
    fun readLines(): Flow<String>
    fun format(): String
}

data class StringInput(val value: String) : Input {
    override fun readLines(): Flow<String> = value.splitToSequence(System.lineSeparator()).asFlow()
    override fun format(): String = value.replace(System.lineSeparator(), "\u21B5")
        .let { if (it.length > 32) it.take(31) + Typography.ellipsis else it }
}

data class ResourceInput(val resourcePath: String) : Input {
    private val resource by lazy {
        requireNotNull({}.javaClass.getResource(resourcePath)) { "Resource not found: $resourcePath" }
    }

    override fun readLines(): Flow<String> = flow {
        resource.openStream().bufferedReader().use { reader ->
            while (true) {
                reader.readLine()?.also { emit(it) } ?: break
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun format(): String = resource.path
}

interface Output {
    fun format(): String
}

data class StringOutput(val value: String) : Output {
    override fun format(): String = "\"$value\""
}

data class NumberOutput(val value: Number) : Output {
    override fun format(): String = "$value"
}

val EmptyOutput = object : Output {
    override fun format(): String = "\u2205"
}
