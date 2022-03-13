package com.github.gradle.node.services

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.util.PlatformHelper
import com.github.gradle.node.util.execute
import com.github.gradle.node.util.zip
import com.github.gradle.node.variant.VariantComputer
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import java.io.File
import java.util.*

abstract class NodeRuntime : BuildService<NodeRuntime.Params> {
    interface Params : BuildServiceParameters {
        val gradleUserHome: DirectoryProperty
    }

    fun getNode(extension: NodeExtension): File {
        val version = extension.version.get()
        val installed = findInstalledNode(version)
        if (installed.isPresent) {
            return installed.get()
        } else {
            if (!extension.download.get()) {
                throw NodeNotFoundException("No node installation matching requested version: $version found " +
                        "and download is set to false.")
            }
            val variant = VariantComputer()
            val nodeDir = variant.computeNodeDir(zip(parameters.gradleUserHome.dir("node"), extension.version))
            return nodeDir.get().asFile
        }
    }

    private fun checkNodeVersion(nodePath: File): String? {
        val result = execute(nodePath.absolutePath, "--version")
        if (result.startsWith("v")) {
            return result
        }

        return null
    }

    private fun getNodes(): MutableList<File> {
        val node = if (PlatformHelper.INSTANCE.isWindows) "node.exe" else "node"
        return PathDetector.findOnPath(node)
    }

    private fun findInstalledNode(version: String): Optional<File> {
        return getNodes().stream()
            .map { file -> Pair(file, checkNodeVersion(file)) }
            .filter { t -> t.second == "v$version" }
            .map { pair -> pair.first }
            .findAny()
    }
}