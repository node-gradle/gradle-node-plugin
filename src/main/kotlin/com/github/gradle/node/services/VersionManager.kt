package com.github.gradle.node.services

import com.github.gradle.node.util.execute
import java.io.File

class VersionManager {
    companion object {
        fun checkNodeVersion(nodePath: File): String? {
            val result = execute(nodePath.absolutePath, "--version")
            if (result.startsWith("v")) {
                return result
            }

            return null
        }
    }
}