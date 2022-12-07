package nl.jjkester.adventofcode22.day07

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import kotlinx.coroutines.test.runTest
import nl.jjkester.adventofcode22.StringInput
import org.junit.jupiter.api.Test

class NoSpaceLeftOnDeviceTest {

    @Test
    fun testTotalSizeOfSmallDirectories() = runTest {
        val input = StringInput(example)
        val preparedInput = NoSpaceLeftOnDevice.prepareInput(input)

        assertThat { NoSpaceLeftOnDevice.totalSizeOfSmallDirectories(preparedInput) }
            .isSuccess()
            .isEqualTo(95437L)
    }

    @Test
    fun testSizeOfDirectoryToDelete() = runTest {
        val input = StringInput(example)
        val preparedInput = NoSpaceLeftOnDevice.prepareInput(input)

        assertThat { NoSpaceLeftOnDevice.sizeOfDirectoryToDelete(preparedInput) }
            .isSuccess()
            .isEqualTo(24933642L)
    }

    companion object {
        private val example = """
            ${'$'} cd /
            ${'$'} ls
            dir a
            14848514 b.txt
            8504156 c.dat
            dir d
            ${'$'} cd a
            ${'$'} ls
            dir e
            29116 f
            2557 g
            62596 h.lst
            ${'$'} cd e
            ${'$'} ls
            584 i
            ${'$'} cd ..
            ${'$'} cd ..
            ${'$'} cd d
            ${'$'} ls
            4060174 j
            8033020 d.log
            5626152 d.ext
            7214296 k
        """.trimIndent()
    }
}
