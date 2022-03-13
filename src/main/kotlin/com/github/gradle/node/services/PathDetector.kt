package com.github.gradle.node.services

import com.github.gradle.node.util.PlatformHelper
import java.io.File
import java.util.stream.Collectors

class PathDetector {
    companion object {
        /**
         * Looks through PATH for a given executable checking if it exists
         * and if it is executable.
         *
         * @return a list of executables
         */
        fun findOnPath(executable: String): MutableList<File> {
            val splitter = if (PlatformHelper.INSTANCE.isWindows) ";" else ":"

            val paths = if (PlatformHelper.INSTANCE.isWindows)
                // On Windows PATH may be of any case.
                System.getenv().keys.filter { key -> key.toLowerCase() == "path" }
            else
                listOf("PATH")

            val result = mutableListOf<File>()
            paths.forEach { path ->
                result.addAll(System.getenv(path).split(splitter)
                    .stream().map { File(it, executable) }
                    .filter(File::exists)
                    .filter(File::canExecute)
                    .collect(Collectors.toList()))
            }
            return result
        }
    }
}