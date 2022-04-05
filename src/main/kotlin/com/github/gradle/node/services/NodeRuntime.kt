package com.github.gradle.node.services

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.util.PlatformHelper
import com.github.gradle.node.util.execute
import com.github.gradle.node.util.zip
import com.github.gradle.node.variant.VariantComputer
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import javax.inject.Inject

@Suppress("UnstableApiUsage")
abstract class NodeRuntime : BuildService<NodeRuntime.Params> {
    interface Params : BuildServiceParameters {
        val gradleUserHome: DirectoryProperty
    }

    private val client = OkHttpClient()

    @get:Inject
    abstract val archiveOperations: ArchiveOperations

    @get:Inject
    abstract val fileSystemOperations: FileSystemOperations

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
            val dir = getNodeDir(extension)
            installNode(dir, extension.distBaseUrl.get(), extension.version.get())
            return File(dir, if (PlatformHelper.INSTANCE.isWindows) "node" else "node.exe")
        }
    }

    @Synchronized
    private fun installNode(dir: File, url: String, version: String) {
        val ext = if (PlatformHelper.INSTANCE.isWindows) "zip" else "tar.gz"
        val name = "${dir.name}.$ext"
        val tmp = Paths.get(dir.parentFile.path, ".node-tmp", name).toFile()
        tmp.parentFile.mkdirs()

        val request = Request.Builder()
            .url("$url/v$version/$name")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val rsp = response.body ?: throw IOException("No response body")
            val sink = tmp.sink().buffer()
            sink.writeAll(rsp.source())
            sink.close()
        }

        fileSystemOperations.copy {
            from(archiveOperations.zipTree(tmp))
            into(dir.parentFile)
        }

        tmp.deleteOnExit()
    }

    private fun getNodeDir(extension: NodeExtension): File {
        val variant = VariantComputer()
        return variant.computeNodeDir(zip(parameters.gradleUserHome.dir("nodejs"), extension.version))
            .get().asFile
    }

    fun getNpm(extension: NodeExtension): File {
        val version = "" //extension.npmVersion.get()
        val installed = findInstalledNpm(version)
        if (installed.isPresent) {
            return installed.get()
        } else {
            if (!extension.download.get()) {
                throw NodeNotFoundException("No npm installation matching requested version: $version found " +
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

    private fun getNpms(): MutableList<File> {
        val npm = if (PlatformHelper.INSTANCE.isWindows) "npm.cmd" else "npm"
        return PathDetector.findOnPath(npm)
    }

    private fun findInstalledNpm(version: String): Optional<File> {
        return if (version.isBlank()) {
            getNpms().stream()
                .map { file -> Pair(file, checkNodeVersion(file)) }
                .map { pair -> pair.first }
                .findAny()
        } else {
            getNpms().stream()
                .map { file -> Pair(file, checkNodeVersion(file)) }
                .filter { t -> t.second == "v$version" }
                .map { pair -> pair.first }
                .findAny()
        }
    }

    private fun findInstalledNode(version: String): Optional<File> {
        return if (version.isBlank()) {
            getNodes().stream()
                .map { file -> Pair(file, checkNodeVersion(file)) }
                .map { pair -> pair.first }
                .findAny()
        } else {
            getNodes().stream()
                .map { file -> Pair(file, checkNodeVersion(file)) }
                .filter { t -> t.second == "v$version" }
                .map { pair -> pair.first }
                .findAny()
        }
    }
}