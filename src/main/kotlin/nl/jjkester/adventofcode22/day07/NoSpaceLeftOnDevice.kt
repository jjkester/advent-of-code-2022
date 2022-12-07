package nl.jjkester.adventofcode22.day07

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.jjkester.adventofcode22.Implementation
import nl.jjkester.adventofcode22.Input
import nl.jjkester.adventofcode22.partitionIsInstance
import nl.jjkester.adventofcode22.toPair

/**
 * https://adventofcode.com/2022/day/7
 */
object NoSpaceLeftOnDevice : Implementation<Flow<TerminalOutput>> {

    /**
     * Input modeled as a flow of terminal output lines, parsed to sealed types for ease of processing.
     */
    override suspend fun prepareInput(input: Input): Flow<TerminalOutput> = input.readLines()
        .map { TerminalOutput.parse(it) }

    /**
     * Calculates the total size of small (at most 100.000 units) directories.
     *
     * This method will count files multiple times, since the total sizes (recursive) of directories are summed.
     */
    suspend fun totalSizeOfSmallDirectories(output: Flow<TerminalOutput>): Long = with(FilesystemSizeCache()) {
        buildFilesystemTree(output)
            .apply {
                withContext(Dispatchers.Default) {
                    precompute()
                }
            }
            .flattenDirectories()
            .map { it.totalSize }
            .filter { it <= 100_000 }
            .reduce(Long::plus)
    }

    /**
     * Calculates the size of the directory to delete, removing the least amount of data while providing sufficient
     * space.
     */
    suspend fun sizeOfDirectoryToDelete(output: Flow<TerminalOutput>): Long = with(FilesystemSizeCache()) {
        val filesystem = buildFilesystemTree(output)
        val spaceToFind = withContext(Dispatchers.Default) {
            30_000_000L - (70_000_000L - filesystem.precompute())
        }

        filesystem.flattenDirectories()
            .map { it.totalSize }
            .filter { it >= spaceToFind }
            .reduce(Math::min)
    }

    private fun FilesystemNode.Directory.flattenDirectories(): Flow<FilesystemNode.Directory> = flow {
        emit(this@flattenDirectories)

        children.filterIsInstance<FilesystemNode.Directory>()
            .forEach { emitAll(it.flattenDirectories()) }
    }

    private suspend fun buildFilesystemTree(output: Flow<TerminalOutput>): FilesystemNode.Directory {
        val builder = FilesystemBuilder()

        output.collect {
            when (it) {
                is TerminalOutput.Command.ChangeDirectory.In -> builder.pushd(it.name)
                TerminalOutput.Command.ChangeDirectory.Out -> builder.popd()
                TerminalOutput.Command.ListDirectory -> {}
                is TerminalOutput.Result.Directory -> builder.dir(it.name)
                is TerminalOutput.Result.File -> builder.file(it.name, it.size)
            }
        }

        return checkNotNull(builder.build()) { "No filesystem" }
    }

    /**
     * Builder to keep track of partial filesystem knowledge while processing the terminal output.
     *
     * This class is stateful, every method on this class operates on the context as shaped by previous calls. Call
     * order is important. This class is not thread-safe.
     */
    private class FilesystemBuilder {
        private val currentPath = mutableListOf(FilesystemNode.Directory(""))
        private val directoryContents = mutableMapOf<FilesystemNode.Directory, MutableSet<FilesystemNode>>()

        private val currentDirectory: FilesystemNode.Directory
            get() = currentPath.last()
        private val currentDirectoryContents: MutableSet<FilesystemNode>
            get() = directoryContents.computeIfAbsent(currentDirectory) { mutableSetOf() }

        /**
         * Enter a subdirectory with the provided [name].
         */
        fun pushd(name: String) {
            currentPath.add(getOrCreateSubdirectory(name))
        }

        /**
         * Return to the parent directory.
         */
        fun popd() {
            if (currentPath.size > 2) {
                currentPath.removeLast()
            }
        }

        /**
         * Record the existence of a directory with the provided [name].
         */
        fun dir(name: String) {
            getOrCreateSubdirectory(name)
        }

        /**
         * Record the existence of a file with the provided [name] and [size].
         */
        fun file(name: String, size: Long) {
            currentDirectoryContents.add(FilesystemNode.File(name, size))
        }

        fun build(): FilesystemNode.Directory? = currentPath.getOrNull(1)?.copyWithContents()

        private fun getOrCreateSubdirectory(name: String): FilesystemNode.Directory = currentDirectory.children
            .filterIsInstance<FilesystemNode.Directory>()
            .find { it.name == name }
            ?: FilesystemNode.Directory(name)
                .also(currentDirectoryContents::add)

        private fun FilesystemNode.Directory.copyWithContents(): FilesystemNode.Directory {
            val (directories, files) = directoryContents.getOrDefault(this, emptySet())
                .partitionIsInstance<FilesystemNode, FilesystemNode.Directory>()
            return copy(children = files.toSet() + directories.map { it.copyWithContents() })
        }
    }

    /**
     * Scoped utility to calculate and cache the total size of filesystem nodes.
     */
    private class FilesystemSizeCache {
        private val cache = mutableMapOf<FilesystemNode.Directory, Long>()

        /**
         * The total size of a filesystem node. In the case of directories, this will recursively sum the sizes of all
         * files in the directory and its subdirectories.
         */
        val FilesystemNode.totalSize: Long
            get() = when (this) {
                is FilesystemNode.File -> this.size
                is FilesystemNode.Directory -> cache.computeIfAbsent(this) { directory ->
                    directory.children.sumOf { it.totalSize }
                }
            }

        /**
         * Computes and caches the size of this node and all children recursively and concurrently.
         */
        suspend fun FilesystemNode.precompute(): Long {
            val channel = Channel<Pair<FilesystemNode.Directory, Long>>()

            return coroutineScope {
                launch {
                    channel.receiveAsFlow().collect { (directory, size) ->
                        cache[directory] = size
                    }
                }
                precomputeInternal(channel).also { channel.close() }
            }
        }

        private suspend fun FilesystemNode.precomputeInternal(
            channel: SendChannel<Pair<FilesystemNode.Directory, Long>>
        ): Long = when (this) {
            is FilesystemNode.File -> size
            is FilesystemNode.Directory -> coroutineScope {
                children
                    .map { async { it.precomputeInternal(channel) } }
                    .fold(0L) { acc, deferred -> acc + deferred.await() }
                    .also { channel.send(this@precomputeInternal to it) }
            }
        }
    }
}

/**
 * Parsed terminal output.
 */
sealed class TerminalOutput {

    /**
     * Command in the terminal.
     */
    sealed class Command : TerminalOutput() {

        /**
         * Change directory (cd) command.
         */
        sealed class ChangeDirectory : Command() {

            /**
             * Change directory command to move one directory up.
             */
            object Out : ChangeDirectory()

            /**
             * Change directory command to move to the child directory identified by its [name].
             */
            class In(val name: String) : ChangeDirectory()

            companion object {
                /**
                 * Parses the list of [arguments] to the correct change directory command.
                 */
                fun parse(arguments: List<String>): ChangeDirectory {
                    require(arguments.size == 1) { "ChangeDirectory requires exactly 1 argument" }

                    return when (val argument = arguments.first()) {
                        ".." -> Out
                        else -> In(argument)
                    }
                }
            }
        }

        /**
         * List command to print the contents of a directory.
         */
        object ListDirectory : Command()

        companion object {
            /**
             * Parses the [line] to a terminal command.
             */
            fun parse(line: String): Command {
                val segments = line.removePrefix("$").trimStart().split(' ')

                require(segments.isNotEmpty()) { "No command" }

                return when (val command = segments.first()) {
                    "cd" -> ChangeDirectory.parse(segments.drop(1))
                    "ls" -> ListDirectory
                    else -> error("Unknown command: '$command'")
                }
            }
        }
    }

    /**
     * Single result of a command in the terminal. One command may have multiple results.
     */
    sealed class Result : TerminalOutput() {

        /**
         * Directory result of listing the contents of a directory.
         */
        class Directory(val name: String) : Result()

        /**
         * File result of listing the contents of a directory.
         */
        class File(val name: String, val size: Long) : Result()

        companion object {
            /**
             * Parses the [line] to a terminal result.
             */
            fun parse(line: String): Result = line.split(' ')
                .also { require(it.size == 2) { "Unparsable result: '$line'" } }
                .toPair()
                .let { (left, right) ->
                    when (left) {
                        "dir" -> Directory(right)
                        else -> File(right, left.toLongOrNull() ?: error("File size not parsable: '$left'"))
                    }
                }
        }
    }

    companion object {
        /**
         * Parses the [line] of terminal output to a command or a result.
         */
        fun parse(line: String): TerminalOutput = if (line.startsWith("$")) {
            Command.parse(line)
        } else {
            Result.parse(line)
        }
    }
}

/**
 * Node in the directory tree of the file system.
 */
sealed interface FilesystemNode {

    /**
     * Name of the file or directory.
     */
    val name: String

    /**
     * Directory in the file system.
     */
    class Directory(override val name: String, val children: Set<FilesystemNode> = emptySet()) : FilesystemNode {
        override fun toString() = "$name (dir)"
        fun copy(children: Set<FilesystemNode>): Directory = Directory(name, children)
    }

    /**
     * File in the file system.
     */
    class File(override val name: String, val size: Long) : FilesystemNode {
        override fun toString(): String = "$name (file, size=$size)"
    }
}
