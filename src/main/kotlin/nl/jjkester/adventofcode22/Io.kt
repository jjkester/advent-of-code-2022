package nl.jjkester.adventofcode22

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.BufferedReader

interface Input {
    fun readChars(): Flow<Char>
    fun readLines(): Flow<String>
    fun format(): String
}

data class StringInput(val value: String) : Input {
    override fun readChars(): Flow<Char> = value.asIterable().asFlow()
    override fun readLines(): Flow<String> = value.splitToSequence(System.lineSeparator()).asFlow()
    override fun format(): String = value.replace(System.lineSeparator(), "\u21B5")
        .let { if (it.length > 32) it.take(31) + Typography.ellipsis else it }
}

data class ResourceInput(val resourcePath: String) : Input {
    private val resource by lazy {
        requireNotNull({}.javaClass.getResource(resourcePath)) { "Resource not found: $resourcePath" }
    }

    override fun readChars(): Flow<Char> = read { read().toChar() }

    override fun readLines(): Flow<String> = read(BufferedReader::readLine)

    private fun <T> read(readOperation: BufferedReader.() -> T) = flow {
            resource.openStream().bufferedReader().use { reader ->
                while (true) {
                    reader.readOperation()?.also { emit(it) } ?: break
                }
            }
        }.flowOn(Dispatchers.IO)

    override fun format(): String = resource.path
}

interface Output {
    fun format(continuationLineIndent: Int = 0): String
}

data class StringOutput(val value: String) : Output {
    override fun format(continuationLineIndent: Int): String = "\"$value\""
        .prependIndent(" ".repeat(continuationLineIndent))
        .trimStart()
}

data class NumberOutput(val value: Number) : Output {
    override fun format(continuationLineIndent: Int): String = "$value"
}

val EmptyOutput = object : Output {
    override fun format(continuationLineIndent: Int): String = "\u2205"
}
